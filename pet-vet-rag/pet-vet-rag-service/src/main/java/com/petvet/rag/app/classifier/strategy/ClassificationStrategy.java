package com.petvet.rag.app.classifier.strategy;

import com.petvet.rag.app.classifier.model.ClassificationResult;
import com.petvet.rag.app.service.MemoryService;

/**
 * 分类策略接口
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
public interface ClassificationStrategy {
    
    /**
     * 判断是否匹配该策略
     * 
     * @param query 用户查询
     * @param memory 对话记忆
     * @return 是否匹配
     */
    boolean matches(String query, MemoryService.ConversationMemory memory);
    
    /**
     * 执行分类判断
     * 
     * @param query 用户查询
     * @param memory 对话记忆
     * @return 分类结果，如果返回null表示不匹配，继续下一个策略
     */
    ClassificationResult classify(String query, MemoryService.ConversationMemory memory);
    
    /**
     * 策略优先级（数字越小优先级越高）
     * 
     * @return 优先级
     */
    int getPriority();
    
    /**
     * 策略名称
     * 
     * @return 策略名称
     */
    String getName();
    
    /**
     * 是否启用
     * 
     * @return 是否启用
     */
    default boolean isEnabled() {
        return true;
    }
}
