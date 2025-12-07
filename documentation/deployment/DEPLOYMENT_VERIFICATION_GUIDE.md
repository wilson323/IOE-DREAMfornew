# IOE-DREAM 部署验证指南

## 📋 验证清单

### ✅ 已完成
- [x] 所有11个微服务模块本地Maven编译成功

### ⏳ 进行中
- [ ] Docker Compose部署（等待镜像构建完成）

### ❌ 待验证
- [ ] 所有服务是否正常启动和运行
- [ ] Nacos服务注册中心是否正常工作
- [ ] 微服务间通信是否正常
- [ ] 前端应用是否能正常访问后端服务

---

## 🔍 验证步骤

### 1. 检查Docker容器状态

```powershell
# 查看所有IOE-DREAM容器状态
docker ps -a --filter "name=ioedream" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 或者使用Docker Compose查看
cd D:\IOE-DREAM
docker-compose -f docker-compose-all.yml ps
```

**预期结果**: 应该看到以下容器都在运行（Status为"Up"）:
- ioedream-mysql
- ioedream-redis
- ioedream-nacos
- ioedream-gateway-service
- ioedream-common-service
- ioedream-device-comm-service
- ioedream-oa-service
- ioedream-access-service
- ioedream-attendance-service
- ioedream-video-service
- ioedream-consume-service
- ioedream-visitor-service

### 2. 检查服务端口

```powershell
# 检查所有服务端口是否开放
$ports = @(3306, 6379, 8848, 8080, 8087, 8088, 8089, 8090, 8091, 8092, 8094, 8095)
foreach ($port in $ports) {
    $result = Test-NetConnection -ComputerName localhost -Port $port -InformationLevel Quiet -WarningAction SilentlyContinue
    if ($result) {
        Write-Host "✅ 端口 $port 已开放" -ForegroundColor Green
    } else {
        Write-Host "❌ 端口 $port 未开放" -ForegroundColor Red
    }
}
```

**预期结果**: 所有端口都应该返回 `True`

### 3. 检查服务健康状态

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
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ $($service.Name) 服务健康" -ForegroundColor Green
        }
    } catch {
        Write-Host "❌ $($service.Name) 服务不可用: $_" -ForegroundColor Red
    }
}
```

**预期结果**: 所有服务都应该返回HTTP 200状态码

### 4. 验证Nacos注册中心

#### 4.1 访问Nacos控制台
打开浏览器访问: http://localhost:8848/nacos
- 用户名: `nacos`
- 密码: `nacos`

#### 4.2 检查服务注册情况
在Nacos控制台的"服务管理" -> "服务列表"中，应该看到以下服务已注册:
- ioedream-gateway-service
- ioedream-common-service
- ioedream-device-comm-service
- ioedream-oa-service
- ioedream-access-service
- ioedream-attendance-service
- ioedream-video-service
- ioedream-consume-service
- ioedream-visitor-service

#### 4.3 使用API检查服务注册

```powershell
# 获取Nacos服务列表
$nacosUrl = "http://localhost:8848/nacos/v1/ns/service/list"
$response = Invoke-RestMethod -Uri $nacosUrl -Method Get
$response.doms | ForEach-Object { Write-Host $_ }
```

**预期结果**: 应该看到9个微服务都已注册

### 5. 验证服务间通信

#### 5.1 通过网关访问服务

```powershell
# 测试网关健康检查
$gatewayHealth = "http://localhost:8080/actuator/health"
Invoke-WebRequest -Uri $gatewayHealth -UseBasicParsing

# 测试通过网关访问公共服务
$commonService = "http://localhost:8080/api/v1/common/health"
Invoke-WebRequest -Uri $commonService -UseBasicParsing
```

#### 5.2 测试服务间调用

```powershell
# 测试门禁服务调用公共服务
# 这需要根据实际API路径调整
$testUrl = "http://localhost:8080/api/v1/access/test"
Invoke-WebRequest -Uri $testUrl -UseBasicParsing
```

**预期结果**: 网关应该能够正确路由请求到对应的微服务

### 6. 验证前端应用

#### 6.1 检查前端是否运行

```powershell
# 检查前端端口
$frontendPorts = @(3000, 8081)
foreach ($port in $frontendPorts) {
    $result = Test-NetConnection -ComputerName localhost -Port $port -InformationLevel Quiet -WarningAction SilentlyContinue
    if ($result) {
        Write-Host "✅ 前端端口 $port 已开放" -ForegroundColor Green
        Write-Host "   访问地址: http://localhost:$port" -ForegroundColor Gray
    }
}
```

#### 6.2 启动前端应用（如果未运行）

```powershell
# 进入前端目录
cd D:\IOE-DREAM\smart-admin-web-javascript

# 安装依赖（如果未安装）
npm install

# 启动开发服务器
npm run localhost
# 或
npm run dev
```

#### 6.3 验证前端访问后端

打开浏览器访问前端应用，检查:
- [ ] 登录功能是否正常
- [ ] API调用是否成功
- [ ] 数据是否正常加载

---

## 🚀 快速验证脚本

使用项目提供的验证脚本进行一键验证:

```powershell
# 完整验证
cd D:\IOE-DREAM
powershell -ExecutionPolicy Bypass -File scripts\verify-deployment-step-by-step.ps1

# 或者使用简化版本
powershell -ExecutionPolicy Bypass -File scripts\verify-deployment-complete.ps1
```

---

## 🔧 常见问题排查

### 问题1: 容器启动失败

**检查日志**:
```powershell
docker-compose -f docker-compose-all.yml logs [服务名]
```

**常见原因**:
- 端口被占用
- 数据库连接失败
- 配置错误

**解决方案**:
```powershell
# 查看具体错误
docker-compose -f docker-compose-all.yml logs --tail=100 [服务名]

# 重启服务
docker-compose -f docker-compose-all.yml restart [服务名]
```

### 问题2: 服务无法注册到Nacos

**检查**:
1. Nacos是否正常运行
2. 服务配置中的Nacos地址是否正确
3. 网络连接是否正常

**解决方案**:
```powershell
# 检查Nacos日志
docker logs ioedream-nacos

# 检查服务配置
docker exec ioedream-common-service env | Select-String "NACOS"
```

### 问题3: 服务间通信失败

**检查**:
1. 网关是否正常运行
2. 服务是否已注册到Nacos
3. 路由配置是否正确

**解决方案**:
```powershell
# 检查网关路由配置
docker exec ioedream-gateway-service cat /app/application.yml

# 检查服务注册情况
curl http://localhost:8848/nacos/v1/ns/service/list
```

### 问题4: 前端无法访问后端

**检查**:
1. 前端代理配置是否正确
2. 后端服务是否正常运行
3. CORS配置是否正确

**解决方案**:
- 检查 `smart-admin-web-javascript/vite.config.js` 中的代理配置
- 确认后端服务地址和端口正确

---

## 📊 验证报告模板

完成验证后，填写以下报告:

```
验证日期: ___________
验证人员: ___________

[ ] Docker容器状态: ✅/❌
[ ] 服务端口检查: ✅/❌
[ ] 服务健康检查: ✅/❌
[ ] Nacos注册中心: ✅/❌
[ ] 服务间通信: ✅/❌
[ ] 前端应用访问: ✅/❌

问题记录:
1. 
2. 
3. 

验证结论: 通过/未通过
```

---

## 📞 获取帮助

如果遇到问题，请:
1. 查看服务日志: `docker-compose -f docker-compose-all.yml logs [服务名]`
2. 检查配置文件: `docker-compose -f docker-compose-all.yml config`
3. 参考项目文档: `documentation/deployment/`
4. 联系技术支持团队
