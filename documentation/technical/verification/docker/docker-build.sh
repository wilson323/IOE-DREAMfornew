#!/bin/bash

# ===================================================================
# IOE-DREAM 微服务Docker镜像构建脚本
# 批量构建所有微服务的Docker镜像，支持并行构建
#
# 使用方法:
#   ./docker-build.sh [build|push|clean|list] [service_name]
#
# 参数说明:
#   build  - 构建Docker镜像 (默认)
#   push   - 推送镜像到仓库
#   clean  - 清理本地镜像
#   list   - 列出所有镜像
# ===================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")/../.."
MICROSERVICES_ROOT="$PROJECT_ROOT/microservices"
DOCKER_REGISTRY="ioedream"
DOCKER_VERSION="latest"
BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
PARALLEL_BUILD=${PARALLEL_BUILD:-true}
MAX_PARALLEL_JOBS=${MAX_PARALLEL_JOBS:-4}

# 微服务配置
declare -A MICROSERVICES=(
    ["smart-gateway"]="gateway"
    ["ioedream-auth-service"]="authentication"
    ["ioedream-identity-service"]="identity"
    ["ioedream-device-service"]="device"
    ["ioedream-access-service"]="access-control"
    ["ioedream-consume-service"]="consumption"
    ["ioedream-attendance-service"]="attendance"
    ["ioedream-video-service"]="video"
    ["ioedream-oa-service"]="office"
    ["ioedream-system-service"]="system"
    ["ioedream-monitor-service"]="monitoring"
)

# 构建统计
declare -A BUILD_RESULTS=()
TOTAL_JOBS=0
COMPLETED_JOBS=0
FAILED_JOBS=0

# 日志记录函数
log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "[$timestamp] [$level] $message"

    case $level in
        "INFO")
            echo -e "${GREEN}[INFO]${NC} $message"
            ;;
        "WARN")
            echo -e "${YELLOW}[WARN]${NC} $message"
            ;;
        "ERROR")
            echo -e "${RED}[ERROR]${NC} $message"
            ;;
        "DEBUG")
            echo -e "${BLUE}[DEBUG]${NC} $message"
            ;;
    esac
}

# 打印分隔线
print_separator() {
    echo -e "${PURPLE}==================================================================${NC}"
}

# 打印标题
print_section() {
    echo ""
    print_separator
    echo -e "${CYAN}📋 $1${NC}"
    print_separator
}

# 检查Docker环境
check_docker() {
    print_section "🐳 检查Docker环境"

    if ! command -v docker &> /dev/null; then
        log "ERROR" "Docker未安装或不在PATH中"
        exit 1
    fi

    if ! docker info &> /dev/null; then
        log "ERROR" "Docker守护进程未运行或权限不足"
        exit 1
    fi

    local docker_version=$(docker --version)
    log "INFO" "Docker版本: $docker_version"

    # 检查磁盘空间
    local available_space=$(df /var/lib/docker | awk 'NR==2 {print $4}')
    local required_space=5242880  # 5GB in KB

    if [ "$available_space" -lt "$required_space" ]; then
        log "WARN" "磁盘空间不足5GB，可能影响镜像构建"
    fi

    return 0
}

# 准备构建环境
prepare_build() {
    print_section "🔧 准备构建环境"

    # 创建构建临时目录
    local build_dir="$SCRIPT_DIR/temp"
    mkdir -p "$build_dir"

    # 检查微服务目录
    if [ ! -d "$MICROSERVICES_ROOT" ]; then
        log "ERROR" "微服务目录不存在: $MICROSERVICES_ROOT"
        exit 1
    fi

    # 统计可构建的服务数量
    local available_services=0
    for service in "${!MICROSERVICES[@]}"; do
        local service_dir="$MICROSERVICES_ROOT/$service"
        if [ -d "$service_dir" ] && [ -f "$service_dir/pom.xml" ]; then
            ((available_services++))
        fi
    done

    log "INFO" "发现 $available_services 个可构建的微服务"

    return 0
}

