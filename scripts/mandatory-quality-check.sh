#!/bin/bash

# 强制性质量检查脚本 - 必须全部通过才能继续任务
# 用法: ./scripts/mandatory-quality-check.sh

set -e  # 任何命令失败立即退出

echo "🔒 开始强制质量检查..."

# 第一层：完整构建验证
echo "📦 第一层：完整构建验证"
cd smart-admin-api-java17-springboot3

echo "  执行完整打包..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ 构建失败，任务终止"
    exit 1
fi
echo "  ✅ 完整打包通过"

echo "  检查 javax 包使用..."
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
if [ $javax_count -ne 0 ]; then
    echo "❌ 发现 javax 包使用: $javax_count 个文件"
    find . -name "*.java" -exec grep -l "javax\." {} \;
    exit 1
fi
echo "  ✅ javax 包检查通过"

echo "  检查 @Autowired 使用..."
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -ne 0 ]; then
    echo "❌ 发现 @Autowired 使用: $autowired_count 个文件"
    find . -name "*.java" -exec grep -l "@Autowired" {} \;
    exit 1
fi
echo "  ✅ @Autowired 检查通过"

# 第二层：MyBatis完整性验证
echo "🗄️ 第二层：MyBatis完整性验证"
find . -name "*.xml" -path "*/mapper/*" -exec sh -c '
    mapper_file="$1"
    echo "  检查文件: $mapper_file"
    entities=$(grep -o "resultType=\"[^\"]*Entity\"" "$mapper_file" | sed "s/resultType=\"//" | sed "s/\"//")
    for entity in $entities; do
        entity_file=$(echo "$entity" | sed "s/\./\//g").java
        if [ ! -f "$entity_file" ]; then
            echo "❌ Mapper $mapper_file 引用的实体类不存在: $entity"
            echo "   查找路径: $entity_file"
            exit 1
        fi
    done
' _ {} \;
echo "  ✅ MyBatis完整性验证通过"

# 第三层：Spring Boot启动验证
echo "🚀 第三层：Spring Boot启动验证"
cd sa-admin

echo "  执行Spring Boot启动测试..."
timeout 90s mvn spring-boot:run -Dspring-boot.run.profiles=docker > ../startup_test.log 2>&1 &
pid=$!

# 等待启动
sleep 60

# 检查进程状态
if ps -p $pid > /dev/null; then
    kill $pid
    wait $pid 2>/dev/null || true
    echo "  ✅ Spring Boot启动测试通过"
else
    wait $pid
    if grep -q "Application run failed\|ERROR\|Exception" ../startup_test.log; then
        echo "❌ Spring Boot启动失败"
        echo "错误详情:"
        tail -30 ../startup_test.log
        exit 1
    fi
fi

# 第四层：Docker部署验证
echo "🐳 第四层：Docker部署验证"
cd ../..

echo "  构建Docker镜像..."
docker-compose build backend
if [ $? -ne 0 ]; then
    echo "❌ Docker镜像构建失败"
    exit 1
fi
echo "  ✅ Docker镜像构建成功"

echo "  启动Docker容器..."
docker-compose up -d backend
sleep 30

echo "  检查容器状态..."
container_status=$(docker-compose ps | grep backend | grep -c "Up")
if [ $container_status -ne 1 ]; then
    echo "❌ Docker容器启动失败"
    docker logs smart-admin-backend --tail 50
    exit 1
fi
echo "  ✅ Docker容器启动成功"

echo "  检查容器健康状态..."
health_check=$(docker logs smart-admin-backend 2>&1 | tail -10)
if echo "$health_check" | grep -q "ERROR\|Exception\|Failed"; then
    echo "⚠️  容器启动但有错误，检查详细日志..."
    docker logs smart-admin-backend --tail 30
    echo "❌ 容器健康检查失败"
    exit 1
fi
echo "  ✅ Docker容器健康检查通过"

echo ""
echo "🎉 所有质量检查通过！可以继续下一个任务。"
echo "🔍 健康检查摘要:"
echo "  - ✅ 完整构建验证"
echo "  - ✅ MyBatis完整性验证"
echo "  - ✅ Spring Boot启动验证"
echo "  - ✅ Docker部署验证"
echo ""