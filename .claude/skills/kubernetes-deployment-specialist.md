# Kubernetes部署专家

> **版本**: v1.0.0
> **更新时间**: 2025-11-21
> **分类**: 云原生技能 > 容器编排
> **标签**: ["Kubernetes", "容器编排", "云原生", "CI/CD", "服务网格"]
> **技能等级**: ★★★ 专家级
> **适用角色**: DevOps工程师、云原生架构师、高级运维工程师
> **前置技能**: docker-optimization-specialist, microservices-architecture-specialist
> **预计学时**: 60-80小时

---

## 📋 技能概述

本技能专注于IOE-DREAM项目的Kubernetes部署、运维和管理，基于Java 17 + Spring Boot 3.x + Jakarta技术栈，提供完整的云原生部署解决方案。涵盖应用部署、服务编排、监控告警、故障恢复等全流程管理。

**技术基础**: Kubernetes 1.28.x + Docker + Helm + Istio
**核心目标**: 构建高可用、可扩展、自愈的云原生应用体系

---

## 🏗️ Kubernetes集群架构

### 1. 集群规划与设计

#### 生产环境集群架构
```yaml
# cluster-architecture.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: cluster-config
  namespace: kube-system
data:
  architecture: |
    cluster:
      name: ioe-dream-prod
      version: "1.28.2"
      region: cn-north-1
      zones: [cn-north-1a, cn-north-1b, cn-north-1c]

    master-nodes:
      - name: master-01
        zone: cn-north-1a
        specs:
          cpu: "4"
          memory: "8Gi"
          storage: "100Gi"
      - name: master-02
        zone: cn-north-1b
        specs:
          cpu: "4"
          memory: "8Gi"
          storage: "100Gi"
      - name: master-03
        zone: cn-north-1c
        specs:
          cpu: "4"
          memory: "8Gi"
          storage: "100Gi"

    worker-nodes:
      - name: worker-01
        zone: cn-north-1a
        specs:
          cpu: "8"
          memory: "16Gi"
          storage: "200Gi"
        labels:
          node-type: application
      - name: worker-02
        zone: cn-north-1b
        specs:
          cpu: "8"
          memory: "16Gi"
          storage: "200Gi"
        labels:
          node-type: application
      - name: worker-03
        zone: cn-north-1c
        specs:
          cpu: "8"
          memory: "16Gi"
          storage: "200Gi"
        labels:
          node-type: application
```

#### 命名空间规划
```yaml
# namespaces.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: ioe-dream-prod
  labels:
    environment: production
    project: ioe-dream
---
apiVersion: v1
kind: Namespace
metadata:
  name: ioe-dream-test
  labels:
    environment: testing
    project: ioe-dream
---
apiVersion: v1
kind: Namespace
metadata:
  name: ioe-dream-dev
  labels:
    environment: development
    project: ioe-dream
---
apiVersion: v1
kind: Namespace
metadata:
  name: ioe-dream-monitoring
  labels:
    environment: production
    project: ioe-dream
    purpose: monitoring
```

### 2. 存储管理配置

#### 存储类定义
```yaml
# storage-classes.yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: ioe-dream-ssd
  labels:
    type: ssd
provisioner: kubernetes.io/aws-ebs
parameters:
  type: gp3
  iops: "3000"
  throughput: "125"
  fsType: ext4
reclaimPolicy: Retain
allowVolumeExpansion: true
---
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: ioe-dream-standard
  labels:
    type: standard
provisioner: kubernetes.io/aws-ebs
parameters:
  type: gp2
  fsType: ext4
reclaimPolicy: Delete
allowVolumeExpansion: true
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
  namespace: ioe-dream-prod
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: ioe-dream-ssd
  resources:
    requests:
      storage: 100Gi
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: redis-pvc
  namespace: ioe-dream-prod
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: ioe-dream-ssd
  resources:
    requests:
      storage: 20Gi
```

---

