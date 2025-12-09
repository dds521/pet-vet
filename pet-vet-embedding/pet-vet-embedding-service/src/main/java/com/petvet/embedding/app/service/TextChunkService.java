package com.petvet.embedding.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petvet.embedding.app.domain.TextChunk;
import com.petvet.embedding.app.domain.TextChunkEntity;
import com.petvet.embedding.app.mapper.TextChunkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文本Chunk服务
 * 使用MySQL数据库持久化存储文本Chunk信息
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TextChunkService {
    
    private final TextChunkMapper chunkMapper;
    
    /**
     * 批量保存文本Chunks
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(List<TextChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<TextChunkEntity> entities = chunks.stream()
            .filter(chunk -> chunk.getChunkId() != null) // 过滤掉没有chunkId的
            .map(chunk -> {
                TextChunkEntity entity = convertToEntity(chunk);
                entity.setCreateTime(now);
                entity.setUpdateTime(now);
                return entity;
            })
            .collect(Collectors.toList());
        
        if (entities.isEmpty()) {
            log.warn("没有有效的Chunks需要保存（所有Chunks都缺少chunkId）");
            return;
        }
        
        // 批量插入或更新
        for (TextChunkEntity entity : entities) {
            TextChunkEntity existing = chunkMapper.selectById(entity.getChunkId());
            if (existing != null) {
                entity.setCreateTime(existing.getCreateTime()); // 保留原有创建时间
                entity.setUpdateTime(now);
                chunkMapper.updateById(entity);
            } else {
                chunkMapper.insert(entity);
            }
        }
        
        log.info("批量保存文本Chunks，数量: {}", entities.size());
    }
    
    /**
     * 根据Chunk ID查询文本内容
     */
    public String getTextByChunkId(String chunkId) {
        if (chunkId == null || chunkId.trim().isEmpty()) {
            return null;
        }
        
        TextChunkEntity entity = chunkMapper.selectById(chunkId);
        return entity != null ? entity.getText() : null;
    }
    
    /**
     * 根据Chunk ID列表批量查询文本内容
     */
    public Map<String, String> getTextsByChunkIds(List<String> chunkIds) {
        if (chunkIds == null || chunkIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<TextChunkEntity> entities = chunkMapper.selectByChunkIds(chunkIds);
        return entities.stream()
            .collect(Collectors.toMap(
                TextChunkEntity::getChunkId,
                TextChunkEntity::getText,
                (existing, replacement) -> existing
            ));
    }
    
    /**
     * 根据简历ID查询所有Chunks
     */
    public List<TextChunk> getByResumeId(String resumeId) {
        if (resumeId == null || resumeId.trim().isEmpty()) {
            return List.of();
        }
        
        List<TextChunkEntity> entities = chunkMapper.selectByResumeId(resumeId);
        return entities.stream()
            .map(this::convertToDomain)
            .collect(Collectors.toList());
    }
    
    /**
     * 根据简历ID删除所有Chunks
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByResumeId(String resumeId) {
        if (resumeId != null && !resumeId.trim().isEmpty()) {
            int deleted = chunkMapper.deleteByResumeId(resumeId);
            log.info("删除简历Chunks，简历ID: {}, 删除数量: {}", resumeId, deleted);
        }
    }
    
    /**
     * 将TextChunk转换为数据库实体
     */
    private TextChunkEntity convertToEntity(TextChunk chunk) {
        return TextChunkEntity.builder()
            .chunkId(chunk.getChunkId())
            .resumeId(chunk.getResumeId())
            .text(chunk.getText())
            .sequence(chunk.getSequence())
            .fieldType(chunk.getFieldType())
            .startPosition(chunk.getStartPosition())
            .endPosition(chunk.getEndPosition())
            .build();
    }
    
    /**
     * 将数据库实体转换为TextChunk
     */
    private TextChunk convertToDomain(TextChunkEntity entity) {
        return TextChunk.builder()
            .chunkId(entity.getChunkId())
            .resumeId(entity.getResumeId())
            .text(entity.getText())
            .sequence(entity.getSequence())
            .fieldType(entity.getFieldType())
            .startPosition(entity.getStartPosition())
            .endPosition(entity.getEndPosition())
            .build();
    }
}
