# PetVet 服务架构设计文档

## 1. 系统架构图

### 1.1 整体架构

```mermaid
graph TB
    subgraph "上游业务层"
        A[业务应用] --> B[pet-vet-ai]
    end
    
    subgraph "RAG 增强检索层"
        B --> C[pet-vet-rag]
        C --> D[查询分类器]
        C --> E[记忆管理服务]
        C --> F[历史记录服务]
        C --> G[LLM 生成服务]
    end
    
    subgraph "向量化服务层"
        C --> H[pet-vet-embedding]
        H --> I[Embedding 模型]
        H --> J[向量数据库服务]
    end
    
    subgraph "工具服务层"
        C --> K[pet-vet-mcp]
        K --> L[MCP 服务器管理]
        K --> M[工具调用服务]
        K --> N[资源服务]
    end
    
    subgraph "外部服务"
        I --> O[Cohere API<br/>默认模型]
        I --> P[Hugging Face<br/>免费模型]
        I --> Q[OpenAI API<br/>可选]
        I --> R[Ollama<br/>本地模型]
        
        J --> S[Zilliz<br/>默认向量库]
        J --> T[Qdrant<br/>可选向量库]
        
        G --> U[DeepSeek API<br/>默认LLM]
        G --> V[OpenAI API<br/>可选]
        G --> W[xAI Grok<br/>可选]
        
        L --> X[filesystem MCP]
        L --> Y[trendradar MCP]
        L --> Z[自定义 MCP]
    end
    
    subgraph "数据存储"
        E --> AA[(Redis<br/>对话记忆)]
        F --> BB[(MySQL<br/>历史记录)]
        J --> CC[(向量数据)]
    end
    
    style C fill:#e1f5ff
    style H fill:#fff4e1
    style K fill:#e8f5e9
    style I fill:#f3e5f5
    style J fill:#f3e5f5
    style G fill:#fff9c4
```

### 1.2 服务详细架构

#### 1.2.1 pet-vet-embedding 服务架构

```mermaid
graph LR
    subgraph "pet-vet-embedding 服务"
        A[REST API<br/>Controller] --> B[EmbeddingService<br/>向量化服务]
        A --> C[VectorDatabaseService<br/>向量数据库服务]
        
        B --> D[EmbeddingModel<br/>向量模型]
        C --> D
        C --> E[EmbeddingStore<br/>向量存储接口]
        
        D --> F{模型类型选择}
        F -->|默认| G[Cohere<br/>embed-english-v3.0<br/>1024维]
        F --> H[Hugging Face<br/>bge-small-zh-v1.5<br/>384维]
        F --> I[OpenAI<br/>text-embedding-3-small<br/>1536维]
        F --> J[Ollama<br/>nomic-embed-text<br/>768维]
        
        E --> K{向量数据库选择}
        K -->|默认| L[Zilliz<br/>Serverless]
        K --> M[Qdrant<br/>Cloud]
    end
    
    style B fill:#e1f5ff
    style C fill:#e1f5ff
    style D fill:#fff4e1
    style E fill:#fff4e1
```

**关键组件说明：**

- **EmbeddingService**: 负责文本向量化，支持单文本和批量向量化
- **VectorDatabaseService**: 负责向量存储和检索，提供相似度搜索功能
- **EmbeddingModel**: 向量模型抽象，支持多种模型提供商
- **EmbeddingStore**: 向量存储抽象，支持多种向量数据库

**支持的向量模型：**
- **Cohere** (默认): `embed-english-v3.0` (1024维) - 有免费额度，性能好
- **Hugging Face**: `BAAI/bge-small-zh-v1.5` (384维) - 完全免费，推荐中文场景
- **OpenAI**: `text-embedding-3-small` (1536维) - 需要 API Key，性能优秀
- **Ollama**: `nomic-embed-text` (768维) - 完全免费，本地运行

**支持的向量数据库：**
- **Zilliz** (默认): Serverless 云服务，支持 TLS 连接
- **Qdrant**: Cloud 云服务，可作为备选方案

