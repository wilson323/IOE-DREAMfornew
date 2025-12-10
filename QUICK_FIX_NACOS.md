# 🚀 Nacos启动问题快速修复指南

## ⚡ 一键修复（推荐）

```powershell
# 执行完整修复脚本
.\scripts\fix-nacos-complete.ps1
```

## 📋 手动修复步骤

### 步骤1: 初始化nacos数据库

```powershell
# 检查MySQL是否就绪
docker inspect ioedream-mysql --format='{{.State.Health.Status}}'

# 创建nacos数据库（如果不存在）
docker exec ioedream-mysql mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS nacos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 初始化nacos数据库表结构
Get-Content deployment\mysql\init\nacos-schema.sql -Raw | docker exec -i ioedream-mysql mysql -uroot -proot nacos

# 验证初始化结果
docker exec ioedream-mysql mysql -uroot -proot -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';"
```

### 步骤2: 重启Nacos

```powershell
# 停止并删除Nacos容器
docker stop ioedream-nacos
docker rm ioedream-nacos

# 重新创建Nacos容器
docker-compose -f docker-compose-all.yml up -d nacos
```

### 步骤3: 等待并验证

```powershell
# 等待Nacos启动（约60-120秒）
Start-Sleep -Seconds 60

# 检查Nacos状态
docker inspect ioedream-nacos --format='{{.State.Health.Status}}'

# 查看Nacos日志
docker logs ioedream-nacos --tail 20

# 测试Nacos API
Invoke-WebRequest -Uri "http://localhost:8848/nacos/v1/console/health/readiness" -UseBasicParsing
```

### 步骤4: 启动所有服务

```powershell
# 如果Nacos已就绪，启动所有服务
docker-compose -f docker-compose-all.yml up -d
```

## 🔍 问题诊断

### 检查数据库

```powershell
# 检查nacos数据库是否存在
docker exec ioedream-mysql mysql -uroot -proot -e "SHOW DATABASES LIKE 'nacos';"

# 检查nacos表数量
docker exec ioedream-mysql mysql -uroot -proot -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';"
```

### 查看日志

```powershell
# 查看Nacos日志
docker logs ioedream-nacos --tail 50

# 查看MySQL日志
docker logs ioedream-mysql --tail 20
```

## ✅ 验证清单

- [ ] MySQL容器运行正常
- [ ] nacos数据库已创建
- [ ] nacos数据库表结构已初始化（表数量 > 0）
- [ ] Nacos容器可以启动
- [ ] Nacos健康检查通过
- [ ] Nacos控制台可访问: http://localhost:8848/nacos

## 📚 详细文档

- [完整问题分析](documentation/technical/NACOS_STARTUP_ISSUE_ROOT_CAUSE_ANALYSIS.md)
- [Docker部署全局分析](documentation/technical/DOCKER_DEPLOYMENT_ROOT_CAUSE_ANALYSIS.md)
