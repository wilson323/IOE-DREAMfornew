#!/bin/bash
# 编码质量守护脚本 - 零容忍政策执行
echo "🔍 执行编码质量守护检查..."

PROJECT_ROOT="D:\IOE-DREAM"
cd "$PROJECT_ROOT"

# UTF-8编码检查
utf8_violations=0
utf8_violations=$(find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
if [ $utf8_violations -gt 0 ]; then
    echo "❌ UTF-8编码违规: $utf8_violations 个文件"
    exit 1
fi

# BOM标记检查
bom_violations=0
bom_violations=$(find . -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; 2>/dev/null | wc -l)
if [ $bom_violations -gt 0 ]; then
    echo "❌ BOM标记违规: $bom_violations 个文件"
    exit 1
fi

# 乱码字符检查
garbage_violations=0
garbage_patterns=("????" "涓?" "鏂?" "锟斤拷")
for pattern in "${garbage_patterns[@]}"; do
    pattern_files=$(find . -name "*.java" -exec grep -l "$pattern" {} \; 2>/dev/null | wc -l)
    garbage_violations=$((garbage_violations + pattern_files))
done
if [ $garbage_violations -gt 0 ]; then
    echo "❌ 乱码字符违规: $garbage_violations 个文件"
    exit 1
fi

echo "✅ 编码质量检查通过"
exit 0
