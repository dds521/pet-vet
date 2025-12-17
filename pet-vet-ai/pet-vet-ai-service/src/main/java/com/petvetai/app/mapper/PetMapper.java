package com.petvetai.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petvetai.app.domain.VetAiPet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetMapper extends BaseMapper<VetAiPet> {
}

