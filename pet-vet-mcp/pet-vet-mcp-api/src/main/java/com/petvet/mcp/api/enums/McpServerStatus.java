package com.petvet.mcp.api.enums;

/**
 * MCP 服务器状态枚举
 */
public enum McpServerStatus {
	
	/**
	 * 已注册但未连接
	 */
	REGISTERED,
	
	/**
	 * 已连接
	 */
	CONNECTED,
	
	/**
	 * 连接失败
	 */
	CONNECTION_FAILED,
	
	/**
	 * 已断开
	 */
	DISCONNECTED,
	
	/**
	 * 错误状态
	 */
	ERROR
}
