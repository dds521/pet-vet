package com.petvet.embedding.app.service;

import com.petvet.embedding.api.dto.ResumeFileInfo;
import com.petvet.embedding.api.dto.ResumeMetadata;
import com.petvet.embedding.api.resp.ResumeParseResp;
import com.petvet.embedding.app.config.ResumeChunkConfig;
import com.petvet.embedding.app.domain.TextChunk;
import com.petvet.embedding.app.util.PdfBox3DocumentParser;
import com.petvet.embedding.app.util.ResumeFileNameParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简历解析服务（优化版）
 * 使用 LangChain4j 的文档分割器，支持分批处理以避免内存溢出
 * 
 * 优化点：
 * 1. 使用 LangChain4j 的递归分割器（DocumentSplitters.recursive）进行智能切分
 * 2. 分批处理 chunks，避免一次性加载所有数据到内存
 * 3. 每批处理完成后释放内存引用
 */
@Service("resumeParseServiceOptimized")
@Slf4j
@RequiredArgsConstructor
public class ResumeParseServiceOptimized {
    
    private final ResumeFileNameParser fileNameParser;
    private final VectorDatabaseService vectorDatabaseService;
    private final ResumeMetadataService metadataService;
    private final ResumeChunkConfig chunkConfig;
    private final PdfBox3DocumentParser pdfDocumentParser;
    
    /**
     * 解析PDF简历文件（优化版，支持分批处理）
     */
    public ResumeParseResp parseResume(MultipartFile file) throws IOException {
        log.info("开始解析简历文件（优化版）: {}", file.getOriginalFilename());
        
        // 1. 解析文件名
        ResumeFileInfo fileInfo = fileNameParser.parse(file.getOriginalFilename());
        
        // 2. 生成简历ID
        String resumeId = generateResumeId(fileInfo);
        
        // 3. 使用 LangChain4j 的 PDF 解析器解析文档
        Document document;
        try (java.io.InputStream inputStream = file.getInputStream()) {
            document = pdfDocumentParser.parse(inputStream);
        }
        
        if (document == null || document.text() == null || document.text().trim().isEmpty()) {
            throw new IllegalArgumentException("PDF文件内容为空，无法解析");
        }
        
        log.info("PDF文档解析完成（使用 LangChain4j），文本长度: {}", document.text().length());
        
        // 5. 使用 LangChain4j 的递归分割器进行智能切分
        // DocumentSplitters.recursive() 会先尝试按段落分割，如果段落太长则递归分割
        var splitter = DocumentSplitters.recursive(
            chunkConfig.getMaxSize(),
            chunkConfig.getOverlapSize()
        );
        
        List<TextSegment> segments = splitter.split(document);
        
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("文本切分后没有生成任何segment");
        }
        
        log.info("文档分割完成，Segment数量: {}", segments.size());
        
        // 6. 转换为 TextChunk 并分批处理，避免内存溢出
        List<TextChunk> allChunks = convertSegmentsToChunks(segments, resumeId);
        List<String> allVectorIds = new ArrayList<>();
        
        // 分批处理 chunks，避免一次性加载所有数据到内存
        int batchSize = chunkConfig.getBatchSize();
        for (int i = 0; i < allChunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allChunks.size());
            List<TextChunk> batch = allChunks.subList(i, end);
            
            log.info("处理第 {}/{} 批，数量: {}", (i / batchSize + 1), (allChunks.size() + batchSize - 1) / batchSize, batch.size());
            
            // 批量向量化并存储
            List<String> batchVectorIds = storeChunksToVectorDB(resumeId, batch);
            allVectorIds.addAll(batchVectorIds);
            
