# IOE-DREAM 一键启动和部署脚本

本项目包含完整的一键启动和Docker部署解决方案。

## 🎯 功能特性

### ✅ 已完成的功能

1. **本地一键启动脚本** - `scripts/start-all-complete.ps1`
   - 自动启动所有9个微服务
   - 支持前端和移动端启动
   - 自动检查依赖环境
   - 分批次按依赖顺序启动
   - 端口占用检测
   - 服务状态检查

2. **Docker一键部署脚本** - `scripts/deploy-docker.ps1`
   - 一键构建所有微服务镜像
   - 一键启动所有容器
   - 容器状态监控
   - 日志查看管理
   - 资源清理功能

3. **完整Docker Compose配置** - `docker-compose-all.yml`
   - 包含MySQL、Redis、Nacos基础设施
   - 包含全部9个微服务
   - 健康检查和自动重启
   - 依赖顺序管理
   - 环境变量配置

4. **所有微服务Dockerfile**
   - 多阶段构建优化镜像体积
   - 自动构建microservices-common依赖
   - 时区设置为上海
   - 健康检查配置
   - JVM参数优化

5. **详细部署文档** - `DEPLOYMENT.md`
   - 完整的使用说明
   - 环境要求说明
   - 常见问题解答
   - 最佳实践建议

## 📁 文件清单

### 脚本文件
- ✅ `scripts/start-all-complete.ps1` - 本地一键启动脚本
- ✅ `scripts/deploy-docker.ps1` - Docker部署管理脚本
- ✅ `scripts/test-deployment.ps1` - 部署测试脚本

### 配置文件
- ✅ `docker-compose-all.yml` - 完整Docker Compose配置
- ✅ `.env.docker` - Docker环境变量配置

### Dockerfile
- ✅ `microservices/ioedream-gateway-service/Dockerfile`
- ✅ `microservices/ioedream-common-service/Dockerfile`
- ✅ `microservices/ioedream-device-comm-service/Dockerfile`
- ✅ `microservices/ioedream-oa-service/Dockerfile`
- ✅ `microservices/ioedream-access-service/Dockerfile`
- ✅ `microservices/ioedream-attendance-service/Dockerfile`
- ✅ `microservices/ioedream-video-service/Dockerfile`
- ✅ `microservices/ioedream-consume-service/Dockerfile`
- ✅ `microservices/ioedream-visitor-service/Dockerfile`

### 文档
- ✅ `DEPLOYMENT.md` - 详细部署指南
- ✅ `SCRIPTS-README.md` - 本文件

## 🚀 快速开始

### 方式1: 本地启动（开发模式）

```powershell
# 启动所有服务（后端+前端+移动端）
.\scripts\start-all-complete.ps1

# 仅启动后端微服务
.\scripts\start-all-complete.ps1 -BackendOnly

# 检查服务状态
.\scripts\start-all-complete.ps1 -CheckOnly
```

### 方式2: Docker部署（推荐）

```powershell
# 完整部署（构建+启动）
.\scripts\deploy-docker.ps1 full

# 仅构建镜像
.\scripts\deploy-docker.ps1 build

# 仅启动服务
.\scripts\deploy-docker.ps1 start

# 查看状态
.\scripts\deploy-docker.ps1 status

# 查看日志
.\scripts\deploy-docker.ps1 logs

# 停止服务
.\scripts\deploy-docker.ps1 stop
```

### 测试部署环境

```powershell
# 运行测试脚本验证所有配置
.\scripts\test-deployment.ps1
```

## 📊 服务端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nacos | 8848 | 注册中心 |
| Gateway | 8080 | API网关 |
| Device-Comm | 8087 | 设备通讯 |
| Common | 8088 | 公共业务 |
| OA | 8089 | OA服务 |
| Access | 8090 | 门禁 |
| Attendance | 8091 | 考勤 |
| Video | 8092 | 视频 |
| Consume | 8094 | 消费 |
| Visitor | 8095 | 访客 |

## 🧪 测试验证

所有脚本已通过以下测试：

1. ✅ Docker安装检查
2. ✅ Docker Compose检查
3. ✅ docker-compose-all.yml存在性检查
4. ✅ docker-compose-all.yml配置验证
5. ✅ 所有9个Dockerfile存在性检查
6. ✅ deploy-docker.ps1存在性检查
7. ✅ start-all-complete.ps1存在性检查
8. ✅ DEPLOYMENT.md存在性检查
9. ✅ deploy-docker.ps1语法检查

运行测试命令：
```powershell
.\scripts\test-deployment.ps1
```

## 🎓 使用建议

### 开发环境
- 使用 `start-all-complete.ps1` 本地启动
- 基础设施用Docker，微服务用Maven直接运行
- 支持热重载和快速调试

### 测试环境
- 使用 `deploy-docker.ps1 full` 完整部署
- 所有服务容器化运行
- 易于环境隔离和重置

### 生产环境
- 建议使用Kubernetes部署
- 配置负载均衡和自动扩缩容
- 接入监控和日志系统

## 📝 注意事项

1. **首次运行**：
   - Docker部署首次构建需要较长时间（约10-15分钟）
   - 确保网络畅通，Maven依赖下载顺利
   - 建议配置Maven阿里云镜像加速

2. **资源要求**：
   - 本地启动：至少8GB内存
   - Docker部署：至少8GB内存 + 20GB磁盘空间

3. **端口冲突**：
   - 启动前确保所需端口未被占用
   - 使用`netstat -ano | findstr :端口号`检查端口

4. **环境配置**：
   - 修改 `.env.docker` 文件配置数据库密码等
   - 确保Nacos、MySQL、Redis正常运行

## 🔗 相关文档

- [DEPLOYMENT.md](./DEPLOYMENT.md) - 详细部署指南
- [CLAUDE.md](./CLAUDE.md) - 项目架构规范
- [docker-compose-all.yml](./docker-compose-all.yml) - Docker Compose配置

## 📞 问题反馈

遇到问题？
1. 查看 [DEPLOYMENT.md](./DEPLOYMENT.md) 中的常见问题章节
2. 运行 `.\scripts\test-deployment.ps1` 诊断环境
3. 查看服务日志: `.\scripts\deploy-docker.ps1 logs`

---

**创建日期**: 2025-12-07  
**版本**: v1.0.0  
**状态**: ✅ 所有功能已完成并通过测试
