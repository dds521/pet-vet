package com.petvet.mcp.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petvet.mcp.api.dto.McpConnectionConfig;
import com.petvet.mcp.api.dto.McpServerInfo;
import com.petvet.mcp.app.service.McpClientManager;
import com.petvet.mcp.app.service.McpServerManagerService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * MCP 服务器初始化器
 * 在应用启动时从配置文件加载并注册 MCP 服务器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpServerInitializer {
	
	private final McpServerManagerService serverManagerService;
	private final McpClientManager clientManager;
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * 应用启动后初始化 MCP 服务器
	 */
	@PostConstruct
	public void initialize() {
		log.info("开始初始化 MCP 服务器...");
		
		try {
			// 从配置文件加载服务器配置
			Map<String, Object> serverConfigs = clientManager.loadServerConfigs();
			
			if (serverConfigs == null || serverConfigs.isEmpty()) {
				log.warn("未找到 MCP 服务器配置");
				return;
			}
			
			// 注册每个服务器
			for (Map.Entry<String, Object> entry : serverConfigs.entrySet()) {
				String serverName = entry.getKey();
				Object serverConfig = entry.getValue();
				
				try {
					registerServerFromConfig(serverName, serverConfig);
					log.info("MCP 服务器注册成功: {}", serverName);
				} catch (Exception e) {
					log.error("注册 MCP 服务器失败: {}", serverName, e);
				}
			}
			
			log.info("MCP 服务器初始化完成，共注册 {} 个服务器", serverConfigs.size());
			
		} catch (Exception e) {
			log.error("初始化 MCP 服务器失败", e);
		}
	}
	
	/**
	 * 从配置注册服务器
	 *
	 * @param serverName   服务器名称
	 * @param serverConfig 服务器配置
	 */
	@SuppressWarnings("unchecked")
	private void registerServerFromConfig(String serverName, Object serverConfig) {
		if (!(serverConfig instanceof Map)) {
			log.warn("服务器配置格式错误: {}", serverName);
			return;
		}
		
		Map<String, Object> config = (Map<String, Object>) serverConfig;
		
		// 构建连接配置
		McpConnectionConfig connectionConfig = McpConnectionConfig.builder()
			.transportType("stdio") // 默认使用 stdio
			.command((String) config.get("command"))
			.args((List<String>) config.get("args"))
			.build();
		
		// 构建服务器信息
		McpServerInfo serverInfo = McpServerInfo.builder()
			.name(serverName)
			.description("从配置文件加载的 MCP 服务器: " + serverName)
			.connectionConfig(connectionConfig)
			.build();
		
		// 注册服务器
		serverManagerService.registerServer(serverInfo);
	}
}
