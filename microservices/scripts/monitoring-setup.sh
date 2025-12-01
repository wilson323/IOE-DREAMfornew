#!/bin/bash

# ===================================================================
# IOE-DREAM 微服务监控系统一键部署脚本
# 版本: v2.0.0
# 更新时间: 2025-11-29
# ===================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
MONITORING_DIR="$PROJECT_ROOT/monitoring"
COMPOSE_FILE="$MONITORING_DIR/docker-compose.yml"

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
    log_info "检查系统依赖..."

    # 检查Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi

    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi

    # 检查端口占用
    local ports=(9090 9091 9093 5601 9200 5044)
    for port in "${ports[@]}"; do
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
            log_warning "端口 $port 已被占用"
        fi
    done

    log_success "依赖检查完成"
}

# 创建目录结构
create_directories() {
    log_info "创建监控目录结构..."

    directories=(
        "$MONITORING_DIR/data/prometheus"
        "$MONITORING_DIR/data/grafana"
        "$MONITORING_DIR/data/alertmanager"
        "$MONITORING_DIR/data/elasticsearch"
        "$MONITORING_DIR/data/logstash"
        "$MONITORING_DIR/data/filebeat"
        "$MONITORING_DIR/data/kibana"
        "$MONITORING_DIR/logs"
    )

    for dir in "${directories[@]}"; do
        mkdir -p "$dir"
        chmod 755 "$dir"
    done

    log_success "目录结构创建完成"
}

# 生成配置文件
generate_configs() {
    log_info "生成监控配置文件..."

    # Prometheus配置
    cat > "$MONITORING_DIR/prometheus/prometheus.yml" << 'EOF'
# IOE-DREAM Prometheus配置
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'ioedream-production'
    environment: 'production'

rule_files:
  - "/etc/prometheus/rules/*.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']

  - job_name: 'cadvisor'
    static_configs:
      - targets: ['cadvisor:8080']

  - job_name: 'mysql-exporter'
    static_configs:
      - targets: ['mysql-exporter:9104']

  - job_name: 'redis-exporter'
    static_configs:
      - targets: ['redis-exporter:9121']

  # 微服务监控配置
  - job_name: 'ioedream-gateway'
    static_configs:
      - targets: ['host.docker.internal:8888']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s

  - job_name: 'ioedream-auth-service'
    static_configs:
      - targets: ['host.docker.internal:8881']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s

  - job_name: 'ioedream-identity-service'
    static_configs:
      - targets: ['host.docker.internal:8882']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s

  - job_name: 'ioedream-device-service'
    static_configs:
      - targets: ['host.docker.internal:8883']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
EOF

    # AlertManager配置
    cat > "$MONITORING_DIR/alertmanager/alertmanager.yml" << 'EOF'
global:
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'alert@ioedream.com'

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'

receivers:
  - name: 'web.hook'
    webhook_configs:
      - url: 'http://127.0.0.1:5001/'
EOF

    # Grafana配置
    cat > "$MONITORING_DIR/grafana/provisioning/datasources/prometheus.yml" << 'EOF'
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
EOF

    log_success "配置文件生成完成"
}

