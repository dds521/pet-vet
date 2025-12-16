# PetVet MCP 服务模块

## 模块概述

`pet-vet-mcp` 是 PetVet 平台的 MCP (Model Context Protocol) 工具服务模块，提供 MCP 服务器管理和工具调用能力。支持动态注册 MCP 服务器，调用各种 MCP 工具，为 RAG 服务提供扩展能力。

## 模块结构

```
pet-vet-mcp/
├── pet-vet-mcp-api/                # API接口定义模块
│   └── src/main/java/com/petvet/mcp/api/
│       ├── constants/              # API常量
│       ├── dto/                    # 数据传输对象
│       └── enums/                 # 枚举类型
│
└── pet-vet-mcp-service/            # 服务实现模块
    └── src/main/java/com/petvet/mcp/app/
        ├── config/                # 配置类
        ├── controller/             # REST控制器
        └── service/                 # 业务服务
```

## 核心功能

### 1. MCP服务器管理
- **McpServerManagerService**: MCP服务器注册和管理
- **McpServerInitializer**: 服务器初始化配置
- 支持动态注册MCP服务器
- 支持服务器状态管理

### 2. MCP工具调用
- **McpToolService**: MCP工具调用服务
- **McpClientManager**: MCP客户端管理
- 支持调用各种MCP工具
- 支持工具参数传递和结果返回

### 3. MCP资源服务
- **McpResourceService**: MCP资源服务
- 支持获取MCP资源
- 支持资源列表查询

### 4. 客户端管理
- **McpClientManager**: MCP客户端连接管理
- 支持同步客户端 (McpSyncClient)
- 支持连接状态管理
- 支持自动重连

## 技术栈

- **框架**: Spring Boot 3.3.5
- **MCP协议**: Model Context Protocol
- **MCP客户端**: MCP Java SDK
- **服务注册**: Nacos Discovery
- **配置中心**: Nacos Config

## 已配置的MCP服务器

| 服务器名称 | 类型 | 说明 |
|-----------|------|------|
| filesystem | 文件系统 | 文件系统操作工具（开源） |
| trendradar | 趋势分析 | 趋势分析工具（开源） |
| 自定义MCP | 业务工具 | 可扩展的业务工具 |

## API接口

### 服务器管理
- `POST /api/mcp/servers/register`: 注册MCP服务器
- `GET /api/mcp/servers`: 获取所有服务器列表
- `GET /api/mcp/servers/{serverName}`: 获取服务器信息
- `DELETE /api/mcp/servers/{serverName}`: 删除服务器

### 工具调用
- `POST /api/mcp/tools/call`: 调用MCP工具
- `GET /api/mcp/servers/{serverName}/tools`: 列出服务器工具
- `GET /api/mcp/tools/{toolName}`: 获取工具信息

### 资源服务
- `POST /api/mcp/resources/get`: 获取资源
- `GET /api/mcp/servers/{serverName}/resources`: 列出服务器资源

## 配置说明

### MCP服务器配置
MCP服务器配置位于 `resources/mcp-servers.json`，格式如下：

```json
{
  "servers": [
    {
      "name": "filesystem",
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path/to/allowed/files"]
    }
  ]
}
```

### Nacos配置
- `pet-vet-mcp-common.yml`: 通用配置
- `pet-vet-mcp-dev.yml`: 开发环境配置

## 数据流转

1. **工具调用请求** → McpToolService
2. **服务器管理** → McpServerManagerService
3. **客户端连接** → McpClientManager → MCP Server
4. **工具执行** → MCP Server → 返回结果
5. **结果返回** → 格式化响应 → 返回给调用方

## 使用示例

### 注册MCP服务器
```bash
POST /api/mcp/servers/register
{
  "name": "filesystem",
  "type": "stdio",
  "command": "npx",
  "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path"]
}
```

### 调用MCP工具
```bash
POST /api/mcp/tools/call
{
  "serverName": "filesystem",
  "toolName": "read_file",
  "arguments": {
    "path": "/path/to/file.txt"
  }
}
```

## 启动说明

1. 确保Nacos服务已启动
2. 配置MCP服务器信息（`mcp-servers.json`）
3. 确保MCP服务器可执行文件已安装
4. 运行 `PetVetMcpApplication` 主类

## 注意事项

- MCP服务器需要支持stdio或HTTP协议
- 需要确保MCP服务器可执行文件在PATH中
- 客户端连接会自动管理，支持重连
- 工具调用是同步的，注意超时设置
- 资源服务需要MCP服务器支持资源协议
