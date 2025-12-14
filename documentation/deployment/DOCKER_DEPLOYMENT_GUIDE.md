# IOE-DREAM Docker 部署指南

> **项目**: IOE-DREAM智慧园区一卡通管理平台
> **版本**: v1.0.0
> **更新时间**: 2024-12-07
> **适用环境**: 生产环境 / 开发环境 / 测试环境

---

## 📋 目录

- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [详细部署步骤](#详细部署步骤)
- [服务管理](#服务管理)
- [监控与日志](#监控与日志)
- [故障排查](#故障排查)
- [性能优化](#性能优化)
- [安全配置](#安全配置)
- [备份与恢复](#备份与恢复)
- [升级指南](#升级指南)

---

## 🔧 环境要求

### 硬件要求

#### 最低配置（开发/测试环境）
| 资源 | 要求 |
|------|------|
| **CPU** | 4核心 |
| **内存** | 8GB |
| **磁盘** | 50GB SSD |
| **网络** | 100Mbps |

#### 推荐配置（生产环境）
| 资源 | 要求 |
|------|------|
| **CPU** | 8核心以上 |
| **内存** | 32GB以上 |
| **磁盘** | 200GB+ SSD |
| **网络** | 1Gbps |
| **备份存储** | 500GB+ |

### 软件要求

| 软件 | 版本要求 |
|------|----------|
| **操作系统** | Linux (Ubuntu 20.04+, CentOS 8+) / Windows 10+ / macOS 11+ |
| **Docker** | 20.10+ |
| **Docker Compose** | 2.0+ |
| **Git** | 2.25+ |
| **OpenSSL** | 1.1.1+ (生产环境) |

### 端口规划

| 端口 | 服务 | 说明 |
|------|------|------|
| 80 | Nginx (HTTP) | 开发环境Web访问 |
| 443 | Nginx (HTTPS) | 生产环境Web访问 |
| 8080 | Gateway Service | API网关 |
| 8087 | Device Comm Service | 设备通讯服务 |
| 8088 | Common Service | 公共业务服务 |
| 8089 | OA Service | OA服务 |
| 8090 | Access Service | 门禁服务 |
| 8091 | Attendance Service | 考勤服务 |
| 8092 | Video Service | 视频服务 |
| 8094 | Consume Service | 消费服务 |
| 8095 | Visitor Service | 访客服务 |
| 8848 | Nacos | 注册中心 |
| 3306 | MySQL | 数据库 |
| 6379 | Redis | 缓存 |

---

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/your-org/IOE-DREAM.git
cd IOE-DREAM
```

### 2. 环境配置

```bash
# 复制环境变量文件
cp .env.production .env

# 编辑环境变量（重要：修改所有密码和密钥！）
nano .env
```

### 3. 一键部署

```bash
# Windows (PowerShell)
.\scripts\docker-build.ps1
docker-compose -f docker-compose-production.yml up -d

# Linux/macOS
chmod +x scripts/docker-build.sh
./scripts/docker-build.sh
docker-compose -f docker-compose-production.yml up -d
```

### 4. 验证部署

```bash
# 检查服务状态
docker-compose -f docker-compose-production.yml ps

# 检查健康状态
curl http://localhost/health

# 访问系统
# 开发环境: http://localhost
# 生产环境: https://your-domain.com
```

---

## 📝 详细部署步骤

### 步骤1: 环境准备

#### 1.1 安装Docker

**Ubuntu/Debian:**
```bash
# 更新包索引
sudo apt-get update

# 安装Docker
sudo apt-get install docker.io docker-compose-plugin

# 启动Docker服务
sudo systemctl start docker
sudo systemctl enable docker

# 添加用户到docker组
sudo usermod -aG docker $USER
```

**CentOS/RHEL:**
```bash
# 安装依赖
sudo yum install -y yum-utils

# 添加Docker仓库
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# 安装Docker
sudo yum install docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动Docker服务
sudo systemctl start docker
sudo systemctl enable docker
```

**Windows:**
1. 下载并安装 [Docker Desktop](https://www.docker.com/products/docker-desktop)
2. 启动Docker Desktop
3. 确保WSL 2已启用

#### 1.2 验证Docker安装

```bash
docker --version
docker compose version
docker info
```

### 步骤2: 项目配置

#### 2.1 获取源码

```bash
# 克隆项目
git clone https://github.com/your-org/IOE-DREAM.git
cd IOE-DREAM

# 检查分支
git branch -a
git checkout main
```

#### 2.2 配置环境变量

```bash
# 复制配置文件模板
cp .env.production .env

# 编辑配置文件
nano .env
```

**重要配置项说明:**

```bash
# 数据库密码（必须修改！）
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_PASSWORD=your_secure_password

# Redis密码（必须修改！）
REDIS_PASSWORD=your_secure_redis_password

# Nacos认证密钥（必须生成新密钥！）
NACOS_AUTH_TOKEN=your_64_character_random_string

# JWT密钥（必须生成新密钥！）
JWT_SECRET=your_256_character_jwt_secret

# 文件存储路径
DATA_DIR=/data/ioedream
LOGS_DIR=/var/log/ioedream
```

#### 2.3 生成安全密钥

```bash
# 生成随机密钥
openssl rand -hex 32  # JWT密钥
openssl rand -hex 64  # Nacos密钥
```

### 步骤3: SSL证书配置（生产环境）

#### 3.1 获取SSL证书

**Let's Encrypt (推荐):**
```bash
# 安装certbot
sudo apt-get install certbot

# 获取证书
sudo certbot certonly --standalone -d your-domain.com

# 复制证书
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem deployment/nginx/ssl/cert.pem
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem deployment/nginx/ssl/key.pem
```

**自签名证书（仅测试）:**
```bash
# 创建SSL目录
mkdir -p deployment/nginx/ssl

# 生成自签名证书
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout deployment/nginx/ssl/key.pem \
    -out deployment/nginx/ssl/cert.pem \
    -subj "/C=CN/ST=State/L=City/O=Organization/CN=localhost"
```

#### 3.2 配置证书权限

```bash
# 设置证书文件权限
chmod 600 deployment/nginx/ssl/key.pem
chmod 644 deployment/nginx/ssl/cert.pem
chown -R $USER:$USER deployment/nginx/ssl/
```

### 步骤4: 构建Docker镜像

#### 4.1 使用构建脚本（推荐）

```bash
# Windows (PowerShell)
.\scripts\docker-build.ps1

# Linux/macOS
chmod +x scripts/docker-build.sh
./scripts/docker-build.sh
```

#### 4.2 手动构建（高级用户）

```bash
# 1. 构建公共模块
cd microservices
mvn clean install -N -DskipTests
cd microservices-common
mvn clean install -DskipTests
cd ../..

# 2. 构建微服务镜像
docker build -f microservices/ioedream-gateway-service/Dockerfile -t ioedream/gateway-service:latest .
docker build -f microservices/ioedream-common-service/Dockerfile -t ioedream/common-service:latest .
# ... 其他服务

# 3. 构建前端镜像
docker build -t ioedream/web-admin:latest ./smart-admin-web-javascript/
```

### 步骤5: 启动服务

#### 5.1 启动基础设施服务

```bash
# 启动数据库、缓存、注册中心
docker-compose -f docker-compose-production.yml up -d mysql redis nacos

# 等待服务启动完成（约2-3分钟）
docker-compose -f docker-compose-production.yml logs -f nacos
```

#### 5.2 启动微服务

```bash
# 启动网关和公共服务
docker-compose -f docker-compose-production.yml up -d gateway-service common-service

# 等待基础服务启动完成
sleep 60

# 启动业务服务
docker-compose -f docker-compose-production.yml up -d device-comm-service oa-service access-service attendance-service video-service consume-service visitor-service

# 启动前端和负载均衡
docker-compose -f docker-compose-production.yml up -d web-admin nginx
```

#### 5.3 验证服务启动

```bash
# 检查所有服务状态
docker-compose -f docker-compose-production.yml ps

# 检查服务健康状态
curl http://localhost/health
curl http://localhost/api/common/health
```

### 步骤6: 初始化数据

#### 6.1 数据库初始化

```bash
# 检查数据库初始化日志
docker-compose -f docker-compose-production.yml logs mysql

# 手动执行初始化脚本（如需要）
docker exec -i ioedream-mysql-prod mysql -uroot -p${MYSQL_ROOT_PASSWORD} ioedream < deployment/mysql/init/init-data.sql
```

#### 6.2 Nacos配置导入

```bash
# 访问Nacos控制台
# URL: http://localhost:8848/nacos
# 用户名: nacos
# 密码: ${NACOS_AUTH_IDENTITY_VALUE}

# 导入配置文件（在Nacos控制台中操作）
# 配置文件位于: deployment/nacos/config/
```

---

## 🔧 服务管理

### 查看服务状态

```bash
# 查看所有服务
docker-compose -f docker-compose-production.yml ps

# 查看特定服务
docker-compose -f docker-compose-production.yml ps gateway-service

# 查看服务资源使用情况
docker stats
```

### 日志管理

```bash
# 查看所有服务日志
docker-compose -f docker-compose-production.yml logs -f

# 查看特定服务日志
docker-compose -f docker-compose-production.yml logs -f gateway-service

# 查看最近100行日志
docker-compose -f docker-compose-production.yml logs --tail=100 gateway-service

# 查看特定时间段的日志
docker-compose -f docker-compose-production.yml logs --since="2024-12-07T10:00:00" gateway-service
```

### 服务控制

```bash
# 启动服务
docker-compose -f docker-compose-production.yml up -d gateway-service

# 停止服务
docker-compose -f docker-compose-production.yml stop gateway-service

# 重启服务
docker-compose -f docker-compose-production.yml restart gateway-service

# 重新构建并启动
docker-compose -f docker-compose-production.yml up -d --build gateway-service

# 停止所有服务
docker-compose -f docker-compose-production.yml down

# 停止并删除数据卷
docker-compose -f docker-compose-production.yml down -v
```

### 扩缩容服务

```bash
# 扩展服务实例
docker-compose -f docker-compose-production.yml up -d --scale gateway-service=3

# 查看扩展后的服务
docker-compose -f docker-compose-production.yml ps
```

---

## 📊 监控与日志

### 系统监控

#### 1. 资源监控

```bash
# 实时资源使用情况
docker stats

# 查看磁盘使用情况
df -h
du -sh ${DATA_DIR}
du -sh ${LOGS_DIR}

# 查看内存使用情况
free -h
```

#### 2. 服务健康检查

```bash
# 检查所有服务健康状态
for service in gateway common device-comm oa access attendance video consume visitor; do
    echo "=== $service-service ==="
    curl -f http://localhost:8080/api/$service/health || echo "Health check failed"
done

# 检查Nacos服务列表
curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=gateway-service" \
  -H "Authorization: Bearer ${NACOS_AUTH_TOKEN}"
```

#### 3. 数据库监控

```bash
# 查看数据库连接数
docker exec ioedream-mysql-prod mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SHOW STATUS LIKE 'Threads_connected';"

# 查看慢查询
docker exec ioedream-mysql-prod mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SHOW VARIABLES LIKE 'slow_query_log';"
```

### 日志分析

#### 1. 应用日志

```bash
# 查看应用错误日志
docker-compose -f docker-compose-production.yml logs | grep ERROR

# 查看特定服务的警告日志
docker-compose -f docker-compose-production.yml logs gateway-service | grep WARN

# 实时监控错误日志
docker-compose -f docker-compose-production.yml logs -f | grep -E "ERROR|WARN"
```

#### 2. 访问日志分析

```bash
# 分析Nginx访问日志（如果挂载了日志目录）
tail -f ${LOGS_DIR}/nginx/access.log | grep -v "GET /health"

# 统计访问量最高的接口
awk '{print $7}' ${LOGS_DIR}/nginx/access.log | sort | uniq -c | sort -nr | head -10
```

### 性能指标

#### 1. 导出监控指标

```bash
# 获取Prometheus格式的指标
curl http://localhost:8080/actuator/prometheus

# 获取应用信息
curl http://localhost:8080/actuator/info
```

#### 2. 自定义监控脚本

创建监控脚本 `scripts/monitor.sh`:

```bash
#!/bin/bash

# 监控脚本
check_service_health() {
    local service=$1
    local url="http://localhost:8080/api/$service/health"

    if curl -f -s "$url" > /dev/null; then
        echo "✅ $service: Healthy"
        return 0
    else
        echo "❌ $service: Unhealthy"
        return 1
    fi
}

# 检查所有服务
services=("common" "device-comm" "oa" "access" "attendance" "video" "consume" "visitor")
for service in "${services[@]}"; do
    check_service_health "$service"
done
```

---

## 🔍 故障排查

### 常见问题

#### 1. 服务启动失败

**问题**: 服务无法启动
```bash
# 查看服务状态
docker-compose -f docker-compose-production.yml ps

# 查看启动日志
docker-compose -f docker-compose-production.yml logs gateway-service

# 检查端口占用
netstat -tulpn | grep 8080
```

**解决方案**:
```bash
# 停止占用端口的进程
sudo kill -9 <process_id>

# 或修改端口配置
sed -i 's/GATEWAY_PORT=8080/GATEWAY_PORT=8081/' .env
```

#### 2. 数据库连接失败

**问题**: 无法连接到MySQL

```bash
# 检查MySQL容器状态
docker ps | grep mysql

# 查看MySQL日志
docker logs ioedream-mysql-prod

# 测试数据库连接
docker exec -it ioedream-mysql-prod mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SELECT 1;"
```

**解决方案**:
```bash
# 重启MySQL服务
docker-compose -f docker-compose-production.yml restart mysql

# 检查环境变量配置
grep MYSQL_ .env
```

#### 3. Nacos启动失败

**问题**: Nacos无法连接到数据库

```bash
# 检查Nacos日志
docker logs ioedream-nacos-prod | grep -i error

# 验证数据库连接
docker exec ioedream-mysql-prod mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SHOW DATABASES;"
```

**解决方案**:
```bash
# 创建Nacos数据库
docker exec ioedream-mysql-prod mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "CREATE DATABASE IF NOT EXISTS nacos;"

# 重启Nacos服务
docker-compose -f docker-compose-production.yml restart nacos
```

#### 4. 前端无法访问

**问题**: Web页面无法打开

```bash
# 检查Nginx状态
docker ps | grep nginx

# 查看Nginx配置
docker exec ioedream-nginx-prod nginx -t

# 测试Nginx代理
curl -I http://localhost/
```

**解决方案**:
```bash
# 重新加载Nginx配置
docker exec ioedream-nginx-prod nginx -s reload

# 检查后端服务状态
curl http://localhost:8080/actuator/health
```

#### 5. 内存不足

**问题**: 服务因内存不足被杀死

```bash
# 查看系统内存
free -h

# 查看容器内存使用
docker stats

# 查看OOM日志
dmesg | grep -i "killed process"
```

**解决方案**:
```bash
# 调整JVM内存配置
sed -i 's/JVM_XMS=1g/JVM_XMS=2g/' .env
sed -i 's/JVM_XMX=4g/JVM_XMX=6g/' .env

# 重启服务
docker-compose -f docker-compose-production.yml restart
```

### 调试技巧

#### 1. 进入容器调试

```bash
# 进入应用容器
docker exec -it ioedream-gateway-service /bin/bash

# 进入数据库容器
docker exec -it ioedream-mysql-prod mysql -uroot -p${MYSQL_ROOT_PASSWORD}

# 进入Redis容器
docker exec -it ioedream-redis-prod redis-cli -a ${REDIS_PASSWORD}
```

#### 2. 实时日志监控

```bash
# 实时监控所有服务日志
docker-compose -f docker-compose-production.yml logs -f --tail=100

# 实时监控错误日志
docker-compose -f docker-compose-production.yml logs -f | grep -i error

# 监控特定关键词
docker-compose -f docker-compose-production.yml logs -f | grep -E "(ERROR|Exception|Failed)"
```

#### 3. 网络调试

```bash
# 测试服务间连通性
docker exec ioedream-gateway-service ping mysql
docker exec ioedream-gateway-service ping redis

# 测试端口连通性
docker exec ioedream-gateway-service nc -zv mysql 3306

# 查看网络配置
docker network ls
docker network inspect ioedream-network
```

---

## ⚡ 性能优化

### JVM优化

#### 1. 内存配置优化

```bash
# 根据服务器配置调整JVM内存
# 小型服务器 (8GB RAM)
JVM_XMS=512m
JVM_XMX=1024m

# 中型服务器 (16GB RAM)
JVM_XMS=1g
JVM_XMX=2g

# 大型服务器 (32GB+ RAM)
JVM_XMS=2g
JVM_XMX=4g
```

#### 2. GC优化配置

```bash
# 优化JVM参数
JVM_OPTS="-server
          -Xms${JVM_XMS}
          -Xmx${JVM_XMX}
          -XX:+UseG1GC
          -XX:MaxGCPauseMillis=200
          -XX:+PrintGCDetails
          -XX:+PrintGCTimeStamps
          -XX:+HeapDumpOnOutOfMemoryError
          -XX:HeapDumpPath=/var/log/app/
          -Dfile.encoding=UTF-8
          -Duser.timezone=Asia/Shanghai"
```

### 数据库优化

#### 1. MySQL配置优化

创建 `deployment/mysql/conf/my.cnf`:

```ini
[mysqld]
# 基础配置
default-storage-engine=INNODB
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

# 内存配置
innodb_buffer_pool_size=2G
innodb_log_file_size=256M
innodb_log_buffer_size=16M

# 连接配置
max_connections=500
max_connect_errors=1000
wait_timeout=28800

# 查询缓存
query_cache_type=1
query_cache_size=64M

# 慢查询日志
slow_query_log=1
slow_query_log_file=/var/log/mysql/slow.log
long_query_time=2

# 二进制日志
log-bin=mysql-bin
binlog_format=ROW
expire_logs_days=7
```

#### 2. 索引优化

```sql
-- 查看慢查询
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;

-- 分析表结构
EXPLAIN SELECT * FROM t_common_user WHERE status = 1 AND create_time > '2024-01-01';

-- 添加索引
CREATE INDEX idx_user_status_time ON t_common_user(status, create_time);
```

### 缓存优化

#### 1. Redis配置优化

创建 `deployment/redis/redis.conf`:

```ini
# 内存配置
maxmemory 1gb
maxmemory-policy allkeys-lru

# 持久化配置
save 900 1
save 300 10
save 60 10000

# 网络配置
tcp-keepalive 300
timeout 0

# 安全配置
requirepass ${REDIS_PASSWORD}
rename-command FLUSHDB ""
rename-command FLUSHALL ""

# 慢日志
slowlog-log-slower-than 10000
slowlog-max-len 128
```

#### 2. 应用级缓存策略

```java
// 多级缓存配置示例
@Cacheable(value = "users", key = "#userId", unless = "#result == null")
public UserEntity getUserById(Long userId) {
    return userDao.selectById(userId);
}
```

### Nginx优化

#### 1. 性能配置

```nginx
# 工作进程数
worker_processes auto;

# 连接数配置
events {
    worker_connections 2048;
    use epoll;
    multi_accept on;
}

# 缓存配置
proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=api_cache:100m max_size=1g;

# 连接池配置
upstream gateway_backend {
    least_conn;
    server gateway-service:8080 max_fails=3 fail_timeout=30s;
    keepalive 32;
}
```

#### 2. Gzip压缩

```nginx
# 启用压缩
gzip on;
gzip_min_length 1k;
gzip_comp_level 6;
gzip_types text/plain text/css application/json application/javascript;
gzip_vary on;
```

---

## 🔒 安全配置

### 基础安全

#### 1. 防火墙配置

```bash
# Ubuntu/Debian
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable

# CentOS/RHEL
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

#### 2. SSH安全配置

```bash
# 编辑SSH配置
sudo nano /etc/ssh/sshd_config

# 修改以下配置
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes
Port 22

# 重启SSH服务
sudo systemctl restart sshd
```

#### 3. 系统更新

```bash
# Ubuntu/Debian
sudo apt update && sudo apt upgrade -y

# CentOS/RHEL
sudo yum update -y
```

### 应用安全

#### 1. 密码安全

```bash
# 生成强密码
openssl rand -base64 32  # 数据库密码
openssl rand -hex 32      # JWT密钥
openssl rand -hex 64      # Nacos密钥

# 定期更换密码（建议每3个月）
# 更新.env文件中的密码
# 重启相关服务
```

#### 2. SSL/TLS配置

```nginx
# 仅使用安全的TLS版本
ssl_protocols TLSv1.2 TLSv1.3;

# 使用安全的加密套件
ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;

# 启用HSTS
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

# 其他安全头
add_header X-Frame-Options DENY always;
add_header X-Content-Type-Options nosniff always;
add_header X-XSS-Protection "1; mode=block" always;
```

#### 3. API安全

```java
// 接口限流配置
@RateLimiter(name = "api-limit", fallbackMethod = "rateLimitFallback")
public ResponseDTO<String> sensitiveApi() {
    // 业务逻辑
}

// 数据脱敏
@JsonIgnore
@ApiModelProperty(hidden = true)
private String sensitiveField;
```

### 网络安全

#### 1. 内网隔离

```yaml
# docker-compose.yml网络配置
networks:
  ioedream-network:
    driver: bridge
    internal: true  # 仅内部访问
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

#### 2. 访问控制

```nginx
# IP白名单
location /admin {
    allow 192.168.1.0/24;
    allow 10.0.0.0/8;
    deny all;
    proxy_pass http://web_admin_backend/;
}

# 限制请求频率
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=100r/m;

location /api/ {
    limit_req zone=api_limit burst=20 nodelay;
    proxy_pass http://gateway_backend/;
}
```

---

## 💾 备份与恢复

### 数据库备份

#### 1. 自动备份脚本

创建 `scripts/backup.sh`:

```bash
#!/bin/bash

# 数据库备份脚本
BACKUP_DIR="${DATA_DIR}/backup"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="ioedream"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 执行备份
docker exec ioedream-mysql-prod mysqldump \
  -uroot -p${MYSQL_ROOT_PASSWORD} \
  --single-transaction \
  --routines \
  --triggers \
  ${DB_NAME} > ${BACKUP_DIR}/${DB_NAME}_${DATE}.sql

# 压缩备份文件
gzip ${BACKUP_DIR}/${DB_NAME}_${DATE}.sql

# 删除7天前的备份
find $BACKUP_DIR -name "${DB_NAME}_*.sql.gz" -mtime +7 -delete

echo "备份完成: ${DB_NAME}_${DATE}.sql.gz"
```

#### 2. 设置定时备份

```bash
# 添加到crontab
crontab -e

# 每天凌晨2点执行备份
0 2 * * * /path/to/IOE-DREAM/scripts/backup.sh >> /var/log/backup.log 2>&1
```

### 数据恢复

#### 1. 数据库恢复

```bash
# 恢复数据库备份
gunzip < ${BACKUP_DIR}/${DB_NAME}_20241207_020000.sql.gz | \
docker exec -i ioedream-mysql-prod mysql -uroot -p${MYSQL_ROOT_PASSWORD} ${DB_NAME}

# 验证恢复结果
docker exec ioedream-mysql-prod mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SELECT COUNT(*) FROM ${DB_NAME}.t_common_user;"
```

#### 2. 配置文件备份

```bash
# 备份重要配置文件
tar -czf ${BACKUP_DIR}/config_${DATE}.tar.gz \
  .env \
  deployment/ \
  scripts/

# 恢复配置文件
tar -xzf ${BACKUP_DIR}/config_20241207_020000.tar.gz
```

### 完整系统备份

#### 1. Docker镜像备份

```bash
# 导出所有镜像
docker images --format "table {{.Repository}}:{{.Tag}}" | grep ioedream | \
  awk 'NR>1 {system("docker save "$1" -o "${BACKUP_DIR}/"$1".tar")}'

# 导出镜像列表
docker images --format "{{.Repository}}:{{.Tag}}" | grep ioedream > ${BACKUP_DIR}/images.list
```

#### 2. 数据卷备份

```bash
# 备份所有数据卷
docker volume ls --format "{{.Name}}" | grep ioedream | \
  while read volume; do
    docker run --rm -v $volume:/volume -v ${BACKUP_DIR}:/backup \
      alpine tar czf /backup/${volume}_${DATE}.tar.gz -C /volume .
  done
```

---

## 🔄 升级指南

### 准备工作

#### 1. 升级前检查清单

- [ ] 当前系统运行正常
- [ ] 已完成数据备份
- [ ] 确认新版本兼容性
- [ ] 准备回滚方案
- [ ] 选择维护时间窗口

#### 2. 创建升级脚本

创建 `scripts/upgrade.sh`:

```bash
#!/bin/bash

# 系统升级脚本
set -e

NEW_VERSION=$1
BACKUP_DIR="${DATA_DIR}/backup/upgrade_$(date +%Y%m%d_%H%M%S)"

if [ -z "$NEW_VERSION" ]; then
    echo "用法: $0 <new_version>"
    exit 1
fi

echo "开始升级到版本: $NEW_VERSION"

# 1. 备份当前系统
echo "备份当前系统..."
mkdir -p $BACKUP_DIR
docker-compose -f docker-compose-production.yml down
./scripts/backup.sh

# 2. 获取新代码
echo "获取新代码..."
git fetch origin
git checkout v$NEW_VERSION
git pull origin v$NEW_VERSION

# 3. 重新构建镜像
echo "重新构建Docker镜像..."
./scripts/docker-build.sh

# 4. 启动新版本
echo "启动新版本..."
docker-compose -f docker-compose-production.yml up -d

# 5. 健康检查
echo "等待服务启动..."
sleep 120

if curl -f http://localhost/health; then
    echo "✅ 升级成功！"
    echo "备份文件位置: $BACKUP_DIR"
else
    echo "❌ 升级失败，开始回滚..."
    ./scripts/rollback.sh $BACKUP_DIR
    exit 1
fi
```

### 升级流程

#### 1. 滚动升级

```bash
# 升级单个服务
docker-compose -f docker-compose-production.yml up -d --no-deps gateway-service

# 等待服务就绪
sleep 30

# 升级下一个服务
docker-compose -f docker-compose-production.yml up -d --no-deps common-service
```

#### 2. 蓝绿部署

```bash
# 启动新版本环境
COMPOSE_PROJECT_NAME=ioedream-green docker-compose -f docker-compose-production.yml up -d

# 测试新版本
curl http://localhost:8081/health

# 切换流量
# 修改Nginx配置指向新端口
docker exec ioedream-nginx-prod nginx -s reload

# 停止旧版本
COMPOSE_PROJECT_NAME=ioedream-blue docker-compose -f docker-compose-production.yml down
```

### 回滚方案

#### 1. 创建回滚脚本

创建 `scripts/rollback.sh`:

```bash
#!/bin/bash

# 系统回滚脚本
ROLLBACK_DIR=$1

if [ -z "$ROLLBACK_DIR" ]; then
    echo "用法: $0 <backup_directory>"
    exit 1
fi

echo "开始回滚到: $ROLLBACK_DIR"

# 1. 停止当前服务
docker-compose -f docker-compose-production.yml down

# 2. 恢复代码版本
git checkout $(cat $ROLLBACK_DIR/git_commit.txt)

# 3. 恢复配置文件
cp $ROLLBACK_DIR/.env .env

# 4. 恢复数据库（如需要）
if [ -f "$ROLLBACK_DIR/database_backup.sql.gz" ]; then
    gunzip < $ROLLBACK_DIR/database_backup.sql.gz | \
    docker exec -i ioedream-mysql-prod mysql -uroot -p${MYSQL_ROOT_PASSWORD} ioedream
fi

# 5. 恢复Docker镜像
if [ -f "$ROLLBACK_DIR/images.list" ]; then
    while read image; do
        if [ -f "$ROLLBACK_DIR/${image//\//_}.tar" ]; then
            docker load -i "$ROLLBACK_DIR/${image//\//_}.tar"
        fi
    done < $ROLLBACK_DIR/images.list
fi

# 6. 启动服务
docker-compose -f docker-compose-production.yml up -d

echo "✅ 回滚完成！"
```

---

## 📞 支持与联系

### 技术支持

- **项目地址**: https://github.com/your-org/IOE-DREAM
- **问题反馈**: https://github.com/your-org/IOE-DREAM/issues
- **文档中心**: https://docs.ioedream.com
- **技术交流**: https://community.ioedream.com

### 联系方式

- **技术支持邮箱**: support@ioedream.com
- **商业合作邮箱**: business@ioedream.com
- **安全问题报告**: security@ioedream.com

### 服务支持

- **7x24小时监控**: 生产环境系统监控
- **工作日技术支持**: 9:00-18:00 (工作日)
- **紧急故障响应**: 1小时内响应
- **定期维护通知**: 提前7天通知

---

## 📄 许可证

本部署指南遵循 [MIT License](https://opensource.org/licenses/MIT)。

---

**最后更新**: 2024-12-07
**版本**: v1.0.0
**维护团队**: IOE-DREAM 开发团队