# PetVet Embedding 服务模块

## 模块概述

`pet-vet-embedding` 是 PetVet 平台的向量化服务模块，提供文本向量化和向量数据库管理能力。支持多种向量模型和向量数据库，为RAG检索提供基础能力。

## 模块结构

```
pet-vet-embedding/
├── pet-vet-embedding-api/          # API接口定义模块
│   └── src/main/java/com/petvet/embedding/api/
│       ├── constants/              # API常量
│       ├── dto/                    # 数据传输对象
│       ├── enums/                  # 枚举类型
│       ├── feign/                  # Feign客户端
│       ├── req/                    # 请求对象
│       └── resp/                   # 响应对象
│
└── pet-vet-embedding-service/      # 服务实现模块
    └── src/main/java/com/petvet/embedding/app/
        ├── config/                 # 配置类
        ├── controller/             # REST控制器
        ├── domain/                 # 领域模型
        ├── mapper/                 # MyBatis映射器
        ├── service/                # 业务服务
        └── util/                   # 工具类
```

## 核心功能

### 1. 文本向量化服务
- **EmbeddingService**: 核心向量化服务
- **EmbeddingModelConfig**: 向量模型配置
- 支持多种向量模型：
  - **Cohere** (默认): `embed-english-v3.0` (1024维)
  - **Hugging Face**: `BAAI/bge-small-zh-v1.5` (384维)
  - **OpenAI**: `text-embedding-3-small` (1536维)
  - **Ollama**: `nomic-embed-text` (768维)

### 2. 向量数据库服务
- **VectorDatabaseService**: 向量存储和检索服务
- **VectorDatabaseConfig**: 向量数据库配置
- **VectorDatabaseInitializer**: 向量数据库初始化
- 支持多种向量数据库：
  - **Zilliz** (默认): Serverless云服务
  - **Qdrant**: Cloud云服务

### 3. 简历解析服务
- **ResumeParseService**: 简历解析服务
- **ResumeParseServiceOptimized**: 优化版简历解析
- **ResumeChunkStrategy**: 简历分块策略
- **PdfBox3DocumentParser**: PDF文档解析器
- 支持简历上传、解析、向量化和检索

### 4. 文本分块服务
- **TextChunkService**: 文本分块服务
- **ChunkStrategy**: 分块策略接口
- **ResumeChunkStrategy**: 简历分块策略实现

## 技术栈

- **框架**: Spring Boot 3.3.5
- **向量化库**: LangChain4j
- **向量模型**: Cohere / Hugging Face / OpenAI / Ollama
- **向量数据库**: Zilliz / Qdrant
- **PDF解析**: PDFBox 3.x
- **数据库**: MySQL + MyBatis Plus
- **服务注册**: Nacos Discovery
- **配置中心**: Nacos Config

## 支持的向量模型

| 模型 | 提供商 | 维度 | 说明 |
|------|--------|------|------|
| embed-english-v3.0 | Cohere | 1024 | 默认模型，有免费额度，性能好 |
| BAAI/bge-small-zh-v1.5 | Hugging Face | 384 | 完全免费，推荐中文场景 |
| text-embedding-3-small | OpenAI | 1536 | 需要API Key，性能优秀 |
| nomic-embed-text | Ollama | 768 | 完全免费，本地运行 |

## 支持的向量数据库

| 数据库 | 类型 | 说明 |
|--------|------|------|
| Zilliz | Serverless | 默认选择，支持TLS连接 |
| Qdrant | Cloud | 备选方案，性能优秀 |

## API接口

### 向量化接口
- `POST /api/embedding/embed`: 文本向量化
- `POST /api/embedding/search`: 向量相似度搜索

### 简历解析接口
- `POST /api/resume/parse`: 简历解析和向量化
- `POST /api/resume/search`: 简历向量检索

## 配置说明

### 环境变量
- `EMBEDDING_MODEL_TYPE`: 向量模型类型 (cohere/hugging-face/openai/ollama)
- `VECTOR_DB_TYPE`: 向量数据库类型 (zilliz/qdrant)

### Nacos配置
- `pet-vet-embedding-common.yml`: 通用配置
- `pet-vet-embedding-dev.yml`: 开发环境配置
- `pet-vet-embedding-test.yml`: 测试环境配置
- `pet-vet-embedding-prod.yml`: 生产环境配置

## 数据流转

1. **文本输入** → EmbeddingService
2. **向量化** → EmbeddingModel (Cohere/Hugging Face/OpenAI/Ollama)
3. **向量存储** → EmbeddingStore (Zilliz/Qdrant)
4. **向量检索** → 相似度搜索 → 返回Top-K结果

## 启动说明

1. 确保Nacos服务已启动
2. 确保MySQL数据库已创建并初始化
3. 配置向量模型API Key（如需要）
4. 配置向量数据库连接信息
5. 运行 `PetVetEmbeddingApplication` 主类

## 注意事项

- Cohere模型需要配置API Key，但有免费额度
- Hugging Face模型完全免费，适合中文场景
- OpenAI模型需要付费API Key
- Ollama需要本地部署Ollama服务
- Zilliz需要配置云服务连接信息
- Qdrant需要配置Cloud服务连接信息
