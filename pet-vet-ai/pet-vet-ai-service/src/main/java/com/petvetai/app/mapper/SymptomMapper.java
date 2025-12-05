package com.petvetai.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petvetai.app.domain.Symptom;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SymptomMapper extends BaseMapper<Symptom> {
}

