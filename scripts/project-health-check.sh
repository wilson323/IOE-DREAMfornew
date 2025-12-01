#!/bin/bash
# SmartAdmin 项目健康检查脚本
# 老王的深度体检工具 - 全面检查项目健康状况
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
FRONTEND_DIR="$PROJECT_ROOT/smart-admin-web-javascript"

# 输出函数
print_header() {
    echo -e "${BLUE}🏥 SmartAdmin 项目健康检查${NC}"
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

# 健康评分
HEALTH_SCORE=100
ISSUES_FOUND=()

# 1. 项目结构检查
check_project_structure() {
    print_section "项目结构检查"

    local structure_ok=true

    # 检查关键目录
    if [ ! -d "$BACKEND_DIR" ]; then
        print_error "后端目录不存在: $BACKEND_DIR"
        structure_ok=false
    else
        print_success "后端目录存在"
    fi

    if [ ! -f "$BACKEND_DIR/pom.xml" ]; then
        print_error "根pom.xml不存在"
        structure_ok=false
    else
        print_success "根pom.xml存在"
    fi

    if [ ! -d "$BACKEND_DIR/sa-base" ]; then
        print_error "sa-base模块不存在"
        structure_ok=false
    else
        print_success "sa-base模块存在"
    fi

    if [ ! -d "$BACKEND_DIR/sa-admin" ]; then
        print_error "sa-admin模块不存在"
        structure_ok=false
    else
        print_success "sa-admin模块存在"
    fi

    if [ ! -d "$PROJECT_ROOT/docs" ]; then
        print_warning "docs目录不存在"
        HEALTH_SCORE=$((HEALTH_SCORE - 5))
    else
        print_success "docs目录存在"
    fi

    if [ "$structure_ok" = false ]; then
        HEALTH_SCORE=$((HEALTH_SCORE - 20))
        ISSUES_FOUND+=("项目结构不完整")
    fi
}

# 2. Maven配置检查
check_maven_config() {
    print_section "Maven配置检查"

    local root_pom="$BACKEND_DIR/pom.xml"

    if [ ! -f "$root_pom" ]; then
        print_error "根pom.xml不存在"
        HEALTH_SCORE=$((HEALTH_SCORE - 15))
        ISSUES_FOUND+=("根pom.xml缺失")
        return 1
    fi

    # 检查Spring Boot版本
    if grep -q "3\." "$root_pom"; then
        print_success "使用Spring Boot 3.x版本"
    else
        print_warning "建议使用Spring Boot 3.x版本"
        HEALTH_SCORE=$((HEALTH_SCORE - 10))
        ISSUES_FOUND+=("Spring Boot版本建议升级")
    fi

    # 检查Java版本
    if grep -q "17" "$root_pom"; then
        print_success "使用Java 17版本"
    else
        print_warning "建议使用Java 17版本"
        HEALTH_SCORE=$((HEALTH_SCORE - 5))
        ISSUES_FOUND+=("Java版本建议升级")
    fi
}

# 3. 编译健康检查
check_compilation_health() {
    print_section "编译健康检查"

    cd "$BACKEND_DIR"

    # 检查依赖解析
    print_info "检查Maven依赖解析..."
    if mvn dependency:resolve -q > /dev/null 2>&1; then
        print_success "Maven依赖解析正常"
    else
        print_error "Maven依赖解析失败"
        HEALTH_SCORE=$((HEALTH_SCORE - 25))
        ISSUES_FOUND+=("Maven依赖解析失败")
        return 1
    fi

    # 检查编译
    print_info "检查项目编译..."
    if mvn clean compile -q -DskipTests 2>/dev/null; then
        print_success "项目编译成功"
    else
        print_error "项目编译失败"
        HEALTH_SCORE=$((HEALTH_SCORE - 30))
        ISSUES_FOUND+=("项目编译失败")
        return 1
    fi

    # 检查测试编译
    print_info "检查测试编译..."
    if mvn test-compile -q > /dev/null 2>&1; then
        print_success "测试代码编译成功"
    else
        print_warning "测试代码编译失败"
        HEALTH_SCORE=$((HEALTH_SCORE - 10))
        ISSUES_FOUND+=("测试编译失败")
    fi
}

# 4. 代码质量检查
check_code_quality() {
    print_section "代码质量检查"

    local quality_issues=0

    # 检查javax包使用
    local javax_count=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)
    if [ "$javax_count" -gt 0 ]; then
        print_error "发现 $javax_count 个文件使用javax包（应使用jakarta）"
        quality_issues=$((quality_issues + 1))
        HEALTH_SCORE=$((HEALTH_SCORE - 15))
        ISSUES_FOUND+=("javax包使用不规范")
    else
        print_success "所有文件都使用jakarta包"
    fi

    # 检查@Autowired使用
    local autowired_count=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    if [ "$autowired_count" -gt 0 ]; then
        print_error "发现 $autowired_count 个文件使用@Autowired（应使用@Resource）"
        quality_issues=$((quality_issues + 1))
        HEALTH_SCORE=$((HEALTH_SCORE - 15))
        ISSUES_FOUND+=("@Autowired使用不规范")
    else
        print_success "所有文件都使用@Resource依赖注入"
    fi

    # 检查System.out使用
    local sout_count=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "System\.out\.println\|System\.err\.println" {} \; 2>/dev/null | wc -l)
    if [ "$sout_count" -gt 0 ]; then
        print_error "发现 $sout_count 个文件使用System.out.println（应使用日志框架）"
        quality_issues=$((quality_issues + 1))
        HEALTH_SCORE=$((HEALTH_SCORE - 10))
        ISSUES_FOUND+=("System.out使用不规范")
    else
        print_success "所有文件都使用日志框架"
    fi

    if [ $quality_issues -eq 0 ]; then
        print_success "代码质量检查通过"
    fi
}

