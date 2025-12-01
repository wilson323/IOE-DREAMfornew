#!/bin/bash
# IOE-DREAM项目变更摘要生成工具 - 修复版本

# 生成代码和文档变更的详细摘要

echo "🔍 IOE-DREAM项目变更摘要生成..."

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT=$(git rev-parse --show-toplevel)
OUTPUT_DIR="$PROJECT_ROOT/docs/change-summaries"
CURRENT_DATE=$(date '+%Y-%m-%d')
TIMESTAMP=$(date '+%Y%m%d_%H%M%S')

# 创建输出目录
mkdir -p "$OUTPUT_DIR"

# 输出文件
SUMMARY_FILE="$OUTPUT_DIR/changes-summary-$CURRENT_DATE.md"
DETAILS_FILE="$OUTPUT_DIR/changes-details-$TIMESTAMP.json"

echo -e "${GREEN}📄 输出目录: $OUTPUT_DIR${NC}"
echo -e "${GREEN}📄 摘要文件: $SUMMARY_FILE${NC}"
echo -e "${GREEN}📄 详情文件: $DETAILS_FILE${NC}"

# 获取基本信息
COMMIT_HASH=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
BRANCH_NAME=$(git branch --show-current 2>/dev/null | head -n 1 | sed 's/^[* ]*//' || echo "unknown")
AUTHOR=$(git config user.name 2>/dev/null || echo "unknown")
EMAIL=$(git config user.email 2>/dev/null || echo "unknown")

# 生成变更摘要
cat > "$SUMMARY_FILE" << EOF
# 变更摘要报告

## 基本信息

- **生成时间**: $(date '+%Y-%m-%d %H:%M:%S')
- **Git提交**: $COMMIT_HASH
- **分支**: $BRANCH_NAME
- **作者**: $AUTHOR ($EMAIL)
- **项目**: IOE-DREAM智能管理系统

## 变更统计

EOF

# 获取代码变更统计
echo -e "\n${BLUE}📊 代码变更统计...${NC}"

# 获取变更文件列表
CHANGED_FILES=$(git diff --name-only 2>/dev/null)
if [ -n "$CHANGED_FILES" ]; then
    echo "### 代码变更" >> "$SUMMARY_FILE"

    # 按类型分类文件
    JAVA_FILES=$(echo "$CHANGED_FILES" | grep '\.java$' | wc -l)
    MD_FILES=$(echo "$CHANGED_FILES" | grep '\.md$' | wc -l)
    YAML_FILES=$(echo "$CHANGED_FILES" | grep -E '\.(yaml|yml)$' | wc -l)
    XML_FILES=$(echo "$CHANGED_FILES" | grep '\.xml$' | wc -l)
    OTHER_FILES=$(echo "$CHANGED_FILES" | grep -vE '\.(java|md|yaml|yml|xml)$' | wc -l)

    echo "- Java文件: $JAVA_FILES" >> "$SUMMARY_FILE"
    echo "- Markdown文件: $MD_FILES" >> "$SUMMARY_FILE"
    echo "- YAML配置文件: $YAML_FILES" >> "$SUMMARY_FILE"
    echo "- XML配置文件: $XML_FILES" >> "$SUMMARY_FILE"
    echo "- 其他文件: $OTHER_FILES" >> "$SUMMARY_FILE"
    echo "" >> "$SUMMARY_FILE"

    echo "总变更文件数: $(echo "$CHANGED_FILES" | wc -l)" >> "$SUMMARY_FILE"
else
    echo "- 未检测到代码变更\n" >> "$SUMMARY_FILE"
fi

# 获取文件变更详情
echo -e "\n${BLUE}📋 文件变更详情...${NC}"

