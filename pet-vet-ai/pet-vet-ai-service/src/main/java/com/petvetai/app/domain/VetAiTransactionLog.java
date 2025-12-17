package com.petvetai.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petvet.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 事务日志实体类
 * 
 * 用于 RocketMQ 2阶段提交测试，存储事务日志信息
 * 
 * @author daidasheng
 * @date 2024-12-19
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vet_ai_transaction_log")
public class VetAiTransactionLog extends BaseEntity {
    
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 事务ID
     */
    private String transactionId;
    
    /**
     * 消息主题
     */
    private String topic;
    
    /**
     * 消息标签
     */
    private String tag;
    
    /**
     * 消息体
     */
    private String messageBody;
    
    /**
     * 事务状态：PENDING-待提交, COMMITTED-已提交, ROLLBACKED-已回滚
     */
    private String status;
    
    /**
     * 构造函数
     * 
     * @param transactionId 事务ID
     * @param topic 消息主题
     * @param tag 消息标签
     * @param messageBody 消息体
     * @author daidasheng
     * @date 2024-12-19
     */
    public VetAiTransactionLog(String transactionId, String topic, String tag, String messageBody) {
        this.transactionId = transactionId;
        this.topic = topic;
        this.tag = tag;
        this.messageBody = messageBody;
        this.status = "PENDING";
    }
}

