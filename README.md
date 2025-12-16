# PetVet 宠物医疗平台

## 项目概述

PetVet 是一个基于 Spring Cloud 微服务架构的宠物医疗平台，采用 RAG (Retrieval-Augmented Generation) 技术增强的智能问答系统。平台提供向量化、检索增强生成、工具调用等完整的 AI 能力支持。

## 系统架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                     上游业务层                                │
│              PC端、小程序                                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    业务服务层                                 │
│                  (pet-vet-ai)                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ 用户认证服务   │  │ 地址匹配服务  │  │ 宠物信息管理  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│  ┌──────────────┐  ┌──────────────┐                        │
│  │ 订单管理      │  │ RAG查询服务   │                        │
│  └──────────────┘  └──────────────┘                        │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  RAG 增强检索层                               │
│                  (pet-vet-rag)                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ 查询分类器    │  │ 记忆管理服务   │  │ LLM生成服务   │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└──────────────────────┬──────────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐
│ 向量化服务层   │ │ 工具服务层    │
│pet-vet-embedding│ │ pet-vet-mcp  │
└──────────────┘ └──────────────┘
```

### 服务分层

1. **业务服务层 (pet-vet-ai)**: 提供宠物医疗相关的业务功能，接收PC端和小程序的请求
2. **RAG 增强检索层 (pet-vet-rag)**: 提供智能查询分类、向量检索、LLM生成，由pet-vet-ai调用
3. **向量化服务层 (pet-vet-embedding)**: 提供文本向量化和向量数据库管理，由pet-vet-rag调用
4. **工具服务层 (pet-vet-mcp)**: 提供 MCP 工具调用能力，由pet-vet-rag调用
│pet-vet-embedding│ │ pet-vet-mcp  │ │ pet-vet-ai   │
└──────────────┘ └──────────────┘ └──────────────┘
```

### 服务分层

1. **业务服务层 (pet-vet-ai)**: 提供宠物医疗相关的业务功能
2. **RAG 增强检索层 (pet-vet-rag)**: 提供智能查询分类、向量检索、LLM生成
3. **向量化服务层 (pet-vet-embedding)**: 提供文本向量化和向量数据库管理
4. **工具服务层 (pet-vet-mcp)**: 提供 MCP 工具调用能力

## 模块说明

### 1. pet-vet-ai (业务服务模块)

**职责**: 提供宠物医疗相关的核心业务功能

**主要功能**:
- 用户认证服务（微信登录）
- 地址匹配服务
- 宠物信息管理
- 订单管理
- 诊断记录管理
- 消息队列处理
- 分布式事务管理

**技术栈**:
- Spring Boot 3.3.5
- Nacos (服务注册/配置中心)
- MySQL + MyBatis Plus
- Redis
- RocketMQ
- Seata (分布式事务)
- Sentinel (流控)

**详细文档**: [pet-vet-ai/README.md](pet-vet-ai/README.md)

### 2. pet-vet-rag (RAG 增强检索模块)

**职责**: 提供智能查询分类、向量检索、LLM生成、对话记忆管理

**主要功能**:
- 智能查询分类（规则引擎 + 缓存 + 兜底）
- 向量检索集成
- LLM 答案生成
- 对话记忆管理
- 历史记录管理

**核心组件**:
- **QueryClassifier**: 查询分类器，判断是否需要检索知识库
- **RuleEngine**: QLExpress 4.0.4 规则引擎，支持配置化规则
- **RagValidationService**: 核心业务服务，整合检索、生成、记忆管理
- **MemoryService**: 对话记忆管理，维护用户会话上下文
- **HistoryService**: 历史记录管理，持久化查询历史

**支持的 LLM**:
- DeepSeek (默认): `deepseek-chat` - 价格便宜，国内访问稳定
- OpenAI: `gpt-4o` - 性能优秀
- xAI Grok: `grok-4-latest` - 性能优秀

**技术栈**:
- Spring Boot 3.3.5
- LangChain4j
- QLExpress 4.0.4
- Feign (服务调用)
- Redis (记忆存储)
- MySQL (历史存储)

**详细文档**: [pet-vet-rag/README.md](pet-vet-rag/README.md)

### 3. pet-vet-embedding (向量化服务模块)

**职责**: 提供文本向量化和向量数据库管理能力

**主要功能**:
- 文本向量化（支持多种模型）
- 向量存储和检索
- 简历解析和向量化
- 文本分块处理

**支持的向量模型**:
- **Cohere** (默认): `embed-english-v3.0` (1024维) - 有免费额度，性能好
- **Hugging Face**: `BAAI/bge-small-zh-v1.5` (384维) - 完全免费，推荐中文场景
- **OpenAI**: `text-embedding-3-small` (1536维) - 需要 API Key，性能优秀
- **Ollama**: `nomic-embed-text` (768维) - 完全免费，本地运行

**支持的向量数据库**:
- **Zilliz** (默认): Serverless 云服务，支持 TLS 连接
- **Qdrant**: Cloud 云服务，可作为备选方案

