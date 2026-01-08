package com.petvetai.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Demo 控制器
 * 
 * 用于验证通过网关进行服务调用的简单接口
 * 
 * @author daidasheng
 * @date 2026-01-07
 */
@RestController
@RequestMapping("/api/demo")
@Slf4j
public class DemoController {
    
    /**
     * 服务版本（从配置中获取，用于验证灰度发布）
     */
    @Value("${spring.cloud.nacos.discovery.metadata.version:v1.0}")
    private String serviceVersion;
    
    /**
     * 服务名称
     */
    @Value("${spring.application.name:pet-vet-ai-service}")
    private String serviceName;
    
    /**
     * 简单的健康检查接口
     * 
     * @return 健康状态
     * @author daidasheng
     * @date 2026-01-07
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", serviceName);
        response.put("version", serviceVersion);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        log.info("健康检查 - 服务: {}, 版本: {}", serviceName, serviceVersion);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 简单的 Echo 接口（回显请求信息）
     * 
     * @param message 消息内容（可选）
     * @return 回显信息
     * @author daidasheng
     * @date 2026-01-07
     */
    @GetMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(
            @RequestParam(value = "message", required = false, defaultValue = "Hello from Gateway!") String message) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("service", serviceName);
        response.put("version", serviceVersion);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        log.info("Echo 请求 - 消息: {}, 服务: {}, 版本: {}", message, serviceName, serviceVersion);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 简单的 POST 接口（用于验证 POST 请求）
     * 
     * @param requestBody 请求体
     * @return 响应信息
     * @author daidasheng
     * @date 2026-01-07
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> test(@RequestBody(required = false) Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        response.put("received", requestBody != null ? requestBody : "No request body");
        response.put("service", serviceName);
        response.put("version", serviceVersion);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        log.info("Test POST 请求 - 服务: {}, 版本: {}, 请求体: {}", serviceName, serviceVersion, requestBody);
        
        return ResponseEntity.ok(response);
    }
}

