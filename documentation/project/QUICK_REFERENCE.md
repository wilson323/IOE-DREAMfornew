# IOE-DREAM 快速参考指南

**用途**: 快速查找和执行关键操作

---

## 🚀 立即执行命令（复制即用）

### 完整执行流程

```powershell
# 切换到项目目录
cd D:\IOE-DREAM

# 步骤1: 清理根目录临时文件
powershell -ExecutionPolicy Bypass -File scripts\cleanup-root-temp-files.ps1

# 步骤2: 清理之前的Docker构建
docker-compose -f docker-compose-all.yml down

# 步骤3: 重新构建所有镜像（不使用缓存）
docker-compose -f docker-compose-all.yml build --no-cache

# 步骤4: 启动所有服务
docker-compose -f docker-compose-all.yml up -d

# 步骤5: 等待服务启动（2-3分钟）
Start-Sleep -Seconds 180

# 步骤6: 验证部署
powershell -ExecutionPolicy Bypass -File scripts\verify-deployment-step-by-step.ps1
```

---

## 📋 关键文件位置

### 分析报告
- **全局分析**: `documentation/project/GLOBAL_DEEP_ANALYSIS_ROOT_CAUSE_SOLUTION.md`
- **执行计划**: `documentation/project/IMMEDIATE_ACTION_PLAN.md`
- **记忆索引**: `documentation/project/MEMORY_INDEX.md`

### 部署文档
- **Docker修复**: `documentation/deployment/DOCKER_BUILD_FIX.md`
- **验证指南**: `documentation/deployment/DEPLOYMENT_VERIFICATION_GUIDE.md`
- **快速启动**: `documentation/deployment/DOCKER_COMPOSE_QUICK_START.md`

### 脚本工具
- **清理脚本**: `scripts/cleanup-root-temp-files.ps1`
- **验证脚本**: `scripts/verify-deployment-step-by-step.ps1`
- **状态检查**: `scripts/check-docker-status.ps1`

---

## 🔧 常见问题快速解决

### 问题1: Docker构建失败
```powershell
# 检查Dockerfile是否包含-N参数
Select-String -Path "microservices\*\Dockerfile" -Pattern "-N"

# 如果缺少，已修复的Dockerfile应该包含：
# mvn clean install -N -DskipTests
# mvn clean package -N -DskipTests
```

### 问题2: 服务无法启动
```powershell
# 查看服务日志
docker-compose -f docker-compose-all.yml logs [服务名] --tail=100

# 检查端口占用
netstat -ano | findstr ":8080"
```

### 问题3: Nacos注册失败
```powershell
# 检查Nacos是否运行
docker ps | findstr nacos

# 查看Nacos日志
docker logs ioedream-nacos
```

---

## 📊 5个根源性问题速查

1. **R-001 项目结构混乱** → 执行 `scripts/cleanup-root-temp-files.ps1`
2. **R-002 构建策略冲突** → 已修复，使用 `-N` 参数
3. **R-003 文档管理分散** → 查看全局分析报告
4. **R-004 架构边界不清** → 查看CLAUDE.md规范
5. **R-005 遗留代码风险** → 待执行代码审查

---

**快速参考**: 查看 `MEMORY_INDEX.md` 获取完整记忆索引
