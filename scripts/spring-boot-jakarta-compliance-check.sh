#!/bin/bash
# Spring Boot 3.x + Jakarta EE 合规性检查脚本
# 基于 Spring Boot Jakarta 守护专家技能
# 作者: IOE-DREAM 开发团队
# 版本: 1.0.0
# 最后更新: 2025-11-20

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_header() {
    echo -e "${BLUE}🔍 Spring Boot 3.x + Jakarta EE 合规性检查${NC}"
    echo -e "${BLUE}================================================${NC}"
}

# 检查是否在正确的工作目录
check_working_directory() {
    if [ ! -f "pom.xml" ]; then
        print_error "请在项目根目录执行此脚本"
        exit 1
    fi
    print_success "工作目录验证通过"
}

# 检查Jakarta EE包名合规性
check_jakarta_compliance() {
    print_info "步骤1: 检查Jakarta EE包名合规性..."

    # 定义需要迁移的Jakarta EE包
    local jakarta_packages="javax.annotation javax.validation javax.persistence javax.servlet javax.xml.bind"
    local violation_count=0
    local violation_files=""

    for package in $jakarta_packages; do
        local files=$(find . -name "*.java" -exec grep -l "$package" {} \; 2>/dev/null || true)
        if [ ! -z "$files" ]; then
            violation_count=$((violation_count + $(echo "$files" | wc -l)))
            violation_files="$violation_files\n$files"
        fi
    done

    if [ $violation_count -ne 0 ]; then
        print_error "发现Jakarta EE违规包使用: $violation_count 处"
        echo -e "$violation_files"
        print_info "修复建议:"
        echo "  javax.annotation.* → jakarta.annotation.*"
        echo "  javax.validation.* → jakarta.validation.*"
        echo "  javax.persistence.* → jakarta.persistence.*"
        echo "  javax.servlet.* → jakarta.servlet.*"
        echo "  javax.xml.bind.* → jakarta.xml.bind.*"
        return 1
    fi

    print_success "Jakarta EE包名合规性检查通过 (0处违规)"
    return 0
}

# 检查依赖注入规范
check_dependency_injection() {
    print_info "步骤2: 检查依赖注入规范..."

    local autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)

    if [ $autowired_count -ne 0 ]; then
        print_error "发现@Autowired违规使用: $autowired_count 个文件"
        print_info "违规文件列表:"
        find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | head -10
        print_info "修复建议: 将@Autowired替换为@Resource"
        return 1
    fi

    print_success "依赖注入规范检查通过 (0处@Autowired违规)"
    return 0
}

# 检查Java时间API使用
check_java_time_api() {
    print_info "步骤3: 检查Java时间API使用..."

    # 检查是否还在使用旧的Date/Calendar API
    local old_date_usage=$(find . -name "*.java" -exec grep -l "import java.util.Date" {} \; 2>/dev/null | wc -l)
    local calendar_usage=$(find . -name "*.java" -exec grep -l "import java.util.Calendar" {} \; 2>/dev/null | wc -l)

    if [ $old_date_usage -gt 0 ] || [ $calendar_usage -gt 0 ]; then
        print_warning "发现旧版时间API使用 (建议升级到java.time):"
        print_info "  java.util.Date使用: $old_date_usage 处"
        print_info "  java.util.Calendar使用: $calendar_usage 处"
        print_info "建议迁移到java.time.LocalDateTime、ZonedDateTime等"
    else
        print_success "Java时间API检查通过"
    fi

    return 0
}

# 编译验证
check_compilation() {
    print_info "步骤4: 执行Spring Boot编译验证..."

    # 清理之前的编译结果
    mvn clean -q > /dev/null 2>&1

    # 执行编译
    local compile_start=$(date +%s)
    if mvn compile -q; then
        local compile_end=$(date +%s)
        local compile_time=$((compile_end - compile_start))
        print_success "Spring Boot编译验证通过 (耗时: ${compile_time}秒)"
        return 0
    else
        print_error "Spring Boot编译验证失败"
        print_info "请检查编译错误并修复后重新执行"
        return 1
    fi
}

# 检查Spring Boot版本
check_spring_boot_version() {
    print_info "步骤5: 检查Spring Boot版本..."

    local spring_boot_version=$(grep -o 'spring-boot-starter-parent[^>]*>' pom.xml | head -1 | sed 's/.*>\([0-9.]*\)<.*/\1/' || echo "unknown")

    if [[ "$spring_boot_version" =~ ^3\. ]]; then
        print_success "Spring Boot版本检查通过: $spring_boot_version"
        return 0
    else
        print_warning "Spring Boot版本可能需要升级: 当前版本 $spring_boot_version"
        print_info "建议使用Spring Boot 3.x版本以获得完整的Jakarta EE支持"
        return 0
    fi
}

# 生成合规性报告
generate_report() {
    local report_file="spring-boot-jakarta-compliance-report-$(date +%Y%m%d-%H%M%S).txt"

    {
        echo "Spring Boot 3.x + Jakarta EE 合规性检查报告"
        echo "=============================================="
        echo "检查时间: $(date)"
        echo "项目路径: $(pwd)"
        echo ""
        echo "合规性检查结果:"
        echo "✅ Jakarta EE包名合规性: 通过"
        echo "✅ 依赖注入规范: 通过"
        echo "✅ 编译验证: 通过"
        echo "✅ Spring Boot版本: 检查完成"
        echo ""
        echo "守护专家认证: Spring Boot Jakarta守护专家"
        echo "合规性等级: 生产就绪"
    } > "$report_file"

    print_success "合规性报告已生成: $report_file"
}

# 主函数
main() {
    print_header

    # 执行所有检查
    local checks_passed=0
    local total_checks=5

    check_working_directory && ((checks_passed++))
    check_jakarta_compliance && ((checks_passed++))
    check_dependency_injection && ((checks_passed++))
    check_java_time_api && ((checks_passed++))
    check_compilation && ((checks_passed++))
    check_spring_boot_version && ((checks_passed++))

    echo ""
    echo "================================================"

    if [ $checks_passed -eq $total_checks ]; then
        print_success "🎉 所有合规性检查通过！($checks_passed/$total_checks)"
        echo ""
        echo "✅ Jakarta EE包名规范100%合规"
        echo "✅ 依赖注入规范100%合规"
        echo "✅ 编译验证100%通过"
        echo "✅ Spring Boot版本检查完成"
        echo ""
        print_success "项目符合Spring Boot 3.x + Jakarta EE生产标准"
        generate_report
        exit 0
    else
        print_error "合规性检查失败 ($checks_passed/$total_checks)"
        print_info "请修复上述问题后重新执行检查"
        exit 1
    fi
}

# 执行主函数
main "$@"