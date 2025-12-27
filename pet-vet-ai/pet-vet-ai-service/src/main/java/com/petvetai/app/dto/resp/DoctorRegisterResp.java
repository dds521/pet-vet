package com.petvetai.app.dto.resp;

import com.petvetai.domain.doctor.model.Doctor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 医生注册响应 DTO
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRegisterResp {
    
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
     * 医生状态：0-待审核，1-已审核，2-已禁用，3-审核失败
     */
    private Integer status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 从领域对象创建响应DTO
     * 
     * @param doctor 医生聚合根
     * @return 响应DTO
     * @author daidasheng
     * @date 2024-12-27
     */
    public static DoctorRegisterResp from(Doctor doctor) {
        return DoctorRegisterResp.builder()
                .doctorId(doctor.getId().getValue())
                .name(doctor.getDoctorInfo().getName())
                .type(doctor.getType().getCode())
                .status(doctor.getStatus().getCode())
                .statusDesc(doctor.getStatus().getDescription())
                .build();
    }
}

