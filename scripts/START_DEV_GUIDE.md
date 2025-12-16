# IOE-DREAM 开发环境启动脚本使用指南

## 📋 脚本说明

`start-dev.ps1` 是用于开发环境启动前后端和移动端服务的PowerShell脚本，**不使用Docker**，直接在本地启动服务。

## ⚠️ 前置条件：启动基础设施

**后端微服务启动前，必须先启动以下基础设施服务：**

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nacos | 8848 | 服务注册与配置中心 |

### 方式1：使用Docker Compose启动基础设施（推荐）

```powershell
# 在项目根目录执行
docker-compose -f docker-compose-all.yml up -d mysql redis nacos

# 等待约30秒让服务完全启动
Start-Sleep -Seconds 30

# 验证服务状态
docker-compose -f docker-compose-all.yml ps
```

### 方式2：手动安装启动

1. **MySQL 8.0+**
   - 下载安装：https://dev.mysql.com/downloads/
   - 确保端口 3306 可用

2. **Redis**
   - Windows: https://github.com/microsoftarchive/redis/releases
   - 确保端口 6379 可用

3. **Nacos 2.x**
   - 下载：https://github.com/alibaba/nacos/releases
   - 启动：`startup.cmd -m standalone`
   - 访问：http://localhost:8848/nacos (默认账号: nacos/nacos)

## 🚀 快速开始

### 基本用法

```powershell
# 启动所有服务（后端 + 前端 + 移动端）
.\scripts\start-dev.ps1

# 仅启动后端服务
.\scripts\start-dev.ps1 -BackendOnly

# 仅启动前端服务
.\scripts\start-dev.ps1 -FrontendOnly

# 仅启动移动端服务
.\scripts\start-dev.ps1 -MobileOnly

# 跳过构建（如果已经构建过）
.\scripts\start-dev.ps1 -SkipBuild
```

## 📦 启动的服务

### 后端微服务（按顺序启动）

| 服务名称 | 端口 | 说明 |
|---------|------|------|
| ioedream-gateway-service | 8080 | API网关 |
| ioedream-common-service | 8088 | 公共业务服务 |
| ioedream-device-comm-service | 8087 | 设备通讯服务 |
| ioedream-oa-service | 8089 | OA服务 |
| ioedream-access-service | 8090 | 门禁服务 |
| ioedream-attendance-service | 8091 | 考勤服务 |
| ioedream-video-service | 8092 | 视频服务 |
| ioedream-consume-service | 8094 | 消费服务 |
| ioedream-visitor-service | 8095 | 访客服务 |

### 前端服务

| 服务名称 | 端口 | 说明 |
|---------|------|------|
| smart-admin-web-javascript | 3000 | 管理后台前端 |

### 移动端服务

| 服务名称 | 端口 | 说明 |
|---------|------|------|
| smart-app | 8081 | 移动端H5应用 |

## 🔧 环境要求

### 必需工具

- **Java 17+**: 后端服务运行环境
- **Maven 3.6+**: 后端项目构建工具
- **Node.js 18+**: 前端和移动端运行环境
- **npm**: Node.js包管理器

### 环境检查

脚本会自动检查以下工具是否已安装：

```powershell
# 检查Java
java -version

# 检查Maven
mvn --version

# 检查Node.js
node --version

# 检查npm
npm --version
```

## 📝 使用示例

### 示例1: 启动所有服务

```powershell
# 在项目根目录执行
.\scripts\start-dev.ps1
```

**执行流程**:
1. 环境检查（Java、Maven、Node.js、npm）
2. 构建 microservices-common（如果未使用 -SkipBuild）
3. 启动所有后端微服务（每个服务在独立的后台任务中运行）
4. 启动前端服务（npm run dev）
5. 启动移动端服务（npm run dev:h5）
6. 显示服务状态和访问地址

### 示例2: 仅启动后端服务

```powershell
.\scripts\start-dev.ps1 -BackendOnly
```

适用于：
- 只需要测试后端API
- 前端和移动端在其他终端启动

### 示例3: 仅启动前端服务

```powershell
.\scripts\start-dev.ps1 -FrontendOnly
```

适用于：
- 后端服务已启动
- 只需要开发前端界面

### 示例4: 跳过构建

```powershell
.\scripts\start-dev.ps1 -SkipBuild
```

适用于：
- 已经构建过 microservices-common
- 快速启动服务

## 🎯 服务管理

### 查看运行中的任务

```powershell
# 查看所有后台任务
Get-Job

# 查看任务状态
Get-Job | Format-Table -AutoSize
```

