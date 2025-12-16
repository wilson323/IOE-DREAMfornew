# IOE-DREAM 考勤管理服务 - 部署运维指南

## 📋 部署概述

**服务名称**: IOE-DREAM 考勤管理服务 (Attendance Service)
**服务版本**: v1.0.0
**技术架构**: Spring Boot 3.5.8 + Spring Cloud 2025.0.0 + MySQL 8.0 + Redis 6.0
**部署模式**: Docker容器化 + Kubernetes集群部署
**监控体系**: Prometheus + Grafana + ELK Stack

---

## 🚀 快速部署

### 前置要求

#### 硬件要求

**最低配置**:
- CPU: 2核心
- 内存: 4GB
- 磁盘: 50GB SSD
- 网络: 100Mbps

**推荐配置**:
- CPU: 4核心
- 内存: 8GB
- 磁盘: 100GB SSD
- 网络: 1Gbps

**生产环境**:
- CPU: 8核心
- 内存: 16GB
- 磁盘: 200GB SSD
- 网络: 10Gbps

#### 软件要求

- **操作系统**: CentOS 7.6+ / Ubuntu 18.04+ / RHEL 7.6+
- **Docker**: 20.10+
- **Kubernetes**: 1.20+
- **Java**: OpenJDK 17+
- **Maven**: 3.8+
- **Git**: 2.30+

### 依赖服务

| 服务名称 | 版本要求 | 端口 | 说明 |
|---------|---------|------|------|
| MySQL | 8.0+ | 3306 | 主数据库 |
| Redis | 6.0+ | 6379 | 缓存数据库 |
| RabbitMQ | 3.9+ | 5672 | 消息队列 |
| Nacos | 2.0+ | 8848 | 注册中心/配置中心 |
| Elasticsearch | 7.17+ | 9200 | 日志存储 |
| Prometheus | 2.30+ | 9090 | 监控数据 |
| Grafana | 8.0+ | 3000 | 监控面板 |

---

## 🐳 Docker部署

### 1. 环境准备

```bash
# 安装Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# 安装Docker Compose
curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
```

### 2. 拉取代码

```bash
git clone https://github.com/your-org/IOE-DREAM.git
cd IOE-DREAM/microservices/ioedream-attendance-service
```

### 3. 配置环境变量

```bash
# 复制环境配置文件
cp .env.example .env

# 编辑环境配置
vim .env
```

**环境配置示例 (.env)**:
```bash
# 应用配置
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8091

# 数据库配置
DB_HOST=mysql
DB_PORT=3306
DB_NAME=ioedream_attendance
DB_USERNAME=attendance_user
DB_PASSWORD=your_secure_password

# Redis配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# RabbitMQ配置
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=attendance_user
RABBITMQ_PASSWORD=your_rabbitmq_password

# Nacos配置
NACOS_SERVER_ADDR=nacos:8848
NACOS_NAMESPACE=prod
NACOS_USERNAME=nacos
NACOS_PASSWORD=your_nacos_password

# 安全配置
JWT_SECRET=your_jwt_secret_key
ENCRYPT_KEY=your_encrypt_key

# 监控配置
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
```

### 4. 启动依赖服务

```bash
# 启动基础设施服务
docker-compose -f docker-compose.infrastructure.yml up -d

# 等待服务启动完成
sleep 60

# 验证服务状态
docker-compose -f docker-compose.infrastructure.yml ps
```

### 5. 启动应用服务

```bash
# 构建应用镜像
docker build -t ioedream/attendance-service:latest .

# 启动应用服务
docker-compose -f docker-compose.app.yml up -d

# 查看启动日志
docker-compose -f docker-compose.app.yml logs -f attendance-service
```

### 6. 健康检查

```bash
# 检查应用健康状态
curl http://localhost:8091/actuator/health

# 检查应用信息
curl http://localhost:8091/actuator/info

# 检查监控指标
curl http://localhost:8091/actuator/metrics
```

---

## ☸️ Kubernetes部署

### 1. 命名空间创建

```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: ioedream-attendance
  labels:
    name: ioedream-attendance
    environment: production
```

