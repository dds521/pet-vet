package com.petvet.mcp.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petvet.mcp.api.dto.McpServerInfo;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 客户端管理器
 * 负责创建和管理 MCP 客户端连接
 */
@Slf4j
@Component
public class McpClientManager {
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * MCP 客户端缓存
	 * Key: 服务器名称, Value: MCP 同步客户端
	 */
	private final Map<String, McpSyncClient> clientCache = new ConcurrentHashMap<>();
	
	/**
	 * 客户端初始化状态
	 * Key: 服务器名称, Value: 是否已初始化
	 */
	private final Map<String, Boolean> clientInitialized = new ConcurrentHashMap<>();
	
	/**
	 * 获取或创建 MCP 客户端
	 *
	 * @param serverInfo 服务器信息
	 * @return MCP 同步客户端
	 */
	public McpSyncClient getOrCreateClient(McpServerInfo serverInfo) {
		String serverName = serverInfo.getName();
		
		// 从缓存获取
		if (clientCache.containsKey(serverName)) {
			return clientCache.get(serverName);
		}
		
		// 创建新客户端
		McpSyncClient client = createClient(serverInfo);
		clientCache.put(serverName, client);
		
		log.info("MCP 客户端已创建: {}", serverName);
		return client;
	}
	
	/**
	 * 创建 MCP 客户端
	 *
	 * @param serverInfo 服务器信息
	 * @return MCP 同步客户端
	 */
	private McpSyncClient createClient(McpServerInfo serverInfo) {
		String transportType = serverInfo.getConnectionConfig() != null 
			? serverInfo.getConnectionConfig().getTransportType() 
			: "stdio";
		
		switch (transportType.toLowerCase()) {
			case "stdio":
				return createStdioClient(serverInfo);
			case "http":
			case "sse":
				// TODO: 实现 HTTP/SSE 客户端
				throw new UnsupportedOperationException("HTTP/SSE 传输方式暂未实现");
			default:
				throw new IllegalArgumentException("不支持的传输类型: " + transportType);
		}
	}
	
	/**
	 * 创建 stdio 类型的 MCP 客户端
	 *
	 * @param serverInfo 服务器信息
	 * @return MCP 同步客户端
	 */
	private McpSyncClient createStdioClient(McpServerInfo serverInfo) {
		var connectionConfig = serverInfo.getConnectionConfig();
		
		if (connectionConfig == null || connectionConfig.getCommand() == null) {
			throw new IllegalArgumentException("stdio 传输类型需要 command 配置");
		}
		
		String command = connectionConfig.getCommand();
		java.util.List<String> args = connectionConfig.getArgs() != null 
			? connectionConfig.getArgs() 
			: java.util.Collections.emptyList();
		
		// 解析命令路径（如果命令是 npx/node 等，尝试找到完整路径）
		String resolvedCommand = resolveCommandPath(command);
		
		log.info("创建 stdio MCP 客户端: command={}, resolvedCommand={}, args={}", 
			command, resolvedCommand, args);
		
		// 检查命令是否可用（仅警告，不阻止创建）
		checkCommandAvailable(resolvedCommand);
		
		try {
			// 构建服务器参数
			ServerParameters.Builder paramsBuilder = ServerParameters.builder(resolvedCommand);
			
			if (args != null && !args.isEmpty()) {
				// args() 方法可以接受多个参数（可变参数）
				paramsBuilder.args(args.toArray(new String[0]));
			}
			
			// 创建传输层
			StdioClientTransport transport = new StdioClientTransport(paramsBuilder.build());
			
			// 创建同步客户端（不立即初始化）
			McpSyncClient client = McpClient.sync(transport)
				.requestTimeout(java.time.Duration.ofSeconds(30))
				.build();
			
			// 标记为未初始化（延迟初始化）
			clientInitialized.put(serverInfo.getName(), false);
			log.info("MCP 客户端已创建（延迟初始化）: {}", serverInfo.getName());
			
			return client;
			
		} catch (Exception e) {
			log.error("MCP 客户端初始化失败: {}", serverInfo.getName(), e);
			throw new RuntimeException("MCP 客户端初始化失败: " + e.getMessage(), e);
		}
	}
	
