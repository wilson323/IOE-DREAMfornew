#!/bin/bash

# ==============================================================================
# 编码修复系统性解决方案
# 修复IOE-DREAM项目中的中文乱码问题
# ==============================================================================

set -e

PROJECT_ROOT="D:/IOE-DREAM"
JAVA_SOURCE_DIR="smart-admin-api-java17-springboot3"

echo "🔧 开始系统性修复编码问题..."

# 1. 创建修复后的文件备份目录
BACKUP_DIR="$PROJECT_ROOT/encoding_backup_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"
echo "📁 创建备份目录: $BACKUP_DIR"

# 2. 统计需要修复的文件数量
echo "📊 统计需要修复的Java文件..."
TOTAL_FILES=$(find "$PROJECT_ROOT/$JAVA_SOURCE_DIR" -name "*.java" | wc -l)
echo "   总共 $TOTAL_FILES 个Java文件需要检查"

# 3. 检测并修复编码问题的函数
fix_file_encoding() {
    local file="$1"
    local temp_file="$file.tmp"

    # 备份原文件
    cp "$file" "$BACKUP_DIR/$(basename $(dirname "$file"))_$(basename "$file").bak"

    # 检查文件是否包含乱码字符（常见模式）
    if grep -q "鑰\|绠\|悊\|嫟\|鎵\|崱\|涓\|庡\|煡\|璇\|娑\|樺\|煑" "$file"; then
        echo "🔍 发现编码问题: $(basename "$file")"

        # 尝试多种编码修复方案
        # 方案1: 尝试从GBK转换为UTF-8
        iconv -f GBK -t UTF-8 "$file" 2>/dev/null > "$temp_file" && mv "$temp_file" "$file" && return 0

        # 方案2: 尝试从GB2312转换为UTF-8
        iconv -f GB2312 -t UTF-8 "$file" 2>/dev/null > "$temp_file" && mv "$temp_file" "$file" && return 0

        # 方案3: 尝试从BIG5转换为UTF-8
        iconv -f BIG5 -t UTF-8 "$file" 2>/dev/null > "$temp_file" && mv "$temp_file" "$file" && return 0

        # 方案4: 清除BOM字符
        sed -i '1s/^\xEF\xBB\xBF//' "$file" 2>/dev/null || true

        echo "⚠️  无法自动修复: $(basename "$file")"
        return 1
    fi

    # 清除BOM字符
    sed -i '1s/^\xEF\xBB\xBF//' "$file" 2>/dev/null || true

    return 0
}

# 4. 批量修复文件
FIXED_COUNT=0
FAILED_COUNT=0

echo "🔄 开始批量修复Java文件编码..."

# 使用find命令遍历所有Java文件并进行修复
find "$PROJECT_ROOT/$JAVA_SOURCE_DIR" -name "*.java" -print0 | while IFS= read -r -d '' file; do
    if fix_file_encoding "$file"; then
        ((FIXED_COUNT++))
        echo "✅ 已修复: $(basename "$file")"
    else
        ((FAILED_COUNT++))
        echo "❌ 修复失败: $(basename "$file")"
    fi

    # 显示进度
    if ((FIXED_COUNT + FAILED_COUNT % 10 == 0)); then
        echo "📈 进度: 已处理 $((FIXED_COUNT + FAILED_COUNT)) / $TOTAL_FILES 个文件"
    fi
done

# 5. 修复关键配置文件
echo "🔧 修复关键配置文件..."

CONFIG_FILES=(
    "$PROJECT_ROOT/CLAUDE.md"
    "$PROJECT_ROOT/pom.xml"
    "$PROJECT_ROOT/smart-admin-api-java17-springboot3/pom.xml"
    "$PROJECT_ROOT/smart-admin-web-javascript/package.json"
    "$PROJECT_ROOT/smart-admin-api-java17-springboot3/sa-base/src/main/resources/dev/sa-base.yaml"
)

for config_file in "${CONFIG_FILES[@]}"; do
    if [ -f "$config_file" ]; then
        cp "$config_file" "$BACKUP_DIR/$(basename "$config_file").bak"
        # 清除BOM字符
        sed -i '1s/^\xEF\xBB\xBF//' "$config_file" 2>/dev/null || true
        echo "✅ 已修复配置文件: $(basename "$config_file")"
    fi
done

# 6. 验证修复效果
echo "🔍 验证修复效果..."

# 检查是否还有明显的乱码字符
REMAINING_ISSUES=$(find "$PROJECT_ROOT/$JAVA_SOURCE_DIR" -name "*.java" -exec grep -l "鑰\|绠\|悊\|嫟\|鎵\|崱\|涓\|庡\|煡\|璇\|娑\|樺\|煑" {} \; | wc -l)

if [ "$REMAINING_ISSUES" -eq 0 ]; then
    echo "🎉 编码问题修复完成！未发现残留的乱码字符。"
else
    echo "⚠️  仍有 $REMAINING_ISSUES 个文件可能存在编码问题，需要手动处理。"
fi

# 7. 生成修复报告
REPORT_FILE="$PROJECT_ROOT/encoding_fix_report_$(date +%Y%m%d_%H%M%S).md"
cat > "$REPORT_FILE" << EOF
# 编码修复报告

**修复时间**: $(date)
**项目路径**: $PROJECT_ROOT
**备份目录**: $BACKUP_DIR

## 修复统计
- 总文件数: $TOTAL_FILES
- 修复成功: $FIXED_COUNT
- 修复失败: $FAILED_COUNT
- 残留问题: $REMAINING_ISSUES

## 修复方法
1. 备份所有原始文件
2. 检测常见的中文乱码模式
3. 尝试多种编码转换方案:
   - GBK → UTF-8
   - GB2312 → UTF-8
   - BIG5 → UTF-8
4. 清除BOM字符
5. 验证修复效果

## 后续建议
1. 配置IDE统一使用UTF-8编码
2. 设置Git属性确保文件编码一致性
3. 建立代码审查机制防止编码问题
4. 考虑使用.pre-commit钩子检查编码

## 恢复方法
如需恢复原始文件，请从备份目录复制:
\`\`\`bash
cp "$BACKUP_DIR"/*_*.bak <目标路径>
\`\`\`
EOF

echo "📋 生成修复报告: $REPORT_FILE"

echo "✅ 编码修复流程完成！"