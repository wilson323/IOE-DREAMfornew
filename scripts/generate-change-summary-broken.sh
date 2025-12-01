#!/bin/bash
# IOE-DREAM项目变更摘要生成工具

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
COMMIT_HASH=$(git rev-parse --short HEAD)
BRANCH_NAME=$(git branch --show-current | head -n 1 | sed 's/^[* ]*//')
AUTHOR=$(git config user.name)
EMAIL=$(git config user.email)

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

CODE_STATS=$(git diff --stat --name-only | tail -n 1)
if [ -n "$CODE_STATS" ]; then
    echo "### 代码变更" >> "$SUMMARY_FILE"
    echo "\`\`\$CODE_STATS\`\`\n" >> "$SUMMARY_FILE"
else
    echo "- 未检测到代码变更\n" >> "$SUMMARY_FILE"
fi

# 获取文件变更详情
echo -e "\n${BLUE}📋 文件变更详情...${NC}"

CHANGED_FILES=$(git diff --name-only)
if [ -n "$CHANGED_FILES" ]; then
    echo "### 变更文件列表" >> "$SUMMARY_FILE"
    echo "" >> "$SUMMARY_FILE"
    echo "总变更文件数: $(echo "$CHANGED_FILES" | wc -l)" >> "$SUMMARY_FILE"
    echo "" >> "$SUMMARY_FILE"

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
    echo "#### 文件详情" >> "$SUMMARY_FILE"
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

        # 获取变更信息
        file_diff=$(git diff --numstat "$file" 2>/dev/null || echo "0 0 $file")
        additions=$(echo "$file_diff" | awk '{print $1}' | head -1)
        deletions=$(echo "$file_diff" | awk '{print $2}' | head -1)

        # 确保数字格式正确
        additions=${additions:-0}
        deletions=${deletions:-0}

        # 确保是纯数字
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

# 获取Java代码变更详情
echo -e "\n${BLUE}☕️ Java代码变更详情...${NC}"

JAVA_CHANGES=$(git diff --name-only -- '*.java' 2>/dev/null)
if [ -n "$JAVA_CHANGES" ]; then
    echo "### Java代码变更" >> "$SUMMARY_FILE"
    echo "" >> "$SUMMARY_FILE"

    for java_file in $JAVA_CHANGES; do
        if [ -f "$PROJECT_ROOT/$java_file" ]; then
            echo "#### $java_file" >> "$SUMMARY_FILE"
            echo "" >> "$SUMMARY_FILE"

            # 提取类信息
            class_name=$(basename "$java_file" .java)
            class_info=$(grep -n "class $class_name" "$PROJECT_ROOT/$java_file" | head -n1)

            if [ -n "$class_info" ]; then
                echo "**类定义**:" >> "$SUMMARY_FILE"
                echo '\n```' >> "$SUMMARY_FILE"
                echo "$class_info" >> "$SUMMARY_FILE"
                echo '```' >> "$SUMMARY_FILE"
                echo "" >> "$SUMMARY_FILE"
            fi

            # 提取API方法变更
            method_changes=$(git diff "$PROJECT_ROOT/$java_file" | grep -E "^\+.*@(Get|Post|Put|Delete)Mapping")
            if [ -n "$method_changes" ]; then
                echo "**API方法变更**:" >> "$SUMMARY_FILE"
                echo "- $(echo "$method_changes" | wc -l) 个方法变更" >> "$SUMMARY_FILE"
                echo "" >> "$SUMMARY_FILE"
            fi
        fi
    done
else
    echo "- 未检测到Java文件变更\n" >> "$SUMMARY_FILE"
fi

# 获取文档变更详情
echo -e "\n${BLUE}📚️ 文档变更详情...${NC}"

