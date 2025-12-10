#!/bin/bash

# ============================================================
# IOE-DREAM Redis集群部署脚本
#
# @Author:    IOE-DREAM Team
# @Date:      2025-12-09
# @Description: 针对单企业1000台设备20000人规模的Redis集群部署
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
DEPLOYMENT_DIR="$(pwd)/deployments/redis-cluster"
DOCKER_COMPOSE_FILE="$DEPLOYMENT_DIR/docker-compose.yml"

# 规模配置（1000台设备，20000人）
REDIS_CLUSTER_SIZE=6           # Redis集群节点数（3主3从）
REDIS_MEMORY_LIMIT="2g"        # 每个Redis实例内存限制
REDIS_MAX_MEMORY="1.5g"        # Redis最大内存
REDIS_MAX_CLIENTS=20000         # 最大客户端连接数
REDIS_TCP_KEEPALIVE=300        # TCP keepalive

# 服务端口配置
REDIS_BASE_PORT=6379            # Redis基础端口
REDIS_CLUSTER_BUS_PORT=16379    # 集群总线端口
REDIS_EXPORTER_PORT=9121       # Redis Exporter端口
NGINX_REDIS_PORT=6380          # Nginx负载均衡端口

# 健康检查配置
HEALTH_CHECK_INTERVAL=30
HEALTH_CHECK_TIMEOUT=10
HEALTH_CHECK_RETRIES=3
HEALTH_CHECK_START_PERIOD=60

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

# 检查系统资源
check_system_resources() {
    log_info "检查系统资源..."

    # 检查内存
    local total_memory=$(free -m | awk 'NR==2{printf "%.0f", $2/1024}')
    if [ "$total_memory" -lt 8 ]; then
        log_warning "系统内存较小（${total_memory}GB），建议至少8GB内存"
    fi

    # 检查可用磁盘空间
    local available_disk=$(df -h / | awk 'NR==2{print $4}' | sed 's/G//')
    if [ "${available_disk%G}" -lt 20 ]; then
        log_warning "磁盘空间不足（${available_disk}），建议至少20GB可用空间"
    fi

    log_success "系统资源检查完成"
}

# 检查端口占用
check_ports() {
    log_info "检查端口占用情况..."

    for i in $(seq 0 $((REDIS_CLUSTER_SIZE-1))); do
        local redis_port=$((REDIS_BASE_PORT + i))
        local bus_port=$((REDIS_CLUSTER_BUS_PORT + i))

        # 检查Redis端口
        if lsof -Pi :$redis_port -sTCP:LISTEN -t >/dev/null 2>&1; then
            log_warning "端口 $redis_port 已被占用，尝试释放..."
            local pid=$(lsof -ti:$redis_port)
            if [ ! -z "$pid" ]; then
                kill -9 $pid 2>/dev/null || true
                sleep 2
            fi
        fi

        # 检查集群总线端口
        if lsof -Pi :$bus_port -sTCP:LISTEN -t >/dev/null 2>&1; then
            log_warning "端口 $bus_port 已被占用，尝试释放..."
            local pid=$(lsof -ti:$bus_port)
            if [ ! -z "$pid" ]; then
                kill -9 $pid 2>/dev/null || true
                sleep 2
            fi
        fi
    done

    # 检查Exporter端口
    if lsof -Pi :$REDIS_EXPORTER_PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_warning "端口 $REDIS_EXPORTER_PORT 已被占用，尝试释放..."
        local pid=$(lsof -ti:$REDIS_EXPORTER_PORT)
        if [ ! -z "$pid" ]; then
            kill -9 $pid 2>/dev/null || true
            sleep 2
        fi
    fi

    # 检查负载均衡端口
    if lsof -Pi :$NGINX_REDIS_PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_warning "端口 $NGINX_REDIS_PORT 已被占用，尝试释放..."
        local pid=$(lsof -ti:$NGINX_REDIS_PORT)
        if [ ! -z "$pid" ]; then
            kill -9 $pid 2>/dev/null || true
            sleep 2
        fi
    fi

    log_success "端口检查完成"
}