## 🚀 应用部署配置

### 1. Spring Boot应用部署

#### 用户服务部署配置
```yaml
# user-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: ioe-dream-prod
  labels:
    app: user-service
    version: v1.0.0
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  selector:
    matchLabels:
      app: user-service
      version: v1.0.0
  template:
    metadata:
      labels:
        app: user-service
        version: v1.0.0
        metrics: enabled
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8081"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
      nodeSelector:
        node-type: application
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              podAffinityTerm:
                labelSelector:
                  matchExpressions:
                    - key: app
                      operator: In
                      values:
                        - user-service
                topologyKey: kubernetes.io/hostname
      containers:
      - name: user-service
        image: ioe-dream/user-service:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - name: http
          containerPort: 8080
          protocol: TCP
        - name: management
          containerPort: 8081
          protocol: TCP
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: NACOS_SERVER_ADDR
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: nacos.server.addr
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: hostname
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        - name: REDIS_HOST
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: redis.host
        - name: JAVA_OPTS
          value: "-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: management
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
          successThreshold: 1
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: management
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
          successThreshold: 1
        startupProbe:
          httpGet:
            path: /actuator/health
            port: management
          initialDelaySeconds: 10
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 30
          successThreshold: 1
        volumeMounts:
        - name: log-volume
          mountPath: /app/logs
        - name: config-volume
          mountPath: /app/config
          readOnly: true
      - name: log-collector
        image: fluent/fluent-bit:2.2.0
        resources:
          requests:
            memory: "64Mi"
            cpu: "50m"
          limits:
            memory: "128Mi"
            cpu: "100m"
        volumeMounts:
        - name: log-volume
          mountPath: /app/logs
          readOnly: true
        - name: fluent-bit-config
          mountPath: /fluent-bit/etc/
      volumes:
      - name: log-volume
        emptyDir: {}
      - name: config-volume
        configMap:
          name: user-service-config
      - name: fluent-bit-config
        configMap:
          name: fluent-bit-config
---
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: ioe-dream-prod
  labels:
    app: user-service
spec:
  type: ClusterIP
  ports:
  - name: http
    port: 8080
    targetPort: http
    protocol: TCP
  - name: management
    port: 8081
    targetPort: management
    protocol: TCP
  selector:
    app: user-service
---
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: user-service-pdb
  namespace: ioe-dream-prod
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: user-service
```

### 2. 配置管理

#### 应用配置文件
```yaml
# user-service-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: user-service-config
  namespace: ioe-dream-prod
data:
  application.yml: |
    server:
      port: 8080
      tomcat:
        max-threads: 200
        min-spare-threads: 10

    spring:
      application:
        name: user-service

      cloud:
        nacos:
          discovery:
            server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
            namespace: prod
            group: IOE-DREAM
          config:
            server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
            namespace: prod
            group: IOE-DREAM
            file-extension: yaml

      datasource:
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://${DB_HOST:mysql-service}:3306/ioe_dream_user?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
        hikari:
          maximum-pool-size: 20
          minimum-idle: 5
          idle-timeout: 300000
          connection-timeout: 20000
          leak-detection-threshold: 60000

      redis:
        host: ${REDIS_HOST:redis-service}
        port: 6379
        timeout: 2000ms
        lettuce:
          pool:
            max-active: 20
            max-idle: 10
            min-idle: 5
            max-wait: 2000ms

    management:
      endpoints:
        web:
          exposure:
            include: "health,info,metrics,prometheus"
      endpoint:
        health:
          show-details: always
      metrics:
    export:
      prometheus:
        enabled: true

    logging:
      level:
        net.lab1024.sa: INFO
        org.springframework.cloud: INFO
        com.alibaba.nacos: WARN
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-}] %logger{36} - %msg%n"
        file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-}] %logger{36} - %msg%n"
      file:
        name: /app/logs/user-service.log
        max-size: 100MB
        max-history: 30

  logback-spring.xml: |
    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>
        <springProfile name="prod">
            <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
                <file>/app/logs/user-service.log</file>
                <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                    <fileNamePattern>/app/logs/user-service.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                    <maxFileSize>100MB</maxFileSize>
                    <maxHistory>30</maxHistory>
                    <totalSizeCap>3GB</totalSizeCap>
                </rollingPolicy>
                <encoder>
                    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-}] %logger{36} - %msg%n</pattern>
                </encoder>
            </appender>

            <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
                <encoder>
                    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-}] %logger{36} - %msg%n</pattern>
                </encoder>
            </appender>

            <root level="INFO">
                <appender-ref ref="FILE"/>
                <appender-ref ref="STDOUT"/>
            </root>
        </springProfile>
    </configuration>
```

