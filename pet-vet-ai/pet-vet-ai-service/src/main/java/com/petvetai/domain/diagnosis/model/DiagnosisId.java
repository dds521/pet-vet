package com.petvetai.domain.diagnosis.model;

import java.util.Objects;

/**
 * 诊断ID值对象
 * 
 * 值对象，不可变，通过值来区分
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public class DiagnosisId {
    
    private final Long value;
    
    private DiagnosisId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("诊断ID必须大于0");
        }
        this.value = value;
    }
    
    /**
     * 创建诊断ID值对象
     * 
     * @param value 诊断ID值
     * @return 诊断ID值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public static DiagnosisId of(Long value) {
        return new DiagnosisId(value);
    }
    
    /**
     * 生成新的诊断ID值对象
     * 
     * @return 诊断ID值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public static DiagnosisId generate() {
        long id = System.currentTimeMillis();
        return new DiagnosisId(id);
    }
    
    /**
     * 获取诊断ID值
     * 
     * @return 诊断ID值
     * @author daidasheng
     * @date 2024-12-20
     */
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiagnosisId that = (DiagnosisId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

