# 🔧 Docker构建故障排查指南

**最后更新**: 2025-12-07

---

## ❌ 常见错误

### 错误1: `Child module ... does not exist`

**错误信息**:
```
[ERROR] Child module /build/microservices/ioedream-xxx-service of /build/microservices/pom.xml does not exist
```

**原因**:
- Docker使用了缓存的旧版本Dockerfile（V4方案）
- Dockerfile中仍使用`pom-temp.xml`而不是直接替换`pom.xml`

**解决方案**:

1. **验证Dockerfile修复**:
   ```powershell
   # 检查Dockerfile是否使用V5方案
   Get-Content microservices\ioedream-gateway-service\Dockerfile | Select-String -Pattern "pom-original.xml"
   ```

2. **清理Docker缓存并强制重建**:
   ```powershell
   # 使用强制重建脚本
   powershell -ExecutionPolicy Bypass -File scripts\force-rebuild-all.ps1
   ```

3. **手动清理并重建**:
   ```powershell
   # 停止并删除所有容器
   docker-compose -f docker-compose-all.yml down -v
   
   # 清理构建缓存
   docker builder prune -af
   
   # 强制重新构建（不使用缓存）
   docker-compose -f docker-compose-all.yml build --no-cache --pull
   ```

---

### 错误2: `python3: not found`

**错误信息**:
```
/bin/sh: 1: python3: not found
```

**原因**:
- Dockerfile中使用了Python脚本（V3方案）
- Maven镜像中没有Python3

**解决方案**:
- 确保所有Dockerfile使用V5方案（awk命令，不使用Python）

---

### 错误3: PowerShell脚本语法错误

**错误信息**:
```
Missing closing '}' in statement block or type definition
Unexpected token '}' in expression or statement
```

**原因**:
- 字符串中的引号未正确转义
- 使用了错误的引号类型

**解决方案**:
- 使用单引号或正确转义的双引号
- 检查脚本中的字符串拼接

---

## ✅ 验证步骤

### 步骤1: 验证Dockerfile修复

```powershell
# 检查所有Dockerfile是否使用V5方案
$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-consume-service",
    "ioedream-visitor-service"
)

foreach ($service in $services) {
    $dockerfilePath = "microservices\$service\Dockerfile"
    $content = Get-Content $dockerfilePath -Raw
    
    if ($content -match "cp pom\.xml pom-original\.xml" -and 
        $content -match "awk.*pom-original\.xml > pom\.xml" -and
        $content -match "mvn install:install-file -Dfile=pom\.xml") {
        Write-Host "✅ $service - V5方案正确" -ForegroundColor Green
    } else {
        Write-Host "❌ $service - 需要修复" -ForegroundColor Red
    }
}
```

### 步骤2: 清理Docker缓存

```powershell
# 停止所有容器
docker-compose -f docker-compose-all.yml down -v

# 清理构建缓存
docker builder prune -af

# 清理未使用的镜像
docker image prune -af
```

### 步骤3: 强制重建

```powershell
# 使用--no-cache和--pull强制重新构建
docker-compose -f docker-compose-all.yml build --no-cache --pull
```

---

## 🔍 调试技巧

### 查看详细构建日志

```powershell
# 查看单个服务的详细构建日志
docker-compose -f docker-compose-all.yml build gateway-service --progress=plain 2>&1 | Select-String -Pattern "ERROR"

# 查看所有服务的构建日志
docker-compose -f docker-compose-all.yml build --progress=plain 2>&1 | tee build.log
```

### 检查Dockerfile内容

```powershell
# 查看特定服务的Dockerfile
Get-Content microservices\ioedream-gateway-service\Dockerfile

# 检查关键行
Get-Content microservices\ioedream-gateway-service\Dockerfile | Select-String -Pattern "pom.xml"
```

### 测试单个服务构建

```powershell
# 测试单个服务构建
powershell -ExecutionPolicy Bypass -File scripts\test-single-service-build.ps1 -ServiceName gateway-service
```

---

## 📞 相关文档

- **修复方案V5**: `documentation/deployment/DOCKER_BUILD_FIX_V5_FINAL.md`
- **最终执行指令**: `documentation/deployment/FINAL_BUILD_INSTRUCTIONS.md`
- **执行状态**: `documentation/project/EXECUTION_STATUS.md`

---

**如果问题仍然存在，请检查**:
1. Dockerfile是否真的使用了V5方案
2. Docker是否使用了缓存的旧版本
3. 是否清理了所有Docker缓存
