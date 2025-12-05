package com.petvetai.app.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.petvetai.app.domain.WeChatUser;
import com.petvetai.app.mapper.WeChatUserMapper;
import com.petvetai.app.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 微信认证服务
 * 
 * 负责处理微信小程序的登录认证逻辑
 * 包括：获取微信用户信息、创建/更新用户、生成JWT Token等
 * 
 * @author PetVetAI Team
 * @date 2024-01-01
 */
@Slf4j
@Service
public class WeChatAuthService {
    
    /**
     * 微信小程序AppID（从配置文件读取）
     */
    @Value("${wechat.miniapp.appid:}")
    private String appId;
    
    /**
     * 微信小程序AppSecret（从配置文件读取）
     */
    @Value("${wechat.miniapp.secret:}")
    private String appSecret;
    
    /**
     * 微信API：通过code获取openId和sessionKey（从配置文件读取）
     * 默认值：https://api.weixin.qq.com/sns/jscode2session
     */
    @Value("${wechat.api.login-url:https://api.weixin.qq.com/sns/jscode2session}")
    private String wechatLoginUrl;
    
    /**
     * Redis中存储sessionKey的key前缀（从配置文件读取）
     * 默认值：wechat:session:
     */
    @Value("${wechat.redis.session-key-prefix:wechat:session:}")
    private String redisSessionKeyPrefix;
    
    /**
     * sessionKey在Redis中的过期时间（秒，从配置文件读取）
     * 微信sessionKey有效期为3天，默认设置为2天
     * 默认值：172800（2天）
     */
    @Value("${wechat.redis.session-key-expire-seconds:172800}")
    private long sessionKeyExpireSeconds;
    
    @Autowired
    private WeChatUserMapper weChatUserMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 微信登录
     * 
     * 1. 通过code从微信服务器获取openId和sessionKey
     * 2. 查询或创建用户
     * 3. 生成JWT Token返回给前端
     * 
     * @param code 微信登录凭证code
     * @return 登录结果，包含token和用户信息
     */
    @Transactional
    public LoginResult login(String code) {
        log.info("开始微信登录，code: {}", code);
        
        // 1. 通过code获取openId和sessionKey
        WeChatSession session = getWeChatSession(code);
        if (session == null || session.getOpenId() == null) {
            log.error("获取微信session失败，code: {}", code);
            throw new RuntimeException("微信登录失败：无法获取用户信息");
        }
        
        String openId = session.getOpenId();
        String sessionKey = session.getSessionKey();
        
        log.info("获取到openId: {}, sessionKey: {}", openId, maskSessionKey(sessionKey));
        
        // 2. 将sessionKey存储到Redis（用于后续解密数据）
        String redisKey = redisSessionKeyPrefix + openId;
        redisTemplate.opsForValue().set(redisKey, sessionKey, sessionKeyExpireSeconds, TimeUnit.SECONDS);
        
        // 3. 查询或创建用户
        WeChatUser user = weChatUserMapper.selectByOpenId(openId);
        if (user == null) {
            // 新用户，创建账户
            user = new WeChatUser(openId);
            user.setUnionId(session.getUnionId());
            weChatUserMapper.insert(user);
            log.info("创建新用户，openId: {}, userId: {}", openId, user.getId());
        } else {
            // 老用户，更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            if (session.getUnionId() != null && user.getUnionId() == null) {
                // 如果之前没有unionId，现在有了，则更新
                user.setUnionId(session.getUnionId());
            }
            weChatUserMapper.updateById(user);
            log.info("更新用户登录时间，openId: {}, userId: {}", openId, user.getId());
        }
        
        // 4. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), openId);
        
        log.info("微信登录成功，userId: {}, openId: {}", user.getId(), openId);
        
