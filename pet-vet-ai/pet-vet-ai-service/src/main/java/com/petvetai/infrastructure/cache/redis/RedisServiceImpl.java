package com.petvetai.infrastructure.cache.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis服务实现
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
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

