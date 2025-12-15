package com.petvet.rag.app.classifier.chain;

import com.petvet.rag.app.classifier.model.ClassificationResult;
import com.petvet.rag.app.classifier.strategy.ClassificationStrategy;
import com.petvet.rag.app.service.MemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 分类责任链
 * 按优先级顺序执行策略，直到找到匹配的策略
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Component
@Slf4j
public class ClassificationChain {
    
    private final List<ClassificationStrategy> strategies = new ArrayList<>();
    
    /**
     * 添加策略
     * 
     * @param strategy 策略
     */
    public void addStrategy(ClassificationStrategy strategy) {
        if (strategy != null && strategy.isEnabled()) {
            strategies.add(strategy);
            // 按优先级排序
            strategies.sort(Comparator.comparingInt(ClassificationStrategy::getPriority));
            log.debug("添加分类策略: {}, 优先级: {}", strategy.getName(), strategy.getPriority());
        }
    }
    
    /**
     * 执行分类
     * 
     * @param query 用户查询
     * @param memory 对话记忆
     * @return 分类结果
     */
    public ClassificationResult execute(String query, MemoryService.ConversationMemory memory) {
        if (query == null || query.trim().isEmpty()) {
            return ClassificationResult.builder()
                .needRetrieval(false)
                .confidence(0.0)
                .reason("查询为空")
                .strategyName("EmptyQuery")
                .build();
        }
        
        long startTime = System.currentTimeMillis();
        
        // 按优先级顺序执行策略
        for (ClassificationStrategy strategy : strategies) {
            try {
                // 检查是否匹配
                if (strategy.matches(query, memory)) {
                    // 执行分类
                    ClassificationResult result = strategy.classify(query, memory);
                    
                    if (result != null) {
                        long costTime = System.currentTimeMillis() - startTime;
                        result.setCostTime(costTime);
                        log.debug("策略 {} 匹配成功, 结果: needRetrieval={}, confidence={}, reason={}, cost={}ms",
                            strategy.getName(), result.getNeedRetrieval(), result.getConfidence(),
                            result.getReason(), costTime);
                        return result;
                    }
                }
            } catch (Exception e) {
                log.warn("策略 {} 执行失败，继续下一个策略", strategy.getName(), e);
                // 继续执行下一个策略
            }
        }
        
        // 所有策略都不匹配，返回null
        log.warn("所有策略都不匹配，查询: {}", query);
        return null;
    }
    
    /**
     * 获取策略列表
     * 
     * @return 策略列表
     */
    public List<ClassificationStrategy> getStrategies() {
        return new ArrayList<>(strategies);
    }
}
