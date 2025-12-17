package com.petvetai.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petvetai.app.domain.VetAiOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<VetAiOrder> {
}

