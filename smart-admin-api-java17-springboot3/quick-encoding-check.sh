#!/bin/bash

# 快速编码检查和修复脚本
echo "🔍 快速编码检查和修复..."

# 检查最常见的乱码模式
echo "检查最常见乱码模式："

patterns=("鍒涙柊瀹為獙瀹" "搴忓垪鍖栧" "涓讳换" "鍗撳ぇ" "闂撮殧" "娑堣" "璁哄" "搴撳崟" "鏍锋鏋" "鎵愭崐")

total_found=0
for pattern in "${patterns[@]}"; do
    count=$(find . -name "*.java" -exec grep -l "$pattern" {} \; | wc -l)
    if [ $count -gt 0 ]; then
        echo "发现模式 '$pattern': $count 个文件"
        ((total_found += count))
    fi
done

echo "总计发现乱码文件: $total_found 个"

if [ $total_found -eq 0 ]; then
    echo "✅ 未发现常见乱码模式，编码状况良好！"
else
    echo "⚠️ 发现 $total_found 个文件包含乱码，建议运行完整修复脚本"
fi

# 检查文件编码
echo ""
echo "检查文件编码类型："
non_utf8=$(find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
echo "非UTF-8/ASCII文件数量: $non_utf8"

if [ $non_utf8 -gt 0 ]; then
    echo "发现非UTF-8文件："
    find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | head -5
fi

echo "✅ 快速编码检查完成"