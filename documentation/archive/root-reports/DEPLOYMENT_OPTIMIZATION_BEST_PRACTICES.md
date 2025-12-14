# IOE-DREAM 智慧园区一卡通管理平台 - 部署优化最佳实践指南

> **文档版本**: v1.0.0
> **发布日期**: 2025-01-08
> **适用架构**: Spring Boot 3.5.8 + Spring Cloud 2025.0.0 + 微服务架构
> **核心目标**: 最小化服务器资源占用，最大化系统性能，确保高可用部署

---

## 📋 目录

1. [部署架构概述](#1-部署架构概述)
2. [环境准备与优化](#2-环境准备与优化)
3. [微服务部署策略](#3-微服务部署策略)
4. [性能优化配置](#4-性能优化配置)
5. [安全加固配置](#5-安全加固配置)
6. [监控告警体系](#6-监控告警体系)
7. [运维管理最佳实践](#7-运维管理最佳实践)
8. [故障排查指南](#8-故障排查指南)
9. [成本优化策略](#9-成本优化策略)

---

## 1. 部署架构概述

### 1.1 整体架构设计

**架构原则**:
- ✅ **微服务拆分**: 9个核心微服务独立部署
- ✅ **资源优化**: 基于实际负载动态分配资源
- ✅ **高可用设计**: 多实例部署 + 负载均衡
- ✅ **容器化部署**: Docker + Kubernetes标准化

**核心微服务列表**:
| 服务名称 | 端口 | 资源配置 | 实例数 | 说明 |
|---------|------|---------|--------|------|
| ioedream-gateway-service | 8080 | 1C/2G | 2+ | API网关，高可用要求 |
| ioedream-common-service | 8088 | 2C/4G | 2+ | 公共服务，业务核心 |
| ioedream-device-comm-service | 8087 | 1C/2G | 1+ | 设备通讯，资源轻量 |
| ioedream-oa-service | 8089 | 2C/4G | 1+ | OA办公，业务核心 |
| ioedream-access-service | 8090 | 1C/2G | 1+ | 门禁管理 |
| ioedream-attendance-service | 8091 | 1C/2G | 1+ | 考勤管理 |
| ioedream-video-service | 8092 | 2C/8G | 1+ | 视频监控，资源密集 |
| ioedream-consume-service | 8094 | 2C/4G | 2+ | 消费管理，高并发 |
| ioedream-visitor-service | 8095 | 1C/2G | 1+ | 访客管理 |

### 1.2 资源分配策略

**生产环境推荐配置**:

```yaml
# 总资源配置（示例）
cluster:
  total_nodes: 3
  total_cpu: "24 cores"
  total_memory: "64GB"
  total_storage: "500GB SSD"

# 按服务重要性分配资源分配
resource_allocation:
  # P0级核心服务
  gateway_service:
    cpu: "1 core"
    memory: "2GB"
    instances: 2
    priority: "high"

  common_service:
    cpu: "2 cores"
    memory: "4GB"
    instances: 2
    priority: "high"

  # P1级业务服务
  consume_service:
    cpu: "2 cores"
    memory: "4GB"
    instances: 2
    priority: "medium"

  video_service:
    cpu: "2 cores"
    memory: "8GB"
    instances: 1
    priority: "medium"

  # P2级辅助服务
  other_services:
    cpu: "1 core"
    memory: "2GB"
    instances: 1
    priority: "low"
```

---

## 2. 环境准备与优化

### 2.1 操作系统优化

**Linux系统优化**:

```bash
#!/bin/bash
# 系统内核参数优化
cat >> /etc/sysctl.conf << EOF
# 网络优化
net.core.rmem_max = 16777216
net.core.wmem_max = 16777216
net.ipv4.tcp_rmem = 4096 87380 16777216
net.ipv4.tcp_wmem = 4096 65536 16777216
net.ipv4.tcp_congestion_control = bbr

# 文件句柄优化
fs.file-max = 1000000

# 虚拟内存优化
vm.swappiness = 10
vm.dirty_ratio = 15
vm.dirty_background_ratio = 5
EOF

# 应用优化
sysctl -p

# 用户限制优化
cat >> /etc/security/limits.conf << EOF
* soft nofile 65536
* hard nofile 65536
* soft nproc 32768
* hard nproc 32768
EOF
```

**Docker环境优化**:

```bash
#!/bin/bash
# Docker存储驱动优化
cat > /etc/docker/daemon.json << EOF
{
  "storage-driver": "overlay2",
  "storage-opts": [
    "overlay2.override_kernel_check=true"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  },
  "default-ulimits": {
    "nofile": {
      "Name": "nofile",
      "Hard": 65536,
      "Soft": 65536
    }
  },
  "max-concurrent-downloads": 10,
  "max-concurrent-uploads": 5
}
EOF

systemctl restart docker
```

### 2.2 Kubernetes集群优化

**Master节点优化**:

```yaml
# kubelet配置优化
cat > /etc/kubernetes/kubelet-config.yaml << EOF
apiVersion: kubelet.config.k8s.io/v1beta1
kind: KubeletConfiguration
maxPods: 200
podPidsLimit: 200
maxOpenFiles: 100000
kubeAPIQPS: 50
kubeAPIBurst: 100
serializeImagePulls: false
imagePullProgressDeadline: "10m"
evictionHard:
  memory.available: "200Mi"
  nodefs.available: "10%"
  nodefs.inodesFree: "5%"
EOF
```

**资源配额优化**:

```yaml
# namespace资源配额
apiVersion: v1
kind: ResourceQuota
metadata:
  name: ioedream-quota
  namespace: ioedream
spec:
  hard:
    requests.cpu: "20"
    requests.memory: 48Gi
    limits.cpu: "24"
    limits.memory: 64Gi
    persistentvolumeclaims: "10"
    services: "20"
    secrets: "20"
    configmaps: "20"
```

---

## 3. 微服务部署策略

### 3.1 容器镜像优化

**多阶段构建优化**:

```dockerfile
# Java应用优化Dockerfile
FROM openjdk:21-jre-slim as builder

# 构建阶段
WORKDIR /app
COPY target/*.jar app.jar

# 运行阶段 - 最小化镜像
FROM eclipse-temurin:21-jre-alpine

# 安装必要工具
RUN apk add --no-cache curl jq tzdata && \
    rm -rf /var/cache/apk/*

# 设置时区
ENV TZ=Asia/Shanghai

# 创建应用用户
RUN addgroup -g 1000 appgroup && \
    adduser -D -u 1000 -G appgroup appuser

WORKDIR /app

# 只复制必要文件
COPY --from=builder /app/app.jar app.jar
COPY --from=builder /app/BOOT-INF/lib ./lib
COPY --from=builder /app/BOOT-INF/classes ./classes

# 设置权限
RUN chown -R appuser:appgroup /app

# 切换到应用用户
USER appuser

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${server.port}/actuator/health || exit 1

# 启动应用
EXPOSE ${server.port}
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

**镜像大小优化策略**:

```bash
# 镜像清理脚本
#!/bin/bash
docker image prune -f
docker volume prune -f
docker network prune -f

# 多架构构建
docker buildx build --platform linux/amd64,linux/arm64 -t ioedream/service:latest .
```

### 3.2 Kubernetes部署配置

**网关服务部署**:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ioedream-gateway-service
  namespace: ioedream
spec:
  replicas: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  selector:
    matchLabels:
      app: ioedream-gateway-service
  template:
    metadata:
      labels:
        app: ioedream-gateway-service
    spec:
      containers:
      - name: gateway
        image: ioedream/gateway-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: JAVA_OPTS
          value: "-Xms1g -Xmx2g -XX:+UseG1GC"
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
      volumes:
      - name: config-volume
        configMap:
          name: gateway-config
      restartPolicy: Always
---
apiVersion: v1
kind: Service
metadata:
  name: ioedream-gateway-service
  namespace: ioedream
spec:
  selector:
    app: ioedream-gateway-service
  ports:
  - name: http
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

**HPA自动扩缩容**:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ioedream-gateway-hpa
  namespace: ioedream
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ioedream-gateway-service
  minReplicas: 2
  maxReplicas: 10
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
        value: 100
        periodSeconds: 15
      - type: Pods
        value: 2
        periodSeconds: 15
      selectPolicy: Max
```

---

## 4. 性能优化配置

### 4.1 JVM调优参数

**通用JVM配置**:

```bash
# 生产环境JVM参数模板
JAVA_OPTS="-server \
-Xms${HEAP_SIZE} \
-Xmx${HEAP_SIZE} \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:+UseStringDeduplication \
-XX:+OptimizeStringConcat \
-XX:+UseCompressedOops \
-XX:+UseCompressedClassPointers \
-XX:+UnlockExperimentalVMOptions \
-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=75.0 \
-XX:InitialRAMPercentage=50.0 \
-XX:MinRAMPercentage=25.0 \
-XX:+PrintGCDetails \
-XX:+PrintGCTimeStamps \
-XX:+PrintGCDateStamps \
-XX:+UseGCLogFileRotation \
-XX:NumberOfGCLogFiles=5 \
-XX:GCLogFileSize=10M \
-Xloggc:/var/log/app/gc.log \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/var/log/app/heapdump.hprof \
-Djava.security.egd=file:/dev/./urandom \
-Dfile.encoding=UTF-8 \
-Duser.timezone=Asia/Shanghai"
```

**不同服务特定调优**:

```yaml
# gateway-service - 高并发优化
gateway_service_jvm: |
  JAVA_OPTS="-server \
  -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=150 \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+UseZGC \
  -XX:ConcGCThreads=2 \
  -Dreactor.netty.ioWorkerCount=8 \
  -Dreactor.netty.ioSelectCount=4"

# consume-service - 事务处理优化
consume_service_jvm: |
  JAVA_OPTS="-server \
  -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:+UseStringDeduplication \
  -Dspring.transaction.default-timeout=30"

# video-service - 内存密集优化
video_service_jvm: |
  JAVA_OPTS="-server \
  -Xms4g -Xmx8g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=300 \
  -XX:+UseLargePages \
  -XX:LargePageSizeInBytes=2m"
```

### 4.2 数据库连接池优化

**Druid连接池配置**:

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      # 基础配置
      initial-size: 10
      min-idle: 10
      max-active: 50
      max-wait: 60000

      # 性能配置
      query-timeout: 30
      transaction-query-timeout: 30
      queryRetry: 3
      retry-query: true

      # 监控配置
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        reset-enable: false
        login-username: admin
        login-password: ${DRUID_PASSWORD}

      # Web监控配置
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"

      # 慢查询监控
      filter:
        stat:
          enabled: true
          slow-sql-millis: 1000
          log-slow-sql: true
          merge-sql: true
        wall:
          enabled: true
          config:
            multi-statement-allow: false
```

### 4.3 Redis缓存优化

**Redis配置优化**:

```conf
# redis.conf
# 内存配置
maxmemory 2gb
maxmemory-policy allkeys-lru
maxmemory-samples 5

# 持久化配置
save 900 1
save 300 10
save 60 10000
rdbcompression yes
rdbchecksum yes

# AOF配置
appendonly yes
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb

# 网络配置
tcp-keepalive 300
timeout 0
tcp-backlog 511

# 客户端连接
maxclients 10000

# 慢查询日志
slowlog-log-slower-than 10000
slowlog-max-len 128

# 内存优化
hash-max-ziplist-entries 512
hash-max-ziplist-value 64
list-max-ziplist-size -2
set-max-intset-entries 512
zset-max-ziplist-entries 128
zset-max-ziplist-value 64
```

**Spring缓存配置**:

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用String序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // 设置key和value的序列化规则
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // 设置默认序列化器
        template.setDefaultSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues()
                .prefixCacheNameWith("ioedream:");

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
}
```

---

## 5. 安全加固配置

### 5.1 容器安全

**安全基础镜像**:

```dockerfile
# 使用安全的基础镜像
FROM eclipse-temurin:21-jre-alpine

# 创建非root用户
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 设置文件权限
COPY --chown=appuser:appgroup . /app
RUN chmod -R 755 /app && \
    chmod -R 644 /app/*.jar

# 切换到非root用户
USER appuser

# 安全扫描
RUN apk add --no-cache trivy && \
    trivy fs --exit-code 0 --severity HIGH,CRITICAL /app && \
    apk del trivy
```

**Pod安全策略**:

```yaml
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: ioedream-psp
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

### 5.2 网络安全

**NetworkPolicy配置**:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: ioedream-network-policy
  namespace: ioedream
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ioedream
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: kube-system
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
  - to:
    - namespaceSelector:
        matchLabels:
          name: database
    ports:
    - protocol: TCP
      port: 3306
    - protocol: TCP
      port: 6379
```

**Ingress TLS配置**:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ioedream-ingress
  namespace: ioedream
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    nginx.ingress.kubernetes.io/ssl-protocols: "TLSv1.2 TLSv1.3"
    nginx.ingress.kubernetes.io/ssl-ciphers: "ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - api.ioedream.com
    secretName: ioedream-tls
  rules:
  - host: api.ioedream.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: ioedream-gateway-service
            port:
              number: 80
```

### 5.3 应用安全配置

**Spring Security配置**:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'")
                )
                .frameOptions().deny()
                .xssProtection().block(true)
                .httpStrictTransportSecurity().maxAgeInSeconds(31536000).includeSubDomains(true)
            );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/static/**", "/public/**", "/error");
    }
}
```

---

## 6. 监控告警体系

### 6.1 Prometheus配置

**Prometheus配置**:

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "ioedream_rules.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

scrape_configs:
  - job_name: 'kubernetes-pods'
    kubernetes_sd_configs:
      - role: pod
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
        action: replace
        target_label: __metrics_path__
        regex: (.+)

  - job_name: 'ioedream-gateway'
    static_configs:
      - targets: ['ioedream-gateway-service:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  - job_name: 'ioedream-common-service'
    static_configs:
      - targets: ['ioedream-common-service:8088']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
```

**告警规则配置**:

```yaml
groups:
- name: ioedream_alerts
  rules:
  - alert: ServiceDown
    expr: up == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "Service {{ $labels.job }} is down"
      description: "Service {{ $labels.job }} has been down for more than 1 minute."

  - alert: HighCpuUsage
    expr: 100 - (avg by(instance) (irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High CPU usage on {{ $labels.instance }}"
      description: "CPU usage is above 80% for more than 5 minutes."

  - alert: HighMemoryUsage
    expr: (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100 > 85
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High memory usage on {{ $labels.instance }}"
      description: "Memory usage is above 85% for more than 5 minutes."

  - alert: HighErrorRate
    expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
    for: 2m
    labels:
      severity: critical
    annotations:
      summary: "High error rate for {{ $labels.job }}"
      description: "Error rate is above 10% for more than 2 minutes."
```

### 6.2 Grafana仪表板

**系统概览仪表板**:

```json
{
  "dashboard": {
    "title": "IOE-DREAM 系统概览",
    "panels": [
      {
        "title": "服务状态",
        "type": "stat",
        "targets": [
          {
            "expr": "up",
            "legendFormat": "{{ job }}"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "mappings": [
              {
                "value": "0",
                "text": "DOWN",
                "color": "red"
              },
              {
                "value": "1",
                "text": "UP",
                "color": "green"
              }
            ]
          }
        }
      },
      {
        "title": "请求率",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total[5m])",
            "legendFormat": "{{ job }} - {{ status }}"
          }
        ]
      },
      {
        "title": "响应时间",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
            "legendFormat": "95th percentile - {{ job }}"
          }
        ]
      }
    ]
  }
}
```

### 6.3 日志聚合配置

**ELK Stack配置**:

```yaml
# elasticsearch.yml
cluster.name: ioedream-logs
node.name: es-node-1
network.host: 0.0.0.0
discovery.type: single-node

# JVM内存设置
ES_JAVA_OPTS: "-Xms2g -Xmx2g"

# logstash.conf
input {
  beats {
    port => 5044
  }
}

filter {
  if [fields][service] == "ioedream" {
    json {
      source => "message"
    }

    date {
      match => [ "timestamp", "ISO8601" ]
    }

    if [level] == "ERROR" {
      mutate {
        add_tag => [ "error" ]
      }
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "ioedream-logs-%{+YYYY.MM.dd}"
  }
}
```

---

## 7. 运维管理最佳实践

### 7.1 部署流程自动化

**CI/CD流水线配置**:

```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - security
  - deploy

variables:
  DOCKER_DRIVER: overlay2
  DOCKER_TLS_CERTDIR: "/certs"

build:
  stage: build
  script:
    - mvn clean package -DskipTests
    - docker build -t $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA .
    - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
  only:
    - main
    - develop

test:
  stage: test
  script:
    - mvn test
    - mvn jacoco:report
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    reports:
      junit:
        - target/surefire-reports/TEST-*.xml
      coverage_report:
        coverage_format: cobertura
        path: target/site/cobertura/coverage.xml

security:
  stage: security
  script:
    - trivy image --exit-code 0 --severity HIGH,CRITICAL $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
    - dependency-check --project "ioedream" --scan "./target/dependency-check-report.html"

deploy_staging:
  stage: deploy
  script:
    - kubectl set image deployment/ioedream-gateway-service gateway=$CI_REGISTRY_IMAGE:$CI_COMMIT_SHA -n ioedream-staging
    - kubectl rollout status deployment/ioedream-gateway-service -n ioedream-staging
  environment:
    name: staging
  only:
    - develop

deploy_production:
  stage: deploy
  script:
    - kubectl set image deployment/ioedream-gateway-service gateway=$CI_REGISTRY_IMAGE:$CI_COMMIT_SHA -n ioedream-production
    - kubectl rollout status deployment/ioedream-gateway-service -n ioedream-production
  environment:
    name: production
  when: manual
  only:
    - main
```

### 7.2 备份与恢复策略

**数据库备份策略**:

```bash
#!/bin/bash
# MySQL自动备份脚本

BACKUP_DIR="/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="ioedream"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 全量备份
mysqldump -h $DB_HOST -u $DB_USER -p$DB_PASSWORD \
  --single-transaction \
  --routines \
  --triggers \
  --all-databases \
  --flush-logs \
  --master-data=2 \
  | gzip > $BACKUP_DIR/full_backup_$DATE.sql.gz

# 保留30天备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

# 上传到云存储
aws s3 cp $BACKUP_DIR/full_backup_$DATE.sql.gz s3://ioedream-backup/mysql/

# 记录备份日志
echo "$(date): Full backup completed: full_backup_$DATE.sql.gz" >> /var/log/backup.log
```

**Redis备份策略**:

```bash
#!/bin/bash
# Redis备份脚本

REDIS_HOST="redis"
REDIS_PORT="6379"
REDIS_PASSWORD="$REDIS_PASSWORD"
BACKUP_DIR="/backup/redis"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# 创建Redis备份
redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD \
  --rdb $BACKUP_DIR/redis_backup_$DATE.rdb

# 压缩备份文件
gzip $BACKUP_DIR/redis_backup_$DATE.rdb

# 保留7天备份
find $BACKUP_DIR -name "*.rdb.gz" -mtime +7 -delete

echo "$(date): Redis backup completed: redis_backup_$DATE.rdb.gz" >> /var/log/backup.log
```

### 7.3 配置管理

**ConfigMap配置**:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ioedream-config
  namespace: ioedream
data:
  application.yml: |
    spring:
      profiles:
        active: production

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
        distribution:
          percentiles-histogram:
            http.server.requests: true
          percentiles:
            http.server.requests: 0.5,0.9,0.95,0.99

    logging:
      level:
        net.lab1024: INFO
        org.springframework.security: DEBUG
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
        file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

**Secret配置**:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: ioedream-secrets
  namespace: ioedream
type: Opaque
data:
  # Base64编码的密码
  db-password: <base64-encoded-password>
  redis-password: <base64-encoded-password>
  jwt-secret: <base64-encoded-jwt-secret>
```

---

## 8. 故障排查指南

### 8.1 常见问题诊断

**服务启动失败**:

```bash
# 1. 检查Pod状态
kubectl get pods -n ioedream
kubectl describe pod <pod-name> -n ioedream

# 2. 查看Pod日志
kubectl logs <pod-name> -n ioedream -f

# 3. 检查资源使用
kubectl top pods -n ioedream
kubectl top nodes

# 4. 检查事件
kubectl get events -n ioedream --sort-by='.lastTimestamp'
```

**内存溢出排查**:

```bash
# 1. 查看内存使用
kubectl exec -it <pod-name> -n ioedream -- jstat -gc -t 1

# 2. 生成堆转储
kubectl exec -it <pod-name> -n ioedream -- jcmd 1 GC.run_finalization
kubectl exec -it <pod-name> -n ioedream -- jcmd 1 VM.native_memory summary

# 3. 分析堆转储文件
kubectl cp <pod-name>:/var/log/app/heapdump.hprof ./heapdump.hprof
jhat heapdump.hprof
```

**数据库连接问题**:

```bash
# 1. 检查数据库连接
kubectl exec -it <pod-name> -n ioedream -- netstat -an | grep 3306

# 2. 测试数据库连接
kubectl exec -it <pod-name> -n ioedream -- telnet $DB_HOST 3306

# 3. 查看连接池状态
curl http://<pod-ip>:8080/actuator/druid/stat.json

# 4. 检查慢查询
mysql -h $DB_HOST -u $DB_USER -p -e "SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;"
```

### 8.2 性能问题诊断

**响应时间分析**:

```bash
# 1. 查看应用性能指标
curl http://<pod-ip>:8080/actuator/metrics/http.server.requests

# 2. JVM性能分析
curl http://<pod-ip>:8080/actuator/metrics/jvm.memory.used
curl http://<pod-ip>:8080/actuator/metrics/jvm.gc.pause

# 3. 线程分析
kubectl exec -it <pod-name> -n ioedream -- jstack 1 > threads.dump

# 4. 网络延迟测试
kubectl exec -it <pod-name> -n ioedream -- ping $DB_HOST -c 10
```

### 8.3 故障恢复预案

**服务快速恢复**:

```bash
#!/bin/bash
# 快速恢复脚本

SERVICE_NAME=$1
NAMESPACE="ioedream"

echo "开始恢复服务: $SERVICE_NAME"

# 1. 重启服务
kubectl rollout restart deployment/$SERVICE_NAME -n $NAMESPACE

# 2. 等待服务就绪
kubectl rollout status deployment/$SERVICE_NAME -n $NAMESPACE --timeout=300s

# 3. 验证服务健康
kubectl wait --for=condition=ready pod -l app=$SERVICE_NAME -n $NAMESPACE --timeout=300s

# 4. 检查服务状态
kubectl get pods -l app=$SERVICE_NAME -n $NAMESPACE

echo "服务 $SERVICE_NAME 恢复完成"
```

---

## 9. 成本优化策略

### 9.1 资源使用优化

**资源请求优化**:

```yaml
# 基于实际使用情况调整资源配置
apiVersion: v1
kind: LimitRange
metadata:
  name: ioedream-limits
  namespace: ioedream
spec:
  limits:
  - default:
      cpu: "500m"
      memory: "1Gi"
    defaultRequest:
      cpu: "100m"
      memory: "128Mi"
    type: Container
  - max:
      cpu: "4"
      memory: "8Gi"
    min:
      cpu: "50m"
      memory: "64Mi"
    type: Container
```

**节点亲和性配置**:

```yaml
# 优化节点调度
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ioedream-gateway-service
  namespace: ioedream
spec:
  template:
    spec:
      affinity:
        nodeAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
            - matchExpressions:
              - key: node-type
                operator: In
                values: ["compute"]
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            preference:
              matchExpressions:
              - key: zone
                operator: In
                values: ["zone-a"]
```

### 9.2 存储成本优化

**存储类配置**:

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: ioedream-ssd
provisioner: kubernetes.io/aws-ebs
parameters:
  type: gp3
  iops: "3000"
  throughput: "125"
  fsType: ext4
reclaimPolicy: Retain
allowVolumeExpansion: true
volumeBindingMode: WaitForFirstConsumer
```

**PV生命周期管理**:

```yaml
# 自动清理过期PV
apiVersion: batch/v1
kind: CronJob
metadata:
  name: cleanup-expired-pv
  namespace: ioedream
spec:
  schedule: "0 2 * * 0"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: cleanup
            image: bitnami/kubectl:latest
            command:
            - /bin/sh
            - -c
            - |
              kubectl get pv -o json | jq '.items[] | select(.status.phase == "Released") | .metadata.name' | xargs -I {} kubectl delete pv {}
          restartPolicy: OnFailure
```

### 9.3 网络成本优化

**Ingress控制器优化**:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: ioedream-gateway-service
  namespace: ioedream
  annotations:
    # 使用内部负载均衡器节省成本
    service.beta.kubernetes.io/aws-load-balancer-internal: "true"
    # 启用跨可用区负载均衡
    service.beta.kubernetes.io/aws-load-balancer-cross-zone-load-balancing-enabled: "true"
    # 启用连接排空
    service.beta.kubernetes.io/aws-load-balancer-connection-draining-enabled: "true"
spec:
  type: LoadBalancer
  externalTrafficPolicy: Local
  loadBalancerSourceRanges:
  - 10.0.0.0/8
  - 172.16.0.0/12
  - 192.168.0.0/16
```

---

## 📊 性能指标监控

### 关键性能指标（KPI）

| 指标类别 | 指标名称 | 目标值 | 告警阈值 |
|---------|---------|--------|----------|
| **系统性能** | CPU使用率 | < 70% | > 85% |
| | 内存使用率 | < 80% | > 90% |
| | 磁盘使用率 | < 70% | > 85% |
| **应用性能** | 响应时间(P95) | < 200ms | > 500ms |
| | 吞吐量(QPS) | > 1000 | < 500 |
| | 错误率 | < 1% | > 5% |
| **数据库性能** | 连接数使用率 | < 80% | > 90% |
| | 查询响应时间 | < 100ms | > 300ms |
| | 慢查询比例 | < 1% | > 5% |
| **缓存性能** | 缓存命中率 | > 90% | < 80% |
| | 缓存响应时间 | < 10ms | > 50ms |

### 成本效益分析

```yaml
# 资源使用效率指标
resource_efficiency:
  cpu_utilization:
    target: "70-80%"
    current: "65%"
    action: "优化调度策略，提高CPU利用率"

  memory_utilization:
    target: "75-85%"
    current: "60%"
    action: "调整内存配置，避免过度分配"

  storage_efficiency:
    target: ">80%"
    current: "70%"
    action: "清理无用数据，优化存储分配"

# 成本节约目标
cost_optimization:
  monthly_target: "降低15%"
  current_saving: "8%"
  strategies:
    - "使用Spot实例处理非关键负载"
    - "启用自动扩缩容减少空闲资源"
    - "优化镜像大小减少存储成本"
    - "使用预付费实例降低单价"
```

---

## 🎯 总结与最佳实践建议

### 核心优化原则

1. **资源合理分配**
   - 基于实际负载动态调整资源配置
   - 使用HPA实现自动扩缩容
   - 避免资源过度分配

2. **性能持续优化**
   - 定期监控和分析性能指标
   - 使用JVM调优参数优化Java应用
   - 优化数据库查询和索引

3. **安全第一原则**
   - 实施多层次安全防护
   - 定期安全扫描和漏洞修复
   - 遵循最小权限原则

4. **运维自动化**
   - 实现CI/CD自动化部署
   - 建立完善的监控告警体系
   - 制定详细的故障恢复预案

### 持续改进计划

- **月度回顾**: 分析性能指标，识别优化机会
- **季度评估**: 评估架构合理性，规划技术升级
- **年度优化**: 全面系统优化，技术栈升级

---

**📞 技术支持**:
- **架构团队**: 负责架构设计和优化指导
- **运维团队**: 负责系统部署和日常维护
- **安全团队**: 负责安全加固和合规检查

**🔄 版本更新**:
- 本文档将根据技术发展和实践经验持续更新
- 建议每季度进行一次全面审查和优化

---

*本文档由IOE-DREAM架构委员会制定，为智慧园区一卡通管理平台的生产部署提供全面的最佳实践指导。*