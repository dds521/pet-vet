package com.petvetai.app.dto.resp;

import com.petvetai.domain.doctor.model.Doctor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 医生详情响应 DTO
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDetailResp {
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
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
    
    /**
     * 医生状态：0-待审核，1-已审核，2-已禁用，3-审核失败
     */
    private Integer status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 审核通过时间
     */
    private java.time.LocalDateTime approveTime;
    
    /**
     * 从领域对象创建响应DTO
     * 
     * @param doctor 医生聚合根
     * @return 响应DTO
     * @author daidasheng
     * @date 2024-12-27
     */
    public static DoctorDetailResp from(Doctor doctor) {
        return DoctorDetailResp.builder()
                .doctorId(doctor.getId().getValue())
                .type(doctor.getType().getCode())
                .name(doctor.getDoctorInfo().getName())
                .gender(doctor.getDoctorInfo().getGender())
                .age(doctor.getDoctorInfo().getAge())
                .phone(doctor.getDoctorInfo().getPhone())
                .email(doctor.getDoctorInfo().getEmail())
                .avatarUrl(doctor.getDoctorInfo().getAvatarUrl())
                .bio(doctor.getDoctorInfo().getBio())
                .institutionName(doctor.getDoctorInfo().getInstitutionName())
                .institutionAddress(doctor.getDoctorInfo().getInstitutionAddress())
                .province(doctor.getAddress().getProvince())
                .city(doctor.getAddress().getCity())
                .district(doctor.getAddress().getDistrict())
                .detail(doctor.getAddress().getDetail())
                .fullAddress(doctor.getAddress().getFullAddress())
                .longitude(doctor.getAddress().getLongitude())
                .latitude(doctor.getAddress().getLatitude())
                .addressCode(doctor.getAddress().getAddressCode())
                .licenseNumber(doctor.getQualification().getLicenseNumber())
                .licensePhotoUrl(doctor.getQualification().getLicensePhotoUrl())
                .certificateNumber(doctor.getQualification().getCertificateNumber())
                .certificatePhotoUrl(doctor.getQualification().getCertificatePhotoUrl())
                .specialty(doctor.getQualification().getSpecialty())
                .yearsOfExperience(doctor.getQualification().getYearsOfExperience())
                .education(doctor.getQualification().getEducation())
                .status(doctor.getStatus().getCode())
                .statusDesc(doctor.getStatus().getDescription())
                .approveTime(doctor.getApproveTime())
                .build();
    }
}

