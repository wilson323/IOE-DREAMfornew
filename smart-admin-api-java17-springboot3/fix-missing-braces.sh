#!/bin/bash

# 修复缺少类结束符的Java文件
echo "开始修复缺少类结束符的Java文件..."

# 获取所有有编译错误的文件
error_files=$(mvn compile 2>&1 | grep "\[ERROR\].*\.java:" | sed 's/.*\[ERROR\].*\(.*\.java\):.*/\1/' | sort -u)

for file in $error_files; do
    if [ -f "$file" ]; then
        echo "检查文件: $file"
        
        # 检查文件是否以 } 结尾
        last_char=$(tail -c 1 "$file")
        if [ "$last_char" != "}" ]; then
            echo "  -> 缺少结束符，添加 }"
            echo "}" >> "$file"
        else
            echo "  -> 文件正常"
        fi
    fi
done

echo "修复完成！"
