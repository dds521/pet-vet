package com.petvetgateway.filter;

import com.petvetgateway.config.GatewayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

/**
 * 灰度发布过滤器（基于 Nacos 元数据版本选择）
 * 
 * 实现简单的灰度发布功能：
 * - 可配置的流量百分比走新版本
 * - 通过 Nacos 元数据选择服务实例，前端无需修改
 * - 支持配置化的服务路径匹配
 * 
 * 工作原理：
 * 1. 根据配置的随机数决定目标版本
 * 2. 将版本信息添加到请求头 X-Target-Version（仅网关内部使用）
 * 3. VersionLoadBalancer 根据版本选择对应的服务实例
 * 
 * @author daidasheng
 * @date 2026-01-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GrayReleaseFilter implements GlobalFilter, Ordered {
    
    /**
     * 网关配置（支持动态刷新）
     */
    private final GatewayConfig gatewayConfig;
    
    /**
     * 路径匹配器（支持通配符）
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    /**
     * 随机数生成器
     */
    private final Random random = new Random();
    
    /**
     * 过滤器执行顺序（在路由之前，优先级高于其他过滤器）
     */
    private static final int FILTER_ORDER = -200;
    
    /**
     * 执行过滤逻辑
     * 
     * @param exchange 服务器Web交换对象
     * @param chain 过滤器链
     * @return Mono<Void>
     * @author daidasheng
     * @date 2026-01-07
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 检查是否启用灰度发布
        GatewayConfig.GrayReleaseConfig grayConfig = gatewayConfig.getGrayRelease();
        if (grayConfig == null || grayConfig.getEnabled() == null || !grayConfig.getEnabled()) {
            return chain.filter(exchange);
        }
        
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 检查路径是否匹配配置的服务路径
        if (!isPathMatched(path, grayConfig)) {
            return chain.filter(exchange);
        }
        
        // 根据配置的策略决定版本
        String targetVersion = determineVersion(grayConfig, request);
        
        log.info("灰度发布 - 路径: {}, 目标版本: {}, 灰度比例: {}%, 策略: {}", path, targetVersion, grayConfig.getPercentage(), grayConfig.getStrategy());
        
        // 将版本信息添加到请求头中，供 LoadBalancer 使用
        // 注意：这个请求头仅在网关内部使用，LoadBalancer 选择实例后会被移除，不会传递给下游服务
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Target-Version", targetVersion)
                .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    /**
     * 检查路径是否匹配配置的服务路径
     * 
     * @param path 请求路径
     * @param grayConfig 灰度配置
     * @return 是否匹配
     * @author daidasheng
     * @date 2026-01-07
     */
    private boolean isPathMatched(String path, GatewayConfig.GrayReleaseConfig grayConfig) {
        List<String> servicePaths = grayConfig.getServicePaths();
        
        // 如果没有配置服务路径，默认匹配所有 /api/ai/** 路径（向后兼容）
        if (CollectionUtils.isEmpty(servicePaths)) {
            return path.startsWith("/api/ai/");
        }
        
        // 检查路径是否匹配配置的任一服务路径
        return servicePaths.stream()
                .filter(pattern -> pattern != null && !pattern.isEmpty())
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    
    /**
     * 根据灰度配置和策略决定目标版本
     * 
     * 支持多种策略：
     * - hybrid: 混合策略（推荐）- 优先用户ID，其次IP，最后请求ID
     * - user-id: 基于用户ID的一致性哈希
     * - ip: 基于IP的一致性哈希
     * - request-id: 基于请求ID的取模
     * - random: 纯随机数（不推荐）
     * 
     * @param grayConfig 灰度配置
     * @param request 请求对象
     * @return 版本号
     * @author daidasheng
     * @date 2026-01-07
     */
    private String determineVersion(GatewayConfig.GrayReleaseConfig grayConfig, ServerHttpRequest request) {
        // 获取配置的灰度百分比（默认10%）
        int percentage = grayConfig.getPercentage() != null ? grayConfig.getPercentage() : 10;
        // 限制在 0-100 范围内
        percentage = Math.max(0, Math.min(100, percentage));
        
        // 获取策略（默认混合策略）
        String strategy = grayConfig.getStrategy() != null ? grayConfig.getStrategy() : "hybrid";
        
        // 根据策略获取哈希键
        String hashKey = getHashKey(strategy, request);
        
        // 计算哈希值并取模
        int hash = hashKey.hashCode();
        int mod = Math.abs(hash % 100);
        
        // 根据取模结果决定版本
        boolean useNewVersion = mod < percentage;
        
        if (useNewVersion) {
            return grayConfig.getNewVersion() != null ? grayConfig.getNewVersion() : "v2.0";
        } else {
            return grayConfig.getOldVersion() != null ? grayConfig.getOldVersion() : "v1.0";
        }
    }
    
    /**
     * 根据策略获取哈希键
     * 
     * @param strategy 策略名称
     * @param request 请求对象
     * @return 哈希键
     * @author daidasheng
     * @date 2026-01-07
     */
    private String getHashKey(String strategy, ServerHttpRequest request) {
        switch (strategy.toLowerCase()) {
            case "user-id":
                // 基于用户ID（需要用户已登录）
                String userId = request.getHeaders().getFirst("X-User-Id");
                if (userId != null && !userId.isEmpty()) {
                    return "user:" + userId;
                }
                // 如果没有用户ID，降级到请求ID
                return "request:" + generateRequestId();
                
            case "ip":
                // 基于IP地址
                String clientIp = getClientIp(request);
                return "ip:" + (clientIp != null ? clientIp : "unknown");
                
            case "request-id":
                // 基于请求ID
                return "request:" + generateRequestId();
                
            case "random":
                // 纯随机数（不推荐，但保留兼容性）
                return "random:" + random.nextInt(Integer.MAX_VALUE);
                
            case "hybrid":
            default:
                // 混合策略：优先用户ID，其次IP，最后请求ID
                String hybridUserId = request.getHeaders().getFirst("X-User-Id");
                if (hybridUserId != null && !hybridUserId.isEmpty()) {
                    return "user:" + hybridUserId;
                }
                
                String hybridIp = getClientIp(request);
                if (hybridIp != null && !hybridIp.isEmpty() && !"unknown".equals(hybridIp)) {
                    return "ip:" + hybridIp;
                }
                
                return "request:" + generateRequestId();
        }
    }
    
    /**
     * 生成请求ID
     * 
     * @return 请求ID
     * @author daidasheng
     * @date 2026-01-07
     */
    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }
    
    /**
     * 获取客户端IP地址
     * 
     * @param request 请求对象
     * @return 客户端IP地址
     * @author daidasheng
     * @date 2026-01-07
     */
    private String getClientIp(ServerHttpRequest request) {
        // 优先从 X-Forwarded-For 获取（经过代理的情况）
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For 可能包含多个IP，取第一个
            return xForwardedFor.split(",")[0].trim();
        }
        
        // 其次从 X-Real-IP 获取
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // 最后从远程地址获取
        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }
    
    /**
     * 获取过滤器执行顺序
     * 
     * @return 顺序值
     * @author daidasheng
     * @date 2026-01-07
     */
    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }
}

