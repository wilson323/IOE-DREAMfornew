# IOE-DREAM 一键启动脚本使用说明

## 📋 脚本说明

项目提供了两个启动脚本：

1. **`start-all-complete.ps1`** - 完整启动脚本（推荐）
   - 功能最全面，包含详细的状态检查和错误处理
   - 支持按需启动不同模块
   - 自动检查依赖服务（MySQL、Redis、Nacos）
   - 自动构建公共模块

2. **`quick-start.ps1`** - 快速启动脚本（简化版）
   - 更简洁，快速启动所有服务
   - 基于完整脚本的封装

## 🚀 使用方法

### 方式一：完整启动脚本（推荐）

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

### 方式二：快速启动脚本

```powershell
# 启动所有服务
.\scripts\quick-start.ps1

# 仅启动后端
.\scripts\quick-start.ps1 -Backend

# 仅启动前端
.\scripts\quick-start.ps1 -Frontend

# 仅启动移动端
.\scripts\quick-start.ps1 -Mobile
```

## 📦 启动的服务

### 后端微服务（按顺序启动）

| 服务名称 | 端口 | 类型 | 启动顺序 |
|---------|------|------|---------|
| ioedream-gateway-service | 8080 | 基础设施 | 第1批 |
| ioedream-common-service | 8088 | 公共模块 | 第2批 |
| ioedream-device-comm-service | 8087 | 设备通讯 | 第2批 |
| ioedream-oa-service | 8089 | OA服务 | 第3批 |
| ioedream-access-service | 8090 | 门禁服务 | 第3批 |
| ioedream-attendance-service | 8091 | 考勤服务 | 第3批 |
| ioedream-video-service | 8092 | 视频服务 | 第3批 |
| ioedream-consume-service | 8094 | 消费服务 | 第3批 |
| ioedream-visitor-service | 8095 | 访客服务 | 第3批 |

### 前端应用

| 应用名称 | 端口 | 启动命令 |
|---------|------|---------|
| 前端管理后台 | 3000 | `npm run localhost` |
| 移动端应用(H5) | 8081 | `npm run dev:h5` |

## 🔧 前置条件

### 必需环境

1. **Java 17+** - 后端运行环境
2. **Maven 3.8+** - 项目构建工具
3. **Node.js 18+** - 前端运行环境
4. **MySQL 8.0+** - 数据库（端口3306）
5. **Redis 6.0+** - 缓存服务（端口6379）
6. **Nacos 2.2.0+** - 服务注册中心（端口8848）

### 依赖服务检查

脚本会自动检查以下服务：

- ✅ **Nacos** (端口8848) - 必需，服务注册发现
- ⚠️ **MySQL** (端口3306) - 建议，数据库连接
- ⚠️ **Redis** (端口6379) - 建议，缓存功能

如果Nacos未运行，脚本会提示是否继续启动。

## 📍 访问地址

启动完成后，可以通过以下地址访问：

- **前端管理后台**: http://localhost:3000
- **移动端应用**: http://localhost:8081
- **API网关**: http://localhost:8080
- **Nacos控制台**: http://localhost:8848/nacos
  - 默认用户名/密码: `nacos/nacos`

## 🔍 服务状态检查

```powershell
# 检查所有服务状态
.\scripts\start-all-complete.ps1 -CheckOnly
```

输出示例：
```
【后端微服务】
  ✅ ioedream-gateway-service (端口8080) - 运行中
  ✅ ioedream-common-service (端口8088) - 运行中
  ⭕ ioedream-access-service (端口8090) - 未运行

【前端应用】
  ✅ 前端管理后台 (端口3000) - 运行中

【移动端应用】
  ✅ 移动端应用(H5) (端口8081) - 运行中
```

## ⚠️ 注意事项

1. **首次启动**：
   - 脚本会自动构建 `microservices-common` 模块
   - 如果前端/移动端依赖未安装，会自动执行 `npm install`

2. **端口占用**：
   - 如果端口被占用，脚本会跳过该服务的启动
   - 请先停止占用端口的进程，或修改服务端口配置

3. **启动顺序**：
   - 后端服务按依赖顺序分批次启动
   - 每批次之间有等待时间，确保服务完全启动

4. **新窗口启动**：
   - 每个服务会在新的PowerShell窗口中启动
   - 可以独立查看每个服务的日志输出

5. **编码问题**：
   - 脚本已设置UTF-8编码，解决中文乱码问题
   - 如果仍有乱码，请检查PowerShell编码设置

## 🐛 常见问题

### 1. Maven未找到

**错误**: `Maven未安装或未配置到PATH`

**解决**: 
- 安装Maven并配置环境变量
- 或使用IDE内置的Maven

### 2. Node.js未找到

**错误**: `Node.js未安装或未配置到PATH`

**解决**: 
- 安装Node.js 18+
- 配置到系统PATH环境变量

### 3. 端口被占用

**错误**: `端口 XXX 已被占用`

**解决**: 
```powershell
# 查找占用端口的进程
netstat -ano | findstr :8080

# 结束进程（替换PID为实际进程ID）
taskkill /PID <PID> /F
```

### 4. 服务启动失败

**检查**:
1. 检查依赖服务是否运行（MySQL、Redis、Nacos）
2. 检查配置文件是否正确
3. 查看服务窗口的错误日志

### 5. 前端无法连接后端

**检查**:
1. 确认后端服务已启动
2. 检查前端 `.env.development` 或 `.env.localhost` 配置
3. 确认API地址配置正确

## 📚 相关文档

- [项目README](../README.md)
- [部署指南](../documentation/deployment/)
- [开发指南](../documentation/technical/00-快速开始/)

## 💡 提示

- 首次启动建议使用完整脚本 `start-all-complete.ps1`，功能更全面
- 日常开发可以使用快速脚本 `quick-start.ps1`，更简洁
- 使用 `-CheckOnly` 参数可以快速检查服务状态，无需启动
