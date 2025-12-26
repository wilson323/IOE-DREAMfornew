# 部署指南

**版本**: v1.0.0  
**日期**: 2025-01-30  
**状态**: 进行中

---

## 📋 目录

1. [环境要求](#环境要求)
2. [Docker部署](#docker部署)
3. [Kubernetes部署](#kubernetes部署)
4. [配置说明](#配置说明)
5. [健康检查](#健康检查)
6. [故障排查](#故障排查)

---

## 🔧 环境要求

### 基础环境
- **操作系统**: Linux (CentOS 7+/Ubuntu 18+) 或 Windows Server 2016+
- **JDK**: OpenJDK 17+
- **Maven**: 3.8+
- **Docker**: 20.10+ (可选)
- **Kubernetes**: 1.20+ (可选)

### 中间件
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Nacos**: 2.0+

### 资源要求
- **CPU**: 4核+
- **内存**: 8GB+
- **磁盘**: 50GB+

---

## 🐳 Docker部署

### 1. 构建镜像

```bash
# 构建公共模块
cd microservices/microservices-common
mvn clean install -DskipTests

# 构建服务镜像
cd microservices/ioedream-consume-service
docker build -t ioedream-consume-service:latest .
```

### 2. 使用Docker Compose

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ioedream
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  redis:
    image: redis:6.0
    ports:
      - "6379:6379"

  nacos:
    image: nacos/nacos-server:2.0
    ports:
      - "8848:8848"
    environment:
      MODE: standalone

  consume-service:
    image: ioedream-consume-service:latest
    ports:
      - "8094:8094"
    depends_on:
      - mysql
      - redis
      - nacos
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DB_HOST: mysql
      REDIS_HOST: redis
      NACOS_SERVER_ADDR: nacos:8848

volumes:
  mysql-data:
```

### 3. 启动服务

```bash
docker-compose up -d
```

---

## ☸️ Kubernetes部署

### 1. 创建命名空间

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: ioedream
```

### 2. 部署MySQL

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
  namespace: ioedream
spec:
  serviceName: mysql
  replicas: 1
  template:
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: password
```

### 3. 部署Redis

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: ioedream
spec:
  replicas: 1
  template:
    spec:
      containers:
      - name: redis
        image: redis:6.0
        ports:
        - containerPort: 6379
```

### 4. 部署微服务

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: consume-service
  namespace: ioedream
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: consume-service
        image: ioedream-consume-service:latest
        ports:
        - containerPort: 8094
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: DB_HOST
          value: "mysql"
        - name: REDIS_HOST
          value: "redis"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8094
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8094
          initialDelaySeconds: 30
          periodSeconds: 5
```

---

## ⚙️ 配置说明

### 1. 数据库配置

在Nacos配置中心配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:ioedream}?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 50
```

### 2. Redis配置

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
```

### 3. Nacos配置

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:IOE-DREAM}
```

---

## 🏥 健康检查

### 1. 服务健康检查

```bash
# 检查服务健康状态
curl http://localhost:8094/actuator/health

# 预期响应
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

### 2. 监控指标

```bash
# 查看指标
curl http://localhost:8094/actuator/metrics

# Prometheus格式
curl http://localhost:8094/actuator/prometheus
```

### 3. Druid监控

访问: `http://localhost:8094/druid/index.html`

---

## 🔍 故障排查

### 问题1: 服务启动失败

**检查项**:
- [ ] 数据库连接是否正常
- [ ] Redis连接是否正常
- [ ] Nacos连接是否正常
- [ ] 端口是否被占用

**解决方法**:
```bash
# 检查端口占用
netstat -tuln | grep 8094

# 查看服务日志
docker logs consume-service
```

### 问题2: 数据库连接失败

**检查项**:
- [ ] 数据库服务是否启动
- [ ] 数据库用户名密码是否正确
- [ ] 数据库是否已创建
- [ ] 防火墙是否开放端口

**解决方法**:
```bash
# 测试数据库连接
mysql -h localhost -u root -p

# 检查数据库是否存在
SHOW DATABASES;
```

### 问题3: Redis连接失败

**检查项**:
- [ ] Redis服务是否启动
- [ ] Redis密码是否正确
- [ ] 防火墙是否开放端口

**解决方法**:
```bash
# 测试Redis连接
redis-cli -h localhost -p 6379 ping

# 预期输出: PONG
```

---

## 📚 相关文档

- [索引优化执行指南](./INDEX_OPTIMIZATION_EXECUTION_GUIDE.md)
- [Druid连接池配置](./DRUID_CONNECTION_POOL_CONFIGURATION.md)
- [Redisson配置验证](./REDISSON_CONFIGURATION_VERIFICATION.md)
- [性能测试指南](../scripts/performance/performance_test_guide.md)

---

**更多详细信息请参考各模块的README文档**

