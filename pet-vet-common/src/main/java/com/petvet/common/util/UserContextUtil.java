package com.petvet.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类
 * 
 * 用于获取当前登录用户信息，支持扩展不同的用户获取方式
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
public class UserContextUtil {
    
    /**
     * 用户ID的请求头名称
     */
    private static final String USER_ID_HEADER = "X-User-Id";
    
    /**
     * 用户名的请求头名称
     */
    private static final String USER_NAME_HEADER = "X-User-Name";
    
    /**
     * 默认系统用户ID
     */
    private static final String DEFAULT_SYSTEM_USER = "system";
    
    /**
     * 获取当前登录用户ID
     * 
     * 优先从请求头获取，如果没有则返回系统默认用户
     * 可以根据实际项目需求扩展，例如从 JWT Token、Session 等获取
     * 
     * @return 当前用户ID，如果无法获取则返回系统默认用户
     * @author daidasheng
     * @date 2024-12-27
     */
    public static String getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.debug("无法获取请求上下文，返回系统默认用户");
                return DEFAULT_SYSTEM_USER;
            }
            
            HttpServletRequest request = attributes.getRequest();
            if (request == null) {
                log.debug("无法获取请求对象，返回系统默认用户");
                return DEFAULT_SYSTEM_USER;
            }
            
            // 从请求头获取用户ID
            String userId = request.getHeader(USER_ID_HEADER);
            if (userId != null && !userId.trim().isEmpty()) {
                return userId.trim();
            }
            
            // 如果没有从请求头获取到，可以扩展其他方式：
            // 1. 从 JWT Token 中解析
            // 2. 从 Session 中获取
            // 3. 从 SecurityContext 中获取
            // 示例：
            // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // if (authentication != null && authentication.isAuthenticated()) {
            //     return authentication.getName();
            // }
            
            log.debug("无法从请求头获取用户ID，返回系统默认用户");
            return DEFAULT_SYSTEM_USER;
        } catch (Exception e) {
            log.warn("获取当前用户ID失败，返回系统默认用户", e);
            return DEFAULT_SYSTEM_USER;
        }
    }
    
    /**
     * 获取当前登录用户名
     * 
     * 优先从请求头获取，如果没有则返回系统默认用户
     * 
     * @return 当前用户名，如果无法获取则返回系统默认用户
     * @author daidasheng
     * @date 2024-12-27
     */
    public static String getCurrentUserName() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return DEFAULT_SYSTEM_USER;
            }
            
            HttpServletRequest request = attributes.getRequest();
            if (request == null) {
                return DEFAULT_SYSTEM_USER;
            }
            
            // 从请求头获取用户名
            String userName = request.getHeader(USER_NAME_HEADER);
            if (userName != null && !userName.trim().isEmpty()) {
                return userName.trim();
            }
            
            // 如果没有用户名，则使用用户ID
            return getCurrentUserId();
        } catch (Exception e) {
            log.warn("获取当前用户名失败，返回系统默认用户", e);
            return DEFAULT_SYSTEM_USER;
        }
    }
}

