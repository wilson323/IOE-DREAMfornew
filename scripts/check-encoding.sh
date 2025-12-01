#!/bin/bash
# 编码检查脚本

echo "🔍 检查文件编码..."

# 检查Java文件
JAVA_ISSUES=$(find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
echo "Java文件编码问题: $JAVA_ISSUES"

# 检查中文字符显示
CHINESE_ISSUES=$(find . -name "*.java" -exec grep -l "????\|涓?\|鏂?" {} \; 2>/dev/null | wc -l)
echo "中文显示问题: $CHINESE_ISSUES"

if [ "$JAVA_ISSUES" -eq 0 ] && [ "$CHINESE_ISSUES" -eq 0 ]; then
    echo "✅ 编码检查通过"
    exit 0
else
    echo "❌ 发现编码问题"
    exit 1
fi