# 创建必要目录
create_directories() {
    log_info "创建必要的目录结构..."

    local directories=(
        "$DEPLOYMENT_DIR"
        "/opt/$PROJECT_NAME/data/redis-cluster"
        "/opt/$PROJECT_NAME/logs/redis-cluster"
        "/opt/$PROJECT_NAME/config/redis-cluster"
    )

    for dir in "${directories[@]}"; do
        if [ ! -d "$dir" ]; then
            mkdir -p "$dir"
            log_info "创建目录: $dir"
        fi
    done

    # 设置目录权限
    chmod 755 "/opt/$PROJECT_NAME/data/redis-cluster"
    chmod 755 "/opt/$PROJECT_NAME/logs/redis-cluster"
    chmod 755 "/opt/$PROJECT_NAME/config/redis-cluster"

    log_success "目录结构创建完成"
}

# 创建Redis配置文件
create_redis_config() {
    log_info "创建Redis配置文件..."

    for i in $(seq 0 $((REDIS_CLUSTER_SIZE-1))); do
        local port=$((REDIS_BASE_PORT + i))
        local bus_port=$((REDIS_CLUSTER_BUS_PORT + i))
        local config_file="$DEPLOYMENT_DIR/redis-$port.conf"

        cat > "$config_file" << EOF
# ============================================================
# IOE-DREAM Redis集群配置
# 节点: $i, 端口: $port
# 规模: 1000台设备, 20000人
# ============================================================

# 网络配置
port $port
bind 0.0.0.0
protected-mode no
tcp-backlog 511
timeout 0
tcp-keepalive $REDIS_TCP_KEEPALIVE

# 通用配置
daemonize no
supervised no
pidfile /var/run/redis_$port.pid
loglevel notice
logfile ""

# 数据库配置
databases 16
always-show-logo yes

# 持久化配置 - 针对20000人规模优化
save 900 1
save 300 10
save 60 10000
stop-writes-on-bgsave-error yes
rdbcompression yes
rdbchecksum yes
dbfilename dump-$port.rdb
rdb-del-sync-files no
dir /data

# AOF配置 - 针对高并发优化
appendonly yes
appendfilename "appendonly-$port.aof"
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
aof-load-truncated yes
aof-use-rdb-preamble yes

# 内存配置 - 针对20000人规模
maxmemory $REDIS_MAX_MEMORY
maxmemory-policy allkeys-lru
maxmemory-samples 5
replica-ignore-maxmemory yes

# 慢查询日志 - 针对性能监控
slowlog-log-slower-than 10000
slowlog-max-len 128

# 客户端配置
maxclients $REDIS_MAX_CLIENTS
tcp-timeout 300

# 集群配置
cluster-enabled yes
cluster-config-file nodes-$port.conf
cluster-node-timeout 15000
cluster-announce-ip
cluster-announce-port $port
cluster-announce-bus-port $bus_port
cluster-migration-barrier 1
cluster-require-full-coverage yes
cluster-replica-no-failover no
cluster-replica-validity-factor 10
cluster-move-target-ratio 1.0

# 内存优化
hash-max-ziplist-entries 512
hash-max-ziplist-value 64
list-max-ziplist-size -2
list-compress-depth 0
set-max-intset-entries 512
zset-max-ziplist-entries 128
zset-max-ziplist-value 64
hll-sparse-max-bytes 3000
stream-node-max-bytes 4096
stream-node-max-entries 100

# 安全配置
requirepass ioedream_redis_2023

# 客户端输出缓冲区限制 - 针对20000人优化
client-output-buffer-limit normal 0 0 0
client-output-buffer-limit replica 256mb 64mb 60
client-output-buffer-limit pubsub 32mb 8mb 60

# 命令频率限制
client-query-buffer-limit 1gb
proto-max-bulk-len 512mb

# 监控配置
latency-monitor-threshold 100

# 内存使用优化
lazyfree-lazy-eviction no
lazyfree-lazy-expire no
lazyfree-lazy-server-del no
replica-lazy-flush no
lazyfree-lazy-user-del no

# 模块加载
# loadmodule /path/to/your_module.so

# 活跃重新哈希
activerehashing yes

# 其他优化
notify-keyspace-events "Ex"
hash-max-ziplist-entries 512
EOF

        log_info "创建配置文件: redis-$port.conf"
    done

    log_success "Redis配置文件创建完成"
}

