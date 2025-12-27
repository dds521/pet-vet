package com.petvetai.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.Environment;

/**
 * Pet-Vet-AI 应用主类
 * 
 * 顶层服务，供 APP 端、PC 端使用
 * 所有 AI 能力（向量化、RAG、LLM）均通过以下服务提供：
 * - pet-vet-rag: RAG 增强检索服务
 * - pet-vet-embedding: 向量化服务
 * - pet-vet-mcp: MCP 工具服务
 * 
 * @author PetVetAI Team
 * @date 2024-12-16
 */
@SpringBootApplication
@MapperScan({"com.petvetai.infrastructure.persistence.user.mapper", 
             "com.petvetai.infrastructure.persistence.pet.mapper",
             "com.petvetai.infrastructure.persistence.transaction.mapper"})
@EnableFeignClients(basePackages = {"com.petvet.rag.api.feign"})
public class PetVetAiApplication {

	public static void main(String[] args) {
		var app = new SpringApplication(PetVetAiApplication.class);
		var context = app.run(args);
		Environment env = context.getEnvironment();
		
		// 打印当前激活的 profile
		System.out.println("==========================================");
		System.out.println("当前激活的 Profile: " + String.join(", ", env.getActiveProfiles()));
		System.out.println("Seata 是否启用: " + env.getProperty("seata.enabled", "未配置"));
		System.out.println("==========================================");
	}

}
