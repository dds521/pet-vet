package com.petvetai.domain.doctor.model;

import java.util.Objects;

/**
 * 医生信息值对象
 * 
 * 包含医生的基本信息和机构信息
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
public class DoctorInfo {
    
    /**
     * 医生姓名
     */
    private final String name;
    
    /**
     * 性别：0-未知，1-男，2-女
     */
    private final Integer gender;
    
    /**
     * 年龄
     */
    private final Integer age;
    
    /**
     * 手机号
     */
    private final String phone;
    
    /**
     * 邮箱
     */
    private final String email;
    
    /**
     * 头像URL
     */
    private final String avatarUrl;
    
    /**
     * 个人简介
     */
    private final String bio;
    
    /**
     * 机构名称（机构类型时必填）
     */
    private final String institutionName;
    
    /**
     * 机构地址（机构类型时必填）
     */
    private final String institutionAddress;
    
    /**
     * 私有构造函数
     * 
     * @param name 医生姓名
     * @param gender 性别
     * @param age 年龄
     * @param phone 手机号
     * @param email 邮箱
     * @param avatarUrl 头像URL
     * @param bio 个人简介
     * @param institutionName 机构名称
     * @param institutionAddress 机构地址
     * @author daidasheng
     * @date 2024-12-27
     */
    private DoctorInfo(String name, Integer gender, Integer age, String phone, String email,
                       String avatarUrl, String bio, String institutionName, String institutionAddress) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.phone = phone;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.institutionName = institutionName;
        this.institutionAddress = institutionAddress;
    }
    
    /**
     * 创建医生信息值对象（个人类型）
     * 
     * @param name 医生姓名
     * @param gender 性别
     * @param age 年龄
     * @param phone 手机号
     * @param email 邮箱
     * @param avatarUrl 头像URL
     * @param bio 个人简介
     * @return 医生信息值对象
     * @author daidasheng
     * @date 2024-12-27
     */
    public static DoctorInfo ofIndividual(String name, Integer gender, Integer age, String phone,
                                         String email, String avatarUrl, String bio) {
        return new DoctorInfo(name, gender, age, phone, email, avatarUrl, bio, null, null);
    }
    
    /**
     * 创建医生信息值对象（机构类型）
     * 
     * @param name 医生姓名
     * @param gender 性别
     * @param age 年龄
     * @param phone 手机号
     * @param email 邮箱
     * @param avatarUrl 头像URL
     * @param bio 个人简介
     * @param institutionName 机构名称
     * @param institutionAddress 机构地址
     * @return 医生信息值对象
     * @author daidasheng
     * @date 2024-12-27
     */
    public static DoctorInfo ofInstitution(String name, Integer gender, Integer age, String phone,
                                           String email, String avatarUrl, String bio,
                                           String institutionName, String institutionAddress) {
        if (institutionName == null || institutionName.isEmpty()) {
            throw new IllegalArgumentException("机构名称不能为空");
        }
        if (institutionAddress == null || institutionAddress.isEmpty()) {
            throw new IllegalArgumentException("机构地址不能为空");
        }
        return new DoctorInfo(name, gender, age, phone, email, avatarUrl, bio, 
                            institutionName, institutionAddress);
    }
    
    /**
     * 判断是否为机构类型
     * 
     * @return 是否为机构类型
     * @author daidasheng
     * @date 2024-12-27
     */
    public boolean isInstitution() {
        return institutionName != null && !institutionName.isEmpty();
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public Integer getGender() {
        return gender;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public String getBio() {
        return bio;
    }
    
    public String getInstitutionName() {
        return institutionName;
    }
    
    public String getInstitutionAddress() {
        return institutionAddress;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoctorInfo that = (DoctorInfo) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(phone, that.phone) &&
               Objects.equals(email, that.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, phone, email);
    }
}

