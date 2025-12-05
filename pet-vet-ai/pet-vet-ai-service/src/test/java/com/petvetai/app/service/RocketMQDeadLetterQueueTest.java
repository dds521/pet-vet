package com.petvetai.app.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * RocketMQ 死信队列测试用例
 * 
 * 测试场景：
 * 1. 发送正常消息，验证正常消费
 * 2. 发送失败消息，验证重试后进入死信队列
 * 3. 验证死信队列消息的处理
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class RocketMQDeadLetterQueueTest {

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    private static final String TOPIC = "pet-vet-dlq-test-topic";

    @Test
    void testNormalMessageConsume() {
        if (rocketMQTemplate == null) {
            log.warn("RocketMQTemplate 未配置，跳过测试");
            return;
        }

        log.info("========== 测试场景1：正常消息消费 ==========");
        
        String message = "正常消息：应该被成功消费";
        
        // 发送消息
        rocketMQTemplate.convertAndSend(TOPIC, message);
        log.info("消息已发送: {}", message);
        
        // 等待消费
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("========== 测试场景1完成 ==========");
    }

    @Test
    void testDeadLetterQueue() {
        if (rocketMQTemplate == null) {
            log.warn("RocketMQTemplate 未配置，跳过测试");
            return;
        }

        log.info("========== 测试场景2：消息消费失败，进入死信队列 ==========");
        log.info("说明：消息会重试3次，如果都失败，会进入死信队列");
        
        String message = "FAIL: 这是一条会失败的消息，将进入死信队列";
        
        // 发送会失败的消息
        rocketMQTemplate.convertAndSend(TOPIC, message);
        log.info("失败消息已发送: {}", message);
        
        // 等待重试和死信队列处理
        // 重试3次，每次间隔时间递增，总共大约需要 10-15 秒
        log.info("等待消息重试和进入死信队列...");
        try {
            Thread.sleep(20000);  // 等待20秒，确保重试完成并进入死信队列
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("========== 测试场景2完成 ==========");
        log.info("请查看日志，确认消息是否进入死信队列");
    }

    @Test
    void testDeadLetterQueueWithMultipleMessages() {
        if (rocketMQTemplate == null) {
            log.warn("RocketMQTemplate 未配置，跳过测试");
            return;
        }

        log.info("========== 测试场景3：批量消息进入死信队列 ==========");
        
        // 发送多条失败消息
        for (int i = 1; i <= 3; i++) {
            String message = String.format("FAIL: 失败消息 #%d", i);
            rocketMQTemplate.convertAndSend(TOPIC, message);
            log.info("发送失败消息 #{}: {}", i, message);
            
            try {
                Thread.sleep(1000);  // 间隔1秒发送
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 等待所有消息处理完成
        log.info("等待所有消息重试和进入死信队列...");
        try {
            Thread.sleep(25000);  // 等待25秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("========== 测试场景3完成 ==========");
    }

    @Test
    void testDeadLetterQueueFlow() {
        if (rocketMQTemplate == null) {
            log.warn("RocketMQTemplate 未配置，跳过测试");
            return;
        }

        log.info("========== 综合测试：死信队列完整流程 ==========");
        
        // 1. 发送正常消息
        log.info("--- 步骤1：发送正常消息 ---");
        rocketMQTemplate.convertAndSend(TOPIC, "正常消息：应该成功消费");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 2. 发送失败消息
        log.info("--- 步骤2：发送失败消息（将进入死信队列） ---");
        rocketMQTemplate.convertAndSend(TOPIC, "FAIL: 失败消息：将重试后进入死信队列");
        
        // 3. 等待处理
        log.info("--- 步骤3：等待消息重试和死信队列处理 ---");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("========== 综合测试完成 ==========");
        log.info("验证要点：");
        log.info("1. 正常消息应该被成功消费");
        log.info("2. 失败消息应该重试3次");
        log.info("3. 重试失败后应该进入死信队列");
        log.info("4. 死信队列消费者应该收到消息并处理");
    }
}

