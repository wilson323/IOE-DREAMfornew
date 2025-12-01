#!/bin/bash

echo "======================================"
echo "编译错误详细分析报告"
echo "======================================"
echo "时间: $(date)"
echo "项目: IOE-DREAM sa-admin模块"
echo ""

cd "D:\IOE-DREAM\smart-admin-api-java17-springboot3\sa-admin"

# 1. 重新编译获取最新错误
echo "步骤1: 重新编译获取最新错误..."
mvn clean compile -DskipTests > latest_compile.log 2>&1

# 2. 统计总错误数
total_errors=$(grep -c "\[ERROR\]" latest_compile.log)
echo "总编译错误数: $total_errors"

# 3. 统计警告数
total_warnings=$(grep -c "\[WARNING\]" latest_compile.log)
echo "总编译警告数: $total_warnings"

echo ""

# 4. 错误类型分析
echo "======================================"
echo "错误类型分析"
echo "======================================"

# 找不到符号错误
symbol_not_found=$(grep -c "找不到符号" latest_compile.log)
echo "找不到符号错误: $symbol_not_found"

# 类型不匹配错误
type_mismatch=$(grep -c "类型不匹配\|无法转换" latest_compile.log)
echo "类型不匹配错误: $type_mismatch"

# 符号设置不正确错误
symbol_incorrect=$(grep -c "符号设置不正确" latest_compile.log)
echo "符号设置不正确错误: $symbol_incorrect"

# log变量未找到错误
log_errors=$(grep -c "找不到.*log" latest_compile.log)
echo "log变量未找到错误: $log_errors"

# 方法不存在错误
method_not_found=$(grep -c "不存在.*方法" latest_compile.log)
echo "方法不存在错误: $method_not_found"

# 需要接口错误
interface_errors=$(grep -c "需要接口" latest_compile.log)
echo "需要接口错误: $interface_errors"

echo ""

# 5. 出错文件统计
echo "======================================"
echo "出错文件统计 (前15个)"
echo "======================================"

grep "\[ERROR\].*\.java:" latest_compile.log | sed 's/.*\/\([^\/]*\.java\):\[.*/\1/' | sort | uniq -c | sort -nr | head -15

echo ""

# 6. 错误详情提取
echo "======================================"
echo "关键错误详情"
echo "======================================"

echo "--- AbnormalDetectionServiceImpl.java 错误 ---"
grep "\[ERROR\].*AbnormalDetectionServiceImpl.java" latest_compile.log | head -10

echo ""
echo "--- 考勤模块错误 ---"
grep "\[ERROR\].*attendance.*\.java" latest_compile.log | head -10

echo ""
echo "--- 消费模块错误 ---"
grep "\[ERROR\].*consume.*\.java" latest_compile.log | head -10

echo ""
echo "--- log相关错误 ---"
grep "\[ERROR\].*找不到.*log" latest_compile.log | head -5

echo ""

# 7. 修复建议优先级
echo "======================================"
echo "修复建议优先级"
echo "======================================"

echo "🔴 高优先级 (必须立即修复):"
echo "1. AbnormalDetectionServiceImpl 类的符号冲突 (影响20+个错误)"
echo "2. log 变量未定义问题 (影响多个Repository类)"
echo "3. Entity字段缺失问题 (getShiftId, getHolidayRules等)"

echo ""
echo "🟡 中优先级 (批量修复):"
echo "1. 类型转换警告 (16个警告)"
echo "2. 方法参数类型不匹配"
echo "3. DAO层缺失方法"

echo ""
echo "🟢 低优先级 (后续优化):"
echo "1. 代码重复和过时API使用"
echo "2. 性能优化建议"

echo ""

# 8. 预估修复工作量
echo "======================================"
echo "预估修复工作量"
echo "======================================"

echo "基于错误类型分析:"
echo "- 高优先级错误: 预计需要2-4小时"
echo "- 中优先级错误: 预计需要1-2小时"
echo "- 低优先级优化: 预计需要30分钟"

echo ""
echo "总预计修复时间: 3.5-6.5小时"

echo ""
echo "======================================"
echo "分析完成"
echo "======================================"