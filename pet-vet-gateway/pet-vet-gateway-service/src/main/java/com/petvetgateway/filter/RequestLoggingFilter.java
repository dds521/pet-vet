package com.petvetgateway.filter;

import com.petvetgateway.config.GatewayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 请求/响应日志记录过滤器
 * 
 * 记录所有经过网关的请求和响应信息，包括请求头、请求体、响应状态、响应体等
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    
    /**
     * 网关配置
     */
    private final GatewayConfig gatewayConfig;
    
    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 过滤器执行顺序（在鉴权过滤器之后）
     */
    private static final int FILTER_ORDER = -50;
    
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
        // 如果日志功能未启用，直接跳过
        if (!gatewayConfig.getLog().getEnabled()) {
            return chain.filter(exchange);
        }
        
        ServerHttpRequest request = exchange.getRequest();
        String requestId = generateRequestId();
        long startTime = System.currentTimeMillis();
        
        // 记录请求信息
        logRequest(request, requestId);
        
        // 如果需要记录请求体，需要包装请求
        ServerHttpRequest decoratedRequest = request;
        if (gatewayConfig.getLog().getRequestBody()) {
            decoratedRequest = new RequestLoggingDecorator(request, requestId);
        }
        
        // 包装响应以记录响应信息
        ServerHttpResponseDecorator decoratedResponse = new ResponseLoggingDecorator(
                exchange.getResponse(), requestId, startTime);
        
        return chain.filter(exchange.mutate()
                .request(decoratedRequest)
                .response(decoratedResponse)
                .build());
    }
    
    /**
     * 生成请求ID
     * 
     * @return 请求ID
     * @author daidasheng
     * @date 2024-12-27
     */
    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }
    
    /**
     * 记录请求信息
     * 
     * @param request 请求对象
     * @param requestId 请求ID
     * @author daidasheng
     * @date 2024-12-27
     */
    private void logRequest(ServerHttpRequest request, String requestId) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n========== 请求开始 ==========\n");
        logBuilder.append("请求ID: ").append(requestId).append("\n");
        logBuilder.append("时间: ").append(LocalDateTime.now().format(DATE_TIME_FORMATTER)).append("\n");
        logBuilder.append("方法: ").append(request.getMethod()).append("\n");
        logBuilder.append("路径: ").append(request.getURI().getPath()).append("\n");
        logBuilder.append("查询参数: ").append(request.getURI().getQuery()).append("\n");
        logBuilder.append("请求头: ").append(request.getHeaders()).append("\n");
        logBuilder.append("客户端IP: ").append(getClientIp(request)).append("\n");
        logBuilder.append("==============================");
        
        log.info(logBuilder.toString());
    }
    
    /**
     * 记录响应信息
     * 
     * @param response 响应对象
     * @param requestId 请求ID
     * @param startTime 请求开始时间
     * @author daidasheng
     * @date 2024-12-27
     */
    private void logResponse(ServerHttpResponse response, String requestId, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n========== 响应结束 ==========\n");
        logBuilder.append("请求ID: ").append(requestId).append("\n");
        logBuilder.append("时间: ").append(LocalDateTime.now().format(DATE_TIME_FORMATTER)).append("\n");
        logBuilder.append("状态码: ").append(response.getStatusCode()).append("\n");
        logBuilder.append("响应头: ").append(response.getHeaders()).append("\n");
        logBuilder.append("耗时: ").append(duration).append("ms\n");
        logBuilder.append("==============================");
        
        log.info(logBuilder.toString());
    }
    
    /**
     * 获取客户端IP地址
     * 
     * @param request 请求对象
     * @return 客户端IP地址
     * @author daidasheng
     * @date 2024-12-27
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    
    /**
     * 请求日志装饰器
     * 用于读取和记录请求体
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    private class RequestLoggingDecorator extends ServerHttpRequestDecorator {
        
        /**
         * 请求ID
         */
        private final String requestId;
        
        /**
         * 构造函数
         * 
         * @param delegate 原始请求
         * @param requestId 请求ID
         * @author daidasheng
         * @date 2024-12-27
         */
        public RequestLoggingDecorator(ServerHttpRequest delegate, String requestId) {
            super(delegate);
            this.requestId = requestId;
        }
        
        /**
         * 获取请求体
         * 
         * @return 请求体Flux
         * @author daidasheng
         * @date 2024-12-27
         */
        @Override
        public Flux<DataBuffer> getBody() {
            return DataBufferUtils.join(super.getBody())
                    .doOnNext(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        
                        String body = new String(bytes, StandardCharsets.UTF_8);
                        int maxSize = gatewayConfig.getLog().getMaxBodySize();
                        
                        if (body.length() > maxSize) {
                            body = body.substring(0, maxSize) + "...(已截断)";
                        }
                        
                        log.info("请求ID: {}, 请求体: {}", requestId, body);
                    })
                    .flatMapMany(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        return Flux.just(getDelegate().bufferFactory().wrap(bytes));
                    });
        }
    }
    
    /**
     * 响应日志装饰器
     * 用于读取和记录响应体
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    private class ResponseLoggingDecorator extends ServerHttpResponseDecorator {
        
        /**
         * 请求ID
         */
        private final String requestId;
        
        /**
         * 请求开始时间
         */
        private final long startTime;
        
        /**
         * 构造函数
         * 
         * @param delegate 原始响应
         * @param requestId 请求ID
         * @param startTime 请求开始时间
         * @author daidasheng
         * @date 2024-12-27
         */
        public ResponseLoggingDecorator(ServerHttpResponse delegate, String requestId, long startTime) {
            super(delegate);
            this.requestId = requestId;
            this.startTime = startTime;
        }
        
        /**
         * 写入响应体
         * 
         * @param body 响应体Publisher
         * @return Mono<Void>
         * @author daidasheng
         * @date 2024-12-27
         */
        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            if (gatewayConfig.getLog().getResponseBody() && body instanceof Flux) {
                Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                // 使用缓存方式读取响应体，避免影响原始流
                return DataBufferUtils.join(fluxBody)
                        .flatMap(dataBuffer -> {
                            // 读取响应体内容用于日志记录
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            
                            String responseBody = new String(bytes, StandardCharsets.UTF_8);
                            int maxSize = gatewayConfig.getLog().getMaxBodySize();
                            
                            if (responseBody.length() > maxSize) {
                                responseBody = responseBody.substring(0, maxSize) + "...(已截断)";
                            }
                            
                            log.info("请求ID: {}, 响应体: {}", requestId, responseBody);
                            
                            // 重新创建DataBuffer用于写入响应
                            DataBuffer newBuffer = getDelegate().bufferFactory().wrap(bytes);
                            return super.writeWith(Flux.just(newBuffer));
                        })
                        .then(Mono.fromRunnable(() -> {
                            logResponse(getDelegate(), requestId, startTime);
                        }));
            } else {
                return super.writeWith(body).doOnSuccess(aVoid -> {
                    logResponse(getDelegate(), requestId, startTime);
                });
            }
        }
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

