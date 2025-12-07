package com.petvet.mcp.app.service;

import com.petvet.mcp.api.dto.McpToolRequest;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP 工具调用服务
 * 负责调用 MCP 服务器提供的工具
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolService {
	
	private final McpServerManagerService serverManagerService;
	private final McpClientManager clientManager;
	
	/**
	 * 调用 MCP 工具
	 *
	 * @param request 工具调用请求
	 * @return 工具执行结果
	 */
	public Map<String, Object> callTool(McpToolRequest request) {
		// 1. 验证服务器是否存在
		if (!serverManagerService.serverExists(request.getServerName())) {
			throw new IllegalArgumentException("MCP 服务器不存在: " + request.getServerName());
		}
		
		log.info("调用 MCP 工具: server={}, tool={}, args={}", 
			request.getServerName(), 
			request.getToolName(), 
			request.getArguments());
		
		try {
			// 2. 获取服务器信息
			var serverInfo = serverManagerService.getServer(request.getServerName());
			
			// 3. 获取或创建 MCP 客户端
			McpSyncClient client = clientManager.getOrCreateClient(serverInfo);
			
			// 3.5. 确保客户端已初始化（延迟初始化）
			clientManager.ensureInitialized(client, request.getServerName());
			
			// 4. 构建工具调用请求
			Map<String, Object> arguments = request.getArguments() != null 
				? request.getArguments() 
				: new HashMap<>();
			
			McpSchema.CallToolRequest callToolRequest = new McpSchema.CallToolRequest(
				request.getToolName(),
				arguments
			);
			
			// 5. 调用工具
			McpSchema.CallToolResult result = client.callTool(callToolRequest);
			
			// 6. 构建返回结果
			Map<String, Object> response = new HashMap<>();
			response.put("serverName", request.getServerName());
			response.put("toolName", request.getToolName());
			response.put("result", result.content());
			response.put("isError", result.isError());
			
			// 如果出错，错误信息在 content 中
			// MCP 规范：当 isError=true 时，content 包含错误详情
			if (result.isError()) {
				// 尝试从 content 中提取错误信息
				// content 是 List<McpSchema.Content>，包含错误详情
				// 这里简化处理，将整个 content 作为错误信息返回
				// 实际使用时，可以根据 content 的类型和结构进行解析
				response.put("error", "工具执行失败，错误详情请查看 result 字段中的 content");
			}
			
			response.put("timestamp", System.currentTimeMillis());
			
			log.info("MCP 工具调用成功: server={}, tool={}", 
				request.getServerName(), 
				request.getToolName());
			
			return response;
			
		} catch (Exception e) {
			log.error("调用 MCP 工具失败: server={}, tool={}", 
				request.getServerName(), 
				request.getToolName(), 
				e);
			
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("serverName", request.getServerName());
			errorResponse.put("toolName", request.getToolName());
			errorResponse.put("error", e.getMessage());
			errorResponse.put("timestamp", System.currentTimeMillis());
			
			throw new RuntimeException("调用 MCP 工具失败: " + e.getMessage(), e);
		}
	}
	
	/**
	 * 列出服务器可用的工具
	 *
	 * @param serverName 服务器名称
	 * @return 工具列表
	 */
	public Map<String, Object> listTools(String serverName) {
		if (!serverManagerService.serverExists(serverName)) {
			throw new IllegalArgumentException("MCP 服务器不存在: " + serverName);
		}
		
		log.info("列出服务器工具: {}", serverName);
		
		try {
			// 1. 获取服务器信息
			var serverInfo = serverManagerService.getServer(serverName);
			
			// 2. 获取或创建 MCP 客户端
			McpSyncClient client = clientManager.getOrCreateClient(serverInfo);
			
			// 2.5. 确保客户端已初始化（延迟初始化）
			clientManager.ensureInitialized(client, serverName);
			
			// 3. 获取工具列表
			McpSchema.ListToolsResult toolsResult = client.listTools();
			
			// 4. 构建返回结果
			Map<String, Object> response = new HashMap<>();
			response.put("serverName", serverName);
			response.put("tools", toolsResult.tools());
			response.put("timestamp", System.currentTimeMillis());
			
			log.info("获取工具列表成功: server={}, count={}", 
				serverName, 
				toolsResult.tools() != null ? toolsResult.tools().size() : 0);
			
			return response;
			
		} catch (Exception e) {
			log.error("获取工具列表失败: server={}", serverName, e);
			
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("serverName", serverName);
			errorResponse.put("error", e.getMessage());
			errorResponse.put("timestamp", System.currentTimeMillis());
			
			throw new RuntimeException("获取工具列表失败: " + e.getMessage(), e);
		}
	}
}
