#!/bin/bash

# SA-ADMIN编译错误快速修复脚本
# 作者: Claude Code Assistant
# 创建时间: 2025-11-30

echo "=========================================="
echo "🚀 SA-ADMIN编译错误快速修复脚本"
echo "=========================================="

# 进入sa-admin目录
cd "$(dirname "$0")"

echo "📍 当前目录: $(pwd)"

# 1. 清理编译缓存
echo ""
echo "🧹 步骤1: 清理编译缓存..."
mvn clean
if [ $? -eq 0 ]; then
    echo "✅ 编译缓存清理成功"
else
    echo "❌ 编译缓存清理失败"
    exit 1
fi

# 2. 添加@Slf4j注解到缺失的文件
echo ""
echo "📝 步骤2: 添加缺失的@Slf4j注解..."

# 需要添加@Slf4j的文件列表
declare -a files_with_missing_log=(
    "src/main/java/net/lab1024/sa/admin/module/attendance/service/AttendanceLocationService.java"
)

for file in "${files_with_missing_log[@]}"; do
    if [ -f "$file" ]; then
        # 检查是否已有@Slf4j
        if ! grep -q "@Slf4j" "$file"; then
            # 检查是否有import lombok.extern.slf4j.Slf4j
            if ! grep -q "import lombok.extern.slf4j.Slf4j;" "$file"; then
                # 在package后添加import
                sed -i '/^package.*;$/a \
import lombok.extern.slf4j.Slf4j;' "$file"
            fi
            # 在public class前添加@Slf4j
            sed -i '/^public class/i \
@Slf4j' "$file"
            echo "✅ 已为 $file 添加 @Slf4j 注解"
        else
            echo "ℹ️  $file 已有 @Slf4j 注解，跳过"
        fi
    else
        echo "⚠️  文件不存在: $file"
    fi
done

# 3. 修复重复的log字段问题
echo ""
echo "🔧 步骤3: 修复重复的log字段问题..."

zkteco_file="src/main/java/net/lab1024/sa/admin/module/access/adapter/protocol/impl/ZKTecoAdapter.java"
if [ -f "$zkteco_file" ]; then
    if grep -q "Field 'log' already exists" "$zkteco_file" 2>/dev/null || true; then
        echo "⚠️  发现ZKTecoAdapter中log字段重复，需要手动检查"
    fi
fi

# 4. 添加@EqualsAndHashCode(callSuper=false)注解
echo ""
echo "🏷️  步骤4: 添加@EqualsAndHashCode注解..."

# 需要添加@EqualsAndHashCode的文件列表
declare -a files_equals_hashcode=(
    "src/main/java/net/lab1024/sa/admin/module/attendance/domain/entity/LeaveTypesEntity.java"
    "src/main/java/net/lab1024/sa/admin/module/attendance/domain/query/ShiftsQuery.java"
    "src/main/java/net/lab1024/sa/admin/module/attendance/domain/entity/AttendanceRulesEntity.java"
    "src/main/java/net/lab1024/sa/admin/module/attendance/domain/entity/ExceptionApplicationsEntity.java"
    "src/main/java/net/lab1024/sa/admin/module/smart/video/domain/entity/VideoRecordingEntity.java"
    "src/main/java/net/lab1024/sa/admin/module/attendance/domain/query/TimePeriodsQuery.java"
    "src/main/java/net/lab1024/sa/admin/module/hr/domain/form/EmployeeQueryForm.java"
    "src/main/java/net/lab1024/sa/admin/module/smart/video/domain/form/VideoDeviceQueryForm.java"
    "src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/RefundRecordEntity.java"
    "src/main/java/net/lab1024/sa/admin/module/attendance/domain/entity/ExceptionApprovalsEntity.java"
    "src/main/java/net/lab1024/sa/admin/module/attendance/domain/entity/ClockRecordsEntity.java"
    "src/main/java/net/lab1024/sa/admin/module/attendance/domain/entity/TimePeriodsEntity.java"
    "src/main/java/net/lab1024/sa/admin/module/attendance/domain/entity/ShiftsEntity.java"
    "src/main/java/net/lab1024/sa/admin/module/oa/document/domain/form/DocumentQueryForm.java"
)

for file in "${files_equals_hashcode[@]}"; do
    if [ -f "$file" ]; then
        # 检查是否已有@EqualsAndHashCode
        if ! grep -q "@EqualsAndHashCode" "$file"; then
            # 在类声明前添加注解
            sed -i '/^public class/i \
@EqualsAndHashCode(callSuper = false)' "$file"
            echo "✅ 已为 $file 添加 @EqualsAndHashCode 注解"
        else
            echo "ℹ️  $file 已有 @EqualsAndHashCode 注解，跳过"
        fi
    else
        echo "⚠️  文件不存在: $file"
    fi
done

# 5. 尝试编译并检查错误数量变化
echo ""
echo "🔍 步骤5: 重新编译并检查结果..."

# 重新编译
mvn compile -q 2>&1 | tee compile_result.log

# 统计错误数量
error_count=$(grep -c "ERROR" compile_result.log || echo "0")
warning_count=$(grep -c "WARNING" compile_result.log || echo "0")

echo ""
echo "=========================================="
echo "📊 编译结果统计"
echo "=========================================="
echo "❌ 错误数量: $error_count"
echo "⚠️  警告数量: $warning_count"

if [ "$error_count" -eq 0 ]; then
    echo ""
    echo "🎉 恭喜！编译成功，没有错误！"
    exit 0
elif [ "$error_count" -lt 100 ]; then
    echo ""
    echo "📈 进展：错误数量已减少！"
    echo "💡 接下来需要手动修复剩余的错误"
else
    echo ""
    echo "⚠️  错误数量仍然较多，需要进一步排查"
fi

echo ""
echo "📄 详细编译日志已保存到: compile_result.log"
echo ""
echo "🔧 下一步建议："
echo "1. 查看 compile_result.log 了解具体错误"
echo "2. 按照优先级修复剩余错误"
echo "3. 参考编译错误分析报告进行详细修复"
echo ""
echo "=========================================="