# 创建Docker Compose文件
create_docker_compose() {
    log_info "创建Docker Compose配置..."

    cat > "$DEPLOYMENT_DIR/docker-compose.yml" << EOF
version: '3.8'

services:
  # Redis集群节点
EOF

    # 生成Redis节点服务
    for i in $(seq 0 $((REDIS_CLUSTER_SIZE-1))); do
        local port=$((REDIS_BASE_PORT + i))
        local bus_port=$((REDIS_CLUSTER_BUS_PORT + i))
        local service_name="redis-node-$i"

        cat >> "$DEPLOYMENT_DIR/docker-compose.yml" << EOF
  $service_name:
    image: redis:7.2-alpine
    container_name: ioedream-redis-$i
    hostname: ioedream-redis-$i
    ports:
      - "$port:6379"
      - "$bus_port:16379"
    volumes:
      - ./redis-$port.conf:/usr/local/etc/redis/redis.conf:ro
      - ioedream-redis-data-$i:/data
      - ioedream-redis-logs-$i:/var/log/redis
    command: redis-server /usr/local/etc/redis/redis.conf
    environment:
      - TZ=Asia/Shanghai
      - REDIS_PASSWORD=ioedream_redis_2023
    restart: unless-stopped
    networks:
      - redis-cluster-network
    healthcheck:
      test: ["CMD", "redis-cli", "-p", "6379", "ping"]
      interval: \${HEALTH_CHECK_INTERVAL:-30}s
      timeout: \${HEALTH_CHECK_TIMEOUT:-10}s
      retries: \${HEALTH_CHECK_RETRIES:-3}
      start_period: \${HEALTH_CHECK_START_PERIOD:-60}s
    labels:
      - "ioe-dream.service=redis"
      - "ioe-dream.node-type=cluster"
      - "ioe-dream.cluster-node=$i"

EOF

    done

    # 添加Redis Exporter服务
    cat >> "$DEPLOYMENT_DIR/docker-compose.yml" << EOF
  # Redis Exporter监控
  redis-exporter:
    image: oliver006/redis_exporter:v1.46.0
    container_name: ioedream-redis-exporter
    ports:
      - "$REDIS_EXPORTER_PORT:9121"
    environment:
      - REDIS_ADDR=redis://redis-node-0:6379,redis://redis-node-1:6379,redis://redis-node-2:6379,redis://redis-node-3:6379,redis://redis-node-4:6379,redis://redis-node-5:6379
      - REDIS_PASSWORD=ioedream_redis_2023
      - REDIS_EXPORTER_LOG_FORMAT=txt
    restart: unless-stopped
    networks:
      - redis-cluster-network
    depends_on:
      - redis-node-0
      - redis-node-1
      - redis-node-2
    labels:
      - "ioe-dream.service=redis-exporter"

  # Nginx负载均衡器
  nginx-redis-lb:
    image: nginx:1.24-alpine
    container_name: ioedream-redis-lb
    ports:
      - "$NGINX_REDIS_PORT:6379"
    volumes:
      - ./nginx-redis.conf:/etc/nginx/nginx.conf:ro
    restart: unless-stopped
    networks:
      - redis-cluster-network
    depends_on:
      - redis-node-0
      - redis-node-1
      - redis-node-2
    labels:
      - "ioe-dream.service=nginx"

networks:
  redis-cluster-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.23.0.0/16
          gateway: 172.23.0.1

volumes:
EOF

    # 生成数据卷配置
    for i in $(seq 0 $((REDIS_CLUSTER_SIZE-1))); do
        cat >> "$DEPLOYMENT_DIR/docker-compose.yml" << EOF
  ioedream-redis-data-$i:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /opt/$PROJECT_NAME/data/redis-cluster/node-$i
  ioedream-redis-logs-$i:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /opt/$PROJECT_NAME/logs/redis-cluster/node-$i

EOF
    done

    log_success "Docker Compose配置创建完成"
}

