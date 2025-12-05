package com.petvet.embedding.app.config;

import com.petvet.embedding.api.enums.VectorDatabaseType;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 向量数据库配置类
 * 支持 Qdrant 和 Zilliz 两种向量数据库，通过配置开关控制使用哪一个
 */
@Slf4j
@Configuration
public class VectorDatabaseConfig {

	@Value("${vector.database.type:qdrant}")
	private String vectorDatabaseType;

	@Value("${vector.database.qdrant.host:localhost}")
	private String qdrantHost;

	@Value("${vector.database.qdrant.port:6333}")
	private Integer qdrantPort;

	@Value("${vector.database.qdrant.api-key:}")
	private String qdrantApiKey;

	@Value("${vector.database.qdrant.collection-name:pet-vet-embeddings}")
	private String qdrantCollectionName;

	@Value("${vector.database.zilliz.host:localhost}")
	private String zillizHost;

	@Value("${vector.database.zilliz.port:19530}")
	private Integer zillizPort;

	@Value("${vector.database.zilliz.api-key:}")
	private String zillizApiKey;

	@Value("${vector.database.zilliz.collection-name:pet-vet-embeddings}")
	private String zillizCollectionName;

	@Value("${vector.database.dimension:1024}")
	private Integer dimension;

	/**
	 * 创建向量数据库存储实例
	 * 根据配置的 vector.database.type 选择使用 Qdrant 或 Zilliz
	 *
	 * @return EmbeddingStore 实例
	 */
	@Bean
	@Primary
	public EmbeddingStore<TextSegment> embeddingStore() {
		VectorDatabaseType type = VectorDatabaseType.fromValue(vectorDatabaseType);
		
		log.info("初始化向量数据库，类型: {}", type.getValue());
		
		switch (type) {
			case QDRANT:
				return createQdrantStore();
			case ZILLIZ:
				return createZillizStore();
			default:
				log.warn("未知的向量数据库类型: {}，使用默认的 Qdrant", vectorDatabaseType);
				return createQdrantStore();
		}
	}

	/**
	 * 创建 Qdrant 向量数据库存储
	 */
	private EmbeddingStore<TextSegment> createQdrantStore() {
		// 处理 host 配置，移除协议前缀
		String host = qdrantHost;
		boolean useTls = false;
		
		if (host.startsWith("https://")) {
			host = host.replace("https://", "");
			useTls = true;
		} else if (host.startsWith("http://")) {
			host = host.replace("http://", "");
			useTls = false;
		}
		
		// 如果端口是 6334，通常需要 TLS
		if (qdrantPort == 6334) {
			useTls = true;
		}
		
		log.info("创建 Qdrant 向量数据库连接 - Host: {}, Port: {}, TLS: {}, Collection: {}", 
			host, qdrantPort, useTls, qdrantCollectionName);
		log.info("注意: 当前 embedding 模型维度为 {}，如果集合已存在但维度不匹配，请删除集合或修改集合名称", dimension);
		
		QdrantEmbeddingStore.Builder builder = QdrantEmbeddingStore.builder()
			.host(host)
			.port(qdrantPort)
			.collectionName(qdrantCollectionName)
			.useTls(useTls);
		
		// 注意：QdrantEmbeddingStore 不支持直接设置维度
		// 如果集合不存在，会在第一次 add 时自动创建，维度由第一个向量决定
		// 如果集合已存在但维度不匹配，会报错，需要删除集合或修改集合名称
		
		if (qdrantApiKey != null && !qdrantApiKey.trim().isEmpty()) {
			builder.apiKey(qdrantApiKey);
		}
		
		return builder.build();
	}

	/**
	 * 创建 Zilliz 向量数据库存储
	 * 注意: Zilliz 基于 Milvus，使用 MilvusEmbeddingStore
	 */
	private EmbeddingStore<TextSegment> createZillizStore() {
		// 处理 host 配置，移除协议前缀
		String host = zillizHost;
		if (host.startsWith("https://")) {
			host = host.replace("https://", "");
		} else if (host.startsWith("http://")) {
			host = host.replace("http://", "");
		}
		
		log.info("创建 Zilliz 向量数据库连接 - Host: {}, Port: {}, Collection: {}", 
			host, zillizPort, zillizCollectionName);
		log.info("注意: Zilliz 基于 Milvus，使用 MilvusEmbeddingStore");
		
		MilvusEmbeddingStore.Builder builder = MilvusEmbeddingStore.builder()
			.host(host)
			.port(zillizPort)
			.collectionName(zillizCollectionName)
			.dimension(dimension);
		
		// Milvus/Zilliz 使用 token 而不是 apiKey
		if (zillizApiKey != null && !zillizApiKey.trim().isEmpty()) {
			builder.token(zillizApiKey);
		}
		
		return builder.build();
	}
}
