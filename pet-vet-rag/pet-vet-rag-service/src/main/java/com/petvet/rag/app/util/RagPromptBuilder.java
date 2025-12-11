package com.petvet.rag.app.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 提示词构建工具类
 * 
 * 用于构建 RAG 查询的提示词模板
 * 
 * @author daidasheng
 * @date 2024-12-11
 */
@Slf4j
public class RagPromptBuilder {
    
    /**
     * 默认提示词模板
     */
    private static final String DEFAULT_PROMPT_TEMPLATE = 
        "基于以下上下文信息回答用户的问题。如果上下文中没有相关信息，请说明无法从提供的信息中找到答案。\n\n" +
        "上下文信息：\n{context}\n\n" +
        "用户问题：{question}\n\n" +
        "请提供详细、准确的答案：";
    
    /**
     * 构建提示词
     * 
     * @param template 提示词模板，如果为空则使用默认模板
     * @param context 上下文信息（检索到的文档列表）
     * @param question 用户问题
     * @return 构建好的提示词
     * @author daidasheng
     * @date 2024-12-11
     */
    public static String buildPrompt(String template, List<String> context, String question) {
        if (!StringUtils.hasText(template)) {
            template = DEFAULT_PROMPT_TEMPLATE;
        }
        
        // 构建上下文文本
        String contextText = buildContextText(context);
        
        // 替换模板中的占位符
        String prompt = template
            .replace("{context}", contextText)
            .replace("{question}", question != null ? question : "");
        
        log.debug("构建提示词完成，长度: {}", prompt.length());
        return prompt;
    }
    
    /**
     * 构建上下文文本
     * 
     * @param context 上下文列表
     * @return 格式化后的上下文文本
     * @author daidasheng
     * @date 2024-12-11
     */
    private static String buildContextText(List<String> context) {
        if (context == null || context.isEmpty()) {
            return "无相关上下文信息";
        }
        
        return context.stream()
            .map((text, index) -> String.format("文档%d：\n%s", index + 1, text))
            .collect(Collectors.joining("\n\n"));
    }
    
    /**
     * 构建带评分的上下文文本
     * 
     * @param context 上下文列表（包含文本和评分）
     * @return 格式化后的上下文文本
     * @author daidasheng
     * @date 2024-12-11
     */
    public static String buildContextTextWithScore(List<ContextItem> context) {
        if (context == null || context.isEmpty()) {
            return "无相关上下文信息";
        }
        
        return context.stream()
            .map((item, index) -> String.format(
                "文档%d（相似度: %.2f）：\n%s", 
                index + 1, 
                item.getScore() != null ? item.getScore() : 0.0,
                item.getText()
            ))
            .collect(Collectors.joining("\n\n"));
    }
    
    /**
     * 上下文项（包含文本和评分）
     */
    public static class ContextItem {
        private String text;
        private Double score;
        
        public ContextItem(String text, Double score) {
            this.text = text;
            this.score = score;
        }
        
        public String getText() {
            return text;
        }
        
        public Double getScore() {
            return score;
        }
    }
}