# 创建Nginx负载均衡配置
create_nginx_config() {
    log_info "创建Nginx负载均衡配置..."

    cat > "$DEPLOYMENT_DIR/nginx-redis.conf" << 'EOF'
events {
    worker_connections 1024;
}

stream {
    upstream redis_cluster {
        # Redis节点负载均衡
        server redis-node-0:6379 max_fails=3 fail_timeout=30s;
        server redis-node-1:6379 max_fails=3 fail_timeout=30s;
        server redis-node-2:6379 max_fails=3 fail_timeout=30s;
        server redis-node-3:6379 max_fails=3 fail_timeout=30s;
        server redis-node-4:6379 max_fails=3 fail_timeout=30s;
        server redis-node-5:6379 max_fails=3 fail_timeout=30s;
    }

    server {
        listen 6379;
        proxy_pass redis_cluster;
        proxy_timeout 1s;
        proxy_responses 1;
        proxy_bind $remote_addr transparent;
    }
}

http {
    server {
        listen 80;
        server_name localhost;

        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
    }
}
EOF

    log_success "Nginx负载均衡配置创建完成"
}

# 创建环境变量文件
create_env_file() {
    log_info "创建环境变量配置文件..."

    cat > "$DEPLOYMENT_DIR/.env" << EOF
# ============================================================
# IOE-DREAM Redis集群环境变量配置
# 规模: 1000台设备, 20000人
# ============================================================

# 项目标识
COMPOSE_PROJECT_NAME=ioedream-redis-cluster

# Redis版本
REDIS_VERSION=7.2-alpine

# 集群配置
REDIS_CLUSTER_SIZE=6
REDIS_PASSWORD=ioedream_redis_2023

# 端口配置
REDIS_BASE_PORT=6379
REDIS_CLUSTER_BUS_PORT=16379
REDIS_EXPORTER_PORT=9121
NGINX_REDIS_PORT=6380

# 内存配置 - 针对20000人优化
REDIS_MEMORY_LIMIT=2g
REDIS_MAX_MEMORY=1.5g
REDIS_MAX_CLIENTS=20000

# 健康检查配置
HEALTH_CHECK_INTERVAL=30
HEALTH_CHECK_TIMEOUT=10
HEALTH_CHECK_RETRIES=3
HEALTH_CHECK_START_PERIOD=60

# 网络配置
SUBNET=172.23.0.0/16
GATEWAY=172.23.0.1

# 数据存储路径
REDIS_DATA_PATH=/opt/$PROJECT_NAME/data/redis-cluster
REDIS_LOGS_PATH=/opt/$PROJECT_NAME/logs/redis-cluster
REDIS_CONFIG_PATH=/opt/$PROJECT_NAME/config/redis-cluster

# 集群节点
REDIS_NODES="redis-node-0:6379,redis-node-1:6379,redis-node-2:6379,redis-node-3:6379,redis-node-4:6379,redis-node-5:6379"

# 主节点配置
REDIS_MASTER_NODES="redis-node-0,redis-node-1,redis-node-2"

# 从节点配置
REDIS_SLAVE_NODES="redis-node-3,redis-node-4,redis-node-5"

# 监控配置
PROMETHEUS_ENABLED=true
REDIS_EXPORTER_ENABLED=true

# 性能配置
TCP_KEEPALIVE=300
MAXMEMORY_POLICY=allkeys-lru

# 持久化配置
SAVE_INTERVALS="900 1 300 10 60 10000"
AOF_FSYNC_EVERYSEC=true

# 安全配置
SSL_ENABLED=false
PROTECTED_MODE=false

# 开发环境配置
---
spring:
  config:
    activate:
      profile: dev

REDIS_PASSWORD=dev_redis_2023
REDIS_MAX_MEMORY=512m
REDIS_MAX_CLIENTS=5000

# 生产环境配置
---
spring:
  config:
    activate:
      profile: prod

REDIS_PASSWORD=${REDIS_ADMIN_PASSWORD}
REDIS_MAX_MEMORY=4g
REDIS_MAX_CLIENTS=50000
AOF_FSYNC_EVERYSEC=true
SSL_ENABLED=true
EOF

    log_success "环境变量配置文件创建完成"
}

