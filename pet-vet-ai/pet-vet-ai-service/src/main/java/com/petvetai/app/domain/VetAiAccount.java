package com.petvetai.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petvet.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 账户实体类
 * 
 * 用于 Seata 分布式事务演示，存储用户账户余额信息
 * 
 * @author daidasheng
 * @date 2024-12-19
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vet_ai_account")
public class VetAiAccount extends BaseEntity {
    
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 账户余额
     */
    private BigDecimal balance;
    
    /**
     * 构造函数
     * 
     * @param userId 用户ID
     * @param balance 账户余额
     * @author daidasheng
     * @date 2024-12-19
     */
    public VetAiAccount(Long userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
    }
}

