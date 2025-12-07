#!/bin/bash

echo "🔄 回滚RESTful重构..."

BACKUP_DIR="restful_refactor_backup_*"
LATEST_BACKUP=$(ls -td $BACKUP_DIR 2>/dev/null | head -1)

if [ -z "$LATEST_BACKUP" ]; then
    echo "❌ 未找到备份目录"
    exit 1
fi

echo "📋 从备份目录恢复: $LATEST_BACKUP"

find "$LATEST_BACKUP" -name "*.java" | while read backup_file; do
    if [ -f "$backup_file" ]; then
        # 计算相对路径
        relative_path=$(echo "$backup_file" | sed "s|$LATEST_BACKUP/||")
        original_file="${relative_path//_/\/}"

        echo "恢复: $backup_file → $original_file"
        cp "$backup_file" "$original_file"
    fi
done

echo ""
echo "✅ 回滚完成"
echo "📊 所有文件已恢复到重构前状态"
