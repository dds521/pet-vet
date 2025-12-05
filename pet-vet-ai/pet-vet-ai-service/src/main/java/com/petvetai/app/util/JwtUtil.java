package com.petvetai.app.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * 用于生成和验证JWT Token
 * 支持用户身份认证和授权
 * 
 * @author PetVetAI Team
 * @date 2024-01-01
 */
@Component
public class JwtUtil {
    
    /**
     * JWT密钥（从配置文件读取）
     * 生产环境应使用强随机密钥，建议至少32位
     */
    @Value("${jwt.secret:pet-vet-ai-secret-key-change-in-production-environment-minimum-32-characters}")
    private String secret;
    
    /**
     * JWT过期时间（毫秒）
     * 默认7天
     */
    @Value("${jwt.expiration:604800000}")
    private Long expiration;
    
    /**
     * 生成SecretKey
     * 
     * @return SecretKey对象
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 生成JWT Token
     * 
     * @param userId 用户ID
     * @param openId 微信openId
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String openId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("openId", openId);
        return generateToken(claims);
    }
    
    /**
     * 根据claims生成JWT Token
     * 
     * @param claims 声明信息
     * @return JWT Token字符串
     */
    private String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 从Token中获取Claims
     * 
     * @param token JWT Token
     * @return Claims对象
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
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
     * @return 微信openId
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
            return false;
        }
    }
    
    /**
     * 获取Token过期时间
     * 
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.getExpiration();
        }
        return null;
    }
}

