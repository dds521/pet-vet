package com.petvet.mcp.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP 资源请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpResourceRequest {
	
	/**
	 * MCP 服务器名称
	 */
	@NotBlank(message = "服务器名称不能为空")
	private String serverName;
	
	/**
	 * 资源 URI
	 */
	@NotBlank(message = "资源 URI 不能为空")
	private String uri;
}