DOC_CHANGES=$(git diff --name-only -- '*.md' 2>/dev/null)
if [ -n "$DOC_CHANGES" ]; then
    echo "### 文档变更" >> "$SUMMARY_FILE"
    echo "" >> "$SUMMARY_FILE"

    for doc_file in $DOC_CHANGES; do
        if [ -f "$PROJECT_ROOT/$doc_file" ]; then
            echo "#### $doc_file" >> "$SUMMARY_FILE"
            echo "" >> "$SUMMARY_FILE"

            # 提取文档标题
            title=$(grep -m 1 "^# " "$PROJECT_ROOT/$doc_file" | sed 's/^# //')
            if [ -n "$title" ]; then
                echo "**文档标题**: $title" >> "$SUMMARY_FILE"
            fi

            # 检查版本号
            version=$(grep -i "文档版本" "$PROJECT_ROOT/$doc_file" | head -n 1 | sed 's/.*文档版本[:：]*\s*v?//' | sed 's/[^0-9.]//g')
            if [ -n "$version" ]; then
                echo "**文档版本**: $version" >> "$SUMMARY_FILE"
            fi

            # 统计变更行数
            lines_changed=$(git diff --numstat "$PROJECT_ROOT/$doc_file" 2>/dev/null | awk '{print $1 + $2}' 2>/dev/null || echo "0")
            if [ "$lines_changed" -gt "0" ]; then
                echo "**变更行数**: $lines_changed" >> "$SUMMARY_FILE"
            fi

            echo "" >> "$SUMMARY_FILE"
        fi
    done
else
    echo "- 未检测到文档变更\n" >> "$SUMMARY_FILE"
fi

# 获取API变更信息
echo -e "\n${BLUE}🔌 API接口变更详情...${NC}"

API_CHANGES=$(git diff --name-only -- '*Controller.java' 2>/dev/null)
if [ -n "$API_CHANGES" ]; then
    echo "### API接口变更" >> "$SUMMARY_FILE"
    echo "" >> "$SUMMARY_FILE"

    api_count=0
    for controller_file in $API_CHANGES; do
        if [ -f "$PROJECT_ROOT/$controller_file" ]; then
            # 提取新增的API方法
            new_apis=$(git diff "$PROJECT_ROOT/$controller_file" | grep -E "^\+.*@(Get|Post|Put|Delete)Mapping" | wc -l)
            if [ "$new_apis" -gt "0" ]; then
                api_count=$((api_count + new_apis))
                controller_name=$(basename "$controller_file" .java)
                echo "- **$controller_name**: 新增 $new_apis 个API接口" >> "$SUMMARY_FILE"
            fi
        fi
    done

    if [ "$api_count" -gt "0" ]; then
        echo "\n**总新增API接口**: $api_count 个" >> "$SUMMARY_FILE"
    else
        echo "- 未检测到新增API接口\n" >> "$SUMMARY_FILE"
    fi
else
    echo "- 未检测到Controller文件变更\n" >> "$SUMMARY_FILE"
fi

# 获取数据库变更信息
echo -e "\n${BLUE}💾 数据库变更详情...${NC}"

ENTITY_CHANGES=$(git diff --name-only -- '*Entity.java' 2>/dev/null)
if [ -n "$ENTITY_CHANGES" ]; then
    echo "### 数据库变更" >> "$SUMMARY_FILE"
    echo "" >> "$SUMMARY_FILE"

    table_count=0
    for entity_file in $ENTITY_CHANGES; do
        if [ -f "$PROJECT_ROOT/$entity_file" ]; then
            # 检查是否有@Table注解变更
            table_changes=$(git diff "$PROJECT_ROOT/$entity_file" | grep -E "^\+.*@Table" | wc -l)
            if [ "$table_changes" -gt 0 ]; then
                table_count=$((table_count + 1))
                entity_name=$(basename "$entity_file" .java)
                echo "- **$entity_name**: 数据表结构变更" >> "$SUMMARY_FILE"
            fi
        fi
    done

    if [ "$table_count" -gt 0 ]; then
        echo "\n**数据表变更数量**: $table_count 个" >> "$SUMMARY_FILE"
    else
        echo "- 未检测到数据表结构变更\n" >> "$SUMMARY_FILE"
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
JAVA_COUNT=$(echo "$CHANGED_FILES" | grep '\.java$' | wc -l)
MD_COUNT=$(echo "$CHANGED_FILES" | grep '\.md$' | wc -l)
YAML_COUNT=$(echo "$CHANGED_FILES" | grep -E '\.(yaml|yml)$' | wc -l)
XML_COUNT=$(echo "$CHANGED_FILES" | grep '\.xml$' | wc -l)

