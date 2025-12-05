package com.petvet.embedding.app.controller;

import com.petvet.embedding.app.service.EmbeddingService;
import dev.langchain4j.data.embedding.Embedding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 向量化控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/embedding")
@RequiredArgsConstructor
public class EmbeddingController {

	private final EmbeddingService embeddingService;

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
	 * 健康检查
	 */
	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of("status", "ok", "service", "pet-vet-embedding");
	}
}
