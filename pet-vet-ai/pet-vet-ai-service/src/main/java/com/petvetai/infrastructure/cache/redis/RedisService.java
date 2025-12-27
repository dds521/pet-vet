package com.petvetai.infrastructure.cache.redis;

import java.util.concurrent.TimeUnit;

/**
 * Redis服务接口
 * 
 * 封装Redis操作
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public interface RedisService {
    
    /**
     * 设置缓存
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @author daidasheng
     * @date 2024-12-20
     */
    void set(String key, String value, long timeout, TimeUnit unit);
    
    /**
     * 获取缓存
     * 
     * @param key 键
     * @return 值
     * @author daidasheng
     * @date 2024-12-20
     */
    String get(String key);
    
    /**
     * 删除缓存
     * 
     * @param key 键
     * @author daidasheng
     * @date 2024-12-20
     */
    void delete(String key);
}

