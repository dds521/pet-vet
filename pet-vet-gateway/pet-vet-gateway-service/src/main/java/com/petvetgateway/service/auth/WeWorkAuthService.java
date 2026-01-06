package com.petvetgateway.service.auth;

import com.petvetgateway.config.GatewayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信授权服务
 * 
 * 实现企业微信扫码授权登录功能
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeWorkAuthService {
    
    /**
     * 网关配置
     */
    private final GatewayConfig gatewayConfig;
    
    /**
     * Web客户端（用于调用企业微信API）
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
        GatewayConfig.WeWorkConfig config = gatewayConfig.getAuth().getWework();
        if (config == null || config.getUrls() == null) {
            log.error("企业微信配置或URL配置为空");
            return null;
        }
        
        String state = String.valueOf(System.currentTimeMillis());
        String authUrlTemplate = config.getUrls().getAuthUrlTemplate();
        if (authUrlTemplate == null || authUrlTemplate.isEmpty()) {
            log.error("企业微信授权URL模板为空");
            return null;
        }
        
        return String.format(authUrlTemplate, 
                config.getCorpId(), 
                config.getAgentId(), 
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
            // 1. 获取Access Token
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取企业微信Access Token失败");
                return null;
            }
            
            // 2. 通过code获取用户信息
            GatewayConfig.WeWorkUrlConfig urlConfig = gatewayConfig.getAuth().getWework().getUrls();
            if (urlConfig == null || urlConfig.getUserInfoUrlTemplate() == null) {
                log.error("企业微信用户信息URL模板为空");
                return null;
            }
            String userInfoUrl = String.format(urlConfig.getUserInfoUrlTemplate(), accessToken, code);
            
            Map<String, Object> response = webClient.get()
                    .uri(userInfoUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response == null || response.containsKey("errcode")) {
                Integer errcode = (Integer) response.get("errcode");
                log.error("获取企业微信用户信息失败，错误码: {}", errcode);
                return null;
            }
            
            // 3. 构建用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", response.get("UserId")); // 企业微信用户ID
            userInfo.put("openId", response.get("OpenId")); // 企业微信OpenId
            userInfo.put("deviceId", response.get("DeviceId")); // 设备ID
            userInfo.put("authType", "wework");
            
            return userInfo;
        } catch (Exception e) {
            log.error("企业微信授权失败", e);
            return null;
        }
    }
    
    /**
     * 获取Access Token
     * 
     * @return Access Token
     * @author daidasheng
     * @date 2024-12-27
     */
    private String getAccessToken() {
        GatewayConfig.WeWorkConfig config = gatewayConfig.getAuth().getWework();
        if (config == null || config.getUrls() == null) {
            log.error("企业微信配置或URL配置为空");
            return null;
        }
        
        String accessTokenUrlTemplate = config.getUrls().getAccessTokenUrlTemplate();
        if (accessTokenUrlTemplate == null || accessTokenUrlTemplate.isEmpty()) {
            log.error("企业微信Access Token URL模板为空");
            return null;
        }
        
        String tokenUrl = String.format(accessTokenUrlTemplate, config.getCorpId(), config.getSecret());
        
        try {
            Map<String, Object> response = webClient.get()
                    .uri(tokenUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("access_token")) {
                return (String) response.get("access_token");
            }
            
            Integer errcode = (Integer) response.get("errcode");
            log.error("获取企业微信Access Token失败，错误码: {}", errcode);
            return null;
        } catch (Exception e) {
            log.error("获取企业微信Access Token异常", e);
            return null;
        }
    }
}

