package com.petvetgateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 * 
 * 用于生成和验证JWT Token
 * 支持用户身份认证和授权
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Component
public class JwtUtil {
    
    /**
     * JWT密钥（从配置文件读取）
     * 生产环境应使用强随机密钥，建议至少32位
     */
    @Value("${gateway.jwt.secret:pet-vet-gateway-secret-key-change-in-production-environment-minimum-32-characters}")
    private String secret;
    
    /**
     * JWT过期时间（毫秒）
     * 默认7天
     */
    @Value("${gateway.jwt.expiration:604800000}")
    private Long expiration;
    
    /**
     * 生成SecretKey
     * 
     * @return SecretKey对象
     * @author daidasheng
     * @date 2024-12-27
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 从Token中获取Claims
     * 
     * @param token JWT Token
     * @return Claims对象，如果解析失败返回null
     * @author daidasheng
     * @date 2024-12-27
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("JWT Token解析失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID，如果解析失败返回null
     * @author daidasheng
     * @date 2024-12-27
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }
    
    /**
     * 从Token中获取openId
     * 
     * @param token JWT Token
     * @return 微信openId，如果解析失败返回null
     * @author daidasheng
     * @date 2024-12-27
     */
    public String getOpenIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.get("openId", String.class);
        }
        return null;
    }
    
    /**
     * 验证Token是否有效
     * 
     * @param token JWT Token
     * @return 是否有效
     * @author daidasheng
     * @date 2024-12-27
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }
            // 检查是否过期
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("JWT Token验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取Token过期时间
     * 
     * @param token JWT Token
     * @return 过期时间，如果解析失败返回null
     * @author daidasheng
     * @date 2024-12-27
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.getExpiration();
        }
        return null;
    }
}

