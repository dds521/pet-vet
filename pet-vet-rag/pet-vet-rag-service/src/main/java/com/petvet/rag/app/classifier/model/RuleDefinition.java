package com.petvet.rag.app.classifier.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则定义
 * 
 * 用于配置化规则表达式
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleDefinition {
    
    /**
     * 规则名称
     */
    private String name;
    
    /**
     * 规则优先级（数字越小优先级越高）
     */
    private Integer priority;
    
    /**
     * 规则表达式
     * 例如：lowerQuery.contains("疾病") || lowerQuery.contains("症状")
     */
    private String expression;
    
    /**
     * 规则匹配后的动作表达式
     * 例如：
     * result.setNeedRetrieval(false);
     * result.setConfidence(0.9);
     * result.setReason("识别为闲聊");
     * return true;
     */
    private String action;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 规则描述
     */
    private String description;
}
