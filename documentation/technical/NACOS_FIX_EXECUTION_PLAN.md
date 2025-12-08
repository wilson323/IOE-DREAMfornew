# Nacos启动问题修复执行计划

## 🎯 问题总结

**核心问题**: Nacos容器启动失败，错误信息 `[db-load-error]load jdbc.properties error`

**根本原因**: nacos数据库未初始化，导致Nacos无法加载数据库配置

**影响范围**: 所有微服务（都依赖Nacos作为服务注册中心）

## ⚡ 立即执行修复（3步）

### 步骤1: 初始化nacos数据库

```powershell
# 执行初始化脚本
.\scripts\init-nacos-database.ps1
```

**或者手动执行**:
```powershell
# 创建nacos数据库
docker exec ioedream-mysql mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS nacos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 初始化表结构
Get-Content deployment\mysql\init\nacos-schema.sql -Raw | docker exec -i ioedream-mysql mysql -uroot -proot nacos
```

### 步骤2: 重启Nacos

```powershell
# 停止并删除容器
docker stop ioedream-nacos
docker rm ioedream-nacos

# 重新创建
docker-compose -f docker-compose-all.yml up -d nacos
```

### 步骤3: 验证修复

```powershell
# 等待60秒
Start-Sleep -Seconds 60

# 检查状态
docker inspect ioedream-nacos --format='{{.State.Health.Status}}'

# 如果显示 "healthy"，说明修复成功
```

## 🔄 完整修复流程（一键执行）

```powershell
# 执行完整修复脚本（推荐）
.\scripts\fix-nacos-complete.ps1
```

该脚本会自动：
1. ✅ 诊断问题
2. ✅ 初始化数据库
3. ✅ 重启Nacos
4. ✅ 验证修复结果

## 📋 修复后验证

### 检查清单

```powershell
# 1. 检查nacos数据库
docker exec ioedream-mysql mysql -uroot -proot -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';"
# 应该返回表数量 > 0

# 2. 检查Nacos状态
docker inspect ioedream-nacos --format='{{.State.Health.Status}}'
# 应该返回 "healthy"

# 3. 测试Nacos API
Invoke-WebRequest -Uri "http://localhost:8848/nacos/v1/console/health/readiness" -UseBasicParsing
# 应该返回 200 状态码

# 4. 访问Nacos控制台
# 浏览器打开: http://localhost:8848/nacos
# 账号: nacos / nacos
```

### 启动所有服务

```powershell
# 如果Nacos已就绪，启动所有服务
docker-compose -f docker-compose-all.yml up -d

# 查看所有服务状态
docker-compose -f docker-compose-all.yml ps
```

## 🔍 如果仍然失败

### 查看详细日志

```powershell
# Nacos日志
docker logs ioedream-nacos --tail 100

# MySQL日志
docker logs ioedream-mysql --tail 50
```

### 常见问题排查

1. **数据库连接失败**
   - 检查MySQL容器状态
   - 检查网络连接
   - 验证连接参数

2. **表结构初始化失败**
   - 检查SQL脚本是否存在
   - 检查SQL脚本语法
   - 手动执行SQL脚本

3. **健康检查超时**
   - 增加启动等待时间
   - 检查Nacos启动日志
   - 验证端口是否被占用

## 📚 相关文档

- [快速修复指南](../../QUICK_FIX_NACOS.md)
- [问题根本原因分析](./NACOS_STARTUP_ISSUE_ROOT_CAUSE_ANALYSIS.md)
- [Docker部署全局分析](./DOCKER_DEPLOYMENT_ROOT_CAUSE_ANALYSIS.md)

---

**执行时间**: 2025-01-30  
**修复状态**: ⏳ 等待执行