### 3. 密钥管理

#### 数据库凭据
```yaml
# db-credentials-secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
  namespace: ioe-dream-prod
type: Opaque
data:
  hostname: bXlzcWwtc2VydmljZQ==  # mysql-service (base64)
  username: cm9vdA==              # root (base64)
  password: MWYyZGM1MjUzZmFjNjY=  # 1f2dc5253fac66 (base64)
  database: aW9lX2RyZWFtX3VzZXI=  # ioe_dream_user (base64)
---
apiVersion: v1
kind: Secret
metadata:
  name: redis-credentials
  namespace: ioe-dream-prod
type: Opaque
data:
  password: ""  # 空密码
---
apiVersion: v1
kind: Secret
metadata:
  name: jwt-secret
  namespace: ioe-dream-prod
type: Opaque
data:
  secret: bXlfc2VjcmV0X2tleV9mb3Jfand0X3Rva2VuX2FuZF9pbm5vdmF0aW9uX3B1cnBvc2Vz  # my_secret_key_for_jwt_token_and_innovation_purposes (base64)
```

---

## 🔧 服务网格配置

### 1. Istio服务网格部署

#### Istio配置文件
```yaml
# istio-installation.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
metadata:
  name: ioe-dream-istio
  namespace: istio-system
spec:
  profile: production
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: ioe-dream-prod
      network: network1

    components:
      pilot:
        k8s:
          resources:
            requests:
              cpu: 500m
              memory: 2Gi
            limits:
              cpu: 1000m
              memory: 4Gi

      ingressGateways:
      - name: istio-ingressgateway
        enabled: true
        k8s:
          resources:
            requests:
              cpu: 500m
              memory: 2Gi
          service:
            type: LoadBalancer
            ports:
            - port: 80
              targetPort: 8080
              name: http
            - port: 443
              targetPort: 8443
              name: https

      egressGateways:
      - name: istio-egressgateway
        enabled: true
        k8s:
          resources:
            requests:
              cpu: 200m
              memory: 1Gi

---
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: ioe-dream-gateway
  namespace: istio-system
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "ioe-dream.example.com"
    tls:
      httpsRedirect: true
  - port:
      number: 443
      name: https
      protocol: HTTPS
    tls:
      mode: SIMPLE
      credentialName: ioe-dream-tls
    hosts:
    - "ioe-dream.example.com"

---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: ioe-dream-vs
  namespace: istio-system
spec:
  hosts:
  - "ioe-dream.example.com"
  gateways:
  - ioe-dream-gateway
  http:
  - match:
    - uri:
        prefix: /api/v1/users
    route:
    - destination:
        host: user-service
        port:
          number: 8080
      weight: 100
  - match:
    - uri:
        prefix: /api/v1/consume
    route:
    - destination:
        host: consume-service
        port:
          number: 8080
      weight: 100
```

### 2. 服务间通信配置

