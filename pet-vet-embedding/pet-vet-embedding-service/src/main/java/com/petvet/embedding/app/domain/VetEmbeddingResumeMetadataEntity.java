package com.petvet.embedding.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petvet.common.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 简历元数据数据库实体类
 * 
 * 用于数据库持久化存储简历元数据信息
 * 
 * @author daidasheng
 * @date 2024-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vet_embedding_resume_metadata")
public class VetEmbeddingResumeMetadataEntity extends BaseEntity {
    
    /**
     * 简历ID（主键）
     */
    @TableId(type = IdType.INPUT)
    private String resumeId;
    
    /**
     * 文件名（原始文件名）
     */
    private String fileName;
    
    /**
     * 解析出的姓名
     */
    private String name;
    
    /**
     * 解析出的职位
     */
    private String position;
    
    /**
     * 版本标识（从文件名提取，如：new）
     */
    private String versionTag;
    
    /**
     * 联系方式（邮箱、电话等）
     */
    private String contactInfo;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 解析时间
     */
    private LocalDateTime parseTime;
    
    /**
     * Chunk数量
     */
    private Integer chunkCount;
    
    /**
     * 向量数据库中的向量ID列表（JSON格式存储）
     * 注意：在数据库中以JSON字符串形式存储，查询时自动反序列化为List
     */
    private String vectorIdsJson;
}
