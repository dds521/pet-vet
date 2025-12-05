package com.petvetai.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 微信用户实体类
 * 
 * 用于存储微信小程序用户的登录信息
 * 包括openId、unionId、sessionKey等微信相关字段
 * 
 * @author PetVetAI Team
 * @date 2024-01-01
 */
@Data
@NoArgsConstructor
@TableName("wechat_users")
public class WeChatUser {
    
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 微信用户唯一标识（openId）
     * 每个用户在每个小程序中的唯一标识
     */
    private String openId;
    
    /**
     * 微信用户统一标识（unionId）
     * 同一微信开放平台账号下的应用，unionId是唯一的
     * 可选字段，需要绑定微信开放平台账号
     */
    private String unionId;
    
    /**
     * 微信会话密钥（sessionKey）
     * 用于解密微信加密数据，不存储到数据库（仅临时使用）
     * 注意：此字段仅用于传输，实际不持久化
     */
    private transient String sessionKey;
    
    /**
     * 用户昵称
     */
    private String nickName;
    
    /**
     * 用户头像URL
     */
    private String avatarUrl;
    
    /**
     * 用户性别：0-未知，1-男，2-女
     */
    private Integer gender;
    
    /**
     * 用户所在国家
     */
    private String country;
    
    /**
     * 用户所在省份
     */
    private String province;
    
    /**
     * 用户所在城市
     */
    private String city;
    
    /**
     * 用户语言
     */
    private String language;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 用户状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 构造函数
     * 
     * @param openId 微信openId
     */
    public WeChatUser(String openId) {
        this.openId = openId;
        this.status = 1; // 默认启用
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.lastLoginTime = LocalDateTime.now();
    }
}

