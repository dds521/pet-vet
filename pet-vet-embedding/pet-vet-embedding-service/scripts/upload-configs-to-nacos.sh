#!/bin/bash

# ============================================
# 将 PetVet Embedding 配置文件上传到 Nacos
# ============================================

# 配置参数
NACOS_SERVER="${NACOS_SERVER_ADDR:-127.0.0.1:8848}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"
NAMESPACE="${NACOS_NAMESPACE:-}"
GROUP="DEFAULT_GROUP"

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_DIR="${SCRIPT_DIR}/../src/main/resources/nacos"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印信息
info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Nacos 服务是否可用
check_nacos() {
    info "检查 Nacos 服务连接..."
    if curl -s "http://${NACOS_SERVER}/nacos/v1/console/health" > /dev/null 2>&1; then
        info "Nacos 服务连接成功: http://${NACOS_SERVER}"
    else
        error "无法连接到 Nacos 服务: http://${NACOS_SERVER}"
        error "请确保 Nacos 服务正在运行"
        exit 1
    fi
}

# 获取访问令牌
get_access_token() {
    local response=$(curl -s -X POST "http://${NACOS_SERVER}/nacos/v1/auth/login" \
        -d "username=${NACOS_USERNAME}&password=${NACOS_PASSWORD}")
    
    local token=$(echo "$response" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
    
    if [ -z "$token" ]; then
        error "获取访问令牌失败，请检查用户名和密码"
        error "响应: $response"
        exit 1
    fi
    
    echo "$token"
}

# 上传配置文件到 Nacos
upload_config() {
    local data_id=$1
    local file_path=$2
    local namespace_param=""
    
    if [ -n "$NAMESPACE" ]; then
        namespace_param="&namespaceId=${NAMESPACE}"
    fi
    
    if [ ! -f "$file_path" ]; then
        error "配置文件不存在: $file_path"
        return 1
    fi
    
    # 读取文件内容并进行 URL 编码
    local content=$(cat "$file_path")
    
    # 获取访问令牌
    local token=$(get_access_token)
    
    info "上传配置: ${data_id} (${file_path})"
    
    # 构建 URL
    local url="http://${NACOS_SERVER}/nacos/v1/cs/configs?dataId=${data_id}&group=${GROUP}&content=$(echo -n "$content" | sed 's/ /%20/g' | sed 's/&/%26/g' | sed 's/=/%3D/g' | sed 's/#/%23/g' | sed 's/\n/%0A/g')${namespace_param}"
    
    # 使用 POST 方法上传（更可靠的方式）
    local response=$(curl -s -X POST "http://${NACOS_SERVER}/nacos/v1/cs/configs" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "dataId=${data_id}&group=${GROUP}&content=$(printf '%s' "$content" | sed 's/&/%26/g' | sed 's/=/%3D/g' | sed 's/#/%23/g' | sed 's/\n/%0A/g')${namespace_param}&accessToken=${token}")
    
    if [ "$response" = "true" ]; then
        info "✓ 配置上传成功: ${data_id}"
        return 0
    else
        error "✗ 配置上传失败: ${data_id}"
        error "响应: $response"
        return 1
    fi
}

# 使用更简单的方法：直接使用 curl 的 --data-urlencode
upload_config_v2() {
    local data_id=$1
    local file_path=$2
    local namespace_param=""
    
    if [ -n "$NAMESPACE" ]; then
        namespace_param="&namespaceId=${NAMESPACE}"
    fi
    
    if [ ! -f "$file_path" ]; then
        error "配置文件不存在: $file_path"
        return 1
    fi
    
    # 获取访问令牌
    local token=$(get_access_token)
    
    info "上传配置: ${data_id} (${file_path})"
    
    # 读取文件内容
    local content=$(cat "$file_path")
    
    # 使用 curl 上传配置
    local response=$(curl -s -X POST "http://${NACOS_SERVER}/nacos/v1/cs/configs" \
        --data-urlencode "dataId=${data_id}" \
        --data-urlencode "group=${GROUP}" \
        --data-urlencode "content=${content}" \
        --data-urlencode "accessToken=${token}" \
        ${namespace_param:+--data-urlencode "namespaceId=${NAMESPACE}"})
    
    if [ "$response" = "true" ]; then
        info "✓ 配置上传成功: ${data_id}"
        return 0
    else
        error "✗ 配置上传失败: ${data_id}"
        error "响应: $response"
        return 1
    fi
}

# 主函数
main() {
    info "============================================"
    info "PetVet Embedding 配置文件上传工具"
    info "============================================"
    info "Nacos 服务器: ${NACOS_SERVER}"
    info "命名空间: ${NAMESPACE:-public}"
    info "分组: ${GROUP}"
    info "配置目录: ${CONFIG_DIR}"
    info "============================================"
    echo ""
    
    # 检查 Nacos 服务
    check_nacos
    
    # 检查配置目录
    if [ ! -d "$CONFIG_DIR" ]; then
        error "配置目录不存在: $CONFIG_DIR"
        exit 1
    fi
    
    # 上传配置文件
    local success_count=0
    local fail_count=0
    
    # 上传公共配置
    if upload_config_v2 "pet-vet-embedding-common.yml" "${CONFIG_DIR}/pet-vet-embedding-common.yml"; then
        ((success_count++))
    else
        ((fail_count++))
    fi
    
    # 上传开发环境配置
    if upload_config_v2 "pet-vet-embedding-dev.yml" "${CONFIG_DIR}/pet-vet-embedding-dev.yml"; then
        ((success_count++))
    else
        ((fail_count++))
    fi
    
    # 上传测试环境配置
    if upload_config_v2 "pet-vet-embedding-test.yml" "${CONFIG_DIR}/pet-vet-embedding-test.yml"; then
        ((success_count++))
    else
        ((fail_count++))
    fi
    
    # 上传生产环境配置
    if upload_config_v2 "pet-vet-embedding-prod.yml" "${CONFIG_DIR}/pet-vet-embedding-prod.yml"; then
        ((success_count++))
    else
        ((fail_count++))
    fi
    
    echo ""
    info "============================================"
    info "上传完成"
    info "成功: ${success_count} 个"
    info "失败: ${fail_count} 个"
    info "============================================"
    
    if [ $fail_count -gt 0 ]; then
        exit 1
    fi
}

# 运行主函数
main

