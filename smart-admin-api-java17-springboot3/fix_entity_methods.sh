#!/bin/bash

# Entity缺失方法快速修复脚本
# 专门修复因为Lombok注解问题导致的getter/setter方法缺失

echo "🔧 开始修复Entity缺失方法问题..."

# 找出所有编译错误中提到的缺失方法
echo "📋 分析编译错误中的缺失方法..."

# 检查编译输出，提取缺失的方法信息
mvn clean compile -q 2>&1 | grep "找不到符号" | grep "方法" > missing_methods.txt

if [ ! -s missing_methods.txt ]; then
    echo "❌ 没有找到缺失方法信息"
    exit 1
fi

echo "📊 发现以下缺失方法："
cat missing_methods.txt

# 统计错误数量
total_errors=$(mvn clean compile -q 2>&1 | grep -c "ERROR")
echo "🔢 当前编译错误总数: $total_errors"

# 重点修复 AttendanceRuleEntity 的缺失方法
echo "🔧 修复 AttendanceRuleEntity 缺失方法..."

ATTENDANCE_RULE_ENTITY="sa-base/src/main/java/net/lab1024/sa/base/common/device/domain/entity/AttendanceRuleEntity.java"

if [ -f "$ATTENDANCE_RULE_ENTITY" ]; then
    # 检查文件末尾是否已经有 }
    if grep -q "}" "$ATTENDANCE_RULE_ENTITY" && ! grep -q "getWorkStartTime" "$ATTENDANCE_RULE_ENTITY"; then
        echo "  添加缺失的 getter 方法到 AttendanceRuleEntity..."

        # 在最后一个 } 之前添加方法
        sed -i '/^}/i\
\
    /**\
     * 获取工作开始时间\
     *\
     * @return 工作开始时间\
     */\
    public LocalTime getWorkStartTime() {\
        return workStartTime;\
    }\
\
    /**\
     * 获取工作结束时间\
     *\
     * @return 工作结束时间\
     */\
    public LocalTime getWorkEndTime() {\
        return workEndTime;\
    }\
\
    /**\
     * 获取状态文本\
     *\
     * @return 状态描述\
     */\
    public String getStatusText() {\
        if (status == null) {\
            return "未知";\
        }\
        switch (status) {\
            case "ACTIVE":\
                return "启用";\
            case "INACTIVE":\
                return "禁用";\
            default:\
                return status;\
        }\
    }' "$ATTENDANCE_RULE_ENTITY"

        echo "  ✅ AttendanceRuleEntity 方法添加完成"
    else
        echo "  ⚠️ AttendanceRuleEntity 可能已包含方法或文件结构异常"
    fi
else
    echo "  ❌ 找不到 AttendanceRuleEntity 文件"
fi

# 类似地修复其他Entity文件...
echo "🔧 检查其他需要修复的Entity文件..."

# 检查 AttendanceScheduleEntity
ATTENDANCE_SCHEDULE_ENTITY="sa-base/src/main/java/net/lab1024/sa/base/common/device/domain/entity/AttendanceScheduleEntity.java"
if [ -f "$ATTENDANCE_SCHEDULE_ENTITY" ]; then
    echo "  检查 AttendanceScheduleEntity..."
    if grep -q "}" "$ATTENDANCE_SCHEDULE_ENTITY" && ! grep -q "getDayOfWeekText" "$ATTENDANCE_SCHEDULE_ENTITY"; then
        echo "  添加缺失的方法到 AttendanceScheduleEntity..."
        sed -i '/^}/i\
\
    /**\
     * 获取星期文本\
     *\
     * @return 星期描述\
     */\
    public String getDayOfWeekText() {\
        if (dayOfWeek == null) {\
            return "未知";\
        }\
        switch (dayOfWeek) {\
            case 1: return "星期一";\
            case 2: return "星期二";\
            case 3: return "星期三";\
            case 4: return "星期四";\
            case 5: return "星期五";\
            case 6: return "星期六";\
            case 7: return "星期日";\
            default: return "未知";\
        }\
    }\
\
    /**\
     * 获取工作时间范围文本\
     *\
     * @return 工作时间范围\
     */\
    public String getWorkTimeRangeText() {\
        if (workStartTime != null && workEndTime != null) {\
            return workStartTime + " - " + workEndTime;\
        }\
        return "未设置";\
    }\
\
    /**\
     * 获取排班类型文本\
     *\
     * @return 排班类型描述\
     */\
    public String getScheduleTypeText() {\
        if (scheduleType == null) {\
            return "未知";\
        }\
        switch (scheduleType) {\
            case "WEEKDAY": return "工作日";\
            case "WEEKEND": return "周末";\
            case "HOLIDAY": return "节假日";\
            default: return scheduleType;\
        }\
    }' "$ATTENDANCE_SCHEDULE_ENTITY"
        echo "  ✅ AttendanceScheduleEntity 方法添加完成"
    fi
