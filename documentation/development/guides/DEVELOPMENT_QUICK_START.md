# 🚀 IOE-DREAM 开发环境快速启动指南

> **一键启动开发环境** | 更新时间: 2025-01-30

---

## ⚡ 最快启动方式（推荐）

### 方式一：使用一键启动脚本（最推荐）

```powershell
# 进入项目目录
cd D:\IOE-DREAM

# 从模板创建 .env（注意：.env 不应提交到仓库）
Copy-Item ".env.template" ".env" -Force

# 编辑 .env，至少配置：
# MYSQL_ROOT_PASSWORD、REDIS_PASSWORD、NACOS_USERNAME、NACOS_PASSWORD、NACOS_AUTH_TOKEN、JWT_SECRET
notepad .env

# 一键启动所有服务（自动检测并启动MySQL/Redis/Nacos）
.\scripts\start-all-complete.ps1
```

**脚本会自动完成**:
- ✅ 检测Docker是否可用
- ✅ 如果MySQL未运行，自动通过Docker启动
- ✅ 如果Redis未运行，自动通过Docker启动  
- ✅ 如果Nacos未运行，自动通过Docker启动
- ✅ 自动初始化nacos数据库（如果未初始化）
- ✅ 自动构建公共模块
- ✅ 按顺序启动所有后端微服务
- ✅ 启动前端管理后台
- ✅ 启动移动端应用

### 方式二：使用Docker Compose（已部署Docker环境）

如果已经使用Docker部署了基础设施服务：

```powershell
# 1. 启动基础设施服务（MySQL、Redis、Nacos）
docker-compose -f docker-compose-all.yml up -d mysql redis nacos

# 2. 等待服务就绪（约30秒）
Start-Sleep -Seconds 30

# 3. 初始化nacos数据库（如果需要）
.\scripts\init-nacos-database.ps1

# 4. 启动后端微服务
.\scripts\start-all-complete.ps1 -BackendOnly

# 5. 启动前端（新窗口）
.\scripts\start-all-complete.ps1 -FrontendOnly

# 6. 启动移动端（新窗口，可选）
.\scripts\start-all-complete.ps1 -MobileOnly
```

---

## 📋 启动选项

### 完整启动（后端 + 前端 + 移动端）

```powershell
.\scripts\start-all-complete.ps1
```

### 仅启动后端微服务

```powershell
.\scripts\start-all-complete.ps1 -BackendOnly
```

### 仅启动前端管理后台

```powershell
.\scripts\start-all-complete.ps1 -FrontendOnly
```

### 仅启动移动端应用

```powershell
.\scripts\start-all-complete.ps1 -MobileOnly
```

### 检查服务状态

```powershell
.\scripts\start-all-complete.ps1 -CheckOnly
```

---

## 🔧 环境准备

### 必需软件

| 软件 | 版本 | 检查命令 |
|------|------|---------|
| **JDK** | 17+ | `java -version` |
| **Maven** | 3.8+ | `mvn -version` |
| **Node.js** | 18+ | `node -v` |
| **Docker Desktop** | 20.10+ | `docker --version` |

### 可选软件

如果没有Docker，需要手动安装：
- MySQL 8.0+ (端口3306)
- Redis 6.0+ (端口6379)
- Nacos 2.2.0+ (端口8848)

---

## 📍 服务端口和访问地址

### 后端微服务

| 服务名称 | 端口 | 访问地址 |
|---------|------|---------|
| API网关 | 8080 | http://localhost:8080 |
| 公共服务 | 8088 | http://localhost:8088 |
| 设备通讯 | 8087 | http://localhost:8087 |
| OA服务 | 8089 | http://localhost:8089 |
| 门禁服务 | 8090 | http://localhost:8090 |
| 考勤服务 | 8091 | http://localhost:8091 |
| 视频服务 | 8092 | http://localhost:8092 |
| 消费服务 | 8094 | http://localhost:8094 |
| 访客服务 | 8095 | http://localhost:8095 |

### 前端应用

| 应用 | 端口 | 访问地址 |
|------|------|---------|
| 前端管理后台 | 3000 | http://localhost:3000 |
| 移动端应用(H5) | 8081 | http://localhost:8081 |

### 基础设施服务

| 服务 | 端口 | 访问地址 | 账号/密码 |
|------|------|---------|---------|
| MySQL | 3306 | - | `root/${MYSQL_ROOT_PASSWORD}` |
| Redis | 6379 | - | `${REDIS_PASSWORD}` |
| Nacos控制台 | 8848 | http://localhost:8848/nacos | `${NACOS_USERNAME}/${NACOS_PASSWORD}` |

---

## 🎯 启动流程说明

### 1. 基础设施服务启动

脚本会自动检查并启动：
- **MySQL**: 如果未运行，自动通过Docker启动
- **Redis**: 如果未运行，自动通过Docker启动
- **Nacos**: 如果未运行，自动通过Docker启动
- **数据库初始化**: 自动检查并初始化nacos数据库

