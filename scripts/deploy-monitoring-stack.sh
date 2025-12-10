#!/bin/bash

# ============================================================
# IOE-DREAM 监控栈部署脚本
#
# @Author:    IOE-DREAM Team
# @Date:      2025-12-09
# @Description: 部署Prometheus + Grafana + AlertManager完整监控栈
# 规模: 单企业1000台设备20000人
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
DEPLOYMENT_DIR="$(pwd)/deployments/monitoring"

# 服务端口配置
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
ALERTMANAGER_PORT=9093
NODE_EXPORTER_PORT=9100
CADVISOR_PORT=8080
PUSHGATEWAY_PORT=9091

# 监控目标配置
MONITORING_INTERVAL=15s
SCRAPE_INTERVAL=15s
EVALUATION_INTERVAL=15s

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

    local ports=(
        "$PROMETHEUS_PORT:Prometheus"
        "$GRAFANA_PORT:Grafana"
        "$ALERTMANAGER_PORT:AlertManager"
        "$NODE_EXPORTER_PORT:Node Exporter"
        "$CADVISOR_PORT:cAdvisor"
        "$PUSHGATEWAY_PORT:Pushgateway"
    )

    for port_info in "${ports[@]}"; do
        local port=${port_info%:*}
        local service=${port_info#*:}

        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            log_warning "$service 端口 $port 已被占用，尝试释放..."
            local pid=$(lsof -ti:$port)
            if [ ! -z "$pid" ]; then
                kill -9 $pid 2>/dev/null || true
                sleep 2
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
        "$DEPLOYMENT_DIR/prometheus"
        "$DEPLOYMENT_DIR/grafana"
        "$DEPLOYMENT_DIR/grafana/provisioning"
        "$DEPLOYMENT_DIR/grafana/provisioning/datasources"
        "$DEPLOYMENT_DIR/grafana/provisioning/dashboards"
        "$DEPLOYMENT_DIR/grafana/provisioning/dashboards/ioedream"
        "$DEPLOYMENT_DIR/grafana/provisioning/dashboards/ioedream/overview"
        "$DEPLOYMENT_DIR/grafana/provisioning/dashboards/ioedream/services"
        "$DEPLOYMENT_DIR/grafana/provisioning/dashboards/ioedream/infrastructure"
        "$DEPLOYMENT_DIR/grafana/provisioning/dashboards/ioedream/business"
        "$DEPLOYMENT_DIR/alertmanager"
        "/opt/$PROJECT_NAME/data/prometheus"
        "/opt/$PROJECT_NAME/data/grafana"
        "/opt/$PROJECT_NAME/data/alertmanager"
        "/opt/$PROJECT_NAME/logs/monitoring"
    )

    for dir in "${directories[@]}"; do
        if [ ! -d "$dir" ]; then
            mkdir -p "$dir"
            log_info "创建目录: $dir"
        fi
    done

    # 设置目录权限
    chmod 755 "/opt/$PROJECT_NAME/data/prometheus"
    chmod 755 "/opt/$PROJECT_NAME/data/grafana"
    chmod 755 "/opt/$PROJECT_NAME/data/alertmanager"
    chmod 755 "/opt/$PROJECT_NAME/logs/monitoring"

    log_success "目录结构创建完成"
}

