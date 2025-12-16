# IOE-DREAM 微服务编译错误修复计划

**日期**: 2025-01-30  
**状态**: 进行中

---

## 📋 问题分析

### 编译错误分类

根据`compile-errors.txt`分析，33个编译错误分为以下几类：

#### 1. 导入错误（Map、List）- 5个错误
- `SchedulerService.java:87` - 缺少Map导入
- `AuditService.java:65,70` - 缺少List导入（2个）
- `NotificationSendDTO.java:19,74` - 缺少Map导入（2个）

#### 2. 缺失的Manager类 - 6个错误
- `NotificationManager.java` - 缺少6个NotificationManager子类：
  - EmailNotificationManager
  - SmsNotificationManager
  - WebSocketNotificationManager
  - WechatNotificationManager
  - DingTalkNotificationManager
  - WebhookNotificationManager

#### 3. 缺失的Entity类 - 3个错误
- `PerformanceMonitorManager.java:144,152` - 缺少AlertEntity和NotificationEntity导入

#### 4. 缺失的RBAC类 - 10个错误
- `RbacRoleManager.java` - 缺少：
  - RbacResourceDao
  - UserRoleDao
  - RoleResourceDao
  - RbacResourceEntity
  - UserRoleEntity
  - RoleResourceEntity

#### 5. Sa-Token相关错误 - 6个错误
- `AuthServiceImpl.java` - 缺少SaTokenInfo和ZXing相关类（但项目使用Spring Security）

#### 6. SmartRedisUtil错误 - 2个错误
- `OperationLogManager.java` - 缺少SmartRedisUtil

---

## 🔧 修复策略

### 策略1：修复导入错误（快速修复）

**文件位置**（根据编译错误路径）：
- `ioedream-common-service/src/main/java/net/lab1024/sa/common/scheduler/service/SchedulerService.java`
- `ioedream-common-service/src/main/java/net/lab1024/sa/common/audit/service/AuditService.java`
- `ioedream-common-service/src/main/java/net/lab1024/sa/common/notification/domain/dto/NotificationSendDTO.java`

**修复方法**：添加`import java.util.Map;`和`import java.util.List;`

### 策略2：处理缺失的类

#### 2.1 NotificationManager子类
**方案A**：移除对子类的引用（推荐）
- NotificationManager在microservices-common-monitor中已存在
- 使用策略模式，不需要具体的子类

**方案B**：创建子类（如果需要）
- 在ioedream-common-service中创建具体的实现类

#### 2.2 RBAC相关类
**检查**：这些类应该在`microservices-common/src/main/java/net/lab1024/sa/common/rbac/`中
- 如果存在，修复导入路径
- 如果不存在，需要创建

#### 2.3 AlertEntity和NotificationEntity
**检查**：这些类应该在`microservices-common-monitor`中
- 如果存在，修复导入路径
- 如果不存在，需要创建

#### 2.4 Sa-Token相关
**方案**：移除Sa-Token引用（项目使用Spring Security）
- 检查AuthServiceImpl是否真的使用了Sa-Token
- 如果使用了，替换为Spring Security实现

#### 2.5 SmartRedisUtil
**方案A**：替换为Spring Data Redis（推荐）
- 使用RedisTemplate或RedisUtil

**方案B**：创建SmartRedisUtil适配器
- 如果必须保留，创建适配器类

---

## 📝 修复步骤

### 步骤1：验证文件是否存在

```powershell
# 检查编译错误中提到的文件是否存在
Test-Path "microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\scheduler\service\SchedulerService.java"
Test-Path "microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\audit\service\AuditService.java"
# ... 其他文件
```

### 步骤2：修复导入错误

如果文件存在，添加缺失的导入：
```java
import java.util.Map;
import java.util.List;
```

### 步骤3：处理缺失的类

根据策略修复或创建缺失的类。

### 步骤4：验证编译

重新编译验证修复效果。

---

## ⚠️ 注意事项

1. **编译错误文件可能过时**：compile-errors.txt是2025-12-10的，可能已经部分修复
2. **文件位置可能不同**：某些文件可能在microservices-common模块中
3. **需要重新编译验证**：修复后需要重新编译确认

---

## 📊 修复进度

- [ ] 验证文件存在性
- [ ] 修复导入错误
- [ ] 处理缺失的Manager类
- [ ] 处理缺失的Entity类
- [ ] 处理RBAC类
- [ ] 移除Sa-Token引用
- [ ] 修复SmartRedisUtil
- [ ] 验证编译通过

