package com.petvetai.infrastructure.external.rag;

/**
 * RAG服务接口
 * 
 * 封装与RAG服务的交互
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public interface RagService {
    
    /**
     * 执行RAG查询
     * 
     * @param query 查询文本
     * @return RAG查询结果
     * @author daidasheng
     * @date 2024-12-20
     */
    RagResult query(String query);
    
    /**
     * RAG查询结果
     * 
     * @author daidasheng
     * @date 2024-12-20
     */
    class RagResult {
        private final String answer;
        private final Double confidence;
        
        public RagResult(String answer, Double confidence) {
            this.answer = answer;
            this.confidence = confidence;
        }
        
        public String getAnswer() {
            return answer;
        }
        
        public Double getConfidence() {
            return confidence;
        }
    }
}

