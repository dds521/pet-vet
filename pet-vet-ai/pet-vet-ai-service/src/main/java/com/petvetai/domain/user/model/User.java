package com.petvetai.domain.user.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户聚合根
 * 
 * 用户是用户域的聚合根，负责管理用户相关的所有业务逻辑
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public class User {
    
    private UserId id;                    // 用户ID值对象
    private String nickName;              // 昵称
    private String avatarUrl;              // 头像
    private UserStatus status;             // 用户状态值对象
    private LocalDateTime lastLoginTime;   // 最后登录时间
    private WeChatInfo weChatInfo;         // 微信信息值对象
    private Integer gender;                // 性别：0-未知，1-男，2-女
    private String country;                // 国家
    private String province;               // 省份
    private String city;                   // 城市
    private String language;               // 语言
    
    /**
     * 包级私有方法：设置用户ID（用于从数据库加载后设置）
     * 仅供基础设施层的转换器使用
     * 
     * @param id 用户ID
     * @author daidasheng
     * @date 2024-12-20
     */
    void setIdInternal(UserId id) {
        this.id = id;
    }
    
    /**
     * 包级私有方法：设置微信信息（用于从数据库加载后设置）
     * 仅供基础设施层的转换器使用
     * 
     * @param weChatInfo 微信信息
     * @author daidasheng
     * @date 2024-12-20
     */
    void setWeChatInfoInternal(WeChatInfo weChatInfo) {
        this.weChatInfo = weChatInfo;
    }
    
    /**
     * 包级私有方法：设置用户状态（用于从数据库加载后设置）
     * 仅供基础设施层的转换器使用
     * 
     * @param status 用户状态
     * @author daidasheng
     * @date 2024-12-20
     */
    void setStatusInternal(UserStatus status) {
        this.status = status;
    }
    
    /**
     * 包级私有方法：设置最后登录时间（用于从数据库加载后设置）
     * 仅供基础设施层的转换器使用
     * 
     * @param lastLoginTime 最后登录时间
     * @author daidasheng
     * @date 2024-12-20
     */
    void setLastLoginTimeInternal(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    /**
     * 包级私有方法：设置昵称（用于从数据库加载后设置）
     * 仅供基础设施层的转换器使用
     * 
     * @param nickName 昵称
     * @author daidasheng
     * @date 2024-12-20
     */
    void setNickNameInternal(String nickName) {
        this.nickName = nickName;
    }
    
    /**
     * 包级私有方法：设置头像（用于从数据库加载后设置）
     * 仅供基础设施层的转换器使用
     * 
     * @param avatarUrl 头像URL
     * @author daidasheng
     * @date 2024-12-20
     */
    void setAvatarUrlInternal(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    /**
     * 包级私有方法：设置性别（用于从数据库加载后设置）
     * 仅供基础设施层的转换器使用
     * 
     * @param gender 性别
     * @author daidasheng
     * @date 2024-12-20
     */
    void setGenderInternal(Integer gender) {
        this.gender = gender;
    }
    
    /**
     * 包级私有方法：设置国家（用于从数据库加载后设置）
     * 仅供基础设施层的转换器使用
     * 
     * @param country 国家
     * @author daidasheng
     * @date 2024-12-20
     */
    void setCountryInternal(String country) {
        this.country = country;
    }
    
    /**
     * 包级私有方法：设置省份（用于从数据库加载后设置）
     * 仅供基础设施层的转换器使用
     * 
     * @param province 省份
     * @author daidasheng
     * @date 2024-12-20
     */
    void setProvinceInternal(String province) {
        this.province = province;
    }
    
    /**
     * 包级私有方法：设置城市（用于从数据库加载后设置）
     * 仅供基础设施层的转换器使用
     * 
     * @param city 城市
     * @author daidasheng
     * @date 2024-12-20
     */
    void setCityInternal(String city) {
        this.city = city;
    }
    
    /**
     * 包级私有方法：设置语言（用于从数据库加载后设置）
     * 仅供基础设施层的转换器使用
     * 
     * @param language 语言
     * @author daidasheng
     * @date 2024-12-20
     */
    void setLanguageInternal(String language) {
        this.language = language;
    }
    
    /**
     * 私有构造函数，防止直接创建
     * 
     * @author daidasheng
     * @date 2024-12-20
     */
    private User() {
    }
    
    /**
     * 用户注册
     * 
     * @param weChatInfo 微信信息
     * @return 注册后的用户
     * @author daidasheng
     * @date 2024-12-20
     */
    public static User register(WeChatInfo weChatInfo) {
        User user = new User();
        user.id = UserId.generate();
        user.weChatInfo = weChatInfo;
        user.status = UserStatus.ACTIVE;
        user.lastLoginTime = LocalDateTime.now();
        return user;
    }
    
    /**
     * 从已有数据重建用户（用于从数据库加载）
     * 
     * @param id 用户ID
     * @param weChatInfo 微信信息
     * @param status 用户状态
     * @param lastLoginTime 最后登录时间
     * @return 用户聚合根
     * @author daidasheng
     * @date 2024-12-20
     */
    public static User reconstruct(UserId id, WeChatInfo weChatInfo, UserStatus status, LocalDateTime lastLoginTime) {
        User user = new User();
        user.id = id;
        user.weChatInfo = weChatInfo;
        user.status = status;
        user.lastLoginTime = lastLoginTime;
        return user;
    }
    
    /**
     * 更新登录时间
     * 
     * @author daidasheng
     * @date 2024-12-20
     */
    public void updateLoginTime() {
        this.lastLoginTime = LocalDateTime.now();
    }
    
    /**
     * 更新用户信息
     * 
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
    public void updateProfile(String nickName, String avatarUrl, Integer gender, 
                             String country, String province, String city, String language) {
        if (nickName != null && !nickName.trim().isEmpty()) {
            this.nickName = nickName;
        }
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (country != null) {
            this.country = country;
        }
        if (province != null) {
            this.province = province;
        }
        if (city != null) {
            this.city = city;
        }
        if (language != null) {
            this.language = language;
        }
    }
    
    /**
     * 更新微信unionId
     * 
     * @param unionId 新的unionId
     * @author daidasheng
     * @date 2024-12-20
     */
    public void updateUnionId(String unionId) {
        if (unionId != null && !unionId.equals(this.weChatInfo.getUnionId())) {
            this.weChatInfo = this.weChatInfo.withUnionId(unionId);
        }
    }
    
    /**
     * 禁用用户
     * 
     * @author daidasheng
     * @date 2024-12-20
     */
    public void disable() {
        if (this.status == UserStatus.DISABLED) {
            throw new IllegalStateException("用户已被禁用");
        }
        this.status = UserStatus.DISABLED;
    }
    
    /**
     * 启用用户
     * 
     * @author daidasheng
     * @date 2024-12-20
     */
    public void enable() {
        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("用户已启用");
        }
        this.status = UserStatus.ACTIVE;
    }
    
    /**
     * 检查用户是否可用
     * 
     * @return 是否可用
     * @author daidasheng
     * @date 2024-12-20
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
    
    // Getters
    public UserId getId() {
        return id;
    }
    
    public String getNickName() {
        return nickName;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }
    
    public WeChatInfo getWeChatInfo() {
        return weChatInfo;
    }
    
    public Integer getGender() {
        return gender;
    }
    
    public String getCountry() {
        return country;
    }
    
    public String getProvince() {
        return province;
    }
    
    public String getCity() {
        return city;
    }
    
    public String getLanguage() {
        return language;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

