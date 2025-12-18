#!/bin/bash

# 检测文件中的UTF-8乱码字符
echo "开始检测乱码文件..."
found_count=0

while IFS= read -r file; do
    if [ -f "$file" ]; then
        # 检查是否包含常见的UTF-8乱码模式
        if grep -q $'\xef\xbf\xbd' "$file" 2>/dev/null; then
            echo "包含UTF-8替换字符: $file"
            ((found_count++))
        elif file "$file" | grep -q -i "iso-8859\|windows-1252\|unknown-8bit"; then
            echo "非UTF-8编码: $file"
            ((found_count++))
        # 检查中文字符是否变成乱码
        elif LC_ALL=C grep -q '[\xc0-\xff][\x80-\xbf]' "$file" 2>/dev/null; then
            echo "可能包含乱码: $file"
            ((found_count++))
        fi
    fi
done < "D:\IOE-DREAM\garbled-files-list-clean.txt"

echo "检测完成，发现 $found_count 个可能乱码的文件"
