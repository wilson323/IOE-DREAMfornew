# 考勤服务启动问题修复指南

> **问题日期**: 2025-12-14  
> **问题类型**: 配置错误  
> **状态**: ✅ 已提供解决方案

---

## 📋 问题描述

启动考勤服务时遇到两个主要错误：

### 1. MySQL连接失败

```
java.sql.SQLException: Access denied for user 'root'@'172.18.0.1' (using password: NO)
```

**原因分析**:
- 环境变量 `MYSQL_PASSWORD` 未设置
- 配置文件 `application.yml` 中默认值为空：`password: ${MYSQL_PASSWORD:}`
- MySQL服务需要密码认证，但应用未提供密码

### 2. Nacos认证失败

```
com.alibaba.nacos.api.exception.NacosException: http error, code=403,msg=user not found!
dataId=ioedream-attendance-service-docker.yaml,group=IOE-DREAM,tenant=dev
```

**原因分析**:
- Nacos配置中心启用了认证
- 环境变量 `NACOS_PASSWORD` 未设置或配置错误
- 配置文件中的默认值为空：`password: ${NACOS_PASSWORD:}`

---

## ✅ 解决方案

### 方案1: 使用修复脚本（推荐）

1. **运行修复脚本**:
   ```powershell
   cd D:\IOE-DREAM
   .\scripts\fix-attendance-service-config.ps1
   ```

2. **按提示输入MySQL密码**

3. **执行生成的环境变量脚本**:
   ```powershell
   .\scripts\set-attendance-env.ps1
   ```

4. **启动服务**:
   ```powershell
   cd microservices\ioedream-attendance-service
   mvn spring-boot:run
   ```

### 方案2: 手动设置环境变量

在PowerShell中设置环境变量：

```powershell
# MySQL配置
$env:MYSQL_HOST = "127.0.0.1"
$env:MYSQL_PORT = "3306"
$env:MYSQL_DATABASE = "ioedream"
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = "你的MySQL密码"

# Nacos配置
$env:NACOS_SERVER_ADDR = "127.0.0.1:8848"
$env:NACOS_NAMESPACE = "dev"
$env:NACOS_GROUP = "IOE-DREAM"
$env:NACOS_USERNAME = "nacos"
$env:NACOS_PASSWORD = "nacos"  # 根据实际Nacos配置修改

# Redis配置（如果需要）
$env:REDIS_HOST = "127.0.0.1"
$env:REDIS_PORT = "6379"
$env:REDIS_PASSWORD = ""  # 如果Redis设置了密码，填写密码

# 启动服务
cd microservices\ioedream-attendance-service
mvn spring-boot:run
```

### 方案3: 创建.env.development文件（持久化配置）

在项目根目录创建 `.env.development` 文件：

```properties
# MySQL配置
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_DATABASE=ioedream
MYSQL_USERNAME=root
MYSQL_PASSWORD=你的MySQL密码

# Nacos配置
NACOS_SERVER_ADDR=127.0.0.1:8848
NACOS_NAMESPACE=dev
NACOS_GROUP=IOE-DREAM
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos

# Redis配置
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=
```

然后在启动前加载环境变量：

```powershell
# 加载.env.development文件
Get-Content .env.development | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        Set-Item -Path "env:$name" -Value $value
    }
}

# 启动服务
cd microservices\ioedream-attendance-service
mvn spring-boot:run
```

---

## 🔍 验证步骤

### 1. 验证MySQL连接

```powershell
# 测试MySQL连接
mysql -h 127.0.0.1 -P 3306 -u root -p
# 输入密码后，如果成功连接，说明MySQL服务正常
```

### 2. 验证Nacos连接

```powershell
# 访问Nacos控制台
Start-Process "http://127.0.0.1:8848/nacos"
# 使用用户名密码登录（默认: nacos/nacos）
```

### 3. 验证服务启动

启动服务后，检查日志中是否还有错误：

```powershell
# 查看服务日志
# 应该看到类似以下信息：
# "Started AttendanceServiceApplication in X.XXX seconds"
```

---

## 🛠️ 常见问题排查

### Q1: MySQL密码错误

**症状**: `Access denied for user 'root'@'xxx' (using password: YES)`

**解决方案**:
1. 确认MySQL密码是否正确
2. 检查MySQL用户权限：
   ```sql
   SELECT user, host FROM mysql.user WHERE user='root';
   ```
3. 如果忘记密码，重置MySQL root密码

### Q2: Nacos用户不存在

**症状**: `user not found!`

**解决方案**:
1. 检查Nacos是否启用了认证：
   - 访问 `http://127.0.0.1:8848/nacos`
   - 查看是否需要登录
2. 如果启用了认证，确认用户名密码是否正确
3. 如果未启用认证，在配置中移除用户名密码：
   ```yaml
   spring:
     cloud:
       nacos:
         config:
           username: ${NACOS_USERNAME:}  # 空值表示不使用认证
           password: ${NACOS_PASSWORD:}
   ```

### Q3: 环境变量未生效

**症状**: 设置了环境变量但服务仍然报错

**解决方案**:
1. 确认环境变量在启动服务的同一PowerShell会话中设置
2. 验证环境变量：
   ```powershell
   echo $env:MYSQL_PASSWORD
   echo $env:NACOS_PASSWORD
   ```
3. 如果使用IDE启动，需要在IDE中配置环境变量

---

## 📚 相关文档

- [环境变量配置文档](../deployment/ENVIRONMENT_VARIABLES.md)
- [Nacos认证配置修复](../deployment/docker/NACOS_AUTH_FIX_COMPLETE.md)
- [全局配置一致性规范](../deployment/docker/GLOBAL_CONFIG_CONSISTENCY.md)

---

## 🔄 后续优化建议

1. **统一环境变量管理**: 使用 `.env.development` 文件统一管理开发环境变量
2. **配置验证脚本**: 在启动前自动验证所有必需的环境变量
3. **配置文档更新**: 在README中明确说明必需的环境变量
4. **Docker Compose支持**: 使用Docker Compose启动时，环境变量自动从 `.env` 文件加载

---

**维护人**: IOE-DREAM 架构团队  
**最后更新**: 2025-12-14
