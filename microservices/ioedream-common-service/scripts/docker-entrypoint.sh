#!/bin/bash
set -e

# =====================================================
# IOE-DREAM Docker启动脚本
# 支持多种启动模式和参数配置
# 包含健康检查和优雅关闭
# =====================================================

# 默认参数
DEFAULT_JAVA_OPTS="-server.port=8088"
DEFAULT_SPRING_PROFILES="prod"
DEFAULT_APP_OPTS=""

# 颜色输出
echo_color() {
    local color=$1
    local message=$2
    case $color in
        red)     echo -e "\033[31m$message\033[0m" ;;
        green)   echo -e "\033[32m$message\033[0m" ;;
        yellow)  echo -e "\033[33m$message\033[0m" ;;
        blue)    echo -e "\033[34m$message\033[0m" ;;
        purple)  echo -e "\033[35m$message\033[0m" ;;
        cyan)    echo -e "\033[36m$message\033[0m" ;;
        *)       echo "$message" ;;
    esac
}

# 打印启动信息
print_startup_info() {
    echo_color "blue" "===================================================="
    echo_color "blue" "🚀 IOE-DREAM 通用服务启动脚本"
    echo_color "blue" "===================================================="
    echo_color "cyan" "应用名称: ${APP_NAME:-ioedream-common-service}"
    echo_color "cyan" "应用版本: ${APP_VERSION:-1.0.0}"
    echo_color "cyan" "环境配置: ${SPRING_PROFILES_ACTIVE}"
    echo_color "cyan" "启动时间: $(date)"
    echo_color "blue" "===================================================="
}

# 检查环境依赖
check_environment() {
    echo_color "yellow" "🔍 检查环境依赖..."

    # 检查Java版本
    if ! command -v java &> /dev/null; then
        echo_color "red" "❌ Java未安装或不在PATH中"
        exit 1
    fi

    local java_version=$(java -version 2>&1 | head -n1)
    echo_color "green" "✅ Java版本: $java_version"

    # 检查应用JAR文件
    if [ ! -f "./app.jar" ]; then
        echo_color "red" "❌ 应用JAR文件不存在"
        exit 1
    fi

    echo_color "green" "✅ 应用JAR文件: $(du -h ./app.jar | cut -f1)"
    echo_color "green" "✅ 环境检查完成"
}

# 预启动检查
pre_start_checks() {
    echo_color "yellow" "🔍 执行预启动检查..."

    # 检查端口占用
    local port=${SERVER_PORT:-8088}
    if netstat -tuln | grep -q ":$port "; then
        echo_color "red" "❌ 端口 $port 已被占用"
        echo_color "yellow" "尝试关闭占用端口的进程..."
        fuser -k $port/tcp 2>/dev/null || true
        sleep 2
    fi

    # 检查磁盘空间
    local available_space=$(df -h /app | awk 'NR==2 {print $4}' | sed 's/[^0-9.]//g')
    local required_space=1024  # 1GB

    if [ "$available_space" -lt "$required_space" ]; then
        echo_color "yellow" "⚠️  可用磁盘空间不足: ${available_space}GB"
    else
        echo_color "green" "✅ 磁盘空间充足: ${available_space}GB"
    fi

    # 创建必要目录
    mkdir -p /app/logs /app/dumps /app/temp /app/data
    echo_color "green" "✅ 目录创建完成"

    # 检查配置文件
    if [ ! -f "./config/application.yml" ]; then
        echo_color "yellow" "⚠️  缺少配置文件，使用默认配置"
    else
        echo_color "green" "✅ 配置文件存在"
    fi
}

# 生成JVM参数
generate_jvm_options() {
    local jvm_options=""

    # 基础JVM参数
    jvm_options="$jvm_options $DEFAULT_JAVA_OPTS"
    jvm_options="$jvm_options $JAVA_OPTS"

    # 环境特定参数
    if [ "$ENVIRONMENT" = "prod" ]; then
        jvm_options="$jvm_options -XX:+UseContainerSupport"
        jvm_options="$jvm_options -XX:+UseCGroupMemoryLimitForHeap"
    fi

    # 容器特定参数
    if [ -f "/.dockerenv" ]; then
        source /.dockerenv
        jvm_options="$jvm_options $JAVA_OPTS"
    fi

    echo "$jvm_options"
}

