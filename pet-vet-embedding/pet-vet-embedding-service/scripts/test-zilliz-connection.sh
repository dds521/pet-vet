#!/bin/bash

# ============================================
# Zilliz 连接测试脚本
# 用于测试 Zilliz 向量数据库的连接和 API 调用
# ============================================

# 配置参数（请根据实际情况修改）
ZILLIZ_HOST="https://in03-404be71e0f75d6e.serverless.aws-eu-central-1.cloud.zilliz.com"
# 如果环境变量未设置，使用默认值
ZILLIZ_API_KEY="${ZILLIZ_API_KEY:}"
COLLECTION_NAME="pet_vet_embeddings"

# 检查 API Key 是否设置
if [ -z "$ZILLIZ_API_KEY" ]; then
    echo "❌ 错误: ZILLIZ_API_KEY 未设置"
    echo "请设置环境变量: export ZILLIZ_API_KEY='your-api-key'"
    echo "或在脚本中直接设置 ZILLIZ_API_KEY 变量"
    exit 1
fi

echo "=========================================="
echo "测试 Zilliz 连接"
echo "=========================================="
echo "Host: $ZILLIZ_HOST"
echo "Collection: $COLLECTION_NAME"
echo ""

# 测试 1: 搜索 API（使用示例向量）
echo "测试 1: 搜索 API"
echo "----------------------------------------"

# 创建一个 1024 维的示例向量（全零向量，用于测试）
EXAMPLE_VECTOR=$(python3 -c "
import json
vector = [0.0] * 1024
print(json.dumps([vector]))
" 2>/dev/null)

if [ -z "$EXAMPLE_VECTOR" ]; then
    # 如果 Python 不可用，使用简单的测试向量（前10维）
    EXAMPLE_VECTOR='[[0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0]]'
    echo "⚠️  注意: 使用简化测试向量（10维），实际应为 1024 维"
fi

RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
    --request POST \
    --url "${ZILLIZ_HOST}/v2/vectordb/entities/search" \
    --header 'Accept: application/json' \
    --header "Authorization: Bearer ${ZILLIZ_API_KEY}" \
    --header 'Content-Type: application/json' \
    --data "{
        \"collectionName\": \"${COLLECTION_NAME}\",
        \"data\": ${EXAMPLE_VECTOR},
        \"limit\": 5,
        \"outputFields\": [\"*\"]
    }")

HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE:" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE:/d')

echo "HTTP 状态码: $HTTP_CODE"
echo "响应内容:"
echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
echo ""

# 判断结果
if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ 连接成功！Zilliz API 可以正常访问"
    exit 0
elif [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    echo "❌ 认证失败 (HTTP $HTTP_CODE)"
    echo "可能原因:"
    echo "  1. API Key 不正确或已过期"
    echo "  2. API Key 没有访问该集合的权限"
    exit 1
elif [ "$HTTP_CODE" = "404" ]; then
    echo "❌ 集合不存在 (HTTP $HTTP_CODE)"
    echo "可能原因:"
    echo "  1. 集合名称 '${COLLECTION_NAME}' 不存在"
    echo "  2. 集合在不同的项目或区域"
    exit 1
elif [ "$HTTP_CODE" = "1100" ]; then
    echo "❌ Milvus 连接错误 (HTTP $HTTP_CODE)"
    echo "可能原因:"
    echo "  1. 网络连接问题（防火墙/代理阻止）"
    echo "  2. IP 地址未加入 Zilliz Cloud 白名单"
    echo "  3. Zilliz 实例在休眠（首次连接需要等待）"
    exit 1
else
    echo "❌ 请求失败 (HTTP $HTTP_CODE)"
    echo "响应: $BODY"
    exit 1
fi
