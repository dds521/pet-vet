package com.petvet.embedding.api.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 简历搜索请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeSearchReq implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 查询文本
     */
    private String query;
    
    /**
     * 最大返回结果数
     */
    private Integer maxResults;
    
    /**
     * 最小相似度分数
     */
    private Double minScore;
}
