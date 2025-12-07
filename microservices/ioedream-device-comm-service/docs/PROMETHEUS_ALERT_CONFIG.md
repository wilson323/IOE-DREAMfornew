# Prometheus告警机制配置指南

**配置日期**: 2025-01-30  
**适用范围**: 考勤、门禁、消费三种设备通讯协议监控告警  
**配置目标**: 实现企业级监控告警机制

---

## 📊 一、告警规则配置

### 1.1 协议处理失败率告警

**告警规则**: `protocol_message_failure_rate_high`

```yaml
groups:
  - name: protocol_alerts
    interval: 30s
    rules:
      # 协议处理失败率告警
      - alert: ProtocolMessageFailureRateHigh
        expr: |
          rate(protocol_message_process_total{status="error"}[5m]) 
          / 
          rate(protocol_message_process_total[5m]) 
          > 0.1
        for: 5m
        labels:
          severity: warning
          service: device-comm-service
        annotations:
          summary: "协议消息处理失败率过高"
          description: "协议消息处理失败率超过10%，当前值: {{ $value | humanizePercentage }}"
          runbook_url: "https://wiki.company.com/runbook/protocol-failure-rate"

      # 协议处理失败率严重告警
      - alert: ProtocolMessageFailureRateCritical
        expr: |
          rate(protocol_message_process_total{status="error"}[5m]) 
          / 
          rate(protocol_message_process_total[5m]) 
          > 0.3
        for: 2m
        labels:
          severity: critical
          service: device-comm-service
        annotations:
          summary: "协议消息处理失败率严重过高"
          description: "协议消息处理失败率超过30%，当前值: {{ $value | humanizePercentage }}"
          runbook_url: "https://wiki.company.com/runbook/protocol-failure-rate-critical"
```

---

### 1.2 协议处理延迟告警

**告警规则**: `protocol_message_duration_high`

```yaml
      # 协议处理延迟告警（P99延迟超过阈值）
      - alert: ProtocolMessageDurationHigh
        expr: |
          histogram_quantile(0.99, 
            rate(protocol_message_process_duration_seconds_bucket[5m])
          ) > 2
        for: 5m
        labels:
          severity: warning
          service: device-comm-service
        annotations:
          summary: "协议消息处理延迟过高"
          description: "协议消息P99处理延迟超过2秒，当前值: {{ $value }}秒"
          runbook_url: "https://wiki.company.com/runbook/protocol-duration-high"

      # 协议处理延迟严重告警（P99延迟超过阈值）
      - alert: ProtocolMessageDurationCritical
        expr: |
          histogram_quantile(0.99, 
            rate(protocol_message_process_duration_seconds_bucket[5m])
          ) > 5
        for: 2m
        labels:
          severity: critical
          service: device-comm-service
        annotations:
          summary: "协议消息处理延迟严重过高"
          description: "协议消息P99处理延迟超过5秒，当前值: {{ $value }}秒"
          runbook_url: "https://wiki.company.com/runbook/protocol-duration-critical"
```

---

### 1.3 消息队列积压告警

**告警规则**: `protocol_queue_backlog_high`

```yaml
      # 消息队列积压告警
      - alert: ProtocolQueueBacklogHigh
        expr: |
          rabbitmq_queue_messages > 5000
        for: 5m
        labels:
          severity: warning
          service: device-comm-service
        annotations:
          summary: "协议消息队列积压过多"
          description: "消息队列积压超过5000条，当前值: {{ $value }}条"
          runbook_url: "https://wiki.company.com/runbook/protocol-queue-backlog"

      # 消息队列积压严重告警
      - alert: ProtocolQueueBacklogCritical
        expr: |
          rabbitmq_queue_messages > 10000
        for: 2m
        labels:
          severity: critical
          service: device-comm-service
        annotations:
          summary: "协议消息队列积压严重过多"
          description: "消息队列积压超过10000条，当前值: {{ $value }}条"
          runbook_url: "https://wiki.company.com/runbook/protocol-queue-backlog-critical"
```

---

### 1.4 服务熔断告警

**告警规则**: `protocol_circuit_breaker_open`

```yaml
      # 服务熔断告警
      - alert: ProtocolCircuitBreakerOpen
        expr: |
          resilience4j_circuitbreaker_state{name=~"access-service|attendance-service|consume-service|common-service"} == 1
        for: 1m
        labels:
          severity: critical
          service: device-comm-service
        annotations:
          summary: "协议服务熔断器已打开"
          description: "服务 {{ $labels.name }} 的熔断器已打开，服务不可用"
          runbook_url: "https://wiki.company.com/runbook/protocol-circuit-breaker"
```

---

### 1.5 限流触发告警

**告警规则**: `protocol_rate_limit_triggered`

```yaml
      # 限流触发告警
      - alert: ProtocolRateLimitTriggered
        expr: |
          rate(protocol_message_error_total{error_type="RATE_LIMIT"}[5m]) > 10
        for: 5m
        labels:
          severity: warning
          service: device-comm-service
        annotations:
          summary: "协议接口限流频繁触发"
          description: "协议接口限流触发频率过高，5分钟内超过10次"
          runbook_url: "https://wiki.company.com/runbook/protocol-rate-limit"
```

---

### 1.6 缓存命中率告警

**告警规则**: `protocol_cache_hit_rate_low`

```yaml
      # 缓存命中率告警
      - alert: ProtocolCacheHitRateLow
        expr: |
          (cache_hits_total / cache_requests_total) < 0.7
        for: 10m
        labels:
          severity: warning
          service: device-comm-service
        annotations:
          summary: "协议缓存命中率过低"
          description: "协议缓存命中率低于70%，当前值: {{ $value | humanizePercentage }}"
          runbook_url: "https://wiki.company.com/runbook/protocol-cache-hit-rate"
```

