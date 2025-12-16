# ============================================================
# IOE-DREAM 监控系统自动化部署脚本 (Windows PowerShell版本)
# 功能: 自动化部署完整的监控告警系统
# 兼容性: Windows PowerShell 5.1+, PowerShell Core 7+
# 作者: IOE-DREAM DevOps团队
# 版本: v1.0.0
# 日期: 2025-01-30
# ============================================================

param(
    [switch]$SkipKubernetes,
    [switch]$SkipElasticsearch,
    [switch]$Force,
    [switch]$Verbose
)

# 设置错误处理
$ErrorActionPreference = "Stop"

# 全局变量
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$LogFile = "$ProjectRoot\logs\monitoring-deployment-$(Get-Date -Format 'yyyyMMdd_HHmmss').log"
$MonitoringDir = "$ProjectRoot\monitoring"
$TempDir = "$env:TEMP\ioe-dream-monitoring-$([Guid]::NewGuid().ToString('N'))"

# 创建日志目录
$null = New-Item -ItemType Directory -Force -Path (Split-Path $LogFile -Parent)

# 日志函数
function Write-LogInfo {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [INFO] $Message"
    Write-Host $logMessage -ForegroundColor Green
    Add-Content -Path $LogFile -Value $logMessage -Encoding UTF8
}

function Write-LogWarn {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [WARN] $Message"
    Write-Host $logMessage -ForegroundColor Yellow
    Add-Content -Path $LogFile -Value $logMessage -Encoding UTF8
}

function Write-LogError {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [ERROR] $Message"
    Write-Host $logMessage -ForegroundColor Red
    Add-Content -Path $LogFile -Value $logMessage -Encoding UTF8
}

function Write-LogDebug {
    param([string]$Message)
    if ($Verbose) {
        $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        $logMessage = "[$timestamp] [DEBUG] $Message"
        Write-Host $logMessage -ForegroundColor Blue
        Add-Content -Path $LogFile -Value $logMessage -Encoding UTF8
    }
}

function Write-LogSuccess {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [SUCCESS] $Message"
    Write-Host $logMessage -ForegroundColor Cyan
    Add-Content -Path $LogFile -Value $logMessage -Encoding UTF8
}

# 错误处理函数
function Exit-WithError {
    param([string]$Message)
    Write-LogError $Message
    Cleanup
    exit 1
}

# 清理函数
function Cleanup {
    Write-LogDebug "清理临时文件..."
    if (Test-Path $TempDir) {
        try {
            Remove-Item -Recurse -Force $TempDir -ErrorAction SilentlyContinue
        } catch {
            Write-LogDebug "清理临时文件失败: $_"
        }
    }
}

# 信号处理
$null = Register-EngineEvent PowerShell.Exiting -Action { Cleanup }

# 检查PowerShell版本
function Test-PowerShellVersion {
    $psVersion = $PSVersionTable.PSVersion
    if ($psVersion.Major -lt 5) {
        Exit-WithError "需要PowerShell 5.1或更高版本，当前版本: $($psVersion.ToString())"
    }
    Write-LogInfo "PowerShell版本: $($psVersion.ToString())"
}

# 检查操作系统
function Get-OSType {
    if ($IsWindows) {
        return "windows"
    } elseif ($IsLinux) {
        return "linux"
    } elseif ($IsMacOS) {
        return "macos"
    } else {
        return "unknown"
    }
}

# 检查必要的工具
function Test-Prerequisites {
    Write-LogInfo "检查部署前必要条件..."

    $missingTools = @()
    $osType = Get-OSType

    # 基础工具检查
    $requiredTools = @("curl", "docker", "docker-compose")

    foreach ($tool in $requiredTools) {
        if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) {
            $missingTools += $tool
        }
    }

    # 检查jq（JSON处理工具）
    if (-not (Get-Command jq -ErrorAction SilentlyContinue)) {
        $missingTools += "jq"
    }

    if ($missingTools.Count -gt 0) {
        Write-LogError "缺少必要工具: $($missingTools -join ', ')"

        # 提供安装建议
        if ($osType -eq "windows") {
            Write-LogInfo "Windows安装建议:"
            Write-LogInfo "  1. 安装Docker Desktop for Windows"
            Write-LogInfo "     下载地址: https://www.docker.com/products/docker-desktop"
            Write-LogInfo "  2. 安装Windows Subsystem for Linux (WSL)"
            Write-LogInfo "     在PowerShell管理员中执行: wsl --install"
            Write-LogInfo "  3. 安装必要工具"
            Write-LogInfo "     在WSL中执行: sudo apt-get update && sudo apt-get install -y curl jq docker.io"
        } else {
            Write-LogInfo "Linux/macOS安装建议: 使用包管理器安装缺失的工具"
        }

        Exit-WithError "请安装必要的工具后重试"
    }

    # 检查Docker运行状态
    try {
        $null = docker info 2>&1
        Write-LogSuccess "Docker运行正常"
    } catch {
        Exit-WithError "Docker未运行，请启动Docker服务"
    }

    # 检查Docker Compose版本
    try {
        $composeVersion = docker-compose --version 2>&1
        if ($composeVersion -match 'v(\d+)\.(\d+)\.(\d+)') {
            $majorVersion = [int]$matches[1]
            if ($majorVersion -lt 2) {
                Exit-WithError "需要Docker Compose 2.0或更高版本，当前版本: $composeVersion"
            }
        }
        Write-LogSuccess "Docker Compose版本: $composeVersion"
    } catch {
        Exit-WithError "无法检查Docker Compose版本"
    }

    Write-LogSuccess "所有必要工具检查通过 ✓"
}

# 创建监控配置目录结构
function New-MonitoringStructure {
    Write-LogInfo "创建监控系统配置目录结构..."

    $directories = @(
        "$MonitoringDir\prometheus",
        "$MonitoringDir\prometheus\rules",
        "$MonitoringDir\prometheus\consoles",
        "$MonitoringDir\prometheus\console_libraries",
        "$MonitoringDir\grafana",
        "$MonitoringDir\grafana\provisioning",
        "$MonitoringDir\grafana\provisioning\datasources",
        "$MonitoringDir\grafana\provisioning\dashboards",
        "$MonitoringDir\grafana\dashboards",
        "$MonitoringDir\alertmanager",
        "$MonitoringDir\elasticsearch",
        "$MonitoringDir\filebeat",
        "$MonitoringDir\k8s"
    )

    foreach ($dir in $directories) {
        $null = New-Item -ItemType Directory -Force -Path $dir
    }

    # 创建日志目录
    $null = New-Item -ItemType Directory -Force -Path "$ProjectRoot\logs"

    Write-LogSuccess "监控系统配置目录结构创建完成 ✓"
}

# 生成Prometheus配置
function New-PrometheusConfig {
    Write-LogInfo "生成Prometheus配置..."

    $prometheusConfig = @"
# ============================================================
# IOE-DREAM Prometheus配置
# ============================================================

global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'ioe-dream'
    environment: 'production'

rule_files:
  - "/etc/prometheus/rules/*.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - 'alertmanager:9093'

scrape_configs:
  # Prometheus自监控
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # Gateway服务监控
  - job_name: 'ioedream-gateway'
    static_configs:
      - targets: ['gateway-service:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    scrape_timeout: 5s

  # Common服务监控
  - job_name: 'ioedream-common'
    static_configs:
      - targets: ['common-service:8088']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  # Consume服务监控
  - job_name: 'ioedream-consume'
    static_configs:
      - targets: ['consume-service:8094']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  # Access服务监控
  - job_name: 'ioedream-access'
    static_configs:
      - targets: ['access-service:8090']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  # Attendance服务监控
  - job_name: 'ioedream-attendance'
    static_configs:
      - targets: ['attendance-service:8091']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  # Video服务监控
  - job_name: 'ioedream-video'
    static_configs:
      - targets: ['video-service:8092']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  # Visitor服务监控
  - job_name: 'ioedream-visitor'
    static_configs:
      - targets: ['visitor-service:8095']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  # Device通讯服务监控
  - job_name: 'ioedream-device-comm'
    static_configs:
      - targets: ['device-comm-service:8087']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  # OA服务监控
  - job_name: 'ioedream-oa'
    static_configs:
      - targets: ['oa-service:8089']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  # Node Exporter
  - job_name: 'node-exporter'
    static_configs:
      - targets:
        - 'node-exporter:9100'
    scrape_interval: 30s

  # MySQL Exporter
  - job_name: 'mysql-exporter'
    static_configs:
      - targets: ['mysql-exporter:9104']
    scrape_interval: 30s

  # Redis Exporter
  - job_name: 'redis-exporter'
    static_configs:
      - targets: ['redis-exporter:9121']
    scrape_interval: 30s

# 存储配置
storage:
  tsdb:
    retention.time: 30d
    retention.size: 10GB
"@

    Set-Content -Path "$MonitoringDir\prometheus\prometheus.yml" -Value $prometheusConfig -Encoding UTF8

    Write-LogSuccess "Prometheus配置生成完成 ✓"
}

# 生成Prometheus告警规则
function New-PrometheusAlertRules {
    Write-LogInfo "生成Prometheus告警规则..."

    # 基础设施告警规则
    $infrastructureRules = @"
# 基础设施告警规则

groups:
  - name: infrastructure
    rules:
      # CPU告警
      - alert: HighCPUUsage
        expr: 100 - (avg by(instance) (irate(node_cpu_seconds_total{mode=""idle""}[5m])) * 100) > 80
        for: 5m
        labels:
          severity: warning
          category: infrastructure
        annotations:
          summary: "服务器CPU使用率过高"
          description: "实例 {{ `$labels.instance` }} CPU使用率 {{ `$value` }}%"

      - alert: CriticalCPUUsage
        expr: 100 - (avg by(instance) (irate(node_cpu_seconds_total{mode=""idle""}[5m])) * 100) > 90
        for: 2m
        labels:
          severity: critical
          category: infrastructure
        annotations:
          summary: "服务器CPU使用率危险"
          description: "实例 {{ `$labels.instance` }} CPU使用率 {{ `$value` }}%"

      # 内存告警
      - alert: HighMemoryUsage
        expr: (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100 > 85
        for: 5m
        labels:
          severity: warning
          category: infrastructure
        annotations:
          summary: "服务器内存使用率过高"
          description: "实例 {{ `$labels.instance` }} 内存使用率 {{ `$value` }}%"

      - alert: CriticalMemoryUsage
        expr: (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100 > 95
        for: 2m
        labels:
          severity: critical
          category: infrastructure
        annotations:
          summary: "服务器内存使用率危险"
          description: "实例 {{ `$labels.instance` }} 内存使用率 {{ `$value` }}%"

      # 磁盘告警
      - alert: HighDiskUsage
        expr: (1 - (node_filesystem_avail_bytes / node_filesystem_size_bytes)) * 100 > 85
        for: 10m
        labels:
          severity: warning
          category: infrastructure
        annotations:
          summary: "磁盘空间不足"
          description: "实例 {{ `$labels.instance` }} 磁盘 {{ `$labels.mountpoint` }} 使用率 {{ `$value` }}%"

      - alert: CriticalDiskUsage
        expr: (1 - (node_filesystem_avail_bytes / node_filesystem_size_bytes)) * 100 > 95
        for: 5m
        labels:
          severity: critical
          category: infrastructure
        annotations:
          summary: "磁盘空间严重不足"
          description: "实例 {{ `$labels.instance` }} 磁盘 {{ `$labels.mountpoint` }} 使用率 {{ `$value` }}%"
"@

    Set-Content -Path "$MonitoringDir\prometheus\rules\infrastructure.yml" -Value $infrastructureRules -Encoding UTF8

    # 应用监控告警规则
    $applicationRules = @"
# 应用监控告警规则

groups:
  - name: application
    rules:
      # 服务下线告警
      - alert: ServiceDown
        expr: up == 0
        for: 30s
        labels:
          severity: critical
          category: application
        annotations:
          summary: "服务下线"
          description: "服务 {{ `$labels.job` }} 在实例 {{ `$labels.instance` }} 上已下线"

      # 响应时间告警
      - alert: SlowResponseTime
        expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
          category: application
        annotations:
          summary: "API响应时间过慢"
          description: "API P95响应时间 {{ `$value` }}秒"

      - alert: CriticalResponseTime
        expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 3
        for: 2m
        labels:
          severity: critical
          category: application
        annotations:
          summary: "API响应时间危险"
          description: "API P95响应时间 {{ `$value` }}秒"

      # 错误率告警
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~""5..""}[5m]) / rate(http_requests_total[5m]) > 0.05
        for: 3m
        labels:
          severity: warning
          category: application
        annotations:
          summary: "应用错误率过高"
          description: "服务 {{ `$labels.job` }} 错误率 {{ `$value | humanizePercentage` }}"

      - alert: CriticalErrorRate
        expr: rate(http_requests_total{status=~""5..""}[5m]) / rate(http_requests_total[5m]) > 0.1
        for: 1m
        labels:
          severity: critical
          category: application
        annotations:
          summary: "应用错误率危险"
          description: "服务 {{ `$labels.job` }} 错误率 {{ `$value | humanizePercentage` }}"

      # JVM内存告警
      - alert: HighJVMHeapUsage
        expr: (jvm_memory_used_bytes{area=""heap""} / jvm_memory_max_bytes{area=""heap""}) * 100 > 80
        for: 5m
        labels:
          severity: warning
          category: application
        annotations:
          summary: "JVM堆内存使用率过高"
          description: "实例 {{ `$labels.instance` }} JVM堆内存使用率 {{ `$value` }}%"

      - alert: CriticalJVMHeapUsage
        expr: (jvm_memory_used_bytes{area=""heap""} / jvm_memory_max_bytes{area=""heap""}) * 100 > 90
        for: 2m
        labels:
          severity: critical
          category: application
        annotations:
          summary: "JVM堆内存使用率危险"
          description: "实例 {{ `$labels.instance` }} JVM堆内存使用率 {{ `$value` }}%"
"@

    Set-Content -Path "$MonitoringDir\prometheus\rules\application.yml" -Value $applicationRules -Encoding UTF8

    Write-LogSuccess "Prometheus告警规则生成完成 ✓"
}

# 生成AlertManager配置
function New-AlertManagerConfig {
    Write-LogInfo "生成AlertManager配置..."

    $alertManagerConfig = @"
# ============================================================
# IOE-DREAM AlertManager配置
# ============================================================

global:
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'alerts@ioe-dream.com'
  smtp_auth_username: 'alerts@ioe-dream.com'
  smtp_auth_password: '`${SMTP_PASSWORD}`'
  resolve_timeout: 5m

route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'
  routes:
    # 严重告警路由
    - match:
        severity: critical
      receiver: 'critical-alerts'
      group_wait: 0s
      repeat_interval: 5m

    # 警告告警路由
    - match:
        severity: warning
      receiver: 'warning-alerts'
      group_wait: 30s
      repeat_interval: 30m

    # 业务告警路由
    - match:
        category: business
      receiver: 'business-alerts'
      group_wait: 10s
      repeat_interval: 15m

receivers:
  # Webhook接收器 (默认)
  - name: 'web.hook'
    webhook_configs:
      - url: 'http://dingtalk-webhook:8060/dingtalk'
        send_resolved: true
        http_config:
          bearer_token: '`${DINGTALK_TOKEN}`'

  # 严重告警接收器
  - name: 'critical-alerts'
    email_configs:
      - to: 'ops-team@ioe-dream.com,management@ioe-dream.com'
        subject: '【严重告警】{{ .GroupLabels.alertname }}'
        body: |
          告警组: {{ .GroupLabels.alertname }}
          严重程度: {{ .GroupLabels.severity }}
          分类: {{ .GroupLabels.category }}

          {{ range .Alerts }}
          告警: {{ .Annotations.summary }}
          描述: {{ .Annotations.description }}
          实例: {{ .Labels.instance }}
          服务: {{ .Labels.job }}
          时间: {{ .StartsAt }}
          {{ end }}
    webhook_configs:
      - url: 'http://dingtalk-webhook:8060/dingtalk'
        send_resolved: true
        http_config:
          bearer_token: '`${DINGTALK_TOKEN}`'

  # 警告接收器
  - name: 'warning-alerts'
    email_configs:
      - to: 'dev-team@ioe-dream.com'
        subject: '【警告】{{ .GroupLabels.alertname }}'
        body: |
          告警组: {{ .GroupLabels.alertname }}
          严重程度: {{ .GroupLabels.severity }}
          分类: {{ .GroupLabels.category }}

          {{ range .Alerts }}
          告警: {{ .Annotations.summary }}
          描述: {{ .Annotations.description }}
          实例: {{ .Labels.instance }}
          服务: {{ .Labels.job }}
          时间: {{ .StartsAt }}
          {{ end }}

  # 业务告警接收器
  - name: 'business-alerts'
    email_configs:
      - to: 'business-team@ioe-dream.com'
        subject: '【业务告警】{{ .GroupLabels.alertname }}'
        body: |
          告警组: {{ .GroupLabels.alertname }}
          严重程度: {{ .GroupLabels.severity }}
          业务影响: {{ .GroupLabels.business_impact }}

          {{ range .Alerts }}
          告警: {{ .Annotations.summary }}
          业务描述: {{ .Annotations.business_description }}
          时间: {{ .StartsAt }}
          {{ end }}

inhibit_rules:
  # 严重告警抑制警告告警
  - source_match:
      severity: 'critical'
    target_match:
      severity: 'warning'
    equal: ['alertname', 'instance']

  # 业务告警抑制技术告警
  - source_match:
      category: 'business'
    target_match:
      category: 'infrastructure'
    equal: ['instance']
"@

    Set-Content -Path "$MonitoringDir\alertmanager\alertmanager.yml" -Value $alertManagerConfig -Encoding UTF8

    Write-LogSuccess "AlertManager配置生成完成 ✓"
}

# 生成Grafana数据源配置
function New-GrafanaDatasourceConfig {
    Write-LogInfo "生成Grafana数据源配置..."

    # 创建数据源配置目录
    $null = New-Item -ItemType Directory -Force -Path "$MonitoringDir\grafana\provisioning\datasources"

    $datasourceConfig = @"
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
    jsonData:
      timeInterval: 5s
      queryTimeout: 60s
      httpMethod: POST
"@

    Set-Content -Path "$MonitoringDir\grafana\provisioning\datasources\prometheus.yml" -Value $datasourceConfig -Encoding UTF8

    Write-LogSuccess "Grafana数据源配置生成完成 ✓"
}

# 生成Grafana仪表盘配置
function New-GrafanaDashboardConfig {
    Write-LogInfo "生成Grafana仪表盘配置..."

    # 创建仪表盘配置目录
    $null = New-Item -ItemType Directory -Force -Path "$MonitoringDir\grafana\provisioning\dashboards"

    $dashboardConfig = @"
apiVersion: 1

providers:
  - name: 'default'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /var/lib/grafana/dashboards
"@

    Set-Content -Path "$MonitoringDir\grafana\provisioning\dashboards\dashboards.yml" -Value $dashboardConfig -Encoding UTF8

    Write-LogSuccess "Grafana仪表盘配置生成完成 ✓"
}

# 生成Docker Compose配置
function New-DockerComposeConfig {
    Write-LogInfo "生成Docker Compose监控配置..."

    $dockerComposeConfig = @"
# ============================================================
# IOE-DREAM 监控系统 Docker Compose配置
# ============================================================

version: '3.8'

networks:
  monitoring:
    driver: bridge
  ioe-dream:
    external: true

volumes:
  prometheus_data:
    driver: local
  grafana_data:
    driver: local
  alertmanager_data:
    driver: local
  elasticsearch_data:
    driver: local

services:
  # Prometheus
  prometheus:
    image: prom/prometheus:v2.40.0
    container_name: ioe-prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - ./monitoring/prometheus/rules:/etc/prometheus/rules:ro
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=30d'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--web.enable-lifecycle'
      - '--web.enable-admin-api'
    networks:
      - monitoring
      - ioe-dream
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:9090/-/healthy"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Grafana
  grafana:
    image: grafana/grafana:9.0.0
    container_name: ioe-grafana
    restart: unless-stopped
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=`${GRAFANA_PASSWORD:-admin123}`
      - GF_INSTALL_PLUGINS=grafana-clock-panel,grafana-simple-json-datasource,grafana-piechart-panel
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_SMTP_ENABLED=true
      - GF_SMTP_HOST=`${SMTP_HOST:-smtp.example.com:587}`
      - GF_SMTP_USER=`${SMTP_USER:-alerts@ioe-dream.com}`
      - GF_SMTP_PASSWORD=`${SMTP_PASSWORD}`
      - GF_SMTP_FROM_ADDRESS=`${SMTP_FROM:-alerts@ioe-dream.com}`
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning:ro
      - ./monitoring/grafana/dashboards:/var/lib/grafana/dashboards:ro
    networks:
      - monitoring
      - ioe-dream
    depends_on:
      - prometheus
    healthcheck:
      test: ["CMD-SHELL", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:3000/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # AlertManager
  alertmanager:
    image: prom/alertmanager:v0.25.0
    container_name: ioe-alertmanager
    restart: unless-stopped
    ports:
      - "9093:9093"
    volumes:
      - ./monitoring/alertmanager.yml:/etc/alertmanager/alertmanager.yml:ro
      - alertmanager_data:/alertmanager
    command:
      - '--config.file=/etc/alertmanager/alertmanager.yml'
      - '--storage.path=/alertmanager'
      - '--web.external-url=http://alertmanager:9093'
    networks:
      - monitoring
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:9093/-/healthy"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Node Exporter
  node-exporter:
    image: prom/node-exporter:v1.3.1
    container_name: ioe-node-exporter
    restart: unless-stopped
    ports:
      - "9100:9100"
    command:
      - '--path.rootfs=/host'
    volumes:
      - '/:/host:ro,rslave'
    networks:
      - monitoring

  # MySQL Exporter
  mysql-exporter:
    image: prom/mysqld-exporter:v0.14.0
    container_name: ioe-mysql-exporter
    restart: unless-stopped
    ports:
      - "9104:9104"
    environment:
      - DATA_SOURCE_NAME=ioe-dream:mysql://mysql:3306/metrics
      - DATA_SOURCE_USER=exporter
      - DATA_SOURCE_PASSWORD=`${MYSQL_EXPORTER_PASSWORD}`
    networks:
      - monitoring
      - ioe-dream

  # Redis Exporter
  redis-exporter:
    image: oliver006/redis_exporter:v1.28.0
    container_name: ioe-redis-exporter
    restart: unless-stopped
    ports:
      - "9121:9121"
    environment:
      - REDIS_ADDR=redis://redis:6379
      - REDIS_PASSWORD=`${REDIS_PASSWORD}`
    networks:
      - monitoring
      - ioe-dream

  # DingTalk Webhook (用于告警通知)
  dingtalk-webhook:
    image: timonwong/prometheus-webhook-dingtalk:latest
    container_name: ioe-dingtalk-webhook
    restart: unless-stopped
    ports:
      - "8060:8060"
    environment:
      - DINGTALK_WEBHOOK=`${DINGTALK_WEBHOOK_URL}`
      - DINGTALK_SECRET=`${DINGTALK_SECRET}`
      - LOG_LEVEL=info
    networks:
      - monitoring
"@

    Set-Content -Path "$ProjectRoot\docker-compose.monitoring.yml" -Value $dockerComposeConfig -Encoding UTF8

    Write-LogSuccess "Docker Compose监控配置生成完成 ✓"
}

# 生成Kubernetes部署配置
function New-KubernetesConfig {
    if ($SkipKubernetes) {
        Write-LogWarn "跳过Kubernetes配置生成"
        return
    }

    Write-LogInfo "生成Kubernetes监控部署配置..."

    $namespaceConfig = @"
apiVersion: v1
kind: Namespace
metadata:
  name: monitoring
  labels:
    name: monitoring
    istio-injection: enabled
"@

    Set-Content -Path "$MonitoringDir\k8s\monitoring-namespace.yaml" -Value $namespaceConfig -Encoding UTF8

    # Prometheus StatefulSet
    $prometheusStatefulSet = @"
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: prometheus
  namespace: monitoring
  labels:
    app: prometheus
spec:
  serviceName: prometheus
  replicas: 1
  selector:
    matchLabels:
      app: prometheus
  template:
    metadata:
      labels:
        app: prometheus
    spec:
      containers:
      - name: prometheus
        image: prom/prometheus:v2.40.0
        ports:
        - containerPort: 9090
          name: web
        volumeMounts:
        - name: config-volume
          mountPath: /etc/prometheus
        - name: storage-volume
          mountPath: /prometheus
        command:
        - '--config.file=/etc/prometheus/prometheus.yml'
        - '--storage.tsdb.path=/prometheus'
        - '--web.enable-lifecycle'
        - '--storage.tsdb.retention.time=30d'
        resources:
          requests:
            cpu: 200m
            memory: 400Mi
          limits:
            cpu: 1000m
            memory: 2Gi
        livenessProbe:
          httpGet:
            path: /-/healthy
            port: 9090
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /-/ready
            port: 9090
          initialDelaySeconds: 5
          periodSeconds: 5
      volumes:
      - name: config-volume
        configMap:
          name: prometheus-config
      - name: storage-volume
        persistentVolumeClaim:
          claimName: prometheus-pvc
"@

    Set-Content -Path "$MonitoringDir\k8s\prometheus-statefulset.yaml" -Value $prometheusStatefulSet -Encoding UTF8

    # Grafana Deployment
    $grafanaDeployment = @"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  namespace: monitoring
  labels:
    app: grafana
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grafana
  template:
    metadata:
      labels:
        app: grafana
    spec:
      containers:
      - name: grafana
        image: grafana/grafana:9.0.0
        ports:
        - containerPort: 3000
        env:
        - name: GF_SECURITY_ADMIN_USER
          value: "admin"
        - name: GF_SECURITY_ADMIN_PASSWORD
          value: "admin123"
        - name: GF_USERS_ALLOW_SIGN_UP
          value: "false"
        volumeMounts:
        - name: grafana-storage
          mountPath: /var/lib/grafana
        resources:
          requests:
            cpu: 100m
            memory: 200Mi
          limits:
            cpu: 500m
            memory: 1Gi
        livenessProbe:
          httpGet:
            path: /api/health
            port: 3000
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/health
            port: 3000
          initialDelaySeconds: 5
          periodSeconds: 5
      volumes:
      - name: grafana-storage
        persistentVolumeClaim:
          claimName: grafana-pvc
"@

    Set-Content -Path "$MonitoringDir\k8s\grafana-deployment.yaml" -Value $grafanaDeployment -Encoding UTF8

    Write-LogSuccess "Kubernetes监控配置生成完成 ✓"
}

# 生成环境变量配置文件
function New-EnvFile {
    Write-LogInfo "生成环境变量配置文件..."

    $envConfig = @"
# ============================================================
# IOE-DREAM 监控系统环境变量配置
# ============================================================

# Grafana配置
GRAFANA_PASSWORD=
GRAFANA_DOMAIN=monitoring.ioe-dream.com

# SMTP邮件配置
SMTP_HOST=smtp.example.com:587
SMTP_USER=alerts@ioe-dream.com
SMTP_PASSWORD=your_smtp_password
SMTP_FROM=alerts@ioe-dream.com

# 数据库监控配置
MYSQL_EXPORTER_PASSWORD=mysql_exporter_password
REDIS_PASSWORD=redis_password

# 告警通知配置
DINGTALK_WEBHOOK_URL=https://oapi.dingtalk.com/robot/send?access_token=your_dingtalk_token
DINGTALK_SECRET=your_dingtalk_secret

# 企业微信配置
WECHAT_WEBHOOK_URL=https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=your_wechat_key

# 监控存储配置
PROMETHEUS_RETENTION=30d
ELASTICSEARCH_RETENTION=30d

# 监控阈值配置
CPU_WARNING_THRESHOLD=80
CPU_CRITICAL_THRESHOLD=90
MEMORY_WARNING_THRESHOLD=85
MEMORY_CRITICAL_THRESHOLD=95
DISK_WARNING_THRESHOLD=85
DISK_CRITICAL_THRESHOLD=95
RESPONSE_TIME_WARNING_THRESHOLD=1.0
RESPONSE_TIME_CRITICAL_THRESHOLD=3.0
ERROR_RATE_WARNING_THRESHOLD=0.05
ERROR_RATE_CRITICAL_THRESHOLD=0.1
"@

    Set-Content -Path "$ProjectRoot\.env.monitoring" -Value $envConfig -Encoding UTF8

    Write-LogSuccess "环境变量配置文件生成完成 ✓"
}

# 生成仪表盘JSON文件
function New-DashboardJson {
    Write-LogInfo "生成Grafana仪表盘JSON文件..."

    # 创建仪表盘目录
    $null = New-Item -ItemType Directory -Force -Path "$MonitoringDir\grafana\dashboards"

    # 系统概览仪表盘
    $systemDashboard = @"
{
  "dashboard": {
    "id": null,
    "title": "IOE-DREAM 系统监控概览",
    "tags": ["ioe-dream", "system", "overview"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "服务状态总览",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=~""ioe-dream-.*""}",
            "legendFormat": "{{ job }}",
            "refId": "A"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "mappings": [
              {"options": {"0": {"text": "离线", "color": "red"}}, "type": "value"},
              {"options": {"1": {"text": "在线", "color": "green"}}, "type": "value"}
            ]
          }
        },
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "系统总QPS",
        "type": "stat",
        "targets": [
          {
            "expr": "sum(rate(http_requests_total[5m]))",
            "refId": "A"
          }
        ],
        "gridPos": {"h": 8, "w": 6, "x": 12, "y": 0}
      },
      {
        "id": 3,
        "title": "平均响应时间",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.50, sum(rate(http_request_duration_seconds_bucket[5m])) by (le))",
            "legendFormat": "P50",
            "refId": "A"
          },
          {
            "expr": "histogram_quantile(0.95, sum(rate(http_request_duration_seconds_bucket[5m])) by (le))",
            "legendFormat": "P95",
            "refId": "B"
          }
        ],
        "gridPos": {"h": 9, "w": 12, "x": 0, "y": 8}
      }
    ],
    "time": {"from": "now-1h", "to": "now"},
    "refresh": "5s"
  }
}
"@

    Set-Content -Path "$MonitoringDir\grafana\dashboards\system-overview.json" -Value $systemDashboard -Encoding UTF8

    Write-LogSuccess "仪表盘JSON文件生成完成 ✓"
}

# 部署监控系统
function Deploy-MonitoringSystem {
    Write-LogInfo "开始部署监控系统..."

    # 检查环境变量文件
    if (-not (Test-Path "$ProjectRoot\.env.monitoring")) {
        Write-LogError "环境变量文件不存在：$ProjectRoot\.env.monitoring（禁止使用默认口令，请先生成并填写 GRAFANA_PASSWORD）"
        exit 1
    }

    # 设置环境变量
    if (Test-Path "$ProjectRoot\.env.monitoring") {
        Get-Content "$ProjectRoot\.env.monitoring" | ForEach-Object {
            if ($_.Trim() -and -not $_.TrimStartsWith('#')) {
                $name, $value = $_.Trim().Split('=', 2)
                if ($name -and $value) {
                    [System.Environment]::SetEnvironmentVariable($name, $value)
                }
            }
        }
        Write-LogInfo "已加载环境变量配置"
    }

    if ([string]::IsNullOrWhiteSpace($env:GRAFANA_PASSWORD)) {
        Write-LogError "缺少环境变量：GRAFANA_PASSWORD（禁止使用默认口令，请在 .env.monitoring 中显式配置）"
        exit 1
    }

    # 创建Docker网络（如果不存在）
    $networkExists = docker network inspect ioe-dream 2>$null
    if (-not $networkExists) {
        Write-LogInfo "创建Docker网络: ioe-dream"
        docker network create ioe-dream
    }

    # 部署监控系统
    Write-LogInfo "启动监控系统容器..."
    Push-Location $ProjectRoot

    try {
        # 停止现有容器（如果存在）
        $existingContainers = docker-compose -f docker-compose.monitoring.yml ps -q 2>$null
        if ($existingContainers) {
            Write-LogInfo "停止现有监控系统..."
            docker-compose -f docker-compose.monitoring.yml down
        }

        # 启动监控系统
        docker-compose -f docker-compose.monitoring.yml up -d

        # 等待服务启动
        Write-LogInfo "等待监控系统启动..."
        Start-Sleep -Seconds 30

        # 验证服务状态
        Test-Deployment

    } finally {
        Pop-Location
    }

    Write-LogSuccess "监控系统部署完成 ✓"
}

# 验证部署状态
function Test-Deployment {
    Write-LogInfo "验证监控系统部署状态..."

    $services = @("ioe-prometheus", "ioe-grafana", "ioe-alertmanager", "ioe-node-exporter")
    $ports = @("9090", "3000", "9093", "9100")
    $serviceStatus = $true

    for ($i = 0; $i -lt $services.Count; $i++) {
        $service = $services[$i]
        $port = $ports[$i]

        try {
            $containerStatus = docker ps --filter "name=$service" --format "{{.Status}}"
            if ($containerStatus -match "Up") {
                Write-LogSuccess "$service 运行正常"
            } else {
                Write-LogError "$service 未运行"
                $serviceStatus = $false
            }

            # 检查端口连通性
            try {
                $response = Invoke-WebRequest -Uri "http://localhost:$port" -TimeoutSec 5 -UseBasicParsing
                if ($response.StatusCode -eq 200) {
                    Write-LogSuccess "$service 端口 $port 可访问"
                } else {
                    Write-LogWarn "$service 端口 $port 返回状态码: $($response.StatusCode)"
                }
            } catch {
                Write-LogWarn "$service 端口 $port 不可访问"
            }

        } catch {
            Write-LogError "检查 $service 状态时出错: $_"
            $serviceStatus = $false
        }
    }

    if ($serviceStatus) {
        Write-LogSuccess "所有监控系统服务验证通过 ✓"
    } else {
        Write-LogError "部分服务验证失败，请检查日志"
        Show-DeploymentLogs
    }
}

# 显示部署日志
function Show-DeploymentLogs {
    Write-LogInfo "显示部署日志..."

    $services = @("ioe-prometheus", "ioe-grafana", "ioe-alertmanager")

    foreach ($service in $services) {
        try {
            Write-LogInfo "$service 日志:"
            $logs = docker logs $service --tail 20 2>&1
            if ($logs) {
                Write-Host $logs
            } else {
                Write-LogWarn "$service 无日志输出"
            }
            Write-Host "----------------------------------------"
        } catch {
            Write-LogError "获取 $service 日志失败: $_"
        }
    }
}

# 生成访问信息
function New-AccessInfo {
    Write-LogInfo "生成监控系统访问信息..."

    $accessInfoFile = "$ProjectRoot\monitoring-access-info.md"

    $accessInfo = @"
# IOE-DREAM 监控系统访问信息

> 部署时间: $(Get-Date)
> 访问地址: http://localhost

## 监控服务访问地址

### 📊 Grafana 监控仪表盘
- **访问地址**: http://localhost:3000
- **用户名**: admin
- **密码**: admin123
- **功能**: 监控仪表盘、可视化、告警管理

### 🔍 Prometheus 数据源
- **访问地址**: http://localhost:9090
- **功能**: 指标存储、查询、告警规则

### 🚨 AlertManager 告警管理
- **访问地址**: http://localhost:9093
- **功能**: 告警路由、通知管理、静默规则

### 📈 系统指标
- **Node Exporter**: http://localhost:9100/metrics
- **MySQL Exporter**: http://localhost:9104/metrics
- **Redis Exporter**: http://localhost:9121/metrics

## 管理命令

### 查看服务状态
```powershell
docker-compose -f docker-compose.monitoring.yml ps
```

### 查看服务日志
```powershell
docker-compose -f docker-compose.monitoring.yml logs -f [service-name]
```

### 重启服务
```powershell
docker-compose -f docker-compose.monitoring.yml restart [service-name]
```

### 停止监控系统
```powershell
docker-compose -f docker-compose.monitoring.yml down
```

### 更新配置
```powershell
docker-compose -f docker-compose.monitoring.yml up -d --force-recreate
```

## 故障排查

### 服务无法访问
1. 检查服务状态: `docker ps`
2. 查看服务日志: `docker logs [service-name]`
3. 检查端口占用: `netstat -an | grep [port]`

### 指标缺失
1. 检查Prometheus配置: http://localhost:9090/targets
2. 检查应用actuator端点
3. 查看应用服务日志

### 告警不生效
1. 检查AlertManager配置: http://localhost:9093/#/alerts
2. 检查告警规则: http://localhost:9090/alerts
3. 检查通知渠道配置

## 下一步操作

1. **导入Grafana仪表盘**: 访问Grafana并导入预设仪表盘
2. **配置告警规则**: 根据业务需求配置告警阈值
3. **集成业务指标**: 将业务系统接入监控
4. **设置通知渠道**: 配置邮件、短信、钉钉等通知

---

**联系方式**: IOE-DREAM DevOps团队
**文档更新**: $(Get-Date)
"@

    Set-Content -Path $accessInfoFile -Value $accessInfo -Encoding UTF8

    Write-LogSuccess "访问信息生成完成: $accessInfoFile"
    Write-LogInfo "请查看 $accessInfoFile 获取详细的访问和管理信息"
}

