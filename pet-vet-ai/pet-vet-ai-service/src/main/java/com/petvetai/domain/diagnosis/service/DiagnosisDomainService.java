package com.petvetai.domain.diagnosis.service;

import com.petvetai.domain.diagnosis.model.Diagnosis;
import com.petvetai.domain.diagnosis.model.DiagnosisResult;
import com.petvetai.domain.pet.model.Pet;
import com.petvetai.domain.pet.repository.PetRepository;
import com.petvetai.infrastructure.external.rag.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 诊断领域服务
 * 
 * 处理诊断相关的业务逻辑，如调用RAG服务进行诊断
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisDomainService {
    
    private final PetRepository petRepository;
    private final RagService ragService;
    
    /**
     * 使用RAG进行诊断分析
     * 
     * @param diagnosis 诊断聚合根
     * @return 诊断结果
     * @author daidasheng
     * @date 2024-12-20
     */
    public DiagnosisResult analyzeWithRag(Diagnosis diagnosis) {
        // 1. 获取宠物信息
        Pet pet = petRepository.findById(diagnosis.getPetId());
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        
        // 2. 构建查询文本
        String query = String.format(
            "宠物品种：%s，年龄：%d岁。症状：%s。请提供专业的宠物医疗诊断建议。",
            pet.getPetInfo().getBreed(),
            pet.getPetInfo().getAge(),
            diagnosis.getSymptom().getDescription()
        );
        
        // 3. 调用RAG服务进行诊断分析
        RagService.RagResult ragResult = ragService.query(query);
        
        // 4. 构建诊断结果
        return DiagnosisResult.of(
            ragResult.getAnswer(),
            ragResult.getConfidence()
        );
    }
}

