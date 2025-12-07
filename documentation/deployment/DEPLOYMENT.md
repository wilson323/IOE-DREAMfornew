# IOE-DREAM 一键启动和部署指南

本文档说明如何使用一键脚本启动和部署IOE-DREAM智慧园区一卡通管理平台。

## 📋 目录

- [环境要求](#环境要求)
- [本地启动（开发模式）](#本地启动开发模式)
- [Docker部署（生产模式）](#docker部署生产模式)
- [服务检查](#服务检查)
- [常见问题](#常见问题)

---

## 🔧 环境要求

### 本地启动需要：
- **Java 17+**
- **Maven 3.9+**
- **Node.js 16+** (前端)
- **MySQL 8.0+** (需运行在3306端口)
- **Redis 7+** (需运行在6379端口)
- **Nacos 2.3.0+** (需运行在8848端口)

### Docker部署需要：
- **Docker Desktop 4.0+**
- **Docker Compose V2**
- 至少 **8GB内存** 和 **20GB磁盘空间**

---

## 🚀 本地启动（开发模式）

### 1. 启动基础设施服务

首先确保以下服务已启动：
- MySQL (端口3306)
- Redis (端口6379)  
- Nacos (端口8848)

可以使用Docker快速启动基础设施：
```powershell
# 启动MySQL、Redis、Nacos
docker-compose -f docker-compose-services.yml up -d
```

### 2. 启动所有微服务

使用完整启动脚本：
```powershell
# 启动所有服务（后端+前端+移动端）
.\scripts\start-all-complete.ps1

# 仅启动后端微服务
.\scripts\start-all-complete.ps1 -BackendOnly

# 仅启动前端
.\scripts\start-all-complete.ps1 -FrontendOnly

# 检查服务状态
.\scripts\start-all-complete.ps1 -CheckOnly
```

### 3. 访问应用

- **前端管理后台**: http://localhost:3000
- **移动端应用**: http://localhost:8081
- **API网关**: http://localhost:8080
- **Nacos控制台**: http://localhost:8848/nacos (用户名/密码: nacos/nacos)

---

## 🐳 Docker部署（生产模式）

### 1. 配置环境变量

编辑 `.env.docker` 文件，修改密码等敏感配置：
```bash
MYSQL_ROOT_PASSWORD=your_secure_password
REDIS_PASSWORD=your_redis_password
NACOS_AUTH_TOKEN=your_nacos_token
```

### 2. 完整部署（推荐）

一键构建镜像并启动所有服务：
```powershell
.\scripts\deploy-docker.ps1 full
```

这将自动完成：
1. ✅ 构建所有微服务Docker镜像
2. ✅ 启动MySQL、Redis、Nacos基础设施
3. ✅ 按依赖顺序启动9个微服务
4. ✅ 配置健康检查和自动重启

---

**最后更新**: 2025-12-07
**版本**: v1.0.0
