package com.petvetai.app.controller;

import com.petvetai.app.application.diagnosis.DiagnosisApplicationService;
import com.petvetai.app.application.diagnosis.DiagnosisApplicationService.DiagnosisResultDTO;
import com.petvetai.app.dto.req.DiagnosisReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 宠物医疗控制器（DDD改造后）
 * 
 * 提供基于 RAG 的增强诊断功能
 * 所有 AI 能力（向量化、RAG、LLM）均通过 pet-vet-rag、pet-vet-embedding、pet-vet-mcp 服务提供
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
@Slf4j
public class PetVetController {

    private final DiagnosisApplicationService diagnosisApplicationService;

    /**
     * 基于 RAG 的增强诊断接口
     * 使用 pet-vet-rag 服务提供的 RAG 能力进行诊断
     * RAG 会先检索相关知识库，然后基于检索结果生成更准确的诊断建议
     * 
     * @param request 诊断请求
     * @return 诊断结果
     * @author daidasheng
     * @date 2024-12-20
     */
    @PostMapping("/diagnose")
    public ResponseEntity<Map<String, Object>> diagnose(@Valid @RequestBody DiagnosisReq request) {
        log.info("收到 RAG 增强诊断请求，宠物ID: {}, 症状: {}", request.getPetId(), request.getSymptomDesc());
        try {
            DiagnosisResultDTO result = diagnosisApplicationService.diagnoseWithRag(
                request.getPetId(), 
                request.getSymptomDesc()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("suggestion", result.getSuggestion());
            response.put("confidence", result.getConfidence());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("RAG 诊断失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("suggestion", "诊断失败: " + e.getMessage());
            response.put("confidence", 0.0);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}