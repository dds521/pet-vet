package com.petvet.embedding.app.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

/**
 * PDF解析工具类
 */
@Slf4j
@Component
public class PdfParser {
    
    /**
     * 提取PDF文本内容
     */
    public String extractText(InputStream pdfInputStream) throws IOException {
        // 读取所有字节（兼容Java 8+）
        byte[] pdfBytes = readAllBytes(pdfInputStream);
        
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            
            // 设置提取策略
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            
            // 提取文本
            String text = stripper.getText(document);
            
            // 文本清理
            text = cleanText(text);
            
            log.info("PDF文本提取完成，页数: {}, 字符数: {}", 
                document.getNumberOfPages(), text.length());
            
            return text;
        }
    }
    
    /**
     * 读取InputStream的所有字节（兼容Java 8+）
     */
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
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
     * - 移除特殊字符
     */
    private String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // 规范化换行符
        text = text.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        
        // 移除多余空白行（保留单个空行）
        text = text.replaceAll("\n{3,}", "\n\n");
        
        // 移除行首行尾空白
        text = text.lines()
            .map(String::trim)
            .collect(Collectors.joining("\n"));
        
        return text.trim();
    }
}
