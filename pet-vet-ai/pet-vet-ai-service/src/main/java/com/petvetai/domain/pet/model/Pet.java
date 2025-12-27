package com.petvetai.domain.pet.model;

import java.util.Objects;

/**
 * 宠物聚合根
 * 
 * 宠物是宠物域的聚合根，负责管理宠物相关的业务逻辑
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public class Pet {
    
    private PetId id;
    private PetInfo petInfo;
    
    /**
     * 私有构造函数，防止直接创建
     * 
     * @author daidasheng
     * @date 2024-12-20
     */
    private Pet() {
    }
    
    /**
     * 创建宠物
     * 
     * @param petInfo 宠物信息
     * @return 宠物聚合根
     * @author daidasheng
     * @date 2024-12-20
     */
    public static Pet create(PetInfo petInfo) {
        Pet pet = new Pet();
        pet.petInfo = petInfo;
        return pet;
    }
    
    /**
     * 从已有数据重建宠物（用于从数据库加载）
     * 
     * @param id 宠物ID
     * @param petInfo 宠物信息
     * @return 宠物聚合根
     * @author daidasheng
     * @date 2024-12-20
     */
    public static Pet reconstruct(PetId id, PetInfo petInfo) {
        Pet pet = new Pet();
        pet.id = id;
        pet.petInfo = petInfo;
        return pet;
    }
    
    /**
     * 更新宠物信息
     * 
     * @param petInfo 新的宠物信息
     * @author daidasheng
     * @date 2024-12-20
     */
    public void updateInfo(PetInfo petInfo) {
        if (petInfo == null) {
            throw new IllegalArgumentException("宠物信息不能为空");
        }
        this.petInfo = petInfo;
    }
    
    // Getters
    public PetId getId() {
        return id;
    }
    
    public PetInfo getPetInfo() {
        return petInfo;
    }
    
    // 包级私有方法，用于转换器设置字段
    void setIdInternal(PetId id) {
        this.id = id;
    }
    
    void setPetInfoInternal(PetInfo petInfo) {
        this.petInfo = petInfo;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pet pet = (Pet) o;
        return Objects.equals(id, pet.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

