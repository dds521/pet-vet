package com.petvet.embedding.app.config;

import com.petvet.embedding.api.enums.VectorDatabaseType;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

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

	@Value("${vector.database.qdrant.collection-name:pet-vet-embeddings-v2}")
	private String qdrantCollectionName;

	@Value("${vector.database.zilliz.host:localhost}")
	private String zillizHost;

	@Value("${vector.database.zilliz.port:19530}")
	private Integer zillizPort;

	@Value("${vector.database.zilliz.api-key:}")
	private String zillizApiKey;

	@Value("${vector.database.zilliz.collection-name:pet-vet-embeddings}")
	private String zillizCollectionName;

	@Value("${vector.database.zilliz.use-secure:true}")
	private Boolean zillizUseSecure; // 保留配置项以兼容现有配置，但 Zilliz Cloud 使用完整 URI，此配置不再使用

	@Value("${vector.database.zilliz.fail-on-connection-error:true}")
	private Boolean zillizFailOnConnectionError;

	@Value("${vector.database.dimension:1024}")
	private Integer dimension;

	/**
	 * 创建向量数据库存储实例
	 * 根据配置的 vector.database.type 选择使用 Qdrant 或 Zilliz
	 * 
	 * 注意：使用 @Lazy 延迟初始化，避免启动时立即连接导致超时
	 * 连接将在实际使用时才建立
	 *
	 * @return EmbeddingStore 实例
	 */
	@Bean
	@Primary
	@org.springframework.context.annotation.Lazy
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
	 * 注意: Zilliz 基于 Milvus，使用 langchain4j-milvus 提供的 MilvusEmbeddingStore
	 * 
	 * 注意：Zilliz Cloud 使用完整 URI（包含协议，如 https://xxx.zillizcloud.com）
	 * 如果配置中的 zillizHost 已经是完整 URI，直接使用；否则组合 host 和 port
	 * 
	 * 注意：MilvusEmbeddingStore 支持：
	 * - uri(): 完整 URI（如 https://xxx.zillizcloud.com 或 https://host:port）
	 * - token(): API Key (Token)
	 * - collectionName(): 集合名称
	 * - dimension(): 向量维度
	 * - idFieldName(): 主键字段名称（Zilliz 集合使用 "primary_key" 作为主键字段名）
	 */
	private EmbeddingStore<TextSegment> createZillizStore() {
		// 构建完整 URI
		String uri = buildZillizUri(zillizHost, zillizPort, zillizUseSecure);
		
		log.info("创建 Zilliz 向量数据库连接 - URI: {}, Collection: {}", uri, zillizCollectionName);
		log.info("注意: 使用 langchain4j-milvus 的 MilvusEmbeddingStore，支持完整 URI 和 Token 认证");
		log.info("注意: 当前 embedding 模型维度为 {}，确保 Zilliz 集合维度匹配", dimension);
		
		try {
			MilvusEmbeddingStore.Builder builder = MilvusEmbeddingStore.builder()
					.uri(uri)
					.collectionName(zillizCollectionName)
					.dimension(dimension)
					// 注意: Zilliz 集合的主键字段名是 "primary_key"，不是默认的 "id"
					.idFieldName("primary_key");
			
			// 如果提供了 Token，设置 Token
			if (zillizApiKey != null && !zillizApiKey.trim().isEmpty()) {
				builder.token(zillizApiKey);
			}
			
			EmbeddingStore<TextSegment> store = builder.build();
			log.info("✓ Zilliz 连接成功");
			log.info("注意: 已设置主键字段名为 'primary_key'，与 Zilliz 集合 schema 匹配");
			return store;
		} catch (Exception e) {
			log.error("❌ Zilliz 连接失败: {}", e.getMessage(), e);
			log.error("错误详情: {}", e.getClass().getSimpleName());
			log.error("可能的原因:");
			log.error("  1. Zilliz URI 不正确 (确保使用 https:// 格式)");
			log.error("  2. Token 无效或已过期 (在 Zilliz 控制台检查)");
			log.error("  3. IP 地址未加入 Zilliz Cloud 白名单");
			log.error("  4. Zilliz 实例在休眠 (首次连接可能需要 5-10 秒)");
			log.error("  5. 网络连接问题 (防火墙/代理阻止)");
			log.error("");
			log.error("解决方案:");
			log.error("  1. 验证 Zilliz URI 和 Token 在 Zilliz 控制台");
			log.error("  2. 在 Zilliz Cloud 控制台添加您的公网 IP 到白名单");
			log.error("  3. 检查网络连接 (尝试关闭 VPN/代理)");
			log.error("  4. 暂时切换到 Qdrant: 设置 vector.database.type=qdrant");
			log.error("  5. 如果使用代理，配置代理设置");
			log.error("");
			log.error("应用将继续启动，但向量数据库功能将不可用，直到连接成功");

			if (zillizFailOnConnectionError) {
				throw new RuntimeException("无法连接到 Zilliz 向量数据库，请检查配置和网络连接", e);
			} else {
				log.warn("⚠️  由于 vector.database.zilliz.fail-on-connection-error=false，应用将继续启动");
				log.warn("⚠️  向量数据库功能将不可用，直到连接成功");
				log.warn("⚠️  在实际使用向量数据库时会抛出异常");
				return createFailedZillizStore(e);
			}
		}
	}

	/**
	 * 构建 Zilliz URI
	 * 如果 zillizHost 已经是完整 URI（包含协议），直接使用
	 * 否则根据 useSecure 和 port 组合成完整 URI
	 * 
	 * @param host 主机地址或完整 URI
	 * @param port 端口号（如果 host 不是完整 URI）
	 * @param useSecure 是否使用安全连接（如果 host 不是完整 URI）
	 * @return 完整 URI
	 */
	private String buildZillizUri(String host, Integer port, Boolean useSecure) {
		// 如果 host 已经是完整 URI（包含协议），直接使用
		if (host.startsWith("http://") || host.startsWith("https://")) {
			log.debug("使用配置中的完整 URI: {}", host);
			return host;
		}
		
		// 否则组合 host、port 和协议
		String protocol = (useSecure != null && useSecure) ? "https" : "http";
		String uri = String.format("%s://%s:%d", protocol, host, port);
		log.debug("组合 URI: {} (host: {}, port: {}, secure: {})", uri, host, port, useSecure);
		return uri;
	}

	/**
	 * 创建一个失败的 Zilliz 存储包装器
	 * 允许应用启动，但在实际使用时抛出异常
	 */
	private EmbeddingStore<TextSegment> createFailedZillizStore(Exception connectionException) {
		return new EmbeddingStore<TextSegment>() {
			@Override
			public String add(dev.langchain4j.data.embedding.Embedding embedding) {
				throw new RuntimeException("Zilliz 向量数据库连接失败，无法添加向量。请检查网络连接和配置。原始错误: " + connectionException.getMessage(), connectionException);
			}
			
			@Override
			public void add(String id, dev.langchain4j.data.embedding.Embedding embedding) {
				throw new RuntimeException("Zilliz 向量数据库连接失败，无法添加向量。请检查网络连接和配置。原始错误: " + connectionException.getMessage(), connectionException);
			}
			
			@Override
			public String add(dev.langchain4j.data.embedding.Embedding embedding, TextSegment textSegment) {
				throw new RuntimeException("Zilliz 向量数据库连接失败，无法添加向量。请检查网络连接和配置。原始错误: " + connectionException.getMessage(), connectionException);
			}
			
			@Override
			public List<String> addAll(List<dev.langchain4j.data.embedding.Embedding> embeddings) {
				throw new RuntimeException("Zilliz 向量数据库连接失败，无法添加向量。请检查网络连接和配置。原始错误: " + connectionException.getMessage(), connectionException);
			}
			
			@Override
			public List<String> addAll(List<dev.langchain4j.data.embedding.Embedding> embeddings, List<TextSegment> textSegments) {
				throw new RuntimeException("Zilliz 向量数据库连接失败，无法添加向量。请检查网络连接和配置。原始错误: " + connectionException.getMessage(), connectionException);
			}
			
			@Override
			public dev.langchain4j.store.embedding.EmbeddingSearchResult<TextSegment> search(
					dev.langchain4j.store.embedding.EmbeddingSearchRequest request) {
				throw new RuntimeException("Zilliz 向量数据库连接失败，无法搜索向量。请检查网络连接和配置。原始错误: " + connectionException.getMessage(), connectionException);
			}
			
			@Override
			public void remove(String id) {
				throw new RuntimeException("Zilliz 向量数据库连接失败，无法删除向量。请检查网络连接和配置。原始错误: " + connectionException.getMessage(), connectionException);
			}
		};
	}
}
