package com.petvetai.app.service;

import com.petvetai.app.domain.TransactionLog;
import com.petvetai.app.mapper.TransactionLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * RocketMQ 事务消息服务
 * 
 * 2 阶段提交流程：
 * 1. 发送半消息（Half Message）到 Broker
 * 2. 执行本地事务
 * 3. 根据本地事务结果，提交或回滚消息
 */
@Slf4j
@Service
public class RocketMQTransactionService {

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    @Value("${rocketmq.producer.group:pet-vet-ai-transaction-producer-group}")
    private String producerGroup;

    @Autowired(required = false)
    private TransactionLogMapper transactionLogMapper;

    private TransactionMQProducer producer;

    // 用于存储本地事务状态
    private final ConcurrentHashMap<String, LocalTransactionState> transactionStateMap = new ConcurrentHashMap<>();
    
    // 用于存储事务业务参数
    private final ConcurrentHashMap<String, TransactionBusinessArg> transactionArgMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        producer = new TransactionMQProducer(producerGroup);
        producer.setNamesrvAddr(nameServer);
        
        // 设置事务监听器
        producer.setTransactionListener(new TransactionListener() {
            /**
             * 执行本地事务
             * 当半消息发送成功后，会回调此方法执行本地事务
             */
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                String transactionId = msg.getTransactionId();
                log.info("=== 2阶段提交 - 第1阶段：执行本地事务 ===");
                log.info("事务ID: {}, 消息: {}", transactionId, new String(msg.getBody(), StandardCharsets.UTF_8));
                
                try {
                    // 执行本地事务（数据库操作）
                    boolean success = executeLocalBusiness(msg, arg, transactionId);
                    
                    if (success) {
                        // 本地事务成功，返回 COMMIT_MESSAGE
                        transactionStateMap.put(transactionId, LocalTransactionState.COMMIT_MESSAGE);
                        log.info("本地事务执行成功，消息将被提交");
                        return LocalTransactionState.COMMIT_MESSAGE;
                    } else {
                        // 本地事务失败，返回 ROLLBACK_MESSAGE
                        transactionStateMap.put(transactionId, LocalTransactionState.ROLLBACK_MESSAGE);
                        log.info("本地事务执行失败，消息将被回滚");
                        return LocalTransactionState.ROLLBACK_MESSAGE;
                    }
                } catch (Exception e) {
                    // 本地事务异常，返回 UNKNOW，等待后续检查
                    log.error("本地事务执行异常，等待后续检查: {}", e.getMessage(), e);
                    transactionStateMap.put(transactionId, LocalTransactionState.UNKNOW);
                    return LocalTransactionState.UNKNOW;
                }
            }

            /**
             * 检查本地事务状态
             * 当 executeLocalTransaction 返回 UNKNOW 时，会定期回调此方法检查事务状态
             */
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                String transactionId = msg.getTransactionId();
                log.info("=== 2阶段提交 - 第2阶段：检查本地事务状态 ===");
                log.info("事务ID: {}", transactionId);
                
                LocalTransactionState state = transactionStateMap.get(transactionId);
                if (state != null) {
                    log.info("本地事务状态: {}", state);
                    return state;
                }
                
                // 如果找不到状态，说明事务可能还在执行中，返回 UNKNOW 继续等待
                log.info("本地事务状态未确定，继续等待");
                return LocalTransactionState.UNKNOW;
            }
        });

        // 设置线程池
        ExecutorService executorService = new ThreadPoolExecutor(
            2, 5, 100, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2000),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("rocketmq-transaction-thread");
                return thread;
            }
        );
        producer.setExecutorService(executorService);

        try {
            producer.start();
            log.info("RocketMQ 事务消息生产者启动成功");
        } catch (Exception e) {
            log.error("RocketMQ 事务消息生产者启动失败", e);
        }
    }

    /**
     * 执行本地业务逻辑
     * 
     * @param msg 消息
     * @param arg 业务参数
     * @param transactionId 事务ID
     * @return true 表示成功，false 表示失败
     */
    @Transactional(rollbackFor = Exception.class)
    private boolean executeLocalBusiness(Message msg, Object arg, String transactionId) {
        // 如果 arg 是 TransactionBusinessArg，执行数据库操作
        if (arg instanceof TransactionBusinessArg) {
            TransactionBusinessArg businessArg = (TransactionBusinessArg) arg;
            return executeDatabaseTransaction(msg, businessArg, transactionId);
        }
        
        // 兼容旧的测试方式：根据参数决定是否成功
        if (arg instanceof Boolean) {
            return (Boolean) arg;
        }
        
        // 如果 arg 为 null，模拟异常场景
        if (arg == null) {
            throw new RuntimeException("模拟本地事务异常");
        }
        
        // 默认返回成功
        return true;
    }
    
    /**
     * 执行数据库事务操作
     * 
     * @param msg 消息
     * @param businessArg 业务参数
     * @param transactionId 事务ID
     * @return true 表示成功，false 表示失败
     */
    private boolean executeDatabaseTransaction(Message msg, TransactionBusinessArg businessArg, String transactionId) {
        if (transactionLogMapper == null) {
            log.warn("TransactionLogMapper 未注入，跳过数据库操作");
            return businessArg.shouldSuccess();
        }
        
        try {
            String topic = msg.getTopic();
            String tag = msg.getTags();
            String messageBody = new String(msg.getBody(), StandardCharsets.UTF_8);
            
            // 保存事务日志到数据库
            TransactionLog transactionLog = new TransactionLog(transactionId, topic, tag, messageBody);
            transactionLogMapper.insert(transactionLog);
            log.info("事务日志已保存到数据库，ID: {}", transactionLog.getId());
            
            // 如果业务参数要求失败，抛出异常触发回滚
            if (!businessArg.shouldSuccess()) {
                log.info("模拟数据库操作异常，将触发回滚");
                throw new RuntimeException("数据库操作异常：模拟业务失败");
            }
            
            // 更新状态为已提交
            transactionLog.setStatus("COMMITTED");
            transactionLog.setUpdateTime(LocalDateTime.now());
            transactionLogMapper.updateById(transactionLog);
            log.info("数据库操作成功，事务日志已更新");
            
            return true;
        } catch (Exception e) {
            log.error("数据库操作失败，将触发回滚: {}", e.getMessage());
            // 异常会触发 Spring 事务回滚，数据库操作会被撤销
            throw e;
        }
    }

    /**
     * 发送事务消息
     * 
     * @param topic 主题
     * @param tag 标签
     * @param messageBody 消息体
     * @param businessArg 业务参数（用于决定本地事务是否成功）
     * @return 发送结果
     */
    public TransactionSendResult sendTransactionMessage(String topic, String tag, String messageBody, Object businessArg) {
        try {
            Message message = new Message(topic, tag, messageBody.getBytes(StandardCharsets.UTF_8));
            TransactionSendResult result = producer.sendMessageInTransaction(message, businessArg);
            log.info("事务消息发送结果: {}", result);
            
            // 如果是数据库操作类型，保存业务参数
            if (businessArg instanceof TransactionBusinessArg) {
                transactionArgMap.put(result.getTransactionId(), (TransactionBusinessArg) businessArg);
            }
            
            return result;
        } catch (Exception e) {
            log.error("发送事务消息失败", e);
            throw new RuntimeException("发送事务消息失败", e);
        }
    }
    
    /**
     * 发送带数据库操作的事务消息
     * 
     * @param topic 主题
     * @param tag 标签
     * @param messageBody 消息体
     * @param shouldSuccess 数据库操作是否应该成功
     * @return 发送结果
     */
    public TransactionSendResult sendTransactionMessageWithDatabase(String topic, String tag, String messageBody, boolean shouldSuccess) {
        TransactionBusinessArg businessArg = new TransactionBusinessArg(shouldSuccess);
        return sendTransactionMessage(topic, tag, messageBody, businessArg);
    }
    
    /**
     * 事务业务参数类
     */
    public static class TransactionBusinessArg {
        private final boolean shouldSuccess;
        
        public TransactionBusinessArg(boolean shouldSuccess) {
            this.shouldSuccess = shouldSuccess;
        }
        
        public boolean shouldSuccess() {
            return shouldSuccess;
        }
    }

    @PreDestroy
    public void destroy() {
        if (producer != null) {
            producer.shutdown();
            log.info("RocketMQ 事务消息生产者已关闭");
        }
    }
}