### 2. 后端服务启动顺序

```
第1批: ioedream-gateway-service (8080)
  ↓ 等待15秒
第2批: ioedream-common-service (8088)
       ioedream-device-comm-service (8087)
  ↓ 等待15秒
第3批: ioedream-oa-service (8089)
       ioedream-access-service (8090)
       ioedream-attendance-service (8091)
       ioedream-video-service (8092)
       ioedream-consume-service (8094)
       ioedream-visitor-service (8095)
  ↓ 等待45秒
完成
```

### 3. 前端应用启动

- 自动检查依赖是否安装
- 如果未安装，自动执行 `npm install`
- 在新窗口启动开发服务器

---

## ✅ 验证启动

### 检查所有服务状态

```powershell
.\scripts\start-all-complete.ps1 -CheckOnly
```

### 手动检查关键服务

```powershell
# 检查API网关
Test-NetConnection -ComputerName localhost -Port 8080

# 检查前端
Test-NetConnection -ComputerName localhost -Port 3000

# 检查Nacos
Test-NetConnection -ComputerName localhost -Port 8848
```

### 访问应用

- **前端管理后台**: http://localhost:3000
- **移动端应用**: http://localhost:8081
- **API网关**: http://localhost:8080
- **Nacos控制台**: http://localhost:8848/nacos（账号密码以 `.env` 中的 `NACOS_USERNAME/NACOS_PASSWORD` 为准）

---

## 🔐 API 基线与兼容窗口（30 天）

- **Canonical API 前缀**：统一使用 `/api/v1`
- **鉴权方式**：Spring Security（JWT Bearer），请求头 `Authorization: Bearer <token>`
- **兼容窗口**：legacy 路由与 legacy 登录路径保留 30 天后下线（建议尽快迁移）
  - legacy 登录前缀（兼容）：`/login/**`
  - legacy 业务前缀（兼容）：`/access/**`、`/attendance/**`、`/consume/**`、`/visitor/**`、`/video/**`、`/device/**`

---

## 🐛 常见问题

### 1. MySQL未运行

**解决方案**:
```powershell
# 脚本会自动启动，如果失败，手动执行：
docker-compose -f docker-compose-all.yml up -d mysql

# 等待MySQL就绪
Start-Sleep -Seconds 30
```

### 2. Nacos启动失败

**解决方案**:
```powershell
# 1. 确保MySQL已启动
docker ps --filter "name=ioedream-mysql"

# 2. 初始化nacos数据库
.\scripts\init-nacos-database.ps1

# 3. 重启Nacos
docker restart ioedream-nacos
```

### 3. 端口被占用

**解决方案**:
```powershell
# 查找占用端口的进程
netstat -ano | findstr :8080

# 结束进程（替换PID为实际进程ID）
taskkill /PID <PID> /F
```

### 4. 服务启动失败

**检查步骤**:
1. 检查依赖服务是否运行（MySQL、Redis、Nacos）
2. 查看服务窗口的错误日志
3. 检查配置文件是否正确
4. 确认端口未被占用

### 5. 前端无法连接后端

**检查步骤**:
1. 确认后端服务已启动
2. 检查前端 `.env.localhost` 配置
3. 确认API地址配置正确：`VITE_APP_API_URL=http://localhost:8080`

---

## 💡 开发建议

### 日常开发流程

```powershell
# 1. 启动基础设施（每天一次）
docker-compose -f docker-compose-all.yml up -d mysql redis nacos

# 2. 启动后端（按需）
.\scripts\start-all-complete.ps1 -BackendOnly

# 3. 启动前端（按需）
.\scripts\start-all-complete.ps1 -FrontendOnly
```

### 快速重启服务

```powershell
# 检查服务状态
.\scripts\start-all-complete.ps1 -CheckOnly

# 重启特定服务（关闭对应窗口，重新启动）
.\scripts\start-all-complete.ps1 -BackendOnly
```

### 使用IDE启动（更快）

如果使用IntelliJ IDEA或Eclipse：
1. 在IDE中直接运行Spring Boot应用
2. 比命令行启动更快
3. 支持断点调试

---

## 📚 相关文档

- [开发环境启动指南](documentation/technical/DEVELOPMENT_STARTUP_GUIDE.md) - 详细启动步骤
- [启动脚本使用说明](scripts/README_START.md) - 脚本详细说明
- [快速启动指南](QUICK_START.md) - 3步启动
- [项目README](README.md) - 项目完整文档

---

## 🎉 启动成功标志

当看到以下输出时，表示启动成功：

```
========================================
  所有服务启动完成！
========================================

访问地址:
  前端管理后台: http://localhost:3000
  移动端应用:   http://localhost:8081
  API网关:      http://localhost:8080
```

**提示**: 首次启动可能需要5-10分钟（下载依赖、构建项目）
