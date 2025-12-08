#!/bin/bash

# IOE-DREAM 一键部署脚本
# 适用于开发和测试环境的快速部署

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 项目信息
PROJECT_NAME="IOE-DREAM"
PROJECT_VERSION="v1.0.0"
COMPOSE_FILE="docker-compose-all.yml"
ENV_FILE=".env.development"

# 日志函数
log() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

step() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  步骤 $1${NC}"
    echo -e "${BLUE}============================================${NC}"
}

# 显示横幅
show_banner() {
    echo -e "${PURPLE}"
    echo "============================================"
    echo "    _   __      __   _    _     __   __     "
    echo "   / | / /___  / /_ | |  | |   / /  / /____  "
    echo "  /  |/ / __ \/ __/ | |  | |  / /  / __/ _ \\ "
    echo " / /|  / /_/ / /_   | |  | | / /__/ /_/  __/ "
    echo "/_/ |_/\____/\__/   |_|  |_| |____/____/\___/  "
    echo ""
    echo "       智慧园区一卡通管理平台"
    echo "         Docker 一键部署工具"
    echo "============================================"
    echo -e "${NC}"
}

# 检查系统要求
check_requirements() {
    step "检查系统要求"

    # 检查操作系统
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        OS="Linux"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macOS"
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
        OS="Windows"
    else
        warn "未知操作系统: $OSTYPE"
    fi
    log "操作系统: $OS"

    # 检查Docker
    if ! command -v docker &> /dev/null; then
        error "Docker 未安装，请先安装 Docker"
        echo "安装指南: https://docs.docker.com/get-docker/"
        exit 1
    fi

    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi

    # 检查Docker服务状态
    if ! docker info &> /dev/null; then
        error "Docker 服务未启动，请启动 Docker 服务"
        exit 1
    fi

    # 检查系统资源
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        TOTAL_MEM=$(free -m | awk 'NR==2{print $2}')
        AVAILABLE_MEM=$(free -m | awk 'NR==2{print $7}')
        log "总内存: ${TOTAL_MEM}MB, 可用内存: ${AVAILABLE_MEM}MB"

        if [ "$TOTAL_MEM" -lt 8192 ]; then
            warn "系统内存少于8GB，可能影响性能"
        fi
    fi

    # 检查磁盘空间
    DISK_AVAILABLE=$(df . | awk 'NR==2{print $4}')
    DISK_AVAILABLE_GB=$((DISK_AVAILABLE / 1024 / 1024))
    log "可用磁盘空间: ${DISK_AVAILABLE_GB}GB"

    if [ "$DISK_AVAILABLE_GB" -lt 10 ]; then
        error "磁盘空间不足10GB，无法部署"
        exit 1
    fi

    log "✅ 系统要求检查通过"
}

# 检查项目文件
check_project_files() {
    step "检查项目文件"

    # 检查关键文件
    local required_files=(
        "docker-compose-all.yml"
        "scripts/docker-build.sh"
        ".env.development"
        "microservices/pom.xml"
        "microservices/microservices-common/pom.xml"
    )

    for file in "${required_files[@]}"; do
        if [ ! -f "$file" ]; then
            error "缺少关键文件: $file"
            exit 1
        fi
    done

    # 检查微服务目录
    local services=(
        "gateway-service"
        "common-service"
        "device-comm-service"
        "oa-service"
        "access-service"
        "attendance-service"
        "video-service"
        "consume-service"
        "visitor-service"
    )

    for service in "${services[@]}"; do
        if [ ! -d "microservices/ioedream-${service}" ]; then
            error "缺少微服务目录: microservices/ioedream-${service}"
            exit 1
        fi

        if [ ! -f "microservices/ioedream-${service}/Dockerfile" ]; then
            error "缺少Dockerfile: microservices/ioedream-${service}/Dockerfile"
            exit 1
        fi
    done

    log "✅ 项目文件检查通过"
}

# 配置环境
setup_environment() {
    step "配置环境"

    # 复制环境变量文件
    if [ ! -f ".env" ]; then
        cp "$ENV_FILE" .env
        log "已创建环境变量文件 .env"
    else
        warn "环境变量文件 .env 已存在，跳过创建"
    fi

    # 创建数据目录
    local data_dirs=(
        "data/mysql"
        "data/redis"
        "data/nacos"
        "logs/nginx"
        "logs/nacos"
        "logs/services"
    )

    for dir in "${data_dirs[@]}"; do
        mkdir -p "$dir"
    done

    log "✅ 环境配置完成"
}

# 构建Docker镜像
build_images() {
    step "构建Docker镜像"

    if [ ! -f "scripts/docker-build.sh" ]; then
        error "构建脚本不存在: scripts/docker-build.sh"
        exit 1
    fi

    # 设置执行权限
    chmod +x scripts/docker-build.sh

    # 执行构建
    log "开始构建Docker镜像..."
    ./scripts/docker-build.sh

    if [ $? -eq 0 ]; then
        log "✅ Docker镜像构建成功"
    else
        error "Docker镜像构建失败"
        exit 1
    fi
}

# 启动服务
start_services() {
    step "启动服务"

    # 检查端口占用
    local ports=(80 8080 8848 3306 6379)
    for port in "${ports[@]}"; do
        if lsof -i ":$port" &> /dev/null; then
            warn "端口 $port 已被占用，可能导致服务启动失败"
        fi
    done

    # 启动服务
    log "启动所有服务..."
    docker-compose -f "$COMPOSE_FILE" up -d

    if [ $? -eq 0 ]; then
        log "✅ 服务启动成功"
    else
        error "服务启动失败"
        exit 1
    fi
}

