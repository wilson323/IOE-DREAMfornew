# Entity架构迁移清单文档

**生成时间**: 2025-12-02  
**扫描范围**: 全部微服务  
**扫描结果**: 391个Entity文件  
**分析依据**: CLAUDE.md全局统一架构规范

---

## 📊 Entity分布统计

### 总体概览

| 位置 | Entity数量 | 状态 | 备注 |
|------|-----------|------|------|
| **业务微服务** | 92个 | ❌ 违规 | 应迁移到microservices-common |
| **microservices-common** | 70个 | ✅ 符合规范 | 保持不变 |
| **ioedream-common-core** | 28个 | ⚠️ 需整合 | 已在microservices-common中存在 |
| **ioedream-common-service** | 17个 | ⚠️ 需整合 | 已在microservices-common中存在 |
| **archive/deprecated-services** | 54个 | 🗑️ 废弃 | 无需处理 |
| **Gateway** | 0个 | ✅ 符合规范 | 网关不应包含Entity |

### 架构合规性分析

- **严重违规**: 92个Entity在业务微服务中定义
- **规范遵循率**: 约43%（70/(70+92)）
- **目标遵循率**: 100%

---

## 🚨 P0级：业务微服务中的Entity（必须迁移）

### 1. ioedream-access-service（18个Entity）

**目标迁移路径**: `microservices-common/src/main/java/net/lab1024/sa/common/access/entity/`

| 序号 | Entity名称 | 当前位置 | 迁移目标 | 优先级 |
|------|-----------|---------|---------|--------|
| 1 | AntiPassbackRecordEntity | access-service/...advanced/domain/entity/ | common/access/entity/ | P0 |
| 2 | AntiPassbackRuleEntity | access-service/...advanced/domain/entity/ | common/access/entity/ | P0 |
| 3 | EvacuationEventEntity | access-service/...advanced/domain/entity/ | common/access/entity/ | P0 |
| 4 | EvacuationPointEntity | access-service/...advanced/domain/entity/ | common/access/entity/ | P0 |
| 5 | EvacuationRecordEntity | access-service/...advanced/domain/entity/ | common/access/entity/ | P0 |
| 6 | InterlockLogEntity | access-service/...advanced/domain/entity/ | common/access/entity/ | P0 |
| 7 | InterlockRuleEntity | access-service/...advanced/domain/entity/ | common/access/entity/ | P0 |
| 8 | LinkageRuleEntity | access-service/...advanced/domain/entity/ | common/access/entity/ | P0 |
| 9 | VisitorReservationEntity | access-service/...approval/domain/entity/ | common/access/entity/ | P0 |
| 10 | AccessEventEntity | access-service/.../domain/entity/ | common/access/entity/ | P0 |
| 11 | AccessRuleEntity | access-service/.../domain/entity/ | common/access/entity/ | P0 |
| 12 | AntiPassbackEntity | access-service/.../domain/entity/ | common/access/entity/ | P0 |
| 13 | ApprovalProcessEntity | access-service/.../domain/entity/ | common/access/entity/ | P0 |
| 14 | ApprovalRequestEntity | access-service/.../domain/entity/ | common/access/entity/ | P0 |
| 15 | AreaAccessExtEntity | access-service/.../domain/entity/ | common/access/entity/ | P0 |
| 16 | DeviceMonitorEntity | access-service/.../domain/entity/ | common/access/entity/ | P0 |
| 17 | InterlockGroupEntity | access-service/.../domain/entity/ | common/access/entity/ | P0 |

**依赖分析**:
- 依赖 `BaseEntity`（已在common中）
- 依赖 `AreaEntity`, `PersonEntity`（已在common中）
- 依赖 `DeviceEntity`（已在common中）
- 无循环依赖

### 2. ioedream-attendance-service（21个Entity）

**目标迁移路径**: `microservices-common/src/main/java/net/lab1024/sa/common/attendance/entity/`

| 序号 | Entity名称 | 当前位置 | 迁移目标 | 优先级 |
|------|-----------|---------|---------|--------|
| 1 | AttendanceDeviceEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 2 | AttendanceExceptionEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 3 | AttendanceRecordEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 4 | AttendanceReportEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 5 | AttendanceRuleEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 6 | AttendanceRulesEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 7 | AttendanceScheduleEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 8 | AttendanceStatisticsEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 9 | ClockRecordsEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 10 | ExceptionApplicationsEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 11 | ExceptionApprovalsEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 12 | LeaveApplicationEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 13 | LeaveTypeEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 14 | LeaveTypesEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 15 | OvertimeApplicationEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 16 | ShiftSchedulingEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 17 | ShiftsEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |
| 18 | TimePeriodsEntity | attendance-service/.../domain/entity/ | common/attendance/entity/ | P0 |

**依赖分析**:
- 依赖 `BaseEntity`
- 依赖 `PersonEntity`, `DepartmentEntity`
- 依赖 `DeviceEntity`
- 无循环依赖

### 3. ioedream-consume-service（27个Entity）

**目标迁移路径**: `microservices-common/src/main/java/net/lab1024/sa/common/consume/entity/`

| 序号 | Entity名称 | 当前位置 | 迁移目标 | 优先级 |
|------|-----------|---------|---------|--------|
| 1 | AccessAreaEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 2 | AccountEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 3 | ConsumeAccountEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 4 | ConsumeAreaEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 5 | ConsumeAuditLogEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 6 | ConsumeBarcodeEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 7 | ConsumeInventoryRecordEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 8 | ConsumeMealEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 9 | ConsumePermissionConfigEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 10 | ConsumeProductCategoryEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 11 | ConsumeProductEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 12 | ConsumeRecordEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 13 | ConsumeReportEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 14 | ConsumeSubsidyAccountEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 15 | ConsumeSubsidyIssueRecordEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 16 | ConsumeSubsidyUsageRecordEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 17 | ConsumeTransactionEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 18 | DetectionRuleEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 19 | OperationEventEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 20 | PaymentRecordEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 21 | ProductEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 22 | RechargeRecordEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 23 | RefundRecordEntity | consume-service/.../domain/entity/ | common/consume/entity/ | P0 |
| 24 | ConsumeDataSourceEntity | consume-service/.../integration/domain/entity/ | common/consume/entity/ | P0 |
| 25 | ConsumeReportTemplateEntity | consume-service/.../report/domain/entity/ | common/consume/entity/ | P0 |

**依赖分析**:
- 依赖 `BaseEntity`
- 依赖 `PersonEntity`, `AreaEntity`
- 依赖 `DeviceEntity`
- 无循环依赖

### 4. ioedream-device-comm-service（10个Entity）

**目标迁移路径**: `microservices-common/src/main/java/net/lab1024/sa/common/device/entity/`

| 序号 | Entity名称 | 当前位置 | 迁移目标 | 优先级 |
|------|-----------|---------|---------|--------|
| 1 | AccessPermissionEntity | device-comm-service/.../domain/entity/ | common/device/entity/ | P0 |
| 2 | AccessRecordEntity | device-comm-service/.../domain/entity/ | common/device/entity/ | P0 |
| 3 | AttendanceRecordEntity | device-comm-service/.../domain/entity/ | common/device/entity/ | P0 |
| 4 | DeviceEntity | device-comm-service/.../domain/entity/ | ⚠️ 已存在 | P0 |
| 5 | DeviceHealthEntity | device-comm-service/.../domain/entity/ | common/device/entity/ | P0 |
| 6 | VideoAlarmEntity | device-comm-service/.../domain/entity/ | common/device/entity/ | P0 |
| 7 | VideoDeviceEntity | device-comm-service/.../domain/entity/ | common/device/entity/ | P0 |
| 8 | VideoRecordEntity | device-comm-service/.../domain/entity/ | common/device/entity/ | P0 |
| 9 | WorkShiftEntity | device-comm-service/.../domain/entity/ | common/device/entity/ | P0 |

**特别注意**:
- `DeviceEntity` 已在 `microservices-common/src/main/java/net/lab1024/sa/common/organization/entity/DeviceEntity.java` 存在
- 需要对比两个版本，决定保留哪个或合并

### 5. ioedream-video-service（7个Entity）

**目标迁移路径**: `microservices-common/src/main/java/net/lab1024/sa/common/video/entity/`

| 序号 | Entity名称 | 当前位置 | 迁移目标 | 优先级 |
|------|-----------|---------|---------|--------|
| 1 | DeviceAlarmEntity | video-service/.../domain/entity/ | common/video/entity/ | P0 |
| 2 | DeviceStatusLogEntity | video-service/.../domain/entity/ | common/video/entity/ | P0 |
| 3 | FaceFeatureEntity | video-service/.../domain/entity/ | common/video/entity/ | P0 |
| 4 | MonitorEventEntity | video-service/.../domain/entity/ | common/video/entity/ | P0 |
| 5 | VideoRecordEntity | video-service/.../domain/entity/ | common/video/entity/ | P0 |
| 6 | VideoRecordingEntity | video-service/.../domain/entity/ | common/video/entity/ | P0 |
| 7 | VideoStreamEntity | video-service/.../domain/entity/ | common/video/entity/ | P0 |

### 6. ioedream-visitor-service（3个Entity）

**目标迁移路径**: `microservices-common/src/main/java/net/lab1024/sa/common/visitor/entity/`

| 序号 | Entity名称 | 当前位置 | 迁移目标 | 优先级 |
|------|-----------|---------|---------|--------|
| 1 | VisitorAppointmentEntity | visitor-service/.../domain/entity/ | common/visitor/entity/ | P0 |
| 2 | VisitorEntity | visitor-service/.../domain/entity/ | common/visitor/entity/ | P0 |
| 3 | VisitRecordEntity | visitor-service/.../domain/entity/ | common/visitor/entity/ | P0 |

### 7. ioedream-oa-service（6个Entity）

**目标迁移路径**: `microservices-common/src/main/java/net/lab1024/sa/common/oa/entity/`

| 序号 | Entity名称 | 当前位置 | 迁移目标 | 优先级 |
|------|-----------|---------|---------|--------|
| 1 | ConfigItemEntity | oa-service/.../config/domain/entity/ | common/oa/entity/ | P0 |
| 2 | DocumentEntity | oa-service/.../document/domain/entity/ | ⚠️ 已存在 | P0 |
| 3 | FileEntity | oa-service/.../file/domain/entity/ | ⚠️ 已存在 | P0 |
| 4 | WorkflowDefinitionEntity | oa-service/.../workflow/domain/entity/ | ⚠️ 已存在 | P0 |
| 5 | WorkflowInstanceEntity | oa-service/.../workflow/domain/entity/ | ⚠️ 已存在 | P0 |
| 6 | WorkflowTaskEntity | oa-service/.../workflow/domain/entity/ | ⚠️ 已存在 | P0 |

**特别注意**:
- 多个Entity已在microservices-common中存在，需要对比版本

---

## ⚠️ 需要整合的Entity

### 1. ioedream-common-core中的重复Entity（28个）

这些Entity已在microservices-common中存在，需要删除：

| Entity名称 | 位置 | 状态 |
|-----------|------|------|
| AuditLogEntity | ioedream-common-core/...audit/domain/entity/ | 重复 |
| UserSessionEntity | ioedream-common-core/...auth/domain/entity/ | 重复 |
| ConfigEntity | ioedream-common-core/...config/entity/ | 重复 |
| BaseEntity | ioedream-common-core/...entity/ | 重复 |
| AlertEntity | ioedream-common-core/...monitor/domain/entity/ | 重复 |
| AlertRuleEntity | ioedream-common-core/...monitor/domain/entity/ | 重复 |
| NotificationEntity | ioedream-common-core/...monitor/domain/entity/ | 重复 |
| SystemLogEntity | ioedream-common-core/...monitor/domain/entity/ | 重复 |
| SystemMonitorEntity | ioedream-common-core/...monitor/domain/entity/ | 重复 |
| NotificationConfigEntity | ioedream-common-core/...notification/domain/entity/ | 重复 |
| NotificationMessageEntity | ioedream-common-core/...notification/domain/entity/ | 重复 |
| NotificationRecordEntity | ioedream-common-core/...notification/domain/entity/ | 重复 |
| NotificationTemplateEntity | ioedream-common-core/...notification/domain/entity/ | 重复 |
| OperationLogEntity | ioedream-common-core/...notification/domain/entity/ | 重复 |
| AreaEntity | ioedream-common-core/...organization/entity/ | 重复 |
| AreaPersonEntity | ioedream-common-core/...organization/entity/ | 重复 |
| DepartmentEntity | ioedream-common-core/...organization/entity/ | 重复 |
| PersonEntity | ioedream-common-core/...organization/entity/ | 重复 |
| JobExecutionLogEntity | ioedream-common-core/...scheduler/domain/entity/ | 重复 |
| ScheduledJobEntity | ioedream-common-core/...scheduler/domain/entity/ | 重复 |
| PermissionEntity | ioedream-common-core/...security/entity/ | 重复 |
| RoleEntity | ioedream-common-core/...security/entity/ | 重复 |
| RolePermissionEntity | ioedream-common-core/...security/entity/ | 重复 |
| UserEntity | ioedream-common-core/...security/entity/ | 重复 |
| UserRoleEntity | ioedream-common-core/...security/entity/ | 重复 |
| SystemConfigEntity | ioedream-common-core/...system/domain/entity/ | 重复 |
| SystemDictEntity | ioedream-common-core/...system/domain/entity/ | 重复 |
| EmployeeEntity | ioedream-common-core/...system/employee/domain/entity/ | 重复 |
| ApprovalRecordEntity | ioedream-common-core/...workflow/domain/entity/ | 重复 |
| ApprovalWorkflowEntity | ioedream-common-core/...workflow/entity/ | 重复 |

