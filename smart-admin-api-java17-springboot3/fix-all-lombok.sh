#!/bin/bash
echo "开始全面修复所有Lombok依赖问题..."

# 找到所有使用Lombok的Java文件
find sa-admin/src/main/java -name "*.java" -exec grep -l "lombok\." {} \; > lombok_files.txt

echo "找到 $(wc -l < lombok_files.txt) 个使用Lombok的文件"

# 批量处理每个文件
while read file; do
    echo "处理文件: $file"

    # 备份原文件
    cp "$file" "$file.backup"

    # 移除Lombok导入
    sed -i '/^import lombok\./d' "$file"

    # 移除Lombok注解
    sed -i 's/@Data//g' "$file"
    sed -i 's/@Builder//g' "$file"
    sed -i 's/@NoArgsConstructor//g' "$file"
    sed -i 's/@AllArgsConstructor//g' "$file"
    sed -i 's/@RequiredArgsConstructor//g' "$file"
    sed -i 's/@ToString//g' "$file"
    sed -i 's/@EqualsAndHashCode//g' "$file"
    sed -i 's/@Getter//g' "$file"
    sed -i 's/@Setter//g' "$file"

    echo "已清理Lombok注解: $file"

done < lombok_files.txt

echo "Lombok清理完成！"
echo "注意：手动添加缺失的getter/setter方法可能是必要的。"

# 删除临时文件
rm -f lombok_files.txt

echo "Lombok全面修复完成！"