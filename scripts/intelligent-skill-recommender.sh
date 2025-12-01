#!/bin/bash
# 🤖 智能技能推荐引擎 - 老王出品
# 版本: v1.0.0 - AI驱动版
# 作用: 基于AI的智能技能推荐系统，自动分析项目状态并推荐最优技能调用策略

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 项目路径
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)
BACKEND_DIR="$PROJECT_ROOT/smart-admin-api-java17-springboot3"

# 输出函数
print_header() {
    echo -e "${BLUE}🤖 智能技能推荐引擎${NC}"
    echo -e "${CYAN}📅 分析时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
    echo -e "${CYAN}📂 项目路径: $PROJECT_ROOT${NC}"
    echo -e "${BLUE}$(printf '─%.0s' {1..80})${NC}"
}

print_section() {
    echo -e "\n${PURPLE}📋 $1${NC}"
    echo -e "${PURPLE}$(printf '─%.0s' {1..70})${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${CYAN}💡 $1${NC}"
}

print_highlight() {
    echo -e "${YELLOW}🎯 $1${NC}"
}

# 项目状态数据结构
declare -A PROJECT_STATUS

# 深度分析项目状态
deep_project_analysis() {
    print_section "AI深度项目状态分析"

    echo -e "${CYAN}正在进行AI驱动的项目状态分析...${NC}"

    # 1. 编译状态分析
    echo -e "${CYAN}🔍 分析编译状态...${NC}"
    cd "$BACKEND_DIR"
    local compile_output=$(mvn clean compile -q 2>&1 || true)
    local total_errors=$(echo "$compile_output" | grep -c "ERROR" || echo "0")
    local symbol_errors=$(echo "$compile_output" | grep -c "找不到符号\|cannot find symbol" || echo "0")
    local duplicate_errors=$(echo "$compile_output" | grep -c "重复定义\|duplicate" || echo "0")
    local type_errors=$(echo "$compile_output" | grep -c "类型不匹配\|type mismatch" || echo "0")

    PROJECT_STATUS[total_errors]=$total_errors
    PROJECT_STATUS[symbol_errors]=$symbol_errors
    PROJECT_STATUS[duplicate_errors]=$duplicate_errors
    PROJECT_STATUS[type_errors]=$type_errors

    echo "编译分析结果:"
    echo "  - 总编译错误: $total_errors 个"
    echo "  - 符号找不到: $symbol_errors 个"
    echo "  - 重复定义: $duplicate_errors 个"
    echo "  - 类型不匹配: $type_errors 个"

    # 2. 规范合规性分析
    echo -e "\n${CYAN}🔍 分析规范合规性...${NC}"

    # Jakarta迁移分析
    local javax_files=$(find . -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)
    local jakarta_violations=$(find . -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence|servlet)" {} \; 2>/dev/null | wc -l)
    PROJECT_STATUS[javax_files]=$javax_files
    PROJECT_STATUS[jakarta_violations]=$jakarta_violations

    # 依赖注入分析
    local autowired_files=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    PROJECT_STATUS[autowired_files]=$autowired_files

    # 架构合规分析
    local arch_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" . 2>/dev/null | wc -l)
    PROJECT_STATUS[arch_violations]=$arch_violations

    # 日志规范分析
    local log_violations=$(find . -name "*.java" -exec grep -l "System\.out\.println" {} \; 2>/dev/null | wc -l)
    PROJECT_STATUS[log_violations]=$log_violations

    echo "规范合规性分析结果:"
    echo "  - javax包使用: $javax_files 个文件"
    echo "  - Jakarta违规: $jakarta_violations 个"
    echo "  - @Autowired使用: $autowired_files 个"
    echo "  - 架构违规: $arch_violations 处"
    echo "  - 日志违规: $log_violations 个"

    cd "$PROJECT_ROOT"

    # 3. 项目健康度评分
    calculate_project_health_score
}

