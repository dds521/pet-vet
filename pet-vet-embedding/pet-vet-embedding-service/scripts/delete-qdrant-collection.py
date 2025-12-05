#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
删除 Qdrant 集合脚本
用于解决向量维度不匹配的问题

用法:
    python delete-qdrant-collection.py [collection_name]

示例:
    python delete-qdrant-collection.py pet-vet-embeddings
"""

import sys
import os
import requests
from typing import Optional

# Qdrant 配置（从环境变量或默认值读取）
QDRANT_HOST = os.getenv("QDRANT_HOST", "https://e2b72e30-6371-4b2f-a925-af91477e9f25.europe-west3-0.gcp.cloud.qdrant.io")
QDRANT_PORT = int(os.getenv("QDRANT_PORT", "6334"))
QDRANT_API_KEY = os.getenv("QDRANT_API_KEY", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3MiOiJtIiwiZXhwIjoyMDgwMjk0MjkzfQ.3cJ-2asZdugoSgPYDs40UCPZPdD6BemNcLNm9D0ELSs")
DEFAULT_COLLECTION_NAME = "pet-vet-embeddings"


def get_base_url() -> str:
    """获取 Qdrant 基础 URL"""
    host = QDRANT_HOST
    if host.startswith("https://"):
        host = host.replace("https://", "")
    elif host.startswith("http://"):
        host = host.replace("http://", "")
    
    protocol = "https" if QDRANT_PORT == 6334 else "http"
    return f"{protocol}://{host}:{QDRANT_PORT}"


def delete_collection(collection_name: str) -> tuple[bool, str]:
    """
    删除 Qdrant 集合
    
    Args:
        collection_name: 集合名称
        
    Returns:
        (成功标志, 消息)
    """
    base_url = get_base_url()
    url = f"{base_url}/collections/{collection_name}"
    
    headers = {
        "api-key": QDRANT_API_KEY,
        "Content-Type": "application/json"
    }
    
    try:
        print(f"正在删除集合: {collection_name}")
        print(f"Qdrant URL: {base_url}")
        
        response = requests.delete(url, headers=headers, timeout=30)
        
        if response.status_code == 200:
            return True, f"✓ 集合 '{collection_name}' 删除成功"
        elif response.status_code == 404:
            return False, f"✗ 集合 '{collection_name}' 不存在"
        else:
            return False, f"✗ 删除失败: {response.status_code} - {response.text}"
            
    except requests.exceptions.RequestException as e:
        return False, f"✗ 请求失败: {str(e)}"


def check_collection_exists(collection_name: str) -> tuple[bool, Optional[dict]]:
    """
    检查集合是否存在
    
    Args:
        collection_name: 集合名称
        
    Returns:
        (是否存在, 集合信息)
    """
    base_url = get_base_url()
    url = f"{base_url}/collections/{collection_name}"
    
    headers = {
        "api-key": QDRANT_API_KEY,
        "Content-Type": "application/json"
    }
    
    try:
        response = requests.get(url, headers=headers, timeout=30)
        
        if response.status_code == 200:
            collection_info = response.json()
            return True, collection_info
        elif response.status_code == 404:
            return False, None
        else:
            return False, None
            
    except requests.exceptions.RequestException:
        return False, None


def main():
    """主函数"""
    # 获取集合名称
    if len(sys.argv) > 1:
        collection_name = sys.argv[1]
    else:
        collection_name = DEFAULT_COLLECTION_NAME
    
    print("=" * 60)
    print("Qdrant 集合删除工具")
    print("=" * 60)
    print()
    
    # 检查集合是否存在
    print(f"检查集合是否存在: {collection_name}")
    exists, collection_info = check_collection_exists(collection_name)
    
    if not exists:
        print(f"✗ 集合 '{collection_name}' 不存在，无需删除")
        return
    
    if collection_info:
        print(f"✓ 集合 '{collection_name}' 存在")
        if "config" in collection_info and "params" in collection_info["config"]:
            params = collection_info["config"]["params"]
            if "vectors" in params and "size" in params["vectors"]:
                dimension = params["vectors"]["size"]
                print(f"  当前维度: {dimension}")
        print()
    
    # 确认删除
    print(f"⚠️  警告: 即将删除集合 '{collection_name}'")
    print("   此操作不可恢复，集合中的所有向量数据将被永久删除！")
    print()
    
    confirm = input("确认删除？(yes/no): ").strip().lower()
    if confirm not in ["yes", "y"]:
        print("取消删除操作")
        return
    
    # 执行删除
    print()
    success, message = delete_collection(collection_name)
    print(message)
    
    if success:
        print()
        print("=" * 60)
        print("删除成功！")
        print("现在可以重新启动应用，集合将使用新的维度自动创建")
        print("=" * 60)
    else:
        print()
        print("=" * 60)
        print("删除失败，请检查:")
        print("  1. Qdrant 服务是否可访问")
        print("  2. API Key 是否正确")
        print("  3. 集合名称是否正确")
        print("=" * 60)


if __name__ == "__main__":
    main()

