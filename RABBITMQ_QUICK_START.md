# RabbitMQ连接问题 - 快速修复指南

## 🔴 问题现象

应用日志中出现大量错误：
```
org.springframework.amqp.AmqpConnectException: java.net.ConnectException: Connection refused: getsockopt
```

## ✅ 解决方案（已修复）

已在 `docker-compose-all.yml` 中添加RabbitMQ服务配置。现在需要启动RabbitMQ服务。

## 🚀 快速启动

### 方法1: 启动所有基础设施（推荐）

```powershell
# 启动MySQL、Redis、Nacos和RabbitMQ
docker-compose -f docker-compose-all.yml up -d mysql redis nacos rabbitmq

# 等待服务启动（约30-60秒）
Start-Sleep -Seconds 30

# 验证RabbitMQ状态
docker-compose -f docker-compose-all.yml ps rabbitmq
```

### 方法2: 只启动RabbitMQ

```powershell
# 启动RabbitMQ服务
docker-compose -f docker-compose-all.yml up -d rabbitmq

# 查看启动日志
docker-compose -f docker-compose-all.yml logs -f rabbitmq
```

### 方法3: 使用启动脚本

```powershell
# 使用项目启动脚本（会自动启动所有基础设施服务）
.\start.ps1
```

## 🔍 验证RabbitMQ运行状态

```powershell
# 检查容器状态
docker ps --filter "name=rabbitmq"

# 检查服务健康状态
docker exec ioedream-rabbitmq rabbitmq-diagnostics ping

# 检查端口
Test-NetConnection -ComputerName localhost -Port 5672
```

## 🌐 访问管理界面

RabbitMQ管理界面：**http://localhost:15672**

- **用户名**: `admin`
- **密码**: `admin123`

## 🔄 重启应用服务

RabbitMQ启动后，重启应用服务以重新连接：

```powershell
# 重启所有微服务
docker-compose -f docker-compose-all.yml restart

# 或者重启特定服务
docker-compose -f docker-compose-all.yml restart device-comm-service
```

## 📋 配置说明

### 默认配置

- **AMQP端口**: 5672
- **管理界面端口**: 15672
- **用户名**: admin
- **密码**: admin123
- **虚拟主机**: ioedream

### 环境变量（可选）

在 `.env` 文件中可以自定义配置：

```env
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=your_password
RABBITMQ_VHOST=ioedream
```

## ⚠️ 注意事项

1. **端口占用**: 确保5672和15672端口未被占用
2. **服务顺序**: RabbitMQ应该在微服务之前启动
3. **网络连接**: 确保RabbitMQ在 `ioedream-network` 网络中

## 📚 详细文档

查看完整文档: [documentation/technical/RABBITMQ_CONNECTION_FIX.md](documentation/technical/RABBITMQ_CONNECTION_FIX.md)