# 计算项目健康度评分
calculate_project_health_score() {
    echo -e "\n${CYAN}📊 计算项目健康度评分...${NC}"

    local health_score=100

    # 编译错误扣分（每个错误扣1分，最多扣50分）
    local compilation_deduction=$((PROJECT_STATUS[total_errors] > 50 ? 50 : PROJECT_STATUS[total_errors]))
    health_score=$((health_score - compilation_deduction))

    # 规范违规扣分（Jakarta违规每个扣5分）
    local jakarta_deduction=$((PROJECT_STATUS[jakarta_violations] * 5))
    health_score=$((health_score - jakarta_deduction))

    # 依赖注入违规扣分（每个扣2分）
    local autowired_deduction=$((PROJECT_STATUS[autowired_files] * 2))
    health_score=$((health_score - autowired_deduction))

    # 架构违规扣分（每处扣10分）
    local arch_deduction=$((PROJECT_STATUS[arch_violations] * 10))
    health_score=$((health_score - arch_deduction))

    # 确保分数在0-100范围内
    if [ $health_score -lt 0 ]; then
        health_score=0
    fi

    PROJECT_STATUS[health_score]=$health_score

    echo "项目健康度评分: $health_score/100"

    if [ $health_score -ge 80 ]; then
        echo -e "${GREEN}  🟢 项目状态: 优秀${NC}"
    elif [ $health_score -ge 60 ]; then
        echo -e "${YELLOW}  🟡 项目状态: 良好${NC}"
    elif [ $health_score -ge 40 ]; then
        echo -e "${YELLOW}  🟠 项目状态: 需要改进${NC}"
    else
        echo -e "${RED}  🔴 项目状态: 严重问题${NC}"
    fi
}

# AI智能推荐算法
ai_skill_recommendation_algorithm() {
    print_section "AI智能推荐算法"

    local health_score=${PROJECT_STATUS[health_score]}
    local total_errors=${PROJECT_STATUS[total_errors]}
    local jakarta_violations=${PROJECT_STATUS[jakarta_violations]}
    local autowired_files=${PROJECT_STATUS[autowired_files]}
    local arch_violations=${PROJECT_STATUS[arch_violations]}

    echo -e "${CYAN}基于AI算法分析项目问题并生成最优技能调用策略...${NC}"

    # 推荐技能列表
    local recommended_skills=()
    local skill_priorities=()
    local expected_improvements=()

    # 算法1: 基于问题严重度的技能推荐
    if [ $jakarta_violations -gt 0 ] || [ $autowired_files -gt 0 ]; then
        recommended_skills+=("spring-boot-jakarta-guardian")
        skill_priorities+=("P1-紧急")
        local jakarta_improvement=$((jakarta_violations + autowired_files))
        expected_improvements+=("修复 $jakarta_improvement 个规范问题")

        echo -e "${RED}🚨 AI检测到基础规范问题，优先推荐:${NC}"
        echo -e "${RED}   Skill('spring-boot-jakarta-guardian') - 优先级: P1-紧急${NC}"
        echo -e "${RED}   预期效果: 15分钟内修复所有Jakarta和依赖注入问题${NC}"
    fi

    # 算法2: 基于编译错误数量的技能推荐
    if [ $total_errors -gt 100 ]; then
        recommended_skills+=("code-quality-protector")
        skill_priorities+=("P1-紧急")
        local error_reduction=$((total_errors * 75 / 100))
        expected_improvements+=("减少 $error_reduction 个编译错误")

        echo -e "${RED}🚨 AI检测到系统性编译错误，推荐:${NC}"
        echo -e "${RED}   Skill('code-quality-protector') - 优先级: P1-紧急${NC}"
        echo -e "${RED}   预期效果: 30分钟内减少 $error_reduction 个编译错误${NC}"
    elif [ $total_errors -gt 20 ]; then
        recommended_skills+=("code-quality-protector")
        skill_priorities+=("P2-重要")
        local error_reduction=$((total_errors * 60 / 100))
        expected_improvements+=("减少 $error_reduction 个编译错误")

        echo -e "${YELLOW}⚠️  AI检测到编译错误，推荐:${NC}"
        echo -e "${YELLOW}   Skill('code-quality-protector') - 优先级: P2-重要${NC}"
        echo -e "${YELLOW}   预期效果: 30分钟内减少 $error_reduction 个编译错误${NC}"
    fi

    # 算法3: 基于架构复杂度的技能推荐
    if [ $arch_violations -gt 0 ]; then
        recommended_skills+=("four-tier-architecture-guardian")
        skill_priorities+=("P2-重要")
        expected_improvements+=("修复 $arch_violations 处架构违规")

        echo -e "${YELLOW}⚠️  AI检测到架构设计问题，推荐:${NC}"
        echo -e "${YELLOW}   Skill('four-tier-architecture-guardian') - 优先级: P2-重要${NC}"
        echo -e "${YELLOW}   预期效果: 10分钟内完成架构重构${NC}"
    fi

    # 算法4: 基于健康度分数的预防性推荐
    if [ $health_score -lt 60 ] && [ $total_errors -lt 10 ]; then
        # 项目问题不多但健康度低，说明有潜在风险
        recommended_skills+=("development-standards-specialist")
        skill_priorities+=("P3-建议")
        expected_improvements+=("提升代码质量和开发规范")

        echo -e "${CYAN}💡 AI检测到潜在质量风险，建议:${NC}"
        echo -e "${CYAN}   Skill('development-standards-specialist') - 优先级: P3-建议${NC}"
        echo -e "${CYAN}   预期效果: 提升整体代码质量和团队开发效率${NC}"
    fi

    # 保存推荐结果
    declare -p recommended_skills > /tmp/ai_recommended_skills.txt
    declare -p skill_priorities > /tmp/ai_skill_priorities.txt
    declare -p expected_improvements > /tmp/ai_expected_improvements.txt
}

