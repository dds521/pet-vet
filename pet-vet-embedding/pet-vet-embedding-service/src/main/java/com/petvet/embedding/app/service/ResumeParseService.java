package com.petvet.embedding.app.service;

import com.petvet.embedding.api.dto.ResumeFileInfo;
import com.petvet.embedding.api.dto.ResumeMetadata;
import com.petvet.embedding.api.resp.ResumeParseResp;
import com.petvet.embedding.app.domain.TextChunk;
import com.petvet.embedding.app.util.PdfParser;
import com.petvet.embedding.app.util.ResumeFileNameParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 简历解析服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeParseService {
    
    private final PdfParser pdfParser;
    private final ResumeFileNameParser fileNameParser;
    private final ChunkStrategy chunkStrategy;
    private final EmbeddingService embeddingService;
    private final VectorDatabaseService vectorDatabaseService;
    private final ResumeMetadataService metadataService;
    
    @Value("${resume.chunk.max-size:500}")
    private int defaultMaxChunkSize;
    
    @Value("${resume.chunk.overlap-size:100}")
    private int defaultOverlapSize;
    
    /**
     * 解析PDF简历文件
     */
    public ResumeParseResp parseResume(MultipartFile file) throws IOException {
        log.info("开始解析简历文件: {}", file.getOriginalFilename());
        
        // 1. 解析文件名
        ResumeFileInfo fileInfo = fileNameParser.parse(file.getOriginalFilename());
        
        // 2. 提取PDF文本
        String text = pdfParser.extractText(file.getInputStream());
        
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("PDF文件内容为空，无法解析");
        }
        
        // 3. 生成简历ID
        String resumeId = generateResumeId(fileInfo);
        
        // 4. 智能Chunk切分
        List<TextChunk> chunks = chunkStrategy.chunk(text, defaultMaxChunkSize, defaultOverlapSize);
        
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("文本切分后没有生成任何chunk");
        }
        
        // 5. 批量向量化并存储
        List<String> vectorIds = storeChunksToVectorDB(resumeId, chunks);

        log.info("简历解析完成，ID: {}, Chunk数量: {}", resumeId, chunks.size());
        
        // 6. 保存简历元数据（内部使用ResumeMetadata）
        ResumeMetadata metadata = buildMetadata(resumeId, fileInfo, file, chunks, vectorIds);
        metadataService.save(metadata);
        
        // 7. 转换为Resp返回
        return ResumeParseResp.builder()
            .resumeId(resumeId)
            .chunkCount(chunks.size())
            .vectorIds(vectorIds)
            .build();
    }
    
    /**
     * 批量存储chunks到向量数据库
     */
    private List<String> storeChunksToVectorDB(String resumeId, List<TextChunk> chunks) {
        log.info("开始批量存储chunks到向量数据库，数量: {}", chunks.size());
        
        List<String> texts = chunks.stream()
            .map(chunk -> {
                // 构建包含上下文的完整文本
                return buildChunkTextWithContext(chunk);
            })
            .collect(Collectors.toList());
        
        // 批量向量化并存储
        List<String> vectorIds = vectorDatabaseService.addBatch(texts);
        
        // 更新chunk的ID
        for (int i = 0; i < chunks.size() && i < vectorIds.size(); i++) {
            chunks.get(i).setChunkId(vectorIds.get(i));
            chunks.get(i).setResumeId(resumeId);
        }
        
        log.info("Chunks存储完成，向量ID数量: {}", vectorIds.size());
        
        return vectorIds;
    }
    
    /**
     * 构建包含上下文的chunk文本
     * 确保语义完整性
     */
    private String buildChunkTextWithContext(TextChunk chunk) {
        StringBuilder sb = new StringBuilder();
        
        // 添加前文上下文
        Map<String, Object> metadata = chunk.getMetadata();
        if (metadata != null && metadata.containsKey("prevContext")) {
            sb.append(metadata.get("prevContext")).append("\n");
        }
        
        // 添加当前chunk内容
        sb.append(chunk.getText());
        
        // 添加后文上下文
        if (metadata != null && metadata.containsKey("nextContext")) {
            sb.append("\n").append(metadata.get("nextContext"));
        }
        
        return sb.toString();
    }
    
    /**
     * 生成简历ID
     */
    private String generateResumeId(ResumeFileInfo fileInfo) {
        // 使用姓名-职位-时间戳生成唯一ID
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
            MultipartFile file, List<TextChunk> chunks, 
            List<String> vectorIds) {
        
        ResumeMetadata metadata = ResumeMetadata.builder()
            .resumeId(resumeId)
            .fileName(file.getOriginalFilename())
            .name(fileInfo.getName())
            .position(fileInfo.getPosition())
            .version(fileInfo.getVersion())
            .fileSize(file.getSize())
            .parseTime(LocalDateTime.now())
            .chunkCount(chunks.size())
            .vectorIds(vectorIds)
            .build();
        
        return metadata;
    }
}
