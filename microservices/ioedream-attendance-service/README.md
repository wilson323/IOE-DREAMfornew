# IOE-DREAM 考勤管理服务

## 📋 项目概述

IOE-DREAM考勤管理服务是基于Spring Boot 3.5.8 + Spring Cloud 2025.0.0构建的现代化企业级考勤管理微服务。该服务提供完整的考勤管理功能，包括智能排班、实时计算、规则引擎、报表统计、移动端支持等核心功能模块。

### 🎯 核心功能

- ✅ **智能排班引擎**: 支持多种算法（遗传算法、贪心算法、回溯算法、启发式算法）
- ✅ **实时计算引擎**: 高性能事件驱动架构，支持实时考勤计算
- ✅ **考勤规则引擎**: 灵活可配置的规则系统，支持复杂考勤业务逻辑
- ✅ **轮班系统**: 支持三班倒、四班三倒等复杂轮班模式
- ✅ **请假管理**: 完整的请假申请、审批、销假流程
- ✅ **报表统计**: 高性能报表生成，支持多种格式导出
- ✅ **移动端支持**: 完整的移动端API，支持生物识别、位置验证等
- ✅ **销假功能**: 智能销假申请和审批流程

### 🏗️ 技术架构

- **框架**: Spring Boot 3.5.8
- **微服务**: Spring Cloud 2025.0.0
- **数据库**: MySQL 8.0 + MyBatis-Plus 3.5.15
- **缓存**: Redis + Caffeine (多级缓存)
- **消息队列**: RabbitMQ (异步处理)
- **分布式事务**: Seata (最终一致性)
- **API文档**: Swagger 3.0 / Knife4j
- **构建工具**: Maven 3.8+

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **RabbitMQ**: 3.9+

### 本地启动

1. **克隆项目**
```bash
git clone https://github.com/your-org/IOE-DREAM.git
cd IOE-DREAM/microservices/ioedream-attendance-service
```

2. **配置数据库**
```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE ioedream_attendance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行Flyway迁移
mvn flyway:migrate -Dflyway.url=jdbc:mysql://localhost:3306/ioedream_attendance \
    -Dflyway.user=root -Dflyway.password=your_password
```

3. **配置Redis**
```bash
# 启动Redis
redis-server
```

4. **启动应用**
```bash
# 开发环境启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或者使用IDE运行AttendanceServiceApplication.java
```

5. **验证启动**
```bash
# 健康检查
curl http://localhost:8091/actuator/health

# API文档
open http://localhost:8091/doc.html
```

### Docker启动

1. **构建镜像**
```bash
mvn clean package -DskipTests
docker build -t ioedream/attendance-service:latest .
```

2. **运行容器**
```bash
docker run -d \
  --name attendance-service \
  -p 8091:8091 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/ioedream_attendance \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  -e SPRING_REDIS_HOST=redis \
  ioedream/attendance-service:latest
```

## 📚 API文档

### 核心接口

#### 考勤管理
```http
POST /api/v1/attendance/clock-in     # 上班打卡
POST /api/v1/attendance/clock-out    # 下班打卡
GET  /api/v1/attendance/records      # 查询考勤记录
GET  /api/v1/attendance/statistics   # 考勤统计
```

#### 移动端接口
```http
POST /api/mobile/v1/attendance/login          # 移动端登录
POST /api/mobile/v1/attendance/clock-in       # 移动端上班打卡
POST /api/mobile/v1/attendance/clock-out      # 移动端下班打卡
GET  /api/mobile/v1/attendance/today/status   # 今日考勤状态
GET  /api/mobile/v1/attendance/health/check   # 健康检查
```

#### 排班管理
```http
POST /api/v1/schedule/generate               # 生成排班
GET  /api/v1/schedule/employee/{id}          # 员工排班
GET  /api/v1/schedule/shifts                  # 班次信息
```

#### 请假管理
```http
POST /api/v1/leave/apply                     # 请假申请
POST /api/v1/leave/approve                   # 请假审批
POST /api/v1/leave/cancel                    # 销假申请
GET  /api/v1/leave/records                   # 请假记录
```

