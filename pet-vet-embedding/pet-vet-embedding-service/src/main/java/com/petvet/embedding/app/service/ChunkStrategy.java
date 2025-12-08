package com.petvet.embedding.app.service;

import com.petvet.embedding.app.domain.TextChunk;

import java.util.List;

/**
 * Chunk切分策略接口
 */
public interface ChunkStrategy {
    /**
     * 切分文本为多个chunk
     * @param text 原始文本
     * @param maxChunkSize 最大chunk大小（字符数）
     * @param overlapSize 重叠大小（字符数）
     * @return Chunk列表，每个chunk包含文本和元数据
     */
    List<TextChunk> chunk(String text, int maxChunkSize, int overlapSize);
}
