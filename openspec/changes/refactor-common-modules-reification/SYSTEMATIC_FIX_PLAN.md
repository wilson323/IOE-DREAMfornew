# 编译错误系统性修复计划

## 📊 错误分类统计

### 1. Auth 相关类缺失（30+ 错误）

**缺失的类**：

- `net.lab1024.sa.common.auth.dao.UserDao` ❌
- `net.lab1024.sa.common.auth.dao.UserSessionDao` ❌
- `net.lab1024.sa.common.auth.manager.AuthManager` ❌
- `net.lab1024.sa.common.auth.service.AuthService` ❌
- `net.lab1024.sa.common.auth.domain.dto.LoginRequestDTO` ❌
- `net.lab1024.sa.common.auth.domain.dto.RefreshTokenRequestDTO` ❌
- `net.lab1024.sa.common.auth.domain.vo.LoginResponseVO` ❌
- `net.lab1024.sa.common.auth.domain.vo.UserInfoVO` ❌
- `net.lab1024.sa.common.security.entity.UserEntity` ❌

**实际位置**：

- 这些类可能：
  1. 在 `ioedream-common-service` 中，但包路径不同
  2. 在 `microservices-common` 中，但包路径不同
  3. 根本不存在，需要创建

### 2. Monitoring 相关类缺失（10+ 错误）

**缺失的类**：

- `net.lab1024.sa.common.monitoring.AlertManager` ❌
- `net.lab1024.sa.common.monitoring.EnterpriseMonitoringManager` ❌

**实际位置**：

- 这些类可能在 `ioedream-common-service` 中，但包路径是 `net.lab1024.sa.common.monitor.*`

### 3. Notification 相关类缺失（5+ 错误）

**缺失的类**：

- `net.lab1024.sa.common.notification.domain.entity.NotificationConfigEntity` ❌

**实际位置**：

- 这个类可能在 `ioedream-common-service` 中，但包路径不同

## 🎯 修复策略

### 策略1：修复导入路径（优先）

**步骤**：

1. 搜索这些类的实际位置
2. 修复所有导入路径
3. 确保编译通过

### 策略2：创建缺失的类（如果不存在）

**步骤**：

1. 如果类不存在，创建它们
2. 在正确的模块中创建
3. 确保符合架构规范

## 📋 立即执行计划

### 阶段1：查找实际位置（5分钟）

1. 搜索 `UserDao`、`UserSessionDao`、`AuthManager`、`AuthService` 的实际位置
2. 搜索 `LoginRequestDTO`、`LoginResponseVO`、`UserInfoVO` 的实际位置
3. 搜索 `UserEntity` 的实际位置
4. 搜索 `AlertManager`、`EnterpriseMonitoringManager` 的实际位置
5. 搜索 `NotificationConfigEntity` 的实际位置

### 阶段2：修复导入路径（10分钟）

1. 修复 `AuthServiceImpl.java` 的导入路径
2. 修复 `AuthController.java` 的导入路径
3. 修复 `LoginController.java` 的导入路径
4. 修复 `AlertAutoConfiguration.java` 的导入路径
5. 修复 `NotificationConfigDao.java` 的导入路径

### 阶段3：创建缺失的类（如果需要，20分钟）

1. 如果类不存在，在正确的模块中创建它们
2. 确保符合架构规范
3. 添加必要的依赖

### 阶段4：验证编译（5分钟）

1. 运行 `mvn clean compile -DskipTests`
2. 检查是否还有编译错误
3. 修复剩余错误

## ⚠️ 关键发现

从代码分析发现：

- `UserEntity` 的导入路径是 `net.lab1024.sa.common.security.entity.UserEntity`
- 但实际可能在其他位置（如 `net.lab1024.sa.common.organization.entity.UserEntity`）
- 需要系统性地查找并修复所有导入路径