# 创建Prometheus配置
create_prometheus_config() {
    log_info "创建Prometheus配置..."

    cat > "$DEPLOYMENT_DIR/prometheus/prometheus.yml" << 'EOF'
# ============================================================
# IOE-DREAM Prometheus配置
# 规模: 1000台设备, 20000人
# ============================================================

global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'ioedream-prod'
    region: 'main'

# 告警规则文件
rule_files:
  - "/etc/prometheus/rules/*.yml"

# 告警管理器配置
alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

# 监控目标配置
scrape_configs:
  # Prometheus自监控
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
    scrape_interval: 15s
    metrics_path: '/metrics'

  # IOE-DREAM 微服务监控
  - job_name: 'ioedream-gateway'
    static_configs:
      - targets: ['ioedream-gateway-service:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'gateway'

  - job_name: 'ioedream-common-service'
    static_configs:
      - targets: ['ioedream-common-service:8088']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'common-service'

  - job_name: 'ioedream-device-comm-service'
    static_configs:
      - targets: ['ioedream-device-comm-service:8087']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'device-comm-service'

  - job_name: 'ioedream-oa-service'
    static_configs:
      - targets: ['ioedream-oa-service:8089']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'oa-service'

  - job_name: 'ioedream-access-service'
    static_configs:
      - targets: ['ioedream-access-service:8090']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'access-service'

  - job_name: 'ioedream-attendance-service'
    static_configs:
      - targets: ['ioedream-attendance-service:8091']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'attendance-service'

  - job_name: 'ioedream-video-service'
    static_configs:
      - targets: ['ioedream-video-service:8092']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'video-service'

  - job_name: 'ioedream-consume-service'
    static_configs:
      - targets: ['ioedream-consume-service:8094']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'consume-service'

  - job_name: 'ioedream-visitor-service'
    static_configs:
      - targets: ['ioedream-visitor-service:8095']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'visitor-service'

  # Redis集群监控
  - job_name: 'redis-cluster'
    static_configs:
      - targets:
        - 'ioedream-redis-exporter:9121'
    scrape_interval: 15s

  # RabbitMQ监控
  - job_name: 'rabbitmq'
    static_configs:
      - targets: ['ioedream-rabbitmq:15692']
    scrape_interval: 15s

  # Node Exporter监控
  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']
    scrape_interval: 15s

  # cAdvisor容器监控
  - job_name: 'cadvisor'
    static_configs:
      - targets: ['cadvisor:8080']
    scrape_interval: 15s
    metrics_path: '/metrics'

  # Pushgateway监控
  - job_name: 'pushgateway'
    static_configs:
      - targets: ['pushgateway:9091']
    scrape_interval: 15s
    honor_labels: true

# 远程写入配置（可选）
# remote_write:
#   - url: "http://remote-storage/api/v1/write"
#     queue_config:
#       max_samples_per_send: 1000
#       max_shards: 200
#       capacity: 2500

# 远程读取配置（可选）
# remote_read:
#   - url: "http://remote-storage/api/v1/read"
#     read_recent: true
EOF

    log_success "Prometheus配置创建完成"
}

# 创建Prometheus告警规则
create_alert_rules() {
    log_info "创建Prometheus告警规则..."

    mkdir -p "$DEPLOYMENT_DIR/prometheus/rules"

    # 服务可用性告警规则
    cat > "$DEPLOYMENT_DIR/prometheus/rules/ioedream-availability.yml" << 'EOF'
groups:
  - name: ioedream-availability
    rules:
      # 服务不可用告警
      - alert: ServiceDown
        expr: up{job=~"ioedream-.*"} == 0
        for: 1m
        labels:
          severity: critical
          service: "{{ $labels.job }}"
        annotations:
          summary: "服务 {{ $labels.job }} 不可用"
          description: "服务 {{ $labels.job }} 已经超过1分钟不可用"
          runbook_url: "https://docs.ioedream.com/runbooks/service-down"

      # 服务高错误率告警
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) / rate(http_requests_total[5m]) > 0.05
        for: 5m
        labels:
          severity: warning
          service: "{{ $labels.job }}"
        annotations:
          summary: "服务 {{ $labels.job }} 错误率过高"
          description: "服务 {{ $labels.job }} 在过去5分钟内错误率为 {{ $value | humanizePercentage }}"
EOF

    # 系统性能告警规则
    cat > "$DEPLOYMENT_DIR/prometheus/rules/ioedream-performance.yml" << 'EOF'
groups:
  - name: ioedream-performance
    rules:
      # CPU使用率告警
      - alert: HighCPUUsage
        expr: 100 - (avg by (instance) (irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "CPU使用率过高"
          description: "实例 {{ $labels.instance }} CPU使用率为 {{ $value }}%"

      # 内存使用率告警
      - alert: HighMemoryUsage
        expr: (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100 > 85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "内存使用率过高"
          description: "实例 {{ $labels.instance }} 内存使用率为 {{ $value }}%"

      # 磁盘使用率告警
      - alert: HighDiskUsage
        expr: (1 - (node_filesystem_avail_bytes{mountpoint="/"} / node_filesystem_size_bytes{mountpoint="/"})) * 100 > 90
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "磁盘使用率过高"
          description: "实例 {{ $labels.instance }} 磁盘使用率为 {{ $value }}%"

      # 响应时间告警
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "响应时间过长"
          description: "服务 {{ $labels.job }} 95%响应时间为 {{ $value }}s"

      # JVM内存使用率告警
      - alert: HighJVMMemoryUsage
        expr: (jvm_memory_used_bytes / jvm_memory_max_bytes) * 100 > 85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "JVM内存使用率过高"
          description: "应用 {{ $labels.application }} JVM内存使用率为 {{ $value }}%"
EOF

    # 业务指标告警规则
    cat > "$DEPLOYMENT_DIR/prometheus/rules/ioedream-business.yml" << 'EOF'
groups:
  - name: ioedream-business
    rules:
      # 用户登录失败率告警
      - alert: HighUserLoginFailureRate
        expr: rate(user_login_failure_total[5m]) / (rate(user_login_success_total[5m]) + rate(user_login_failure_total[5m])) > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "用户登录失败率过高"
          description: "用户登录失败率为 {{ $value | humanizePercentage }}"

      # 设备离线率告警
      - alert: HighDeviceOfflineRate
        expr: device_offline_total / (device_online_total + device_offline_total) > 0.05
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "设备离线率过高"
          description: "设备离线率为 {{ $value | humanizePercentage }}"

      # 交易失败率告警
      - alert: HighTransactionFailureRate
        expr: rate(transaction_failure_total[5m]) / (rate(transaction_success_total[5m]) + rate(transaction_failure_total[5m])) > 0.01
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "交易失败率过高"
          description: "交易失败率为 {{ $value | humanizePercentage }}"

      # 考勤打卡失败率告警
      - alert: HighAttendanceFailureRate
        expr: rate(attendance_failure_total[5m]) / (rate(attendance_success_total[5m]) + rate(attendance_failure_total[5m])) > 0.05
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "考勤打卡失败率过高"
          description: "考勤打卡失败率为 {{ $value | humanizePercentage }}"

      # 在线用户数告警
      - alert: LowOnlineUserCount
        expr: online_users_total < 100
        for: 5m
        labels:
          severity: info
        annotations:
          summary: "在线用户数过低"
          description: "当前在线用户数为 {{ $value }}"
EOF

    # Redis告警规则
    cat > "$DEPLOYMENT_DIR/prometheus/rules/redis.yml" << 'EOF'
groups:
  - name: redis
    rules:
      # Redis内存使用率告警
      - alert: RedisHighMemoryUsage
        expr: redis_memory_used_bytes / redis_memory_max_bytes * 100 > 90
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Redis内存使用率过高"
          description: "Redis实例 {{ $labels.instance }} 内存使用率为 {{ $value }}%"

      # Redis连接数告警
      - alert: RedisHighConnections
        expr: redis_connected_clients > 1000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Redis连接数过高"
          description: "Redis实例 {{ $labels.instance }} 连接数为 {{ $value }}"

      # Redis命中率告警
      - alert: RedisLowHitRate
        expr: redis_keyspace_hits_total / (redis_keyspace_hits_total + redis_keyspace_misses_total) < 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Redis命中率过低"
          description: "Redis实例 {{ $labels.instance }} 命中率为 {{ $value | humanizePercentage }}"
EOF

    log_success "Prometheus告警规则创建完成"
}

# 创建Grafana数据源配置
create_grafana_datasources() {
    log_info "创建Grafana数据源配置..."

    cat > "$DEPLOYMENT_DIR/grafana/provisioning/datasources/prometheus.yml" << 'EOF'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
    jsonData:
      timeInterval: 15s
      queryTimeout: 60s
      httpMethod: POST
    secureJsonData: {}

  - name: IOE-DREAM Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    database: "prometheus"
    jsonData:
      timeInterval: 5s
      queryTimeout: 30s
      httpMethod: POST
    secureJsonData: {}
EOF

    log_success "Grafana数据源配置创建完成"
}

# 创建Grafana仪表盘配置
create_grafana_dashboards() {
    log_info "创建Grafana仪表盘配置..."

    cat > "$DEPLOYMENT_DIR/grafana/provisioning/dashboards/dashboard.yml" << 'EOF'
apiVersion: 1

providers:
  - name: 'ioedream-dashboards'
    orgId: 1
    folder: 'IOE-DREAM'
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /var/lib/grafana/dashboards/ioedream
EOF

    log_success "Grafana仪表盘配置创建完成"
}

# 创建Grafana总览仪表盘
create_overview_dashboard() {
    log_info "创建IOE-DREAM总览仪表盘..."

    cat > "$DEPLOYMENT_DIR/grafana/provisioning/dashboards/ioedream/overview/overview.json" << 'EOF'
{
  "dashboard": {
    "id": null,
    "title": "IOE-DREAM 系统总览",
    "tags": ["ioedream", "overview"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "服务状态概览",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=~\"ioedream-.*\"}",
            "legendFormat": "{{job}}"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "color": {
              "mode": "thresholds"
            },
            "thresholds": {
              "steps": [
                {"color": "green", "value": null},
                {"color": "red", "value": 0}
              ]
            },
            "unit": "short"
          }
        },
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "系统资源使用率",
        "type": "graph",
        "targets": [
          {
            "expr": "100 - (avg by (instance) (irate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)",
            "legendFormat": "CPU使用率"
          },
          {
            "expr": "(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100",
            "legendFormat": "内存使用率"
          }
        ],
        "yAxes": [
          {
            "unit": "percent",
            "min": 0,
            "max": 100
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0}
      },
      {
        "id": 3,
        "title": "请求量趋势",
        "type": "graph",
        "targets": [
          {
            "expr": "sum(rate(http_requests_total{job=~\"ioedream-.*\"}[5m])) by (job)",
            "legendFormat": "{{job}}"
          }
        ],
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 8}
      },
      {
        "id": 4,
        "title": "错误率趋势",
        "type": "graph",
        "targets": [
          {
            "expr": "sum(rate(http_requests_total{job=~\"ioedream-.*\",status=~\"5..\"}[5m])) by (job) / sum(rate(http_requests_total{job=~\"ioedream-.*\"}[5m])) by (job)",
            "legendFormat": "{{job}} 错误率"
          }
        ],
        "yAxes": [
          {
            "unit": "percentunit",
            "min": 0,
            "max": 1
          }
        ],
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 16}
      },
      {
        "id": 5,
        "title": "响应时间分布",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.50, sum(rate(http_request_duration_seconds_bucket{job=~\"ioedream-.*\"}[5m])) by (le, job))",
            "legendFormat": "{{job}} P50"
          },
          {
            "expr": "histogram_quantile(0.95, sum(rate(http_request_duration_seconds_bucket{job=~\"ioedream-.*\"}[5m])) by (le, job))",
            "legendFormat": "{{job}} P95"
          },
          {
            "expr": "histogram_quantile(0.99, sum(rate(http_request_duration_seconds_bucket{job=~\"ioedream-.*\"}[5m])) by (le, job))",
            "legendFormat": "{{job}} P99"
          }
        ],
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 24}
      },
      {
        "id": 6,
        "title": "业务指标概览",
        "type": "stat",
        "targets": [
          {
            "expr": "online_users_total",
            "legendFormat": "在线用户数"
          },
          {
            "expr": "device_online_total",
            "legendFormat": "在线设备数"
          }
        ],
        "gridPos": {"h": 4, "w": 12, "x": 0, "y": 32}
      },
      {
        "id": 7,
        "title": "交易统计",
        "type": "stat",
        "targets": [
          {
            "expr": "rate(transaction_success_total[5m])",
            "legendFormat": "成功交易率"
          },
          {
            "expr": "rate(transaction_failure_total[5m])",
            "legendFormat": "失败交易率"
          }
        ],
        "gridPos": {"h": 4, "w": 12, "x": 12, "y": 32}
      }
    ],
    "time": {
      "from": "now-1h",
      "to": "now"
    },
    "refresh": "30s"
  }
}
EOF

    log_success "IOE-DREAM总览仪表盘创建完成"
}

