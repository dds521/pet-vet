package com.petvetai.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体 - 用于 Seata 分布式事务演示
 */
@Data
@NoArgsConstructor
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long petId;
    
    private String orderNo;
    
    private BigDecimal amount;
    
    private String status; // PENDING, PAID, CANCELLED
    
    private LocalDateTime createTime;
    
    public Order(Long petId, String orderNo, BigDecimal amount) {
        this.petId = petId;
        this.orderNo = orderNo;
        this.amount = amount;
        this.status = "PENDING";
        this.createTime = LocalDateTime.now();
    }
}