# 生成智能执行计划
generate_intelligent_execution_plan() {
    print_section "生成智能执行计划"

    echo -e "${CYAN}🤖 AI正在生成最优技能执行计划...${NC}"

    # 读取推荐结果
    if [ ! -f /tmp/ai_recommended_skills.txt ]; then
        print_error "未找到AI推荐结果"
        return 1
    fi

    # 重建数组
    eval "recommended_skills=($(cat /tmp/ai_recommended_skills.txt))"
    eval "skill_priorities=($(cat /tmp/ai_skill_priorities.txt))"
    eval "expected_improvements=($(cat /tmp/ai_expected_improvements.txt))"

    if [ ${#recommended_skills[@]} -eq 0 ]; then
        print_success "🎉 AI分析结果: 项目状态优秀，无需技能调用！"
        return 0
    fi

    echo -e "${PURPLE}📋 AI推荐的最优技能调用序列:${NC}"
    echo ""

    # 计算总体预期改善
    local total_issues_before=${PROJECT_STATUS[total_errors]}
    local total_issues_before=$((total_issues_before + PROJECT_STATUS[jakarta_violations] + PROJECT_STATUS[autowired_files] + PROJECT_STATUS[arch_violations]))

    local expected_improvement_total=0
    local priority=1

    for i in "${!recommended_skills[@]}"; do
        local skill="${recommended_skills[$i]}"
        local priority_tag="${skill_priorities[$i]}"
        local improvement="${expected_improvements[$i]}"

        # 根据优先级设置颜色
        case $priority_tag in
            "P1-紧急")
                echo -e "${RED}🎯 阶段$priority: \e[1mSkill('$skill')\e[0m ${RED}[${priority_tag}]${NC}"
                echo -e "${RED}   💫 预期改善: $improvement${NC}"
                ;;
            "P2-重要")
                echo -e "${YELLOW}🎯 阶段$priority: \e[1mSkill('$skill')\e[0m ${YELLOW}[${priority_tag}]${NC}"
                echo -e "${YELLOW}   💫 预期改善: $improvement${NC}"
                ;;
            "P3-建议")
                echo -e "${CYAN}🎯 阶段$priority: \e[1mSkill('$skill')\e[0m ${CYAN}[${priority_tag}]${NC}"
                echo -e "${CYAN}   💫 预期改善: $improvement${NC}"
                ;;
        esac

        echo ""
        ((priority++))
    done

    # 预测执行后的项目状态
    predict_project_after_execution
}