#### Sidecar注入配置
```yaml
# sidecar-injection.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: ioe-dream-prod
  labels:
    istio-injection: enabled
---
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: user-service
  namespace: ioe-dream-prod
spec:
  host: user-service
  trafficPolicy:
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http1MaxPendingRequests: 50
        maxRequestsPerConnection: 10
    loadBalancer:
      simple: LEAST_CONN
    outlierDetection:
      consecutiveErrors: 3
      interval: 30s
      baseEjectionTime: 30s
---
apiVersion: networking.istio.io/v1alpha3
kind: ServiceEntry
metadata:
  name: external-mysql
  namespace: ioe-dream-prod
spec:
  hosts:
  - mysql.external.com
  ports:
  - number: 3306
    name: mysql
    protocol: TCP
  resolution: DNS
```

---

## 📊 监控与可观测性

### 1. Prometheus监控配置

#### Prometheus配置
```yaml
# prometheus-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: ioe-dream-monitoring
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s
      external_labels:
        cluster: ioe-dream-prod
        region: cn-north-1

    rule_files:
    - "/etc/prometheus/rules/*.yml"

    alerting:
      alertmanagers:
      - static_configs:
        - targets:
          - alertmanager:9093

    scrape_configs:
    - job_name: 'kubernetes-apiservers'
      kubernetes_sd_configs:
      - role: endpoints
      scheme: https
      tls_config:
        ca_file: /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
      bearer_token_file: /var/run/secrets/kubernetes.io/serviceaccount/token
      relabel_configs:
      - source_labels: [__meta_kubernetes_namespace, __meta_kubernetes_service_name, __meta_kubernetes_endpoint_port_name]
        action: keep
        regex: default;kubernetes;https

    - job_name: 'kubernetes-nodes'
      kubernetes_sd_configs:
      - role: node
      relabel_configs:
      - action: labelmap
        regex: __meta_kubernetes_node_label_(.+)
      - target_label: __address__
        replacement: kubernetes.default.svc:443
      - source_labels: [__meta_kubernetes_node_name]
        regex: (.+)
        target_label: __metrics_path__
        replacement: /api/v1/nodes/${1}/proxy/metrics

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
      - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
        action: replace
        regex: ([^:]+)(?::\d+)?;(\d+)
        replacement: $1:$2
        target_label: __address__
      - action: labelmap
        regex: __meta_kubernetes_pod_label_(.+)
      - source_labels: [__meta_kubernetes_namespace]
        action: replace
        target_label: kubernetes_namespace
      - source_labels: [__meta_kubernetes_pod_name]
        action: replace
        target_label: kubernetes_pod_name

  alert_rules.yml: |
    groups:
    - name: ioe-dream-alerts
      rules:
      - alert: ServiceDown
        expr: up{job="kubernetes-pods"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Service {{ $labels.kubernetes_pod_name }} is down"
          description: "Service {{ $labels.kubernetes_pod_name }} in namespace {{ $labels.kubernetes_namespace }} has been down for more than 1 minute."

      - alert: HighMemoryUsage
        expr: container_memory_usage_bytes / container_spec_memory_limit_bytes * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage on {{ $labels.kubernetes_pod_name }}"
          description: "Memory usage is above 80% on pod {{ $labels.kubernetes_pod_name }} in namespace {{ $labels.kubernetes_namespace }}."

      - alert: HighCPUUsage
        expr: rate(container_cpu_usage_seconds_total[5m]) * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High CPU usage on {{ $labels.kubernetes_pod_name }}"
          description: "CPU usage is above 80% on pod {{ $labels.kubernetes_pod_name }} in namespace {{ $labels.kubernetes_namespace }}."
```

### 2. Grafana仪表板配置