#### 1.2.2 pet-vet-rag 服务架构

```mermaid
graph TB
    subgraph "pet-vet-rag 服务"
        A[REST API<br/>Controller] --> B[RagValidationService<br/>核心业务服务]
        A --> C[RagService<br/>基础RAG服务]
        
        B --> C
        B --> D[QueryClassifier<br/>查询分类器]
        B --> E[MemoryService<br/>记忆管理]
        B --> F[HistoryService<br/>历史记录]
        B --> G[LangChainConfig<br/>LLM配置]
        
        C --> H[ResumeParseFeignClient<br/>调用embedding服务]
        
        D --> I[HybridQueryClassifier<br/>混合分类器]
        D --> J[规则分类器]
        D --> K[缓存层]
        
        E --> L[(Redis<br/>对话记忆)]
        F --> M[(MySQL<br/>历史记录)]
        
        G --> N{ChatModel选择}
        N -->|默认| O[DeepSeek API]
        N --> P[OpenAI API]
        N --> Q[xAI Grok]
    end
    
    style B fill:#e1f5ff
    style C fill:#e1f5ff
    style D fill:#fff4e1
    style E fill:#fff4e1
    style F fill:#fff4e1
```

**关键组件说明：**

- **RagValidationService**: 核心业务服务，整合检索、生成、记忆管理等功能
- **RagService**: 基础 RAG 服务，提供检索和生成能力
- **QueryClassifier**: 查询分类器，判断是否需要检索知识库
- **MemoryService**: 对话记忆管理，维护用户会话上下文
- **HistoryService**: 历史记录管理，持久化查询历史
- **LangChainConfig**: LLM 配置管理，支持多种大模型

**支持的 LLM 模型：**
- **DeepSeek** (默认): `deepseek-chat` - 价格便宜，国内访问稳定
- **OpenAI**: `gpt-4o` - 性能优秀，需要 API Key
- **xAI Grok**: `grok-4-latest` - 性能优秀，使用 OpenAI 兼容 API

#### 1.2.3 pet-vet-mcp 服务架构

```mermaid
graph TB
    subgraph "pet-vet-mcp 服务"
        A[REST API<br/>Controller] --> B[McpServerManagerService<br/>服务器管理]
        A --> C[McpToolService<br/>工具调用服务]
        A --> D[McpResourceService<br/>资源服务]
        
        B --> E[McpClientManager<br/>客户端管理]
        C --> E
        D --> E
        
        E --> F[MCP 服务器列表]
        
        F --> G[filesystem MCP<br/>文件系统工具]
        F --> H[trendradar MCP<br/>趋势分析工具]
        F --> I[自定义 MCP<br/>业务工具]
        
        E --> J[McpSyncClient<br/>同步客户端]
    end
    
    style B fill:#e8f5e9
    style C fill:#e8f5e9
    style D fill:#e8f5e9
    style E fill:#fff4e1
```

**关键组件说明：**

- **McpServerManagerService**: MCP 服务器注册和管理
- **McpToolService**: MCP 工具调用服务，支持调用各种 MCP 工具
- **McpResourceService**: MCP 资源服务，支持获取 MCP 资源
- **McpClientManager**: MCP 客户端管理，维护与 MCP 服务器的连接

**已配置的 MCP 服务器：**
- **filesystem**: 文件系统操作工具（开源）
- **trendradar**: 趋势分析工具（开源）
- **自定义 MCP**: 可扩展的业务工具

## 2. 数据交互流程图

### 2.1 RAG 查询完整流程

