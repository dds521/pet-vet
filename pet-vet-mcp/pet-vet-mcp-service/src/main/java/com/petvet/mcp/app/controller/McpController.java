package com.petvet.mcp.app.controller;

import com.petvet.mcp.api.constants.ApiConstants;
import com.petvet.mcp.api.dto.*;
import com.petvet.mcp.api.enums.McpServerStatus;
import com.petvet.mcp.app.service.McpResourceService;
import com.petvet.mcp.app.service.McpServerManagerService;
import com.petvet.mcp.app.service.McpToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MCP 管理控制器
 * 提供 MCP 服务器管理和调用的 REST API
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.API_PREFIX)
@RequiredArgsConstructor
public class McpController {
	
	private final McpServerManagerService serverManagerService;
	private final McpToolService toolService;
	private final McpResourceService resourceService;
	
	/**
	 * 注册 MCP 服务器
	 */
	@PostMapping("/servers/register")
	public ResponseEntity<McpServerInfo> registerServer(@Valid @RequestBody McpServerInfo serverInfo) {
		McpServerInfo registered = serverManagerService.registerServer(serverInfo);
		return ResponseEntity.ok(registered);
	}
	
	/**
	 * 获取所有已注册的服务器
	 */
	@GetMapping("/servers")
	public ResponseEntity<List<McpServerInfo>> getAllServers() {
		List<McpServerInfo> servers = serverManagerService.getAllServers();
		return ResponseEntity.ok(servers);
	}
	
	/**
	 * 根据名称获取服务器信息
	 */
	@GetMapping("/servers/{serverName}")
	public ResponseEntity<McpServerInfo> getServer(@PathVariable String serverName) {
		McpServerInfo serverInfo = serverManagerService.getServer(serverName);
		return ResponseEntity.ok(serverInfo);
	}
	
	/**
	 * 更新服务器状态
	 */
	@PutMapping("/servers/{serverName}/status")
	public ResponseEntity<Map<String, String>> updateServerStatus(
		@PathVariable String serverName,
		@RequestBody Map<String, String> request) {
		String statusStr = request.get("status");
		McpServerStatus status = McpServerStatus.valueOf(statusStr.toUpperCase());
		serverManagerService.updateServerStatus(serverName, status);
		return ResponseEntity.ok(Map.of("message", "状态已更新", "serverName", serverName, "status", statusStr));
	}
	
	/**
	 * 注销服务器
	 */
	@DeleteMapping("/servers/{serverName}")
	public ResponseEntity<Map<String, String>> unregisterServer(@PathVariable String serverName) {
		serverManagerService.unregisterServer(serverName);
		return ResponseEntity.ok(Map.of("message", "服务器已注销", "serverName", serverName));
	}
	
	/**
	 * 调用 MCP 工具
	 */
	@PostMapping("/tools/call")
	public ResponseEntity<Map<String, Object>> callTool(@Valid @RequestBody McpToolRequest request) {
		Map<String, Object> result = toolService.callTool(request);
		return ResponseEntity.ok(result);
	}
	
	/**
	 * 列出服务器可用的工具
	 */
	@GetMapping("/servers/{serverName}/tools")
	public ResponseEntity<Map<String, Object>> listTools(@PathVariable String serverName) {
		Map<String, Object> tools = toolService.listTools(serverName);
		return ResponseEntity.ok(tools);
	}
	
	/**
	 * 获取资源
	 */
	@PostMapping("/resources/get")
	public ResponseEntity<Map<String, Object>> getResource(@Valid @RequestBody McpResourceRequest request) {
		Map<String, Object> resource = resourceService.getResource(request);
		return ResponseEntity.ok(resource);
	}
	
	/**
	 * 列出服务器可用的资源
	 */
	@GetMapping("/servers/{serverName}/resources")
	public ResponseEntity<Map<String, Object>> listResources(@PathVariable String serverName) {
		Map<String, Object> resources = resourceService.listResources(serverName);
		return ResponseEntity.ok(resources);
	}
	
	/**
	 * 健康检查
	 */
	@GetMapping("/health")
	public ResponseEntity<Map<String, String>> health() {
		return ResponseEntity.ok(Map.of("status", "ok", "service", ApiConstants.SERVICE_NAME));
	}

	// ====================== Demo：本地 stdio MCP 调用（filesystem） ======================

	/**
	 * Demo1：本地 stdio 模式 - 列出 filesystem Server 提供的工具
	 *
	 * 使用前置条件：
	 * 1. 本机已安装 Node.js / npx；
	 * 2. 能在命令行执行：npx -y @modelcontextprotocol/server-filesystem /tmp；
	 * 3. application.yml 中已启动 pet-vet-mcp 服务；
	 * 4. mcp-servers.json 中已配置 filesystem 服务器（默认已配置）。
	 *
	 * 注意：
	 * - 为了不影响现有业务，此接口仅作为 Demo 使用，路径单独放在 /demo 下。
	 */
	@GetMapping("/demo/stdio/filesystem/tools")
	public ResponseEntity<Map<String, Object>> demoListFilesystemTools() {
		Map<String, Object> tools = toolService.listTools("filesystem");
		return ResponseEntity.ok(tools);
	}

	/**
	 * Demo1：本地 stdio 模式 - 调用 filesystem 的第一个工具
	 *
	 * 说明：
	 * - 为了保证 Demo 简单，不强绑定具体工具名，而是从工具列表中取第一个；
	 * - 真实业务场景建议先调用 /demo/stdio/filesystem/tools，由前端选择具体工具与参数。
	 *
	 * @param args 透传给 MCP 工具的参数（JSON 对象，可为空）
	 */
	@PostMapping("/demo/stdio/filesystem/call-first-tool")
	public ResponseEntity<Map<String, Object>> demoCallFirstFilesystemTool(
		@RequestBody(required = false) Map<String, Object> args) {

		// 1. 先获取工具列表
		Map<String, Object> toolsResult = toolService.listTools("filesystem");
		Object toolsObj = toolsResult.get("tools");

		if (!(toolsObj instanceof java.util.List<?> tools)) {
			return ResponseEntity.badRequest().body(Map.of(
				"error", "tools 列表格式不符合预期",
				"rawTools", toolsObj
			));
		}
		if (tools.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of(
				"error", "filesystem 服务器没有公开任何工具"
			));
		}

		Object first = tools.get(0);
		if (!(first instanceof java.util.Map<?, ?> firstToolRaw)) {
			return ResponseEntity.badRequest().body(Map.of(
				"error", "工具元素格式不符合预期",
				"rawTool", first
			));
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> firstTool = (Map<String, Object>) firstToolRaw;
		Object nameObj = firstTool.get("name");
		if (!(nameObj instanceof String toolName)) {
			return ResponseEntity.badRequest().body(Map.of(
				"error", "工具缺少 name 字段",
				"rawTool", firstTool
			));
		}

		// 2. 构造工具调用请求；arguments 使用调用方传入的 JSON（可为空）
		McpToolRequest request = McpToolRequest.builder()
			.serverName("filesystem")
			.toolName(toolName)
			.arguments(args == null ? Map.of() : args)
			.build();

		// 3. 复用通用工具调用服务
		Map<String, Object> result = toolService.callTool(request);
		return ResponseEntity.ok(result);
	}
}
