# RabbitMQ连接问题解决方案

## 问题描述

应用启动时出现大量RabbitMQ连接错误：
```
org.springframework.amqp.AmqpConnectException: java.net.ConnectException: Connection refused: getsockopt
```

**错误原因**：
- `docker-compose-all.yml` 中缺少RabbitMQ服务配置
- 应用配置了RabbitMQ连接，但RabbitMQ服务未启动
- 导致Spring AMQP无法连接到RabbitMQ服务器

## 解决方案

### 1. 已完成的修复

已在 `docker-compose-all.yml` 中添加了RabbitMQ服务配置：

```yaml
# RabbitMQ消息队列
rabbitmq:
  image: rabbitmq:3.12-management-alpine
  container_name: ioedream-rabbitmq
  environment:
    - RABBITMQ_DEFAULT_USER=${RABBITMQ_USERNAME:-admin}
    - RABBITMQ_DEFAULT_PASS=${RABBITMQ_PASSWORD:-admin123}
    - RABBITMQ_DEFAULT_VHOST=${RABBITMQ_VHOST:-ioedream}
    - TZ=Asia/Shanghai
  ports:
    - "5672:5672"          # AMQP端口
    - "15672:15672"        # 管理界面端口
  volumes:
    - rabbitmq_data:/var/lib/rabbitmq
    - rabbitmq_logs:/var/log/rabbitmq
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
    interval: 30s
    timeout: 10s
    retries: 5
    start_period: 60s
  restart: unless-stopped
  networks:
    - ioedream-network
```

### 2. 已更新的配置

**所有微服务已添加RabbitMQ环境变量**：
- `RABBITMQ_HOST=rabbitmq`
- `RABBITMQ_PORT=5672`
- `RABBITMQ_USERNAME=${RABBITMQ_USERNAME:-admin}`
- `RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD:-admin123}`
- `RABBITMQ_VHOST=${RABBITMQ_VHOST:-ioedream}`

**所有微服务已添加RabbitMQ依赖**：
```yaml
depends_on:
  rabbitmq:
    condition: service_healthy
```

**启动脚本已更新**：
- `start.ps1` 中的基础设施启动命令已包含RabbitMQ

### 3. 启动RabbitMQ服务

#### 方法1: 使用启动脚本（推荐）

```powershell
# 启动所有基础设施服务（包括RabbitMQ）
.\start.ps1

# 或者选择选项启动完整服务
```

#### 方法2: 使用Docker Compose直接启动

```powershell
# 启动RabbitMQ服务
docker-compose -f docker-compose-all.yml up -d rabbitmq

# 启动所有基础设施服务（包括RabbitMQ）
docker-compose -f docker-compose-all.yml up -d mysql redis nacos rabbitmq

# 启动所有服务
docker-compose -f docker-compose-all.yml up -d
```

### 4. 验证RabbitMQ服务

#### 检查服务状态

```powershell
# 检查RabbitMQ容器状态
docker ps --filter "name=rabbitmq"

# 检查RabbitMQ健康状态
docker-compose -f docker-compose-all.yml ps rabbitmq

# 查看RabbitMQ日志
docker-compose -f docker-compose-all.yml logs rabbitmq
```

#### 访问管理界面

RabbitMQ管理界面：http://localhost:15672

- 用户名: `admin`（或环境变量 `RABBITMQ_USERNAME` 配置的值）
- 密码: `admin123`（或环境变量 `RABBITMQ_PASSWORD` 配置的值）

#### 测试连接

```powershell
# 在容器内测试连接
docker exec ioedream-rabbitmq rabbitmq-diagnostics ping

# 检查端口是否开放
Test-NetConnection -ComputerName localhost -Port 5672
```

### 5. 环境变量配置

在 `.env` 文件中添加以下配置（可选，有默认值）：

```env
# RabbitMQ配置
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=admin123
RABBITMQ_VHOST=ioedream
```

**注意**：如果不配置，将使用默认值（admin/admin123）

### 6. 重启应用服务

配置更新后，需要重启所有微服务：

```powershell
# 重启所有微服务
docker-compose -f docker-compose-all.yml restart

# 或者重启特定服务
docker-compose -f docker-compose-all.yml restart device-comm-service
```

## 故障排查

### 问题1: RabbitMQ容器无法启动

**检查**：
```powershell
# 查看RabbitMQ容器日志
docker-compose -f docker-compose-all.yml logs rabbitmq

# 检查端口占用
netstat -ano | findstr :5672
netstat -ano | findstr :15672
```

**解决**：
- 如果端口被占用，停止占用端口的进程或修改端口映射
- 检查Docker资源是否足够（内存、CPU）

### 问题2: 应用仍无法连接RabbitMQ

**检查**：
```powershell
# 检查服务是否在同一网络
docker network inspect ioedream-network

# 检查环境变量是否正确
docker exec ioedream-device-comm-service env | findstr RABBITMQ
```

**解决**：
- 确认RabbitMQ服务已健康启动（healthcheck通过）
- 确认微服务环境变量配置正确
- 检查防火墙设置

### 问题3: 连接超时

**检查**：
```powershell
# 检查RabbitMQ是否正常响应
docker exec ioedream-rabbitmq rabbitmqctl status
```

**解决**：
- 增加连接超时时间（已在配置中设置为15秒）
- 检查网络延迟

## 配置说明

### RabbitMQ默认配置

- **镜像**: `rabbitmq:3.12-management-alpine`
- **AMQP端口**: `5672`
- **管理界面端口**: `15672`
- **默认用户**: `admin`
- **默认密码**: `admin123`
- **虚拟主机**: `ioedream`

### 数据持久化

- **数据卷**: `rabbitmq_data` - 存储RabbitMQ数据
- **日志卷**: `rabbitmq_logs` - 存储RabbitMQ日志

### 健康检查

RabbitMQ服务配置了健康检查，确保服务完全启动后才允许依赖服务连接：

```yaml
healthcheck:
  test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
  interval: 30s
  timeout: 10s
  retries: 5
  start_period: 60s
```

## 相关文档

- [RabbitMQ配置文档](../microservices/common-config/rabbitmq-application.yml)
- [RabbitMQ部署脚本](../../scripts/deploy-rabbitmq.sh)
- [Docker Compose配置](../../docker-compose-all.yml)

## 更新日期

2025-12-14
