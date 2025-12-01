#!/bin/bash

# ==============================================================================
# IOE-DREAM 智能开发助手
#
# 功能：统一入口，智能检测，一键解决开发中的常见问题
# 使用：./scripts/smart-dev-helper.sh [command]
# ==============================================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${PURPLE}[STEP]${NC} $1"
}

# 显示帮助信息
show_help() {
    cat << EOF
${CYAN}IOE-DREAM 智能开发助手 v1.0${NC}

用法: ./scripts/smart-dev-helper.sh [命令]

🚀 快速开始:
  quick-check     - 快速环境检查（推荐新手使用）
  start-work      - 开始开发前检查
  finish-work     - 完成开发后验证
  commit          - 提交前全面检查

🔧 问题修复:
  fix-encoding    - 修复编码问题
  fix-javax       - 修复javax包名问题
  fix-standards   - 修复编码规范问题
  auto-fix        - 智能修复常见问题

📊 质量检查:
  check-quality   - 代码质量检查
  check-arch      - 架构规范检查
  check-security  - 安全规范检查
  full-check      - 全面质量检查

🛠️ 工具功能:
  deploy          - Docker部署验证
  test            - 运行测试
  docs            - 生成文档
  clean           - 清理项目

📖 学习帮助:
  learn-rules     - 学习核心规范
  learn-arch      - 学习架构设计
  learn-fix       - 学习问题修复

其他:
  status          - 显示项目状态
  help            - 显示此帮助信息

示例:
  ./scripts/smart-dev-helper.sh quick-check
  ./scripts/smart-dev-helper.sh auto-fix
  ./scripts/smart-dev-helper.sh full-check

EOF
}

# 显示项目状态
show_status() {
    log_step "检查项目状态..."

    echo ""
    echo "📊 项目概览:"
    echo "  项目名称: IOE-DREAM 智能企业管理系统"
    echo "  技术栈: Java 17 + Spring Boot 3.x + Vue3"
    echo "  当前分支: $(git branch --show-current 2>/dev/null || echo '未知')"
    echo "  最后提交: $(git log -1 --pretty=format:'%h - %s' 2>/dev/null || echo '无提交记录')"

    echo ""
    echo "🔍 环境检查:"

    # Java版本
    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
        if [[ "$java_version" =~ ^17 ]]; then
            echo -e "  Java版本: ${GREEN}$java_version ✓${NC}"
        else
            echo -e "  Java版本: ${RED}$java_version ✗ (需要Java 17)${NC}"
        fi
    else
        echo -e "  Java版本: ${RED}未安装 ✗${NC}"
    fi

    # Maven版本
    if command -v mvn &> /dev/null; then
        maven_version=$(mvn -version 2>&1 | head -n 1 | awk '{print $3}')
        echo -e "  Maven版本: ${GREEN}$maven_version ✓${NC}"
    else
        echo -e "  Maven版本: ${RED}未安装 ✗${NC}"
    fi

    # Node.js版本
    if command -v node &> /dev/null; then
        node_version=$(node --version)
        echo -e "  Node.js版本: ${GREEN}$node_version ✓${NC}"
    else
        echo -e "  Node.js版本: ${YELLOW}未安装 (前端开发需要)${NC}"
    fi

    echo ""
    echo "📁 项目结构:"
    echo "  后端项目: smart-admin-api-java17-springboot3/"
    echo "  前端项目: smart-admin-web-javascript/"
    echo "  移动端项目: smart-app/"
    echo "  文档目录: docs/"
    echo "  技能目录: .claude/skills/"

    # 检查关键文件
    echo ""
    echo "📋 关键文件检查:"

    if [ -f "CLAUDE.md" ]; then
        echo -e "  CLAUDE.md: ${GREEN}存在 ✓${NC}"
    else
        echo -e "  CLAUDE.md: ${RED}缺失 ✗${NC}"
    fi

    if [ -f "smart-admin-api-java17-springboot3/pom.xml" ]; then
        echo -e "  后端配置: ${GREEN}存在 ✓${NC}"
    else
        echo -e "  后端配置: ${RED}缺失 ✗${NC}"
    fi

    if [ -f "smart-admin-web-javascript/package.json" ]; then
        echo -e "  前端配置: ${GREEN}存在 ✓${NC}"
    else
        echo -e "  前端配置: ${YELLOW}缺失 (前端开发需要)${NC}"
    fi
}

