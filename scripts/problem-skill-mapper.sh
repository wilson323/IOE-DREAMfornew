#!/bin/bash
# 🚨 问题-技能自动映射系统 - 老王出品
# 版本: v1.0.0 - 铁腕执行版
# 作用: 自动诊断项目问题并推荐对应技能，强制执行！

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
    echo -e "${BLUE}🔍 IOE-DREAM 问题诊断与技能推荐系统${NC}"
    echo -e "${CYAN}📅 执行时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
    echo -e "${CYAN}📂 项目路径: $PROJECT_ROOT${NC}"
    echo -e "${BLUE}$(printf '─%.0s' {1..70})${NC}"
}

print_section() {
    echo -e "\n${PURPLE}📋 $1${NC}"
    echo -e "${PURPLE}$(printf '─%.0s' {1..60})${NC}"
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

# 问题计数器
TOTAL_ISSUES=0
RECOMMENDED_SKILLS=()

# ==================== 核心诊断函数 ====================

# 1. 检测Jakarta迁移问题
check_jakarta_issues() {
    print_section "检测 Jakarta EE 迁移问题"

    # 检测违规的javax包使用（排除JDK标准包）
    local javax_files=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null || true)
    local violation_count=0
    local violation_files=""

    if [ -n "$javax_files" ]; then
        while IFS= read -r file; do
            if [ -f "$file" ]; then
                # 检查违规的EE包（JDK标准包除外）
                local has_violation=false

                # 检查违规的EE包名
                if grep -q "javax\.annotation\." "$file" && ! grep -q "javax\.annotation\.PostConstruct" "$file"; then
                    has_violation=true
                    violation_files="$violation_files\n  - $file (javax.annotation)"
                fi

                if grep -q "javax\.validation\." "$file"; then
                    has_violation=true
                    violation_files="$violation_files\n  - $file (javax.validation)"
                fi

                if grep -q "javax\.persistence\." "$file"; then
                    has_violation=true
                    violation_files="$violation_files\n  - $file (javax.persistence)"
                fi

                if grep -q "javax\.servlet\." "$file"; then
                    has_violation=true
                    violation_files="$violation_files\n  - $file (javax.servlet)"
                fi

                if [ "$has_violation" = true ]; then
                    ((violation_count++))
                fi
            fi
        done <<< "$javax_files"
    fi

    if [ $violation_count -gt 0 ]; then
        print_error "检测到 Jakarta 迁移问题: $violation_count 个文件违规使用 javax EE 包"
        echo -e "${RED}违规文件列表:$violation_files${NC}"
        print_info "🔧 立即调用技能: Skill('spring-boot-jakarta-guardian')"
        RECOMMENDED_SKILLS+=("spring-boot-jakarta-guardian")
        TOTAL_ISSUES=$((TOTAL_ISSUES + violation_count))
    else
        print_success "Jakarta 包名合规性检查通过"
    fi
}

# 2. 检测依赖注入问题
check_dependency_injection_issues() {
    print_section "检测依赖注入规范问题"

    local autowired_files=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null || true)
    local autowired_count=0

    if [ -n "$autowired_files" ]; then
        autowired_count=$(echo "$autowired_files" | wc -l)
        print_error "检测到依赖注入问题: $autowired_count 个文件使用 @Autowired"

        echo -e "${RED}违规文件列表:${NC}"
        echo "$autowired_files" | while read -r file; do
            if [ -n "$file" ]; then
                echo -e "  ${RED}  - $file${NC}"
            fi
        done

        print_info "🔧 立即调用技能: Skill('spring-boot-jakarta-guardian')"
        RECOMMENDED_SKILLS+=("spring-boot-jakarta-guardian")
        TOTAL_ISSUES=$((TOTAL_ISSUES + autowired_count))
    else
        print_success "依赖注入规范检查通过"
    fi
}

