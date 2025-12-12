# RAG Controller curl 测试用例

## 基础信息

- **服务地址**: `http://localhost:48083`
- **API前缀**: `/api/rag`
- **Content-Type**: `application/json`

## 1. 健康检查接口

### 1.1 健康检查

```bash
curl -X GET http://localhost:48083/api/rag/health \
  -H "Content-Type: application/json"
```

**预期响应**:
```json
{
  "code": 200,
  "message": "健康检查通过",
  "data": "RAG服务运行正常"
}
```

## 2. RAG 查询接口

### 2.1 基础查询（启用生成）

```bash
curl -X POST http://localhost:48083/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "我的狗狗最近总是咳嗽，是什么原因？",
    "maxResults": 5,
    "minScore": 0.7,
    "enableGeneration": true
  }'
```

### 2.2 仅检索不生成

```bash
curl -X POST http://localhost:48083/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "犬类咳嗽的常见原因",
    "maxResults": 10,
    "minScore": 0.6,
    "enableGeneration": false
  }'
```

### 2.3 使用默认参数

```bash
curl -X POST http://localhost:48083/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "宠物疫苗需要接种哪些？"
  }'
```

### 2.4 错误测试 - 查询文本为空

```bash
curl -X POST http://localhost:48083/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": ""
  }'
```

**预期响应**:
```json
{
  "code": 500,
  "message": "查询文本不能为空"
}
```

## 3. RAG 验证接口（重点）

### 3.1 基础验证（使用知识库）

```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "我的狗狗最近总是咳嗽，是什么原因？",
    "userId": "user_12345",
    "sessionId": "session_abc123",
    "maxResults": 5,
    "minScore": 0.7,
    "enableGeneration": true,
    "contextWindowSize": 5
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "验证成功",
  "data": {
    "answer": "根据您描述的症状...",
    "retrievedCount": 3,
    "retrievedDocuments": [
      {
        "chunkId": "chunk_001",
        "score": 0.85,
        "text": "犬类咳嗽的常见原因..."
      }
    ],
    "conversationHistory": {
      "messageCount": 2,
      "recentMessages": [
        {
          "role": "USER",
          "content": "我的狗狗最近总是咳嗽，是什么原因？",
          "timestamp": 1702345678000
        },
        {
          "role": "ASSISTANT",
          "content": "根据您描述的症状...",
          "timestamp": 1702345679000
        }
      ]
    },
    "confidence": 0.88,
    "queryTime": 1250,
    "sessionId": "session_abc123",
    "usedKnowledgeBase": true
  }
}
```

### 3.2 自动生成 sessionId

```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "我的狗狗最近总是咳嗽，是什么原因？",
    "userId": "user_12345"
  }'
```

**说明**: 如果不提供 `sessionId`，系统会自动生成一个。

### 3.3 多轮对话场景

**第一轮对话**:
```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "我的狗狗最近总是咳嗽",
    "userId": "user_12345",
    "sessionId": "session_abc123"
  }'
```

**第二轮对话**（使用相同的 sessionId）:
```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "那应该怎么治疗呢？",
    "userId": "user_12345",
    "sessionId": "session_abc123"
  }'
```

**第三轮对话**:
```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "需要吃什么药？",
    "userId": "user_12345",
    "sessionId": "session_abc123"
  }'
```

**说明**: 多轮对话会保留历史上下文，AI可以基于之前的对话内容进行回答。

### 3.4 闲聊场景（不使用知识库）

```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "你好",
    "userId": "user_12345",
    "sessionId": "session_abc123"
  }'
```

**预期**: `usedKnowledgeBase` 应该为 `false`，因为"你好"是闲聊，不需要检索知识库。

### 3.5 专业问题（强制使用知识库）

```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "狗狗得了犬瘟热有什么症状？",
    "userId": "user_12345",
    "sessionId": "session_abc123"
  }'
```

**预期**: `usedKnowledgeBase` 应该为 `true`，因为包含专业术语"症状"。

### 3.6 仅使用历史对话（不启用生成）

```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "我的狗狗最近总是咳嗽",
    "userId": "user_12345",
    "sessionId": "session_abc123",
    "enableGeneration": false
  }'
```

### 3.7 自定义检索参数

```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "宠物疫苗相关问题",
    "userId": "user_12345",
    "maxResults": 10,
    "minScore": 0.8,
    "contextWindowSize": 8
  }'
```

### 3.8 错误测试 - 查询文本为空

```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "",
    "userId": "user_12345"
  }'
```

**预期响应**:
```json
{
  "code": 500,
  "message": "查询文本不能为空"
}
```

### 3.9 错误测试 - 用户ID为空

