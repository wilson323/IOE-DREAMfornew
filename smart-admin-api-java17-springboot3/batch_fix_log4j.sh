#!/bin/bash

echo "开始批量修复 @Slf4j 注解问题..."

cd /d/IOE-DREAM/smart-admin-api-java17-springboot3

# 查找所有使用log变量的Java文件
find . -name "*.java" -exec grep -l "log\." {} \; > files_with_log.txt

while IFS= read -r file; do
    file_path="${file//.\//}"

    if [ -f "$file_path" ]; then
        # 检查是否已经有@Slf4j注解
        if ! grep -q "@Slf4j" "$file_path" && grep -q "log\." "$file_path"; then
            echo "添加 @Slf4j 到: $file_path"

            # 检查是否已经有lombok.extern.slf4j import
            if ! grep -q "import lombok.extern.slf4j.Slf4j" "$file_path"; then
                # 在最后一个import后添加lombok import
                sed -i '/^import /h; ${x;s/.*/&\nimport lombok.extern.slf4j.Slf4j;/;x;}' "$file_path"
            fi

            # 在class声明前添加@Slf4j注解
            sed -i '/^public class/i@Slf4j\n' "$file_path"
        fi
    fi
done < files_with_log.txt

echo "@Slf4j 注解修复完成"