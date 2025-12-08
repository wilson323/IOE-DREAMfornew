# IOE-DREAM 开发环境启动指南

> **适用场景**: 本地开发环境启动
> **更新日期**: 2025-01-30
> **适用系统**: Windows 10/11

---

## 📋 目录

- [环境准备](#环境准备)
- [快速启动（推荐）](#快速启动推荐)
- [分步启动（详细）](#分步启动详细)
- [验证启动](#验证启动)
- [常见问题](#常见问题)

---

## 🔧 环境准备

### 必需软件

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| **JDK** | 17+ | Java运行环境 |
| **Maven** | 3.8+ | 项目构建工具 |
| **Node.js** | 18+ | 前端运行环境 |
| **Docker Desktop** | 20.10+ | 可选，用于自动启动MySQL/Redis/Nacos |
| **Git** | 2.0+ | 版本控制 |

### 可选软件（推荐）

| 软件 | 说明 |
|------|------|
| **MySQL 8.0+** | 如果不用Docker，需要本地安装 |
| **Redis 6.0+** | 如果不用Docker，需要本地安装 |
| **Nacos 2.2.0+** | 如果不用Docker，需要本地安装 |

### 环境检查

```powershell
# 检查Java版本
java -version

# 检查Maven版本
mvn -version

# 检查Node.js版本
node -v
npm -v

# 检查Docker（可选）
docker --version
docker-compose --version
```

---

## 🚀 快速启动（推荐）

### 方式一：一键启动所有服务（最推荐）

```powershell
# 进入项目根目录
cd D:\IOE-DREAM

# 一键启动（自动检测并启动MySQL/Redis/Nacos）
.\scripts\start-all-complete.ps1
```

**功能说明**:
- ✅ 自动检测Docker是否可用
- ✅ 如果MySQL未运行，自动通过Docker启动
- ✅ 如果Redis未运行，自动通过Docker启动
- ✅ 如果Nacos未运行，自动通过Docker启动
- ✅ 自动构建公共模块
- ✅ 按顺序启动所有后端微服务
- ✅ 启动前端管理后台
- ✅ 启动移动端应用

### 方式二：仅启动后端服务

```powershell
.\scripts\start-all-complete.ps1 -BackendOnly
```

### 方式三：仅启动前端

```powershell
.\scripts\start-all-complete.ps1 -FrontendOnly
```

### 方式四：仅启动移动端

```powershell
.\scripts\start-all-complete.ps1 -MobileOnly
```

---

## 📝 分步启动（详细）

如果一键启动遇到问题，可以按以下步骤手动启动：

### 步骤1: 启动基础设施服务

#### 选项A：使用Docker（推荐）

```powershell
# 进入项目根目录
cd D:\IOE-DREAM

# 启动MySQL、Redis、Nacos
docker-compose -f docker-compose-all.yml up -d mysql redis nacos

# 等待服务就绪（约30秒）
Start-Sleep -Seconds 30

# 初始化Nacos数据库（如果需要）
.\scripts\init-nacos-database.ps1
```

#### 选项B：本地安装的服务

```powershell
# 启动MySQL（Windows服务）
net start MySQL80

# 启动Redis（如果作为服务安装）
net start Redis

# 启动Nacos（进入Nacos目录）
cd D:\nacos\bin
startup.cmd -m standalone
```

### 步骤2: 验证基础设施服务

```powershell
# 检查MySQL（端口3306）
Test-NetConnection -ComputerName localhost -Port 3306

# 检查Redis（端口6379）
Test-NetConnection -ComputerName localhost -Port 6379

# 检查Nacos（端口8848）
Test-NetConnection -ComputerName localhost -Port 8848

# 访问Nacos控制台
# http://localhost:8848/nacos
# 用户名/密码: nacos/nacos
```

### 步骤3: 初始化数据库

```powershell
# 如果使用Docker，数据库会自动初始化
# 如果使用本地MySQL，需要手动初始化

# 创建数据库
mysql -u root -p
CREATE DATABASE ioedream DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE nacos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;

# 导入数据库脚本
mysql -u root -p ioedream < database-scripts\common-service\00-database-init.sql

# 初始化Nacos数据库
mysql -u root -p nacos < deployment\mysql\init\nacos-schema.sql
```

### 步骤4: 构建公共模块

```powershell
# 进入公共模块目录
cd D:\IOE-DREAM\microservices\microservices-common

# 构建并安装到本地仓库
mvn clean install -DskipTests

# 返回项目根目录
cd D:\IOE-DREAM
```

### 步骤5: 启动后端微服务

#### 方式A：使用启动脚本（推荐）

```powershell
.\scripts\start-all-complete.ps1 -BackendOnly
```

#### 方式B：手动启动（按顺序）

```powershell
# 第1批：基础设施服务
cd D:\IOE-DREAM\microservices\ioedream-gateway-service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"

# 等待15秒
Start-Sleep -Seconds 15

# 第2批：公共模块服务
cd D:\IOE-DREAM\microservices\ioedream-common-service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"

cd D:\IOE-DREAM\microservices\ioedream-device-comm-service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"

# 等待15秒
Start-Sleep -Seconds 15

# 第3批：业务服务（可并行启动）
cd D:\IOE-DREAM\microservices\ioedream-oa-service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"

cd D:\IOE-DREAM\microservices\ioedream-access-service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"

# ... 其他业务服务类似
```

### 步骤6: 启动前端应用

```powershell
# 进入前端目录
cd D:\IOE-DREAM\smart-admin-web-javascript

# 安装依赖（首次启动）
npm install

# 启动开发服务器（localhost模式，连接本地后端）
npm run localhost

# 或使用dev模式
npm run dev
```

### 步骤7: 启动移动端应用（可选）

```powershell
# 进入移动端目录
cd D:\IOE-DREAM\smart-app

# 安装依赖（首次启动）
npm install

# 启动H5开发服务器
npm run dev:h5
```

---

## ✅ 验证启动

### 检查服务状态

```powershell
# 使用脚本检查
.\scripts\start-all-complete.ps1 -CheckOnly
```

### 手动检查

```powershell
# 检查后端服务端口
Test-NetConnection -ComputerName localhost -Port 8080  # 网关
Test-NetConnection -ComputerName localhost -Port 8088  # 公共服务
Test-NetConnection -ComputerName localhost -Port 8090  # 门禁服务

# 检查前端
Test-NetConnection -ComputerName localhost -Port 3000  # 前端管理后台
Test-NetConnection -ComputerName localhost -Port 8081  # 移动端
```

### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端管理后台 | http://localhost:3000 | Vue3管理界面 |
| 移动端应用 | http://localhost:8081 | uni-app H5 |
| API网关 | http://localhost:8080 | Spring Cloud Gateway |
| Nacos控制台 | http://localhost:8848/nacos | 服务注册中心 |
| Swagger文档 | http://localhost:8080/doc.html | API文档 |

### 默认账号

- **系统管理员**: `admin / 123456`
- **Nacos控制台**: `nacos / nacos`
- **MySQL数据库**: `root / root`（Docker默认）

---

## 🐛 常见问题

### 1. MySQL未运行

**问题**: 脚本提示MySQL未运行

**解决方案**:

```powershell
# 方案1：使用Docker自动启动（推荐）
# 脚本会自动检测并启动，无需手动操作

# 方案2：手动启动Docker MySQL
docker-compose -f docker-compose-all.yml up -d mysql

# 方案3：启动本地MySQL服务
net start MySQL80
```

### 2. Nacos启动失败

**问题**: Nacos无法连接MySQL

**解决方案**:

```powershell
# 1. 确保MySQL已启动
Test-NetConnection -ComputerName localhost -Port 3306

# 2. 初始化Nacos数据库
.\scripts\init-nacos-database.ps1

# 3. 重启Nacos
docker restart ioedream-nacos
```

### 3. 端口被占用

**问题**: 端口已被其他程序占用

**解决方案**:

```powershell
# 查找占用端口的进程
netstat -ano | findstr :8080

# 结束进程（替换PID为实际进程ID）
taskkill /PID <PID> /F

# 或修改服务端口配置
# 编辑对应服务的 application.yml
```

### 4. Maven构建失败

**问题**: `microservices-common` 构建失败

**解决方案**:

```powershell
# 1. 清理并重新构建
cd D:\IOE-DREAM\microservices\microservices-common
mvn clean install -DskipTests

# 2. 检查Maven配置
mvn -version

# 3. 检查网络连接（下载依赖）
mvn dependency:resolve
```

### 5. 前端无法连接后端

**问题**: 前端页面显示API请求失败

**解决方案**:

```powershell
# 1. 检查后端服务是否启动
.\scripts\start-all-complete.ps1 -CheckOnly

# 2. 检查前端配置文件
# 编辑 smart-admin-web-javascript/.env.localhost
# 确认 VITE_APP_API_URL 配置正确

# 3. 检查CORS配置
# 确保后端网关允许前端域名访问
```

### 6. 服务注册失败

**问题**: 服务无法注册到Nacos

**解决方案**:

```powershell
# 1. 检查Nacos是否运行
Test-NetConnection -ComputerName localhost -Port 8848

# 2. 访问Nacos控制台
# http://localhost:8848/nacos

# 3. 检查服务配置
# 确认 bootstrap.yml 中Nacos配置正确

# 4. 查看服务日志
# 检查服务启动窗口的错误信息
```

---

## 📚 相关文档

- [项目README](../../README.md)
- [启动脚本说明](../../scripts/README_START.md)
- [Docker部署指南](../../DOCKER_DEPLOYMENT_GUIDE.md)
- [开发规范](../UNIFIED_DEVELOPMENT_STANDARDS.md)

---

## 💡 开发建议

### 日常开发流程

1. **启动基础设施**（每天一次）
   ```powershell
   docker-compose -f docker-compose-all.yml up -d mysql redis nacos
   ```

2. **启动后端服务**（按需）
   ```powershell
   .\scripts\start-all-complete.ps1 -BackendOnly
   ```

3. **启动前端**（按需）
   ```powershell
   .\scripts\start-all-complete.ps1 -FrontendOnly
   ```

### 性能优化

- 如果只开发某个微服务，可以只启动该服务和依赖的服务
- 使用IDE的Spring Boot运行配置，比命令行启动更快
- 前端使用 `npm run localhost` 模式，热更新更快

### 调试技巧

- 每个服务在独立窗口启动，方便查看日志
- 使用Nacos控制台查看服务注册状态
- 使用Swagger文档测试API接口
- 前端使用浏览器开发者工具调试

---

**最后更新**: 2025-01-30
**维护人**: IOE-DREAM开发团队
