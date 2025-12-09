package com.petvet.rag.api.feign;

import com.petvet.embedding.api.dto.ApiResponse;
import com.petvet.embedding.api.req.ResumeSearchReq;
import com.petvet.embedding.api.resp.ResumeSearchResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Embedding服务Feign客户端
 * 用于调用pet-vet-embedding服务的向量检索功能
 * 
 * @author PetVetRAG Team
 */
@FeignClient(
    name = "pet-vet-embedding",
    path = "/api/resume",
    fallbackFactory = EmbeddingServiceFeignClientFallbackFactory.class
)
public interface EmbeddingServiceFeignClient {
    
    /**
     * 语义搜索简历
     * 
     * @param request 搜索请求
     * @return 搜索响应
     */
    @PostMapping("/search")
    ApiResponse<ResumeSearchResp> searchResume(@RequestBody ResumeSearchReq request);
}