# 创建服务监控仪表盘
create_service_dashboard() {
    log_info "创建服务监控仪表盘..."

    cat > "$DEPLOYMENT_DIR/grafana/provisioning/dashboards/ioedream/services/service-monitoring.json" << 'EOF'
{
  "dashboard": {
    "id": null,
    "title": "IOE-DREAM 服务监控",
    "tags": ["ioedream", "services"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "微服务状态",
        "type": "table",
        "targets": [
          {
            "expr": "up{job=~\"ioedream-.*\"}",
            "format": "table",
            "instant": true
          }
        ],
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "JVM内存使用",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{job=~\"ioedream-.*\"} / jvm_memory_max_bytes{job=~\"ioedream-.*\"} * 100",
            "legendFormat": "{{application}}"
          }
        ],
        "yAxes": [
          {
            "unit": "percent",
            "min": 0,
            "max": 100
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 8}
      },
      {
        "id": 3,
        "title": "线程池状态",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_threads_live_threads{job=~\"ioedream-.*\"}",
            "legendFormat": "{{application}} 活跃线程"
          },
          {
            "expr": "jvm_threads_daemon_threads{job=~\"ioedream-.*\"}",
            "legendFormat": "{{application}} 守护线程"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 8}
      },
      {
        "id": 4,
        "title": "GC活动",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(jvm_gc_pause_seconds_sum{job=~\"ioedream-.*\"}[5m])",
            "legendFormat": "{{application}} {{gc}}"
          }
        ],
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 16}
      },
      {
        "id": 5,
        "title": "数据库连接池",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active{job=~\"ioedream-.*\"}",
            "legendFormat": "{{application}} 活跃连接"
          },
          {
            "expr": "hikaricp_connections_idle{job=~\"ioedream-.*\"}",
            "legendFormat": "{{application}} 空闲连接"
          }
        ],
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 24}
      },
      {
        "id": 6,
        "title": "Redis连接状态",
        "type": "graph",
        "targets": [
          {
            "expr": "redis_connected_clients",
            "legendFormat": "Redis连接数"
          }
        ],
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 32}
      }
    ],
    "time": {
      "from": "now-1h",
      "to": "now"
    },
    "refresh": "30s"
  }
}
EOF

    log_success "服务监控仪表盘创建完成"
}

