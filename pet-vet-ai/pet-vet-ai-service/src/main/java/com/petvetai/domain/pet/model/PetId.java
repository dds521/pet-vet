package com.petvetai.domain.pet.model;

import java.util.Objects;

/**
 * 宠物ID值对象
 * 
 * 值对象，不可变，通过值来区分
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public class PetId {
    
    private final Long value;
    
    private PetId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("宠物ID必须大于0");
        }
        this.value = value;
    }
    
    /**
     * 创建宠物ID值对象
     * 
     * @param value 宠物ID值
     * @return 宠物ID值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public static PetId of(Long value) {
        return new PetId(value);
    }
    
    /**
     * 获取宠物ID值
     * 
     * @return 宠物ID值
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
        PetId petId = (PetId) o;
        return Objects.equals(value, petId.value);
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