```bash
kubectl apply -f k8s/namespace.yaml
```

### 2. ConfigMap配置

```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: attendance-config
  namespace: ioedream-attendance
data:
  application.yml: |
    server:
      port: 8091
    spring:
      profiles:
        active: k8s
      application:
        name: ioedream-attendance-service
      cloud:
        nacos:
          discovery:
            server-addr: nacos-service:8848
            namespace: prod
            group: IOE-DREAM
          config:
            server-addr: nacos-service:8848
            namespace: prod
            group: IOE-DREAM
            file-extension: yaml
      datasource:
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://mysql-service:3306/ioedream_attendance?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
        type: com.alibaba.druid.pool.DruidDataSource
        druid:
          initial-size: 10
          min-idle: 10
          max-active: 50
          max-wait: 60000
          validation-query: SELECT 1
          test-while-idle: true
          test-on-borrow: false
          test-on-return: false
      redis:
        host: redis-service
        port: 6379
        password: ${REDIS_PASSWORD}
        database: 0
        timeout: 3000
        lettuce:
          pool:
            max-active: 8
            max-idle: 8
            min-idle: 0
      rabbitmq:
        host: rabbitmq-service
        port: 5672
        username: ${RABBITMQ_USERNAME}
        password: ${RABBITMQ_PASSWORD}
        virtual-host: /

    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: always
      metrics:
        export:
          prometheus:
            enabled: true

    logging:
      level:
        net.lab1024.sa.attendance: INFO
        org.springframework.cloud: INFO
        com.alibaba.nacos: INFO
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{50} - %msg%n"
        file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{50} - %msg%n"
      file:
        name: /app/logs/attendance-service.log
        max-size: 100MB
        max-history: 30
```

```bash
kubectl apply -f k8s/configmap.yaml
```

### 3. Secret配置

```yaml
# k8s/secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: attendance-secret
  namespace: ioedream-attendance
type: Opaque
data:
  db-username: YXR0ZW5kYW5jZV91c2Vy  # base64编码
  db-password: eW91cl9zZWN1cmVfcGFzc3dvcmQ=
  redis-password: eW91cl9yZWRpc19wYXNzd29yZA==
  rabbitmq-username: YXR0ZW5kYW5jZV91c2Vy
  rabbitmq-password: eW91cl9yYWJiaXRtcV9wYXNzd29yZA==
  jwt-secret: eW91cl9qd3Rfc2VjcmV0X2tleQ==
  encrypt-key: eW91cl9lbmNyeXB0X2tleQ==
```

```bash
kubectl apply -f k8s/secret.yaml
```

### 4. PVC持久化存储

```yaml
# k8s/pvc.yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: attendance-logs-pvc
  namespace: ioedream-attendance
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 50Gi
  storageClassName: fast-ssd
```

```bash
kubectl apply -f k8s/pvc.yaml
```

### 5. 应用部署

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: attendance-service
  namespace: ioedream-attendance
  labels:
    app: attendance-service
    version: v1.0.0
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: attendance-service
  template:
    metadata:
      labels:
        app: attendance-service
        version: v1.0.0
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8091"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      containers:
      - name: attendance-service
        image: ioedream/attendance-service:v1.0.0
        imagePullPolicy: Always
        ports:
        - containerPort: 8091
          name: http
          protocol: TCP
        env:
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: attendance-secret
              key: db-username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: attendance-secret
              key: db-password
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: attendance-secret
              key: redis-password
        - name: RABBITMQ_USERNAME
          valueFrom:
            secretKeyRef:
              name: attendance-secret
              key: rabbitmq-username
        - name: RABBITMQ_PASSWORD
          valueFrom:
            secretKeyRef:
              name: attendance-secret
              key: rabbitmq-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: attendance-secret
              key: jwt-secret
        - name: ENCRYPT_KEY
          valueFrom:
            secretKeyRef:
              name: attendance-secret
              key: encrypt-key
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: logs-volume
          mountPath: /app/logs
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8091
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8091
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
      volumes:
      - name: config-volume
        configMap:
          name: attendance-config
      - name: logs-volume
        persistentVolumeClaim:
          claimName: attendance-logs-pvc
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
```

```bash
kubectl apply -f k8s/deployment.yaml
```

### 6. 服务暴露

```yaml
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: attendance-service
  namespace: ioedream-attendance
  labels:
    app: attendance-service
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8091"
    prometheus.io/path: "/actuator/prometheus"
