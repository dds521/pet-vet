package com.petvet.rag.app.controller;

import com.petvet.rag.api.constants.ApiConstants;
import com.petvet.rag.api.dto.ApiResponse;
import com.petvet.rag.api.req.RagQueryReq;
import com.petvet.rag.api.req.RagValidationReq;
import com.petvet.rag.api.resp.RagQueryResp;
import com.petvet.rag.api.resp.RagValidationResp;
import com.petvet.rag.app.service.RagService;
import com.petvet.rag.app.service.RagValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * RAG 控制器
 * 提供增强型检索API
 * 
 * @author PetVetRAG Team
 */
@RestController
@RequestMapping(ApiConstants.RAG_API_PREFIX)
@RequiredArgsConstructor
@Slf4j
public class RagController {
    
    private final RagService ragService;
    private final RagValidationService ragValidationService;
    
    /**
     * RAG 查询接口
     * 执行增强型检索，可选地生成答案
     * 
     * @param request 查询请求
     * @return 查询响应
     */
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<RagQueryResp>> query(@RequestBody RagQueryReq request) {
        try {
            // 参数验证
            if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("查询文本不能为空"));
            }
            
            // 执行RAG查询
            RagQueryResp result = ragService.query(request);
            
            return ResponseEntity.ok(ApiResponse.success(result, "查询成功"));
            
        } catch (IllegalArgumentException e) {
            log.warn("RAG查询参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail("参数错误: " + e.getMessage()));
            
        } catch (Exception e) {
            log.error("RAG查询失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("查询失败: " + e.getMessage()));
        }
    }
    
    /**
     * RAG 验证接口
     * 执行RAG验证，支持长期记忆和用户历史记录
     * 
     * @param request 验证请求
     * @return 验证响应
     * @author daidasheng
     * @date 2024-12-11
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<RagValidationResp>> validate(@RequestBody RagValidationReq request) {
        try {
            // 参数验证
            if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("查询文本不能为空"));
            }
            
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("用户ID不能为空"));
            }
            
            // 执行RAG验证
            RagValidationResp result = ragValidationService.validate(request);
            
            return ResponseEntity.ok(ApiResponse.success(result, "验证成功"));
            
        } catch (IllegalArgumentException e) {
            log.warn("RAG验证参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail("参数错误: " + e.getMessage()));
            
        } catch (Exception e) {
            log.error("RAG验证失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("验证失败: " + e.getMessage()));
        }
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("RAG服务运行正常", "健康检查通过"));
    }
}
