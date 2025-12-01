#!/bin/bash

echo "=========================================="
echo "📊 IOE-DREAM 项目编译错误分析报告"
echo "=========================================="
echo "分析时间: $(date)"
echo ""

# 进入项目目录
cd smart-admin-api-java17-springboot3

# 1. 总体编译状态
echo "🔍 1. 总体编译状态分析"
echo "------------------------"
mvn clean compile -DskipTests > compile-output.txt 2>&1

# 统计各类错误数量
ERROR_COUNT=$(grep -c "ERROR" compile-output.txt)
WARNING_COUNT=$(grep -c "WARNING" compile-output.txt)

echo "编译错误总数: $ERROR_COUNT"
echo "编译警告总数: $WARNING_COUNT"
echo ""

# 2. 错误类型分析
echo "📋 2. 错误类型详细分析"
echo "------------------------"

# 符号未找到错误
SYMBOL_ERRORS=$(grep -c "找不到符号\|cannot find symbol" compile-output.txt)
echo "符号未找到错误: $SYMBOL_ERRORS"

# 方法未找到错误
METHOD_ERRORS=$(grep -c "找不到方法\|method.*not found" compile-output.txt)
echo "方法未找到错误: $METHOD_ERRORS"

# 类型转换错误
CAST_ERRORS=$(grep -c "数据类型.*无法转换\|cannot be cast" compile-output.txt)
echo "类型转换错误: $CAST_ERRORS"

# 包导入错误
IMPORT_ERRORS=$(grep -c "包.*不存在\|package.*does not exist" compile-output.txt)
echo "包导入错误: $IMPORT_ERRORS"

# 缓存相关错误
CACHE_ERRORS=$(grep -c "UnifiedCacheService" compile-output.txt)
echo "缓存服务相关错误: $CACHE_ERRORS"

# 枚举相关错误
ENUM_ERRORS=$(grep -c "Enum" compile-output.txt)
echo "枚举相关错误: $ENUM_ERRORS"

echo ""

# 3. 按文件分析错误分布
echo "📁 3. 错误文件分布分析"
echo "------------------------"
grep "ERROR.*java" compile-output.txt | sed 's/.*\/src\/main\/java\///' | sed 's/:[0-9]*.*//' | sort | uniq -c | sort -nr | head -10

echo ""

# 4. 按模块分析错误分布
echo "🏗️ 4. 模块错误分布分析"
echo "------------------------"

# 考勤模块错误
ATTENDANCE_ERRORS=$(grep -c "/attendance/" compile-output.txt)
echo "考勤模块错误: $ATTENDANCE_ERRORS"

# 消费模块错误
CONSUME_ERRORS=$(grep -c "/consume/" compile-output.txt)
echo "消费模块错误: $CONSUME_ERRORS"

# 门禁模块错误
ACCESS_ERRORS=$(grep -c "/access/" compile-output.txt)
echo "门禁模块错误: $ACCESS_ERRORS"

# 视频模块错误
VIDEO_ERRORS=$(grep -c "/video/" compile-output.txt)
echo "视频模块错误: $VIDEO_ERRORS"

# 系统模块错误
SYSTEM_ERRORS=$(grep -c "/system/" compile-output.txt)
echo "系统模块错误: $SYSTEM_ERRORS"

# OA模块错误
OA_ERRORS=$(grep -c "/oa/" compile-output.txt)
echo "OA模块错误: $OA_ERRORS"

echo ""

# 5. 高频错误模式识别
echo "🔍 5. 高频错误模式识别"
echo "------------------------"

# 缓存方法调用错误
echo "缓存方法调用模式:"
grep -E "get.*UnifiedCacheService|set.*UnifiedCacheService" compile-output.txt | head -5

echo ""

# ResponseDTO类型错误
echo "ResponseDTO类型转换错误:"
grep -E "ResponseDTO.*无法转换" compile-output.txt | head -5

echo ""

# 6. 修复建议
echo "🛠️ 6. 修复优先级建议"
echo "------------------------"

echo "🔴 高优先级 (立即修复):"
echo "  - UnifiedCacheService 方法签名不匹配"
echo "  - ResponseDTO 类型转换错误"
echo ""

echo "🟡 中优先级 (批量修复):"
echo "  - 符号未找到错误"
echo "  - 方法未找到错误"
echo ""

echo "🟢 低优先级 (后续优化):"
echo "  - 警告信息处理"
echo "  - 代码风格优化"
echo ""

# 7. 自动化修复方案
echo "🤖 7. 自动化修复方案"
echo "------------------------"

echo "可以立即执行的修复命令:"
echo "1. 修复缓存方法签名:"
echo "   grep -r 'UnifiedCacheService.*get.*(' src/ | sed 's/:.*//' | sort | uniq"
echo ""

echo "2. 修复ResponseDTO类型转换:"
echo "   grep -r 'ResponseDTO<.*>.*ResponseDTO<' src/ | sed 's/:.*//' | sort | uniq"
echo ""

# 清理临时文件
rm -f compile-output.txt

echo "=========================================="
echo "✅ 编译错误分析完成"
echo "=========================================="