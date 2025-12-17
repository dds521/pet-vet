package com.petvet.rag.app.domain;

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
 * RAG 查询历史记录实体类
 * 
 * 用于数据库持久化存储 RAG 查询历史记录
 * 
 * @author daidasheng
 * @date 2024-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vet_rag_query_history")
public class VetRagQueryHistoryEntity extends BaseEntity {
    
    /**
     * 查询ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 查询文本（用户问题）
     */
    private String query;
    
    /**
     * 检索到的文档数量
     */
    private Integer retrievedCount;
    
    /**
     * 是否启用了生成
     */
    private Boolean enableGeneration;
    
    /**
     * 生成的答案（如果启用）
     */
    private String generatedAnswer;
    
    /**
     * 使用的模型名称
     */
    private String modelName;
    
    /**
     * 查询耗时（毫秒）
     */
    private Long queryTime;
    
    /**
     * 会话ID（用于关联同一会话的多次查询）
     */
    private String sessionId;
    
    /**
     * 用户ID（如果有）
     */
    private String userId;
    
    /**
     * 检索到的文档（JSON格式）
     */
    private String retrievedDocuments;
    
    /**
     * 对话上下文（历史对话摘要，JSON格式）
     */
    private String conversationContext;
    
    /**
     * 置信度分数（0.0-1.0）
     */
    private Double confidence;
}
