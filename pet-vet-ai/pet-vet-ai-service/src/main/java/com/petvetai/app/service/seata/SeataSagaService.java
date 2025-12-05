package com.petvetai.app.service.seata;

import com.petvetai.app.domain.Order;
import com.petvetai.app.mapper.OrderMapper;
import io.seata.saga.engine.StateMachineEngine;
import io.seata.saga.statelang.domain.StateMachineInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Seata Saga 模式演示
 * 
 * Saga 模式是一种长事务解决方案，通过状态机来管理分布式事务
 * 每个服务实现自己的业务逻辑和补偿逻辑
 * 
 * 工作原理：
 * 1. 将长事务拆分为多个本地事务
 * 2. 每个本地事务都有对应的补偿操作
 * 3. 如果某个步骤失败，会按照相反顺序执行补偿操作
 * 
 * 适用场景：
 * - 业务流程长、步骤多
 * - 不需要强一致性，最终一致性即可
 * - 需要支持长时间运行的事务
 * 
 * 注意：Saga 模式需要配置状态机定义文件（JSON），这里提供简化示例
 */
@Slf4j
@Service
public class SeataSagaService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired(required = false)
    private StateMachineEngine stateMachineEngine;

    /**
     * Saga 模式示例：创建订单流程
     * 
     * 实际使用中，Saga 模式需要：
     * 1. 定义状态机 JSON 配置文件
     * 2. 实现各个服务的正向操作和补偿操作
     * 3. 使用 StateMachineEngine 执行状态机
     * 
     * 这里提供一个简化的示例，展示 Saga 模式的基本思路
     */
    public void createOrderWithSaga(Long petId, BigDecimal amount) {
        log.info("=== Saga 模式：开始创建订单流程 ===");
        
        // 实际 Saga 模式需要状态机配置，这里展示简化流程
        try {
            // Step 1: 创建订单
            String orderNo = createOrderStep(petId, amount);
            log.info("Saga Step 1: 创建订单成功，orderNo={}", orderNo);
            
            // Step 2: 扣减库存（如果有库存服务）
            // deductInventoryStep(orderNo);
            
            // Step 3: 扣减余额
            // deductBalanceStep(orderNo);
            
            // Step 4: 发送通知
            // sendNotificationStep(orderNo);
            
            log.info("=== Saga 模式：订单创建流程完成 ===");
            
        } catch (Exception e) {
            log.error("Saga 模式执行失败，开始补偿：{}", e.getMessage());
            // Saga 模式会自动执行补偿操作
            // 补偿顺序与执行顺序相反
        }
    }

    /**
     * Saga Step 1: 创建订单
     * 如果后续步骤失败，需要提供补偿方法：cancelOrderStep
     */
    private String createOrderStep(Long petId, BigDecimal amount) {
        String orderNo = "ORDER_" + UUID.randomUUID().toString().replace("-", "");
        Order order = new Order(petId, orderNo, amount);
        order.setStatus("PENDING");
        orderMapper.insert(order);
        return orderNo;
    }

    /**
     * Saga 补偿操作：取消订单
     */
    private void cancelOrderStep(String orderNo) {
        log.info("Saga 补偿：取消订单，orderNo={}", orderNo);
        // 实现取消订单的逻辑
    }

    /**
     * 使用状态机引擎执行 Saga（需要配置状态机 JSON）
     * 
     * 状态机 JSON 配置示例（需要放在 resources/statelang/ 目录下）：
     * {
     *   "Name": "createOrderSaga",
     *   "Comment": "创建订单 Saga",
     *   "StartState": "CreateOrder",
     *   "States": {
     *     "CreateOrder": {
     *       "Type": "ServiceTask",
     *       "ServiceName": "orderService",
     *       "ServiceMethod": "createOrder",
     *       "CompensateState": "CancelOrder",
     *       "Next": "DeductBalance"
     *     },
     *     "DeductBalance": {
     *       "Type": "ServiceTask",
     *       "ServiceName": "accountService",
     *       "ServiceMethod": "deductBalance",
     *       "CompensateState": "RefundBalance",
     *       "Next": "Success"
     *     },
     *     "CancelOrder": {
     *       "Type": "ServiceTask",
     *       "ServiceName": "orderService",
     *       "ServiceMethod": "cancelOrder"
     *     },
     *     "RefundBalance": {
     *       "Type": "ServiceTask",
     *       "ServiceName": "accountService",
     *       "ServiceMethod": "refundBalance"
     *     },
     *     "Success": {
     *       "Type": "Succeed"
     *     }
     *   }
     * }
     */
    public void executeSagaWithStateMachine(Long petId, BigDecimal amount) {
        if (stateMachineEngine == null) {
            log.warn("StateMachineEngine 未配置，无法执行 Saga 状态机");
            return;
        }
        
        Map<String, Object> startParams = new HashMap<>();
        startParams.put("petId", petId);
        startParams.put("amount", amount);
        
        try {
            StateMachineInstance instance = stateMachineEngine.start(
                "createOrderSaga", 
                null, 
                startParams
            );
            log.info("Saga 状态机执行完成，instanceId={}", instance.getId());
        } catch (Exception e) {
            log.error("Saga 状态机执行失败：{}", e.getMessage());
            throw e;
        }
    }
}

