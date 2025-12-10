# IOE-DREAM 全局配置标准

**版本**: v1.0.0  
**生效日期**: 2025-01-30  
**适用范围**: 所有微服务和Docker Compose配置  
**规范优先级**: P0级 - 强制执行

---

## 📋 核心原则

### 1. 配置一致性原则（强制执行）

**黄金法则**：
- ✅ **所有微服务必须使用相同的配置标准**
- ✅ **Docker Compose环境变量必须与配置文件默认值一致**
- ✅ **本地开发环境与Docker环境配置必须兼容**
- ❌ **禁止硬编码配置值**
- ❌ **禁止不同服务使用不同的配置标准**

---

## 🔧 全局配置参数标准

### 1. Nacos配置中心标准

| 配置项 | 环境变量 | 本地开发默认值 | Docker默认值 | 说明 |
|--------|---------|---------------|-------------|------|
| **server-addr** | `NACOS_SERVER_ADDR` | `127.0.0.1:8848` | `nacos:8848` | Nacos服务器地址 |
| **namespace** | `NACOS_NAMESPACE` | `dev` | `public` | 命名空间（Docker使用public） |
| **group** | `NACOS_GROUP` | `IOE-DREAM` | `IOE-DREAM` | **必须一致** |
| **username** | `NACOS_USERNAME` | `nacos` | `nacos` | **必须一致** |
| **password** | `NACOS_PASSWORD` | `nacos` | `nacos` | **必须一致** |

**标准配置模板**：
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:IOE-DREAM}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        enabled: true
        register-enabled: true
      config:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:IOE-DREAM}
        file-extension: yaml
        enabled: true
```

**Docker Compose环境变量**：
```yaml
environment:
  - NACOS_SERVER_ADDR=nacos:8848
  - NACOS_NAMESPACE=public
  - NACOS_GROUP=IOE-DREAM
  - NACOS_USERNAME=nacos
  - NACOS_PASSWORD=nacos
```

---

### 2. Redis缓存配置标准

| 配置项 | 环境变量 | 本地开发默认值 | Docker默认值 | 说明 |
|--------|---------|---------------|-------------|------|
| **host** | `REDIS_HOST` | `127.0.0.1` | `redis` | Redis服务器地址 |
| **port** | `REDIS_PORT` | `6379` | `6379` | **必须一致** |
| **password** | `REDIS_PASSWORD` | `redis123` | `redis123` | **必须一致** |
| **database** | `REDIS_DATABASE` | `0` | `0` | **必须一致** |

**标准配置模板**：
```yaml
spring:
  redis:
    host: ${REDIS_HOST:127.0.0.1}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:redis123}
    database: ${REDIS_DATABASE:0}
    timeout: 3000
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

**Docker Compose环境变量**：
```yaml
environment:
  - REDIS_HOST=redis
  - REDIS_PORT=6379
  - REDIS_PASSWORD=${REDIS_PASSWORD:-redis123}
  - REDIS_DATABASE=0
```

---

### 3. MySQL数据库配置标准

| 配置项 | 环境变量 | 本地开发默认值 | Docker默认值 | 说明 |
|--------|---------|---------------|-------------|------|
| **host** | `MYSQL_HOST` | `127.0.0.1` | `mysql` | MySQL服务器地址 |
| **port** | `MYSQL_PORT` | `3306` | `3306` | **必须一致** |
| **database** | `MYSQL_DATABASE` | `ioedream` | `ioedream` | **必须一致** |
| **username** | `MYSQL_USERNAME` | `root` | `root` | **必须一致** |
| **password** | `MYSQL_PASSWORD` | `root1234` | `root1234` | **必须一致** |

**标准配置模板**：
```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${MYSQL_HOST:127.0.0.1}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:ioedream}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:root1234}
```

**Docker Compose环境变量**：
```yaml
environment:
  - MYSQL_HOST=mysql
  - MYSQL_PORT=3306
  - MYSQL_DATABASE=ioedream
  - MYSQL_USERNAME=root
  - MYSQL_PASSWORD=${MYSQL_ROOT_PASSWORD:-root1234}
```

---

### 4. 服务端口配置标准

| 服务名称 | HTTP端口 | 内部端口 | 环境变量 | 说明 |
|---------|---------|---------|---------|------|
| **ioedream-gateway-service** | 8080 | - | `SERVER_PORT` | API网关 |
| **ioedream-device-comm-service** | 8087 | TCP:18087, UDP:18089 | `SERVER_PORT` | 设备通讯服务 |
| **ioedream-common-service** | 8088 | - | `SERVER_PORT` | 公共业务服务 |
| **ioedream-oa-service** | 8089 | - | `SERVER_PORT` | OA服务 |
| **ioedream-access-service** | 8090 | - | `SERVER_PORT` | 门禁服务 |
| **ioedream-attendance-service** | 8091 | - | `SERVER_PORT` | 考勤服务 |
| **ioedream-video-service** | 8092 | - | `SERVER_PORT` | 视频服务 |
| **ioedream-consume-service** | 8094 | - | `SERVER_PORT` | 消费服务 |
| **ioedream-visitor-service** | 8095 | - | `SERVER_PORT` | 访客服务 |

**重要说明**:
- **设备通讯服务内部端口**: TCP端口18087和UDP端口18089用于接收设备推送数据，仅在容器内部使用
- **端口冲突避免**: 设备通讯服务的TCP/UDP端口使用18000+范围，避免与HTTP服务端口冲突

**标准配置模板**：
```yaml
server:
  port: ${SERVER_PORT:8080}  # 每个服务使用对应的端口
```

**Docker Compose端口映射**：
```yaml
ports:
  - "${SERVER_PORT:8080}:${SERVER_PORT:8080}"
```

---

### 5. 日志配置标准

**标准配置模板**：
```yaml
logging:
  level:
    root: INFO
    net.lab1024.sa: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
```

---

### 6. Actuator监控配置标准

**标准配置模板**：
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## 🔍 配置一致性检查清单

### 检查项

- [ ] **Nacos配置一致性**
  - [ ] 所有服务的 `NACOS_GROUP` 都是 `IOE-DREAM`
  - [ ] 所有服务的 `NACOS_USERNAME` 都是 `nacos`
  - [ ] 所有服务的 `NACOS_PASSWORD` 都是 `nacos`
  - [ ] Docker Compose中的Nacos环境变量与配置文件一致

- [ ] **Redis配置一致性**
  - [ ] 所有服务的 `REDIS_PASSWORD` 都是 `redis123`
  - [ ] 所有服务的 `REDIS_DATABASE` 都是 `0`
  - [ ] Docker Compose中的Redis密码与配置文件一致

- [ ] **MySQL配置一致性**
  - [ ] 所有服务的 `MYSQL_DATABASE` 都是 `ioedream`
  - [ ] 所有服务的 `MYSQL_USERNAME` 都是 `root`
  - [ ] 所有服务的 `MYSQL_PASSWORD` 都是 `root1234`
  - [ ] Docker Compose中的MySQL密码与配置文件一致

- [ ] **服务端口一致性**
  - [ ] 每个服务的端口与docker-compose中的端口映射一致
  - [ ] 服务端口没有冲突

- [ ] **环境变量一致性**
  - [ ] Docker Compose中的环境变量与配置文件默认值一致
  - [ ] 本地开发环境变量与Docker环境变量兼容

---

## 🚨 常见配置不一致问题

### 问题1: Nacos命名空间不一致

**错误示例**：
```yaml
# application.yml
namespace: ${NACOS_NAMESPACE:dev}

# docker-compose-all.yml
NACOS_NAMESPACE=public  # ❌ 不一致
```

**正确示例**：
```yaml
# application.yml
namespace: ${NACOS_NAMESPACE:dev}  # 本地开发默认dev

# docker-compose-all.yml
NACOS_NAMESPACE=public  # Docker环境使用public，通过环境变量覆盖
```

**说明**：本地开发使用 `dev` 命名空间，Docker环境使用 `public` 命名空间，通过环境变量覆盖，这是正确的。

---

### 问题2: Redis密码不一致

**错误示例**：
```yaml
# application.yml
password: ${REDIS_PASSWORD:redis123}

# docker-compose-all.yml
REDIS_PASSWORD=redis456  # ❌ 不一致
```

**正确示例**：
```yaml
# application.yml
password: ${REDIS_PASSWORD:redis123}

# docker-compose-all.yml
REDIS_PASSWORD=${REDIS_PASSWORD:-redis123}  # ✅ 使用相同的默认值
```

---

### 问题3: 服务地址不一致

**错误示例**：
```yaml
# application.yml
server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}

# docker-compose-all.yml
NACOS_SERVER_ADDR=nacos:8848  # ✅ 正确，Docker环境使用服务名
```

**说明**：本地开发使用 `127.0.0.1:8848`，Docker环境使用 `nacos:8848`，这是正确的，因为Docker Compose会自动解析服务名。

---

## 📝 配置模板文件

### 标准application.yml模板

创建 `templates/application.yml.template` 作为所有微服务的标准模板。

---

## 🔧 配置验证工具

创建 `scripts/verify-config-consistency.ps1` 自动检查配置一致性。

---

## 📚 相关文档

- [Docker Compose配置规范](./docker-compose-configuration-standards.md)
- [环境变量管理规范](./environment-variables-management.md)
- [Nacos配置中心使用指南](./nacos-configuration-center-guide.md)

---

**维护责任人**: IOE-DREAM 架构委员会  
**最后更新**: 2025-01-30
