#!/bin/bash

# 全局BOM修复脚本
# 修复所有微服务中的BOM字符问题

echo "开始全局BOM字符修复..."

# 遍历所有微服务目录
for service_dir in microservices/ioedream-*; do
    if [ -d "$service_dir" ]; then
        service_name=$(basename "$service_dir")
        echo "正在修复 $service_name..."

        # 查找并修复所有Java文件
        find "$service_dir" -name "*.java" -type f | while read file; do
            # 移除BOM字符
            sed -i '1s/^\xEF\xBB\xBF//' "$file"
            # 修复package声明
            sed -i 's/^ackage/package/' "$file"
            # 修复import声明
            sed -i 's/^iimport/import/g' "$file"
        done

        echo "✓ $service_name 修复完成"
    fi
done

echo "全局BOM字符修复完成!"