package com.petvet.rag.api.feign;

import com.petvet.embedding.api.dto.ApiResponse;
import com.petvet.embedding.api.req.ResumeSearchReq;
import com.petvet.embedding.api.resp.ResumeSearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Embedding服务Feign客户端降级工厂
 * 
 * @author PetVetRAG Team
 */
@Component
@Slf4j
public class EmbeddingServiceFeignClientFallbackFactory 
    implements FallbackFactory<EmbeddingServiceFeignClient> {
    
    @Override
    public EmbeddingServiceFeignClient create(Throwable cause) {
        log.error("调用Embedding服务失败，使用降级处理", cause);
        
        return new EmbeddingServiceFeignClient() {
            @Override
            public ApiResponse<ResumeSearchResp> searchResume(ResumeSearchReq request) {
                log.warn("Embedding服务不可用，返回空结果");
                return ApiResponse.fail("Embedding服务暂时不可用，请稍后重试");
            }
        };
    }
}
