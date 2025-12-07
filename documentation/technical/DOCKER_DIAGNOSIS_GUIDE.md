# Docker服务诊断指南

> **重要原则**: ✅ **先诊断现有状态，再决定是否需要启动服务**

---

## 🎯 正确的操作流程

### ❌ 错误做法
直接执行 `docker-compose up -d` 而不检查现有状态

### ✅ 正确做法
1. **先诊断** - 检查Docker服务当前状态
2. **再决策** - 根据诊断结果决定操作
3. **后执行** - 启动缺失的服务或使用现有服务

---

## 📋 诊断步骤（按顺序执行）

### 步骤1：检查Docker环境
```powershell
# 检查Docker是否安装
docker --version

# 检查Docker是否运行
docker ps
```

### 步骤2：检查现有容器
```powershell
# 查看所有容器（包括已停止的）
docker ps -a

# 格式化输出
docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

### 步骤3：检查特定服务容器
```powershell
# MySQL容器
docker ps -a --filter "name=mysql"

# Redis容器
docker ps -a --filter "name=redis"

# Nacos容器
docker ps -a --filter "name=nacos"
```

### 步骤4：检查端口占用情况
```powershell
# 检查端口是否开放
Test-NetConnection -ComputerName localhost -Port 3306 -InformationLevel Quiet
Test-NetConnection -ComputerName localhost -Port 6379 -InformationLevel Quiet
Test-NetConnection -ComputerName localhost -Port 8848 -InformationLevel Quiet

# 检查端口占用进程
netstat -ano | findstr ":3306"
netstat -ano | findstr ":6379"
netstat -ano | findstr ":8848"
```

### 步骤5：检查Docker网络
```powershell
# 查看所有网络
docker network ls

# 查找ioedream相关网络
docker network ls --format "{{.Name}}" | Select-String "ioedream"
```

### 步骤6：检查docker-compose配置
```powershell
cd D:\IOE-DREAM\documentation\technical\verification\docker

# 查看定义的服务
docker-compose config --services

# 查看容器状态
docker-compose ps
```

---

## 🔍 诊断结果处理

### 场景A：服务已运行 ✅
**现象**:
- 容器状态为 `Up`
- 端口3306/6379/8848已开放

**操作**: 
- ✅ **无需启动**，直接使用现有服务
- 启动微服务进行功能验证

### 场景B：容器存在但未运行 ⚠️
**现象**:
- 容器状态为 `Exited`
- 端口未开放

**操作**:
```powershell
cd D:\IOE-DREAM\documentation\technical\verification\docker
# 启动现有容器
docker-compose start mysql redis nacos
# 或启动单个容器
docker start <container-name>
```

### 场景C：容器不存在 ❌
**现象**:
- `docker ps -a` 中未发现相关容器
- 端口未开放

**操作**:
```powershell
cd D:\IOE-DREAM\documentation\technical\verification\docker
# 创建并启动容器
docker-compose up -d mysql redis nacos
```

### 场景D：端口被其他服务占用 ⚠️
**现象**:
- 端口已开放但非Docker容器占用
- `netstat` 显示其他进程占用

**操作**:
1. 检查占用进程: `netstat -ano | findstr ":3306"`
2. 停止冲突服务或修改docker-compose.yml端口映射

---

## 🛠️ 使用诊断脚本

### 自动诊断脚本
```powershell
cd D:\IOE-DREAM
.\scripts\diagnose-docker-status.ps1
```

**脚本功能**:
- ✅ 自动检查Docker环境
- ✅ 检查所有容器状态
- ✅ 检查MySQL/Redis/Nacos服务
- ✅ 检查端口占用
- ✅ 检查Docker网络
- ✅ 提供操作建议

---

## 📊 诊断检查清单

### 必须检查项
- [ ] Docker是否安装和运行
- [ ] 现有容器状态（运行中/已停止/不存在）
- [ ] MySQL容器和端口3306状态
- [ ] Redis容器和端口6379状态
- [ ] Nacos容器和端口8848状态
- [ ] Docker网络配置
- [ ] docker-compose配置

### 根据检查结果
- [ ] 服务已运行 → 直接使用
- [ ] 容器存在但未运行 → 启动容器
- [ ] 容器不存在 → 创建并启动
- [ ] 端口冲突 → 解决冲突

---

## ✅ 最佳实践总结

### 诊断优先原则
1. **先诊断，后操作** - 不要盲目执行命令
2. **检查现有资源** - 利用已运行的容器
3. **避免重复启动** - 防止端口冲突和资源浪费
4. **验证端口状态** - 确保服务可访问

### 操作流程
```
┌─────────────────┐
│  运行诊断脚本   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  查看诊断结果   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  根据结果决策   │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌───────┐ ┌───────┐
│已运行 │ │需启动 │
│直接用 │ │执行   │
└───────┘ └───────┘
    │         │
    └────┬────┘
         │
         ▼
┌─────────────────┐
│  验证服务状态   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  启动微服务验证 │
└─────────────────┘
```

---

## 📚 相关文档和脚本

- `scripts/diagnose-docker-status.ps1` - 完整诊断脚本
- `scripts/check-docker-services.ps1` - 服务状态检查脚本
- `scripts/fix-docker-network.ps1` - 网络冲突修复脚本
- `documentation/technical/DOCKER_DIAGNOSIS_REPORT.md` - 诊断报告模板

---

**核心原则**: ✅ **先诊断现有状态，再决定是否需要启动服务**  
**诊断脚本**: ✅ **已创建**  
**下一步**: 手动执行诊断脚本或按步骤检查
