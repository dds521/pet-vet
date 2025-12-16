package com.petvet.rag.app.classifier;

import com.petvet.rag.app.classifier.chain.ClassificationChain;
import com.petvet.rag.app.classifier.model.ClassificationResult;
import com.petvet.rag.app.classifier.strategy.impl.CacheLayerStrategy;
import com.petvet.rag.app.classifier.strategy.impl.RedisCacheLayerStrategy;
import com.petvet.rag.app.service.MemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 混合分类编排器
 * 协调各个策略，处理缓存更新
 * 支持 Redis 分布式缓存和本地缓存
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HybridClassificationOrchestrator {
    
    private final ClassificationChain classificationChain;
    private final CacheLayerStrategy cacheLayerStrategy;
    
    @Autowired(required = false)
    private RedisCacheLayerStrategy redisCacheLayerStrategy;
    
    /**
     * 初始化策略链
     * @date 2024-12-15
     * @author daidasheng
     */
    @PostConstruct
    public void init() {
        // 策略已通过Spring自动注入到责任链中
        // 这里可以添加额外的初始化逻辑
        log.info("混合分类编排器初始化完成，策略数量: {}", classificationChain.getStrategies().size());
    }
    
    /**
     * 执行分类
     * 
     * @param query 用户查询
     * @param memory 对话记忆
     * @return 分类结果
     * @date 2024-12-15
     * @author daidasheng
     */
    public ClassificationResult classify(String query, MemoryService.ConversationMemory memory) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 执行责任链分类
            ClassificationResult result = classificationChain.execute(query, memory);
            
            // 2. 如果结果不为空，更新缓存（异步）
            if (result != null && !Boolean.TRUE.equals(result.getCacheHit())) {
                // 只有高置信度的结果才缓存
                if (result.getConfidence() != null && result.getConfidence() >= 0.8) {
                    // 优先使用 Redis 缓存策略，如果未启用则使用本地缓存策略
                    if (redisCacheLayerStrategy != null && redisCacheLayerStrategy.isEnabled()) {
                        redisCacheLayerStrategy.cacheResult(query, result);
                    } else {
                        cacheLayerStrategy.cacheResult(query, result);
                    }
                }
            }
            
            // 3. 如果所有策略都不匹配，返回null（由调用方处理）
            if (result == null) {
                log.warn("所有策略都不匹配，查询: {}", query);
            }
            
            long totalCost = System.currentTimeMillis() - startTime;
            if (result != null && result.getCostTime() == null) {
                result.setCostTime(totalCost);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("分类执行失败, query: {}", query, e);
            // 返回兜底结果
            return ClassificationResult.builder()
                .needRetrieval(true)
                .confidence(0.3)
                .reason("分类执行异常: " + e.getMessage())
                .strategyName("ErrorFallback")
                .costTime(System.currentTimeMillis() - startTime)
                .build();
        }
    }
}
