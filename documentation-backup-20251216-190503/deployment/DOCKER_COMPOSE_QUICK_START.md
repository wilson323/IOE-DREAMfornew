# Docker Compose 快速启动和验证指南

## ✅ 已修复的问题

- [x] 修复了 `docker-compose-all.yml` 中的 `version` 警告（Docker Compose v2+ 不再需要此字段）

## 🚀 启动服务

### 方法1: 启动所有服务（推荐）

```powershell
cd D:\IOE-DREAM
docker-compose -f docker-compose-all.yml up -d
```

### 方法2: 分步启动

```powershell
# 1. 先启动基础设施服务
docker-compose -f docker-compose-all.yml up -d mysql redis nacos

# 2. 等待基础设施服务就绪（约30-60秒）
Start-Sleep -Seconds 60

# 3. 启动所有微服务
docker-compose -f docker-compose-all.yml up -d
```

## 📊 检查服务状态

### 查看所有服务状态

```powershell
docker-compose -f docker-compose-all.yml ps
```

**预期输出示例**:
```
NAME                          STATUS              PORTS
ioedream-mysql                Up 2 minutes        0.0.0.0:3306->3306/tcp
ioedream-redis                Up 2 minutes        0.0.0.0:6379->6379/tcp
ioedream-nacos                Up 2 minutes        0.0.0.0:8848->8848/tcp, 0.0.0.0:9848->9848/tcp
ioedream-gateway-service      Up 1 minute         0.0.0.0:8080->8080/tcp
ioedream-common-service        Up 1 minute         0.0.0.0:8088->8088/tcp
...
```

### 查看服务日志

```powershell
# 查看所有服务日志
docker-compose -f docker-compose-all.yml logs -f

# 查看特定服务日志
docker-compose -f docker-compose-all.yml logs -f gateway-service
docker-compose -f docker-compose-all.yml logs -f common-service
docker-compose -f docker-compose-all.yml logs -f nacos
```

### 查看最近100行日志

```powershell
docker-compose -f docker-compose-all.yml logs --tail=100 [服务名]
```

## 🔍 验证服务

### 1. 快速验证脚本

```powershell
# 使用项目提供的验证脚本
powershell -ExecutionPolicy Bypass -File scripts\verify-deployment-step-by-step.ps1
```

### 2. 手动验证

#### 检查容器是否运行

```powershell
docker ps --filter "name=ioedream" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

#### 检查服务健康状态

```powershell
# 检查各个服务的健康端点
$services = @(
    @{ Name = "Gateway"; Port = 8080 }
    @{ Name = "Common"; Port = 8088 }
    @{ Name = "DeviceComm"; Port = 8087 }
    @{ Name = "OA"; Port = 8089 }
    @{ Name = "Access"; Port = 8090 }
    @{ Name = "Attendance"; Port = 8091 }
    @{ Name = "Video"; Port = 8092 }
    @{ Name = "Consume"; Port = 8094 }
    @{ Name = "Visitor"; Port = 8095 }
)

foreach ($service in $services) {
    try {
        $url = "http://localhost:$($service.Port)/actuator/health"
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -UseBasicParsing
        Write-Host "✅ $($service.Name) 服务正常 (HTTP $($response.StatusCode))" -ForegroundColor Green
    } catch {
        Write-Host "❌ $($service.Name) 服务异常: $_" -ForegroundColor Red
    }
}
```

#### 检查Nacos注册中心

1. **访问控制台**: http://localhost:8848/nacos
   - 用户名: `nacos`
   - 密码: `nacos`

2. **检查服务注册**:
   - 导航到: "服务管理" -> "服务列表"
   - 应该看到9个微服务已注册

3. **使用API检查**:
```powershell
$nacosUrl = "http://localhost:8848/nacos/v1/ns/service/list"
$response = Invoke-RestMethod -Uri $nacosUrl -Method Get
$response.doms | ForEach-Object { Write-Host "  - $_" }
```

## 🛠️ 常见问题

### 问题1: 服务启动失败

**检查日志**:
```powershell
docker-compose -f docker-compose-all.yml logs [服务名]
```

**常见原因**:
- 端口被占用
- 数据库连接失败
- 配置错误
- 依赖服务未就绪

**解决方案**:
```powershell
# 1. 检查端口占用
netstat -ano | findstr :8080

# 2. 重启服务
docker-compose -f docker-compose-all.yml restart [服务名]

# 3. 重新构建并启动
docker-compose -f docker-compose-all.yml up -d --build [服务名]
```

### 问题2: 服务无法连接到Nacos

**检查**:
1. Nacos是否正常运行
2. 服务配置中的Nacos地址是否正确
3. 网络连接是否正常

**解决方案**:
```powershell
# 检查Nacos日志
docker-compose -f docker-compose-all.yml logs nacos

# 检查服务环境变量
docker exec ioedream-common-service env | Select-String "NACOS"
```

### 问题3: 数据库连接失败

**检查**:
```powershell
# 检查MySQL是否运行
docker-compose -f docker-compose-all.yml ps mysql

# 检查MySQL日志
docker-compose -f docker-compose-all.yml logs mysql

# 测试MySQL连接
docker exec -it ioedream-mysql mysql -uroot -proot -e "SHOW DATABASES;"
```

### 问题4: 镜像构建失败

**解决方案**:
```powershell
# 清理并重新构建
docker-compose -f docker-compose-all.yml build --no-cache

# 或者只构建特定服务
docker-compose -f docker-compose-all.yml build --no-cache gateway-service
```

## 📋 服务启动顺序

服务启动有依赖关系，Docker Compose会自动处理，但了解顺序有助于排查问题:

1. **基础设施层** (必须首先启动):
   - MySQL (3306)
   - Redis (6379)
   - Nacos (8848) - 依赖MySQL

2. **公共服务层**:
   - Common Service (8088) - 依赖Nacos, Redis, MySQL

3. **业务服务层** (依赖Common Service):
   - Gateway Service (8080)
   - Device Comm Service (8087)
   - OA Service (8089)
   - Access Service (8090)
   - Attendance Service (8091)
   - Video Service (8092)
   - Consume Service (8094)
   - Visitor Service (8095)

## 🎯 验证清单

完成部署后，请验证以下项目:

- [ ] 所有12个容器都在运行 (`docker-compose ps`)
- [ ] 所有服务端口都已开放
- [ ] 所有服务健康检查通过 (`/actuator/health`)
- [ ] Nacos控制台可访问，9个微服务已注册
- [ ] 通过网关可以访问各个服务
- [ ] 前端应用可以正常访问后端API

## 📞 获取帮助

如果遇到问题:

1. **查看服务日志**: `docker-compose -f docker-compose-all.yml logs [服务名]`
2. **检查服务状态**: `docker-compose -f docker-compose-all.yml ps`
3. **查看验证指南**: `documentation/deployment/DEPLOYMENT_VERIFICATION_GUIDE.md`
4. **运行验证脚本**: `scripts\verify-deployment-step-by-step.ps1`
