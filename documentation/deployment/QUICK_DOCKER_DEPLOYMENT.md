# IOE-DREAM Docker 快速部署指南

> **5分钟快速部署IOE-DREAM智慧园区一卡通管理平台**

---

## 🚀 一键部署（推荐新手）

### 前置要求
- Docker 20.10+
- Docker Compose 2.0+
- 8GB+ RAM
- 20GB+ 磁盘空间

### 快速部署命令

```bash
# 1. 克隆项目
git clone https://github.com/your-org/IOE-DREAM.git
cd IOE-DREAM

# 2. 一键启动（Windows）
.\scripts\docker-build.ps1 && docker-compose -f docker-compose-all.yml up -d

# 2. 一键启动（Linux/macOS）
chmod +x scripts/docker-build.sh && ./scripts/docker-build.sh && docker-compose -f docker-compose-all.yml up -d

# 3. 等待3-5分钟，然后访问
# Web管理后台: http://localhost:80
# API网关: http://localhost:8080
# Nacos控制台: http://localhost:8848/nacos (账号: nacos/nacos)
```

**就这么简单！** 🎉

---

## ⚙️ 详细步骤

### 步骤1: 环境检查

```bash
# 检查Docker
docker --version
docker compose version

# 检查可用资源
docker system df
free -h
```

### 步骤2: 配置环境

```bash
# 复制配置文件
cp .env.development .env

# 编辑配置（可选）
nano .env
```

### 步骤3: 构建镜像

```bash
# Windows
.\scripts\docker-build.ps1

# Linux/macOS
./scripts/docker-build.sh
```

### 步骤4: 启动服务

```bash
# 启动所有服务
docker-compose -f docker-compose-all.yml up -d

# 查看启动进度
docker-compose -f docker-compose-all.yml logs -f

# 检查服务状态
docker-compose -f docker-compose-all.yml ps
```

### 步骤5: 验证部署

```bash
# 健康检查
curl http://localhost/health

# 访问管理后台
echo "访问地址: http://localhost"
echo "默认账号: admin/123456"

# 查看所有服务端口
docker-compose -f docker-compose-all.yml ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"
```

---

## 🔧 常用命令

### 服务管理

```bash
# 启动所有服务
docker-compose -f docker-compose-all.yml up -d

# 停止所有服务
docker-compose -f docker-compose-all.yml down

# 重启特定服务
docker-compose -f docker-compose-all.yml restart gateway-service

# 查看服务日志
docker-compose -f docker-compose-all.yml logs -f [service_name]

# 查看所有服务状态
docker-compose -f docker-compose-all.yml ps
```

### 调试命令

```bash
# 进入容器
docker exec -it ioedream-gateway-service /bin/bash

# 查看实时日志
docker logs -f ioedream-gateway-service

# 查看资源使用
docker stats

# 清理未使用的资源
docker system prune -f
```

### 数据管理

```bash
# 查看数据库
docker exec -it ioedream-mysql mysql -uroot -p

# 查看Redis
docker exec -it ioedream-redis redis-cli

# 备份数据库
docker exec ioedream-mysql mysqldump -uroot -p ioedream > backup.sql
```

---

## 🔍 服务访问地址

| 服务 | 地址 | 账号/密码 | 说明 |
|------|------|-----------|------|
| **管理后台** | http://localhost:80 | admin/123456 | 主要操作界面 |
| **API网关** | http://localhost:8080 | - | 所有API入口 |
| **Nacos控制台** | http://localhost:8848/nacos | nacos/nacos | 服务管理 |
| **MySQL数据库** | localhost:3306 | root/root | 数据存储 |
| **Redis缓存** | localhost:6379 | - | 缓存服务 |

---

## ⚠️ 常见问题

### Q: 服务启动失败怎么办？
```bash
# 查看具体错误
docker-compose -f docker-compose-all.yml logs [service_name]

# 重新构建并启动
docker-compose -f docker-compose-all.yml up -d --build [service_name]
```

### Q: 端口被占用？
```bash
# 查看端口占用
netstat -tulpn | grep [port]

# 修改端口配置
sed -i 's/8080/8081/g' .env
```

### Q: 内存不足？
```bash
# 调整JVM内存
echo "JVM_XMS=256m" >> .env
echo "JVM_XMX=512m" >> .env

# 重启服务
docker-compose -f docker-compose-all.yml restart
```

### Q: 忘记密码？
```bash
# 查看环境变量
grep PASSWORD .env

# 重置数据库密码
docker exec -it ioedream-mysql mysql -uroot -p
ALTER USER 'root'@'%' IDENTIFIED BY 'new_password';
```

---

## 🚨 生产环境注意事项

### 1. 安全配置

```bash
# 修改默认密码
nano .env.production
# 修改所有密码和密钥

# 配置SSL证书
mkdir -p deployment/nginx/ssl
# 放置证书文件
```

### 2. 资源配置

```bash
# 使用生产环境配置
cp .env.production .env

# 启动生产环境
docker-compose -f docker-compose-production.yml up -d
```

### 3. 备份策略

```bash
# 设置定时备份
crontab -e
# 添加: 0 2 * * * /path/to/scripts/backup.sh
```

---

## 📚 更多资源

- **完整部署指南**: [DOCKER_DEPLOYMENT_GUIDE.md](./DOCKER_DEPLOYMENT_GUIDE.md)
- **项目文档**: [./documentation/](./documentation/)
- **问题反馈**: [GitHub Issues](https://github.com/your-org/IOE-DREAM/issues)
- **技术交流**: [社区论坛](https://community.ioedream.com)

---

## 🎯 快速测试

部署完成后，可以快速测试以下功能：

```bash
# 1. 健康检查
curl http://localhost/health

# 2. API测试
curl http://localhost/api/common/health

# 3. 登录测试
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

如果以上测试都通过，恭喜你！IOE-DREAM已经成功部署。🎉

---

**遇到问题？** 查看完整部署指南或提交Issue获取帮助。