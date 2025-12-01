#!/bin/bash

echo "开始批量修复 Resource import 问题..."

cd /d/IOE-DREAM/smart-admin-api-java17-springboot3

# 读取文件列表
while IFS= read -r file; do
    # 转换Windows路径格式
    file_path="${file//.\//}"

    if [ -f "$file_path" ]; then
        # 检查是否已经有Resource import
        if ! grep -q "import jakarta.annotation.Resource" "$file_path" && grep -q "@Resource" "$file_path"; then
            echo "添加 Resource import 到: $file_path"
            # 在package行后添加import
            sed -i '/^package/a\\nimport jakarta.annotation.Resource;' "$file_path"
        fi
    fi
done < files_with_resource.txt

echo "Resource import 修复完成"