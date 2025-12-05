package com.petvet.embedding.app.config;

import com.petvet.embedding.api.enums.VectorDatabaseType;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 向量数据库初始化器
 * 在应用启动时初始化向量数据库，确保集合存在
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorDatabaseInitializer implements CommandLineRunner {

	private final EmbeddingStore<TextSegment> embeddingStore;
	private final EmbeddingModel embeddingModel;

	@Value("${vector.database.type:qdrant}")
	private String vectorDatabaseType;

	@Value("${vector.database.qdrant.collection-name:pet-vet-embeddings-v2}")
	private String qdrantCollectionName;

	@Value("${vector.database.zilliz.collection-name:pet-vet-embeddings}")
	private String zillizCollectionName;

	@Override
	public void run(String... args) {
		VectorDatabaseType type = VectorDatabaseType.fromValue(vectorDatabaseType);
		
		if (type == VectorDatabaseType.QDRANT) {
			log.info("初始化 Qdrant 向量数据库集合: {}", qdrantCollectionName);
			initializeQdrantCollection();
		} else if (type == VectorDatabaseType.ZILLIZ) {
			log.info("初始化 Zilliz 向量数据库集合: {}", zillizCollectionName);
			initializeZillizCollection();
		} else {
			log.debug("向量数据库类型为 {}，跳过集合初始化", type.getValue());
		}
	}

	/**
	 * 初始化 Qdrant 集合
	 * 通过添加一个测试向量来触发集合的自动创建（如果不存在）
	 * 
	 * 注意：QdrantEmbeddingStore 在第一次 add 时应该会自动创建集合（如果提供了 dimension）
	 * 但如果集合已存在但配置不匹配（如 dimension 不同），会报错
	 */
	private void initializeQdrantCollection() {
		try {
			log.info("尝试初始化 Qdrant 集合: {}", qdrantCollectionName);
			log.info("注意: QdrantEmbeddingStore 会在第一次 add 时自动创建集合");
			
			// 创建一个测试向量来触发集合创建
			// 如果集合不存在，QdrantEmbeddingStore 应该会自动创建
			String testText = "__INIT__";
			TextSegment testSegment = TextSegment.from(testText);
			Embedding testEmbedding = embeddingModel.embed(testSegment).content();
			
			log.info("测试向量维度: {}", testEmbedding.dimension());
			
			// 尝试添加测试向量，这会触发集合的自动创建（如果不存在）
			String testId = embeddingStore.add(testEmbedding, testSegment);
			log.info("✓ Qdrant 集合初始化成功，测试向量 ID: {}", testId);
			
			// 立即删除测试向量，保持集合干净
			try {
				embeddingStore.remove(testId);
				log.debug("✓ 测试向量已清理");
			} catch (Exception e) {
				log.warn("清理测试向量失败（可忽略）: {}", e.getMessage());
			}
			
		} catch (Exception e) {
			log.error("❌ Qdrant 集合初始化失败: {}", e.getMessage());
			log.error("错误详情:", e);
			log.error("");
			log.error("可能的原因:");
			log.error("  1. 集合已存在但维度不匹配（例如：集合是 1536 维，但当前模型是 1024 维）");
			log.error("  2. Qdrant 服务不可访问");
			log.error("  3. API Key 不正确");
			log.error("");
			log.error("解决方案:");
			log.error("  1. 在 Qdrant 控制台中删除现有集合: {}", qdrantCollectionName);
			log.error("  2. 或修改配置中的集合名称，使用新的集合");
			log.error("  3. 或确保向量维度配置与 embedding 模型匹配");
			log.error("");
			// 不抛出异常，允许应用继续启动，但会在实际使用时失败
		}
	}

	/**
	 * 初始化 Zilliz 集合
	 * 通过添加一个测试向量来触发集合的自动创建（如果不存在）
	 * 
	 * 注意：MilvusEmbeddingStore 在第一次 add 时应该会自动创建集合（如果提供了 dimension）
	 * 但如果集合已存在但配置不匹配（如 dimension 不同），可能会失败
	 */
	private void initializeZillizCollection() {
		try {
			log.info("尝试初始化 Zilliz 集合: {}", zillizCollectionName);
			log.info("注意: MilvusEmbeddingStore 会在第一次 add 时自动创建集合");
			
			// 创建一个测试向量来触发集合创建
			// 如果集合不存在，MilvusEmbeddingStore 应该会自动创建
			String testText = "__INIT__";
			TextSegment testSegment = TextSegment.from(testText);
			Embedding testEmbedding = embeddingModel.embed(testSegment).content();
			
			log.info("测试向量维度: {}", testEmbedding.dimension());
			
			// 尝试添加测试向量，这会触发集合的自动创建（如果不存在）
			String testId = embeddingStore.add(testEmbedding, testSegment);
			log.info("✓ Zilliz 集合初始化成功，测试向量 ID: {}", testId);
			
			// 立即删除测试向量，保持集合干净
			try {
				embeddingStore.remove(testId);
				log.debug("✓ 测试向量已清理");
			} catch (Exception e) {
				log.warn("清理测试向量失败（可忽略）: {}", e.getMessage());
			}
			
		} catch (Exception e) {
			log.error("❌ Zilliz 集合初始化失败: {}", e.getMessage());
			log.error("错误详情:", e);
			log.error("");
			log.error("请检查以下配置:");
			log.error("  1. Zilliz 服务可访问: {}", System.getenv("ZILLIZ_HOST"));
			log.error("  2. API Key 已配置: {}", 
				(System.getenv("ZILLIZ_API_KEY") != null && !System.getenv("ZILLIZ_API_KEY").isEmpty()) ? "是" : "否");
			log.error("  3. 集合名称: {}", zillizCollectionName);
			log.error("  4. 向量维度配置是否正确（应与 embedding 模型维度匹配）");
			log.error("");
			log.error("如果集合已存在但维度不匹配，请:");
			log.error("  - 在 Zilliz 控制台中删除现有集合，或");
			log.error("  - 修改配置中的集合名称");
			log.error("");
			// 不抛出异常，允许应用继续启动，但会在实际使用时失败
			// 这样可以让用户看到详细的错误信息
		}
	}
}