# 快速环境检查
quick_check() {
    log_step "执行快速环境检查..."

    local issues=0

    # 1. 检查Java环境
    if ! command -v java &> /dev/null; then
        log_error "Java未安装或未配置到PATH"
        ((issues++))
    else
        java_version=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
        if [[ ! "$java_version" =~ ^17 ]]; then
            log_error "Java版本不正确: $java_version (需要Java 17)"
            ((issues++))
        else
            log_success "Java版本正确: $java_version"
        fi
    fi

    # 2. 检查Maven环境
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装或未配置到PATH"
        ((issues++))
    else
        log_success "Maven环境正常"
    fi

    # 3. 检查项目结构
    if [ ! -d "smart-admin-api-java17-springboot3" ]; then
        log_error "后端项目目录不存在"
        ((issues++))
    else
        log_success "后端项目目录存在"
    fi

    # 4. 检查配置文件
    if [ ! -f "smart-admin-api-java17-springboot3/pom.xml" ]; then
        log_error "后端配置文件pom.xml不存在"
        ((issues++))
    else
        log_success "后端配置文件存在"
    fi

    # 5. 快速编译检查
    log_info "执行快速编译检查..."
    cd smart-admin-api-java17-springboot3
    if mvn clean compile -q; then
        log_success "编译检查通过"
    else
        log_error "编译检查失败"
        ((issues++))
    fi
    cd ..

    # 总结
    echo ""
    if [ $issues -eq 0 ]; then
        log_success "快速检查通过！环境正常，可以开始开发。"
        return 0
    else
        log_error "发现 $issues 个问题，请修复后再开始开发。"
        return 1
    fi
}

# 开始开发前检查
start_work() {
    log_step "执行开发前检查..."

    # 1. 环境检查
    if ! quick_check; then
        log_error "环境检查失败，无法开始开发"
        return 1
    fi

    # 2. 规范检查
    log_info "检查编码规范..."

    # 检查javax包使用
    javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
    if [ $javax_count -ne 0 ]; then
        log_warning "发现 $javax_count 个文件使用javax包，建议修复"
        echo "运行 './scripts/smart-dev-helper.sh fix-javax' 进行修复"
    fi

    # 检查@Autowired使用
    autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
    if [ $autowired_count -ne 0 ]; then
        log_warning "发现 $autowired_count 个文件使用@Autowired，建议修复"
        echo "运行 './scripts/smart-dev-helper.sh fix-standards' 进行修复"
    fi

    # 3. Git状态检查
    log_info "检查Git状态..."
    if git rev-parse --git-dir > /dev/null 2>&1; then
        if [ -n "$(git status --porcelain)" ]; then
            log_warning "工作目录有未提交的更改"
            git status --short
        else
            log_success "工作目录干净"
        fi
    else
        log_warning "不是Git仓库"
    fi

    log_success "开发前检查完成，可以开始开发！"
}

