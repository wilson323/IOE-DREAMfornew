#!/bin/bash

echo "🔍 编码持续监控检查"
echo "=================="

# 检查最近修改的文件
echo "检查最近1小时内修改的文件..."
MODIFIED_FILES=$(find smart-admin-api-java17-springboot3 -name "*.java" -mmin -60 | wc -l)
echo "最近修改文件数: $MODIFIED_FILES"

if [ "$MODIFIED_FILES" -gt 0 ]; then
    echo "最近修改的文件:"
    find smart-admin-api-java17-springboot3 -name "*.java" -mmin -60 -exec file {} \; | grep -v "UTF-8"
fi

# 快速乱码检查
echo ""
echo "快速乱码检查..."
QUICK_CHECK=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "????" {} \; 2>/dev/null | wc -l)
if [ "$QUICK_CHECK" -gt 0 ]; then
    echo "⚠️ 发现 $QUICK_CHECK 个文件包含乱码"
    find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "????" {} \; 2>/dev/null | head -5
else
    echo "✅ 未发现乱码文件"
fi

# 编码质量评分
echo ""
echo "编码质量评分:"
TOTAL_FILES=$(find smart-admin-api-java17-springboot3 -name "*.java" | wc -l)
UTF8_FILES=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec file {} \; | grep -c "UTF-8" | wc -l)
GARBAGE_FILES=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "????" {} \; 2>/dev/null | wc -l)

SCORE=$((UTF8_FILES - GARBAGE_FILES))
MAX_SCORE=$TOTAL_FILES

if [ "$MAX_SCORE" -gt 0 ]; then
    QUALITY_SCORE=$((SCORE * 100 / MAX_SCORE))
    echo "编码质量评分: $QUALITY_SCORE/100"

    if [ "$QUALITY_SCORE" -ge 95 ]; then
        echo "✅ 编码质量: 优秀"
    elif [ "$QUALITY_SCORE" -ge 85 ]; then
        echo "✅ 编码质量: 良好"
    elif [ "$QUALITY_SCORE" -ge 70 ]; then
        echo "⚠️ 编码质量: 一般"
    else
        echo "❌ 编码质量: 需要改进"
    fi
else
    echo "无法计算编码质量评分"
fi

echo ""
echo "监控完成时间: $(date)"
