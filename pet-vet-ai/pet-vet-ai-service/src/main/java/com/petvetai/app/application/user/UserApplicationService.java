package com.petvetai.app.application.user;

import com.petvetai.domain.user.model.User;
import com.petvetai.domain.user.service.UserDomainService;
import com.petvetai.infrastructure.cache.redis.RedisService;
import com.petvetai.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 用户应用服务
 * 
 * 协调领域对象完成业务用例，处理事务、缓存等技术问题
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationService {
    
    private final UserDomainService userDomainService;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    
    @Value("${wechat.redis.session-key-prefix:wechat:session:}")
    private String redisSessionKeyPrefix;
    
    @Value("${wechat.redis.session-key-expire-seconds:172800}")
    private long sessionKeyExpireSeconds;
    
    /**
     * 微信登录
     * 
     * @param code 微信登录凭证code
     * @return 登录结果
     * @author daidasheng
     * @date 2024-12-20
     */
    @Transactional
    public WeChatLoginResult weChatLogin(String code) {
        // 1. 调用领域服务完成业务逻辑
        User user = userDomainService.loginOrRegister(code);
        
        // 2. 生成JWT Token（技术实现）
        String token = jwtUtil.generateToken(user.getId().getValue(), user.getWeChatInfo().getOpenId());
        
        // 3. 缓存sessionKey（技术实现）
        if (user.getWeChatInfo().getSessionKey() != null) {
            String redisKey = redisSessionKeyPrefix + user.getWeChatInfo().getOpenId();
            redisService.set(redisKey, user.getWeChatInfo().getSessionKey(), sessionKeyExpireSeconds, TimeUnit.SECONDS);
        }
        
        log.info("微信登录成功，userId: {}, openId: {}", user.getId(), user.getWeChatInfo().getOpenId());
        
        // 4. 构建返回结果
        return WeChatLoginResult.builder()
            .token(token)
            .userId(user.getId().getValue())
            .openId(user.getWeChatInfo().getOpenId())
            .nickName(user.getNickName())
            .avatarUrl(user.getAvatarUrl())
            .gender(user.getGender())
            .build();
    }
    
    /**
     * 更新用户信息
     * 
     * @param openId 微信openId
     * @param nickName 昵称
     * @param avatarUrl 头像
     * @param gender 性别
     * @param country 国家
     * @param province 省份
     * @param city 城市
     * @param language 语言
     * @author daidasheng
     * @date 2024-12-20
     */
    @Transactional
    public void updateUserInfo(String openId, String nickName, String avatarUrl, Integer gender,
                              String country, String province, String city, String language) {
        // 1. 通过领域服务获取用户（这里简化处理，实际应该通过领域服务）
        User user = userDomainService.findUserByOpenId(openId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 2. 更新用户信息
        user.updateProfile(nickName, avatarUrl, gender, country, province, city, language);
        
        // 3. 保存（通过领域服务）
        userDomainService.saveUser(user);
        
        log.info("更新用户信息成功，openId: {}", openId);
    }
    
    /**
     * 微信登录结果
     * 
     * @author daidasheng
     * @date 2024-12-20
     */
    @lombok.Data
    @lombok.Builder
    public static class WeChatLoginResult {
        private String token;
        private Long userId;
        private String openId;
        private String nickName;
        private String avatarUrl;
        private Integer gender;
    }
}

