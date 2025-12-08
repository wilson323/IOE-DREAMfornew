# IOE-DREAM 全局配置一致性规范

> **版本**: v1.2.0  
> **更新日期**: 2025-01-31  
> **维护人**: IOE-DREAM 架构团队

---

## 📋 配置一致性原则

### 黄金法则
1. **单一数据源**: 所有配置参数应在 `docker-compose-all.yml` 和 `.env` 文件中集中定义
2. **环境变量传递**: 微服务从Docker环境变量获取配置，避免硬编码
3. **默认值一致**: 所有默认值必须在各处保持相同
4. **命名规范**: 使用统一的环境变量命名约定

---

## 🔧 基础设施配置参数

### MySQL 数据库配置

| 参数名称 | 环境变量 | 默认值 | 使用位置 |
|---------|---------|--------|---------|
| 主机地址 | `MYSQL_HOST` | `mysql` (容器) / `localhost` (本地) | 所有微服务 |
| 端口 | `MYSQL_PORT` | `3306` | 所有微服务 |
| Root密码 | `MYSQL_ROOT_PASSWORD` | `root1234` | MySQL, Nacos, 微服务 |
| 业务数据库 | `MYSQL_DATABASE` | `ioedream` | 微服务 |
| Nacos数据库 | - | `nacos` | Nacos, db-init |
| 用户名 | `MYSQL_USERNAME` | `root` | 微服务 |
| 字符集 | - | `utf8mb4` | MySQL, Nacos |

### Redis 缓存配置

| 参数名称 | 环境变量 | 默认值 | 使用位置 |
|---------|---------|--------|---------|
| 主机地址 | `REDIS_HOST` | `redis` (容器) / `localhost` (本地) | 所有微服务 |
| 端口 | `REDIS_PORT` | `6379` | 所有微服务 |
| 密码 | `REDIS_PASSWORD` | `redis123` | Redis, 所有微服务 |
| 数据库编号 | - | `0` | 微服务 |

### Nacos 注册中心配置

| 参数名称 | 环境变量 | 默认值 | 使用位置 |
|---------|---------|--------|---------|
| 服务地址 | `NACOS_SERVER_ADDR` | `nacos:8848` (容器) | 所有微服务 |
| 命名空间 | `NACOS_NAMESPACE` | `public` | 所有微服务 |
| 用户名 | `NACOS_USERNAME` | `nacos` | 微服务 (可选) |
| 密码 | `NACOS_PASSWORD` | `nacos` | 微服务 (可选) |
| 认证Token | `NACOS_AUTH_TOKEN` | `SecretKey0123...` (64字符) | Nacos |

---

## 🔌 微服务端口分配

### 端口分配表（强制规范）

| 服务名称 | 容器名称 | HTTP端口 | 内部端口 | 说明 |
|---------|---------|---------|---------|------|
| **基础设施** |
| MySQL | ioedream-mysql | 3306 | - | 数据库 |
| Redis | ioedream-redis | 6379 | - | 缓存 |
| Nacos | ioedream-nacos | 8848, 9848, 9849 | - | 注册中心 |
| **业务服务** |
| gateway-service | ioedream-gateway-service | 8080 | - | API网关 |
| common-service | ioedream-common-service | 8088 | - | 公共服务 |
| device-comm-service | ioedream-device-comm-service | 8087 | TCP:18087, UDP:18089 | 设备通讯 |
| oa-service | ioedream-oa-service | 8089 | - | OA服务 |
| access-service | ioedream-access-service | 8090 | - | 门禁服务 |
| attendance-service | ioedream-attendance-service | 8091 | - | 考勤服务 |
| video-service | ioedream-video-service | 8092 | - | 视频服务 |
| consume-service | ioedream-consume-service | 8094 | - | 消费服务 |
| visitor-service | ioedream-visitor-service | 8095 | - | 访客服务 |

### 端口冲突预防规则

