#!/bin/bash

# 快速编译错误检测和分类工具
# 适用于Windows环境的简化版本

echo "🔍 IOE-DREAM 项目编译错误快速检测"
echo "=================================="

cd "$(dirname "$0")/smart-admin-api-java17-springboot3"

# 1. 基础环境检查
echo "步骤 1: 检查编译环境..."
java -version 2>&1 | head -1
mvn -version 2>&1 | head -1

# 2. 快速编译检查
echo -e "\n步骤 2: 执行快速编译..."
mvn clean compile -q > compile_output.log 2>&1
compile_result=$?

# 3. 错误分析
if [ $compile_result -eq 0 ]; then
    echo "✅ 编译成功！0个错误"
    echo "🎉 OpenSpec Task 1.1 完成：编译错误消除"
    exit 0
fi

error_count=$(grep -c "ERROR" compile_output.log 2>/dev/null || echo "0")
echo "❌ 编译失败，发现 $error_count 个错误"

# 4. 错误分类统计
echo -e "\n步骤 3: 错误分类分析..."

# javax包名错误
javax_count=$(grep -c "javax\." compile_output.log 2>/dev/null || echo "0")
echo "  - javax包名问题: $javax_count 个"

# 找不到符号错误
symbol_count=$(grep -c "找不到符号" compile_output.log 2>/dev/null || echo "0")
echo "  - 找不到符号: $symbol_count 个"

# 重复定义错误
duplicate_count=$(grep -c "重复定义\|duplicate" compile_output.log 2>/dev/null || echo "0")
echo "  - 重复定义: $duplicate_count 个"

# 5. 主要错误展示
echo -e "\n步骤 4: 主要错误类型展示..."

if [ $javax_count -gt 0 ]; then
    echo "🔴 javax包名相关错误:"
    grep "javax\." compile_output.log | head -3
fi

if [ $symbol_count -gt 0 ]; then
    echo "🔴 找不到符号错误:"
    grep "找不到符号" compile_output.log | head -3
fi

# 6. 保存详细报告
echo -e "\n详细编译日志保存到: compile_output.log"
echo "请查看日志文件进行手动修复"

exit $compile_result