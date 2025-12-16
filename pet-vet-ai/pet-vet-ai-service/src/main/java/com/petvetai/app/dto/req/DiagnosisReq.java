package com.petvetai.app.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 诊断请求 DTO
 * 
 * @author daidasheng
 * @date 2024-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisReq {
    
    /**
     * 宠物ID
     */
    @NotNull(message = "宠物ID不能为空")
    private Long petId;
    
    /**
     * 症状描述
     */
    @NotBlank(message = "症状描述不能为空")
    private String symptomDesc;
}
