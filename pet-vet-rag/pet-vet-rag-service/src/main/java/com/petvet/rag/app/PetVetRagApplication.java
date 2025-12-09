package com.petvet.rag.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * PetVetRAG 应用启动类
 * 用于增强型检索（RAG - Retrieval-Augmented Generation）
 * 
 * @author PetVetRAG Team
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.petvet.rag.api.feign", "com.petvet.embedding.api.feign"})
public class PetVetRagApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetVetRagApplication.class, args);
	}
}
