#!/bin/bash

# IOE-DREAM微服务Docker镜像构建脚本

set -e

# 配置变量
REGISTRY=${DOCKER_REGISTRY:-"registry.ioedream.com"}
NAMESPACE=${DOCKER_NAMESPACE:-"ioedream"}
VERSION=${BUILD_VERSION:-"latest"}
PUSH_IMAGES=${PUSH_IMAGES:-false}

# 服务配置列表
declare -A SERVICES=(
    ["gateway"]="smart-gateway:8080"
    ["auth-service"]="ioedream-auth-service:8081"
    ["identity-service"]="ioedream-identity-service:8082"
    ["device-service"]="ioedream-device-service:8083"
    ["access-service"]="ioedream-access-service:8084"
    ["consume-service"]="ioedream-consume-service:8085"
    ["attendance-service"]="ioedream-attendance-service:8086"
    ["video-service"]="ioedream-video-service:8087"
    ["oa-service"]="ioedream-oa-service:8088"
    ["system-service"]="ioedream-system-service:8089"
    ["monitor-service"]="ioedream-monitor-service:8090"
    ["file-service"]="ioedream-file-service:8091"
    ["notification-service"]="ioedream-notification-service:8092"
    ["visitor-service"]="ioedream-visitor-service:8093"
    ["hr-service"]="ioedream-hr-service:8094"
    ["report-service"]="ioedream-report-service:8095"
)

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

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi

    if ! docker info &> /dev/null; then
        log_error "Docker daemon未运行，请启动Docker"
        exit 1
    fi

    log_success "Docker检查通过"
}

# 创建Docker网络（如果不存在）
create_docker_network() {
    local network_name="ioedream-network"

    if ! docker network ls | grep -q "$network_name"; then
        log_info "创建Docker网络: $network_name"
        docker network create "$network_name"
        log_success "Docker网络创建完成"
    else
        log_info "Docker网络已存在: $network_name"
    fi
}

# 构建单个服务的Docker镜像
build_service_image() {
    local service_name=$1
    local service_config=$2
    local artifact_name=$(echo "$service_config" | cut -d':' -f1)
    local service_port=$(echo "$service_config" | cut -d':' -f2)

    log_info "构建服务镜像: $service_name"

    # 确定Dockerfile路径
    local dockerfile_path=""
    if [[ -f "docker/$service_name/Dockerfile" ]]; then
        dockerfile_path="docker/$service_name/Dockerfile"
    else
        log_warning "未找到专用Dockerfile，使用模板构建: $service_name"
        mkdir -p "docker/$service_name"

        # 使用模板创建Dockerfile
        sed -e "s/{{SERVICE_NAME}}/$service_name/g" \
            -e "s/{{ARTIFACT_NAME}}/$artifact_name/g" \
            -e "s/{{SERVICE_PORT}}/$service_port/g" \
            "docker/common/Dockerfile.template" > "docker/$service_name/Dockerfile"

        # 复制启动脚本
        cp "docker/common/docker-entrypoint.sh" "docker/$service_name/"
        chmod +x "docker/$service_name/docker-entrypoint.sh"

        dockerfile_path="docker/$service_name/Dockerfile"
    fi

    # 构建上下文路径
    local context_path="../$service_name"
    if [[ ! -d "$context_path" ]]; then
        log_error "服务目录不存在: $context_path"
        return 1
    fi

    # 构建镜像
    local image_name="$REGISTRY/$NAMESPACE/$service_name:$VERSION"

    log_info "开始构建镜像: $image_name"

    docker build \
        --file "$dockerfile_path" \
        --build-arg ARTIFACT_NAME="$artifact_name" \
        --build-arg SERVICE_NAME="$service_name" \
        --build-arg SERVICE_PORT="$service_port" \
        --tag "$image_name" \
        "$context_path"

    if [[ $? -eq 0 ]]; then
        log_success "镜像构建成功: $image_name"

        # 推送镜像（如果需要）
        if [[ "$PUSH_IMAGES" == "true" ]]; then
            log_info "推送镜像: $image_name"
            docker push "$image_name"
            if [[ $? -eq 0 ]]; then
                log_success "镜像推送成功: $image_name"
            else
                log_error "镜像推送失败: $image_name"
                return 1
            fi
        fi

        # 添加到构建列表
        BUILT_IMAGES+=("$image_name")
    else
        log_error "镜像构建失败: $image_name"
        return 1
    fi

    return 0
}

