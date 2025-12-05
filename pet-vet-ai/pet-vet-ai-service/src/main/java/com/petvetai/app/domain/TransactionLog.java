package com.petvetai.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 事务日志实体 - 用于 RocketMQ 2阶段提交测试
 */
@Data
@NoArgsConstructor
@TableName("transaction_logs")
public class TransactionLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String transactionId;
    
    private String topic;
    
    private String tag;
    
    private String messageBody;
    
    private String status; // PENDING, COMMITTED, ROLLBACKED
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    public TransactionLog(String transactionId, String topic, String tag, String messageBody) {
        this.transactionId = transactionId;
        this.topic = topic;
        this.tag = tag;
        this.messageBody = messageBody;
        this.status = "PENDING";
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
}

