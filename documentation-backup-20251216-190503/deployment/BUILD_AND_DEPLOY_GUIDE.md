# Docker构建与部署完整指南

**更新时间**: 2025-12-07  
**修复版本**: V5 - 直接替换pom.xml方案  
**状态**: ✅ 所有Dockerfile已修复并验证

---

## 🚀 快速开始

### 步骤1: 验证修复

```powershell
# 验证所有Dockerfile修复
powershell -ExecutionPolicy Bypass -File scripts\final-verify-dockerfiles.ps1
```

### 步骤2: 测试单个服务构建（可选）

```powershell
# 测试gateway-service构建
powershell -ExecutionPolicy Bypass -File scripts\test-single-service-build.ps1 -ServiceName gateway-service
```

### 步骤3: 构建所有服务

```powershell
# 清理之前的构建
docker-compose -f docker-compose-all.yml down

# 重新构建所有镜像（不使用缓存）
docker-compose -f docker-compose-all.yml build --no-cache
```

### 步骤4: 启动所有服务

```powershell
# 启动所有服务
docker-compose -f docker-compose-all.yml up -d

# 查看服务状态
docker-compose -f docker-compose-all.yml ps
```

### 步骤5: 等待服务启动

```powershell
# 等待2-3分钟让服务完全启动
Start-Sleep -Seconds 180

# 查看服务日志
docker-compose -f docker-compose-all.yml logs -f
```

### 步骤6: 验证部署

```powershell
# 运行完整验证脚本
powershell -ExecutionPolicy Bypass -File scripts\verify-deployment-step-by-step.ps1
```

---

## 📋 服务列表

| 服务名称 | 端口 | 健康检查端点 |
|---------|------|-------------|
| ioedream-gateway-service | 8080 | http://localhost:8080/actuator/health |
| ioedream-common-service | 8088 | http://localhost:8088/actuator/health |
| ioedream-device-comm-service | 8087 | http://localhost:8087/actuator/health |
| ioedream-oa-service | 8089 | http://localhost:8089/actuator/health |
| ioedream-access-service | 8090 | http://localhost:8090/actuator/health |
| ioedream-attendance-service | 8091 | http://localhost:8091/actuator/health |
| ioedream-video-service | 8092 | http://localhost:8092/actuator/health |
| ioedream-consume-service | 8094 | http://localhost:8094/actuator/health |
| ioedream-visitor-service | 8095 | http://localhost:8095/actuator/health |
| Nacos | 8848 | http://localhost:8848/nacos |
| MySQL | 3306 | - |
| Redis | 6379 | - |

---

## 🔍 故障排查

### 构建失败

**问题**: `Child module ... does not exist`
- **原因**: Dockerfile未使用V5方案
- **解决**: 检查Dockerfile是否直接替换pom.xml

**问题**: `python3: not found`
- **原因**: Dockerfile仍使用Python脚本
- **解决**: 检查Dockerfile是否使用awk命令

### 服务启动失败

**问题**: 服务无法连接到Nacos
- **检查**: Nacos服务是否正常运行
- **命令**: `docker-compose -f docker-compose-all.yml ps nacos`

**问题**: 服务无法连接到MySQL
- **检查**: MySQL服务是否正常运行
- **命令**: `docker-compose -f docker-compose-all.yml ps mysql`

### 查看日志

```powershell
# 查看所有服务日志
docker-compose -f docker-compose-all.yml logs -f

# 查看特定服务日志
docker-compose -f docker-compose-all.yml logs -f gateway-service

# 查看最近100行日志
docker-compose -f docker-compose-all.yml logs --tail=100 gateway-service
```

---

## 📞 相关文档

- **修复方案V5**: `documentation/deployment/DOCKER_BUILD_FIX_V5_FINAL.md`
- **验证报告**: `documentation/deployment/DOCKER_BUILD_FIX_V5_VERIFIED.md`
- **完整解决方案**: `documentation/deployment/DOCKER_BUILD_FIX_COMPLETE.md`
- **执行状态**: `documentation/project/EXECUTION_STATUS.md`

---

**最后更新**: 2025-12-07
