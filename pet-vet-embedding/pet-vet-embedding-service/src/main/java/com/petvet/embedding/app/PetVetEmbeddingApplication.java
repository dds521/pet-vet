package com.petvet.embedding.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PetVetEmbedding 应用启动类
 * 用于数据向量化操作
 * 
 * 注意：langchain4j-spring-boot-starter 的自动配置通过 application.yml 中的配置属性禁用
 */
@SpringBootApplication
@MapperScan("com.petvet.embedding.app.mapper")
public class PetVetEmbeddingApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetVetEmbeddingApplication.class, args);
	}
}