```mermaid
sequenceDiagram
    participant Client as 客户端/业务应用
    participant RAG as pet-vet-rag
    participant Classifier as 查询分类器
    participant Memory as 记忆管理
    participant Embedding as pet-vet-embedding
    participant VectorDB as 向量数据库
    participant LLM as 大语言模型
    participant History as 历史记录
    participant Redis as Redis
    participant MySQL as MySQL
    
    Client->>RAG: 1. 发送查询请求
    RAG->>Memory: 2. 加载对话记忆
    Memory->>Redis: 查询历史对话
    Redis-->>Memory: 返回对话历史
    Memory-->>RAG: 返回记忆对象
    
    RAG->>Classifier: 3. 判断是否需要检索
    Classifier->>Classifier: 规则匹配/混合分类
    Classifier-->>RAG: 返回分类结果
    
    alt 需要检索知识库
        RAG->>Embedding: 4. 调用向量检索接口
        Embedding->>Embedding: 5. 将查询文本向量化
        Embedding->>VectorDB: 6. 向量相似度搜索
        VectorDB-->>Embedding: 返回相似文档
        Embedding-->>RAG: 返回检索结果
        
        RAG->>RAG: 7. 检查检索质量
        alt 检索质量满足要求
            RAG->>LLM: 8. RAG模式生成答案<br/>(知识库+历史对话)
            LLM-->>RAG: 返回生成的答案
        else 检索质量不满足
            RAG->>LLM: 8. 纯LLM模式生成答案<br/>(仅历史对话)
            LLM-->>RAG: 返回生成的答案
        end
    else 不需要检索
        RAG->>LLM: 8. 纯LLM模式生成答案<br/>(仅历史对话)
        LLM-->>RAG: 返回生成的答案
    end
    
    RAG->>Memory: 9. 更新对话记忆
    Memory->>Redis: 保存新对话
    RAG->>History: 10. 异步保存历史记录
    History->>MySQL: 持久化历史数据
    
    RAG-->>Client: 11. 返回完整响应<br/>(答案+检索结果+历史)
```

### 2.2 向量化存储流程

```mermaid
sequenceDiagram
    participant Client as 客户端/业务应用
    participant Embedding as pet-vet-embedding
    participant EmbeddingModel as 向量模型
    participant VectorDB as 向量数据库
    
    Client->>Embedding: 1. 发送文本向量化请求
    Embedding->>EmbeddingModel: 2. 调用向量模型
    EmbeddingModel->>EmbeddingModel: 3. 文本向量化处理
    alt 使用 Cohere
        EmbeddingModel->>Cohere: API调用
        Cohere-->>EmbeddingModel: 返回向量(1024维)
    else 使用 Hugging Face
        EmbeddingModel->>HuggingFace: 本地模型推理
        HuggingFace-->>EmbeddingModel: 返回向量(384维)
    else 使用 OpenAI
        EmbeddingModel->>OpenAI: API调用
        OpenAI-->>EmbeddingModel: 返回向量(1536维)
    else 使用 Ollama
        EmbeddingModel->>Ollama: 本地API调用
        Ollama-->>EmbeddingModel: 返回向量(768维)
    end
    EmbeddingModel-->>Embedding: 返回向量结果
    
    Embedding->>VectorDB: 4. 存储向量
    alt 使用 Zilliz
        Embedding->>Zilliz: gRPC连接
        Zilliz-->>Embedding: 存储成功
    else 使用 Qdrant
        Embedding->>Qdrant: HTTP/gRPC连接
        Qdrant-->>Embedding: 存储成功
    end
    VectorDB-->>Embedding: 返回存储ID
    Embedding-->>Client: 5. 返回向量化结果
```

### 2.3 向量检索流程

```mermaid
sequenceDiagram
    participant RAG as pet-vet-rag
    participant Embedding as pet-vet-embedding
    participant EmbeddingModel as 向量模型
    participant VectorDB as 向量数据库
    
    RAG->>Embedding: 1. 发送检索请求<br/>(查询文本+参数)
    Embedding->>EmbeddingModel: 2. 将查询文本向量化
    EmbeddingModel->>EmbeddingModel: 向量化处理
    EmbeddingModel-->>Embedding: 返回查询向量
    
    Embedding->>VectorDB: 3. 向量相似度搜索
    alt 使用 Zilliz
        Embedding->>Zilliz: gRPC相似度搜索<br/>(maxResults, minScore)
        Zilliz->>Zilliz: 计算余弦相似度
        Zilliz-->>Embedding: 返回Top-K结果<br/>(ID+文本+相似度分数)
    else 使用 Qdrant
        Embedding->>Qdrant: HTTP/gRPC相似度搜索
        Qdrant->>Qdrant: 计算相似度
        Qdrant-->>Embedding: 返回Top-K结果
    end
    VectorDB-->>Embedding: 返回检索结果列表
    
    Embedding->>Embedding: 4. 格式化结果
    Embedding-->>RAG: 5. 返回检索文档列表<br/>(chunkId+text+score)
```

