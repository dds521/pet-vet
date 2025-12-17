package com.petvetai.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petvetai.app.domain.VetAiAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<VetAiAccount> {
}