**技术栈**:
- Spring Boot 3.3.5
- LangChain4j
- PDFBox 3.x (PDF解析)
- MySQL + MyBatis Plus

**详细文档**: [pet-vet-embedding/README.md](pet-vet-embedding/README.md)

### 4. pet-vet-mcp (MCP 工具服务模块)

**职责**: 提供 MCP (Model Context Protocol) 工具调用能力

**主要功能**:
- MCP 服务器注册和管理
- MCP 工具调用
- MCP 资源服务
- 客户端连接管理

**已配置的 MCP 服务器**:
- **filesystem**: 文件系统操作工具（开源）
- **trendradar**: 趋势分析工具（开源）
- **自定义 MCP**: 可扩展的业务工具

**技术栈**:
- Spring Boot 3.3.5
- MCP Java SDK
- Nacos (服务注册/配置中心)

**详细文档**: [pet-vet-mcp/README.md](pet-vet-mcp/README.md)

## 数据流转

### RAG 查询完整流程

```
1. PC端/小程序发送查询请求
   ↓
2. pet-vet-ai 接收请求并调用RAG服务
   ↓
3. pet-vet-rag 加载对话记忆 (Redis)
   ↓
4. 查询分类器判断是否需要检索
   ├─ 规则层策略 (QLExpress规则引擎)
   ├─ 缓存层策略 (本地/Redis缓存)
   └─ 兜底策略
   ↓
5. 需要检索 → 调用 pet-vet-embedding 向量检索
   ├─ 查询文本向量化
   ├─ 向量相似度搜索 (Zilliz/Qdrant)
   └─ 返回相似文档
   ↓
6. 检查检索质量
   ├─ 质量满足 → RAG模式生成 (知识库+历史对话)
   └─ 质量不满足 → 纯LLM模式生成 (仅历史对话)
   ↓
7. LLM生成答案 (DeepSeek/OpenAI/Grok)
   ↓
8. 更新对话记忆 (Redis)
   ↓
9. 异步保存历史记录 (MySQL)
   ↓
10. 返回给pet-vet-ai → 返回给PC端/小程序
```

### 向量化存储流程

```
1. 文本输入 → EmbeddingService
   ↓
2. 向量化 → EmbeddingModel
   ├─ Cohere API
   ├─ Hugging Face (本地)
   ├─ OpenAI API
   └─ Ollama (本地)
   ↓
3. 向量存储 → EmbeddingStore
   ├─ Zilliz (gRPC)
   └─ Qdrant (HTTP/gRPC)
   ↓
4. 返回存储ID
```

### MCP 工具调用流程

```
1. 工具调用请求 → McpToolService
   ↓
2. 获取服务器信息 → McpServerManagerService
   ↓
3. 获取或创建客户端 → McpClientManager
   ↓
4. 建立连接 → MCP Server
   ↓
5. 发送工具调用请求
   ↓
6. MCP Server 执行工具逻辑
   ↓
7. 返回执行结果
   ↓
8. 格式化响应 → 返回给调用方
```

## 技术栈总览

### 基础框架
- **Spring Boot**: 3.3.5
- **Spring Cloud**: 2023.0.1
- **Spring Cloud Alibaba**: 2023.0.1.2
- **Java**: 17

### 服务治理
- **Nacos**: 服务注册与发现、配置中心
- **Feign**: 服务间调用
- **Sentinel**: 流控和熔断

### 数据存储
- **MySQL**: 关系型数据库
- **MyBatis Plus**: ORM框架
- **Redis**: 缓存和对话记忆

### AI能力
- **LangChain4j**: Java LLM集成库
- **QLExpress 4.0.4**: 规则引擎
- **向量模型**: Cohere / Hugging Face / OpenAI / Ollama
- **向量数据库**: Zilliz / Qdrant
- **LLM模型**: DeepSeek / OpenAI / xAI Grok

### 其他
- **RocketMQ**: 消息队列
- **Seata**: 分布式事务
- **PDFBox**: PDF解析
- **MCP SDK**: MCP工具调用

## 服务接口

### pet-vet-ai 服务接口
- `GET /api/address/match`: 地址匹配
- `GET /api/pet/{id}`: 获取宠物信息
- `POST /api/order`: 创建订单
- `POST /api/rag/query`: RAG查询接口
- `POST /api/wechat/auth`: 微信授权

### pet-vet-rag 服务接口
- `POST /api/rag/query`: 基础RAG查询
- `POST /api/rag/validate`: RAG验证查询（包含记忆管理）
- `GET /api/rag/health`: 健康检查

### pet-vet-embedding 服务接口
- `POST /api/embedding/embed`: 文本向量化
- `POST /api/embedding/search`: 向量相似度搜索
- `POST /api/resume/parse`: 简历解析和向量化
- `POST /api/resume/search`: 简历向量检索

