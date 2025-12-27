package com.petvetai.infrastructure.persistence.diagnosis;

import com.petvetai.domain.diagnosis.model.Diagnosis;
import com.petvetai.domain.diagnosis.model.DiagnosisId;
import com.petvetai.domain.diagnosis.repository.DiagnosisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 诊断仓储实现
 * 
 * 在基础设施层实现领域层定义的仓储接口
 * 注意：当前简化实现，诊断结果暂不持久化到数据库
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DiagnosisRepositoryImpl implements DiagnosisRepository {
    
    @Override
    public void save(Diagnosis diagnosis) {
        // 当前简化实现，诊断结果暂不持久化
        // 如果需要持久化，可以创建 DiagnosisPO 和对应的 Mapper
        log.debug("保存诊断，诊断ID: {}", diagnosis.getId());
    }
    
    @Override
    public Diagnosis findById(DiagnosisId diagnosisId) {
        // 当前简化实现，诊断结果暂不持久化
        // 如果需要查询，可以创建 DiagnosisPO 和对应的 Mapper
        log.debug("查找诊断，诊断ID: {}", diagnosisId);
        return null;
    }
}

