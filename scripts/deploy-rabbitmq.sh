#!/bin/bash

# ============================================================
# IOE-DREAM RabbitMQ消息队列部署脚本
#
# @Author:    IOE-DREAM Team
# @Date:      2025-12-09
# @Description: 自动化部署和管理RabbitMQ消息队列系统
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
DEPLOYMENT_DIR="$(pwd)/deployments/rabbitmq"
DOCKER_COMPOSE_FILE="$DEPLOYMENT_DIR/docker-compose.yml"
RABBITMQ_CONFIG="$DEPLOYMENT_DIR/rabbitmq.conf"

require_env() {
    local name="$1"
    if [ -z "${!name}" ]; then
        log_error "缺少环境变量：$name（禁止使用默认口令，请显式配置）"
        exit 1
    fi
}

# 服务端口配置
RABBITMQ_PORT=5672
RABBITMQ_MANAGEMENT_PORT=15672
RABBITMQ_EPMD_PORT=4369
RABBITMQ_PROMETHEUS_PORT=15692

# 健康检查URLs
RABBITMQ_HEALTH_URL="http://localhost:$RABBITMQ_MANAGEMENT_PORT/api/healthchecks/node"

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

    if ! docker info &> /dev/null; then
        log_error "Docker服务未运行，请启动Docker服务"
        exit 1
    fi

    log_success "Docker环境检查通过"
}

# 检查端口占用
check_ports() {
    log_info "检查端口占用情况..."

    local ports=("$RABBITMQ_PORT" "$RABBITMQ_MANAGEMENT_PORT" "$RABBITMQ_EPMD_PORT" "$RABBITMQ_PROMETHEUS_PORT")
    local services=("RabbitMQ AMQP" "RabbitMQ Management" "RabbitMQ EPMD" "RabbitMQ Prometheus")

    for i in "${!ports[@]}"; do
        local port=${ports[$i]}
        local service=${services[$i]}

        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            log_warning "$service 端口 $port 已被占用"
            log_info "尝试释放端口 $port..."

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
        "/opt/$PROJECT_NAME/data/rabbitmq"
        "/opt/$PROJECT_NAME/logs/rabbitmq"
        "/opt/$PROJECT_NAME/config/rabbitmq"
    )

    for dir in "${directories[@]}"; do
        if [ ! -d "$dir" ]; then
            mkdir -p "$dir"
            log_info "创建目录: $dir"
        fi
    done

    # 设置目录权限
    chmod 755 "/opt/$PROJECT_NAME/data/rabbitmq"
    chmod 755 "/opt/$PROJECT_NAME/logs/rabbitmq"
    chmod 755 "/opt/$PROJECT_NAME/config/rabbitmq"

    log_success "目录结构创建完成"
}

# 创建RabbitMQ配置文件
create_rabbitmq_config() {
    log_info "创建RabbitMQ配置文件..."

    cat > "$DEPLOYMENT_DIR/rabbitmq.conf" << 'EOF'
# ============================================================
# IOE-DREAM RabbitMQ 配置文件
# ============================================================

# 基础配置
loopback_users.guest = false
listeners.tcp.default = 5672

# 集群配置
cluster_formation.peer_discovery_backend = classic_config
cluster_formation.classic_config.nodes.1 = rabbit@ioedream-rabbitmq-1
cluster_formation.classic_config.nodes.2 = rabbit@ioedream-rabbitmq-2
cluster_formation.classic_config.nodes.3 = rabbit@ioedream-rabbitmq-3
cluster_formation.node_cleanup_interval = 30
cluster_partition_handling = autoheal

# 内存和磁盘配置
vm_memory_high_watermark.relative = 0.6
disk_free_limit.absolute = 2GB
disk_free_limit.percentage = 2.0

# 心跳配置
heartbeat = 60

# 队列配置
default_vhost = ioedream

# 管理插件配置
management.tcp.port = 15672
management.tcp.ip = 0.0.0.0
management.path_prefix = /
management.ssl.render_fail_if_no_cert = false

# 日志配置
log.file.level = info
log.console = true
log.console.level = info
log.exchange = true
log.ldap = false

# Prometheus配置
prometheus.tcp.port = 15692
prometheus.tcp.ip = 0.0.0.0

# 延迟消息插件配置
delayed_message_exchange.enabled = true

# 消息TTL配置
default_message_ttl = 86400000

# 镜像队列配置
mirroring_enabled = true
mirroring_sync_batch_size = 4096

# 流控配置
vm_memory_high_watermark_paging_ratio = 0.75

# 消费者确认配置
consumer_timeout = 1800000

# 网络配置
tcp_listen_options.backlog = 128
tcp_listen_options.nodelay = true
tcp_listen_options.keepalive = true
tcp_listen_options.exit_on_close = true
tcp_listen_options.send_timeout = 15000
tcp_listen_options.recbuf = 32768
tcp_listen_options.sndbuf = 32768
EOF

    log_success "RabbitMQ配置文件创建完成"
}

