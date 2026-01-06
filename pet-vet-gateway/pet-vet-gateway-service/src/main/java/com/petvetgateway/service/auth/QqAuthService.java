package com.petvetgateway.service.auth;

import com.petvetgateway.config.GatewayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * QQ授权服务
 * 
 * 实现QQ授权登录功能
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QqAuthService {
    
    /**
     * 网关配置
     */
    private final GatewayConfig gatewayConfig;
    
    /**
     * Web客户端（用于调用QQ API）
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
        GatewayConfig.QqConfig config = gatewayConfig.getAuth().getQq();
        if (config == null || config.getUrls() == null) {
            log.error("QQ配置或URL配置为空");
            return null;
        }
        
        String state = String.valueOf(System.currentTimeMillis());
        String authUrlTemplate = config.getUrls().getAuthUrlTemplate();
        if (authUrlTemplate == null || authUrlTemplate.isEmpty()) {
            log.error("QQ授权URL模板为空");
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
            GatewayConfig.QqConfig config = gatewayConfig.getAuth().getQq();
            
            // 1. 通过code获取Access Token
            GatewayConfig.QqUrlConfig urlConfig = config.getUrls();
            if (urlConfig == null || urlConfig.getAccessTokenUrlTemplate() == null) {
                log.error("QQ Access Token URL模板为空");
                return null;
            }
            
            String tokenUrl = String.format(urlConfig.getAccessTokenUrlTemplate(), 
                    config.getAppId(), 
                    config.getAppKey(), 
                    code,
                    java.net.URLEncoder.encode(config.getRedirectUri(), java.nio.charset.StandardCharsets.UTF_8));
            
            String tokenResponse = webClient.get()
                    .uri(tokenUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (tokenResponse == null || tokenResponse.contains("error")) {
                log.error("获取QQ Access Token失败: {}", tokenResponse);
                return null;
            }
            
            // 解析Access Token
            String accessToken = parseAccessToken(tokenResponse);
            if (accessToken == null) {
                log.error("解析QQ Access Token失败");
                return null;
            }
            
            // 2. 通过Access Token获取OpenId
            String openIdUrlTemplate = urlConfig.getOpenIdUrlTemplate();
            if (openIdUrlTemplate == null || openIdUrlTemplate.isEmpty()) {
                log.error("QQ OpenId URL模板为空");
                return null;
            }
            
            String openIdUrl = String.format(openIdUrlTemplate, accessToken);
            String openIdResponse = webClient.get()
                    .uri(openIdUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (openIdResponse == null || openIdResponse.contains("error")) {
                log.error("获取QQ OpenId失败: {}", openIdResponse);
                return null;
            }
            
            String openId = parseOpenId(openIdResponse);
            if (openId == null) {
                log.error("解析QQ OpenId失败");
                return null;
            }
            
            // 3. 通过Access Token和OpenId获取用户信息
            String userInfoUrlTemplate = urlConfig.getUserInfoUrlTemplate();
            if (userInfoUrlTemplate == null || userInfoUrlTemplate.isEmpty()) {
                log.error("QQ用户信息URL模板为空");
                return null;
            }
            
            String userInfoUrl = String.format(userInfoUrlTemplate, accessToken, config.getAppId(), openId);
            Map<String, Object> userInfoResponse = webClient.get()
                    .uri(userInfoUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (userInfoResponse == null || (Integer) userInfoResponse.get("ret") != 0) {
                Integer ret = (Integer) userInfoResponse.get("ret");
                log.error("获取QQ用户信息失败，错误码: {}", ret);
                return null;
            }
            
            // 4. 构建用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("openId", openId);
            userInfo.put("nickname", userInfoResponse.get("nickname"));
            userInfo.put("figureurl", userInfoResponse.get("figureurl"));
            userInfo.put("authType", "qq");
            
            return userInfo;
        } catch (Exception e) {
            log.error("QQ授权失败", e);
            return null;
        }
    }
    
    /**
     * 解析Access Token
     * 
     * @param response 响应字符串
     * @return Access Token
     * @author daidasheng
     * @date 2024-12-27
     */
    private String parseAccessToken(String response) {
        // QQ返回格式: access_token=xxx&expires_in=7776000&refresh_token=xxx
        String[] params = response.split("&");
        for (String param : params) {
            if (param.startsWith("access_token=")) {
                return param.substring("access_token=".length());
            }
        }
        return null;
    }
    
    /**
     * 解析OpenId
     * 
     * @param response 响应字符串（JSON格式）
     * @return OpenId
     * @author daidasheng
     * @date 2024-12-27
     */
    private String parseOpenId(String response) {
        // QQ返回格式: callback({"client_id":"xxx","openid":"xxx"});
        // 需要提取JSON部分
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        if (start >= 0 && end > start) {
            String json = response.substring(start, end + 1);
            // 这里应该使用JSON解析器，简化处理
            int openIdStart = json.indexOf("\"openid\":\"");
            if (openIdStart >= 0) {
                openIdStart += "\"openid\":\"".length();
                int openIdEnd = json.indexOf("\"", openIdStart);
                if (openIdEnd > openIdStart) {
                    return json.substring(openIdStart, openIdEnd);
                }
            }
        }
        return null;
    }
}

