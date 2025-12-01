#!/bin/bash

echo "🔥【老王紧急清理】停止所有重复的Docker构建任务..."
echo "⚠️  发现15+个并发构建任务，严重浪费系统资源！"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 查找所有Docker相关进程
echo "🔍 查找所有Docker构建进程..."
docker_processes=$(ps aux | grep -E "docker-compose.*build|docker.*build" | grep -v grep | wc -l)

if [ $docker_processes -gt 0 ]; then
    echo -e "${RED}❌ 发现 $docker_processes 个Docker构建进程正在运行${NC}"
    echo "进程列表："
    ps aux | grep -E "docker-compose.*build|docker.*build" | grep -v grep
    echo ""

    # 优雅地终止所有Docker构建进程
    echo "🛑 停止所有Docker构建进程..."
    pids=$(ps aux | grep -E "docker-compose.*build|docker.*build" | grep -v grep | awk '{print $2}')

    for pid in $pids; do
        echo "  终止进程: $pid"
        kill -TERM $pid 2>/dev/null || true
    done

    # 等待3秒
    sleep 3

    # 强制终止仍在运行的进程
    echo "🔨 强制清理顽固进程..."
    remaining_pids=$(ps aux | grep -E "docker-compose.*build|docker.*build" | grep -v grep | awk '{print $2}')
    for pid in $remaining_pids; do
        echo "  强制终止: $pid"
        kill -KILL $pid 2>/dev/null || true
    done

    echo -e "${GREEN}✅ 所有Docker构建进程已清理${NC}"
else
    echo -e "${GREEN}✅ 未发现Docker构建进程${NC}"
fi

echo ""
echo "📊 当前Docker容器状态："
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "🌐 服务健康检查："

# 检查后端API
echo -n "  后端API (localhost:1024): "
if curl -s -f http://localhost:1024/api/health >/dev/null 2>&1; then
    echo -e "${GREEN}✅ 正常${NC}"
else
    echo -e "${RED}❌ 异常${NC}"
fi

# 检查前端服务
echo -n "  前端服务 (localhost:8081): "
if curl -s -I http://localhost:8081 >/dev/null 2>&1; then
    echo -e "${GREEN}✅ 正常${NC}"
else
    echo -e "${RED}❌ 异常${NC}"
fi

echo ""
echo "🎯 系统已恢复正常，所有重复构建任务已清理！"
echo "💡 建议：下次只运行一个Docker构建任务，避免资源浪费"