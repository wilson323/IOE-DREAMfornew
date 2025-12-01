#!/bin/bash

echo "🔍 验证编码状态..."
echo "=================="

# 检查文件编码
ENCODING_ISSUES=0
JAVA_FILES=$(find smart-admin-api-java17-springboot3 -name "*.java" | wc -l)

echo "Java文件总数: $JAVA_FILES"

# 检查非UTF-8文件
NON_UTF8=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
if [ "$NON_UTF8" -gt 0 ]; then
    echo "❌ 发现 $NON_UTF8 个非UTF-8编码文件"
    find smart-admin-api-java17-springboot3 -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII"
    ENCODING_ISSUES=$((ENCODING_ISSUES + NON_UTF8))
fi

# 检查BOM
BOM_FILES=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; 2>/dev/null | wc -l)
if [ "$BOM_FILES" -gt 0 ]; then
    echo "❌ 发现 $BOM_FILES 个包含BOM的文件"
    ENCODING_ISSUES=$((ENCODING_ISSUES + BOM_FILES))
fi

# 检查乱码模式
GARBAGE_COUNT=0
for pattern in "????" "????" "涓?" "鏂?"; do
    count=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "$pattern" {} \; 2>/dev/null | wc -l)
    if [ "$count" -gt 0 ]; then
        echo "❌ 发现乱码模式 '$pattern': $count 个文件"
        GARBAGE_COUNT=$((GARBAGE_COUNT + count))
        ENCODING_ISSUES=$((ENCODING_ISSUES + count))
    fi
done

if [ "$ENCODING_ISSUES" -eq 0 ]; then
    echo "✅ 所有文件编码验证通过！"
    exit 0
else
    echo "❌ 编码验证失败！发现问题文件: $ENCODING_ISSUES"
    exit 1
fi
