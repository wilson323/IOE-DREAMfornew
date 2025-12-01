#!/bin/bash

# Lombok日志注解修复脚本
# 修复同时使用手动Logger导入和@Slf4j注解的冲突

echo "🔧 开始修复Lombok日志注解冲突..."

# 查找所有冲突的文件
find . -name "*.java" -exec grep -l "import.*Logger.*\|import.*LoggerFactory" {} \; | xargs grep -l "@Slf4j" > conflict_files.txt

echo "📊 发现 $(cat conflict_files.txt | wc -l) 个文件存在日志注解冲突"

# 修复每个文件
while IFS= read -r file; do
    echo "🔨 修复文件: $file"

    # 备份原文件
    cp "$file" "$file.backup"

    # 移除Logger相关导入
    sed -i '/import org\.slf4j\.Logger;/d' "$file"
    sed -i '/import org\.slf4j\.LoggerFactory;/d' "$file"

    # 移除手动logger变量声明
    sed -i '/private static final Logger log = LoggerFactory\.getClass();/d' "$file"
    sed -i '/private static final Logger log = LoggerFactory\.getLogger.*;/d' "$file"
    sed -i '/private final Logger log = LoggerFactory\.getLogger.*;/d' "$file"

    echo "✅ 修复完成: $file"
done < conflict_files.txt

echo "🎉 所有文件修复完成！"
echo "📁 备份文件已创建，可通过.backup后缀找回原始文件"

# 清理临时文件
rm conflict_files.txt

echo "🔄 验证修复结果..."
conflict_count=$(find . -name "*.java" -exec grep -l "import.*Logger.*\|import.*LoggerFactory" {} \; | xargs grep -l "@Slf4j" | wc -l)
echo "📊 剩余冲突文件数: $conflict_count"

if [ $conflict_count -eq 0 ]; then
    echo "🎉 所有冲突已修复！"
else
    echo "⚠️ 仍有 $conflict_count 个文件存在冲突，需要手动检查"
fi