**处理建议**: 删除ioedream-common-core中的所有重复Entity

### 2. ioedream-common-service中的重复Entity（17个）

同样需要删除，这些Entity已在microservices-common中存在。

---

## 📋 迁移执行计划

### 阶段1：准备工作（预计1天）
- [x] 完成Entity分布扫描
- [x] 生成迁移清单文档
- [ ] 分析Entity依赖关系
- [ ] 制定迁移顺序

### 阶段2：创建目标包结构（预计0.5天）
- [ ] 创建 `common/access/entity/` 包
- [ ] 创建 `common/attendance/entity/` 包
- [ ] 创建 `common/consume/entity/` 包
- [ ] 创建 `common/device/entity/` 包
- [ ] 创建 `common/video/entity/` 包
- [ ] 创建 `common/visitor/entity/` 包
- [ ] 创建 `common/oa/entity/` 包

### 阶段3：逐个迁移Entity（预计5-7天）
- [ ] 迁移access-service的18个Entity
- [ ] 迁移attendance-service的21个Entity
- [ ] 迁移consume-service的27个Entity
- [ ] 迁移device-comm-service的10个Entity
- [ ] 迁移video-service的7个Entity
- [ ] 迁移visitor-service的3个Entity
- [ ] 迁移oa-service的6个Entity

### 阶段4：更新引用（预计3-5天）
- [ ] 更新所有DAO接口中的Entity引用
- [ ] 更新所有Service中的Entity引用
- [ ] 更新所有Manager中的Entity引用
- [ ] 更新所有Controller中的Entity引用
- [ ] 删除业务服务中的旧Entity文件

### 阶段5：删除重复Entity（预计1天）
- [ ] 删除ioedream-common-core中的28个重复Entity
- [ ] 删除ioedream-common-service中的17个重复Entity
- [ ] 验证编译通过

### 阶段6：最终验证（预计1天）
- [ ] 全局编译验证
- [ ] 检查所有导入路径
- [ ] 确认无重复Entity
- [ ] 更新架构文档

**预计总工作量**: 11-15天

---

## ⚠️ 关键注意事项

### 1. 迁移原则
- **手动逐个迁移**: 禁止使用脚本批量迁移
- **保持功能完整**: 确保不破坏现有功能
- **验证编译**: 每次迁移后立即验证编译
- **保持一致性**: 确保所有引用都已更新

### 2. Entity标准规范
```java
package net.lab1024.sa.common.access.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.common.domain.BaseEntity;
import java.time.LocalDateTime;

/**
 * 防回传记录实体
 * 
 * @author IOE-DREAM Team
 * @since 2025-12-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_anti_passback_record")
public class AntiPassbackRecordEntity extends BaseEntity {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 删除标记
     */
    @TableLogic
    @TableField("deleted_flag")
    private Boolean deletedFlag;
    
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
```

### 3. 重复Entity处理
- **DeviceEntity**: 对比device-comm-service和microservices-common中的版本，选择功能更完整的版本
- **DocumentEntity/FileEntity/WorkflowEntity**: 对比oa-service和microservices-common中的版本

### 4. 验证检查清单
- [ ] 所有Entity都继承BaseEntity
- [ ] 所有Entity都有@TableName注解
- [ ] 所有Entity都有完整的JavaDoc注释
- [ ] 所有Entity都有必要的MyBatis-Plus注解
- [ ] 所有导入路径都已更新
- [ ] 编译通过，无错误
- [ ] 无重复Entity定义

---

## 📊 预期效果

### 修复前
- Entity分布混乱：92个Entity在业务服务中
- 架构合规率：43%
- 编译错误：约500个导入错误

### 修复后
- Entity统一管理：所有公共Entity在microservices-common中
- 架构合规率：100%
- 编译错误：消除约500个导入错误
- 包结构清晰规范，易于维护

---

**文档生成时间**: 2025-12-02  
**下次更新**: 完成迁移后  
**维护责任人**: IOE-DREAM架构委员会