# 创建启用插件配置
create_enabled_plugins() {
    log_info "创建启用插件配置..."

    cat > "$DEPLOYMENT_DIR/enabled_plugins" << 'EOF'
[rabbitmq_management,rabbitmq_prometheus,rabbitmq_delayed_message_exchange,rabbitmq_shovel,rabbitmq_shovel_management,rabbitmq_stomp].
EOF

    log_success "启用插件配置创建完成"
}

# 创建环境变量文件
create_env_file() {
    log_info "创建环境变量配置文件..."

    cat > "$DEPLOYMENT_DIR/.env" << EOF
# ============================================================
# IOE-DREAM RabbitMQ环境变量配置
# ============================================================

# 项目标识
COMPOSE_PROJECT_NAME=ioedream-rabbitmq

# RabbitMQ版本
RABBITMQ_VERSION=3.12.10-management

# 默认用户配置
RABBITMQ_DEFAULT_USER=admin
RABBITMQ_DEFAULT_PASS=${RABBITMQ_ADMIN_PASSWORD}
RABBITMQ_DEFAULT_VHOST=ioedream

# 集群配置
RABBITMQ_ERLANG_COOKIE=ioedream_rabbitmq_cookie_2023
RABBITMQ_USE_LONGNAME=true
RABBITMQ_NODENAME=rabbit@ioedream-rabbitmq-1

# 数据存储路径
RABBITMQ_DATA_PATH=/opt/$PROJECT_NAME/data/rabbitmq
RABBITMQ_LOGS_PATH=/opt/$PROJECT_NAME/logs/rabbitmq
RABBITMQ_CONFIG_PATH=/opt/$PROJECT_NAME/config/rabbitmq

# 网络配置
SUBNET=172.22.0.0/16
GATEWAY=172.22.0.1

# 健康检查配置
HEALTH_CHECK_INTERVAL=30
HEALTH_CHECK_TIMEOUT=10
HEALTH_CHECK_RETRIES=5
HEALTH_CHECK_START_PERIOD=60

# 监控配置
PROMETHEUS_ENABLED=true
MANAGEMENT_ENABLED=true

# 集群节点
RABBITMQ_NODE_1=ioedream-rabbitmq-1
RABBITMQ_NODE_2=ioedream-rabbitmq-2
RABBITMQ_NODE_3=ioedream-rabbitmq-3

# 性能配置
RABBITMQ_VM_MEMORY_HIGH_WATERMARK=0.6
RABBITMQ_DISK_FREE_LIMIT=2GB
RABBITMQ_DEFAULT_VHOST=ioedream

# 日志配置
RABBITMQ_LOG_LEVEL=info
RABBITMQ_LOG_CONSOLE=true
RABBITMQ_LOG_FILE=true

# 安全配置
RABBITMQ_SSL_ENABLED=false
RABBITMQ_MANAGEMENT_SSL_ENABLED=false

# 开发环境配置
---
spring:
  config:
    activate:
      profile: dev

RABBITMQ_DEFAULT_PASS=${RABBITMQ_ADMIN_PASSWORD}
RABBITMQ_ERLANG_COOKIE=dev_rabbitmq_cookie

# 生产环境配置
---
spring:
  config:
    activate:
      profile: prod

RABBITMQ_DEFAULT_PASS=${RABBITMQ_ADMIN_PASSWORD}
RABBITMQ_VM_MEMORY_HIGH_WATERMARK=0.7
RABBITMQ_DISK_FREE_LIMIT=5GB
RABBITMQ_LOG_LEVEL=warn
EOF

    log_success "环境变量配置文件创建完成"
}