# 创建集群初始化脚本
create_cluster_init_script() {
    log_info "创建集群初始化脚本..."

    cat > "$DEPLOYMENT_DIR/init-cluster.sh" << 'EOF'
#!/bin/bash

# Redis集群初始化脚本

set -e

log_info() {
    echo -e "\033[0;34m[INFO]\033[0m $1"
}

log_success() {
    echo -e "\033[0;32m[SUCCESS]\033[0m $1"
}

log_error() {
    echo -e "\033[0;31m[ERROR]\033[0m $1"
}

# 等待Redis节点启动
wait_for_redis() {
    log_info "等待Redis节点启动..."

    for i in {0..5}; do
        port=$((6379 + i))
        log_info "等待节点 $i (端口 $port) 启动..."

        local retries=0
        local max_retries=30

        while [ $retries -lt $max_retries ]; do
            if docker exec ioedream-redis-$i redis-cli -p 6379 ping >/dev/null 2>&1; then
                log_success "节点 $i 启动成功"
                break
            fi

            echo -n "."
            sleep 2
            ((retries++))
        done

        if [ $retries -eq $max_retries ]; then
            log_error "节点 $i 启动超时"
            exit 1
        fi
    done

    log_success "所有Redis节点启动完成"
}

# 初始化集群
init_cluster() {
    log_info "初始化Redis集群..."

    # 创建集群
    local cluster_nodes=""
    for i in {0..5}; do
        port=$((6379 + i))
        cluster_nodes="$cluster_nodes ioedream-redis-$i:$port"
    done

    log_info "集群节点: $cluster_nodes"

    # 执行集群初始化
    docker exec ioedream-redis-0 redis-cli --cluster create \
        --cluster-replicas 1 \
        --cluster-yes \
        $cluster_nodes

    log_success "Redis集群初始化完成"
}

# 检查集群状态
check_cluster() {
    log_info "检查集群状态..."

    docker exec ioedream-redis-0 redis-cli --cluster info

    echo ""
    log_info "集群节点信息:"
    docker exec ioedream-redis-0 redis-cli cluster nodes

    echo ""
    log_info "集群健康状态:"
    for i in {0..5}; do
        echo "节点 $i:"
        docker exec ioedream-redis-$i redis-cli --cluster check
        echo ""
    done
}

# 主函数
main() {
    wait_for_redis
    sleep 10  # 额外等待时间确保所有节点完全启动
    init_cluster
    check_cluster
    log_success "Redis集群初始化完成！"
}

main "$@"
EOF

    chmod +x "$DEPLOYMENT_DIR/init-cluster.sh"
    log_success "集群初始化脚本创建完成"
}

# 部署服务
deploy_services() {
    log_info "部署Redis集群服务..."

    cd "$DEPLOYMENT_DIR"

    # 停止现有服务（如果存在）
    log_info "停止现有服务..."
    docker-compose down -v || true

    # 创建数据目录
    for i in $(seq 0 $((REDIS_CLUSTER_SIZE-1))); do
        mkdir -p "/opt/$PROJECT_NAME/data/redis-cluster/node-$i"
        mkdir -p "/opt/$PROJECT_NAME/logs/redis-cluster/node-$i"
    done

    # 构建并启动服务
    log_info "启动Redis集群服务..."
    docker-compose up -d --build

    # 等待服务启动
    log_info "等待服务启动..."
    sleep 60

    # 初始化集群
    log_info "初始化Redis集群..."
    ./init-cluster.sh

    log_success "Redis集群服务部署完成"
}

