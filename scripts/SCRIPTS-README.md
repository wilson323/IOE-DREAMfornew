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

5. **详细部署文档** - `documentation/deployment/DEPLOYMENT.md`
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

---

**创建日期**: 2025-12-07  
**版本**: v1.0.0  
**状态**: ✅ 所有功能已完成并通过测试
