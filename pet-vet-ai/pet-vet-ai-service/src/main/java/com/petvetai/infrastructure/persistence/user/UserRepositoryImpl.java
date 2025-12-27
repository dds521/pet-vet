package com.petvetai.infrastructure.persistence.user;

import com.petvetai.domain.user.model.User;
import com.petvetai.domain.user.model.UserId;
import com.petvetai.domain.user.repository.UserRepository;
import com.petvetai.infrastructure.persistence.user.converter.UserConverter;
import com.petvetai.infrastructure.persistence.user.mapper.UserMapper;
import com.petvetai.infrastructure.persistence.user.po.VetAiUserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 用户仓储实现
 * 
 * 在基础设施层实现领域层定义的仓储接口
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    
    private final UserMapper userMapper;
    private final UserConverter userConverter;
    
    @Override
    public void save(User user) {
        // 转换为PO（持久化对象）
        VetAiUserPO userPO = userConverter.toPO(user);
        
        // 判断是新增还是更新
        if (user.getId() != null && userMapper.selectById(user.getId().getValue()) != null) {
            userMapper.updateById(userPO);
        } else {
            userMapper.insert(userPO);
            // 注意：新增后ID由数据库生成，但领域对象无法直接更新
            // 实际使用中，应该在应用服务层处理ID的更新，或者使用领域事件
        }
    }
    
    @Override
    public User findById(UserId userId) {
        if (userId == null) {
            return null;
        }
        VetAiUserPO userPO = userMapper.selectById(userId.getValue());
        if (userPO == null) {
            return null;
        }
        return userConverter.toDomain(userPO);
    }
    
    @Override
    public User findByOpenId(String openId) {
        if (openId == null || openId.trim().isEmpty()) {
            return null;
        }
        VetAiUserPO userPO = userMapper.selectByOpenId(openId);
        if (userPO == null) {
            return null;
        }
        return userConverter.toDomain(userPO);
    }
    
    @Override
    public User findByUnionId(String unionId) {
        if (unionId == null || unionId.trim().isEmpty()) {
            return null;
        }
        VetAiUserPO userPO = userMapper.selectByUnionId(unionId);
        if (userPO == null) {
            return null;
        }
        return userConverter.toDomain(userPO);
    }
    
    @Override
    public boolean existsById(UserId userId) {
        if (userId == null) {
            return false;
        }
        return userMapper.selectById(userId.getValue()) != null;
    }
}

