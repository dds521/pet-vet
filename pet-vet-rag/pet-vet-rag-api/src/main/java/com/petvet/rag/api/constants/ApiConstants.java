package com.petvet.rag.api.constants;

/**
 * RAG API 常量定义
 * 
 * @author PetVetRAG Team
 * @date 2024-12-16
 */
public class ApiConstants {
    
    /**
     * 服务名称
     */
    public static final String SERVICE_NAME = "pet-vet-rag";
    
    /**
     * RAG API 前缀
     */
    public static final String RAG_API_PREFIX = "/api/rag";
    
    /**
     * 私有构造函数，防止实例化
     */
    private ApiConstants() {
        throw new UnsupportedOperationException("常量类不能被实例化");
    }
}
