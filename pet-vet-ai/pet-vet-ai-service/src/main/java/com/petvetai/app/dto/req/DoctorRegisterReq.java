package com.petvetai.app.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 医生注册请求 DTO
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRegisterReq {
    
    /**
     * 医生类型：1-个人，2-机构
     */
    @NotNull(message = "医生类型不能为空")
    private Integer type;
    
    /**
     * 医生姓名
     */
    @NotBlank(message = "医生姓名不能为空")
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
    @NotBlank(message = "手机号不能为空")
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
     * 机构名称（机构类型时必填）
     */
    private String institutionName;
    
    /**
     * 机构地址（机构类型时必填）
     */
    private String institutionAddress;
    
    /**
     * 省份
     */
    @NotBlank(message = "省份不能为空")
    private String province;
    
    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    private String city;
    
    /**
     * 区县
     */
    private String district;
    
    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
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
     * 地址编码（行政区划编码）
     */
    private String addressCode;
    
    /**
     * 执业证书编号
     */
    @NotBlank(message = "执业证书编号不能为空")
    private String licenseNumber;
    
    /**
     * 执业证书照片URL
     */
    @NotBlank(message = "执业证书照片不能为空")
    private String licensePhotoUrl;
    
    /**
     * 资格证书编号
     */
    @NotBlank(message = "资格证书编号不能为空")
    private String certificateNumber;
    
    /**
     * 资格证书照片URL
     */
    @NotBlank(message = "资格证书照片不能为空")
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

