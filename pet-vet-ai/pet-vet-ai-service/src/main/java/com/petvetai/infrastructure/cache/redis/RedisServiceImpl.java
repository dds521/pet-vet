package com.petvetai.infrastructure.cache.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis服务实现
 * 
 * 使用 Spring Boot 自动配置的 StringRedisTemplate
 * StringRedisTemplate 继承自 RedisTemplate<String, String>，已经配置好了 String 序列化
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    
    private final StringRedisTemplate redisTemplate;
    
    @Override
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}

