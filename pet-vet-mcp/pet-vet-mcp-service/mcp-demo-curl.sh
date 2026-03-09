#!/usr/bin/env bash

##
## PetVet MCP Demo Curl 脚本
##
## 说明：
## - 用于验证 pet-vet-mcp 服务在两种模式下的调用链路：
##   1）本地 stdio MCP（以 filesystem 为例）；
##   2）远程 HTTP MCP（以 DeepWiki 官方 MCP Server 为例，serverName=deepwiki）。
## - 默认服务地址：http://localhost:48083（见 application.yml 的 server.port）。
##

BASE_URL="${BASE_URL:-http://localhost:48083}"

echo "使用 BASE_URL=${BASE_URL}"

echo
echo "=== Demo1：本地 stdio MCP（filesystem）- 列出工具 ==="
curl -sS -X GET "${BASE_URL}/api/mcp/demo/stdio/filesystem/tools" \
  -H "Accept: application/json" | jq .

echo
echo "=== Demo1：本地 stdio MCP（filesystem）- 调用第一个工具（不带参数） ==="
curl -sS -X POST "${BASE_URL}/api/mcp/demo/stdio/filesystem/call-first-tool" \
  -H "Content-Type: application/json" \
  -d '{}' | jq .

echo
echo "=== Demo2：远程 HTTP MCP（DeepWiki，serverName=deepwiki）- 列出工具 ==="
echo "提示：需要网络可以访问 https://mcp.deepwiki.com/mcp。"
curl -sS -X GET "${BASE_URL}/api/mcp/servers/deepwiki/tools" \
  -H "Accept: application/json" | jq .

echo
echo "=== Demo2：远程 HTTP MCP（DeepWiki）- 调用 ask_question 工具示例 ==="
echo "该示例会向 DeepWiki 提一个关于 GitHub 仓库的简单问题。"
curl -sS -X POST "${BASE_URL}/api/mcp/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "serverName": "deepwiki",
    "toolName": "ask_question",
    "arguments": {
      "question": "简要介绍一下 GitHub 仓库 rose-compiler/rose-full 是做什么的？请用中文回答。"
    }
  }' | jq .