---

## 📧 二、告警通知配置

### 2.1 Alertmanager配置

**配置文件**: `alertmanager.yml`

```yaml
global:
  resolve_timeout: 5m
  # 邮件通知配置
  smtp_smarthost: 'smtp.company.com:587'
  smtp_from: 'alertmanager@company.com'
  smtp_auth_username: 'alertmanager@company.com'
  smtp_auth_password: 'your-password'
  smtp_require_tls: true

# 路由配置
route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  receiver: 'default-receiver'
  routes:
    # 严重告警立即通知
    - match:
        severity: critical
      receiver: 'critical-receiver'
      continue: true
    # 协议相关告警
    - match:
        service: device-comm-service
      receiver: 'protocol-receiver'
      continue: true

# 接收器配置
receivers:
  # 默认接收器（邮件）
  - name: 'default-receiver'
    email_configs:
      - to: 'devops@company.com'
        headers:
          Subject: '{{ .GroupLabels.alertname }} - {{ .GroupLabels.service }}'
        html: |
          <h2>告警通知</h2>
          <p><strong>告警名称:</strong> {{ .GroupLabels.alertname }}</p>
          <p><strong>服务:</strong> {{ .GroupLabels.service }}</p>
          <p><strong>严重程度:</strong> {{ .GroupLabels.severity }}</p>
          <p><strong>告警详情:</strong></p>
          <ul>
            {{ range .Alerts }}
            <li>{{ .Annotations.description }}</li>
            {{ end }}
          </ul>

  # 严重告警接收器（邮件 + 短信）
  - name: 'critical-receiver'
    email_configs:
      - to: 'devops@company.com,oncall@company.com'
        headers:
          Subject: '[CRITICAL] {{ .GroupLabels.alertname }}'
        html: |
          <h2 style="color: red;">严重告警通知</h2>
          <p><strong>告警名称:</strong> {{ .GroupLabels.alertname }}</p>
          <p><strong>服务:</strong> {{ .GroupLabels.service }}</p>
          <p><strong>告警详情:</strong></p>
          <ul>
            {{ range .Alerts }}
            <li>{{ .Annotations.description }}</li>
            {{ end }}
          </ul>
    # 短信通知（需要配置短信网关）
    # webhook_configs:
    #   - url: 'http://sms-gateway.company.com/send'
    #     send_resolved: true

  # 协议服务接收器（邮件 + 钉钉）
  - name: 'protocol-receiver'
    email_configs:
      - to: 'protocol-team@company.com'
        headers:
          Subject: '[Protocol] {{ .GroupLabels.alertname }}'
    # 钉钉通知
    webhook_configs:
      - url: 'https://oapi.dingtalk.com/robot/send?access_token=your-token'
        send_resolved: true
        http_config:
          bearer_token: 'your-bearer-token'
```

---

## 🔔 三、告警通知渠道

### 3.1 邮件通知

**配置方式**: 在Alertmanager中配置SMTP服务器

**通知对象**:
- 默认告警: `devops@company.com`
- 严重告警: `devops@company.com`, `oncall@company.com`
- 协议告警: `protocol-team@company.com`

---

### 3.2 钉钉通知

**配置方式**: 使用钉钉机器人Webhook

**步骤**:
1. 在钉钉群中创建自定义机器人
2. 获取Webhook URL
3. 在Alertmanager配置中添加webhook_configs

**示例**:
```yaml
webhook_configs:
  - url: 'https://oapi.dingtalk.com/robot/send?access_token=your-token'
    send_resolved: true
```

---

### 3.3 短信通知（可选）

**配置方式**: 通过短信网关API

**适用场景**: 严重告警（critical级别）

**示例**:
```yaml
webhook_configs:
  - url: 'http://sms-gateway.company.com/send'
    send_resolved: false
```

---

## 📈 四、告警规则部署

### 4.1 Prometheus配置

**配置文件**: `prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

# Alertmanager配置
alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

# 告警规则文件
rule_files:
  - "/etc/prometheus/rules/*.yml"
```

---

### 4.2 告警规则文件位置

**目录结构**:
```
/etc/prometheus/rules/
├── protocol_alerts.yml      # 协议相关告警规则
├── system_alerts.yml        # 系统相关告警规则
└── business_alerts.yml      # 业务相关告警规则
```

---

## 🚀 五、告警测试

### 5.1 测试告警规则

**方法1**: 使用Prometheus UI测试

1. 访问 `http://prometheus:9090/alerts`
2. 查看告警规则状态
3. 手动触发告警（修改阈值）

**方法2**: 使用curl测试Alertmanager

```bash
# 发送测试告警
curl -XPOST http://alertmanager:9093/api/v1/alerts -d '[
  {
    "labels": {
      "alertname": "TestAlert",
      "severity": "warning",
      "service": "device-comm-service"
    },
    "annotations": {
      "summary": "测试告警",
      "description": "这是一个测试告警"
    }
  }
]'
```

---

## 📊 六、告警监控面板

### 6.1 Grafana告警面板

**面板配置**: 在Grafana中创建告警面板，展示：
- 告警数量趋势
- 告警类型分布
- 告警处理时间
- 告警恢复时间

---

## ✅ 七、告警机制检查清单

- [ ] Prometheus告警规则已配置
- [ ] Alertmanager已部署并配置
- [ ] 告警通知渠道已配置（邮件、钉钉等）
- [ ] 告警规则已测试
- [ ] 告警处理流程已建立
- [ ] 告警响应SLA已定义
- [ ] 告警升级机制已建立

---

**📅 配置完成日期**: 2025-01-30  
**👥 配置人员**: IOE-DREAM 架构委员会  
**✅ 配置状态**: **待部署**

