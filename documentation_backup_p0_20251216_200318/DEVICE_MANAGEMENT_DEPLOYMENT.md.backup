# 设备管理模块部署文档

## 概述

本文档描述了SmartAdmin v3设备管理模块的完整部署流程，包括后端服务、前端Web应用和移动端应用的部署步骤。

## 系统要求

### 基础环境
- **操作系统**: Linux (推荐 CentOS 7+ 或 Ubuntu 18+) 或 Windows Server 2016+
- **Java**: JDK 17+
- **Node.js**: 18.0+
- **MySQL**: 8.0+
- **Redis**: 6.0+

### 硬件要求
- **CPU**: 4核心以上
- **内存**: 8GB以上
- **存储**: 50GB以上可用空间
- **网络**: 稳定的网络连接

## 部署架构
## 📋 IOE-DREAM七微服务架构

**核心架构组成**:
- **Gateway Service (8080)**: API网关
- **Common Service (8088)**: 公共模块微服务
- **DeviceComm Service (8087)**: 设备通讯微服务
- **OA Service (8089)**: OA微服务
- **Access Service (8090)**: 门禁服务
- **Attendance Service (8091)**: 考勤服务
- **Video Service (8092)**: 视频服务
- **Consume Service (8094)**: 消费服务
- **Visitor Service (8095)**: 访客服务

**架构特点**:
- 基于Spring Boot 3.5.8 + Java 17
- 严格遵循企业级微服务规范
- 支持高并发、高可用、水平扩展

**技术栈标准**:
- **数据库**: MySQL 8.0 + Druid连接池
- **缓存**: Redis + Caffeine多级缓存
- **注册中心**: Nacos
- **配置中心**: Nacos Config
- **认证授权**: Sa-Token

## 🏗️ 四层架构规范

**标准架构模式**:
```
Controller (接口控制层)
    ↓
Service (核心业务层)
    ↓
Manager (流程管理层)
    ↓
DAO (数据访问层)
```

**层级职责**:
- **Controller层**: HTTP请求处理、参数验证、权限控制
- **Service层**: 核心业务逻辑、事务管理、业务规则验证
- **Manager层**: 复杂流程编排、多数据组装、第三方服务集成
- **DAO层**: 数据库CRUD操作、SQL查询实现、数据访问边界

**严格禁止跨层访问**: Controller不能直接调用Manager/DAO！
```
## ⚠️ IOE-DREAM零容忍规则（强制执行）

**必须遵守的架构规则**:
- ✅ **必须使用 @Resource 注入依赖**
- ✅ **必须使用 @Mapper 注解** (禁止@Repository)
- ✅ **必须使用 Dao 后缀** (禁止Repository)
- ✅ **必须使用 @RestController 注解**
- ✅ **必须使用 @Valid 参数校验**
- ✅ **必须返回统一ResponseDTO格式**
- ✅ **必须遵循四层架构边界**

**严格禁止事项**:
- ❌ **禁止使用 @Autowired 注入**
- ❌ **禁止使用 @Repository 注解**
- ❌ **禁止使用 Repository 后缀命名**
- ❌ **禁止跨层访问**
- ❌ **禁止在Controller中包含业务逻辑**
- ❌ **禁止直接访问数据库**

**违规后果**: P0级问题，立即修复，禁止合并！
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端Web应用    │    │   移动端应用     │    │   管理后台       │
│   (Vue 3)       │    │   (uni-app)     │    │   (Vue 3)       │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │      Nginx反向代理        │
                    │    (端口80/443)          │
                    └─────────────┬─────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │     Spring Boot应用       │
                    │    (端口1024)            │
                    └─────────────┬─────────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
┌─────────▼───────┐    ┌─────────▼───────┐    ┌─────────▼───────┐
│    MySQL数据库   │    │    Redis缓存     │    │   文件存储       │
│   (端口33060)    │    │   (端口6389)     │    │  (本地/OSS)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 部署步骤

### 1. 数据库初始化

#### 1.1 创建数据库
```sql
CREATE DATABASE smart_admin_v3 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 1.2 导入数据库脚本
```bash
# 进入数据库脚本目录
cd 数据库SQL脚本/mysql

# 导入基础数据库结构
mysql -h192.168.10.110 -P33060 -uroot -p smart_admin_v3 < smart_admin_v3.sql

# 导入设备管理模块表结构
mysql -h192.168.10.110 -P33060 -uroot -p smart_admin_v3 < ../../smart_device_tables.sql
```

