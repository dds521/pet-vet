package com.petvet.rag.app.classifier.strategy.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.petvet.rag.app.classifier.config.ClassifierProperties;
import com.petvet.rag.app.classifier.model.ClassificationResult;
import com.petvet.rag.app.classifier.strategy.ClassificationStrategy;
import com.petvet.rag.app.service.MemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式缓存层策略
 * 支持两级缓存：本地 Caffeine 缓存 + Redis 分布式缓存
 * 优先级高于 CacheLayerStrategy，如果启用 Redis 缓存，会优先使用此策略
 * 
 * @author daidasheng
 * @date 2024-12-16
 */
@Component
@Slf4j
public class RedisCacheLayerStrategy implements ClassificationStrategy {
    
    private final ClassifierProperties properties;
    private final Cache<String, ClassificationResult> localCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final boolean redisEnabled;
    private final String redisKeyPrefix;
    private final int redisExpireMinutes;
    
    /**
     * 构造函数
     * 
     * @param properties 分类器配置属性
     * @param redisTemplate Redis 模板（可选，如果 Redis 未配置则为 null）
     * @author daidasheng
     * @date 2024-12-16
     */
    public RedisCacheLayerStrategy(ClassifierProperties properties, @Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        
        // 初始化本地 Caffeine 缓存（作为一级缓存）
        ClassifierProperties.Cache cacheConfig = properties.getCache();
        this.localCache = Caffeine.newBuilder()
            .maximumSize(cacheConfig.getMaxSize() != null ? cacheConfig.getMaxSize() : 1000)
            .expireAfterWrite(
                cacheConfig.getExpireMinutes() != null ? cacheConfig.getExpireMinutes() : 5,
                TimeUnit.MINUTES
            )
            .recordStats()
            .build();
        
        // Redis 配置
        this.redisEnabled = cacheConfig.getRedisEnabled() != null 
            && cacheConfig.getRedisEnabled() 
            && redisTemplate != null;
        this.redisKeyPrefix = StringUtils.hasText(cacheConfig.getRedisKeyPrefix()) 
            ? cacheConfig.getRedisKeyPrefix() 
            : "rag:classifier:cache:";
        this.redisExpireMinutes = cacheConfig.getRedisExpireMinutes() != null 
            ? cacheConfig.getRedisExpireMinutes() 
            : (cacheConfig.getExpireMinutes() != null ? cacheConfig.getExpireMinutes() : 5);
        
        if (redisEnabled) {
            log.info("Redis 分布式缓存策略已启用，Key前缀: {}, 过期时间: {} 分钟", 
                redisKeyPrefix, redisExpireMinutes);
        } else {
            log.debug("Redis 分布式缓存策略未启用（配置未启用或 Redis 未配置）");
        }
    }
    
    @Override
    public boolean matches(String query, MemoryService.ConversationMemory memory) {
        // 只有启用 Redis 缓存时才匹配
        return redisEnabled 
            && (properties.getCache().getEnabled() == null || properties.getCache().getEnabled());
    }
    
    @Override
    public ClassificationResult classify(String query, MemoryService.ConversationMemory memory) {
        if (!redisEnabled) {
            return null;
        }
        
        if (properties.getCache().getEnabled() == null || !properties.getCache().getEnabled()) {
            return null;
        }
        
        String cacheKey = buildCacheKey(query);
        
        // 1. 先查本地缓存（一级缓存）
        ClassificationResult cached = localCache.getIfPresent(cacheKey);
        if (cached != null) {
            cached.setCacheHit(true);
            log.debug("本地缓存命中, query: {}, result: {}", query, cached);
            return cached;
        }
        
        // 2. 查 Redis 分布式缓存（二级缓存）
        try {
            String redisKey = buildRedisKey(cacheKey);
            if (redisKey != null) {
                Object redisValue = redisTemplate.opsForValue().get(redisKey);
                if (redisValue != null && redisValue instanceof ClassificationResult) {
                    cached = (ClassificationResult) redisValue;
                    cached.setCacheHit(true);
                    // 将 Redis 中的结果同步到本地缓存，提高后续访问速度
                    localCache.put(cacheKey, cached);
                    log.debug("Redis 缓存命中, query: {}, result: {}", query, cached);
                    return cached;
                }
            }
        } catch (Exception e) {
            // Redis 异常时降级，不影响功能
            log.warn("Redis 缓存查询失败，降级处理, query: {}", query, e);
        }
        
        // 缓存未命中，返回null继续下一个策略
        return null;
    }
    
    /**
     * 缓存分类结果
     * 同时写入本地缓存和 Redis 分布式缓存
     * 
     * @param query 查询
     * @param result 分类结果
     * @author daidasheng
     * @date 2024-12-16
     */
    public void cacheResult(String query, ClassificationResult result) {
        if (!redisEnabled) {
            return;
        }
        
        if (properties.getCache().getEnabled() != null && properties.getCache().getEnabled() && result != null) {
            String cacheKey = buildCacheKey(query);
            
            // 1. 写入本地缓存
            localCache.put(cacheKey, result);
            
            // 2. 写入 Redis 分布式缓存
            try {
                String redisKey = buildRedisKey(cacheKey);
                if (redisKey != null) {
                    redisTemplate.opsForValue().set(
                        redisKey, 
                        result, 
                        redisExpireMinutes, 
                        TimeUnit.MINUTES
                    );
                    log.debug("缓存分类结果到本地和Redis, key: {}, result: {}", redisKey, result);
                }
            } catch (Exception e) {
                // Redis 写入失败不影响本地缓存
                log.warn("Redis 缓存写入失败，仅写入本地缓存, query: {}", query, e);
            }
        }
    }
    
    /**
     * 构建缓存Key（基于查询文本的MD5）
     * 
     * @param query 查询
     * @return 缓存Key
     * @author daidasheng
     * @date 2024-12-16
     */
    private String buildCacheKey(String query) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(query.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("构建缓存Key失败，使用原始查询", e);
            return String.valueOf(query.hashCode());
        }
    }
    
    /**
     * 构建 Redis Key（添加前缀）
     * 
     * @param cacheKey 缓存Key
     * @return Redis Key
     * @author daidasheng
     * @date 2024-12-16
     */
    private String buildRedisKey(String cacheKey) {
        return redisKeyPrefix + cacheKey;
    }
    
    @Override
    public int getPriority() {
        // 优先级设为 0，高于 CacheLayerStrategy（优先级为1），优先执行
        return 0;
    }
    
    @Override
    public String getName() {
        return "RedisCacheLayerStrategy";
    }
    
    @Override
    public boolean isEnabled() {
        return redisEnabled && (properties.getCache().getEnabled() == null || properties.getCache().getEnabled());
    }
}
