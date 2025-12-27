package com.petvetai.app.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 医生更新请求 DTO
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorUpdateReq {
    
    /**
     * 医生姓名
     */
    private String name;
    
    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * 机构名称
     */
    private String institutionName;
    
    /**
     * 机构地址
     */
    private String institutionAddress;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 区县
     */
    private String district;
    
    /**
     * 详细地址
     */
    private String detail;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 地址编码
     */
    private String addressCode;
    
    /**
     * 执业证书编号
     */
    private String licenseNumber;
    
    /**
     * 执业证书照片URL
     */
    private String licensePhotoUrl;
    
    /**
     * 资格证书编号
     */
    private String certificateNumber;
    
    /**
     * 资格证书照片URL
     */
    private String certificatePhotoUrl;
    
    /**
     * 专业领域
     */
    private String specialty;
    
    /**
     * 从业年限
     */
    private Integer yearsOfExperience;
    
    /**
     * 教育背景
     */
    private String education;
}