### 2.4 MCP 工具调用流程

```mermaid
sequenceDiagram
    participant RAG as pet-vet-rag
    participant MCP as pet-vet-mcp
    participant Manager as 服务器管理
    participant Client as MCP客户端
    participant MCPServer as MCP服务器
    
    RAG->>MCP: 1. 调用MCP工具请求<br/>(serverName+toolName+args)
    MCP->>Manager: 2. 获取服务器信息
    Manager-->>MCP: 返回服务器配置
    
    MCP->>Client: 3. 获取或创建客户端
    Client->>Client: 检查连接状态
    alt 客户端未初始化
        Client->>MCPServer: 建立连接
        MCPServer-->>Client: 连接成功
    end
    
    MCP->>Client: 4. 构建工具调用请求
    Client->>MCPServer: 5. 发送工具调用<br/>(CallToolRequest)
    MCPServer->>MCPServer: 6. 执行工具逻辑
    MCPServer-->>Client: 返回执行结果<br/>(CallToolResult)
    Client-->>MCP: 返回结果
    
    MCP->>MCP: 7. 格式化响应
    MCP-->>RAG: 8. 返回工具执行结果
```

### 2.5 查询分类决策流程

```mermaid
flowchart TD
    A[接收查询请求] --> B[加载对话记忆]
    B --> C{是否启用混合分类器?}
    
    C -->|是| D[执行混合分类]
    C -->|否| E[执行原有分类器]
    
    D --> F{对比模式?}
    F -->|是| G[同时运行新旧方案]
    F -->|否| H[仅运行混合方案]
    
    G --> I[对比结果]
    H --> J[获取分类结果]
    E --> J
    
    J --> K{需要检索?}
    I --> K
    
    K -->|是| L[执行向量检索]
    K -->|否| M[跳过检索]
    
    L --> N{检索质量满足?}
    N -->|是| O[RAG模式生成<br/>知识库+历史]
    N -->|否| P[纯LLM模式生成<br/>仅历史]
    
    M --> P
    
    O --> Q[更新记忆]
    P --> Q
    Q --> R[保存历史记录]
    R --> S[返回响应]
    
    style D fill:#e1f5ff
    style E fill:#fff4e1
    style L fill:#e8f5e9
    style O fill:#fff9c4
    style P fill:#fff9c4
```

## 3. 技术栈说明

### 3.1 pet-vet-embedding 服务

| 组件 | 技术选型 | 说明 |
|------|---------|------|
| 向量模型 | Cohere (默认) | embed-english-v3.0, 1024维 |
| 向量模型 | Hugging Face | BAAI/bge-small-zh-v1.5, 384维 |
| 向量模型 | OpenAI | text-embedding-3-small, 1536维 |
| 向量模型 | Ollama | nomic-embed-text, 768维 |
| 向量数据库 | Zilliz (默认) | Serverless 云服务 |
| 向量数据库 | Qdrant | Cloud 云服务 |
| 框架 | Spring Boot | Java 微服务框架 |
| 向量化库 | LangChain4j | Java 向量化库 |

### 3.2 pet-vet-rag 服务

