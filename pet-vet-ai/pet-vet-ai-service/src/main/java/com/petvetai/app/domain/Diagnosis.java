// 新文件：定义 Diagnosis 值对象
package com.petvetai.app.domain;

public class Diagnosis {
    private String suggestion;
    private Double confidence;

    public Diagnosis(String suggestion, Double confidence) {
        this.suggestion = suggestion;
        this.confidence = confidence;
    }

    // Getters and Setters
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
}
