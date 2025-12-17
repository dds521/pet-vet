package com.petvet.embedding.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petvet.embedding.app.domain.TextChunk;
import com.petvet.embedding.app.domain.VetEmbeddingTextChunkEntity;
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
            log.warn("TextChunkService.saveBatch: chunks 为空，跳过保存");
            return;
        }
        
        log.debug("开始批量保存文本Chunks，输入数量: {}", chunks.size());
        
        LocalDateTime now = LocalDateTime.now();
        List<VetEmbeddingTextChunkEntity> entities = chunks.stream()
            .filter(chunk -> {
                if (chunk.getChunkId() == null || chunk.getChunkId().trim().isEmpty()) {
                    log.warn("跳过保存：chunk 缺少 chunkId, resumeId: {}, text: {}", 
                        chunk.getResumeId(), 
                        chunk.getText() != null ? chunk.getText().substring(0, Math.min(50, chunk.getText().length())) : "null");
                    return false;
                }
                return true;
            })
            .map(chunk -> {
                VetEmbeddingTextChunkEntity entity = convertToEntity(chunk);
                entity.setCreateTime(now);
                entity.setUpdateTime(now);
                return entity;
            })
            .collect(Collectors.toList());
        
        if (entities.isEmpty()) {
            log.warn("没有有效的Chunks需要保存（所有Chunks都缺少chunkId），输入数量: {}", chunks.size());
            return;
        }
        
        log.debug("准备保存 {} 个有效的Chunks", entities.size());
        
        // 批量插入或更新
        int savedCount = 0;
        int updatedCount = 0;
        for (VetEmbeddingTextChunkEntity entity : entities) {
            try {
                VetEmbeddingTextChunkEntity existing = chunkMapper.selectById(entity.getChunkId());
                if (existing != null) {
                    entity.setCreateTime(existing.getCreateTime()); // 保留原有创建时间
                    entity.setUpdateTime(now);
                    chunkMapper.updateById(entity);
                    updatedCount++;
                } else {
                    chunkMapper.insert(entity);
                    savedCount++;
                }
            } catch (Exception e) {
                log.error("保存Chunk失败，chunkId: {}, resumeId: {}, 错误: {}", 
                    entity.getChunkId(), entity.getResumeId(), e.getMessage(), e);
                throw e; // 重新抛出异常，让事务回滚
            }
        }
        
        log.info("批量保存文本Chunks完成，总数: {}, 新增: {}, 更新: {}", entities.size(), savedCount, updatedCount);
    }
    
    /**
     * 根据Chunk ID查询文本内容
     */
    public String getTextByChunkId(String chunkId) {
        if (chunkId == null || chunkId.trim().isEmpty()) {
            return null;
        }
        
        VetEmbeddingTextChunkEntity entity = chunkMapper.selectById(chunkId);
        return entity != null ? entity.getText() : null;
    }
    
    /**
     * 根据Chunk ID列表批量查询文本内容
     */
    public Map<String, String> getTextsByChunkIds(List<String> chunkIds) {
        if (chunkIds == null || chunkIds.isEmpty()) {
            log.warn("getTextsByChunkIds: chunkIds 为空");
            return new HashMap<>();
        }
        
        log.debug("查询文本内容，chunkIds数量: {}, chunkIds: {}", chunkIds.size(), chunkIds);
        
        List<VetEmbeddingTextChunkEntity> entities = chunkMapper.selectByChunkIds(chunkIds);
        
        log.debug("数据库查询结果，找到 {} 条记录", entities.size());
        if (entities.isEmpty()) {
            log.warn("数据库中没有找到任何匹配的chunks，查询的chunkIds: {}", chunkIds);
        } else {
            log.debug("找到的chunkIds: {}", entities.stream().map(VetEmbeddingTextChunkEntity::getChunkId).collect(Collectors.toList()));
        }
        
        Map<String, String> result = entities.stream()
            .collect(Collectors.toMap(
                VetEmbeddingTextChunkEntity::getChunkId,
                VetEmbeddingTextChunkEntity::getText,
                (existing, replacement) -> existing
            ));
        
        log.debug("返回文本内容Map，大小: {}", result.size());
        return result;
    }
    
    /**
     * 根据简历ID查询所有Chunks
     */
    public List<TextChunk> getByResumeId(String resumeId) {
        if (resumeId == null || resumeId.trim().isEmpty()) {
            return List.of();
        }
        
        List<VetEmbeddingTextChunkEntity> entities = chunkMapper.selectByResumeId(resumeId);
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
     * 删除所有文本Chunks
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteAll() {
        int deleted = chunkMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>());
        log.info("删除所有文本Chunks，数量: {}", deleted);
        return deleted;
    }
    
    /**
     * 将TextChunk转换为数据库实体
     */
    private VetEmbeddingTextChunkEntity convertToEntity(TextChunk chunk) {
        return VetEmbeddingTextChunkEntity.builder()
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
    private TextChunk convertToDomain(VetEmbeddingTextChunkEntity entity) {
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