#### 应用监控仪表板
```json
{
  "dashboard": {
    "id": null,
    "title": "IOE-DREAM应用监控",
    "tags": ["ioe-dream", "spring-boot", "kubernetes"],
    "timezone": "browser",
    "panels": [
      {
        "title": "应用实例状态",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=\"kubernetes-pods\", app=~\"user-service|consume-service|access-control-service\"}",
            "legendFormat": "{{kubernetes_pod_name}}"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "color": {
              "mode": "thresholds"
            },
            "thresholds": {
              "steps": [
                {"color": "red", "value": 0},
                {"color": "green", "value": 1}
              ]
            },
            "mappings": [
              {"options": {"0": {"text": "DOWN", "color": "red"}}, "type": "value"},
              {"options": {"1": {"text": "UP", "color": "green"}}, "type": "value"}
            ]
          }
        }
      },
      {
        "title": "请求速率",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{job=\"kubernetes-pods\", app=~\"user-service|consume-service\"}[5m])",
            "legendFormat": "{{app}} - {{uri}}"
          }
        ]
      },
      {
        "title": "响应时间",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{job=\"kubernetes-pods\", app=~\"user-service|consume-service\"}[5m]))",
            "legendFormat": "{{app}} - 95th percentile"
          }
        ]
      },
      {
        "title": "错误率",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{job=\"kubernetes-pods\", app=~\"user-service|consume-service\", outcome=\"ERROR\"}[5m]) / rate(http_server_requests_seconds_count{job=\"kubernetes-pods\", app=~\"user-service|consume-service\"}[5m]) * 100",
            "legendFormat": "{{app}} - Error Rate"
          }
        ]
      }
    ],
    "time": {
      "from": "now-1h",
      "to": "now"
    },
    "refresh": "30s"
  }
}
```

---

## 🚀 CI/CD自动化部署

### 1. GitLab CI/CD配置

#### 部署流水线
```yaml
# .gitlab-ci.yml
stages:
  - test
  - build
  - deploy-dev
  - deploy-test
  - deploy-prod

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
  DOCKER_REGISTRY: registry.example.com
  KUBERNETES_NAMESPACE_DEV: ioe-dream-dev
  KUBERNETES_NAMESPACE_TEST: ioe-dream-test
  KUBERNETES_NAMESPACE_PROD: ioe-dream-prod

cache:
  paths:
    - .m2/repository/
    - target/

test:
  stage: test
  image: maven:3.9.4-openjdk-17
  script:
    - mvn clean test
    - mvn jacoco:report
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    reports:
      junit:
        - target/surefire-reports/TEST-*.xml
      coverage_report:
        coverage_format: cobertura
        path: target/site/cobertura/coverage.xml

build:
  stage: build
  image: docker:20.10.16
  services:
    - docker:20.10.16-dind
  script:
    - |
      # 构建应用
      docker build -t $DOCKER_REGISTRY/ioe-dream/user-service:$CI_COMMIT_SHA .
      docker tag $DOCKER_REGISTRY/ioe-dream/user-service:$CI_COMMIT_SHA $DOCKER_REGISTRY/ioe-dream/user-service:latest

      # 推送镜像
      echo $DOCKER_REGISTRY_PASSWORD | docker login $DOCKER_REGISTRY -u $DOCKER_REGISTRY_USERNAME --password-stdin
      docker push $DOCKER_REGISTRY/ioe-dream/user-service:$CI_COMMIT_SHA
      docker push $DOCKER_REGISTRY/ioe-dream/user-service:latest
  only:
    - main
    - develop

deploy-dev:
  stage: deploy-dev
  image: bitnami/kubectl:latest
  script:
    - |
      # 更新镜像标签
      sed -i "s|image: ioe-dream/user-service:.*|image: $DOCKER_REGISTRY/ioe-dream/user-service:$CI_COMMIT_SHA|g" k8s/dev/user-service-deployment.yaml

      # 部署到开发环境
      kubectl apply -f k8s/dev/ -n $KUBERNETES_NAMESPACE_DEV

      # 等待部署完成
      kubectl rollout status deployment/user-service -n $KUBERNETES_NAMESPACE_DEV --timeout=300s
  environment:
    name: development
    url: https://dev.ioe-dream.example.com
  only:
    - develop

deploy-test:
  stage: deploy-test
  image: bitnami/kubectl:latest
  script:
    - |
      # 运行集成测试
      kubectl apply -f k8s/test/ -n $KUBERNETES_NAMESPACE_TEST
      kubectl rollout status deployment/user-service -n $KUBERNETES_NAMESPACE_TEST --timeout=300s

      # 执行集成测试
      kubectl run integration-test --image=curlimages/curl --rm -i --restart=Never -- \
        curl -f http://user-service.$KUBERNETES_NAMESPACE_TEST.svc.cluster.local:8080/actuator/health
  environment:
    name: testing
    url: https://test.ioe-dream.example.com
  only:
    - main

deploy-prod:
  stage: deploy-prod
  image: bitnami/kubectl:latest
  script:
    - |
      # 生产环境部署 - 采用蓝绿部署
      kubectl apply -f k8s/prod/ -n $KUBERNETES_NAMESPACE_PROD

      # 检查部署状态
      kubectl rollout status deployment/user-service -n $KUBERNETES_NAMESPACE_PROD --timeout=600s

      # 健康检查
      for i in {1..10}; do
        if kubectl get pods -n $KUBERNETES_NAMESPACE_PROD -l app=user-service | grep -q "1/1"; then
          echo "Health check passed"
          break
        fi
        sleep 30
      done
  environment:
    name: production
    url: https://ioe-dream.example.com
  when: manual
  only:
    - main
```

