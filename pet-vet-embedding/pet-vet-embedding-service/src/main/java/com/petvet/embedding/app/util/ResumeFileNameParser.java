package com.petvet.embedding.app.util;

import com.petvet.embedding.api.dto.ResumeFileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 简历文件名解析工具类
 * 格式：姓名-职位-版本.pdf
 * 示例：代大胜-Java-new.pdf
 */
@Slf4j
@Component
public class ResumeFileNameParser {
    
    /**
     * 解析文件名
     */
    public ResumeFileInfo parse(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 移除扩展名
        String nameWithoutExt = fileName.replaceAll("\\.pdf$", "").replaceAll("\\.PDF$", "");
        
        // 按"-"分割
        String[] parts = nameWithoutExt.split("-");
        
        if (parts.length >= 2) {
            String name = parts[0].trim();
            String position = parts[1].trim();
            String version = parts.length > 2 ? parts[2].trim() : "default";
            
            log.info("解析文件名: {} -> 姓名: {}, 职位: {}, 版本: {}", fileName, name, position, version);
            
            return ResumeFileInfo.builder()
                .name(name)
                .position(position)
                .version(version)
                .build();
        }
        
        throw new IllegalArgumentException("文件名格式不正确，应为：姓名-职位-版本.pdf，实际: " + fileName);
    }
}