        return new LoginResult(token, user);
    }
    
    /**
     * 更新用户信息
     * 
     * 用于用户授权后更新昵称、头像等信息
     * 
     * @param openId 微信openId
     * @param userInfo 用户信息
     */
    @Transactional
    public void updateUserInfo(String openId, WeChatUserInfo userInfo) {
        WeChatUser user = weChatUserMapper.selectByOpenId(openId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 更新用户信息
        if (userInfo.getNickName() != null) {
            user.setNickName(userInfo.getNickName());
        }
        if (userInfo.getAvatarUrl() != null) {
            user.setAvatarUrl(userInfo.getAvatarUrl());
        }
        if (userInfo.getGender() != null) {
            user.setGender(userInfo.getGender());
        }
        if (userInfo.getCountry() != null) {
            user.setCountry(userInfo.getCountry());
        }
        if (userInfo.getProvince() != null) {
            user.setProvince(userInfo.getProvince());
        }
        if (userInfo.getCity() != null) {
            user.setCity(userInfo.getCity());
        }
        if (userInfo.getLanguage() != null) {
            user.setLanguage(userInfo.getLanguage());
        }
        
        user.setUpdateTime(LocalDateTime.now());
        weChatUserMapper.updateById(user);
        
        log.info("更新用户信息成功，openId: {}", openId);
    }
    
    /**
     * 通过code从微信服务器获取openId和sessionKey
     * 
     * @param code 微信登录凭证code
     * @return 微信session信息
     */
    private WeChatSession getWeChatSession(String code) {
        try {
            // 构建请求URL
            String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wechatLoginUrl, appId, appSecret, code);
            
            log.info("请求微信API: {}", url.replace(appSecret, "***"));
            
            // 发送HTTP请求
            String response = HttpUtil.get(url);
            log.info("微信API响应: {}", response);
            
            // 解析响应
            JSONObject json = JSONUtil.parseObj(response);
            
            // 检查是否有错误
            if (json.containsKey("errcode")) {
                Integer errcode = json.getInt("errcode");
                String errmsg = json.getStr("errmsg");
                log.error("微信API返回错误，errcode: {}, errmsg: {}", errcode, errmsg);
                throw new RuntimeException("微信登录失败：" + errmsg);
            }
            
            // 提取openId和sessionKey
            WeChatSession session = new WeChatSession();
            session.setOpenId(json.getStr("openid"));
            session.setSessionKey(json.getStr("session_key"));
            session.setUnionId(json.getStr("unionid")); // 可选字段
            
            return session;
        } catch (Exception e) {
            log.error("获取微信session异常", e);
            throw new RuntimeException("获取微信用户信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 掩码sessionKey，只显示前后几位
     * 
     * @param sessionKey 原始sessionKey
     * @return 掩码后的sessionKey
     */
    private String maskSessionKey(String sessionKey) {
        if (sessionKey == null || sessionKey.length() <= 8) {
            return "***";
        }
        return sessionKey.substring(0, 4) + "..." + sessionKey.substring(sessionKey.length() - 4);
    }
    
    /**
     * 微信Session信息
     */
    public static class WeChatSession {
        private String openId;
        private String sessionKey;
        private String unionId;
        
        public String getOpenId() {
            return openId;
        }
        
        public void setOpenId(String openId) {
            this.openId = openId;
        }
        
        public String getSessionKey() {
            return sessionKey;
        }
        
        public void setSessionKey(String sessionKey) {
            this.sessionKey = sessionKey;
        }
        
        public String getUnionId() {
            return unionId;
        }
        
        public void setUnionId(String unionId) {
            this.unionId = unionId;
        }
    }
    
    /**
     * 登录结果
     */
    public static class LoginResult {
        private String token;
        private WeChatUser user;
        
        public LoginResult(String token, WeChatUser user) {
            this.token = token;
            this.user = user;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public WeChatUser getUser() {
            return user;
        }
        
        public void setUser(WeChatUser user) {
            this.user = user;
        }
    }
    
    /**
     * 微信用户信息（用于更新用户信息）
     */
    public static class WeChatUserInfo {
        private String nickName;
        private String avatarUrl;
        private Integer gender;
        private String country;
        private String province;
        private String city;
        private String language;
        
        public String getNickName() {
            return nickName;
        }
        
        public void setNickName(String nickName) {
            this.nickName = nickName;
        }
        
        public String getAvatarUrl() {
            return avatarUrl;
        }
        
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
        
        public Integer getGender() {
            return gender;
        }
        
        public void setGender(Integer gender) {
            this.gender = gender;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String getProvince() {
            return province;
        }
        
        public void setProvince(String province) {
            this.province = province;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getLanguage() {
            return language;
        }
        
        public void setLanguage(String language) {
            this.language = language;
        }
    }
}