# 3. 检测编译错误问题
check_compilation_errors() {
    print_section "检测编译错误问题"

    echo -e "${CYAN}正在执行编译检查...${NC}"

    cd "$BACKEND_DIR"
    local compile_output=$(mvn clean compile -q 2>&1 || true)
    local error_count=$(echo "$compile_output" | grep -c "ERROR" || echo "0")

    if [ $error_count -gt 0 ]; then
        print_error "检测到编译错误: $error_count 个"

        # 分析主要错误类型
        local symbol_errors=$(echo "$compile_output" | grep -c "找不到符号\|cannot find symbol" || echo "0")
        local duplicate_errors=$(echo "$compile_output" | grep -c "重复定义\|duplicate" || echo "0")
        local type_errors=$(echo "$compile_output" | grep -c "类型不匹配\|type mismatch" || echo "0")

        echo -e "${RED}错误类型分析:${NC}"
        echo -e "  ${RED}  - 符号找不到: $symbol_errors 个${NC}"
        echo -e "  ${RED}  - 重复定义: $duplicate_errors 个${NC}"
        echo -e "  ${RED}  - 类型不匹配: $type_errors 个${NC}"

        print_info "🔧 立即调用技能: Skill('code-quality-protector')"
        RECOMMENDED_SKILLS+=("code-quality-protector")
        TOTAL_ISSUES=$((TOTAL_ISSUES + error_count))

        # 保存编译错误详情供后续分析
        echo "$compile_output" > "$PROJECT_ROOT/compile-errors-detailed.log"
        print_info "📄 编译错误详情已保存到: compile-errors-detailed.log"
    else
        print_success "编译检查通过，无错误"
    fi

    cd "$PROJECT_ROOT"
}

# 4. 检测架构违规问题
check_architecture_violations() {
    print_section "检测四层架构违规问题"

    local controller_dao_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" "$BACKEND_DIR" 2>/dev/null || true)
    local violation_count=0

    if [ -n "$controller_dao_violations" ]; then
        violation_count=$(echo "$controller_dao_violations" | wc -l)
        print_error "检测到架构违规: Controller直接访问DAO $violation_count 处"

        echo -e "${RED}违规详情:${NC}"
        echo "$controller_dao_violations" | while read -r line; do
            if [ -n "$line" ]; then
                echo -e "  ${RED}  - $line${NC}"
            fi
        done

        print_info "🔧 立即调用技能: Skill('four-tier-architecture-guardian')"
        RECOMMENDED_SKILLS+=("four-tier-architecture-guardian")
        TOTAL_ISSUES=$((TOTAL_ISSUES + violation_count))
    else
        print_success "四层架构规范检查通过"
    fi
}

# 5. 检测日志规范问题
check_logging_issues() {
    print_section "检测日志规范问题"

    local system_out_files=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "System\.out\.println\|System\.err\.println" {} \; 2>/dev/null || true)
    local violation_count=0

    if [ -n "$system_out_files" ]; then
        violation_count=$(echo "$system_out_files" | wc -l)
        print_warning "检测到日志规范问题: $violation_count 个文件使用 System.out.println"

        echo -e "${YELLOW}违规文件列表:${NC}"
        echo "$system_out_files" | while read -r file; do
            if [ -n "$file" ]; then
                echo -e "  ${YELLOW}  - $file${NC}"
            fi
        done

        print_info "🔧 建议调用技能: Skill('code-quality-protector')"
        # 日志问题相对较轻，不加入强制技能列表
    else
        print_success "日志规范检查通过"
    fi
}

# 6. 生成技能执行计划
generate_skill_execution_plan() {
    print_section "技能执行计划"

    if [ ${#RECOMMENDED_SKILLS[@]} -eq 0 ]; then
        print_success "🎉 恭喜！项目未发现严重问题，无需调用修复技能"
        return 0
    fi

    echo -e "${PURPLE}📋 推荐技能调用序列（按优先级排序）:${NC}"

    # 去重并排序技能推荐
    local unique_skills=($(printf "%s\n" "${RECOMMENDED_SKILLS[@]}" | sort -u))

    local priority=1
    for skill in "${unique_skills[@]}"; do
        case $skill in
            "spring-boot-jakarta-guardian")
                echo -e "${RED}🎯 优先级$priority: Skill('spring-boot-jakarta-guardian')${NC}"
                echo -e "   ${RED}   解决: Jakarta迁移和依赖注入问题${NC}"
                echo -e "   ${RED}   预期修复: 15分钟内完成所有规范问题${NC}"
                ;;
            "code-quality-protector")
                echo -e "${RED}🎯 优先级$priority: Skill('code-quality-protector')${NC}"
                echo -e "   ${RED}   解决: 系统性编译错误问题${NC}"
                echo -e "   ${RED}   预期修复: 30分钟内减少80%编译错误${NC}"
                ;;
            "four-tier-architecture-guardian")
                echo -e "${YELLOW}🎯 优先级$priority: Skill('four-tier-architecture-guardian')${NC}"
                echo -e "   ${YELLOW}   解决: 架构设计违规问题${NC}"
                echo -e "   ${YELLOW}   预期修复: 10分钟内完成架构重构${NC}"
                ;;
        esac
        ((priority++))
    done

    echo ""
    print_error "🚨 总计发现 $TOTAL_ISSUES 个问题需要解决"
    print_info "💡 请按上述优先级顺序调用技能进行修复"
}

