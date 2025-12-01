#!/bin/bash
echo "快速修复Lombok清理后的类声明问题..."

# 27个问题文件的完整路径
problem_files=(
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/biometric/domain/entity/AuthenticationStrategyEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/PasswordSecurityPolicyEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/LimitConfigEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/attendance/domain/entity/AttendanceScheduleEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/attendance/domain/entity/AttendanceStatisticsEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/access/domain/entity/SmartAccessPermissionEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/system/device/domain/form/UnifiedDeviceUpdateForm.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/AbnormalDetectionRuleEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/oa/document/domain/entity/DocumentPermissionEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/oa/workflow/domain/entity/WorkflowDefinitionEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/hr/domain/entity/EmployeeEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/oa/workflow/domain/entity/WorkflowInstanceEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/AccountTransactionEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/AccountBalanceEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/video/domain/entity/VideoStreamEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/access/domain/entity/AccessEventEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/ProductCategoryEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/MenuEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/OrderingItemEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/oa/document/domain/entity/DocumentVersionEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/PasswordVerificationHistoryEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/ConsumeRecordEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/oa/document/domain/entity/DocumentEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/AbnormalOperationLogEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/SecurityNotificationLogEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/ConsumeAuditLogEntity.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/attendance/domain/entity/AttendanceRuleEntity.java"
)

fixed_count=0
for file in "${problem_files[@]}"; do
    if [ -f "$file" ]; then
        echo "修复文件: $file"

        # 修复残留的Lombok注解语法
        sed -i 's/^(callSuper = true)$\/\/ 残留的EqualsAndHashCode注解参数/' "$file"
        sed -i 's/^(@Data$/@Data\/\/ 残留注解/' "$file"
        sed -i 's/^(@Builder$/@Builder\/\/ 残留注解/' "$file"

        # 移除单行残留注解
        sed -i '/^(@Data$/d' "$file"
        sed -i '/^(@Builder$/d' "$file"
        sed -i '/^(@NoArgsConstructor$/d' "$file"
        sed -i '/^(@AllArgsConstructor$/d' "$file"
        sed -i '/^(callSuper = true)$/d' "$file"

        # 修复可能的语法错误
        sed -i 's/})/}/g' "$file"

        echo "已修复: $file"
        ((fixed_count++))
    else
        echo "文件不存在: $file"
    fi
done

echo "快速修复完成！共修复 $fixed_count 个文件"