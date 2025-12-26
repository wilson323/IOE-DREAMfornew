#!/bin/bash

echo "=========================================="
echo "IOE-DREAM 持续监控检查"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 监控报告文件
MONITORING_REPORT="continuous-monitoring-report-$(date +%Y%m%d_%H%M%S).txt"

# 创建报告头部
cat > "$MONITORING_REPORT" << EOF
IOE-DREAM 持续监控检查报告
================================
检查时间: $(date '+%Y-%m-%d %H:%M:%S')
检查类型: 自动化架构监控
================================

EOF

echo -e "${BLUE}开始执行持续监控检查...${NC}"

# 1. 检查Git预提交钩子
echo -e "\n${YELLOW}1. Git预提交钩子检查${NC}"
echo "----------------------------------------"

HOOKS_DIR=".git/hooks"
PRE_COMMIT_HOOK="$HOOKS_DIR/pre-commit"

if [ -f "$PRE_COMMIT_HOOK" ]; then
    echo -e "${GREEN}✅ pre-commit钩子已安装${NC}"
    echo "✅ pre-commit钩子已安装" >> "$MONITORING_REPORT"

    # 检查钩子是否包含架构检查
    if grep -q "architecture" "$PRE_COMMIT_HOOK" 2>/dev/null; then
        echo -e "${GREEN}✅ 架构检查已集成到pre-commit钩子${NC}"
        echo "✅ 架构检查已集成到pre-commit钩子" >> "$MONITORING_REPORT"
    else
        echo -e "${YELLOW}⚠️ pre-commit钩子未包含架构检查${NC}"
        echo "⚠️ pre-commit钩子未包含架构检查" >> "$MONITORING_REPORT"
    fi
else
    echo -e "${RED}❌ pre-commit钩子未安装${NC}"
    echo "❌ pre-commit钩子未安装" >> "$MONITORING_REPORT"
fi

# 2. 检查CI/CD集成状态
echo -e "\n${YELLOW}2. CI/CD集成状态检查${NC}"
echo "----------------------------------------"

# 检查GitHub Actions工作流
GITHUB_WORKFLOWS_DIR=".github/workflows"
if [ -d "$GITHUB_WORKFLOWS_DIR" ]; then
    WORKFLOW_COUNT=$(find "$GITHUB_WORKFLOWS_DIR" -name "*.yml" -o -name "*.yaml" | wc -l)
    echo -e "${GREEN}✅ GitHub Actions工作流已配置 ($WORKFLOW_COUNT 个工作流)${NC}"
    echo "✅ GitHub Actions工作流已配置 ($WORKFLOW_COUNT 个工作流)" >> "$MONITORING_REPORT"

    # 检查是否有架构合规检查工作流
    ARCHITECTURE_WORKFLOW=$(find "$GITHUB_WORKFLOWS_DIR" -name "*architecture*.yml" -o -name "*quality*.yml" | wc -l)
    if [ "$ARCHITECTURE_WORKFLOW" -gt 0 ]; then
        echo -e "${GREEN}✅ 发现 $ARCHITECTURE_WORKFLOW 个架构检查工作流${NC}"
        echo "✅ 发现 $ARCHITECTURE_WORKFLOW 个架构检查工作流" >> "$MONITORING_REPORT"
    else
        echo -e "${YELLOW}⚠️ 未发现专门的架构检查工作流${NC}"
        echo "⚠️ 未发现专门的架构检查工作流" >> "$MONITORING_REPORT"
    fi
else
    echo -e "${YELLOW}⚠️ 未发现GitHub Actions工作流目录${NC}"
    echo "⚠️ 未发现GitHub Actions工作流目录" >> "$MONITORING_REPORT"
fi

# 3. 检查验证脚本状态
echo -e "\n${YELLOW}3. 验证脚本状态检查${NC}"
echo "----------------------------------------"

