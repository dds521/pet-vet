package com.petvetai.app.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/pet/**", "/actuator/**", "/api/auth/**", "/api/sentinel/**").permitAll() // 暂时开放用于测试
                .anyRequest().authenticated()
            );
        
        // 可以在此添加 JWT Filter (OAuth2 Resource Server)
        // http.oauth2ResourceServer(oauth2 -> oauth2.jwt());

        return http.build();
    }
}

