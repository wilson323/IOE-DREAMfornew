# Docker优化专家

> **版本**: v1.0.0
> **更新时间**: 2025-11-21
> **分类**: 容器化技能 > Docker优化
> **标签**: ["Docker", "容器优化", "多阶段构建", "安全加固", "性能调优"]
> **技能等级**: ★★★ 专家级
> **适用角色**: DevOps工程师、容器架构师、高级运维工程师
> **前置技能**: kubernetes-deployment-specialist, spring-boot-jakarta-guardian
> **预计学时**: 40-60小时

---

## 📋 技能概述

本技能专注于IOE-DREAM项目的Docker容器化优化，基于Java 17 + Spring Boot 3.x + Jakarta技术栈，提供从镜像构建、安全加固到性能调优的完整容器化解决方案。涵盖多阶段构建、镜像分层优化、安全扫描、性能监控等关键环节。

**技术基础**: Docker 24.x + Docker Compose + BuildKit + Trivy
**核心目标**: 构建安全、高效、标准化的容器镜像体系

---

## 🏗️ 镜像构建优化

### 1. 多阶段构建策略

#### Spring Boot应用最优Dockerfile
```dockerfile
# ================= 基础构建镜像 =================
# 使用官方Maven镜像，包含OpenJDK 17
FROM maven:3.9.4-openjdk-17-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml（利用Docker缓存机制）
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# 下载依赖（缓存依赖层）
RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用（跳过测试以提高构建速度）
RUN ./mvnw clean package -DskipTests -B

# ================= 运行时镜像 =================
# 使用最小化的OpenJDK运行时镜像
FROM openjdk:17.0.2-jre-slim

# 安装必要的系统工具
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        netcat-openbsd \
        dumb-init \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# 创建应用用户（安全最佳实践）
RUN groupadd -r appuser && \
    useradd -r -g appuser -d /app -s /bin/bash appuser

# 设置工作目录
WORKDIR /app

# 从构建阶段复制应用JAR
COPY --from=builder /app/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && \
    chown -R appuser:appuser /app

# 健康检查脚本
COPY docker/healthcheck.sh /usr/local/bin/healthcheck.sh
RUN chmod +x /usr/local/bin/healthcheck.sh

# 切换到非root用户
USER appuser

# JVM参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m \
    -XX:+UseG1GC \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+UseCGroupMemoryLimitForHeap \
    -XX:+DisableExplicitGC \
    -XX:+AlwaysPreTouch \
    -XX:G1NewSizePercent=30 \
    -XX:G1MaxNewSizePercent=40 \
    -XX:G1HeapRegionSize=8M \
    -XX:G1ReservePercent=20 \
    -XX:G1HeapWastePercent=5 \
    -XX:G1MixedGCCountTarget=4 \
    -XX:InitiatingHeapOccupancyPercent=45 \
    -XX:+ParallelRefProcEnabled \
    -XX:+PerfDisableSharedMem \
    -XX:+MaxInlineLevel=15"

# 应用配置
ENV SPRING_PROFILES_ACTIVE=prod
ENV MANAGEMENT_SERVER_PORT=8081
ENV SERVER_PORT=8080

# 暴露端口
EXPOSE 8080 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD /usr/local/bin/healthcheck.sh

# 使用dumb-init作为PID 1
ENTRYPOINT ["dumb-init", "--"]

# 启动命令
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### 构建参数化Dockerfile
```dockerfile
# Dockerfile.template
# 支持多环境构建的参数化Dockerfile

ARG APP_NAME=user-service
ARG APP_VERSION=1.0.0
ARG BUILD_ENV=production
ARG BASE_IMAGE=openjdk:17.0.2-jre-slim
ARG MAVEN_IMAGE=maven:3.9.4-openjdk-17-slim

# 构建阶段
FROM ${MAVEN_IMAGE} AS builder
ARG APP_NAME
ARG BUILD_ENV

WORKDIR /app

# 优化Maven配置
RUN echo "<settings><mirrors><mirror><id>aliyun-maven</id><mirrorOf>central</mirrorOf><url>https://maven.aliyun.com/repository/central</url></mirror></mirrors></settings>" > /root/.m2/settings.xml

# 依赖缓存层
COPY --chown=appuser:appuser pom.xml .mvn mvnw ./
RUN chmod +x mvnw && \
    if [ "${BUILD_ENV}" = "production" ]; then \
        ./mvnw dependency:go-offline -B -DskipTests; \
    else \
        ./mvnw dependency:resolve -B; \
    fi

# 源代码构建
COPY --chown=appuser:appuser src ./src
ARG BUILD_ENV
RUN if [ "${BUILD_ENV}" = "production" ]; then \
        ./mvnw clean package -DskipTests -B; \
    else \
        ./mvnw clean package -B; \
    fi

# 运行时阶段
FROM ${BASE_IMAGE}
ARG APP_NAME
ARG APP_VERSION

