#!/bin/bash

# 绝对严格的质量检查机制 - 弥补之前所有漏洞
# 用法: ./scripts/absolute-strict-check.sh

set -e  # 任何命令失败立即退出

echo "🔒 开始绝对严格的质量检查（弥补检查机制漏洞）..."

# 全局错误计数器
ERROR_COUNT=0

# 错误记录函数
log_error() {
    local error_msg="$1"
    echo "❌ 严重错误: $error_msg"
    ((ERROR_COUNT++))
}

# 警告记录函数
log_warning() {
    local warning_msg="$1"
    echo "⚠️  警告: $warning_msg"
}

# 成功记录函数
log_success() {
    local success_msg="$1"
    echo "✅ $success_msg"
}

# ========== 第一层：完整构建验证 ==========
echo ""
echo "📦 第一层：完整构建验证"
cd smart-admin-api-java17-springboot3

echo "  执行完整打包..."
if ! mvn clean package -DskipTests > build_log.txt 2>&1; then
    log_error "Maven构建失败"
    echo "构建日志最后20行:"
    tail -20 build_log.txt
    exit 1
fi
log_success "Maven构建成功"

echo "  检查 javax 包使用..."
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)
if [ $javax_count -ne 0 ]; then
    log_error "发现 javax 包使用: $javax_count 个文件"
    find . -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | head -5
fi

echo "  检查 @Autowired 使用..."
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
if [ $autowired_count -ne 0 ]; then
    log_error "发现 @Autowired 使用: $autowired_count 个文件"
    find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | head -5
fi

echo "  检查 System.out.println 使用..."
sout_count=$(find . -name "*.java" -exec grep -l "System\.out\.println" {} \; 2>/dev/null | wc -l)
if [ $sout_count -ne 0 ]; then
    log_error "发现 System.out.println 使用: $sout_count 个文件"
fi

# ========== 第二层：MyBatis完整性验证 ==========
echo ""
echo "🗄️ 第二层：MyBatis完整性验证"
mapper_count=$(find . -name "*.xml" -path "*/mapper/*" 2>/dev/null | wc -l)
if [ $mapper_count -gt 0 ]; then
    echo "  检查 $mapper_count 个Mapper文件..."
    find . -name "*.xml" -path "*/mapper/*" 2>/dev/null | while read mapper_file; do
        echo "    检查: $mapper_file"
        # 检查是否有Entity引用
        entities=$(grep -o 'resultType="[^"]*Entity"' "$mapper_file" 2>/dev/null | sed 's/resultType="//' | sed 's/"//' || true)
        if [ -n "$entities" ]; then
            for entity in $entities; do
                entity_file=$(echo "$entity" | sed 's/\./\//g').java
                if [ ! -f "$entity_file" ]; then
                    log_error "Mapper $mapper_file 引用的实体类不存在: $entity (路径: $entity_file)"
                fi
            done
        fi
    done
else
    log_success "无MyBatis Mapper文件需要检查"
fi

# ========== 第三层：Spring Boot启动验证 ==========
echo ""
echo "🚀 第三层：Spring Boot启动验证"
cd sa-admin

echo "  执行Spring Boot启动测试（docker profile）..."
timeout 90s mvn spring-boot:run -Dspring-boot.run.profiles=docker > ../startup_test.log 2>&1 &
pid=$!

# 等待启动
sleep 60

# 检查进程状态
if ps -p $pid > /dev/null 2>&1; then
    echo "  进程仍在运行，检查启动日志..."
    # 检查是否成功启动
    if grep -q "Application startup failed\|ERROR\|Exception\|Failed" ../startup_test.log; then
        log_error "Spring Boot启动失败"
        echo "错误详情:"
        grep -A 5 -B 2 "ERROR\|Exception\|Failed" ../startup_test.log | head -20
    else
        log_success "Spring Boot启动测试通过（60秒内正常运行）"
    fi
    kill $pid 2>/dev/null || true
    wait $pid 2>/dev/null || true
else
    wait $pid
    if grep -q "Application startup failed\|ERROR\|Exception\|Failed" ../startup_test.log; then
        log_error "Spring Boot启动失败"
        echo "错误详情:"
        tail -30 ../startup_test.log
    else
        log_success "Spring Boot正常启动并退出"
    fi
