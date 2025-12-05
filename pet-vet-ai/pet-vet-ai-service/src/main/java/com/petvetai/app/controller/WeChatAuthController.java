package com.petvetai.app.controller;

import com.petvetai.app.domain.WeChatUser;
import com.petvetai.app.service.WeChatAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信认证Controller
 * 
 * 提供微信小程序登录相关的API接口
 * 
 * @author PetVetAI Team
 * @date 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/wechat")
public class WeChatAuthController {
    
    @Autowired
    private WeChatAuthService weChatAuthService;
    
    /**
     * 微信登录接口
     * 
     * 接收小程序端传来的code，调用微信API获取openId和sessionKey
     * 然后创建或更新用户，生成JWT Token返回
     * 
     * @param request 登录请求，包含code
     * @return 登录结果，包含token和用户信息
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        log.info("收到微信登录请求，code: {}", request.getCode());
        
        try {
            // 调用登录服务
            WeChatAuthService.LoginResult result = weChatAuthService.login(request.getCode());
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", result.getToken());
            
            // 用户信息（不包含敏感信息）
            WeChatUser user = result.getUser();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("openId", user.getOpenId());
            userInfo.put("nickName", user.getNickName());
            userInfo.put("avatarUrl", user.getAvatarUrl());
            userInfo.put("gender", user.getGender());
            response.put("user", userInfo);
            
            log.info("微信登录成功，userId: {}", user.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("微信登录失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "登录失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 更新用户信息接口
     * 
     * 用于用户授权后更新昵称、头像等信息
     * 
     * @param request 更新用户信息请求
     * @return 更新结果
     */
    @PostMapping("/userinfo")
    public ResponseEntity<Map<String, Object>> updateUserInfo(@RequestBody UpdateUserInfoRequest request) {
        log.info("收到更新用户信息请求，openId: {}", request.getOpenId());
        
        try {
            // 构建用户信息对象
            WeChatAuthService.WeChatUserInfo userInfo = new WeChatAuthService.WeChatUserInfo();
            userInfo.setNickName(request.getNickName());
            userInfo.setAvatarUrl(request.getAvatarUrl());
            userInfo.setGender(request.getGender());
            userInfo.setCountry(request.getCountry());
            userInfo.setProvince(request.getProvince());
            userInfo.setCity(request.getCity());
            userInfo.setLanguage(request.getLanguage());
            
            // 更新用户信息
            weChatAuthService.updateUserInfo(request.getOpenId(), userInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "更新成功");
            
            log.info("更新用户信息成功，openId: {}", request.getOpenId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 登录请求DTO
     */
    public static class LoginRequest {
        /**
         * 微信登录凭证code
         * 由小程序端调用wx.login()获取
         */
        private String code;
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
    }
    
    /**
     * 更新用户信息请求DTO
     */
    public static class UpdateUserInfoRequest {
        /**
         * 微信openId
         */
        private String openId;
        
        /**
         * 用户昵称
         */
        private String nickName;
        
        /**
         * 用户头像URL
         */
        private String avatarUrl;
        
        /**
         * 用户性别：0-未知，1-男，2-女
         */
        private Integer gender;
        
        /**
         * 用户所在国家
         */
        private String country;
        
        /**
         * 用户所在省份
         */
        private String province;
        
        /**
         * 用户所在城市
         */
        private String city;
        
        /**
         * 用户语言
         */
        private String language;
        
        // Getters and Setters
        public String getOpenId() {
            return openId;
        }
        
        public void setOpenId(String openId) {
            this.openId = openId;
        }
        
        public String getNickName() {
            return nickName;
        }
        
        public void setNickName(String nickName) {
            this.nickName = nickName;
        }
        
        public String getAvatarUrl() {
            return avatarUrl;
        }
        
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
        
        public Integer getGender() {
            return gender;
        }
        
        public void setGender(Integer gender) {
            this.gender = gender;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String getProvince() {
            return province;
        }
        
        public void setProvince(String province) {
            this.province = province;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getLanguage() {
            return language;
        }
        
        public void setLanguage(String language) {
            this.language = language;
        }
    }
}

