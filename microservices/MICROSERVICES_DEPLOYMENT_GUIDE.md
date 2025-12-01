# 🚀 IOE-DREAM 微服务架构部署指南

## 📋 部署概述

本文档提供IOE-DREAM微服务架构的完整部署指南，涵盖从环境准备到生产运维的全流程。

### 🎯 架构规模
- **20个核心微服务**
- **4层架构设计** (基础设施层 → 业务核心层 → 业务应用层 → 通信支撑层)
- **统一API网关入口**
- **服务注册与发现**
- **分布式配置管理**
- **实时监控与告警**

---

## 🏗️ 一、环境准备

### 1.1 基础软件要求

#### 🔧 **必须安装的软件**
```bash
# Java 17+
java -version

# Maven 3.8+
mvn -version

# Node.js 16+ (前端工具)
node --version

# Redis (缓存和限流)
redis-server --version

# Git (版本控制)
git --version
```

#### 📦 **可选软件 (推荐)**
- **Docker** (容器化部署)
- **Nginx** (反向代理)
- **MySQL** (数据持久化)
- **Prometheus + Grafana** (监控)

### 1.2 系统资源要求

#### 💻 **开发环境**
- CPU: 4核心以上
- 内存: 8GB以上
- 磁盘: 50GB可用空间
- 网络: 带宽100Mbps以上

#### 🖥️ **生产环境**
- CPU: 8核心以上
- 内存: 16GB以上
- 磁盘: 200GB+ SSD
- 网络: 带宽1Gbps以上

---

## 🚀 二、快速启动

### 2.1 一键启动所有服务

```bash
# Windows环境
.\start-microservices.bat

# 选择选项 1：启动所有微服务
```

### 2.2 分步启动

#### 第1步：启动基础设施
```bash
# 运行启动脚本，选择选项 2
.\start-microservices.bat
# 选择 2️⃣ 启动基础设施服务
```

#### 第2步：启动核心业务
```bash
# 运行启动脚本，选择选项 3
.\start-microservices.bat
# 选择 3️⃣ 启动核心业务服务
```

#### 第3步：启动其他服务
```bash
# 运行启动脚本，选择选项 1
.\start-microservices.bat
# 选择 1️⃣ 启动所有微服务
```

### 2.3 检查服务状态

```bash
# Windows环境
.\check-services-status.bat

# 或手动检查关键端口
netstat -an | findstr "8080"  # API网关
netstat -an | findstr "8081"  # 认证服务
netstat -an | findstr "8090"  # 门禁服务
```

---

## 🔧 三、服务配置详解

### 3.1 API网关服务 (端口: 8080)

#### 📍 **文件位置**
```
ioedream-gateway-service/
├── src/main/resources/application.yml
├── src/main/java/net/lab1024/sa/gateway/
│   ├── GatewayApplication.java
│   ├── config/GatewayConfig.java
│   └── filter/GlobalAuthFilter.java
```

#### ⚙️ **主要配置项**
```yaml
# 路由配置
spring.cloud.gateway.routes:
  - id: auth-service
    uri: lb://ioedream-auth-service
    predicates:
      - Path=/api/auth/**
    filters:
      - StripPrefix=2
      - RequestRateLimiter

# 限流配置
spring.redis:
  host: localhost
  port: 6379

# 服务发现
spring.cloud.nacos:
  discovery:
    server-addr: localhost:8848
```

#### 🌐 **访问地址**
- **API网关入口**: http://localhost:8080
- **路由信息**: http://localhost:8080/actuator/gateway/routes
- **健康检查**: http://localhost:8080/actuator/health

### 3.2 身份认证服务 (端口: 8081)

#### 🔐 **核心功能**
- 用户身份认证
- JWT令牌管理
- 权限控制
- 单点登录

#### 📍 **关键配置**
```yaml
# JWT配置
jwt:
  secret: your-secret-key
  expiration: 3600
  refresh-expiration: 7200

# Redis配置 (Session存储)
spring:
  redis:
    host: localhost
    port: 6379
    database: 1
```

### 3.3 设备管理服务 (端口: 8093)

#### 🔌 **核心功能**
- IoT设备接入
- 设备状态监控
- 远程控制
- 数据采集

#### 📍 **技术栈**
- Spring Boot
- MQTT协议支持
- CoAP协议支持
- WebSocket实时通信

---

## 📊 四、服务端口分配

### 4.1 基础设施层服务
| 服务名称 | 端口 | 功能描述 |
|---------|------|----------|
| **API网关** | 8080 | 统一入口、路由转发 |
| **配置中心** | 8888 | 统一配置管理 |
| **审计服务** | 8085 | 系统操作审计 |
| **报表分析** | 8086 | 数据分析报表 |
| **任务调度** | 8087 | 定时任务管理 |
| **集成服务** | 8088 | 第三方系统集成 |
| **基础设施** | 8089 | 运维支撑服务 |

