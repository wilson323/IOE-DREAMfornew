# Docker Compose 配置修复记录

> **修复日期**: 2025-12-08  
> **修复类型**: 配置语法和资源限制优化  
> **状态**: ✅ 已应用

---

## 📋 修复的问题

### 问题1: Docker Compose 配置语法错误

**错误信息**:
```
services.oa-service.environment.[2]: unexpected type map[string]interface {}
```

**原因**: 环境变量值 `nacos:` 中的冒号可能导致YAML解析器混淆。

**修复方案**: 为环境变量值添加引号
```yaml
# 修复前
- SPRING_CONFIG_IMPORT=nacos:

# 修复后
- SPRING_CONFIG_IMPORT="nacos:"
```

### 问题2: Nacos 容器退出码 137 (OOM Killed)

**错误信息**:
```
✘ Container ioedream-nacos                Error                                                               191.9s
dependency failed to start: container ioedream-nacos exited (137)
```

**原因**: Nacos容器内存不足，被系统OOM killer杀死。退出码137 = 128 + 9 (SIGKILL)。

**修复方案**: 增加Nacos内存配置和Docker内存限制

```yaml
# JVM配置优化（增加内存以避免OOM）
- JVM_XMS=512m      # 从256m增加到512m
- JVM_XMX=1024m    # 从512m增加到1024m
- JVM_XMN=256m     # 从128m增加到256m

# Docker内存限制
mem_limit: 1536m      # 最大内存限制
mem_reservation: 1024m # 内存保留
```

---

## ✅ 修复内容

### 1. 环境变量格式修复

为所有9个微服务的 `SPRING_CONFIG_IMPORT` 环境变量值添加引号：

| 服务 | 修复状态 |
|------|---------|
| gateway-service | ✅ |
| common-service | ✅ |
| device-comm-service | ✅ |
| oa-service | ✅ |
| access-service | ✅ |
| attendance-service | ✅ |
| video-service | ✅ |
| consume-service | ✅ |
| visitor-service | ✅ |

### 2. Nacos 内存配置优化

| 配置项 | 修复前 | 修复后 | 说明 |
|--------|--------|--------|------|
| JVM_XMS | 256m | 512m | 初始堆内存 |
| JVM_XMX | 512m | 1024m | 最大堆内存 |
| JVM_XMN | 128m | 256m | 新生代内存 |
| mem_limit | 未设置 | 1536m | Docker最大内存 |
| mem_reservation | 未设置 | 1024m | Docker保留内存 |

---

## 🔧 使用方法

### 重启服务应用修复

```powershell
# 停止所有服务
docker-compose -f docker-compose-all.yml down

# 清理Nacos数据卷（可选，如果Nacos持续崩溃）
docker volume rm ioedream_nacos_data ioedream_nacos_logs

# 重新启动服务
docker-compose -f docker-compose-all.yml up -d

# 检查服务状态
docker-compose -f docker-compose-all.yml ps

# 监控Nacos启动过程
docker logs -f ioedream-nacos
```

### 验证修复

1. **检查Nacos容器状态**:
   ```powershell
   docker ps | Select-String nacos
   # 应该显示 "Up" 状态，而不是 "Exited"
   ```

2. **检查Nacos内存使用**:
   ```powershell
   docker stats ioedream-nacos --no-stream
   # MEM USAGE 应该小于 1.5GB
   ```

3. **检查Nacos健康状态**:
   ```powershell
   docker exec ioedream-nacos wget -qO- http://localhost:8848/nacos/v2/console/health/readiness
   # 应该返回健康状态JSON
   ```

4. **检查微服务启动**:
   ```powershell
   docker logs ioedream-attendance-service --tail 50
   # 不应该再出现 "No spring.config.import property has been defined" 错误
   ```

---

## 📊 内存配置说明

### Nacos 内存需求

| 环境 | 推荐配置 | 最小配置 |
|------|---------|---------|
| 开发环境 | JVM_XMX=1024m, mem_limit=1536m | JVM_XMX=512m, mem_limit=768m |
| 测试环境 | JVM_XMX=2048m, mem_limit=2560m | JVM_XMX=1024m, mem_limit=1536m |
| 生产环境 | JVM_XMX=4096m, mem_limit=5120m | JVM_XMX=2048m, mem_limit=2560m |

### 内存分配建议

- **JVM_XMS**: 初始堆内存，建议设置为最大堆内存的50%
- **JVM_XMX**: 最大堆内存，根据服务数量和数据量调整
- **JVM_XMN**: 新生代内存，建议设置为最大堆内存的25%
- **mem_limit**: Docker容器最大内存，建议为JVM_XMX的1.5倍
- **mem_reservation**: Docker容器保留内存，建议等于JVM_XMX

---

## ⚠️ 注意事项

### 1. 系统资源要求

确保Docker主机有足够的内存：
- **最小要求**: 4GB RAM
- **推荐配置**: 8GB+ RAM
- **生产环境**: 16GB+ RAM

### 2. 内存监控

定期监控容器内存使用情况：
```powershell
# 查看所有容器内存使用
docker stats --no-stream

# 查看Nacos内存使用
docker stats ioedream-nacos --no-stream
```

### 3. 如果Nacos仍然崩溃

如果增加内存后Nacos仍然崩溃，检查：

1. **系统总内存是否足够**:
   ```powershell
   systeminfo | Select-String "Total Physical Memory"
   ```

2. **其他容器是否占用过多内存**:
   ```powershell
   docker stats --no-stream | Sort-Object -Property "MEM USAGE" -Descending
   ```

3. **Docker Desktop内存限制**:
   - 打开Docker Desktop设置
   - 进入 Resources → Advanced
   - 增加Memory限制（建议至少4GB）

4. **清理未使用的资源**:
   ```powershell
   docker system prune -a --volumes
   ```

---

## 🔄 修复验证清单

- [ ] Docker Compose配置语法验证通过 (`docker-compose config`)
- [ ] Nacos容器能够正常启动（不再退出码137）
- [ ] Nacos健康检查通过
- [ ] 所有微服务能够正常连接到Nacos
- [ ] 不再出现 `No spring.config.import property has been defined` 错误
- [ ] 服务内存使用在合理范围内

---

## 📝 相关文档

- [MyBatis-Plus Spring Boot 3.x 修复](./MYBATIS_PLUS_SPRING_BOOT3_FIX.md)
- [Spring Config Import 环境变量修复](./SPRING_CONFIG_IMPORT_ENV_FIX.md)

---

**修复完成时间**: 2025-12-08  
**修复人员**: IOE-DREAM架构团队  
**下一步**: 验证所有服务正常启动
