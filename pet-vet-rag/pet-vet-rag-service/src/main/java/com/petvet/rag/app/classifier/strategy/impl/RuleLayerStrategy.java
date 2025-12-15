package com.petvet.rag.app.classifier.strategy.impl;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;
import com.petvet.rag.app.classifier.config.ClassifierProperties;
import com.petvet.rag.app.classifier.model.ClassificationResult;
import com.petvet.rag.app.classifier.strategy.ClassificationStrategy;
import com.petvet.rag.app.service.MemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 规则层策略
 * 使用QLExpress执行规则判断
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RuleLayerStrategy implements ClassificationStrategy {
    
    private final ClassifierProperties properties;
    
    @Value("${rag.retrieval.force-retrieval-keywords:疾病,症状,诊断,治疗,疫苗,感染,炎症,手术,药物}")
    private String forceRetrievalKeywords;
    
    @Value("${rag.retrieval.skip-retrieval-keywords:你好,谢谢,再见,哈哈}")
    private String skipRetrievalKeywords;
    
    /**
     * 构造函数
     * 注意：当前版本为简单实现，规则逻辑硬编码，未使用QLExpress脚本执行
     * 后续版本可以将规则配置为QLExpress脚本，通过ExpressRunner.execute()执行
     * 
     * @author daidasheng
     * @date 2024-12-15
     */
    public RuleLayerStrategy() {
        // 当前版本不需要初始化ExpressRunner
        // 后续扩展时，可以在这里初始化：
        // ExpressRunner expressRunner = new ExpressRunner();
        // 然后通过 expressRunner.execute(ruleScript, context, null, true, false) 执行规则脚本
    }
    
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
            // 构建上下文（QLExpress使用DefaultContext）
            IExpressContext<String, Object> context = new DefaultContext<>();
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
            
            // 执行规则（按优先级顺序）
            // 1. 闲聊规则
            if (executeCasualChatRule(query, context, result)) {
                long costTime = System.currentTimeMillis() - startTime;
                result.setCostTime(costTime);
                return result;
            }
            
            // 2. 强制检索规则
            if (executeForceRetrievalRule(query, context, result)) {
                long costTime = System.currentTimeMillis() - startTime;
                result.setCostTime(costTime);
                return result;
            }
            
            // 3. 通用知识规则
            if (executeGeneralKnowledgeRule(query, context, result)) {
                long costTime = System.currentTimeMillis() - startTime;
                result.setCostTime(costTime);
                return result;
            }
            
            // 规则不匹配，返回null继续下一个策略
            return null;
            
        } catch (Exception e) {
            log.warn("规则执行失败, query: {}", query, e);
            return null;
        }
    }
    
    /**
     * 执行闲聊规则
     */
    private boolean executeCasualChatRule(String query, IExpressContext<String, Object> context, 
                                         ClassificationResult result) throws Exception {
        List<String> chatKeywords = Arrays.asList(
            skipRetrievalKeywords != null ? skipRetrievalKeywords.split(",") : 
            new String[]{"你好", "谢谢", "再见", "哈哈"}
        );
        
        String lowerQuery = query.toLowerCase();
        for (String keyword : chatKeywords) {
            if (lowerQuery.contains(keyword.toLowerCase().trim())) {
                result.setNeedRetrieval(false);
                result.setConfidence(0.9);
                result.setReason("识别为闲聊: " + keyword);
                log.debug("闲聊规则匹配: {}", keyword);
                return true;
            }
        }
        return false;
    }
    
    /**
     * 执行强制检索规则
     */
    private boolean executeForceRetrievalRule(String query, IExpressContext<String, Object> context,
                                             ClassificationResult result) throws Exception {
        List<String> domainTerms = Arrays.asList(
            forceRetrievalKeywords != null ? forceRetrievalKeywords.split(",") :
            new String[]{"疾病", "症状", "诊断", "治疗", "疫苗", "感染", "炎症", "手术", "药物"}
        );
        
        String lowerQuery = query.toLowerCase();
        for (String term : domainTerms) {
            if (lowerQuery.contains(term.toLowerCase().trim())) {
                result.setNeedRetrieval(true);
                result.setConfidence(0.95);
                result.setReason("包含领域关键词: " + term);
                log.debug("强制检索规则匹配: {}", term);
                return true;
            }
        }
        return false;
    }
    
    /**
     * 执行通用知识规则
     */
    private boolean executeGeneralKnowledgeRule(String query, IExpressContext<String, Object> context,
                                               ClassificationResult result) throws Exception {
        String[] generalPatterns = {
            "什么是", "介绍一下", "简单说", "解释一下"
        };
        
        String lowerQuery = query.toLowerCase();
        for (String pattern : generalPatterns) {
            if (lowerQuery.contains(pattern)) {
                result.setNeedRetrieval(false);
                result.setConfidence(0.8);
                result.setReason("识别为通用知识查询: " + pattern);
                log.debug("通用知识规则匹配: {}", pattern);
                return true;
            }
        }
        return false;
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
