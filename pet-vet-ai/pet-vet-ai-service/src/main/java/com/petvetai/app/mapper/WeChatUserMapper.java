package com.petvetai.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petvetai.app.domain.WeChatUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 微信用户Mapper接口
 * 
 * 提供微信用户数据的CRUD操作
 * 
 * @author PetVetAI Team
 * @date 2024-01-01
 */
@Mapper
public interface WeChatUserMapper extends BaseMapper<WeChatUser> {
    
    /**
     * 根据openId查询用户
     * 
     * @param openId 微信openId
     * @return 微信用户信息
     */
    WeChatUser selectByOpenId(@Param("openId") String openId);
    
    /**
     * 根据unionId查询用户
     * 
     * @param unionId 微信unionId
     * @return 微信用户信息
     */
    WeChatUser selectByUnionId(@Param("unionId") String unionId);
}

