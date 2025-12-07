package com.petvet.mcp.api.dto;

import com.petvet.mcp.api.enums.McpServerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * MCP 服务器信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpServerInfo {
	
	/**
	 * 服务器名称（唯一标识）
	 */
	private String name;
	
	/**
	 * 服务器描述
	 */
	private String description;
	
	/**
	 * 服务器状态
	 */
	private McpServerStatus status;
	
	/**
	 * 连接配置
	 */
	private McpConnectionConfig connectionConfig;
	
	/**
	 * 服务器元数据
	 */
	private Map<String, Object> metadata;
	
	/**
	 * 注册时间（时间戳）
	 */
	private Long registeredAt;
	
	/**
	 * 最后连接时间（时间戳）
	 */
	private Long lastConnectedAt;
}