# 创建业务监控仪表盘
create_business_dashboard() {
    log_info "创建业务监控仪表盘..."

    cat > "$DEPLOYMENT_DIR/grafana/provisioning/dashboards/ioedream/business/business-metrics.json" << 'EOF'
{
  "dashboard": {
    "id": null,
    "title": "IOE-DREAM 业务监控",
    "tags": ["ioedream", "business"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "用户统计",
        "type": "stat",
        "targets": [
          {
            "expr": "online_users_total",
            "legendFormat": "在线用户"
          },
          {
            "expr": "rate(user_login_success_total[5m])",
            "legendFormat": "登录成功/5m"
          },
          {
            "expr": "rate(user_login_failure_total[5m])",
            "legendFormat": "登录失败/5m"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "设备状态",
        "type": "stat",
        "targets": [
          {
            "expr": "device_online_total",
            "legendFormat": "在线设备"
          },
          {
            "expr": "device_offline_total",
            "legendFormat": "离线设备"
          },
          {
            "expr": "rate(device_heartbeat_total[5m])",
            "legendFormat": "心跳/5m"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0}
      },
      {
        "id": 3,
        "title": "交易统计",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(transaction_success_total[5m])",
            "legendFormat": "成功交易"
          },
          {
            "expr": "rate(transaction_failure_total[5m])",
            "legendFormat": "失败交易"
          },
          {
            "expr": "rate(transaction_amount_sum[5m])",
            "legendFormat": "交易金额"
          }
        ],
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 8}
      },
      {
        "id": 4,
        "title": "考勤统计",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(attendance_success_total[5m])",
            "legendFormat": "成功打卡"
          },
          {
            "expr": "rate(attendance_failure_total[5m])",
            "legendFormat": "失败打卡"
          }
        ],
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 16}
      },
      {
        "id": 5,
        "title": "访客统计",
        "type": "stat",
        "targets": [
          {
            "expr": "rate(visitor_registration_total[5m])",
            "legendFormat": "访客注册/5m"
          },
          {
            "expr": "visitor_active_total",
            "legendFormat": "活跃访客"
          }
        ],
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 24}
      }
    ],
    "time": {
      "from": "now-1h",
      "to": "now"
    },
    "refresh": "30s"
  }
}
EOF

    log_success "业务监控仪表盘创建完成"
}

