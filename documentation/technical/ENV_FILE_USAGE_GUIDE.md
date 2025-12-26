# IOE-DREAM 环境变量文件使用指南

**版本**: v1.0.0  
**更新日期**: 2025-12-15  
**适用范围**: 所有微服务和启动脚本

---

## 📋 核心原则

### 黄金法则

1. **单一数据源**: 所有环境变量配置统一从 `D:\IOE-DREAM\.env` 文件读取
2. **全局一致性**: 所有启动脚本必须使用 `scripts/load-env.ps1` 加载环境变量
3. **禁止硬编码**: 禁止在脚本中硬编码环境变量值
4. **统一管理**: 所有配置修改只需更新 `.env` 文件

---

## 🔧 环境变量文件位置

**标准位置**: `D:\IOE-DREAM\.env`

**文件格式**:
```bash
# MySQL数据库连接配置
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_DATABASE=ioedream
MYSQL_USERNAME=root
MYSQL_PASSWORD=123456

# Redis缓存配置
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=redis123

# Nacos配置中心
NACOS_SERVER_ADDR=127.0.0.1:8848
NACOS_NAMESPACE=dev
NACOS_GROUP=IOE-DREAM
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos
```

---

## 📝 使用方法

### 1. 在PowerShell脚本中使用

**标准方式**（推荐）:
```powershell
# 在脚本开头加载环境变量
$loadEnvScript = Join-Path $PSScriptRoot "load-env.ps1"
if (Test-Path $loadEnvScript) {
    & $loadEnvScript -Silent  # -Silent 参数不显示加载过程
}
```

**示例**:
```powershell
# scripts/my-script.ps1
$ErrorActionPreference = "Stop"

# 加载环境变量
$loadEnvScript = Join-Path $PSScriptRoot "load-env.ps1"
& $loadEnvScript -Silent

# 现在可以使用环境变量
Write-Host "MySQL Host: $env:MYSQL_HOST"
Write-Host "Redis Host: $env:REDIS_HOST"
```

### 2. 在命令行中使用

**方式1: 使用load-env.ps1**
```powershell
# 加载环境变量到当前PowerShell会话
. .\scripts\load-env.ps1

# 然后启动服务
cd microservices\ioedream-common-service
mvn spring-boot:run
```

**方式2: 使用set-env-from-file.ps1**
```powershell
# 加载环境变量
.\scripts\set-env-from-file.ps1

# 启动服务
cd microservices\ioedream-common-service
mvn spring-boot:run
```

### 3. 在启动脚本中自动加载

**已更新的脚本**（自动从.env加载）:
- ✅ `scripts/start-all-services.ps1`
- ✅ `scripts/verify-and-start-services.ps1`
- ✅ `scripts/set-env-from-file.ps1`

**使用示例**:
```powershell
# 直接运行，自动从.env加载环境变量
.\scripts\start-all-services.ps1
```

---

## 🔍 环境变量清单

### MySQL配置

| 变量名 | 说明 | 默认值 | 必需 |
|--------|------|--------|------|
| `MYSQL_HOST` | 数据库主机 | `127.0.0.1` | ✅ |
| `MYSQL_PORT` | 数据库端口 | `3306` | ✅ |
| `MYSQL_DATABASE` | 数据库名称 | `ioedream` | ✅ |
| `MYSQL_USERNAME` | 数据库用户名 | `root` | ✅ |
| `MYSQL_PASSWORD` | 数据库密码 | `123456` | ✅ |
| `MYSQL_ROOT_PASSWORD` | Root密码 | `123456` | ⚠️ |

### Redis配置

| 变量名 | 说明 | 默认值 | 必需 |
|--------|------|--------|------|
| `REDIS_HOST` | Redis主机 | `127.0.0.1` | ✅ |
| `REDIS_PORT` | Redis端口 | `6379` | ✅ |
| `REDIS_PASSWORD` | Redis密码 | `redis123` | ⚠️ |
| `REDIS_DATABASE` | 数据库编号 | `0` | ⚠️ |
| `REDIS_TIMEOUT` | 连接超时 | `3000` | ⚠️ |

### Nacos配置