# 5. 代码统计信息
code_statistics() {
    print_section "代码统计信息"

    # Java文件统计
    local java_files=$(find "$BACKEND_DIR" -name "*.java" 2>/dev/null | wc -l)
    local java_lines=$(find "$BACKEND_DIR" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}' || echo "0")

    print_info "Java文件数量: $java_files"
    print_info "Java代码总行数: $java_lines"

    # 实体类统计
    local entity_files=$(find "$BACKEND_DIR" -name "*Entity.java" 2>/dev/null | wc -l)
    print_info "实体类数量: $entity_files"

    # Controller统计
    local controller_files=$(find "$BACKEND_DIR" -name "*Controller.java" 2>/dev/null | wc -l)
    print_info "Controller数量: $controller_files"

    # Service统计
    local service_files=$(find "$BACKEND_DIR" -name "*Service.java" 2>/dev/null | wc -l)
    print_info "Service数量: $service_files"
}

# 6. 规范文档检查
check_documentation() {
    print_section "规范文档检查"

    local unified_standards="$PROJECT_ROOT/docs/UNIFIED_DEVELOPMENT_STANDARDS.md"

    if [ -f "$unified_standards" ]; then
        print_success "统一开发规范文档存在"
    else
        print_warning "统一开发规范文档不存在"
        HEALTH_SCORE=$((HEALTH_SCORE - 5))
        ISSUES_FOUND+=("缺少统一开发规范文档")
    fi

    # 检查CLAUDE.md
    if [ -f "$PROJECT_ROOT/CLAUDE.md" ]; then
        print_success "CLAUDE.md存在"
    else
        print_warning "CLAUDE.md不存在"
        HEALTH_SCORE=$((HEALTH_SCORE - 5))
        ISSUES_FOUND+=("缺少CLAUDE.md")
    fi

    # 检查脚本目录
    if [ -d "$PROJECT_ROOT/scripts" ]; then
        print_success "scripts目录存在"

        # 检查关键脚本
        if [ -f "$PROJECT_ROOT/scripts/enforce-standards.sh" ]; then
            print_success "规范执行脚本存在"
        else
            print_warning "规范执行脚本不存在"
        fi

        if [ -f "$PROJECT_ROOT/scripts/quick-check.sh" ]; then
            print_success "快速检查脚本存在"
        else
            print_warning "快速检查脚本不存在"
        fi
    else
        print_warning "scripts目录不存在"
        HEALTH_SCORE=$((HEALTH_SCORE - 5))
        ISSUES_FOUND+=("缺少scripts目录")
    fi
}

