package com.petvet.rag.app.classifier.engine;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * QLExpress规则引擎封装类
 * 
 * 提供规则表达式的编译、缓存和执行功能
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Component
@Slf4j
public class RuleEngine {
    
    /**
     * QLExpress执行器
     * 支持表达式缓存，提升重复执行性能
     */
    private ExpressRunner runner;
    
    /**
     * 表达式缓存
     * Key: 表达式字符串，Value: 编译后的指令集
     */
    private final Map<String, Object> expressionCache = new ConcurrentHashMap<>();
    
    /**
     * 初始化规则引擎
     */
    @PostConstruct
    public void init() {
        runner = new ExpressRunner();
        
        // 配置安全策略（可选）
        // runner.addFunctionOfServiceMethod("xxx", service, "methodName", null, null);
        
        // 配置是否使用缓存（默认开启）
        // runner.setInstructionSetCacheSize(1000);
        
        log.info("QLExpress规则引擎初始化完成");
    }
    
    /**
     * 执行规则表达式
     * 
     * @param expression 规则表达式，例如：lowerQuery.contains("疾病") || lowerQuery.contains("症状")
     * @param context 执行上下文，包含变量和对象
     * @return 执行结果
     * @throws Exception 执行异常
     */
    public Object execute(String expression, IExpressContext<String, Object> context) throws Exception {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("表达式不能为空");
        }
        
        try {
            // 执行表达式（QLExpress会自动缓存编译结果）
            Object result = runner.execute(expression, context, null, true, false);
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
     * @param context 执行上下文
     * @return 布尔结果
     * @throws Exception 执行异常
     */
    public boolean executeBoolean(String expression, IExpressContext<String, Object> context) throws Exception {
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
     * @return 上下文对象
     */
    public IExpressContext<String, Object> createContext() {
        return new DefaultContext<>();
    }
    
    /**
     * 验证表达式语法
     * 
     * @param expression 表达式
     * @return 是否有效
     */
    public boolean validateExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        try {
            IExpressContext<String, Object> context = createContext();
            // 使用空上下文验证语法
            runner.execute(expression, context, null, true, false);
            return true;
        } catch (Exception e) {
            log.warn("表达式语法验证失败: {}, 错误: {}", expression, e.getMessage());
            return false;
        }
    }
    
    /**
     * 清除表达式缓存
     */
    public void clearCache() {
        expressionCache.clear();
        log.info("规则表达式缓存已清除");
    }
    
    /**
     * 获取缓存大小
     * 
     * @return 缓存大小
     */
    public int getCacheSize() {
        return expressionCache.size();
    }
}
