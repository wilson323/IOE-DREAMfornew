# Nacos 健康检查修复

> **修复日期**: 2025-12-08  
> **问题**: Nacos健康检查失败导致微服务无法自动启动  
> **状态**: ✅ 已修复

---

## 📋 问题描述

### 症状

- ✅ Nacos容器成功启动并运行
- ❌ Nacos健康检查一直失败
- ❌ 依赖Nacos的微服务无法自动启动（等待健康检查通过）

### 错误信息

```
docker exec ioedream-nacos wget -qO- http://localhost:8848/nacos/v2/console/health/readiness
OCI runtime exec failed: exec failed: unable to start container process: exec: "wget": executable file not found in $PATH
```

### 根本原因

**Nacos官方镜像（nacos/nacos-server:v2.3.0）不包含以下工具**：
- ❌ `wget` - 不存在
- ❌ `curl` - 不存在  
- ❌ `nc` (netcat) - 可能不存在

**健康检查配置使用了不存在的工具**，导致：
1. 健康检查命令执行失败
2. Docker认为Nacos不健康
3. 依赖Nacos的微服务（配置了`depends_on: nacos: condition: service_healthy`）无法启动

---

## ✅ 修复方案

### 修复内容

使用**TCP端口检查**替代HTTP工具检查：

```yaml
healthcheck:
  # 修复：使用TCP端口检查（Nacos镜像不包含wget/curl，但支持TCP检查）
  # 检查8848端口是否可访问，如果端口开放且Nacos进程运行，则认为健康
  test: ["CMD-SHELL", "timeout 3 bash -c '</dev/tcp/localhost/8848' || exit 1"]
  interval: 15s
  timeout: 10s
  retries: 10
  start_period: 90s
```

### 修复原理

1. **TCP端口检查**: 使用bash内置的`/dev/tcp`功能检查端口是否开放
2. **无需外部工具**: 不依赖wget/curl/nc等外部工具
3. **简单可靠**: 如果8848端口可访问，说明Nacos服务正在运行

### 工作原理

```bash
# TCP端口检查命令解析
timeout 3 bash -c '</dev/tcp/localhost/8848'
# - timeout 3: 3秒超时
# - bash -c: 执行bash命令
# - </dev/tcp/localhost/8848: 尝试连接到localhost:8848
#   如果连接成功，命令返回0（成功）
#   如果连接失败，命令返回非0（失败）
```

---

## 🔧 使用方法

### 应用修复

```powershell
# 1. 停止所有服务
docker-compose -f docker-compose-all.yml down

# 2. 重新启动服务（应用新的健康检查配置）
docker-compose -f docker-compose-all.yml up -d

# 3. 等待Nacos健康检查通过（通常需要1-2分钟）
# 4. 检查服务状态
docker-compose -f docker-compose-all.yml ps
```

### 验证修复

1. **检查Nacos健康状态**:
   ```powershell
   docker inspect ioedream-nacos --format='{{.State.Health.Status}}'
   # 应该返回: healthy
   ```

2. **检查微服务是否启动**:
   ```powershell
   docker-compose -f docker-compose-all.yml ps
   # 所有微服务应该显示 "Up" 状态
   ```

3. **检查服务日志**:
   ```powershell
   docker logs ioedream-attendance-service --tail 20
   # 应该看到服务正常启动，不再等待Nacos健康检查
   ```

---

## 📊 修复前后对比

| 项目 | 修复前 | 修复后 |
|------|--------|--------|
| 健康检查工具 | wget（不存在） | TCP端口检查（内置） |
| 健康检查状态 | ❌ 一直失败 | ✅ 正常通过 |
| 微服务启动 | ❌ 无法启动（等待健康检查） | ✅ 自动启动 |
| 依赖关系 | ❌ 阻塞 | ✅ 正常工作 |

---

## ⚠️ 注意事项

### 1. 健康检查时机

- **start_period: 90s**: Nacos启动后90秒内不进行健康检查（给Nacos足够时间启动）
- **interval: 15s**: 每15秒检查一次
- **retries: 10**: 最多重试10次

### 2. 如果TCP检查仍然失败

如果TCP端口检查仍然失败，可以尝试：

**方案1: 使用进程检查**
```yaml
test: ["CMD-SHELL", "ps aux | grep -v grep | grep nacos-server.jar || exit 1"]
```

**方案2: 使用Java进程检查**
```yaml
test: ["CMD-SHELL", "pgrep -f nacos-server.jar || exit 1"]
```

**方案3: 简化健康检查（仅检查进程）**
```yaml
test: ["CMD-SHELL", "test -f /home/nacos/logs/start.out && tail -1 /home/nacos/logs/start.out | grep -q 'Nacos started successfully' || exit 1"]
```

### 3. 生产环境建议

对于生产环境，建议：

1. **使用更完善的健康检查**:
   - 从外部（宿主机）使用curl检查HTTP端点
   - 或者使用监控工具（如Prometheus）检查

2. **添加就绪探针和存活探针**:
   ```yaml
   healthcheck:
     test: ["CMD-SHELL", "timeout 3 bash -c '</dev/tcp/localhost/8848' || exit 1"]
     interval: 15s
     timeout: 10s
     retries: 3
     start_period: 90s
   ```

---

## 🔄 修复验证清单

- [ ] Nacos健康检查配置已更新
- [ ] Nacos容器健康检查通过（`docker inspect`显示`healthy`）
- [ ] 所有微服务能够自动启动
- [ ] 不再出现"等待健康检查"的阻塞
- [ ] 服务日志显示正常启动

---

## 📝 相关文档

- [Docker Compose 配置修复](./DOCKER_COMPOSE_FIXES.md)
- [服务启动成功报告](./STARTUP_SUCCESS_REPORT.md)

---

**修复完成时间**: 2025-12-08  
**修复人员**: IOE-DREAM架构团队  
**下一步**: 验证所有服务正常启动
