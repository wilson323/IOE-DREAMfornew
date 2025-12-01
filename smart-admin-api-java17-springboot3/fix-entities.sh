#!/bin/bash

# 批量修复Entity类继承BaseEntity的脚本
# 老王专用 - 根源性解决重复代码问题

echo "=== 开始批量修复Entity继承问题 ==="

# 查找所有需要修复的Entity文件
ENTITY_FILES=$(find . -name "*Entity.java" -exec grep -L "extends BaseEntity" {} \; 2>/dev/null)

for file in $ENTITY_FILES; do
    echo "处理文件: $file"

    # 检查是否包含重复的基础字段
    if grep -q "createTime\|updateTime\|deletedFlag" "$file"; then
        echo "  发现重复字段，需要修复..."

        # 1. 添加BaseEntity导入
        sed -i 's/import lombok.Data;/import lombok.Data;\nimport net.lab1024.sa.base.common.entity.BaseEntity;/' "$file"

        # 2. 替换类声明
        sed -i 's/public class \([A-Za-z]*Entity\) {/public class \1 extends BaseEntity {/' "$file"

        # 3. 删除重复的基础字段（需要更复杂的处理）
        # 这里先标记，稍后手动处理复杂情况
        echo "  需要手动清理重复字段: $file"
    else
        echo "  添加BaseEntity继承..."

        # 添加BaseEntity导入和继承
        sed -i 's/import lombok.Data;/import lombok.Data;\nimport net.lab1024.sa.base.common.entity.BaseEntity;/' "$file"
        sed -i 's/public class \([A-Za-z]*Entity\) {/public class \1 extends BaseEntity {/' "$file"
    fi
done

echo "=== 批量修复完成 ==="
echo "请检查并手动清理重复字段的文件"