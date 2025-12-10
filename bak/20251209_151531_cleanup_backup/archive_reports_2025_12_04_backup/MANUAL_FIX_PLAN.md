# 手动修复计划 - 架构合规性修复

> **修复方式**: 手动逐个修复，禁止使用脚本  
> **开始时间**: 2025-12-03  
> **状态**: 进行中

---

## 📊 修复进度汇总

### ✅ 已修复（5个）

1. **@Repository违规修复**: 5个文件（通过脚本已修复）
   - `InterlockLogDao.java` ✅
   - `BiometricRecordDao.java` ✅
   - `BiometricTemplateDao.java` ✅
   - `NacosConfigHistoryDao.java` ✅
   - `NacosConfigItemDao.java` ✅

### ⏳ 待手动修复文件

#### @Repository违规（24个文件）

**ioedream-attendance-service (2个)**:
- [ ] `LeaveApplicationDao.java` - 检查中（已使用@Mapper，可能只是注释中提到）
- [ ] `OvertimeApplicationDao.java` - 检查中（已使用@Mapper，可能只是注释中提到）

**ioedream-common-core (13个)**:
- [ ] `UserDao.java` - 检查中（已使用@Mapper）
- [ ] `UserSessionDao.java` - 检查中（已使用@Mapper）
- [ ] `AlertRuleDao.java` - 检查中（已使用@Mapper）
- [ ] `NotificationDao.java` - 检查中（已使用@Mapper）
- [ ] `SystemLogDao.java` - 检查中（已使用@Mapper）
- [ ] `SystemMonitorDao.java` - 检查中（已使用@Mapper）
- [ ] `NotificationRecordDao.java` - 检查中（已使用@Mapper）
- [ ] `OperationLogDao.java` - 检查中（已使用@Mapper）
- [ ] `EmployeeDao.java` - 待检查
- [ ] `ApprovalRecordDao.java` - 待检查
- [ ] `ApprovalWorkflowDao.java` - 待检查

**ioedream-common-service (12个)**:
- [ ] `UserDao.java` - 待检查
- [ ] `UserSessionDao.java` - 待检查
- [ ] `AlertRuleDao.java` - 待检查
- [ ] `NotificationDao.java` - 待检查
- [ ] `SystemLogDao.java` - 待检查
- [ ] `SystemMonitorDao.java` - 待检查
- [ ] `NotificationRecordDao.java` - 待检查
- [ ] `OperationLogDao.java` - 待检查
- [ ] `EmployeeDao.java` - 待检查

**microservices-common (2个)**:
- [ ] `ApprovalRecordDao.java` - 待检查
- [ ] `ApprovalWorkflowDao.java` - 待检查

#### @Autowired违规（37个文件）

**说明**: 检查脚本可能在注释中检测到了@Autowired字样，需要逐个验证实际代码是否使用了@Autowired。

**ioedream-common-core (18个)**:
- [ ] `AlertController.java` - 待验证（已使用@Resource）
- [ ] `SystemHealthController.java` - 待验证
- [ ] `EmailNotificationManager.java` - 待验证（已使用@Resource）
- [ ] `LogManagementManager.java` - 待验证
- [ ] `NotificationManager.java` - 待验证
- [ ] `PerformanceMonitorManager.java` - 待验证
- [ ] `SmsNotificationManager.java` - 待验证
- [ ] `SystemMonitorManager.java` - 待验证
- [ ] `WebhookNotificationManager.java` - 待验证
- [ ] `WechatNotificationManager.java` - 待验证
- [ ] `AlertServiceImpl.java` - 待验证（已使用@Resource）
- [ ] `SystemHealthServiceImpl.java` - 待验证
- [ ] `OperationLogManagerImpl.java` - 待验证
- [ ] `OperationLogServiceImpl.java` - 待验证
- [ ] `CacheController.java` - 待验证
- [ ] `EmployeeController.java` - 待验证
- [ ] `EmployeeManager.java` - 待验证
- [ ] `EmployeeServiceImpl.java` - 待验证

**ioedream-common-service (19个)**:
- [ ] `ConfigManagementServiceImpl.java` - 待验证（已使用@Resource）
- [ ] 其他18个文件 - 待验证

---

## 🔍 检查策略

### 第一步：验证实际违规情况

对于每个被标记为违规的文件：
1. 打开文件
2. 检查import语句中是否有`import org.springframework.stereotype.Repository;`或`import org.springframework.beans.factory.annotation.Autowired;`
3. 检查代码中是否有`@Repository`或`@Autowired`注解（排除注释）
4. 如果只是注释中提到，标记为"误报"，无需修复
5. 如果确实使用了违规注解，手动修复

### 第二步：手动修复

对于确实违规的文件：
1. **@Repository → @Mapper**:
   - 删除`import org.springframework.stereotype.Repository;`
   - 添加`import org.apache.ibatis.annotations.Mapper;`（如果不存在）
   - 将`@Repository`替换为`@Mapper`

2. **@Autowired → @Resource**:
   - 删除`import org.springframework.beans.factory.annotation.Autowired;`
   - 添加`import jakarta.annotation.Resource;`（如果不存在）
   - 将`@Autowired`替换为`@Resource`

---

## 📋 修复顺序

### 优先级1：@Repository违规（更明确）
1. 先检查ioedream-attendance-service的2个文件
2. 然后检查ioedream-common-core的13个文件
3. 再检查ioedream-common-service的12个文件
4. 最后检查microservices-common的2个文件

### 优先级2：@Autowired违规（需要验证）
1. 逐个验证是否真的违规
2. 只修复确实违规的文件

---

## ✅ 修复验证

每修复一个文件后：
1. 检查import语句是否正确
2. 检查注解是否正确
3. 检查代码是否能正常编译
4. 更新修复进度

---

**更新时间**: 2025-12-03 20:50

