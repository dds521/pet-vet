package com.petvet.rag.app.classifier.config;

import com.petvet.rag.app.classifier.model.RuleDefinition;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类器配置属性
 * 绑定 rag.classifier.* 配置项
 * 支持 Nacos 配置动态刷新
 * 
 * 注意：不使用 @Component，通过 @EnableConfigurationProperties 在 ClassifierConfig 中启用
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@RefreshScope
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
        
        /**
         * 是否启用 Redis 分布式缓存
         */
        private Boolean redisEnabled = false;
        
        /**
         * Redis 缓存 Key 前缀
         */
        private String redisKeyPrefix = "rag:classifier:cache:";
        
        /**
         * Redis 缓存过期时间（分钟），如果为 null 则使用 expireMinutes
         */
        private Integer redisExpireMinutes;
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
        
        /**
         * 规则定义列表
         * 支持通过配置文件动态配置规则
         */
        private List<RuleDefinition> rules = new ArrayList<>();
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
