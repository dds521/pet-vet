package com.petvet.rag.app.classifier.engine;

import com.petvet.rag.app.classifier.config.ClassifierProperties;
import com.petvet.rag.app.classifier.model.RuleDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则加载器
 * 
 * 负责从配置中加载规则定义，并提供默认规则
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RuleLoader {
    
    private final ClassifierProperties properties;
    private final RuleEngine ruleEngine;
    
    /**
     * 加载的规则列表（按优先级排序）
     */
    private List<RuleDefinition> loadedRules = new ArrayList<>();
    
    /**
     * 初始化规则
     */
    @PostConstruct
    public void init() {
        loadRules();
    }
    
    /**
     * 加载规则
     */
    public void loadRules() {
        List<RuleDefinition> rules = new ArrayList<>();
        
        // 1. 从配置中加载规则
        if (properties.getRule().getRules() != null && !properties.getRule().getRules().isEmpty()) {
            rules.addAll(properties.getRule().getRules());
            log.info("从配置加载了 {} 条规则", properties.getRule().getRules().size());
        }
        
        // 2. 如果配置中没有规则，使用默认规则
        if (rules.isEmpty()) {
            rules.addAll(getDefaultRules());
            log.info("使用默认规则，共 {} 条", rules.size());
        }
        
        // 3. 验证规则表达式
        List<RuleDefinition> validRules = rules.stream()
            .filter(rule -> {
                if (rule.getEnabled() == null || !rule.getEnabled()) {
                    return false;
                }
                if (rule.getExpression() == null || rule.getExpression().trim().isEmpty()) {
                    log.warn("规则 {} 的表达式为空，跳过", rule.getName());
                    return false;
                }
                // 验证表达式语法
                boolean valid = ruleEngine.validateExpression(rule.getExpression());
                if (!valid) {
                    log.warn("规则 {} 的表达式语法无效，跳过: {}", rule.getName(), rule.getExpression());
                }
                return valid;
            })
            .collect(Collectors.toList());
        
        // 4. 按优先级排序
        validRules.sort(Comparator.comparingInt(rule -> rule.getPriority() != null ? rule.getPriority() : Integer.MAX_VALUE));
        
        loadedRules = validRules;
        log.info("规则加载完成，共 {} 条有效规则", loadedRules.size());
        
        // 打印规则信息
        loadedRules.forEach(rule -> log.debug("规则: {}, 优先级: {}, 表达式: {}", rule.getName(), rule.getPriority(), rule.getExpression()));
    }
    
    /**
     * 获取默认规则
     * 
     * @return 默认规则列表
     */
    private List<RuleDefinition> getDefaultRules() {
        List<RuleDefinition> rules = new ArrayList<>();
        
        // 1. 闲聊规则
        rules.add(RuleDefinition.builder()
            .name("casual_chat")
            .priority(1)
            .enabled(true)
            .description("识别闲聊对话")
            .expression("lowerQuery.contains(\"你好\") || lowerQuery.contains(\"谢谢\") || " +
                       "lowerQuery.contains(\"再见\") || lowerQuery.contains(\"哈哈\")")
            .action("result.setNeedRetrieval(false); " +
                   "result.setConfidence(0.9); " +
                   "result.setReason(\"识别为闲聊\"); " +
                   "return true;")
            .build());
        
        // 2. 强制检索规则
        rules.add(RuleDefinition.builder()
            .name("force_retrieval")
            .priority(2)
            .enabled(true)
            .description("识别需要强制检索的领域关键词")
            .expression("lowerQuery.contains(\"疾病\") || lowerQuery.contains(\"症状\") || " +
                       "lowerQuery.contains(\"诊断\") || lowerQuery.contains(\"治疗\") || " +
                       "lowerQuery.contains(\"疫苗\") || lowerQuery.contains(\"感染\") || " +
                       "lowerQuery.contains(\"炎症\") || lowerQuery.contains(\"手术\") || " +
                       "lowerQuery.contains(\"药物\")")
            .action("result.setNeedRetrieval(true); " +
                   "result.setConfidence(0.95); " +
                   "result.setReason(\"包含领域关键词\"); " +
                   "return true;")
            .build());
        
        // 3. 通用知识规则
        rules.add(RuleDefinition.builder()
            .name("general_knowledge")
            .priority(3)
            .enabled(true)
            .description("识别通用知识查询")
            .expression("lowerQuery.contains(\"什么是\") || lowerQuery.contains(\"介绍一下\") || " +
                       "lowerQuery.contains(\"简单说\") || lowerQuery.contains(\"解释一下\")")
            .action("result.setNeedRetrieval(false); " +
                   "result.setConfidence(0.8); " +
                   "result.setReason(\"识别为通用知识查询\"); " +
                   "return true;")
            .build());
        
        return rules;
    }
    
    /**
     * 获取所有已加载的规则
     * 
     * @return 规则列表
     */
    public List<RuleDefinition> getRules() {
        return new ArrayList<>(loadedRules);
    }
    
    /**
     * 重新加载规则
     */
    public void reload() {
        loadRules();
    }
}
