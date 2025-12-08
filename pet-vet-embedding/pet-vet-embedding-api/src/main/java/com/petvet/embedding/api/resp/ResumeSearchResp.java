package com.petvet.embedding.api.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 简历搜索响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeSearchResp implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 结果数量
     */
    private Integer count;
    
    /**
     * 搜索结果列表
     */
    private List<SearchItem> results;
    
    /**
     * 搜索结果项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchItem implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * Chunk ID
         */
        private String chunkId;
        
        /**
         * 相似度分数
         */
        private Double score;
        
        /**
         * 文本内容
         */
        private String text;
    }
}
