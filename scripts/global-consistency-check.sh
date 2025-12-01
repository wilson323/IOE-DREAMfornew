#!/bin/bash
# SmartAdmin 全局一致性检查脚本
# 确保文档、规范、skills和代码实现的一致性
# 版本: v1.0.0
# 更新: 2025-11-14

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
DOCS_DIR="$PROJECT_ROOT/docs"

# 输出函数
print_header() {
    echo -e "${BLUE}🌍 SmartAdmin 全局一致性检查${NC}"
    echo -e "${CYAN}📅 检查时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
    echo -e "${CYAN}📂 项目路径: $PROJECT_ROOT${NC}"
    echo -e "${BLUE}$(printf '═%.0s' {1..60})${NC}"
}

print_section() {
    echo -e "\n${PURPLE}📋 $1${NC}"
    echo -e "${PURPLE}$(printf '═%.0s' {1..50})${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

# 问题计数
ISSUE_COUNT=0

# 1. 检查文档与规范一致性
check_document_consistency() {
    print_section "检查文档与规范一致性"

    # 检查统一规范文档是否存在
    if [ -f "$DOCS_DIR/UNIFIED_DEVELOPMENT_STANDARDS.md" ]; then
        print_success "统一开发规范文档存在"
    else
        print_error "缺少统一开发规范文档: UNIFIED_DEVELOPMENT_STANDARDS.md"
        ISSUE_COUNT=$((ISSUE_COUNT + 1))
        return 1
    fi

    # 检查CLAUDE.md是否引用统一规范
    if grep -q "UNIFIED_DEVELOPMENT_STANDARDS.md" "$PROJECT_ROOT/CLAUDE.md"; then
        print_success "CLAUDE.md正确引用统一规范文档"
    else
        print_warning "CLAUDE.md可能未正确引用统一规范文档"
    fi

    # 检查是否有过时的规范文档
    local outdated_docs=""
    if [ -f "$DOCS_DIR/DEV_STANDARDS.md" ]; then
        outdated_docs="$outdated_docs DEV_STANDARDS.md"
    fi
    if [ -f "$DOCS_DIR/TECHNOLOGY_MIGRATION.md" ]; then
        outdated_docs="$outdated_docs TECHNOLOGY_MIGRATION.md"
    fi
    if [ -d "$DOCS_DIR/repowiki" ]; then
        outdated_docs="$outdated_docs repowiki目录"
    fi

    if [ -n "$outdated_docs" ]; then
        print_warning "发现可能过时的规范文档: $outdated_docs"
        print_info "建议删除或整合到统一规范文档中"
    else
        print_success "没有发现过时的规范文档"
    fi
}

# 2. 检查技能定义一致性
check_skills_consistency() {
    print_section "检查Skills定义一致性"

    # 检查是否存在skills目录
    if [ ! -d "$PROJECT_ROOT/.claude" ]; then
        print_warning "未找到.claude目录"
        return 0
    fi

    # 检查是否有skills文件
    local skills_dir="$PROJECT_ROOT/.claude/skills"
    if [ -d "$skills_dir" ]; then
        local skill_count=$(find "$skills_dir" -name "*.md" | wc -l)
        print_info "找到 $skill_count 个技能定义文件"

        # 检查技能文档是否与项目状态同步
        find "$skills_dir" -name "*.md" | while read -r skill_file; do
            local skill_name=$(basename "$skill_file" .md)
            print_info "  - 技能: $skill_name"

            # 检查技能是否引用了当前项目结构
            if grep -q "smart-admin-api-java17-springboot3" "$skill_file"; then
                print_info "    ${GREEN}✓${NC} 引用正确的项目结构"
            else
                print_warning "    ${YELLOW}!${NC} 可能未正确引用项目结构"
            fi
        done
    else
        print_warning "未找到skills目录"
    fi
}

# 3. 检查代码实现与规范一致性
check_code_vs_standards() {
    print_section "检查代码实现与规范一致性"

    # 检查Spring Boot版本
    if [ -f "$BACKEND_DIR/pom.xml" ]; then
        local spring_boot_version=$(grep -o "spring-boot-starter-parent.*<version>[^<]*" "$BACKEND_DIR/pom.xml" | grep -o "<version>[^<]*" | sed 's/<version>//' 2>/dev/null || echo "unknown")
        print_info "Spring Boot版本: $spring_boot_version"

        if [[ "$spring_boot_version" == 3.* ]]; then
            print_success "使用正确的Spring Boot 3.x版本"
        else
            print_warning "建议使用Spring Boot 3.x版本"
        fi
    fi

    # 检查Java版本
    if grep -q "17" "$BACKEND_DIR/pom.xml"; then
        print_success "使用Java 17版本"
    else
        print_warning "建议使用Java 17版本"
    fi

    # 检查规范执行脚本
    if [ -f "$PROJECT_ROOT/scripts/enforce-standards.sh" ]; then
        print_success "规范执行脚本存在"
    else
        print_error "缺少规范执行脚本"
        ISSUE_COUNT=$((ISSUE_COUNT + 1))
    fi

    if [ -f "$PROJECT_ROOT/scripts/quick-check.sh" ]; then
        print_success "快速检查脚本存在"
    else
        print_warning "缺少快速检查脚本"
    fi
}

# 4. 检查OpenSpec集成
check_openspec_integration() {
    print_section "检查OpenSpec集成"

    # 检查OpenSpec目录
    if [ ! -d "$PROJECT_ROOT/openspec" ]; then
        print_error "缺少OpenSpec目录"
        ISSUE_COUNT=$((ISSUE_COUNT + 1))
        return 1
    fi

    # 检查关键OpenSpec文件
    local required_files=("CHANGELOG.md" "AGENTS.md")
    for file in "${required_files[@]}"; do
        if [ -f "$PROJECT_ROOT/openspec/$file" ]; then
            print_success "OpenSpec文件存在: $file"
        else
            print_warning "OpenSpec文件缺失: $file"
        fi
    done

    # 检查是否有进行中的变更
    if [ -f "$PROJECT_ROOT/openspec/CHANGELOG.md" ]; then
        local in_progress_count=$(grep -c "🔄 进行中" "$PROJECT_ROOT/openspec/CHANGELOG.md" 2>/dev/null || echo "0")
        if [ "$in_progress_count" -gt 0 ]; then
            print_info "有 $in_progress_count 个进行中的OpenSpec变更"
        fi
    fi
}

# 5. 检查项目健康度
check_project_health() {
    print_section "检查项目健康度"

    # 运行快速检查脚本
    if [ -f "$PROJECT_ROOT/scripts/quick-check.sh" ]; then
        print_info "运行快速规范检查..."
        if "$PROJECT_ROOT/scripts/quick-check.sh" > /dev/null 2>&1; then
            print_success "快速规范检查通过"
        else
            print_warning "快速规范检查发现问题"
        fi
    fi

    # 检查Maven依赖
    if [ -f "$BACKEND_DIR/pom.xml" ]; then
        cd "$BACKEND_DIR"
        if mvn dependency:analyze -q > /dev/null 2>&1; then
            print_success "Maven依赖分析正常"
        else
            print_warning "Maven依赖可能存在问题"
        fi
    fi
}

# 6. 生成一致性报告
generate_consistency_report() {
    print_section "生成一致性报告"

    local report_file="$PROJECT_ROOT/CONSISTENCY_REPORT.md"
    cat > "$report_file" << EOF
# SmartAdmin 全局一致性检查报告

> **生成时间**: $(date '+%Y-%m-%d %H:%M:%S')
> **检查工具**: global-consistency-check.sh

## 📊 检查结果摘要

- **文档一致性**: $([ $ISSUE_COUNT -eq 0 ] && echo "✅ 通过" || echo "❌ 存在问题")
- **技能定义**: 已检查
- **代码规范**: 已验证
- **OpenSpec集成**: 已检查
- **项目健康度**: 已评估

## 🔧 发现的问题

$([ "$ISSUE_COUNT" -eq 0 ] && echo "未发现一致性问题。" || echo "发现 $ISSUE_COUNT 个一致性问题，请查看详细检查结果。")

## 📝 改进建议

1. **定期运行**: 建议每次提交前运行全局一致性检查
2. **文档更新**: 及时更新文档以反映项目当前状态
3. **技能维护**: 保持skills目录与项目功能同步
4. **规范执行**: 使用自动化脚本确保规范执行

---

**维护者**: SmartAdmin团队
**更新频率**: 建议每周一次
EOF

    print_success "一致性报告已生成: $report_file"
}

# 7. 输出总结和改进建议
print_summary_and_recommendations() {
    print_section "总结与改进建议"

    echo -e "${CYAN}🎯 全局一致性检查总结:${NC}"

    if [ $ISSUE_COUNT -eq 0 ]; then
        echo -e "${GREEN}🎉 恭喜！全局一致性检查全部通过${NC}"
    else
        echo -e "${YELLOW}⚠️  发现 $ISSUE_COUNT 个一致性问题，建议及时修复${NC}"
    fi

    echo -e "\n${CYAN}📋 改进建议:${NC}"
    echo -e "${CYAN}1. 定期运行${NC} ./scripts/global-consistency-check.sh"
    echo -e "${CYAN}2. 集成到CI/CD${NC} 确保每次提交都进行检查"
    echo -e "${CYAN}3. 文档同步${NC} 及时更新文档以反映项目变化"
    echo -e "${CYAN}4. 技能维护${NC} 保持skills与项目功能一致"
    echo -e "${CYAN}5. 规范强化${NC} 使用自动化工具确保规范执行"
}

# 主函数
main() {
    print_header

    # 执行所有检查
    check_document_consistency
    check_skills_consistency
    check_code_vs_standards
    check_openspec_integration
    check_project_health
    generate_consistency_report
    print_summary_and_recommendations

    echo -e "\n${BLUE}🏁 全局一致性检查完成${NC}"
    echo -e "${BLUE}$(printf '═%.0s' {1..60})${NC}"

    # 根据问题数量决定退出码
    if [ $ISSUE_COUNT -gt 0 ]; then
        exit 1
    else
        exit 0
    fi
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi