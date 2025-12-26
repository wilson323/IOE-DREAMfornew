#!/bin/bash

# 最终日志模式修复脚本
# 处理所有剩余的未修复文件
# 作者: IOE-DREAM Team
# 版本: 3.0.0

echo "=== 最终日志模式修复 ==="
echo "处理所有剩余的未修复文件"

# 查找所有需要修复的文件
echo "查找需要修复的文件..."
files_to_fix=()
while IFS= read -r -d '' file; do
    files_to_fix+=("$file")
done < <(find microservices -name "*.java" -type f -exec grep -l "import org.slf4j.Logger" {} \; -print0)

total_files=${#files_to_fix[@]}
echo "找到 $total_files 个需要修复的文件"

if [ $total_files -eq 0 ]; then
    echo "✓ 所有文件都已修复完成！"
    exit 0
fi

fixed_count=0

# 逐个修复文件
for file in "${files_to_fix[@]}"; do
    echo "修复: $file"

    # 创建临时文件
    temp_file=$(mktemp)

    # 逐行处理文件
    while IFS= read -r line; do
        # 替换import语句
        if [[ $line =~ ^[[:space:]]*import[[:space:]]+org\.slf4j\.Logger[[:space:]]*;[[:space:]]*$ ]]; then
            echo "import lombok.extern.slf4j.Slf4j;" >> "$temp_file"
        # 删除LoggerFactory import行
        elif [[ $line =~ ^[[:space:]]*import[[:space:]]+org\.slf4j\.LoggerFactory[[:space:]]*;[[:space:]]*$ ]]; then
            # 跳过这行
            :
        # 删除Logger声明行
        elif [[ $line =~ ^[[:space:]]*private[[:space:]]+static[[:space:]]+final[[:space:]]+Logger[[:space:]]+log[[:space:]]*=.*LoggerFactory\.getLogger.*\;[[:space:]]*$ ]]; then
            # 跳过这行
            :
        else
            echo "$line" >> "$temp_file"
        fi
    done < "$file"

    # 在类声明前添加@Slf4j注解
    final_temp_file=$(mktemp)
    class_found=false

    while IFS= read -r line; do
        # 检查是否是类声明
        if [[ $line =~ ^[[:space:]]*(public|protected|private)?[[:space:]]*(abstract|final)?[[:space:]]*class[[:space:]]+ ]] && ! $class_found; then
            class_found=true
            # 获取缩进
            indent=$(echo "$line" | sed -n 's/^\([[:space:]]*\).*$/\1/p')
            # 添加@Slf4j注解
            echo "${indent}@Slf4j" >> "$final_temp_file"
        fi
        echo "$line" >> "$final_temp_file"
    done < "$temp_file"

    # 替换原文件
    mv "$final_temp_file" "$file"
    rm -f "$temp_file"

    echo "✓ 修复完成"
    ((fixed_count++))
done

echo ""
echo "=== 修复完成 ==="
echo "修复的文件数量: $fixed_count/$total_files"
echo ""

# 最终验证
echo "=== 最终验证 ==="
remaining_files=$(find microservices -name "*.java" -type f -exec grep -l "import org.slf4j.Logger" {} \;)
if [ -z "$remaining_files" ]; then
    echo "✓ 没有发现剩余的Logger导入"
else
    echo "✗ 仍有 $(echo "$remaining_files" | wc -l) 个文件包含Logger导入"
    echo "$remaining_files" | sed 's/^/  /'
fi

echo ""
echo "=== 验证LoggerFactory使用 ==="
factory_files=$(find microservices -name "*.java" -type f -exec grep -l "LoggerFactory.getLogger" {} \;)
if [ -z "$factory_files" ]; then
    echo "✓ 没有发现LoggerFactory使用"
else
    echo "✗ 仍有 $(echo "$factory_files" | wc -l) 个文件使用LoggerFactory"
    echo "$factory_files" | sed 's/^/  /'
fi

echo ""
echo "=== 统计@Slf4j使用情况 ==="
slf4j_files=$(find microservices -name "*.java" -type f -exec grep -l "@Slf4j" {} \;)
echo "使用 @Slf4j 的文件数量: $(echo "$slf4j_files" | wc -l)"

echo ""
if [ -z "$remaining_files" ] && [ -z "$factory_files" ]; then
    echo "🎉 恭喜！所有文件的日志模式都已成功转换为 @Slf4j 注解！"
else
    echo "⚠️ 还有少量文件需要手动处理"
fi