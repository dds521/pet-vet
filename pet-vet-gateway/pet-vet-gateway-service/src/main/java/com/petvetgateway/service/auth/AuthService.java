package com.petvetgateway.service.auth;

import java.util.Map;

/**
 * 授权服务接口
 * 
 * 提供统一的授权服务，支持多种授权方式
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
public interface AuthService {
    
    /**
     * 获取授权URL
     * 
     * @param authType 授权类型（wework/wechat/alipay/qq）
     * @param redirectUri 重定向URI
     * @return 授权URL
     * @author daidasheng
     * @date 2024-12-27
     */
    String getAuthUrl(String authType, String redirectUri);
    
    /**
     * 通过授权码获取用户信息并生成JWT Token
     * 
     * @param authType 授权类型
     * @param code 授权码
     * @return 包含Token和用户信息的Map
     * @author daidasheng
     * @date 2024-12-27
     */
    Map<String, Object> authByCode(String authType, String code);
}

