package com.petvet.rag.api.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RAG 验证请求
 * 
 * @author daidasheng
 * @date 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagValidationReq {
    
    /**
     * 查询文本（用户问题）
     */
    @NotBlank(message = "查询文本不能为空")
    private String query;
    
    /**
     * 用户ID（必填）
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    /**
     * 会话ID（可选，如果不提供则自动生成）
     */
    private String sessionId;
    
    /**
     * 最大检索结果数（默认5）
     */
    private Integer maxResults;
    
    /**
     * 最小相似度分数（默认0.7）
     */
    private Double minScore;
    
    /**
     * 是否启用生成（默认true）
     */
    private Boolean enableGeneration;
    
    /**
     * 上下文窗口大小（默认5）
     */
    private Integer contextWindowSize;
    
    /**
     * 模型名称（可选，用于动态选择模型，如 deepseek、openai、grok）
     */
    private String modelName;
}
