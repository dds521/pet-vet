package com.petvetai.infrastructure.persistence.doctor.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petvet.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 医生持久化对象
 * 
 * 用于数据库持久化，对应 vet_ai_doctor 表
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vet_ai_doctor")
public class VetAiDoctorPO extends BaseEntity {
    
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 医生类型：1-个人，2-机构
     */
    private Integer type;
    
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
     * 完整地址
     */
    private String fullAddress;
    
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
    
    /**
     * 医生状态：0-待审核，1-已审核，2-已禁用，3-审核失败
     */
    private Integer status;
    
    /**
     * 审核通过时间
     */
    private LocalDateTime approveTime;
    
    /**
     * 审核失败原因
     */
    private String rejectReason;
}

