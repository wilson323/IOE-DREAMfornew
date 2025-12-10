#!/bin/bash

# IOE-DREAM Docker 构建脚本
# 解决构建顺序依赖问题，确保microservices-common先构建

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 检查Docker环境
check_docker() {
    log_step "检查Docker环境..."

    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi

    if ! docker info &> /dev/null; then
        log_error "Docker服务未启动，请启动Docker服务"
        exit 1
    fi

    log_info "Docker环境检查通过"
}

# 检查Docker Compose环境
check_docker_compose() {
    log_step "检查Docker Compose环境..."

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi

    log_info "Docker Compose环境检查通过"
}

# 创建Maven配置文件
create_maven_settings() {
    log_step "创建Maven配置文件..."

    mkdir -p ~/.m2

    cat > ~/.m2/settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0
          http://maven.apache.org/xsd/settings-1.2.0.xsd">

  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>

  <profiles>
    <profile>
      <id>aliyun</id>
      <repositories>
        <repository>
          <id>aliyun-central</id>
          <name>阿里云中央仓库</name>
          <url>https://maven.aliyun.com/repository/central</url>
        </repository>
        <repository>
          <id>aliyun-public</id>
          <name>阿里云公共仓库</name>
          <url>https://maven.aliyun.com/repository/public</url>
        </repository>
        <repository>
          <id>aliyun-spring</id>
          <name>阿里云Spring仓库</name>
          <url>https://maven.aliyun.com/repository/spring</url>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>aliyun-plugin</id>
          <name>阿里云插件仓库</name>
          <url>https://maven.aliyun.com/repository/public</url>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>

  <activeProfiles>
    <activeProfile>aliyun</activeProfile>
  </activeProfiles>
</settings>
EOF

    log_info "Maven配置文件创建完成"
}

# 构建microservices-common（关键步骤）
build_common_module() {
    log_step "构建microservices-common模块（关键依赖）..."

    # 检查项目结构
    if [ ! -d "microservices/microservices-common" ]; then
        log_error "microservices-common目录不存在"
        exit 1
    fi

    # 构建父POM
    log_info "构建父POM..."
    cd microservices
    mvn clean install -N -DskipTests -Dmaven.test.skip=true
    if [ $? -ne 0 ]; then
        log_error "父POM构建失败"
        exit 1
    fi

    # 构建common模块
    log_info "构建microservices-common模块..."
    cd microservices-common
    mvn clean install -DskipTests -Dmaven.test.skip=true
    if [ $? -ne 0 ]; then
        log_error "microservices-common模块构建失败"
        exit 1
    fi

    cd ../../
    log_info "microservices-common模块构建成功"
}

# 验证common模块是否安装成功
verify_common_module() {
    log_step "验证microservices-common模块安装..."

    # 检查本地Maven仓库
    COMMON_JAR="$HOME/.m2/repository/net/lab1024/sa/microservices-common/1.0.0/microservices-common-1.0.0.jar"
    if [ ! -f "$COMMON_JAR" ]; then
        log_error "microservices-common模块未安装到本地仓库"
        exit 1
    fi

    log_info "microservices-common模块验证成功"
}

