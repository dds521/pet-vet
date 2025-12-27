package com.petvetai.domain.doctor.model;

import java.util.Objects;

/**
 * 医生ID值对象
 * 
 * 值对象，不可变，通过值来区分
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
public class DoctorId {
    
    private final Long value;
    
    private DoctorId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("医生ID必须大于0");
        }
        this.value = value;
    }
    
    /**
     * 创建医生ID值对象
     * 
     * @param value 医生ID值
     * @return 医生ID值对象
     * @author daidasheng
     * @date 2024-12-27
     */
    public static DoctorId of(Long value) {
        return new DoctorId(value);
    }
    
    /**
     * 生成新的医生ID值对象
     * 
     * @return 医生ID值对象
     * @author daidasheng
     * @date 2024-12-27
     */
    public static DoctorId generate() {
        // 使用雪花算法生成ID（这里简化处理，实际应该使用ID生成器）
        long id = System.currentTimeMillis();
        return new DoctorId(id);
    }
    
    /**
     * 获取医生ID值
     * 
     * @return 医生ID值
     * @author daidasheng
     * @date 2024-12-27
     */
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoctorId doctorId = (DoctorId) o;
        return Objects.equals(value, doctorId.value);
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

