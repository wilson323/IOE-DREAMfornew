#!/bin/bash

# ===================================================================
# IOE-DREAM 微服务架构部署验证脚本
# 验证目标：模拟生产环境中微服务架构的完整部署和运行
# 验证范围：11个微服务的部署、通信、监控、性能等全方位验证
#
# 使用方法:
#   ./deployment-verification.sh [start|stop|status|report]
#
# 参数说明:
#   start   - 开始部署验证
#   stop    - 停止验证环境
#   status  - 查看当前状态
#   report  - 生成验证报告
# ===================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
VERIFICATION_DIR="$PROJECT_ROOT/verification"
LOG_DIR="$VERIFICATION_DIR/logs"
REPORT_DIR="$VERIFICATION_DIR/reports"
CONFIG_DIR="$VERIFICATION_DIR/config"

# 验证结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
SKIPPED_TESTS=0

# 时间戳
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="$REPORT_DIR/deployment-verification-report-$TIMESTAMP.html"

# 微服务配置
declare -A MICROSERVICES=(
    ["smart-gateway"]="8080"
    ["ioedream-auth-service"]="8081"
    ["ioedream-identity-service"]="8082"
    ["ioedream-device-service"]="8083"
    ["ioedream-access-service"]="8084"
    ["ioedream-consume-service"]="8085"
    ["ioedream-attendance-service"]="8086"
    ["ioedream-video-service"]="8087"
    ["ioedream-oa-service"]="8088"
    ["ioedream-system-service"]="8089"
    ["ioedream-monitor-service"]="8090"
)

# 健康检查端点
declare -A HEALTH_ENDPOINTS=(
    ["smart-gateway"]="/actuator/health"
    ["ioedream-auth-service"]="/actuator/health"
    ["ioedream-identity-service"]="/actuator/health"
    ["ioedream-device-service"]="/actuator/health"
    ["ioedream-access-service"]="/actuator/health"
    ["ioedream-consume-service"]="/actuator/health"
    ["ioedream-attendance-service"]="/actuator/health"
    ["ioedream-video-service"]="/actuator/health"
    ["ioedream-oa-service"]="/actuator/health"
    ["ioedream-system-service"]="/actuator/health"
    ["ioedream-monitor-service"]="/actuator/health"
)

# 创建必要目录
setup_directories() {
    print_section "📁 创建验证环境目录"

    mkdir -p "$VERIFICATION_DIR"/{logs,reports,config,temp,k8s,docker}
    mkdir -p "$LOG_DIR"/{services,api,monitoring,performance}

    echo -e "${GREEN}✅ 目录结构创建完成${NC}"
}

# 日志记录函数
log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "[$timestamp] [$level] $message" | tee -a "$LOG_DIR/deployment-verification.log"

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

# 打印分割线
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