### pet-vet-mcp 服务接口
- `POST /api/mcp/servers/register`: 注册MCP服务器
- `GET /api/mcp/servers`: 获取所有服务器
- `POST /api/mcp/tools/call`: 调用MCP工具
- `GET /api/mcp/servers/{serverName}/tools`: 列出服务器工具
- `POST /api/mcp/resources/get`: 获取资源

## 配置说明

### 环境变量配置

#### 向量模型配置
- `EMBEDDING_MODEL_TYPE`: 向量模型类型
  - `cohere` (默认)
  - `hugging-face`
  - `openai`
  - `ollama`

#### 向量数据库配置
- `VECTOR_DB_TYPE`: 向量数据库类型
  - `zilliz` (默认)
  - `qdrant`

#### LLM模型配置
- `AI_PROVIDER_TYPE`: LLM提供商
  - `deepseek` (默认)
  - `openai`
  - `grok`

### Nacos配置

所有服务的配置都通过Nacos配置中心管理：

- **pet-vet-ai**: `nacos-common-config.yml`, `nacos-dev-config.yml`, `nacos-prod-config.yml`
- **pet-vet-rag**: `pet-vet-rag-common.yml`, `pet-vet-rag-dev.yml`, `pet-vet-rag-prod.yml`
- **pet-vet-embedding**: `pet-vet-embedding-common.yml`, `pet-vet-embedding-dev.yml`
- **pet-vet-mcp**: `pet-vet-mcp-common.yml`, `pet-vet-mcp-dev.yml`

## 快速开始

### 前置要求

1. **Java 17+**
2. **Maven 3.6+**
3. **MySQL 8.0+**
4. **Redis 6.0+**
5. **Nacos 2.0+**
6. **RocketMQ 5.0+** (可选，用于消息队列)

### 启动步骤

1. **启动基础设施**
   ```bash
   # 启动 Nacos
   # 启动 MySQL
   # 启动 Redis
   # 启动 RocketMQ (可选)
   ```

2. **初始化数据库**
   ```bash
   # 执行各模块的SQL脚本
   ```

3. **配置 Nacos**
   ```bash
   # 上传各模块的配置文件到 Nacos
   ```

4. **启动服务** (按依赖顺序)
   ```bash
   # 1. 启动 pet-vet-embedding
   cd pet-vet-embedding/pet-vet-embedding-service
   mvn spring-boot:run
   
   # 2. 启动 pet-vet-mcp
   cd pet-vet-mcp/pet-vet-mcp-service
   mvn spring-boot:run
   
   # 3. 启动 pet-vet-rag
   cd pet-vet-rag/pet-vet-rag-service
   mvn spring-boot:run
   
   # 4. 启动 pet-vet-ai
   cd pet-vet-ai/pet-vet-ai-service
   mvn spring-boot:run
   ```

### 验证服务

```bash
# 检查服务健康状态
curl http://localhost:48083/api/rag/health

# 测试RAG查询
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "什么是犬瘟热？",
    "sessionId": "test-session"
  }'
```

## 项目结构

```
pet-vet/
├── pom.xml                          # 父级POM
├── README.md                        # 项目总体说明（本文件）
├── ARCHITECTURE.html                # 架构设计文档（HTML格式）
│
├── pet-vet-ai/                      # 业务服务模块
│   ├── README.md                    # 模块说明
│   ├── pet-vet-ai-api/             # API接口定义
│   └── pet-vet-ai-service/         # 服务实现
│
├── pet-vet-embedding/               # 向量化服务模块
│   ├── README.md                    # 模块说明
│   ├── pet-vet-embedding-api/      # API接口定义
│   └── pet-vet-embedding-service/   # 服务实现
│
├── pet-vet-mcp/                     # MCP工具服务模块
│   ├── README.md                    # 模块说明
│   ├── pet-vet-mcp-api/            # API接口定义
│   └── pet-vet-mcp-service/         # 服务实现
│
└── pet-vet-rag/                     # RAG增强检索模块
    ├── README.md                    # 模块说明
    ├── pet-vet-rag-api/            # API接口定义
    └── pet-vet-rag-service/         # 服务实现
```

## 架构设计

详细的架构设计文档请参考：[ARCHITECTURE.html](ARCHITECTURE.html)

### 架构特点

1. **分层架构**: 业务层 → RAG层 → 向量化层 → 工具层
2. **微服务架构**: 服务独立部署，通过Feign调用
3. **配置中心**: 统一使用Nacos管理配置
4. **服务治理**: 使用Nacos进行服务注册与发现
5. **智能分类**: 多策略查询分类器，支持规则引擎
6. **记忆管理**: Redis存储对话记忆，支持上下文理解
7. **可扩展性**: 支持多种向量模型、向量数据库、LLM模型

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 统一使用Slf4j进行日志记录

### 注释规范
- 所有类和方法必须包含JavaDoc注释
- 使用 `@author daidasheng` 和 `@date` 标注

### 提交规范
- 使用清晰的提交信息
- 遵循Conventional Commits规范

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证。

## 联系方式

如有问题或建议，请联系项目维护者。

---

**最后更新**: 2024-12-16