spec:
  selector:
    app: attendance-service
  ports:
  - name: http
    port: 8091
    targetPort: 8091
    protocol: TCP
  type: ClusterIP
```

```bash
kubectl apply -f k8s/service.yaml
```

### 7. Ingress配置

```yaml
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: attendance-ingress
  namespace: ioedream-attendance
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "50m"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - attendance.ioedream.com
    secretName: attendance-tls
  rules:
  - host: attendance.ioedream.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: attendance-service
            port:
              number: 8091
```

```bash
kubectl apply -f k8s/ingress.yaml
```

### 8. HPA自动扩缩容

```yaml
# k8s/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: attendance-hpa
  namespace: ioedream-attendance
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: attendance-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
      - type: Pods
        value: 2
        periodSeconds: 60
      selectPolicy: Max
```

```bash
kubectl apply -f k8s/hpa.yaml
```

---

## 📊 监控配置

### 1. Prometheus监控

```yaml
# k8s/monitoring/prometheus.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: ioedream-attendance
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s

    rule_files:
      - "attendance_rules.yml"

    scrape_configs:
      - job_name: 'attendance-service'
        static_configs:
          - targets: ['attendance-service:8091']
        metrics_path: '/actuator/prometheus'
        scrape_interval: 10s
        scrape_timeout: 5s

    alerting:
      alertmanagers:
        - static_configs:
            - targets:
              - alertmanager:9093

  attendance_rules.yml: |
    groups:
    - name: attendance.rules
      rules:
      - alert: AttendanceServiceDown
        expr: up{job="attendance-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "考勤服务不可用"
          description: "考勤服务已停止运行超过1分钟"

      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes / jvm_memory_max_bytes) * 100 > 85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "内存使用率过高"
          description: "考勤服务内存使用率超过85%持续5分钟"

      - alert: HighCPUUsage
        expr: rate(process_cpu_seconds_total[5m]) * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "CPU使用率过高"
          description: "考勤服务CPU使用率超过80%持续5分钟"

      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "错误率过高"
          description: "考勤服务5xx错误率超过10%持续2分钟"

      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "响应时间过长"
          description: "考勤服务95%请求响应时间超过1秒持续5分钟"
```

### 2. Grafana仪表板

```json
{
  "dashboard": {
    "title": "IOE-DREAM 考勤服务监控",
    "uid": "attendance-dashboard",
    "panels": [
      {
        "title": "应用健康状态",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=\"attendance-service\"}",
            "legendFormat": "服务状态"
          }
        ]
      },
      {
        "title": "请求QPS",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total{job=\"attendance-service\"}[5m])",
            "legendFormat": "QPS"
          }
        ]
      },
      {
        "title": "响应时间",
        "type": "graph",
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
        ]
      },
      {
        "title": "错误率",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total{job=\"attendance-service\",status=~\"4..\"}[5m])",
            "legendFormat": "4xx错误率"
          },
          {
            "expr": "rate(http_requests_total{job=\"attendance-service\",status=~\"5..\"}[5m])",
            "legendFormat": "5xx错误率"
          }
        ]
      },
      {
        "title": "JVM内存使用",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{job=\"attendance-service\", area=\"heap\"}",
            "legendFormat": "堆内存使用"
          },
          {
            "expr": "jvm_memory_max_bytes{job=\"attendance-service\", area=\"heap\"}",
            "legendFormat": "堆内存最大"
          }
        ]
      },
      {
        "title": "GC情况",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(jvm_gc_pause_seconds_count{job=\"attendance-service\"}[5m])",
            "legendFormat": "GC次数"
          },
          {
            "expr": "rate(jvm_gc_pause_seconds_sum{job=\"attendance-service\"}[5m])",
            "legendFormat": "GC耗时"
          }
        ]
      },
      {
        "title": "数据库连接池",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active{job=\"attendance-service\"}",
            "legendFormat": "活跃连接"
          },
          {
            "expr": "hikaricp_connections_idle{job=\"attendance-service\"}",
            "legendFormat": "空闲连接"
          }
        ]
      },
      {
        "title": "考勤业务指标",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(attendance_clock_in_total{job=\"attendance-service\"}[5m])",
            "legendFormat": "打卡速率"
          },
          {
            "expr": "attendance_processing_duration_seconds{job=\"attendance-service\"}",
            "legendFormat": "处理耗时"
          }
        ]
      }
    ]
  }
}
```

---

## 🔧 运维操作

### 1. 日常运维

#### 健康检查

```bash
# 检查Pod状态
kubectl get pods -n ioedream-attendance

