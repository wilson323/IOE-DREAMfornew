# IOE-DREAM Entity迁移计划
## 企业级高质量迁移方案

### 迁移原则
1. **零风险迁移**: 确保迁移过程中系统始终可编译可运行
2. **向后兼容**: 保留原文件作为过渡期适配器
3. **完整依赖分析**: 逐个Entity分析依赖关系
4. **渐进式迁移**: 分模块分阶段执行
5. **完整测试验证**: 每个Entity迁移后都要验证功能

### Entity清单和优先级

#### P0级 - 核心基础Entity（立即迁移）
- **AccountEntity** - 消费账户，核心基础实体
- **UserEntity** - 用户基础实体
- **DepartmentEntity** - 部门基础实体
- **AreaEntity** - 区域基础实体

#### P1级 - 业务核心Entity（第二阶段）
- **Access模块**: AccessRecordEntity, AccessPermissionApplyEntity
- **Consume模块**: ConsumeRecordEntity, PaymentRecordEntity
- **Attendance模块**: AttendanceRecordEntity, AttendanceShiftEntity

#### P2级 - 扩展业务Entity（第三阶段）
- **Visitor模块**: VisitorAppointmentEntity, VisitorAreaEntity
- **OA模块**: WorkflowDefinitionEntity, WorkflowInstanceEntity

### 迁移步骤
1. 依赖关系分析
2. 目标目录创建
3. Entity复制和包声明修改
4. 依赖文件更新
5. 编译验证
6. 测试执行
7. 原文件标记为废弃（保留过渡期）

### 风险控制
- 保留原文件作为适配器
- 逐个Entity迁移，避免批量操作
- 每步都要验证编译
- 完整的回滚方案

---

## 详细迁移清单

### Access模块Entity (6个)
1. ApprovalProcessEntity (已修复包名)
2. AreaAccessExtEntity (已修复包名)
3. AccessPermissionApplyEntity
4. AccessRecordEntity

### Consume模块Entity (16个)
1. ConsumeRecordEntity (已修复包名)
2. PaymentRecordEntity (已修复包名)
3. PaymentRefundRecordEntity (已修复包名)
4. QrCodeEntity (已修复包名)
5. AccountEntity - **P0级优先**
6. ConsumeAreaEntity
7. ConsumeProductEntity
8. ConsumeSubsidyIssueRecordEntity
9. ConsumeTransactionEntity
10. MealOrderEntity
11. MealOrderItemEntity
12. OfflineConsumeRecordEntity
13. RefundApplicationEntity
14. ReimbursementApplicationEntity

### Attendance模块Entity (6个)
1. AttendanceLeaveEntity
2. AttendanceOvertimeEntity
3. AttendanceRecordEntity
4. AttendanceShiftEntity
5. AttendanceSupplementEntity
6. AttendanceTravelEntity

### Visitor模块Entity (5个)
1. VisitorAppointmentEntity
2. VisitorAreaEntity
3. DriverEntity
4. ElectronicPassEntity
5. VehicleEntity

### OA模块Entity (10个)
1. WorkflowDefinitionEntity (2个重复)
2. WorkflowInstanceEntity (2个重复)
3. WorkflowTaskEntity (2个重复)
4. ApprovalConfigEntity
5. ApprovalInstanceEntity
6. ApprovalNodeConfigEntity
7. ApprovalStatisticsEntity
8. ApprovalTaskEntity
9. ApprovalTemplateEntity

**总计**: 43个Entity文件
**重复文件**: 3个（需要合并）