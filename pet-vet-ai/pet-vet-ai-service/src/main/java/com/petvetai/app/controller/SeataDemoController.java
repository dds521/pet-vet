package com.petvetai.app.controller;

import com.petvetai.app.service.seata.SeataAtService;
import com.petvetai.app.service.seata.SeataSagaService;
import com.petvetai.app.service.seata.SeataTccService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Seata 分布式事务演示 Controller
 * 
 * 提供三种模式的测试接口：
 * 1. AT 模式 - 自动模式，最简单
 * 2. TCC 模式 - 手动实现 try/confirm/cancel
 * 3. Saga 模式 - 状态机模式
 */
@Slf4j
@RestController
@RequestMapping("/api/seata")
public class SeataDemoController {

    @Autowired
    private SeataAtService seataAtService;
    
    @Autowired
    private SeataTccService seataTccService;
    
    @Autowired
    private SeataSagaService seataSagaService;

    /**
     * AT 模式测试：创建订单并扣减余额
     * 
     * GET /api/seata/at/create-order?petId=1&userId=1&amount=100
     */
    @GetMapping("/at/create-order")
    public ResponseEntity<Map<String, Object>> testAtMode(
            @RequestParam Long petId,
            @RequestParam Long userId,
            @RequestParam BigDecimal amount) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            seataAtService.createOrderAndDeductBalance(petId, userId, amount);
            result.put("success", true);
            result.put("message", "AT 模式：订单创建和扣减余额成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("AT 模式执行失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "AT 模式执行失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * AT 模式测试：模拟异常回滚
     * 
     * GET /api/seata/at/create-order-with-exception?petId=1&userId=1&amount=100
     */
    @GetMapping("/at/create-order-with-exception")
    public ResponseEntity<Map<String, Object>> testAtModeWithException(
            @RequestParam Long petId,
            @RequestParam Long userId,
            @RequestParam BigDecimal amount) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            seataAtService.createOrderWithException(petId, userId, amount);
            result.put("success", true);
            result.put("message", "不应该执行到这里");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.info("AT 模式异常回滚测试：{}", e.getMessage());
            result.put("success", true);
            result.put("message", "AT 模式异常回滚测试成功，事务已回滚");
            result.put("exception", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * TCC 模式测试：创建订单并扣减余额
     * 
     * GET /api/seata/tcc/create-order?petId=1&userId=1&amount=100
     */
    @GetMapping("/tcc/create-order")
    public ResponseEntity<Map<String, Object>> testTccMode(
            @RequestParam Long petId,
            @RequestParam Long userId,
            @RequestParam BigDecimal amount) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = seataTccService.tryCreateOrderAndDeductBalance(
                null, petId, userId, amount
            );
            result.put("success", success);
            result.put("message", "TCC 模式：Try 阶段执行成功，等待 Confirm 或 Cancel");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("TCC 模式执行失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "TCC 模式执行失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * Saga 模式测试：创建订单流程
     * 
     * GET /api/seata/saga/create-order?petId=1&amount=100
     */
    @GetMapping("/saga/create-order")
    public ResponseEntity<Map<String, Object>> testSagaMode(
            @RequestParam Long petId,
            @RequestParam BigDecimal amount) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            seataSagaService.createOrderWithSaga(petId, amount);
            result.put("success", true);
            result.put("message", "Saga 模式：订单创建流程完成");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Saga 模式执行失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "Saga 模式执行失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取 Seata 模式说明
     * 
     * GET /api/seata/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSeataInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("at", Map.of(
            "name", "AT 模式（自动模式）",
            "description", "最简单的分布式事务模式，只需要添加 @GlobalTransactional 注解",
            "特点", "自动处理事务提交和回滚，无需手动编写补偿逻辑",
            "适用场景", "大多数业务场景，特别是数据库操作"
        ));
        result.put("tcc", Map.of(
            "name", "TCC 模式（手动模式）",
            "description", "需要手动实现 Try、Confirm、Cancel 三个阶段",
            "特点", "精确控制事务，性能较高，但实现复杂",
            "适用场景", "需要精确控制、性能要求高的场景"
        ));
        result.put("saga", Map.of(
            "name", "Saga 模式（状态机模式）",
            "description", "通过状态机管理长事务，每个服务实现正向和补偿操作",
            "特点", "支持长时间运行的事务，最终一致性",
            "适用场景", "业务流程长、步骤多的场景"
        ));
        return ResponseEntity.ok(result);
    }
}

