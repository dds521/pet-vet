#!/bin/bash

# ============================================
# 简单的配置文件上传脚本
# 使用 curl 直接上传，无需认证（如果 Nacos 未开启认证）
# ============================================

NACOS_SERVER="${NACOS_SERVER_ADDR:-127.0.0.1:8848}"
NAMESPACE="${NACOS_NAMESPACE:-}"
GROUP="DEFAULT_GROUP"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_DIR="${SCRIPT_DIR}/../src/main/resources/nacos"

# 上传单个配置文件
upload_file() {
    local data_id=$1
    local file_path=$2
    
    if [ ! -f "$file_path" ]; then
        echo "❌ 文件不存在: $file_path"
        return 1
    fi
    
    echo "📤 上传: $data_id"
    
    local url="http://${NACOS_SERVER}/nacos/v1/cs/configs"
    local data="dataId=${data_id}&group=${GROUP}"
    
    if [ -n "$NAMESPACE" ]; then
        data="${data}&namespaceId=${NAMESPACE}"
    fi
    
    # 读取文件内容并添加到 data
    local content=$(cat "$file_path")
    data="${data}&content=${content}"
    
    # URL 编码（简单处理）
    data=$(echo "$data" | sed 's/ /%20/g' | sed 's/#/%23/g')
    
    local response=$(curl -s -X POST "$url" -d "$data")
    
    if [ "$response" = "true" ]; then
        echo "✅ 成功: $data_id"
        return 0
    else
        echo "❌ 失败: $data_id"
        echo "   响应: $response"
        return 1
    fi
}

# 主函数
echo "============================================"
echo "上传配置文件到 Nacos"
echo "============================================"
echo "Nacos 服务器: $NACOS_SERVER"
echo "命名空间: ${NAMESPACE:-public}"
echo "分组: $GROUP"
echo "============================================"
echo ""

# 检查 Nacos 是否可访问
if ! curl -s "http://${NACOS_SERVER}/nacos/" > /dev/null 2>&1; then
    echo "❌ 无法连接到 Nacos 服务: http://${NACOS_SERVER}"
    echo "   请确保 Nacos 服务正在运行"
    exit 1
fi

# 上传所有配置文件
upload_file "pet-vet-embedding-common.yml" "${CONFIG_DIR}/pet-vet-embedding-common.yml"
upload_file "pet-vet-embedding-dev.yml" "${CONFIG_DIR}/pet-vet-embedding-dev.yml"
upload_file "pet-vet-embedding-test.yml" "${CONFIG_DIR}/pet-vet-embedding-test.yml"
upload_file "pet-vet-embedding-prod.yml" "${CONFIG_DIR}/pet-vet-embedding-prod.yml"

echo ""
echo "============================================"
echo "上传完成"
echo "============================================"

