#!/bin/bash

echo "开始修复乱码文件..."

# 创建已修复文件列表
fixed_files="fixed_files.txt"
echo "" > $fixed_files

# 从检测脚本输出中提取文件路径
while IFS= read -r line; do
    if [[ $line =~ ^可能包含乱码:\ (.+)$ ]]; then
        file="${BASH_REMATCH[1]}"
        echo "正在处理文件: $file"
        
        if [ -f "$file" ]; then
            # 备份原文件
            cp "$file" "$file.backup"
            
            # 尝试使用iconv转换编码
            # 首先尝试GBK转UTF-8
            if iconv -f GBK -t UTF-8 "$file" > "$file.tmp" 2>/dev/null; then
                # 检查转换结果
                if [ -s "$file.tmp" ]; then
                    mv "$file.tmp" "$file"
                    echo "$file" >> $fixed_files
                    echo "  ✓ 成功修复 (GBK->UTF-8)"
                else
                    rm "$file.tmp"
                    echo "  ✗ 转换失败"
                fi
            else
                # 尝试其他编码
                if iconv -f GB18030 -t UTF-8 "$file" > "$file.tmp" 2>/dev/null; then
                    if [ -s "$file.tmp" ]; then
                        mv "$file.tmp" "$file"
                        echo "$file" >> $fixed_files
                        echo "  ✓ 成功修复 (GB18030->UTF-8)"
                    else
                        rm "$file.tmp"
                        echo "  ✗ 转换失败"
                    fi
                else
                    # 如果iconv失败，尝试其他方法
                    echo "  ✗ 无法自动转换，需要手动处理"
                fi
            fi
        else
            echo "  ✗ 文件不存在"
        fi
    fi
done < <(grep "可能包含乱码:" /tmp/claude/tasks/b27969c.output)

echo ""
echo "修复完成！"
echo "已修复文件数: $(wc -l < $fixed_files)"
echo "详细修复记录保存在: $fixed_files"
