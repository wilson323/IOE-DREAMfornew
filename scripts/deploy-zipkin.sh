#!/bin/bash

# ============================================================
# IOE-DREAM Zipkin分布式追踪系统部署脚本
#
# @Author:    IOE-DREAM Team
# @Date:      2025-12-09
# @Description: 自动化部署和管理Zipkin分布式追踪系统
# ============================================================

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

# 项目配置
PROJECT_NAME="ioedream"
DEPLOYMENT_DIR="$(pwd)/deployments/zipkin"
DOCKER_COMPOSE_FILE="$DEPLOYMENT_DIR/docker-compose.yml"
ZIPKIN_CONFIG="$DEPLOYMENT_DIR/zipkin-config.properties"

# 服务端口配置
ZIPKIN_WEB_PORT=9411
ZIPKIN_API_PORT=9410
ELASTICSEARCH_PORT=9200
KIBANA_PORT=5601
PROMETHEUS_PORT=9943

# 健康检查URLs
ZIPKIN_HEALTH_URL="http://localhost:$ZIPKIN_WEB_PORT/health"
ELASTICSEARCH_HEALTH_URL="http://localhost:$ELASTICSEARCH_PORT/_cluster/health"
KIBANA_HEALTH_URL="http://localhost:$KIBANA_PORT/api/status"

# 检查Docker环境
check_docker() {
    log_info "检查Docker环境..."

    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装或未配置到PATH"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装或未配置到PATH"
        exit 1
    fi

    # 检查Docker服务状态
    if ! docker info &> /dev/null; then
        log_error "Docker服务未运行，请启动Docker服务"
        exit 1
    fi

    log_success "Docker环境检查通过"
}

# 检查端口占用
check_ports() {
    log_info "检查端口占用情况..."

    local ports=("$ZIPKIN_WEB_PORT" "$ELASTICSEARCH_PORT" "$KIBANA_PORT" "$PROMETHEUS_PORT")
    local services=("Zipkin Web UI" "Elasticsearch" "Kibana" "Prometheus")

    for i in "${!ports[@]}"; do
        local port=${ports[$i]}
        local service=${services[$i]}

        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            log_warning "$service 端口 $port 已被占用"
            log_info "尝试释放端口 $port..."

            # 尝试杀死占用端口的进程
            local pid=$(lsof -ti:$port)
            if [ ! -z "$pid" ]; then
                kill -9 $pid 2>/dev/null || true
                sleep 2
                if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
                    log_error "无法释放端口 $port，请手动处理"
                    exit 1
                else
                    log_success "端口 $port 已释放"
                fi
            fi
        fi
    done

    log_success "端口检查完成"
}

# 创建必要目录
create_directories() {
    log_info "创建必要的目录结构..."

    local directories=(
        "$DEPLOYMENT_DIR"
        "/opt/$PROJECT_NAME/data/elasticsearch-data"
        "/opt/$PROJECT_NAME/logs/elasticsearch"
        "/opt/$PROJECT_NAME/logs/zipkin"
        "$DEPLOYMENT_DIR/fluentd/conf"
        "$DEPLOYMENT_DIR/kibana"
    )

    for dir in "${directories[@]}"; do
        if [ ! -d "$dir" ]; then
            mkdir -p "$dir"
            log_info "创建目录: $dir"
        fi
    done

    # 设置目录权限
    chmod 755 "/opt/$PROJECT_NAME/data/elasticsearch-data"
    chmod 755 "/opt/$PROJECT_NAME/logs"

    log_success "目录结构创建完成"
}

# 创建Fluentd配置
create_fluentd_config() {
    log_info "创建Fluentd配置文件..."

    cat > "$DEPLOYMENT_DIR/fluentd/conf/fluent.conf" << 'EOF'
<source>
  @type tail
  path /var/log/zipkin
  pos_file /var/log/fluentd/zipkin.log.pos
  tag zipkin.logs
  read_from_head true
</source>

<filter zipkin.logs>
  @type record_transformer
  <record>
    hostname "#{Socket.gethostname}"
    service_name "zipkin"
  </record>
</filter>

<match zipkin.**>
  @type elasticsearch
  host elasticsearch
  port 9200
  index_name zipkin-logs
  type_name _doc
  include_tag_key true
  tag_key @log_name
</match>
EOF

    log_success "Fluentd配置文件创建完成"
}

