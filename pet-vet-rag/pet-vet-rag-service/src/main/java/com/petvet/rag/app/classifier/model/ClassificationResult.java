package com.petvet.rag.app.classifier.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类结果
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResult {
    
    /**
     * 是否需要检索
     */
    private Boolean needRetrieval;
    
    /**
     * 置信度 (0.0-1.0)
     */
    private Double confidence;
    
    /**
     * 决策原因
     */
    private String reason;
    
    /**
     * 使用的策略名称
     */
    private String strategyName;
    
    /**
     * 是否命中缓存
     */
    private Boolean cacheHit;
    
    /**
     * 处理耗时（毫秒）
     */
    private Long costTime;
}