# 检查服务状态
kubectl get services -n ioedream-attendance

# 检查应用健康
kubectl exec -it deployment/attendance-service -n ioedream-attendance -- curl http://localhost:8091/actuator/health

# 查看资源使用
kubectl top pods -n ioedream-attendance
```

#### 日志查看

```bash
# 查看应用日志
kubectl logs -f deployment/attendance-service -n ioedream-attendance

# 查看最近的错误日志
kubectl logs deployment/attendance-service -n ioedream-attendance --tail=100 | grep ERROR

# 查看特定时间段的日志
kubectl logs deployment/attendance-service -n ioedream-attendance --since=1h | grep ERROR
```

#### 扩缩容操作

```bash
# 手动扩容
kubectl scale deployment attendance-service --replicas=5 -n ioedream-attendance

# 手动缩容
kubectl scale deployment attendance-service --replicas=2 -n ioedream-attendance

# 查看扩缩容状态
kubectl get hpa -n ioedream-attendance
```

### 2. 故障排查

#### 常见问题处理

**问题1: 服务启动失败**

```bash
# 查看Pod状态
kubectl describe pod <pod-name> -n ioedream-attendance

# 查看启动日志
kubectl logs <pod-name> -n ioedream-attendance

# 检查资源配置
kubectl get pod <pod-name> -n ioedream-attendance -o yaml
```

**问题2: 数据库连接失败**

```bash
# 测试数据库连接
kubectl exec -it deployment/attendance-service -n ioedream-attendance -- telnet mysql-service 3306

# 检查数据库配置
kubectl get configmap attendance-config -n ioedream-attendance -o yaml

# 检查Secret配置
kubectl get secret attendance-secret -n ioedream-attendance -o yaml
```

**问题3: 内存溢出**

```bash
# 查看内存使用情况
kubectl exec -it deployment/attendance-service -n ioedream-attendance -- jstat -gcutil $(pgrep java)

# 检查JVM参数
kubectl describe deployment attendance-service -n ioedream-attendance | grep JAVA_OPTS

# 调整内存限制
kubectl patch deployment attendance-service -p '{"spec":{"template":{"spec":{"containers":[{"name":"attendance-service","resources":{"limits":{"memory":"4Gi"},"requests":{"memory":"2Gi"}}}]}}}' -n ioedream-attendance
```

**问题4: 高CPU使用率**

```bash
# 查看CPU使用情况
kubectl top pods -n ioedream-attendance

# 分析CPU使用原因
kubectl exec -it deployment/attendance-service -n ioedream-attendance -- top -Hp $(pgrep java)

# 生成线程快照
kubectl exec -it deployment/attendance-service -n ioedream-attendance -- jstack $(pgrep java) > thread_dump.txt
```

### 3. 备份恢复

#### 数据备份

```bash
# 数据库备份
kubectl exec -it deployment/mysql -n ioedream-attendance -- mysqldump -u root -p ioedream_attendance > backup_$(date +%Y%m%d_%H%M%S).sql

# Redis备份
kubectl exec -it deployment/redis -n ioedream-attendance -- redis-cli BGSAVE

# 配置备份
kubectl get configmap attendance-config -n ioedream-attendance -o yaml > config_backup_$(date +%Y%m%d_%H%M%S).yaml
```

#### 数据恢复

```bash
# 数据库恢复
kubectl exec -i deployment/mysql -n ioedream-attendance -- mysql -u root -p ioedream_attendance < backup_20251216_100000.sql

