package com.petvetai.infrastructure.persistence.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petvetai.infrastructure.persistence.user.po.UserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 * 
 * 提供用户数据的CRUD操作
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {
    
    /**
     * 根据openId查询用户
     * 
     * @param openId 微信openId
     * @return 用户信息
     * @author daidasheng
     * @date 2024-12-20
     */
    UserPO selectByOpenId(@Param("openId") String openId);
    
    /**
     * 根据unionId查询用户
     * 
     * @param unionId 微信unionId
     * @return 用户信息
     * @author daidasheng
     * @date 2024-12-20
     */
    UserPO selectByUnionId(@Param("unionId") String unionId);
}

