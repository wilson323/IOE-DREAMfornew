#!/bin/bash

# IOE-DREAM 微服务启动脚本
# 作者: 老王 (AI工程师)
# 版本: 1.0.0
# 描述: 一键启动所有IOE-DREAM核心微服务

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 打印分隔线
print_separator() {
    echo "=================================================="
}

# 检查环境
check_environment() {
    log_info "检查部署环境..."

    # 检查Java版本
    if command -v java >/dev/null 2>&1; then
        JAVA_VERSION=$(java -version 2>&1 | head -1)
        log_success "Java已安装: $JAVA_VERSION"

        # 检查是否为Java 17+
        if java -version 2>&1 | grep -q "1[7-9]\|2[0-9]"; then
            log_success "Java版本符合要求 (17+)"
        else
            log_error "Java版本不符合要求，请安装Java 17+"
            exit 1
        fi
    else
        log_error "Java未安装，请先安装Java 17+"
        exit 1
    fi

    # 检查Maven
    if command -v mvn >/dev/null 2>&1; then
        MVN_VERSION=$(mvn -version | head -1)
        log_success "Maven已安装: $MVN_VERSION"
    else
        log_error "Maven未安装，请先安装Maven"
        exit 1
    fi

    # 检查Docker (可选)
    if command -v docker >/dev/null 2>&1; then
        DOCKER_VERSION=$(docker --version)
        log_success "Docker已安装: $DOCKER_VERSION"
    else
        log_warning "Docker未安装，某些功能可能无法使用"
    fi

    print_separator
}

# 编译通用模块
build_common_module() {
    log_info "编译通用模块..."

    cd microservices
    if mvn clean install -pl microservices-common -am -DskipTests -q; then
        log_success "通用模块编译成功"
    else
        log_error "通用模块编译失败"
        exit 1
    fi

    print_separator
}

# 启动单个服务
start_service() {
    local service_name=$1
    local service_port=$2
    local service_dir="ioedream-${service_name}-service"

    log_info "启动 $service_name 服务..."

    if [ ! -d "$service_dir" ]; then
        log_warning "$service_dir 目录不存在，跳过"
        return 0
    fi

    cd "$service_dir"

    # 检查端口是否被占用
    if lsof -Pi :$service_port -sTCP:LISTEN -t >/dev/null ; then
        log_warning "端口 $service_port 已被占用，$service_name 服务可能已经在运行"
    else
        log_info "启动 $service_name 在端口 $service_port..."
        # 后台启动服务
        nohup mvn spring-boot:run > logs/${service_name}.log 2>&1 &
        echo $! > ${service_name}.pid

        # 等待服务启动
        sleep 10

        # 检查服务是否启动成功
        if curl -s http://localhost:${service_port}/actuator/health >/dev/null 2>&1; then
            log_success "$service_name 服务启动成功 (端口: $service_port)"
        else
            log_error "$service_name 服务启动失败，请检查日志: logs/${service_name}.log"
        fi
    fi

    cd ..
}

# 停止单个服务
stop_service() {
    local service_name=$1
    local pid_file="${service_name}.pid"

    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if kill -0 $pid 2>/dev/null; then
            log_info "停止 $service_name 服务 (PID: $pid)..."
            kill $pid
            rm -f "$pid_file"
            log_success "$service_name 服务已停止"
        else
            log_warning "$service_name 服务进程不存在"
            rm -f "$pid_file"
        fi
    fi
}

# 检查服务状态
check_service_status() {
    local service_name=$1
    local service_port=$2
    local service_url="http://localhost:${service_port}/actuator/health"

    log_info "检查 $service_name 服务状态..."

    if curl -s "$service_url" >/dev/null 2>&1; then
        local status=$(curl -s "$service_url" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        if [ "$status" = "UP" ]; then
            log_success "$service_name 服务状态: UP ✅"
        else
            log_warning "$service_name 服务状态: DOWN ❌"
        fi
    else
        log_error "$service_name 服务不可访问"
    fi
}

# 启动所有核心服务
start_all_services() {
    log_info "启动所有IOE-DREAM核心微服务..."
    print_separator

    # 按启动顺序启动服务
    local services=(
        "config:8888"
        "auth:8889"
        "gateway:8080"
        "device:8081"
        "consume:8082"
        "monitor:8083"
        "oa:8084"
        "report:8085"
        "video:8086"
    )

    # 创建日志目录
    mkdir -p logs

    for service_info in "${services[@]}"; do
        IFS=':' read -r service_name service_port <<< "$service_info"
        start_service "$service_name" "$service_port"
        sleep 5  # 服务间隔启动时间
    done

    print_separator
    log_info "所有服务启动命令已执行，请等待服务完全启动..."
    sleep 30

    # 检查所有服务状态
    for service_info in "${services[@]}"; do
        IFS=':' read -r service_name service_port <<< "$service_info"
        check_service_status "$service_name" "$service_port"
    done

    print_separator
    log_success "IOE-DREAM微服务启动完成！"
    log_info "服务访问地址："
    echo "  - API网关: http://localhost:8080"
    echo "  - 认证服务: http://localhost:8889"
    echo "  - 配置中心: http://localhost:8888"
    echo "  - 监控服务: http://localhost:8083"
    print_separator
}

# 停止所有服务
stop_all_services() {
    log_info "停止所有IOE-DREAM微服务..."

    local services=(
        "config"
        "auth"
        "gateway"
        "device"
        "consume"
        "monitor"
        "oa"
        "report"
        "video"
    )

    for service_name in "${services[@]}"; do
        stop_service "$service_name"
    done

    print_separator
    log_success "所有IOE-DREAM微服务已停止"
}

# 显示帮助信息
show_help() {
    echo "IOE-DREAM 微服务管理脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  start     启动所有微服务"
    echo "  stop      停止所有微服务"
    echo "  restart   重启所有微服务"
    echo "  status    检查服务状态"
    echo "  build     编译项目"
    echo "  help      显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start     # 启动所有服务"
    echo "  $0 stop      # 停止所有服务"
    echo "  $0 restart   # 重启所有服务"
    echo "  $0 status    # 检查服务状态"
}

# 主函数
main() {
    case "${1:-help}" in
        start)
            check_environment
            build_common_module
            start_all_services
            ;;
        stop)
            stop_all_services
            ;;
        restart)
            stop_all_services
            sleep 5
            check_environment
            build_common_module
            start_all_services
            ;;
        status)
            # 检查所有服务状态
            local services=(
                "config:8888"
                "auth:8889"
                "gateway:8080"
                "device:8081"
                "consume:8082"
                "monitor:8083"
                "oa:8084"
                "report:8085"
                "video:8086"
            )

            for service_info in "${services[@]}"; do
                IFS=':' read -r service_name service_port <<< "$service_info"
                check_service_status "$service_name" "$service_port"
            done
            ;;
        build)
            check_environment
            build_common_module
            log_success "项目编译完成"
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"