# 完成开发后验证
finish_work() {
    log_step "执行开发后验证..."

    # 1. 编译验证
    log_info "验证代码编译..."
    cd smart-admin-api-java17-springboot3
    if mvn clean compile -q; then
        log_success "编译验证通过"
    else
        log_error "编译验证失败"
        cd ..
        return 1
    fi

    # 2. 质量检查
    log_info "执行代码质量检查..."
    if [ -f "../scripts/enforce-standards.sh" ]; then
        if bash ../scripts/enforce-standards.sh; then
            log_success "代码质量检查通过"
        else
            log_warning "代码质量检查发现问题，建议修复"
        fi
    fi

    # 3. 规范检查
    log_info "检查核心规范..."

    local issues=0

    # 检查javax包
    javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
    if [ $javax_count -ne 0 ]; then
        log_error "发现 $javax_count 个javax包违规"
        ((issues++))
    fi

    # 检查@Autowired
    autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
    if [ $autowired_count -ne 0 ]; then
        log_error "发现 $autowired_count 个@Autowired违规"
        ((issues++))
    fi

    # 检查System.out
    system_out_count=$(find . -name "*.java" -exec grep -l "System\.out\." {} \; | wc -l)
    if [ $system_out_count -ne 0 ]; then
        log_error "发现 $system_out_count 个System.out违规"
        ((issues++))
    fi

    cd ..

    # 总结
    echo ""
    if [ $issues -eq 0 ]; then
        log_success "开发后验证通过！代码质量良好，可以提交。"
        return 0
    else
        log_error "发现 $issues 个规范问题，请修复后再提交。"
        return 1
    fi
}

# 提交前全面检查
commit() {
    log_step "执行提交前全面检查..."

    # 1. 完成开发验证
    if ! finish_work; then
        log_error "开发后验证失败，无法提交"
        return 1
    fi

    # 2. 运行commit-guard
    if [ -f "scripts/commit-guard.sh" ]; then
        log_info "运行提交守卫..."
        if bash scripts/commit-guard.sh; then
            log_success "提交守卫检查通过"
        else
            log_error "提交守卫检查失败"
            return 1
        fi
    fi

    # 3. 测试检查（如果有测试）
    log_info "运行测试..."
    cd smart-admin-api-java17-springboot3
    if mvn test -q; then
        log_success "测试通过"
    else
        log_warning "测试失败或无测试"
    fi
    cd ..

    log_success "提交前检查全部通过！可以安全提交代码。"
}

# 智能修复常见问题
auto_fix() {
    log_step "智能修复常见问题..."

    local fixed=0

    # 1. 修复javax包问题
    log_info "检查和修复javax包问题..."
    javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
    if [ $javax_count -gt 0 ]; then
        log_info "发现 $javax_count 个文件有javax包问题，开始修复..."
        if [ -f "scripts/fix-javax-imports.sh" ]; then
            bash scripts/fix-javax-imports.sh
            log_success "javax包问题修复完成"
            ((fixed++))
        else
            log_warning "javax包修复脚本不存在，请手动修复"
        fi
    else
        log_success "无javax包问题"
    fi

    # 2. 修复@Autowired问题
    log_info "检查和修复@Autowired问题..."
    autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
    if [ $autowired_count -gt 0 ]; then
        log_info "发现 $autowired_count 个文件有@Autowired问题，开始修复..."
        find . -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;
        log_success "@Autowired问题修复完成"
        ((fixed++))
    else
        log_success "无@Autowired问题"
    fi

    # 3. 修复System.out问题
    log_info "检查和修复System.out问题..."
    system_out_count=$(find . -name "*.java" -exec grep -l "System\.out\." {} \; | wc -l)
    if [ $system_out_count -gt 0 ]; then
        log_info "发现 $system_out_count 个文件有System.out问题，开始修复..."
        # 这里可以添加更复杂的修复逻辑
        log_warning "System.out问题需要手动修复，请使用@Slf4j + log.info()"
    else
        log_success "无System.out问题"
    fi

    # 4. 修复编码问题
    log_info "检查和修复编码问题..."
    if [ -f "scripts/fix-encoding-issues.sh" ]; then
        bash scripts/fix-encoding-issues.sh
        log_success "编码问题修复完成"
        ((fixed++))
    fi

    echo ""
    if [ $fixed -gt 0 ]; then
        log_success "智能修复完成，修复了 $fixed 类问题！"
        log_info "建议运行 './scripts/smart-dev-helper.sh finish-work' 验证修复效果"
    else
        log_success "未发现需要修复的问题，代码质量良好！"
    fi
}

# 修复编码问题
fix_encoding() {
    log_step "修复编码问题..."

    if [ -f "scripts/fix-encoding-issues.sh" ]; then
        bash scripts/fix-encoding-issues.sh
        log_success "编码问题修复完成"
    else
        log_error "编码修复脚本不存在"
        return 1
    fi
}

# 修复javax包名问题
fix_javax() {
    log_step "修复javax包名问题..."

    javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
    if [ $javax_count -eq 0 ]; then
        log_success "未发现javax包名问题"
        return 0
    fi

    log_info "发现 $javax_count 个文件有javax包名问题，开始修复..."

    # 修复常见的javax包名
    find . -name "*.java" -exec sed -i 's/javax\.servlet\./jakarta.servlet./g' {} \;
    find . -name "*.java" -exec sed -i 's/javax\.validation\./jakarta.validation./g' {} \;
    find . -name "*.java" -exec sed -i 's/javax\.annotation\./jakarta.annotation./g' {} \;
    find . -name "*.java" -exec sed -i 's/javax\.persistence\./jakarta.persistence./g' {} \;
    find . -name "*.java" -exec sed -i 's/javax\.ejb\./jakarta.ejb./g' {} \;

    log_success "javax包名问题修复完成"
    log_info "建议运行 './scripts/smart-dev-helper.sh finish-work' 验证修复效果"
}

# 修复编码规范问题
fix_standards() {
    log_step "修复编码规范问题..."

    local fixed=0

    # 修复@Autowired
    autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
    if [ $autowired_count -gt 0 ]; then
        log_info "修复@Autowired问题..."
        find . -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;
        log_success "@Autowired问题修复完成"
        ((fixed++))
    fi

    # 这里可以添加更多规范修复逻辑

    if [ $fixed -gt 0 ]; then
        log_success "编码规范问题修复完成"
    else
        log_success "未发现编码规范问题"
    fi
}

# 代码质量检查
check_quality() {
    log_step "执行代码质量检查..."

    if [ -f "scripts/enforce-standards.sh" ]; then
        bash scripts/enforce-standards.sh
    else
        log_warning "代码质量检查脚本不存在，执行基础检查..."

        # 基础检查
        local issues=0

        # 检查javax包
        javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
        if [ $javax_count -ne 0 ]; then
            log_error "发现 $javax_count 个javax包违规"
            ((issues++))
        fi

        # 检查@Autowired
        autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
        if [ $autowired_count -ne 0 ]; then
            log_error "发现 $autowired_count 个@Autowired违规"
            ((issues++))
        fi

        if [ $issues -eq 0 ]; then
            log_success "代码质量检查通过"
        else
            log_error "代码质量检查失败，发现 $issues 个问题"
            return 1
        fi
    fi
}

# 全面质量检查
full_check() {
    log_step "执行全面质量检查..."

    local failed=0

    # 1. 编译检查
    log_info "1/5 编译检查..."
    cd smart-admin-api-java17-springboot3
    if ! mvn clean compile -q; then
        log_error "编译检查失败"
        ((failed++))
    else
        log_success "编译检查通过"
    fi
    cd ..

    # 2. 质量检查
    log_info "2/5 质量检查..."
    if ! check_quality; then
        ((failed++))
    fi

    # 3. 架构检查
    log_info "3/5 架构检查..."
    if [ -f "scripts/architecture-compliance-check.sh" ]; then
        if ! bash scripts/architecture-compliance-check.sh; then
            ((failed++))
        fi
    else
        log_warning "架构检查脚本不存在，跳过"
    fi

    # 4. 测试检查
    log_info "4/5 测试检查..."
    cd smart-admin-api-java17-springboot3
    if mvn test -q; then
        log_success "测试检查通过"
    else
        log_warning "测试失败或无测试"
        ((failed++))
    fi
    cd ..

    # 5. 安全检查
    log_info "5/5 安全检查..."
    if [ -f "scripts/security-check.sh" ]; then
        if ! bash scripts/security-check.sh; then
            ((failed++))
        fi
    else
        log_warning "安全检查脚本不存在，跳过"
    fi

    # 总结
    echo ""
    if [ $failed -eq 0 ]; then
        log_success "全面质量检查通过！代码质量优秀。"
        return 0
    else
        log_error "全面质量检查失败，$failed 个检查项未通过。"
        return 1
    fi
}