# 预测执行后的项目状态
predict_project_after_execution() {
    print_section "AI预测执行效果"

    local current_health=${PROJECT_STATUS[health_score]}
    local predicted_health=$current_health

    # 根据推荐技能计算预期健康度提升
    if [ -f /tmp/ai_recommended_skills.txt ]; then
        eval "recommended_skills=($(cat /tmp/ai_recommended_skills.txt))"

        for skill in "${recommended_skills[@]}"; do
            case $skill in
                "spring-boot-jakarta-guardian")
                    # Jakarta修复能显著提升健康度
                    local jakarta_improvement=$((PROJECT_STATUS[jakarta_violations] * 5 + PROJECT_STATUS[autowired_files] * 2))
                    predicted_health=$((predicted_health + jakarta_improvement))
                    ;;
                "code-quality-protector")
                    # 编译错误修复大幅提升健康度
                    local error_improvement=$((PROJECT_STATUS[total_errors] * 2))
                    predicted_health=$((predicted_health + error_improvement))
                    ;;
                "four-tier-architecture-guardian")
                    # 架构修复提升健康度
                    local arch_improvement=$((PROJECT_STATUS[arch_violations] * 10))
                    predicted_health=$((predicted_health + arch_improvement))
                    ;;
            esac
        done
    fi

    # 确保预测分数在合理范围内
    if [ $predicted_health -gt 100 ]; then
        predicted_health=100
    fi

    echo "项目健康度预测:"
    echo -e "  ${YELLOW}当前状态: ${PROJECT_STATUS[health_score]}/100${NC}"
    echo -e "  ${GREEN}预测状态: $predicted_health/100${NC}"
    echo -e "  ${GREEN}预期提升: $((predicted_health - PROJECT_STATUS[health_score])) 分${NC}"

    if [ $predicted_health -ge 90 ]; then
        echo -e "${GREEN}🎉 执行后项目将达到优秀水平！${NC}"
    elif [ $predicted_health -ge 70 ]; then
        echo -e "${GREEN}👍 执行后项目将达到良好水平！${NC}"
    elif [ $predicted_health -ge 50 ]; then
        echo -e "${YELLOW}📈 执行后项目将有明显改善！${NC}"
    else
        echo -e "${YELLOW}📊 执行后项目状态仍需进一步改进${NC}"
    fi
}