# 配置恢复
kubectl apply -f config_backup_20251216_100000.yaml
```

### 4. 版本更新

#### 滚动更新

```bash
# 更新镜像版本
kubectl set image deployment/attendance-service attendance-service=ioedream/attendance-service:v1.1.0 -n ioedream-attendance

# 查看更新状态
kubectl rollout status deployment/attendance-service -n ioedream-attendance

# 回滚到上一版本
kubectl rollout undo deployment/attendance-service -n ioedream-attendance

# 回滚到指定版本
kubectl rollout undo deployment/attendance-service --to-revision=2 -n ioedream-attendance
```

#### 蓝绿部署

```yaml
# k8s/blue-green-deployment.yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: attendance-service
  namespace: ioedream-attendance
spec:
  replicas: 3
  strategy:
    blueGreen:
      activeService: attendance-service-active
      previewService: attendance-service-preview
      autoPromotionEnabled: false
      scaleDownDelaySeconds: 30
      prePromotionAnalysis:
        templates:
        - templateName: success-rate
        args:
        - name: service-name
          value: attendance-service-preview
      postPromotionAnalysis:
        templates:
        - templateName: success-rate
        args:
        - name: service-name
          value: attendance-service-active
  selector:
    matchLabels:
      app: attendance-service
  template:
    metadata:
      labels:
        app: attendance-service
    spec:
      containers:
      - name: attendance-service
        image: ioedream/attendance-service:v1.1.0
        ports:
        - containerPort: 8091
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8091
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8091
          initialDelaySeconds: 30
          periodSeconds: 10
```

---

## 🚨 告警配置

### 1. 告警规则

```yaml
# k8s/monitoring/alerting.yml
apiVersion: v1
kind: ConfigMap
metadata:
  name: alertmanager-config
  namespace: ioedream-attendance
data:
  alertmanager.yml: |
    global:
      smtp_smarthost: 'smtp.example.com:587'
      smtp_from: 'alerts@ioedream.com'
      smtp_auth_username: 'alerts@ioedream.com'
      smtp_auth_password: 'your_email_password'

    route:
      group_by: ['alertname', 'service']
      group_wait: 10s
      group_interval: 10s
      repeat_interval: 12h
      receiver: 'web.hook'
      routes:
      - match:
          severity: critical
        receiver: 'critical-alerts'
      - match:
          severity: warning
        receiver: 'warning-alerts'

    receivers:
    - name: 'web.hook'
      webhook_configs:
      - url: 'http://alertmanager-webhook:5001/'

    - name: 'critical-alerts'
      email_configs:
      - to: 'ops-team@ioedream.com'
        subject: '🚨 [CRITICAL] IOE-DREAM 考勤服务告警'
        body: |
          {{ range .Alerts }}
          告警名称: {{ .Annotations.summary }}
          告警描述: {{ .Annotations.description }}
          告警级别: {{ .Labels.severity }}
          开始时间: {{ .StartsAt }}
          {{ end }}
      slack_configs:
      - api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
        channel: '#alerts-critical'
        title: '🚨 IOE-DREAM 考勤服务告警'
        text: |
          {{ range .Alerts }}
          *告警名称*: {{ .Annotations.summary }}
          *告警描述*: {{ .Annotations.description }}
          *告警级别*: {{ .Labels.severity }}
          *开始时间*: {{ .StartsAt }}
          {{ end }}

    - name: 'warning-alerts'
      email_configs:
      - to: 'dev-team@ioedream.com'
        subject: '⚠️ [WARNING] IOE-DREAM 考勤服务告警'
        body: |
          {{ range .Alerts }}
          告警名称: {{ .Annotations.summary }}
          告警描述: {{ .Annotations.description }}
          告警级别: {{ .Labels.severity }}
          开始时间: {{ .StartsAt }}
          {{ end }}
```

### 2. 告警通知渠道

#### 邮件通知配置

```yaml
# k8s/monitoring/email-config.yaml
apiVersion: v1
kind: Secret
metadata:
  name: email-config
  namespace: ioedream-attendance
