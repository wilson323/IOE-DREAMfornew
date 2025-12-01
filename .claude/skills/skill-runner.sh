#!/bin/bash
# Claude Skills 调用和执行脚本
# 确保在后续开发过程中能够充分利用skills

set -e

SKILLS_DIR="D:/IOE-DREAM/.claude/skills"
PROJECT_ROOT="D:/IOE-DREAM"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 技能映射
declare -A SKILL_MAP=(
    ["code-quality"]="code-quality-protector.md"
    ["jakarta"]="spring-boot-jakarta-guardian.md"
    ["architecture"]="four-tier-architecture-guardian.md"
    ["database"]="database-design-specialist.md"
    ["business"]="business-module-developer.md"
    ["quality"]="quality-assurance-expert.md"
    ["frontend"]="frontend-development-specialist.md"
    ["operations"]="intelligent-operations-expert.md"
    ["access-control"]="access-control-business-specialist.md"
    ["openspec"]="openspec-compliance-specialist.md"
)

# 显示帮助信息
show_help() {
    echo -e "${BLUE}🎯 Claude Skills 调用工具${NC}"
    echo ""
    echo "用法: $0 [选项] <技能名称>"
    echo ""
    echo "选项:"
    echo "  -h, --help          显示帮助信息"
    echo "  -l, --list          列出所有可用技能"
    echo "  -v, --validate      验证技能文件"
    echo "  -c, --check         检查项目合规性"
    echo "  -s, --suggest       根据当前情况推荐技能"
    echo ""
    echo "技能名称:"
    echo "  code-quality      代码质量和编码规范"
    echo "  jakarta           Spring Boot Jakarta包名"
    echo "  architecture      四层架构设计"
    echo "  database          数据库设计"
    echo "  business          业务模块开发"
    echo "  quality           质量保证和测试"
    echo "  frontend          前端开发"
    echo "  operations        智能运维"
    echo "  access-control    门禁系统业务"
    echo "  openspec          OpenSpec规范遵循"
    echo ""
    echo "示例:"
    echo "  $0 code-quality     # 调用代码质量检查技能"
    echo "  $0 jakarta          # 调用Spring Boot Jakarta技能"
    echo "  $0 -l               # 列出所有技能"
    echo "  $0 -c               # 检查项目合规性"
}

# 列出所有可用技能
list_skills() {
    echo -e "${CYAN}📋 可用的Claude Skills:${NC}"
    echo ""

    local i=1
    for skill_name in "${!SKILL_MAP[@]}"; do
        local skill_file="${SKILL_MAP[$skill_name]}"
        local skill_path="$SKILLS_DIR/$skill_file"

        if [ -f "$skill_path" ]; then
            # 提取技能描述
            local description=$(grep "^description:" "$skill_path" | cut -d'"' -f2)
            local color=$(grep "^color:" "$skill_path" | cut -d'"' -f2)

            # 根据颜色设置显示颜色
            case $color in
                "red") color_code=$RED ;;
                "green") color_code=$GREEN ;;
                "yellow") color_code=$YELLOW ;;
                "blue") color_code=$BLUE ;;
                "purple") color_code=$PURPLE ;;
                "cyan") color_code=$CYAN ;;
                *) color_code=$NC ;;
            esac

            printf "${color_code}%2d. %-18s${NC} %s\n" $i "$skill_name" "$description"
            ((i++))
        fi
    done

    echo ""
    echo -e "${YELLOW}💡 使用方法: $0 <技能名称>${NC}"
}

# 调用指定技能
call_skill() {
    local skill_name="$1"

    if [ -z "$skill_name" ]; then
        echo -e "${RED}❌ 错误: 请指定技能名称${NC}"
        echo "使用 '$0 --help' 查看可用技能"
        exit 1
    fi

    # 查找技能文件
    local skill_file="${SKILL_MAP[$skill_name]}"
    if [ -z "$skill_file" ]; then
        echo -e "${RED}❌ 错误: 未找到技能 '$skill_name'${NC}"
        echo "使用 '$0 --list' 查看可用技能"
        exit 1
    fi

    local skill_path="$SKILLS_DIR/$skill_file"

    if [ ! -f "$skill_path" ]; then
        echo -e "${RED}❌ 错误: 技能文件不存在: $skill_path${NC}"
        exit 1
    fi

    echo -e "${BLUE}🚀 调用技能: $skill_name${NC}"
    echo -e "${CYAN}📁 技能文件: $skill_file${NC}"
    echo ""

    # 显示技能内容
    cat "$skill_path"

    echo ""
    echo -e "${GREEN}✅ 技能调用完成: $skill_name${NC}"
}

