#!/bin/bash

# ============================================================
# IOE-DREAM Resilience4j 监控部署脚本
#
# @Author:    IOE-DREAM Team
# @Date:      2025-12-09
# @Description: 部署Resilience4j监控组件，包含Grafana仪表盘和告警规则
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
DEPLOYMENT_DIR="$(pwd)/deployments/resilience4j"
GRAFANA_PROVISIONING_DIR="$DEPLOYMENT_DIR/grafana/provisioning"
PROMETHEUS_CONFIG_DIR="$DEPLOYMENT_DIR/prometheus"

# 服务端口配置
GRAFANA_PORT=3001
PROMETHEUS_PORT=9091
ALERTMANAGER_PORT=9093

# 检查Docker环境
check_docker() {
    log_info "检查Docker环境..."

    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装或未配置到PATH"
        exit 1
    fi

    if ! docker info &> /dev/null; then
        log_error "Docker服务未运行，请启动Docker服务"
        exit 1
    fi

    log_success "Docker环境检查通过"
}

# 创建必要目录
create_directories() {
    log_info "创建必要的目录结构..."

    local directories=(
        "$DEPLOYMENT_DIR"
        "$GRAFANA_PROVISIONING_DIR/datasources"
        "$GRAFANA_PROVISIONING_DIR/dashboards"
        "$PROMETHEUS_CONFIG_DIR"
        "$DEPLOYMENT_DIR/alertmanager"
    )

    for dir in "${directories[@]}"; do
        if [ ! -d "$dir" ]; then
            mkdir -p "$dir"
            log_info "创建目录: $dir"
        fi
    done

    log_success "目录结构创建完成"
}

# 创建Grafana数据源配置
create_grafana_datasources() {
    log_info "创建Grafana数据源配置..."

    cat > "$GRAFANA_PROVISIONING_DIR/datasources/prometheus.yml" << 'EOF'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
    jsonData:
      timeInterval: "5s"
      queryTimeout: "60s"
      httpMethod: "POST"
    secureJsonData: {}

  - name: Prometheus-Resilience4j
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    database: "resilience4j"
    jsonData:
      timeInterval: "5s"
    secureJsonData: {}
EOF

    log_success "Grafana数据源配置创建完成"
}

# 创建Grafana仪表盘配置
create_grafana_dashboards() {
    log_info "创建Grafana仪表盘配置..."

    cat > "$GRAFANA_PROVISIONING_DIR/dashboards/dashboard.yml" << 'EOF'
apiVersion: 1

providers:
  - name: 'resilience4j-dashboards'
    orgId: 1
    folder: 'Resilience4j'
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /var/lib/grafana/dashboards/resilience4j
EOF

    log_success "Grafana仪表盘配置创建完成"
}