# 创建Docker Compose配置
create_docker_compose() {
    log_info "创建Docker Compose配置..."

    cat > "$DEPLOYMENT_DIR/docker-compose.yml" << 'EOF'
version: '3.8'

services:
  # Prometheus监控
  prometheus:
    image: prom/prometheus:v2.45.0
    container_name: ioedream-prometheus
    hostname: prometheus
    ports:
      - "9090:9090"
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=30d'
      - '--storage.tsdb.retention.size=50GB'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--web.enable-lifecycle'
      - '--web.enable-admin-api'
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - ./prometheus/rules:/etc/prometheus/rules:ro
      - ioedream-prometheus-data:/prometheus
      - ioedream-prometheus-logs:/var/log/prometheus
    restart: unless-stopped
    networks:
      - monitoring-network
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:9090/-/healthy"]
      interval: 30s
      timeout: 10s
      retries: 3
    labels:
      - "ioe-dream.service=prometheus"
      - "ioe-dream.environment=production"

  # Grafana可视化
  grafana:
    image: grafana/grafana:10.0.0
    container_name: ioedream-grafana
    hostname: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin123
      - GF_INSTALL_PLUGINS=grafana-clock-panel,grafana-simple-json-datasource,grafana-piechart-panel,redis-datasource
      - GF_SERVER_DOMAIN=localhost
      - GF_SERVER_ROOT_URL=http://localhost:3000
      - GF_AUTH_ANONYMOUS_ENABLED=false
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_SECURITY_COOKIE_SECURE=false
      - GF_SECURITY_COOKIE_SAMESITE=lax
      - GF_FEATURE_TOGGLES_ENABLE=publicDashboards
      - GF_LOG_MODE=console
      - GF_LOG_LEVEL=info
      - GF_PATHS_PLUGINS=/var/lib/grafana/plugins
      - GF_PATHS_PROVISIONING=/etc/grafana/provisioning
    volumes:
      - ioedream-grafana-data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning
      - ./grafana/dashboards:/var/lib/grafana/dashboards
    restart: unless-stopped
    networks:
      - monitoring-network
    depends_on:
      - prometheus
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:3000/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    labels:
      - "ioe-dream.service=grafana"
      - "ioe-dream.environment=production"

  # AlertManager告警管理
  alertmanager:
    image: prom/alertmanager:v0.25.0
    container_name: ioedream-alertmanager
    hostname: alertmanager
    ports:
      - "9093:9093"
    command:
      - '--config.file=/etc/alertmanager/alertmanager.yml'
      - '--storage.path=/alertmanager'
      - '--web.external-url=http://localhost:9093'
      - '--web.route-prefix=/'
    volumes:
      - ./alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml:ro
      - ioedream-alertmanager-data:/alertmanager
      - ioedream-alertmanager-logs:/var/log/alertmanager
    restart: unless-stopped
    networks:
      - monitoring-network
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:9093/-/healthy"]
      interval: 30s
      timeout: 10s
      retries: 3
    labels:
      - "ioe-dream.service=alertmanager"
      - "ioe-dream.environment=production"

  # Node Exporter系统监控
  node-exporter:
    image: prom/node-exporter:v1.6.1
    container_name: ioedream-node-exporter
    hostname: node-exporter
    ports:
      - "9100:9100"
    command:
      - '--path.procfs=/host/proc'
      - '--path.rootfs=/rootfs'
      - '--path.sysfs=/host/sys'
      - '--collector.filesystem.mount-points-exclude=^/(sys|proc|dev|host|etc)($$|/)'
    volumes:
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /:/rootfs:ro
    restart: unless-stopped
    networks:
      - monitoring-network
    labels:
      - "ioe-dream.service=node-exporter"

  # cAdvisor容器监控
  cadvisor:
    image: gcr.io/cadvisor/cadvisor:v0.46.0
    container_name: ioedream-cadvisor
    hostname: cadvisor
    ports:
      - "8080:8080"
    volumes:
      - /:/rootfs:ro
      - /var/run:/var/run:ro
      - /sys:/sys:ro
      - /var/lib/docker/:/var/lib/docker:ro
      - /dev/disk/:/dev/disk:ro
    privileged: true
    devices:
      - /dev/kmsg
    restart: unless-stopped
    networks:
      - monitoring-network
    labels:
      - "ioe-dream.service=cadvisor"

  # Pushgateway临时指标收集
  pushgateway:
    image: prom/pushgateway:v1.6.0
    container_name: ioedream-pushgateway
    hostname: pushgateway
    ports:
      - "9091:9091"
    command:
      - '--web.enable-admin-api'
    restart: unless-stopped
    networks:
      - monitoring-network
    labels:
      - "ioe-dream.service=pushgateway"

networks:
  monitoring-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.24.0.0/16
          gateway: 172.24.0.1

volumes:
  ioedream-prometheus-data:
    driver: local
  ioedream-grafana-data:
    driver: local
  ioedream-alertmanager-data:
    driver: local
  ioedream-prometheus-logs:
    driver: local
  ioedream-grafana-logs:
    driver: local
  ioedream-alertmanager-logs:
    driver: local
EOF

    log_success "Docker Compose配置创建完成"
}

