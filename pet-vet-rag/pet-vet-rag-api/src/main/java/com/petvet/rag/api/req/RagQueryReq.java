package com.petvet.rag.api.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RAG 查询请求
 * 
 * @author PetVetRAG Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagQueryReq {
    
    /**
     * 查询文本（用户问题）
     */
    @NotBlank(message = "查询文本不能为空")
    private String query;
    
    /**
     * 最大检索结果数
     */
    private Integer maxResults;
    
    /**
     * 最小相似度分数（0.0-1.0）
     */
    private Double minScore;
    
    /**
     * 是否启用生成（使用LLM生成答案）
     */
    private Boolean enableGeneration;
    
    /**
     * 生成模型配置（可选，使用默认配置）
     */
    private String modelName;
    
    /**
     * 上下文窗口大小（用于生成答案时包含的上下文chunk数量）
     */
    private Integer contextWindowSize;
}
