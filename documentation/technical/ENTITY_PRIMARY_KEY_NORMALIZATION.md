# 实体类主键命名规范化修复报告

**修复时间**: 2025-12-09  
**修复状态**: ✅ **进行中**  
**修复范围**: 统一所有实体类主键字段命名为 `id`

---

## 📋 修复摘要

本次修复统一了实体类主键命名规范，将所有实体类的主键字段统一命名为 `id`，通过 `@TableId(value="xxx_id")` 映射到数据库列，避免方法引用错误。

---

## ✅ 已修复的实体类（P1优先级）

### 1. ✅ AreaEntity
- **修复前**: `private Long areaId;`
- **修复后**: `private Long id;` + `@TableId(value = "area_id")`
- **修复文件**: `microservices-common/src/main/java/net/lab1024/sa/common/organization/entity/AreaEntity.java`
- **引用修复**: 
  - `AreaUnifiedServiceImpl.java`: `AreaEntity::getAreaId` → `AreaEntity::getId` (2处)

### 2. ✅ DeviceEntity
- **修复前**: `private Long deviceId;`
- **修复后**: `private Long id;` + `@TableId(value = "device_id")`
- **修复文件**: `microservices-common/src/main/java/net/lab1024/sa/common/organization/entity/DeviceEntity.java`
- **引用修复**:
  - `DeviceHealthMonitor.java`: `device.getDeviceId()` → `device.getId()` (4处)
  - `DeviceStatusManager.java`: `device.getDeviceId()` → `device.getId()` (1处)
  - `AccessDeviceServiceImpl.java`: `device.getDeviceId()` → `device.getId()` (7处)
  - `AccessDeviceServiceImpl.java`: `DeviceEntity::getDeviceId` → `DeviceEntity::getId` (2处)

---

## ✅ 已修复的实体类（P2优先级）

### 3. ✅ SystemConfigEntity
- **修复前**: `private Long configId;`
- **修复后**: `private Long id;` + `@TableId(value = "config_id")`
- **修复文件**: `microservices-common/src/main/java/net/lab1024/sa/common/system/domain/entity/SystemConfigEntity.java`
- **引用修复**:
  - `SystemServiceImpl.java`: `config.getConfigId()` → `config.getId()` (1处)
  - `SystemConfigBatchManager.java`: `config.getConfigId()` → `config.getId()` (3处)

### 4. ✅ ThemeTemplateEntity
- **修复前**: `private Long templateId;`
- **修复后**: `private Long id;` + `@TableId(value = "template_id")`
- **修复文件**: `microservices-common/src/main/java/net/lab1024/sa/common/theme/entity/ThemeTemplateEntity.java`
- **引用修复**:
  - `ThemeTemplateManager.java`: `ThemeTemplateEntity::getTemplateId` → `ThemeTemplateEntity::getId` (1处)
  - `ThemeTemplateManager.java`: `template.getTemplateId()` → `template.getId()` (1处)
  - `ThemeTemplateManager.java`: `original.getTemplateId()` → `original.getId()` (1处)

### 5. ✅ UserThemeConfigEntity
- **修复前**: `private Long configId;`
- **修复后**: `private Long id;` + `@TableId(value = "config_id")`
- **修复文件**: `microservices-common/src/main/java/net/lab1024/sa/common/theme/entity/UserThemeConfigEntity.java`
- **引用修复**:
  - `UserThemeManager.java`: `themeConfig.getConfigId()` → `themeConfig.getId()` (4处)
  - `UserThemeManager.java`: `themeConfig.setConfigId()` → `themeConfig.setId()` (2处)
  - `UserThemeManager.java`: `defaultTheme.setConfigId()` → `defaultTheme.setId()` (1处)

---

## ⏳ 待修复的实体类（P2优先级）

### 6. ⏳ NotificationConfigEntity
- **当前状态**: `private Long configId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "config_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/notification/domain/entity/NotificationConfigEntity.java`

### 7. ⏳ NotificationTemplateEntity
- **当前状态**: `private Long templateId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "template_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/notification/domain/entity/NotificationTemplateEntity.java`

### 8. ⏳ UserPreferenceEntity
- **当前状态**: `private Long preferenceId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "preference_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/preference/entity/UserPreferenceEntity.java`

### 9. ⏳ I18nResourceEntity
- **当前状态**: `private Long resourceId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "resource_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/i18n/entity/I18nResourceEntity.java`

### 10. ⏳ SystemDictEntity
- **当前状态**: `private Long dictDataId;` (注意：字段名是dictDataId，但表列是dict_data_id)
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "dict_data_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/system/domain/entity/SystemDictEntity.java`

### 11. ⏳ DictTypeEntity
- **当前状态**: `private Long typeId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "type_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/dict/entity/DictTypeEntity.java`

### 12. ⏳ DictDataEntity
- **当前状态**: `private Long dataId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "data_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/dict/entity/DictDataEntity.java`

