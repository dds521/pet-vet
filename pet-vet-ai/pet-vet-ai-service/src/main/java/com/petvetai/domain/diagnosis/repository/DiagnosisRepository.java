package com.petvetai.domain.diagnosis.repository;

import com.petvetai.domain.diagnosis.model.Diagnosis;
import com.petvetai.domain.diagnosis.model.DiagnosisId;

/**
 * 诊断仓储接口
 * 
 * 定义诊断聚合的持久化接口，实现在基础设施层
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public interface DiagnosisRepository {
    
    /**
     * 保存诊断
     * 
     * @param diagnosis 诊断聚合根
     * @author daidasheng
     * @date 2024-12-20
     */
    void save(Diagnosis diagnosis);
    
    /**
     * 根据ID查找诊断
     * 
     * @param diagnosisId 诊断ID
     * @return 诊断聚合根，如果不存在则返回null
     * @author daidasheng
     * @date 2024-12-20
     */
    Diagnosis findById(DiagnosisId diagnosisId);
}