### 4.2 业务核心层服务
| 服务名称 | 端口 | 功能描述 |
|---------|------|----------|
| **身份认证** | 8081 | 用户认证、JWT管理 |
| **身份管理** | 8082 | 用户信息管理 |
| **企业服务** | 8083 | OA+HR+文档管理 |
| **办公自动化** | 8084 | 审批流程、协作管理 |

### 4.3 业务应用层服务
| 服务名称 | 端口 | 功能描述 |
|---------|------|----------|
| **门禁管理** | 8090 | 智能门禁控制 |
| **考勤管理** | 8091 | 考勤规则管理 |
| **视频监控** | 8092 | 视频流管理 |
| **设备管理** | 8093 | IoT设备管理 |
| **消费管理** | 8094 | 消费记录管理 |
| **报表服务** | 8095 | 业务报表生成 |
| **通知服务** | 8096 | 消息推送服务 |
| **监控服务** | 8097 | 系统监控告警 |

---

## 🔍 五、监控与运维

### 5.1 服务状态监控

#### 📋 **自动监控脚本**
```bash
# 运行服务状态检查
.\check-services-status.bat
```

#### 📊 **监控指标**
- **服务可用性**: 端口连通性检查
- **响应时间**: HTTP请求响应时间
- **资源使用**: CPU、内存、网络
- **错误率**: 请求成功率统计

### 5.2 日志管理

#### 📝 **日志配置**
```yaml
logging:
  level:
    org.springframework.cloud.gateway: INFO
    net.lab1024.sa: DEBUG
  file:
    name: logs/ioedream-gateway.log
    max-size: 100MB
    max-history: 30
```

#### 🗂️ **日志收集方案**
- **ELK Stack** (推荐生产环境)
- **文件系统存储** (开发环境)
- **集中式日志服务** (企业级)

### 5.3 性能监控

#### 📈 **Prometheus指标**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

#### 📊 **Grafana仪表板**
- **服务概览**: 整体服务状态
- **性能指标**: QPS、延迟、错误率
- **资源监控**: 系统资源使用情况
- **业务指标**: 业务KPI统计

---

## 🛠️ 六、故障排查

### 6.1 常见问题

#### ❌ **问题1：服务启动失败**
**症状**: 端口被占用
```bash
# 解决方案
netstat -ano | findstr "8080"
taskkill /PID <进程ID> /F
```

#### ❌ **问题2：服务注册失败**
**症状**: 无法在Nacos中找到服务
```yaml
# 检查Nacos配置
spring.cloud.nacos:
  discovery:
    server-addr: localhost:8848
    enabled: true
```

#### ❌ **问题3：网关路由失败**
**症状**: API请求返回404
```bash
# 检查路由配置
curl http://localhost:8080/actuator/gateway/routes
```

### 6.2 性能调优

#### ⚡ **JVM参数优化**
```bash
java -Xms512m -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar service.jar
```

#### 🚀 **网关性能优化**
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 5000
        response-timeout: 60s
        pool:
          max-connections: 500
          max-idle-time: 30s
```

---

## 🔄 七、开发调试

### 7.1 本地开发环境

#### 💻 **IDE配置**
- **IntelliJ IDEA**: 推荐配置
- **VS Code**: 轻量级开发
- **Eclipse**: 传统选择

#### 🔧 **开发工具**
```xml
<!-- Maven编译插件配置 -->
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <jvmArguments>
            -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005
        </jvmArguments>
    </configuration>
</plugin>
```

### 7.2 热部署配置

#### 🔥 **DevTools配置**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

---

## 🌐 八、生产部署

### 8.1 Docker容器化

#### 🐳 **Dockerfile示例**
```dockerfile
FROM openjdk:17-jre-slim
COPY target/service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

#### 📦 **Docker Compose**
```yaml
version: '3.8'
services:
  gateway:
    build: ./ioedream-gateway-service
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
  nacos:
    image: nacos/nacos-server:v2.2.0
    ports:
      - "8848:8848"
```

### 8.2 Kubernetes部署

#### ☸️ **K8s配置示例**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ioedream-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ioedream-gateway
  template:
    metadata:
      labels:
        app: ioedream-gateway
    spec:
      containers:
      - name: gateway
        image: ioedream/gateway:latest
        ports:
        - containerPort: 8080
```

---

## 📞 九、技术支持

### 9.1 联系方式
- **技术团队**: ioedream-tech@company.com
- **运维支持**: ops@company.com
- **紧急联系**: +86-xxx-xxxx-xxxx

### 9.2 文档资源
- **API文档**: http://localhost:8080/swagger-ui.html
- **架构文档**: ./MICROSERVICES_ARCHITECTURE_ANALYSIS.md
- **更新日志**: CHANGELOG.md

---

*部署指南版本: v1.0.0*
*最后更新: 2025-11-30*
*维护团队: IOE-DREAM技术团队*