| 组件 | 技术选型 | 说明 |
|------|---------|------|
| LLM 模型 | DeepSeek (默认) | deepseek-chat, 价格便宜 |
| LLM 模型 | OpenAI | gpt-4o, 性能优秀 |
| LLM 模型 | xAI Grok | grok-4-latest, 性能优秀 |
| 服务调用 | Feign | 调用 embedding 服务 |
| 记忆存储 | Redis | 对话记忆缓存 |
| 历史存储 | MySQL | 历史记录持久化 |
| 框架 | Spring Boot | Java 微服务框架 |
| LLM 库 | LangChain4j | Java LLM 集成库 |

### 3.3 pet-vet-mcp 服务

| 组件 | 技术选型 | 说明 |
|------|---------|------|
| MCP 协议 | Model Context Protocol | 标准 MCP 协议 |
| MCP 客户端 | MCP Java SDK | 官方 Java SDK |
| 框架 | Spring Boot | Java 微服务框架 |
| 工具管理 | 动态注册 | 支持动态注册 MCP 服务器 |

## 4. 数据流转说明

### 4.1 向量化数据流转

1. **文本输入** → EmbeddingService
2. **向量化** → EmbeddingModel (Cohere/Hugging Face/OpenAI/Ollama)
3. **向量存储** → EmbeddingStore (Zilliz/Qdrant)
4. **向量检索** → 相似度搜索 → 返回 Top-K 结果

### 4.2 RAG 数据流转

1. **用户查询** → RagValidationService
2. **记忆加载** → MemoryService → Redis
3. **查询分类** → QueryClassifier → 判断是否需要检索
4. **向量检索** → RagService → EmbeddingService → VectorDB
5. **答案生成** → LLM (DeepSeek/OpenAI/Grok)
6. **记忆更新** → MemoryService → Redis
7. **历史保存** → HistoryService → MySQL

### 4.3 MCP 工具数据流转

1. **工具调用请求** → McpToolService
2. **服务器管理** → McpServerManagerService
3. **客户端连接** → McpClientManager → MCP Server
4. **工具执行** → MCP Server → 返回结果
5. **结果返回** → 格式化响应 → 返回给调用方

## 5. 服务交互接口

### 5.1 pet-vet-embedding 服务接口

- **POST /api/embedding/embed**: 文本向量化
- **POST /api/embedding/search**: 向量相似度搜索
- **POST /api/resume/parse**: 简历解析和向量化
- **POST /api/resume/search**: 简历向量检索

### 5.2 pet-vet-rag 服务接口

- **POST /api/rag/query**: 基础 RAG 查询
- **POST /api/rag/validate**: RAG 验证查询（包含记忆管理）
- **GET /api/rag/health**: 健康检查

### 5.3 pet-vet-mcp 服务接口

- **POST /api/mcp/servers/register**: 注册 MCP 服务器
- **GET /api/mcp/servers**: 获取所有服务器
- **POST /api/mcp/tools/call**: 调用 MCP 工具
- **GET /api/mcp/servers/{serverName}/tools**: 列出服务器工具
- **POST /api/mcp/resources/get**: 获取资源
- **GET /api/mcp/servers/{serverName}/resources**: 列出服务器资源

## 6. 配置说明

### 6.1 向量模型配置

通过环境变量 `EMBEDDING_MODEL_TYPE` 选择模型类型：
- `cohere` (默认)
- `hugging-face`
- `openai`
- `ollama`

### 6.2 向量数据库配置

通过环境变量 `VECTOR_DB_TYPE` 选择数据库类型：
- `zilliz` (默认)
- `qdrant`

### 6.3 LLM 模型配置

通过环境变量 `AI_PROVIDER_TYPE` 选择 LLM 提供商：
- `deepseek` (默认)
- `openai`
- `grok`

## 7. 总结

本架构设计采用分层架构，实现了：

1. **基础服务层 (pet-vet-embedding)**: 提供向量化和向量存储能力，支持多种模型和数据库
2. **增强服务层 (pet-vet-rag)**: 提供 RAG 增强检索，整合知识库和 LLM
3. **工具服务层 (pet-vet-mcp)**: 提供 MCP 工具调用能力，支持扩展

三层服务相互配合，为上层业务提供完整的 AI 能力支持。