# 标签信息
LABEL maintainer="IOE-DREAM Team" \
      version="${APP_VERSION}" \
      description="IOE-DREAM ${APP_NAME}" \
      org.opencontainers.image.source="https://github.com/ioe-dream/${APP_NAME}" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${VCS_REF}"

# 系统优化
RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
        curl \
        ca-certificates \
        tzdata \
        dumb-init \
    ; \
    # 设置时区
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime; \
    dpkg-reconfigure -f noninteractive tzdata; \
    # 清理缓存
    apt-get clean; \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*; \
    # 创建用户
    groupadd -r appuser && \
    useradd -r -g appuser -d /app -s /bin/bash appuser

WORKDIR /app

# 复制应用
COPY --from=builder --chown=appuser:appuser /app/target/*.jar app.jar

# 配置文件
COPY --chown=appuser:appuser docker/app-entrypoint.sh /usr/local/bin/app-entrypoint.sh
RUN chmod +x /usr/local/bin/app-entrypoint.sh

# 用户切换
USER appuser

# 环境变量
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"
ENV APP_NAME=${APP_NAME}
ENV APP_VERSION=${APP_VERSION}

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${MANAGEMENT_SERVER_PORT:-8081}/actuator/health || exit 1

# 入口点
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "exec /usr/local/bin/app-entrypoint.sh"]
```

### 2. 构建优化脚本

#### 智能构建脚本
```bash
#!/bin/bash
# build.sh - IOE-DREAM项目智能构建脚本

set -euo pipefail

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_CONTEXT="${PROJECT_ROOT}"
DOCKER_REGISTRY="${DOCKER_REGISTRY:-registry.ioe-dream.com}"
IMAGE_NAME="${IMAGE_NAME:-$(basename "$PROJECT_ROOT")}"
IMAGE_TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD)}"
BUILD_ENV="${BUILD_ENV:-production}"
PUSH_IMAGE="${PUSH_IMAGE:-false}"
SCAN_SECURITY="${SCAN_SECURITY:-true}"

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
    log_info "检查构建依赖..."

    local deps=("docker" "git")
    for dep in "${deps[@]}"; do
        if ! command -v "$dep" &> /dev/null; then
            log_error "依赖 '$dep' 未安装"
            exit 1
        fi
    done

    # 检查Docker是否运行
    if ! docker info &> /dev/null; then
        log_error "Docker守护进程未运行"
        exit 1
    fi

    log_success "依赖检查通过"
}

# 构建参数生成
generate_build_args() {
    local build_args=""

    # 应用信息
    build_args="$build_args --build-arg APP_NAME=$IMAGE_NAME"
    build_args="$build_args --build-arg APP_VERSION=$IMAGE_TAG"
    build_args="$build_args --build-arg BUILD_ENV=$BUILD_ENV"

    # Git信息
    if git rev-parse --git-dir > /dev/null 2>&1; then
        local git_commit=$(git rev-parse HEAD)
        local git_branch=$(git rev-parse --abbrev-ref HEAD)
        local git_remote_url=$(git config --get remote.origin.url)

        build_args="$build_args --build-arg VCS_REF=$git_commit"
        build_args="$build_args --label org.opencontainers.image.revision=$git_commit"
        build_args="$build_args --label org.opencontainers.image.branch=$git_branch"
        build_args="$build_args --label org.opencontainers.image.source=$git_remote_url"
    fi

    # 构建信息
    local build_timestamp=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    build_args="$build_args --label org.opencontainers.image.created=$build_timestamp"
    build_args="$build_args --label build.env=$BUILD_ENV"

    echo "$build_args"
}

# 安全扫描
security_scan() {
    local image_full_name="$1"

    if [[ "$SCAN_SECURITY" != "true" ]]; then
        log_warning "跳过安全扫描"
        return 0
    fi

    log_info "开始安全扫描: $image_full_name"

    # 检查Trivy是否安装
    if ! command -v trivy &> /dev/null; then
        log_warning "Trivy未安装，跳过安全扫描"
        return 0
    fi

    # 运行扫描
    local scan_report="security-scan-$(date +%Y%m%d%H%M%S).json"

    if trivy image --format json --output "$scan_report" "$image_full_name"; then
        log_success "安全扫描完成，报告: $scan_report"

        # 解析扫描结果
        local critical_vulns=$(jq -r '.Results[]?.Vulnerabilities[]? | select(.Severity == "CRITICAL") | .Severity' "$scan_report" | wc -l || echo "0")
        local high_vulns=$(jq -r '.Results[]?.Vulnerabilities[]? | select(.Severity == "HIGH") | .Severity' "$scan_report" | wc -l || echo "0")

        if [[ "$critical_vulns" -gt 0 ]]; then
            log_error "发现 $critical_vulns 个严重漏洞！"
            return 1
        fi

        if [[ "$high_vulns" -gt 0 ]]; then
            log_warning "发现 $high_vulns 个高危漏洞"
        fi
    else
        log_error "安全扫描失败"
        return 1
    fi
}

# 镜像推送到注册表
push_image() {
    local image_full_name="$1"

    if [[ "$PUSH_IMAGE" != "true" ]]; then
        log_info "跳过镜像推送"
        return 0
    fi

    log_info "推送镜像: $image_full_name"

    # 检查登录状态
    if ! docker system info | grep -q "Username"; then
        log_error "未登录到Docker注册表"
        return 1
    fi

    # 推送镜像
    if docker push "$image_full_name"; then
        log_success "镜像推送成功: $image_full_name"

        # 推送latest标签（如果是生产环境）
        if [[ "$BUILD_ENV" == "production" ]]; then
            local latest_tag="${DOCKER_REGISTRY}/${IMAGE_NAME}:latest"
            docker tag "$image_full_name" "$latest_tag"
            docker push "$latest_tag"
            log_success "latest标签推送成功"
        fi
    else
        log_error "镜像推送失败"
        return 1
    fi
}

# 主构建流程
build_image() {
    log_info "开始构建镜像..."
    log_info "镜像名称: $IMAGE_NAME"
    log_info "镜像标签: $IMAGE_TAG"
    log_info "构建环境: $BUILD_ENV"

    local image_full_name="${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
    local build_args

    # 检查构建上下文
    if [[ ! -f "$BUILD_CONTEXT/Dockerfile" ]]; then
        log_error "Dockerfile不存在: $BUILD_CONTEXT/Dockerfile"
        exit 1
    fi

    # 生成构建参数
    build_args=$(generate_build_args)

    # 构建镜像
    log_info "执行Docker构建..."
    if docker buildx build \
        --platform linux/amd64,linux/arm64 \
        --tag "$image_full_name" \
        $build_args \
        "$BUILD_CONTEXT"; then
        log_success "镜像构建成功: $image_full_name"
    else
        log_error "镜像构建失败"
        exit 1
    fi

    # 镜像信息
    local image_size=$(docker images --format "{{.Size}}" "$image_full_name")
    log_info "镜像大小: $image_size"

    # 安全扫描
    security_scan "$image_full_name"

    # 推送镜像
    push_image "$image_full_name"

    log_success "构建流程完成"
}

# 清理旧镜像
cleanup_old_images() {
    log_info "清理旧镜像..."

    # 删除未使用的镜像
    docker image prune -f

    # 删除超过30天的旧镜像
    docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.CreatedAt}}" | \
        grep "$IMAGE_NAME" | \
        awk 'NR>1 && $3 < "'$(date -d '30 days ago' '+%Y-%m-%d')'" {print $1":"$2}' | \
        xargs -r docker rmi -f

    log_success "镜像清理完成"
}

# 显示帮助信息
show_help() {
    cat << EOF
IOE-DREAM项目Docker构建脚本

用法: $0 [选项]

选项:
    -n, --name NAME           镜像名称 (默认: 项目目录名)
    -t, --tag TAG             镜像标签 (默认: Git commit short hash)
    -e, --env ENV             构建环境 (默认: production)
    -r, --registry REGISTRY    Docker注册表 (默认: registry.ioe-dream.com)
    -p, --push                推送镜像到注册表
    -s, --scan                执行安全扫描 (默认: true)
    -c, --cleanup             构建完成后清理旧镜像
    -h, --help                显示帮助信息

示例:
    $0                        # 使用默认参数构建
    $0 -n user-service -t v1.0.0  # 自定义名称和标签
    $0 -e test -p             # 测试环境构建并推送

环境变量:
    DOCKER_REGISTRY           Docker注册表地址
    IMAGE_NAME                镜像名称
    IMAGE_TAG                 镜像标签
    BUILD_ENV                 构建环境
    PUSH_IMAGE                是否推送镜像
    SCAN_SECURITY             是否执行安全扫描
EOF
}

# 解析命令行参数
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -n|--name)
                IMAGE_NAME="$2"
                shift 2
                ;;
            -t|--tag)
                IMAGE_TAG="$2"
                shift 2
                ;;
            -e|--env)
                BUILD_ENV="$2"
                shift 2
                ;;
            -r|--registry)
                DOCKER_REGISTRY="$2"
                shift 2
                ;;
            -p|--push)
                PUSH_IMAGE=true
                shift
                ;;
            -s|--scan)
                SCAN_SECURITY=true
                shift
                ;;
            --no-scan)
                SCAN_SECURITY=false
                shift
                ;;
            -c|--cleanup)
                CLEANUP=true
                shift
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# 主函数
main() {
    parse_args "$@"

    log_info "IOE-DREAM Docker构建脚本启动"

    # 检查依赖
    check_dependencies

    # 构建镜像
    build_image

    # 清理旧镜像
    if [[ "${CLEANUP:-false}" == "true" ]]; then
        cleanup_old_images
    fi

    log_success "构建脚本执行完成"
}

# 执行主函数
main "$@"
```

---

## 🔒 安全加固配置

### 1. 容器安全策略

#### 安全扫描脚本
```bash
#!/bin/bash
# security-scan.sh - 容器安全扫描脚本

set -euo pipefail

# 配置变量
IMAGE_NAME="${1:-}"
SCAN_TYPE="${2:-full}"
REPORT_DIR="security-reports"
TIMESTAMP=$(date +%Y%m%d%H%M%S)

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 检查工具依赖
check_tools() {
    local tools=("docker" "trivy")
    for tool in "${tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            log_error "工具 '$tool' 未安装"
            exit 1
        fi
    done
}

# 创建报告目录
setup_report_dir() {
    mkdir -p "$REPORT_DIR"
    log_info "报告目录: $REPORT_DIR"
}

# Trivy安全扫描
trivy_scan() {
    local image="$1"
    local report_file="$REPORT_DIR/trivy-${TIMESTAMP}.json"

    log_info "执行Trivy安全扫描: $image"

    case "$SCAN_TYPE" in
        "critical")
            trivy image --severity CRITICAL --format json --output "$report_file" "$image"
            ;;
        "high")
            trivy image --severity HIGH,CRITICAL --format json --output "$report_file" "$image"
            ;;
        "full")
            trivy image --severity UNKNOWN,LOW,MEDIUM,HIGH,CRITICAL --format json --output "$report_file" "$image"
            ;;
        *)
            log_error "未知的扫描类型: $SCAN_TYPE"
            exit 1
            ;;
    esac

    if [[ $? -eq 0 ]]; then
        log_success "Trivy扫描完成: $report_file"

        # 解析结果
        parse_trivy_results "$report_file"
    else
        log_error "Trivy扫描失败"
        exit 1
    fi
}

# 解析Trivy结果
parse_trivy_results() {
    local report_file="$1"

    if [[ ! -f "$report_file" ]]; then
        log_warning "扫描报告文件不存在: $report_file"
        return 0
    fi

    local critical_count=$(jq -r '.Results[]?.Vulnerabilities[]? | select(.Severity == "CRITICAL") | .Severity' "$report_file" | wc -l || echo "0")
    local high_count=$(jq -r '.Results[]?.Vulnerabilities[]? | select(.Severity == "HIGH") | .Severity' "$report_file" | wc -l || echo "0")
    local medium_count=$(jq -r '.Results[]?.Vulnerabilities[]? | select(.Severity == "MEDIUM") | .Severity' "$report_file" | wc -l || echo "0")

    log_info "漏洞统计:"
    log_info "  严重 (CRITICAL): $critical_count"
    log_info "  高危 (HIGH): $high_count"
    log_info "  中危 (MEDIUM): $medium_count"

    # 检查是否通过安全检查
    if [[ "$critical_count" -gt 0 ]]; then
        log_error "发现严重漏洞，安全检查失败！"
        return 1
    elif [[ "$high_count" -gt 5 ]]; then
        log_warning "高危漏洞数量较多 ($high_count)，建议修复"
        return 1
    else
        log_success "安全检查通过"
        return 0
    fi
}

# Docker安全检查
docker_security_check() {
    local image="$1"
    local report_file="$REPORT_DIR/docker-security-${TIMESTAMP}.txt"

    log_info "执行Docker安全配置检查"

    # 检查镜像配置
    echo "Docker安全配置检查报告" > "$report_file"
    echo "生成时间: $(date)" >> "$report_file"
    echo "镜像: $image" >> "$report_file"
    echo "" >> "$report_file"

    # 获取镜像配置
    local config=$(docker inspect "$image" | jq '.[0].Config')

    # 检查用户配置
    local user=$(echo "$config" | jq -r '.User // "root"')
    if [[ "$user" == "root" || "$user" == "0" ]]; then
        echo "❌ 以root用户运行" >> "$report_file"
    else
        echo "✅ 使用非root用户" >> "$report_file"
    fi

    # 检查暴露端口
    local exposed_ports=$(echo "$config" | jq -r '.ExposedPorts // {} | keys[]' 2>/dev/null || echo "")
    if [[ -n "$exposed_ports" ]]; then
        echo "📡 暴露端口: $exposed_ports" >> "$report_file"
    fi

    # 检查健康检查
    local healthcheck=$(echo "$config" | jq -r '.Healthcheck // null')
    if [[ "$healthcheck" == "null" ]]; then
        echo "⚠️  未配置健康检查" >> "$report_file"
    else
        echo "✅ 已配置健康检查" >> "$report_file"
    fi

    # 检查环境变量中的敏感信息
    local env_vars=$(echo "$config" | jq -r '.Env[]? | select(. | test("(PASSWORD|SECRET|TOKEN|KEY)"; "i"))')
    if [[ -n "$env_vars" ]]; then
        echo "⚠️  发现可能包含敏感信息的环境变量" >> "$report_file"
        echo "$env_vars" >> "$report_file"
    fi

    log_success "Docker安全检查完成: $report_file"
}

# 生成HTML报告
generate_html_report() {
    local html_file="$REPORT_DIR/security-report-${TIMESTAMP}.html"

    cat > "$html_file" << EOF
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>容器安全扫描报告</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #f5f5f5; padding: 20px; border-radius: 5px; }
        .section { margin: 20px 0; }
        .success { color: green; }
        .warning { color: orange; }
        .error { color: red; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <div class="header">
        <h1>IOE-DREAM 容器安全扫描报告</h1>
        <p>生成时间: $(date)</p>
        <p>镜像: $IMAGE_NAME</p>
    </div>

    <div class="section">
        <h2>扫描配置</h2>
        <p>扫描类型: $SCAN_TYPE</p>
    </div>

    <div class="section">
        <h2>扫描结果摘要</h2>
        <!-- 这里可以添加更详细的统计信息 -->
    </div>
</body>
</html>
EOF

    log_success "HTML报告生成: $html_file"
}

# 主函数
main() {
    if [[ -z "$IMAGE_NAME" ]]; then
        log_error "请指定镜像名称"
        echo "用法: $0 <image_name> [scan_type]"
        echo "scan_type: critical, high, full (默认: full)"
        exit 1
    fi

    log_info "开始安全扫描: $IMAGE_NAME"
    log_info "扫描类型: $SCAN_TYPE"

    # 检查工具
    check_tools

    # 设置报告目录
    setup_report_dir

    # 执行扫描
    local scan_success=true

    trivy_scan "$IMAGE_NAME" || scan_success=false
    docker_security_check "$IMAGE_NAME"
    generate_html_report

    # 输出结果
    if [[ "$scan_success" == "true" ]]; then
        log_success "安全扫描完成，所有检查通过"
        exit 0
    else
        log_error "安全扫描发现问题，请检查报告"
        exit 1
    fi
}

# 执行主函数
main "$@"
```

### 2. 运行时安全配置

#### 安全启动脚本
```bash
#!/bin/bash
# app-entrypoint.sh - 应用安全启动脚本

set -euo pipefail

# 配置变量
APP_NAME="${APP_NAME:-unknown}"
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"

# 安全检查函数
security_check() {
    # 检查是否以root用户运行
    if [[ $EUID -eq 0 ]]; then
        echo "错误: 不应该以root用户运行容器"
        exit 1
    fi

    # 检查关键目录权限
    local dirs=("/app" "/app/logs" "/tmp")
    for dir in "${dirs[@]}"; do
        if [[ ! -w "$dir" ]]; then
            echo "错误: 目录 $dir 不可写"
            exit 1
        fi
    done

    echo "安全检查通过"
}

# 环境变量验证
validate_env() {
    # 验证必要的环境变量
    local required_vars=("SERVER_PORT" "MANAGEMENT_SERVER_PORT")
    for var in "${required_vars[@]}"; do
        if [[ -z "${!var:-}" ]]; then
            echo "警告: 环境变量 $var 未设置"
        fi
    done

    # 检查JVM参数合理性
    local max_heap=$(echo "$JAVA_OPTS" | grep -oP '-Xmx([0-9]+[mgMG])' | head -1)
    if [[ -n "$max_heap" ]]; then
        # 验证堆内存不超过容器内存的80%
        local container_memory_limit=$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes 2>/dev/null || echo "0")
        if [[ "$container_memory_limit" -gt 0 ]]; then
            local max_heap_bytes=$(echo "$max_heap" | sed 's/m/*1024*1024/g; s/g/*1024*1024*1024/g; s/M/*1024*1024/g; s/G/*1024*1024*1024/g' | bc)
            local max_allowed=$((container_memory_limit * 80 / 100))

            if [[ "$max_heap_bytes" -gt "$max_allowed" ]]; then
                echo "警告: 堆内存 $max_heap 超过容器内存的80%"
            fi
        fi
    fi
}

# JVM优化
optimize_jvm() {
    # 检测容器内存限制
    local container_memory_limit=$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes 2>/dev/null || echo "0")

    if [[ "$container_memory_limit" -gt 0 ]]; then
        local memory_mb=$((container_memory_limit / 1024 / 1024))

        # 动态调整JVM参数
        if [[ -z "$(echo "$JAVA_OPTS" | grep -o '-Xmx')" ]]; then
            # 如果未设置最大堆，使用容器内存的75%
            local heap_size=$((memory_mb * 75 / 100))
            JAVA_OPTS="$JAVA_OPTS -Xmx${heap_size}m -Xms${heap_size}m"
        fi

        # 添加容器支持参数
        JAVA_OPTS="$JAVA_OPTS -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
    fi

    # 添加GC优化参数
    JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC -XX:+DisableExplicitGC -XX:+AlwaysPreTouch"

    # 添加JFR参数（如果启用）
    if [[ "${ENABLE_JFR:-false}" == "true" ]]; then
        JAVA_OPTS="$JAVA_OPTS -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=/app/logs/startup.jfr"
    fi

    export JAVA_OPTS
    echo "JVM参数: $JAVA_OPTS"
}

# 信号处理
signal_handler() {
    echo "收到停止信号，正在优雅关闭应用..."

    # 获取Java进程ID
    local java_pid=$(pgrep -f "app.jar")

    if [[ -n "$java_pid" ]]; then
        # 发送SIGTERM信号
        kill -TERM "$java_pid"

        # 等待进程结束
        local count=0
        while kill -0 "$java_pid" 2>/dev/null && [[ $count -lt 30 ]]; do
            sleep 1
            ((count++))
        done

        # 如果进程仍在运行，强制杀死
        if kill -0 "$java_pid" 2>/dev/null; then
            echo "强制终止应用进程"
            kill -KILL "$java_pid"
        fi
    fi

    exit 0
}

# 健康检查函数
health_check() {
    local max_attempts=30
    local attempt=1

    while [[ $attempt -le $max_attempts ]]; do
        if curl -f -s "http://localhost:${MANAGEMENT_SERVER_PORT:-8081}/actuator/health" > /dev/null; then
            echo "应用健康检查通过"
            return 0
        fi

        echo "等待应用启动... ($attempt/$max_attempts)"
        sleep 2
        ((attempt++))
    done

    echo "应用启动超时"
    return 1
}

# 主启动函数
start_application() {
    echo "启动应用: $APP_NAME"
    echo "Spring Profile: $SPRING_PROFILES_ACTIVE"
    echo "JVM参数: $JAVA_OPTS"

    # 启动应用
    exec java $JAVA_OPTS -jar app.jar
}

# 主流程
main() {
    echo "=== IOE-DREAM 应用启动脚本 ==="
    echo "应用名称: $APP_NAME"
    echo "启动时间: $(date)"

    # 设置信号处理
    trap 'signal_handler' SIGTERM SIGINT SIGQUIT

    # 执行检查
    security_check
    validate_env
    optimize_jvm

    # 启动应用
    start_application
}

# 执行主函数
main "$@"
```

---

## 📊 性能监控与优化

### 1. 容器性能监控

#### 监控配置
```yaml
# docker-compose.monitoring.yml
version: '3.8'

services:
  # 应用服务
  app:
    image: ioe-dream/user-service:latest
    environment:
      - JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
    ports:
      - "8080:8080"
      - "8081:8081"
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
    labels:
      - "prometheus.scrape=true"
      - "prometheus.port=8081"
      - "prometheus.path=/actuator/prometheus"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  # 性能监控
  cAdvisor:
    image: gcr.io/cadvisor/cadvisor:v0.47.0
    container_name: cadvisor
    ports:
      - "8082:8080"
    volumes:
      - /:/rootfs:ro
      - /var/run:/var/run:rw
      - /sys:/sys:ro
      - /var/lib/docker/:/var/lib/docker:ro
      - /dev/disk/:/dev/disk:ro
    privileged: true
    devices:
      - /dev/kmsg
    labels:
      - "prometheus.scrape=true"
      - "prometheus.port=8080"

  # Node Exporter
  node-exporter:
    image: prom/node-exporter:v1.6.1
    container_name: node-exporter
    ports:
      - "9100:9100"
    volumes:
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /:/rootfs:ro
    command:
      - '--path.procfs=/host/proc'
      - '--path.rootfs=/rootfs'
      - '--path.sysfs=/host/sys'
      - '--collector.filesystem.mount-points-exclude=^/(sys|proc|dev|host|etc)($$|/)'
    labels:
      - "prometheus.scrape=true"
      - "prometheus.port=9100"

  # Docker Exporter
  docker-exporter:
    image: prom/docker-exporter:v0.3.1
    container_name: docker-exporter
    ports:
      - "9323:9323"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
    environment:
      - DOCKER_EXPORTER_PATH=/var/run/docker.sock
    labels:
      - "prometheus.scrape=true"
      - "prometheus.port=9323"

networks:
  default:
    name: ioe-dream-monitoring
```

### 2. 性能分析脚本

#### 容器性能分析工具
```bash
#!/bin/bash
# container-performance.sh - 容器性能分析脚本

set -euo pipefail

# 配置变量
CONTAINER_NAME="${1:-}"
DURATION="${2:-60}"
REPORT_DIR="performance-reports"
TIMESTAMP=$(date +%Y%m%d%H%M%S)

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 检查容器是否存在
check_container() {
    if ! docker ps --format "table {{.Names}}" | grep -q "^$CONTAINER_NAME$"; then
        log_error "容器 '$CONTAINER_NAME' 不存在或未运行"
        exit 1
    fi
}

# 收集CPU使用率
collect_cpu_stats() {
    local report_file="$REPORT_DIR/cpu-stats-${TIMESTAMP}.txt"

    log_info "收集CPU统计信息..."

    echo "容器CPU使用率统计" > "$report_file"
    echo "时间范围: ${DURATION}秒" >> "$report_file"
    echo "开始时间: $(date)" >> "$report_file"
    echo "" >> "$report_file"

    # 收集实时CPU使用率
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}" "$CONTAINER_NAME" >> "$report_file"

    # 持续监控CPU使用率
    echo "" >> "$report_file"
    echo "实时CPU使用率 (每5秒采样):" >> "$report_file"

    local end_time=$(($(date +%s) + DURATION))
    while [[ $(date +%s) -lt $end_time ]]; do
        local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
        local cpu_usage=$(docker stats --no-stream --format "{{.CPUPerc}}" "$CONTAINER_NAME")
        echo "$timestamp: $cpu_usage" >> "$report_file"
        sleep 5
    done

    log_success "CPU统计信息收集完成: $report_file"
}

# 收集内存使用情况
collect_memory_stats() {
    local report_file="$REPORT_DIR/memory-stats-${TIMESTAMP}.txt"

    log_info "收集内存统计信息..."

    echo "容器内存使用统计" > "$report_file"
    echo "时间范围: ${DURATION}秒" >> "$report_file"
    echo "开始时间: $(date)" >> "$report_file"
    echo "" >> "$report_file"

    # 获取容器内存限制
    local memory_limit=$(docker inspect "$CONTAINER_NAME" | jq '.[0].HostConfig.Memory // "unlimited"')
    echo "内存限制: $memory_limit bytes" >> "$report_file"

    # 监控内存使用
    local end_time=$(($(date +%s) + DURATION))
    while [[ $(date +%s) -lt $end_time ]]; do
        local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
        local mem_usage=$(docker stats --no-stream --format "{{.MemUsage}}" "$CONTAINER_NAME")
        echo "$timestamp: $mem_usage" >> "$report_file"
        sleep 5
    done

    log_success "内存统计信息收集完成: $report_file"
}

# 收集网络I/O统计
collect_network_stats() {
    local report_file="$REPORT_DIR/network-stats-${TIMESTAMP}.txt"

    log_info "收集网络I/O统计信息..."

    echo "容器网络I/O统计" > "$report_file"
    echo "时间范围: ${DURATION}秒" >> "$report_file"
    echo "开始时间: $(date)" >> "$report_file"
    echo "" >> "$report_file"

    # 监控网络I/O
    local end_time=$(($(date +%s) + DURATION))
    while [[ $(date +%s) -lt $end_time ]]; do
        local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
        local net_io=$(docker stats --no-stream --format "{{.NetIO}}" "$CONTAINER_NAME")
        echo "$timestamp: $net_io" >> "$report_file"
        sleep 5
    done

    log_success "网络I/O统计信息收集完成: $report_file"
}

# 收集磁盘I/O统计
collect_disk_stats() {
    local report_file="$REPORT_DIR/disk-stats-${TIMESTAMP}.txt"

    log_info "收集磁盘I/O统计信息..."

    echo "容器磁盘I/O统计" > "$report_file"
    echo "时间范围: ${DURATION}秒" >> "$report_file"
    echo "开始时间: $(date)" >> "$report_file"
    echo "" >> "$report_file"

    # 监控磁盘I/O
    local end_time=$(($(date +%s) + DURATION))
    while [[ $(date +%s) -lt $end_time ]]; do
        local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
        local disk_io=$(docker stats --no-stream --format "{{.BlockIO}}" "$CONTAINER_NAME")
        echo "$timestamp: $disk_io" >> "$report_file"
        sleep 5
    done

    log_success "磁盘I/O统计信息收集完成: $report_file"
}

# 生成性能报告
generate_performance_report() {
    local report_file="$REPORT_DIR/performance-report-${TIMESTAMP}.html"

    log_info "生成性能分析报告..."

    cat > "$report_file" << EOF
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>容器性能分析报告</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #f5f5f5; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
        .section { margin: 20px 0; }
        .chart-container { width: 100%; height: 400px; margin: 20px 0; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .metric { display: flex; justify-content: space-between; margin: 10px 0; }
        .metric-value { font-weight: bold; }
    </style>
</head>
<body>
    <div class="header">
        <h1>IOE-DREAM 容器性能分析报告</h1>
        <p><strong>容器名称:</strong> $CONTAINER_NAME</p>
        <p><strong>分析时间:</strong> $(date)</p>
        <p><strong>监控时长:</strong> ${DURATION}秒</p>
    </div>

    <div class="section">
        <h2>关键性能指标</h2>
        <div class="metric">
            <span>CPU使用率峰值:</span>
            <span class="metric-value" id="cpu-peak">计算中...</span>
        </div>
        <div class="metric">
            <span>内存使用率峰值:</span>
            <span class="metric-value" id="memory-peak">计算中...</span>
        </div>
        <div class="metric">
            <span>平均网络I/O:</span>
            <span class="metric-value" id="network-avg">计算中...</span>
        </div>
        <div class="metric">
            <span>平均磁盘I/O:</span>
            <span class="metric-value" id="disk-avg">计算中...</span>
        </div>
    </div>

    <div class="section">
        <h2>CPU使用率趋势</h2>
        <div class="chart-container">
            <canvas id="cpuChart"></canvas>
        </div>
    </div>

    <div class="section">
        <h2>内存使用趋势</h2>
        <div class="chart-container">
            <canvas id="memoryChart"></canvas>
        </div>
    </div>

    <script>
        // 这里可以添加JavaScript代码来读取数据并渲染图表
        // 由于篇幅限制，这里只提供基本框架
        document.getElementById('cpu-peak').textContent = 'XX%';
        document.getElementById('memory-peak').textContent = 'XX%';
        document.getElementById('network-avg').textContent = 'XX MB/s';
        document.getElementById('disk-avg').textContent = 'XX MB/s';
    </script>
</body>
</html>
EOF

    log_success "性能报告生成: $report_file"
}

# 主函数
main() {
    if [[ -z "$CONTAINER_NAME" ]]; then
        log_error "请指定容器名称"
        echo "用法: $0 <container_name> [duration_seconds]"
        exit 1
    fi

    log_info "开始容器性能分析"
    log_info "容器名称: $CONTAINER_NAME"
    log_info "监控时长: ${DURATION}秒"

    # 创建报告目录
    mkdir -p "$REPORT_DIR"

    # 检查容器
    check_container

    # 收集性能数据
    collect_cpu_stats &
    collect_memory_stats &
    collect_network_stats &
    collect_disk_stats &

    # 等待所有收集任务完成
    wait

    # 生成综合报告
    generate_performance_report

    log_success "性能分析完成，报告保存在: $REPORT_DIR"
}

# 执行主函数
main "$@"
```

---

## ⚠️ 最佳实践与注意事项

### ✅ 推荐实践

1. **镜像优化**
   - 使用多阶段构建减少镜像大小
   - 选择合适的基础镜像
   - 优化Dockerfile层结构
   - 启用镜像扫描和漏洞检测

2. **安全加固**
   - 使用非root用户运行容器
   - 最小化运行时依赖
   - 定期更新基础镜像
   - 实施镜像签名验证

3. **性能优化**
   - 合理设置资源限制
   - 优化JVM参数
   - 配置健康检查
   - 监控资源使用情况

4. **运维管理**
   - 标准化镜像标签规范
   - 自动化构建和部署
   - 完善的日志和监控
   - 建立回滚机制

### ❌ 避免的陷阱

1. **构建问题**
   - 避免在镜像中包含敏感信息
   - 不要使用latest标签
   - 避免镜像层数过多
   - 不要忽略构建缓存优化

2. **运行时问题**
   - 避免root用户运行
   - 不要忽视资源限制
   - 避免容器内部状态管理
   - 不要忽略健康检查

3. **安全问题**
   - 不要使用不安全的镜像源
   - 避免暴露不必要的端口
   - 不要忽视漏洞扫描
   - 避免弱密码和默认配置

---

## 📊 评估标准

### 🎯 技能掌握评估

#### 理论知识 (30%)
- [ ] Docker核心概念和架构
- [ ] 容器安全和最佳实践
- [ ] 性能优化原理和方法
- [ ] 容器编排和管理

#### 实践能力 (50%)
- [ ] 能够编写高效的Dockerfile
- [ ] 熟练使用多阶段构建
- [ ] 能够进行安全扫描和加固
- [ ] 掌握性能监控和调优

#### 问题解决 (20%)
- [ ] 容器性能问题排查
- [ ] 安全漏洞修复
- [ ] 构建优化策略
- [ ] 运维自动化实现

### 📈 质量标准

- **镜像大小**: < 500MB (应用镜像)
- **构建时间**: < 5分钟
- **安全扫描**: 0严重漏洞
- **性能监控**: 100%覆盖

---

## 🔗 相关技能

- **前置技能**: kubernetes-deployment-specialist, spring-boot-jakarta-guardian
- **相关技能**: monitoring-alerting-specialist, security-hardening-specialist
- **进阶技能**: cloud-native-architecture-specialist, performance-tuning-specialist

---

## 💡 持续学习方向

1. **容器新技术**: Podman、Buildah、Finch
2. **云原生安全**: Kata Containers、gVisor
3. **无服务器容器**: Knative、OpenFaaS
4. **边缘计算容器**: K3s、MicroK8s

---

**⚠️ 重要提醒**: Docker优化需要严格遵循IOE-DREAM项目的安全规范和性能要求，确保容器化应用的安全性、稳定性和高性能。