package com.petvet.ai.api.feign;

import com.petvet.rag.api.dto.ApiResponse;
import com.petvet.rag.api.req.RagQueryReq;
import com.petvet.rag.api.resp.RagQueryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * RAG 服务 Feign 客户端降级工厂
 * 当 RAG 服务不可用时提供降级处理
 * 
 * @author PetVetAI Team
 */
@Slf4j
@Component
public class RagServiceFeignClientFallbackFactory implements FallbackFactory<RagServiceFeignClient> {
    
    @Override
    public RagServiceFeignClient create(Throwable cause) {
        log.warn("RAG 服务调用失败，使用降级处理", cause);
        return new RagServiceFeignClient() {
            @Override
            public ApiResponse<RagQueryResp> query(RagQueryReq request) {
                log.error("RAG 查询服务不可用，返回空结果");
                return ApiResponse.fail("RAG 服务暂时不可用，请稍后重试");
            }
            
            @Override
            public ApiResponse<String> health() {
                return ApiResponse.fail("RAG 服务不可用");
            }
        };
    }
}