### 13. ⏳ NotificationEntity
- **当前状态**: `private Long notificationId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "notification_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/monitor/domain/entity/NotificationEntity.java`

### 14. ⏳ RoleMenuEntity
- **当前状态**: `private Long roleMenuId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "role_menu_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/menu/entity/RoleMenuEntity.java`

### 15. ⏳ ConfigChangeAuditEntity
- **当前状态**: `private Long auditId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "audit_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/audit/entity/ConfigChangeAuditEntity.java`

### 16. ⏳ ConfigChangeRollbackEntity
- **当前状态**: `private Long rollbackId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "rollback_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/audit/entity/ConfigChangeRollbackEntity.java`

### 17. ⏳ ConfigChangeApprovalEntity
- **当前状态**: `private Long approvalId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "approval_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/audit/entity/ConfigChangeApprovalEntity.java`

### 18. ⏳ WorkflowTaskEntity
- **当前状态**: `private Long taskId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "task_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/workflow/entity/WorkflowTaskEntity.java`

### 19. ⏳ WorkflowInstanceEntity
- **当前状态**: `private Long instanceId;`
- **需要修复**: 改为 `private Long id;` + `@TableId(value = "instance_id")`
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/workflow/entity/WorkflowInstanceEntity.java`

### 20. ⏳ WorkflowDefinitionEntity
- **当前状态**: ✅ 已使用 `id`（无需修复）
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/workflow/entity/WorkflowDefinitionEntity.java`

### 21. ⏳ ApprovalTemplateEntity
- **当前状态**: ✅ 已使用 `id`（无需修复）
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/workflow/entity/ApprovalTemplateEntity.java`

### 22. ⏳ ApprovalStatisticsEntity
- **当前状态**: ✅ 已使用 `id`（无需修复）
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/workflow/entity/ApprovalStatisticsEntity.java`

### 23. ⏳ ApprovalNodeConfigEntity
- **当前状态**: ✅ 已使用 `id`（无需修复）
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/workflow/entity/ApprovalNodeConfigEntity.java`

### 24. ⏳ ApprovalConfigEntity
- **当前状态**: ✅ 已使用 `id`（无需修复）
- **文件路径**: `microservices-common/src/main/java/net/lab1024/sa/common/workflow/entity/ApprovalConfigEntity.java`

---

## 📊 修复进度统计

| 优先级 | 总数 | 已完成 | 进行中 | 待修复 |
|--------|------|--------|--------|--------|
| **P1（已导致问题）** | 2 | 2 | 0 | 0 |
| **P2（后续优化）** | 22 | 3 | 0 | 11 |
| **已符合规范** | - | 5 | - | - |
| **总计** | 24 | 10 | 0 | 11 |

---

## 🎯 统一命名规范

### 规范标准
```java
/**
 * 实体类主键字段统一使用 id 作为字段名
 * 通过 @TableId(value = "xxx_id") 映射到数据库列
 * 符合实体类主键命名规范：统一使用id，避免方法引用错误
 */
@TableId(value = "xxx_id", type = IdType.AUTO)
private Long id;
```

### 修复模板
```java
// ❌ 修复前
@TableId(value = "area_id", type = IdType.AUTO)
private Long areaId;

// ✅ 修复后
@TableId(value = "area_id", type = IdType.AUTO)
private Long id;
```

### 引用修复模板
```java
// ❌ 修复前
wrapper.in(AreaEntity::getAreaId, accessibleAreaIds);
device.getDeviceId();

// ✅ 修复后
wrapper.in(AreaEntity::getId, accessibleAreaIds);
device.getId();
```

---

## 🔍 修复验证

### 编译检查
- ✅ 已修复的实体类编译通过
- ✅ 已修复的引用代码编译通过
- ⏳ 待修复的实体类需要继续修复

### 代码质量
- ✅ 无linter错误
- ✅ 符合项目编码规范
- ✅ 添加了注释说明

---

## 📝 后续工作

1. **继续修复P2优先级实体类**（11个）
2. **更新所有引用代码**（需要全局搜索并修复）
3. **更新数据库迁移脚本**（如果需要）
4. **更新API文档**（如果主键字段名影响API）
5. **更新测试用例**（确保测试通过）

---

## 🚨 注意事项

1. **数据库列名不变**: 只修改Java字段名，数据库列名通过`@TableId(value="xxx_id")`保持原样
2. **方法参数不变**: 方法参数中的`configId`、`templateId`等保持不变，只修改实体类字段
3. **DTO/Form不变**: DTO和Form类中的字段名保持不变，只修改Entity类
4. **外键字段不变**: 外键字段（如`areaId`、`deviceId`）保持不变，只修改主键字段

---

**修复负责人**: IOE-DREAM 架构委员会  
**最后更新**: 2025-12-09