# 创建服务Dockerfile
create_service_dockerfile() {
    local service=$1
    local service_dir="$MICROSERVICES_ROOT/$service"
    local component=${MICROSERVICES[$service]}

    log "DEBUG" "为 $service 创建Dockerfile"

    # 读取模板并替换变量
    local dockerfile_content=$(cat "$SCRIPT_DIR/dockerfile-template")

    # 替换变量
    dockerfile_content=${dockerfile_content//\$\{SERVICE_NAME\}/$service}
    dockerfile_content=${dockerfile_content//\$\{COMPONENT\}/$component}
    dockerfile_content=${dockerfile_content//\$\{BUILD_DATE\}/$BUILD_DATE}
    dockerfile_content=${dockerfile_content//\$\{APP_VERSION\}/$DOCKER_VERSION}

    # 写入Dockerfile
    echo "$dockerfile_content" > "$service_dir/Dockerfile"

    log "DEBUG" "Dockerfile已创建: $service_dir/Dockerfile"
}

# 构建单个微服务
build_single_service() {
    local service=$1
    local service_dir="$MICROSERVICES_ROOT/$service"
    local image_name="$DOCKER_REGISTRY/$service:$DOCKER_VERSION"

    log "INFO" "开始构建 $service..."

    # 检查服务目录
    if [ ! -d "$service_dir" ]; then
        log "ERROR" "$service 目录不存在: $service_dir"
        BUILD_RESULTS[$service]="FAILED: Directory not found"
        ((FAILED_JOBS++))
        return 1
    fi

    # 检查pom.xml
    if [ ! -f "$service_dir/pom.xml" ]; then
        log "ERROR" "$service 缺少pom.xml文件"
        BUILD_RESULTS[$service]="FAILED: Missing pom.xml"
        ((FAILED_JOBS++))
        return 1
    fi

    # 进入服务目录
    cd "$service_dir"

    # 编译Java项目
    log "DEBUG" "编译 $service..."
    if ! mvn clean package -DskipTests -q; then
        log "ERROR" "$service Maven编译失败"
        BUILD_RESULTS[$service]="FAILED: Maven compilation failed"
        ((FAILED_JOBS++))
        return 1
    fi

    # 检查JAR文件
    local jar_file=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
    if [ -z "$jar_file" ]; then
        log "ERROR" "$service 未找到可执行的JAR文件"
        BUILD_RESULTS[$service]="FAILED: No executable JAR found"
        ((FAILED_JOBS++))
        return 1
    fi

    log "DEBUG" "找到JAR文件: $jar_file"

    # 创建Dockerfile
    create_service_dockerfile "$service"

    # 构建Docker镜像
    local build_start=$(date +%s)
    log "DEBUG" "构建Docker镜像: $image_name"

    if docker build -t "$image_name" . > "$SCRIPT_DIR/logs/${service}-build.log" 2>&1; then
        local build_end=$(date +%s)
        local build_time=$((build_end - build_start))
        local image_size=$(docker images --format "table {{.Size}}" "$image_name" | tail -1)

        log "INFO" "$service 构建成功 (耗时: ${build_time}s, 大小: $image_size)"
        BUILD_RESULTS[$service]="SUCCESS"
        ((COMPLETED_JOBS++))
    else
        log "ERROR" "$service Docker构建失败，查看日志: $SCRIPT_DIR/logs/${service}-build.log"
        BUILD_RESULTS[$service]="FAILED: Docker build failed"
        ((FAILED_JOBS++))
    fi

    return 0
}

# 并行构建微服务
build_parallel() {
    print_section "🚀 并行构建微服务"

    # 创建日志目录
    mkdir -p "$SCRIPT_DIR/logs"

    local services=()
    for service in "${!MICROSERVICES[@]}"; do
        local service_dir="$MICROSERVICES_ROOT/$service"
        if [ -d "$service_dir" ] && [ -f "$service_dir/pom.xml" ]; then
            services+=("$service")
        fi
    done

    TOTAL_JOBS=${#services[@]}
    log "INFO" "开始并行构建 $TOTAL_JOBS 个服务，最大并发数: $MAX_PARALLEL_JOBS"

    local pids=()
    local active_jobs=0
    local index=0

    while [ $index -lt $TOTAL_JOBS ] || [ ${#pids[@]} -gt 0 ]; do
        # 启动新的构建任务
        while [ $index -lt $TOTAL_JOBS ] && [ $active_jobs -lt $MAX_PARALLEL_JOBS ]; do
            local service="${services[$index]}"
            build_single_service "$service" &
            pids+=($!)
            ((active_jobs++))
            ((index++))
        done

        # 检查已完成的任务
        for i in "${!pids[@]}"; do
            if ! kill -0 "${pids[$i]}" 2>/dev/null; then
                wait "${pids[$i]}"
                unset "pids[$i]"
                ((active_jobs--))
            fi
        done

        # 显示进度
        local progress=$(( (COMPLETED_JOBS + FAILED_JOBS) * 100 / TOTAL_JOBS ))
        echo -ne "\r构建进度: ${progress}% (${COMPLETED_JOBS} 成功, ${FAILED_JOBS} 失败) / $TOTAL_JOBS"

        sleep 1
    done

    echo ""
}

# 串行构建微服务
build_sequential() {
    print_section "🚀 串行构建微服务"

    # 创建日志目录
    mkdir -p "$SCRIPT_DIR/logs"

    local services=()
    for service in "${!MICROSERVICES[@]}"; do
        local service_dir="$MICROSERVICES_ROOT/$service"
        if [ -d "$service_dir" ] && [ -f "$service_dir/pom.xml" ]; then
            services+=("$service")
        fi
    done

    TOTAL_JOBS=${#services[@]}
    log "INFO" "开始串行构建 $TOTAL_JOBS 个服务"

    for service in "${services[@]}"; do
        build_single_service "$service"
    done
}

# 构建所有微服务
build_all_services() {
    print_section "🏗️ 构建所有微服务镜像"

    # 记录开始时间
    local start_time=$(date +%s)

    # 根据配置选择构建方式
    if [ "$PARALLEL_BUILD" = "true" ]; then
        build_parallel
    else
        build_sequential
    fi

    # 计算总耗时
    local end_time=$(date +%s)
    local total_time=$((end_time - start_time))

    # 显示构建结果
    print_section "📊 构建结果汇总"

    echo -e "总构建任务: ${YELLOW}$TOTAL_JOBS${NC}"
    echo -e "成功构建:   ${GREEN}$COMPLETED_JOBS${NC}"
    echo -e "构建失败:   ${RED}$FAILED_JOBS${NC}"
    echo -e "总耗时:     ${BLUE}${total_time}s${NC}"

    local success_rate=0
    if [ $TOTAL_JOBS -gt 0 ]; then
        success_rate=$((COMPLETED_JOBS * 100 / TOTAL_JOBS))
    fi

    echo -e "成功率:     ${BLUE}${success_rate}%${NC}"

    if [ $FAILED_JOBS -gt 0 ]; then
        echo -e "\n${RED}失败的构建:${NC}"
        for service in "${!BUILD_RESULTS[@]}"; do
            if [[ "${BUILD_RESULTS[$service]}" == FAILED* ]]; then
                echo -e "  - $service: ${BUILD_RESULTS[$service]}"
            fi
        done
    fi

    # 列出构建的镜像
    echo -e "\n${GREEN}成功构建的镜像:${NC}"
    docker images --filter "reference=$DOCKER_REGISTRY/*" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}" | grep $DOCKER_VERSION

    return $FAILED_JOBS
}

# 推送镜像到仓库
push_images() {
    print_section "📤 推送镜像到仓库"

    local push_count=0
    local push_failed=0

    for service in "${!MICROSERVICES[@]}"; do
        if [ "${BUILD_RESULTS[$service]}" = "SUCCESS" ]; then
            local image_name="$DOCKER_REGISTRY/$service:$DOCKER_VERSION"

            log "INFO" "推送 $image_name..."
            if docker push "$image_name"; then
                log "INFO" "$image_name 推送成功"
                ((push_count++))
            else
                log "ERROR" "$image_name 推送失败"
                ((push_failed++))
            fi
        fi
    done

    echo -e "推送完成: ${GREEN}$push_count${NC} 成功, ${RED}$push_failed${NC} 失败"
}

# 清理本地镜像
clean_images() {
    print_section "🧹 清理本地镜像"

    log "INFO" "清理IOE-DREAM相关镜像..."

    # 删除构建的镜像
    for service in "${!MICROSERVICES[@]}"; do
        local image_name="$DOCKER_REGISTRY/$service:$DOCKER_VERSION"
        if docker images | grep -q "$image_name"; then
            log "INFO" "删除镜像: $image_name"
            docker rmi "$image_name" 2>/dev/null || true
        fi
    done

    # 清理悬空镜像
    log "INFO" "清理悬空镜像..."
    docker image prune -f

    log "INFO" "清理完成"
}

# 列出镜像
list_images() {
    print_section "📋 列出IOE-DREAM镜像"

    echo -e "${BLUE}本地IOE-DREAM镜像:${NC}"
    docker images --filter "reference=$DOCKER_REGISTRY/*" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"

    echo ""
    echo -e "${BLUE}镜像统计:${NC}"
    local image_count=$(docker images --filter "reference=$DOCKER_REGISTRY/*" | wc -l)
    local total_size=$(docker images --filter "reference=$DOCKER_REGISTRY/*" --format "{{.Size}}" | tail -n +2 | awk '{s+=$1} END {print s"B"}')
    echo "镜像数量: $((image_count - 1))"
    echo "总大小: $total_size"
}

# 显示帮助信息
show_help() {
    echo "IOE-DREAM 微服务Docker构建工具"
    echo ""
    echo "使用方法:"
    echo "  $0 [命令] [服务名称]"
    echo ""
    echo "命令:"
    echo "  build  - 构建Docker镜像 (默认)"
    echo "  push   - 推送镜像到仓库"
    echo "  clean  - 清理本地镜像"
    echo "  list   - 列出所有镜像"
    echo "  help   - 显示帮助信息"
    echo ""
    echo "环境变量:"
    echo "  DOCKER_REGISTRY     - Docker镜像仓库地址 (默认: ioedream)"
    echo "  DOCKER_VERSION      - 镜像版本标签 (默认: latest)"
    echo "  PARALLEL_BUILD      - 是否并行构建 (默认: true)"
    echo "  MAX_PARALLEL_JOBS   - 最大并行任务数 (默认: 4)"
    echo ""
    echo "示例:"
    echo "  $0 build                           # 构建所有服务镜像"
    echo "  $0 build smart-gateway             # 仅构建网关镜像"
    echo "  $0 push                            # 推送所有镜像"
    echo "  PARALLEL_BUILD=false $0 build     # 串行构建"
    echo "  DOCKER_VERSION=v1.0.0 $0 build    # 指定版本构建"
}

# 主函数
main() {
    local command=${1:-"build"}
    local service_name=${2:-""}

    # 检查Docker环境
    check_docker

    # 准备构建环境
    prepare_build

    case $command in
        "build")
            if [ -n "$service_name" ]; then
                print_section "🏗️ 构建单个服务: $service_name"
                build_single_service "$service_name"
                show_images
            else
                build_all_services
            fi
            ;;
        "push")
            push_images
            ;;
        "clean")
            clean_images
            ;;
        "list")
            list_images
            ;;
        "help"|"--help"|"-h")
            show_help
            ;;
        *)
            log "ERROR" "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

# 脚本入口点
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi