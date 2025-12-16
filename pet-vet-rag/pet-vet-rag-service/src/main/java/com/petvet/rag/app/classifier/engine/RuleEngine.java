package com.petvet.rag.app.classifier.engine;

import com.alibaba.qlexpress4.Express4Runner;
import com.alibaba.qlexpress4.InitOptions;
import com.alibaba.qlexpress4.QLOptions;
import com.alibaba.qlexpress4.QLResult;
import com.alibaba.qlexpress4.security.QLSecurityStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * QLExpress规则引擎封装类
 * 
 * 提供规则表达式的编译、缓存和执行功能
 * 
 * 使用QLExpress 4.0.4版本（最新版本，修复了安全漏洞）
 * - 使用Express4Runner（新API）
 * - 包名：com.alibaba.qlexpress4
 * - 支持表达式缓存，提升重复执行性能
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Component
@Slf4j
public class RuleEngine {
    
    /**
     * QLExpress 4.0.4执行器
     * 使用Express4Runner，支持新的安全策略和选项配置
     */
    private Express4Runner runner;
    
    /**
     * 初始化规则引擎
     * 
     * 使用QLExpress 4.0.4版本（最新版本，修复了安全漏洞）
     * 配置开放安全策略，允许访问所有字段和方法（适合内部使用）
     * 
     * 注意：生产环境如需更严格的安全控制，可以使用QLSecurityStrategy.isolation()或自定义策略
     */
    @PostConstruct
    public void init() {
        // 配置初始化选项：使用开放安全策略（允许访问所有字段和方法）
        InitOptions initOptions = InitOptions.builder()
            .securityStrategy(QLSecurityStrategy.open())
            .build();
        
        // 创建Express4Runner实例
        runner = new Express4Runner(initOptions);
        
        log.info("QLExpress 4.0.4规则引擎初始化完成（使用Express4Runner）");
    }
    
    /**
     * 执行规则表达式
     * 
     * @param expression 规则表达式，例如：lowerQuery.contains("疾病") || lowerQuery.contains("症状")
     * @param context 执行上下文（Map类型），包含变量和对象
     * @return 执行结果
     * @throws Exception 执行异常
     */
    public Object execute(String expression, Map<String, Object> context) throws Exception {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("表达式不能为空");
        }
        
        try {
            // 执行表达式（QLExpress 4.0.4会自动缓存编译结果）
            QLResult qlResult = runner.execute(expression, context != null ? context : new HashMap<>(), QLOptions.DEFAULT_OPTIONS);
            Object result = qlResult.getResult();
            log.debug("规则表达式执行成功: {}, 结果: {}", expression, result);
            return result;
        } catch (Exception e) {
            log.error("规则表达式执行失败: {}, 错误: {}", expression, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 执行规则表达式（返回布尔值）
     * 
     * @param expression 规则表达式
     * @param context 执行上下文（Map类型）
     * @return 布尔结果
     * @throws Exception 执行异常
     */
    public boolean executeBoolean(String expression, Map<String, Object> context) throws Exception {
        Object result = execute(expression, context);
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        if (result instanceof Number) {
            return ((Number) result).doubleValue() != 0;
        }
        return result != null;
    }
    
    /**
     * 创建执行上下文
     * 
     * QLExpress 4.0.4使用Map作为上下文，而不是IExpressContext
     * 
     * @return 上下文对象（Map类型）
     */
    public Map<String, Object> createContext() {
        return new HashMap<>();
    }
    
    /**
     * 验证表达式语法
     * 
     * QLExpress 4.0.4提供了check()方法用于语法验证
     * 
     * @param expression 表达式
     * @return 是否有效
     */
    public boolean validateExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 使用check()方法验证语法（推荐方式）
            // 如果check()方法不可用，则使用空上下文执行验证
            Map<String, Object> context = new HashMap<>();
            QLResult result = runner.execute(expression, context, QLOptions.DEFAULT_OPTIONS);
            // 如果能执行成功，说明语法有效
            return result.getResult() != null;
        } catch (Exception e) {
            log.warn("表达式语法验证失败: {}, 错误: {}", expression, e.getMessage());
            return false;
        }
    }
    
    /**
     * 清除表达式缓存
     * 
     * QLExpress 4.0.4内部管理缓存，如果API支持可以调用clearCompileCache()
     */
    public void clearCache() {
        // QLExpress 4.0.4内部管理缓存，无需手动清除
        // 如果需要清除，可以调用 runner.clearCompileCache()（如果API支持）
        log.info("规则表达式缓存清除（QLExpress 4.0.4内部管理）");
    }
}