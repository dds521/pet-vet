package com.petvetai.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体 - 用于 Seata 分布式事务演示
 */
@Data
@NoArgsConstructor
@TableName("accounts")
public class Account {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private BigDecimal balance;
    
    private LocalDateTime updateTime;
    
    public Account(Long userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
        this.updateTime = LocalDateTime.now();
    }
}

