package com.petvetai.app.controller;

import com.petvetai.app.domain.Diagnosis;
import com.petvetai.app.dto.req.DiagnosisReq;
import com.petvetai.app.service.RagPetMedicalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 宠物医疗控制器
 * 
 * 提供基于 RAG 的增强诊断功能
 * 所有 AI 能力（向量化、RAG、LLM）均通过 pet-vet-rag、pet-vet-embedding、pet-vet-mcp 服务提供
 * 
 * @author PetVetAI Team
 * @date 2024-12-16
 */
@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
@Slf4j
public class PetVetController {

    private final RagPetMedicalService ragPetMedicalService;

    /**
     * 基于 RAG 的增强诊断接口
     * 使用 pet-vet-rag 服务提供的 RAG 能力进行诊断
     * RAG 会先检索相关知识库，然后基于检索结果生成更准确的诊断建议
     * 
     * @param request 诊断请求
     * @return 诊断结果
     * @author PetVetAI Team
     * @date 2024-12-16
     */
    @PostMapping("/diagnose")
    public ResponseEntity<Diagnosis> diagnose(@Valid @RequestBody DiagnosisReq request) {
        log.info("收到 RAG 增强诊断请求，宠物ID: {}, 症状: {}", request.getPetId(), request.getSymptomDesc());
        try {
            Diagnosis diagnosis = ragPetMedicalService.analyzeSymptomWithRag(
                request.getPetId(), 
                request.getSymptomDesc()
            );
            return ResponseEntity.ok(diagnosis);
        } catch (Exception e) {
            log.error("RAG 诊断失败", e);
            return ResponseEntity.internalServerError()
                .body(new Diagnosis("诊断失败: " + e.getMessage(), 0.0));
        }
    }
}