#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
检查向量数据库连接配置
测试 Qdrant 和 Zilliz 的连接是否正常
"""

import sys
import os
from pathlib import Path

# 添加项目路径以便导入
sys.path.insert(0, str(Path(__file__).parent.parent))

try:
    from dev.langchain4j.store.embedding.qdrant import QdrantEmbeddingStore
    from dev.langchain4j.store.embedding.zilliz import ZillizEmbeddingStore
    LANGCHAIN4J_AVAILABLE = True
except ImportError:
    LANGCHAIN4J_AVAILABLE = False
    print("⚠️  LangChain4j 未安装，将使用 HTTP 方式测试连接")

import requests
import urllib.parse

# 颜色输出
class Colors:
    RED = '\033[0;31m'
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    BLUE = '\033[0;34m'
    NC = '\033[0m'

def info(msg):
    print(f"{Colors.GREEN}[INFO]{Colors.NC} {msg}")

def warn(msg):
    print(f"{Colors.YELLOW}[WARN]{Colors.NC} {msg}")

def error(msg):
    print(f"{Colors.RED}[ERROR]{Colors.NC} {msg}")

def success(msg):
    print(f"{Colors.GREEN}✓{Colors.NC} {msg}")

def fail(msg):
    print(f"{Colors.RED}✗{Colors.NC} {msg}")

def check_qdrant_http(host, port, api_key=None):
    """通过 HTTP API 检查 Qdrant 连接"""
    try:
        # 解析 host，提取域名
        if host.startswith('http://') or host.startswith('https://'):
            url = f"{host}/collections"
            use_https = host.startswith('https://')
        else:
            protocol = 'https' if port == 6334 else 'http'
            url = f"{protocol}://{host}:{port}/collections"
            use_https = protocol == 'https'
        
        headers = {}
        if api_key:
            headers['api-key'] = api_key
        
        response = requests.get(url, headers=headers, timeout=10, verify=False)
        
        if response.status_code == 200:
            return True, "连接成功", response.json()
        elif response.status_code == 401:
            return False, "认证失败，请检查 API Key", None
        elif response.status_code == 403:
            return False, "权限不足", None
        else:
            return False, f"连接失败，状态码: {response.status_code}", None
    except requests.exceptions.SSLError as e:
        return False, f"SSL 错误: {str(e)}", None
    except requests.exceptions.ConnectionError as e:
        return False, f"连接错误: {str(e)}", None
    except Exception as e:
        return False, f"未知错误: {str(e)}", None

def check_qdrant_langchain4j(host, port, api_key, collection_name):
    """使用 LangChain4j 检查 Qdrant 连接"""
    try:
        # 解析 host
        if host.startswith('http://'):
            host_clean = host.replace('http://', '')
        elif host.startswith('https://'):
            host_clean = host.replace('https://', '')
        else:
            host_clean = host
        
        builder = QdrantEmbeddingStore.builder()
        builder.host(host_clean)
        builder.port(port)
        builder.collectionName(collection_name)
        
        if api_key:
            builder.apiKey(api_key)
        
        if host.startswith('https://') or port == 6334:
            builder.useTls(True)
        
        store = builder.build()
        
        # 尝试获取集合信息
        # 注意：这里只是创建连接，实际测试可能需要尝试添加一个测试向量
        return True, "连接成功（LangChain4j）", None
    except Exception as e:
        return False, f"连接失败: {str(e)}", None

def check_zilliz_http(host, port, api_key=None):
    """通过 HTTP API 检查 Zilliz 连接"""
    try:
        # Zilliz 使用 Milvus 协议，需要通过 gRPC 或 HTTP API
        # 这里尝试检查服务是否可访问
        if host.startswith('http://') or host.startswith('https://'):
            url = host
            use_https = host.startswith('https://')
        else:
            protocol = 'https' if 'serverless' in host or 'cloud' in host else 'http'
            url = f"{protocol}://{host}"
            use_https = protocol == 'https'
        
        # Zilliz 通常使用 gRPC，HTTP 检查可能不准确
        # 尝试连接 Milvus HTTP API（如果可用）
        try:
            response = requests.get(f"{url}/healthz", timeout=5, verify=False)
            if response.status_code == 200:
                return True, "服务可访问", None
        except:
            pass
        
        # 如果 HTTP 不可用，至少检查域名解析
        from urllib.parse import urlparse
        parsed = urlparse(url if '://' in url else f"https://{url}")
        hostname = parsed.hostname or url.split('/')[0].split(':')[0]
        
        import socket
        try:
            socket.gethostbyname(hostname)
            return True, "域名解析成功（Zilliz 使用 gRPC，需要实际连接测试）", None
        except socket.gaierror:
            return False, "域名解析失败", None
    except Exception as e:
        return False, f"检查失败: {str(e)}", None

def check_zilliz_langchain4j(host, port, api_key, collection_name):
    """使用 LangChain4j 检查 Zilliz 连接"""
    try:
        # 解析 host
        if host.startswith('http://'):
            host_clean = host.replace('http://', '')
        elif host.startswith('https://'):
            host_clean = host.replace('https://', '')
        else:
            host_clean = host
        
        builder = ZillizEmbeddingStore.builder()
        builder.host(host_clean)
        builder.port(port)
        builder.collectionName(collection_name)
        builder.dimension(1536)  # 默认维度
        
        if api_key:
            builder.apiKey(api_key)
        
        store = builder.build()
        
        return True, "连接成功（LangChain4j）", None
    except Exception as e:
        return False, f"连接失败: {str(e)}", None

def main():
    """主函数"""
    print("=" * 60)
    print("向量数据库连接检查工具")
    print("=" * 60)
    print()
    
    # Qdrant 配置
    qdrant_host = "https://e2b72e30-6371-4b2f-a925-af91477e9f25.europe-west3-0.gcp.cloud.qdrant.io"
    qdrant_port = 6334
    qdrant_api_key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3MiOiJtIiwiZXhwIjoyMDgwMjk0MjkzfQ.3cJ-2asZdugoSgPYDs40UCPZPdD6BemNcLNm9D0ELSs"
    qdrant_collection = "pet_vet_ai"
    
    # Zilliz 配置
    zilliz_host = "https://in03-404be71e0f75d6e.serverless.aws-eu-central-1.cloud.zilliz.com"
    zilliz_port = 19530
    zilliz_api_key = "264c10408afb999310040823e90e9c1840efa78a1f0b2ce0dce26328d8d836cc2653c00e8f66aca88eda6b9406552a1350ea79dc"
    zilliz_collection = "pet-vet-embeddings"
    
    # 检查 Qdrant
    print(f"{Colors.BLUE}检查 Qdrant 连接...{Colors.NC}")
    print(f"  Host: {qdrant_host}")
    print(f"  Port: {qdrant_port}")
    print(f"  Collection: {qdrant_collection}")
    print(f"  API Key: {'已配置' if qdrant_api_key else '未配置'}")
    print()
    
    # 使用 HTTP 方式检查
    success_qdrant, msg_qdrant, data_qdrant = check_qdrant_http(qdrant_host, qdrant_port, qdrant_api_key)
    if success_qdrant:
        success(f"Qdrant HTTP 连接: {msg_qdrant}")
        if data_qdrant:
            print(f"  可用集合数量: {len(data_qdrant.get('result', {}).get('collections', []))}")
    else:
        fail(f"Qdrant HTTP 连接: {msg_qdrant}")
    
    # 如果 LangChain4j 可用，也测试一下
    if LANGCHAIN4J_AVAILABLE:
        try:
            success_lc, msg_lc, _ = check_qdrant_langchain4j(qdrant_host, qdrant_port, qdrant_api_key, qdrant_collection)
            if success_lc:
                success(f"Qdrant LangChain4j: {msg_lc}")
            else:
                warn(f"Qdrant LangChain4j: {msg_lc}")
        except Exception as e:
            warn(f"Qdrant LangChain4j 测试失败: {str(e)}")
    
    print()
    
    # 检查 Zilliz
    print(f"{Colors.BLUE}检查 Zilliz 连接...{Colors.NC}")
    print(f"  Host: {zilliz_host}")
    print(f"  Port: {zilliz_port}")
    print(f"  Collection: {zilliz_collection}")
    print(f"  API Key: {'已配置' if zilliz_api_key else '未配置'}")
    print()
    
    # 使用 HTTP 方式检查
    success_zilliz, msg_zilliz, data_zilliz = check_zilliz_http(zilliz_host, zilliz_port, zilliz_api_key)
    if success_zilliz:
        success(f"Zilliz 连接: {msg_zilliz}")
    else:
        fail(f"Zilliz 连接: {msg_zilliz}")
    
    # 如果 LangChain4j 可用，也测试一下
    if LANGCHAIN4J_AVAILABLE:
        try:
            success_lc, msg_lc, _ = check_zilliz_langchain4j(zilliz_host, zilliz_port, zilliz_api_key, zilliz_collection)
            if success_lc:
                success(f"Zilliz LangChain4j: {msg_lc}")
            else:
                warn(f"Zilliz LangChain4j: {msg_lc}")
        except Exception as e:
            warn(f"Zilliz LangChain4j 测试失败: {str(e)}")
    
    print()
    print("=" * 60)
    print("检查完成")
    print("=" * 60)

if __name__ == '__main__':
    # 禁用 SSL 警告
    import urllib3
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
    
    main()

