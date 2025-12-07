#!/bin/bash

# =============================================================================
# IOE-DREAM 项目编码规范检查脚本
# 版本: v1.0.0
# 作用: 检查项目中的编码规范合规性，防止乱码问题
# 使用方法: ./encoding-check.sh
# =============================================================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_header() {
    echo -e "\n${BLUE}🔧 IOE-DREAM 编码规范检查开始...${NC}"
    echo "========================================"
}

# 统计变量
TOTAL_FILES=0
ISSUE_FILES=0
ISSUE_COUNT=0

print_header

# 1. 检查文件编码
print_info "1. 检查文件编码规范..."
ENCODING_FILE="encoding_issues.txt"
find . -type f \( -name "*.java" -o -name "*.xml" -o -name "*.yml" -o -name "*.yaml" -o -name "*.properties" -o -name "*.md" -o -name "*.vue" -o -name "*.js" -o -name "*.ts" -o -name "*.txt" \) -not -path "./.git/*" -not -path "./node_modules/*" -not -path "./target/*" -not -path "./dist/*" | while read file; do
    if [ -f "$file" ]; then
        encoding=$(file -b --mime-encoding "$file" 2>/dev/null || echo "unknown")
        if [[ "$encoding" != "utf-8" && "$encoding" != "us-ascii" ]]; then
            echo "$file: $encoding" >> "$ENCODING_FILE"
            ((ISSUE_COUNT++))
        fi
        ((TOTAL_FILES++))
    fi
done

if [ -f "$ENCODING_FILE" ] && [ -s "$ENCODING_FILE" ]; then
    print_error "发现 $ISSUE_COUNT 个非UTF-8编码文件："
    cat "$ENCODING_FILE"
    ((ISSUE_FILES++))
else
    print_success "文件编码检查通过 (已检查 $TOTAL_FILES 个文件)"
fi

# 2. 检查文件名编码
print_info "2. 检查文件名编码规范..."
FILENAME_FILE="filename_issues.txt"
find . -type f -not -path "./.git/*" -not -path "./node_modules/*" -not -path "./target/*" -not -path "./dist/*" | grep -P '[^\x00-\x7F]' > "$FILENAME_FILE" 2>/dev/null || true

if [ -s "$FILENAME_FILE" ]; then
    print_error "发现包含非ASCII字符的文件名："
    cat "$FILENAME_FILE"
    ((ISSUE_FILES++))
else
    print_success "文件名编码检查通过"
fi

# 3. 检查BOM头
print_info "3. 检查BOM头..."
BOM_FILE="bom_issues.txt"
find . -type f \( -name "*.java" -o -name "*.xml" -o -name "*.yml" -o -name "*.yaml" -o -name "*.properties" \) -not -path "./.git/*" -not -path "./node_modules/*" -not -path "./target/*" | while read file; do
    if [ -f "$file" ]; then
        # 检查UTF-8 BOM
        if [ "$(head -c 3 "$file")" = $'\xEF\xBB\xBF' ]; then
            echo "$file has UTF-8 BOM" >> "$BOM_FILE"
            ((ISSUE_COUNT++))
        fi
    fi
done

if [ -f "$BOM_FILE" ] && [ -s "$BOM_FILE" ]; then
    print_error "发现带BOM头的文件："
    cat "$BOM_FILE"
    ((ISSUE_FILES++))
else
    print_success "BOM头检查通过"
fi

# 4. 检查Git配置
print_info "4. 检查Git编码配置..."
GIT_ISSUES=false

if [ "$(git config --get core.quotepath)" != "false" ]; then
    print_warning "建议设置: git config --global core.quotepath false"
    GIT_ISSUES=true
fi

if [ "$(git config --get i18n.commitencoding)" != "utf-8" ]; then
    print_warning "建议设置: git config --global i18n.commitencoding utf-8"
    GIT_ISSUES=true
fi

if [ "$GIT_ISSUES" = false ]; then
    print_success "Git编码配置检查通过"
fi

# 5. 检查最近的Git提交信息
print_info "5. 检查最近的Git提交信息编码..."
COMMIT_ISSUES=false
RECENT_COMMITS=10

for i in $(seq 0 $((RECENT_COMMITS - 1))); do
    commit_hash=$(git rev-parse HEAD~$i 2>/dev/null || break)
    if [ -n "$commit_hash" ]; then
        commit_msg=$(git log --format="%B" -n 1 $commit_hash 2>/dev/null || continue)
        if echo "$commit_msg" | grep -P '[\x00-\x08\x0B\x0C\x0E-\x1F\x7F-\x9F]' > /dev/null 2>&1; then
            print_error "提交 $commit_hash 包含控制字符"
            echo "$commit_msg" | head -1
            COMMIT_ISSUES=true
            ((ISSUE_COUNT++))
        fi
    fi
