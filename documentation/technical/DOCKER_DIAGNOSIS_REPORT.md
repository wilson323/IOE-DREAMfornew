# Docker服务诊断报告

> **诊断时间**: 2025-01-30  
> **诊断方法**: 先检查现有状态，再决定是否需要启动服务

---

## 🔍 诊断流程

### 正确的诊断顺序

1. ✅ **检查Docker环境** - 确认Docker是否安装和运行
2. ✅ **检查现有容器** - 查看所有容器的状态
3. ✅ **检查特定服务** - MySQL、Redis、Nacos容器状态
4. ✅ **检查端口占用** - 3306、6379、8848端口是否开放
5. ✅ **检查Docker网络** - 网络配置和状态
6. ✅ **检查docker-compose配置** - 配置文件和服务定义
7. ✅ **根据诊断结果决定操作** - 启动缺失的服务或使用现有服务

---

## 📋 诊断命令清单

### 1. 检查Docker环境
```powershell
docker --version
docker ps
```

### 2. 检查所有容器
```powershell
docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

### 3. 检查特定服务容器
```powershell
# MySQL
docker ps -a --filter "name=mysql" --format "{{.Names}} - {{.Status}} - {{.Ports}}"

# Redis
docker ps -a --filter "name=redis" --format "{{.Names}} - {{.Status}} - {{.Ports}}"

# Nacos
docker ps -a --filter "name=nacos" --format "{{.Names}} - {{.Status}} - {{.Ports}}"
```

### 4. 检查端口占用
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

### 5. 检查Docker网络
```powershell
docker network ls
docker network ls --format "{{.Name}}" | Select-String "ioedream"
```

### 6. 检查docker-compose配置
```powershell
cd D:\IOE-DREAM\documentation\technical\verification\docker
docker-compose config --services
docker-compose ps
```

---

## 🎯 诊断结果处理

### 场景1：服务已运行
- **MySQL容器运行中** → ✅ 无需启动
- **Redis容器运行中** → ✅ 无需启动
- **Nacos容器运行中** → ✅ 无需启动
- **端口已开放** → ✅ 可以直接使用

**操作**: 直接启动微服务进行验证

### 场景2：容器存在但未运行
- **容器存在但状态为Exited** → ⚠️ 需要启动
- **端口未开放** → ⚠️ 需要启动容器

**操作**: 
```powershell
cd D:\IOE-DREAM\documentation\technical\verification\docker
docker-compose start mysql redis nacos
# 或
docker start <container-name>
```

### 场景3：容器不存在
- **未发现MySQL/Redis/Nacos容器** → ❌ 需要创建和启动

**操作**:
```powershell
cd D:\IOE-DREAM\documentation\technical\verification\docker
docker-compose up -d mysql redis nacos
```

### 场景4：端口被其他服务占用
- **端口3306/6379/8848被非Docker进程占用** → ⚠️ 需要处理冲突

**操作**:
1. 检查占用进程: `netstat -ano | findstr ":3306"`
2. 停止冲突服务或修改docker-compose.yml端口映射

---

## 📝 诊断脚本

已创建诊断脚本: `scripts/diagnose-docker-status.ps1`

**使用方法**:
```powershell
cd D:\IOE-DREAM
.\scripts\diagnose-docker-status.ps1
```

**脚本功能**:
- ✅ 检查Docker环境
- ✅ 检查所有容器状态
- ✅ 检查MySQL/Redis/Nacos容器
- ✅ 检查端口占用情况
- ✅ 检查Docker网络
- ✅ 检查docker-compose配置
- ✅ 提供操作建议

---

## ✅ 最佳实践

### 诊断优先原则
1. **先诊断，后操作** - 不要盲目启动服务
2. **检查现有资源** - 利用已运行的容器
3. **避免重复启动** - 防止端口冲突
4. **验证端口状态** - 确保服务可访问

### 操作流程
```
1. 运行诊断脚本
   ↓
2. 查看诊断结果
   ↓
3. 根据结果决定操作
   ↓
4. 执行相应操作（启动/使用现有）
   ↓
5. 验证服务状态
   ↓
6. 启动微服务进行功能验证
```

---

## 🔧 相关脚本

- `scripts/diagnose-docker-status.ps1` - 完整诊断脚本
- `scripts/check-docker-services.ps1` - 服务状态检查脚本
- `scripts/fix-docker-network.ps1` - 网络冲突修复脚本

---

**诊断原则**: ✅ **先诊断，后操作**  
**诊断脚本**: ✅ **已创建**  
**下一步**: 运行诊断脚本查看实际状态
