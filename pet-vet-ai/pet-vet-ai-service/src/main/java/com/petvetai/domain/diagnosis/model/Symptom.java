package com.petvetai.domain.diagnosis.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 症状值对象
 * 
 * 封装症状信息，不可变
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public class Symptom {
    
    private final String description;
    private final LocalDateTime reportedAt;
    
    /**
     * 构造函数
     * 
     * @param description 症状描述
     * @author daidasheng
     * @date 2024-12-20
     */
    private Symptom(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("症状描述不能为空");
        }
        this.description = description;
        this.reportedAt = LocalDateTime.now();
    }
    
    /**
     * 创建症状值对象
     * 
     * @param description 症状描述
     * @return 症状值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public static Symptom of(String description) {
        return new Symptom(description);
    }
    
    /**
     * 从已有数据创建症状值对象
     * 
     * @param description 症状描述
     * @param reportedAt 报告时间
     * @return 症状值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public static Symptom of(String description, LocalDateTime reportedAt) {
        Symptom symptom = new Symptom(description);
        // 使用反射设置 reportedAt（简化处理）
        try {
            java.lang.reflect.Field field = Symptom.class.getDeclaredField("reportedAt");
            field.setAccessible(true);
            field.set(symptom, reportedAt);
        } catch (Exception e) {
            // 忽略，使用当前时间
        }
        return symptom;
    }
    
    /**
     * 获取症状描述
     * 
     * @return 症状描述
     * @author daidasheng
     * @date 2024-12-20
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 获取报告时间
     * 
     * @return 报告时间
     * @author daidasheng
     * @date 2024-12-20
     */
    public LocalDateTime getReportedAt() {
        return reportedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symptom symptom = (Symptom) o;
        return Objects.equals(description, symptom.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(description);
    }
    
    @Override
    public String toString() {
        return "Symptom{" +
                "description='" + description + '\'' +
                ", reportedAt=" + reportedAt +
                '}';
    }
}