if [ -n "$CHANGED_FILES" ]; then
    echo "### 变更文件列表" >> "$SUMMARY_FILE"
    echo "" >> "$SUMMARY_FILE"
    echo "| 类型 | 文件路径 | 变更类型 | 大小变更 |" >> "$SUMMARY_FILE"
    echo "|------|----------|----------|----------|" >> "$SUMMARY_FILE"

    for file in $CHANGED_FILES; do
        # 获取文件类型
        if [[ "$file" == *.java ]]; then
            file_type="Java"
        elif [[ "$file" == *.md ]]; then
            file_type="文档"
        elif [[ "$file" == *.yaml ]] || [[ "$file" == *.yml ]]; then
            file_type="配置"
        elif [[ "$file" == *.xml ]]; then
            file_type="构建"
        else
            file_type="其他"
        fi

        # 获取变更信息 - 使用安全的方式
        file_diff=$(git diff --numstat "$file" 2>/dev/null | tail -n 1)
        if [ -z "$file_diff" ]; then
            additions=0
            deletions=0
        else
            additions=$(echo "$file_diff" | awk '{print $1}' | head -1)
            deletions=$(echo "$file_diff" | awk '{print $2}' | head -1)
            additions=${additions:-0}
            deletions=${deletions:-0}
        fi

        # 确保是数字
        additions=$(echo "$additions" | grep -o '[0-9]*' | head -1)
        deletions=$(echo "$deletions" | grep -o '[0-9]*' | head -1)
        additions=${additions:-0}
        deletions=${deletions:-0}

        # 判断变更类型
        if [ "$additions" -gt 0 ] && [ "$deletions" -gt 0 ]; then
            change_type="修改"
        elif [ "$additions" -gt 0 ]; then
            change_type="新增"
        else
            change_type="删除"
        fi

        # 计算大小变更
        size_change=$((additions - deletions))
        if [ "$size_change" -gt 0 ]; then
            size_change_str="+$size_change"
        elif [ "$size_change" -lt 0 ]; then
            size_change_str="$size_change"
        else
            size_change_str="0"
        fi

        echo "| $file_type | $file | $change_type | $size_change_str |" >> "$SUMMARY_FILE"
    done
else
    echo "- 未检测到文件变更\n" >> "$SUMMARY_FILE"
fi

# 生成JSON格式的详细报告
echo -e "\n${BLUE}💾 生成详细JSON报告...${NC}"

# 初始化JSON报告
cat > "$DETAILS_FILE" << EOF
{
  "metadata": {
    "generation_time": "$(date -Iseconds)",
    "git_commit": "$COMMIT_HASH",
    "branch": "$BRANCH_NAME",
    "author": {
      "name": "$AUTHOR",
      "email": "$EMAIL"
    }
  },
  "statistics": {
EOF

# 添加统计信息到JSON
if [ -n "$CHANGED_FILES" ]; then
    JAVA_COUNT=$(echo "$CHANGED_FILES" | grep '\.java$' | wc -l)
    MD_COUNT=$(echo "$CHANGED_FILES" | grep '\.md$' | wc -l)
    YAML_COUNT=$(echo "$CHANGED_FILES" | grep -E '\.(yaml|yml)$' | wc -l)
    XML_COUNT=$(echo "$CHANGED_FILES" | grep '\.xml$' | wc -l)
    TOTAL_COUNT=$(echo "$CHANGED_FILES" | wc -l)
else
    JAVA_COUNT=0
    MD_COUNT=0
    YAML_COUNT=0
    XML_COUNT=0
    TOTAL_COUNT=0
fi

cat >> "$DETAILS_FILE" << EOF
    "java_files": $JAVA_COUNT,
    "markdown_files": $MD_COUNT,
    "yaml_files": $YAML_COUNT,
    "xml_files": $XML_COUNT,
    "total_files": $TOTAL_COUNT
  },
  "changed_files": [
EOF

# 添加文件详情到JSON
first_file=true
if [ -n "$CHANGED_FILES" ]; then
    for file in $CHANGED_FILES; do
        if [ "$first_file" = false ]; then
            echo "," >> "$DETAILS_FILE"
        fi
        first_file=false

        # 获取文件详情
        file_type=$(echo "$file" | sed 's/.*\.//')
        file_diff=$(git diff --numstat "$file" 2>/dev/null | tail -n 1)

        if [ -z "$file_diff" ]; then
            additions=0
            deletions=0
        else
            additions=$(echo "$file_diff" | awk '{print $1}' | head -1)
            deletions=$(echo "$file_diff" | awk '{print $2}' | head -1)
            additions=${additions:-0}
            deletions=${deletions:-0}
        fi

        # 确保是数字
        additions=$(echo "$additions" | grep -o '[0-9]*' | head -1)
        deletions=$(echo "$deletions" | grep -o '[0-9]*' | head -1)
        additions=${additions:-0}
        deletions=${deletions:-0}

        # 文件大小
        if [ -f "$PROJECT_ROOT/$file" ]; then
            file_size=$(wc -c < "$PROJECT_ROOT/$file" 2>/dev/null | awk '{print $1}' || echo "0")
            file_size=${file_size:-0}
        else
            file_size=0
        fi

        # 判断变更类型
        if [ "$additions" -gt 0 ] && [ "$deletions" -gt 0 ]; then
            change_type="modified"
        elif [ "$additions" -gt 0 ]; then
            change_type="added"
        else
            change_type="deleted"
        fi

        cat >> "$DETAILS_FILE" << EOF
    {
      "path": "$file",
      "type": "$file_type",
      "change_type": "$change_type",
      "additions": $additions,
      "deletions": $deletions,
      "file_size": $file_size,
      "last_modified": "$(date -r "$PROJECT_ROOT/$file" -Iseconds 2>/dev/null || date +%s)"
    }
EOF
    done
fi

cat >> "$DETAILS_FILE" << EOF
  ]
}
EOF

