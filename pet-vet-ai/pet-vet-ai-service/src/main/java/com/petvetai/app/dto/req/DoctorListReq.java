package com.petvetai.app.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 医生列表查询请求 DTO（附近医生）
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorListReq {
    
    /**
     * 经度
     */
    @NotNull(message = "经度不能为空")
    private Double longitude;
    
    /**
     * 纬度
     */
    @NotNull(message = "纬度不能为空")
    private Double latitude;
    
    /**
     * 最大距离（公里），默认10公里
     */
    private Double maxDistance;
    
    /**
     * 返回数量限制，默认20条
     */
    private Integer limit;
}

