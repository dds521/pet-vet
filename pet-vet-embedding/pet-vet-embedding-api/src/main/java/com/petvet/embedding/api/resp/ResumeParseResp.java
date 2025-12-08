package com.petvet.embedding.api.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 简历解析响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeParseResp implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 简历ID
     */
    private String resumeId;
    
    /**
     * Chunk数量
     */
    private Integer chunkCount;
    
    /**
     * 向量ID列表
     */
    private List<String> vectorIds;
}
