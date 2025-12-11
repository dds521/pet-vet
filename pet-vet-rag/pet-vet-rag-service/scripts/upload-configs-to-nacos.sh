#!/bin/bash

# ============================================
# 将 PetVetRAG 配置文件上传到 Nacos
# 支持本地 Nacos（无需认证）和带认证的 Nacos
# ============================================

# 配置参数
NACOS_SERVER="${NACOS_SERVER_ADDR:-127.0.0.1:8848}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"
NAMESPACE="${NACOS_NAMESPACE:-}"
GROUP="DEFAULT_GROUP"
USE_AUTH="${NACOS_USE_AUTH:-false}"

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_DIR="${SCRIPT_DIR}/../src/main/resources/nacos"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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
    if curl -s "http://${NACOS_SERVER}/nacos/" > /dev/null 2>&1 || curl -s "http://${NACOS_SERVER}/nacos/v1/console/health" > /dev/null 2>&1; then
        info "✓ Nacos 服务连接成功: http://${NACOS_SERVER}"
    else
        error "✗ 无法连接到 Nacos 服务: http://${NACOS_SERVER}"
        error "   请确保 Nacos 服务正在运行"
        exit 1
    fi
}

# 获取访问令牌（如果需要认证）
get_access_token() {
    if [ "$USE_AUTH" != "true" ]; then
        echo ""
        return 0
    fi
    
    local response=$(curl -s -X POST "http://${NACOS_SERVER}/nacos/v1/auth/login" \
        -d "username=${NACOS_USERNAME}&password=${NACOS_PASSWORD}")
    
    local token=$(echo "$response" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
    
    if [ -z "$token" ]; then
        warn "获取访问令牌失败，将尝试不使用认证上传"
        warn "响应: $response"
        echo ""
        return 1
    fi
    
    echo "$token"
}

# 上传单个配置文件
upload_file() {
    local data_id=$1
    local file_path=$2
    
    if [ ! -f "$file_path" ]; then
        warn "文件不存在，跳过: $file_path"
        return 1
    fi
    
    info "📤 上传: ${data_id}"
    
    # 获取访问令牌（如果需要）
    local token=""
    if [ "$USE_AUTH" = "true" ]; then
        token=$(get_access_token)
    fi
    
    # 构建 curl 参数数组
    local curl_args=(
        -s
        -X POST
        "http://${NACOS_SERVER}/nacos/v1/cs/configs"
        --data-urlencode "dataId=${data_id}"
        --data-urlencode "group=${GROUP}"
    )
    
    # 添加命名空间参数
    if [ -n "$NAMESPACE" ]; then
        curl_args+=(--data-urlencode "namespaceId=${NAMESPACE}")
    fi
    
    # 添加访问令牌参数
    if [ -n "$token" ]; then
        curl_args+=(--data-urlencode "accessToken=${token}")
    fi
    
    # 使用临时文件来传递内容，避免命令行长度限制和特殊字符问题
    local temp_file=$(mktemp)
    cat "$file_path" > "$temp_file"
    
    # 添加内容参数（从文件读取）
    curl_args+=(--data-urlencode "content@${temp_file}")
    
    # 执行上传
    local response=$(curl "${curl_args[@]}")
    
    # 清理临时文件
    rm -f "$temp_file"
    
    if [ "$response" = "true" ]; then
        info "  ✓ 成功: ${data_id}"
        return 0
    else
        error "  ✗ 失败: ${data_id}"
        error "    响应: ${response}"
        return 1
    fi
}

# 主函数
main() {
    echo ""
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}PetVetRAG 配置文件上传工具${NC}"
    echo -e "${BLUE}============================================${NC}"
    info "Nacos 服务器: ${NACOS_SERVER}"
    info "命名空间: ${NAMESPACE:-public}"
    info "分组: ${GROUP}"
    if [ "$USE_AUTH" = "true" ]; then
        info "认证: 启用 (用户名: ${NACOS_USERNAME})"
    else
        info "认证: 禁用"
    fi
    info "配置目录: ${CONFIG_DIR}"
    echo -e "${BLUE}============================================${NC}"
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
    
    echo ""
    info "开始上传配置文件..."
    echo ""
    
    # 上传公共配置
    if upload_file "pet-vet-rag-common.yml" "${CONFIG_DIR}/pet-vet-rag-common.yml"; then
        ((success_count++))
    else
        ((fail_count++))
    fi
    
    # 上传开发环境配置
    if upload_file "pet-vet-rag-dev.yml" "${CONFIG_DIR}/pet-vet-rag-dev.yml"; then
        ((success_count++))
    else
        ((fail_count++))
    fi
    
    # 上传测试环境配置（如果存在）
    if [ -f "${CONFIG_DIR}/pet-vet-rag-test.yml" ]; then
        if upload_file "pet-vet-rag-test.yml" "${CONFIG_DIR}/pet-vet-rag-test.yml"; then
            ((success_count++))
        else
            ((fail_count++))
        fi
    fi
    
    # 上传生产环境配置（如果存在）
    if [ -f "${CONFIG_DIR}/pet-vet-rag-prod.yml" ]; then
        if upload_file "pet-vet-rag-prod.yml" "${CONFIG_DIR}/pet-vet-rag-prod.yml"; then
            ((success_count++))
        else
            ((fail_count++))
        fi
    fi
    
    echo ""
    echo -e "${BLUE}============================================${NC}"
    if [ $fail_count -eq 0 ]; then
        info "✓ 上传完成！"
        info "  成功: ${success_count} 个配置文件"
    else
        warn "上传完成（部分失败）"
        info "  成功: ${success_count} 个"
        error "  失败: ${fail_count} 个"
    fi
    echo -e "${BLUE}============================================${NC}"
    echo ""
    
    if [ $fail_count -gt 0 ]; then
        exit 1
    fi
}

# 运行主函数
main
