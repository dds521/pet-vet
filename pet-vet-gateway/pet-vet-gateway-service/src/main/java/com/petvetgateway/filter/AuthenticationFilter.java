package com.petvetgateway.filter;

import com.petvetgateway.config.GatewayConfig;
import com.petvetgateway.util.JwtUtil;
import com.petvetgateway.util.WhitelistUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一鉴权过滤器
 * 
 * 负责对请求进行鉴权验证，支持JWT Token验证
 * 白名单路径跳过鉴权
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {
    
    /**
     * 网关配置
     */
    private final GatewayConfig gatewayConfig;
    
    /**
     * JWT工具类
     */
    private final JwtUtil jwtUtil;
    
    /**
     * 白名单工具类
     */
    private final WhitelistUtil whitelistUtil;
    
    /**
     * 过滤器执行顺序（数字越小优先级越高）
     */
    private static final int FILTER_ORDER = -100;
    
    /**
     * 执行过滤逻辑
     * 
     * @param exchange 服务器Web交换对象
     * @param chain 过滤器链
     * @return Mono<Void>
     * @author daidasheng
     * @date 2024-12-27
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 检查是否在白名单中
        if (whitelistUtil.isWhitelisted(path)) {
            log.debug("路径 {} 在白名单中，跳过鉴权", path);
            return chain.filter(exchange);
        }
        
        // 获取Token
        String token = extractToken(request);
        
        // 验证Token
        if (!StringUtils.hasText(token) || !jwtUtil.validateToken(token)) {
            log.warn("请求路径 {} 鉴权失败，Token无效或缺失", path);
            return handleUnauthorized(exchange);
        }
        
        // 从Token中提取用户信息并添加到请求头
        Long userId = jwtUtil.getUserIdFromToken(token);
        String openId = jwtUtil.getOpenIdFromToken(token);
        
        // 将用户信息添加到请求头，供下游服务使用
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId != null ? userId.toString() : "")
                .header("X-Open-Id", openId != null ? openId : "")
                .header("X-Auth-Token", token)
                .build();
        
        log.debug("请求路径 {} 鉴权成功，用户ID: {}", path, userId);
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    /**
     * 从请求中提取Token
     * 
     * @param request 请求对象
     * @return Token字符串，如果不存在返回null
     * @author daidasheng
     * @date 2024-12-27
     */
    private String extractToken(ServerHttpRequest request) {
        // 从请求头中获取Token
        String authHeader = request.getHeaders().getFirst(gatewayConfig.getJwt().getHeader());
        
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(gatewayConfig.getJwt().getPrefix())) {
            return authHeader.substring(gatewayConfig.getJwt().getPrefix().length()).trim();
        }
        
        // 从查询参数中获取Token（可选，用于某些特殊场景）
        String tokenParam = request.getQueryParams().getFirst("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        
        return null;
    }
    
    /**
     * 处理未授权请求
     * 
     * @param exchange 服务器Web交换对象
     * @return Mono<Void>
     * @author daidasheng
     * @date 2024-12-27
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", 401);
        result.put("message", "未授权，请先登录");
        result.put("data", null);
        
        String json = convertMapToJson(result);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }
    
    /**
     * 将Map转换为JSON字符串
     * 
     * @param map Map对象
     * @return JSON字符串
     * @author daidasheng
     * @date 2024-12-27
     */
    private String convertMapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
    
    /**
     * 获取过滤器执行顺序
     * 
     * @return 顺序值
     * @author daidasheng
     * @date 2024-12-27
     */
    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }
}

