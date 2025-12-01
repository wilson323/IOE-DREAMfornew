#!/bin/bash

# 针对性乱码快速修复脚本
# 基于快速检查发现的20个包含"搴忓垪鍖栧"的文件

echo "🎯 针对性乱码快速修复..."
echo "目标：修复包含 '搴忓垪鍖栧'、'涓讳换'、'鍗撳ぇ' 的文件"

# 修复最常见的模式
echo "步骤1: 修复 '搴忓垪鍖栧' → '序列化'"
find . -name "*.java" -exec sed -i 's/搴忓垪鍖栧/序列化/g' {} \;

echo "步骤2: 修复 '涓讳换.*鍗撳ぇ' → '设计模式'"
find . -name "*.java" -exec sed -i 's/涓讳换.*鍗撳ぇ/设计模式/g' {} \;

echo "步骤3: 修复 '涓讳换' → '设计'"
find . -name "*.java" -exec sed -i 's/涓讳换/设计/g' {} \;

echo "步骤4: 修复 '鍗撳ぇ' → '模式'"
find . -name "*.java" -exec sed -i 's/鍗撳ぇ/模式/g' {} \;

echo "步骤5: 修复其他常见乱码"
find . -name "*.java" -exec sed -i 's/鍒涙柊瀹為獙瀹/1024创新实验室/g' {} \;
find . -name "*.java" -exec sed -i 's/闂撮殧/心跳/g' {} \;
find . -name "*.java" -exec sed -i 's/娑堣/消费/g' {} \;
find . -name "*.java" -exec sed -i 's/璁哄/访问/g' {} \;
find . -name "*.java" -exec sed -i 's/搴撳崟/统计/g' {} \;
find . -name "*.java" -exec sed -i 's/鏍锋鏋/智能/g' {} \;
find . -name "*.java" -exec sed -i 's/鎵愭崐/设备/g' {} \;
find . -name "*.java" -exec sed -i 's/杩戞/系统/g' {} \;

# 验证修复效果
echo ""
echo "🔍 验证修复效果："
remaining_serialization=$(find . -name "*.java" -exec grep -l "搴忓垪鍖栧" {} \; | wc -l)
remaining_sheji=$(find . -name "*.java" -exec grep -l "涓讳换" {} \; | wc -l)
remaining_moshi=$(find . -name "*.java" -exec grep -l "鍗撳ぇ" {} \; | wc -l)

echo "剩余 '搴忓垪鍖栧' 文件: $remaining_serialization 个"
echo "剩余 '涓讳换' 文件: $remaining_sheji 个"
echo "剩余 '鍗撳ぇ' 文件: $remaining_moshi 个"

total_remaining=$((remaining_serialization + remaining_sheji + remaining_moshi))
echo "总计剩余: $total_remaining 个"

if [ $total_remaining -eq 0 ]; then
    echo "✅ 针对性修复完成！所有目标乱码已清除"
else
    echo "⚠️ 仍有 $total_remaining 个文件需要手动检查"
fi

echo "🎉 针对性乱码修复脚本执行完成"