# 创建Grafana仪表盘JSON
create_grafana_dashboard_json() {
    log_info "创建Resilience4j仪表盘JSON配置..."

    mkdir -p "$GRAFANA_PROVISIONING_DIR/dashboards/resilience4j"

    # 熔断器监控仪表盘
    cat > "$GRAFANA_PROVISIONING_DIR/dashboards/resilience4j/circuitbreaker-dashboard.json" << 'EOF'
{
  "dashboard": {
    "id": null,
    "title": "IOE-DREAM - Resilience4j 熔断器监控",
    "tags": ["resilience4j", "circuitbreaker", "ioedream"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "熔断器状态概览",
        "type": "stat",
        "targets": [
          {
            "expr": "sum(resilience4j_circuitbreaker_state{state=\"CLOSED\"})",
            "legendFormat": "关闭"
          },
          {
            "expr": "sum(resilience4j_circuitbreaker_state{state=\"OPEN\"})",
            "legendFormat": "打开"
          },
          {
            "expr": "sum(resilience4j_circuitbreaker_state{state=\"HALF_OPEN\"})",
            "legendFormat": "半开"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "color": {
              "mode": "palette-classic"
            },
            "unit": "short"
          }
        }
      },
      {
        "id": 2,
        "title": "熔断器调用成功率",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(resilience4j_circuitbreaker_calls_total{kind=\"successful\"}[5m]) / rate(resilience4j_circuitbreaker_calls_total[5m]) * 100",
            "legendFormat": "{{name}} 成功率"
          }
        ],
        "yAxes": [
          {
            "unit": "percent",
            "min": 0,
            "max": 100
          }
        ]
      },
      {
        "id": 3,
        "title": "熔断器失败率",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(resilience4j_circuitbreaker_calls_total{kind=\"failed\"}[5m]) / rate(resilience4j_circuitbreaker_calls_total[5m]) * 100",
            "legendFormat": "{{name}} 失败率"
          }
        ],
        "yAxes": [
          {
            "unit": "percent",
            "min": 0,
            "max": 100
          }
        ]
      },
      {
        "id": 4,
        "title": "熔断器调用次数",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(resilience4j_circuitbreaker_calls_total[5m])",
            "legendFormat": "{{name}}-{{kind}}"
          }
        ]
      }
    ],
    "time": {
      "from": "now-1h",
      "to": "now"
    },
    "refresh": "5s"
  }
}
EOF

    # 限流器监控仪表盘
    cat > "$GRAFANA_PROVISIONING_DIR/dashboards/resilience4j/ratelimiter-dashboard.json" << 'EOF'
{
  "dashboard": {
    "id": null,
    "title": "IOE-DREAM - Resilience4j 限流器监控",
    "tags": ["resilience4j", "ratelimiter", "ioedream"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "可用许可数量",
        "type": "graph",
        "targets": [
          {
            "expr": "resilience4j_ratelimiter_available_permissions",
            "legendFormat": "{{name}}"
          }
        ]
      },
      {
        "id": 2,
        "title": "限流拒绝次数",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(resilience4j_ratelimiter_calls_total{kind=\"failed\"}[5m])",
            "legendFormat": "{{name}} 拒绝率"
          }
        ]
      },
      {
        "id": 3,
        "title": "限流通过次数",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(resilience4j_ratelimiter_calls_total{kind=\"successful\"}[5m])",
            "legendFormat": "{{name}} 通过率"
          }
        ]
      },
      {
        "id": 4,
        "title": "等待时间统计",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(resilience4j_ratelimiter_waiting_duration_seconds_bucket[5m]))",
            "legendFormat": "{{name}} P95等待时间"
          },
          {
            "expr": "histogram_quantile(0.50, rate(resilience4j_ratelimiter_waiting_duration_seconds_bucket[5m]))",
            "legendFormat": "{{name}} P50等待时间"
          }
        ]
      }
    ],
    "time": {
      "from": "now-1h",
      "to": "now"
    },
    "refresh": "5s"
  }
}
EOF

    # 重试监控仪表盘
    cat > "$GRAFANA_PROVISIONING_DIR/dashboards/resilience4j/retry-dashboard.json" << 'EOF'
{
  "dashboard": {
    "id": null,
    "title": "IOE-DREAM - Resilience4j 重试监控",
    "tags": ["resilience4j", "retry", "ioedream"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "重试调用次数",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(resilience4j_retry_calls_total[5m])",
            "legendFormat": "{{name}}"
          }
        ]
      },
      {
        "id": 2,
        "title": "重试成功率",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(resilience4j_retry_calls_total{kind=\"successful\"}[5m]) / rate(resilience4j_retry_calls_total[5m]) * 100",
            "legendFormat": "{{name}} 成功率"
          }
        ],
        "yAxes": [
          {
            "unit": "percent",
            "min": 0,
            "max": 100
          }
        ]
      },
      {
        "id": 3,
        "title": "重试次数分布",
        "type": "graph",
        "targets": [
          {
            "expr": "increase(resilience4j_retry_calls_total[5m])",
            "legendFormat": "{{name}}-{{kind}}"
          }
        ]
      }
    ],
    "time": {
      "from": "now-1h",
      "to": "now"
    },
    "refresh": "5s"
  }
}
EOF

    log_success "Grafana仪表盘JSON配置创建完成"
}

