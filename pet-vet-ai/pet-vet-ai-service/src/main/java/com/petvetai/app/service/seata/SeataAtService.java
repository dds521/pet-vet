package com.petvetai.app.service.seata;

import com.petvetai.app.domain.Account;
import com.petvetai.app.domain.Order;
import com.petvetai.app.mapper.AccountMapper;
import com.petvetai.app.mapper.OrderMapper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Seata AT 模式演示
 * 
 * AT 模式是最简单的分布式事务模式，只需要在方法上添加 @GlobalTransactional 注解
 * Seata 会自动处理事务的提交和回滚，无需手动编写补偿逻辑
 * 
 * 工作原理：
 * 1. 业务方法执行前，Seata 会拦截 SQL，解析 SQL 语义，保存数据快照（before image）
 * 2. 业务方法执行后，保存数据快照（after image），生成行锁
 * 3. 如果全局事务提交，释放行锁；如果回滚，使用 before image 恢复数据
 */
@Slf4j
@Service
public class SeataAtService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private AccountMapper accountMapper;

    /**
     * AT 模式示例：创建订单并扣减账户余额
     * 
     * @GlobalTransactional 注解会自动开启全局事务
     * 如果方法执行过程中抛出异常，所有操作都会自动回滚
     */
    @GlobalTransactional(name = "createOrderAndDeductBalance", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void createOrderAndDeductBalance(Long petId, Long userId, BigDecimal amount) {
        log.info("=== AT 模式：开始创建订单并扣减余额 ===");
        
        // 1. 创建订单
        String orderNo = "ORDER_" + UUID.randomUUID().toString().replace("-", "");
        Order order = new Order(petId, orderNo, amount);
        orderMapper.insert(order);
        log.info("创建订单成功：orderNo={}, amount={}", orderNo, amount);
        
        // 2. 查询账户
        Account account = accountMapper.selectById(userId);
        if (account == null) {
            throw new RuntimeException("账户不存在");
        }
        
        // 3. 检查余额
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("余额不足");
        }
        
        // 4. 扣减余额
        account.setBalance(account.getBalance().subtract(amount));
        accountMapper.updateById(account);
        log.info("扣减余额成功：userId={}, balance={}", userId, account.getBalance());
        
        log.info("=== AT 模式：订单创建和扣减余额完成 ===");
    }

    /**
     * AT 模式示例：模拟异常回滚
     * 这个方法会故意抛出异常，演示 Seata 的自动回滚功能
     */
    @GlobalTransactional(name = "createOrderWithException", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void createOrderWithException(Long petId, Long userId, BigDecimal amount) {
        log.info("=== AT 模式：开始创建订单（将抛出异常） ===");
        
        // 1. 创建订单
        String orderNo = "ORDER_" + UUID.randomUUID().toString().replace("-", "");
        Order order = new Order(petId, orderNo, amount);
        orderMapper.insert(order);
        log.info("创建订单成功：orderNo={}", orderNo);
        
        // 2. 故意抛出异常，触发全局事务回滚
        throw new RuntimeException("模拟业务异常，触发 Seata 自动回滚");
    }
}