1. **HTTP端口范围**: 8080-8095（业务微服务）
2. **TCP协议端口范围**: 18000-18999（设备通讯协议）
3. **UDP协议端口范围**: 18000-18999（设备通讯协议）
4. **禁止使用端口**: 8848, 9848, 9849（Nacos专用）, 3306（MySQL）, 6379（Redis）

---

## 🏥 健康检查配置

### 标准健康检查参数

| 服务类型 | 检查命令 | interval | timeout | retries | start_period |
|---------|---------|---------|---------|---------|-------------|
| MySQL | `mysqladmin ping` | 10s | 5s | 5 | 30s |
| Redis | `redis-cli -a $PWD ping` | 10s | 3s | 3 | 10s |
| Nacos | `wget --spider ...readiness` | 15s | 10s | 10 | 90s |
| 微服务 | `curl .../actuator/health` | 30s | 5s | 3 | 90s |

### 健康检查端点

```yaml
# MySQL健康检查
test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD:-root1234}"]

# Redis健康检查（必须包含密码）
test: ["CMD-SHELL", "redis-cli -a ${REDIS_PASSWORD:-redis123} ping || exit 1"]

# Nacos健康检查（使用wget，不用curl）
test: ["CMD-SHELL", "wget --spider --tries=1 --no-verbose http://localhost:8848/nacos/v1/console/health/readiness || exit 1"]

# Spring Boot微服务健康检查
test: ["CMD", "curl", "-f", "http://localhost:${SERVER_PORT}/actuator/health"]
```

---

## 📝 已修复问题清单

### 2025-01-31 修复

| 问题编号 | 问题描述 | 根本原因 | 解决方案 |
|---------|---------|---------|---------|
| #001 | Redis健康检查失败 | 健康检查命令缺少密码参数 | 添加 `-a ${REDIS_PASSWORD}` |
| #002 | device-comm端口冲突 | TCP/UDP端口与其他服务HTTP端口冲突 | 更改为18087/18089 |
| #003 | Nacos容器启动失败 | 使用curl但Nacos镜像不包含curl | 改用wget命令 |
| #004 | Nacos健康检查超时 | start_period和retries不足 | 增加到90s/10次 |
| #005 | 数据库连接超时 | 缺少连接参数 | 添加connectTimeout等参数 |

---

## ✅ 配置一致性检查清单

### 启动前检查

- [ ] `.env` 文件存在且包含所有必需的环境变量
- [ ] `MYSQL_ROOT_PASSWORD` 在MySQL、Nacos、所有微服务中一致
- [ ] `REDIS_PASSWORD` 在Redis命令、健康检查、微服务中一致
- [ ] 所有微服务的 `NACOS_SERVER_ADDR` 指向正确地址
- [ ] 端口分配无冲突
- [ ] 网络配置正确（所有服务在同一网络）

### 健康检查清单

- [ ] MySQL: `mysqladmin` 命令包含正确密码
- [ ] Redis: `redis-cli` 命令包含 `-a $PASSWORD`
- [ ] Nacos: 使用 `wget` 而非 `curl`
- [ ] 微服务: actuator health端点正确暴露

---

## 🚀 快速诊断命令

```powershell
# 查看所有容器状态
docker ps -a --filter "name=ioedream"

# 查看Nacos日志
docker logs ioedream-nacos --tail 100

# 查看数据库初始化日志
docker logs ioedream-db-init

# 检查网络连通性
docker exec ioedream-nacos ping -c 1 mysql

# 检查Nacos健康状态
docker exec ioedream-nacos wget -q -O - http://localhost:8848/nacos/v1/console/health/readiness

# 完全重启
docker-compose -f docker-compose-all.yml down -v && docker-compose -f docker-compose-all.yml up -d
```

---

## 📚 相关文档

- [Docker部署指南](./README.md)
- [端口冲突分析](./PORT_CONFLICT_ANALYSIS.md)
- [全局配置标准](../../technical/GLOBAL_CONFIGURATION_STANDARDS.md)
- [启动问题诊断脚本](../../../scripts/fix-docker-compose-startup.ps1)
