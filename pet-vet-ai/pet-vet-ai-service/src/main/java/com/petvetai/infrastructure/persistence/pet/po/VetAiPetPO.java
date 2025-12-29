package com.petvetai.infrastructure.persistence.pet.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petvet.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宠物持久化对象
 * 
 * 用于数据库持久化，对应 vet_ai_pet 表
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vet_ai_pet")
public class VetAiPetPO extends BaseEntity {
    
    /**
     * 主键ID，雪花算法生成（保证严格递增）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 宠物名称
     */
    private String name;
    
    /**
     * 宠物品种
     */
    private String breed;
    
    /**
     * 宠物年龄
     */
    private Integer age;
}