type: Opaque
data:
  smtp-username: YWxlcnRzQGlvZWRyZWFtLmNvbQ==  # base64编码
  smtp-password: eW91cl9lbWFpbF9wYXNzd29yZA==
  smtp-host: c210cC5leGFtcGxlLmNvbQ==
  smtp-port: NTg3
```

#### Slack通知配置

```yaml
# k8s/monitoring/slack-config.yaml
apiVersion: v1
kind: Secret
metadata:
  name: slack-config
  namespace: ioedream-attendance
type: Opaque
data:
  webhook-url: aHR0cHM6Ly9ob29rcy5zbGFjay5jb20vc2VydmljZXMvWU9VUi9TTEFDSy9XRUJIT09L # base64编码
  channel: '#attendance-alerts'
```

---

## 📈 性能优化

### 1. JVM优化

#### 生产环境JVM参数

```bash
JAVA_OPTS="-Xms2g -Xmx4g \
           -XX:+UseG1GC \
           -XX:MaxGCPauseMillis=200 \
           -XX:G1HeapRegionSize=16m \
           -XX:+UnlockExperimentalVMOptions \
           -XX:+UseStringDeduplication \
           -XX:+PrintGCDetails \
           -XX:+PrintGCTimeStamps \
           -XX:+HeapDumpOnOutOfMemoryError \
           -XX:HeapDumpPath=/app/logs/ \
           -Dfile.encoding=UTF-8 \
           -Duser.timezone=Asia/Shanghai \
           -Dspring.profiles.active=k8s"
```

### 2. 数据库优化

#### MySQL配置优化

```sql
-- MySQL配置优化
SET GLOBAL innodb_buffer_pool_size = '2G';
SET GLOBAL innodb_log_file_size = '256M';
SET GLOBAL innodb_log_buffer_size = '16M';
SET GLOBAL innodb_flush_log_at_trx_commit = 2;
SET GLOBAL sync_binlog = 0;
SET GLOBAL innodb_file_per_table = 1;
SET GLOBAL innodb_flush_method = O_DIRECT;
SET GLOBAL innodb_lock_wait_timeout = 50;
SET GLOBAL max_connections = 500;
SET GLOBAL query_cache_size = '128M';
SET GLOBAL query_cache_type = ON;
```

#### 索引优化

```sql
-- 考勤记录表索引优化
CREATE INDEX idx_attendance_user_time ON t_attendance_record(user_id, create_time);
CREATE INDEX idx_attendance_shift_time ON t_attendance_record(shift_id, create_time);
CREATE INDEX idx_attendance_status_time ON t_attendance_record(status, create_time);

-- 用户表索引优化
CREATE INDEX idx_user_department ON t_common_user(department_id, status);
CREATE INDEX idx_user_mobile ON t_common_user(mobile_phone, status);

-- 班次表索引优化
CREATE INDEX idx_shift_department ON t_attendance_shift(department_id, status);
CREATE INDEX idx_shift_time_range ON t_attendance_shift(start_time, end_time);
```

### 3. Redis优化

#### Redis配置优化

```bash
# redis.conf 优化配置
maxmemory 2gb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
appendonly yes
appendfsync everysec
tcp-keepalive 300
timeout 0
```

### 4. 应用优化

#### 连接池优化

```yaml
spring:
  datasource:
    druid:
      initial-size: 20
      min-idle: 20
      max-active: 100
      max-wait: 60000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        reset-enable: false
        login-username: admin
        login-password: admin123
        allow: 127.0.0.1,192.168.1.0/24
```

---

## 🔒 安全配置

### 1. 网络安全

#### 网络策略

```yaml
# k8s/network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: attendance-network-policy
  namespace: ioedream-attendance
spec:
  podSelector:
    matchLabels:
      app: attendance-service
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ioedream-gateway
    - namespaceSelector:
        matchLabels:
          name: monitoring
    ports:
    - protocol: TCP
      port: 8091
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: ioedream-database
    ports:
    - protocol: TCP
      port: 3306
  - to:
    - namespaceSelector:
        matchLabels:
          name: ioedream-cache
    ports:
    - protocol: TCP
      port: 6379
  - to:
    - namespaceSelector:
        matchLabels:
          name: ioedream-mq
    ports:
    - protocol: TCP
      port: 5672
