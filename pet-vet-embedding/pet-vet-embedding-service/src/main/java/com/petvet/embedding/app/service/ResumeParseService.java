package com.petvet.embedding.app.service;

import com.petvet.embedding.api.resp.ResumeParseResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 简历解析服务
 * 
 * 注意：此服务已优化为使用 ResumeParseServiceOptimized
 * 保留此类是为了向后兼容，实际处理委托给优化版本
 * 
 * 优化版本特点：
 * 1. 使用 LangChain4j 的递归分割器（DocumentSplitters.recursive）进行智能切分
 * 2. 分批处理 chunks，避免一次性加载所有数据到内存（每批50个）
 * 3. 每批处理完成后释放内存引用，有效避免内存溢出
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeParseService {
    
    private final ResumeParseServiceOptimized optimizedService;
    
    /**
     * 解析PDF简历文件
     * 委托给优化版本处理，支持分批处理以避免内存溢出
     */
    public ResumeParseResp parseResume(MultipartFile file) throws IOException {
        return optimizedService.parseResume(file);
    }
}
