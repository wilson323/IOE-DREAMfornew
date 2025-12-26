#!/bin/bash
# QueryBuilder批量迁移工具
# 用于批量迁移所有Service到QueryBuilder模式

echo "======================================"
echo "QueryBuilder批量迁移工具"
echo "======================================"
echo ""

# 需要迁移的服务列表
services=(
    "AccessAreaServiceImpl:ioedream-access-service"
    "AccessUserPermissionServiceImpl:ioedream-access-service"
    "BiometricTemplateServiceImpl:ioedream-biometric-service"
    "AttendanceReportServiceImpl:ioedream-attendance-service"
    "AttendanceRuleServiceImpl:ioedream-attendance-service"
    "AttendanceSummaryServiceImpl:ioedream-attendance-service"
    "SmartScheduleServiceImpl:ioedream-attendance-service"
    "ConsumeSubsidyServiceImpl:ioedream-consume-service"
    "VideoRecordingServiceImpl:ioedream-video-service"
    "VideoFaceServiceImpl:ioedream-video-service"
    "VideoBehaviorServiceImpl:ioedream-video-service"
    "VisitorStatisticsServiceImpl:ioedream-visitor-service"
    "EmployeeServiceImpl:ioedream-common-service"
    "AreaUnifiedServiceImpl:ioedream-common-service"
)

total=${#services[@]}
completed=0

echo "计划迁移 $total 个服务"
echo ""

for service in "${services[@]}"; do
    IFS=':' read -r service_name module_name <<< "$service"

    echo "[$((completed+1))/$total] 处理 $service_name ($module_name)..."

    # 查找文件路径
    file_path=$(find D:/IOE-DREAM/microservices/$module_name -name "$service_name.java" 2>/dev/null | grep -v target)

    if [ -z "$file_path" ]; then
        echo "   ⚠️  文件未找到，跳过"
        continue
    fi

    # 检查是否使用了LambdaQueryWrapper
    lambda_count=$(grep -c "new LambdaQueryWrapper" "$file_path" 2>/dev/null || echo "0")

    if [ "$lambda_count" -eq "0" ]; then
        echo "   ✅ 无需迁移（未使用LambdaQueryWrapper）"
        ((completed++))
        continue
    fi

    echo "   📝 找到 $lambda_count 处LambdaQueryWrapper使用"
    echo "   📄 文件: $file_path"

    # 这里添加具体的迁移逻辑
    # 实际迁移需要根据每个服务的具体代码模式调整

    echo "   ⏳  需要手动迁移"
    echo ""

    ((completed++))
done

echo "======================================"
echo "批量迁移分析完成"
echo "======================================"
echo "总计服务: $total"
echo "已完成分析: $completed"
echo ""
echo "下一步: 逐个手动迁移需要处理的服务"