done

if [ "$COMMIT_ISSUES" = false ]; then
    print_success "Git提交信息编码检查通过 (最近 $RECENT_COMMITS 个提交)"
fi

# 6. 检查配置文件特殊字符
print_info "6. 检查配置文件中的特殊字符..."
CONFIG_ISSUES=false
find . -name "*.properties" -not -path "./.git/*" -not -path "./node_modules/*" -not -path "./target/*" | while read file; do
    if [ -f "$file" ]; then
        # 检查是否包含非ISO-8859-1字符
        if LC_ALL=C grep -P '[^\x00-\x7F]' "$file" > /dev/null 2>&1; then
            print_warning "配置文件 $file 包含非ASCII字符，建议使用UTF-8编码"
            CONFIG_ISSUES=true
        fi
    fi
done

# 生成报告
print_info "生成检查报告..."
REPORT_FILE="encoding_check_report_$(date +%Y%m%d_%H%M%S).md"

cat > "$REPORT_FILE" << EOF
# IOE-DREAM 编码规范检查报告

**检查时间**: $(date '+%Y-%m-%d %H:%M:%S')
**检查范围**: IOE-DREAM 全项目
**检查工具**: encoding-check.sh v1.0.0

## 📊 检查统计

| 检查项目 | 状态 | 问题数量 |
|---------|------|----------|
| 文件编码 | $([ -s "$ENCODING_FILE" ] && echo "❌ 发现问题" || echo "✅ 通过") | $([ -f "$ENCODING_FILE" ] && wc -l < "$ENCODING_FILE" || echo 0) |
| 文件名编码 | $([ -s "$FILENAME_FILE" ] && echo "❌ 发现问题" || echo "✅ 通过") | $([ -f "$FILENAME_FILE" ] && wc -l < "$FILENAME_FILE" || echo 0) |
| BOM头检查 | $([ -s "$BOM_FILE" ] && echo "❌ 发现问题" || echo "✅ 通过") | $([ -f "$BOM_FILE" ] && wc -l < "$BOM_FILE" || echo 0) |
| Git配置 | $([ "$GIT_ISSUES" = true ] && echo "⚠️ 需要优化" || echo "✅ 通过") | 0 |
| Git提交信息 | $([ "$COMMIT_ISSUES" = true ] && echo "❌ 发现问题" || echo "✅ 通过") | $([ "$COMMIT_ISSUES" = true ] && echo "有" || echo "无") |

## 🔍 详细问题清单

EOF

# 添加详细问题信息
if [ -f "$ENCODING_FILE" ] && [ -s "$ENCODING_FILE" ]; then
    echo "### 文件编码问题" >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    cat "$ENCODING_FILE" >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
fi

if [ -f "$FILENAME_FILE" ] && [ -s "$FILENAME_FILE" ]; then
    echo "### 文件名编码问题" >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    cat "$FILENAME_FILE" >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
fi

if [ -f "$BOM_FILE" ] && [ -s "$BOM_FILE" ]; then
    echo "### BOM头问题" >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    cat "$BOM_FILE" >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
fi

cat >> "$REPORT_FILE" << EOF
## 📋 修复建议

1. **文件编码问题**：使用以下命令转换编码
   \`\`\`bash
   iconv -f <原编码> -t UTF-8 input_file > output_file
   \`\`\`

2. **文件名问题**：将包含非ASCII字符的文件名重命名为英文名称

3. **BOM头问题**：使用以下命令移除BOM
   \`\`\`bash
   sed -i '1s/^\xEF\xBB\xBF//' filename.java
   \`\`\`

4. **Git配置问题**：执行以下命令
   \`\`\`bash
   git config --global core.quotepath false
   git config --global i18n.commitencoding utf-8
   \`\`\`

## 📞 技术支持

如有编码问题，请联系技术架构团队。

---

**报告生成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**检查脚本版本**: v1.0.0
EOF

# 清理临时文件
rm -f "$ENCODING_FILE" "$FILENAME_FILE" "$BOM_FILE"

# 最终结果判断
echo ""
echo "========================================"
if [ $ISSUE_FILES -eq 0 ] && [ "$COMMIT_ISSUES" = false ]; then
    print_success "🎉 所有编码规范检查通过！"
    print_info "详细报告已保存到: $REPORT_FILE"
    exit 0
else
    print_error "❌ 发现 $ISSUE_FILES 个编码问题，请及时修复！"
    print_info "详细报告已保存到: $REPORT_FILE"
    print_warning "修复建议: 请参考报告中的修复建议或联系技术架构团队"
    exit 1
fi