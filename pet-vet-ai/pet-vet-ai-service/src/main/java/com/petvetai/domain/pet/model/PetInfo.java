package com.petvetai.domain.pet.model;

import java.util.Objects;

/**
 * 宠物信息值对象
 * 
 * 封装宠物的基本信息，不可变
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public class PetInfo {
    
    private final String name;
    private final String breed;
    private final Integer age;
    
    /**
     * 构造函数
     * 
     * @param name 宠物名称
     * @param breed 宠物品种
     * @param age 宠物年龄
     * @author daidasheng
     * @date 2024-12-20
     */
    public PetInfo(String name, String breed, Integer age) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("宠物名称不能为空");
        }
        this.name = name;
        this.breed = breed;
        this.age = age;
    }
    
    /**
     * 创建宠物信息值对象
     * 
     * @param name 宠物名称
     * @param breed 宠物品种
     * @param age 宠物年龄
     * @return 宠物信息值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public static PetInfo of(String name, String breed, Integer age) {
        return new PetInfo(name, breed, age);
    }
    
    /**
     * 获取宠物名称
     * 
     * @return 宠物名称
     * @author daidasheng
     * @date 2024-12-20
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取宠物品种
     * 
     * @return 宠物品种
     * @author daidasheng
     * @date 2024-12-20
     */
    public String getBreed() {
        return breed;
    }
    
    /**
     * 获取宠物年龄
     * 
     * @return 宠物年龄
     * @author daidasheng
     * @date 2024-12-20
     */
    public Integer getAge() {
        return age;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PetInfo petInfo = (PetInfo) o;
        return Objects.equals(name, petInfo.name) &&
               Objects.equals(breed, petInfo.breed) &&
               Objects.equals(age, petInfo.age);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, breed, age);
    }
    
    @Override
    public String toString() {
        return "PetInfo{" +
                "name='" + name + '\'' +
                ", breed='" + breed + '\'' +
                ", age=" + age +
                '}';
    }
}

