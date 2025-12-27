package com.petvetai.app.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 医生列表查询请求 DTO（按地址编码）
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorListByAddressReq {
    
    /**
     * 地址编码（行政区划编码）
     */
    @NotBlank(message = "地址编码不能为空")
    private String addressCode;
    
    /**
     * 医生状态：0-待审核，1-已审核，2-已禁用，3-审核失败（可选，默认查询已审核）
     */
    private Integer status;
}