# 验证所有技能文件
validate_skills() {
    echo -e "${BLUE}🔍 验证Claude Skills文件...${NC}"
    echo ""

    local total_skills=0
    local valid_skills=0

    for skill_file in "${SKILL_MAP[@]}"; do
        local skill_path="$SKILLS_DIR/$skill_file"
        ((total_skills++))

        if [ -f "$skill_path" ]; then
            # 检查YAML frontmatter
            if grep -q "^---" "$skill_path" && grep -q "^name:" "$skill_path" && grep -q "^description:" "$skill_path"; then
                echo -e "${GREEN}✅ $skill_file${NC}"
                ((valid_skills++))
            else
                echo -e "${RED}❌ $skill_file (格式错误)${NC}"
            fi
        else
            echo -e "${RED}❌ $skill_file (文件不存在)${NC}"
        fi
    done

    echo ""
    echo -e "${CYAN}验证结果:${NC}"
    echo "总技能数: $total_skills"
    echo "有效技能: $valid_skills"

    if [ $valid_skills -eq $total_skills ]; then
        echo -e "${GREEN}🎉 所有技能文件验证通过！${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  部分技能文件需要修复${NC}"
        return 1
    fi
}

# 检查项目合规性
check_project_compliance() {
    echo -e "${BLUE}🔍 检查项目合规性...${NC}"
    echo ""

    cd "$PROJECT_ROOT"

    # 1. 检查Java文件编码
    echo -e "${CYAN}1. 检查Java文件编码...${NC}"
    local non_utf8_files=$(find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
    if [ $non_utf8_files -eq 0 ]; then
        echo -e "${GREEN}✅ 所有Java文件使用UTF-8编码${NC}"
    else
        echo -e "${RED}❌ 发现 $non_utf8_files 个非UTF-8编码的Java文件${NC}"
    fi

    # 2. 检查Jakarta包名
    echo -e "${CYAN}2. 检查Jakarta包名合规性...${NC}"
    local jakarta_files=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
    if [ $jakarta_files -eq 0 ]; then
        echo -e "${GREEN}✅ 所有Java文件使用Jakarta包名${NC}"
    else
        echo -e "${RED}❌ 发现 $jakarta_files 个文件仍在使用javax包名${NC}"
        echo "建议调用: $0 jakarta"
    fi

    # 3. 检查依赖注入
    echo -e "${CYAN}3. 检查依赖注入规范...${NC}"
    local autowired_files=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
    if [ $autowired_files -eq 0 ]; then
        echo -e "${GREEN}✅ 所有文件使用@Resource依赖注入${NC}"
    else
        echo -e "${RED}❌ 发现 $autowired_files 个文件使用@Autowired${NC}"
    fi

    # 4. 检查架构违规
    echo -e "${CYAN}4. 检查架构违规...${NC}"
    local architecture_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)
    if [ $architecture_violations -eq 0 ]; then
        echo -e "${GREEN}✅ 未发现架构违规${NC}"
    else
        echo -e "${RED}❌ 发现 $architecture_violations 处架构违规${NC}"
        echo "建议调用: $0 architecture"
    fi

    echo ""
    echo -e "${YELLOW}💡 建议:${NC}"
    echo "- 如果发现合规性问题，可以调用相应的技能进行修复"
    echo "- 使用 '$0 -s' 获取基于当前情况的技能推荐"
}

# 推荐技能
suggest_skills() {
    echo -e "${BLUE}🤖 基于当前情况推荐技能...${NC}"
    echo ""

    cd "$PROJECT_ROOT"

    local recommendations=()

    # 检查常见问题并推荐相应技能
    local jakarta_files=$(find . -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)
    if [ $jakarta_files -gt 0 ]; then
        recommendations+=("jakarta: 发现Jakarta包名问题需要修复")
    fi

    local autowired_files=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    if [ $autowired_files -gt 0 ]; then
        recommendations+=("code-quality: 发现@Autowired使用需要修复")
    fi

    local architecture_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" . 2>/dev/null | wc -l)
    if [ $architecture_violations -gt 0 ]; then
        recommendations+=("architecture: 发现架构违规需要修复")
    fi

    local test_coverage=$(find . -name "*Test.java" 2>/dev/null | wc -l)
    if [ $test_coverage -lt 10 ]; then
        recommendations+=("quality: 测试覆盖率可能不足")
    fi

    if [ ${#recommendations[@]} -eq 0 ]; then
        echo -e "${GREEN}✅ 当前项目状态良好，无需特殊技能处理${NC}"
    else
        echo -e "${YELLOW}📋 推荐技能:${NC}"
        echo ""
        for recommendation in "${recommendations[@]}"; do
            local skill=$(echo "$recommendation" | cut -d':' -f1)
            local reason=$(echo "$recommendation" | cut -d':' -f2)
            echo -e "  ${CYAN}• $skill${NC}: $reason"
        done

        echo ""
        echo -e "${YELLOW}💡 使用方法:${NC}"
        echo "  $0 jakarta          # 修复Jakarta包名问题"
        echo "  $0 code-quality     # 修复代码质量问题"
        echo "  $0 architecture      # 修复架构违规问题"
        echo "  $0 quality          # 提升测试覆盖率"
    fi
}

# 主函数
main() {
    case "${1:-}" in
        -h|--help)
            show_help
            ;;
        -l|--list)
            list_skills
            ;;
        -v|--validate)
            validate_skills
            ;;
        -c|--check)
            check_project_compliance
            ;;
        -s|--suggest)
            suggest_skills
            ;;
        "")
            echo -e "${RED}❌ 错误: 请指定操作或技能名称${NC}"
            echo "使用 '$0 --help' 查看帮助信息"
            exit 1
            ;;
        *)
            call_skill "$1"
            ;;
    esac
}

# 执行主函数
main "$@"