	/**
	 * 从配置文件加载服务器配置
	 *
	 * @return 服务器配置 Map
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> loadServerConfigs() {
		try {
			ClassPathResource resource = new ClassPathResource("mcp-servers.json");
			Map<String, Object> config = objectMapper.readValue(
				resource.getInputStream(), 
				Map.class
			);
			return (Map<String, Object>) config.get("mcpServers");
		} catch (IOException e) {
			log.error("加载 MCP 服务器配置失败", e);
			return java.util.Collections.emptyMap();
		}
	}
	
	/**
	 * 关闭客户端
	 *
	 * @param serverName 服务器名称
	 */
	public void closeClient(String serverName) {
		McpSyncClient client = clientCache.remove(serverName);
		if (client != null) {
			try {
				client.closeGracefully();
				log.info("MCP 客户端已关闭: {}", serverName);
			} catch (Exception e) {
				log.error("关闭 MCP 客户端失败: {}", serverName, e);
			}
		}
	}
	
	/**
	 * 关闭所有客户端
	 */
	public void closeAll() {
		clientCache.keySet().forEach(this::closeClient);
	}
	
	/**
	 * 解析命令路径
	 * 如果命令是 npx/node 等，尝试找到完整路径
	 * 这对于通过 nvm 安装的 Node.js 特别重要
	 *
	 * @param command 命令名称
	 * @return 解析后的命令路径
	 */
	private String resolveCommandPath(String command) {
		// 如果已经是绝对路径，直接返回
		if (command.startsWith("/")) {
			return command;
		}
		
		// 尝试通过 which/where 命令找到完整路径
		try {
			String[] findCommand = isWindows() 
				? new String[]{"where", command}
				: new String[]{"which", command};
			
			ProcessBuilder pb = new ProcessBuilder(findCommand);
			pb.redirectErrorStream(true);
			Process process = pb.start();
			
			try (java.io.BufferedReader reader = new java.io.BufferedReader(
					new java.io.InputStreamReader(process.getInputStream()))) {
				
				boolean finished = process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
				if (!finished) {
					process.destroy();
					log.debug("查找命令 {} 路径超时，使用原始命令", command);
					return command;
				}
				
				if (process.exitValue() == 0) {
					String path = reader.readLine();
					if (path != null && !path.trim().isEmpty()) {
						log.debug("解析命令路径: {} -> {}", command, path);
						return path.trim();
					}
				}
			}
		} catch (Exception e) {
			log.debug("无法解析命令 {} 的路径: {}，使用原始命令", command, e.getMessage());
		}
		
		// 如果找不到，尝试常见的 nvm 路径（Mac/Linux）
		if ("npx".equals(command) || "node".equals(command)) {
			String homeDir = System.getProperty("user.home");
			
			// 首先尝试读取 nvm 的当前版本（通过 .nvmrc 或默认版本）
			String nvmCurrentPath = findNvmCurrentVersion(homeDir, command);
			if (nvmCurrentPath != null) {
				return nvmCurrentPath;
			}
			
			// 如果找不到当前版本，尝试常见的版本路径
			String[] commonPaths = {
				homeDir + "/.nvm/versions/node/v22.16.0/bin/" + command,
				homeDir + "/.nvm/versions/node/v22.0.0/bin/" + command,
				homeDir + "/.nvm/versions/node/v20.0.0/bin/" + command,
				homeDir + "/.nvm/versions/node/v18.0.0/bin/" + command,
				"/usr/local/bin/" + command,
				"/opt/homebrew/bin/" + command,
			};
			
			for (String path : commonPaths) {
				java.io.File file = new java.io.File(path);
				if (file.exists() && file.canExecute()) {
					log.info("找到命令 {} 在常见路径: {}", command, path);
					return path;
				}
			}
		}
		
		// 如果都找不到，返回原始命令（让系统尝试在 PATH 中查找）
		return command;
	}
	
