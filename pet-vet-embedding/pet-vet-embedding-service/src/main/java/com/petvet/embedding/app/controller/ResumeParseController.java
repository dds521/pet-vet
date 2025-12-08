package com.petvet.embedding.app.controller;

import com.petvet.embedding.api.constants.ApiConstants;
import com.petvet.embedding.api.dto.ApiResponse;
import com.petvet.embedding.api.req.ResumeSearchReq;
import com.petvet.embedding.api.resp.ResumeMetadataResp;
import com.petvet.embedding.api.resp.ResumeParseResp;
import com.petvet.embedding.api.resp.ResumeSearchResp;
import com.petvet.embedding.app.service.ResumeMetadataService;
import com.petvet.embedding.app.service.ResumeParseService;
import com.petvet.embedding.app.service.VectorDatabaseService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 简历解析控制器
 * 实现 ResumeParseFeignClient 接口，提供REST API和Feign调用支持
 */
@RestController
@RequestMapping(ApiConstants.RESUME_API_PREFIX)
@RequiredArgsConstructor
@Slf4j
public class ResumeParseController {
    
    private final ResumeParseService resumeParseService;
    private final ResumeMetadataService metadataService;
    private final VectorDatabaseService vectorDatabaseService;
    
    /**
     * 上传并解析PDF简历
     */
    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeParseResp>> parseResume(
            @RequestPart("file") MultipartFile file) {
        
        try {
            // 文件验证
            validateFile(file);
            
            // 解析简历
            ResumeParseResp result = resumeParseService.parseResume(file);
            
            return ResponseEntity.ok(ApiResponse.success(result, "简历解析成功"));
            
        } catch (IllegalArgumentException e) {
            log.warn("简历解析参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail("参数错误: " + e.getMessage()));
            
        } catch (Exception e) {
            log.error("简历解析失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("简历解析失败: " + e.getMessage()));
        }
    }
    
    /**
     * 查询简历信息
     */
    @GetMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<ResumeMetadataResp>> getResume(@PathVariable String resumeId) {
        ResumeMetadataResp metadata = metadataService.getById(resumeId);
        if (metadata == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("简历不存在，ID: " + resumeId));
        }
        
        return ResponseEntity.ok(ApiResponse.success(metadata));
    }
    
    /**
     * 语义搜索简历
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<ResumeSearchResp>> searchResume(
            @RequestBody ResumeSearchReq request) {
        
        try {
            if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("查询文本不能为空"));
            }
            
            String queryText = request.getQuery();
            int maxResults = request.getMaxResults() != null ? request.getMaxResults() : 10;
            double minScore = request.getMinScore() != null ? request.getMinScore() : 0.7;
            
            // 搜索相似向量
            List<EmbeddingMatch<TextSegment>> matches = vectorDatabaseService.findSimilar(queryText, maxResults, minScore);
            
            // 构建返回结果
            List<ResumeSearchResp.SearchItem> results = matches.stream()
                .map(match -> ResumeSearchResp.SearchItem.builder()
                    .chunkId(match.embeddingId())
                    .score(match.score())
                    .text(match.embedded() != null ? match.embedded().text() : null)
                    .build())
                .collect(Collectors.toList());
            
            ResumeSearchResp searchResult = ResumeSearchResp.builder()
                .count(results.size())
                .results(results)
                .build();
            
            return ResponseEntity.ok(ApiResponse.success(searchResult));
            
        } catch (Exception e) {
            log.error("简历搜索失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("搜索失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除简历
     */
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(@PathVariable String resumeId) {
        try {
            ResumeMetadataResp metadata = metadataService.getById(resumeId);
            if (metadata == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("简历不存在，ID: " + resumeId));
            }
            
            // 删除向量数据库中的向量
            if (metadata.getVectorIds() != null) {
                for (String vectorId : metadata.getVectorIds()) {
                    try {
                        vectorDatabaseService.delete(vectorId);
                    } catch (Exception e) {
                        log.warn("删除向量失败，ID: {}", vectorId, e);
                    }
                }
            }
            
            // 删除元数据
            metadataService.delete(resumeId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "简历删除成功"));
            
        } catch (Exception e) {
            log.error("删除简历失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 文件验证
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("只支持PDF文件");
        }
        
        // 验证文件名格式：姓名-职位-版本.pdf
        String nameWithoutExt = fileName.replaceAll("\\.pdf$", "").replaceAll("\\.PDF$", "");
        String[] parts = nameWithoutExt.split("-");
        if (parts.length < 2) {
            throw new IllegalArgumentException(
                "文件名格式不正确，应为：姓名-职位-版本.pdf，实际: " + fileName);
        }
        
        // 验证文件大小（例如：最大10MB）
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }
    }
}
