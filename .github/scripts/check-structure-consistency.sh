#!/bin/bash
# P1：工程结构一致性检查脚本
# 验证包结构与目录结构的对齐性

set -euo pipefail

echo "🔍 开始P1：工程结构一致性检查..."

# 日志文件
LOG_FILE="structure-violations.log"
echo "IOE-DREAM 工程结构一致性检查报告" > "$LOG_FILE"
echo "检查时间: $(date -u +"%Y-%m-%d %H:%M:%S UTC")" >> "$LOG_FILE"
echo "======================================" >> "$LOG_FILE"

VIOLATION_COUNT=0
MAX_VIOLATIONS=0

# 检查函数
check_package_alignment() {
    local service_dir="$1"
    local service_name=$(basename "$service_dir")

    if [ ! -d "$service_dir/src/main/java" ]; then
        echo "⚠️  跳过 $service_name：未找到Java源代码目录"
        return
    fi

    echo "📋 检查 $service_name 的包结构一致性..." | tee -a "$LOG_FILE"

    # 查找所有Java文件并检查包声明
    find "$service_dir/src/main/java" -name "*.java" -type f | while read -r java_file; do
        # 获取相对路径
        rel_path=$(echo "$java_file" | sed "s|$service_dir/src/main/java/||")

        # 提取包名
        package_line=$(grep -m1 "^package " "$java_file" | head -1)
        if [ -n "$package_line" ]; then
            package_name=$(echo "$package_line" | sed 's/package //; s/;//; s/[[:space:]]//g')

            # 预期包路径
            expected_pkg_path=$(echo "$rel_path" | sed 's|/[^/]*\.java$||; s|/|.|g')

            # 比较包名和路径
            if [ "$package_name" != "$expected_pkg_path" ]; then
                echo "❌ 包结构不一致：$java_file" >> "$LOG_FILE"
                echo "   声明包: $package_name" >> "$LOG_FILE"
                echo "   预期包: $expected_pkg_path" >> "$LOG_FILE"
                echo "   文件路径: $rel_path" >> "$LOG_FILE"
                echo "" >> "$LOG_FILE"

                ((VIOLATION_COUNT++))
            fi
        fi
    done
}

# 检查重复包名模式
check_duplicate_packages() {
    echo "🔍 检查重复包名模式..." | tee -a "$LOG_FILE"

    # 检查是否存在重复的包结构（如 visitor/visitor/, attendance/attendance/）
    find microservices -type d -name "*-*service" | while read -r service_dir; do
        service_name=$(basename "$service_dir")

        # 检查是否有重复的服务名模式
        if find "$service_dir/src/main/java" -type d -name "*$service_name*" | grep -q "$service_name/$service_name"; then
            echo "❌ 发现重复包结构：$service_name" >> "$LOG_FILE"
            find "$service_dir/src/main/java" -type d -name "*$service_name*" | grep "$service_name/$service_name" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((VIOLATION_COUNT++))
        fi
    done
}

# 检查标准包结构
check_standard_package_structure() {
    echo "🔍 检查标准包结构..." | tee -a "$LOG_FILE"

    find microservices -type d -name "*-*service" | while read -r service_dir; do
        service_name=$(basename "$service_dir")
        java_dir="$service_dir/src/main/java"

        if [ ! -d "$java_dir" ]; then
            continue
        fi

        # 检查核心包结构
        core_packages=("controller" "service" "service/impl" "dao" "domain" "entity" "config")

        for pkg in "${core_packages[@]}"; do
            if [ ! -d "$java_dir/**/$pkg" ]; then
                # 检查是否存在相关的Java文件
                if find "$java_dir" -name "*${pkg}*" -type f | grep -q .; then
                    echo "⚠️  建议创建标准包结构：$service_name 缺少 $pkg 包" >> "$LOG_FILE"
                fi
            fi
        done
    done
}

# 检查包命名规范
check_package_naming() {
    echo "🔍 检查包命名规范..." | tee -a "$LOG_FILE"

    find microservices -name "*.java" -type f | while read -r java_file; do
        # 检查是否使用了正确的包名模式
        if grep -q "net.lab1024.sa" "$java_file"; then
            # 检查是否包含不规范的包名
            if grep -q -E "(service\.service|dao\.dao|controller\.controller)" "$java_file"; then
                echo "❌ 发现不规范包名模式：$java_file" >> "$LOG_FILE"
                grep -n -E "net\.lab1024\.sa\..*\.(service\.service|dao\.dao|controller\.controller)" "$java_file" >> "$LOG_FILE"
                echo "" >> "$LOG_FILE"
                ((VIOLATION_COUNT++))
            fi
        fi
    done
}

# 执行检查
echo "开始结构一致性检查..." | tee -a "$LOG_FILE"

# 1. 检查主要服务
for service in visitor attendance consume access device-comm video oa common; do
    service_path="microservices/ioedream-${service}-service"
    if [ -d "$service_path" ]; then
        check_package_alignment "$service_path"
    fi
done

# 2. 检查公共模块
for module in microservices-common-core microservices-common-business microservices-common-data microservices-common-cache microservices-common; do
    module_path="microservices/$module"
    if [ -d "$module_path" ]; then
        check_package_alignment "$module_path"
    fi
done

# 3. 检查重复包结构
check_duplicate_packages

# 4. 检查标准包结构
check_standard_package_structure

# 5. 检查包命名规范
check_package_naming

# 生成检查结果报告
echo "======================================" >> "$LOG_FILE"
echo "检查完成时间: $(date -u +"%Y-%m-%d %H:%M:%S UTC")" >> "$LOG_FILE"
echo "发现问题数量: $VIOLATION_COUNT" >> "$LOG_FILE"

# 输出结果到控制台
echo ""
echo "📊 P1检查结果总结："
echo "🔍 检查项目: 包结构一致性、重复包名、标准包结构、包命名规范"
echo "❌ 发现问题: $VIOLATION_COUNT 个"

if [ $VIOLATION_COUNT -gt $MAX_VIOLATIONS ]; then
    echo "🚫 P1检查失败：发现 $VIOLATION_COUNT 个结构违规（最大允许: $MAX_VIOLATIONS）"
    echo "📋 详细报告见: $LOG_FILE"
    exit 1
else
    echo "✅ P1检查通过：结构一致性符合要求"
    echo "📋 详细报告见: $LOG_FILE"
    exit 0
fi