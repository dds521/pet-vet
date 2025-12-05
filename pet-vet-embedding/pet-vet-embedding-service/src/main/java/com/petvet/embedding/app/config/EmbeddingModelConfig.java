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

	@Value("${embedding.model.type:openai}")
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

	/**
	 * 创建 Embedding 模型实例
	 * 根据配置自动选择使用哪个模型
	 *
	 * @return EmbeddingModel 实例
	 */
	@Bean
	@Primary
	public EmbeddingModel embeddingModel() {
		EmbeddingModelType type = EmbeddingModelType.fromValue(embeddingModelType);
		
		log.info("初始化 Embedding 模型，类型: {}", type.getValue());
		
		switch (type) {
			case OPENAI:
				return createOpenAiModel();
			case HUGGING_FACE:
				return createHuggingFaceModel();
			case OLLAMA:
				return createOllamaModel();
			case COHERE:
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
		log.info("创建 OpenAI Embedding 模型 - Model: {}, Dimensions: {}", openAiModel, openAiDimensions);
		
		return OpenAiEmbeddingModel.builder()
			.baseUrl(openAiBaseUrl)
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
		log.info("创建 Cohere Embedding 模型 - Model: {}", cohereModelName);
		
		return CohereEmbeddingModel.builder()
			.apiKey(cohereApiKey)
			.modelName(cohereModelName)
			.build();
	}
}

