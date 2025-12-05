package com.petvet.embedding.app.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 向量化服务
 * 用于将文本数据转换为向量表示
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

	private final EmbeddingModel embeddingModel;

	/**
	 * 将文本转换为向量
	 *
	 * @param text 待向量化的文本
	 * @return 向量表示
	 */
	public Embedding embed(String text) {
		log.debug("开始向量化文本，长度: {}", text.length());
		try {
			Embedding embedding = embeddingModel.embed(text).content();
			log.debug("向量化完成，向量维度: {}", embedding.dimension());
			return embedding;
		} catch (Exception e) {
			log.error("向量化失败: {}", e.getMessage());
			log.error("错误类型: {}", e.getClass().getName());
			if (e.getCause() != null) {
				log.error("根本原因: {}", e.getCause().getMessage());
			}
			// 如果是 404 错误，提供更详细的提示
			if (e.getMessage() != null && e.getMessage().contains("404")) {
				log.error("⚠️  404 错误通常表示：");
				log.error("  1. baseUrl 配置错误（应该是 https://api.openai.com，不包含 /v1）");
				log.error("  2. API Key 无效或已过期");
				log.error("  3. 使用了代理服务器但配置不正确");
				log.error("  建议：检查 Nacos 配置中的 spring.ai.openai.embedding.base-url");
			}
			throw new RuntimeException("向量化失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 批量向量化文本
	 *
	 * @param texts 待向量化的文本数组
	 * @return 向量数组
	 */
	public Embedding[] embedBatch(String... texts) {
		log.debug("开始批量向量化，文本数量: {}", texts.length);
		List<TextSegment> textSegments = Arrays.stream(texts)
			.map(TextSegment::from)
			.collect(Collectors.toList());
		return embeddingModel.embedAll(textSegments).content().toArray(new Embedding[0]);
	}
}
