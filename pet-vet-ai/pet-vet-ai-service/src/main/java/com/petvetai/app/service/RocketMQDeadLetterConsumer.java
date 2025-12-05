package com.petvetai.app.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * RocketMQ 死信队列消费者
 * 
 * 死信队列说明：
 * 1. 当消息消费失败，达到最大重试次数（默认16次）后，会被发送到死信队列
 * 2. 死信队列的 Topic 名称格式：%DLQ%{ConsumerGroup}
 * 3. 例如：消费者组为 "pet-vet-consumer-group"，则死信队列为 "%DLQ%pet-vet-consumer-group"
 * 
 * 使用场景：
 * 1. 消息格式错误，无法解析
 * 2. 业务逻辑异常，重试多次仍失败
 * 3. 依赖的外部服务不可用，导致消费失败
 * 4. 需要人工介入处理的异常消息
 */
@Slf4j
@Service
@RocketMQMessageListener(
    topic = "%DLQ%pet-vet-consumer-group",  // 死信队列 Topic
    consumerGroup = "pet-vet-dlq-consumer-group"  // 死信队列消费者组
)
public class RocketMQDeadLetterConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.warn("========== 收到死信队列消息 ==========");
        log.warn("消息内容: {}", message);
        log.warn("处理方式：可以记录到数据库、发送告警、人工处理等");
        
        // 死信队列消息处理逻辑
        try {
            // 1. 记录到数据库，便于后续分析和处理
            log.info("死信消息已记录，等待人工处理");
            
            // 2. 可以发送告警通知
            log.info("发送告警通知：有消息进入死信队列");
            
            // 3. 可以尝试修复后重新发送（根据业务场景）
            // 例如：修复消息格式后，重新发送到原 Topic
            
        } catch (Exception e) {
            log.error("处理死信消息失败", e);
        }
        
        log.warn("========== 死信队列消息处理完成 ==========");
    }
}

