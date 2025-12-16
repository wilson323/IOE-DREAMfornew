#!/bin/bash

set -e

# Docker启动脚本
# 用于IOE-DREAM考勤管理服务的容器启动

echo "========================================"
echo "IOE-DREAM Attendance Management Service"
echo "Version: 1.0.0"
echo "Build Date: $(date)"
echo "========================================"

# 设置环境变量
export JAVA_HOME=${JAVA_HOME:-/usr/local/openjdk-17}
export PATH=${JAVA_HOME}/bin:${PATH}

# 等待依赖服务启动
echo "Waiting for dependent services..."

# 等待MySQL启动
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local timeout=${4:-60}

    echo "Waiting for ${service_name} (${host}:${port})..."

    local count=0
    while ! nc -z "$host" "$port" >/dev/null 2>&1; do
        count=$((count + 1))
        if [ "$count" -ge "$timeout" ]; then
            echo "ERROR: ${service_name} not ready after ${timeout} seconds"
            exit 1
        fi
        echo "  Attempt ${count}/${timeout}..."
        sleep 1
    done

    echo "✅ ${service_name} is ready!"
}

# 检查环境变量
check_environment() {
    echo "Checking environment variables..."

    # 检查必需的环境变量
    required_vars=("SPING_DATASOURCE_URL" "SPRING_DATASOURCE_USERNAME" "SPRING_DATASOURCE_PASSWORD")

    for var in "${required_vars[@]}"; do
        if [ -z "${!var}" ]; then
            echo "WARNING: Environment variable $var is not set"
        fi
    done

    echo "✅ Environment variables checked"
}

# 数据库迁移
run_database_migration() {
    echo "Running database migrations..."

    if [ "$SPRING_FLYWAY_ENABLED" = "true" ]; then
        echo "Flyway migration is enabled, skipping manual migration"
        return 0
    fi

    # 这里可以添加自定义的数据库迁移逻辑
    echo "✅ Database migrations completed"
}

# 应用启动前检查
pre_startup_checks() {
    echo "Running pre-startup checks..."

    # 检查JVM内存配置
    if [ -n "$JAVA_XMS" ]; then
        echo "Java initial memory: $JAVA_XMS"
    fi

    if [ -n "$JAVA_XMX" ]; then
        echo "Java max memory: $JAVA_XMX"
    fi

    # 检查配置文件
    if [ -f "/app/config/application-docker.yml" ]; then
        echo "✅ Found custom application configuration"
    fi

    # 检查日志目录
    mkdir -p /app/logs
    echo "✅ Log directory created: /app/logs"
}

# 启动应用
start_application() {
    echo "Starting application..."

    # 设置JVM参数
    JVM_OPTS=""

    if [ -n "$JAVA_XMS" ]; then
        JVM_OPTS="$JVM_OPTS -Xms$JAVA_XMS"
    fi

    if [ -n "$JAVA_XMX" ]; then
        JVM_OPTS="$JVM_OPTS -Xmx$JAVA_XMX"
    fi

    # 添加GC日志
    JVM_OPTS="$JVM_OPTS -Xloggc:/app/logs/gc.log"

    # 添加JVM调试参数（可选）
    if [ "$JAVA_DEBUG_ENABLED" = "true" ]; then
        JVM_OPTS="$JVM_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
        echo "🐛 Java debug mode enabled on port 5005"
    fi

    # JFR记录（可选）
    if [ "$JFR_ENABLED" = "true" ]; then
        JVM_OPTS="$JVM_OPTS -XX:StartFlightRecording=duration=60s,filename=/app/logs/startup.jfr,settings=profile"
        echo "📊 JFR recording enabled"
    fi

    # 启动应用
    echo "Starting with JVM options: $JVM_OPTS"
    echo "Starting with Spring Boot options: $SPRING_OPTS"

    exec java $JVM_OPTS -jar app.jar $SPRING_OPTS
}

# 主函数
main() {
    echo "Starting IOE-DREAM Attendance Service initialization..."

    # 检查环境变量
    check_environment

    # 等待MySQL（如果配置了）
    if [ -n "$MYSQL_HOST" ] && [ -n "$MYSQL_PORT" ]; then
        wait_for_service "$MYSQL_HOST" "$MYSQL_PORT" "MySQL"
    fi

    # 等待Redis（如果配置了）
    if [ -n "$REDIS_HOST" ] && [ -n "$REDIS_PORT" ]; then
        wait_for_service "$REDIS_HOST" "$REDIS_PORT" "Redis"
    fi

    # 等待RabbitMQ（如果配置了）
    if [ -n "$RABBITMQ_HOST" ] && [ -n "$RABBITMQ_PORT" ]; then
        wait_for_service "$RABBITMQ_HOST" "$RABBITMQ_PORT" "RabbitMQ"
    fi

    # 数据库迁移
    run_database_migration

    # 启动前检查
    pre_startup_checks

    # 启动应用
    start_application
}

# 信号处理
trap 'echo "Received SIGTERM, shutting down gracefully..."; exit 0' SIGTERM SIGINT

# 执行主函数
main "$@"