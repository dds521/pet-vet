package com.petvetai.domain.diagnosis.model;

import java.util.Objects;

/**
 * 诊断结果值对象
 * 
 * 封装诊断结果信息，不可变
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public class DiagnosisResult {
    
    private final String suggestion;
    private final Double confidence;
    
    /**
     * 构造函数
     * 
     * @param suggestion 诊断建议
     * @param confidence 置信度（0.0-1.0）
     * @author daidasheng
     * @date 2024-12-20
     */
    private DiagnosisResult(String suggestion, Double confidence) {
        if (suggestion == null || suggestion.trim().isEmpty()) {
            throw new IllegalArgumentException("诊断建议不能为空");
        }
        if (confidence == null || confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("置信度必须在0.0-1.0之间");
        }
        this.suggestion = suggestion;
        this.confidence = confidence;
    }
    
    /**
     * 创建诊断结果值对象
     * 
     * @param suggestion 诊断建议
     * @param confidence 置信度
     * @return 诊断结果值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public static DiagnosisResult of(String suggestion, Double confidence) {
        return new DiagnosisResult(suggestion, confidence);
    }
    
    /**
     * 获取诊断建议
     * 
     * @return 诊断建议
     * @author daidasheng
     * @date 2024-12-20
     */
    public String getSuggestion() {
        return suggestion;
    }
    
    /**
     * 获取置信度
     * 
     * @return 置信度
     * @author daidasheng
     * @date 2024-12-20
     */
    public Double getConfidence() {
        return confidence;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiagnosisResult that = (DiagnosisResult) o;
        return Objects.equals(suggestion, that.suggestion) &&
               Objects.equals(confidence, that.confidence);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(suggestion, confidence);
    }
    
    @Override
    public String toString() {
        return "DiagnosisResult{" +
                "suggestion='" + suggestion + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}

