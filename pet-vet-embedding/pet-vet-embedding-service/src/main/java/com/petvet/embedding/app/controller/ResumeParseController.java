package com.petvet.embedding.app.controller;

import com.petvet.embedding.api.constants.ApiConstants;
import com.petvet.embedding.api.dto.ApiResponse;
import com.petvet.embedding.api.req.ResumeSearchReq;
import com.petvet.embedding.api.resp.ResumeMetadataResp;
import com.petvet.embedding.api.resp.ResumeParseResp;
import com.petvet.embedding.api.resp.ResumeSearchResp;
import com.petvet.embedding.app.service.ResumeMetadataService;
import com.petvet.embedding.app.service.ResumeParseService;
import com.petvet.embedding.app.service.TextChunkService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final TextChunkService textChunkService;
    
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
            
            if (matches.isEmpty()) {
                log.debug("未找到相似向量");
                return ResponseEntity.ok(ApiResponse.success(
                    ResumeSearchResp.builder()
                        .count(0)
                        .results(List.of())
                        .build()
                ));
            }
            
            // 提取所有chunkId
            List<String> chunkIds = matches.stream()
                .map(match -> match.embeddingId())
                .collect(Collectors.toList());
            
            log.debug("搜索到 {} 个匹配结果，chunkIds: {}", chunkIds.size(), chunkIds);
            
            // 从数据库批量查询文本内容
            Map<String, String> chunkTexts = textChunkService.getTextsByChunkIds(chunkIds);
            
            log.debug("从数据库查询到 {} 个文本内容，chunkTexts keys: {}", chunkTexts.size(), chunkTexts.keySet());
            
            // 统计孤儿向量（在向量数据库中但不在数据库中的chunk）
            List<String> orphanChunkIds = chunkIds.stream()
                .filter(chunkId -> !chunkTexts.containsKey(chunkId))
                .collect(Collectors.toList());
            
            if (!orphanChunkIds.isEmpty()) {
                log.warn("发现 {} 个孤儿向量（在向量数据库中但不在数据库中），chunkIds: {}。这些数据可能是在修复保存逻辑之前上传的。建议使用 /api/resume/cleanup/orphan-vectors 接口清理这些数据。", 
                    orphanChunkIds.size(), orphanChunkIds);
            }
            
            // 构建返回结果，过滤掉数据库中不存在的chunk（孤儿向量）
            List<ResumeSearchResp.SearchItem> results = matches.stream()
                .filter(match -> {
                    String chunkId = match.embeddingId();
                    // 只返回在数据库中存在的chunk
                    boolean exists = chunkTexts.containsKey(chunkId);
                    if (!exists) {
                        log.debug("过滤掉孤儿向量，chunkId: {}", chunkId);
                    }
                    return exists;
                })
                .map(match -> {
                    String chunkId = match.embeddingId();
                    // 从数据库获取文本（此时已经确认存在）
                    String text = chunkTexts.get(chunkId);
                    if (text == null) {
                        log.warn("chunkId {} 在chunkTexts中不存在，这不应该发生", chunkId);
                        text = "[文本内容不可用]";
                    } else {
                        log.debug("从数据库获取文本成功，chunkId: {}, text长度: {}", chunkId, text.length());
                    }
                    return ResumeSearchResp.SearchItem.builder()
                        .chunkId(chunkId)
                        .score(match.score())
                        .text(text)
                        .build();
                })
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
     * 清理孤儿向量数据
     * 用于清理在向量数据库中但不在数据库中的chunk数据
     * 
     * @param chunkIds 要清理的chunkId列表
     */
    @PostMapping("/cleanup/orphan-vectors")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cleanupOrphanVectors(
            @RequestBody Map<String, List<String>> request) {
        try {
            List<String> chunkIds = request.get("chunkIds");
            if (chunkIds == null || chunkIds.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("chunkIds不能为空"));
            }
            
            log.info("开始清理孤儿向量数据，数量: {}", chunkIds.size());
            
            int successCount = 0;
            int failCount = 0;
            List<String> failedIds = new ArrayList<>();
            
            for (String chunkId : chunkIds) {
                try {
                    // 检查数据库中是否存在
                    String text = textChunkService.getTextByChunkId(chunkId);
                    if (text == null) {
                        // 数据库中不存在，删除向量数据库中的向量
                        vectorDatabaseService.delete(chunkId);
                        successCount++;
                        log.debug("清理孤儿向量成功，chunkId: {}", chunkId);
                    } else {
                        log.debug("chunkId {} 在数据库中存在，跳过清理", chunkId);
                    }
                } catch (Exception e) {
                    failCount++;
                    failedIds.add(chunkId);
                    log.warn("清理孤儿向量失败，chunkId: {}", chunkId, e);
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("total", chunkIds.size());
            result.put("success", successCount);
            result.put("fail", failCount);
            result.put("failedIds", failedIds);
            
            log.info("清理孤儿向量数据完成，总数: {}, 成功: {}, 失败: {}", 
                chunkIds.size(), successCount, failCount);
            
            return ResponseEntity.ok(ApiResponse.success(result, "清理完成"));
            
        } catch (Exception e) {
            log.error("清理孤儿向量数据失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("清理失败: " + e.getMessage()));
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
            
            // 删除文本Chunks
            textChunkService.deleteByResumeId(resumeId);
            
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
