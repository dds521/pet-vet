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
 * 注意：Spring Boot 会自动创建 StringRedisTemplate，它已经配置好了 String 序列化
 * 所以这里不需要再定义 stringRedisTemplate，直接使用自动配置的即可
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Configuration
public class RedisConfig {

    /**
     * 配置 Redis 模板（用于存储对象）
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
    
    // 注意：不再定义 stringRedisTemplate，直接使用 Spring Boot 自动配置的 StringRedisTemplate
    // StringRedisTemplate 已经配置好了 String 序列化，满足 RedisServiceImpl 的需求
}

