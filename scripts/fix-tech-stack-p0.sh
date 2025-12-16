#!/bin/bash
# IOE-DREAM P0级技术栈修复脚本
# 修复关键的技术栈不一致问题

echo "🔧 开始执行P0级技术栈修复..."

# 记录修复日志
LOG_FILE="documentation/TECHNOLOGY_STACK_P0_FIX_LOG.md"
echo "# 🔧 P0级技术栈修复执行日志" > $LOG_FILE
echo "" >> $LOG_FILE
echo "执行时间: $(date)" >> $LOG_FILE
echo "执行范围: documentation/" >> $LOG_FILE
echo "" >> $LOG_FILE

# 创建备份目录
BACKUP_DIR="documentation_backup_p0_$(date +%Y%m%d_%H%M%S)"
echo "📦 创建备份目录: $BACKUP_DIR" >> $LOG_FILE
mkdir -p "$BACKUP_DIR"

# 修复函数
fix_spring_cloud_alibaba() {
    echo "🔧 修复1: 统一Spring Cloud Alibaba版本到2025.0.0.0" >> $LOG_FILE

    # 查找需要修复的文件
    files_to_fix=$(find documentation -name "*.md" -o -name "*.yml" -o -name "*.yaml" -o -name "*.xml" | \
        xargs grep -l "spring-cloud-alibaba.*2022\|spring-cloud-alibaba.*2023\|spring-cloud-alibaba.*2024" 2>/dev/null)

    fixed_count=0

    if [ -n "$files_to_fix" ]; then
        echo "找到需要修复的文件:" >> $LOG_FILE
        echo "$files_to_fix" >> $LOG_FILE
        echo "" >> $LOG_FILE

        for file in $files_to_fix; do
            if [ -f "$file" ]; then
                # 备份原文件
                cp "$file" "$BACKUP_DIR/$(basename "$file").backup"

                # 修复版本号
                sed -i 's/spring-cloud-alibaba.*2022\.[0-9]\+\.[0-9]\+\.[0-9]\+/spring-cloud-alibaba-2025.0.0.0/g' "$file"
                sed -i 's/spring-cloud-alibaba.*2023\.[0-9]\+\.[0-9]\+\.[0-9]\+/spring-cloud-alibaba-2025.0.0.0/g' "$file"
                sed -i 's/spring-cloud-alibaba.*2024\.[0-9]\+\.[0-9]\+\.[0-9]\+/spring-cloud-alibaba-2025.0.0.0/g' "$file"
                sed -i 's/2022\.0\.0\.0/2025.0.0.0/g' "$file"
                sed -i 's/2023\.[0-9]\+\.[0-9]+\.[0-9]+/2025.0.0.0/g' "$file"
                sed -i 's/2024\.[0-9]\+\.[0-9]+\.[0-9]+/2025.0.0.0/g' "$file"

                echo "✅ 修复文件: $file" >> $LOG_FILE
                ((fixed_count++))
            fi
        done
    else
        echo "✅ 未找到需要修复的Spring Cloud Alibaba文件" >> $LOG_FILE
    fi

    echo "Spring Cloud Alibaba版本修复完成，共修复 $fixed_count 个文件" >> $LOG_FILE
    echo "" >> $LOG_FILE

    return $fixed_count
}

fix_spring_boot_version() {
    echo "🔧 修复2: 移除Spring Boot硬编码版本" >> $LOG_FILE

    # 查找包含硬编码Spring Boot版本的文件
    files_to_fix=$(find documentation -name "*.md" | \
        xargs grep -l "Spring Boot [0-9]\+\.[0-9]\+\.[0-9]\+" 2>/dev/null | \
        grep -v "3\.5\.8")

    fixed_count=0

    if [ -n "$files_to_fix" ]; then
        echo "找到需要修复的Spring Boot版本文件:" >> $LOG_FILE
        echo "$files_to_fix" >> $LOG_FILE
        echo "" >> $LOG_FILE

        for file in $files_to_fix; do
            if [ -f "$file" ]; then
                # 备份原文件
                cp "$file" "$BACKUP_DIR/$(basename "$file").backup"

                # 统一替换为标准版本
                sed -i 's/Spring Boot [0-9]\+\.[0-9]\+\.[0-9]\+/Spring Boot 3.5.8/g' "$file"
                sed -i 's/Spring Boot 3\.2\.[0-9]\+/Spring Boot 3.5.8/g' "$file"
                sed -i 's/Spring Boot 3\.3\.[0-9]\+/Spring Boot 3.5.8/g' "$file"
                sed -i 's/Spring Boot 3\.4\.[0-9]\+/Spring Boot 3.5.8/g' "$file"

                echo "✅ 修复文件: $file" >> $LOG_FILE
                ((fixed_count++))
            fi
        done
    else
        echo "✅ 未找到需要修复的Spring Boot版本文件" >> $LOG_FILE
    fi

    echo "Spring Boot版本修复完成，共修复 $fixed_count 个文件" >> $LOG_FILE
    echo "" >> $LOG_FILE

    return $fixed_count
}

