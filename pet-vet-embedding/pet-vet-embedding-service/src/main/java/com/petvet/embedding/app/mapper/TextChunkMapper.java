package com.petvet.embedding.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petvet.embedding.app.domain.VetEmbeddingTextChunkEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文本Chunk Mapper接口
 * 
 * 提供文本Chunk的CRUD操作
 * 
 * @author PetVetEmbedding Team
 */
@Mapper
public interface TextChunkMapper extends BaseMapper<VetEmbeddingTextChunkEntity> {
    
    /**
     * 根据简历ID查询所有Chunks
     * 
     * @param resumeId 简历ID
     * @return Chunk列表
     */
    List<VetEmbeddingTextChunkEntity> selectByResumeId(@Param("resumeId") String resumeId);
    
    /**
     * 根据Chunk ID列表批量查询
     * 
     * @param chunkIds Chunk ID列表
     * @return Chunk列表
     */
    List<VetEmbeddingTextChunkEntity> selectByChunkIds(@Param("chunkIds") List<String> chunkIds);
    
    /**
     * 根据简历ID删除所有Chunks
     * 
     * @param resumeId 简历ID
     * @return 删除数量
     */
    int deleteByResumeId(@Param("resumeId") String resumeId);
}
