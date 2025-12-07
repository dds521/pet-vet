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
}