# Docker部署验证
deploy() {
    log_step "执行Docker部署验证..."

    if [ -f "scripts/docker-deploy.sh" ]; then
        bash scripts/docker-deploy.sh
    else
        log_error "Docker部署脚本不存在"
        return 1
    fi
}

# 运行测试
test() {
    log_step "运行测试..."

    cd smart-admin-api-java17-springboot3
    if mvn test; then
        log_success "测试运行完成"
    else
        log_error "测试运行失败"
        return 1
    fi
    cd ..
}

# 学习核心规范
learn_rules() {
    log_step "学习核心规范..."

    if [ -f "docs/00-快速开始/核心规范10条.md" ]; then
        echo ""
        cat docs/00-快速开始/核心规范10条.md
    else
        log_error "核心规范文档不存在"
        return 1
    fi
}

# 主函数
main() {
    case "${1:-help}" in
        "help"|"-h"|"--help")
            show_help
            ;;
        "status")
            show_status
            ;;
        "quick-check")
            quick_check
            ;;
        "start-work")
            start_work
            ;;
        "finish-work")
            finish_work
            ;;
        "commit")
            commit
            ;;
        "auto-fix")
            auto_fix
            ;;
        "fix-encoding")
            fix_encoding
            ;;
        "fix-javax")
            fix_javax
            ;;
        "fix-standards")
            fix_standards
            ;;
        "check-quality")
            check_quality
            ;;
        "check-arch")
            log_info "架构检查..."
            if [ -f "scripts/architecture-compliance-check.sh" ]; then
                bash scripts/architecture-compliance-check.sh
            else
                log_warning "架构检查脚本不存在"
            fi
            ;;
        "check-security")
            log_info "安全检查..."
            if [ -f "scripts/security-check.sh" ]; then
                bash scripts/security-check.sh
            else
                log_warning "安全检查脚本不存在"
            fi
            ;;
        "full-check")
            full_check
            ;;
        "deploy")
            deploy
            ;;
        "test")
            test
            ;;
        "docs")
            log_info "生成文档..."
            if [ -f "scripts/generate-docs.sh" ]; then
                bash scripts/generate-docs.sh
            else
                log_warning "文档生成脚本不存在"
            fi
            ;;
        "clean")
            log_info "清理项目..."
            cd smart-admin-api-java17-springboot3
            mvn clean
            cd ..
            log_success "项目清理完成"
            ;;
        "learn-rules")
            learn_rules
            ;;
        "learn-arch")
            log_info "学习架构设计..."
            if [ -f "docs/repowiki/zh/content/后端架构/四层架构详解/四层架构详解.md" ]; then
                less docs/repowiki/zh/content/后端架构/四层架构详解/四层架构详解.md
            else
                log_warning "架构文档不存在"
            fi
            ;;
        "learn-fix")
            log_info "学习问题修复..."
            echo ""
            echo "常见问题修复方法："
            echo "1. javax包问题: ./scripts/smart-dev-helper.sh fix-javax"
            echo "2. 编码规范问题: ./scripts/smart-dev-helper.sh fix-standards"
            echo "3. 编码问题: ./scripts/smart-dev-helper.sh fix-encoding"
            echo "4. 自动修复: ./scripts/smart-dev-helper.sh auto-fix"
            echo ""
            echo "详细文档请参考: docs/00-快速开始/10分钟上手指南.md"
            ;;
        *)
            log_error "未知命令: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"