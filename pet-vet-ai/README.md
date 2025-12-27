# PetVetAI 服务模块

## 项目简介

PetVetAI 是基于 **领域驱动设计（DDD）** 架构的宠物医疗 AI 咨询服务模块。作为顶层服务，为 APP 端、PC 端提供统一的 API 接口，所有 AI 能力（向量化、RAG、LLM）均通过以下微服务提供：

- **pet-vet-rag**: RAG 增强检索服务
- **pet-vet-embedding**: 向量化服务
- **pet-vet-mcp**: MCP 工具服务

## 模块结构

本模块采用 Maven 多模块结构，包含以下子模块：

```
pet-vet-ai/
├── pet-vet-ai-api/          # API 接口定义模块
│   └── 提供对外 API 接口定义、DTO、常量等
│
└── pet-vet-ai-service/      # 服务实现模块（DDD 架构）
    └── 完整的业务实现，采用 DDD 分层架构
```

### pet-vet-ai-api 模块

**职责**: 定义对外提供的 API 接口、DTO、常量等

**主要包结构**:
- `com.petvet.ai.api.constants`: API 常量定义
- `com.petvet.ai.api.dto`: 数据传输对象
- `com.petvet.ai.api.enums`: 枚举类型
- `com.petvet.ai.api.feign`: Feign 客户端接口
- `com.petvet.ai.api.req`: 请求对象
- `com.petvet.ai.api.resp`: 响应对象

### pet-vet-ai-service 模块

**职责**: 业务逻辑实现，采用 DDD 分层架构

**架构分层**:
- **应用层（app/）**: 应用服务、控制器、DTO
- **领域层（domain/）**: 核心业务逻辑、聚合根、值对象、领域服务
- **基础设施层（infrastructure/）**: 持久化、外部服务、消息队列、缓存等

## DDD 架构设计

### 分层架构

```
pet-vet-ai-service/
├── app/                          # 应用层（Application Layer）
│   ├── application/             # 应用服务
│   │   ├── diagnosis/          # 诊断应用服务
│   │   └── user/               # 用户应用服务
│   ├── controller/              # 控制器（用户界面层）
│   │   ├── PetVetController.java
│   │   └── user/
│   │       └── UserController.java
│   └── dto/                     # 数据传输对象
│       ├── req/                 # 请求 DTO
│       └── resp/                # 响应 DTO
│
├── domain/                      # 领域层（Domain Layer）- 核心业务逻辑
│   ├── user/                    # 用户域
│   │   ├── model/              # 领域模型
│   │   │   ├── User.java       # 用户聚合根
│   │   │   ├── UserId.java     # 用户ID值对象
│   │   │   ├── UserStatus.java # 用户状态枚举
│   │   │   └── WeChatInfo.java # 微信信息值对象
│   │   ├── repository/         # 仓储接口
│   │   │   └── UserRepository.java
│   │   └── service/            # 领域服务
│   │       └── UserDomainService.java
│   │
│   ├── pet/                     # 宠物域
│   │   ├── model/
│   │   │   ├── Pet.java        # 宠物聚合根
│   │   │   ├── PetId.java      # 宠物ID值对象
│   │   │   └── PetInfo.java    # 宠物信息值对象
│   │   └── repository/
│   │       └── PetRepository.java
│   │
│   └── diagnosis/               # 诊断域
│       ├── model/
│       │   ├── Diagnosis.java      # 诊断聚合根
│       │   ├── DiagnosisId.java     # 诊断ID值对象
│       │   ├── DiagnosisResult.java # 诊断结果值对象
│       │   ├── DiagnosisStatus.java # 诊断状态枚举
│       │   └── Symptom.java        # 症状值对象
│       ├── repository/
│       │   └── DiagnosisRepository.java
│       └── service/
│           └── DiagnosisDomainService.java
│
└── infrastructure/              # 基础设施层（Infrastructure Layer）
    ├── persistence/            # 持久化实现
    │   ├── user/            # 用户持久化
    │   ├── pet/               # 宠物持久化
    │   ├── diagnosis/         # 诊断持久化
    │   └── transaction/       # 事务日志持久化
    │
    ├── external/               # 外部服务调用
    │   ├── rag/               # RAG 服务调用
    │   └── wechat/            # 微信服务调用
    │
    ├── messaging/              # 消息队列
    │   └── rocketmq/
    │
    ├── cache/                  # 缓存
    │   └── redis/
    │
    ├── config/                 # 配置类
    │   ├── CorsConfig.java
    │   ├── RedisConfig.java
    │   ├── SeataConfig.java
    │   ├── SentinelConfig.java
    │   └── security/
    │
    ├── security/               # 安全工具
    │   └── JwtUtil.java
    │
    └── util/                   # 工具类
        ├── address/           # 地址匹配工具
        └── PinyinUtil.java    # 拼音工具
```

### 架构特点

1. **领域层（Domain Layer）**
   - 包含核心业务逻辑，不依赖任何技术框架
   - 使用聚合根（Aggregate Root）管理业务实体
   - 使用值对象（Value Object）封装业务概念
   - 使用领域服务（Domain Service）处理跨聚合的业务逻辑

2. **应用层（Application Layer）**
   - 协调领域对象完成业务用例
   - 处理事务、缓存等技术问题
   - 不包含业务逻辑，只负责编排

3. **基础设施层（Infrastructure Layer）**
   - 实现领域层的接口（如 Repository）
   - 处理技术细节（数据库、消息队列、外部服务等）
   - 提供工具类和配置类

## 核心领域

### 1. 用户域（User Domain）

