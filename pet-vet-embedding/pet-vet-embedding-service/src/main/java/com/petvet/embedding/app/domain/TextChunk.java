package com.petvet.embedding.app.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 文本Chunk领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextChunk {
    /**
     * Chunk ID（向量数据库中的ID）
     */
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
    
    /**
     * 元数据（JSON格式，存储额外信息）
     */
    private Map<String, Object> metadata;
}
