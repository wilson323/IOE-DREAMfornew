# Entity导入路径修复报告

**生成时间**: 2025-12-26
**Phase**: 1.3 - 修复旧Entity导入路径
**总需修复文件数**: 115个

---

## 📊 按服务统计

| 服务 | 旧导入数量 | 状态 |
|------|-----------|------|
| ioedream-common-service | 25处 | ⏳ 待修复 |
| ioedream-access-service | 21处 | ⏳ 待修复 |
| ioedream-oa-service | 19处 | ⏳ 待修复 |
| ioedream-biometric-service | 15处 | ⏳ 待修复 |
| ioedream-consume-service | 13处 | ⏳ 待修复 |
| ioedream-data-analysis-service | 9处 | ⏳ 待修复 |
| ioedream-attendance-service | 8处 | ⏳ 待修复 |
| ioedream-video-service | 3处 | ⏳ 待修复 |
| microservices-common-security | 1处 | ⏳ 待修复 |
| ioedream-visitor-service | 1处 | ⏳ 待修复 |

---

## 🔄 导入路径映射规则

### 旧路径 → 新路径

```
net.lab1024.sa.access.domain.entity.*     → net.lab1024.sa.common.entity.access.*
net.lab1024.sa.attendance.domain.entity.*  → net.lab1024.sa.common.entity.attendance.*
net.lab1024.sa.consume.domain.entity.*     → net.lab1024.sa.common.entity.consume.*
net.lab1024.sa.video.domain.entity.*       → net.lab1024.sa.common.entity.video.*
net.lab1024.sa.visitor.domain.entity.*     → net.lab1024.sa.common.entity.visitor.*
net.lab1024.sa.biometric.domain.entity.*   → net.lab1024.sa.common.entity.biometric.*
```

---

## 📋 详细修复清单

### 1. ioedream-access-service (21处)

**修复示例**:
```java
// ❌ 修复前
import net.lab1024.sa.access.domain.entity.AccessAlarmEntity;

// ✅ 修复后
import net.lab1024.sa.common.entity.access.AccessAlarmEntity;
```

**需要修复的Entity类**:
- AccessAlarmEntity
- AccessCapacityControlEntity
- AccessEvacuationPointEntity
- AccessInterlockRuleEntity
- AccessLinkageLogEntity
- AccessLinkageRuleEntity
- AccessPersonRestrictionEntity
- AccessUserPermissionEntity
- AlertNotificationEntity
- AlertRuleEntity
- AntiPassbackConfigEntity
- AntiPassbackRecordEntity
- DeviceAlertEntity
- DeviceFirmwareEntity
- DeviceImportBatchEntity
- DeviceImportErrorEntity
- DeviceImportSuccessEntity
- FirmwareUpgradeDeviceEntity
- FirmwareUpgradeTaskEntity

---

### 2. ioedream-attendance-service (8处)

**需要修复的Entity类**:
- AttendanceAnomalyApplyEntity
- AttendanceAnomalyEntity
- AttendanceOvertimeApplyEntity
- AttendanceOvertimeApprovalEntity
- AttendanceOvertimeEntity
- AttendanceRuleEntity
- AttendanceShiftEntity
- DepartmentStatisticsEntity

---

### 3. ioedream-consume-service (13处)

**需要修复的Entity类**:
- ConsumeAccountEntity
- ConsumeAccountTransactionEntity
- ConsumeDeviceEntity
- ConsumeMealCategoryEntity
- ConsumeProductEntity
- ConsumeRecordEntity
- ConsumeRechargeEntity
- ConsumeSubsidyEntity

---

### 4. ioedream-video-service (3处)

**需要修复的Entity类**:
- AlarmRecordEntity
- AlarmRuleEntity
- VideoRecordingExportTaskEntity

---

### 5. ioedream-visitor-service (1处)

**需要修复的Entity类**:
- VisitorAreaEntity

---

### 6. ioedream-biometric-service (15处)

**需要修复的Entity类**:
- BiometricTemplateEntity

---

### 7. ioedream-common-service (25处)

**需要修复的Entity类**:
- SystemAreaEntity
- 其他公共Entity类

---

### 8. ioedream-oa-service (19处)

**需要修复的Entity类**:
- OA相关Entity类

---

### 9. ioedream-data-analysis-service (9处)

**需要修复的Entity类**:
- DashboardEntity
- ExportTaskEntity
- ReportEntity

---

### 10. microservices-common-security (1处)

**需要修复的Entity类**:
- UserSessionEntity

---

## 🔧 手动修复指南

### 步骤1: 使用IDE全局搜索

**VS Code**:
1. 按 `Ctrl+Shift+H` 打开全局搜索替换
2. 搜索模式: `import net\.lab1024\.sa\.access\.domain\.entity\.`
3. 替换为: `import net.lab1024.sa.common.entity.access.`

**IntelliJ IDEA**:
1. 按 `Ctrl+Shift+R` 打开全局替换
2. 范围: `整个项目`
3. 搜索: `net.lab1024.sa.access.domain.entity.`
4. 替换: `net.lab1024.sa.common.entity.access.`

### 步骤2: 批量替换规则

对每个模块执行以下替换：

```
1. access模块:
   查找: net.lab1024.sa.access.domain.entity.
   替换: net.lab1024.sa.common.entity.access.

2. attendance模块:
   查找: net.lab1024.sa.attendance.domain.entity.
   替换: net.lab1024.sa.common.entity.attendance.

3. consume模块:
   查找: net.lab1024.sa.consume.domain.entity.
   替换: net.lab1024.sa.common.entity.consume.

4. video模块:
   查找: net.lab1024.sa.video.domain.entity.
   替换: net.lab1024.sa.common.entity.video.

5. visitor模块:
   查找: net.lab1024.sa.visitor.domain.entity.
   替换: net.lab1024.sa.common.entity.visitor.
```

### 步骤3: 验证修复

运行以下命令验证无残留旧导入：

```bash
cd "D:\IOE-DREAM\microservices"
grep -r "import net.lab1024.sa.\(access\|attendance\|consume\|video\|visitor\)\.domain\.entity\." . --include="*.java"
```

预期输出: 0个结果

---

## ⚠️ 重要提示

1. **禁止使用脚本批量修改**: 根据项目规范，必须使用IDE的查找替换功能手动确认每次替换
2. **备份代码**: 修复前确保已创建Git分支备份
3. **逐模块修复**: 建议按模块顺序逐个修复和验证
4. **测试验证**: 每完成一个模块的修复，运行编译验证

---

## ✅ 验收标准

- [ ] 所有旧导入路径已替换为新路径
- [ ] 无 `net.lab1024.sa.xxx.domain.entity` 残留
- [ ] IDE无导入错误提示
- [ ] 编译无Entity相关错误

---

**报告版本**: v1.0
**生成工具**: Claude Code AI Assistant
