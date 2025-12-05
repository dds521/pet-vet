#!/bin/bash

# ============================================
# 在 Nacos 中创建 Sentinel 规则配置
# 根据 SentinelDemoController 中的资源创建对应的规则
# ============================================

# Nacos 配置
NACOS_SERVER="127.0.0.1:8848"
NACOS_GROUP="DEFAULT_GROUP"
APP_NAME="pet-vet-ai"
NAMESPACE=""  # 如果使用命名空间，请设置，例如：NAMESPACE="your-namespace-id"

# 如果设置了命名空间，添加命名空间参数
if [ -n "$NAMESPACE" ]; then
    NAMESPACE_PARAM="&tenant=$NAMESPACE"
else
    NAMESPACE_PARAM=""
fi

echo "=========================================="
echo "开始创建 Sentinel 规则到 Nacos"
echo "Nacos 地址: $NACOS_SERVER"
echo "应用名称: $APP_NAME"
echo "=========================================="
echo ""

# 1. 创建流控规则（Flow Rules）
# 包含：flowControl（QPS流控）和 threadControl（线程数流控）
echo "📝 创建流控规则..."
FLOW_RULES='[
  {
    "resource": "flowControl",
    "limitApp": "default",
    "grade": 1,
    "count": 2,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false,
    "warmUpPeriodSec": 10,
    "maxQueueingTimeMs": 500
  },
  {
    "resource": "threadControl",
    "limitApp": "default",
    "grade": 0,
    "count": 2,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  }
]'

curl -X POST "http://$NACOS_SERVER/nacos/v1/cs/configs" \
  -d "dataId=${APP_NAME}-flow-rules" \
  -d "group=${NACOS_GROUP}" \
  -d "content=$FLOW_RULES" \
  ${NAMESPACE_PARAM:+-d "tenant=$NAMESPACE"}

if [ $? -eq 0 ]; then
    echo "✅ 流控规则创建成功"
else
    echo "❌ 流控规则创建失败"
fi
echo ""

# 2. 创建降级规则（Degrade Rules）
# 包含：circuitBreaker（异常比例熔断）
echo "📝 创建降级规则..."
DEGRADE_RULES='[
  {
    "resource": "circuitBreaker",
    "limitApp": "default",
    "grade": 1,
    "count": 0.5,
    "timeWindow": 10,
    "minRequestAmount": 5,
    "slowRatioThreshold": 0.5,
    "statIntervalMs": 1000
  }
]'

curl -X POST "http://$NACOS_SERVER/nacos/v1/cs/configs" \
  -d "dataId=${APP_NAME}-degrade-rules" \
  -d "group=${NACOS_GROUP}" \
  -d "content=$DEGRADE_RULES" \
  ${NAMESPACE_PARAM:+-d "tenant=$NAMESPACE"}

if [ $? -eq 0 ]; then
    echo "✅ 降级规则创建成功"
else
    echo "❌ 降级规则创建失败"
fi
echo ""

# 3. 创建热点参数规则（Param Flow Rules）
# 包含：hotParam（热点参数限流）
echo "📝 创建热点参数规则..."
PARAM_FLOW_RULES='[
  {
    "resource": "hotParam",
    "grade": 1,
    "paramIdx": 0,
    "count": 2,
    "durationInSec": 1,
    "clusterMode": false,
    "burstCount": 0,
    "controlBehavior": 0,
    "limitApp": "default",
    "maxQueueingTimeMs": 500
  }
]'

curl -X POST "http://$NACOS_SERVER/nacos/v1/cs/configs" \
  -d "dataId=${APP_NAME}-param-flow-rules" \
  -d "group=${NACOS_GROUP}" \
  -d "content=$PARAM_FLOW_RULES" \
  ${NAMESPACE_PARAM:+-d "tenant=$NAMESPACE"}

if [ $? -eq 0 ]; then
    echo "✅ 热点参数规则创建成功"
else
    echo "❌ 热点参数规则创建失败"
fi
echo ""

# 4. 创建系统规则（System Rules）
# 系统级规则，保护整个系统
echo "📝 创建系统规则..."
SYSTEM_RULES='[
  {
    "avgRt": 1000,
    "maxThread": 10,
    "qps": 1000,
    "highestSystemLoad": 0.8,
    "highestCpuUsage": 0.8
  }
]'

curl -X POST "http://$NACOS_SERVER/nacos/v1/cs/configs" \
  -d "dataId=${APP_NAME}-system-rules" \
  -d "group=${NACOS_GROUP}" \
  -d "content=$SYSTEM_RULES" \
  ${NAMESPACE_PARAM:+-d "tenant=$NAMESPACE"}

if [ $? -eq 0 ]; then
    echo "✅ 系统规则创建成功"
else
    echo "❌ 系统规则创建失败"
fi
echo ""

# 5. 创建授权规则（Authority Rules）- 可选，当前示例中未使用
echo "📝 创建授权规则（空规则）..."
AUTHORITY_RULES='[]'

curl -X POST "http://$NACOS_SERVER/nacos/v1/cs/configs" \
  -d "dataId=${APP_NAME}-authority-rules" \
  -d "group=${NACOS_GROUP}" \
  -d "content=$AUTHORITY_RULES" \
  ${NAMESPACE_PARAM:+-d "tenant=$NAMESPACE"}

if [ $? -eq 0 ]; then
    echo "✅ 授权规则创建成功（空规则）"
else
    echo "❌ 授权规则创建失败"
fi
echo ""

echo "=========================================="
echo "✅ 所有 Sentinel 规则创建完成！"
echo ""
echo "📋 创建的规则配置："
echo "   - ${APP_NAME}-flow-rules (流控规则)"
echo "   - ${APP_NAME}-degrade-rules (降级规则)"
echo "   - ${APP_NAME}-param-flow-rules (热点参数规则)"
echo "   - ${APP_NAME}-system-rules (系统规则)"
echo "   - ${APP_NAME}-authority-rules (授权规则)"
echo ""
echo "🔍 验证规则："
echo "   访问 Nacos 控制台: http://$NACOS_SERVER/nacos"
echo "   在配置管理 -> 配置列表中查看上述配置"
echo ""
echo "🧪 测试接口："
echo "   - QPS 流控: curl http://localhost:8080/api/sentinel/flow"
echo "   - 熔断测试: curl 'http://localhost:8080/api/sentinel/circuit?error=true'"
echo "   - 线程数流控: curl http://localhost:8080/api/sentinel/thread"
echo "   - 热点参数: curl 'http://localhost:8080/api/sentinel/hot?param=test'"
echo "   - 系统规则: curl http://localhost:8080/api/sentinel/system"
echo "=========================================="