| 变量名 | 说明 | 默认值 | 必需 |
|--------|------|--------|------|
| `NACOS_SERVER_ADDR` | Nacos服务器地址 | `127.0.0.1:8848` | ✅ |
| `NACOS_NAMESPACE` | 命名空间 | `dev` | ✅ |
| `NACOS_GROUP` | 配置组 | `IOE-DREAM` | ✅ |
| `NACOS_USERNAME` | 用户名 | `nacos` | ✅ |
| `NACOS_PASSWORD` | 密码 | `nacos` | ✅ |
| `NACOS_AUTH_TOKEN` | 认证Token | Base64编码 | ⚠️ |

### Spring配置

| 变量名 | 说明 | 默认值 | 必需 |
|--------|------|--------|------|
| `SPRING_PROFILES_ACTIVE` | 激活的Profile | `dev` | ⚠️ |
| `GATEWAY_URL` | 网关URL | `http://localhost:8080` | ⚠️ |

---

## ✅ 验证环境变量

### 检查环境变量是否加载

```powershell
# 加载环境变量
. .\scripts\load-env.ps1

# 检查关键环境变量
Write-Host "MySQL Host: $env:MYSQL_HOST"
Write-Host "Redis Host: $env:REDIS_HOST"
Write-Host "Nacos Server: $env:NACOS_SERVER_ADDR"
```

### 验证脚本

```powershell
# 运行验证脚本（自动加载环境变量）
.\scripts\verify-and-start-services.ps1
```

---

## 🔒 安全注意事项

### 1. .env文件安全

- ✅ `.env` 文件已在 `.gitignore` 中，不会被提交到Git
- ⚠️ 生产环境应使用加密的配置管理（如Nacos加密配置）
- ⚠️ 不要在 `.env` 文件中存储生产环境的真实密码

### 2. 敏感信息处理

**开发环境**:
- 可以使用 `.env` 文件存储配置
- 密码可以明文存储（仅限开发环境）

**生产环境**:
- 使用环境变量或配置中心
- 使用加密配置
- 使用密钥管理服务（如Kubernetes Secrets）

---

## 🛠️ 故障排查

### 问题1: 环境变量未加载

**症状**: 服务启动时提示配置缺失

**解决方法**:
```powershell
# 1. 检查.env文件是否存在
Test-Path D:\IOE-DREAM\.env

# 2. 手动加载环境变量
. .\scripts\load-env.ps1

# 3. 验证环境变量
$env:MYSQL_HOST
```

### 问题2: 环境变量值不正确

**症状**: 服务连接失败

**解决方法**:
```powershell
# 1. 检查.env文件内容
Get-Content D:\IOE-DREAM\.env

# 2. 修改.env文件
notepad D:\IOE-DREAM\.env

# 3. 重新加载环境变量
. .\scripts\load-env.ps1
```

### 问题3: 脚本找不到.env文件

**症状**: 脚本提示找不到.env文件

**解决方法**:
```powershell
# 1. 确认.env文件位置
Get-Item D:\IOE-DREAM\.env

# 2. 检查脚本的项目根目录检测逻辑
# 脚本会自动向上查找包含.env的目录
```

---

## 📚 相关文档

- [环境变量配置文档](../deployment/ENVIRONMENT_VARIABLES.md)
- [全局配置一致性规范](../deployment/docker/GLOBAL_CONFIG_CONSISTENCY.md)
- [启动脚本使用指南](./START_DEV_GUIDE.md)

---

## ✅ 检查清单

**开发前检查**:
- [ ] `.env` 文件存在且格式正确
- [ ] 所有必需的环境变量已设置
- [ ] 启动脚本使用 `load-env.ps1` 加载环境变量

**启动前检查**:
- [ ] 环境变量已加载（运行 `load-env.ps1`）
- [ ] 环境变量值正确（检查关键变量）
- [ ] 基础设施服务已启动（MySQL、Redis、Nacos）

**部署前检查**:
- [ ] 生产环境配置已更新
- [ ] 敏感信息已加密
- [ ] 配置文档已更新

---

**文档维护**: IOE-DREAM架构团队  
**最后更新**: 2025-12-15

