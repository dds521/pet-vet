package com.petvetai.infrastructure.persistence.transaction.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petvet.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 事务日志持久化对象
 * 
 * 用于数据库持久化，对应 vet_ai_transaction_log 表
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vet_ai_transaction_log")
public class VetAiTransactionLogPO extends BaseEntity {
    
    /**
     * 主键ID，雪花算法生成（保证严格递增）
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 事务状态：PENDING, COMMITTED, ROLLBACKED
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

