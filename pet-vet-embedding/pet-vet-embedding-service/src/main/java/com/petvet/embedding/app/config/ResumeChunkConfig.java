package com.petvet.embedding.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 简历Chunk切分配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "resume.chunk")
public class ResumeChunkConfig {
    
    /**
     * 最大chunk大小（字符数）
     */
    private int maxSize = 500;
    
    /**
     * 重叠大小（字符数）
     */
    private int overlapSize = 100;
    
    /**
     * 是否启用句子边界识别
     */
    private boolean enableSentenceBoundary = true;
    
    /**
     * 是否启用段落优先切分
     */
    private boolean enableParagraphPriority = true;
    
    /**
     * 是否启用上下文增强
     */
    private boolean enableContextEnrichment = true;
    
    /**
     * 分批处理大小（每批处理的 chunk 数量）
     * 用于避免内存溢出，建议值：50-100
     */
    private int batchSize = 50;
}
