#!/bin/bash

echo "🔄 恢复Repository备份文件..."

# 恢复所有备份文件
find . -name "*.backup" | while read backup_file; do
    original_file="${backup_file%.backup}"
    echo "恢复: $backup_file → $original_file"
    cp "$backup_file" "$original_file"
done

echo "✅ 恢复完成"
