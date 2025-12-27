package com.petvetai.domain.diagnosis.model;

/**
 * 诊断状态枚举
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public enum DiagnosisStatus {
    
    /**
     * 待诊断
     */
    PENDING,
    
    /**
     * 已完成
     */
    COMPLETED,
    
    /**
     * 已取消
     */
    CANCELLED
}