# 创建环境变量文件
create_env_file() {
    log_info "创建环境变量配置文件..."

    cat > "$DEPLOYMENT_DIR/.env" << EOF
# ============================================================
# IOE-DREAM Zipkin环境变量配置
# ============================================================

# 项目标识
COMPOSE_PROJECT_NAME=ioedream-zipkin

# Zipkin版本
ZIPKIN_VERSION=2.24.1

# Elasticsearch版本
ELASTICSEARCH_VERSION=7.17.9

# 存储配置
STORAGE_TYPE=elasticsearch
ES_HOSTS=elasticsearch:9200
ES_INDEX=zipkin

# JVM配置
ZIPKIN_JAVA_OPTS=-Xms512m -Xmx1024m
ELASTICSEARCH_JAVA_OPTS=-Xms512m -Xmx1024m

# 采样配置
SAMPLE_RATE=0.1

# 日志级别
LOG_LEVEL=INFO

# 网络配置
SUBNET=172.20.0.0/16
GATEWAY=172.20.0.1

# 数据存储路径
ELASTICSEARCH_DATA_PATH=/opt/ioedream/data/elasticsearch-data
ELASTICSEARCH_LOGS_PATH=/opt/ioedream/logs/elasticsearch
ZIPKIN_LOGS_PATH=/opt/ioedream/logs/zipkin

# 健康检查间隔（秒）
HEALTH_CHECK_INTERVAL=30
HEALTH_CHECK_TIMEOUT=10
HEALTH_CHECK_RETRIES=5
HEALTH_CHECK_START_PERIOD=60

# 监控配置
PROMETHEUS_ENABLED=true
GRAFANA_ENABLED=true

# 备份配置
BACKUP_ENABLED=false
BACKUP_SCHEDULE="0 2 * * *"
BACKUP_RETENTION_DAYS=7

# 安全配置
SECURITY_ENABLED=false
CORS_ENABLED=true
CORS_ALLOWED_ORIGINS="*"
EOF

    log_success "环境变量配置文件创建完成"
}

# 创建系统服务配置
create_systemd_service() {
    log_info "创建Systemd服务配置..."

    cat > "/etc/systemd/system/$PROJECT_NAME-zipkin.service" << EOF
[Unit]
Description=IOE-DREAM Zipkin Distributed Tracing System
After=docker.service
Requires=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=$DEPLOYMENT_DIR
ExecStart=/usr/local/bin/docker-compose up -d
ExecStop=/usr/local/bin/docker-compose down
TimeoutStartSec=300
TimeoutStopSec=300

[Install]
WantedBy=multi-user.target
EOF

    # 重新加载systemd
    systemctl daemon-reload

    log_success "Systemd服务配置创建完成"
}

# 部署服务
deploy_services() {
    log_info "开始部署Zipkin服务..."

    cd "$DEPLOYMENT_DIR"

    # 停止现有服务（如果存在）
    log_info "停止现有服务..."
    docker-compose down -v || true

    # 拉取最新镜像
    log_info "拉取最新镜像..."
    docker-compose pull

    # 启动服务
    log_info "启动服务..."
    docker-compose up -d

    log_success "服务部署完成"
}