### 完整API文档
- **Swagger UI**: http://localhost:8091/doc.html
- **OpenAPI JSON**: http://localhost:8091/v3/api-docs

## 🔧 配置说明

### 应用配置

#### application.yml
```yaml
server:
  port: 8091
  servlet:
    context-path: /

spring:
  application:
    name: ioedream-attendance-service

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ioedream_attendance?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      validation-query: SELECT 1

  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 3000
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /

  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: dev
        group: IOE-DREAM
      config:
        server-addr: localhost:8848
        namespace: dev
        group: IOE-DREAM
        file-extension: yaml

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath*:mapper/*.xml
  type-aliases-package: net.lab1024.sa.attendance.domain.entity
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      logic-delete-field: deletedFlag
      logic-delete-value: 1
      logic-not-delete-value: 0

# 考勤服务配置
attendance:
  # 实时计算引擎配置
  realtime:
    enabled: true
    event-queue-size: 10000
    batch-size: 100
    batch-timeout-ms: 5000

  # 规则引擎配置
  rule-engine:
    enabled: true
    max-rules: 100
    cache-size: 1000
    cache-ttl-minutes: 30

  # 排班引擎配置
  scheduling:
    algorithm: genetic  # genetic, greedy, backtrack, heuristic
    population-size: 100
    max-iterations: 1000
    crossover-rate: 0.8
    mutation-rate: 0.2

  # 移动端配置
  mobile:
    cache-timeout: 3600
    max-retry: 3
    async-enabled: true
    location-check-enabled: false

# 监控配置
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

# 日志配置
logging:
  level:
    net.lab1024.sa.attendance: DEBUG
    org.springframework.web: INFO
    org.springframework.security: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/attendance-service.log
    max-size: 100MB
    max-history: 30
```

### 环境配置

#### application-dev.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ioedream_attendance_dev
  redis:
    host: localhost

attendance:
  realtime:
    enabled: true
    debug: true
```

#### application-prod.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://prod-mysql:3306/ioedream_attendance
  redis:
    cluster:
      nodes:
        - redis1:6379
        - redis2:6379
        - redis3:6379

attendance:
  realtime:
    enabled: true
    event-queue-size: 50000
  mobile:
    location-check-enabled: true
```

## 🧪 测试

### 单元测试
```bash
# 运行所有单元测试
mvn test

# 运行特定测试类
mvn test -Dtest=AttendanceServiceTest

# 运行测试并生成覆盖率报告
mvn test jacoco:report
```

### 集成测试
```bash
# 运行集成测试
mvn test -Dtest=**/*IntegrationTest

# 运行性能测试
mvn test -Dtest=**/*PerformanceTest
```

### 测试覆盖率报告
访问：`target/site/jacoco/index.html`

## 📊 性能指标

### 基准性能

| 接口类型 | TPS | 平均响应时间 | 95%响应时间 | 成功率 |
|---------|-----|--------------|--------------|--------|
| 移动端登录 | 100+ | <200ms | <500ms | >95% |
| 移动端打卡 | 200+ | <150ms | <300ms | >98% |
| 实时计算 | 500+ | <100ms | <200ms | >99% |
| 规则引擎 | 1000+ | <50ms | <100ms | >99.5% |

### 资源使用

| 资源类型 | 基准值 | 预警值 | 最大值 |
|---------|--------|--------|--------|
| CPU使用率 | 30% | 70% | 85% |
| 内存使用率 | 40% | 70% | 85% |
| 数据库连接池 | 50% | 80% | 90% |
| 缓存命中率 | 85% | 75% | 60% |

## 🔧 开发指南

### 代码规范

项目严格遵循以下规范：
- **CLAUDE.md全局架构规范**
- **四层架构**: Controller → Service → Manager → DAO
- **依赖注入**: 统一使用 `@Resource`
- **数据访问**: 统一使用 `@Mapper` + `BaseMapper`
- **异常处理**: 统一 `GlobalExceptionHandler`

