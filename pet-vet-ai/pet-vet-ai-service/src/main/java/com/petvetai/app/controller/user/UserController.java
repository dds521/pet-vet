package com.petvetai.app.controller.user;

import com.petvetai.app.application.user.UserApplicationService;
import com.petvetai.app.application.user.UserApplicationService.WeChatLoginResult;
import com.petvetai.app.dto.req.LoginRequest;
import com.petvetai.app.dto.req.UpdateUserInfoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器（DDD改造后）
 * 
 * 处理HTTP请求，参数校验，返回响应
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserApplicationService userApplicationService;
    
    /**
     * 微信登录
     * 
     * @param request 登录请求
     * @return 登录响应
     * @author daidasheng
     * @date 2024-12-20
     */
    @PostMapping("/wechat/login")
    public ResponseEntity<Map<String, Object>> weChatLogin(@Valid @RequestBody LoginRequest request) {
        log.info("收到微信登录请求，code: {}", request.getCode());
        
        try {
            // 调用应用服务
            WeChatLoginResult result = userApplicationService.weChatLogin(request.getCode());
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", result.getToken());
            
            // 用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", result.getUserId());
            userInfo.put("openId", result.getOpenId());
            userInfo.put("nickName", result.getNickName());
            userInfo.put("avatarUrl", result.getAvatarUrl());
            userInfo.put("gender", result.getGender());
            response.put("user", userInfo);
            
            log.info("微信登录成功，userId: {}", result.getUserId());
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
     * 更新用户信息
     * 
     * @param request 更新用户信息请求
     * @return 更新结果
     * @author daidasheng
     * @date 2024-12-20
     */
    @PostMapping("/wechat/userinfo")
    public ResponseEntity<Map<String, Object>> updateUserInfo(@Valid @RequestBody UpdateUserInfoRequest request) {
        log.info("收到更新用户信息请求，openId: {}", request.getOpenId());
        
        try {
            // 调用应用服务
            userApplicationService.updateUserInfo(
                request.getOpenId(),
                request.getNickName(),
                request.getAvatarUrl(),
                request.getGender(),
                request.getCountry(),
                request.getProvince(),
                request.getCity(),
                request.getLanguage()
            );
            
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
}

