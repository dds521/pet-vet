#!/bin/bash

# Sentinel 规则上传到 Nacos 脚本
# 使用方法: ./upload-to-nacos.sh [nacos-server-addr] [namespace] [username] [password]
# 示例: ./upload-to-nacos.sh 127.0.0.1:8848 "" nacos nacos

# 配置参数（可通过命令行参数或环境变量设置）
NACOS_SERVER=${1:-${NACOS_SERVER_ADDR:-127.0.0.1:8848}}
NAMESPACE=${2:-${NACOS_NAMESPACE:-}}
USERNAME=${3:-${NACOS_USERNAME:-nacos}}
PASSWORD=${4:-${NACOS_PASSWORD:-nacos}}
GROUP_ID="SENTINEL_GROUP"

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Sentinel 规则上传到 Nacos${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "Nacos 地址: ${YELLOW}${NACOS_SERVER}${NC}"
echo -e "命名空间: ${YELLOW}${NAMESPACE:-public}${NC}"
echo -e "用户名: ${YELLOW}${USERNAME}${NC}"
echo -e "分组: ${YELLOW}${GROUP_ID}${NC}"
echo ""

# 构建 Nacos API URL
if [ -z "$NAMESPACE" ]; then
    NACOS_URL="http://${NACOS_SERVER}/nacos/v1/cs/configs"
else
    NACOS_URL="http://${NACOS_SERVER}/nacos/v1/cs/configs?tenant=${NAMESPACE}"
fi

# 上传配置函数
upload_config() {
    local data_id=$1
    local file_path=$2
    
    if [ ! -f "$file_path" ]; then
        echo -e "${RED}错误: 文件不存在: ${file_path}${NC}"
        return 1
    fi
    
    # 读取文件内容并转义（兼容 macOS 和 Linux）
    local content=$(cat "$file_path" | sed 's/"/\\"/g' | awk '{printf "%s\\n", $0}')
    
    # 构建请求参数
    local params="dataId=${data_id}&group=${GROUP_ID}&content=${content}"
    
    # 发送 POST 请求
    local response=$(curl -s -w "\n%{http_code}" -X POST \
        -u "${USERNAME}:${PASSWORD}" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "${params}" \
        "${NACOS_URL}")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ] && [ "$body" = "true" ]; then
        echo -e "${GREEN}✓${NC} ${data_id} 上传成功"
        return 0
    else
        echo -e "${RED}✗${NC} ${data_id} 上传失败 (HTTP: ${http_code}, Response: ${body})"
        return 1
    fi
}

# 上传所有配置
success_count=0
fail_count=0

echo -e "${YELLOW}开始上传配置...${NC}"
echo ""

# 1. 网关流控规则
if upload_config "pet-vet-gateway-gw-flow-rules" "${SCRIPT_DIR}/pet-vet-gateway-gw-flow-rules.json"; then
    ((success_count++))
else
    ((fail_count++))
fi

# 2. 网关API分组
if upload_config "pet-vet-gateway-gw-api-group-rules" "${SCRIPT_DIR}/pet-vet-gateway-gw-api-group-rules.json"; then
    ((success_count++))
else
    ((fail_count++))
fi

# 3. 网关降级规则
if upload_config "pet-vet-gateway-gw-degrade-rules" "${SCRIPT_DIR}/pet-vet-gateway-gw-degrade-rules.json"; then
    ((success_count++))
else
    ((fail_count++))
fi

# 4. 系统保护规则
if upload_config "pet-vet-gateway-system-rules" "${SCRIPT_DIR}/pet-vet-gateway-system-rules.json"; then
    ((success_count++))
else
    ((fail_count++))
fi

# 5. 授权规则
if upload_config "pet-vet-gateway-authority-rules" "${SCRIPT_DIR}/pet-vet-gateway-authority-rules.json"; then
    ((success_count++))
else
    ((fail_count++))
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "上传完成: ${GREEN}成功 ${success_count}${NC} / ${RED}失败 ${fail_count}${NC}"
echo -e "${GREEN}========================================${NC}"

if [ $fail_count -eq 0 ]; then
    echo -e "${GREEN}所有配置已成功上传到 Nacos！${NC}"
    exit 0
else
    echo -e "${RED}部分配置上传失败，请检查错误信息${NC}"
    exit 1
fi

