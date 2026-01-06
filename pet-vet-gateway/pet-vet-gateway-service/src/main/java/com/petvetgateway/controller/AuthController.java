package com.petvetgateway.controller;

import com.petvetgateway.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 授权控制器
 * 
 * 提供统一的授权登录接口，支持企业微信、微信、支付宝、QQ等多种授权方式
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    /**
     * 授权服务
     */
    private final AuthService authService;
    
    /**
     * 获取授权URL
     * 
     * @param type 授权类型（wework/wechat/alipay/qq）
     * @param redirectUri 重定向URI
     * @return 授权URL
     * @author daidasheng
     * @date 2024-12-27
     */
    @GetMapping("/url")
    public ResponseEntity<Map<String, Object>> getAuthUrl(
            @RequestParam String type,
            @RequestParam(required = false, defaultValue = "") String redirectUri) {
        log.info("获取授权URL，类型: {}, 重定向URI: {}", type, redirectUri);
        
        try {
            String authUrl = authService.getAuthUrl(type, redirectUri);
            
            if (authUrl == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "不支持的授权类型或授权功能未启用");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("authUrl", authUrl));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取授权URL失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取授权URL失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 通过授权码进行授权
     * 
     * @param type 授权类型（wework/wechat/alipay/qq）
     * @param code 授权码
     * @return 授权结果，包含Token和用户信息
     * @author daidasheng
     * @date 2024-12-27
     */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> authCallback(
            @RequestParam String type,
            @RequestParam String code) {
        log.info("授权回调，类型: {}, 授权码: {}", type, code);
        
        try {
            Map<String, Object> result = authService.authByCode(type, code);
            
            if (result == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "授权失败，请重试");
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "授权成功");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("授权回调处理失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "授权失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}

