package com.petvet.embedding.app.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petvet.embedding.api.dto.ResumeMetadata;
import com.petvet.embedding.api.resp.ResumeMetadataResp;
import com.petvet.embedding.app.domain.ResumeMetadataEntity;
import com.petvet.embedding.app.mapper.ResumeMetadataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简历元数据服务
 * 使用MySQL数据库持久化存储简历元数据
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeMetadataService {
    
    private final ResumeMetadataMapper metadataMapper;
    
    /**
     * 保存简历元数据（内部使用ResumeMetadata）
     */
    @Transactional(rollbackFor = Exception.class)
    public void save(ResumeMetadata metadata) {
        if (metadata == null || metadata.getResumeId() == null) {
            throw new IllegalArgumentException("简历元数据或ID不能为空");
        }
        
        // 转换为数据库实体
        ResumeMetadataEntity entity = convertToEntity(metadata);
        
        // 检查是否已存在
        ResumeMetadataEntity existing = metadataMapper.selectById(metadata.getResumeId());
        if (existing != null) {
            // 更新
            entity.setUpdateTime(LocalDateTime.now());
            metadataMapper.updateById(entity);
            log.info("更新简历元数据，ID: {}, 文件名: {}", metadata.getResumeId(), metadata.getFileName());
        } else {
            // 新增
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            metadataMapper.insert(entity);
            log.info("保存简历元数据，ID: {}, 文件名: {}", metadata.getResumeId(), metadata.getFileName());
        }
    }
    
    /**
     * 根据ID查询简历元数据（返回Resp）
     */
    public ResumeMetadataResp getById(String resumeId) {
        if (resumeId == null || resumeId.trim().isEmpty()) {
            return null;
        }
        
        ResumeMetadataEntity entity = metadataMapper.selectById(resumeId);
        if (entity == null) {
            return null;
        }
        
        log.debug("查询简历元数据，ID: {}", resumeId);
        
        // 转换为Resp返回
        return convertToResp(entity);
    }
    
    /**
     * 删除简历元数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String resumeId) {
        if (resumeId != null && !resumeId.trim().isEmpty()) {
            int deleted = metadataMapper.deleteById(resumeId);
            if (deleted > 0) {
                log.info("删除简历元数据，ID: {}", resumeId);
            } else {
                log.warn("删除简历元数据失败，ID不存在: {}", resumeId);
            }
        }
    }
    
    /**
     * 检查简历是否存在
     */
    public boolean exists(String resumeId) {
        if (resumeId == null || resumeId.trim().isEmpty()) {
            return false;
        }
        return metadataMapper.selectById(resumeId) != null;
    }
    
    /**
     * 查询所有简历元数据
     * 用于获取所有向量ID以便删除向量数据库中的数据
     */
    public List<ResumeMetadataResp> getAll() {
        List<ResumeMetadataEntity> entities = metadataMapper.selectList(new LambdaQueryWrapper<>());
        return entities.stream()
            .map(this::convertToResp)
            .collect(Collectors.toList());
    }
    
    /**
     * 删除所有简历元数据
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteAll() {
        int deleted = metadataMapper.delete(new LambdaQueryWrapper<>());
        log.info("删除所有简历元数据，数量: {}", deleted);
        return deleted;
    }
    
    /**
     * 将ResumeMetadata转换为数据库实体
     */
    private ResumeMetadataEntity convertToEntity(ResumeMetadata metadata) {
        // 序列化vectorIds为JSON字符串
        String vectorIdsJson = null;
        if (metadata.getVectorIds() != null && !metadata.getVectorIds().isEmpty()) {
            vectorIdsJson = JSONUtil.toJsonStr(metadata.getVectorIds());
        }
        
        return ResumeMetadataEntity.builder()
            .resumeId(metadata.getResumeId())
            .fileName(metadata.getFileName())
            .name(metadata.getName())
            .position(metadata.getPosition())
            .version(metadata.getVersion())
            .contactInfo(metadata.getContactInfo())
            .fileSize(metadata.getFileSize())
            .parseTime(metadata.getParseTime())
            .chunkCount(metadata.getChunkCount())
            .vectorIdsJson(vectorIdsJson)
            .build();
    }
    
    /**
     * 将数据库实体转换为ResumeMetadataResp
     */
    private ResumeMetadataResp convertToResp(ResumeMetadataEntity entity) {
        // 反序列化vectorIds JSON字符串为List
        List<String> vectorIds = null;
        if (entity.getVectorIdsJson() != null && !entity.getVectorIdsJson().trim().isEmpty()) {
            try {
                vectorIds = JSONUtil.toList(entity.getVectorIdsJson(), String.class);
            } catch (Exception e) {
                log.warn("解析vectorIds JSON失败，ID: {}, JSON: {}", entity.getResumeId(), entity.getVectorIdsJson(), e);
            }
        }
        
        return ResumeMetadataResp.builder()
            .resumeId(entity.getResumeId())
            .fileName(entity.getFileName())
            .name(entity.getName())
            .position(entity.getPosition())
            .version(entity.getVersion())
            .contactInfo(entity.getContactInfo())
            .fileSize(entity.getFileSize())
            .parseTime(entity.getParseTime())
            .chunkCount(entity.getChunkCount())
            .vectorIds(vectorIds)
            .build();
    }
}
