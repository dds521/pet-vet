# PetVet RAG 服务模块

## 模块概述

`pet-vet-rag` 是 PetVet 平台的 RAG (Retrieval-Augmented Generation) 增强检索服务模块，提供智能查询分类、向量检索、LLM生成、对话记忆管理等核心能力，是平台的核心服务层。

## 模块结构

```
pet-vet-rag/
├── pet-vet-rag-api/                # API接口定义模块
│   └── src/main/java/com/petvet/rag/api/
│       ├── constants/             # API常量
│       ├── dto/                   # 数据传输对象
│       ├── feign/                 # Feign客户端
│       ├── req/                   # 请求对象
│       └── resp/                  # 响应对象
│
└── pet-vet-rag-service/            # 服务实现模块
    └── src/main/java/com/petvet/rag/app/
        ├── classifier/            # 查询分类器
        │   ├── chain/             # 责任链
        │   ├── config/            # 配置
        │   ├── engine/            # 规则引擎
        │   ├── model/             # 模型
        │   ├── strategy/          # 策略接口
        │   └── strategy/impl/     # 策略实现
        ├── config/                 # 配置类
        ├── controller/             # REST控制器
        ├── domain/                 # 领域模型
        ├── mapper/                 # MyBatis映射器
        ├── service/                # 业务服务
        └── util/                   # 工具类
```

## 核心功能

### 1. 查询分类器
- **QueryClassifier**: 基础查询分类器
- **HybridQueryClassifier**: 混合查询分类器
- **ClassificationChain**: 分类责任链
- **分类策略**:
  - **RuleLayerStrategy**: 规则层策略（使用QLExpress 4.0.4）
  - **CacheLayerStrategy**: 缓存层策略
  - **RedisCacheLayerStrategy**: Redis缓存策略
  - **FallbackStrategy**: 兜底策略

### 2. 规则引擎
- **RuleEngine**: QLExpress规则引擎封装
- **RuleLoader**: 规则加载器
- **RuleDefinition**: 规则定义模型
- 支持配置化规则表达式
- 支持规则优先级和动态加载

### 3. RAG核心服务
- **RagValidationService**: RAG验证服务（核心业务服务）
- **RagService**: 基础RAG服务
- 整合检索、生成、记忆管理等功能
- 支持检索质量检查

### 4. 记忆管理
- **MemoryService**: 对话记忆管理
- 维护用户会话上下文
- 支持Redis存储对话历史

### 5. 历史记录
- **HistoryService**: 历史记录管理
- 持久化查询历史到MySQL
- 支持历史记录查询

### 6. LLM生成
- **LangChainConfig**: LLM配置管理
- 支持多种大语言模型：
  - **DeepSeek** (默认): `deepseek-chat`
  - **OpenAI**: `gpt-4o`
  - **xAI Grok**: `grok-4-latest`

## 技术栈

- **框架**: Spring Boot 3.3.5
- **LLM库**: LangChain4j
- **规则引擎**: QLExpress 4.0.4
- **服务调用**: Feign
- **记忆存储**: Redis
- **历史存储**: MySQL + MyBatis Plus
- **服务注册**: Nacos Discovery
- **配置中心**: Nacos Config

## 支持的LLM模型

| 模型 | 提供商 | 说明 |
|------|--------|------|
| deepseek-chat | DeepSeek | 默认模型，价格便宜，国内访问稳定 |
| gpt-4o | OpenAI | 性能优秀，需要API Key |
| grok-4-latest | xAI Grok | 性能优秀，使用OpenAI兼容API |

## API接口

### RAG查询接口
- `POST /api/rag/query`: 基础RAG查询
- `POST /api/rag/validate`: RAG验证查询（包含记忆管理）
- `GET /api/rag/health`: 健康检查

## 配置说明

### 查询分类器配置
```yaml
rag:
  classifier:
    rule:
      enabled: true
      rules:
        - name: "casual_chat"
          priority: 1
          expression: "lowerQuery.contains(\"你好\")"
          action: "result.setNeedRetrieval(false); return true;"
```

### LLM配置
- 通过环境变量 `AI_PROVIDER_TYPE` 选择LLM提供商
- 支持 deepseek / openai / grok

### Nacos配置
- `pet-vet-rag-common.yml`: 通用配置
- `pet-vet-rag-dev.yml`: 开发环境配置
- `pet-vet-rag-test.yml`: 测试环境配置
- `pet-vet-rag-prod.yml`: 生产环境配置

## 数据流转

1. **用户查询** → RagValidationService
2. **记忆加载** → MemoryService → Redis
3. **查询分类** → QueryClassifier → 判断是否需要检索
4. **向量检索** → RagService → EmbeddingService → VectorDB
5. **答案生成** → LLM (DeepSeek/OpenAI/Grok)
6. **记忆更新** → MemoryService → Redis
7. **历史保存** → HistoryService → MySQL

## 查询分类流程

```
接收查询请求
  ↓
加载对话记忆
  ↓
执行查询分类（规则层 → 缓存层 → 兜底层）
  ↓
判断是否需要检索
  ↓
需要检索 → 向量检索 → 检查检索质量 → RAG模式生成
不需要检索 → 纯LLM模式生成
  ↓
更新记忆 → 保存历史 → 返回响应
```

## 启动说明

1. 确保Nacos服务已启动
2. 确保MySQL数据库已创建并初始化
3. 确保Redis服务已启动
4. 确保pet-vet-embedding服务已启动
5. 配置LLM API Key（如需要）
6. 运行 `PetVetRagApplication` 主类

## 注意事项

- 依赖pet-vet-embedding服务的向量检索能力
- 需要配置Redis用于对话记忆存储
- 需要配置MySQL用于历史记录持久化
- LLM API Key需要根据选择的提供商配置
- 规则引擎使用QLExpress 4.0.4，支持配置化规则
