package com.petvet.embedding.api.enums;

/**
 * Embedding 模型类型枚举
 */
public enum EmbeddingModelType {
	/**
	 * OpenAI Embedding 模型
	 * 优点: 性能优秀，支持多种语言
	 * 缺点: 需要 API Key，有使用费用
	 * 推荐模型: text-embedding-3-small (1536维), text-embedding-3-large (3072维)
	 */
	OPENAI("openai"),
	
	/**
	 * Hugging Face Embedding 模型（完全免费）
	 * 优点: 完全免费，开源，模型丰富
	 * 缺点: 首次加载需要下载模型，需要一定内存
	 * 推荐模型: 
	 *   - BAAI/bge-small-zh-v1.5 (中文，384维)
	 *   - sentence-transformers/all-MiniLM-L6-v2 (英文，384维)
	 *   - sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2 (多语言，384维)
	 */
	HUGGING_FACE("hugging-face"),
	
	/**
	 * Ollama Embedding 模型（完全免费，本地运行）
	 * 优点: 完全免费，本地运行，数据隐私好
	 * 缺点: 需要本地安装 Ollama 服务
	 * 推荐模型: nomic-embed-text (768维)
	 */
	OLLAMA("ollama"),
	
	/**
	 * Cohere Embedding 模型
	 * 优点: 性能好，支持多语言
	 * 缺点: 有免费额度限制
	 * 推荐模型: embed-english-v3.0, embed-multilingual-v3.0
	 */
	COHERE("cohere");
	
	private final String value;
	
	EmbeddingModelType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	/**
	 * 根据字符串值获取枚举
	 *
	 * @param value 字符串值
	 * @return 对应的枚举，如果不存在则返回 HUGGING_FACE（免费默认）
	 */
	public static EmbeddingModelType fromValue(String value) {
		if (value == null || value.trim().isEmpty()) {
			return HUGGING_FACE; // 默认使用免费的 Hugging Face
		}
		for (EmbeddingModelType type : values()) {
			if (type.value.equalsIgnoreCase(value.trim())) {
				return type;
			}
		}
		return HUGGING_FACE; // 默认返回免费的 Hugging Face
	}
}

