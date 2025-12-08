package com.petvet.embedding.app.service;

import com.petvet.embedding.api.dto.ResumeMetadata;
import com.petvet.embedding.api.resp.ResumeMetadataResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简历元数据服务
 * 注意：当前使用内存存储，生产环境应使用数据库（如MySQL、MongoDB等）
 */
@Service
@Slf4j
public class ResumeMetadataService {
    
    // 使用内存Map存储，生产环境应替换为数据库
    private final Map<String, ResumeMetadata> metadataStore = new ConcurrentHashMap<>();
    
    /**
     * 保存简历元数据（内部使用ResumeMetadata）
     */
    public void save(ResumeMetadata metadata) {
        if (metadata == null || metadata.getResumeId() == null) {
            throw new IllegalArgumentException("简历元数据或ID不能为空");
        }
        
        metadataStore.put(metadata.getResumeId(), metadata);
        log.info("保存简历元数据，ID: {}, 文件名: {}", metadata.getResumeId(), metadata.getFileName());
    }
    
    /**
     * 根据ID查询简历元数据（返回Resp）
     */
    public ResumeMetadataResp getById(String resumeId) {
        if (resumeId == null || resumeId.trim().isEmpty()) {
            return null;
        }
        
        ResumeMetadata metadata = metadataStore.get(resumeId);
        if (metadata == null) {
            return null;
        }
        
        log.debug("查询简历元数据，ID: {}", resumeId);
        
        // 转换为Resp返回
        return ResumeMetadataResp.builder()
            .resumeId(metadata.getResumeId())
            .fileName(metadata.getFileName())
            .name(metadata.getName())
            .position(metadata.getPosition())
            .version(metadata.getVersion())
            .contactInfo(metadata.getContactInfo())
            .fileSize(metadata.getFileSize())
            .parseTime(metadata.getParseTime())
            .chunkCount(metadata.getChunkCount())
            .vectorIds(metadata.getVectorIds())
            .build();
    }
    
    /**
     * 删除简历元数据
     */
    public void delete(String resumeId) {
        if (resumeId != null && !resumeId.trim().isEmpty()) {
            metadataStore.remove(resumeId);
            log.info("删除简历元数据，ID: {}", resumeId);
        }
    }
    
    /**
     * 检查简历是否存在
     */
    public boolean exists(String resumeId) {
        return resumeId != null && metadataStore.containsKey(resumeId);
    }
}