### 2. Helm Chart配置

#### 用户服务Chart
```yaml
# charts/user-service/Chart.yaml
apiVersion: v2
name: user-service
description: A Helm chart for IOE-DREAM User Service
type: application
version: 1.0.0
appVersion: "1.0.0"

# charts/user-service/values.yaml
replicaCount: 3

image:
  repository: ioe-dream/user-service
  pullPolicy: IfNotPresent
  tag: "1.0.0"

nameOverride: ""
fullnameOverride: ""

serviceAccount:
  create: true
  annotations: {}
  name: ""

podAnnotations:
  prometheus.io/scrape: "true"
  prometheus.io/port: "8081"
  prometheus.io/path: "/actuator/prometheus"

podSecurityContext:
  fsGroup: 1000

securityContext:
  allowPrivilegeEscalation: false
  runAsNonRoot: true
  runAsUser: 1000
  capabilities:
    drop:
    - ALL

service:
  type: ClusterIP
  port: 8080
  managementPort: 8081

ingress:
  enabled: true
  className: "nginx"
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/rewrite-target: /
  hosts:
    - host: user-service.ioe-dream.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: user-service-tls
      hosts:
        - user-service.ioe-dream.example.com

resources:
  limits:
    cpu: 500m
    memory: 1Gi
  requests:
    cpu: 250m
    memory: 512Mi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80

nodeSelector: {}
tolerations: []
affinity:
  podAntiAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
    - weight: 100
      podAffinityTerm:
        labelSelector:
          matchExpressions:
          - key: app.kubernetes.io/name
            operator: In
            values:
            - user-service
        topologyKey: kubernetes.io/hostname

# charts/user-service/templates/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "user-service.fullname" . }}
  labels:
    {{- include "user-service.labels" . | nindent 4 }}
spec:
  {{- if not .Values.autoscaling.enabled }}
  replicas: {{ .Values.replicaCount }}
  {{- end }}
  selector:
    matchLabels:
      {{- include "user-service.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      annotations:
        checksum/config: {{ include (print $.Template.BasePath "/configmap.yaml") . | sha256sum }}
        {{- with .Values.podAnnotations }}
        {{- toYaml . | nindent 8 }}
        {{- end }}
      labels:
        {{- include "user-service.selectorLabels" . | nindent 8 }}
    spec:
      {{- with .Values.imagePullSecrets }}
      imagePullSecrets:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      serviceAccountName: {{ include "user-service.serviceAccountName" . }}
      securityContext:
        {{- toYaml .Values.podSecurityContext | nindent 8 }}
      containers:
        - name: {{ .Chart.Name }}
          securityContext:
            {{- toYaml .Values.securityContext | nindent 12 }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - name: http
              containerPort: 8080
              protocol: TCP
            - name: management
              containerPort: 8081
              protocol: TCP
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: management
            initialDelaySeconds: 60
            periodSeconds: 30
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: management
            initialDelaySeconds: 30
            periodSeconds: 10
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: {{ .Values.spring.profiles.active | quote }}
            - name: NACOS_SERVER_ADDR
              value: {{ .Values.nacos.serverAddr | quote }}
            - name: DB_HOST
              valueFrom:
                secretKeyRef:
                  name: {{ .Values.db.secret.name }}
                  key: {{ .Values.db.secret.hostnameKey }}
            {{- if .Values.extraEnv }}
            {{- toYaml .Values.extraEnv | nindent 12 }}
            {{- end }}
          volumeMounts:
            - name: tmp
              mountPath: /tmp
      volumes:
        - name: tmp
          emptyDir: {}
      {{- with .Values.nodeSelector }}
      nodeSelector:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with .Values.affinity }}
      affinity:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with .Values.tolerations }}
      tolerations:
        {{- toYaml . | nindent 8 }}
      {{- end }}
```

