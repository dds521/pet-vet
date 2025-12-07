package com.petvet.mcp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP 连接配置 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpConnectionConfig {
	
	/**
	 * 连接类型：stdio, http, sse
	 */
	private String transportType;
	
	/**
	 * 命令（用于 stdio 类型）
	 */
	private String command;
	
	/**
	 * 命令参数（用于 stdio 类型）
	 */
	private List<String> args;
	
	/**
	 * HTTP URL（用于 http/sse 类型）
	 */
	private String url;
	
	/**
	 * HTTP 请求头
	 */
	private Map<String, String> headers;
	
	/**
	 * 超时时间（毫秒）
	 */
	private Long timeout;
	
	/**
	 * 其他配置项
	 */
	private Map<String, Object> additionalConfig;
}
