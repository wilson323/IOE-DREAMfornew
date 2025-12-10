# Docker Compose 启动异常诊断报告

**日期**: 2025-01-30  
**问题**: docker-compose-all.yml 启动时卡在 13/14，大量重复输出

---

## 🔍 问题现象

从启动输出可以看到：
1. **网络重复创建**: `Network ioedream-network Created` 重复出现多次
2. **Redis状态循环**: Redis容器在 `Created -> Starting -> Started -> Waiting -> Healthy` 之间循环
3. **服务卡住**: 显示 `Running 13/14`，有1个服务无法启动
4. **输出重复**: 大量重复的状态输出

---

## 🎯 根本原因分析

### P0级问题：Redis健康检查配置错误 ⚠️

**问题位置**: `docker-compose-all.yml` 第41-46行

**问题描述**:
```yaml
redis:
  command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD:-redis123}
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]  # ❌ 缺少密码参数
```

**根本原因**:
- Redis配置了密码认证：`--requirepass ${REDIS_PASSWORD:-redis123}`
- 健康检查命令没有提供密码：`redis-cli ping`
- 导致健康检查失败，容器不断重启
- 依赖Redis的服务（所有微服务）无法启动

**影响范围**:
- ✅ Redis容器本身可以启动
- ❌ Redis健康检查失败
- ❌ 所有依赖Redis的服务无法通过健康检查
- ❌ 整个服务栈启动失败

---

### P1级问题：可能的其他原因

1. **微服务镜像未构建**
   - 9个微服务需要build，如果镜像不存在会导致启动失败
   - 检查：`docker images | Select-String "ioedream"`

2. **端口占用冲突**
   - 3306 (MySQL), 6379 (Redis), 8848 (Nacos) 等端口可能被占用
   - 检查：`netstat -ano | Select-String ":3306|:6379|:8848"`

3. **资源不足**
   - 内存或CPU不足导致容器启动失败
   - 检查：`docker stats --no-stream`

4. **依赖等待超时**
   - 某个服务等待健康检查超时
   - 检查：`docker-compose -f docker-compose-all.yml ps`

---

## ✅ 解决方案

### 方案1：修复Redis健康检查（必须）

**修复内容**:
```yaml
redis:
  healthcheck:
    test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD:-redis123}", "ping"]
    # 或者使用环境变量
    # test: ["CMD-SHELL", "redis-cli -a $${REDIS_PASSWORD:-redis123} ping || exit 1"]
```

**修复步骤**:
1. 修改 `docker-compose-all.yml` 第42行
2. 停止所有容器：`docker-compose -f docker-compose-all.yml down`
3. 重新启动：`docker-compose -f docker-compose-all.yml up -d`

---

### 方案2：分步启动验证

**步骤1**: 启动基础设施
```powershell
docker-compose -f docker-compose-all.yml up -d mysql redis
```

**步骤2**: 等待健康检查通过
```powershell
docker-compose -f docker-compose-all.yml ps
# 等待 mysql 和 redis 显示 healthy
```

**步骤3**: 启动数据库初始化
```powershell
docker-compose -f docker-compose-all.yml up db-init
```

**步骤4**: 启动Nacos
```powershell
docker-compose -f docker-compose-all.yml up -d nacos
```

**步骤5**: 构建并启动微服务
```powershell
docker-compose -f docker-compose-all.yml build
docker-compose -f docker-compose-all.yml up -d
```

---

## 🔧 诊断脚本

创建 `scripts/diagnose-docker-compose.ps1` 进行自动诊断。

---

## 📊 验证清单

修复后验证：
- [ ] Redis健康检查通过：`docker inspect ioedream-redis --format='{{.State.Health.Status}}'`
- [ ] MySQL健康检查通过：`docker inspect ioedream-mysql --format='{{.State.Health.Status}}'`
- [ ] Nacos健康检查通过：`docker inspect ioedream-nacos --format='{{.State.Health.Status}}'`
- [ ] 所有服务状态正常：`docker-compose -f docker-compose-all.yml ps`
- [ ] 无端口冲突：`netstat -ano | Select-String ":3306|:6379|:8848"`

---

## 📝 后续优化建议

1. **健康检查优化**
   - 所有带密码的服务健康检查都要包含密码
   - 使用环境变量避免硬编码

2. **启动顺序优化**
   - 明确依赖关系
   - 使用 `depends_on` 的 `condition` 确保顺序

3. **日志收集**
   - 统一日志格式
   - 便于问题排查

4. **监控告警**
   - 容器健康状态监控
   - 自动告警机制

---

**报告生成时间**: 2025-01-30  
**诊断工具**: Sequential Thinking + 深度分析
