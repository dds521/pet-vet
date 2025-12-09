package com.petvet.embedding.app.util;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * PDFBox 3.x 兼容的 PDF 文档解析器
 * 
 * 注意：langchain4j-document-parser-apache-pdfbox 使用的是 PDFBox 2.x API，
 * 与 PDFBox 3.x 不兼容。此解析器使用 PDFBox 3.x 的 API（Loader.loadPDF）
 * 来实现 DocumentParser 接口。
 */
@Slf4j
@Component
public class PdfBox3DocumentParser implements DocumentParser {
    
    @Override
    public Document parse(InputStream inputStream) {
        try {
            // 读取所有字节（兼容 Java 8+）
            byte[] pdfBytes = readAllBytes(inputStream);
            
            // 使用 PDFBox 3.x 的 Loader.loadPDF 方法
            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                
                // 设置提取策略
                stripper.setStartPage(1);
                stripper.setEndPage(document.getNumberOfPages());
                
                // 提取文本
                String text = stripper.getText(document);
                
                // 文本清理
                text = cleanText(text);
                
                log.info("PDF文档解析完成（PDFBox 3.x），页数: {}, 字符数: {}", document.getNumberOfPages(), text.length());
                
                return Document.from(text);
            }
        } catch (IOException e) {
            log.error("PDF解析失败", e);
            throw new RuntimeException("PDF解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 读取InputStream的所有字节（兼容Java 8+）
     */
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
    
    /**
     * 文本清理和增强
     * - 规范化换行符
     * - 移除多余空白字符
     * - 保留段落结构
     * - 处理特殊字符
     * - 增强文本质量
     */
    private String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // 1. 规范化换行符
        text = text.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        
        // 2. 移除特殊控制字符（保留可打印字符和常见空白字符）
        text = text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
        
        // 3. 规范化空白字符（多个空格合并为一个，但保留换行）
        text = text.replaceAll("[ \\t]+", " "); // 多个空格/制表符合并为一个空格
        
        // 4. 处理段落边界（保留段落结构）
        // 将多个连续换行符（3个以上）替换为双换行符（段落分隔）
        text = text.replaceAll("\n{3,}", "\n\n");
        
        // 5. 移除行首行尾空白（但保留段落结构）
        // 先按行处理，再合并
        String[] lines = text.split("\n");
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                cleaned.append(line);
                // 如果下一行不为空，添加换行符
                if (i < lines.length - 1 && !lines[i + 1].trim().isEmpty()) {
                    cleaned.append("\n");
                }
            } else if (i < lines.length - 1 && !lines[i + 1].trim().isEmpty()) {
                // 空行后跟非空行，保留段落分隔
                cleaned.append("\n");
            }
        }
        text = cleaned.toString();
        
        // 6. 移除文档首尾空白
        text = text.trim();
        
        // 7. 最终检查：确保文本不为空
        if (text.isEmpty()) {
            log.warn("文本清理后为空，可能PDF解析有问题");
        }
        
        return text;
    }
}
