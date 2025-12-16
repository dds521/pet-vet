package com.petvet.rag.app.classifier.strategy.impl;

import com.petvet.rag.app.classifier.config.ClassifierProperties;
import com.petvet.rag.app.classifier.engine.RuleEngine;
import com.petvet.rag.app.classifier.engine.RuleLoader;
import com.petvet.rag.app.classifier.model.ClassificationResult;
import com.petvet.rag.app.classifier.model.RuleDefinition;
import com.petvet.rag.app.classifier.strategy.ClassificationStrategy;
import com.petvet.rag.app.service.MemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 规则层策略
 * 使用QLExpress执行规则判断
 * 
 * 真正使用QLExpress规则引擎执行配置化的规则表达式
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RuleLayerStrategy implements ClassificationStrategy {
    
    private final ClassifierProperties properties;
    private final RuleEngine ruleEngine;
    private final RuleLoader ruleLoader;
    
    @Override
    public boolean matches(String query, MemoryService.ConversationMemory memory) {
        // 规则层总是匹配
        return properties.getRule().getEnabled() != null && properties.getRule().getEnabled();
    }
    
    @Override
    public ClassificationResult classify(String query, MemoryService.ConversationMemory memory) {
        if (properties.getRule().getEnabled() == null || !properties.getRule().getEnabled()) {
            return null;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 构建执行上下文（QLExpress 4.0.4使用Map作为上下文）
            Map<String, Object> context = ruleEngine.createContext();
            context.put("query", query);
            context.put("lowerQuery", query.toLowerCase());
            context.put("memory", memory);
            
            // 创建结果对象
            ClassificationResult result = ClassificationResult.builder()
                .needRetrieval(null)
                .confidence(0.0)
                .reason("")
                .strategyName("RuleLayerStrategy")
                .build();
            context.put("result", result);
            
            // 获取所有规则（已按优先级排序）
            List<RuleDefinition> rules = ruleLoader.getRules();
            
            // 按优先级顺序执行规则
            for (RuleDefinition rule : rules) {
                try {
                    // 1. 执行规则表达式，判断是否匹配
                    boolean matched = ruleEngine.executeBoolean(rule.getExpression(), context);
                    
                    if (matched) {
                        log.debug("规则 {} 匹配成功, 表达式: {}", rule.getName(), rule.getExpression());
                        
                        // 2. 执行规则动作
                        if (rule.getAction() != null && !rule.getAction().trim().isEmpty()) {
                            // 执行动作表达式（设置result属性等）
                            String actionExpression = rule.getAction();
                            Object actionResult = ruleEngine.execute(actionExpression, context);
                            
                            // 如果动作返回true，表示规则处理完成
                            if (actionResult instanceof Boolean && (Boolean) actionResult) {
                                long costTime = System.currentTimeMillis() - startTime;
                                result.setCostTime(costTime);
                                log.debug("规则 {} 执行完成, 结果: needRetrieval={}, confidence={}, reason={}, cost={}ms",
                                    rule.getName(), result.getNeedRetrieval(), result.getConfidence(),
                                    result.getReason(), costTime);
                                return result;
                            }
                        } else {
                            // 如果没有动作表达式，默认返回匹配结果
                            long costTime = System.currentTimeMillis() - startTime;
                            result.setCostTime(costTime);
                            log.debug("规则 {} 匹配但无动作表达式, 返回默认结果", rule.getName());
                            return result;
                        }
                    }
                } catch (Exception e) {
                    log.warn("规则 {} 执行失败, 继续下一个规则, 错误: {}", rule.getName(), e.getMessage());
                    // 继续执行下一个规则
                }
            }
            
            // 所有规则都不匹配，返回null继续下一个策略
            log.debug("所有规则都不匹配, query: {}", query);
            return null;
            
        } catch (Exception e) {
            log.warn("规则执行失败, query: {}", query, e);
            return null;
        }
    }
    
    
    @Override
    public int getPriority() {
        return 2; // 第二优先级
    }
    
    @Override
    public String getName() {
        return "RuleLayerStrategy";
    }
    
    @Override
    public boolean isEnabled() {
        return properties.getRule().getEnabled() != null && properties.getRule().getEnabled();
    }
}
