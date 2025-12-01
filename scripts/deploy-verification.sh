#!/bin/bash

# IOE-DREAM系统部署验证脚本
# 严格遵循D:\IOE-DREAM\docs\repowiki规范

echo "🚀【IOE-DREAM系统部署验证】"

# 步骤1：检查Docker服务状态
echo "📋 步骤1：检查Docker服务状态..."
echo "==============================="

# 检查Redis容器
echo "🔍 Redis容器状态："
docker ps | grep "smart-admin-redis" || echo "❌ Redis容器未运行"

# 检查MySQL容器
echo "🔍 MySQL容器状态："
docker ps | grep "smart-admin-mysql" || echo "❌ MySQL容器未运行"

# 检查RabbitMQ容器
echo "🔍 RabbitMQ容器状态："
docker ps | grep "ecopro-rabbitmq" || echo "❌ RabbitMQ容器未运行"

echo ""

# 步骤2：检查后端服务
echo "📋 步骤2：检查后端服务状态..."
echo "=============================="

# 检查后端容器
BACKEND_STATUS=$(docker ps --format "table {{.Status}}" | grep smart-admin-backend | awk '{print $1}')
if [ -z "$BACKEND_STATUS" ]; then
    echo "🔍 后端容器未运行，尝试启动..."
    docker-compose up -d backend
    sleep 10
    BACKEND_STATUS=$(docker ps --format "table {{.Status}}" | grep smart-admin-backend | awk '{print $1}')
fi

echo "🔍 后端容器状态: ${BACKEND_STATUS:-Unknown}"
echo ""

# 步骤3：服务健康检查
echo "📋 步骤3：服务健康检查..."
echo "=============================="

# Redis连接测试
echo "🔍 Redis连接测试..."
REDIS_TEST=$(docker exec smart-admin-redis redis-cli ping 2>/dev/null)
if [ "$REDIS_TEST" = "PONG" ]; then
    echo "✅ Redis连接正常"
else
    echo "❌ Redis连接失败: $REDIS_TEST"
fi

# MySQL连接测试
echo "🔍 MySQL连接测试..."
MYSQL_TEST=$(docker exec smart-admin-mysql mysqladmin ping -h localhost -u root -proot1234 2>&1)
if [[ "$MYSQL_TEST == *"mysqld"* ]]; then
    echo "✅ MySQL连接正常"
else
    echo "❌ MySQL连接失败: $MYSQL_TEST"
fi

# 后端健康检查
echo "🔍 后端健康检查..."
if [ "$BACKEND_STATUS" = "Up" ]; then
    HEALTH_CHECK=$(docker exec smart-admin-backend curl -f http://localhost:1024/api/health 2>/dev/null)
    if [ $? -eq 0 ]; then
        echo "✅ 后端健康检查通过: $HEALTH_CHECK"
    else
        echo "❌ 后端健康检查失败"
    fi
else
    echo "⏳ 后端容器未运行，跳过健康检查"
fi

echo ""

# 步骤4：服务端口检查
echo "📋 步骤4：服务端口检查..."
echo "=============================="

# 检查端口占用情况
echo "🔍 端口占用检查："
echo "  - Redis (6379): $(netstat -tlnp 2>/dev/null | grep :6379 | wc -l) 个连接"
echo "  - MySQL (3306): $(netstat -tlnp 2>/dev/null | grep :3306 | wc -l) 个连接"
echo "  - RabbitMQ (5672): $(netstat -tlnp 2>/dev/null | grep :5672 | wc -l) 个连接"
echo "  - 后端API (1024): $(netstat -tlnp 2>/dev/null | grep :1024 | wc -l) 个连接"

echo ""

# 步骤5：应用功能验证
echo "📋 步骤5：应用功能验证..."
echo "=============================="

# API访问测试
if [ "$BACKEND_STATUS" = "Up" ]; then
    echo "🔍 API访问测试..."

    # 测试根路径
    ROOT_TEST=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:1024/ 2>/dev/null)
    if [ "$ROOT_TEST" = "404" ]; then
        echo "✅ 根路径返回404（预期，需要认证）"
    else
        echo "🔍 根路径状态码: $ROOT_TEST"
    fi

    # 测试Swagger文档
    SWAGGER_TEST=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:1024/doc.html 2>/dev/null)
    if [ "$SWAGGER_TEST" = "200" ]; then
        echo "✅ Swagger文档可访问"
    else
        echo "🔍 Swagger文档状态码: $SWAGGER_TEST"
    fi

    # 测试健康检查端点
    HEALTH_TEST=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:1024/api/health 2>/dev/null)
    if [ "$HEALTH_TEST" = "200" ]; then
        echo "✅ 健康检查端点正常"
    else
        echo "🔍 健康检查端点状态码: $HEALTH_TEST"
    fi
else
    echo "⏳ 后端容器未运行，跳过API测试"
fi

echo ""

# 步骤6：日志检查
echo "📋 步骤6：日志检查..."
echo "=============================="

if [ "$BACKEND_STATUS" = "Up" ]; then
    echo "🔍 后端容器最新日志（最后20行）："
    docker logs --tail 20 smart-admin-backend
    echo ""

    echo "🔍 错误日志统计："
    ERROR_COUNT=$(docker logs smart-admin-backend 2>&1 | grep -c "ERROR")
    WARN_COUNT=$(docker logs smart-admin-backend 2>&1 | grep -c "WARN")
    echo "  - ERROR数量: $ERROR_COUNT"
    echo "  - WARN数量: $WARN_COUNT"
else
    echo "⏳ 后端容器未运行，无法查看日志"
fi

echo ""

# 最终状态总结
echo "📋 部署状态总结"
echo "=============================="

ALL_SERVICES_UP=true

# 统计运行状态
RUNNING_CONTAINERS=$(docker ps --format "table {{.Names}}\t{{.Status}}" | grep -E "(smart-admin|ecopro)" | grep -c "Up" || echo "0")
TOTAL_CONTAINERS=$(docker ps --format "table {{.Names}}\t{{.Status}}" | grep -E "(smart-admin|ecopro)" | wc -l || echo "0")

echo "📊 服务统计："
echo "  - 运行中容器: $RUNNING_CONTAINERS"
echo "  - 总容器数量: $TOTAL_CONTAINERS"

if [ "$RUNNING_CONTAINERS" -eq 4 ]; then
    echo "🎉 所有服务正常运行！"
    DEPLOY_STATUS="SUCCESS"
elif [ "$RUNNING_CONTAINERS" -eq 3 ]; then
    echo "⚠️ 大部分服务正常运行，但有1个服务异常"
    DEPLOY_STATUS="PARTIAL"
elif [ "$RUNNING_CONTAINERS" -eq 2 ]; then
    echo "❌ 部分服务异常，需要检查"
    DEPLOY_STATUS="PARTIAL"
else
    echo "🚨 严重问题：多个服务异常"
    DEPLOY_STATUS="FAILED"
fi

echo ""
echo "🎯 部署状态: $DEPLOY_STATUS"
echo "🚀 验证完成时间: $(date '+%Y-%m-%d %H:%M:%S')"

# 如果部署成功，提供访问信息
if [ "$DEPLOY_STATUS" = "SUCCESS" ]; then
    echo ""
    echo "🌟️ 服务访问地址："
    echo "  - 后端API: http://localhost:1024"
    echo "  - Swagger文档: http://localhost:1024/doc.html"
    echo "  - Redis: localhost:6379"
    echo "  - MySQL: localhost:3306"
    echo "  - RabbitMQ: http://localhost:15672 (管理界面)"
    echo ""
    echo "🎉 IOE-DREAM系统部署验证完成！"
fi