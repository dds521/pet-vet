# PetVet MCP 模块

## 概述

PetVet MCP 模块是一个用于管理和使用 MCP (Model Context Protocol) 服务器的服务模块。该模块提供了灵活的 MCP 服务器管理能力，支持动态注册、连接、调用 MCP 服务器提供的工具和资源。

## 功能特性

- ✅ **MCP 服务器管理**：支持动态注册、查询、更新和注销 MCP 服务器
- ✅ **工具调用**：提供统一的 API 调用 MCP 服务器提供的工具
- ✅ **资源访问**：支持访问 MCP 服务器提供的资源
- ✅ **多种传输协议**：支持 stdio、HTTP、SSE 等多种传输方式
- ✅ **状态管理**：实时跟踪 MCP 服务器的连接状态
- ✅ **简单集成**：独立模块，不影响现有服务
- ✅ **RESTful API**：提供完整的 REST API 接口

## 模块结构

```
pet-vet-mcp/
├── pet-vet-mcp-api/          # API 模块（接口定义、DTO、常量）
│   └── src/main/java/com/petvet/mcp/api/
│       ├── constants/         # 常量定义
│       ├── enums/             # 枚举类型
│       └── dto/               # 数据传输对象
└── pet-vet-mcp-service/       # 服务模块（业务实现）
    └── src/main/java/com/petvet/mcp/app/
        ├── config/            # 配置类
        ├── controller/        # REST 控制器
        └── service/           # 业务服务
```

## 技术栈

- **Spring Boot 3.3.5**：应用框架
- **Spring AI MCP Client**：MCP 客户端支持
- **Spring Cloud Alibaba**：服务注册与配置管理
- **Java 17**：编程语言

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- Nacos（用于配置管理，可选）

### 2. 配置说明

#### 2.1 应用配置

在 `application.yml` 中配置服务端口和 Nacos：

```yaml
spring:
  application:
    name: pet-vet-mcp
  profiles:
    active: dev
  config:
    import:
      - optional:nacos:pet-vet-mcp-common.yml?group=DEFAULT_GROUP
      - optional:nacos:pet-vet-mcp-${spring.profiles.active}.yml?group=DEFAULT_GROUP

server:
  port: 48083
```

#### 2.2 MCP 服务器配置

在 `mcp-servers.json` 中配置 MCP 服务器：

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/tmp"
      ]
    }
  }
}
```

### 3. 启动服务

```bash
cd pet-vet-mcp/pet-vet-mcp-service
mvn spring-boot:run
```

## API 文档

### 服务器管理

#### 注册 MCP 服务器

```http
POST /api/mcp/servers/register
Content-Type: application/json

{
  "name": "filesystem",
  "description": "文件系统 MCP 服务器",
  "connectionConfig": {
    "transportType": "stdio",
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-filesystem", "/tmp"]
  }
}
```

#### 获取所有服务器

```http
GET /api/mcp/servers
```

#### 获取指定服务器信息

```http
GET /api/mcp/servers/{serverName}
```

#### 更新服务器状态

```http
PUT /api/mcp/servers/{serverName}/status
Content-Type: application/json

{
  "status": "CONNECTED"
}
```

#### 注销服务器

```http
DELETE /api/mcp/servers/{serverName}
```

### 工具调用

#### 调用 MCP 工具

```http
POST /api/mcp/tools/call
Content-Type: application/json

{
  "serverName": "filesystem",
  "toolName": "read_file",
  "arguments": {
    "path": "/tmp/example.txt"
  }
}
```

#### 列出服务器工具

```http
GET /api/mcp/servers/{serverName}/tools
```

### 资源访问

#### 获取资源

```http
POST /api/mcp/resources/get
Content-Type: application/json

