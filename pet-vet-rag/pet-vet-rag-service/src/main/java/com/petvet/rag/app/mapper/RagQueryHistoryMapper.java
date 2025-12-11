package com.petvet.rag.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petvet.rag.app.domain.RagQueryHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * RAG 查询历史记录 Mapper 接口
 * 
 * 提供 RAG 查询历史记录的数据库访问方法
 * 
 * @author daidasheng
 * @date 2024-12-11
 */
@Mapper
public interface RagQueryHistoryMapper extends BaseMapper<RagQueryHistoryEntity> {
    // 可以在此添加自定义查询方法
    // 复杂查询可以在对应的 XML 文件中实现
}
