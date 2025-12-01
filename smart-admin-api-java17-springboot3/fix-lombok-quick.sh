#!/bin/bash
echo "快速修复Lombok依赖问题..."

# 需要立即修复的关键文件列表
critical_files=(
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo/ConsumeStatistics.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo/GlobalLimitConfig.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo/LimitCheckResult.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo/BatchLimitSetResult.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo/BatchLimitSetting.java"
)

for file in "${critical_files[@]}"; do
    if [ -f "$file" ]; then
        echo "正在修复: $file"
        # 移除Lombok导入
        sed -i '/import lombok\./d' "$file"
        # 移除Lombok注解
        sed -i 's/@Data//g' "$file"
        sed -i 's/@Builder//g' "$file"
        sed -i 's/@NoArgsConstructor//g' "$file"
        sed -i 's/@AllArgsConstructor//g' "$file"
        echo "已修复: $file"
    else
        echo "文件不存在: $file"
    fi
done

echo "Lombok快速修复完成！"