# 等待服务就绪
wait_for_services() {
    step "等待服务启动"

    log "等待基础设施服务启动..."
    sleep 30

    log "等待应用服务启动..."
    sleep 60

    log "等待所有服务就绪..."
    sleep 30

    # 检查关键服务状态
    local critical_services=(
        "ioedream-mysql"
        "ioedream-redis"
        "ioedream-nacos"
        "ioedream-gateway-service"
    )

    for service in "${critical_services[@]}"; do
        local status=$(docker ps --filter "name=$service" --format "{{.Status}}")
        if [[ "$status" == *"Up"* ]]; then
            log "✅ $service 运行正常"
        else
            warn "⚠️ $service 状态异常: $status"
        fi
    done
}

# 验证部署
verify_deployment() {
    step "验证部署"

    log "执行健康检查..."

    # 检查HTTP响应
    local retries=0
    local max_retries=10
    while [ $retries -lt $max_retries ]; do
        if curl -f -s http://localhost/health > /dev/null 2>&1; then
            log "✅ 健康检查通过"
            break
        else
            if [ $retries -eq $((max_retries - 1)) ]; then
                error "健康检查失败"
                return 1
            fi
            log "等待服务启动... ($((retries + 1))/$max_retries)"
            sleep 10
            retries=$((retries + 1))
        fi
    done

    # 检查服务端口
    log "检查服务端口..."
    local service_ports=(
        "80:Nginx"
        "8080:Gateway"
        "8848:Nacos"
        "3306:MySQL"
        "6379:Redis"
    )

    for port_info in "${service_ports[@]}"; do
        local port=$(echo $port_info | cut -d: -f1)
        local name=$(echo $port_info | cut -d: -f2)

        if nc -z localhost $port 2>/dev/null; then
            log "✅ $name 端口 $port 可访问"
        else
            warn "⚠️ $name 端口 $port 不可访问"
        fi
    done
}

# 显示部署结果
show_deployment_result() {
    step "部署完成"

    echo -e "${CYAN}"
    echo "🎉 IOE-DREAM 部署成功！"
    echo ""
    echo "============================================"
    echo "📱 访问地址"
    echo "============================================"
    echo "管理后台:       http://localhost:80"
    echo "API网关:        http://localhost:8080"
    echo "Nacos控制台:    http://localhost:8848/nacos"
    echo ""
    echo "============================================"
    echo "🔑 默认账号信息"
    echo "============================================"
    echo "系统管理员:     admin / 123456"
    echo "Nacos控制台:    nacos / nacos"
    echo "MySQL数据库:    root / root"
    echo "Redis缓存:      (无密码)"
    echo ""
    echo "============================================"
    echo "🔧 常用命令"
    echo "============================================"
    echo "查看服务状态:   docker-compose -f $COMPOSE_FILE ps"
    echo "查看日志:       docker-compose -f $COMPOSE_FILE logs -f"
    echo "停止服务:       docker-compose -f $COMPOSE_FILE down"
    echo "重启服务:       docker-compose -f $COMPOSE_FILE restart"
    echo ""
    echo "============================================"
    echo "📚 更多帮助"
    echo "============================================"
    echo "完整文档:       ./DOCKER_DEPLOYMENT_GUIDE.md"
    echo "快速指南:       ./QUICK_DOCKER_DEPLOYMENT.md"
    echo "问题反馈:       https://github.com/your-org/IOE-DREAM/issues"
    echo ""
    echo -e "${NC}"
}

# 清理函数
cleanup() {
    warn "收到中断信号，正在清理..."
    docker-compose -f "$COMPOSE_FILE" down
    exit 1
}

# 主函数
main() {
    # 设置中断处理
    trap cleanup SIGINT SIGTERM

    # 显示横幅
    show_banner

    log "开始部署 $PROJECT_NAME $PROJECT_VERSION..."

    # 检查是否为root用户
    if [ "$EUID" -eq 0 ]; then
        warn "不建议使用root用户运行此脚本"
        read -p "是否继续? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi

    # 执行部署步骤
    check_requirements
    check_project_files
    setup_environment
    build_images
    start_services
    wait_for_services
    verify_deployment
    show_deployment_result

    log "🎉 部署完成！享受使用 IOE-DREAM 吧！"
}

# 错误处理
trap 'error "脚本执行失败，退出码: $?"' ERR

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        --help|-h)
            echo "IOE-DREAM Docker 一键部署脚本"
            echo ""
            echo "用法: $0 [选项]"
            echo ""
            echo "选项:"
            echo "  --help, -h     显示此帮助信息"
            echo "  --skip-build   跳过镜像构建"
            echo "  --dev          使用开发环境"
            echo "  --prod         使用生产环境"
            echo ""
            exit 0
            ;;
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --dev)
            ENV_FILE=".env.development"
            COMPOSE_FILE="docker-compose-all.yml"
            shift
            ;;
        --prod)
            ENV_FILE=".env.production"
            COMPOSE_FILE="docker-compose-production.yml"
            shift
            ;;
        *)
            error "未知选项: $1"
            echo "使用 --help 查看帮助信息"
            exit 1
            ;;
    esac
done

# 执行主函数
main "$@"