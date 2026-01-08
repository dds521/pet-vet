package com.petvetai.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全配置类
 * 
 * 配置 Spring Security 的安全策略
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     * 
     * @param http HTTP 安全配置
     * @return 安全过滤器链
     * @throws Exception 配置异常
     * @author daidasheng
     * @date 2024-12-20
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/pet/**", "/api/user/**", "/api/demo/**","/api/doctor/**", "/actuator/**").permitAll() // 开放 API 接口
                .anyRequest().authenticated()
            );
        
        // 可以在此添加 JWT Filter (OAuth2 Resource Server)
        // http.oauth2ResourceServer(oauth2 -> oauth2.jwt());

        return http.build();
    }
}

