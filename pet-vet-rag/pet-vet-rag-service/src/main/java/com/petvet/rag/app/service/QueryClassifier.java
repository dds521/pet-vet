package com.petvet.rag.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 查询分类器
 * 判断是否需要使用向量数据库检索
 * 
 * @author daidasheng
 * @date 2024-12-11
 */
@Service
@Slf4j
public class QueryClassifier {
    
    @Value("${rag.retrieval.force-retrieval-keywords:疾病,症状,诊断,治疗,疫苗,感染,炎症,手术,药物}")
    private String forceRetrievalKeywords;
    
    @Value("${rag.retrieval.skip-retrieval-keywords:你好,谢谢,再见,哈哈}")
    private String skipRetrievalKeywords;
    
    @Value("${rag.retrieval.default-retrieval:true}")
    private Boolean defaultRetrieval;
    
    /**
     * 判断是否需要检索知识库
     * 
     * @param query 用户查询
     * @param conversationHistory 对话历史（可选）
     * @return 是否需要检索
     * @author daidasheng
     * @date 2024-12-11
     */
    public boolean needRetrieval(String query, Object conversationHistory) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        
        String lowerQuery = query.toLowerCase();
        
        // 1. 检查是否是闲聊（跳过检索）
        if (isCasualChat(lowerQuery)) {
            log.debug("查询被识别为闲聊，跳过检索: {}", query);
            return false;
        }
        
        // 2. 检查是否包含强制检索的关键词
        if (containsForceRetrievalKeywords(lowerQuery)) {
            log.debug("查询包含强制检索关键词，需要检索: {}", query);
            return true;
        }
        
        // 3. 检查是否是通用知识查询（跳过检索）
        if (isGeneralKnowledge(lowerQuery)) {
            log.debug("查询被识别为通用知识，跳过检索: {}", query);
            return false;
        }
        
        // 4. 默认策略
        boolean needRetrieval = defaultRetrieval != null ? defaultRetrieval : true;
        log.debug("使用默认策略，是否需要检索: {}, 查询: {}", needRetrieval, query);
        return needRetrieval;
    }
    
    /**
     * 判断是否是闲聊
     * 
     * @param query 查询文本（已转小写）
     * @return 是否是闲聊
     * @author daidasheng
     * @date 2024-12-11
     */
    private boolean isCasualChat(String query) {
        List<String> chatKeywords = Arrays.asList(
            skipRetrievalKeywords != null ? skipRetrievalKeywords.split(",") : 
            new String[]{"你好", "谢谢", "再见", "哈哈"}
        );
        
        return chatKeywords.stream()
            .anyMatch(keyword -> query.contains(keyword.toLowerCase().trim()));
    }
    
    /**
     * 判断是否包含强制检索的关键词
     * 
     * @param query 查询文本（已转小写）
     * @return 是否包含强制检索关键词
     * @author daidasheng
     * @date 2024-12-11
     */
    private boolean containsForceRetrievalKeywords(String query) {
        List<String> domainTerms = Arrays.asList(
            forceRetrievalKeywords != null ? forceRetrievalKeywords.split(",") :
            new String[]{"疾病", "症状", "诊断", "治疗", "疫苗", "感染", "炎症", "手术", "药物"}
        );
        
        return domainTerms.stream()
            .anyMatch(term -> query.contains(term.toLowerCase().trim()));
    }
    
    /**
     * 判断是否是通用知识查询
     * 
     * @param query 查询文本（已转小写）
     * @return 是否是通用知识查询
     * @author daidasheng
     * @date 2024-12-11
     */
    private boolean isGeneralKnowledge(String query) {
        // 简单的通用知识查询模式
        String[] generalPatterns = {
            "什么是", "介绍一下", "简单说", "解释一下",
            "什么是", "什么是", "什么是"
        };
        
        return Arrays.stream(generalPatterns)
            .anyMatch(pattern -> query.contains(pattern));
    }
}
