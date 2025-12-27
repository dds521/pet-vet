package com.petvetai.domain.user.repository;

import com.petvetai.domain.user.model.User;
import com.petvetai.domain.user.model.UserId;

/**
 * 用户仓储接口
 * 
 * 定义用户聚合的持久化接口，实现在基础设施层
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public interface UserRepository {
    
    /**
     * 保存用户
     * 
     * @param user 用户聚合根
     * @author daidasheng
     * @date 2024-12-20
     */
    void save(User user);
    
    /**
     * 根据ID查找用户
     * 
     * @param userId 用户ID
     * @return 用户聚合根，如果不存在则返回null
     * @author daidasheng
     * @date 2024-12-20
     */
    User findById(UserId userId);
    
    /**
     * 根据openId查找用户
     * 
     * @param openId 微信openId
     * @return 用户聚合根，如果不存在则返回null
     * @author daidasheng
     * @date 2024-12-20
     */
    User findByOpenId(String openId);
    
    /**
     * 根据unionId查找用户
     * 
     * @param unionId 微信unionId
     * @return 用户聚合根，如果不存在则返回null
     * @author daidasheng
     * @date 2024-12-20
     */
    User findByUnionId(String unionId);
    
    /**
     * 检查用户是否存在
     * 
     * @param userId 用户ID
     * @return 是否存在
     * @author daidasheng
     * @date 2024-12-20
     */
    boolean existsById(UserId userId);
}

