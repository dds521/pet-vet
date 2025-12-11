package com.petvet.rag.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * PetVetRAG 应用启动类
 * 用于增强型检索（RAG - Retrieval-Augmented Generation）
 * 
 * @author daidasheng
 * @date 2024-12-11
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.petvet.embedding.api.feign"})
@MapperScan("com.petvet.rag.app.mapper")
@EnableAsync
public class PetVetRagApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetVetRagApplication.class, args);
	}
}
