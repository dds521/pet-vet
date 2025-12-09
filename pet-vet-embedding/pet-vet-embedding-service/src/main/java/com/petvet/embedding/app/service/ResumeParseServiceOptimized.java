package com.petvet.embedding.app.service;

import com.petvet.embedding.api.dto.ResumeFileInfo;
import com.petvet.embedding.api.dto.ResumeMetadata;
import com.petvet.embedding.api.resp.ResumeParseResp;
import com.petvet.embedding.app.config.ResumeChunkConfig;
import com.petvet.embedding.app.domain.TextChunk;
import com.petvet.embedding.app.service.TextChunkService;
import com.petvet.embedding.app.util.PdfBox3DocumentParser;
import com.petvet.embedding.app.util.ResumeFileNameParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final TextChunkService textChunkService;
    private final ResumeChunkConfig chunkConfig;
    private final PdfBox3DocumentParser pdfDocumentParser;
    
    /**
     * 解析PDF简历文件（优化版，支持分批处理）
     */
    @Transactional(rollbackFor = Exception.class)
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
        
        String rawText = document.text();
        log.info("PDF文档解析完成（使用 LangChain4j），文本长度: {}，文档内容: {}", rawText.length());
        
        // 4. 预处理文本：识别简历结构并增强
        String enhancedText = preprocessResumeText(rawText);
        log.debug("文本预处理完成，增强后长度: {}，文档内容: {}", enhancedText.length(), enhancedText);
        
        // 5. 使用 LangChain4j 的递归分割器进行智能切分
        // DocumentSplitters.recursive() 会先尝试按段落分割，如果段落太长则递归分割
        // 注意：对于简历，建议使用更大的chunk size以保持语义完整性
        int effectiveMaxSize = Math.max(chunkConfig.getMaxSize(), 800); // 至少800字符
        int effectiveOverlapSize = Math.max(chunkConfig.getOverlapSize(), (int)(effectiveMaxSize * 0.2)); // overlap至少是maxSize的20%
        
        log.info("使用切分参数 - maxSize: {}, overlapSize: {}", effectiveMaxSize, effectiveOverlapSize);
        
        // 使用增强后的文本创建新的Document
        Document enhancedDocument = Document.from(enhancedText);
        var splitter = DocumentSplitters.recursive(
            effectiveMaxSize,
            effectiveOverlapSize
        );
        
        List<TextSegment> segments = splitter.split(enhancedDocument);
        
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("文本切分后没有生成任何segment");
        }
        
        log.info("文档分割完成，Segment数量: {}, 平均长度: {}", 
            segments.size(), 
            segments.stream().mapToInt(s -> s.text().length()).average().orElse(0));
        
        // 记录切分统计信息
        int minLength = segments.stream().mapToInt(s -> s.text().length()).min().orElse(0);
        int maxLength = segments.stream().mapToInt(s -> s.text().length()).max().orElse(0);
        log.debug("Segment长度统计 - 最小: {}, 最大: {}", minLength, maxLength);
        
        // 6. 转换为 TextChunk 并分批处理，避免内存溢出
        List<TextChunk> allChunks = convertSegmentsToChunks(segments, resumeId);
        List<String> allVectorIds = new ArrayList<>();
        
        // 分批处理 chunks，避免一次性加载所有数据到内存
        int batchSize = chunkConfig.getBatchSize();
        for (int i = 0; i < allChunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allChunks.size());
            // 创建新的列表副本，避免 subList 视图被 clear() 影响
            List<TextChunk> batch = new ArrayList<>(allChunks.subList(i, end));
            
            log.info("处理第 {}/{} 批，数量: {}", (i / batchSize + 1), (allChunks.size() + batchSize - 1) / batchSize, batch.size());
            
            // 批量向量化并存储（此时会设置 chunkId）
            List<String> batchVectorIds = storeChunksToVectorDB(resumeId, batch);
            allVectorIds.addAll(batchVectorIds);
            
            // 更新 allChunks 中对应位置的 chunks（保持 chunkId 同步）
            for (int j = 0; j < batch.size() && (i + j) < allChunks.size(); j++) {
                allChunks.set(i + j, batch.get(j));
            }
        }
        
        log.info("简历解析完成，ID: {}, Chunk数量: {}, Vector数量: {}", resumeId, allChunks.size(), allVectorIds.size());
        
        // 7. 先保存简历元数据（必须在保存 text_chunk 之前，因为外键约束）
        ResumeMetadata metadata = buildMetadata(resumeId, fileInfo, file, allChunks.size(), allVectorIds);
        log.debug("准备保存简历元数据，resumeId: {}", resumeId);
        metadataService.save(metadata);
        log.debug("简历元数据保存成功，resumeId: {}", resumeId);
        
        // 8. 保存文本Chunks到数据库（必须在保存 resume_metadata 之后，因为外键约束）
        log.info("准备保存所有Chunks到数据库，总数: {}", allChunks.size());
        // 验证所有chunks都有chunkId
        long chunksWithoutId = allChunks.stream()
            .filter(chunk -> chunk.getChunkId() == null || chunk.getChunkId().trim().isEmpty())
            .count();
        if (chunksWithoutId > 0) {
            log.warn("有 {} 个Chunks缺少chunkId，这些Chunks将不会被保存", chunksWithoutId);
        }
        textChunkService.saveBatch(allChunks);
        log.info("所有Chunks保存完成");
        
        // 9. 转换为Resp返回
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
     * 预处理简历文本，增强结构识别
     * - 识别简历段落（工作经历、教育背景等）
     * - 添加段落标记以便后续切分
     * - 规范化格式
     */
    private String preprocessResumeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        // 识别常见的简历段落标题并添加标记
        // 这样可以帮助切分器更好地识别段落边界
        String[] sectionKeywords = {
            "工作经历", "工作经验", "工作履历",
            "教育背景", "教育经历", "学历",
            "项目经验", "项目经历",
            "技能", "专业技能", "技术栈",
            "自我评价", "个人简介",
            "联系方式", "联系信息"
        };
        
        String processed = text;
        for (String keyword : sectionKeywords) {
            // 在段落标题前添加特殊标记（保留原文本，只是增强识别）
            // 使用正则表达式匹配，确保是独立的段落标题
            String pattern = "(?m)^\\s*" + keyword + "[：:：]?\\s*$";
            processed = processed.replaceAll(pattern, "\n\n【" + keyword + "】\n");
        }
        
        // 规范化段落分隔（确保段落之间有明确的分隔）
        processed = processed.replaceAll("\n{3,}", "\n\n");
        
        return processed.trim();
    }
    
    /**
     * 识别文本所属的简历字段类型（增强版）
     */
    private String identifyFieldType(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "OTHER";
        }
        
        String lowerText = text.toLowerCase();
        
        // 优先匹配明确的段落标记
        if (lowerText.contains("【工作经历】") || lowerText.contains("【工作经验】") || 
            lowerText.contains("【工作履历】") || lowerText.startsWith("工作经历") ||
            lowerText.startsWith("工作经验") || lowerText.startsWith("工作履历")) {
            return "WORK_EXPERIENCE";
        }
        
        if (lowerText.contains("【项目经验】") || lowerText.contains("【项目经历】") ||
            lowerText.startsWith("项目经验") || lowerText.startsWith("项目经历")) {
            return "WORK_EXPERIENCE"; // 项目经验也归类为工作经历
        }
        
        if (lowerText.contains("【教育背景】") || lowerText.contains("【教育经历】") ||
            lowerText.contains("【学历】") || lowerText.startsWith("教育背景") ||
            lowerText.startsWith("教育经历") || lowerText.startsWith("学历")) {
            return "EDUCATION";
        }
        
        if (lowerText.contains("【技能】") || lowerText.contains("【专业技能】") ||
            lowerText.contains("【技术栈】") || lowerText.startsWith("技能") ||
            lowerText.startsWith("专业技能") || lowerText.startsWith("技术栈")) {
            return "SKILLS";
        }
        
        if (lowerText.contains("【联系方式】") || lowerText.contains("【联系信息】") ||
            lowerText.startsWith("联系方式") || lowerText.startsWith("联系信息")) {
            return "CONTACT";
        }
        
        // 如果没有明确的段落标记，使用关键词匹配
        if (lowerText.contains("工作经历") || lowerText.contains("工作经验") || 
            lowerText.contains("工作职责") || lowerText.contains("项目经验") ||
            lowerText.contains("项目经历") || 
            (lowerText.contains("工作") && (lowerText.contains("公司") || lowerText.contains("职位")))) {
            return "WORK_EXPERIENCE";
        }
        
        if (lowerText.contains("教育背景") || lowerText.contains("教育经历") || 
            lowerText.contains("学历") || lowerText.contains("毕业院校") || 
            lowerText.contains("专业") || lowerText.contains("毕业") ||
            lowerText.contains("大学") || lowerText.contains("学院")) {
            return "EDUCATION";
        }
        
        if (lowerText.contains("技能") || lowerText.contains("技术栈") || 
            lowerText.contains("熟悉") || lowerText.contains("掌握") ||
            lowerText.contains("精通") || lowerText.contains("了解")) {
            return "SKILLS";
        }
        
        if (lowerText.contains("联系方式") || lowerText.contains("邮箱") || 
            lowerText.contains("电话") || lowerText.contains("手机") ||
            lowerText.contains("email") || lowerText.contains("phone") ||
            lowerText.matches(".*@.*\\..*") || lowerText.matches(".*1[3-9]\\d{9}.*")) {
            return "CONTACT";
        }
        
        return "OTHER";
    }
    
    /**
     * 批量存储chunks到向量数据库
     * 优化：为每个chunk添加上下文信息以增强向量表示
     */
    private List<String> storeChunksToVectorDB(String resumeId, List<TextChunk> chunks) {
        log.debug("开始批量存储chunks到向量数据库，数量: {}", chunks.size());
        
        // 增强文本：为每个chunk添加结构化前缀和上下文
        List<String> enhancedTexts = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            TextChunk chunk = chunks.get(i);
            String originalText = chunk.getText();
            String enhancedText = enhanceChunkText(originalText, chunk.getFieldType(), i, chunks.size());
            enhancedTexts.add(enhancedText);
        }
        
        // 批量向量化并存储（使用增强后的文本）
        List<String> vectorIds = vectorDatabaseService.addBatch(enhancedTexts);
        
        // 更新chunk的ID
        if (vectorIds.size() != chunks.size()) {
            log.warn("向量ID数量 ({}) 与Chunks数量 ({}) 不匹配", vectorIds.size(), chunks.size());
        }
        
        for (int i = 0; i < chunks.size() && i < vectorIds.size(); i++) {
            String vectorId = vectorIds.get(i);
            chunks.get(i).setChunkId(vectorId);
            chunks.get(i).setResumeId(resumeId);
            log.debug("设置Chunk ID: chunk[{}] -> vectorId: {}, fieldType: {}", 
                i, vectorId, chunks.get(i).getFieldType());
        }
        
        // 检查是否有chunks没有设置chunkId
        long chunksWithoutId = chunks.stream()
            .filter(chunk -> chunk.getChunkId() == null || chunk.getChunkId().trim().isEmpty())
            .count();
        if (chunksWithoutId > 0) {
            log.warn("有 {} 个Chunks没有设置chunkId", chunksWithoutId);
        }
        
        log.debug("Chunks存储完成，向量ID数量: {}, Chunks数量: {}", vectorIds.size(), chunks.size());
        
        return vectorIds;
    }
    
    /**
     * 增强chunk文本，添加结构化信息以改善向量表示
     * - 添加字段类型前缀
     * - 保留原始文本（用于搜索时返回）
     */
    private String enhanceChunkText(String originalText, String fieldType, int index, int total) {
        if (originalText == null || originalText.trim().isEmpty()) {
            return originalText;
        }
        
        // 构建增强文本
        StringBuilder enhanced = new StringBuilder();
        
        // 1. 添加字段类型前缀（帮助模型理解上下文）
        if (fieldType != null && !"OTHER".equals(fieldType)) {
            String typeLabel = getFieldTypeLabel(fieldType);
            enhanced.append("[").append(typeLabel).append("] ");
        }
        
        // 2. 添加原始文本
        enhanced.append(originalText);
        
        // 注意：我们不在向量化时添加位置信息，因为这可能会干扰语义搜索
        // 位置信息已经保存在TextChunk的sequence字段中
        
        return enhanced.toString();
    }
    
    /**
     * 获取字段类型的中文标签
     */
    private String getFieldTypeLabel(String fieldType) {
        switch (fieldType) {
            case "WORK_EXPERIENCE":
                return "工作经历";
            case "EDUCATION":
                return "教育背景";
            case "SKILLS":
                return "技能";
            case "CONTACT":
                return "联系方式";
            default:
                return "其他";
        }
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
