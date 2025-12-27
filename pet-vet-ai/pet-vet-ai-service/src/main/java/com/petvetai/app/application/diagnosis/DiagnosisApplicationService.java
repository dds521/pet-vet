package com.petvetai.app.application.diagnosis;

import com.petvetai.domain.diagnosis.model.Diagnosis;
import com.petvetai.domain.diagnosis.model.DiagnosisResult;
import com.petvetai.domain.pet.model.PetId;
import com.petvetai.domain.diagnosis.repository.DiagnosisRepository;
import com.petvetai.domain.diagnosis.service.DiagnosisDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 诊断应用服务
 * 
 * 协调领域对象完成业务用例，处理事务等技术问题
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisApplicationService {
    
    private final DiagnosisDomainService diagnosisDomainService;
    private final DiagnosisRepository diagnosisRepository;
    
    /**
     * 使用RAG进行诊断分析
     * 
     * @param petId 宠物ID
     * @param symptomDesc 症状描述
     * @return 诊断结果DTO
     * @author daidasheng
     * @date 2024-12-20
     */
    @Transactional
    public DiagnosisResultDTO diagnoseWithRag(Long petId, String symptomDesc) {
        log.info("开始RAG诊断，宠物ID: {}, 症状: {}", petId, symptomDesc);
        
        // 1. 创建诊断聚合根
        Diagnosis diagnosis = Diagnosis.create(PetId.of(petId), symptomDesc);
        
        // 2. 调用领域服务进行诊断分析
        DiagnosisResult result = diagnosisDomainService.analyzeWithRag(diagnosis);
        
        // 3. 完成诊断
        diagnosis.complete(result);
        
        // 4. 保存诊断
        diagnosisRepository.save(diagnosis);
        
        log.info("RAG诊断完成，诊断ID: {}, 置信度: {}", diagnosis.getId(), result.getConfidence());
        
        // 5. 转换为DTO返回
        return DiagnosisResultDTO.builder()
            .suggestion(result.getSuggestion())
            .confidence(result.getConfidence())
            .build();
    }
    
    /**
     * 诊断结果DTO
     * 
     * @author daidasheng
     * @date 2024-12-20
     */
    @lombok.Data
    @lombok.Builder
    public static class DiagnosisResultDTO {
        private String suggestion;
        private Double confidence;
    }
}

