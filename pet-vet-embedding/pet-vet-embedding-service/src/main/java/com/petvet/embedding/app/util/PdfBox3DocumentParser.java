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
                
                log.info("PDF文档解析完成（PDFBox 3.x），页数: {}, 字符数: {}", 
                    document.getNumberOfPages(), text.length());
                
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
     * 文本清理
     * - 移除多余空白字符
     * - 规范化换行符
     */
    private String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // 规范化换行符
        text = text.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        
        // 移除多余的空行（保留单个换行符）
        text = text.replaceAll("\n{3,}", "\n\n");
        
        // 移除行首行尾空白
        text = text.trim();
        
        return text;
    }
}
