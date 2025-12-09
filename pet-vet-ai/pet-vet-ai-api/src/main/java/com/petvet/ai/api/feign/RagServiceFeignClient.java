package com.petvet.ai.api.feign;

import com.petvet.rag.api.dto.ApiResponse;
import com.petvet.rag.api.req.RagQueryReq;
import com.petvet.rag.api.resp.RagQueryResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * RAG 服务 Feign 客户端
 * 用于调用 pet-vet-rag 服务的 RAG 功能
 * 
 * @author PetVetAI Team
 */
@FeignClient(
    name = "pet-vet-rag",
    path = "/api/rag",
    fallbackFactory = RagServiceFeignClientFallbackFactory.class
)
public interface RagServiceFeignClient {
    
    /**
     * 执行 RAG 查询
     * 
     * @param request RAG 查询请求
     * @return RAG 查询响应
     */
    @PostMapping("/query")
    ApiResponse<RagQueryResp> query(@RequestBody RagQueryReq request);
    
    /**
     * 健康检查
     * 
     * @return 健康状态
     */
    @org.springframework.web.bind.annotation.GetMapping("/health")
    ApiResponse<String> health();
}