**聚合根**: `User`
- 管理用户的基本信息和微信信息
- 提供用户注册、登录、信息更新等业务方法

**值对象**:
- `UserId`: 用户ID值对象
- `WeChatInfo`: 微信信息值对象
- `UserStatus`: 用户状态枚举

**领域服务**: `UserDomainService`
- 处理用户登录/注册的业务逻辑
- 协调用户聚合根和外部服务（微信API）

### 2. 宠物域（Pet Domain）

**聚合根**: `Pet`
- 管理宠物的基本信息
- 提供宠物信息查询和更新方法

**值对象**:
- `PetId`: 宠物ID值对象
- `PetInfo`: 宠物信息值对象（品种、年龄等）

### 3. 诊断域（Diagnosis Domain）

**聚合根**: `Diagnosis`
- 管理诊断的完整生命周期
- 提供诊断创建、完成等业务方法

**值对象**:
- `DiagnosisId`: 诊断ID值对象
- `Symptom`: 症状值对象
- `DiagnosisResult`: 诊断结果值对象
- `DiagnosisStatus`: 诊断状态枚举

**领域服务**: `DiagnosisDomainService`
- 协调诊断聚合根和外部服务（RAG服务）
- 处理诊断分析的业务逻辑

## 技术栈

### 核心框架
- **Spring Boot 3.x**: 应用框架
- **Spring Cloud**: 微服务框架
- **MyBatis-Plus 3.5.5**: ORM 框架

### 中间件
- **MySQL**: 关系型数据库
- **Redis**: 缓存和会话存储
- **RocketMQ**: 消息队列（支持事务消息）
- **Nacos**: 服务注册与配置中心
- **Seata**: 分布式事务（可选）
- **Sentinel**: 流量控制和熔断降级

### 安全
- **Spring Security**: 安全框架
- **JWT**: 身份认证
- **OAuth2 Resource Server**: 资源服务器

### 工具库
- **Lombok**: 简化代码
- **Hutool**: Java 工具库
- **Pinyin4j**: 中文拼音处理
- **Apache Commons Compress**: 压缩包处理

## 主要功能

### 1. 用户管理
- 微信登录/注册
- 用户信息更新
- JWT Token 生成和验证

### 2. 宠物诊断
- 基于 RAG 的增强诊断
- 症状分析和建议生成
- 诊断结果保存

### 3. 消息处理
- RocketMQ 事务消息
- 死信队列处理
- 消息重试机制

## 构建和运行

### 构建项目

```bash
# 构建整个模块
mvn clean package

# 只构建 API 模块
cd pet-vet-ai-api
mvn clean package

# 只构建 Service 模块
cd pet-vet-ai-service
mvn clean package
```

### 运行服务

```bash
cd pet-vet-ai-service
java -jar target/pet-vet-ai-service-1.0.0-SNAPSHOT.jar
```

### Docker 构建

```bash
cd pet-vet-ai-service
docker build -t pet-vet-ai-service:latest .
```

## API 接口

### 用户接口

- `POST /api/user/wechat/login` - 微信登录
- `POST /api/user/wechat/userinfo` - 更新用户信息

### 诊断接口

- `POST /api/pet/diagnose` - 宠物诊断

## 配置说明

### 应用配置

配置文件位于 `pet-vet-ai-service/src/main/resources/`:
- `application.yml`: 基础配置
- `nacos/`: Nacos 配置中心配置

### 环境变量

- `SERVER_PORT`: 服务端口（默认 48080）
- `SPRING_PROFILES_ACTIVE`: 激活的 Profile（默认 dev）

### Nacos 配置

配置从 Nacos 配置中心加载：
- `pet-vet-ai-common.yml`: 公共配置
- `pet-vet-ai-${profile}.yml`: 环境特定配置

## 依赖服务

- **pet-vet-rag**: RAG 增强检索服务
- **pet-vet-embedding**: 向量化服务
- **pet-vet-mcp**: MCP 工具服务

## 开发规范

### DDD 设计原则

1. **聚合根（Aggregate Root）**
   - 每个聚合只有一个聚合根
   - 聚合根负责维护聚合内的业务不变性
   - 外部只能通过聚合根访问聚合内的实体

2. **值对象（Value Object）**
   - 不可变对象
   - 通过值相等性判断相等
   - 用于封装业务概念（如 ID、状态等）

3. **仓储（Repository）**
   - 领域层定义接口，基础设施层实现
   - 提供聚合的持久化和查询能力
   - 隐藏持久化细节

4. **领域服务（Domain Service）**
   - 处理跨聚合的业务逻辑
   - 协调多个聚合根完成业务操作
   - 不持有状态

### 代码注释规范

所有类和方法都必须包含完整的 JavaDoc 注释，包括：
- `@author daidasheng`
- `@date YYYY-MM-DD`
- 方法参数和返回值说明

## 测试

测试代码位于 `pet-vet-ai-service/src/test/java/`:
- 单元测试：测试领域逻辑
- 集成测试：测试应用服务和基础设施

运行测试：
```bash
cd pet-vet-ai-service
mvn test
```

## 版本历史

- **v1.0.0-SNAPSHOT**: 初始版本，采用 DDD 架构

## 相关文档

- 详细的 DDD 改造方案请参考: `../DDD_TRANSFORMATION.html`
- Service 模块详细文档: `pet-vet-ai-service/README.md`

## 作者

- **daidasheng**

## 许可证

本项目为内部项目，版权归公司所有。

---

**注意**: 本项目已完全采用 DDD 架构，所有业务逻辑都在领域层实现，应用层只负责编排，基础设施层负责技术实现。
