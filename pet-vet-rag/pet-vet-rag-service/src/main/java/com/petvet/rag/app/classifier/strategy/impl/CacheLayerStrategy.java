package com.petvet.rag.app.classifier.strategy.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.petvet.rag.app.classifier.config.ClassifierProperties;
import com.petvet.rag.app.classifier.model.ClassificationResult;
import com.petvet.rag.app.classifier.strategy.ClassificationStrategy;
import com.petvet.rag.app.service.MemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

/**
 * 缓存层策略
 * 优先从缓存中获取分类结果
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Component
@Slf4j
public class CacheLayerStrategy implements ClassificationStrategy {
    
    private final ClassifierProperties properties;
    private final Cache<String, ClassificationResult> cache;
    
    public CacheLayerStrategy(ClassifierProperties properties) {
        this.properties = properties;
        // 初始化Caffeine缓存，使用配置值
        ClassifierProperties.Cache cacheConfig = properties.getCache();
        this.cache = Caffeine.newBuilder()
            .maximumSize(cacheConfig.getMaxSize() != null ? cacheConfig.getMaxSize() : 1000)
            .expireAfterWrite(
                cacheConfig.getExpireMinutes() != null ? cacheConfig.getExpireMinutes() : 5,
                TimeUnit.MINUTES
            )
            .recordStats()
            .build();
    }
    
    @Override
    public boolean matches(String query, MemoryService.ConversationMemory memory) {
        // 缓存层总是匹配，但如果没有缓存结果则返回null
        return properties.getCache().getEnabled() != null && properties.getCache().getEnabled();
    }
    
    @Override
    public ClassificationResult classify(String query, MemoryService.ConversationMemory memory) {
        if (properties.getCache().getEnabled() == null || !properties.getCache().getEnabled()) {
            return null;
        }
        
        String cacheKey = buildCacheKey(query);
        ClassificationResult cached = cache.getIfPresent(cacheKey);
        
        if (cached != null) {
            // 标记为缓存命中
            cached.setCacheHit(true);
            log.debug("缓存命中, query: {}, result: {}", query, cached);
            return cached;
        }
        
        // 缓存未命中，返回null继续下一个策略
        return null;
    }
    
    /**
     * 缓存分类结果
     * 
     * @param query 查询
     * @param result 分类结果
     */
    public void cacheResult(String query, ClassificationResult result) {
        if (properties.getCache().getEnabled() != null 
            && properties.getCache().getEnabled() 
            && result != null) {
            String cacheKey = buildCacheKey(query);
            cache.put(cacheKey, result);
            log.debug("缓存分类结果, key: {}, result: {}", cacheKey, result);
        }
    }
    
    /**
     * 构建缓存Key
     * 
     * @param query 查询
     * @return 缓存Key
     */
    private String buildCacheKey(String query) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(query.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return "query_classifier:" + sb.toString();
        } catch (Exception e) {
            log.warn("构建缓存Key失败，使用原始查询", e);
            return "query_classifier:" + query.hashCode();
        }
    }
    
    @Override
    public int getPriority() {
        return 1; // 最高优先级
    }
    
    @Override
    public String getName() {
        return "CacheLayerStrategy";
    }
    
    @Override
    public boolean isEnabled() {
        return properties.getCache().getEnabled() != null && properties.getCache().getEnabled();
    }
}

