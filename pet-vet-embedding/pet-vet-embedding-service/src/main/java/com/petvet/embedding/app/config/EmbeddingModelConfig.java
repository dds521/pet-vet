package com.petvet.embedding.app.config;

import com.petvet.embedding.api.enums.EmbeddingModelType;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.cohere.CohereEmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Embedding 模型配置类
 * 支持多种 Embedding 模型，包括免费和付费选项
 */
@Slf4j
@Configuration
public class EmbeddingModelConfig {

	@Value("${embedding.model.type}")
	private String embeddingModelType;

	// OpenAI 配置
	@Value("${spring.ai.openai.embedding.api-key:}")
	private String openAiApiKey;

	@Value("${spring.ai.openai.embedding.base-url:https://api.openai.com}")
	private String openAiBaseUrl;

	@Value("${spring.ai.openai.embedding.options.model:text-embedding-3-small}")
	private String openAiModel;

	@Value("${spring.ai.openai.embedding.options.dimensions:1536}")
	private Integer openAiDimensions;

	// Hugging Face 配置
	@Value("${embedding.model.hugging-face.model-id:BAAI/bge-small-zh-v1.5}")
	private String huggingFaceModelId;

	@Value("${embedding.model.hugging-face.wait-for-model:true}")
	private Boolean huggingFaceWaitForModel;

	// Ollama 配置
	@Value("${embedding.model.ollama.base-url:http://localhost:11434}")
	private String ollamaBaseUrl;

	@Value("${embedding.model.ollama.model-name:nomic-embed-text}")
	private String ollamaModelName;

	// Cohere 配置
	@Value("${embedding.model.cohere.api-key:}")
	private String cohereApiKey;

	@Value("${embedding.model.cohere.model-name:embed-english-v3.0}")
	private String cohereModelName;

	@Value("${embedding.model.cohere.input-type:search_document}")
	private String cohereInputType;

	/**
	 * 创建 Embedding 模型实例
	 * 根据配置自动选择使用哪个模型
	 *
	 * @return EmbeddingModel 实例
	 */
	@Bean
	@Primary
	public EmbeddingModel embeddingModel() {
		log.info("========================================");
		log.info("配置加载检查:");
		log.info("  原始配置值: embedding.model.type = {}", embeddingModelType);
		log.info("  环境变量 EMBEDDING_MODEL_TYPE: {}", System.getenv("EMBEDDING_MODEL_TYPE"));
		log.info("  系统属性 embedding.model.type: {}", System.getProperty("embedding.model.type"));
		log.info("========================================");
		
		EmbeddingModelType type = EmbeddingModelType.fromValue(embeddingModelType);
		
		log.info("初始化 Embedding 模型");
		log.info("  解析后的类型: {}", type.getValue());
		log.info("  枚举值: {}", type);
		
		switch (type) {
			case OPENAI:
				log.info("  → 使用 OpenAI 模型");
				return createOpenAiModel();
			case HUGGING_FACE:
				log.info("  → 使用 Hugging Face 模型（免费）");
				return createHuggingFaceModel();
			case OLLAMA:
				log.info("  → 使用 Ollama 模型（免费，本地）");
				return createOllamaModel();
			case COHERE:
				log.info("  → 使用 Cohere 模型");
				return createCohereModel();
			default:
				log.warn("未知的 Embedding 模型类型: {}，使用默认的 Hugging Face", embeddingModelType);
				return createHuggingFaceModel();
		}
	}

	/**
	 * 创建 OpenAI Embedding 模型
	 */
	private EmbeddingModel createOpenAiModel() {
		// 清理 baseUrl，确保格式正确
		String cleanedBaseUrl = openAiBaseUrl;
		if (cleanedBaseUrl != null) {
			cleanedBaseUrl = cleanedBaseUrl.trim();
			// 移除末尾的斜杠
			if (cleanedBaseUrl.endsWith("/")) {
				cleanedBaseUrl = cleanedBaseUrl.substring(0, cleanedBaseUrl.length() - 1);
			}
			// 如果 baseUrl 包含 /v1，移除它（langchain4j 会自动添加）
			if (cleanedBaseUrl.endsWith("/v1")) {
				cleanedBaseUrl = cleanedBaseUrl.substring(0, cleanedBaseUrl.length() - 3);
			}
		}
		
		log.info("========================================");
		log.info("创建 OpenAI Embedding 模型");
		log.info("  BaseUrl (清理后): {}", cleanedBaseUrl);
		log.info("  BaseUrl (原始): {}", openAiBaseUrl);
		log.info("  Model: {}", openAiModel);
		log.info("  Dimensions: {}", openAiDimensions);
		log.info("  API Key: {}", (openAiApiKey != null && !openAiApiKey.isEmpty() 
			? openAiApiKey.substring(0, Math.min(10, openAiApiKey.length())) + "..." 
			: "未设置"));
		log.info("  预期 API 端点: {}/v1/embeddings", cleanedBaseUrl);
		log.info("========================================");
		
		if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
			log.warn("⚠️  OpenAI API Key 未设置，Embedding 功能将无法使用");
		}
		
