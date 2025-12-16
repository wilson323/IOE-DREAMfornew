# 统一配置源使用指南

> **创建日期**: 2025-12-14  
> **目的**: 使用Nacos配置中心作为统一配置源，避免在本地配置文件中硬编码默认值  
> **状态**: ✅ 已提供方案

---

## 📋 统一配置源原则

### 核心原则

**所有配置应该从Nacos配置中心加载，本地配置文件仅作为兜底方案**

1. **优先使用Nacos配置中心**: 所有服务的数据库、Redis、Nacos等配置应该从Nacos配置中心加载
2. **本地配置作为兜底**: 本地`application.yml`中的默认值仅用于开发环境或Nacos不可用时的兜底
3. **环境变量覆盖**: 环境变量优先级最高，可以覆盖Nacos配置和本地配置

### 配置优先级（从高到低）

```
环境变量 > Nacos配置中心 > 本地application.yml默认值
```

---

## 🏗️ Nacos配置中心配置结构

### 共享配置（所有服务共用）

在Nacos配置中心创建以下共享配置：

#### 1. `common-database.yaml` (共享数据库配置)

```yaml
# 数据库配置（所有服务共享）
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${MYSQL_HOST:127.0.0.1}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:ioedream}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:123456}  # 从环境变量读取，默认123456
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      validation-query: SELECT 1
      test-while-idle: true
```

#### 2. `common-redis.yaml` (共享Redis配置)

```yaml
# Redis配置（所有服务共享）
spring:
  data:
    redis:
      host: ${REDIS_HOST:127.0.0.1}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:redis123}  # 从环境变量读取，默认redis123
      database: ${REDIS_DATABASE:0}
      timeout: 3000
  redis:
    redisson:
      config: |
        singleServerConfig:
          address: "redis://${REDIS_HOST:127.0.0.1}:${REDIS_PORT:6379}"
          password: ${REDIS_PASSWORD:redis123}
          database: ${REDIS_DATABASE:0}
          connectionPoolSize: 10
          connectionMinimumIdleSize: 5
          connectTimeout: 3000
          timeout: 3000
```

#### 3. `common-monitoring.yaml` (共享监控配置)

```yaml
# 监控配置（所有服务共享）
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
  prometheus:
    metrics:
      export:
        enabled: true
  tracing:
    enabled: true
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

#### 4. `common-security.yaml` (共享安全配置)

```yaml
# 安全配置（所有服务共享）
security:
  jwt:
    secret: ${JWT_SECRET:ioedream-dev-jwt-secret-key-2025-must-be-at-least-256-bits}
    expiration: ${JWT_EXPIRATION:86400}
    refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800}
```

---

## 🔧 服务配置调整

### bootstrap.yml配置（已正确配置）

公共服务已经配置了从Nacos加载共享配置：

```yaml
spring:
  cloud:
    nacos:
      config:
        enabled: true
        shared-configs:
          - data-id: common-database.yaml
            group: IOE-DREAM
            refresh: true
          - data-id: common-redis.yaml
            group: IOE-DREAM
            refresh: true
          - data-id: common-monitoring.yaml
            group: IOE-DREAM
            refresh: true
          - data-id: common-security.yaml
            group: IOE-DREAM
            refresh: true
```

### application.yml配置（仅保留服务特定配置）

本地`application.yml`应该：
- ✅ 保留服务特定的配置（端口、服务名等）
- ✅ 保留Nacos连接配置（用于首次连接Nacos）
- ❌ 移除数据库、Redis等共享配置（从Nacos加载）
- ✅ 保留兜底默认值（仅在Nacos不可用时使用）

---

## 📝 配置迁移步骤

### 步骤1: 在Nacos配置中心创建共享配置

1. 登录Nacos控制台: http://127.0.0.1:8848/nacos
2. 进入"配置管理" → "配置列表"
3. 选择命名空间: `dev`
4. 选择分组: `IOE-DREAM`
5. 创建以下配置文件：
   - `common-database.yaml`
   - `common-redis.yaml`
   - `common-monitoring.yaml`
   - `common-security.yaml`

### 步骤2: 更新服务配置

所有服务的`application.yml`应该：
- 移除数据库配置（从`common-database.yaml`加载）
- 移除Redis配置（从`common-redis.yaml`加载）
- 移除监控配置（从`common-monitoring.yaml`加载）
- 保留服务特定配置

### 步骤3: 验证配置加载

启动服务后，检查日志确认配置已从Nacos加载：

```
[Nacos Config] Loaded config: common-database.yaml
[Nacos Config] Loaded config: common-redis.yaml
```

---

## ⚠️ 注意事项

1. **Nacos配置中心必须启动**: 如果Nacos不可用，服务将使用本地配置的兜底值
2. **环境变量优先级最高**: 即使Nacos配置了密码，环境变量仍会覆盖
3. **配置刷新**: 共享配置支持动态刷新，修改Nacos配置后服务会自动更新
4. **加密配置**: 敏感信息（如密码）应该使用Jasypt加密后存储在Nacos

---

## 🔄 当前状态

### 已配置统一配置源的服务

- ✅ ioedream-common-service (bootstrap.yml已配置)
- ✅ ioedream-oa-service (bootstrap.yml已配置)
- ✅ ioedream-consume-service (bootstrap.yml已配置)
- ✅ ioedream-device-comm-service (bootstrap.yml已配置)
- ✅ ioedream-attendance-service (bootstrap.yml已配置)
- ✅ ioedream-visitor-service (bootstrap.yml已配置)
- ✅ ioedream-video-service (bootstrap.yml已配置)
- ✅ ioedream-gateway-service (bootstrap.yml已配置)

### 待完成工作

1. **在Nacos配置中心创建共享配置文件**
2. **清理本地application.yml中的重复配置**
3. **验证配置加载顺序和优先级**

---

**维护人**: IOE-DREAM 架构团队  
**最后更新**: 2025-12-14
