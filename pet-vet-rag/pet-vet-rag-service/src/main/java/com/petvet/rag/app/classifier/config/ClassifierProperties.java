package com.petvet.rag.app.classifier.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分类器配置属性
 * 绑定 rag.classifier.* 配置项
 * 
 * 注意：不使用 @Component，通过 @EnableConfigurationProperties 在 ClassifierConfig 中启用
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@ConfigurationProperties(prefix = "rag.classifier")
@Data
public class ClassifierProperties {
    
    /**
     * 混合方案配置
     */
    private Hybrid hybrid = new Hybrid();
    
    /**
     * 对比验证模式
     * 同时运行新旧方案进行对比，用于验证
     */
    private Boolean compareMode = false;
    
    /**
     * 缓存层配置
     */
    private Cache cache = new Cache();
    
    /**
     * 规则层配置
     */
    private Rule rule = new Rule();
    
    /**
     * 兜底策略配置
     */
    private Fallback fallback = new Fallback();
    
    /**
     * 混合方案配置
     */
    @Data
    public static class Hybrid {
        /**
         * 是否启用混合方案
         */
        private Boolean enabled = false;
    }
    
    /**
     * 缓存层配置
     */
    @Data
    public static class Cache {
        /**
         * 是否启用缓存
         */
        private Boolean enabled = true;
        
        /**
         * 最大缓存条目数
         */
        private Integer maxSize = 1000;
        
        /**
         * 缓存过期时间（分钟）
         */
        private Integer expireMinutes = 5;
    }
    
    /**
     * 规则层配置
     */
    @Data
    public static class Rule {
        /**
         * 是否启用规则层
         */
        private Boolean enabled = true;
    }
    
    /**
     * 兜底策略配置
     */
    @Data
    public static class Fallback {
        /**
         * 是否启用兜底策略
         */
        private Boolean enabled = true;
    }
}
