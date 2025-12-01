#!/bin/bash
# 🚨 强制技能调用检查系统 - 老王出品
# 版本: v1.0.0 - 铁腕执行版
# 作用: 强制检查技能调用情况，不调用技能禁止提交！

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
    echo -e "${BLUE}🔒 强制技能调用检查系统${NC}"
    echo -e "${CYAN}📅 检查时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
    echo -e "${CYAN}📂 项目路径: $PROJECT_ROOT${NC}"
    echo -e "${BLUE}$(printf '─%.0s' {1..70})${NC}"
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

# 检查最近的技能调用记录
check_recent_skill_calls() {
    echo -e "\n${PURPLE}📋 检查最近的技能调用记录${NC}"

    # 检查最近1小时内的技能调用
    local recent_skills=$(git log --since="1 hour ago" --grep="Skill(" --oneline 2>/dev/null || echo "")

    if [ -n "$recent_skills" ]; then
        print_success "发现最近的技能调用记录:"
        echo "$recent_skills" | head -5
        return 0
    else
        print_warning "最近1小时内未发现技能调用记录"
        return 1
    fi
}

# 检查当前项目状态是否需要技能调用
check_project_status() {
    echo -e "\n${PURPLE}🔍 检查项目当前状态${NC}"

    # 快速检查是否还有需要技能调用的问题
    local javax_count=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence|servlet)" {} \; 2>/dev/null | wc -l)
    local autowired_count=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    local error_count=$(cd "$BACKEND_DIR" && mvn compile -q 2>&1 | grep -c "ERROR" 2>/dev/null || echo "0")

    echo "项目状态快照:"
    echo "  - javax EE包问题: $javax_count 个"
    echo "  - @Autowired问题: $autowired_count 个"
    echo "  - 编译错误: $error_count 个"

    local total_issues=$((javax_count + autowired_count + error_count))

    if [ $total_issues -gt 0 ]; then
        print_error "发现 $total_issues 个问题需要技能调用解决"
        return 1
    else
        print_success "项目状态良好，无需技能调用"
        return 0
    fi
}

# 智能推荐需要调用的技能
recommend_required_skills() {
    echo -e "\n${PURPLE}🤖 智能推荐需要调用的技能${NC}"

    local javax_count=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence|servlet)" {} \; 2>/dev/null | wc -l)
    local autowired_count=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    local error_count=$(cd "$BACKEND_DIR" && mvn compile -q 2>&1 | grep -c "ERROR" 2>/dev/null || echo "0")

    local recommended_skills=()

    if [ $javax_count -gt 0 ] || [ $autowired_count -gt 0 ]; then
        recommended_skills+=("spring-boot-jakarta-guardian")
        echo -e "${RED}🎯 必须调用: Skill('spring-boot-jakarta-guardian')${NC}"
        echo -e "   ${RED}   解决: Jakarta迁移和依赖注入问题 ($((javax_count + autowired_count)) 个)${NC}"
    fi

    if [ $error_count -gt 50 ]; then
        recommended_skills+=("code-quality-protector")
        echo -e "${RED}🎯 必须调用: Skill('code-quality-protector')${NC}"
        echo -e "   ${RED}   解决: 系统性编译错误 ($error_count 个)${NC}"
    fi

    # 保存推荐技能到临时文件
    printf "%s\n" "${recommended_skills[@]}" > /tmp/recommended_skills.txt
}