#### 1.3 验证表创建
```sql
USE smart_admin_v3;

-- 检查设备管理相关表
SHOW TABLES LIKE 't_smart_device%';

-- 验证表结构
DESC t_smart_device;
DESC t_smart_device_type;
DESC t_smart_device_group;
```

### 2. 后端服务部署

#### 2.1 编译打包
```bash
# 进入后端项目目录
cd smart-admin-api-java17-springboot3

# 编译整个项目（跳过测试）
mvn clean install -DskipTests

# 编译成功后，JAR包位置：
# sa-admin/target/sa-admin-dev-3.0.0.jar
```

#### 2.2 配置文件修改
编辑 `sa-base/src/main/resources/prod/sa-base.yaml`：

```yaml
# 数据库配置
spring:
  datasource:
    druid:
      url: jdbc:mysql://192.168.10.110:33060/smart_admin_v3?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
      username: root
      password: # 实际密码

  # Redis配置
  data:
    redis:
      host: 127.0.0.1
      port: 6389
      password: zkteco3100
      database: 1

# 应用配置
server:
  port: 1024
  servlet:
    context-path: /

# 文件上传配置
file:
  upload:
    path: /data/smart-admin/uploads
    domain: http://your-domain.com/uploads
```

#### 2.3 启动服务
```bash
# 创建日志目录
mkdir -p /data/smart-admin/logs

# 启动应用
java -jar \
  -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/data/smart-admin/logs/heapdump.hprof \
  -Dspring.profiles.active=prod \
  -Dfile.encoding=UTF-8 \
  sa-admin/target/sa-admin-dev-3.0.0.jar \
  > /data/smart-admin/logs/application.log 2>&1 &

# 检查服务状态
ps aux | grep sa-admin
tail -f /data/smart-admin/logs/application.log
```

#### 2.4 验证后端服务
```bash
# 检查端口监听
netstat -tlnp | grep 1024

# 测试API健康检查
curl -X GET http://localhost:1024/api/auth/health

# 运行API测试脚本
node scripts/test-device-api.js
```

### 3. 前端Web应用部署

#### 3.1 安装依赖
```bash
# 进入前端项目目录
cd smart-admin-web-javascript

# 安装依赖
npm install --registry=https://registry.npmmirror.com
```

#### 3.2 环境配置
编辑 `.env.production`：
```env
# API地址
VITE_API_BASE_URL=http://your-domain.com:1024

# 应用标题
VITE_APP_TITLE=SmartAdmin 设备管理系统

# 其他配置...
```

#### 3.3 构建生产版本
```bash
# 构建生产环境
npm run build:prod

# 构建完成后，dist目录包含所有静态文件
ls -la dist/
```

#### 3.4 Nginx配置
创建 `/etc/nginx/conf.d/smart-admin.conf`：
```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /data/smart-admin/web;
        index index.html;
        try_files $uri $uri/ /index.html;

        # 缓存配置
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }

    # API代理
    location /api/ {
        proxy_pass http://localhost:1024;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # 文件上传
    location /uploads/ {
        alias /data/smart-admin/uploads/;
        expires 30d;
    }
}
```

#### 3.5 部署Web应用
```bash
# 创建Web目录
mkdir -p /data/smart-admin/web

# 复制构建文件
cp -r dist/* /data/smart-admin/web/

# 设置权限
chown -R nginx:nginx /data/smart-admin/web
chmod -R 755 /data/smart-admin/web

# 重启Nginx
systemctl restart nginx
systemctl enable nginx
```

### 4. 移动端应用部署

#### 4.1 H5版本部署
```bash
# 进入移动端项目目录
cd smart-app

# 安装依赖
npm install --registry=https://registry.npmmirror.com

# 修改配置文件 src/config/index.js
# 将 API_BASE_URL 改为实际地址

# 构建H5版本
npm run build:h5

# 部署到Web服务器
mkdir -p /data/smart-admin/mobile
cp -r dist/build/h5/* /data/smart-admin/mobile/
```

#### 4.2 Nginx移动端配置
在Nginx配置中添加：
```nginx
# 移动端H5版本
location /mobile/ {
    alias /data/smart-admin/mobile/;
    index index.html;
    try_files $uri $uri/ /mobile/index.html;
}
```

#### 4.3 微信小程序部署
```bash
# 使用HBuilderX打开项目
# 配置小程序AppID
# 发行 -> 小程序-微信
# 生成小程序包，上传到微信开发者后台
```

### 5. 监控配置

