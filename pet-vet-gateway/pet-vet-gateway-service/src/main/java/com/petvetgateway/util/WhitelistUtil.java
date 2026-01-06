package com.petvetgateway.util;

import com.petvetgateway.config.GatewayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * 白名单工具类
 * 
 * 用于检查请求路径是否在白名单中，白名单中的路径不需要鉴权
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WhitelistUtil {
    
    /**
     * 网关配置
     */
    private final GatewayConfig gatewayConfig;
    
    /**
     * 路径匹配器
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    /**
     * 检查路径是否在白名单中
     * 
     * @param path 请求路径
     * @return 是否在白名单中
     * @author daidasheng
     * @date 2024-12-27
     */
    public boolean isWhitelisted(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        List<String> whitelist = gatewayConfig.getWhitelist();
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        
        // 使用Ant路径匹配器进行匹配
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern, path)) {
                log.debug("路径 {} 匹配白名单模式 {}", path, pattern);
                return true;
            }
        }
        
        return false;
    }
}

