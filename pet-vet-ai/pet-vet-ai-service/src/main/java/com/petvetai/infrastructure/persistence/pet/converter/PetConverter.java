package com.petvetai.infrastructure.persistence.pet.converter;

import com.petvetai.domain.pet.model.Pet;
import com.petvetai.domain.pet.model.PetId;
import com.petvetai.domain.pet.model.PetInfo;
import com.petvetai.infrastructure.persistence.pet.po.VetAiPetPO;
import org.springframework.stereotype.Component;

/**
 * 宠物转换器
 * 
 * 负责领域对象和持久化对象之间的转换
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Component
public class PetConverter {
    
    /**
     * 将领域对象转换为持久化对象
     * 
     * @param pet 宠物聚合根
     * @return 持久化对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public VetAiPetPO toPO(Pet pet) {
        if (pet == null) {
            return null;
        }
        
        VetAiPetPO po = new VetAiPetPO();
        po.setId(pet.getId() != null ? pet.getId().getValue() : null);
        if (pet.getPetInfo() != null) {
            po.setName(pet.getPetInfo().getName());
            po.setBreed(pet.getPetInfo().getBreed());
            po.setAge(pet.getPetInfo().getAge());
        }
        
        return po;
    }
    
    /**
     * 将持久化对象转换为领域对象
     * 
     * @param po 持久化对象
     * @return 宠物聚合根
     * @author daidasheng
     * @date 2024-12-20
     */
    public Pet toDomain(VetAiPetPO po) {
        if (po == null) {
            return null;
        }
        
        // 构建宠物信息值对象
        PetInfo petInfo = PetInfo.of(po.getName(), po.getBreed(), po.getAge());
        
        // 重建宠物聚合根
        Pet pet = Pet.reconstruct(PetId.of(po.getId()), petInfo);
        
        return pet;
    }
}