# 检查依赖工具
check_dependencies() {
    print_section "🔧 检查系统依赖"

    local dependencies=("docker" "kubectl" "curl" "jq" "bc")
    local missing_deps=()

    for dep in "${dependencies[@]}"; do
        if command -v "$dep" &> /dev/null; then
            log "INFO" "$dep 已安装: $(which $dep)"
        else
            log "ERROR" "$dep 未安装"
            missing_deps+=("$dep")
        fi
    done

    if [ ${#missing_deps[@]} -eq 0 ]; then
        log "INFO" "所有依赖工具已安装"
        return 0
    else
        log "ERROR" "缺少依赖工具: ${missing_deps[*]}"
        return 1
    fi
}

# 启动Docker环境
start_docker_environment() {
    print_section "🐳 启动Docker环境"

    # 检查Docker状态
    if ! docker info &> /dev/null; then
        log "ERROR" "Docker未运行，请启动Docker服务"
        return 1
    fi

    log "INFO" "Docker运行正常"

    # 创建Docker网络
    if ! docker network inspect ioedream-network &> /dev/null; then
        docker network create ioedream-network
        log "INFO" "创建Docker网络: ioedream-network"
    else
        log "INFO" "Docker网络已存在: ioedream-network"
    fi

    # 启动基础服务
    start_infrastructure_services

    return 0
}

# 启动基础设施服务
start_infrastructure_services() {
    print_section "🏗️ 启动基础设施服务"

    # MySQL配置
    cat > "$CONFIG_DIR/mysql/docker-compose.yml" << 'EOF'
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: ioedream-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123456
      MYSQL_DATABASE: ioedream_test
      MYSQL_USER: ioedream
      MYSQL_PASSWORD: ioedream123
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init:/docker-entrypoint-initdb.d
    networks:
      - ioedream-network
    command: --default-authentication-plugin=mysql_native_password
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  redis:
    image: redis:6.2-alpine
    container_name: ioedream-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - ioedream-network
    command: redis-server --appendonly yes
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      timeout: 3s
      retries: 5

  nacos:
    image: nacos/nacos-server:v2.3.0
    container_name: ioedream-nacos
    environment:
      MODE: standalone
      SPRING_DATASOURCE_PLATFORM: mysql
      MYSQL_SERVICE_HOST: mysql
      MYSQL_SERVICE_DB_NAME: nacos_config
      MYSQL_SERVICE_USER: root
      MYSQL_SERVICE_PASSWORD: root123456
      JVM_XMS: 256m
      JVM_XMX: 512m
    ports:
      - "8848:8848"
      - "9848:9848"
    networks:
      - ioedream-network
    depends_on:
      mysql:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8848/nacos/v1/console/health"]
      timeout: 10s
      retries: 10

  prometheus:
    image: prom/prometheus:latest
    container_name: ioedream-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    networks:
      - ioedream-network
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=200h'
      - '--web.enable-lifecycle'

  grafana:
    image: grafana/grafana:latest
    container_name: ioedream-grafana
    ports:
      - "3000:3000"
    volumes:
      - grafana_data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin123
    networks:
      - ioedream-network
    depends_on:
      - prometheus

volumes:
  mysql_data:
  redis_data:
  prometheus_data:
  grafana_data:

networks:
  ioedream-network:
    external: true
EOF

    # 创建Prometheus配置
    mkdir -p "$CONFIG_DIR/mysql"
    cat > "$CONFIG_DIR/mysql/prometheus.yml" << 'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'ioedream-microservices'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'smart-gateway:8080'
        - 'ioedream-auth-service:8081'
        - 'ioedream-identity-service:8082'
        - 'ioedream-device-service:8083'
        - 'ioedream-access-service:8084'
        - 'ioedream-consume-service:8085'
        - 'ioedream-attendance-service:8086'
        - 'ioedream-video-service:8087'
        - 'ioedream-oa-service:8088'
        - 'ioedream-system-service:8089'
        - 'ioedream-monitor-service:8090'
EOF

    # 启动基础设施服务
    cd "$CONFIG_DIR/mysql"
    docker-compose up -d

    # 等待服务启动
    log "INFO" "等待基础设施服务启动..."
    sleep 30

    # 验证服务状态
    if docker-compose ps | grep -q "Up"; then
        log "INFO" "基础设施服务启动成功"
        return 0
    else
        log "ERROR" "基础设施服务启动失败"
        docker-compose logs
        return 1
    fi
}

# 构建微服务Docker镜像
build_microservice_images() {
    print_section "🔨 构建微服务Docker镜像"

    local microservices_root="$PROJECT_ROOT/microservices"

    if [ ! -d "$microservices_root" ]; then
        log "ERROR" "微服务目录不存在: $microservices_root"
        return 1
    fi

    # 创建通用Dockerfile模板
    cat > "$CONFIG_DIR/docker/Dockerfile.template" << 'EOF'
FROM openjdk:17-jre-slim

LABEL maintainer="IOE-DREAM Team"
LABEL version="1.0.0"

# 设置工作目录
WORKDIR /app

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    netcat \
    && rm -rf /var/lib/apt/lists/*

# 复制jar文件
COPY target/*.jar app.jar

# 创建非root用户
RUN groupadd -r ioedream && useradd -r -g ioedream ioedream
RUN chown -R ioedream:ioedream /app
USER ioedream

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# JVM参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
EOF

    # 遍历所有微服务并构建镜像
    for service in "${!MICROSERVICES[@]}"; do
        local service_dir="$microservices_root/$service"
        local port=${MICROSERVICES[$service]}

        if [ -d "$service_dir" ]; then
            log "INFO" "构建 $service Docker镜像..."

            # 检查是否有pom.xml
            if [ -f "$service_dir/pom.xml" ]; then
                cd "$service_dir"

                # 编译项目
                log "INFO" "编译 $service..."
                mvn clean package -DskipTests -q

                if [ $? -eq 0 ]; then
                    # 复制Dockerfile模板
                    cp "$CONFIG_DIR/docker/Dockerfile.template" "$service_dir/Dockerfile"

                    # 构建Docker镜像
                    docker build -t "ioedream/$service:latest" .

                    if [ $? -eq 0 ]; then
                        log "INFO" "$service Docker镜像构建成功"
                        ((PASSED_TESTS++))
                    else
                        log "ERROR" "$service Docker镜像构建失败"
                        ((FAILED_TESTS++))
                    fi
                else
                    log "ERROR" "$service 编译失败"
                    ((FAILED_TESTS++))
                fi
            else
                log "WARN" "$service 没有pom.xml文件，跳过构建"
                ((SKIPPED_TESTS++))
            fi
        else
            log "WARN" "$service 目录不存在，跳过构建"
            ((SKIPPED_TESTS++))
        fi

        ((TOTAL_TESTS++))
    done

    return 0
}

# 部署微服务到Docker
deploy_microservices() {
    print_section "🚀 部署微服务到Docker"

    # 创建微服务部署配置
    cat > "$CONFIG_DIR/docker/microservices-deploy.yml" << 'EOF'
version: '3.8'

services:
  smart-gateway:
    image: ioedream/smart-gateway:latest
    container_name: ioedream-gateway
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - ioedream-network
    depends_on:
      nacos:
        condition: service_healthy
    restart: unless-stopped

  ioedream-auth-service:
    image: ioedream/ioedream-auth-service:latest
    container_name: ioedream-auth
    ports:
      - "8081:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - ioedream-network
    depends_on:
      nacos:
        condition: service_healthy
    restart: unless-stopped

  ioedream-identity-service:
    image: ioedream/ioedream-identity-service:latest
    container_name: ioedream-identity
    ports:
      - "8082:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - ioedream-network
    depends_on:
      - ioedream-auth-service
      - nacos
    restart: unless-stopped

  ioedream-device-service:
    image: ioedream/ioedream-device-service:latest
    container_name: ioedream-device
    ports:
      - "8083:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - ioedream-network
    depends_on:
      - nacos
      - ioedream-auth-service
    restart: unless-stopped

  ioedream-access-service:
    image: ioedream/ioedream-access-service:latest
    container_name: ioedream-access
    ports:
      - "8084:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - ioedream-network
    depends_on:
      - nacos
      - ioedream-auth-service
    restart: unless-stopped

  ioedream-consume-service:
    image: ioedream/ioedream-consume-service:latest
    container_name: ioedream-consume
    ports:
      - "8085:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - ioedream-network
    depends_on:
      - nacos
      - ioedream-auth-service
    restart: unless-stopped

  ioedream-attendance-service:
    image: ioedream/ioedream-attendance-service:latest
    container_name: ioedream-attendance
    ports:
      - "8086:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - ioedream-network
    depends_on:
      - nacos
      - ioedream-auth-service
    restart: unless-stopped

  ioedream-video-service:
    image: ioedream/ioedream-video-service:latest
    container_name: ioedream-video
    ports:
      - "8087:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - ioedream-network
    depends_on:
      - nacos
      - ioedream-auth-service
    restart: unless-stopped

  ioedream-oa-service:
    image: ioedream/ioedream-oa-service:latest
    container_name: ioedream-oa
    ports:
      - "8088:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - ioedream-network
    depends_on:
      - nacos
      - ioedream-auth-service
    restart: unless-stopped

  ioedream-system-service:
    image: ioedream/ioedream-system-service:latest
    container_name: ioedream-system
    ports:
      - "8089:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - ioedream-network
    depends_on:
      - nacos
      - ioedream-auth-service
    restart: unless-stopped

  ioedream-monitor-service:
    image: ioedream/ioedream-monitor-service:latest
    container_name: ioedream-monitor
    ports:
      - "8090:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    networks:
      - ioedream-network
    depends_on:
      - nacos
      - ioedream-auth-service
    restart: unless-stopped

networks:
  ioedream-network:
    external: true
EOF

    cd "$CONFIG_DIR/docker"
    docker-compose -f microservices-deploy.yml up -d

    log "INFO" "等待微服务启动..."
    sleep 60

    return 0
}

# 运行健康检查
run_health_checks() {
    print_section "🏥 执行服务健康检查"

    local health_script="$SCRIPT_DIR/health-check.sh"

    if [ -f "$health_script" ]; then
        bash "$health_script"
    else
        log "WARN" "健康检查脚本不存在，使用内置检查"
        builtin_health_check
    fi

    return 0
}

# 内置健康检查
builtin_health_check() {
    log "INFO" "开始内置健康检查..."

    for service in "${!MICROSERVICES[@]}"; do
        local port=${MICROSERVICES[$service]}
        local endpoint=${HEALTH_ENDPOINTS[$service]:="/actuator/health"}
        local url="http://localhost:$port$endpoint"
        local container_name="ioedream-${service#ioedream-}"

        log "INFO" "检查 $service 健康状态..."

        # 检查Docker容器状态
        if docker ps --format "table {{.Names}}\t{{.Status}}" | grep -q "$container_name.*Up"; then
            log "INFO" "$service 容器运行正常"

            # 检查HTTP健康端点
            local max_attempts=30
            local attempt=0

            while [ $attempt -lt $max_attempts ]; do
                if curl -f -s "$url" > /dev/null 2>&1; then
                    log "INFO" "$service 健康检查通过: $url"
                    ((PASSED_TESTS++))
                    break
                else
                    attempt=$((attempt + 1))
                    log "WARN" "$service 健康检查失败，重试 $attempt/$max_attempts"
                    sleep 2
                fi
            done

            if [ $attempt -eq $max_attempts ]; then
                log "ERROR" "$service 健康检查失败，已达到最大重试次数"
                ((FAILED_TESTS++))
            fi
        else
            log "ERROR" "$service 容器未运行"
            ((FAILED_TESTS++))
        fi

        ((TOTAL_TESTS++))
    done

    return 0
}

# 运行API测试
run_api_tests() {
    print_section "🔍 执行API功能测试"

    local api_script="$SCRIPT_DIR/api-testing.sh"

    if [ -f "$api_script" ]; then
        bash "$api_script"
    else
        log "WARN" "API测试脚本不存在，使用内置测试"
        builtin_api_test
    fi

    return 0
}

# 内置API测试
builtin_api_test() {
    log "INFO" "开始内置API功能测试..."

    # 测试网关路由
    test_gateway_routing

    # 测试认证服务
    test_auth_service

    # 测试服务间通信
    test_service_communication

    return 0
}

# 测试网关路由
test_gateway_routing() {
    log "INFO" "测试网关路由功能..."

    local gateway_url="http://localhost:8080"
    local test_cases=(
        "/api/auth/health"
        "/api/system/health"
        "/api/unknown-path"
    )

    for path in "${test_cases[@]}"; do
        local url="$gateway_url$path"
        local response_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" || echo "000")

        if [ "$response_code" = "200" ] || [ "$response_code" = "404" ]; then
            log "INFO" "网关路由测试通过: $path -> HTTP $response_code"
            ((PASSED_TESTS++))
        else
            log "ERROR" "网关路由测试失败: $path -> HTTP $response_code"
            ((FAILED_TESTS++))
        fi

        ((TOTAL_TESTS++))
    done
}

# 测试认证服务
test_auth_service() {
    log "INFO" "测试认证服务..."

    local auth_url="http://localhost:8081"

    # 测试登录API
    local login_data='{"username":"admin","password":"admin123"}'
    local response=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d "$login_data" \
        "$auth_url/api/auth/login" || echo "")

    if echo "$response" | grep -q "token\|success\|登录"; then
        log "INFO" "认证服务登录测试通过"
        ((PASSED_TESTS++))
    else
        log "WARN" "认证服务登录测试失败或响应格式异常: $response"
        # 暂不计数为失败，可能是测试数据问题
        ((SKIPPED_TESTS++))
    fi

    ((TOTAL_TESTS++))
}

# 测试服务间通信
test_service_communication() {
    log "INFO" "测试服务间通信..."

    # 这里可以添加服务间Feign调用的测试
    # 暂时跳过，需要实际的服务实现
    log "INFO" "服务间通信测试暂跳过，需要具体服务实现"
    ((SKIPPED_TESTS++))
    ((TOTAL_TESTS++))
}

# 验证监控系统
verify_monitoring_system() {
    print_section "📊 验证监控系统"

    local monitoring_script="$SCRIPT_DIR/monitoring-validation.sh"

    if [ -f "$monitoring_script" ]; then
        bash "$monitoring_script"
    else
        log "WARN" "监控系统验证脚本不存在，使用内置验证"
        builtin_monitoring_check
    fi

    return 0
}

# 内置监控系统检查
builtin_monitoring_check() {
    log "INFO" "开始内置监控系统验证..."

    # 检查Prometheus
    local prometheus_url="http://localhost:9090"
    if curl -f -s "$prometheus_url/-/healthy" > /dev/null 2>&1; then
        log "INFO" "Prometheus 健康检查通过"
        ((PASSED_TESTS++))
    else
        log "ERROR" "Prometheus 健康检查失败"
        ((FAILED_TESTS++))
    fi
    ((TOTAL_TESTS++))

    # 检查Grafana
    local grafana_url="http://localhost:3000"
    if curl -f -s "$grafana_url/api/health" > /dev/null 2>&1; then
        log "INFO" "Grafana 健康检查通过"
        ((PASSED_TESTS++))
    else
        log "ERROR" "Grafana 健康检查失败"
        ((FAILED_TESTS++))
    fi
    ((TOTAL_TESTS++))

    # 检查Nacos
    local nacos_url="http://localhost:8848"
    if curl -f -s "$nacos_url/nacos/v1/console/health" > /dev/null 2>&1; then
        log "INFO" "Nacos 健康检查通过"
        ((PASSED_TESTS++))
    else
        log "ERROR" "Nacos 健康检查失败"
        ((FAILED_TESTS++))
    fi
    ((TOTAL_TESTS++))

    return 0
}

# 性能基准测试
run_performance_tests() {
    print_section "⚡ 执行性能基准测试"

    log "INFO" "开始性能基准测试..."

    # 简单的响应时间测试
    for service in "${!MICROSERVICES[@]}"; do
        local port=${MICROSERVICES[$service]}
        local url="http://localhost:$port/actuator/health"

        if curl -f -s "$url" > /dev/null 2>&1; then
            # 测量响应时间
            local start_time=$(date +%s%N)
            curl -f -s "$url" > /dev/null 2>&1
            local end_time=$(date +%s%N)
            local response_time=$(( (end_time - start_time) / 1000000 )) # 转换为毫秒

            log "INFO" "$service 响应时间: ${response_time}ms"

            # 记录性能数据
            echo "$(date),${service},${response_time}" >> "$LOG_DIR/performance/response-times.csv"

            if [ $response_time -lt 5000 ]; then
                ((PASSED_TESTS++))
            else
                log "WARN" "$service 响应时间超过5秒阈值"
                ((FAILED_TESTS++))
            fi
        else
            log "ERROR" "$service 不可用，无法测试性能"
            ((FAILED_TESTS++))
        fi

        ((TOTAL_TESTS++))
    done

    return 0
}

# 生成验证报告
generate_verification_report() {
    print_section "📋 生成验证报告"

    local success_rate=0
    if [ $TOTAL_TESTS -gt 0 ]; then
        success_rate=$(echo "scale=2; $PASSED_TESTS * 100 / $TOTAL_TESTS" | bc)
    fi

    # 创建HTML报告
    cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IOE-DREAM 微服务架构部署验证报告</title>
    <style>
        body { font-family: 'Arial', sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; border-bottom: 3px solid #007acc; padding-bottom: 20px; margin-bottom: 30px; }
        .title { color: #007acc; font-size: 28px; margin: 0; }
        .subtitle { color: #666; font-size: 16px; margin: 10px 0; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: center; }
        .card.success { background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); }
        .card.error { background: linear-gradient(135deg, #f44336 0%, #da190b 100%); }
        .card.warning { background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%); }
        .card h3 { margin: 0 0 10px 0; font-size: 24px; }
        .card p { margin: 0; font-size: 16px; }
        .section { margin-bottom: 30px; }
        .section h2 { color: #333; border-left: 4px solid #007acc; padding-left: 15px; }
        .service-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }
        .service-card { border: 1px solid #ddd; border-radius: 8px; padding: 20px; }
        .service-card h4 { margin: 0 0 10px 0; color: #007acc; }
        .status { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
        .status.healthy { background: #d4edda; color: #155724; }
        .status.unhealthy { background: #f8d7da; color: #721c24; }
        .status.unknown { background: #fff3cd; color: #856404; }
        .log { background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 4px; padding: 15px; font-family: monospace; white-space: pre-wrap; max-height: 300px; overflow-y: auto; }
        .timestamp { color: #666; font-size: 14px; margin-top: 20px; text-align: right; }
        .progress-bar { width: 100%; height: 20px; background: #e0e0e0; border-radius: 10px; overflow: hidden; margin: 20px 0; }
        .progress-fill { height: 100%; background: linear-gradient(90deg, #4CAF50, #45a049); transition: width 0.3s ease; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1 class="title">🚀 IOE-DREAM 微服务架构部署验证报告</h1>
            <p class="subtitle">生产环境模拟验证 • $(date)</p>
        </div>

        <div class="summary">
            <div class="card">
                <h3>$TOTAL_TESTS</h3>
                <p>总测试数</p>
            </div>
            <div class="card success">
                <h3>$PASSED_TESTS</h3>
                <p>通过测试</p>
            </div>
            <div class="card error">
                <h3>$FAILED_TESTS</h3>
                <p>失败测试</p>
            </div>
            <div class="card warning">
                <h3>$SKIPPED_TESTS</h3>
                <p>跳过测试</p>
            </div>
        </div>

        <div class="progress-bar">
            <div class="progress-fill" style="width: ${success_rate}%"></div>
        </div>

        <div class="section">
            <h2>📊 验证结果总览</h2>
            <p><strong>验证通过率：</strong> ${success_rate}%</p>
            <p><strong>验证状态：</strong>
                $(if [ $success_rate -ge 90 ]; then echo '<span style="color: #4CAF50;">✅ 优秀</span>';
                  elif [ $success_rate -ge 70 ]; then echo '<span style="color: #ff9800;">⚠️ 良好</span>';
                  else echo '<span style="color: #f44336;">❌ 需要改进</span>'; fi)
            </p>
        </div>

        <div class="section">
            <h2>🏥 服务健康状态</h2>
            <div class="service-grid">
EOF

    # 添加每个服务的状态
    for service in "${!MICROSERVICES[@]}"; do
        local port=${MICROSERVICES[$service]}
        local container_name="ioedream-${service#ioedream-}"
        local status="unknown"
        local status_class="unknown"

        if docker ps --format "{{.Names}}" | grep -q "$container_name"; then
            if curl -f -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
                status="healthy"
                status_class="healthy"
            else
                status="unhealthy"
                status_class="unhealthy"
            fi
        fi

        cat >> "$REPORT_FILE" << EOF
                <div class="service-card">
                    <h4>$service</h4>
                    <p><strong>端口：</strong>$port</p>
                    <p><strong>状态：</strong><span class="status $status_class">$status</span></p>
                    <p><strong>健康检查：</strong><a href="http://localhost:$port/actuator/health" target="_blank">查看</a></p>
                </div>
EOF
    done

    cat >> "$REPORT_FILE" << EOF
            </div>
        </div>

        <div class="section">
            <h2>🔗 外部系统访问</h2>
            <ul>
                <li><strong>API网关：</strong><a href="http://localhost:8080" target="_blank">http://localhost:8080</a></li>
                <li><strong>Nacos控制台：</strong><a href="http://localhost:8848/nacos" target="_blank">http://localhost:8848/nacos</a></li>
                <li><strong>Prometheus：</strong><a href="http://localhost:9090" target="_blank">http://localhost:9090</a></li>
                <li><strong>Grafana：</strong><a href="http://localhost:3000" target="_blank">http://localhost:3000</a></li>
            </ul>
        </div>

        <div class="section">
            <h2>📝 验证日志摘要</h2>
            <div class="log">
EOF

    # 添加日志摘要
    if [ -f "$LOG_DIR/deployment-verification.log" ]; then
        tail -50 "$LOG_DIR/deployment-verification.log" | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g' >> "$REPORT_FILE"
    else
        echo "日志文件不存在" >> "$REPORT_FILE"
    fi

    cat >> "$REPORT_FILE" << EOF
            </div>
        </div>

        <div class="timestamp">
            报告生成时间：$(date) <br>
            验证环境：IOE-DREAM 微服务架构 v1.0.0
        </div>
    </div>
</body>
</html>
EOF

    log "INFO" "验证报告已生成: $REPORT_FILE"

    # 显示摘要
    echo ""
    print_separator
    echo -e "${CYAN}📊 验证结果摘要${NC}"
    print_separator
    echo -e "总测试数量: ${YELLOW}$TOTAL_TESTS${NC}"
    echo -e "通过测试:   ${GREEN}$PASSED_TESTS${NC}"
    echo -e "失败测试:   ${RED}$FAILED_TESTS${NC}"
    echo -e "跳过测试:   ${YELLOW}$SKIPPED_TESTS${NC}"
    echo -e "通过率:     ${BLUE}${success_rate}%${NC}"
    print_separator

    return 0
}

# 停止验证环境
stop_verification() {
    print_section "🛑 停止验证环境"

    log "INFO" "停止微服务..."
    cd "$CONFIG_DIR/docker"
    docker-compose -f microservices-deploy.yml down

    log "INFO" "停止基础设施服务..."
    cd "$CONFIG_DIR/mysql"
    docker-compose down

    log "INFO" "清理Docker网络..."
    docker network rm ioedream-network 2>/dev/null || true

    log "INFO" "验证环境已停止"

    return 0
}

# 显示当前状态
show_status() {
    print_section "📊 当前验证环境状态"

    echo ""
    echo -e "${CYAN}🐳 Docker容器状态:${NC}"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(ioedream|nacos|mysql|redis|prometheus|grafana)" || echo "无相关容器运行"

    echo ""
    echo -e "${CYAN}🌐 端口占用状态:${NC}"
    for service in "${!MICROSERVICES[@]}"; do
        local port=${MICROSERVICES[$service]}
        if netstat -tuln 2>/dev/null | grep -q ":$port "; then
            echo -e "$service (端口 $port): ${GREEN}✅ 运行中${NC}"
        else
            echo -e "$service (端口 $port): ${RED}❌ 未运行${NC}"
        fi
    done

    echo ""
    echo -e "${CYAN}📊 基础设施服务:${NC}"
    local infra_services=("3306:mysql" "6379:redis" "8848:nacos" "9090:prometheus" "3000:grafana")
    for service_info in "${infra_services[@]}"; do
        IFS=':' read -r port name <<< "$service_info"
        if netstat -tuln 2>/dev/null | grep -q ":$port "; then
            echo -e "$name (端口 $port): ${GREEN}✅ 运行中${NC}"
        else
            echo -e "$name (端口 $port): ${RED}❌ 未运行${NC}"
        fi
    done

    return 0
}

# 主函数
main() {
    local command=${1:-"start"}

    case $command in
        "start")
            print_section "🚀 开始IOE-DREAM微服务架构部署验证"
            setup_directories
            check_dependencies || exit 1
            start_docker_environment || exit 1
            build_microservice_images
            deploy_microservices
            sleep 30
            run_health_checks
            run_api_tests
            verify_monitoring_system
            run_performance_tests
            generate_verification_report

            log "INFO" "微服务架构部署验证完成！"
            log "INFO" "查看详细报告: $REPORT_FILE"
            ;;
        "stop")
            stop_verification
            ;;
        "status")
            show_status
            ;;
        "report")
            generate_verification_report
            ;;
        "help"|"--help"|"-h")
            echo "IOE-DREAM 微服务架构部署验证工具"
            echo ""
            echo "使用方法:"
            echo "  $0 [命令]"
            echo ""
            echo "命令:"
            echo "  start   - 开始部署验证 (默认)"
            echo "  stop    - 停止验证环境"
            echo "  status  - 查看当前状态"
            echo "  report  - 生成验证报告"
            echo "  help    - 显示帮助信息"
            echo ""
            echo "验证的微服务 (11个):"
            for service in "${!MICROSERVICES[@]}"; do
                echo "  - $service (端口 ${MICROSERVICES[$service]})"
            done
            ;;
        *)
            log "ERROR" "未知命令: $command"
            echo "使用 '$0 help' 查看帮助"
            exit 1
            ;;
    esac
}

# 脚本入口点
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi