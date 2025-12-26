# IOE-DREAM 考勤管理服务 - 监控告警指南

## 📋 监控体系概述

**监控目标**: 确保7x24小时系统稳定运行，及时发现问题并快速响应
**监控架构**: Prometheus + Grafana + AlertManager + ELK Stack
**覆盖范围**: 应用监控、基础设施监控、业务监控、安全监控

---

## 🏗️ 监控架构

### 监控组件架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   应用服务      │    │   监控采集      │    │   数据存储      │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │ Spring Boot │ │───►│ │ Prometheus  │ │───►│ │ InfluxDB    │ │
│ │ Actuator   │ │    │ │   +          │ │    │ │   + TSDB    │ │
│ │ Micrometer  │ │    │ │   Node      │ │    │ │   Grafana   │ │
│ └─────────────┘ │    │ │   Exporter  │ │    │ │   + Loki     │ │
│                 │    │ └─────────────┘ │    │ └─────────────┘ │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                           │                           │
         │                           │                           │
         ▼                           ▼                           ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   告警通知       │    │   日志收集       │    │   可视化面板     │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │AlertManager│ │    │ │ ELK Stack    │ │    │ │ Grafana     │ │
│ │   + Slack  │ │    │ │Elasticsearch│ │    │ │Dashboard   │ │
│ │   + Email  │ │    │ │Logstash     │ │    │ │     +       │ │
│ │   + WeChat  │ │    │ │Kibana       │ │    │ │Alert Panel │ │
│ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## 📊 核心监控指标

### 1. 应用性能指标 (APM)

#### JVM监控指标

| 指标名称 | 说明 | 告警阈值 | 严重等级 |
|---------|------|----------|----------|
| `jvm_memory_used_bytes` | JVM内存使用量 | > 85% | 🔴 Critical |
| `jvm_memory_max_bytes` | JVM最大内存 | - | - |
| `jvm_gc_pause_seconds_count` | GC次数 | > 10/min | 🟡 Warning |
| `jvm_gc_pause_seconds_sum` | GC总耗时 | > 1000ms | 🟡 Warning |
| `jvm_threads_live_threads` | 活跃线程数 | > 80% | 🟡 Warning |
| `jvm_threads_daemon_threads` | 守护线程数 | - | - |
| `jvm_cpu_usage` | CPU使用率 | > 80% | 🟡 Warning |

**Prometheus查询示例**:

```promql
# JVM内存使用率
(jvm_memory_used_bytes{job="attendance-service"} / jvm_memory_max_bytes{job="attendance-service"}) * 100

# GC频率
rate(jvm_gc_pause_seconds_count{job="attendance-service"}[5m])

# CPU使用率
rate(process_cpu_seconds_total{job="attendance-service"}[5m]) * 100
```

#### HTTP请求指标

| 指标名称 | 说明 | 告警阈值 | 严重等级 |
|---------|------|----------|----------|
| `http_requests_total` | HTTP请求总数 | - | - |
| `http_request_duration_seconds` | 请求响应时间 | P95 > 1s | 🟡 Warning |
| `http_requests_success_rate` | 请求成功率 | < 99% | 🟡 Warning |
| `http_requests_error_rate` | 5xx错误率 | > 1% | 🔴 Critical |
| `http_requests_4xx_rate` | 4xx错误率 | > 5% | 🟡 Warning |

**Prometheus查询示例**:

```promql
# 请求QPS
rate(http_requests_total{job="attendance-service"}[5m])

# 响应时间P95
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket{job="attendance-service"}[5m]))

# 错误率
rate(http_requests_total{job="attendance-service",status=~"5.."}[5m]) / rate(http_requests_total{job="attendance-service"}[5m])
```

### 2. 业务监控指标

#### 考勤业务指标

| 指标名称 | 说明 | 告警阈值 | 严重等级 |
|---------|------|----------|----------|
| `attendance_clock_in_rate` | 打卡速率 | < 1/min | 🟡 Warning |
| `attendance_verification_success_rate` | 生物识别成功率 | < 95% | 🟡 Warning |
| `attendance_location_valid_rate` | 位置验证成功率 | < 98% | 🟡 Warning |
| `attendance_processing_duration` | 考勤处理耗时 | > 2s | 🟡 Warning |
| `attendance_concurrent_users` | 并发用户数 | > 1000 | 🟡 Warning |

**Prometheus查询示例**:

```promql
# 打卡速率
rate(attendance_clock_in_total{job="attendance-service"}[5m])

# 生物识别成功率
rate(attendance_biometric_success_total{job="attendance-service"}[5m]) / rate(attendance_biometric_total{job="attendance-service"}[5m])

# 平均处理时间
rate(attendance_processing_duration_seconds_sum{job="attendance-service"}[5m]) / rate(attendance_processing_duration_seconds_count{job="attendance-service"}[5m])
```

#### 实时计算指标

| 指标名称 | 说明 | 告警阈值 | 严重等级 |
|---------|------|----------|----------|
| `realtime_event_processing_rate` | 事件处理速率 | < 50/min | 🟡 Warning |
| `realtime_event_success_rate` | 事件处理成功率 | < 99% | 🟡 Warning |
| `realtime_rule_execution_duration` | 规则执行耗时 | > 1s | 🟡 Warning |
| `realtime_queue_size` | 事件队列大小 | > 1000 | 🔴 Critical |

### 3. 基础设施监控指标

#### 数据库监控 (MySQL)

| 指标名称 | 说明 | 告警阈值 | 严重等级 |
|---------|------|----------|----------|
| `mysql_connections_active` | 活跃连接数 | > 80% | 🟡 Warning |
| `mysql_queries_duration` | 查询响应时间 | > 500ms | 🟡 Warning |
| `mysql_queries_slow` | 慢查询数 | > 10/min | 🟡 Warning |
| `mysql_buffer_pool_usage` | 缓冲池使用率 | > 90% | 🟡 Warning |
| `mysql_disk_usage` | 磁盘使用率 | > 80% | 🟡 Warning |

#### 缓存监控 (Redis)

| 指标名称 | 说明 | 告警阈值 | 严重等级 |
|---------|------|----------|----------|
| `redis_memory_usage` | 内存使用率 | > 80% | 🟡 Warning |
| `redis_hit_rate` | 缓存命中率 | < 85% | 🟡 Warning |
| `redis_connections_active` | 活跃连接数 | > 80% | 🟡 Warning |
| `redis_keyspace_hits` | 键空间命中数 | - | - |
| `redis_evicted_keys` | 淘汰键数 | > 100/min | 🟡 Warning |

#### 消息队列监控 (RabbitMQ)

| 指标名称 | 说明 | 告警阈值 | 严重等级 |
|---------|------|----------|----------|
| `rabbitmq_queue_messages` | 队列消息数 | > 1000 | 🟡 Warning |
| `rabbitmq_connections_active` | 活跃连接数 | > 80% | 🟡 Warning |
| `rabbitmq_message_publish_rate` | 消息发布速率 | < 10/min | 🟡 Warning |
| `rabbitmq_message_consume_rate` | 消息消费速率 | < 10/min | 🟡 Warning |

---

## 🚨 告警规则配置

### 1. 应用告警规则

#### 服务可用性告警

```yaml
groups:
- name: attendance-service.rules
  rules:
  # 服务不可用告警
  - alert: AttendanceServiceDown
    expr: up{job="attendance-service"} == 0
    for: 1m
    labels:
      severity: critical
      service: attendance-service
    annotations:
      summary: "考勤服务不可用"
      description: "考勤服务已停止运行超过1分钟"
      runbook_url: "https://docs.ioedream.com/runbook/attendance-service-down"

  # 内存使用率过高
  - alert: HighMemoryUsage
    expr: (jvm_memory_used_bytes{job="attendance-service"} / jvm_memory_max_bytes{job="attendance-service"}) * 100 > 85
    for: 5m
    labels:
      severity: warning
      service: attendance-service
    annotations:
      summary: "内存使用率过高"
      description: "考勤服务内存使用率超过85%持续5分钟"

  # CPU使用率过高
  - alert: HighCPUUsage
    expr: rate(process_cpu_seconds_total{job="attendance-service"}[5m]) * 100 > 80
    for: 5m
    labels:
      severity: warning
      service: attendance-service
    annotations:
      summary: "CPU使用率过高"
      description: "考勤服务CPU使用率超过80%持续5分钟"

  # GC频率过高
  - alert: HighGCRate
    expr: rate(jvm_gc_pause_seconds_count{job="attendance-service"}[5m]) > 10
    for: 2m
    labels:
      severity: warning
      service: attendance-service
    annotations:
      summary: "GC频率过高"
      description: "考勤服务GC频率超过10次/分钟"

  # 响应时间过长
  - alert: HighResponseTime
    expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket{job="attendance-service"}[5m])) > 1
    for: 5m
    labels:
      severity: warning
      service: attendance-service
    annotations:
      summary: "响应时间过长"
      description: "考勤服务95%请求响应时间超过1秒持续5分钟"

  # 错误率过高
  - alert: HighErrorRate
    expr: rate(http_requests_total{job="attendance-service",status=~"5.."}[5m]) / rate(http_requests_total{job="attendance-service"}[5m]) > 0.01
    for: 2m
    labels:
      severity: critical
      service: attendance-service
    annotations:
      summary: "5xx错误率过高"
      description: "考勤服务5xx错误率超过1%持续2分钟"
```

#### 业务指标告警

```yaml
groups:
- name: attendance-business.rules
  rules:
  # 打卡成功率过低
  - alert: LowClockInSuccessRate
    expr: rate(attendance_clock_in_success_total{job="attendance-service"}[5m]) / rate(attendance_clock_in_total{job="attendance-service"}[5m]) < 0.95
    for: 3m
    labels:
      severity: warning
      service: attendance-service
    annotations:
      summary: "打卡成功率过低"
      description: "考勤服务打卡成功率低于95%持续3分钟"

  # 生物识别成功率过低
  - alert: LowBiometricSuccessRate
    expr: rate(attendance_biometric_success_total{job="attendance-service"}[5m]) / rate(attendance_biometric_total{job="attendance-service"}[5m]) < 0.95
    for: 3m
    labels:
      severity: warning
      service: attendance-service
    annotations:
      summary: "生物识别成功率过低"
      description: "生物识别成功率低于95%持续3分钟"

  # 位置验证成功率过低
  - alert: LowLocationValidationRate
    expr: rate(attendance_location_valid_total{job="attendance-service"}[5m]) / rate(attendance_location_total{job="attendance-service"}[5m]) < 0.98
    for: 3m
    labels:
      severity: warning
      service: attendance-service
    annotations:
      summary: "位置验证成功率过低"
      description: "位置验证成功率低于98%持续3分钟"

  # 实时处理延迟过高
  - alert: HighRealtimeProcessingLatency
    expr: rate(realtime_event_processing_duration_seconds_sum{job="attendance-service"}[5m]) / rate(realtime_event_processing_duration_seconds_count{job="attendance-service"}[5m]) > 2
    for: 3m
    labels:
      severity: warning
      service: attendance-service
    annotations:
      summary: "实时处理延迟过高"
      description: "实时事件处理平均延迟超过2秒持续3分钟"
```

### 2. 基础设施告警规则

#### 数据库告警

```yaml
groups:
- name: database.rules
  rules:
  # 数据库连接池使用率过高（Druid连接池）
  - alert: HighDatabaseConnectionUsage
    expr: (druid_connection_pool_active_count{instance="mysql"} / druid_connection_pool_max_active{instance="mysql"}) * 100 > 80
    for: 2m
    labels:
      severity: warning
      service: mysql
    annotations:
      summary: "数据库连接池使用率过高"
      description: "MySQL连接池使用率超过80%"

  # 慢查询过多
  - alert: TooManySlowQueries
    expr: rate(mysql_slow_queries_total{instance="mysql"}[5m]) > 10
    for: 1m
    labels:
      severity: warning
      service: mysql
    annotations:
      summary: "慢查询过多"
      description: "MySQL慢查询数量超过10个/分钟"

  # 数据库磁盘空间不足
  - alert: DatabaseDiskSpaceLow
    expr: (mysql_table_size_bytes{instance="mysql"} / mysql_data_dir_size_bytes{instance="mysql"}) * 100 > 80
    for: 5m
    labels:
      severity: critical
      service: mysql
    annotations:
      summary: "数据库磁盘空间不足"
      description: "MySQL数据目录使用率超过80%"
```

#### 缓存告警

```yaml
groups:
- name: cache.rules
  rules:
  # Redis内存使用率过高
  - alert: HighRedisMemoryUsage
    expr: (redis_memory_used_bytes{instance="redis"} / redis_memory_max_bytes{instance="redis"}) * 100 > 80
    for: 5m
    labels:
      severity: warning
      service: redis
    annotations:
      summary: "Redis内存使用率过高"
      description: "Redis内存使用率超过80%"

  # Redis缓存命中率过低
  - alert: LowRedisHitRate
    expr: rate(redis_keyspace_hits_total{instance="redis"}[5m]) / (rate(redis_keyspace_hits_total{instance="redis"}[5m]) + rate(redis_keyspace_misses_total{instance="redis"}[5m])) < 0.85
    for: 5m
    labels:
      severity: warning
      service: redis
    annotations:
      summary: "Redis缓存命中率过低"
      description: "Redis缓存命中率低于85%"

  # Redis连接数过多
  - alert: TooManyRedisConnections
    expr: redis_connected_clients{instance="redis"} > 80
    for: 2m
    labels:
      severity: warning
      service: redis
    annotations:
      summary: "Redis连接数过多"
      description: "Redis连接数超过80"
```

---

## 📱 通知渠道配置

### 1. 邮件通知

#### SMTP配置

```yaml
# alertmanager.yml
global:
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'alerts@ioedream.com'
  smtp_auth_username: 'alerts@ioedream.com'
  smtp_auth_password: 'your_email_password'
  smtp_require_tls: true

route:
  group_by: ['alertname', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  receiver: 'email-alerts'

receivers:
- name: 'email-alerts'
  email_configs:
  - to: 'ops-team@ioedream.com'
    cc: 'dev-team@ioedream.com'
    subject: '🚨 [{{ .Labels.severity | toUpper }}] IOE-DREAM 考勤服务告警'
    body: |
      告警名称: {{ .GroupLabels.alertname }}
      告警级别: {{ .Labels.severity }}
      服务名称: {{ .Labels.service }}
      告警描述: {{ range .Alerts }}{{ .Annotations.description }}{{ end }}
      开始时间: {{ .StartsAt }}
      结束时间: {{ .EndsAt }}

      查看详情: {{ .GeneratorURL }}
      运维手册: https://docs.ioedream.com/runbook/{{ .Labels.service }}-{{ .GroupLabels.alertname | lower }}
    headers:
      X-Priority: '1'
      X-Auto-Submitted: 'auto-generated'
```

### 2. Slack通知

#### Webhook配置

```yaml
receivers:
- name: 'slack-alerts'
  slack_configs:
  - api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
    channel: '#attendance-alerts'
    title: 'IOE-DREAM 考勤服务告警'
    text: |
      {{ range .Alerts }}
      *告警名称*: {{ .Annotations.summary }}
      *告警级别*: {{ .Labels.severity }}
      *服务名称*: {{ .Labels.service }}
      *告警描述*: {{ .Annotations.description }}
      *开始时间*: {{ .StartsAt }}
      {{ end }}
    actions:
    - type: button
      text: '查看详情'
      url: '{{ .GeneratorURL }}'
    - type: button
      text: '运维手册'
      url: 'https://docs.ioedream.com/runbook/{{ .Labels.service }}-{{ .GroupLabels.alertname | lower }}'
```

### 3. 企业微信通知

#### Webhook配置

```yaml
receivers:
- name: 'wechat-alerts'
  webhook_configs:
  - url: 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=YOUR_WEBHOOK_KEY'
    send_resolved: true
    title: 'IOE-DREAM 考勤服务告警'
    message: |
      {{ if .Alerts.Fired }}
      🚨 **告警通知**
      {{ else }}
      ✅ **告警恢复**
      {{ end }}

      **服务**: {{ .GroupLabels.service }}
      **级别**: {{ .Labels.severity }}
      **告警**: {{ .GroupLabels.alertname }}

      {{ range .Alerts }}
      **描述**: {{ .Annotations.description }}
      **时间**: {{ .StartsAt }}
      {{ end }}

      **查看**: {{ .GeneratorURL }}
```

### 4. 钉钉通知

#### Webhook配置

```yaml
receivers:
- name: 'dingtalk-alerts'
  webhook_configs:
  - url: 'https://oapi.dingtalk.com/robot/send?access_token=YOUR_ACCESS_TOKEN'
    message: |
      {
        "msgtype": "markdown",
        "markdown": {
          "title": "IOE-DREAM 考勤服务告警",
          "text": {{ range .Alerts }}# 🚨 告警通知\n\n**服务**: {{ .Labels.service }}\n**级别**: {{ .Labels.severity }}\n**告警**: {{ .Annotations.summary }}\n\n**描述**: {{ .Annotations.description }}\n**时间**: {{ .StartsAt }}\n\n[查看详情]({{ .GeneratorURL }})\n[运维手册](https://docs.ioedream.com/runbook/{{ .Labels.service }}-{{ .GroupLabels.alertname | lower }}){{ end }}"
        }
      }
```

---

## 📊 Grafana仪表板

### 1. 应用监控仪表板

#### 主仪表板配置

```json
{
  "dashboard": {
    "title": "IOE-DREAM 考勤服务监控",
    "uid": "attendance-main",
    "tags": ["attendance", "application"],
    "timezone": "browser",
    "panels": [
      {
        "title": "服务健康状态",
        "type": "stat",
        "gridPos": {"h": 8, "w": 6, "x": 0, "y": 0},
        "targets": [
          {
            "expr": "up{job=\"attendance-service\"}",
            "legendFormat": "服务状态"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "mappings": [
              {
                "options": {
                  "1": {
                    "text": "正常",
                    "color": "green"
                  },
                  "0": {
                    "text": "异常",
                    "color": "red"
                  }
                },
                "type": "value"
              }
            ]
          }
        }
      },
      {
        "title": "请求QPS",
        "type": "graph",
        "gridPos": {"h": 8, "w": 12, "x": 6, "y": 0},
        "targets": [
          {
            "expr": "rate(http_requests_total{job=\"attendance-service\"}[5m])",
            "legendFormat": "QPS"
          }
        ],
        "yAxes": [
          {
            "label": "QPS",
            "min": 0
          }
        ]
      },
      {
        "title": "响应时间分布",
        "type": "graph",
        "gridPos": {"h": 8, "w": 12, "x": 18, "y": 0},
        "targets": [
          {
            "expr": "histogram_quantile(0.50, rate(http_request_duration_seconds_bucket{job=\"attendance-service\"}[5m]))",
            "legendFormat": "P50"
          },
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket{job=\"attendance-service\"}[5m]))",
            "legendFormat": "P95"
          },
          {
            "expr": "histogram_quantile(0.99, rate(http_request_duration_seconds_bucket{job=\"attendance-service\"}[5m]))",
            "legendFormat": "P99"
          }
        ],
        "yAxes": [
          {
            "label": "响应时间 (ms)",
            "min": 0
          }
        ]
      },
      {
        "title": "错误率",
        "type": "graph",
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 8},
        "targets": [
          {
            "expr": "rate(http_requests_total{job=\"attendance-service\",status=~\"4..\"}[5m])",
            "legendFormat": "4xx错误率"
          },
          {
            "expr": "rate(http_requests_total{job=\"attendance-service\",status=~\"5..\"}[5m])",
            "legendFormat": "5xx错误率"
          }
        ],
        "yAxes": [
          {
            "label": "错误率 (%)",
            "min": 0,
            "max": 100,
            "unit": "percent"
          }
        ]
      },
      {
        "title": "JVM内存使用",
        "type": "graph",
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 8},
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{job=\"attendance-service\",area=\"heap\"}",
            "legendFormat": "堆内存使用"
          },
          {
            "expr": "jvm_memory_max_bytes{job=\"attendance-service\",area=\"heap\"}",
            "legendFormat": "堆内存最大"
          }
        ],
        "yAxes": [
          {
            "label": "内存 (MB)",
            "min": 0,
            "unit": "bytes"
          }
        ]
      },
      {
        "title": "CPU使用率",
        "type": "graph",
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 16},
        "targets": [
          {
            "expr": "rate(process_cpu_seconds_total{job=\"attendance-service\"}[5m]) * 100",
            "legendFormat": "CPU使用率"
          }
        ],
        "yAxes": [
          {
            "label": "CPU使用率 (%)",
            "min": 0,
            "max": 100,
            "unit": "percent"
          }
        ]
      },
      {
        "title": "GC情况",
        "type": "graph",
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 16},
        "targets": [
          {
            "expr": "rate(jvm_gc_pause_seconds_count{job=\"attendance-service\"}[5m])",
            "legendFormat": "GC次数"
          },
          {
            "expr": "rate(jvm_gc_pause_seconds_sum{job=\"attendance-service\"}[5m])",
            "legendFormat": "GC耗时"
          }
        ],
        "yAxes": [
          {
            "label": "GC"
          }
        ]
      },
      {
        "title": "线程池状态",
        "type": "graph",
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 24},
        "targets": [
          {
            "expr": "jvm_threads_live_threads{job=\"attendance-service\"}",
            "legendFormat": "活跃线程"
          },
          {
            "expr": "jvm_threads_daemon_threads{job=\"attendance-service\"}",
            "legendFormat": "守护线程"
          }
        ],
        "yAxes": [
          {
            "label": "线程数",
            "min": 0
          }
        ]
      },
      {
        "title": "数据库连接池",
        "type": "graph",
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 24},
        "targets": [
          {
            "expr": "hikaricp_connections_active{job=\"attendance-service\"}",
            "legendFormat": "活跃连接"
          },
          {
            "expr": "druid_connection_pool_idle_count{job=\"attendance-service\"}",
            "legendFormat": "空闲连接"
          },
          {
            "expr": "druid_connection_pool_max_active{job=\"attendance-service\"}",
            "legendFormat": "最大连接"
          }
        ],
        "yAxes": [
          {
            "label": "连接数",
            "min": 0
          }
        ]
      }
    ]
  }
}
```

### 2. 业务监控仪表板

```json
{
  "dashboard": {
    "title": "IOE-DREAM 考勤业务监控",
    "uid": "attendance-business",
    "tags": ["attendance", "business"],
    "timezone": "browser",
    "panels": [
      {
        "title": "打卡速率",
        "type": "graph",
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0},
        "targets": [
          {
            "expr": "rate(attendance_clock_in_total{job=\"attendance-service\"}[5m])",
            "legendFormat": "打卡速率"
          }
        ]
      },
      {
        "title": "生物识别成功率",
        "type": "stat",
        "gridPos": {"h": 8, "w": 6, "x": 12, "y": 0},
        "targets": [
          {
            "expr": "rate(attendance_biometric_success_total{job=\"attendance-service\"}[5m]) / rate(attendance_biometric_total{job=\"attendance-service\"}[5m]) * 100",
            "legendFormat": "成功率"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "percent",
            "min": 0,
            "max": 100,
            "thresholds": {
              "steps": [
                {"color": "red", "value": 80},
                {"color": "yellow", "value": 90},
                {"color": "green", "value": 95}
              ]
            }
          }
        }
      },
      {
        "title": "位置验证成功率",
        "type": "stat",
        "gridPos": {"h": 8, "w": 6, "x": 18, "y": 0},
        "targets": [
          {
            "expr": "rate(attendance_location_valid_total{job=\"attendance-service\"}[5m]) / rate(attendance_location_total{job=\"attendance-service\"}[5m]) * 100",
            "legendFormat": "成功率"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "percent",
            "min": 0,
            "max": 100,
            "thresholds": {
              "steps": [
                {"color": "red", "value": 90},
                {"color": "yellow", "value": 95},
                {"color": "green", "value": 98}
              ]
            }
          }
        }
      },
      {
        "title": "考勤处理耗时",
        "type": "graph",
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 8},
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(attendance_processing_duration_seconds_bucket{job=\"attendance-service\"}[5m]))",
            "legendFormat": "P95"
          },
          {
            "expr": "rate(attendance_processing_duration_seconds_sum{job=\"attendance-service\"}[5m]) / rate(attendance_processing_duration_seconds_count{job=\"attendance-service\"}[5m])",
            "legendFormat": "平均值"
          }
        ]
      },
      {
        "title": "实时处理队列",
        "type": "graph",
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 8},
        "targets": [
          {
            "expr": "realtime_queue_size{job=\"attendance-service\"}",
            "legendFormat": "队列大小"
          },
          {
            "expr": "rate(realtime_event_processing_total{job=\"attendance-service\"}[5m])",
            "legendFormat": "处理速率"
          }
        ]
      },
      {
        "title": "今日考勤统计",
        "type": "table",
        "gridPos": {"h": 16, "w": 24, "x": 0, "y": 16},
        "targets": [
          {
            "expr": "attendance_today_stats",
            "format": "table",
            "instant": true,
            "legendFormat": "{{ department }}"
          }
        ],
        "transformations": [
          {
            "id": "filterFieldsByName",
            "options": {
              "include": {
                "names": ["department", "total", "present", "late", "absent", "rate"]
              }
            }
          }
        ]
      }
    ]
  }
}
```

---

## 🔧 监控配置部署

### 1. Prometheus配置

#### prometheus.yml

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'ioedream-prod'
    replica: 'prometheus-1'

rule_files:
  - "attendance_rules.yml"
  - "infrastructure_rules.yml"

alerting:
  alertmanagers:
  - static_configs:
    - targets:
      - alertmanager:9093

scrape_configs:
  # 应用服务监控
  - job_name: 'attendance-service'
    static_configs:
      - targets: ['attendance-service:8091']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    scrape_timeout: 5s

  # 基础设施监控
  - job_name: 'mysql'
    static_configs:
      - targets: ['mysql-exporter:9104']
    scrape_interval: 30s

  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']
    scrape_interval: 30s

  - job_name: 'rabbitmq'
    static_configs:
      - targets: ['rabbitmq-exporter:9419']
    scrape_interval: 30s

  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']
    scrape_interval: 30s

# 存储配置
storage:
  tsdb:
    retention.time: 30d
    retention.size: 10GB
```

### 2. AlertManager配置

#### alertmanager.yml

```yaml
global:
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'alerts@ioedream.com'
  smtp_auth_username: 'alerts@ioedream.com'
  smtp_auth_password: 'your_email_password'
  smtp_require_tls: true

route:
  group_by: ['alertname', 'service', 'severity']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  receiver: 'default'
  routes:
  - match:
      severity: critical
    receiver: 'critical-alerts'
    continue: true
  - match:
      service: attendance-service
    receiver: 'attendance-alerts'
    continue: true

receivers:
- name: 'default'
  email_configs:
  - to: 'ops-team@ioedream.com'
    subject: '📊 [INFO] IOE-DREAM 监控告警'

- name: 'critical-alerts'
  email_configs:
  - to: 'ops-team@ioedream.com'
    subject: '🚨 [CRITICAL] IOE-DREAM 关键告警'
  slack_configs:
  - api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
    channel: '#critical-alerts'
    title: 'IOE-DREAM 关键告警'
  webhook_configs:
  - url: 'http://alertmanager-webhook:5001/'

- name: 'attendance-alerts'
  email_configs:
  - to: 'attendance-team@ioedream.com'
    subject: '⚠️ [WARNING] 考勤服务告警'
  slack_configs:
  - api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
    channel: '#attendance-alerts'
    title: '考勤服务告警'

inhibit_rules:
- source_match:
    alertname: 'AttendanceServiceDown'
  target_match:
    severity: 'warning'
```

### 3. Grafana配置

#### datasource.yml

```yaml
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
```

---

## 🔍 故障排查指南

### 1. 服务不可用故障

#### 故障现象

- 服务健康检查失败
- 无法访问API接口
- 5xx错误率激增

#### 排查步骤

1. **检查Pod状态**

```bash
kubectl get pods -n ioedream-attendance
kubectl describe pod <pod-name> -n ioedream-attendance
```

2. **查看服务日志**

```bash
kubectl logs -f deployment/attendance-service -n ioedream-attendance --tail=100
```

3. **检查资源使用**

```bash
kubectl top pods -n ioedream-attendance
kubectl describe pod <pod-name> -n ioedream-attendance | grep -A 10 "Events:"
```

4. **验证网络连通性**

```bash
kubectl exec -it deployment/attendance-service -n ioedream-attendance -- curl http://localhost:8091/actuator/health
```

### 2. 高内存使用故障

#### 故障现象

- 内存使用率超过85%
- OutOfMemoryError
- 服务响应缓慢

#### 排查步骤

1. **检查JVM内存指标**

```bash
curl http://localhost:8091/actuator/metrics | grep jvm_memory
```

2. **分析内存堆栈**

```bash
jmap -histo:live,format=b <pid> > memory_dump.txt
jstack <pid> > thread_dump.txt
```

3. **查看垃圾回收情况**

```bash
curl http://localhost:8091/actuator/metrics | grep jvm_gc
```

4. **调整JVM参数**

```bash
# 增加堆内存
-Xms2g -Xmx4g -XX:NewRatio=1.2

# 优化GC参数
-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1HeapRegionSize=16m
```

### 3. 数据库连接问题

#### 故障现象

- 数据库连接池耗尽
- 查询响应超时
- 连接建立失败

#### 排查步骤

1. **检查连接池状态**

```bash
curl http://localhost:8091/actuator/health | jq .components.db
```

2. **验证数据库连接**

```bash
kubectl exec -it deployment/mysql -n ioedream-database -- mysql -u root -p -e "SHOW PROCESSLIST;"
```

3. **检查慢查询**

```bash
kubectl exec -it deployment/mysql -n ioedream-database -- mysql -u root -p -e "SHOW SLOW QUERY LOG;"
```

4. **优化连接池配置**

```yaml
spring:
  datasource:
    druid:
      initial-size: 20
      max-active: 50
      max-wait: 60000
      test-while-idle: true
      validation-query: SELECT 1
```

### 4. 缓存问题

#### 故障现象

- 缓存命中率低
- Redis连接超时
- 缓存数据不一致

#### 排查步骤

1. **检查Redis状态**

```bash
kubectl exec -it deployment/redis -n ioedream-cache -- redis-cli ping
kubectl exec -it deployment/redis -n ioedream-cache -- redis-cli info
```

2. **查看缓存指标**

```bash
kubectl exec -it deployment/redis -n ioedream-cache -- redis-cli info stats
```

3. **分析缓存使用**

```bash
kubectl exec -it deployment/redis -n ioedream-cache -- redis-cli keys "*" | wc -l
```

---

## 📋 运维检查清单

### 日常监控检查

#### 每小时检查

- [ ] 检查服务健康状态
- [ ] 查看关键指标是否正常
- [ ] 验证告警是否正常触发
- [ ] 确认通知渠道是否正常

#### 每日检查

- [ ] 检查系统整体性能指标
- [ ] 分析日志错误和异常
- [ ] 检查磁盘空间使用情况
- [ ] 验证备份任务执行状态
- [ ] 检查监控告警规则有效性

#### 每周检查

- [ ] 分析性能趋势和容量规划
- [ ] 优化监控告警规则
- [ ] 检查仪表板配置和展示
- [ ] 验证告警通知配置
- [ ] 更新监控文档和运维手册

#### 每月检查

- [ ] 全面评估监控系统性能
- [ ] 优化监控指标和告警阈值
- [ ] 检查监控存储容量规划
- [ ] 更新监控技术栈版本
- [ ] 进行监控系统故障演练

### 告警处理流程

#### P0级告警（关键）

1. **立即响应** (5分钟内)
   - 确认告警严重程度
   - 通知相关负责人和团队
   - 启动应急响应流程

2. **故障处理** (15分钟内)
   - 快速定位故障原因
   - 执行应急处理措施
   - 恢复服务正常运行

3. **后续跟进** (1小时内)
   - 分析故障根本原因
   - 制定预防措施
   - 更新运维文档

#### P1级告警（重要）

1. **及时响应** (30分钟内)
   - 分析告警影响范围
   - 确定处理优先级
   - 通知相关处理人员

2. **故障处理** (2小时内)
   - 按优先级处理问题
   - 验证处理效果
   - 更新处理状态

3. **后续跟进** (24小时内)
   - 分析问题根本原因
   - 制定长期解决方案
   - 优化相关配置

---

## 📊 监控最佳实践

### 1. 指标设计原则

#### 关键指标选择

- **业务指标**: 关注核心业务流程
- **技术指标**: 关注系统性能和稳定性
- **用户体验指标**: 关注响应时间和可用性

#### 指标命名规范

- 使用清晰的指标名称
- 包含必要的标签信息
- 遵循统一命名约定

### 2. 告警规则优化

#### 告警阈值设置

- 基于历史数据统计
- 考虑业务影响范围
- 避免告警风暴

#### 告警分级策略

- **Critical**: 系统不可用，影响核心业务
- **Warning**: 性能下降，影响用户体验
- **Info**: 信息提示，关注系统状态

### 3. 仪表板设计

#### 可视化设计原则

- 突出重要指标
- 合理布局展示
- 提供对比分析
- 支持下钻分析

#### 用户体验优化

- 响应式设计
- 快速加载优化
- 交互式操作
- 定制化视图

---

## 📞 联系支持

### 技术支持团队

- **监控团队**: <monitoring-team@ioedream.com>
- **运维团队**: <ops-team@ioedream.com>
- **开发团队**: <dev-team@ioedream.com>
- **SRE团队**: <sre-team@ioedream.com>

### 应急联系方式

- **24小时值班**: +86-xxx-xxxx-xxxx
- **紧急响应群**: WeChat/DingTalk/Slack
- **故障上报平台**: <https://alert.ioedream.com>
- **知识库**: <https://kb.ioedream.com/monitoring>

### 相关文档

- [部署运维指南](./DEPLOYMENT_OPERATIONS_GUIDE.md)
- [API接口文档](./API_DOCUMENTATION.md)
- [架构设计文档](./ARCHITECTURE.md)
- [故障排查手册](./TROUBLESHOOTING.md)

---

**📅 文档更新时间**: 2025年12月16日
**📝 文档维护**: IOE-DREAM 监控团队
**🔄 版本**: v1.0.0
**📞 联系方式**: <monitoring-team@ioedream.com>
