#!/bin/bash

echo "开始批量修复 Logger 冲突问题..."

cd /d/IOE-DREAM/smart-admin-api-java17-springboot3

# 查找所有使用@Slf4j注解的文件
find . -name "*.java" -exec grep -l "@Slf4j" {} \; > files_with_sl4j.txt

while IFS= read -r file; do
    file_path="${file//.\//}"

    if [ -f "$file_path" ]; then
        # 检查是否同时有手动Logger定义
        if grep -q "LoggerFactory.getLogger" "$file_path"; then
            echo "修复 Logger 冲突: $file_path"

            # 删除手动Logger的import语句
            sed -i '/import org.slf4j.Logger;/d' "$file_path"
            sed -i '/import org.slf4j.LoggerFactory;/d' "$file_path"

            # 删除手动Logger的变量定义
            sed -i '/private static final Logger log = LoggerFactory.getLogger/d' "$file_path"
            sed -i '/private final Logger log = LoggerFactory.getLogger/d' "$file_path"
        fi
    fi
done < files_with_sl4j.txt

echo "Logger 冲突修复完成"