package com.petvetai.app.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Sentinel 演示控制器
 * 展示流控、熔断、降级等功能
 */
@RestController
@RequestMapping("/api/sentinel")
public class SentinelDemoController {

    /**
     * 流控演示 - QPS 流控
     * 在 Sentinel Dashboard 中配置：资源名称为 "flowControl"
     * 设置 QPS 阈值为 2，超过后触发流控
     */
    @GetMapping("/flow")
    @SentinelResource(value = "flowControl", blockHandler = "flowBlockHandler")
    public Map<String, Object> flowControl() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "流控测试成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("resource", "flowControl");
        return result;
    }

    /**
     * 流控降级处理方法
     */
    public Map<String, Object> flowBlockHandler(BlockException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "触发流控，请求被限流");
        result.put("error", ex.getClass().getSimpleName());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 熔断演示 - 异常比例熔断
     * 在 Sentinel Dashboard 中配置：资源名称为 "circuitBreaker"
     * 设置异常比例阈值为 50%，最小请求数为 5，熔断时长为 10 秒
     */
    @GetMapping("/circuit")
    @SentinelResource(value = "circuitBreaker", blockHandler = "circuitBlockHandler", fallback = "circuitFallback")
    public Map<String, Object> circuitBreaker(@RequestParam(defaultValue = "false") boolean error) {
        if (error) {
            throw new RuntimeException("模拟业务异常，触发熔断");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "熔断测试成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("resource", "circuitBreaker");
        return result;
    }

    /**
     * 熔断降级处理方法
     */
    public Map<String, Object> circuitBlockHandler(boolean error, BlockException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "触发熔断，请求被拒绝");
        result.put("error", ex.getClass().getSimpleName());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 熔断降级回退方法（业务异常时调用）
     */
    public Map<String, Object> circuitFallback(boolean error, Throwable ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "业务异常，执行降级逻辑");
        result.put("error", ex.getMessage());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 线程数流控演示
     * 在 Sentinel Dashboard 中配置：资源名称为 "threadControl"
     * 设置线程数阈值为 2，超过后触发流控
     */
    @GetMapping("/thread")
    @SentinelResource(value = "threadControl", blockHandler = "threadBlockHandler")
    public Map<String, Object> threadControl() throws InterruptedException {
        // 模拟耗时操作
        TimeUnit.SECONDS.sleep(2);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "线程数流控测试成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("thread", Thread.currentThread().getName());
        return result;
    }

    /**
     * 线程数流控降级处理方法
     */
    public Map<String, Object> threadBlockHandler(BlockException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "触发线程数流控，请求被拒绝");
        result.put("error", ex.getClass().getSimpleName());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 热点参数限流演示
     * 在 Sentinel Dashboard 中配置：资源名称为 "hotParam"
     * 设置参数索引为 0，QPS 阈值为 2
     */
    @GetMapping("/hot")
    @SentinelResource(value = "hotParam", blockHandler = "hotParamBlockHandler")
    public Map<String, Object> hotParam(@RequestParam String param) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "热点参数限流测试成功");
        result.put("param", param);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 热点参数限流降级处理方法
     */
    public Map<String, Object> hotParamBlockHandler(String param, BlockException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "触发热点参数限流，参数: " + param);
        result.put("error", ex.getClass().getSimpleName());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 系统自适应限流演示
     * 在 Sentinel Dashboard 中配置系统规则
     */
    @GetMapping("/system")
    @SentinelResource(value = "systemRule", blockHandler = "systemBlockHandler")
    public Map<String, Object> systemRule() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "系统自适应限流测试成功");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 系统规则降级处理方法
     */
    public Map<String, Object> systemBlockHandler(BlockException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "触发系统自适应限流");
        result.put("error", ex.getClass().getSimpleName());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 获取 Sentinel 规则配置说明
     */
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("dashboard", "http://localhost:8718");
        info.put("endpoints", Map.of(
            "flow", "/api/sentinel/flow - QPS 流控演示",
            "circuit", "/api/sentinel/circuit?error=true - 熔断演示",
            "thread", "/api/sentinel/thread - 线程数流控演示",
            "hot", "/api/sentinel/hot?param=test - 热点参数限流演示",
            "system", "/api/sentinel/system - 系统自适应限流演示"
        ));
        info.put("instructions", "请在 Sentinel Dashboard 中配置相应的流控规则");
        return info;
    }
}

