package com.petvetai.infrastructure.persistence.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petvetai.infrastructure.persistence.transaction.po.TransactionLogPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 事务日志Mapper接口
 * 
 * 提供事务日志数据的CRUD操作
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Mapper
public interface TransactionLogMapper extends BaseMapper<TransactionLogPO> {
}

