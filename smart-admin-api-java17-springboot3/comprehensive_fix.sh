#!/bin/bash

echo "=== IOE-DREAM 编译错误系统性修复工具 ==="
echo "开始时间: $(date)"

cd "D:\IOE-DREAM\smart-admin-api-java17-springboot3\sa-admin"

echo "1. 分析当前编译错误状态..."
mvn clean compile > compilation_analysis.log 2>&1

echo "2. 分析错误类型..."

# 统计总错误数
total_errors=$(grep -c "ERROR" compilation_analysis.log)
echo "   总错误数: $total_errors"

# 分析找不到符号错误
symbol_errors=$(grep -c "找不到符号" compilation_analysis.log)
echo "   找不到符号错误: $symbol_errors"

# 分析程序包不存在错误
package_errors=$(grep -c "程序包.*不存在" compilation_analysis.log)
echo "   程序包不存在错误: $package_errors"

# 分析方法不存在错误
method_errors=$(grep -c "方法.*不存在" compilation_analysis.log)
echo "   方法不存在错误: $method_errors"

echo "3. 提取具体错误信息..."
grep -E "找不到符号|程序包.*不存在|方法.*不存在" compilation_analysis.log > detailed_errors.txt

echo "4. 分析高频错误类型..."
echo "   === Top 10 错误文件 ==="
grep "\[ERROR\]" compilation_analysis.log | awk '{print $3}' | sort | uniq -c | sort -nr | head -10

echo "   === Top 10 错误类型 ==="
grep "找不到符号\|程序包.*不存在" compilation_analysis.log | sed 's/.*符号: *//' | sed 's/.*程序包 *//' | sort | uniq -c | sort -nr | head -10

echo "分析完成，详细信息保存在:"
echo "  - compilation_analysis.log (完整编译日志)"
echo "  - detailed_errors.txt (具体错误列表)"

echo "结束时间: $(date)"