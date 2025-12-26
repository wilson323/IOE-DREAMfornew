#!/bin/bash

echo "开始批量修复LoggerFactory到@Slf4j转换..."

# 查找所有包含LoggerFactory.getLogger的Java文件
find "D:\IOE-DREAM\microservices" -name "*.java" -exec grep -l "LoggerFactory\.getLogger" {} \; > /tmp/files_to_fix.txt

total_files=$(wc -l < /tmp/files_to_fix.txt)
echo "找到 $total_files 个文件需要修复"

fixed_count=0
while IFS= read -r file; do
    echo "正在修复: $file"

    # 1. 移除LoggerFactory导入
    sed -i '/import org\.slf4j\.LoggerFactory;/d' "$file"
    sed -i '/import org\.slf4j\.Logger;/d' "$file"

    # 2. 添加lombok导入（在package后）
    if ! grep -q "import lombok.extern.slf4j.Slf4j;" "$file"; then
        sed -i '/^package /a\\nimport lombok.extern.slf4j.Slf4j;' "$file"
    fi

    # 3. 移除Logger声明
    sed -i '/private static final Logger log = LoggerFactory\.getLogger/d' "$file"

    # 4. 在类声明前添加@Slf4j注解
    # 查找第一个public class行并添加@Slf4j注解
    class_line=$(grep -n "^public class" "$file" | head -1 | cut -d: -f1)
    if [ -n "$class_line" ]; then
        sed -i "${class_line}i\\@Slf4j" "$file"
    fi

    ((fixed_count++))
    echo "已修复: $file ($fixed_count/$total_files)"

done < /tmp/files_to_fix.txt

echo "批量修复完成！共修复 $fixed_count 个文件"
rm -f /tmp/files_to_fix.txt