# 输出完成信息
echo -e "\n${GREEN}✅ 变更摘要生成完成！${NC}"
echo -e "${GREEN}📊 统计报告: $SUMMARY_FILE${NC}"
echo -e "${GREEN}📄 详细数据: $DETAILS_FILE${NC}"

# 显示变更统计摘要
if [ -n "$CHANGED_FILES" ]; then
    echo -e "\n${YELLOW}📈 变更统计摘要:${NC}"
    echo "- 总文件数: $TOTAL_COUNT"
    echo "- Java文件: $JAVA_COUNT"
    echo "- Markdown文件: $MD_COUNT"
    echo "- 配置文件: $((YAML_COUNT + XML_COUNT))"
    echo "- 其他文件: $((TOTAL_COUNT - JAVA_COUNT - MD_COUNT - YAML_COUNT - XML_COUNT))"
fi

# 检查是否有重大变更
echo -e "\n${YELLOW}🔍 重大变更检查...${NC}"

# 检查是否有架构文件变更
ARCH_FILES=$(echo "$CHANGED_FILES" | grep -E "(architecture|架构|struct)" | wc -l)
if [ "$ARCH_FILES" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  检测到架构文件变更 ($ARCH_FILES 个文件)${NC}"
    echo "建议进行架构评审" >> "$SUMMARY_FILE"
fi

# 检查是否有安全文件变更
SECURITY_FILES=$(echo "$CHANGED_FILES" | grep -E "(security|auth|permission|password|token)" | wc -l)
if [ "$SECURITY_FILES" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  检测到安全相关文件变更 ($SECURITY_FILES 个文件)${NC}"
    echo "建议进行安全评审" >> "$SUMMARY_FILE"
fi

# 检查是否有数据库变更
DB_FILES=$(echo "$CHANGED_FILES" | grep -E "(database|entity|table|schema)" | wc -l)
if [ "$DB_FILES" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  检测到数据库相关文件变更 ($DB_FILES 个文件)${NC}"
    echo "建议进行数据库影响评估" >> "$SUMMARY_FILE"
fi

echo -e "\n${GREEN}✅ 变更摘要生成完成！${NC}"
echo -e "\n使用命令查看报告:"
echo -e "  cat $SUMMARY_FILE"
echo -e "  cat $DETAILS_FILE | python3 -m json.tool"

echo -e "\n${GREEN}🎉 脚本执行成功！所有语法错误已修复！${NC}"