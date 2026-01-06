package com.petvetgateway.service.auth;

import com.petvetgateway.config.GatewayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信授权服务
 * 
 * 实现微信授权登录功能
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatAuthService {
    
    /**
     * 网关配置
     */
    private final GatewayConfig gatewayConfig;
    
    /**
     * Web客户端（用于调用微信API）
     */
    private final WebClient webClient = WebClient.builder().build();
    
    /**
     * 获取授权URL
     * 
     * @param redirectUri 重定向URI
     * @return 授权URL
     * @author daidasheng
     * @date 2024-12-27
     */
    public String getAuthUrl(String redirectUri) {
        GatewayConfig.WeChatConfig config = gatewayConfig.getAuth().getWechat();
        if (config == null || config.getUrls() == null) {
            log.error("微信配置或URL配置为空");
            return null;
        }
        
        String state = String.valueOf(System.currentTimeMillis());
        String authUrlTemplate = config.getUrls().getAuthUrlTemplate();
        if (authUrlTemplate == null || authUrlTemplate.isEmpty()) {
            log.error("微信授权URL模板为空");
            return null;
        }
        
        return String.format(authUrlTemplate, 
                config.getAppId(), 
                java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8),
                state);
    }
    
    /**
     * 通过授权码获取用户信息
     * 
     * @param code 授权码
     * @return 用户信息
     * @author daidasheng
     * @date 2024-12-27
     */
    public Map<String, Object> getUserInfoByCode(String code) {
        try {
            // 1. 通过code获取Access Token
            GatewayConfig.WeChatConfig config = gatewayConfig.getAuth().getWechat();
            if (config == null || config.getUrls() == null) {
                log.error("微信配置或URL配置为空");
                return null;
            }
            
            String accessTokenUrlTemplate = config.getUrls().getAccessTokenUrlTemplate();
            if (accessTokenUrlTemplate == null || accessTokenUrlTemplate.isEmpty()) {
                log.error("微信Access Token URL模板为空");
                return null;
            }
            
            String tokenUrl = String.format(accessTokenUrlTemplate, config.getAppId(), config.getAppSecret(), code);
            
            Map<String, Object> tokenResponse = webClient.get()
                    .uri(tokenUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (tokenResponse == null || tokenResponse.containsKey("errcode")) {
                Integer errcode = (Integer) tokenResponse.get("errcode");
                log.error("获取微信Access Token失败，错误码: {}", errcode);
                return null;
            }
            
            String accessToken = (String) tokenResponse.get("access_token");
            String openId = (String) tokenResponse.get("openid");
            
            // 2. 通过Access Token和OpenId获取用户信息
            String userInfoUrlTemplate = config.getUrls().getUserInfoUrlTemplate();
            if (userInfoUrlTemplate == null || userInfoUrlTemplate.isEmpty()) {
                log.error("微信用户信息URL模板为空");
                return null;
            }
            
            String userInfoUrl = String.format(userInfoUrlTemplate, accessToken, openId);
            
            Map<String, Object> userInfoResponse = webClient.get()
                    .uri(userInfoUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (userInfoResponse == null || userInfoResponse.containsKey("errcode")) {
                Integer errcode = (Integer) userInfoResponse.get("errcode");
                log.error("获取微信用户信息失败，错误码: {}", errcode);
                return null;
            }
            
            // 3. 构建用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("openId", openId);
            userInfo.put("nickname", userInfoResponse.get("nickname"));
            userInfo.put("headimgurl", userInfoResponse.get("headimgurl"));
            userInfo.put("unionid", userInfoResponse.get("unionid"));
            userInfo.put("authType", "wechat");
            
            return userInfo;
        } catch (Exception e) {
            log.error("微信授权失败", e);
            return null;
        }
    }
}