fi

# 修复 AttendanceExceptionEntity
ATTENDANCE_EXCEPTION_ENTITY="sa-base/src/main/java/net/lab1024/sa/base/common/device/domain/entity/AttendanceExceptionEntity.java"
if [ -f "$ATTENDANCE_EXCEPTION_ENTITY" ]; then
    echo "  检查 AttendanceExceptionEntity..."
    if grep -q "}" "$ATTENDANCE_EXCEPTION_ENTITY" && ! grep -q "getExceptionTypeText" "$ATTENDANCE_EXCEPTION_ENTITY"; then
        echo "  添加缺失的方法到 AttendanceExceptionEntity..."
        sed -i '/^}/i\
\
    /**\
     * 获取异常类型文本\
     *\
     * @return 异常类型描述\
     */\
    public String getExceptionTypeText() {\
        if (exceptionType == null) {\
            return "未知";\
        }\
        switch (exceptionType) {\
            case "LATE": return "迟到";\
            case "EARLY_LEAVE": return "早退";\
            case "ABSENTEEISM": return "旷工";\
            case "FORGET_PUNCH": return "忘打卡";\
            default: return exceptionType;\
        }\
    }\
\
    /**\
     * 获取异常级别文本\
     *\
     * @return 异常级别描述\
     */\
    public String getExceptionLevelText() {\
        if (exceptionLevel == null) {\
            return "未知";\
        }\
        switch (exceptionLevel) {\
            case "LOW": return "低";\
            case "MEDIUM": return "中";\
            case "HIGH": return "高";\
            case "CRITICAL": return "严重";\
            default: return exceptionLevel;\
        }\
    }\
\
    /**\
     * 获取状态文本\
     *\
     * @return 状态描述\
     */\
    public String getStatusText() {\
        if (status == null) {\
            return "未知";\
        }\
        switch (status) {\
            case "PENDING": return "待处理";\
            case "PROCESSING": return "处理中";\
            case "RESOLVED": return "已解决";\
            case "REJECTED": return "已拒绝";\
            default: return status;\
        }\
    }\
\
    /**\
     * 获取处理动作文本\
     *\
     * @return 处理动作描述\
     */\
    public String getHandleActionText() {\
        if (handleAction == null) {\
            return "无";\
        }\
        switch (handleAction) {\
            case "APPROVE": return "批准";\
            case "REJECT": return "拒绝";\
            case "MODIFY": return "修改";\
            case "IGNORE": return "忽略";\
            default: return handleAction;\
        }\
    }' "$ATTENDANCE_EXCEPTION_ENTITY"
        echo "  ✅ AttendanceExceptionEntity 方法添加完成"
    fi
fi

echo "🎉 Entity缺失方法修复完成！"

# 验证修复效果
echo "🔍 验证修复效果..."
new_error_count=$(mvn clean compile -q 2>&1 | grep -c "ERROR")
echo "📊 修复后编译错误数量: $new_error_count (之前: $total_errors)"

if [ $new_error_count -lt $total_errors ]; then
    echo "✅ 错误数量减少了 $((total_errors - new_error_count)) 个"
else
    echo "⚠️ 错误数量没有显著减少，可能需要进一步处理"
fi

# 清理临时文件
rm -f missing_methods.txt

echo "🚀 Entity方法修复脚本执行完成！"