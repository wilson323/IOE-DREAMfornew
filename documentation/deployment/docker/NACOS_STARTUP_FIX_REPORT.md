# Nacos容器启动失败修复报告

> **修复日期**: 2025-01-31  
> **问题严重程度**: P0 (阻塞系统启动)  
> **状态**: ✅ 已修复

---

## 📋 问题描述

### 错误现象
```
✘ Container ioedream-nacos Error
dependency failed to start: container ioedream-nacos is unhealthy
```

### 影响范围
- Nacos服务无法启动
- 所有依赖Nacos的微服务（9个）全部启动失败
- 整个系统无法正常运行

---

## 🔍 根因分析

### 直接原因
**Nacos健康检查命令使用了`curl`，但Nacos官方Docker镜像(`nacos/nacos-server:v2.3.0`)不包含`curl`命令**

### 原始配置 (错误)
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8848/nacos/v1/console/health/readiness"]
```

### 问题链
1. Docker启动Nacos容器
2. 健康检查执行 `curl` 命令
3. `curl` 命令不存在，健康检查失败
4. 连续10次失败后，容器被标记为 `unhealthy`
5. 依赖Nacos的微服务检测到Nacos不健康，拒绝启动

---

## ✅ 修复方案

### 1. 更换健康检查命令

**修复后配置**:
```yaml
healthcheck:
  # 使用wget替代curl（Nacos镜像包含wget但不包含curl）
  test: ["CMD-SHELL", "wget --spider --tries=1 --no-verbose http://localhost:8848/nacos/v1/console/health/readiness || exit 1"]
  interval: 15s
  timeout: 10s
  retries: 10
  start_period: 90s
```

### 2. 优化Nacos配置

新增配置项:
```yaml
# 数据库连接参数优化
- MYSQL_SERVICE_DB_PARAM=characterEncoding=utf8&connectTimeout=10000&socketTimeout=30000&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai

# JVM内存优化
- JVM_XMS=256m
- JVM_XMX=512m
- JVM_XMN=128m

# 新增gRPC端口映射
ports:
  - "8848:8848"   # HTTP端口
  - "9848:9848"   # gRPC端口
  - "9849:9849"   # gRPC端口(Raft)
```

### 3. 调整健康检查参数

| 参数 | 原值 | 新值 | 原因 |
|-----|-----|-----|-----|
| `retries` | 5 | 10 | Nacos启动较慢，需要更多重试 |
| `start_period` | 60s | 90s | 给予更多初始化时间 |
| `timeout` | 5s | 10s | 防止网络延迟导致超时 |

---

## 📁 修改的文件

| 文件 | 修改内容 |
|-----|---------|
| `docker-compose-all.yml` | Nacos健康检查命令、环境变量、端口映射 |
| `scripts/fix-docker-compose-startup.ps1` | 新增：启动诊断和修复脚本 |
| `scripts/verify-docker-config.ps1` | 新增：配置验证脚本 |
| `documentation/deployment/docker/GLOBAL_CONFIG_CONSISTENCY.md` | 新增：全局配置一致性文档 |

---

## 🚀 验证步骤

### 1. 验证配置
```powershell
.\scripts\verify-docker-config.ps1
```

### 2. 完全重启服务
```powershell
docker-compose -f docker-compose-all.yml down -v
docker-compose -f docker-compose-all.yml up -d
```

### 3. 监控启动过程
```powershell
# 查看Nacos日志
docker logs -f ioedream-nacos

# 查看容器状态
docker ps -a --filter "name=ioedream"
```

### 4. 验证Nacos健康
```powershell
# 等待90秒后检查
docker exec ioedream-nacos wget -q -O - http://localhost:8848/nacos/v1/console/health/readiness
```

---

## 📊 预期结果

启动后应看到：
```
✔ Container ioedream-mysql     Healthy
✔ Container ioedream-redis     Healthy
✔ Container ioedream-db-init   Exited (0)
✔ Container ioedream-nacos     Healthy     # 关键！
✔ Container ioedream-gateway-service    Started
✔ Container ioedream-common-service     Started
... (其他微服务)
```

---

## 🔗 相关问题

此修复同时解决了之前发现的问题：

1. **#001 Redis健康检查失败** - 已在之前修复
2. **#002 端口冲突** - 已在之前修复
3. **#003 Nacos健康检查失败** - 本次修复 ✅

---

## 📝 经验总结

### Docker镜像健康检查最佳实践

1. **检查镜像内可用命令**: 不同镜像包含不同的工具
   - Alpine镜像通常有: `wget`, `nc`
   - Debian镜像通常有: `curl`, `wget`
   - 最小镜像可能只有: `sh`

2. **使用通用命令**:
   ```yaml
   # 推荐：使用wget（大多数镜像都有）
   wget --spider --tries=1 URL || exit 1
   
   # 或使用nc检查端口
   nc -z localhost 8848 || exit 1
   ```

3. **合理设置超时参数**:
   - Java应用: `start_period` >= 60s
   - 数据库应用: `start_period` >= 30s
   - 轻量服务: `start_period` >= 10s
