#!/bin/bash

# IOE-DREAM 微服务启动脚本
# 支持分阶段启动：基础设施 → 基础服务 → 业务服务 → 监控服务

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
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

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# 检查Docker和Docker Compose
check_prerequisites() {
    log_step "检查系统环境..."

    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi

    if ! docker info &> /dev/null; then
        log_error "Docker 服务未启动，请先启动 Docker"
        exit 1
    fi

    log_success "系统环境检查通过"
}

# 创建必要的目录
create_directories() {
    log_step "创建必要的目录结构..."

    # 创建日志目录
    mkdir -p /var/log/ioedream
    mkdir -p ./logs/{infrastructure,basic,business,monitoring}

    # 创建配置目录
    mkdir -p ./config/{mysql,redis,nacos,rabbitmq,nginx}
    mkdir -p ./config/{monitoring,prometheus,grafana}
    mkdir -p ./config/{logstash,filebeat,jaeger,seata}

    # 创建数据目录
    mkdir -p ./data/{mysql,redis,rabbitmq,elasticsearch,minio}

    # 创建备份目录
    mkdir -p ./backup/{mysql,redis,config}

    log_success "目录结构创建完成"
}

# 启动基础设施服务
start_infrastructure() {
    log_step "启动基础设施服务..."

    cd "$(dirname "$0")"

    log_info "启动 MySQL、Redis、Nacos、RabbitMQ、Elasticsearch..."
    docker-compose -f infrastructure.yml up -d

    log_info "等待基础设施服务启动..."
    sleep 30

    # 检查关键服务状态
    check_service_health "mysql-master" "MySQL主库"
    check_service_health "redis-master" "Redis主库"
    check_service_health "nacos" "Nacos注册中心"
    check_service_health "rabbitmq" "RabbitMQ消息队列"
    check_service_health "elasticsearch" "Elasticsearch搜索引擎"

    log_success "基础设施服务启动完成"
}

# 启动基础服务
start_basic_services() {
    log_step "启动基础服务..."

    cd "$(dirname "$0")"

    log_info "启动认证服务、身份服务、设备服务、区域服务..."
    docker-compose -f basic-services.yml up -d

    log_info "等待基础服务启动..."
    sleep 60

    # 检查基础服务状态
    check_service_health "ioedream-auth-service" "认证服务"
    check_service_health "ioedream-identity-service" "身份服务"
    check_service_health "ioedream-device-service" "设备服务"
    check_service_health "ioedream-area-service" "区域服务"

    log_success "基础服务启动完成"
}

# 启动业务服务
start_business_services() {
    log_step "启动核心业务服务..."

    cd "$(dirname "$0")"

    log_info "启动智能网关和核心业务服务..."
    docker-compose -f business-services.yml up -d

    log_info "等待核心业务服务启动..."
    sleep 90

    # 检查核心业务服务状态
    check_service_health "smart-gateway" "智能网关"
    check_service_health "ioedream-access-service" "门禁服务"
    check_service_health "ioedream-consume-service" "消费服务"
    check_service_health "ioedream-attendance-service" "考勤服务"
    check_service_health "ioedream-video-service" "视频服务"
    check_service_health "ioedream-visitor-service" "访客服务"
    check_service_health "ioedream-notification-service" "通知服务"
    check_service_health "ioedream-file-service" "文件服务"
    check_service_health "ioedream-report-service" "报表服务"

    log_success "核心业务服务启动完成"
}

# 启动扩展业务服务
start_extended_services() {
    log_step "启动扩展业务服务..."

    cd "$(dirname "$0")"

    log_info "启动新发现的扩展业务服务..."
    docker-compose -f extended-services.yml up -d

    log_info "等待扩展业务服务启动..."
    sleep 120

    # 检查扩展业务服务状态
    check_service_health "ioedream-hr-service" "人力资源服务"
    check_service_health "ioedream-erp-service" "企业资源服务"
    check_service_health "ioedream-monitor-service" "监控服务"
    check_service_health "ioedream-oa-service" "办公自动化服务"
    check_service_health "ioedream-biometric-service" "生物特征服务"
    check_service_health "ioedream-system-service" "系统管理服务"
    check_service_health "ioedream-web-main" "主前端应用"
    check_service_health "ioedream-admin-console" "管理控制台"
    check_service_health "ioedream-business-portal" "业务门户"
    check_service_health "ioedream-mobile-gateway" "移动端网关"

    log_success "扩展业务服务启动完成"
}

