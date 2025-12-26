#!/bin/bash
###############################################################################
# IOE-DREAM 脚本修改检测工具
# 用途：检测代码是否通过脚本批量修改，确保手动修复质量
# 使用：./scripts/detect-script-modification.sh [目录]
###############################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================="
echo "🔍 IOE-DREAM 脚本修改检测工具"
echo "========================================="
echo ""

# 默认检测当前目录
TARGET_DIR="${1:-.}"

echo "📁 检测目录: $TARGET_DIR"
echo "⏰ 检测时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# 统计变量
VIOLATION_COUNT=0
FILE_COUNT=0
SUSPICIOUS_FILES=()

# 检测函数
check_file_modification() {
    local file="$1"
    FILE_COUNT=$((FILE_COUNT + 1))

    # 检查1: 文件修改时间是否过于接近（批量修改特征）
    local mod_time=$(stat -c %Y "$file" 2>/dev/null || stat -f %m "$file" 2>/dev/null || echo "0")
    local current_time=$(date +%s)
    local time_diff=$((current_time - mod_time))

    # 如果文件在最近5分钟内修改，可能是批量修改
    if [ $time_diff -lt 300 ]; then
        # 检查2: Git提交历史中是否有批量修改模式
        if git rev-parse --git-dir > /dev/null 2>&1; then
            local commits=$(git log --since="5 minutes ago" --oneline "$file" | wc -l)
            if [ $commits -eq 0 ]; then
                # 文件最近修改但未提交，可疑
                SUSPICIOUS_FILES+=("$file (未提交的最近修改)")
                VIOLATION_COUNT=$((VIOLATION_COUNT + 1))
            fi
        fi
    fi

    # 检查3: 文件中是否包含大量相同模式的修改
    if [ -f "$file" ]; then
        # 检查是否有连续多个相同的修改模式（如连续的@Resource替换）
        if grep -c "@Resource" "$file" > 10 2>/dev/null; then
            # 检查是否在短时间内添加了大量@Resource
            local resource_count=$(grep -c "@Resource" "$file" 2>/dev/null || echo "0")
            if [ $resource_count -gt 10 ]; then
                # 进一步检查：是否同时缺少日志或注释
                local log_count=$(grep -c "log\." "$file" 2>/dev/null || echo "0")
                if [ $log_count -lt $resource_count ]; then
                    SUSPICIOUS_FILES+=("$file (大量@Resource但缺少日志)")
                    VIOLATION_COUNT=$((VIOLATION_COUNT + 1))
                fi
            fi
        fi
    fi
}

# 遍历所有Java文件
echo "🔍 开始扫描Java文件..."
echo ""

find "$TARGET_DIR" -name "*.java" -type f | while read -r file; do
    check_file_modification "$file"
done

echo ""
echo "========================================="
echo "📊 检测结果汇总"
echo "========================================="
echo ""
echo "📁 扫描文件数: $FILE_COUNT"
echo "❌ 发现可疑文件: ${#SUSPICIOUS_FILES[@]}"
echo "🔴 违规计数: $VIOLATION_COUNT"
echo ""

if [ $VIOLATION_COUNT -gt 0 ]; then
    echo "========================================="
    echo "🚨 检测到可能的脚本修改！"
    echo "========================================="
    echo ""
    echo "以下文件存在脚本修改嫌疑："
    echo ""

    for file in "${SUSPICIOUS_FILES[@]}"; do
        echo "  ❌ $file"
    done

    echo ""
    echo "========================================="
    echo "📋 违规处理流程"
    echo "========================================="
    echo ""
    echo "1. 📧 立即通知架构委员会"
    echo "2. 🔍 回顾所有相关代码修改"
    echo "3. 📝 提交手动修复说明"
    echo "4. ✅ 代码审查验证"
    echo "5. 🔒 重新提交代码"
    echo ""
    echo "⚠️  警告：此代码将被拒绝合并！"
    echo ""

    # 返回错误码，阻止提交
    exit 1
else
    echo "✅ 未检测到脚本修改痕迹"
    echo "✅ 代码修改符合手动修复规范"
    echo ""
    echo "🎉 可以继续提交代码！"
    echo ""

    # 返回成功码
    exit 0
fi
