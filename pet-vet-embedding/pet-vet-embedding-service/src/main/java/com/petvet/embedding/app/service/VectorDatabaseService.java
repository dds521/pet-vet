package com.petvet.embedding.app.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 向量数据库服务
 * 提供向量存储和检索功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorDatabaseService {

	private final EmbeddingStore<TextSegment> embeddingStore;
	private final EmbeddingModel embeddingModel;

	/**
	 * 添加文本段到向量数据库
	 *
	 * @param text 文本内容
	 * @return 存储的 ID
	 */
	public String add(String text) {
		log.debug("添加文本到向量数据库，长度: {}", text.length());
		TextSegment segment = TextSegment.from(text);
		Embedding embedding = embeddingModel.embed(segment).content();
		String id = embeddingStore.add(embedding, segment);
		log.debug("文本已添加到向量数据库，ID: {}", id);
		return id;
	}

	/**
	 * 添加文本段到向量数据库，并指定 ID
	 * 
	 * 注意: EmbeddingStore 接口的 add(String, Embedding) 方法不支持 TextSegment 参数
	 * 如果需要保存 TextSegment 元数据，请使用 add(Embedding, TextSegment) 方法（自动生成 ID）
	 *
	 * @param id 指定的 ID（主键值，String 类型）
	 * @param text 文本内容
	 */
	public void addWithId(String id, String text) {
		log.debug("添加文本到向量数据库，ID: {}, 长度: {}", id, text.length());
		TextSegment segment = TextSegment.from(text);
		Embedding embedding = embeddingModel.embed(segment).content();
		// 注意: add(String, Embedding) 方法不支持 TextSegment，会丢失元数据
		embeddingStore.add(id, embedding);
		log.debug("文本已添加到向量数据库，ID: {}", id);
	}

	/**
	 * 批量添加文本段到向量数据库
	 *
	 * @param texts 文本内容列表
	 * @return 存储的 ID 列表
	 */
	public List<String> addBatch(List<String> texts) {
		log.debug("批量添加文本到向量数据库，数量: {}", texts.size());
		List<TextSegment> segments = texts.stream()
			.map(TextSegment::from)
			.toList();
		List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
		List<String> ids = embeddingStore.addAll(embeddings, segments);
		log.debug("批量文本已添加到向量数据库，数量: {}", ids.size());
		return ids;
	}

	/**
	 * 批量添加文本段到向量数据库，并指定 ID 列表
	 *
	 * @param ids 指定的 ID 列表（主键值列表）
	 * @param texts 文本内容列表
	 */
	public void addBatchWithIds(List<String> ids, List<String> texts) {
		if (ids.size() != texts.size()) {
			throw new IllegalArgumentException("ID 列表和文本列表的长度必须相同");
		}
		log.debug("批量添加文本到向量数据库，数量: {}", texts.size());
		List<TextSegment> segments = texts.stream()
			.map(TextSegment::from)
			.toList();
		List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
		embeddingStore.addAll(ids, embeddings, segments);
		log.debug("批量文本已添加到向量数据库，数量: {}", ids.size());
	}

	/**
	 * 根据文本内容搜索相似向量
	 *
	 * @param queryText 查询文本
	 * @param maxResults 最大返回结果数
	 * @param minScore 最小相似度分数
	 * @return 相似向量匹配结果列表
	 */
	public List<EmbeddingMatch<TextSegment>> findSimilar(String queryText, int maxResults, double minScore) {
		log.debug("搜索相似向量，查询文本长度: {}, 最大结果数: {}, 最小分数: {}", 
			queryText.length(), maxResults, minScore);
		Embedding queryEmbedding = embeddingModel.embed(queryText).content();
		// 注意: langchain4j-core:1.9.1 使用 search 方法而不是 findRelevant
		EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
			.queryEmbedding(queryEmbedding)
			.maxResults(maxResults)
			.minScore(minScore)
			.build();
		EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
		List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
		log.debug("找到 {} 个相似向量", matches.size());
		return matches;
	}

	/**
	 * 根据向量搜索相似向量
	 *
	 * @param embedding 查询向量
	 * @param maxResults 最大返回结果数
	 * @param minScore 最小相似度分数
	 * @return 相似向量匹配结果列表
	 */
	public List<EmbeddingMatch<TextSegment>> findSimilar(Embedding embedding, int maxResults, double minScore) {
		log.debug("根据向量搜索相似向量，最大结果数: {}, 最小分数: {}", maxResults, minScore);
		// 注意: langchain4j-core:1.9.1 使用 search 方法而不是 findRelevant
		EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
			.queryEmbedding(embedding)
			.maxResults(maxResults)
			.minScore(minScore)
			.build();
		EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
		List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
		log.debug("找到 {} 个相似向量", matches.size());
		return matches;
	}

	/**
	 * 删除向量
	 *
	 * @param id 向量 ID
	 */
	public void delete(String id) {
		log.debug("删除向量，ID: {}", id);
		embeddingStore.remove(id);
		log.debug("向量已删除，ID: {}", id);
	}

	/**
	 * 批量删除向量
	 *
	 * @param ids 向量 ID 列表
	 */
	public void deleteBatch(List<String> ids) {
		log.debug("批量删除向量，数量: {}", ids.size());
		ids.forEach(embeddingStore::remove);
		log.debug("批量向量已删除，数量: {}", ids.size());
	}
}
