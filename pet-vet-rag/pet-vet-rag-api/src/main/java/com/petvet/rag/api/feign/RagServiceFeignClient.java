package com.petvet.rag.api.feign;

import com.petvet.rag.api.constants.ApiConstants;
import com.petvet.rag.api.dto.ApiResponse;
import com.petvet.rag.api.req.RagQueryReq;
import com.petvet.rag.api.resp.RagQueryResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * RAG 服务 Feign 客户端
 * 其他服务可以通过依赖 pet-vet-rag-api 模块，注入此接口来调用 RAG 服务
 * 
 * @author PetVetRAG Team
 * @date 2024-12-16
 */
@FeignClient(
    name = ApiConstants.SERVICE_NAME,
    path = ApiConstants.RAG_API_PREFIX,
    fallbackFactory = RagServiceFeignClientFallbackFactory.class
)
public interface RagServiceFeignClient {
    
    /**
     * 执行 RAG 查询
     * 
     * @param request RAG 查询请求
     * @return RAG 查询响应
     * @author PetVetRAG Team
     * @date 2024-12-16
     */
    @PostMapping("/query")
    ApiResponse<RagQueryResp> query(@RequestBody RagQueryReq request);
    
    /**
     * 健康检查
     * 
     * @return 健康状态
     * @author PetVetRAG Team
     * @date 2024-12-16
     */
    @GetMapping("/health")
    ApiResponse<String> health();
}
