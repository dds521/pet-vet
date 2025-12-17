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
 * 订单实体类
 * 
 * 用于 Seata 分布式事务演示，存储订单信息
 * 
 * @author daidasheng
 * @date 2024-12-19
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vet_ai_order")
public class VetAiOrder extends BaseEntity {
    
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 宠物ID
     */
    private Long petId;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 订单金额
     */
    private BigDecimal amount;
    
    /**
     * 订单状态：PENDING-待支付, PAID-已支付, CANCELLED-已取消
     */
    private String status;
    
    /**
     * 构造函数
     * 
     * @param petId 宠物ID
     * @param orderNo 订单号
     * @param amount 订单金额
     * @author daidasheng
     * @date 2024-12-19
     */
    public VetAiOrder(Long petId, String orderNo, BigDecimal amount) {
        this.petId = petId;
        this.orderNo = orderNo;
        this.amount = amount;
        this.status = "PENDING";
    }
}