# 构建所有服务镜像
build_all_images() {
    local failed_services=()
    BUILT_IMAGES=()

    log_info "开始构建所有服务镜像..."
    log_info "Registry: $REGISTRY"
    log_info "Namespace: $NAMESPACE"
    log_info "Version: $VERSION"

    for service_name in "${!SERVICES[@]}"; do
        if ! build_service_image "$service_name" "${SERVICES[$service_name]}"; then
            failed_services+=("$service_name")
        fi
        echo "----------------------------------------"
    done

    # 构建结果汇总
    echo ""
    echo "========================================"
    echo "构建结果汇总"
    echo "========================================"

    if [[ ${#failed_services[@]} -eq 0 ]]; then
        log_success "所有服务镜像构建成功!"
        log_success "构建镜像数量: ${#BUILT_IMAGES[@]}"
    else
        log_error "以下服务构建失败:"
        for service in "${failed_services[@]}"; do
            echo "  - $service"
        done
        log_info "成功构建镜像数量: ${#BUILT_IMAGES[@]}"
        return 1
    fi

    # 列出所有构建的镜像
    echo ""
    echo "已构建的镜像:"
    for image in "${BUILT_IMAGES[@]}"; do
        echo "  - $image"
    done
}

# 清理旧的镜像
cleanup_old_images() {
    log_info "清理旧版本的镜像..."

    # 清理当前服务的旧版本镜像
    for service_name in "${!SERVICES[@]}"; do
        # 删除除当前版本外的其他版本
        docker images "$REGISTRY/$NAMESPACE/$service_name" --format "table {{.Repository}}:{{.Tag}}" | \
        grep -v "$VERSION" | grep -v "REPOSITORY:TAG" | \
        while read -r image; do
            if [[ -n "$image" ]]; then
                log_info "删除旧镜像: $image"
                docker rmi "$image" 2>/dev/null || true
            fi
        done
    done

    # 清理悬空镜像
    log_info "清理悬空镜像..."
    docker image prune -f

    log_success "镜像清理完成"
}

# 显示帮助信息
show_help() {
    echo "IOE-DREAM微服务Docker镜像构建脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help                显示帮助信息"
    echo "  -v, --version VERSION     指定镜像版本 (默认: latest)"
    echo "  -r, --registry REGISTRY   指定镜像仓库 (默认: registry.ioedream.com)"
    echo "  -n, --namespace NAMESPACE 指定命名空间 (默认: ioedream)"
    echo "  -p, --push               构建完成后推送镜像"
    echo "  -c, --cleanup            构建前清理旧镜像"
    echo "  --service SERVICE        仅构建指定服务"
    echo ""
    echo "环境变量:"
    echo "  BUILD_VERSION            镜像版本"
    echo "  DOCKER_REGISTRY          镜像仓库"
    echo "  DOCKER_NAMESPACE         命名空间"
    echo "  PUSH_IMAGES              是否推送镜像 (true/false)"
    echo ""
    echo "示例:"
    echo "  $0 -v v1.0.0 -p                      # 构建v1.0.0版本并推送"
    echo "  $0 --service auth-service             # 仅构建认证服务"
    echo "  $0 -c                                 # 清理旧镜像并构建"
}

# 主函数
main() {
    local build_service=""
    local cleanup_before_build=false

    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -v|--version)
                VERSION="$2"
                shift 2
                ;;
            -r|--registry)
                REGISTRY="$2"
                shift 2
                ;;
            -n|--namespace)
                NAMESPACE="$2"
                shift 2
                ;;
            -p|--push)
                PUSH_IMAGES=true
                shift
                ;;
            -c|--cleanup)
                cleanup_before_build=true
                shift
                ;;
            --service)
                build_service="$2"
                shift 2
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done

    # 检查依赖
    check_docker
    create_docker_network

    # 清理旧镜像（如果需要）
    if [[ "$cleanup_before_build" == "true" ]]; then
        cleanup_old_images
    fi

    # 构建镜像
    if [[ -n "$build_service" ]]; then
        if [[ -n "${SERVICES[$build_service]}" ]]; then
            build_service_image "$build_service" "${SERVICES[$build_service]}"
        else
            log_error "未知服务: $build_service"
            echo "可用服务:"
            for service in "${!SERVICES[@]}"; do
                echo "  - $service"
            done
            exit 1
        fi
    else
        build_all_images
    fi

    log_success "构建脚本执行完成"
}

# 执行主函数
main "$@"