# 等待服务就绪
wait_for_services() {
    log_info "等待服务就绪..."

    local services=(
        "$ELASTICSEARCH_HEALTH_URL:Elasticsearch"
        "$ZIPKIN_HEALTH_URL:Zipkin"
        "$KIBANA_HEALTH_URL:Kibana"
    )

    for service_info in "${services[@]}"; do
        local url=${service_info%:*}
        local name=${service_info#*:}

        log_info "等待 $name 服务启动..."

        local retries=0
        local max_retries=60

        while [ $retries -lt $max_retries ]; do
            if curl -sf "$url" >/dev/null 2>&1; then
                log_success "$name 服务已就绪"
                break
            fi

            echo -n "."
            sleep 5
            ((retries++))
        done

        if [ $retries -eq $max_retries ]; then
            log_error "$name 服务启动超时"
            return 1
        fi
    done

    log_success "所有服务已就绪"
}

# 显示访问信息
show_access_info() {
    log_success "🎉 Zipkin分布式追踪系统部署成功！"
    echo ""
    echo "============================================================"
    echo "📊 服务访问地址："
    echo "============================================================"
    echo "🔍 Zipkin Web UI:     http://localhost:$ZIPKIN_WEB_PORT"
    echo "🔍 Zipkin API:       http://localhost:$ZIPKIN_API_PORT"
    echo "🔍 Elasticsearch:    http://localhost:$ELASTICSEARCH_PORT"
    echo "🔍 Kibana:           http://localhost:$KIBANA_PORT"
    echo "🔍 Prometheus:       http://localhost:$PROMETHEUS_PORT"
    echo ""
    echo "============================================================"
    echo "📖 访问说明："
    echo "============================================================"
    echo "📚 Zipkin Web UI: 查看分布式追踪链路"
    echo "📊 Elasticsearch: 直接查询追踪数据"
    echo "📈 Kibana: 数据可视化和分析"
    echo "📉 Prometheus: 监控指标收集"
    echo ""
    echo "============================================================"
    echo "🔧 管理命令："
    echo "============================================================"
    echo "📦 查看服务状态: docker-compose ps"
    echo "📦 查看服务日志: docker-compose logs [service-name]"
    echo "🛑 停止所有服务: docker-compose down"
    echo "🚀 重启所有服务: docker-compose restart"
    echo "🔄 更新服务: docker-compose pull && docker-compose up -d"
    echo ""
    echo "============================================================"
    echo "⚠️  注意事项："
    echo "============================================================"
    echo "🔒 确保防火墙已开放对应端口"
    echo "💾 Elasticsearch数据存储在: /opt/$PROJECT_NAME/data/"
    echo "📝 日志文件存储在: /opt/$PROJECT_NAME/logs/"
    echo "🔧 配置文件位置: $DEPLOYMENT_DIR/"
    echo ""
}

# 健康检查
health_check() {
    log_info "执行健康检查..."

    local all_healthy=true

    # 检查Docker容器状态
    log_info "检查Docker容器状态..."
    local containers=("$PROJECT_NAME-zipkin" "$PROJECT_NAME-zipkin-elasticsearch" "$PROJECT_NAME-zipkin-kibana")

    for container in "${containers[@]}"; do
        if docker ps --filter "name=$container" --format "table {{.Status}}" | grep -q "Up"; then
            log_success "✓ 容器 $container 运行正常"
        else
            log_error "✗ 容器 $container 运行异常"
            all_healthy=false
        fi
    done

    # 检查服务健康状态
    log_info "检查服务健康状态..."
    local urls=(
        "$ZIPKIN_HEALTH_URL:Zipkin"
        "$ELASTICSEARCH_HEALTH_URL:Elasticsearch"
    )

    for service_info in "${urls[@]}"; do
        local url=${service_info%:*}
        local name=${service_info#*:}

        if curl -sf "$url" >/dev/null 2>&1; then
            log_success "✓ $name 服务健康"
        else
            log_error "✗ $name 服务异常"
            all_healthy=false
        fi
    done

    if [ "$all_healthy" = true ]; then
        log_success "🎉 所有服务健康检查通过"
        return 0
    else
        log_error "❌ 部分服务存在问题"
        return 1
    fi
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  deploy     部署Zipkin服务"
    echo "  start      启动Zipkin服务"
    echo "  stop       停止Zipkin服务"
    echo "  restart    重启Zipkin服务"
    echo "  status     查看服务状态"
    echo "  logs       查看服务日志"
    echo "  health     执行健康检查"
    echo "  clean      清理数据和服务"
    echo "  backup     备份配置和数据"
    echo "  help       显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 deploy    # 完整部署"
    echo "  $0 start     # 启动服务"
    echo "  $0 status    # 查看状态"
    echo "  $0 health    # 健康检查"
}

# 启动服务
start_services() {
    log_info "启动Zipkin服务..."
    cd "$DEPLOYMENT_DIR"
    docker-compose up -d
    show_access_info
}

# 停止服务
stop_services() {
    log_info "停止Zipkin服务..."
    cd "$DEPLOYMENT_DIR"
    docker-compose down
    log_success "服务已停止"
}

# 重启服务
restart_services() {
    log_info "重启Zipkin服务..."
    stop_services
    sleep 5
    start_services
}

# 查看服务状态
show_status() {
    log_info "Zipkin服务状态："
    cd "$DEPLOYMENT_DIR"
    docker-compose ps
}

# 查看服务日志
show_logs() {
    local service="$1"
    cd "$DEPLOYMENT_DIR"

    if [ -z "$service" ]; then
        docker-compose logs -f
    else
        docker-compose logs -f "$service"
    fi
}

# 清理服务
clean_services() {
    log_warning "这将删除所有服务、配置和数据！"
    read -p "确定要继续吗？(y/N): " -n 1 -r
    echo

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cd "$DEPLOYMENT_DIR"
        docker-compose down -v
        docker system prune -f

        # 清理数据目录
        rm -rf /opt/$PROJECT_NAME/data/
        rm -rf /opt/$PROJECT_NAME/logs/

        log_success "清理完成"
    else
        log_info "取消清理操作"
    fi
}

# 备份配置和数据
backup_services() {
    log_info "备份Zipkin配置和数据..."

    local backup_dir="/opt/$PROJECT_NAME/backup/zipkin-$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$backup_dir"

    # 备份配置文件
    cp -r "$DEPLOYMENT_DIR"/* "$backup_dir/config/"

    # 备份数据
    if [ -d "/opt/$PROJECT_NAME/data/" ]; then
        cp -r "/opt/$PROJECT_NAME/data/" "$backup_dir/data/"
    fi

    # 备份日志
    if [ -d "/opt/$PROJECT_NAME/logs/" ]; then
        cp -r "/opt/$PROJECT_NAME/logs/" "$backup_dir/logs/"
    fi

    log_success "备份完成，备份位置: $backup_dir"
}

# 主函数
main() {
    case "${1:-deploy}" in
        "deploy")
            check_docker
            check_ports
            create_directories
            create_fluentd_config
            create_env_file
            create_systemd_service
            deploy_services
            wait_for_services
            show_access_info
            ;;
        "start")
            start_services
            ;;
        "stop")
            stop_services
            ;;
        "restart")
            restart_services
            ;;
        "status")
            show_status
            ;;
        "logs")
            show_logs "$2"
            ;;
        "health")
            health_check
            ;;
        "clean")
            clean_services
            ;;
        "backup")
            backup_services
            ;;
        "help"|"-h"|"--help")
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