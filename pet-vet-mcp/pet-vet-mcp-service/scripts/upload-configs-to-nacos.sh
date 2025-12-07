#!/bin/bash

# ============================================
# 将 PetVet MCP 配置文件上传到 Nacos
# 适用于本地 Nacos（无需认证）
# ============================================

# 配置参数
NACOS_SERVER="${NACOS_SERVER_ADDR:-127.0.0.1:8848}"
NAMESPACE="${NACOS_NAMESPACE:-}"
GROUP="DEFAULT_GROUP"

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
    if curl -s "http://${NACOS_SERVER}/nacos/v1/console/health" > /dev/null 2>&1; then
        info "✓ Nacos 服务连接成功: http://${NACOS_SERVER}"
        return 0
    else
        error "✗ 无法连接到 Nacos 服务: http://${NACOS_SERVER}"
        error "请确保 Nacos 服务正在运行"
        return 1
    fi
}

# 上传配置文件到 Nacos（无需认证）
upload_config() {
    local data_id=$1
    local file_path=$2
    local namespace_param=""
    
    if [ -n "$NAMESPACE" ]; then
        namespace_param="&namespaceId=${NAMESPACE}"
    fi
    
    if [ ! -f "$file_path" ]; then
        warn "配置文件不存在，跳过: $file_path"
        return 1
    fi
    
    info "📤 上传配置: ${data_id}"
    
    # 读取文件内容
    local content=$(cat "$file_path")
    
    # 使用 curl 上传配置（无需认证）
    local response=$(curl -s -X POST "http://${NACOS_SERVER}/nacos/v1/cs/configs" \
        --data-urlencode "dataId=${data_id}" \
        --data-urlencode "group=${GROUP}" \
        --data-urlencode "content=${content}" \
        ${namespace_param:+--data-urlencode "namespaceId=${NAMESPACE}"})
    
    if [ "$response" = "true" ]; then
        info "  ✅ 配置上传成功: ${data_id}"
        return 0
    else
        error "  ❌ 配置上传失败: ${data_id}"
        error "     响应: $response"
        return 1
    fi
}

# 主函数
main() {
    echo ""
    info "============================================"
    info "PetVet MCP 配置文件上传工具"
    info "============================================"
    info "Nacos 服务器: ${NACOS_SERVER}"
    info "命名空间: ${NAMESPACE:-public}"
    info "分组: ${GROUP}"
    info "配置目录: ${CONFIG_DIR}"
    info "============================================"
    echo ""
    
    # 检查 Nacos 服务
    if ! check_nacos; then
        exit 1
    fi
    
    # 检查配置目录
    if [ ! -d "$CONFIG_DIR" ]; then
        error "配置目录不存在: $CONFIG_DIR"
        exit 1
    fi
    
    # 上传配置文件
    local success_count=0
    local fail_count=0
    local skip_count=0
    
    echo ""
    info "开始上传配置文件..."
    echo ""
    
    # 上传公共配置
    if upload_config "pet-vet-mcp-common.yml" "${CONFIG_DIR}/pet-vet-mcp-common.yml"; then
        ((success_count++))
    else
        ((fail_count++))
    fi
    
    # 上传开发环境配置
    if upload_config "pet-vet-mcp-dev.yml" "${CONFIG_DIR}/pet-vet-mcp-dev.yml"; then
        ((success_count++))
    else
        if [ ! -f "${CONFIG_DIR}/pet-vet-mcp-dev.yml" ]; then
            ((skip_count++))
        else
            ((fail_count++))
        fi
    fi
    
    # 上传测试环境配置（如果存在）
    if [ -f "${CONFIG_DIR}/pet-vet-mcp-test.yml" ]; then
        if upload_config "pet-vet-mcp-test.yml" "${CONFIG_DIR}/pet-vet-mcp-test.yml"; then
            ((success_count++))
        else
            ((fail_count++))
        fi
    else
        ((skip_count++))
        warn "跳过: pet-vet-mcp-test.yml (文件不存在)"
    fi
    
    # 上传生产环境配置（如果存在）
    if [ -f "${CONFIG_DIR}/pet-vet-mcp-prod.yml" ]; then
        if upload_config "pet-vet-mcp-prod.yml" "${CONFIG_DIR}/pet-vet-mcp-prod.yml"; then
            ((success_count++))
        else
            ((fail_count++))
        fi
    else
        ((skip_count++))
        warn "跳过: pet-vet-mcp-prod.yml (文件不存在)"
    fi
    
    echo ""
    info "============================================"
    info "上传完成"
    info "成功: ${success_count} 个"
    if [ $skip_count -gt 0 ]; then
        warn "跳过: ${skip_count} 个"
    fi
    if [ $fail_count -gt 0 ]; then
        error "失败: ${fail_count} 个"
    fi
    info "============================================"
    echo ""
    
    if [ $fail_count -gt 0 ]; then
        exit 1
    fi
}

# 运行主函数
main