fix_mysql_version() {
    echo "🔧 修复3: 统一MySQL版本到8.0+" >> $LOG_FILE

    # 查找包含MySQL 5.7的文件
    files_to_fix=$(find documentation -name "*.md" -o -name "*.yml" -o -name "*.yaml" | \
        xargs grep -l "MySQL 5\.7" 2>/dev/null)

    fixed_count=0

    if [ -n "$files_to_fix" ]; then
        echo "找到需要修复的MySQL版本文件:" >> $LOG_FILE
        echo "$files_to_fix" >> $LOG_FILE
        echo "" >> $LOG_FILE

        for file in $files_to_fix; do
            if [ -f "$file" ]; then
                # 备份原文件
                cp "$file" "$BACKUP_DIR/$(basename "$file").backup"

                # 统一替换为MySQL 8.0
                sed -i 's/MySQL 5\.7\.[0-9]*/MySQL 8.0/g' "$file"
                sed -i 's/mysql 5\.7\.[0-9]*/mysql 8.0/g' "$file"
                sed -i 's/MySQL5\.7\.[0-9]*/MySQL 8.0/g' "$file"

                echo "✅ 修复文件: $file" >> $LOG_FILE
                ((fixed_count++))
            fi
        done
    else
        echo "✅ 未找到需要修复的MySQL版本文件" >> $LOG_FILE
    fi

    echo "MySQL版本修复完成，共修复 $fixed_count 个文件" >> $LOG_FILE
    echo "" >> $LOG_FILE

    return $fixed_count
}

# 执行修复
echo "开始执行修复..." >> $LOG_FILE

fix1_count=$(fix_spring_cloud_alibaba)
fix2_count=$(fix_spring_boot_version)
fix3_count=$(fix_mysql_version)

# 统计修复结果
total_fixed=$((fix1_count + fix2_count + fix3_count))

echo "📊 修复统计:" >> $LOG_FILE
echo "- Spring Cloud Alibaba版本修复: $fix1_count 个文件" >> $LOG_FILE
echo "- Spring Boot硬编码版本修复: $fix2_count 个文件" >> $LOG_FILE
echo "- MySQL版本统一修复: $fix3_count 个文件" >> $LOG_FILE
echo "- 总计修复: $total_fixed 个文件" >> $LOG_FILE
echo "" >> $LOG_FILE

# 生成修复摘要
echo "## 🎯 P0级技术栈修复摘要" >> $LOG_FILE
echo "" >> $LOG_FILE
echo "✅ **修复完成时间**: $(date)" >> $LOG_FILE
echo "✅ **备份目录**: $BACKUP_DIR" >> $LOG_FILE
echo "✅ **修复文件数**: $total_fixed 个" >> $LOG_FILE
echo "" >> $LOG_FILE
echo "### 修复详情" >> $LOG_FILE
echo "1. **Spring Cloud Alibaba**: 统一升级到 2025.0.0.0 版本" >> $LOG_FILE
echo "2. **Spring Boot**: 移除硬编码版本，统一使用 3.5.8" >> $LOG_FILE
echo "3. **MySQL**: 统一升级到 8.0+ 版本" >> $LOG_FILE
echo "" >> $LOG_FILE
echo "### 下一步建议" >> $LOG_FILE
echo "1. 验证修复结果" >> $LOG_FILE
echo "2. 执行P1级别修复" >> $LOG_FILE
echo "3. 建立持续监控机制" >> $LOG_FILE

echo "🎉 P0级技术栈修复执行完成！"
echo "📋 修复日志: $LOG_FILE"
echo "📦 备份目录: $BACKUP_DIR"
echo "✅ 总计修复: $total_fixed 个文件"