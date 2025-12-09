package com.petvetai.app.service;

import com.petvet.ai.api.feign.RagServiceFeignClient;
import com.petvet.rag.api.dto.ApiResponse;
import com.petvet.rag.api.req.RagQueryReq;
import com.petvet.rag.api.resp.RagQueryResp;
import com.petvetai.app.domain.Diagnosis;
import com.petvetai.app.domain.Pet;
import com.petvetai.app.domain.Symptom;
import com.petvetai.app.mapper.PetMapper;
import com.petvetai.app.mapper.SymptomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基于 RAG 的宠物医疗服务
 * 
 * 使用 pet-vet-rag 模块提供的 RAG 能力进行增强型医疗咨询
 * RAG（Retrieval-Augmented Generation）结合了向量检索和 LLM 生成能力
 * 
 * @author PetVetAI Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RagPetMedicalService {
    
    private final RagServiceFeignClient ragServiceFeignClient;
    private final PetMapper petMapper;
    private final SymptomMapper symptomMapper;
    
    /**
     * 使用 RAG 能力分析宠物症状
     * 
     * @param petId 宠物ID
     * @param symptomDesc 症状描述
     * @return 诊断结果
     */
    @Transactional
    public Diagnosis analyzeSymptomWithRag(Long petId, String symptomDesc) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw new RuntimeException("Pet not found");
        }
        
        // 构建查询文本，包含宠物信息和症状
        String query = String.format(
            "宠物品种：%s，年龄：%d岁。症状：%s。请提供专业的宠物医疗诊断建议。",
            pet.getBreed(), pet.getAge(), symptomDesc
        );
        
        // 构建 RAG 查询请求
        RagQueryReq ragRequest = RagQueryReq.builder()
            .query(query)
            .maxResults(5)  // 检索前5个最相关的文档
            .minScore(0.7)  // 最小相似度分数
            .enableGeneration(true)  // 启用 LLM 生成答案
            .modelName("deepseek")  // 使用 DeepSeek 模型
            .contextWindowSize(5)  // 使用前5个检索结果作为上下文
            .build();
        
        try {
            // 调用 RAG 服务
            ApiResponse<RagQueryResp> response = ragServiceFeignClient.query(ragRequest);
            
            if (response == null || response.getCode() != 200 || response.getData() == null) {
                log.warn("RAG 查询失败，响应: {}", response);
                throw new RuntimeException("RAG 服务调用失败: " + (response != null ? response.getMessage() : "未知错误"));
            }
            
            RagQueryResp ragResult = response.getData();
            
            // 提取生成的答案
            String suggestion = ragResult.getGeneratedAnswer();
            if (suggestion == null || suggestion.trim().isEmpty()) {
                // 如果没有生成答案，使用检索到的文档
                if (ragResult.getRetrievedDocuments() != null && !ragResult.getRetrievedDocuments().isEmpty()) {
                    suggestion = ragResult.getRetrievedDocuments().get(0).getText();
                } else {
                    suggestion = "未找到相关信息";
                }
            }
            
            // 计算置信度（基于检索到的文档数量和相似度分数）
            Double confidence = calculateConfidence(ragResult);
            
            Diagnosis diagnosis = new Diagnosis(suggestion, confidence);
            
            // 保存症状记录
            Symptom symptom = new Symptom(symptomDesc, pet.getId());
            symptomMapper.insert(symptom);
            
            log.info("RAG 诊断完成，宠物ID: {}, 检索到 {} 个相关文档, 置信度: {}", 
                petId, ragResult.getRetrievedCount(), confidence);
            
            return diagnosis;
            
        } catch (Exception e) {
            log.error("RAG 诊断失败", e);
            throw new RuntimeException("RAG 诊断失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 计算诊断置信度
     * 基于检索到的文档数量和平均相似度分数
     * 
     * @param ragResult RAG 查询结果
     * @return 置信度分数（0.0-1.0）
     */
    private Double calculateConfidence(RagQueryResp ragResult) {
        if (ragResult.getRetrievedDocuments() == null || ragResult.getRetrievedDocuments().isEmpty()) {
            return 0.3;  // 没有检索到文档，置信度较低
        }
        
        // 计算平均相似度分数
        double avgScore = ragResult.getRetrievedDocuments().stream()
            .mapToDouble(doc -> doc.getScore() != null ? doc.getScore() : 0.0)
            .average()
            .orElse(0.0);
        
        // 基于文档数量和平均分数计算置信度
        int docCount = ragResult.getRetrievedCount();
        double confidence = Math.min(0.95, avgScore * 0.7 + (docCount >= 3 ? 0.2 : docCount * 0.067));
        
        return Math.max(0.5, confidence);  // 最低置信度 0.5
    }
}
