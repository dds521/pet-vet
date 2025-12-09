package com.petvetai.app.controller;

import com.petvetai.app.domain.Diagnosis;
import com.petvetai.app.service.PetMedicalService;
import com.petvetai.app.service.RagPetMedicalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 宠物医疗控制器
 * 
 * 提供基础的 AI 诊断和基于 RAG 的增强诊断功能
 * RAG 功能由 pet-vet-rag 模块提供
 * 
 * @author PetVetAI Team
 */
@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
@Slf4j
public class PetVetController {

    private final PetMedicalService petMedicalService;
    private final RagPetMedicalService ragPetMedicalService;

    /**
     * 基础 AI 诊断接口
     * 使用 LangChain4j 直接调用 LLM 进行诊断
     * 
     * @param request 诊断请求
     * @return 诊断结果
     */
    @PostMapping("/diagnose")
    public ResponseEntity<Diagnosis> diagnose(@RequestBody DiagnosisRequest request) {
        log.info("收到基础诊断请求，宠物ID: {}, 症状: {}", request.getPetId(), request.getSymptomDesc());
        Diagnosis diagnosis = petMedicalService.analyzeSymptom(request.getPetId(), request.getSymptomDesc());
        return ResponseEntity.ok(diagnosis);
    }
    
    /**
     * 基于 RAG 的增强诊断接口
     * 使用 pet-vet-rag 模块提供的 RAG 能力进行诊断
     * RAG 会先检索相关知识库，然后基于检索结果生成更准确的诊断建议
     * 
     * @param request 诊断请求
     * @return 诊断结果
     */
    @PostMapping("/diagnose/rag")
    public ResponseEntity<Diagnosis> diagnoseWithRag(@RequestBody DiagnosisRequest request) {
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

    /**
     * 诊断请求 DTO
     */
    public static class DiagnosisRequest {
        private Long petId;
        private String symptomDesc;

        // Getters and Setters
        public Long getPetId() { return petId; }
        public void setPetId(Long petId) { this.petId = petId; }

        public String getSymptomDesc() { return symptomDesc; }
        public void setSymptomDesc(String symptomDesc) { this.symptomDesc = symptomDesc; }
    }
}