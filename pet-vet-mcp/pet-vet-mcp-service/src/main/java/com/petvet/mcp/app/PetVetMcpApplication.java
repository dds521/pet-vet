package com.petvet.mcp.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * PetVetMCP 应用启动类
 * 用于管理 MCP (Model Context Protocol) 服务器
 */
@SpringBootApplication(exclude = {
	org.springframework.ai.mcp.client.autoconfigure.McpClientAutoConfiguration.class
})
public class PetVetMcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetVetMcpApplication.class, args);
	}
}
