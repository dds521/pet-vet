package com.petvet.mcp.app.service;

import com.petvet.mcp.api.dto.McpConnectionConfig;
import com.petvet.mcp.api.dto.McpServerInfo;
import com.petvet.mcp.api.enums.McpServerStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 服务器管理服务
 * 负责注册、连接、管理和调用 MCP 服务器
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpServerManagerService {
	
	/**
	 * 已注册的 MCP 服务器信息
	 * Key: 服务器名称, Value: 服务器信息
	 */
	private final Map<String, McpServerInfo> registeredServers = new ConcurrentHashMap<>();
	
	/**
	 * 注册 MCP 服务器
	 *
	 * @param serverInfo 服务器信息
	 * @return 注册结果
	 */
	public McpServerInfo registerServer(McpServerInfo serverInfo) {
		if (serverInfo.getName() == null || serverInfo.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("服务器名称不能为空");
		}
		
		if (registeredServers.containsKey(serverInfo.getName())) {
			throw new IllegalStateException("服务器 " + serverInfo.getName() + " 已存在");
		}
		
		serverInfo.setStatus(McpServerStatus.REGISTERED);
		serverInfo.setRegisteredAt(System.currentTimeMillis());
		
		registeredServers.put(serverInfo.getName(), serverInfo);
		log.info("MCP 服务器已注册: {}", serverInfo.getName());
		
		return serverInfo;
	}
	
	/**
	 * 获取所有已注册的服务器
	 *
	 * @return 服务器列表
	 */
	public List<McpServerInfo> getAllServers() {
		return new ArrayList<>(registeredServers.values());
	}
	
	/**
	 * 根据名称获取服务器信息
	 *
	 * @param serverName 服务器名称
	 * @return 服务器信息
	 */
	public McpServerInfo getServer(String serverName) {
		McpServerInfo serverInfo = registeredServers.get(serverName);
		if (serverInfo == null) {
			throw new IllegalArgumentException("服务器 " + serverName + " 不存在");
		}
		return serverInfo;
	}
	
	/**
	 * 更新服务器状态
	 *
	 * @param serverName 服务器名称
	 * @param status     新状态
	 */
	public void updateServerStatus(String serverName, McpServerStatus status) {
		McpServerInfo serverInfo = registeredServers.get(serverName);
		if (serverInfo == null) {
			throw new IllegalArgumentException("服务器 " + serverName + " 不存在");
		}
		
		serverInfo.setStatus(status);
		if (status == McpServerStatus.CONNECTED) {
			serverInfo.setLastConnectedAt(System.currentTimeMillis());
		}
		
		log.debug("服务器 {} 状态已更新为: {}", serverName, status);
	}
	
	/**
	 * 删除服务器
	 *
	 * @param serverName 服务器名称
	 */
	public void unregisterServer(String serverName) {
		McpServerInfo removed = registeredServers.remove(serverName);
		if (removed != null) {
			log.info("MCP 服务器已注销: {}", serverName);
		} else {
			throw new IllegalArgumentException("服务器 " + serverName + " 不存在");
		}
	}
	
	/**
	 * 检查服务器是否存在
	 *
	 * @param serverName 服务器名称
	 * @return 是否存在
	 */
	public boolean serverExists(String serverName) {
		return registeredServers.containsKey(serverName);
	}
}