{
  "serverName": "filesystem",
  "uri": "file:///tmp/example.txt"
}
```

#### 列出服务器资源

```http
GET /api/mcp/servers/{serverName}/resources
```

### 健康检查

```http
GET /api/mcp/health
```

## 架构设计

### 核心组件

1. **McpServerManagerService**：MCP 服务器管理服务
   - 负责服务器的注册、查询、状态更新和注销
   - 维护服务器信息的内存存储

2. **McpToolService**：MCP 工具调用服务
   - 提供统一的工具调用接口
   - 支持工具列表查询

3. **McpResourceService**：MCP 资源访问服务
   - 提供资源获取和列表查询功能

4. **McpController**：REST API 控制器
   - 提供完整的 RESTful API 接口
   - 参数校验和异常处理

### 数据模型

- **McpServerInfo**：服务器信息
  - 名称、描述、状态
  - 连接配置
  - 元数据

- **McpConnectionConfig**：连接配置
  - 传输类型（stdio/http/sse）
  - 连接参数

- **McpToolRequest**：工具调用请求
  - 服务器名称
  - 工具名称
  - 工具参数

- **McpResourceRequest**：资源请求
  - 服务器名称
  - 资源 URI

### 状态管理

服务器状态枚举：

- `REGISTERED`：已注册但未连接
- `CONNECTED`：已连接
- `CONNECTION_FAILED`：连接失败
- `DISCONNECTED`：已断开
- `ERROR`：错误状态

## 使用示例

### 示例 1：注册并调用文件系统 MCP 服务器

```bash
# 1. 注册服务器
curl -X POST http://localhost:48083/api/mcp/servers/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "filesystem",
    "description": "文件系统 MCP 服务器",
    "connectionConfig": {
      "transportType": "stdio",
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/tmp"]
    }
  }'

# 2. 调用工具
curl -X POST http://localhost:48083/api/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "serverName": "filesystem",
    "toolName": "read_file",
    "arguments": {
      "path": "/tmp/example.txt"
    }
  }'
```

### 示例 2：查询服务器信息

```bash
# 获取所有服务器
curl http://localhost:48083/api/mcp/servers

# 获取指定服务器
curl http://localhost:48083/api/mcp/servers/filesystem

# 列出服务器工具
curl http://localhost:48083/api/mcp/servers/filesystem/tools
```

## 集成说明

### 与其他模块的集成

PetVet MCP 模块是一个独立模块，不依赖其他业务模块，可以：

1. **独立部署**：作为独立服务运行
2. **被其他模块调用**：通过 REST API 或 Feign 客户端调用
3. **不影响现有模块**：完全独立的代码和配置

### 依赖关系

```
pet-vet-mcp-service
  └── pet-vet-mcp-api (内部依赖)
```

不依赖其他 pet-vet 模块，保持模块独立性。

## 配置说明

### Nacos 配置

模块支持从 Nacos 加载配置：

- **公共配置**：`pet-vet-mcp-common.yml`
- **环境配置**：`pet-vet-mcp-{profile}.yml`

### MCP 服务器配置

MCP 服务器配置支持以下传输类型：

1. **stdio**：标准输入输出（进程通信）
   ```json
   {
     "command": "npx",
     "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path"]
   }
   ```

2. **http**：HTTP 传输
   ```json
   {
     "url": "http://localhost:8080/mcp",
     "headers": {
       "Authorization": "Bearer token"
     }
   }
   ```

3. **sse**：Server-Sent Events 传输
   ```json
   {
     "url": "http://localhost:8080/mcp/sse"
   }
   ```

## 开发指南

### 添加新的 MCP 服务器

1. 在 `mcp-servers.json` 中添加服务器配置
2. 通过 API 注册服务器
3. 调用服务器提供的工具和资源

### 扩展功能

- 实现实际的 MCP 客户端连接逻辑
- 添加服务器连接池管理
- 实现工具和资源的缓存机制
- 添加监控和日志记录

## 注意事项

1. **MCP 客户端实现**：当前版本的工具调用和资源访问为模拟实现，需要根据实际需求实现真正的 MCP 客户端调用逻辑。

2. **服务器连接**：服务器注册后需要手动触发连接，或实现自动连接机制。

3. **错误处理**：建议在生产环境中添加更完善的错误处理和重试机制。

4. **安全性**：在生产环境中，建议添加认证和授权机制。

## 版本历史

- **v1.0.0-SNAPSHOT**：初始版本
  - 基础服务器管理功能
  - 工具调用和资源访问 API
  - RESTful API 接口

## 许可证

本项目遵循项目主许可证。

## 联系方式

如有问题或建议，请联系项目维护团队。
