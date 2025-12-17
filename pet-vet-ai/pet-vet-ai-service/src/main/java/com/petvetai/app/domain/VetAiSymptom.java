package com.petvetai.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petvet.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 症状实体类
 * 
 * 用于存储宠物症状信息
 * 
 * @author daidasheng
 * @date 2024-12-19
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vet_ai_symptom")
public class VetAiSymptom extends BaseEntity {
    
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 症状描述
     */
    private String description;

    /**
     * 宠物ID
     */
    private Long petId;

    /**
     * 报告时间
     */
    private java.time.LocalDateTime reportedAt;

    /**
     * 构造函数
     * 
     * @param description 症状描述
     * @param petId 宠物ID
     * @author daidasheng
     * @date 2024-12-19
     */
    public VetAiSymptom(String description, Long petId) {
        this.description = description;
        this.petId = petId;
        this.reportedAt = java.time.LocalDateTime.now();
    }
}