# 7. 环境配置检查
check_environment() {
    print_section "环境配置检查"

    # 检查Java版本
    if command -v java >/dev/null 2>&1; then
        local java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
        print_info "Java版本: $java_version"

        if [[ "$java_version" == *"17"* ]]; then
            print_success "Java版本符合要求"
        else
            print_warning "建议使用Java 17"
            HEALTH_SCORE=$((HEALTH_SCORE - 5))
        fi
    else
        print_error "Java未安装"
        HEALTH_SCORE=$((HEALTH_SCORE - 20))
        ISSUES_FOUND+=("Java环境缺失")
    fi

    # 检查Maven
    if command -v mvn >/dev/null 2>&1; then
        local maven_version=$(mvn -version | head -1 | cut -d' ' -f3)
        print_info "Maven版本: $maven_version"
        print_success "Maven环境正常"
    else
        print_error "Maven未安装"
        HEALTH_SCORE=$((HEALTH_SCORE - 20))
        ISSUES_FOUND+=("Maven环境缺失")
    fi
}

# 8. 健康评级
health_rating() {
    print_section "健康评级"

    echo -e "${CYAN}🏆 项目健康评分: ${BLUE}$HEALTH_SCORE/100${NC}"

    if [ $HEALTH_SCORE -ge 90 ]; then
        echo -e "${GREEN}🎉 项目健康状况: 优秀${NC}"
        print_success "项目质量极高，可以安全部署"
    elif [ $HEALTH_SCORE -ge 80 ]; then
        echo -e "${GREEN}💚 项目健康状况: 良好${NC}"
        print_success "项目质量较好，建议修复少量问题后部署"
    elif [ $HEALTH_SCORE -ge 70 ]; then
        echo -e "${YELLOW}💛 项目健康状况: 一般${NC}"
        print_warning "项目存在一些问题，建议修复后再考虑部署"
    elif [ $HEALTH_SCORE -ge 60 ]; then
        echo -e "${YELLOW}🧡 项目健康状况: 较差${NC}"
        print_warning "项目存在较多问题，必须修复关键问题"
    else
        echo -e "${RED}❤️ 项目健康状况: 危险${NC}"
        print_error "项目存在严重问题，不建议部署"
    fi

    # 显示问题列表
    if [ ${#ISSUES_FOUND[@]} -gt 0 ]; then
        echo -e "\n${PURPLE}📋 发现的问题:${NC}"
        for issue in "${ISSUES_FOUND[@]}"; do
            echo -e "  ${RED}  - $issue${NC}"
        done
    fi
}

# 9. 修复建议
repair_suggestions() {
    if [ $HEALTH_SCORE -lt 90 ]; then
        print_section "修复建议"

        if [[ " ${ISSUES_FOUND[@]} " =~ " javax包使用不规范 " ]]; then
            echo -e "${CYAN}📝 javax包修复:${NC}"
            echo -e "   find $BACKEND_DIR -name '*.java' -exec sed -i 's/javax\\./jakarta\\./g' {} \\;"
            echo
        fi

        if [[ " ${ISSUES_FOUND[@]} " =~ " @Autowired使用不规范 " ]]; then
            echo -e "${CYAN}📝 依赖注入修复:${NC}"
            echo -e "   find $BACKEND_DIR -name '*.java' -exec sed -i 's/@Autowired/@Resource/g' {} \\;"
            echo
        fi

        if [[ " ${ISSUES_FOUND[@]} " =~ " 项目编译失败 " ]]; then
            echo -e "${CYAN}📝 编译问题排查:${NC}"
            echo -e "   1. 检查Maven依赖: mvn dependency:tree"
            echo -e "   2. 清理重新编译: mvn clean compile"
            echo -e "   3. 查看详细错误: mvn compile -X"
            echo
        fi

        echo -e "${CYAN}🔧 运行规范检查:${NC}"
        echo -e "   ./scripts/enforce-standards.sh"
        echo
        echo -e "${CYAN}🔧 运行快速检查:${NC}"
        echo -e "   ./scripts/quick-check.sh"
    fi
}

# 主函数
main() {
    print_header

    # 执行所有检查
    check_project_structure
    check_maven_config
    check_compilation_health
    check_code_quality
    code_statistics
    check_documentation
    check_environment
    health_rating
    repair_suggestions

    echo -e "\n${BLUE}🏁 项目健康检查完成${NC}"
    echo -e "${BLUE}$(printf '═%.0s' {1..60})${NC}"
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi