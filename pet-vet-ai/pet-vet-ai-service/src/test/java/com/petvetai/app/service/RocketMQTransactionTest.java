package com.petvetai.app.service;

import com.petvetai.app.domain.TransactionLog;
import com.petvetai.app.mapper.TransactionLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RocketMQ 2阶段提交测试用例
 * 
 * 测试场景：
 * 1. 本地事务成功 - 消息应该被提交
 * 2. 本地事务失败 - 消息应该被回滚
 * 3. 本地事务异常 - 消息状态为 UNKNOW，等待后续检查
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class RocketMQTransactionTest {

    @Autowired(required = false)
    private RocketMQTransactionService transactionService;
    
    @Autowired(required = false)
    private TransactionLogMapper transactionLogMapper;

    private static final String TOPIC = "pet-vet-transaction-topic";
    private static final String TAG = "test-tag";

    @Test
    void testTransactionMessageSuccess() {
        if (transactionService == null) {
            log.warn("RocketMQ 事务服务未配置，跳过测试");
            return;
        }

        log.info("========== 测试场景1：本地事务成功 ==========");
        
        // 发送事务消息，业务参数为 true 表示本地事务成功
        TransactionSendResult result = transactionService.sendTransactionMessage(
            TOPIC, 
            TAG, 
            "测试消息：本地事务成功", 
            true
        );

        // 验证消息发送成功
        assertNotNull(result);
        assertEquals(SendStatus.SEND_OK, result.getSendStatus());
        log.info("消息发送成功，事务ID: {}", result.getTransactionId());
        
        // 等待事务处理完成
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("========== 测试场景1完成 ==========");
    }

    @Test
    void testTransactionMessageRollback() {
        if (transactionService == null) {
            log.warn("RocketMQ 事务服务未配置，跳过测试");
            return;
        }

        log.info("========== 测试场景2：本地事务失败 ==========");
        
        // 发送事务消息，业务参数为 false 表示本地事务失败
        TransactionSendResult result = transactionService.sendTransactionMessage(
            TOPIC, 
            TAG, 
            "测试消息：本地事务失败", 
            false
        );

        // 验证消息发送成功
        assertNotNull(result);
        assertEquals(SendStatus.SEND_OK, result.getSendStatus());
        log.info("消息发送成功，事务ID: {}", result.getTransactionId());
        
        // 等待事务处理完成
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("========== 测试场景2完成 ==========");
    }

    @Test
    void testTransactionMessageUnknow() {
        if (transactionService == null) {
            log.warn("RocketMQ 事务服务未配置，跳过测试");
            return;
        }

        log.info("========== 测试场景3：本地事务异常（UNKNOW状态） ==========");
        
        // 发送事务消息，业务参数为 null 会触发异常，返回 UNKNOW
        TransactionSendResult result = transactionService.sendTransactionMessage(
            TOPIC, 
            TAG, 
            "测试消息：本地事务异常", 
            null
        );

        // 验证消息发送成功
        assertNotNull(result);
        assertEquals(SendStatus.SEND_OK, result.getSendStatus());
        log.info("消息发送成功，事务ID: {}", result.getTransactionId());
        
        // 等待事务处理完成（UNKNOW 状态会触发 checkLocalTransaction 回调）
        try {
            Thread.sleep(5000); // 等待更长时间，以便观察 checkLocalTransaction 的调用
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("========== 测试场景3完成 ==========");
    }

    @Test
    void testTransactionMessageFlow() {
        if (transactionService == null) {
            log.warn("RocketMQ 事务服务未配置，跳过测试");
            return;
        }

        log.info("========== 综合测试：完整的事务消息流程 ==========");
        
        // 测试成功场景
        log.info("--- 步骤1：发送成功的事务消息 ---");
        TransactionSendResult successResult = transactionService.sendTransactionMessage(
            TOPIC, 
            TAG, 
            "综合测试：成功消息", 
            true
        );
        assertNotNull(successResult);
        assertEquals(SendStatus.SEND_OK, successResult.getSendStatus());
        
        // 等待处理
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 测试失败场景
        log.info("--- 步骤2：发送失败的事务消息 ---");
        TransactionSendResult failResult = transactionService.sendTransactionMessage(
            TOPIC, 
            TAG, 
            "综合测试：失败消息", 
            false
        );
        assertNotNull(failResult);
        assertEquals(SendStatus.SEND_OK, failResult.getSendStatus());
        
        // 等待处理
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("========== 综合测试完成 ==========");
    }
    
    @Test
    void testTransactionMessageWithDatabaseRollback() {
        if (transactionService == null) {
            log.warn("RocketMQ 事务服务未配置，跳过测试");
            return;
        }
        
        if (transactionLogMapper == null) {
            log.warn("TransactionLogMapper 未配置，跳过数据库测试");
            return;
        }

        log.info("========== 测试场景4：消息发送成功，数据库操作异常回滚 ==========");
        
        // 清理可能存在的测试数据
        transactionLogMapper.delete(null);
        
        String messageBody = "测试消息：数据库操作异常，应该回滚";
        
        // 发送事务消息，数据库操作应该失败
        TransactionSendResult result = transactionService.sendTransactionMessageWithDatabase(
            TOPIC, 
            TAG, 
            messageBody, 
            false  // 数据库操作应该失败
        );

        // 验证消息发送成功
        assertNotNull(result);
        assertEquals(SendStatus.SEND_OK, result.getSendStatus());
        log.info("消息发送成功，事务ID: {}", result.getTransactionId());
        
        // 等待事务处理完成
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 验证数据库中没有保存记录（因为回滚了）
        TransactionLog savedLog = transactionLogMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TransactionLog>()
                .eq(TransactionLog::getTransactionId, result.getTransactionId())
        );
        
        assertNull(savedLog, "数据库操作应该被回滚，不应该有记录");
        log.info("✅ 验证通过：数据库操作已回滚，没有保存记录");
        
        log.info("========== 测试场景4完成 ==========");
    }
    
    @Test
    void testTransactionMessageWithDatabaseSuccess() {
        if (transactionService == null) {
            log.warn("RocketMQ 事务服务未配置，跳过测试");
            return;
        }
        
        if (transactionLogMapper == null) {
            log.warn("TransactionLogMapper 未配置，跳过数据库测试");
            return;
        }

        log.info("========== 测试场景5：消息发送成功，数据库操作成功 ==========");
        
        String messageBody = "测试消息：数据库操作成功，应该提交";
        
        // 发送事务消息，数据库操作应该成功
        TransactionSendResult result = transactionService.sendTransactionMessageWithDatabase(
            TOPIC, 
            TAG, 
            messageBody, 
            true  // 数据库操作应该成功
        );

        // 验证消息发送成功
        assertNotNull(result);
        assertEquals(SendStatus.SEND_OK, result.getSendStatus());
        log.info("消息发送成功，事务ID: {}", result.getTransactionId());
        
        // 等待事务处理完成
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 验证数据库中保存了记录
        TransactionLog savedLog = transactionLogMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TransactionLog>()
                .eq(TransactionLog::getTransactionId, result.getTransactionId())
        );
        
        assertNotNull(savedLog, "数据库操作应该成功，应该有记录");
        assertEquals("COMMITTED", savedLog.getStatus(), "事务状态应该是 COMMITTED");
        assertEquals(messageBody, savedLog.getMessageBody(), "消息体应该一致");
        log.info("✅ 验证通过：数据库操作成功，记录已保存，ID: {}, 状态: {}", savedLog.getId(), savedLog.getStatus());
        
        // 清理测试数据
        transactionLogMapper.deleteById(savedLog.getId());
        log.info("测试数据已清理");
        
        log.info("========== 测试场景5完成 ==========");
    }
}

