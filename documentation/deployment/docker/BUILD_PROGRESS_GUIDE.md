# Docker 构建进度监控指南

## 构建状态说明

### 正常构建输出示例

```
[+] Building 0/2
 - Service common-service   Building                                                                           228.6s
 - Service gateway-service  Building                                                                           228.6s
```

**说明**:
- `Building 0/2` 表示正在构建 2 个服务（并行构建）
- 每个服务显示已构建时间（秒）
- Maven 依赖下载是正常过程

### 常见警告（可忽略）

```
WARNING: opening from cache https://dl-cdn.alpinelinux.org/alpine/v3.22/main: No such file or directory
```

**说明**: Alpine Linux 缓存警告，Docker 会自动从其他源下载，不影响构建。

## 监控构建进度

### 方法1: 查看 Docker 镜像列表

```powershell
docker images | Select-String ioedream
```

**预期输出**:
```
ioedream/gateway-service    latest    <image-id>    X minutes ago    XXX MB
ioedream/common-service      latest    <image-id>    X minutes ago    XXX MB
```

### 方法2: 查看构建日志（实时）

```powershell
# 查看所有服务的构建日志
docker-compose -f docker-compose-all.yml logs -f

# 查看特定服务的构建日志
docker-compose -f docker-compose-all.yml logs -f gateway-service
```

### 方法3: 检查构建进程

```powershell
docker ps -a
```

## 构建时间估算

| 服务类型 | 预计时间 | 说明 |
|---------|---------|------|
| 单个服务 | 5-10 分钟 | 包含 Maven 构建和 Docker 镜像创建 |
| 全部 9 个服务 | 15-30 分钟 | 并行构建可缩短总时间 |

**影响因素**:
- 网络速度（Maven 依赖下载）
- CPU 性能（Maven 编译）
- 磁盘 I/O（Docker 镜像构建）

## 构建完成标志

### 成功标志

```
[+] Building 9/9
 - Service gateway-service    Built                                                                           XXXs
 - Service common-service     Built                                                                           XXXs
 - Service device-comm-service Built                                                                          XXXs
 ...
Successfully built <image-id>
Successfully tagged ioedream/gateway-service:latest
```

### 失败标志

```
ERROR: failed to solve: ...
ERROR: failed to build: ...
```

## 构建完成后验证

### 1. 验证所有镜像已创建

```powershell
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | Select-String ioedream
```

**预期**: 应该看到 9 个服务的镜像

### 2. 启动服务

```powershell
docker-compose -f docker-compose-all.yml up -d
```

### 3. 检查服务状态

```powershell
docker-compose -f docker-compose-all.yml ps
```

### 4. 验证无 dataId 错误

```powershell
docker-compose -f docker-compose-all.yml logs gateway-service | Select-String -Pattern "dataId"
```

**预期**: 应该没有 `dataId must be specified` 错误

## 常见问题

### Q: 构建很慢怎么办？

**A**: 
- 检查网络连接（Maven 依赖下载）
- 确保 Docker 有足够资源（CPU、内存）
- 可以逐个构建服务：`docker-compose -f docker-compose-all.yml build --no-cache gateway-service`

### Q: 构建失败怎么办？

**A**:
1. 查看详细错误日志：`docker-compose -f docker-compose-all.yml build --no-cache gateway-service`
2. 检查 Maven 构建是否成功：`mvn clean install -DskipTests`
3. 检查 Dockerfile 路径是否正确
4. 清理 Docker 缓存：`docker system prune -a`

### Q: 如何只构建特定服务？

**A**:
```powershell
docker-compose -f docker-compose-all.yml build --no-cache <service-name>
```

例如：
```powershell
docker-compose -f docker-compose-all.yml build --no-cache gateway-service
docker-compose -f docker-compose-all.yml build --no-cache common-service
```

## 下一步

构建完成后，参考 [URGENT_REBUILD_REQUIRED.md](./URGENT_REBUILD_REQUIRED.md) 启动和验证服务。
