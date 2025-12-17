package com.petvetai.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petvet.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 宠物实体类
 * 
 * 用于存储宠物基本信息
 * 
 * @author daidasheng
 * @date 2024-12-19
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vet_ai_pet")
public class VetAiPet extends BaseEntity {
    
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 症状列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<VetAiSymptom> symptoms;

    /**
     * 构造函数
     * 
     * @param name 宠物名称
     * @param breed 宠物品种
     * @param age 宠物年龄
     * @author daidasheng
     * @date 2024-12-19
     */
    public VetAiPet(String name, String breed, Integer age) {
        this.name = name;
        this.breed = breed;
        this.age = age;
    }
}