# 构建微服务镜像
build_microservice_images() {
    log_step "构建微服务Docker镜像..."

    # 微服务列表（按依赖顺序）
    services=(
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
        log_info "构建 ${service} 镜像..."

        # 检查Dockerfile是否存在
        if [ ! -f "microservices/ioedream-${service}/Dockerfile" ]; then
            log_error "${service} 的Dockerfile不存在"
            exit 1
        fi

        # 构建镜像
        docker build \
            -f "microservices/ioedream-${service}/Dockerfile" \
            -t "ioedream/${service}:latest" \
            --build-arg MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository" \
            .

        if [ $? -ne 0 ]; then
            log_error "${service} 镜像构建失败"
            exit 1
        fi

        log_info "${service} 镜像构建成功"
    done
}

# 构建前端镜像
build_frontend_images() {
    log_step "构建前端应用镜像..."

    # Web管理后台
    if [ -d "smart-admin-web-javascript" ]; then
        log_info "构建Web管理后台镜像..."
        docker build -t "ioedream/web-admin:latest" ./smart-admin-web-javascript/
        if [ $? -eq 0 ]; then
            log_info "Web管理后台镜像构建成功"
        else
            log_warn "Web管理后台镜像构建失败（可选组件）"
        fi
    fi

    # 移动端应用
    if [ -d "smart-app" ]; then
        log_info "构建移动端应用镜像..."
        # 注意：uni-app通常不需要Docker镜像，这里只是示例
        log_warn "移动端应用（uni-app）通常不需要Docker镜像部署"
    fi
}

# 清理构建缓存
cleanup_build_cache() {
    log_step "清理构建缓存..."

    # 清理Maven缓存
    mvn clean > /dev/null 2>&1 || true

    # 清理Docker构建缓存（可选）
    read -p "是否清理Docker构建缓存？这将释放大量磁盘空间 (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker builder prune -f
        log_info "Docker构建缓存已清理"
    fi
}

# 显示构建结果
show_build_results() {
    log_step "显示构建结果..."

    echo ""
    echo -e "${GREEN}构建完成的镜像：${NC}"
    docker images | grep ioedream/

    echo ""
    echo -e "${GREEN}镜像大小统计：${NC}"
    docker images ioedream/ --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"

    echo ""
    echo -e "${GREEN}构建日志位置：${NC}"
    echo "- Maven日志: ./target/"
    echo "- Docker构建日志: docker logs <container_id>"
}

# 主函数
main() {
    echo ""
    echo -e "${GREEN}=============================================${NC}"
    echo -e "${GREEN}    IOE-DREAM Docker 构建 v1.0.0${NC}"
    echo -e "${GREEN}=============================================${NC}"
    echo ""

    # 解析命令行参数
    SKIP_COMMON=false
    SKIP_MICROSERVICES=false
    SKIP_FRONTEND=false
    CLEANUP=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            --skip-common)
                SKIP_COMMON=true
                shift
                ;;
            --skip-microservices)
                SKIP_MICROSERVICES=true
                shift
                ;;
            --skip-frontend)
                SKIP_FRONTEND=true
                shift
                ;;
            --cleanup)
                CLEANUP=true
                shift
                ;;
            --help)
                echo "用法: $0 [选项]"
                echo ""
                echo "选项:"
                echo "  --skip-common         跳过构建common模块"
                echo "  --skip-microservices  跳过构建微服务"
                echo "  --skip-frontend       跳过构建前端"
                echo "  --cleanup             构建完成后清理缓存"
                echo "  --help                显示此帮助信息"
                exit 0
                ;;
            *)
                log_error "未知选项: $1"
                exit 1
                ;;
        esac
    done

    # 执行构建步骤
    check_docker
    check_docker_compose
    create_maven_settings

    if [ "$SKIP_COMMON" = false ]; then
        build_common_module
        verify_common_module
    else
        log_warn "跳过构建common模块"
    fi

    if [ "$SKIP_MICROSERVICES" = false ]; then
        build_microservice_images
    else
        log_warn "跳过构建微服务"
    fi

    if [ "$SKIP_FRONTEND" = false ]; then
        build_frontend_images
    else
        log_warn "跳过构建前端"
    fi

    if [ "$CLEANUP" = true ]; then
        cleanup_build_cache
    fi

    show_build_results

    echo ""
    echo -e "${GREEN}🎉 Docker镜像构建完成！${NC}"
    echo ""
    echo -e "${BLUE}下一步操作：${NC}"
    echo "1. 配置环境变量文件: .env.production"
    echo "2. 启动服务: docker-compose -f docker-compose-production.yml up -d"
    echo "3. 查看服务状态: docker-compose -f docker-compose-production.yml ps"
    echo "4. 查看日志: docker-compose -f docker-compose-production.yml logs -f [service_name]"
    echo ""
}

# 错误处理
trap 'log_error "构建过程中发生错误，退出码: $?"' ERR

# 执行主函数
main "$@"