            // 释放当前批次的内存引用
            batch.clear();
        }
        
        log.info("简历解析完成，ID: {}, Chunk数量: {}, Vector数量: {}", resumeId, allChunks.size(), allVectorIds.size());
        
        // 7. 保存简历元数据
        ResumeMetadata metadata = buildMetadata(resumeId, fileInfo, file, allChunks.size(), allVectorIds);
        metadataService.save(metadata);
        
        // 8. 转换为Resp返回
        return ResumeParseResp.builder()
            .resumeId(resumeId)
            .chunkCount(allChunks.size())
            .vectorIds(allVectorIds)
            .build();
    }
    
    /**
     * 将 LangChain4j 的 TextSegment 转换为 TextChunk
     */
    private List<TextChunk> convertSegmentsToChunks(List<TextSegment> segments, String resumeId) {
        List<TextChunk> chunks = new ArrayList<>();
        
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            TextChunk chunk = new TextChunk();
            
            chunk.setText(segment.text());
            chunk.setSequence(i);
            chunk.setResumeId(resumeId);
            
            // 识别字段类型
            String fieldType = identifyFieldType(segment.text());
            chunk.setFieldType(fieldType);
            
            // 设置元数据（如果有）
            // 注意：LangChain4j 的 TextSegment.metadata() 返回的 Metadata 对象
            // 如果需要保存元数据，可以在这里扩展，目前先跳过
            // 因为我们的 TextChunk 已经有自己的字段类型识别逻辑
            
            chunks.add(chunk);
        }
        
        return chunks;
    }
    
    /**
     * 识别文本所属的简历字段类型
     */
    private String identifyFieldType(String text) {
        String lowerText = text.toLowerCase();
        
        if (lowerText.contains("工作经历") || lowerText.contains("工作经验") || 
            lowerText.contains("工作职责") || lowerText.contains("项目经验") ||
            lowerText.contains("工作") || lowerText.contains("项目")) {
            return "WORK_EXPERIENCE";
        }
        if (lowerText.contains("教育背景") || lowerText.contains("学历") || 
            lowerText.contains("毕业院校") || lowerText.contains("专业") ||
            lowerText.contains("教育") || lowerText.contains("毕业")) {
            return "EDUCATION";
        }
        if (lowerText.contains("技能") || lowerText.contains("技术栈") || 
            lowerText.contains("熟悉") || lowerText.contains("掌握") ||
            lowerText.contains("精通")) {
            return "SKILLS";
        }
        if (lowerText.contains("联系方式") || lowerText.contains("邮箱") || 
            lowerText.contains("电话") || lowerText.contains("手机") ||
            lowerText.contains("email") || lowerText.contains("phone")) {
            return "CONTACT";
        }
        return "OTHER";
    }
    
    /**
     * 批量存储chunks到向量数据库
     */
    private List<String> storeChunksToVectorDB(String resumeId, List<TextChunk> chunks) {
        log.debug("开始批量存储chunks到向量数据库，数量: {}", chunks.size());
        
        // 提取文本内容
        List<String> texts = chunks.stream()
            .map(TextChunk::getText)
            .collect(Collectors.toList());
        
        // 批量向量化并存储
        List<String> vectorIds = vectorDatabaseService.addBatch(texts);
        
        // 更新chunk的ID
        for (int i = 0; i < chunks.size() && i < vectorIds.size(); i++) {
            chunks.get(i).setChunkId(vectorIds.get(i));
            chunks.get(i).setResumeId(resumeId);
        }
        
        log.debug("Chunks存储完成，向量ID数量: {}", vectorIds.size());
        
        return vectorIds;
    }
    
    /**
     * 生成简历ID
     */
    private String generateResumeId(ResumeFileInfo fileInfo) {
        return String.format("resume_%s_%s_%d", 
            fileInfo.getName().replaceAll("\\s+", "_"), 
            fileInfo.getPosition().replaceAll("\\s+", "_"),
            System.currentTimeMillis());
    }
    
    /**
     * 构建简历元数据
     */
    private ResumeMetadata buildMetadata(
            String resumeId, ResumeFileInfo fileInfo, 
            MultipartFile file, int chunkCount, 
            List<String> vectorIds) {
        
        return ResumeMetadata.builder()
            .resumeId(resumeId)
            .fileName(file.getOriginalFilename())
            .name(fileInfo.getName())
            .position(fileInfo.getPosition())
            .version(fileInfo.getVersion())
            .fileSize(file.getSize())
            .parseTime(LocalDateTime.now())
            .chunkCount(chunkCount)
            .vectorIds(vectorIds)
            .build();
    }
}
