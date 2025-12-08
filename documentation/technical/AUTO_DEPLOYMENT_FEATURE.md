# 自动化部署功能说明

## 🎯 功能概述

**自动化部署脚本**会在部署时**自动检测并初始化数据库**，无需手动操作。

## ✅ 已实现功能

### 1. 自动数据库检测和初始化

**脚本**: `scripts/deploy.ps1` 和 `scripts/deploy-auto.ps1`

**功能**:
- ✅ 自动检测MySQL容器状态
- ✅ 自动检测nacos数据库是否存在
- ✅ 自动检测nacos数据库表结构是否已初始化
- ✅ 如果数据库不存在或未初始化，**自动执行初始化**
- ✅ 验证初始化结果

### 2. 部署流程

```
1. 环境检查
   ↓
2. 启动MySQL和Redis
   ↓
3. 等待MySQL就绪（健康检查）
   ↓
4. 自动检测nacos数据库状态
   ↓
5. 如果未初始化 → 自动初始化
   ↓
6. 启动Nacos（此时数据库已就绪）
   ↓
7. 启动所有微服务
   ↓
8. 验证部署结果
```

## 📋 使用方法

### 方法1: 使用deploy.ps1（推荐）

```powershell
# 完整部署（自动检测并初始化数据库）
.\scripts\deploy.ps1

# 跳过构建（使用已有镜像）
.\scripts\deploy.ps1 -SkipBuild

# 开发环境
.\scripts\deploy.ps1 -Dev

# 生产环境
.\scripts\deploy.ps1 -Prod
```

### 方法2: 使用deploy-auto.ps1

```powershell
# 自动化部署（自动检测并初始化数据库）
.\scripts\deploy-auto.ps1

# 强制重新初始化
.\scripts\deploy-auto.ps1 -Force

# 跳过数据库初始化（如果已确认数据库已初始化）
.\scripts\deploy-auto.ps1 -SkipInit
```

## 🔍 自动检测逻辑

### 检测步骤

1. **MySQL容器状态检测**
   ```powershell
   docker inspect ioedream-mysql --format='{{.State.Health.Status}}'
   ```
   - 等待MySQL健康检查通过（最多120秒）

2. **nacos数据库存在性检测**
   ```powershell
   docker exec ioedream-mysql mysql -uroot -proot -e "SHOW DATABASES LIKE 'nacos';"
   ```
   - 如果数据库不存在，标记为需要初始化

3. **nacos数据库表结构检测**
   ```powershell
   docker exec ioedream-mysql mysql -uroot -proot -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';"
   ```
   - 如果表数量为0，标记为需要初始化

4. **自动初始化**
   - 创建nacos数据库（如果不存在）
   - 执行 `deployment/mysql/init/nacos-schema.sql`
   - 验证初始化结果

## 🛠️ 技术实现

### 核心函数

```powershell
function Initialize-Databases {
    # 1. 启动MySQL和Redis
    # 2. 等待MySQL就绪
    # 3. 检测nacos数据库状态
    # 4. 自动初始化（如果需要）
    # 5. 验证初始化结果
}
```

### 集成点

在 `deploy.ps1` 的 `Main` 函数中：

```powershell
function Main {
    Test-Requirements
    Test-ProjectFiles
    Initialize-Environment
    
    if (-not $SkipBuild) {
        Build-DockerImages
    }
    
    Initialize-Databases  # ← 自动数据库初始化
    
    Start-Services
    Wait-ServicesReady
    Test-Deployment
}
```

## 📊 优势

### 相比手动操作

| 操作 | 手动方式 | 自动化方式 |
|------|---------|-----------|
| 检测数据库状态 | 需要手动执行命令 | ✅ 自动检测 |
| 初始化数据库 | 需要手动执行脚本 | ✅ 自动初始化 |
| 验证初始化结果 | 需要手动检查 | ✅ 自动验证 |
| 错误处理 | 需要手动排查 | ✅ 自动错误处理 |
| 部署时间 | 5-10分钟 | ✅ 3-5分钟 |

### 用户体验

- ✅ **零手动操作**: 一键部署，自动处理所有初始化
- ✅ **智能检测**: 只在需要时初始化，避免重复操作
- ✅ **错误提示**: 清晰的错误信息和修复建议
- ✅ **进度显示**: 实时显示部署进度和状态

## 🔧 故障处理

### 如果自动初始化失败

1. **查看日志**
   ```powershell
   docker logs ioedream-mysql
   ```

2. **手动初始化**
   ```powershell
   .\scripts\init-nacos-database.ps1
   ```

3. **检查SQL脚本**
   ```powershell
   Test-Path deployment\mysql\init\nacos-schema.sql
   ```

### 常见问题

**Q: 为什么数据库初始化失败？**
- A: 检查MySQL容器是否正常运行
- A: 检查SQL脚本是否存在
- A: 检查MySQL连接参数是否正确

**Q: 如何跳过自动初始化？**
- A: 使用 `-SkipInit` 参数（仅限 `deploy-auto.ps1`）

**Q: 如何强制重新初始化？**
- A: 使用 `-Force` 参数（仅限 `deploy-auto.ps1`）

## 📚 相关文档

- [Nacos启动问题分析](./NACOS_STARTUP_ISSUE_ROOT_CAUSE_ANALYSIS.md)
- [Docker部署全局分析](./DOCKER_DEPLOYMENT_ROOT_CAUSE_ANALYSIS.md)
- [快速修复指南](../../QUICK_FIX_NACOS.md)

---

**更新日期**: 2025-01-30  
**功能状态**: ✅ 已实现并集成到部署脚本