### 分支管理

```bash
# 功能开发分支
git checkout -b feature/intelligent-scheduling

# 提交代码
git add .
git commit -m "feat: 实现智能排班引擎"

# 推送分支
git push origin feature/intelligent-scheduling
```

### 代码审查

所有代码必须经过以下审查：
1. **架构合规性检查**
2. **代码质量检查**
3. **单元测试覆盖率检查**
4. **性能影响评估**

## 🚀 部署指南

### Kubernetes部署

#### deployment.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: attendance-service
  labels:
    app: attendance-service
spec:
  replicas: 3
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
        image: ioedream/attendance-service:latest
        ports:
        - containerPort: 8091
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: url
        - name: SPRING_REDIS_HOST
          value: "redis-cluster"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
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

#### service.yaml
```yaml
apiVersion: v1
kind: Service
metadata:
  name: attendance-service
spec:
  selector:
    app: attendance-service
  ports:
  - protocol: TCP
    port: 8091
    targetPort: 8091
  type: ClusterIP
```

### 监控配置

#### Prometheus配置
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'attendance-service'
    static_configs:
    - targets: ['attendance-service:8091']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
```

#### Grafana Dashboard
- **JVM监控**: 内存、GC、线程池
- **业务监控**: TPS、响应时间、成功率
- **系统监控**: CPU、内存、网络、磁盘

## 🔐 安全配置

### 认证授权

```yaml
security:
  jwt:
    secret: your-jwt-secret
    expiration: 86400
    refresh-expiration: 604800

  oauth2:
    client:
      client-id: attendance-service
      client-secret: your-client-secret
      access-token-uri: http://auth-service/oauth/token
      user-authorization-uri: http://auth-service/oauth/authorize
```

### 数据安全

```yaml
attendance:
  security:
    # 数据脱敏配置
    data-masking:
      enabled: true
      phone-mask: "****"
      email-mask: "***@***"

    # 接口加密配置
    api-encryption:
      enabled: true
      algorithm: AES256
      key: your-encryption-key

    # 审计日志配置
    audit:
      enabled: true
      log-sensitive-data: false
      retention-days: 90
```

## 📋 问题排查

### 常见问题

#### 1. 启动失败
```bash
# 检查端口占用
netstat -tulpn | grep 8091

# 检查数据库连接
telnet localhost 3306

# 检查Redis连接
redis-cli ping
```

#### 2. 性能问题
```bash
# 查看JVM堆内存
jstat -gc <pid>

# 查看线程堆栈
jstack <pid>

# 查看内存分布
jmap -histo <pid>
```

#### 3. 数据库问题
```sql
-- 检查连接数
SHOW PROCESSLIST;

-- 检查慢查询
SHOW VARIABLES LIKE 'slow_query_log';

-- 检查表结构
DESC t_attendance_record;
```

### 日志分析

```bash
# 查看应用日志
tail -f logs/attendance-service.log

# 查看错误日志
grep ERROR logs/attendance-service.log

# 分析访问日志
grep "POST /api/v1/attendance/clock-in" logs/access.log
```

## 📚 相关文档

- [CLAUDE.md 全局架构规范](../CLAUDE.md)
- [微服务统一开发规范](../UNIFIED_MICROSERVICES_STANDARDS.md)
- [API设计规范](../../documentation/api/API_DESIGN_STANDARDS.md)
- [数据库设计规范](../../documentation/technical/DATABASE_DESIGN_STANDARDS.md)
- [部署运维手册](../../documentation/deployment/DEPLOYMENT_GUIDE.md)

## 🤝 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

## 👥 团队

- **架构师**: SmartAdmin团队
- **开发团队**: IOE-DREAM开发团队
- **测试团队**: 质量保障团队

## 📞 联系我们

- **邮箱**: dev@ioedream.com
- **Issues**: [GitHub Issues](https://github.com/your-org/IOE-DREAM/issues)
- **文档**: [项目文档](https://docs.ioedream.com)

---

**🚀 让我们一起构建更智能的考勤管理系统！**