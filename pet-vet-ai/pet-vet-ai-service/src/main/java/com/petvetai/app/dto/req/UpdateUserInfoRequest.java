package com.petvetai.app.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户信息请求 DTO
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoRequest {
    
    /**
     * 微信openId
     */
    @NotBlank(message = "微信openId不能为空")
    private String openId;
    
    /**
     * 昵称
     */
    private String nickName;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;
    
    /**
     * 国家
     */
    private String country;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 语言
     */
    private String language;
}

