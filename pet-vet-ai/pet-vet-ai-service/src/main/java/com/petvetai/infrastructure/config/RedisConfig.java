package com.petvetai.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * 
 * 配置 Redis 模板的序列化方式
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Configuration
public class RedisConfig {

    /**
     * 配置 Redis 模板
     * 
     * @param connectionFactory Redis 连接工厂
     * @return Redis 模板
     * @author daidasheng
     * @date 2024-12-20
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Key 序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Value 序列化
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        return template;
    }
    
    /**
     * 配置 Redis String 模板（用于 RedisService）
     * 
     * @param connectionFactory Redis 连接工厂
     * @return Redis String 模板
     * @author daidasheng
     * @date 2024-12-20
     */
    @Bean
    public org.springframework.data.redis.core.RedisTemplate<String, String> stringRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        org.springframework.data.redis.core.RedisTemplate<String, String> template = 
            new org.springframework.data.redis.core.RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Key 和 Value 都使用 String 序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        return template;
    }
}