# 验证技能调用是否有效
validate_skill_effectiveness() {
    echo -e "\n${PURPLE}⚡ 验证技能调用效果${NC}"

    # 获取推荐技能列表
    if [ ! -f /tmp/recommended_skills.txt ]; then
        print_warning "未找到推荐技能列表"
        return 1
    fi

    local recommended_skills=($(cat /tmp/recommended_skills.txt))

    if [ ${#recommended_skills[@]} -eq 0 ]; then
        print_success "无需技能调用验证"
        return 0
    fi

    echo "验证推荐技能的调用情况..."

    # 检查是否调用了推荐技能
    local all_skills_called=true

    for skill in "${recommended_skills[@]}"; do
        local skill_call_count=$(git log --since="2 hours ago" --grep="Skill('$skill')" --oneline | wc -l)

        if [ $skill_call_count -eq 0 ]; then
            print_error "❌ 未调用必需技能: Skill('$skill')"
            all_skills_called=false
        else
            print_success "✅ 已调用技能: Skill('$skill') ($skill_call_count 次)"
        fi
    done

    if [ "$all_skills_called" = true ]; then
        print_success "所有推荐技能都已调用"
        return 0
    else
        print_error "存在未调用的必需技能"
        return 1
    fi
}

# 创建技能调用模板
create_skill_call_template() {
    echo -e "\n${PURPLE}📝 创建技能调用模板${NC}"

    if [ ! -f /tmp/recommended_skills.txt ]; then
        return 0
    fi

    local recommended_skills=($(cat /tmp/recommended_skills.txt))

    if [ ${#recommended_skills[@]} -eq 0 ]; then
        return 0
    fi

    echo "建议的技能调用命令:"
    echo ""

    for skill in "${recommended_skills[@]}"; do
        echo -e "${CYAN}# 调用 $skill 技能${NC}"
        echo "Skill(\"$skill\")"
        echo ""
    done

    print_info "💡 复制上述命令到终端执行，即可调用相应技能"
}

# 生成强制检查报告
generate_mandatory_check_report() {
    local report_file="$PROJECT_ROOT/mandatory-skill-check-$(date +%Y%m%d-%H%M%S).md"

    cat > "$report_file" << EOF
# 🔒 强制技能调用检查报告

**检查时间**: $(date '+%Y-%m-%d %H:%M:%S')
**检查工具**: mandatory-skill-check.sh v1.0.0

## 📋 检查结果

EOF

    # 读取推荐技能
    if [ -f /tmp/recommended_skills.txt ]; then
        local recommended_skills=($(cat /tmp/recommended_skills.txt))

        if [ ${#recommended_skills[@]} -gt 0 ]; then
            echo "### 🚨 需要调用的技能" >> "$report_file"
            echo "" >> "$report_file"

            for skill in "${recommended_skills[@]}"; do
                echo "- \`Skill('$skill')\`" >> "$report_file"
            done

            echo "" >> "$report_file"
            echo "### 📝 执行命令" >> "$report_file"
            echo "" >> "$report_file"
            echo "复制以下命令到终端执行:" >> "$report_file"
            echo "" >> "$report_file"

            for skill in "${recommended_skills[@]}"; do
                echo "\`\`\`bash" >> "$report_file"
                echo "Skill(\"$skill\")" >> "$report_file"
                echo "\`\`\`" >> "$report_file"
                echo "" >> "$report_file"
            done
        else
            echo "### ✅ 检查通过" >> "$report_file"
            echo "" >> "$report_file"
            echo "项目状态良好，无需调用技能。" >> "$report_file"
        fi
    else
        echo "### ✅ 检查通过" >> "$report_file"
        echo "" >> "$report_file"
        echo "项目状态良好，无需调用技能。" >> "$report_file"
    fi

    cat >> "$report_file" << EOF

## ⚠️ 强制要求

- **必须调用**上述推荐技能后才能继续开发
- **必须验证**技能调用效果
- **必须确保**编译错误为0

---
*本报告由 IOE-DREAM 强制技能调用系统生成*
EOF

    print_success "📄 强制检查报告已生成: $report_file"
}

# ==================== 主执行流程 ====================

main() {
    print_header

    echo -e "${CYAN}开始强制技能调用检查...${NC}"

    # 检查项目状态
    if check_project_status; then
        print_success "🎉 项目状态良好，无需强制技能调用！"
        exit 0
    fi

    # 检查最近技能调用
    if ! check_recent_skill_calls; then
        print_error "❌ 缺少最近的技能调用记录"
    fi

    # 推荐需要调用的技能
    recommend_required_skills

    # 创建技能调用模板
    create_skill_call_template

    # 验证技能调用效果
    validate_skill_effectiveness

    # 生成检查报告
    generate_mandatory_check_report

    print_section "强制检查完成"

    print_error "🚨 检测到项目问题，必须调用推荐技能后才能继续！"
    print_info "💡 请复制上述技能调用命令立即执行"
    print_info "📄 详细报告已生成，请查看"

    exit 1
}

# 执行主函数
main "$@"