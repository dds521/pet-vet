package com.petvetai.domain.pet.repository;

import com.petvetai.domain.pet.model.Pet;
import com.petvetai.domain.pet.model.PetId;

/**
 * 宠物仓储接口
 * 
 * 定义宠物聚合的持久化接口，实现在基础设施层
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public interface PetRepository {
    
    /**
     * 根据ID查找宠物
     * 
     * @param petId 宠物ID
     * @return 宠物聚合根，如果不存在则返回null
     * @author daidasheng
     * @date 2024-12-20
     */
    Pet findById(PetId petId);
}