# 等待服务就绪
wait_for_readiness() {
    local max_attempts=${READINESS_WAIT_MAX_ATTEMPTS:-60}
    local attempt=0
    local sleep_time=${READINESS_WAIT_SLEEP_TIME:-2}

    echo_color "yellow" "⏳ 等待服务就绪..."

    while [ $attempt -lt $max_attempts ]; do
        if curl -sS -f http://localhost:${SERVER_PORT:-8088}/actuator/health >/dev/null 2>&1; then
            echo_color "green" "✅ 服务已就绪"
            return 0
        fi

        attempt=$((attempt + 1))
        echo -n "."
        sleep $sleep_time
    done

    echo
    echo_color "red" "❌ 服务启动超时"
    return 1
}

# 应用启动
start_application() {
    local jvm_options=$(generate_jvm_options)
    local spring_profiles="${SPRING_PROFILES_ACTIVE:-$DEFAULT_SPRING_PROFILES}"
    local app_options="${APP_OPTS:-$DEFAULT_APP_OPTS}"

    echo_color "yellow" "🚀 启动应用程序..."
    echo_color "cyan" "JVM参数: $jvm_options"
    echo_color "cyan" "Spring配置: --spring.profiles.active=$spring_profiles"
    echo_color "cyan" "应用参数: $app_options"

    # 启动应用
    exec java $jvm_options \
         -Dspring.profiles.active=$spring_profiles \
         -jar ./app.jar \
         $app_options
}

# 应用关闭处理
shutdown_handler() {
    echo_color "yellow" "🛑 接收到关闭信号，开始优雅关闭..."

    # 发送关闭信号到应用
    if [ -f "/tmp/app.pid" ]; then
        local pid=$(cat /tmp/app.pid)
        echo_color "cyan" "向进程 $pid 发送关闭信号"
        kill -TERM $pid 2>/dev/null || true
    fi

    # 等待应用关闭
    local max_wait=30
    local wait_count=0

    while [ $wait_count -lt $max_wait ]; do
        if ! kill -0 $(cat /tmp/app.pid 2>/dev/null) 2>/dev/null; then
            echo_color "green" "✅ 应用已关闭"
            break
        fi
        sleep 1
        wait_count=$((wait_count + 1))
    done

    # 强制关闭
    if [ $wait_count -eq $max_wait ]; then
        echo_color "red" "❌ 强制关闭应用"
        kill -KILL $(cat /tmp/app.pid 2>/dev/null) 2>/dev/null || true
    fi

    # 清理临时文件
    rm -f /tmp/app.pid 2>/dev/null || true

    echo_color "blue" "🏁 应用关闭完成"
}

# 信号处理
trap 'shutdown_handler' SIGTERM SIGINT SIGQUIT

# 主执行流程
main() {
    # 打印启动信息
    print_startup_info

    # 检查环境
    check_environment

    # 预启动检查
    pre_start_checks

    # 启动应用
    start_application
}

# 参数处理
while [[ $# -gt 0 ]]; do
    case "$1" in
        --port=*)
            SERVER_PORT="${1#*=}"
            shift
            ;;
        --profiles=*)
            SPRING_PROFILES_ACTIVE="${1#*=}"
            shift
            ;;
        --debug)
            DEBUG_MODE=true
            DEFAULT_JAVA_OPTS="$DEFAULT_JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:5005"
            shift
            ;;
        --wait)
            READINESS_WAIT_ENABLED=true
            shift
            ;;
        --help|"-h")
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --port=PORT        设置服务端口 (默认: 8088)"
            echo "  --profiles=PROFS    设置Spring激活的配置文件"
            echo "  --debug              启用调试模式"
            echo "  --wait               等待服务就绪"
            echo "  --help, -h           显示帮助信息"
            exit 0
            ;;
        *)
            # 其他参数传递给应用
            DEFAULT_APP_OPTS="$DEFAULT_APP_OPTS $1"
            shift
            ;;
    esac
done

# 执行主函数
main "$@"