package com.petvet.embedding.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petvet.common.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 文本Chunk数据库实体类
 * 
 * 用于数据库持久化存储文本Chunk信息
 * 
 * @author daidasheng
 * @date 2024-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vet_embedding_text_chunk")
public class VetEmbeddingTextChunkEntity extends BaseEntity {
    
    /**
     * Chunk ID（主键，向量数据库中的ID）
     */
    @TableId(type = IdType.INPUT)
    private String chunkId;
    
    /**
     * 关联的简历ID
     */
    private String resumeId;
    
    /**
     * Chunk文本内容
     */
    private String text;
    
    /**
     * Chunk序号（在同一简历中的顺序）
     */
    private Integer sequence;
    
    /**
     * 所属字段类型（如：工作经历、教育背景、技能等）
     */
    private String fieldType;
    
    /**
     * 在原文中的起始位置
     */
    private Integer startPosition;
    
    /**
     * 在原文中的结束位置
     */
    private Integer endPosition;
}
