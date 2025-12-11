package com.petvet.rag.api.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG 验证响应
 * 
 * @author daidasheng
 * @date 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagValidationResp {
    
    /**
     * 生成的答案
     */
    private String answer;
    
    /**
     * 检索到的文档数量
     */
    private Integer retrievedCount;
    
    /**
     * 检索到的文档列表
     */
    private List<RetrievedDocument> retrievedDocuments;
    
    /**
     * 对话历史信息
     */
    private ConversationHistory conversationHistory;
    
    /**
     * 置信度分数（0.0-1.0）
     */
    private Double confidence;
    
    /**
     * 查询耗时（毫秒）
     */
    private Long queryTime;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 是否使用了知识库
     */
    private Boolean usedKnowledgeBase;
    
    /**
     * 检索到的文档信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrievedDocument {
        private String chunkId;
        private Double score;
        private String text;
        private String resumeId;
        private String fieldType;
    }
    
    /**
     * 对话历史信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationHistory {
        /**
         * 消息数量
         */
        private Integer messageCount;
        
        /**
         * 最近的消息列表
         */
        private List<Message> recentMessages;
    }
    
    /**
     * 消息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 角色：USER 或 ASSISTANT
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
        
        /**
         * 时间戳
         */
        private Long timestamp;
    }
}
