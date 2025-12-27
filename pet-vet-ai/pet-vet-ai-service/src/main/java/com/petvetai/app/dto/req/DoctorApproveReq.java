package com.petvetai.app.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 医生审核请求 DTO
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorApproveReq {
    
    /**
     * 是否通过审核
     */
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;
    
    /**
     * 审核失败原因（未通过时必填）
     */
    private String rejectReason;
}

