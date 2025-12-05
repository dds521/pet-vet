package com.petvetai.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

/**
 * Pet-Vet-AI 应用主类
 * 
 * 项目使用 LangChain4j 进行 AI 集成，支持 OpenAI、DeepSeek 和 xAI Grok
 */
@SpringBootApplication
@MapperScan("com.petvetai.app.mapper")
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