# 启动前端微服务
start_frontend_services() {
    log_step "启动前端微服务..."

    cd "$(dirname "$0")"

    log_info "启动前端微服务应用..."
    docker-compose -f frontend-services.yml up -d

    log_info "等待前端服务启动..."
    sleep 60

    # 检查前端服务状态
    check_service_health "ioedream-web-main" "主前端应用"
    check_service_health "ioedream-admin-console" "管理控制台"
    check_service_health "ioedream-business-portal" "业务门户"

    log_success "前端微服务启动完成"
}

# 启动移动端架构
start_mobile_services() {
    log_step "启动移动端架构..."

    cd "$(dirname "$0")"

    log_info "启动移动端网关和应用..."
    docker-compose -f mobile-services.yml up -d

    log_info "等待移动端服务启动..."
    sleep 60

    # 检查移动端服务状态
    check_service_health "ioedream-mobile-gateway" "移动端网关"
    check_service_health "ioedream-mobile-file-server" "移动端文件服务"

    log_success "移动端架构启动完成"
}

# 启动监控服务
start_monitoring() {
    log_step "启动监控服务..."

    cd "$(dirname "$0")"

    log_info "启动监控、日志、链路追踪等服务..."
    docker-compose -f monitoring.yml up -d

    log_info "等待监控服务启动..."
    sleep 60

    # 检查监控服务状态
    check_service_health "prometheus" "Prometheus监控"
    check_service_health "grafana" "Grafana可视化"
    check_service_health "zipkin" "Zipkin链路追踪"
    check_service_health "jaeger" "Jaeger分布式追踪"
    check_service_health "minio" "MinIO对象存储"

    log_success "监控服务启动完成"
}

# 检查服务健康状态
check_service_health() {
    local service_name=$1
    local display_name=$2
    local max_attempts=30
    local attempt=1

    log_info "检查 $display_name 健康状态..."

    while [ $attempt -le $max_attempts ]; do
        if docker-compose ps | grep -q "$service_name.*Up"; then
            log_success "$display_name 启动成功"
            return 0
        fi

        if [ $attempt -eq $max_attempts ]; then
            log_error "$display_name 启动失败或超时"
            return 1
        fi

        log_warn "$display_name 启动中... ($attempt/$max_attempts)"
        sleep 10
        ((attempt++))
    done
}

# 显示服务状态
show_services_status() {
    log_step "显示服务状态..."

    cd "$(dirname "$0")"

    echo -e "\n${CYAN}=== 服务状态概览 ===${NC}"
    echo -e "${BLUE}基础设施服务:${NC}"
    docker-compose -f infrastructure.yml ps

    echo -e "\n${BLUE}基础服务:${NC}"
    docker-compose -f basic-services.yml ps

    echo -e "\n${BLUE}业务服务:${NC}"
    docker-compose -f business-services.yml ps

    echo -e "\n${BLUE}监控服务:${NC}"
    docker-compose -f monitoring.yml ps

    echo -e "\n${GREEN}=== 服务访问地址 ===${NC}"
    echo -e "${YELLOW}• 智能网关: ${CYAN}http://localhost:8080${NC}"
    echo -e "${YELLOW}• Nacos控制台: ${CYAN}http://localhost:8848/nacos${NC}"
    echo -e "${YELLOW}• RabbitMQ管理: ${CYAN}http://localhost:15672${NC}"
    echo -e "${YELLOW}• Grafana监控: ${CYAN}http://localhost:3000${NC}"
    echo -e "${YELLOW}• Prometheus: ${CYAN}http://localhost:9090${NC}"
    echo -e "${YELLOW}• Zipkin链路追踪: ${CYAN}http://localhost:9411${NC}"
    echo -e "${YELLOW}• Jaeger分布式追踪: ${CYAN}http://localhost:16686${NC}"
    echo -e "${YELLOW}• Kibana日志分析: ${CYAN}http://localhost:5601${NC}"
    echo -e "${YELLOW}• MinIO对象存储: ${CYAN}http://localhost:9001${NC}"
}

# 停止所有服务
stop_all_services() {
    log_step "停止所有服务..."

    cd "$(dirname "$0")"

    log_info "停止监控服务..."
    docker-compose -f monitoring.yml down

    log_info "停止业务服务..."
    docker-compose -f business-services.yml down

    log_info "停止基础服务..."
    docker-compose -f basic-services.yml down

    log_info "停止基础设施服务..."
    docker-compose -f infrastructure.yml down

    log_success "所有服务已停止"
}