#### 5.1 应用监控
```bash
# 安装监控工具
yum install -y htop iotop nethogs

# 监控Java进程
jstat -gc -t $(pgrep java) 5s

# 监控系统资源
htop
```

#### 5.2 日志监控
```bash
# 配置logrotate
cat > /etc/logrotate.d/smart-admin << EOF
/data/smart-admin/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 644 root root
    postrotate
        systemctl reload smart-admin
    endscript
}
EOF
```

#### 5.3 服务监控脚本
创建 `/data/smart-admin/scripts/monitor.sh`：
```bash
#!/bin/bash

# 检查Java进程
if ! pgrep -f "sa-admin" > /dev/null; then
    echo "$(date): SmartAdmin服务未运行，正在启动..."
    cd /data/smart-admin
    nohup java -jar -Dspring.profiles.active=prod sa-admin-dev-3.0.0.jar > logs/application.log 2>&1 &
fi

# 检查Nginx
if ! systemctl is-active nginx > /dev/null; then
    echo "$(date): Nginx服务未运行，正在启动..."
    systemctl start nginx
fi

# 检查MySQL连接
if ! mysqladmin ping -h192.168.10.110 -P33060 -uroot -p123456 --silent; then
    echo "$(date): MySQL连接失败"
fi

# 检查Redis连接
if ! redis-cli -h 127.0.0.1 -p 6389 -a zkteco3100 ping > /dev/null; then
    echo "$(date): Redis连接失败"
fi
```

设置定时任务：
```bash
# 添加到crontab
crontab -e

# 每5分钟检查一次服务状态
*/5 * * * * /data/smart-admin/scripts/monitor.sh >> /data/smart-admin/logs/monitor.log 2>&1
```

## 验证部署

### 1. 后端服务验证
```bash
# 检查服务状态
curl -X GET http://your-domain.com:1024/api/auth/health

# 运行完整API测试
node scripts/test-device-api.js
```

### 2. 前端应用验证
1. 访问 `http://your-domain.com`
2. 使用管理员账号登录（admin/123456）
3. 进入设备管理模块
4. 验证设备列表、新增、编辑、删除功能

### 3. 移动端验证
1. 访问 `http://your-domain.com/mobile`
2. 验证移动端设备管理功能
3. 测试响应式布局

## 常见问题

### 1. 后端启动失败
- 检查Java版本：`java -version`
- 检查端口占用：`netstat -tlnp | grep 1024`
- 检查数据库连接
- 查看应用日志：`tail -f /data/smart-admin/logs/application.log`

### 2. 前端访问失败
- 检查Nginx状态：`systemctl status nginx`
- 检查Nginx配置：`nginx -t`
- 检查文件权限：`ls -la /data/smart-admin/web/`

### 3. 数据库连接问题
- 检查MySQL服务：`systemctl status mysql`
- 检查网络连通性：`telnet 192.168.10.110 33060`
- 验证数据库用户权限

### 4. Redis连接问题
- 检查Redis服务：`systemctl status redis`
- 检查Redis配置：`redis-cli -h 127.0.0.1 -p 6389 ping`

## 性能优化

### 1. JVM优化
```bash
# 生产环境JVM参数
-Xms4g -Xmx8g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat
-XX:+HeapDumpOnOutOfMemoryError
```

### 2. 数据库优化
- 配置合适的连接池大小
- 添加适当的索引
- 定期分析和优化查询

### 3. 前端优化
- 启用Gzip压缩
- 配置CDN加速
- 优化静态资源缓存

## 备份策略

### 1. 数据库备份
```bash
# 每日备份脚本
#!/bin/bash
BACKUP_DIR="/data/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -h192.168.10.110 -P33060 -uroot -p123456 smart_admin_v3 > $BACKUP_DIR/smart_admin_v3_$DATE.sql
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
```

### 2. 应用备份
```bash
# 备份应用文件和配置
tar -czf /data/backup/app/smart-admin_$(date +%Y%m%d).tar.gz \
  /data/smart-admin/web \
  /data/smart-admin/mobile \
  /data/smart-admin/scripts
```

## 安全配置

### 1. 防火墙配置
```bash
# 开放必要端口
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --permanent --add-port=1024/tcp
firewall-cmd --reload
```

### 2. SSL证书配置
```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;

    # SSL配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # 其他配置...
}
```

### 3. 安全加固
- 定期更新系统和依赖
- 配置 fail2ban 防止暴力破解
- 限制管理后台访问IP
- 定期安全扫描

---

**部署完成后，请务必进行完整的测试验证，确保所有功能正常运行。**