		// 验证 baseUrl 格式
		if (cleanedBaseUrl == null || cleanedBaseUrl.isEmpty()) {
			log.error("❌ BaseUrl 为空，使用默认值: https://api.openai.com");
			cleanedBaseUrl = "https://api.openai.com";
		} else if (!cleanedBaseUrl.startsWith("http://") && !cleanedBaseUrl.startsWith("https://")) {
			log.error("❌ BaseUrl 格式错误，缺少协议: {}", cleanedBaseUrl);
			log.error("   自动添加 https:// 前缀");
			cleanedBaseUrl = "https://" + cleanedBaseUrl;
		}
		
		return OpenAiEmbeddingModel.builder()
			.baseUrl(cleanedBaseUrl)
			.apiKey(openAiApiKey)
			.modelName(openAiModel)
			.dimensions(openAiDimensions)
			.build();
	}

	/**
	 * 创建 Hugging Face Embedding 模型（完全免费）
	 */
	private EmbeddingModel createHuggingFaceModel() {
		log.info("创建 Hugging Face Embedding 模型 - Model: {}", huggingFaceModelId);
		log.info("注意: 首次使用需要下载模型，可能需要一些时间");
		
		return HuggingFaceEmbeddingModel.builder()
			.modelId(huggingFaceModelId)
			.waitForModel(huggingFaceWaitForModel)
			.build();
	}

	/**
	 * 创建 Ollama Embedding 模型（完全免费，本地运行）
	 */
	private EmbeddingModel createOllamaModel() {
		log.info("创建 Ollama Embedding 模型 - BaseUrl: {}, Model: {}", ollamaBaseUrl, ollamaModelName);
		log.info("注意: 需要确保 Ollama 服务正在运行: {}", ollamaBaseUrl);
		
		return OllamaEmbeddingModel.builder()
			.baseUrl(ollamaBaseUrl)
			.modelName(ollamaModelName)
			.build();
	}

	/**
	 * 创建 Cohere Embedding 模型
	 */
	private EmbeddingModel createCohereModel() {
		log.info("创建 Cohere Embedding 模型");
		log.info("  Model: {}", cohereModelName);
		log.info("  InputType: {}", cohereInputType);
		
		if (cohereApiKey == null || cohereApiKey.trim().isEmpty()) {
			log.error("❌ Cohere API Key 未设置，无法使用 Cohere 模型");
			log.warn("⚠️  自动切换到 Hugging Face 模型（免费）");
			return createHuggingFaceModel();
		}
		
		// Cohere v3 模型（如 embed-english-v3.0）需要指定 input_type
		// 可选值: search_document, search_query, classification, clustering
		// 默认使用 search_document（用于存储文档到向量数据库）
		boolean isV3Model = cohereModelName != null && cohereModelName.contains("v3");
		if (isV3Model && (cohereInputType == null || cohereInputType.trim().isEmpty())) {
			log.warn("⚠️  Cohere v3 模型需要 input_type，使用默认值: search_document");
			cohereInputType = "search_document";
		}
		
		// 构建 Cohere 模型，使用链式调用
		// 注意：如果 v3 模型，需要设置 input_type
		if (isV3Model && cohereInputType != null && !cohereInputType.trim().isEmpty()) {
			log.info("  ✓ 已设置 input_type: {}", cohereInputType);
			return CohereEmbeddingModel.builder()
				.apiKey(cohereApiKey)
				.modelName(cohereModelName)
				.inputType(cohereInputType)
				.build();
		} else {
			return CohereEmbeddingModel.builder()
				.apiKey(cohereApiKey)
				.modelName(cohereModelName)
				.build();
		}
	}
}