# 重启所有服务
restart_all_services() {
    log_step "重启所有服务..."

    stop_all_services
    sleep 10
    start_all_services
}

# 查看服务日志
view_logs() {
    local service_name=$1

    if [ -z "$service_name" ]; then
        log_error "请指定服务名称"
        echo "使用方法: $0 logs <service-name>"
        echo "可用服务:"
        cd "$(dirname "$0")"
        docker-compose config --services
        return 1
    fi

    log_info "查看 $service_name 日志..."
    cd "$(dirname "$0")"

    # 尝试在所有compose文件中查找服务
    for compose_file in infrastructure.yml basic-services.yml business-services.yml monitoring.yml; do
        if docker-compose -f "$compose_file" config --services | grep -q "^$service_name$"; then
            docker-compose -f "$compose_file" logs -f "$service_name"
            return 0
        fi
    done

    log_error "未找到服务: $service_name"
    return 1
}

# 备份数据
backup_data() {
    log_step "备份重要数据..."

    local backup_dir="./backup/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$backup_dir"

    log_info "备份数据到 $backup_dir"

    # 备份MySQL数据
    log_info "备份MySQL数据..."
    docker exec ioedream-mysql-master mysqldump -u root -pioedream@2024 --all-databases > "$backup_dir/mysql_backup.sql"

    # 备份Redis数据
    log_info "备份Redis数据..."
    docker exec redis-master redis-cli BGSAVE
    docker cp ioedream-redis-master:/data/dump.rdb "$backup_dir/redis_backup.rdb"

    # 备份配置文件
    log_info "备份配置文件..."
    cp -r ./config "$backup_dir/"

    log_success "数据备份完成: $backup_dir"
}

# 显示帮助信息
show_help() {
    echo -e "${CYAN}IOE-DREAM 微服务管理脚本${NC}"
    echo -e "${GREEN}使用方法:${NC}"
    echo "  $0 [命令] [选项]"
    echo ""
    echo -e "${YELLOW}可用命令:${NC}"
    echo "  start              启动所有服务"
    echo "  start infra         只启动基础设施服务"
    echo "  start basic        只启动基础服务"
    echo "  start business     只启动业务服务"
    echo "  start monitoring    只启动监控服务"
    echo "  stop               停止所有服务"
    echo "  restart            重启所有服务"
    echo "  status             显示服务状态"
    echo "  logs <service>     查看指定服务日志"
    echo "  backup             备份重要数据"
    echo "  help               显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 start           # 启动所有服务"
    echo "  $0 logs nacos      # 查看Nacos日志"
    echo "  $0 status          # 显示服务状态"
}

# 主函数
main() {
    case "$1" in
        "start")
            check_prerequisites
            create_directories

            if [ "$2" = "infra" ]; then
                start_infrastructure
            elif [ "$2" = "basic" ]; then
                start_infrastructure
                start_basic_services
            elif [ "$2" = "business" ]; then
                start_infrastructure
                start_basic_services
                start_business_services
            elif [ "$2" = "extended" ]; then
                start_infrastructure
                start_basic_services
                start_business_services
                start_extended_services
            elif [ "$2" = "frontend" ]; then
                start_infrastructure
                start_basic_services
                start_business_services
                start_frontend_services
            elif [ "$2" = "mobile" ]; then
                start_infrastructure
                start_basic_services
                start_business_services
                start_mobile_services
            elif [ "$2" = "monitoring" ]; then
                start_infrastructure
                start_basic_services
                start_business_services
                start_monitoring
            elif [ "$2" = "complete" ]; then
                start_infrastructure
                start_basic_services
                start_business_services
                start_extended_services
                start_frontend_services
                start_mobile_services
                start_monitoring
            else
                start_infrastructure
                start_basic_services
                start_business_services
                start_extended_services
                start_frontend_services
                start_mobile_services
                start_monitoring
            fi
            show_services_status
            ;;
        "stop")
            stop_all_services
            ;;
        "restart")
            restart_all_services
            ;;
        "status")
            show_services_status
            ;;
        "logs")
            view_logs "$2"
            ;;
        "backup")
            backup_data
            ;;
        "help"|"")
            show_help
            ;;
        *)
            log_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"