# 创建Prometheus配置
create_prometheus_config() {
    log_info "创建Prometheus配置..."

    cat > "$PROMETHEUS_CONFIG_DIR/prometheus.yml" << 'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "/etc/prometheus/rules/*.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

scrape_configs:
  # IOE-DREAM 微服务监控
  - job_name: 'ioedream-gateway'
    static_configs:
      - targets: ['ioedream-gateway-service:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'ioedream-common-service'
    static_configs:
      - targets: ['ioedream-common-service:8088']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'ioedream-device-comm-service'
    static_configs:
      - targets: ['ioedream-device-comm-service:8087']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'ioedream-oa-service'
    static_configs:
      - targets: ['ioedream-oa-service:8089']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'ioedream-access-service'
    static_configs:
      - targets: ['ioedream-access-service:8090']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'ioedream-attendance-service'
    static_configs:
      - targets: ['ioedream-attendance-service:8091']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'ioedream-video-service'
    static_configs:
      - targets: ['ioedream-video-service:8092']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'ioedream-consume-service'
    static_configs:
      - targets: ['ioedream-consume-service:8094']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'ioedream-visitor-service'
    static_configs:
      - targets: ['ioedream-visitor-service:8095']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  # Prometheus自监控
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
EOF

    log_success "Prometheus配置创建完成"
}

# 创建告警规则
create_alert_rules() {
    log_info "创建Resilience4j告警规则..."

    mkdir -p "$PROMETHEUS_CONFIG_DIR/rules"

    cat > "$PROMETHEUS_CONFIG_DIR/rules/resilience4j-alerts.yml" << 'EOF'
groups:
  - name: resilience4j_circuitbreaker_alerts
    rules:
      # 熔断器打开告警
      - alert: CircuitBreakerOpen
        expr: resilience4j_circuitbreaker_state{state="OPEN"} == 1
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "熔断器 {{labels.name }} 已打开"
          description: "服务 {{labels.name }} 的熔断器已打开，需要检查服务状态"

      # 熔断器高失败率告警
      - alert: CircuitBreakerHighFailureRate
        expr: rate(resilience4j_circuitbreaker_calls_total{kind="failed"}[5m]) / rate(resilience4j_circuitbreaker_calls_total[5m]) * 100 > 70
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "熔断器 {{labels.name}} 失败率过高"
          description: "服务 {{labels.name}} 的失败率已达到 {{ $value }}%，超过70%阈值"

      # 熔断器成功率低告警
      - alert: CircuitBreakerLowSuccessRate
        expr: rate(resilience4j_circuitbreaker_calls_total{kind="successful"}[5m]) / rate(resilience4j_circuitbreaker_calls_total[5m]) * 100 < 80
        for: 3m
        labels:
          severity: critical
        annotations:
          summary: "熔断器 {{labels.name}} 成功率过低"
          description: "服务 {{labels.name}} 的成功率仅为 {{ $value }}%，低于80%阈值"

  - name: resilience4j_ratelimiter_alerts
    rules:
      # 限流器高拒绝率告警
      - alert: RateLimiterHighRejectionRate
        expr: rate(resilience4j_ratelimiter_calls_total{kind="failed"}[5m]) / rate(resilience4j_ratelimiter_calls_total[5m]) * 100 > 50
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "限流器 {{labels.name}} 拒绝率过高"
          description: "限流器 {{labels.name}} 的拒绝率已达到 {{ $value }}%，超过50%阈值"

      # 限流器可用许可不足告警
      - alert: RateLimiterLowAvailablePermissions
        expr: resilience4j_ratelimiter_available_permissions < 5
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "限流器 {{labels.name}} 可用许可不足"
          description: "限流器 {{labels.name}} 的可用许可仅为 {{ $value }}，建议调整限流配置"

  - name: resilience4j_retry_alerts
    rules:
      # 重试次数过多告警
      - alert: RetryHighRetryRate
        expr: rate(resilience4j_retry_calls_total[5m]) > 10
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "服务 {{labels.name}} 重试次数过多"
          description: "服务 {{labels.name}} 的重试频率为 {{ $value }}/秒，可能存在服务不稳定"

      # 重试成功率低告警
      - alert: RetryLowSuccessRate
        expr: rate(resilience4j_retry_calls_total{kind="successful_without_retry"}[5m]) / rate(resilience4j_retry_calls_total[5m]) * 100 < 50
        for: 3m
        labels:
          severity: critical
        annotations:
          summary: "服务 {{labels.name}} 重试成功率低"
          description: "服务 {{labels.name}} 的重试成功率仅为 {{ $value }}%，服务可能存在严重问题"

  - name: resilience4j_bulkhead_alerts
    rules:
      # 舱壁并发使用率高告警
      - alert: BulkheadHighConcurrency
        expr: (resilience4j_bulkhead_max_concurrent_calls - resilience4j_bulkhead_available_concurrent_calls) / resilience4j_bulkhead_max_concurrent_calls * 100 > 80
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "舱壁 {{labels.name}} 并发使用率过高"
          description: "舱壁 {{labels.name}} 的并发使用率为 {{ $value }}%，超过80%阈值"

  - name: resilience4j_system_alerts
    rules:
      # 大量熔断器同时打开告警
      - alert: MultipleCircuitBreakersOpen
        expr: sum(resilience4j_circuitbreaker_state{state="OPEN"}) > 3
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "多个熔断器同时打开"
          description: "当前有 {{ $value }} 个熔断器处于打开状态，系统可能存在严重问题"

      # 整体服务健康度告警
      - alert: LowOverallServiceHealth
        expr: sum(rate(resilience4j_circuitbreaker_calls_total{kind="successful"}[5m])) / sum(rate(resilience4j_circuitbreaker_calls_total[5m])) * 100 < 90
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "系统整体服务健康度下降"
          description: "系统整体成功率为 {{ $value }}%，低于90%阈值"
EOF

    log_success "告警规则创建完成"
}

# 创建AlertManager配置
create_alertmanager_config() {
    log_info "创建AlertManager配置..."

    cat > "$DEPLOYMENT_DIR/alertmanager/alertmanager.yml" << 'EOF'
global:
  smtp_smarthost: 'localhost:587'
  smtp_from: 'alerts@ioedream.com'

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'

receivers:
  - name: 'web.hook'
    email_configs:
      - to: 'admin@ioedream.com'
        subject: '[IOE-DREAM Alert] {{ .GroupLabels.alertname }}'
        body: |
          {{ range .Alerts }}
          告警名称: {{ .Annotations.summary }}
          告警描述: {{ .Annotations.description }}
          告警级别: {{ .Labels.severity }}
          开始时间: {{ .StartsAt }}
          {{ end }}

inhibit_rules:
  - source_match:
      severity: 'critical'
    target_match:
      severity: 'warning'
    equal: ['alertname', 'instance']
EOF

    log_success "AlertManager配置创建完成"
}

# 创建Docker Compose文件
create_docker_compose() {
    log_info "创建Docker Compose配置..."

    cat > "$DEPLOYMENT_DIR/docker-compose.yml" << 'EOF'
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:v2.45.0
    container_name: ioedream-prometheus
    hostname: prometheus
    ports:
      - "9090:9090"
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=30d'
      - '--web.enable-lifecycle'
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - ./prometheus/rules:/etc/prometheus/rules
      - prometheus-data:/prometheus
    restart: unless-stopped
    networks:
      - resilience4j-network
    labels:
      - "ioe-dream.service=prometheus"
      - "ioe-dream.environment=production"

  grafana:
    image: grafana/grafana:10.0.0
    container_name: ioedream-grafana
    hostname: grafana
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin123
      - GF_INSTALL_PLUGINS=grafana-clock-panel,grafana-simple-json-datasource
      - GF_SERVER_DOMAIN=localhost
      - GF_SERVER_ROOT_URL=http://localhost:3001
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning
      - ./grafana/provisioning/dashboards:/var/lib/grafana/dashboards/resilience4j
    restart: unless-stopped
    networks:
      - resilience4j-network
    depends_on:
      - prometheus
    labels:
      - "ioe-dream.service=grafana"
      - "ioe-dream.environment=production"

  alertmanager:
    image: prom/alertmanager:v0.25.0
    container_name: ioedream-alertmanager
    hostname: alertmanager
    ports:
      - "9093:9093"
    command:
      - '--config.file=/etc/alertmanager/alertmanager.yml'
      - '--storage.path=/alertmanager'
    volumes:
      - ./alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml
      - alertmanager-data:/alertmanager
    restart: unless-stopped
    networks:
      - resilience4j-network
    labels:
      - "ioe-dream.service=alertmanager"
      - "ioe-dream.environment=production"

networks:
  resilience4j-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.21.0.0/16
          gateway: 172.21.0.1

volumes:
  prometheus-data:
    driver: local
  grafana-data:
    driver: local
  alertmanager-data:
    driver: local
EOF

    log_success "Docker Compose配置创建完成"
}

# 部署服务
deploy_services() {
    log_info "部署Resilience4j监控服务..."

    cd "$DEPLOYMENT_DIR"

    # 停止现有服务
    log_info "停止现有服务..."
    docker-compose down -v || true

    # 启动服务
    log_info "启动监控服务..."
    docker-compose up -d

    log_success "Resilience4j监控服务部署完成"
}

# 等待服务就绪
wait_for_services() {
    log_info "等待服务就绪..."

    local services=(
        "http://localhost:9090/-/healthy:Prometheus"
        "http://localhost:3001/api/health:Grafana"
    )

    for service_info in "${services[@]}"; do
        local url=${service_info%:*}
        local name=${service_info#*:}

        log_info "等待 $name 服务启动..."

        local retries=0
        local max_retries=30

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
    log_success "🎉 Resilience4j监控部署成功！"
    echo ""
    echo "============================================================"
    echo "📊 服务访问地址："
    echo "============================================================"
    echo "🔍 Prometheus:       http://localhost:9090"
    echo "📊 Grafana:          http://localhost:3001"
    echo "🚨 AlertManager:    http://localhost:9093"
    echo ""
    echo "============================================================"
    echo "🔐 登录信息："
    echo "============================================================"
    echo "📊 Grafana用户名:    admin"
    echo "📊 Grafana密码:      admin123"
    echo ""
    echo "============================================================"
    echo "📈 预配置仪表盘："
    echo "============================================================"
    echo "🔥 熔断器监控:      IOE-DREAM - Resilience4j 熔断器监控"
    echo "⏱️ 限流器监控:      IOE-DREAM - Resilience4j 限流器监控"
    echo "🔄 重试监控:        IOE-DREAM - Resilience4j 重试监控"
    echo ""
    echo "============================================================"
    echo "🔧 管理命令："
    echo "============================================================"
    echo "📦 查看服务状态:     docker-compose ps"
    echo "📦 查看服务日志:     docker-compose logs [service-name]"
    echo "🛑 停止所有服务:     docker-compose down"
    echo "🚀 重启所有服务:     docker-compose restart"
    echo ""
    echo "============================================================"
    echo "📊 监控指标说明："
    echo "============================================================"
    echo "🔥 熔断器状态:      resilience4j_circuitbreaker_state"
    echo "⚡ 熔断器调用率:    resilience4j_circuitbreaker_calls_total"
    echo "🎯 限流器许可:      resilience4j_ratelimiter_available_permissions"
    echo "🔄 重试次数:        resilience4j_retry_calls_total"
    echo "🚀 舱壁并发:        resilience4j_bulkhead_available_concurrent_calls"
    echo ""
    echo "============================================================"
    echo "⚠️  注意事项："
    echo "============================================================"
    echo "🔒 确保微服务已启动并暴露/actuator/prometheus端点"
    echo "📊 确保防火墙已开放对应端口"
    echo "🔧 根据实际需求调整告警阈值"
    echo "📈 定期检查仪表盘和告警状态"
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  deploy     部署Resilience4j监控"
    echo "  start      启动监控服务"
    echo "  stop       停止监控服务"
    echo "  restart    重启监控服务"
    echo "  status     查看服务状态"
    echo "  logs       查看服务日志"
    echo "  clean      清理监控数据和服务"
    echo "  help       显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 deploy    # 完整部署"
    echo "  $0 start     # 启动服务"
    echo "  $0 status    # 查看状态"
}

# 启动服务
start_services() {
    log_info "启动Resilience4j监控服务..."
    cd "$DEPLOYMENT_DIR"
    docker-compose up -d
    show_access_info
}

# 停止服务
stop_services() {
    log_info "停止Resilience4j监控服务..."
    cd "$DEPLOYMENT_DIR"
    docker-compose down
    log_success "服务已停止"
}

# 重启服务
restart_services() {
    log_info "重启Resilience4j监控服务..."
    stop_services
    sleep 5
    start_services
}

# 查看服务状态
show_status() {
    log_info "Resilience4j监控服务状态："
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
    log_warning "这将删除所有监控数据和服务！"
    read -p "确定要继续吗？(y/N): " -n 1 -r
    echo

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cd "$DEPLOYMENT_DIR"
        docker-compose down -v
        docker system prune -f
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
            create_directories
            create_grafana_datasources
            create_grafana_dashboards
            create_grafana_dashboard_json
            create_prometheus_config
            create_alert_rules
            create_alertmanager_config
            create_docker_compose
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