# 创建Docker Compose文件
create_docker_compose() {
    log_info "创建Docker Compose配置..."

    cat > "$COMPOSE_FILE" << 'EOF'
version: '3.8'

services:
  # Prometheus监控服务
  prometheus:
    image: prom/prometheus:v2.40.0
    container_name: ioedream-prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - ./prometheus/rules:/etc/prometheus/rules
      - ./data/prometheus:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=15d'
      - '--web.enable-lifecycle'
    networks:
      - monitoring

  # AlertManager告警管理
  alertmanager:
    image: prom/alertmanager:v0.24.0
    container_name: ioedream-alertmanager
    restart: unless-stopped
    ports:
      - "9093:9093"
    volumes:
      - ./alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml
      - ./data/alertmanager:/alertmanager
    command:
      - '--config.file=/etc/alertmanager/alertmanager.yml'
      - '--storage.path=/alertmanager'
    networks:
      - monitoring

  # Grafana可视化
  grafana:
    image: grafana/grafana:9.1.0
    container_name: ioedream-grafana
    restart: unless-stopped
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin123
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - ./grafana/provisioning:/etc/grafana/provisioning
      - ./data/grafana:/var/lib/grafana
    networks:
      - monitoring

  # Elasticsearch日志存储
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.4.0
    container_name: ioedream-elasticsearch
    restart: unless-stopped
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms1g -Xmx1g"
    volumes:
      - ./data/elasticsearch:/usr/share/elasticsearch/data
    ports:
      - "9200:9200"
    networks:
      - monitoring

  # Kibana日志可视化
  kibana:
    image: docker.elastic.co/kibana/kibana:8.4.0
    container_name: ioedream-kibana
    restart: unless-stopped
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch
    networks:
      - monitoring

  # Logstash日志处理
  logstash:
    image: docker.elastic.co/logstash/logstash:8.4.0
    container_name: ioedream-logstash
    restart: unless-stopped
    volumes:
      - ./logstash/config:/usr/share/logstash/config
      - ./logstash/pipeline:/usr/share/logstash/pipeline
      - ./data/logstash:/usr/share/logstash/data
    ports:
      - "5044:5044"
    depends_on:
      - elasticsearch
    networks:
      - monitoring

  # Node Exporter系统指标
  node-exporter:
    image: prom/node-exporter:v1.3.1
    container_name: ioedream-node-exporter
    restart: unless-stopped
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
    networks:
      - monitoring

  # cAdvisor容器监控
  cadvisor:
    image: gcr.io/cadvisor/cadvisor:v0.46.0
    container_name: ioedream-cadvisor
    restart: unless-stopped
    ports:
      - "8080:8080"
    volumes:
      - /:/rootfs:ro
      - /var/run:/var/run:rw
      - /sys:/sys:ro
      - /var/lib/docker/:/var/lib/docker:ro
      - /dev/disk/:/dev/disk:ro
    privileged: true
    devices:
      - /dev/kmsg
    networks:
      - monitoring

networks:
  monitoring:
    driver: bridge

volumes:
  prometheus_data:
  grafana_data:
  elasticsearch_data:
  alertmanager_data:
EOF

    log_success "Docker Compose配置创建完成"
}

# 启动服务
start_services() {
    log_info "启动监控服务..."

    cd "$MONITORING_DIR"

    # 拉取镜像
    log_info "拉取Docker镜像..."
    docker-compose pull

    # 启动服务
    log_info "启动服务容器..."
    docker-compose up -d

    # 等待服务启动
    log_info "等待服务启动..."
    sleep 30

    # 检查服务状态
    log_info "检查服务状态..."
    docker-compose ps

    log_success "监控服务启动完成"
}

# 验证部署
verify_deployment() {
    log_info "验证监控部署..."

    # 检查Prometheus
    if curl -s http://localhost:9090/api/v1/status/config > /dev/null; then
        log_success "✓ Prometheus运行正常 (http://localhost:9090)"
    else
        log_error "✗ Prometheus访问异常"
    fi

    # 检查Grafana
    if curl -s http://localhost:3000/api/health > /dev/null; then
        log_success "✓ Grafana运行正常 (http://localhost:3000, 用户名/密码: admin/admin123)"
    else
        log_error "✗ Grafana访问异常"
    fi

    # 检查Elasticsearch
    if curl -s http://localhost:9200/_cluster/health > /dev/null; then
        log_success "✓ Elasticsearch运行正常 (http://localhost:9200)"
    else
        log_error "✗ Elasticsearch访问异常"
    fi

    # 检查Kibana
    if curl -s http://localhost:5601/api/status > /dev/null; then
        log_success "✓ Kibana运行正常 (http://localhost:5601)"
    else
        log_error "✗ Kibana访问异常"
    fi

    log_success "部署验证完成"
}

# 显示访问信息
show_access_info() {
    log_info "监控服务访问信息:"
    echo ""
    echo "🔧 Prometheus:      http://localhost:9090"
    echo "📊 Grafana:         http://localhost:3000 (admin/admin123)"
    echo "📋 AlertManager:   http://localhost:9093"
    echo "🔍 Elasticsearch:  http://localhost:9200"
    echo "📈 Kibana:          http://localhost:5601"
    echo "📁 cAdvisor:        http://localhost:8080"
    echo "🖥️ Node Exporter:   http://localhost:9100/metrics"
    echo ""
    log_info "管理命令:"
    echo "  启动服务: cd $MONITORING_DIR && docker-compose up -d"
    echo "  停止服务: cd $MONITORING_DIR && docker-compose down"
    echo "  查看日志: cd $MONITORING_DIR && docker-compose logs -f [service_name]"
    echo "  重启服务: cd $MONITORING_DIR && docker-compose restart [service_name]"
}

# 主函数
main() {
    log_info "开始部署IOE-DREAM监控系统..."

    check_dependencies
    create_directories
    generate_configs
    create_docker_compose
    start_services
    verify_deployment
    show_access_info

    log_success "IOE-DREAM监控系统部署完成！"
}

# 执行主函数
main "$@"