package com.petvetai.infrastructure.persistence.user.converter;

import com.petvetai.domain.user.model.User;
import com.petvetai.domain.user.model.UserId;
import com.petvetai.domain.user.model.UserStatus;
import com.petvetai.domain.user.model.WeChatInfo;
import com.petvetai.infrastructure.persistence.user.po.UserPO;
import org.springframework.stereotype.Component;

/**
 * 用户转换器
 * 
 * 负责领域对象和持久化对象之间的转换
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Component
public class UserConverter {
    
    /**
     * 将领域对象转换为持久化对象
     * 
     * @param user 用户聚合根
     * @return 持久化对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public UserPO toPO(User user) {
        if (user == null) {
            return null;
        }
        
        UserPO po = new UserPO();
        po.setId(user.getId() != null ? user.getId().getValue() : null);
        po.setOpenId(user.getWeChatInfo() != null ? user.getWeChatInfo().getOpenId() : null);
        po.setUnionId(user.getWeChatInfo() != null ? user.getWeChatInfo().getUnionId() : null);
        po.setNickName(user.getNickName());
        po.setAvatarUrl(user.getAvatarUrl());
        po.setGender(user.getGender());
        po.setCountry(user.getCountry());
        po.setProvince(user.getProvince());
        po.setCity(user.getCity());
        po.setLanguage(user.getLanguage());
        po.setLastLoginTime(user.getLastLoginTime());
        po.setStatus(user.getStatus() != null ? user.getStatus().getCode() : UserStatus.DISABLED.getCode());
        
        return po;
    }
    
    /**
     * 将持久化对象转换为领域对象
     * 
     * @param po 持久化对象
     * @return 用户聚合根
     * @author daidasheng
     * @date 2024-12-20
     */
    public User toDomain(UserPO po) {
        if (po == null) {
            return null;
        }
        
        // 构建微信信息值对象
        WeChatInfo weChatInfo = WeChatInfo.of(po.getOpenId(), po.getUnionId());
        
        // 重建用户聚合根
        User user = User.reconstruct(
            UserId.of(po.getId()),
            weChatInfo,
            UserStatus.fromCode(po.getStatus()),
            po.getLastLoginTime() != null ? po.getLastLoginTime() : java.time.LocalDateTime.now()
        );
        
        // 设置其他属性（使用反射访问包级私有方法）
        try {
            java.lang.reflect.Method setNickName = User.class.getDeclaredMethod("setNickNameInternal", String.class);
            java.lang.reflect.Method setAvatarUrl = User.class.getDeclaredMethod("setAvatarUrlInternal", String.class);
            java.lang.reflect.Method setGender = User.class.getDeclaredMethod("setGenderInternal", Integer.class);
            java.lang.reflect.Method setCountry = User.class.getDeclaredMethod("setCountryInternal", String.class);
            java.lang.reflect.Method setProvince = User.class.getDeclaredMethod("setProvinceInternal", String.class);
            java.lang.reflect.Method setCity = User.class.getDeclaredMethod("setCityInternal", String.class);
            java.lang.reflect.Method setLanguage = User.class.getDeclaredMethod("setLanguageInternal", String.class);
            
            setNickName.setAccessible(true);
            setAvatarUrl.setAccessible(true);
            setGender.setAccessible(true);
            setCountry.setAccessible(true);
            setProvince.setAccessible(true);
            setCity.setAccessible(true);
            setLanguage.setAccessible(true);
            
            setNickName.invoke(user, po.getNickName());
            setAvatarUrl.invoke(user, po.getAvatarUrl());
            setGender.invoke(user, po.getGender());
            setCountry.invoke(user, po.getCountry());
            setProvince.invoke(user, po.getProvince());
            setCity.invoke(user, po.getCity());
            setLanguage.invoke(user, po.getLanguage());
        } catch (Exception e) {
            throw new RuntimeException("转换用户对象失败", e);
        }
        
        return user;
    }
}

