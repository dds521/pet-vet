package com.petvetai.infrastructure.persistence.pet;

import com.petvetai.domain.pet.model.Pet;
import com.petvetai.domain.pet.model.PetId;
import com.petvetai.domain.pet.repository.PetRepository;
import com.petvetai.infrastructure.persistence.pet.converter.PetConverter;
import com.petvetai.infrastructure.persistence.pet.mapper.PetMapper;
import com.petvetai.infrastructure.persistence.pet.po.PetPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 宠物仓储实现
 * 
 * 在基础设施层实现领域层定义的仓储接口
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Repository
@RequiredArgsConstructor
public class PetRepositoryImpl implements PetRepository {
    
    private final PetMapper petMapper;
    private final PetConverter petConverter;
    
    @Override
    public Pet findById(PetId petId) {
        if (petId == null) {
            return null;
        }
        PetPO petPO = petMapper.selectById(petId.getValue());
        if (petPO == null) {
            return null;
        }
        return petConverter.toDomain(petPO);
    }
}

