#!/bin/bash
# IOE-DREAM Identity Service 部署脚本
# 基于现有项目部署模式重构
#
# @author IOE-DREAM Team
# @since 2025-11-27

set -e

# 配置变量
PROJECT_NAME="ioedream-identity-service"
DOCKER_REGISTRY="localhost"
DOCKER_TAG="latest"
NAMESPACE="ioedream"

# 颜色输出
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

# 检查依赖
check_dependencies() {
    log_info "检查部署依赖..."

    # 检查Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装或未在PATH中"
        exit 1
    fi

    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose未安装或未在PATH中"
        exit 1
    fi

    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装或未在PATH中"
        exit 1
    fi

    log_success "依赖检查通过"
}

# 构建项目
build_project() {
    log_info "构建Identity Service项目..."

    # 检查POM文件
    if [ ! -f "pom.xml" ]; then
        log_error "未找到pom.xml文件"
        exit 1
    fi

    # 编译项目
    mvn clean package -DskipTests -q
    if [ $? -ne 0 ]; then
        log_error "项目构建失败"
        exit 1
    fi

    # 检查JAR文件
    if [ ! -f "target/*.jar" ]; then
        log_error "未找到编译后的JAR文件"
        exit 1
    fi

    log_success "项目构建完成"
}

# 构建Docker镜像
build_docker_image() {
    log_info "构建Docker镜像..."

    # 构建镜像
    docker build -t ${PROJECT_NAME}:${DOCKER_TAG} .
    if [ $? -ne 0 ]; then
        log_error "Docker镜像构建失败"
        exit 1
    fi

    # 标记镜像
    docker tag ${PROJECT_NAME}:${DOCKER_TAG} ${DOCKER_REGISTRY}/${PROJECT_NAME}:${DOCKER_TAG}

    log_success "Docker镜像构建完成"
}

# 启动服务
start_services() {
    log_info "启动Identity Service服务..."

    # 创建必要的目录
    mkdir -p logs config/mysql config/redis config/prometheus config/grafana

    # 启动服务
    if command -v docker-compose &> /dev/null; then
        docker-compose up -d
    else
        docker compose up -d
    fi

    if [ $? -ne 0 ]; then
        log_error "服务启动失败"
        exit 1
    fi

    log_success "服务启动完成"
}

# 等待服务就绪
wait_for_services() {
    log_info "等待服务就绪..."

    # 等待MySQL启动
    log_info "等待MySQL启动..."
    timeout 60 bash -c 'until docker exec ioedream-mysql mysqladmin ping -h localhost --silent; do sleep 2; done'

    # 等待Redis启动
    log_info "等待Redis启动..."
    timeout 30 bash -c 'until docker exec ioedream-redis redis-cli ping; do sleep 2; done'

    # 等待Nacos启动
    log_info "等待Nacos启动..."
    timeout 120 bash -c 'until curl -f http://localhost:8848/nacos; do sleep 5; done'

    # 等待Identity Service启动
    log_info "等待Identity Service启动..."
    timeout 120 bash -c 'until curl -f http://localhost:8081/api/auth/health; do sleep 5; done'

    log_success "所有服务已就绪"
}

# 健康检查
health_check() {
    log_info "执行健康检查..."

    # 检查Identity Service健康状态
    health_response=$(curl -s -w "%{http_code}" http://localhost:8081/api/auth/health)
    http_code="${health_response: -3}"

    if [ "$http_code" = "200" ]; then
        log_success "Identity Service健康检查通过"
    else
        log_error "Identity Service健康检查失败，HTTP状态码: $http_code"
        exit 1
    fi

    # 检查服务状态
    if command -v docker-compose &> /dev/null; then
        docker-compose ps
    else
        docker compose ps
    fi

    log_success "健康检查完成"
}

# 显示服务信息
show_service_info() {
    log_success "Identity Service部署成功！"
    echo ""
    echo "服务访问地址:"
    echo "  Identity Service: http://localhost:8081"
    echo "  API文档: http://localhost:8081/doc.html"
    echo "  Nacos控制台: http://localhost:8848/nacos"
    echo "  Prometheus: http://localhost:9090"
    echo "  Grafana: http://localhost:3000 (admin/admin123)"
    echo ""
    echo "健康检查:"
    echo "  Identity Service: curl http://localhost:8081/api/auth/health"
    echo ""
    echo "日志查看:"
    echo "  Identity Service: docker logs -f ioedream-identity-service"
    echo ""
    echo "停止服务:"
    echo "  ./scripts/stop.sh"
}

# 主函数
main() {
    echo "========================================"
    echo "IOE-DREAM Identity Service 自动部署"
    echo "========================================"
    echo ""

    # 检查当前目录
    if [ ! -f "pom.xml" ] || [ ! -f "Dockerfile" ]; then
        log_error "请在项目根目录执行此脚本"
        exit 1
    fi

    # 执行部署步骤
    check_dependencies
    build_project
    build_docker_image
    start_services
    wait_for_services
    health_check
    show_service_info
}

# 处理中断信号
trap 'log_warning "部署被中断"; exit 1' INT TERM

# 执行主函数
main "$@"