SCRIPTS_DIR="scripts"
if [ -d "$SCRIPTS_DIR" ]; then
    # 统计脚本数量
    SCRIPT_COUNT=$(find "$SCRIPTS_DIR" -name "*.sh" -name "*validate*" -o -name "*check*" | wc -l)
    echo -e "${GREEN}✅ 发现 $SCRIPT_COUNT 个验证脚本${NC}"
    echo "✅ 发现 $SCRIPT_COUNT 个验证脚本" >> "$MONITORING_REPORT"

    # 检查脚本执行权限
    EXECUTABLE_SCRIPTS=0
    NON_EXECUTABLE_SCRIPTS=0

    for script in "$SCRIPTS_DIR"/*.sh; do
        if [ -x "$script" ]; then
            ((EXECUTABLE_SCRIPTS++))
        else
            ((NON_EXECUTABLE_SCRIPTS++))
        fi
    done

    if [ "$NON_EXECUTABLE_SCRIPTS" -eq 0 ]; then
        echo -e "${GREEN}✅ 所有脚本都有执行权限${NC}"
        echo "✅ 所有脚本都有执行权限" >> "$MONITORING_REPORT"
    else
        echo -e "${YELLOW}⚠️ $NON_EXECUTABLE_SCRIPTS 个脚本缺少执行权限${NC}"
        echo "⚠️ $NON_EXECUTABLE_SCRIPTS 个脚本缺少执行权限" >> "$MONITORING_REPORT"
    fi
else
    echo -e "${RED}❌ 脚本目录不存在${NC}"
    echo "❌ 脚本目录不存在" >> "$MONITORING_REPORT"
fi

# 4. 检查文档完整性
echo -e "\n${YELLOW}4. 文档完整性检查${NC}"
echo "----------------------------------------"

# 检查优化报告
REPORTS_COUNT=$(ls *OPTIMIZATION*REPORT.md 2>/dev/null | wc -l)
echo -e "${GREEN}✅ 发现 $REPORTS_COUNT 个优化报告${NC}"
echo "✅ 发现 $REPORTS_COUNT 个优化报告" >> "$MONITORING_REPORT"

# 检查技术文档
TECH_DOCS_COUNT=$(find documentation/technical -name "*.md" 2>/dev/null | wc -l)
echo -e "${GREEN}✅ 发现 $TECH_DOCS_COUNT 个技术文档${NC}"
echo "✅ 发现 $TECH_DOCS_COUNT 个技术文档" >> "$MONITORING_REPORT"

# 5. 检查最近架构违规
echo -e "\n${YELLOW}5. 最近架构违规检查${NC}"
echo "----------------------------------------"

# 检查最近的提交是否有Entity违规
RECENT_COMMITS=5
RECENT_ENTITY_VIOLATIONS=0

for ((i=1; i<=RECENT_COMMITS; i++)); do
    COMMIT_HASH=$(git log --oneline -$i 2>/dev/null | head -1 | awk '{print $1}')
    if [ -n "$COMMIT_HASH" ]; then
        # 检查该提交是否包含Entity相关的新增文件
        NEW_ENTITY_FILES=$(git show --name-only --diff-filter=A "$COMMIT_HASH" 2>/dev/null | grep -i "entity\|Entity" | wc -l)
        if [ "$NEW_ENTITY_FILES" -gt 0 ]; then
            ((RECENT_ENTITY_VIOLATIONS++))
            echo -e "${YELLOW}⚠️ 提交 $COMMIT_HASH 包含Entity相关文件${NC}"
        fi
    fi
done

if [ "$RECENT_ENTITY_VIOLATIONS" -eq 0 ]; then
    echo -e "${GREEN}✅ 最近 $RECENT_COMMITS 个提交未发现Entity违规${NC}"
    echo "✅ 最近 $RECENT_COMMITS 个提交未发现Entity违规" >> "$MONITORING_REPORT"
else
    echo -e "${YELLOW}⚠️ 最近发现 $RECENT_ENTITY_VIOLATIONS 个可能Entity违规${NC}"
    echo "⚠️ 最近发现 $RECENT_ENTITY_VIOLATIONS 个可能Entity违规" >> "$MONITORING_REPORT"
fi

# 6. 检查团队遵循度
echo -e "\n${YELLOW}6. 团队遵循度评估${NC}"
echo "----------------------------------------"

# 计算监控得分
MONITORING_SCORE=0
TOTAL_MONITORS=6

# 预提交钩子得分
if [ -f "$PRE_COMMIT_HOOK" ]; then
    MONITORING_SCORE=$((MONITORING_SCORE + 1))
fi

# CI/CD集成得分
if [ -d "$GITHUB_WORKFLOWS_DIR" ]; then
    MONITORING_SCORE=$((MONITORING_SCORE + 1))
fi

# 验证脚本得分
if [ "$SCRIPT_COUNT" -gt 0 ]; then
    MONITORING_SCORE=$((MONITORING_SCORE + 1))
fi

# 文档完整性得分
if [ "$REPORTS_COUNT" -gt 0 ]; then
    MONITORING_SCORE=$((MONITORING_SCORE + 1))
fi

# 架构合规得分
if [ "$RECENT_ENTITY_VIOLATIONS" -eq 0 ]; then
    MONITORING_SCORE=$((MONITORING_SCORE + 1))
fi

# 脚本权限得分
if [ "$NON_EXECUTABLE_SCRIPTS" -eq 0 ]; then
    MONITORING_SCORE=$((MONITORING_SCORE + 1))
fi

COMPLIANCE_SCORE=$((MONITORING_SCORE * 100 / TOTAL_MONITORS))
echo "📊 团队遵循度: $COMPLIANCE_SCORE% ($MONITORING_SCORE/$TOTAL_MONITORS)"

if [ "$COMPLIANCE_SCORE" -ge 80 ]; then
    COMPLIANCE_LEVEL="优秀 🏆"
    echo -e "${GREEN}🏆 团队遵循度: $COMPLIANCE_LEVEL${NC}"
elif [ "$COMPLIANCE_SCORE" -ge 60 ]; then
    COMPLIANCE_LEVEL="良好 👍"
    echo -e "${YELLOW}👍 团队遵循度: $COMPLIANCE_LEVEL${NC}"
else
    COMPLIANCE_LEVEL="需改进 ⚠️"
    echo -e "${RED}⚠️ 团队遵循度: $COMPLIANCE_LEVEL${NC}"
fi

# 完成报告
cat >> "$MONITORING_REPORT" << EOF

================================
团队遵循度评估
================================
监控得分: $COMPLIANCE_SCORE/100 ($MONITORING_SCORE/$TOTAL_MONITORS)
遵循度等级: $COMPLIANCE_LEVEL

建议措施:
EOF

# 根据遵循度给出建议
if [ ! -f "$PRE_COMMIT_HOOK" ]; then
    echo "- 安装pre-commit钩子进行自动检查" >> "$MONITORING_REPORT"
fi

if [ "$NON_EXECUTABLE_SCRIPTS" -gt 0 ]; then
    echo "- 为脚本文件添加执行权限" >> "$MONITORING_REPORT"
fi

if [ "$RECENT_ENTITY_VIOLATIONS" -gt 0 ]; then
    echo "- 加强团队架构规范培训" >> "$MONITORING_REPORT"
fi

if [ "$ARCHITECTURE_WORKFLOW" -eq 0 ]; then
    echo "- 添加架构检查到CI/CD流水线" >> "$MONITORING_REPORT"
fi

cat >> "$MONITORING_REPORT" << EOF

监控建议:
- 每日运行持续监控检查
- 每周生成架构健康报告
- 每月评估团队遵循度趋势
- 季度回顾和改进监控机制

================================
报告完成时间: $(date '+%Y-%m-%d %H:%M:%S')
================================
EOF

# 显示结果
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                🔄 持续监控检查结果${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "📊 团队遵循度: ${GREEN}$COMPLIANCE_SCORE%${NC}"
echo -e "🏆 遵循度等级: ${GREEN}$COMPLIANCE_LEVEL${NC}"
echo -e "📋 监控报告: ${CYAN}$MONITORING_REPORT${NC}"
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# 显示改进建议
echo -e "${CYAN}🎯 改进建议:${NC}"
if [ ! -f "$PRE_COMMIT_HOOK" ]; then
    echo "• 安装pre-commit钩子进行自动架构检查"
fi
if [ "$ARCHITECTURE_WORKFLOW" -eq 0 ]; then
    echo "• 添加架构检查到GitHub Actions工作流"
fi
if [ "$NON_EXECUTABLE_SCRIPTS" -gt 0 ]; then
    echo "• 为验证脚本添加执行权限"
fi
if [ "$RECENT_ENTITY_VIOLATIONS" -gt 0 ]; then
    echo "• 检查最近提交的Entity违规问题"
fi

echo ""
echo -e "${YELLOW}💡 建议设置定时任务定期运行: bash scripts/continuous-monitoring-check.sh${NC}"
echo -e "${YELLOW}💡 或集成到CI/CD流水线进行持续监控${NC}"