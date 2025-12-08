package com.petvet.embedding.api.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 简历元数据响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeMetadataResp implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 简历ID（主键）
     */
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
    private String version;
    
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
     * 向量数据库中的向量ID列表
     */
    private List<String> vectorIds;
}
