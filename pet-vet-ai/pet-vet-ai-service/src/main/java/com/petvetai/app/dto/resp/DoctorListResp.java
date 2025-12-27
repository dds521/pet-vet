package com.petvetai.app.dto.resp;

import com.petvetai.domain.doctor.model.Doctor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 医生列表响应 DTO
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorListResp {
    
    /**
     * 医生列表
     */
    private List<DoctorItem> doctors;
    
    /**
     * 总数
     */
    private Integer total;
    
    /**
     * 医生项
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorItem {
        
        /**
         * 医生ID
         */
        private Long doctorId;
        
        /**
         * 医生姓名
         */
        private String name;
        
        /**
         * 医生类型：1-个人，2-机构
         */
        private Integer type;
        
        /**
         * 机构名称（机构类型时显示）
         */
        private String institutionName;
        
        /**
         * 头像URL
         */
        private String avatarUrl;
        
        /**
         * 个人简介
         */
        private String bio;
        
        /**
         * 专业领域
         */
        private String specialty;
        
        /**
         * 从业年限
         */
        private Integer yearsOfExperience;
        
        /**
         * 完整地址
         */
        private String fullAddress;
        
        /**
         * 距离（公里），仅附近医生查询时返回
         */
        private Double distance;
        
        /**
         * 从领域对象创建响应DTO
         * 
         * @param doctor 医生聚合根
         * @param distance 距离（公里），可为null
         * @return 响应DTO
         * @author daidasheng
         * @date 2024-12-27
         */
        public static DoctorItem from(Doctor doctor, Double distance) {
            return DoctorItem.builder()
                    .doctorId(doctor.getId().getValue())
                    .name(doctor.getDoctorInfo().getName())
                    .type(doctor.getType().getCode())
                    .institutionName(doctor.getDoctorInfo().getInstitutionName())
                    .avatarUrl(doctor.getDoctorInfo().getAvatarUrl())
                    .bio(doctor.getDoctorInfo().getBio())
                    .specialty(doctor.getQualification().getSpecialty())
                    .yearsOfExperience(doctor.getQualification().getYearsOfExperience())
                    .fullAddress(doctor.getAddress().getFullAddress())
                    .distance(distance)
                    .build();
        }
    }
}

