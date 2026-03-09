package com.petvet.mcp.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PetVetMCP 应用启动类
 * 用于管理 MCP (Model Context Protocol) 服务器。
 *
 * 说明：
 * - 使用默认的 Spring Boot 自动配置；
 * - Spring AI MCP 的自动配置在不同版本中包名有所变化，
 *   为避免编译期依赖具体类名，这里不显式 exclude，
 *   仅依赖我们在代码中手动创建的 MCP 客户端。
 */
@SpringBootApplication
public class PetVetMcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetVetMcpApplication.class, args);
	}
}