---

## ⚠️ 最佳实践与注意事项

### ✅ 推荐实践

1. **资源管理**
   - 合理设置资源请求和限制
   - 使用PodDisruptionBudget保障可用性
   - 实施自动扩缩容策略

2. **配置管理**
   - 使用ConfigMap管理应用配置
   - 敏感信息使用Secret管理
   - 环境隔离和配置版本控制

3. **安全防护**
   - 使用RBAC进行权限控制
   - 启用网络策略限制流量
   - 定期更新镜像和依赖

4. **监控告警**
   - 完善的监控指标收集
   - 合理的告警阈值设置
   - 建立应急响应流程

### ❌ 避免的陷阱

1. **部署问题**
   - 避免在容器中存储重要数据
   - 不要使用latest标签
   - 避免单点故障

2. **性能问题**
   - 避免资源过度分配
   - 不要忽视健康检查配置
   - 避免不合理的镜像层结构

3. **运维问题**
   - 不要在生产环境使用kubectl apply --force
   - 避免手动修改运行时配置
   - 不要忽视日志收集和管理

---

## 📊 评估标准

### 🎯 技能掌握评估

#### 理论知识 (30%)
- [ ] Kubernetes核心概念和架构
- [ ] 容器编排和服务网格原理
- [ ] 云原生监控和可观测性
- [ ] DevOps和CI/CD最佳实践

#### 实践能力 (50%)
- [ ] 能够部署和管理Kubernetes集群
- [ ] 熟练使用Helm进行应用打包和部署
- [ ] 能够配置监控告警系统
- [ ] 掌握故障排查和性能优化

#### 问题解决 (20%)
- [ ] 集群故障恢复
- [ ] 应用性能调优
- [ ] 安全漏洞修复
- [ ] 成本优化策略

### 📈 质量标准

- **服务可用性**: > 99.9%
- **部署成功率**: > 99%
- **故障恢复时间**: < 5分钟
- **监控覆盖度**: 100%

---

## 🔗 相关技能

- **前置技能**: docker-optimization-specialist, microservices-architecture-specialist
- **相关技能**: service-mesh-specialist, monitoring-alerting-specialist
- **进阶技能**: cloud-architecture-specialist, devops-expert

---

## 💡 持续学习方向

1. **多云管理**: 跨云Kubernetes集群管理
2. **Serverless**: Knative和Serverless架构
3. **AI运维**: AIOps和智能化运维
4. **边缘计算**: 边缘Kubernetes部署

---

**⚠️ 重要提醒**: Kubernetes部署需要严格遵循IOE-DREAM项目的安全规范和运维标准，确保生产环境的稳定性和安全性。