package com.petvetai.infrastructure.persistence.user.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petvet.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户持久化对象
 * 
 * 用于数据库持久化，对应 vet_ai_wechat_user 表
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vet_ai_wechat_user")
public class UserPO extends BaseEntity {
    
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 微信用户唯一标识（openId）
     */
    private String openId;
    
    /**
     * 微信用户统一标识（unionId）
     */
    private String unionId;
    
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
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 用户状态：0-禁用，1-启用
     */
    private Integer status;
}

