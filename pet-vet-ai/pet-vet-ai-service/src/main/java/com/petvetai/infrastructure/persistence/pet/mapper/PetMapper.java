package com.petvetai.infrastructure.persistence.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petvetai.infrastructure.persistence.pet.po.VetAiPetPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物Mapper接口
 * 
 * 提供宠物数据的CRUD操作
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Mapper
public interface PetMapper extends BaseMapper<VetAiPetPO> {
}