# 主函数
function Main {
    Write-LogInfo "开始执行IOE-DREAM监控系统自动化部署..."
    Write-LogInfo "================================================"

    # 显示系统信息
    Write-LogInfo "系统信息:"
    Write-LogInfo "  操作系统: $(Get-OSType)"
    Write-LogInfo "  PowerShell版本: $($PSVersionTable.PSVersion.ToString())"
    Write-LogInfo "  项目路径: $ProjectRoot"
    Write-LogInfo "  脚本路径: $ScriptDir"
    Write-LogInfo "  监控目录: $MonitoringDir"
    Write-LogInfo "  日志文件: $LogFile"

    # 创建临时目录
    $null = New-Item -ItemType Directory -Force -Path $TempDir

    # 执行部署步骤
    Test-PowerShellVersion
    Test-Prerequisites
    New-MonitoringStructure
    New-PrometheusConfig
    New-PrometheusAlertRules
    New-AlertManagerConfig
    New-GrafanaDatasourceConfig
    New-GrafanaDashboardConfig
    New-DockerComposeConfig
    New-KubernetesConfig
    New-EnvFile
    New-DashboardJson

    # 部署监控系统
    Deploy-MonitoringSystem

    # 生成访问信息
    New-AccessInfo

    Write-LogInfo "================================================"
    Write-LogSuccess "IOE-DREAM监控系统自动化部署完成！"
    Write-LogInfo ""
    Write-LogInfo "部署结果:"
    Write-LogInfo "  - Prometheus: http://localhost:9090"
    Write-LogInfo "  - Grafana: http://localhost:3000 (admin/(已隐藏，使用 GRAFANA_PASSWORD))"
    Write-LogInfo "  - AlertManager: http://localhost:9093"
    Write-LogInfo "  - Node Exporter: http://localhost:9100"
    Write-LogInfo ""
    Write-LogInfo "下一步操作:"
    Write-LogInfo "  1. 访问Grafana配置监控仪表盘"
    Write-LogInfo "  2. 验证告警规则和通知配置"
    Write-LogInfo "  3. 根据业务需求调整监控指标"
    Write-LogInfo "  4. 集成业务系统监控数据"
    Write-LogInfo ""
    Write-LogInfo "管理命令:"
    Write-LogInfo "  - 查看状态: docker-compose -f docker-compose.monitoring.yml ps"
    Write-LogInfo "  - 查看日志: docker-compose -f docker-compose.monitoring.yml logs -f"
    Write-LogInfo "  - 停止服务: docker-compose -f docker-compose.monitoring.yml down"
    Write-LogInfo "  - 重启服务: docker-compose -f docker-compose.monitoring.yml restart"
    Write-LogInfo ""
    Write-LogInfo "访问详情: $ProjectRoot\monitoring-access-info.md"
    Write-LogInfo "  完整日志: $LogFile"

    Write-LogSuccess "监控系统部署脚本执行结束 ✓"
}

# 检查是否直接执行此脚本
if ($MyInvocation.InvocationName -eq $MyInvocation.MyCommand.Name) {
    Main
}
