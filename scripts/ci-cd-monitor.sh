#!/bin/bash
###############################################################################
# CI/CD架构合规性监控脚本
# 功能: 检查CI/CD工作流状态并生成监控报告
# 使用: ./scripts/ci-cd-monitor.sh
###############################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║        CI/CD架构合规性监控仪表板                          ║${NC}"
echo -e "${BLUE}║        生成时间: $(date '+%Y-%m-%d %H:%M:%S')                       ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

###############################################################################
# 1. 检查GitHub CLI是否安装
###############################################################################

if ! command -v gh &> /dev/null; then
    echo -e "${YELLOW}⚠️  GitHub CLI (gh) 未安装，跳过GitHub Actions数据获取${NC}"
    echo -e "${YELLOW}   安装方法: https://cli.github.com/${NC}"
    GITHUB_CLI_AVAILABLE=false
else
    GITHUB_CLI_AVAILABLE=true
fi

###############################################################################
# 2. 本地架构合规性检查
###############################################################################

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}📊 本地架构合规性检查${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# 检查@Repository使用
echo -n "检查@Repository使用: "
REPOSITORY_COUNT=$(grep -r "@Repository" microservices/*/src --include="*.java" 2>/dev/null | wc -l)
if [ "$REPOSITORY_COUNT" -eq 0 ]; then
    echo -e "${GREEN}✅ 通过 (0处违规)${NC}"
else
    echo -e "${YELLOW}⚠️  警告 (${REPOSITORY_COUNT}处违规)${NC}"
fi

# 检查@Autowired使用
echo -n "检查@Autowired使用: "
AUTOWIRED_COUNT=$(grep -r "@Autowired" microservices/*/src --include="*.java" 2>/dev/null | wc -l)
if [ "$AUTOWIRED_COUNT" -eq 0 ]; then
    echo -e "${GREEN}✅ 通过 (0处违规)${NC}"
else
    echo -e "${YELLOW}⚠️  警告 (${AUTOWIRED_COUNT}处违规)${NC}"
fi

# 检查超大Entity
echo -n "检查超大Entity (>400行): "
OVERSIZED=0
for file in $(find microservices/*/src -name "*Entity.java" -type f 2>/dev/null); do
    LINES=$(wc -l < "$file" 2>/dev/null || echo 0)
    if [ "$LINES" -gt 400 ]; then
        OVERSIZED=$((OVERSIZED + 1))
    fi
done
if [ "$OVERSIZED" -eq 0 ]; then
    echo -e "${GREEN}✅ 通过 (0个超大Entity)${NC}"
else
    echo -e "${RED}❌ 失败 (${OVERSIZED}个超大Entity)${NC}"
fi

# 检查Entity业务逻辑
echo -n "检查Entity业务逻辑: "
BUSINESS_LOGIC=0
for file in $(find microservices/*/src -name "*Entity.java" -type f 2>/dev/null); do
    if grep -E "public (?!boolean|class|Double|Float|Int|Long|String)" "$file" 2>/dev/null | \
       grep -v "Entity\|get\|set\|is\|can\|equals\|hashCode\|toString" > /dev/null; then
        BUSINESS_LOGIC=$((BUSINESS_LOGIC + 1))
    fi
done
if [ "$BUSINESS_LOGIC" -eq 0 ]; then
    echo -e "${GREEN}✅ 通过 (0个Entity包含业务逻辑)${NC}"
else
    echo -e "${YELLOW}⚠️  警告 (${BUSINESS_LOGIC}个Entity包含业务逻辑)${NC}"
fi

# 检查Entity导入一致性
echo -n "检查Entity导入一致性: "
IMPORT_VIOLATIONS=$(grep -r "import net\.lab1024\.sa\.\(access\|attendance\|consume\|video\|visitor\)\.entity\.\(User\|Department\|Area\|Device\)Entity" \
   microservices/*/src --include="*.java" 2>/dev/null | wc -l)
if [ "$IMPORT_VIOLATIONS" -eq 0 ]; then
    echo -e "${GREEN}✅ 通过 (0处导入不一致)${NC}"
else
    echo -e "${RED}❌ 失败 (${IMPORT_VIOLATIONS}处导入不一致)${NC}"
fi

echo ""

###############################################################################
# 3. GitHub Actions状态（如果可用）
###############################################################################

if [ "$GITHUB_CLI_AVAILABLE" = true ]; then
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}📊 GitHub Actions执行状态${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""

    # 获取最近7天的运行记录
    echo "最近7天运行记录:"
    gh run list --workflow=architecture-compliance.yml --json conclusion,databaseId,createdAt,displayTitle --limit 7 --jq '.[] | "  \(.createdAt | split("T")[0]): \(.conclusion // "unknown") - \(.displayTitle)"' 2>/dev/null || echo -e "${YELLOW}  ⚠️  无法获取运行记录${NC}"

    echo ""

    # 获取最新运行结果
    echo "最新运行状态:"
    gh run view --workflow=architecture-compliance.yml --json conclusion,createdAt,displayTitle --jq 'if .conclusion == "success" then "  ✅ 成功" elif .conclusion == "failure" then "  ❌ 失败" else "  ⏳ 运行中" end' 2>/dev/null || echo -e "${YELLOW}  ⚠️  无法获取最新状态${NC}"

    echo ""
fi

###############################################################################
# 4. 总体合规性评分
###############################################################################

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}📈 总体合规性评分${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# 计算合规性得分
TOTAL_CHECKS=5
PASSED_CHECKS=0

[ "$REPOSITORY_COUNT" -eq 0 ] && PASSED_CHECKS=$((PASSED_CHECKS + 1))
[ "$AUTOWIRED_COUNT" -eq 0 ] && PASSED_CHECKS=$((PASSED_CHECKS + 1))
[ "$OVERSIZED" -eq 0 ] && PASSED_CHECKS=$((PASSED_CHECKS + 1))
[ "$BUSINESS_LOGIC" -eq 0 ] && PASSED_CHECKS=$((PASSED_CHECKS + 1))
[ "$IMPORT_VIOLATIONS" -eq 0 ] && PASSED_CHECKS=$((PASSED_CHECKS + 1))

SCORE=$((PASSED_CHECKS * 100 / TOTAL_CHECKS))

echo "合规性得分: ${SCORE}%"

if [ $SCORE -eq 100 ]; then
    echo -e "评级: ${GREEN}🟢 优秀 (100%)${NC}"
elif [ $SCORE -ge 90 ]; then
    echo -e "评级: ${GREEN}🟢 良好 (${SCORE}%)${NC}"
elif [ $SCORE -ge 80 ]; then
    echo -e "评级: ${YELLOW}🟡 及格 (${SCORE}%)${NC}"
else
    echo -e "评级: ${RED}🔴 不及格 (${SCORE}%)${NC}"
fi

echo ""

###############################################################################
# 5. 告警和行动建议
###############################################################################

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}⚠️  告警和行动建议${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [ "$AUTOWIRED_COUNT" -gt 0 ]; then
    echo -e "${YELLOW}🔔 @Autowired使用违规 (${AUTOWIRED_COUNT}处)${NC}"
    echo "   行动建议: 使用@Resource替换@Autowired"
    echo "   优先级: P1"
    echo "   截止时间: 3天内"
    echo ""
fi

if [ "$OVERSIZED" -gt 0 ]; then
    echo -e "${RED}🔔 超大Entity (${OVERSIZED}个)${NC}"
    echo "   行动建议: 拆分超大Entity或移除部分字段"
    echo "   优先级: P0 (致命)"
    echo "   截止时间: 立即"
    echo ""
fi

if [ "$BUSINESS_LOGIC" -gt 0 ]; then
    echo -e "${YELLOW}🔔 Entity包含业务逻辑 (${BUSINESS_LOGIC}个)${NC}"
    echo "   行动建议: Week 2 Day 6-7执行清理"
    echo "   优先级: P1"
    echo "   截止时间: Week 2结束前"
    echo ""
fi

if [ "$IMPORT_VIOLATIONS" -gt 0 ]; then
    echo -e "${RED}🔔 Entity导入不一致 (${IMPORT_VIOLATIONS}处)${NC}"
    echo "   行动建议: 修正Entity导入路径"
    echo "   优先级: P0 (致命)"
    echo "   截止时间: 立即"
    echo ""
fi

if [ $SCORE -eq 100 ]; then
    echo -e "${GREEN}✅ 无告警，系统运行正常！${NC}"
fi

echo ""

###############################################################################
# 6. Week进度追踪
###############################################################################

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}📅 Week进度追踪${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

CURRENT_WEEK=$(date +%U)
WEEK1_START=$(date -d "2025-12-26" +%U)
WEEK2_START=$((WEEK1_START + 1))

if [ $CURRENT_WEEK -eq $WEEK1_START ]; then
    echo "当前阶段: Week 1 (Entity管理规范 + CI/CD增强)"
    echo "状态: ✅ 已完成"
    echo "进度: 100% (7/7天)"
elif [ $CURRENT_WEEK -eq $WEEK2_START ]; then
    echo "当前阶段: Week 2 (代码质量提升)"
    echo "状态: ⏳ 执行中"
    echo "进度: 待更新"
    echo ""
    echo "Week 2任务:"
    echo "  [ ] Day 6-7: 清理Entity业务逻辑"
    echo "  [ ] Day 8-9: 重组common-util模块"
    echo "  [ ] Day 10: 架构演进文档"
else
    echo "当前阶段: Week 3-4 (性能优化 + 文档完善)"
    echo "状态: ⏳ 待开始"
fi

echo ""

###############################################################################
# 7. 下一步行动
###############################################################################

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}🎯 下一步行动${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [ $CURRENT_WEEK -eq $WEEK1_START ]; then
    echo "✅ Week 1任务完成"
    echo "⏭️  准备开始Week 2任务"
    echo ""
    echo "执行命令:"
    echo "  bash scripts/week2-day67-cleanup-entity-logic.sh"
elif [ $CURRENT_WEEK -eq $WEEK2_START ]; then
    echo "⏳ Week 2任务执行中"
    echo ""
    echo "当前任务优先级:"
    if [ "$BUSINESS_LOGIC" -gt 0 ]; then
        echo "  1. 清理Entity业务逻辑 (${BUSINESS_LOGIC}个Entity待处理)"
    fi
    echo "  2. 重组common-util模块"
    echo "  3. 创建架构演进文档"
else
    echo "⏭️  Week 1-2已完成，准备Week 3-4任务"
fi

echo ""

###############################################################################
# 8. 监控报告生成
###############################################################################

REPORT_FILE="reports/ci-cd-monitoring-$(date +%Y%m%d-%H%M%S).txt"
mkdir -p reports

cat > "$REPORT_FILE" << REPORT
CI/CD架构合规性监控报告
生成时间: $(date '+%Y-%m-%d %H:%M:%S')

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
本地架构合规性检查
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Repository使用:     ${REPOSITORY_COUNT}处违规
@Autowired使用:      ${AUTOWIRED_COUNT}处违规
超大Entity (>400行):  ${OVERSIZED}个
Entity业务逻辑:       ${BUSINESS_LOGIC}个
Entity导入一致性:     ${IMPORT_VIOLATIONS}处违规

总体合规性评分: ${SCORE}%
$(if [ $SCORE -ge 90 ]; then echo "评级: 🟢 优秀"; elif [ $SCORE -ge 80 ]; then echo "评级: 🟡 良好"; else echo "评级: 🔴 需改进"; fi)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
告警和行动建议
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

$(if [ "$AUTOWIRED_COUNT" -gt 0 ]; then echo "🔔 @Autowired使用违规 - 优先级: P1"; fi)
$(if [ "$OVERSIZED" -gt 0 ]; then echo "🔔 超大Entity - 优先级: P0"; fi)
$(if [ "$BUSINESS_LOGIC" -gt 0 ]; then echo "🔔 Entity业务逻辑 - 优先级: P1"; fi)
$(if [ "$IMPORT_VIOLATIONS" -gt 0 ]; then echo "🔔 Entity导入不一致 - 优先级: P0"; fi)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Week进度
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

$(if [ $CURRENT_WEEK -eq $WEEK1_START ]; then echo "Week 1: ✅ 已完成"; elif [ $CURRENT_WEEK -eq $WEEK2_START ]; then echo "Week 2: ⏳ 执行中"; else echo "Week 3-4: ⏳ 待开始"; fi)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
下一步行动
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

查看详细监控仪表板: cat scripts/CI_CD_MONITORING_DASHBOARD.md
执行Week 2任务: bash scripts/week2-execution-plan.sh

报告结束
REPORT

echo -e "${GREEN}✅ 监控报告已生成: ${REPORT_FILE}${NC}"
echo ""

###############################################################################
# 结束
###############################################################################

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}✅ CI/CD监控检查完成${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

exit 0
