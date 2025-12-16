# 🚀 IOE-DREAM 快速启动指南

> **一键启动开发环境** | 更新时间: 2025-01-30

---

## ⚡ 最快启动方式（3步）

### 1️⃣ 配置环境变量（必须）

`docker-compose-all.yml` 已移除默认口令占位，必须通过 `.env` 注入敏感配置。

```powershell
# 进入项目目录
cd D:\IOE-DREAM

# 从模板创建 .env（注意：.env 不应提交到仓库）
Copy-Item ".env.template" ".env" -Force

# 编辑 .env，至少配置：
# MYSQL_ROOT_PASSWORD、REDIS_PASSWORD、NACOS_USERNAME、NACOS_PASSWORD、NACOS_AUTH_TOKEN、JWT_SECRET
notepad .env
```

### 2️⃣ 确保Docker运行

```powershell
# 检查Docker
docker --version

# 如果未安装，请先安装Docker Desktop
# 下载地址: https://www.docker.com/products/docker-desktop
```

### 3️⃣ 一键启动所有服务

```powershell
# 进入项目目录
cd D:\IOE-DREAM

# 一键启动（自动检测并启动MySQL/Redis/Nacos）
.\scripts\start-all-complete.ps1
```

### 4️⃣ 访问应用

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

## 📋 启动脚本功能

### ✅ 自动功能

- ✅ **自动检测Docker** - 如果Docker可用，自动启动基础设施服务
- ✅ **自动启动MySQL** - 如果MySQL未运行，自动通过Docker启动
- ✅ **自动启动Redis** - 如果Redis未运行，自动通过Docker启动
- ✅ **自动启动Nacos** - 如果Nacos未运行，自动通过Docker启动
- ✅ **自动构建公共模块** - 自动构建 `microservices-common`
- ✅ **自动安装依赖** - 前端/移动端依赖未安装时自动执行 `npm install`
- ✅ **按顺序启动服务** - 自动按依赖顺序启动所有微服务

### 🎯 启动选项

```powershell
# 启动所有服务（后端 + 前端 + 移动端）
.\scripts\start-all-complete.ps1

# 仅启动后端微服务
.\scripts\start-all-complete.ps1 -BackendOnly

# 仅启动前端管理后台
.\scripts\start-all-complete.ps1 -FrontendOnly

# 仅启动移动端应用
.\scripts\start-all-complete.ps1 -MobileOnly

# 仅检查服务状态（不启动）
.\scripts\start-all-complete.ps1 -CheckOnly
```

---

## 🔧 环境要求

| 软件 | 版本 | 必需 |
|------|------|------|
| JDK | 17+ | ✅ |
| Maven | 3.8+ | ✅ |
| Node.js | 18+ | ✅ |
| Docker Desktop | 20.10+ | ⚠️ 推荐（自动启动MySQL/Redis/Nacos） |

**如果没有Docker**:
- 需要手动安装并启动 MySQL、Redis、Nacos
- 或使用本地安装的服务

---

## 📍 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端管理后台 | 3000 | Vue3应用 |
| 移动端应用 | 8081 | uni-app H5 |
| API网关 | 8080 | Spring Cloud Gateway |
| 公共服务 | 8088 | ioedream-common-service |
| 设备通讯 | 8087 | ioedream-device-comm-service |
| OA服务 | 8089 | ioedream-oa-service |
| 门禁服务 | 8090 | ioedream-access-service |
| 考勤服务 | 8091 | ioedream-attendance-service |
| 视频服务 | 8092 | ioedream-video-service |
| 消费服务 | 8094 | ioedream-consume-service |
| 访客服务 | 8095 | ioedream-visitor-service |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nacos | 8848 | 服务注册中心 |

---

## 🐛 常见问题快速解决

### MySQL未运行？

```powershell
# 脚本会自动启动，如果失败，手动执行：
docker-compose -f docker-compose-all.yml up -d mysql
```

### 端口被占用？

```powershell
# 查找占用进程
netstat -ano | findstr :8080

# 结束进程
taskkill /PID <PID> /F
```

### 服务启动失败？

```powershell
# 检查服务状态
.\scripts\start-all-complete.ps1 -CheckOnly

# 查看服务日志（每个服务在独立窗口）
```

---

## 📚 详细文档

- 📖 [开发环境启动指南](documentation/technical/DEVELOPMENT_STARTUP_GUIDE.md) - **完整启动步骤**
- 📖 [启动脚本使用说明](scripts/README_START.md) - **脚本详细说明**
- 📖 [项目README](README.md) - **项目完整文档**

---

## 💡 开发建议

### 日常开发

```powershell
# 1. 启动基础设施（每天一次）
docker-compose -f docker-compose-all.yml up -d mysql redis nacos

# 2. 启动后端（按需）
.\scripts\start-all-complete.ps1 -BackendOnly

# 3. 启动前端（按需）
.\scripts\start-all-complete.ps1 -FrontendOnly
```

### 快速重启

```powershell
# 检查服务状态
.\scripts\start-all-complete.ps1 -CheckOnly

# 重启特定服务（关闭对应窗口，重新启动）
```

---

**提示**: 首次启动可能需要5-10分钟（下载依赖、构建项目）
**问题反馈**: 查看 [开发环境启动指南](documentation/technical/DEVELOPMENT_STARTUP_GUIDE.md) 的常见问题部分
