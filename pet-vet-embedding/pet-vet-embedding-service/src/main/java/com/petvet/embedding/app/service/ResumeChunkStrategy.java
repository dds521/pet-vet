package com.petvet.embedding.app.service;

import com.petvet.embedding.app.config.ResumeChunkConfig;
import com.petvet.embedding.app.domain.TextChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 简历专用Chunk切分策略
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeChunkStrategy implements ChunkStrategy {
    
    private final ResumeChunkConfig config;
    
    /**
     * 切分文本为多个chunk
     */
    @Override
    public List<TextChunk> chunk(String text, int maxChunkSize, int overlapSize) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // 使用默认值如果未指定
        if (maxChunkSize <= 0) {
            maxChunkSize = config.getMaxSize();
        }
        if (overlapSize <= 0) {
            overlapSize = config.getOverlapSize();
        }
        
        List<TextChunk> chunks = new ArrayList<>();
        
        // 1. 首先尝试按段落切分
        List<String> paragraphs = config.isEnableParagraphPriority() 
            ? splitByParagraph(text) 
            : Collections.singletonList(text);
        
        // 2. 对每个段落进行处理
        for (int i = 0; i < paragraphs.size(); i++) {
            String paragraph = paragraphs.get(i);
            
            if (paragraph.length() <= maxChunkSize) {
                // 段落长度合适，直接作为一个chunk
                chunks.add(createChunk(paragraph, i, text));
            } else {
                // 段落过长，使用滑动窗口切分
                List<TextChunk> subChunks = splitWithSlidingWindow(paragraph, maxChunkSize, overlapSize, i, text);
                chunks.addAll(subChunks);
            }
        }
        
        // 3. 添加上下文信息到每个chunk
        if (config.isEnableContextEnrichment()) {
            enrichWithContext(chunks);
        }
        
        log.info("文本切分完成，原始长度: {}, Chunk数量: {}", text.length(), chunks.size());
        
        return chunks;
    }
    
    /**
     * 按段落切分文本
     * 识别空行、缩进等段落边界
     */
    private List<String> splitByParagraph(String text) {
        List<String> paragraphs = new ArrayList<>();
        
        // 按双换行符切分（段落边界）
        String[] parts = text.split("\n\n+");
        
        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                paragraphs.add(part);
            }
        }
        
        // 如果没有找到段落边界，按单换行符切分
        if (paragraphs.isEmpty()) {
            paragraphs = Arrays.stream(text.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        }
        
        return paragraphs;
    }
    
    /**
     * 使用滑动窗口切分长文本
     * 确保重叠部分保留上下文
     */
    private List<TextChunk> splitWithSlidingWindow(String text, int maxChunkSize, int overlapSize, int paragraphIndex, String fullText) {
        
        List<TextChunk> chunks = new ArrayList<>();
        int start = 0;
        int chunkIndex = 0;
        
        while (start < text.length()) {
            int end = Math.min(start + maxChunkSize, text.length());
            
            // 尝试在句子边界处结束（避免截断句子）
            if (config.isEnableSentenceBoundary() && end < text.length()) {
                end = findSentenceBoundary(text, end);
            }
            
            String chunkText = text.substring(start, end);
            TextChunk chunk = createChunk(chunkText, paragraphIndex * 1000 + chunkIndex, fullText);
            chunks.add(chunk);
            
            // 滑动窗口：下一个chunk的起始位置 = 当前结束位置 - 重叠大小
            start = end - overlapSize;
            if (start < 0) start = 0;
            if (start >= text.length()) break;
            
            chunkIndex++;
        }
        
        return chunks;
    }
    
    /**
     * 查找句子边界（句号、问号、感叹号等）
     * 避免在句子中间截断
     */
    private int findSentenceBoundary(String text, int position) {
        // 向前查找最近的句子结束符
        for (int i = position; i > position - 50 && i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '。' || c == '.' || c == '！' || c == '!' || 
                c == '？' || c == '?') {
                return i + 1;
            }
        }
        
        // 如果没找到，尝试在标点符号处截断
        for (int i = position; i > position - 30 && i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '，' || c == ',' || c == '；' || c == ';') {
                return i + 1;
            }
        }
        
        // 如果都没找到，在空格处截断
        for (int i = position; i > position - 20 && i >= 0; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i + 1;
            }
        }
        
        return position;
    }
    
    /**
     * 为chunk添加上下文信息
     * 包括前一个chunk和后一个chunk的部分内容
     */
    private void enrichWithContext(List<TextChunk> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            TextChunk chunk = chunks.get(i);
            Map<String, Object> metadata = chunk.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
                chunk.setMetadata(metadata);
            }
            
            // 添加上下文信息
            if (i > 0) {
                TextChunk prevChunk = chunks.get(i - 1);
                String prevContext = prevChunk.getText();
                // 只保留最后50个字符作为前文上下文
                if (prevContext.length() > 50) {
                    prevContext = "..." + prevContext.substring(prevContext.length() - 50);
                }
                metadata.put("prevContext", prevContext);
            }
            
            if (i < chunks.size() - 1) {
                TextChunk nextChunk = chunks.get(i + 1);
                String nextContext = nextChunk.getText();
                // 只保留前50个字符作为后文上下文
                if (nextContext.length() > 50) {
                    nextContext = nextContext.substring(0, 50) + "...";
                }
                metadata.put("nextContext", nextContext);
            }
        }
    }
    
    /**
     * 创建TextChunk对象
     */
    private TextChunk createChunk(String text, int sequence, String fullText) {
        TextChunk chunk = new TextChunk();
        chunk.setText(text);
        chunk.setSequence(sequence);
        
        // 识别字段类型
        String fieldType = identifyFieldType(text);
        chunk.setFieldType(fieldType);
        
        // 计算在原文中的位置
        int startPos = fullText.indexOf(text);
        if (startPos >= 0) {
            chunk.setStartPosition(startPos);
            chunk.setEndPosition(startPos + text.length());
        }
        
        return chunk;
    }
    
    /**
     * 识别文本所属的简历字段类型
     */
    private String identifyFieldType(String text) {
        String lowerText = text.toLowerCase();
        
        // 基于关键词识别字段类型
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
}
