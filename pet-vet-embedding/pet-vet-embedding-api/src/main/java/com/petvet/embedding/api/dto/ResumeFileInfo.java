package com.petvet.embedding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 简历文件信息
 * 从文件名解析出的信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeFileInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 职位
     */
    private String position;
    
    /**
     * 版本标识
     */
    private String version;
}