	/**
	 * 查找 nvm 的当前版本路径
	 * 尝试读取 .nvmrc 文件或查找最新的版本
	 *
	 * @param homeDir 用户主目录
	 * @param command 命令名称
	 * @return 命令的完整路径，如果找不到返回 null
	 */
	private String findNvmCurrentVersion(String homeDir, String command) {
		try {
			// 方法1: 尝试读取项目根目录的 .nvmrc 文件
			String projectDir = System.getProperty("user.dir");
			java.io.File nvmrcFile = new java.io.File(projectDir, ".nvmrc");
			if (nvmrcFile.exists()) {
				try (java.io.BufferedReader reader = new java.io.BufferedReader(
						new java.io.FileReader(nvmrcFile))) {
					String version = reader.readLine();
					if (version != null) {
						version = version.trim();
						String path = homeDir + "/.nvm/versions/node/" + version + "/bin/" + command;
						java.io.File file = new java.io.File(path);
						if (file.exists() && file.canExecute()) {
							log.info("从 .nvmrc 找到命令 {}: {}", command, path);
							return path;
						}
					}
				}
			}
			
			// 方法2: 查找 nvm 目录下最新的版本
			java.io.File nvmVersionsDir = new java.io.File(homeDir, ".nvm/versions/node");
			if (nvmVersionsDir.exists() && nvmVersionsDir.isDirectory()) {
				java.io.File[] versionDirs = nvmVersionsDir.listFiles();
				if (versionDirs != null) {
					// 按版本号排序，取最新的
					java.util.Arrays.sort(versionDirs, (a, b) -> b.getName().compareTo(a.getName()));
					for (java.io.File versionDir : versionDirs) {
						String path = versionDir.getAbsolutePath() + "/bin/" + command;
						java.io.File file = new java.io.File(path);
						if (file.exists() && file.canExecute()) {
							log.info("找到 nvm 最新版本命令 {}: {}", command, path);
							return path;
						}
					}
				}
			}
		} catch (Exception e) {
			log.debug("查找 nvm 当前版本时出错: {}", e.getMessage());
		}
		
		return null;
	}
	
	/**
	 * 检查是否为 Windows 系统
	 *
	 * @return 是否为 Windows
	 */
	private boolean isWindows() {
		return System.getProperty("os.name", "").toLowerCase().contains("windows");
	}
	
	/**
	 * 检查命令是否可用（非阻塞，仅记录警告）
	 *
	 * @param command 命令路径
	 */
	private void checkCommandAvailable(String command) {
		// 异步检查，不阻塞启动
		new Thread(() -> {
			try {
				ProcessBuilder pb = new ProcessBuilder(command, "--version");
				pb.redirectErrorStream(true);
				pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
				Process process = pb.start();
				boolean finished = process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
				if (!finished) {
					process.destroy();
					log.warn("命令 {} 检查超时，可能不可用", command);
				} else if (process.exitValue() != 0) {
					log.warn("命令 {} 可能不可用（退出码: {}）", command, process.exitValue());
				} else {
					log.debug("命令 {} 可用", command);
				}
			} catch (java.io.IOException e) {
				log.warn("命令 {} 不可用: {}。请确保已安装并在 PATH 中", command, e.getMessage());
				log.warn("提示：对于 TrendRadar，需要安装 Node.js 和 npx");
			} catch (Exception e) {
				log.debug("检查命令 {} 时出错: {}", command, e.getMessage());
			}
		}, "mcp-command-check-" + command).start();
	}
	
	/**
	 * 确保客户端已初始化（延迟初始化）
	 * 使用同步锁确保只初始化一次
	 *
	 * @param client 客户端
	 * @param serverName 服务器名称
	 */
	public void ensureInitialized(McpSyncClient client, String serverName) {
		// 检查是否已初始化
		if (Boolean.TRUE.equals(clientInitialized.get(serverName))) {
			return;
		}
		
		// 同步初始化，确保只初始化一次
		synchronized (this) {
			// 双重检查
			if (Boolean.TRUE.equals(clientInitialized.get(serverName))) {
				return;
			}
			
			log.info("初始化 MCP 客户端: {}", serverName);
			try {
				client.initialize();
				clientInitialized.put(serverName, true);
				log.info("MCP 客户端初始化成功: {}", serverName);
			} catch (Exception e) {
				log.error("MCP 客户端初始化失败: {}", serverName, e);
				// 检查是否是 npx 不可用的问题
				if (e.getMessage() != null && e.getMessage().contains("Cannot run program")) {
					throw new RuntimeException(
						"MCP 客户端初始化失败: 无法执行命令。请确保已安装 Node.js 和 npx，并且 npx 在系统 PATH 中。\n" +
						"错误详情: " + e.getMessage(), e);
				}
				throw new RuntimeException("MCP 客户端初始化失败: " + e.getMessage(), e);
			}
		}
	}
}
