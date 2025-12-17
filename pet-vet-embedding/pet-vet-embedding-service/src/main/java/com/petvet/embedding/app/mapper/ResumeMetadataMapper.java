package com.petvet.embedding.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petvet.embedding.app.domain.VetEmbeddingResumeMetadataEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 简历元数据Mapper接口
 * 
 * 提供简历元数据的CRUD操作
 * 
 * @author PetVetEmbedding Team
 */
@Mapper
public interface ResumeMetadataMapper extends BaseMapper<VetEmbeddingResumeMetadataEntity> {
}