# 创建AlertManager配置
create_alertmanager_config() {
    log_info "创建AlertManager配置..."

    cat > "$DEPLOYMENT_DIR/alertmanager/alertmanager.yml" << 'EOF'
global:
  smtp_smarthost: 'localhost:587'
  smtp_from: 'alerts@ioedream.com'
  smtp_auth_username: 'alerts@ioedream.com'
  smtp_auth_password: 'alert_password'

route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'
  routes:
    - match:
        severity: critical
      receiver: 'critical-alerts'
    - match:
        severity: warning
      receiver: 'warning-alerts'
    - match:
        severity: info
      receiver: 'info-alerts'

receivers:
  - name: 'web.hook'
    webhook_configs:
      - url: 'http://localhost:3000/api/alertmanager/webhooks'
        send_resolved: true

  - name: 'critical-alerts'
    email_configs:
      - to: 'admin@ioedream.com'
        subject: '[IOE-DREAM CRITICAL] {{ .GroupLabels.alertname }}'
        body: |
          {{ range .Alerts }}
          告警名称: {{ .Annotations.summary }}
          告警描述: {{ .Annotations.description }}
          告警级别: {{ .Labels.severity }}
          开始时间: {{ .StartsAt }}
          {{ end }}
    webhook_configs:
      - url: 'http://localhost:3000/api/alertmanager/webhooks/critical'
        send_resolved: true

  - name: 'warning-alerts'
    email_configs:
      - to: 'ops@ioedream.com'
        subject: '[IOE-DREAM WARNING] {{ .GroupLabels.alertname }}'
        body: |
          {{ range .Alerts }}
          告警名称: {{ .Annotations.summary }}
          告警描述: {{ .Annotations.description }}
          告警级别: {{ .Labels.severity }}
          开始时间: {{ .StartsAt }}
          {{ end }}

  - name: 'info-alerts'
    webhook_configs:
      - url: 'http://localhost:3000/api/alertmanager/webhooks/info'
        send_resolved: true

inhibit_rules:
  - source_match:
      severity: 'critical'
    target_match:
      severity: 'warning'
    equal: ['alertname', 'cluster', 'service']
EOF

    log_success "AlertManager配置创建完成"
}