cat >> "$DETAILS_FILE" << EOF
    "java_files": $JAVA_COUNT,
    "markdown_files": $MD_COUNT,
    "yaml_files": $YAML_COUNT,
    "xml_files": $XML_COUNT,
    "total_files": $(echo "$CHANGED_FILES" | wc -l)
  },
  "changed_files": [
EOF

# 添加文件详情到JSON
first_file=true
for file in $CHANGED_FILES; do
    if [ "$first_file" = false ]; then
        echo "," >> "$DETAILS_FILE"
    fi
    first_file=false

    # 获取文件详情
    file_type=$(echo "$file" | sed 's/.*\.//')
    file_diff=$(git diff --numstat "$file" 2>/dev/null | tail -n 1)
    additions=$(echo "$file_diff" | awk '{print $1}' 2>/dev/null | head -1 || echo "0")
    deletions=$(echo "$file_diff" | awk '{print $2}' 2>/dev/null | head -1 || echo "0")
    file_size=$(wc -c < "$PROJECT_ROOT/$file" 2>/dev/null | awk '{print $1}' || echo "0")

    # 确保数字格式正确
    additions=${additions:-0}
    deletions=${deletions:-0}
    file_size=${file_size:-0}

    # 确保是纯数字
    additions=$(echo "$additions" | grep -o '[0-9]*' | head -1)
    deletions=$(echo "$deletions" | grep -o '[0-9]*' | head -1)
    file_size=$(echo "$file_size" | grep -o '[0-9]*' | head -1)
    additions=${additions:-0}
    deletions=${deletions:-0}
    file_size=${file_size:-0}

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
    echo "- 总文件数: $(echo "$CHANGED_FILES" | wc -l)"
    echo "- Java文件: $JAVA_COUNT"
    echo "- Markdown文件: $MD_COUNT"
    echo "- 配置文件: $((YAML_COUNT + XML_COUNT))"
    echo "- 其他文件: $OTHER_FILES"
fi

# 如果有Python脚本可用，运行Python分析工具
if command -v python3 >/dev/null 2>&1; then
    echo -e "\n${BLUE}🔍 运行Python深度分析...${NC}"

    # 生成Python分析报告
    python3 -c "
import json
import subprocess
import re
from datetime import datetime
from pathlib import Path

def analyze_code_changes():
    project_root = Path('$PROJECT_ROOT')

    # 获取所有变更文件
    result = subprocess.run(['git', 'diff', '--name-only'],
                          capture_output=True, text=True, cwd=project_root)
    changed_files = result.stdout.strip().split('\n') if result.stdout else []

    analysis = {
        'api_changes': [],
        'database_changes': [],
        'security_changes': [],
        'performance_changes': []
    }

    for file_path in changed_files:
        full_path = project_root / file_path

        if not full_path.exists():
            continue

        try:
            content = full_path.read_text(encoding='utf-8')

            # 分析API变更
            if file_path.endswith('.java'):
                if 'Controller.java' in file_path:
                    api_methods = re.findall(r'@(Get|Post|Put|Delete)Mapping\([^\)]+\)', content)
                    if api_methods:
                        analysis['api_changes'].append({
                            'file': file_path,
                            'apis': api_methods
                        })

            # 分析数据库变更
            if 'Entity.java' in file_path or '@Table' in content:
                analysis['database_changes'].append({
                    'file': file_path,
                    'has_table_annotation': '@Table' in content
                })

            # 分析安全相关变更
            if any(keyword in content for keyword in ['password', 'token', 'security', 'auth', 'permission']):
                analysis['security_changes'].append({
                    'file': file_path,
                    'keywords': [kw for kw in ['password', 'token', 'security', 'auth', 'permission'] if kw in content]
                })

            # 分析性能相关变更
            if any(keyword in content for keyword in ['cache', 'performance', 'optimization', 'async']):
                analysis['performance_changes'].append({
                    'file': file_path,
                    'keywords': [kw for kw in ['cache', 'performance', 'optimization', 'async'] if kw in content]
                })

        except Exception:
            continue

    # 将分析结果添加到JSON报告
    with open('$DETAILS_FILE', 'r+', encoding='utf-8') as f:
        data = json.load(f)
        data['code_analysis'] = analysis
        f.seek(0)
        json.dump(data, f, indent=2, ensure_ascii=False)

analyze_code_changes()
"
fi

# 在当前目录创建软链接便于访问
if [ ! -L "latest-summary.md" ]; then
    ln -sf "$OUTPUT_DIR/changes-summary-$CURRENT_DATE.md" "latest-summary.md"
fi

if [ ! -L "latest-details.json" ]; then
    ln -sf "$OUTPUT_DIR/changes-details-$TIMESTAMP.json" "latest-details.json"
fi

echo -e "${GREEN}📈 快速访问链接:${NC}"
echo -e "${GREEN}  - 最新摘要: latest-summary.md${NC}"
echo -e "${GREEN}  - 最新详情: latest-details.json${NC}"

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