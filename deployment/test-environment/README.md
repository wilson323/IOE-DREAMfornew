# IOE-DREAM 集成测试环境使用指南

## 📋 概述

本目录包含IOE-DREAM集成测试所需的Docker Compose配置和初始化脚本。

## 🚀 快速开始

### 1. 启动测试环境

```bash
# 进入测试环境目录
cd deployment/test-environment

# 启动所有测试服务（MySQL + Redis + Nacos）
docker-compose -f docker-compose-test.yml up -d

# 查看服务状态
docker-compose -f docker-compose-test.yml ps

# 查看服务日志
docker-compose -f docker-compose-test.yml logs -f
```

### 2. 等待服务就绪

```bash
# 等待MySQL启动完成（约30秒）
# 等待Redis启动完成（约10秒）
# 等待Nacos启动完成（约60秒）

# 验证MySQL连接
docker exec -it ioedream-mysql-test mysql -uroot -p123456 -e "SELECT 1"

# 验证Redis连接
docker exec -it ioedream-redis-test redis-cli ping

# 验证Nacos连接
curl http://localhost:8849/nacos/v1/console/health/readiness
```

### 3. 运行集成测试

```bash
# 运行考勤服务集成测试
cd microservices/ioedream-attendance-service
mvn test -Dtest=AttendanceStrategyEndToEndTest

# 运行门禁服务集成测试
cd microservices/ioedream-access-service
mvn test -Dtest=AccessMobileEndToEndTest

# 运行所有集成测试
mvn test -Dtest="*EndToEndTest"
```

### 4. 停止测试环境

```bash
cd deployment/test-environment

# 停止所有服务
docker-compose -f docker-compose-test.yml down

# 停止并删除数据卷（完全清理）
docker-compose -f docker-compose-test.yml down -v
```

## 📊 服务配置

### MySQL 测试数据库

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 容器名 | ioedream-mysql-test | Docker容器名称 |
| 端口 | 3307 | 宿主机端口（避免冲突） |
| 数据库 | ioedream_test | 测试数据库名 |
| 用户名 | root / test_user | 默认用户 |
| 密码 | 123456 | 默认密码 |
| 字符集 | utf8mb4 | 支持emoji和特殊字符 |

**连接字符串**:
```
jdbc:mysql://127.0.0.1:3307/ioedream_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
```

### Redis 测试缓存

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 容器名 | ioedream-redis-test | Docker容器名称 |
| 端口 | 6380 | 宿主机端口（避免冲突） |
| 数据库 | 15 | 测试专用数据库 |
| 最大内存 | 256MB | 测试环境限制 |
| 持久化 | 禁用 | 加快测试速度 |

**连接配置**:
```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6380
      database: 15
      password: (无)
```

### Nacos 测试环境

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 容器名 | ioedream-nacos-test | Docker容器名称 |
| 端口 | 8849 | 宿主机端口（避免冲突） |
| 命名空间 | test | 测试专用命名空间 |
| 用户名 | nacos | 默认用户 |
| 密码 | nacos | 默认密码 |

**访问地址**: http://localhost:8849/nacos

## 🔧 自定义配置

### 修改端口

创建 `.env` 文件来自定义端口配置：

```bash
# .env 文件内容
MYSQL_TEST_PORT=3307
REDIS_TEST_PORT=6380
NACOS_TEST_PORT=8849
```

### 修改密码

```bash
# .env 文件内容
MYSQL_TEST_PASSWORD=your_secure_password
REDIS_TEST_PASSWORD=your_redis_password
NACOS_TEST_PASSWORD=your_nacos_password
```

### 修改数据库名

```bash
# .env 文件内容
MYSQL_TEST_DATABASE=ioedream_test_custom
```

## 📁 初始化脚本

### 数据库初始化

在 `mysql-test-init/` 目录下放置SQL初始化脚本：

1. **01-create-test-database.sql** - 创建测试数据库
2. **02-create-test-schema.sql** - 创建测试表结构
3. **03-insert-test-data.sql** - 插入测试数据

