package com.petvet.embedding.app.controller;

import com.petvet.embedding.app.service.EmbeddingService;
import com.petvet.embedding.app.service.VectorDatabaseService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 向量化控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/embedding")
@RequiredArgsConstructor
public class EmbeddingController {

	private final EmbeddingService embeddingService;
	private final VectorDatabaseService vectorDatabaseService;

	/**
	 * 单个文本向量化
	 *
	 * @param request 包含文本的请求
	 * @return 向量数据
	 */
	@PostMapping("/embed")
	public Map<String, Object> embed(@RequestBody Map<String, String> request) {
		String text = request.get("text");
		if (text == null || text.trim().isEmpty()) {
			throw new IllegalArgumentException("文本内容不能为空");
		}
		
		Embedding embedding = embeddingService.embed(text);
		return Map.of(
			"dimension", embedding.dimension(),
			"vector", embedding.vectorAsList()
		);
	}

	/**
	 * 批量文本向量化
	 *
	 * @param request 包含文本列表的请求
	 * @return 向量数据列表
	 */
	@PostMapping("/embed/batch")
	public Map<String, Object> embedBatch(@RequestBody Map<String, List<String>> request) {
		List<String> texts = request.get("texts");
		if (texts == null || texts.isEmpty()) {
			throw new IllegalArgumentException("文本列表不能为空");
		}
		
		Embedding[] embeddings = embeddingService.embedBatch(texts.toArray(new String[0]));
		return Map.of(
			"count", embeddings.length,
			"embeddings", List.of(embeddings)
		);
	}

	/**
	 * 添加文本到向量数据库
	 *
	 * @param request 包含文本的请求
	 * @return 存储的 ID
	 */
	@PostMapping("/vector/add")
	public Map<String, Object> addToVectorStore(@RequestBody Map<String, String> request) {
		String text = request.get("text");
		if (text == null || text.trim().isEmpty()) {
			throw new IllegalArgumentException("文本内容不能为空");
		}
		
		String id = vectorDatabaseService.add(text);
		return Map.of("id", id, "message", "文本已添加到向量数据库");
	}

	/**
	 * 批量添加文本到向量数据库
	 *
	 * @param request 包含文本列表的请求
	 * @return 存储的 ID 列表
	 */
	@PostMapping("/vector/add/batch")
	public Map<String, Object> addBatchToVectorStore(@RequestBody Map<String, List<String>> request) {
		List<String> texts = request.get("texts");
		if (texts == null || texts.isEmpty()) {
			throw new IllegalArgumentException("文本列表不能为空");
		}
		
		List<String> ids = vectorDatabaseService.addBatch(texts);
		return Map.of("count", ids.size(), "ids", ids);
	}

	/**
	 * 搜索相似向量
	 *
	 * @param request 包含查询文本、最大结果数和最小分数的请求
	 * @return 相似向量匹配结果
	 */
	@PostMapping("/vector/search")
	public Map<String, Object> searchSimilar(@RequestBody Map<String, Object> request) {
		String queryText = (String) request.get("text");
		if (queryText == null || queryText.trim().isEmpty()) {
			throw new IllegalArgumentException("查询文本不能为空");
		}
		
		int maxResults = request.get("maxResults") != null 
			? ((Number) request.get("maxResults")).intValue() 
			: 10;
		double minScore = request.get("minScore") != null 
			? ((Number) request.get("minScore")).doubleValue() 
			: 0.0;
		
		List<EmbeddingMatch<TextSegment>> matches = vectorDatabaseService.findSimilar(queryText, maxResults, minScore);
		
		List<Map<String, Object>> results = matches.stream()
			.map(match -> {
				Map<String, Object> result = new java.util.HashMap<>();
				result.put("id", match.embeddingId());
				result.put("score", match.score());
				result.put("text", match.embedded().text());
				return result;
			})
			.collect(Collectors.toList());
		
		Map<String, Object> response = new java.util.HashMap<>();
		response.put("count", results.size());
		response.put("results", results);
		return response;
	}

	/**
	 * 删除向量
	 *
	 * @param request 包含向量 ID 的请求
	 * @return 删除结果
	 */
	@DeleteMapping("/vector/delete")
	public Map<String, String> deleteFromVectorStore(@RequestBody Map<String, String> request) {
		String id = request.get("id");
		if (id == null || id.trim().isEmpty()) {
			throw new IllegalArgumentException("向量 ID 不能为空");
		}
		
		vectorDatabaseService.delete(id);
		return Map.of("message", "向量已删除", "id", id);
	}

	/**
	 * 健康检查
	 */
	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of("status", "ok", "service", "pet-vet-embedding");
	}
}
