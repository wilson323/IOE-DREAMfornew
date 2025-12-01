#!/bin/bash

# Docker容器启动脚本

set -e

# 等待依赖服务启动
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local timeout=${4:-60}

    echo "等待服务 $service_name ($host:$port) 启动..."

    for i in $(seq 1 $timeout); do
        if nc -z "$host" "$port"; then
            echo "服务 $service_name 已启动!"
            return 0
        fi
        echo "等待 $service_name 启动... ($i/$timeout)"
        sleep 2
    done

    echo "错误: 服务 $service_name 在 $timeout 秒内未启动"
    exit 1
}

# 检查环境变量
check_env_vars() {
    local required_vars=("$@")

    for var in "${required_vars[@]}"; do
        if [[ -z "${!var}" ]]; then
            echo "错误: 环境变量 $var 未设置"
            exit 1
        fi
    done
}

# 应用启动前检查
pre_start_checks() {
    echo "执行启动前检查..."

    # 检查必要的目录是否存在
    mkdir -p /app/logs

    # 设置日志文件权限
    touch /app/logs/application.log
    chmod 644 /app/logs/application.log

    echo "启动前检查完成"
}

# 应用启动后检查
post_start_checks() {
    local app_port=$1
    local health_check_url=${2:-"/actuator/health"}
    local max_retries=${3:-30}

    echo "执行启动后健康检查..."

    for i in $(seq 1 $max_retries); do
        if curl -f "http://localhost:$app_port$health_check_url" > /dev/null 2>&1; then
            echo "应用健康检查通过!"
            return 0
        fi
        echo "等待应用启动... ($i/$max_retries)"
        sleep 5
    done

    echo "错误: 应用健康检查失败"
    return 1
}

# 主启动逻辑
main() {
    echo "========================================"
    echo "启动 IOE-DREAM 微服务容器"
    echo "应用: ${SPRING_APPLICATION_NAME:-unknown}"
    echo "版本: ${APP_VERSION:-latest}"
    echo "环境: ${SPRING_PROFILES_ACTIVE:-default}"
    echo "========================================"

    # 执行启动前检查
    pre_start_checks

    # 等待依赖服务（如果有配置）
    if [[ -n "$WAIT_FOR_SERVICES" ]]; then
        IFS=',' read -ra SERVICES <<< "$WAIT_FOR_SERVICES"
        for service in "${SERVICES[@]}"; do
            IFS=':' read -ra ADDR <<< "$service"
            wait_for_service "${ADDR[0]}" "${ADDR[1]}" "$service"
        done
    fi

    # 设置JVM参数
    if [[ -z "$JAVA_OPTS" ]]; then
        JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
    fi

    # 添加容器感知JVM参数
    JAVA_OPTS="$JAVA_OPTS -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

    # 设置时区
    export TZ=${TZ:-Asia/Shanghai}

    echo "启动应用..."
    echo "Java版本: $(java -version 2>&1 | head -n 1)"
    echo "JVM参数: $JAVA_OPTS"

    # 启动应用（后台运行）
    java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar &
    APP_PID=$!

    # 等待应用启动并执行健康检查
    if [[ -n "$SERVER_PORT" ]]; then
        sleep 10
        if ! post_start_checks "$SERVER_PORT"; then
            echo "应用启动失败，终止进程"
            kill $APP_PID
            exit 1
        fi
    fi

    # 等待应用进程
    wait $APP_PID
}

# 信号处理
cleanup() {
    echo "接收到终止信号，正在关闭应用..."
    if [[ -n "$APP_PID" ]]; then
        kill -TERM $APP_PID
        wait $APP_PID
    fi
    echo "应用已关闭"
    exit 0
}

# 设置信号处理
trap cleanup SIGTERM SIGINT

# 执行主函数
main "$@"