package com.petvet.embedding.api.feign;

import com.petvet.embedding.api.constants.ApiConstants;
import com.petvet.embedding.api.dto.ApiResponse;
import com.petvet.embedding.api.req.ResumeSearchReq;
import com.petvet.embedding.api.resp.ResumeMetadataResp;
import com.petvet.embedding.api.resp.ResumeParseResp;
import com.petvet.embedding.api.resp.ResumeSearchResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简历解析Feign客户端接口
 * 其他服务可以通过依赖API模块，注入此接口来调用简历解析服务
 * 
 * 使用示例：
 * <pre>
 * {@code
 * @Autowired
 * private ResumeParseFeignClient resumeParseFeignClient;
 * 
 * // 解析简历
 * ApiResponse<ResumeParseResp> result = resumeParseFeignClient.parseResume(file);
 * 
 * // 查询简历
 * ApiResponse<ResumeMetadataResp> metadata = resumeParseFeignClient.getResume("resume_id");
 * 
 * // 搜索简历
 * ResumeSearchReq request = ResumeSearchReq.builder()
 *     .query("Java开发经验")
 *     .maxResults(10)
 *     .minScore(0.7)
 *     .build();
 * ApiResponse<ResumeSearchResp> searchResult = resumeParseFeignClient.searchResume(request);
 * }
 * </pre>
 */
@FeignClient(
    name = ApiConstants.SERVICE_NAME,
    path = ApiConstants.RESUME_API_PREFIX,
    fallbackFactory = ResumeParseFeignClientFallbackFactory.class
)
public interface ResumeParseFeignClient {
    
    /**
     * 上传并解析PDF简历
     * 
     * @param file PDF文件
     * @return 解析结果
     */
    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<ResumeParseResp> parseResume(@RequestPart("file") MultipartFile file);
    
    /**
     * 查询简历信息
     * 
     * @param resumeId 简历ID
     * @return 简历元数据
     */
    @GetMapping("/{resumeId}")
    ApiResponse<ResumeMetadataResp> getResume(@PathVariable("resumeId") String resumeId);
    
    /**
     * 语义搜索简历
     * 
     * @param request 搜索请求
     * @return 搜索结果
     */
    @PostMapping("/search")
    ApiResponse<ResumeSearchResp> searchResume(@RequestBody ResumeSearchReq request);
    
    /**
     * 删除简历
     * 
     * @param resumeId 简历ID
     * @return 删除结果
     */
    @DeleteMapping("/{resumeId}")
    ApiResponse<Void> deleteResume(@PathVariable("resumeId") String resumeId);
}