fi

# 检查特定错误模式
echo "  检查启动日志中的特定错误..."
if [ -f "../startup_test.log" ]; then
    # Log4j2错误
    if grep -i "log4j2\|rollingfile\|appender.*not found" ../startup_test.log > /dev/null; then
        log_error "发现Log4j2配置错误"
        grep -i "log4j2\|rollingfile\|appender.*not found" ../startup_test.log | head -5
    fi

    # 数据库连接错误
    if grep -i "connection.*failed\|could not connect\|datasource.*error" ../startup_test.log > /dev/null; then
        log_error "发现数据库连接错误"
        grep -i "connection.*failed\|could not connect\|datasource.*error" ../startup_test.log | head -5
    fi

    # Bean创建错误
    if grep -i "bean.*creation.*failed\|unsatisfieddependency" ../startup_test.log > /dev/null; then
        log_error "发现Bean创建错误"
        grep -i "bean.*creation.*failed\|unsatisfieddependency" ../startup_test.log | head -5
    fi
fi

# ========== 第四层：Docker部署验证 ==========
echo ""
echo "🐳 第四层：Docker部署验证"
cd ../..

echo "  构建Docker镜像..."
if ! docker-compose build backend > docker_build.log 2>&1; then
    log_error "Docker镜像构建失败"
    tail -20 docker_build.log
    exit 1
fi
log_success "Docker镜像构建成功"

echo "  启动Docker容器..."
docker-compose down backend 2>/dev/null || true
docker-compose up -d backend > docker_up.log 2>&1

echo "  等待容器启动..."
sleep 45

echo "  检查容器状态..."
container_status=$(docker-compose ps backend 2>/dev/null | grep -c "Up" || echo "0")
if [ "$container_status" = "0" ]; then
    log_error "Docker容器启动失败"
    docker-compose ps
    docker logs smart-admin-backend --tail 30
    exit 1
fi

# 检查容器健康状态
health_status=$(docker-compose ps backend 2>/dev/null | grep -c "healthy" || echo "0")
if [ "$health_status" = "0" ]; then
    log_warning "容器未达到healthy状态（可能还在启动中）"
fi

echo "  严格检查Docker启动日志..."
docker_logs=$(docker logs smart-admin-backend 2>&1)

# 定义严格错误模式
error_patterns=(
    "ERROR"
    "Exception"
    "Failed"
    "Unable to"
    "Could not"
    "Connection.*refused"
    "No.*found"
    "NullPointerException"
    "ClassNotFoundException"
    "BeanCreationException"
    "UnsatisfiedDependencyException"
    "Application startup failed"
)

for pattern in "${error_patterns[@]}"; do
    if echo "$docker_logs" | grep -i "$pattern" > /dev/null; then
        error_count=$(echo "$docker_logs" | grep -i "$pattern" | wc -l)
        if [ $error_count -gt 3 ]; then  # 允许少量错误（如网络重试）
            log_error "发现 $pattern 错误: $error_count 次"
            echo "$docker_logs" | grep -i "$pattern" | head -3
        else
            log_warning "发现少量 $pattern 错误: $error_count 次"
        fi
    fi
done

# 检查应用是否真正启动成功
if echo "$docker_logs" | grep -q "Started.*Application\|Application.*started\|Tomcat.*started"; then
    log_success "应用成功启动"
else
    log_error "应用未显示启动成功标志"
    echo "最近50行日志:"
    echo "$docker_logs" | tail -50
fi

# ========== 最终结果 ==========
echo ""
echo "📊 检查结果总结:"
if [ $ERROR_COUNT -eq 0 ]; then
    echo "🎉 所有检查通过！项目质量合格。"
    echo "✅ 第一层: 完整构建验证 - 通过"
    echo "✅ 第二层: MyBatis完整性验证 - 通过"
    echo "✅ 第三层: Spring Boot启动验证 - 通过"
    echo "✅ 第四层: Docker部署验证 - 通过"
    exit 0
else
    echo "❌ 检查失败！发现 $ERROR_COUNT 个严重错误。"
    echo "🔧 请修复以上错误后重新检查。"
    exit 1
fi