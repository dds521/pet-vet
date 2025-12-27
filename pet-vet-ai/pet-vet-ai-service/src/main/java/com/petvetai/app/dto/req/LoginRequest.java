package com.petvetai.app.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信登录请求 DTO
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    /**
     * 微信登录凭证code
     */
    @NotBlank(message = "微信登录凭证code不能为空")
    private String code;
}

