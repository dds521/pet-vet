package com.petvet.rag.app.service;

import com.petvet.rag.app.classifier.HybridClassificationOrchestrator;
import com.petvet.rag.app.classifier.config.ClassifierProperties;
import com.petvet.rag.app.classifier.model.ClassificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 混合查询分类器
 * 使用混合方案（缓存 + 规则引擎 + 兜底）进行分类
 * 保持与现有QueryClassifier接口兼容
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HybridQueryClassifier {
    
    private final HybridClassificationOrchestrator orchestrator;
    private final QueryClassifier originalClassifier; // 保留原有实现作为兜底
    private final ClassifierProperties properties;
    
    /**
     * 判断是否需要检索知识库
     * 兼容原有接口
     * 
     * @param query 用户查询
     * @param conversationHistory 对话历史（可选）
     * @return 是否需要检索
     */
    public boolean needRetrieval(String query, Object conversationHistory) {
        // 如果混合方案未启用，使用原有实现
        if (properties.getHybrid().getEnabled() == null || !properties.getHybrid().getEnabled()) {
            log.debug("混合方案未启用，使用原有实现");
            return originalClassifier.needRetrieval(query, conversationHistory);
        }
        
        try {
            // 转换对话历史
            MemoryService.ConversationMemory memory = null;
            if (conversationHistory instanceof MemoryService.ConversationMemory) {
                memory = (MemoryService.ConversationMemory) conversationHistory;
            }
            
            // 使用混合方案分类
            ClassificationResult result = orchestrator.classify(query, memory);
            
            if (result != null && result.getNeedRetrieval() != null) {
                log.debug("混合方案分类完成, query: {}, needRetrieval: {}, strategy: {}, confidence: {}, cost: {}ms",
                    query, result.getNeedRetrieval(), result.getStrategyName(), 
                    result.getConfidence(), result.getCostTime());
                return result.getNeedRetrieval();
            }
            
            // 如果混合方案返回null，降级到原有实现
            log.warn("混合方案返回null，降级到原有实现, query: {}", query);
            return originalClassifier.needRetrieval(query, conversationHistory);
            
        } catch (Exception e) {
            log.error("混合方案执行失败，降级到原有实现, query: {}", query, e);
            return originalClassifier.needRetrieval(query, conversationHistory);
        }
    }
    
    /**
     * 获取详细分类结果（扩展方法）
     * 
     * @param query 用户查询
     * @param conversationHistory 对话历史
     * @return 分类结果
     */
    public ClassificationResult classify(String query, Object conversationHistory) {
        // 如果混合方案未启用，返回null
        if (properties.getHybrid().getEnabled() == null || !properties.getHybrid().getEnabled()) {
            return null;
        }
        
        try {
            MemoryService.ConversationMemory memory = null;
            if (conversationHistory instanceof MemoryService.ConversationMemory) {
                memory = (MemoryService.ConversationMemory) conversationHistory;
            }
            
            return orchestrator.classify(query, memory);
            
        } catch (Exception e) {
            log.error("混合方案分类失败, query: {}", query, e);
            return null;
        }
    }
}