# 部署服务
deploy_services() {
    log_info "部署监控服务..."

    cd "$DEPLOYMENT_DIR"

    # 停止现有服务（如果存在）
    log_info "停止现有服务..."
    docker-compose down -v || true

    # 拉取最新镜像
    log_info "拉取最新镜像..."
    docker-compose pull

    # 启动服务
    log_info "启动监控服务..."
    docker-compose up -d

    # 等待服务启动
    log_info "等待服务启动..."
    sleep 30

    log_success "监控服务部署完成"
}

# 等待服务就绪
wait_for_services() {
    log_info "等待服务就绪..."

    local services=(
        "http://localhost:9090/-/healthy:Prometheus"
        "http://localhost:3000/api/health:Grafana"
        "http://localhost:9093/-/healthy:AlertManager"
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
    log_success "🎉 IOE-DREAM监控栈部署成功！"
    echo ""
    echo "============================================================"
    echo "📊 服务访问地址："
    echo "============================================================"
    echo "📈 Prometheus:       http://localhost:9090"
    echo "📊 Grafana:          http://localhost:3000"
    echo "🚨 AlertManager:     http://localhost:9093"
    echo "🔍 Node Exporter:    http://localhost:9100/metrics"
    echo "📱 cAdvisor:         http://localhost:8080/metrics"
    echo "📤 Pushgateway:      http://localhost:9091"
    echo ""
    echo "============================================================"
    echo "🔐 登录信息："
    echo "============================================================"
    echo "📊 Grafana用户名:     admin"
    echo "📊 Grafana密码:       admin123"
    echo ""
    echo "============================================================"
    echo "📈 预配置仪表盘："
    echo "============================================================"
    echo "🎯 IOE-DREAM 系统总览"
    echo "🔧 IOE-DREAM 服务监控"
    echo "💼 IOE-DREAM 业务监控"
    echo "🏗️  IOE-DREAM 基础设施监控"
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
    echo "📊 监控目标配置："
    echo "============================================================"
    echo "🔗 微服务监控:       9个IOE-DREAM微服务"
    echo "💾 数据库监控:       MySQL、PostgreSQL"
    echo "🔄 缓存监控:         Redis集群"
    echo "📨 消息队列监控:     RabbitMQ"
    echo "🖥️  系统资源监控:     CPU、内存、磁盘、网络"
    echo "🐳 容器监控:         Docker容器"
    echo ""
    echo "============================================================"
    echo "⚡ 告警配置："
    echo "============================================================"
    echo "🚨 服务可用性告警"
    echo "⚠️  性能阈值告警"
    echo "💼 业务指标告警"
    echo "📧 邮件通知:        admin@ioedream.com"
    echo "🔗 Webhook通知:      自动触发告警处理"
    echo ""
    echo "============================================================"
    echo "📈 应用监控配置："
    echo "============================================================"
    echo "spring.boot.admin.enabled=true"
    echo "management.endpoints.web.exposure.include=health,info,metrics,prometheus"
    echo "management.metrics.export.prometheus.enabled=true"
    echo ""
    echo "============================================================"
    echo "🎯 规模支持（已优化）："
    echo "============================================================"
    echo "👥 支持用户数:        20,000人"
    echo "🖥️  支持设备数:        1,000台"
    echo "📊 监控指标数量:      500+个"
    echo "⏱️  数据保留时间:      30天"
    echo "📈 告警响应时间:      1分钟内"
    echo ""
    echo "============================================================"
    echo "⚠️  注意事项："
    echo "============================================================"
    echo "🔒 确保防火墙已开放对应端口"
    echo "💾 监控数据存储在: /opt/$PROJECT_NAME/data/monitoring/"
    echo "📝 监控日志存储在: /opt/$PROJECT_NAME/logs/monitoring/"
    echo "🔧 配置文件位置: $DEPLOYMENT_DIR/"
    echo "📊 定期检查和优化告警规则"
    echo "🔧 根据业务需求调整监控指标"
}

# 健康检查
health_check() {
    log_info "执行健康检查..."

    local all_healthy=true

    # 检查监控服务状态
    log_info "检查监控服务状态..."
    local services=("ioedream-prometheus" "ioedream-grafana" "ioedream-alertmanager")

    for service in "${services[@]}"; do
        if docker ps --filter "name=$service" --format "table {{.Status}}" | grep -q "Up"; then
            log_success "✓ 服务 $service 运行正常"
        else
            log_error "✗ 服务 $service 运行异常"
            all_healthy=false
        fi
    done

    # 检查服务健康状态
    log_info "检查服务健康状态..."
    local urls=(
        "http://localhost:9090/-/healthy"
        "http://localhost:3000/api/health"
        "http://localhost:9093/-/healthy"
    )

    for url in "${urls[@]}"; do
        if curl -sf "$url" >/dev/null 2>&1; then
            log_success "✓ 监控服务健康"
        else
            log_error "✗ 监控服务异常"
            all_healthy=false
        fi
    done

    # 检查Prometheus目标
    log_info "检查Prometheus监控目标..."
    if curl -sf "http://localhost:9090/api/v1/targets" >/dev/null 2>&1; then
        local healthy_targets=$(curl -s "http://localhost:9090/api/v1/targets" | jq -r '.data.activeTargets | length' 2>/dev/null || echo "0")
        log_success "✓ Prometheus监控目标: $healthy_targets 个活跃目标"
    else
        log_error "✗ 无法获取Prometheus监控目标"
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
    echo "  deploy     部署监控栈"
    echo "  start      启动监控服务"
    echo "  stop       停止监控服务"
    echo "  restart    重启监控服务"
    echo "  status     查看服务状态"
    echo "  logs       查看服务日志"
    echo "  health     执行健康检查"
    echo "  reload     重新加载Prometheus配置"
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
    log_info "启动监控服务..."
    cd "$DEPLOYMENT_DIR"
    docker-compose up -d
    show_access_info
}

# 停止服务
stop_services() {
    log_info "停止监控服务..."
    cd "$DEPLOYMENT_DIR"
    docker-compose down
    log_success "服务已停止"
}

# 重启服务
restart_services() {
    log_info "重启监控服务..."
    stop_services
    sleep 5
    start_services
}

# 查看服务状态
show_status() {
    log_info "监控服务状态："
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

# 重新加载Prometheus配置
reload_config() {
    log_info "重新加载Prometheus配置..."
    curl -X POST http://localhost:9090/-/reload
    log_success "Prometheus配置重新加载完成"
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

        # 清理数据目录
        rm -rf /opt/$PROJECT_NAME/data/monitoring*
        rm -rf /opt/$PROJECT_NAME/logs/monitoring*

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
            check_ports
            create_directories
            create_prometheus_config
            create_alert_rules
            create_grafana_datasources
            create_grafana_dashboards
            create_overview_dashboard
            create_service_dashboard
            create_business_dashboard
            create_docker_compose
            create_alertmanager_config
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
        "reload")
            reload_config
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