# 创建Docker Compose文件
create_docker_compose() {
    log_info "创建Docker Compose配置..."

    cat > "$DEPLOYMENT_DIR/docker-compose.yml" << 'EOF'
version: '3.8'

services:
  # RabbitMQ主节点
  rabbitmq-1:
    image: rabbitmq:3.12.10-management
    container_name: ioedream-rabbitmq-1
    hostname: ioedream-rabbitmq-1
    ports:
      - "5672:5672"          # AMQP端口
      - "15672:15672"        # 管理界面端口
      - "15692:15692"        # Prometheus端口
      - "4369:4369"          # EPMD端口
      - "25672:25672"        # 集群通信端口
    environment:
      - RABBITMQ_DEFAULT_USER=admin
      - RABBITMQ_DEFAULT_PASS=${RABBITMQ_DEFAULT_PASS}
      - RABBITMQ_DEFAULT_VHOST=ioedream
      - RABBITMQ_ERLANG_COOKIE=ioedream_rabbitmq_cookie_2023
      - RABBITMQ_USE_LONGNAME=true
      - RABBITMQ_NODENAME=rabbit@ioedream-rabbitmq-1
      - RABBITMQ_CONFIG_FILE=/etc/rabbitmq/rabbitmq
    volumes:
      - ./rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf:ro
      - ./enabled_plugins:/etc/rabbitmq/enabled_plugins:ro
      - ioedream-rabbitmq-data:/var/lib/rabbitmq
      - ioedream-rabbitmq-logs:/var/log/rabbitmq
    restart: unless-stopped
    networks:
      - rabbitmq-network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    labels:
      - "ioe-dream.service=rabbitmq"
      - "ioe-dream.environment=production"
      - "ioe-dream.node-type=master"

  # RabbitMQ从节点1
  rabbitmq-2:
    image: rabbitmq:3.12.10-management
    container_name: ioedream-rabbitmq-2
    hostname: ioedream-rabbitmq-2
    ports:
      - "5673:5672"
      - "15673:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=admin
      - RABBITMQ_DEFAULT_PASS=${RABBITMQ_DEFAULT_PASS}
      - RABBITMQ_DEFAULT_VHOST=ioedream
      - RABBITMQ_ERLANG_COOKIE=ioedream_rabbitmq_cookie_2023
      - RABBITMQ_USE_LONGNAME=true
      - RABBITMQ_NODENAME=rabbit@ioedream-rabbitmq-2
      - RABBITMQ_CONFIG_FILE=/etc/rabbitmq/rabbitmq
    volumes:
      - ./rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf:ro
      - ./enabled_plugins:/etc/rabbitmq/enabled_plugins:ro
      - ioedream-rabbitmq-data-2:/var/lib/rabbitmq
      - ioedream-rabbitmq-logs-2:/var/log/rabbitmq
    depends_on:
      - rabbitmq-1
    restart: unless-stopped
    networks:
      - rabbitmq-network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    labels:
      - "ioe-dream.service=rabbitmq"
      - "ioe-dream.environment=production"
      - "ioe-dream.node-type=slave"

  # RabbitMQ从节点2
  rabbitmq-3:
    image: rabbitmq:3.12.10-management
    container_name: ioedream-rabbitmq-3
    hostname: ioedream-rabbitmq-3
    ports:
      - "5674:5672"
      - "15674:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=admin
      - RABBITMQ_DEFAULT_PASS=${RABBITMQ_DEFAULT_PASS}
      - RABBITMQ_DEFAULT_VHOST=ioedream
      - RABBITMQ_ERLANG_COOKIE=ioedream_rabbitmq_cookie_2023
      - RABBITMQ_USE_LONGNAME=true
      - RABBITMQ_NODENAME=rabbit@ioedream-rabbitmq-3
      - RABBITMQ_CONFIG_FILE=/etc/rabbitmq/rabbitmq
    volumes:
      - ./rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf:ro
      - ./enabled_plugins:/etc/rabbitmq/enabled_plugins:ro
      - ioedream-rabbitmq-data-3:/var/lib/rabbitmq
      - ioedream-rabbitmq-logs-3:/var/log/rabbitmq
    depends_on:
      - rabbitmq-1
    restart: unless-stopped
    networks:
      - rabbitmq-network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    labels:
      - "ioe-dream.service=rabbitmq"
      - "ioe-dream.environment=production"
      - "ioe-dream.node-type=slave"

  # Nginx负载均衡器
  nginx:
    image: nginx:1.24-alpine
    container_name: ioedream-rabbitmq-lb
    ports:
      - "5680:5672"          # AMQP负载均衡端口
      - "15675:80"           # 管理界面负载均衡端口
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - rabbitmq-1
      - rabbitmq-2
      - rabbitmq-3
    restart: unless-stopped
    networks:
      - rabbitmq-network
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    labels:
      - "ioe-dream.service=nginx"
      - "ioe-dream.environment=production"

networks:
  rabbitmq-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.22.0.0/16
          gateway: 172.22.0.1

volumes:
  # RabbitMQ数据卷
  ioedream-rabbitmq-data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /opt/ioedream/data/rabbitmq
  ioedream-rabbitmq-data-2:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /opt/ioedream/data/rabbitmq-2
  ioedream-rabbitmq-data-3:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /opt/ioedream/data/rabbitmq-3

  # RabbitMQ日志卷
  ioedream-rabbitmq-logs:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /opt/ioedream/logs/rabbitmq
  ioedream-rabbitmq-logs-2:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /opt/ioedream/logs/rabbitmq-2
  ioedream-rabbitmq-logs-3:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /opt/ioedream/logs/rabbitmq-3
EOF

    log_success "Docker Compose配置创建完成"
}

