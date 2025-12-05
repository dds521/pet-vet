#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
将 PetVet Embedding 配置文件上传到 Nacos
使用 Python 实现，更可靠地处理中文和特殊字符
"""

import os
import sys
import urllib.parse
import requests
from pathlib import Path

# 配置参数
NACOS_SERVER = os.getenv('NACOS_SERVER_ADDR', '127.0.0.1:8848')
NACOS_USERNAME = os.getenv('NACOS_USERNAME', 'nacos')
NACOS_PASSWORD = os.getenv('NACOS_PASSWORD', 'nacos')
NAMESPACE = os.getenv('NACOS_NAMESPACE', '')
GROUP = 'DEFAULT_GROUP'

# 脚本目录
SCRIPT_DIR = Path(__file__).parent.absolute()
CONFIG_DIR = SCRIPT_DIR.parent / 'src' / 'main' / 'resources' / 'nacos'

# 颜色输出
class Colors:
    RED = '\033[0;31m'
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    NC = '\033[0m'  # No Color

def info(msg):
    print(f"{Colors.GREEN}[INFO]{Colors.NC} {msg}")

def warn(msg):
    print(f"{Colors.YELLOW}[WARN]{Colors.NC} {msg}")

def error(msg):
    print(f"{Colors.RED}[ERROR]{Colors.NC} {msg}")

def check_nacos():
    """检查 Nacos 服务是否可用"""
    info("检查 Nacos 服务连接...")
    try:
        # 尝试访问配置 API 来检查服务是否可用
        response = requests.get(f"http://{NACOS_SERVER}/nacos/v1/cs/configs?dataId=test&group=DEFAULT_GROUP", timeout=5)
        if response.status_code == 200:
            info(f"Nacos 服务连接成功: http://{NACOS_SERVER}")
            return True
        else:
            error(f"无法连接到 Nacos 服务: http://{NACOS_SERVER} (状态码: {response.status_code})")
            return False
    except Exception as e:
        error(f"无法连接到 Nacos 服务: http://{NACOS_SERVER}")
        error(f"错误: {str(e)}")
        return False

def get_access_token():
    """获取 Nacos 访问令牌"""
    try:
        url = f"http://{NACOS_SERVER}/nacos/v1/auth/login"
        data = {
            'username': NACOS_USERNAME,
            'password': NACOS_PASSWORD
        }
        response = requests.post(url, data=data, timeout=5)
        
        if response.status_code == 200:
            result = response.json()
            token = result.get('accessToken', '')
            if token:
                return token
            else:
                error("获取访问令牌失败，响应中没有 accessToken")
                error(f"响应: {response.text}")
                return None
        else:
            error(f"获取访问令牌失败，状态码: {response.status_code}")
            error(f"响应: {response.text}")
            return None
    except Exception as e:
        error(f"获取访问令牌时发生错误: {str(e)}")
        return None

def upload_config(data_id, file_path):
    """上传配置文件到 Nacos"""
    if not file_path.exists():
        error(f"配置文件不存在: {file_path}")
        return False
    
    # 读取文件内容
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        error(f"读取配置文件失败: {str(e)}")
        return False
    
    info(f"上传配置: {data_id} ({file_path.name})")
    
    # 构建请求参数
    url = f"http://{NACOS_SERVER}/nacos/v1/cs/configs"
    data = {
        'dataId': data_id,
        'group': GROUP,
        'content': content
    }
    
    if NAMESPACE:
        data['namespaceId'] = NAMESPACE
    
    # 尝试获取访问令牌（如果 Nacos 开启了认证）
    token = get_access_token()
    if token:
        data['accessToken'] = token
    
    try:
        response = requests.post(url, data=data, timeout=10)
        
        if response.status_code == 200 and response.text.strip() == 'true':
            info(f"✓ 配置上传成功: {data_id}")
            return True
        else:
            error(f"✗ 配置上传失败: {data_id}")
            error(f"状态码: {response.status_code}")
            error(f"响应: {response.text}")
            return False
    except Exception as e:
        error(f"上传配置时发生错误: {str(e)}")
        return False

def main():
    """主函数"""
    info("=" * 44)
    info("PetVet Embedding 配置文件上传工具")
    info("=" * 44)
    info(f"Nacos 服务器: {NACOS_SERVER}")
    info(f"命名空间: {NAMESPACE if NAMESPACE else 'public'}")
    info(f"分组: {GROUP}")
    info(f"配置目录: {CONFIG_DIR}")
    info("=" * 44)
    print()
    
    # 检查 Nacos 服务（可选，如果失败也继续尝试上传）
    check_nacos()
    
    # 检查配置目录
    if not CONFIG_DIR.exists():
        error(f"配置目录不存在: {CONFIG_DIR}")
        sys.exit(1)
    
    # 配置文件列表
    config_files = [
        ('pet-vet-embedding-common.yml', CONFIG_DIR / 'pet-vet-embedding-common.yml'),
        ('pet-vet-embedding-dev.yml', CONFIG_DIR / 'pet-vet-embedding-dev.yml'),
        ('pet-vet-embedding-test.yml', CONFIG_DIR / 'pet-vet-embedding-test.yml'),
        ('pet-vet-embedding-prod.yml', CONFIG_DIR / 'pet-vet-embedding-prod.yml'),
    ]
    
    # 上传配置文件
    success_count = 0
    fail_count = 0
    
    for data_id, file_path in config_files:
        if upload_config(data_id, file_path):
            success_count += 1
        else:
            fail_count += 1
    
    print()
    info("=" * 44)
    info("上传完成")
    info(f"成功: {success_count} 个")
    info(f"失败: {fail_count} 个")
    info("=" * 44)
    
    if fail_count > 0:
        sys.exit(1)

if __name__ == '__main__':
    main()

