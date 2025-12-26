#!/bin/bash

# LoggerFactory 违规修复脚本
# 自动修复 LoggerFactory.getLogger 为 @Slf4j

echo "🔧 开始修复 LoggerFactory 违规..."
echo "===================================="

# 函数：修复单个文件的 LoggerFactory
fix_file() {
    local file="$1"
    echo "   修复: $file"

    # 临时文件
    local temp_file=$(mktemp)

    # 检查是否已有 @Slf4j 注解
    if grep -q "@Slf4j" "$file"; then
        # 已有 @Slf4j，只需要移除 LoggerFactory 相关行
        sed -e '/import org.slf4j.Logger;/d' \
            -e '/import org.slf4j.LoggerFactory;/d' \
            -e '/private static final Logger.*= LoggerFactory.getLogger/d' \
            "$file" > "$temp_file"
    else
        # 没有@Slf4j，需要添加
        sed -e '/import org.slf4j.Logger;/d' \
            -e '/import org.slf4j.LoggerFactory;/d' \
            -e '/private static final Logger.*= LoggerFactory.getLogger/d' \
            -e "0a\\
import lombok.extern.slf4j.Slf4j;
" "$file" > "$temp_file"

        # 在类声明前添加 @Slf4j 注解
        sed -i '/^@/!{/^public class/h;$!d};x' "$temp_file"
        sed -i '/^public class/i\
@Slf4j' "$temp_file"
    fi

    # 替换文件
    mv "$temp_file" "$file"

    # 移除多余的空行
    sed -i '/^$/N;/^\n$/d' "$file"
}

# 检查参数
if [ $# -eq 0 ]; then
    echo "❌ 请指定要修复的服务名称"
    echo "   用法: $0 access-service attendance-service oa-service video-service visitor-service device-comm-service biometric-service"
    exit 1
fi

# 修复指定服务
for service in "$@"; do
    service_path="microservices/$service"
    if [ ! -d "$service_path" ]; then
        echo "❌ 服务不存在: $service"
        continue
    fi

    echo "🔧 修复服务: $service"

    # 查找所有违规文件
    violation_files=$(find "$service_path" -name "*.java" -type f -exec grep -l "LoggerFactory.getLogger" {} \; 2>/dev/null)

    if [ -z "$violation_files" ]; then
        echo "   ✅ 没有发现 LoggerFactory 违规"
        continue
    fi

    # 统计
    total_violations=$(echo "$violation_files" | wc -l)
    echo "   📊 发现 $total_violations 个违规文件"

    # 修复每个文件
    fixed_count=0
    echo "$violation_files" | while read file; do
        fix_file "$file"
        fixed_count=$((fixed_count + 1))

        # 显示进度
        if [ $((fixed_count % 10)) -eq 0 ]; then
            echo "   进度: $fixed_count/$total_violations"
        fi
    done

    echo "   ✅ 修复完成: $fixed_count 个文件"
    echo ""
done

echo "===================================="
echo "🎉 所有服务修复完成！"
echo ""
echo "📋 后续步骤:"
echo "1. 运行编译检查: ./scripts/build-all.ps1 -Service $1"
echo "2. 运行质量检查: ./scripts/quality-check.sh"
echo "3. 提交代码: git add . && git commit -m 'feat: 统一 SLF4J 日志规范'