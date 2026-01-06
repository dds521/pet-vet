package com.petvetgateway.service.auth;

import com.petvetgateway.config.GatewayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝授权服务
 * 
 * 实现支付宝授权登录功能
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayAuthService {
    
    /**
     * 网关配置
     */
    private final GatewayConfig gatewayConfig;
    
    /**
     * Web客户端（用于调用支付宝API）
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
        GatewayConfig.AlipayConfig config = gatewayConfig.getAuth().getAlipay();
        if (config == null || config.getUrls() == null) {
            log.error("支付宝配置或URL配置为空");
            return null;
        }
        
        String state = String.valueOf(System.currentTimeMillis());
        String authUrlTemplate = config.getUrls().getAuthUrlTemplate();
        if (authUrlTemplate == null || authUrlTemplate.isEmpty()) {
            log.error("支付宝授权URL模板为空");
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
            GatewayConfig.AlipayConfig config = gatewayConfig.getAuth().getAlipay();
            
            // 支付宝需要先通过code获取access_token，然后再获取用户信息
            // 这里简化处理，实际应该调用支付宝的API
            // 注意：支付宝的API调用需要签名，这里只是示例
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("openId", code); // 临时使用code作为openId
            userInfo.put("authType", "alipay");
            
            log.warn("支付宝授权功能需要实现完整的API调用和签名逻辑");
            
            return userInfo;
        } catch (Exception e) {
            log.error("支付宝授权失败", e);
            return null;
        }
    }
}

