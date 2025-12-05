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
		Embedding embedding = embeddingModel.embed(text).content();
		log.debug("向量化完成，向量维度: {}", embedding.dimension());
		return embedding;
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
