# Nacos 配置文件上传脚本使用说明

## 快速开始

```bash
# 1. 进入服务目录
cd pet-vet-mcp/pet-vet-mcp-service

# 2. 运行上传脚本
./scripts/upload-configs-to-nacos.sh
```

## 环境变量配置

```bash
# 自定义 Nacos 地址
export NACOS_SERVER_ADDR=127.0.0.1:8848

# 指定命名空间（可选）
export NACOS_NAMESPACE=dev

# 运行脚本
./scripts/upload-configs-to-nacos.sh
```

## 上传的配置文件

- `pet-vet-mcp-common.yml` - 公共配置
- `pet-vet-mcp-dev.yml` - 开发环境配置

## 注意事项

- 确保本地 Nacos 服务正在运行
- 本地 Nacos 无需认证（脚本已适配）
- 配置文件会自动上传到 `DEFAULT_GROUP` 分组
