#!/usr/bin/env bash

##
## PetVet MCP Demo Curl 脚本
##
## 说明：
## - 用于验证 pet-vet-mcp 服务在两种模式下的调用链路：
##   1）本地 stdio MCP（以 filesystem 为例）；
##   2）远程 HTTP/SSE MCP（以 serverName=demo-remote-http 为例，需要你在 mcp-servers.json 中配置）。
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
echo "=== Demo2：远程 HTTP/SSE MCP（示例 serverName=demo-remote-http）- 列出工具 ==="
echo "提示：需要在 mcp-servers.json 中配置 demo-remote-http，且远程 MCP Server 可访问。"
curl -sS -X GET "${BASE_URL}/api/mcp/servers/demo-remote-http/tools" \
  -H "Accept: application/json" | jq .

echo
echo "=== Demo2：远程 HTTP/SSE MCP（demo-remote-http）- 调用工具示例 ==="
echo "请根据上一步返回的 tools 列表中的 name 字段，替换下面 JSON 中的 toolName。"
curl -sS -X POST "${BASE_URL}/api/mcp/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "serverName": "demo-remote-http",
    "toolName": "请替换为实际工具名",
    "arguments": {}
  }' | jq .