示例：

```sql
-- 01-create-test-database.sql
CREATE DATABASE IF NOT EXISTS ioedream_test
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ioedream_test;
```

```sql
-- 02-create-test-schema.sql
-- 用户表
CREATE TABLE IF NOT EXISTS t_common_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  status TINYINT DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_flag TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

```sql
-- 03-insert-test-data.sql
-- 插入测试用户
INSERT INTO t_common_user (username, password) VALUES
  ('test_user_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EQCbKYaLJHc2FJWNNJqK4'),
  ('test_user_002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EQCbKYaLJHc2FJWNNJqK4');
```

## 🐛 故障排查

### MySQL 连接失败

```bash
# 检查MySQL容器状态
docker ps | grep mysql-test

# 查看MySQL日志
docker logs ioedream-mysql-test

# 进入MySQL容器
docker exec -it ioedream-mysql-test bash

# 检查MySQL进程
ps aux | grep mysql

# 重启MySQL容器
docker restart ioedream-mysql-test
```

### Redis 连接失败

```bash
# 检查Redis容器状态
docker ps | grep redis-test

# 查看Redis日志
docker logs ioedream-redis-test

# 测试Redis连接
docker exec -it ioedream-redis-test redis-cli ping

# 重启Redis容器
docker restart ioedream-redis-test
```

### Nacos 连接失败

```bash
# 检查Nacos容器状态
docker ps | grep nacos-test

# 查看Nacos日志
docker logs ioedream-nacos-test

# 检查Nacos健康状态
curl http://localhost:8849/nacos/v1/console/health/readiness

# 重启Nacos容器
docker restart ioedream-nacos-test
```

### 端口冲突

如果默认端口与本地服务冲突：

1. 修改 `.env` 文件中的端口配置
2. 重新启动服务：
   ```bash
   docker-compose -f docker-compose-test.yml down
   docker-compose -f docker-compose-test.yml up -d
   ```

## 📝 环境变量配置

在项目根目录创建 `.env.test` 文件：

```bash
# MySQL测试环境
MYSQL_TEST_HOST=127.0.0.1
MYSQL_TEST_PORT=3307
MYSQL_TEST_DATABASE=ioedream_test
MYSQL_TEST_USERNAME=root
MYSQL_TEST_PASSWORD=123456

# Redis测试环境
REDIS_TEST_HOST=127.0.0.1
REDIS_TEST_PORT=6380
REDIS_TEST_DATABASE=15
REDIS_TEST_PASSWORD=

# Nacos测试环境
NACOS_TEST_SERVER_ADDR=127.0.0.1:8849
NACOS_TEST_NAMESPACE=test
NACOS_TEST_GROUP=TEST_GROUP
NACOS_TEST_USERNAME=nacos
NACOS_TEST_PASSWORD=nacos

# JWT测试环境
JWT_TEST_SECRET=test-jwt-secret-key-for-integration-testing-must-be-at-least-256-bits-long-for-security
JWT_TEST_EXPIRATION=3600
JWT_TEST_REFRESH_EXPIRATION=7200
```

然后运行测试时加载环境变量：

```bash
# Windows PowerShell
$env:MYSQL_TEST_HOST="127.0.0.1"
$env:MYSQL_TEST_PORT="3307"
mvn test -Dtest=AttendanceStrategyEndToEndTest

# Linux/Mac
source .env.test
mvn test -Dtest=AttendanceStrategyEndToEndTest
```

## 🔍 相关文档

- **集成测试报告**: [P1_INTEGRATION_TEST_COMPLETE_REPORT.md](../../P1_INTEGRATION_TEST_COMPLETE_REPORT.md)
- **单元测试报告**: [P0_UNIT_TEST_COMPLETE_REPORT.md](../../P0_UNIT_TEST_COMPLETE_REPORT.md)
- **全局架构规范**: [CLAUDE.md](../../CLAUDE.md)

---

**作者**: IOE-DREAM Team
**创建日期**: 2025-01-30
**版本**: v1.0.0
