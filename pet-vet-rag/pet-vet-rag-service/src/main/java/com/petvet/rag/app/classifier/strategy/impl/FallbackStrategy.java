package com.petvet.rag.app.classifier.strategy.impl;

import com.petvet.rag.app.classifier.config.ClassifierProperties;
import com.petvet.rag.app.classifier.model.ClassificationResult;
import com.petvet.rag.app.classifier.strategy.ClassificationStrategy;
import com.petvet.rag.app.service.MemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 兜底策略
 * 当所有策略都不匹配时，使用默认策略
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FallbackStrategy implements ClassificationStrategy {
    
    private final ClassifierProperties properties;
    
    @Value("${rag.retrieval.default-retrieval:true}")
    private Boolean defaultRetrieval;
    
    @Override
    public boolean matches(String query, MemoryService.ConversationMemory memory) {
        // 兜底策略总是匹配（但优先级最低）
        return properties.getFallback().getEnabled() != null && properties.getFallback().getEnabled();
    }
    
    @Override
    public ClassificationResult classify(String query, MemoryService.ConversationMemory memory) {
        if (properties.getFallback().getEnabled() == null || !properties.getFallback().getEnabled()) {
            return null;
        }
        
        boolean needRetrieval = defaultRetrieval != null ? defaultRetrieval : true;
        
        ClassificationResult result = ClassificationResult.builder()
            .needRetrieval(needRetrieval)
            .confidence(0.5) // 中等置信度
            .reason("使用默认策略")
            .strategyName("FallbackStrategy")
            .build();
        
        log.debug("使用兜底策略, query: {}, needRetrieval: {}", query, needRetrieval);
        return result;
    }
    
    @Override
    public int getPriority() {
        return 100; // 最低优先级
    }
    
    @Override
    public String getName() {
        return "FallbackStrategy";
    }
    
    @Override
    public boolean isEnabled() {
        return properties.getFallback().getEnabled() != null && properties.getFallback().getEnabled();
    }
}
