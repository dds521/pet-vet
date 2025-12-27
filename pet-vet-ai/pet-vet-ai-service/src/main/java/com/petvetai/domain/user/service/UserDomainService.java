package com.petvetai.domain.user.service;

import com.petvetai.domain.user.model.User;
import com.petvetai.domain.user.model.WeChatInfo;
import com.petvetai.domain.user.repository.UserRepository;
import com.petvetai.infrastructure.external.wechat.WeChatApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户领域服务
 * 
 * 处理跨聚合的业务逻辑，如微信登录
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDomainService {
    
    private final UserRepository userRepository;
    private final WeChatApiService weChatApiService;
    
    /**
     * 微信登录或注册
     * 
     * 如果用户不存在则注册，存在则更新登录时间
     * 
     * @param code 微信登录code
     * @return 用户聚合根
     * @author daidasheng
     * @date 2024-12-20
     */
    public User loginOrRegister(String code) {
        log.info("开始微信登录，code: {}", code);
        
        // 1. 调用微信服务获取用户信息
        WeChatApiService.WeChatSessionResult session = weChatApiService.getWeChatSession(code);
        if (session == null || session.getOpenId() == null) {
            log.error("获取微信session失败，code: {}", code);
            throw new RuntimeException("微信登录失败：无法获取用户信息");
        }
        
        WeChatInfo weChatInfo = session.toWeChatInfo();
        log.info("获取到openId: {}", weChatInfo.getOpenId());
        
        // 2. 查找用户
        User user = userRepository.findByOpenId(weChatInfo.getOpenId());
        
        // 3. 如果不存在则注册
        if (user == null) {
            user = User.register(weChatInfo);
            userRepository.save(user);
            log.info("创建新用户，openId: {}, userId: {}", weChatInfo.getOpenId(), user.getId());
        } else {
            // 4. 更新登录时间和unionId
            user.updateLoginTime();
            if (weChatInfo.getUnionId() != null) {
                user.updateUnionId(weChatInfo.getUnionId());
            }
            userRepository.save(user);
            log.info("更新用户登录时间，openId: {}, userId: {}", weChatInfo.getOpenId(), user.getId());
        }
        
        return user;
    }
    
    /**
     * 根据openId查找用户
     * 
     * @param openId 微信openId
     * @return 用户聚合根
     * @author daidasheng
     * @date 2024-12-20
     */
    public User findUserByOpenId(String openId) {
        return userRepository.findByOpenId(openId);
    }
    
    /**
     * 保存用户
     * 
     * @param user 用户聚合根
     * @author daidasheng
     * @date 2024-12-20
     */
    public void saveUser(User user) {
        userRepository.save(user);
    }
}

