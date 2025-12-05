package com.petvetai.app.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * RocketMQ 普通消费者
 * 
 * 用于演示消息消费失败后进入死信队列的场景
 */
@Slf4j
@Service
@RocketMQMessageListener(
    topic = "pet-vet-dlq-test-topic",  // 测试 Topic
    consumerGroup = "pet-vet-consumer-group",  // 消费者组（失败后会进入 %DLQ%pet-vet-consumer-group）
    consumeTimeout = 15000L,  // 消费超时时间（毫秒）
    maxReconsumeTimes = 3  // 最大重试次数（默认16次，这里设置为3次便于测试）
)
public class RocketMQNormalConsumer implements RocketMQListener<String> {

    private static int consumeCount = 0;

    @Override
    public void onMessage(String message) {
        consumeCount++;
        log.info("========== 收到消息（第{}次消费） ==========", consumeCount);
        log.info("消息内容: {}", message);
        
        // 模拟消费失败场景
        if (message.contains("FAIL")) {
            log.error("模拟消费失败，抛出异常");
            throw new RuntimeException("消息消费失败，将触发重试");
        }
        
        // 正常消费
        log.info("消息消费成功");
        log.info("========== 消息处理完成 ==========");
    }
}

