package com.petvet.rag.api.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG 查询响应
 * 
 * @author PetVetRAG Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagQueryResp {
    
    /**
     * 检索到的文档数量
     */
    private Integer retrievedCount;
    
    /**
     * 检索到的文档列表
     */
    private List<RetrievedDocument> retrievedDocuments;
    
    /**
     * 生成的答案（如果启用了生成）
     */
    private String generatedAnswer;
    
    /**
     * 是否使用了生成
     */
    private Boolean usedGeneration;
    
    /**
     * 检索到的文档信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrievedDocument {
        /**
         * Chunk ID
         */
        private String chunkId;
        
        /**
         * 相似度分数
         */
        private Double score;
        
        /**
         * 文档文本内容
         */
        private String text;
        
        /**
         * 关联的简历ID（如果有）
         */
        private String resumeId;
        
        /**
         * 字段类型（工作经历、教育背景等）
         */
        private String fieldType;
    }
}
