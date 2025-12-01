#!/bin/bash
# =============================================================================
# IOE-DREAM项目Lombok混搭问题批量修复脚本
# 统一使用Lombok注解，删除@Data下的手动getter/setter方法
# =============================================================================

echo "=== 🚨 IOE-DREAM项目Lombok规范化批量修复 ==="
echo "遵循原则：统一使用Lombok注解，要么全用注解，要么全用手动实现"
echo ""

# 计数器
total_fixed=0
total_errors=0

# 需要跳过的特殊情况（实现接口的方法需要保留）
skip_patterns=(
    "@Override"
    "// 手动"
    "// 手动实现"
    "// 特殊处理"
)

# 检查是否应该跳过某一行
should_skip_line() {
    local line="$1"
    for pattern in "${skip_patterns[@]}"; do
        if [[ "$line" == *"$pattern"* ]]; then
            return 0  # 跳过
        fi
    done
    return 1  # 不跳过
}

# 修复单个Java文件
fix_java_file() {
    local file="$1"
    echo "修复: $file"

    # 检查文件是否存在@Data注解
    if ! grep -q "@Data" "$file"; then
        echo "  ✅ 无@Data注解，跳过"
        return 0
    fi

    # 备份原文件
    cp "$file" "$file.backup"

    # 创建临时文件
    local temp_file=$(mktemp)

    # 处理文件内容
    local in_manual_method_block=false
    local brace_count=0
    local line_number=0

    while IFS= read -r line; do
        line_number=$((line_number + 1))

        # 检查是否进入手动方法块
        if [[ "$line" =~ ^[[:space:]]*public[[:space:]]+.*(get|set).*\(.*\)[[:space:]]*\{[[:space:]]*$ ]]; then
            # 检查是否应该跳过此方法
            if should_skip_line "$line"; then
                echo "$line" >> "$temp_file"
                in_manual_method_block=true
                brace_count=1
                continue
            fi

            # 删除手动getter/setter方法的开始行
            echo "  🗑️  删除手动方法 L$line_number: ${line:0:50}..."
            in_manual_method_block=true
            brace_count=1
            continue
        fi

        # 如果在手动方法块中，计算大括号
        if [ "$in_manual_method_block" = true ]; then
            # 计算这一行的大括号
            open_braces=$(echo "$line" | grep -o '{' | wc -l)
            close_braces=$(echo "$line" | grep -o '}' | wc -l)
            brace_count=$((brace_count + open_braces - close_braces))

            # 如果大括号平衡了，结束手动方法块
            if [ $brace_count -le 0 ]; then
                in_manual_method_block=false
                brace_count=0
                continue
            fi

            # 删除手动方法块内的内容
            continue
        fi

        # 保留非手动方法的内容
        echo "$line" >> "$temp_file"

    done < "$file"

    # 替换原文件
    mv "$temp_file" "$file"

    echo "  ✅ 修复完成"
    total_fixed=$((total_fixed + 1))
}

# 主修复流程
echo "开始扫描和修复Java文件..."

# 查找所有需要修复的Java文件
echo "1. 扫描需要修复的文件..."
find . -name "*.java" -print0 | while IFS= read -r -d '' file; do
    if grep -q "@Data" "$file" && grep -q "public.*\(get\|set\).*(" "$file"; then
        echo "发现需要修复的文件: $file"

        # 预览将要删除的方法数量
        non_override_count=$(grep -n "public.*\(get\|set\).*(" "$file" | grep -v "@Override" | grep -v "// 手动" | wc -l)
        echo "  预计删除 $non_override_count 个手动方法"

        # 执行修复
        fix_java_file "$file"
        echo ""
    fi
done

echo ""
echo "=== 📊 修复结果统计 ==="
echo "✅ 修复文件数: $total_fixed"
echo "❌ 错误文件数: $total_errors"

if [ $total_fixed -gt 0 ]; then
    echo ""
    echo "🎉 Lombok规范化修复完成！"
    echo "💡 建议立即运行编译验证：mvn clean compile"
    echo "💡 如有问题，可从.backup文件恢复"
else
    echo "ℹ️  未发现需要修复的文件"
fi

echo ""
echo "=== 🔧 Lombok规范化原则 ==="
echo "1. 统一使用Lombok注解 - @Data, @Builder, @Slf4j等"
echo "2. 删除@Data注解下的手动getter/setter方法"
echo "3. 保留@Override方法（接口实现必需）"
echo "4. 保留特殊业务逻辑方法（标记为// 手动 或 // 特殊处理）"
echo "5. 定期运行此脚本保持代码规范"