### 查看服务日志

```powershell
# 查看指定任务的日志
Receive-Job -Id <JobId>

# 持续查看日志（实时输出）
Receive-Job -Id <JobId> -Keep
```

### 停止服务

```powershell
# 停止指定服务
Stop-Job -Id <JobId>
Remove-Job -Id <JobId>

# 停止所有服务
Get-Job | Stop-Job
Get-Job | Remove-Job
```

### 检查服务状态

脚本启动后会自动显示服务状态，也可以手动检查：

```powershell
# 检查端口是否被占用
Test-NetConnection -ComputerName localhost -Port 8080

# 检查所有服务端口
8080, 8087, 8088, 8089, 8090, 8091, 8092, 8094, 8095, 3000, 8081 | ForEach-Object {
    $port = $_
    $result = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue -InformationLevel Quiet
    if ($result) {
        Write-Host "端口 $port - 运行中" -ForegroundColor Green
    } else {
        Write-Host "端口 $port - 未运行" -ForegroundColor Red
    }
}
```

## 🌐 访问地址

启动成功后，可以通过以下地址访问：

- **前端管理后台**: http://localhost:3000
- **移动端H5**: http://localhost:8081
- **API网关**: http://localhost:8080

## ⚠️ 注意事项

### 1. 端口冲突

如果端口已被占用，脚本会跳过该服务的启动并显示警告。解决方法：

```powershell
# 查找占用端口的进程
netstat -ano | findstr :8080

# 结束进程（替换PID为实际进程ID）
taskkill /PID <PID> /F
```

### 2. 依赖安装

前端和移动端服务首次启动时会自动安装依赖（`npm install`），这可能需要一些时间。

### 3. 服务启动顺序

后端服务按以下顺序启动：
1. microservices-common（仅构建，不启动）
2. gateway-service（8080）
3. common-service（8088）
4. device-comm-service（8087）
5. oa-service（8089）
6. access-service（8090）
7. attendance-service（8091）
8. video-service（8092）
9. consume-service（8094）
10. visitor-service（8095）

### 4. 服务启动时间

- 每个后端服务启动需要 30-60 秒
- 前端服务启动需要 10-20 秒
- 移动端服务启动需要 10-20 秒
- 所有服务完全启动需要 5-10 分钟

### 5. 内存要求

建议系统内存至少 8GB，每个后端服务默认分配 512MB-1GB 内存。

## 🔍 故障排查

### 问题1: Java未找到

```
[ERROR] Java未安装或未添加到PATH
```

**解决方法**:
1. 安装Java 17+
2. 将Java添加到系统PATH环境变量
3. 重启PowerShell终端

### 问题2: Maven构建失败

```
[ERROR] microservices-common 构建失败
```

**解决方法**:
1. 检查Maven配置（`~/.m2/settings.xml`）
2. 检查网络连接（需要下载依赖）
3. 清理Maven缓存：`mvn clean`
4. 手动构建：`cd microservices\microservices-common && mvn clean install -DskipTests`

### 问题3: 端口被占用

```
[WARN] 端口 8080 已被占用
```

**解决方法**:
1. 查找占用端口的进程并结束
2. 修改服务端口（修改配置文件）
3. 停止其他占用端口的服务

### 问题4: npm install失败

```
[ERROR] npm install 失败
```

**解决方法**:
1. 检查网络连接
2. 清理npm缓存：`npm cache clean --force`
3. 删除node_modules后重新安装：`Remove-Item -Recurse -Force node_modules; npm install`
4. 使用国内镜像：`npm config set registry https://registry.npmmirror.com`

### 问题5: 服务启动后无法访问

**检查步骤**:
1. 确认服务是否正在运行：`Get-Job`
2. 查看服务日志：`Receive-Job -Id <JobId>`
3. 检查端口是否监听：`Test-NetConnection -ComputerName localhost -Port 8080`
4. 检查防火墙设置

## 📚 相关文档

- [项目架构规范](../CLAUDE.md)
- [部署指南](../documentation/04-部署运维/部署指南.md)
- [开发环境配置](../documentation/technical/repowiki/zh/content/开发指南.md)

## 🆘 获取帮助

如果遇到问题，请：

1. 查看脚本输出的错误信息
2. 检查服务日志：`Receive-Job -Id <JobId>`
3. 查看项目文档
4. 提交Issue到项目仓库

---

**版本**: v1.0.0  
**更新日期**: 2025-01-30  
**维护者**: IOE-DREAM 架构委员会