# 7. 生成诊断报告
generate_diagnosis_report() {
    local report_file="$PROJECT_ROOT/skill-diagnosis-report-$(date +%Y%m%d-%H%M%S).md"

    cat > "$report_file" << EOF
# 📊 IOE-DREAM 项目技能诊断报告

**生成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**诊断工具**: problem-skill-mapper.sh v1.0.0
**项目路径**: $PROJECT_ROOT

## 📋 问题统计总览

| 问题类型 | 发现数量 | 严重程度 | 推荐技能 |
|---------|---------|---------|---------|
EOF

    # 添加统计表格
    if [ $TOTAL_ISSUES -gt 0 ]; then
        echo "| 总问题数 | $TOTAL_ISSUES | 🔴 严重 | 见下方推荐 |" >> "$report_file"
    else
        echo "| 总问题数 | 0 | ✅ 良好 | 无需修复 |" >> "$report_file"
    fi

    cat >> "$report_file" << EOF

## 🎯 推荐技能执行计划

EOF

    if [ ${#RECOMMENDED_SKILLS[@]} -gt 0 ]; then
        local unique_skills=($(printf "%s\n" "${RECOMMENDED_SKILLS[@]}" | sort -u))
        local priority=1
        for skill in "${unique_skills[@]}"; do
            echo "### 优先级 $priority: \`Skill('$skill')\`" >> "$report_file"
            case $skill in
                "spring-boot-jakarta-guardian")
                    echo "- **解决问题**: Jakarta EE迁移、依赖注入规范" >> "$report_file"
                    echo "- **预期效果**: 15分钟内完成所有Jakarta相关问题" >> "$report_file"
                    ;;
                "code-quality-protector")
                    echo "- **解决问题**: 系统性编译错误" >> "$report_file"
                    echo "- **预期效果**: 30分钟内减少80%编译错误" >> "$report_file"
                    ;;
                "four-tier-architecture-guardian")
                    echo "- **解决问题**: 四层架构违规" >> "$report_file"
                    echo "- **预期效果**: 10分钟内完成架构重构" >> "$report_file"
                    ;;
            esac
            echo "" >> "$report_file"
            ((priority++))
        done
    else
        echo "🎉 **恭喜！** 项目状态良好，无需调用修复技能。" >> "$report_file"
    fi

    cat >> "$report_file" << EOF

## 📝 执行说明

1. **立即执行**: 按照上述优先级顺序调用推荐技能
2. **效果验证**: 每个技能调用后重新运行本脚本验证效果
3. **持续监控**: 建议每日运行此脚本进行项目健康检查

---
*本报告由 IOE-DREAM 问题诊断系统自动生成*
EOF

    print_success "📄 详细诊断报告已生成: $report_file"
}

# ==================== 主执行流程 ====================

main() {
    print_header

    echo -e "${CYAN}开始项目健康诊断...${NC}"
    echo ""

    # 执行各项检查
    check_jakarta_issues
    check_dependency_injection_issues
    check_compilation_errors
    check_architecture_violations
    check_logging_issues

    # 生成执行计划
    generate_skill_execution_plan

    # 生成诊断报告
    generate_diagnosis_report

    print_section "诊断完成"

    if [ $TOTAL_ISSUES -gt 0 ]; then
        print_error "🚨 发现 $TOTAL_ISSUES 个问题，需要立即处理！"
        print_info "💡 请按照上述技能调用计划立即开始修复"
        echo -e "${RED}🔥 立即执行命令: ./scripts/execute-skills.sh${NC}"
        exit 1
    else
        print_success "🎉 项目状态健康，未发现严重问题！"
        exit 0
    fi
}

# 执行主函数
main "$@"