```

### 2. RBAC配置

```yaml
# k8s/rbac.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: attendance-service
  namespace: ioedream-attendance
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: attendance-role
  namespace: ioedream-attendance
rules:
- apiGroups: [""]
  resources: ["pods", "services", "configmaps", "secrets"]
  verbs: ["get", "list", "watch"]
- apiGroups: ["apps"]
  resources: ["deployments"]
  verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: attendance-rolebinding
  namespace: ioedream-attendance
subjects:
- kind: ServiceAccount
  name: attendance-service
  namespace: ioedream-attendance
roleRef:
  kind: Role
  name: attendance-role
  apiGroup: rbac.authorization.k8s.io
```

### 3. Pod安全策略

```yaml
# k8s/pod-security-policy.yaml
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: attendance-psp
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
    - ALL
  volumes:
    - 'configMap'
    - 'emptyDir'
    - 'projected'
    - 'secret'
    - 'downwardAPI'
    - 'persistentVolumeClaim'
  runAsUser:
    rule: 'MustRunAsNonRoot'
  seLinux:
    rule: 'RunAsAny'
  fsGroup:
    rule: 'RunAsAny'
```

---

## 📋 运维检查清单

### 日常检查清单

- [ ] 检查Pod状态和健康情况
- [ ] 查看应用日志，确认无错误
- [ ] 检查资源使用情况（CPU、内存、磁盘）
- [ ] 验证数据库连接和查询性能
- [ ] 检查缓存命中率和连接状态
- [ ] 确认消息队列正常运行
- [ ] 查看监控指标和告警状态
- [ ] 验证备份任务执行情况

### 周期性检查清单

#### 每周检查
- [ ] 分析性能趋势和容量规划
- [ ] 检查日志存储空间使用情况
- [ ] 验证备份完整性和恢复测试
- [ ] 更新安全补丁和依赖库版本
- [ ] 清理过期的日志和临时文件

#### 每月检查
- [ ] 全面性能压力测试
- [ ] 安全漏洞扫描和风险评估
- [ ] 容量规划评估和扩容建议
- [ ] 监控告警规则优化
- [ ] 文档更新和操作手册完善

### 紧急响应清单

#### 服务不可用
1. **立即响应** (5分钟内)
   - [ ] 确认故障范围和影响
   - [ ] 通知相关人员和团队
   - [ ] 启动应急响应流程

2. **故障排查** (15分钟内)
   - [ ] 检查Pod状态和日志
   - [ ] 验证依赖服务状态
   - [ ] 查看监控指标和告警

3. **故障恢复** (30分钟内)
   - [ ] 尝试重启服务
   - [ ] 回滚到稳定版本
   - [ ] 切换到备用服务

#### 性能问题
1. **性能下降** (10分钟内)
   - [ ] 确认性能问题类型
   - [ ] 查看资源使用情况
   - [ ] 分析慢查询和阻塞

2. **性能优化** (1小时内)
   - [ ] 调整资源配额
   - [ ] 优化数据库查询
   - [ ] 增加缓存策略

---

## 📞 支持联系

### 技术支持团队

- **运维团队**: ops-team@ioedream.com
- **开发团队**: dev-team@ioedream.com
- **安全团队**: security-team@ioedream.com

### 应急联系方式

- **24小时值班电话**: +86-xxx-xxxx-xxxx
- **紧急响应群**: WeChat/Slack/DingTalk
- **故障上报平台**: https://alert.ioedream.com

### 相关文档

- [API接口文档](./API_DOCUMENTATION.md)
- [系统架构文档](./ARCHITECTURE.md)
- [开发指南](./DEVELOPMENT_GUIDE.md)
- [故障排查手册](./TROUBLESHOOTING.md)

---

**📅 文档更新时间**: 2025年12月16日
**📝 文档维护**: IOE-DREAM 运维团队
**🔄 版本**: v1.0.0
**📞 联系方式**: ops-team@ioedream.com