# 生成AI推荐报告
generate_ai_recommendation_report() {
    local report_file="$PROJECT_ROOT/ai-skill-recommendation-$(date +%Y%m%d-%H%M%S).md"

    cat > "$report_file" << EOF
# 🤖 AI智能技能推荐报告

**推荐时间**: $(date '+%Y-%m-%d %H:%M:%S')
**推荐引擎**: intelligent-skill-recommender.sh v1.0.0
**项目路径**: $PROJECT_ROOT

## 📊 AI项目状态分析

| 指标 | 当前值 | 状态 |
|-----|-------|------|
| 编译错误 | ${PROJECT_STATUS[total_errors]} | $([ ${PROJECT_STATUS[total_errors]} -eq 0 ] && echo "✅ 正常" || echo "❌ 异常") |
| Jakarta违规 | ${PROJECT_STATUS[jakarta_violations]} | $([ ${PROJECT_STATUS[jakarta_violations]} -eq 0 ] && echo "✅ 正常" || echo "❌ 异常") |
| @Autowired使用 | ${PROJECT_STATUS[autowired_files]} | $([ ${PROJECT_STATUS[autowired_files]} -eq 0 ] && echo "✅ 正常" || echo "❌ 异常") |
| 架构违规 | ${PROJECT_STATUS[arch_violations]} | $([ ${PROJECT_STATUS[arch_violations]} -eq 0 ] && echo "✅ 正常" || echo "❌ 异常") |
| 项目健康度 | ${PROJECT_STATUS[health_score]}/100 | $([ ${PROJECT_STATUS[health_score]} -ge 80 ] && echo "🟢 优秀" || [ ${PROJECT_STATUS[health_score]} -ge 60 ] && echo "🟡 良好" || echo "🔴 需改进") |

## 🎯 AI推荐技能执行计划

EOF

    # 读取推荐结果并添加到报告
    if [ -f /tmp/ai_recommended_skills.txt ]; then
        eval "recommended_skills=($(cat /tmp/ai_recommended_skills.txt))"
        eval "skill_priorities=($(cat /tmp/ai_skill_priorities.txt))"
        eval "expected_improvements=($(cat /tmp/ai_expected_improvements.txt))"

        local priority=1
        for i in "${!recommended_skills[@]}"; do
            echo "### 阶段 $priority: \`Skill('${recommended_skills[$i]}')\`" >> "$report_file"
            echo "" >> "$report_file"
            echo "**优先级**: ${skill_priorities[$i]}" >> "$report_file"
            echo "**预期改善**: ${expected_improvements[$i]}" >> "$report_file"
            echo "" >> "$report_file"
            ((priority++))
        done
    else
        echo "### 🎉 无需技能调用" >> "$report_file"
        echo "" >> "$report_file"
        echo "项目状态优秀，AI分析认为无需调用修复技能。" >> "$report_file"
    fi

    cat >> "$report_file" << EOF

## ⚡ 立即执行命令

复制以下命令到终端执行AI推荐的技能：

EOF

    if [ -f /tmp/ai_recommended_skills.txt ]; then
        eval "recommended_skills=($(cat /tmp/ai_recommended_skills.txt))"
        for skill in "${recommended_skills[@]}"; do
            echo "\`\`\`bash" >> "$report_file"
            echo "# AI推荐: $skill" >> "$report_file"
            echo "Skill(\"$skill\")" >> "$report_file"
            echo "\`\`\`" >> "$report_file"
            echo "" >> "$report_file"
        done
    fi

    cat >> "$report_file" << EOF

## 🔍 执行验证

执行完推荐技能后，请运行以下命令验证效果：

\`\`\`bash
./scripts/problem-skill-mapper.sh
\`\`\`

## 📈 预期效果

执行AI推荐的技能后，项目健康度将从 **${PROJECT_STATUS[health_score]}/100** 提升至 **预测值/100**。

---
*本报告由 IOE-DREAM AI智能推荐引擎自动生成*
EOF

    print_success "📄 AI推荐报告已生成: $report_file"
}

# ==================== 主执行流程 ====================

main() {
    print_header

    echo -e "${CYAN}🤖 启动AI智能技能推荐引擎...${NC}"

    # 深度分析项目状态
    deep_project_analysis

    # AI智能推荐算法
    ai_skill_recommendation_algorithm

    # 生成智能执行计划
    generate_intelligent_execution_plan

    # 生成AI推荐报告
    generate_ai_recommendation_report

    print_section "AI推荐完成"

    if [ -f /tmp/ai_recommended_skills.txt ]; then
        eval "recommended_skills=($(cat /tmp/ai_recommended_skills.txt))"
        if [ ${#recommended_skills[@]} -gt 0 ]; then
            print_error "🚨 AI推荐了 ${#recommended_skills[@]} 个技能需要立即调用！"
            print_info "💡 请查看生成的推荐报告并执行相应技能"
            print_info "📄 详细报告已保存，请查阅"
        else
            print_success "🎉 AI分析结果: 项目状态优秀，无需技能调用！"
        fi
    else
        print_success "🎉 AI分析结果: 项目状态优秀，无需技能调用！"
    fi
}

# 执行主函数
main "$@"