# 等待服务就绪
wait_for_services() {
    log_info "等待服务就绪..."

    local retries=0
    local max_retries=120

    while [ $retries -lt $max_retries ]; do
        # 检查集群是否正常工作
        if docker exec ioedream-redis-0 redis-cli --cluster info 2>/dev/null | grep -q "cluster_state:ok"; then
            log_success "Redis集群服务已就绪"
            break
        fi

        echo -n "."
        sleep 5
        ((retries++))
    done

    if [ $retries -eq $max_retries ]; then
        log_error "Redis集群服务启动超时"
        return 1
    fi
}

# 显示访问信息
show_access_info() {
    log_success "🎉 Redis集群部署成功！"
    echo ""
    echo "============================================================"
    echo "📊 服务访问地址："
    echo "============================================================"
    echo "🔗 Redis集群地址:     redis://127.0.0.1:$NGINX_REDIS_PORT"
    echo "📊 Redis Exporter:    http://localhost:$REDIS_EXPORTER_PORT/metrics"
    echo ""
    echo "============================================================"
    echo "🏗️ 集群节点信息："
    echo "============================================================"
    for i in $(seq 0 $((REDIS_CLUSTER_SIZE-1))); do
        local port=$((REDIS_BASE_PORT + i))
        echo "🔸 节点$i:           127.0.0.1:$port"
    done
    echo ""
    echo "============================================================"
    echo "🔐 连接配置："
    echo "============================================================"
    echo "👤 密码:             ioedream_redis_2023"
    echo "🔑 密码(开发环境):     dev_redis_2023"
    echo "🏠 负载均衡地址:      127.0.0.1:$NGINX_REDIS_PORT"
    echo ""
    echo "============================================================"
    echo "📈 规模配置（已优化）："
    echo "============================================================"
    echo "👥 支持用户数:        20,000人"
    echo "🖥️  支持设备数:        1,000台"
    echo "💾 每节点内存:        $REDIS_MEMORY_LIMIT"
    echo "🔗 最大客户端连接:    $REDIS_MAX_CLIENTS"
    echo "🎯 集群节点数:        $REDIS_CLUSTER_SIZE"
    echo ""
    echo "============================================================"
    echo "🔧 管理命令："
    echo "============================================================"
    echo "📦 查看服务状态:       docker-compose ps"
    echo "📦 查看服务日志:       docker-compose logs [node-name]"
    echo "🛑 停止所有服务:       docker-compose down"
    echo "🚀 重启所有服务:       docker-compose restart"
    echo ""
    echo "🔧 集群管理命令："
    echo "📋 集群信息:           docker exec ioedream-redis-0 redis-cli --cluster info"
    echo "📋 集群节点:           docker exec ioedream-redis-0 redis-cli cluster nodes"
    echo "🔍 集群检查:           docker exec ioedream-redis-0 redis-cli --cluster check"
    echo ""
    echo "============================================================"
    echo "📊 应用连接配置："
    echo "============================================================"
    echo "spring.redis.host=localhost"
    echo "spring.redis.port=$NGINX_REDIS_PORT"
    echo "spring.redis.password=ioedream_redis_2023"
    echo "spring.redis.cluster.nodes=127.0.0.1:6379,127.0.0.1:6380,127.0.0.1:6381"
    echo ""
    echo "============================================================"
    echo "⚠️  注意事项："
    echo "============================================================"
    echo "🔒 确保防火墙已开放对应端口"
    echo "💾 数据存储在: /opt/$PROJECT_NAME/data/redis-cluster/"
    echo "📝 日志文件存储在: /opt/$PROJECT_NAME/logs/redis-cluster/"
    echo "🔧 配置文件位置: $DEPLOYMENT_DIR/"
    echo "📊 Redis Exporter提供Prometheus监控指标"
    echo "⚡ Nginx负载均衡器提供故障转移和负载分散"
}

