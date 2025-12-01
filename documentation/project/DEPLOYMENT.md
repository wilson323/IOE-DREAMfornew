# SmartAdmin Docker 部署指南

## 📋 概述

本文档提供SmartAdmin项目的完整Docker部署方案，包括开发环境和生产环境的部署配置。

## 🏗️ 架构说明

### 服务组件
- **MySQL 8.0**: 主数据库
- **Redis 7.2**: 缓存和会话存储
- **Backend**: Spring Boot 3后端服务（端口1024）
- **Frontend**: Vue 3前端服务（Nginx，端口8080）
- **Nginx**: 反向代理和负载均衡（生产环境，端口80/443）

### 网络配置
- 使用Docker自定义网络 `172.20.0.0/16`
- 所有服务在同一个网络中通信
- 数据持久化使用Docker volumes

## 🚀 快速开始

### 前置要求
- Docker 20.0+
- Docker Compose 2.0+
- 至少4GB可用内存
- 至少10GB可用磁盘空间

### 1. 克隆项目
```bash
git clone <repository-url>
cd IOE-DREAM
```

### 2. 开发环境部署
```bash
# 启动开发环境（包含热重载）
docker-compose -f docker-compose.dev.yml up -d

# 查看服务状态
docker-compose -f docker-compose.dev.yml ps

# 查看日志
docker-compose -f docker-compose.dev.yml logs -f
```

### 3. 生产环境部署
```bash
# 构建并启动生产环境
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 4. 访问应用
- **前端**: http://localhost:8080 (开发环境) 或 http://localhost (生产环境)
- **后端API**: http://localhost:1024/api
- **API文档**: http://localhost:1024/doc.html
- **MySQL**: localhost:3306
- **Redis**: localhost:6379

## 🔧 配置说明

### 环境变量

#### 后端配置
| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `SPRING_PROFILES_ACTIVE` | prod | 运行环境 |
| `SPRING_DATASOURCE_URL` | - | 数据库连接 |
| `SPRING_DATASOURCE_USERNAME` | root | 数据库用户名 |
| `SPRING_DATASOURCE_PASSWORD` | root1234 | 数据库密码 |
| `SPRING_REDIS_HOST` | redis | Redis主机 |
| `SPRING_REDIS_PORT` | 6379 | Redis端口 |
| `SPRING_REDIS_PASSWORD` | zkteco3100 | Redis密码 |
| `SERVER_PORT` | 1024 | 服务端口 |

### 数据库初始化
首次启动时，Docker会自动执行 `数据库SQL脚本/mysql/` 目录下的SQL文件来初始化数据库。

### 文件上传路径
- 上传文件存储在Docker volume `upload_data` 中
- 对应容器内路径：`/app/upload`

## 📊 监控和维护

### 健康检查
所有服务都配置了健康检查：
```bash
# 查看健康状态
docker-compose ps

# 查看详细健康信息
docker inspect <container_name>
```

### 日志管理
```bash
# 查看实时日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 查看日志文件
tail -f logs/backend/*.log
tail -f logs/nginx/*.log
```

### 数据备份
```bash
# 备份数据库
docker exec smart-admin-mysql mysqldump -u root -proot1234 smart_admin_v3 > backup_$(date +%Y%m%d).sql

# 备份Redis数据
docker exec smart-admin-redis redis-cli --rdb /data/dump_$(date +%Y%m%d).rdb
```

### 服务重启
```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart backend

# 重新构建并启动
docker-compose up -d --build
```

## 🔒 安全配置

### 生产环境安全建议

1. **更改默认密码**：
   - MySQL root密码
   - Redis密码
   - 应用管理员密码

2. **启用HTTPS**：
   ```bash
   # 生成SSL证书
   openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
     -keyout docker/nginx/ssl/key.pem \
     -out docker/nginx/ssl/cert.pem
   ```

3. **防火墙配置**：
   ```bash
   # 只开放必要端口
   ufw allow 80/tcp
   ufw allow 443/tcp
   ufw enable
   ```

4. **网络隔离**：
   - 数据库服务不对外暴露
   - 使用内部网络通信
   - 配置访问控制列表

## 🛠️ 故障排除

### 常见问题

1. **端口冲突**：
   ```bash
   # 检查端口占用
   netstat -tulpn | grep :1024

   # 修改docker-compose.yml中的端口映射
   ```

2. **内存不足**：
   ```bash
   # 增加Docker内存限制
   # 在Docker Desktop中调整内存设置

   # 或调整JVM参数
   JAVA_OPTS="-Xmx256m -Xms128m"
   ```

3. **数据库连接失败**：
   ```bash
   # 检查数据库服务状态
   docker-compose logs mysql

   # 验证数据库连接
   docker exec -it smart-admin-mysql mysql -u root -proot1234
   ```

4. **前端构建失败**：
   ```bash
   # 清理node_modules重新构建
   docker exec -it smart-admin-frontend npm install
   ```

### 性能优化

1. **数据库优化**：
   - 调整MySQL配置参数
   - 添加适当的索引
   - 定期清理日志

2. **缓存优化**：
   - 调整Redis内存限制
   - 配置合适的缓存策略

3. **应用优化**：
   - 调整JVM参数
   - 配置连接池大小
   - 启用Gzip压缩

## 📈 扩展部署

### 多实例部署
```bash
# 扩展后端服务实例
docker-compose up -d --scale backend=3

# 扩展前端服务实例
docker-compose up -d --scale frontend=2
```

### 集群部署
```bash
# 使用Docker Swarm
docker swarm init
docker stack deploy -c docker-compose.yml smart-admin
```

## 🔄 版本更新

### 滚动更新
```bash
# 拉取最新代码
git pull

# 重新构建并更新
docker-compose up -d --build

# 零停机更新
docker-compose up -d --no-deps backend
```

### 回滚
```bash
# 查看历史版本
docker images | grep smart-admin

# 回滚到指定版本
docker-compose up -d --build <image>:<tag>
```

## 📞 技术支持

如遇到问题，请：
1. 查看日志文件
2. 检查服务状态
3. 验证网络连接
4. 参考故障排除章节

## 📝 更新日志

- **v1.0.0** (2025-11-14): 初始Docker部署配置
- 支持5种环境: localhost, dev, test, pre, prod
- 完整的健康检查和监控
- 自动数据库初始化
- 生产级安全配置