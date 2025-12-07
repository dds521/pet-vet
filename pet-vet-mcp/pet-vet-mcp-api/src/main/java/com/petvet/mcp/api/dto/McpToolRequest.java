package com.petvet.mcp.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * MCP 工具调用请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpToolRequest {
	
	/**
	 * MCP 服务器名称
	 */
	@NotBlank(message = "服务器名称不能为空")
	private String serverName;
	
	/**
	 * 工具名称
	 */
	@NotBlank(message = "工具名称不能为空")
	private String toolName;
	
	/**
	 * 工具参数
	 */
	private Map<String, Object> arguments;
}