# 健康检查
health_check() {
    log_info "执行健康检查..."

    local all_healthy=true

    # 检查Redis容器状态
    log_info "检查Redis容器状态..."
    for i in $(seq 0 $((REDIS_CLUSTER_SIZE-1))); do
        if docker ps --filter "name=ioedream-redis-$i" --format "table {{.Status}}" | grep -q "Up"; then
            log_success "✓ 容器 ioedream-redis-$i 运行正常"
        else
            log_error "✗ 容器 ioedream-redis-$i 运行异常"
            all_healthy=false
        fi
    done

    # 检查集群健康状态
    log_info "检查集群健康状态..."
    if docker exec ioedream-redis-0 redis-cli --cluster info 2>/dev/null | grep -q "cluster_state:ok"; then
        log_success "✓ Redis集群状态正常"
    else
        log_error "✗ Redis集群状态异常"
        all_healthy=false
    fi

    # 检查节点连接
    log_info "检查节点连接..."
    for i in $(seq 0 $((REDIS_CLUSTER_SIZE-1))); do
        if docker exec ioedream-redis-$i redis-cli ping 2>/dev/null | grep -q "PONG"; then
            log_success "✓ 节点 $i 连接正常"
        else
            log_error "✗ 节点 $i 连接异常"
            all_healthy=false
        fi
    done

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
    echo "  deploy     部署Redis集群"
    echo "  start      启动Redis服务"
    echo "  stop       停止Redis服务"
    echo "  restart    重启Redis服务"
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
    log_info "启动Redis服务..."
    cd "$DEPLOYMENT_DIR"
    docker-compose up -d
    show_access_info
}

# 停止服务
stop_services() {
    log_info "停止Redis服务..."
    cd "$DEPLOYMENT_DIR"
    docker-compose down
    log_success "服务已停止"
}

# 重启服务
restart_services() {
    log_info "重启Redis服务..."
    stop_services
    sleep 5
    start_services
}

# 查看服务状态
show_status() {
    log_info "Redis服务状态："
    cd "$DEPLOYMENT_DIR"
    docker-compose ps

    # 显示集群状态
    echo ""
    echo "集群状态："
    docker exec ioedream-redis-0 redis-cli --cluster info 2>/dev/null || echo "无法获取集群状态"
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
    log_info "Redis集群管理:"
    echo ""
    echo "1. 查看集群信息:"
    docker exec ioedream-redis-0 redis-cli --cluster info
    echo ""
    echo "2. 查看集群节点:"
    docker exec ioedream-redis-0 redis-cli cluster nodes
    echo ""
    echo "3. 集群健康检查:"
    docker exec ioedream-redis-0 redis-cli --cluster check
    echo ""
    echo "4. 集群修复:"
    docker exec ioedream-redis-0 redis-cli --cluster fix
}

# 清理服务
clean_services() {
    log_warning "这将删除所有Redis数据和服务！"
    read -p "确定要继续吗？(y/N): " -n 1 -r
    echo

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cd "$DEPLOYMENT_DIR"
        docker-compose down -v
        docker system prune -f

        # 清理数据目录
        rm -rf /opt/$PROJECT_NAME/data/redis-cluster*
        rm -rf /opt/$PROJECT_NAME/logs/redis-cluster*

        log_success "清理完成"
    else
        log_info "取消清理操作"
    fi
}

# 主函数
main() {
    case "${1:-deploy}" in
        "deploy")
            check_docker
            check_system_resources
            check_ports
            create_directories
            create_redis_config
            create_docker_compose
            create_nginx_config
            create_env_file
            create_cluster_init_script
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