```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "测试查询",
    "userId": ""
  }'
```

**预期响应**:
```json
{
  "code": 500,
  "message": "用户ID不能为空"
}
```

### 3.10 错误测试 - 用户ID为null

```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "测试查询"
  }'
```

**预期响应**:
```json
{
  "code": 500,
  "message": "用户ID不能为空"
}
```

## 4. 完整测试流程示例

### 4.1 完整的宠物医疗咨询流程

```bash
# 步骤1: 健康检查
curl -X GET http://localhost:48083/api/rag/health

# 步骤2: 第一轮 - 描述症状
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "我的金毛犬，3岁，最近总是咳嗽，特别是晚上，还流鼻涕",
    "userId": "user_pet_owner_001",
    "sessionId": "session_consultation_001"
  }'

# 步骤3: 第二轮 - 询问原因
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "可能是什么原因导致的？",
    "userId": "user_pet_owner_001",
    "sessionId": "session_consultation_001"
  }'

# 步骤4: 第三轮 - 询问治疗
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "应该怎么治疗？需要去医院吗？",
    "userId": "user_pet_owner_001",
    "sessionId": "session_consultation_001"
  }'

# 步骤5: 第四轮 - 询问预防
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "以后怎么预防这种情况？",
    "userId": "user_pet_owner_001",
    "sessionId": "session_consultation_001"
  }'
```

## 5. 使用 jq 美化输出（可选）

如果安装了 `jq`，可以使用以下命令美化 JSON 输出：

```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "我的狗狗最近总是咳嗽，是什么原因？",
    "userId": "user_12345"
  }' | jq .
```

## 6. 保存响应到文件

```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "我的狗狗最近总是咳嗽，是什么原因？",
    "userId": "user_12345"
  }' > response.json
```

## 7. 测试脚本示例

### 7.1 批量测试脚本

创建 `test_rag.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:48083/api/rag"
USER_ID="test_user_$(date +%s)"
SESSION_ID="test_session_$(date +%s)"

echo "=== 1. 健康检查 ==="
curl -X GET ${BASE_URL}/health
echo -e "\n"

echo "=== 2. 第一轮对话 ==="
curl -X POST ${BASE_URL}/validate \
  -H "Content-Type: application/json" \
  -d "{
    \"query\": \"我的狗狗最近总是咳嗽\",
    \"userId\": \"${USER_ID}\",
    \"sessionId\": \"${SESSION_ID}\"
  }"
echo -e "\n"

echo "=== 3. 第二轮对话 ==="
curl -X POST ${BASE_URL}/validate \
  -H "Content-Type: application/json" \
  -d "{
    \"query\": \"那应该怎么治疗呢？\",
    \"userId\": \"${USER_ID}\",
    \"sessionId\": \"${SESSION_ID}\"
  }"
echo -e "\n"

echo "=== 测试完成 ==="
```

使用方式:
```bash
chmod +x test_rag.sh
./test_rag.sh
```

## 8. 常见问题排查

### 8.1 连接失败

```bash
# 检查服务是否启动
curl -X GET http://localhost:48083/api/rag/health

# 如果失败，检查端口是否正确
netstat -an | grep 48083
```

### 8.2 查看详细错误信息

```bash
curl -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "测试",
    "userId": "user_12345"
  }' -v
```

`-v` 参数会显示详细的请求和响应信息。

### 8.3 测试超时

如果请求超时，可以增加超时时间：

```bash
curl --max-time 30 -X POST http://localhost:48083/api/rag/validate \
  -H "Content-Type: application/json" \
  -d '{
    "query": "测试查询",
    "userId": "user_12345"
  }'
```

## 9. 性能测试

### 9.1 并发测试（使用 ab 工具）

```bash
# 安装 ab 工具（Apache Bench）
# macOS: brew install httpd
# Ubuntu: apt-get install apache2-utils

# 准备测试数据文件 test_data.json
echo '{
  "query": "我的狗狗最近总是咳嗽",
  "userId": "user_12345"
}' > test_data.json

# 执行并发测试（100个请求，10个并发）
ab -n 100 -c 10 -p test_data.json -T application/json \
  http://localhost:48083/api/rag/validate
```

## 10. 注意事项

1. **端口号**: 默认端口是 `48083`，如果修改了配置，请相应调整
2. **用户ID**: 每个用户应该有唯一的 `userId`，用于隔离对话历史
3. **会话ID**: 同一会话的多次对话使用相同的 `sessionId`，可以保留上下文
4. **超时时间**: RAG 验证可能需要几秒钟，请耐心等待
5. **知识库**: 确保向量数据库和知识库已正确配置和初始化