# 创建Nginx负载均衡配置
create_nginx_config() {
    log_info "创建Nginx负载均衡配置..."

    cat > "$DEPLOYMENT_DIR/nginx.conf" << 'EOF'
events {
    worker_connections 1024;
}

http {
    upstream rabbitmq_management {
        server ioedream-rabbitmq-1:15672;
        server ioedream-rabbitmq-2:15672;
        server ioedream-rabbitmq-3:15672;
    }

    # 健康检查端点
    server {
        listen 80;
        server_name localhost;

        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }

        # 管理界面代理
        location / {
            proxy_pass http://rabbitmq_management;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
EOF

    log_success "Nginx负载均衡配置创建完成"
}

# 部署服务
deploy_services() {
    log_info "部署RabbitMQ服务..."

    cd "$DEPLOYMENT_DIR"

    # 停止现有服务（如果存在）
    log_info "停止现有服务..."
    docker-compose down -v || true

    # 创建镜像（如果需要）
    log_info "拉取最新镜像..."
    docker-compose pull

    # 启动服务
    log_info "启动服务..."
    docker-compose up -d

    # 等待服务启动
    log_info "等待服务启动..."
    sleep 30

    # 配置集群
    log_info "配置RabbitMQ集群..."
    configure_cluster

    log_success "RabbitMQ服务部署完成"
}

# 配置集群
configure_cluster() {
    log_info "配置RabbitMQ集群..."

    # 等待节点完全启动
    sleep 20

    # 将节点2加入集群
    docker exec ioedream-rabbitmq-2 rabbitmqctl stop_app
    docker exec ioedream-rabbitmq-2 rabbitmqctl reset
    docker exec ioedream-rabbitmq-2 rabbitmqctl join_cluster rabbit@ioedream-rabbitmq-1
    docker exec ioedream-rabbitmq-2 rabbitmqctl start_app

    # 将节点3加入集群
    docker exec ioedream-rabbitmq-3 rabbitmqctl stop_app
    docker exec ioedream-rabbitmq-3 rabbitmqctl reset
    docker exec ioedream-rabbitmq-3 rabbitmqctl join_cluster rabbit@ioedream-rabbitmq-1
    docker exec ioedream-rabbitmq-3 rabbitmqctl start_app

    # 设置集群策略
    docker exec ioedream-rabbitmq-1 rabbitmqctl set_policy ha-all ".*" '{"ha-mode":"all","ha-sync-mode":"automatic"}'

    log_success "RabbitMQ集群配置完成"
}

# 等待服务就绪
wait_for_services() {
    log_info "等待服务就绪..."

    local retries=0
    local max_retries=60

    while [ $retries -lt $max_retries ]; do
        if curl -sf "http://localhost:$RABBITMQ_MANAGEMENT_PORT/api/healthchecks/node" >/dev/null 2>&1; then
            log_success "RabbitMQ服务已就绪"
            break
        fi

        echo -n "."
        sleep 5
        ((retries++))
    done

    if [ $retries -eq $max_retries ]; then
        log_error "RabbitMQ服务启动超时"
        return 1
    fi

    # 检查集群状态
    log_info "检查集群状态..."
    docker exec ioedream-rabbitmq-1 rabbitmqctl cluster_status

    log_success "所有服务已就绪"
}

# 显示访问信息
show_access_info() {
    log_success "🎉 RabbitMQ消息队列部署成功！"
    echo ""
    echo "============================================================"
    echo "📊 服务访问地址："
    echo "============================================================"
    echo "🔗 AMQP地址:          amqp://admin:(已隐藏)@localhost:5672/ioedream"
    echo "📊 管理界面:           http://localhost:15672"
    echo "📊 管理界面(负载均衡): http://localhost:15675"
    echo "📊 Prometheus指标:    http://localhost:15692/metrics"
    echo ""
    echo "============================================================"
    echo "🔐 登录信息："
    echo "============================================================"
    echo "👤 用户名:             admin"
    echo "🔒 密码:               (已隐藏，使用环境变量 RABBITMQ_ADMIN_PASSWORD)"
    echo "🏠 虚拟主机:           ioedream"
    echo ""
    echo "============================================================"
    echo "🏗️ 集群节点信息："
    echo "============================================================"
    echo "🥇 主节点:             ioedream-rabbitmq-1 (15672)"
    echo "🥈 从节点1:            ioedream-rabbitmq-2 (15673)"
    echo "🥉 从节点2:            ioedream-rabbitmq-3 (15674)"
    echo ""
    echo "============================================================"
    echo "🔧 管理命令："
    echo "============================================================"
    echo "📦 查看服务状态:       docker-compose ps"
    echo "📦 查看服务日志:       docker-compose logs [node-name]"
    echo "🛑 停止所有服务:       docker-compose down"
    echo "🚀 重启所有服务:       docker-compose restart"
    echo "🔄 集群状态检查:       docker exec ioedream-rabbitmq-1 rabbitmqctl cluster_status"
    echo ""
    echo "============================================================"
    echo "📊 集群管理命令："
    echo "============================================================"
    echo "📋 列出用户:           docker exec ioedream-rabbitmq-1 rabbitmqctl list_users"
    echo "📋 列出虚拟主机:       docker exec ioedream-rabbitmq-1 rabbitmqctl list_vhosts"
    echo "📋 列出队列:           docker exec ioedream-rabbitmq-1 rabbitmqctl list_queues"
    echo "📋 列出交换机:         docker exec ioedream-rabbitmq-1 rabbitmqctl list_exchanges"
    echo "📋 列出策略:           docker exec ioedream-rabbitmq-1 rabbitmqctl list_policies"
    echo ""
    echo "============================================================"
    echo "📈 应用连接配置："
    echo "============================================================"
    echo "spring.rabbitmq.host=localhost"
    echo "spring.rabbitmq.port=5672"
    echo "spring.rabbitmq.username=admin"
    echo "spring.rabbitmq.password=${RABBITMQ_ADMIN_PASSWORD}"
    echo "spring.rabbitmq.virtual-host=ioedream"
    echo ""
    echo "============================================================"
    echo "⚠️  注意事项："
    echo "============================================================"
    echo "🔒 确保防火墙已开放对应端口"
    echo "💾 数据存储在: /opt/$PROJECT_NAME/data/rabbitmq/"
    echo "📝 日志文件存储在: /opt/$PROJECT_NAME/logs/rabbitmq/"
    echo "🔧 配置文件位置: $DEPLOYMENT_DIR/"
    echo "📊 管理界面提供了完整的集群监控和管理功能"
}

# 健康检查
health_check() {
    log_info "执行健康检查..."

    local all_healthy=true

    # 检查RabbitMQ容器状态
    log_info "检查RabbitMQ容器状态..."
    local containers=("ioedream-rabbitmq-1" "ioedream-rabbitmq-2" "ioedream-rabbitmq-3")

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
        "http://localhost:$RABBITMQ_MANAGEMENT_PORT/api/healthchecks/node"
    )

    for url in "${urls[@]}"; do
        if curl -sf "$url" >/dev/null 2>&1; then
            log_success "✓ RabbitMQ 管理服务健康"
        else
            log_error "✗ RabbitMQ 管理服务异常"
            all_healthy=false
        fi
    done

    # 检查集群状态
    log_info "检查集群状态..."
    local cluster_status=$(docker exec ioedream-rabbitmq-1 rabbitmqctl cluster_status 2>/dev/null)
    if echo "$cluster_status" | grep -q "running_nodes"; then
        local running_nodes=$(echo "$cluster_status" | grep -c "running_nodes")
        if [ "$running_nodes" -eq 3 ]; then
            log_success "✓ 集群状态正常，所有3个节点都在运行"
        else
            log_warning "⚠ 集群中只有 $running_nodes 个节点在运行"
        fi
    else
        log_error "✗ 无法获取集群状态"
        all_healthy=false
    fi

    if [ "$all_healthy" = true ]; then
        log_success "🎉 所有健康检查通过"
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
    echo "  deploy     部署RabbitMQ集群"
    echo "  start      启动RabbitMQ服务"
    echo "  stop       停止RabbitMQ服务"
    echo "  restart    重启RabbitMQ服务"
    echo "  status     查看服务状态"
    echo "  logs       查看服务日志"
    echo "  health     执行健康检查"
    echo "  cluster    集群管理"
    echo "  clean      清理数据和服务"
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
    log_info "启动RabbitMQ服务..."
    cd "$DEPLOYMENT_DIR"
    docker-compose up -d
    show_access_info
}

# 停止服务
stop_services() {
    log_info "停止RabbitMQ服务..."
    cd "$DEPLOYMENT_DIR"
    docker-compose down
    log_success "服务已停止"
}

# 重启服务
restart_services() {
    log_info "重启RabbitMQ服务..."
    stop_services
    sleep 5
    start_services
}

# 查看服务状态
show_status() {
    log_info "RabbitMQ服务状态："
    cd "$DEPLOYMENT_DIR"
    docker-compose ps

    # 显示集群状态
    echo ""
    echo "集群状态："
    docker exec ioedream-rabbitmq-1 rabbitmqctl cluster_status 2>/dev/null || echo "无法获取集群状态"
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

# 集群管理
manage_cluster() {
    log_info "RabbitMQ集群管理:"
    echo ""
    echo "1. 查看集群状态:"
    docker exec ioedream-rabbitmq-1 rabbitmqctl cluster_status
    echo ""
    echo "2. 查看节点列表:"
    docker exec ioedream-rabbitmq-1 rabbitmqctl cluster_status | grep "running_nodes"
    echo ""
    echo "3. 查看策略列表:"
    docker exec ioedream-rabbitmq-1 rabbitmqctl list_policies
    echo ""
    echo "4. 查看队列列表:"
    docker exec ioedream-rabbitmq-1 rabbitmqctl list_queues
}

# 清理服务
clean_services() {
    log_warning "这将删除所有RabbitMQ数据和服务！"
    read -p "确定要继续吗？(y/N): " -n 1 -r
    echo

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cd "$DEPLOYMENT_DIR"
        docker-compose down -v
        docker system prune -f

        # 清理数据目录
        rm -rf /opt/$PROJECT_NAME/data/rabbitmq*
        rm -rf /opt/$PROJECT_NAME/logs/rabbitmq*

        log_success "清理完成"
    else
        log_info "取消清理操作"
    fi
}

# 主函数
main() {
    case "${1:-deploy}" in
        "deploy")
            require_env "RABBITMQ_ADMIN_PASSWORD"
            check_docker
            check_ports
            create_directories
            create_rabbitmq_config
            create_enabled_plugins
            create_env_file
            create_docker_compose
            create_nginx_config
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
        "cluster")
            manage_cluster
            ;;
        "clean")
            clean_services
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
