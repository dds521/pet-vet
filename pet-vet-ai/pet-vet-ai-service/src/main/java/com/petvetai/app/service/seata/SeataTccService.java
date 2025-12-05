package com.petvetai.app.service.seata;

import com.petvetai.app.domain.Account;
import com.petvetai.app.domain.Order;
import com.petvetai.app.mapper.AccountMapper;
import com.petvetai.app.mapper.OrderMapper;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Seata TCC 模式演示
 * 
 * TCC 模式需要手动实现三个阶段：
 * 1. Try：尝试执行，完成所有业务检查，预留业务资源
 * 2. Confirm：确认执行，使用 Try 阶段预留的资源，执行业务
 * 3. Cancel：取消执行，释放 Try 阶段预留的资源
 * 
 * 适用场景：
 * - 需要精确控制事务的提交和回滚
 * - 业务逻辑复杂，需要自定义补偿逻辑
 * - 对性能要求较高的场景
 */
@Slf4j
@Service
@LocalTCC
public class SeataTccService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private AccountMapper accountMapper;

    /**
     * TCC 模式示例：创建订单并扣减余额
     * 
     * @TwoPhaseBusinessAction 注解定义 TCC 方法
     * - name: 事务名称
     * - commitMethod: Confirm 方法名
     * - rollbackMethod: Cancel 方法名
     */
    @TwoPhaseBusinessAction(
        name = "tccCreateOrderAndDeductBalance",
        commitMethod = "confirmCreateOrderAndDeductBalance",
        rollbackMethod = "cancelCreateOrderAndDeductBalance"
    )
    public boolean tryCreateOrderAndDeductBalance(
            BusinessActionContext context,
            @BusinessActionContextParameter(paramName = "petId") Long petId,
            @BusinessActionContextParameter(paramName = "userId") Long userId,
            @BusinessActionContextParameter(paramName = "amount") BigDecimal amount) {
        
        log.info("=== TCC Try 阶段：开始预留资源 ===");
        
        try {
            // Try 阶段：完成业务检查，预留资源
            // 1. 检查账户余额
            Account account = accountMapper.selectById(userId);
            if (account == null) {
                throw new RuntimeException("账户不存在");
            }
            if (account.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("余额不足");
            }
            
            // 2. 冻结余额（预留资源）
            BigDecimal frozenAmount = amount;
            // 这里可以将冻结金额保存到单独的冻结表，或者使用账户的冻结字段
            // 为了简化，我们使用 context 保存冻结金额
            Map<String, Object> actionContext = context.getActionContext();
            if (actionContext == null) {
                actionContext = new HashMap<>();
            }
            actionContext.put("frozenAmount", frozenAmount);
            actionContext.put("originalBalance", account.getBalance());
            
            // 3. 创建订单（状态为 PENDING，等待确认）
            String orderNo = "ORDER_" + UUID.randomUUID().toString().replace("-", "");
            Order order = new Order(petId, orderNo, amount);
            order.setStatus("PENDING"); // 待确认状态
            orderMapper.insert(order);
            
            actionContext.put("orderId", order.getId());
            actionContext.put("orderNo", orderNo);
            context.setActionContext(actionContext);
            
            log.info("TCC Try 阶段完成：orderNo={}, frozenAmount={}", orderNo, frozenAmount);
            return true;
            
        } catch (Exception e) {
            log.error("TCC Try 阶段失败：{}", e.getMessage());
            throw e;
        }
    }

    /**
     * TCC Confirm 阶段：确认执行，使用 Try 阶段预留的资源
     */
    public boolean confirmCreateOrderAndDeductBalance(BusinessActionContext context) {
        log.info("=== TCC Confirm 阶段：确认执行 ===");
        
        try {
            Map<String, Object> actionContext = context.getActionContext();
            if (actionContext == null) {
                log.error("TCC Confirm 阶段失败：actionContext 为空");
                return false;
            }
            
            Long orderId = (Long) actionContext.get("orderId");
            String orderNo = (String) actionContext.get("orderNo");
            Object userIdObj = actionContext.get("userId");
            Object frozenAmountObj = actionContext.get("frozenAmount");
            
            if (orderId == null || orderNo == null || userIdObj == null || frozenAmountObj == null) {
                log.error("TCC Confirm 阶段失败：必要的参数为空");
                return false;
            }
            
            Long userId = Long.valueOf(userIdObj.toString());
            BigDecimal frozenAmount = new BigDecimal(frozenAmountObj.toString());
            
            // 1. 更新订单状态为已支付
            Order order = orderMapper.selectById(orderId);
            if (order != null) {
                order.setStatus("PAID");
                orderMapper.updateById(order);
                log.info("订单状态更新为已支付：orderNo={}", orderNo);
            }
            
            // 2. 扣减账户余额（使用 Try 阶段冻结的金额）
            Account account = accountMapper.selectById(userId);
            if (account != null) {
                account.setBalance(account.getBalance().subtract(frozenAmount));
                accountMapper.updateById(account);
                log.info("扣减余额成功：userId={}, amount={}, balance={}", 
                    userId, frozenAmount, account.getBalance());
            }
            
            log.info("TCC Confirm 阶段完成：orderNo={}", orderNo);
            return true;
            
        } catch (Exception e) {
            log.error("TCC Confirm 阶段失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * TCC Cancel 阶段：取消执行，释放 Try 阶段预留的资源
     */
    public boolean cancelCreateOrderAndDeductBalance(BusinessActionContext context) {
        log.info("=== TCC Cancel 阶段：取消执行 ===");
        
        try {
            Map<String, Object> actionContext = context.getActionContext();
            if (actionContext == null) {
                log.error("TCC Cancel 阶段失败：actionContext 为空");
                return false;
            }
            
            Long orderId = (Long) actionContext.get("orderId");
            String orderNo = (String) actionContext.get("orderNo");
            
            // 1. 更新订单状态为已取消
            if (orderId != null) {
                Order order = orderMapper.selectById(orderId);
                if (order != null) {
                    order.setStatus("CANCELLED");
                    orderMapper.updateById(order);
                    log.info("订单状态更新为已取消：orderNo={}", orderNo);
                }
            }
            
            // 2. 释放冻结的余额（在 Try 阶段已经检查过余额，这里只需要释放冻结）
            // 由于我们使用的是简化版本，这里只需要记录日志
            log.info("释放冻结余额：orderNo={}", orderNo);
            
            log.info("TCC Cancel 阶段完成：orderNo={}", orderNo);
            return true;
            
        } catch (Exception e) {
            log.error("TCC Cancel 阶段失败：{}", e.getMessage());
            return false;
        }
    }
}

