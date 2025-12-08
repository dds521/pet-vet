package com.petvet.embedding.api.feign;

import com.petvet.embedding.api.dto.ApiResponse;
import com.petvet.embedding.api.req.ResumeSearchReq;
import com.petvet.embedding.api.resp.ResumeMetadataResp;
import com.petvet.embedding.api.resp.ResumeParseResp;
import com.petvet.embedding.api.resp.ResumeSearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简历解析Feign客户端降级处理
 */
@Slf4j
@Component
public class ResumeParseFeignClientFallbackFactory implements FallbackFactory<ResumeParseFeignClient> {
    
    @Override
    public ResumeParseFeignClient create(Throwable cause) {
        log.error("简历解析服务调用失败，触发降级处理", cause);
        
        return new ResumeParseFeignClient() {
            @Override
            public ApiResponse<ResumeParseResp> parseResume(MultipartFile file) {
                log.warn("简历解析服务降级：parseResume");
                return ApiResponse.fail("简历解析服务暂时不可用，请稍后重试");
            }
            
            @Override
            public ApiResponse<ResumeMetadataResp> getResume(String resumeId) {
                log.warn("简历解析服务降级：getResume, resumeId: {}", resumeId);
                return ApiResponse.fail("简历解析服务暂时不可用，请稍后重试");
            }
            
            @Override
            public ApiResponse<ResumeSearchResp> searchResume(ResumeSearchReq request) {
                log.warn("简历解析服务降级：searchResume, query: {}", request != null ? request.getQuery() : null);
                return ApiResponse.fail("简历解析服务暂时不可用，请稍后重试");
            }
            
            @Override
            public ApiResponse<Void> deleteResume(String resumeId) {
                log.warn("简历解析服务降级：deleteResume, resumeId: {}", resumeId);
                return ApiResponse.fail("简历解析服务暂时不可用，请稍后重试");
            }
        };
    }
}
