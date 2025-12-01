#!/bin/bash

# ===================================================================
# IOE-DREAM 项目 Pre-commit repowiki 合规性检查脚本
#
# 功能:
# 1. 提交前强制执行repowiki规范检查
# 2. 阻止不符合规范的代码提交
# 3. 提供清晰的错误信息和修复建议
#
# 使用方法:
# 1. 将此脚本复制到 .git/hooks/pre-commit
# 2. 或者通过 make install-hooks 安装
#
# 作者: IOE-DREAM Team
# 版本: v1.0
# 日期: 2025-11-24
# ===================================================================

set -euo pipefail

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT=$(pwd)
SCRIPTS_DIR="$PROJECT_ROOT/scripts"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[⚠]${NC} $1"
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
}

log_header() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

# 检查是否有Java文件被修改
check_java_files_changed() {
    local changed_files=$(git diff --cached --name-only --diff-filter=ACM | grep '\.java$' || true)
    if [ -z "$changed_files" ]; then
        echo "📝 没有Java文件被修改，跳过repowiki合规性检查"
        exit 0
    fi

    echo "🔍 检测到Java文件变更，开始执行repowiki合规性检查..."
    echo "变更的Java文件:"
    echo "$changed_files"
    echo ""

    return 0
}

# 检查脚本是否存在
check_scripts_exist() {
    if [ ! -f "$SCRIPTS_DIR/repowiki-quick-check.sh" ]; then
        log_error "找不到repowiki快速检查脚本: $SCRIPTS_DIR/repowiki-quick-check.sh"
        log_info "请确保scripts目录包含repowiki检查脚本"
        exit 1
    fi

    if [ ! -x "$SCRIPTS_DIR/repowiki-quick-check.sh" ]; then
        log_warning "repowiki检查脚本没有执行权限，正在添加..."
        chmod +x "$SCRIPTS_DIR/repowiki-quick-check.sh"
    fi
}

# 执行repowiki快速检查
run_repowiki_quick_check() {
    log_header "执行repowiki快速合规性检查"

    cd "$PROJECT_ROOT"

    if bash "$SCRIPTS_DIR/repowiki-quick-check.sh"; then
        log_success "repowiki快速合规性检查通过"
        return 0
    else
        log_error "repowiki快速合规性检查失败！"
        log_error "提交被阻止，请先修复上述问题"

        # 提供修复建议
        provide_fix_suggestions

        return 1
    fi
}

# 提供修复建议
provide_fix_suggestions() {
    echo ""
    log_header "修复建议"

    echo "🔧 常见问题的修复方法："
    echo ""
    echo "1. Jakarta EE 包名违规:"
    echo "   - 将 javax.annotation.* 改为 jakarta.annotation.*"
    echo "   - 将 javax.validation.* 改为 jakarta.validation.*"
    echo "   - 将 javax.persistence.* 改为 jakarta.persistence.*"
    echo "   - 将 javax.servlet.* 改为 jakarta.servlet.*"
    echo ""
    echo "2. 依赖注入违规:"
    echo "   - 将 @Autowired 改为 @Resource"
    echo ""
    echo "3. DAO 命名规范:"
    echo "   - 将 *Repository.java 重命名为 *Dao.java"
    echo ""
    echo "4. 项目结构问题:"
    echo "   - 检查是否存在 module/*/net/* 的异常嵌套路径"
    echo ""
    echo "5. 文件编码问题:"
    echo "   - 确保所有Java文件使用UTF-8编码"
    echo "   - 移除BOM标记"
    echo ""

    echo "🚀 快速修复命令："
    echo "   cd $PROJECT_ROOT"
    echo "   bash scripts/repowiki-quick-check.sh  # 查看具体问题"
    echo ""
}

# 检查编译状态
check_compilation() {
    log_header "检查Java项目编译状态"

    if [ ! -d "smart-admin-api-java17-springboot3" ]; then
        log_warning "未找到后端项目目录，跳过编译检查"
        return 0
    fi

    cd "smart-admin-api-java17-springboot3"

    if mvn clean compile -q -DskipTests; then
        log_success "Java项目编译检查通过"
    else
        log_error "Java项目编译失败！"
        log_error "请先修复编译错误再提交代码"
        return 1
    fi

    cd "$PROJECT_ROOT"
}

# 检查关键代码质量指标
check_code_quality_metrics() {
    log_header "检查关键代码质量指标"

    if [ ! -d "smart-admin-api-java17-springboot3" ]; then
        return 0
    fi

    cd "smart-admin-api-java17-springboot3"

    # 检查System.out使用
    local systemout_count=$(find . -name "*.java" -exec grep -l "System\.out\.println" {} \; 2>/dev/null | wc -l)
    if [ "$systemout_count" -gt 0 ]; then
        log_error "发现 $systemout_count 处System.out.println使用，请使用日志框架"
        find . -name "*.java" -exec grep -Hn "System\.out\.println" {} \; 2>/dev/null | head -5
        return 1
    fi

    # 检查硬编码密码
    local hardcoded_count=$(find . -name "*.java" -exec grep -l "password.*=.*[\"']" {} \; 2>/dev/null | wc -l)
    if [ "$hardcoded_count" -gt 3 ]; then
        log_warning "发现 $hardcoded_count 处可能的硬编码密码，请检查"
        find . -name "*.java" -exec grep -Hn "password.*=.*[\"']" {} \; 2>/dev/null | head -3
    fi

    cd "$PROJECT_ROOT"
    log_success "代码质量指标检查通过"
    return 0
}

# 检查前端文件（如果存在）
check_frontend_files() {
    log_header "检查前端文件质量"

    if [ ! -d "smart-admin-web-javascript" ]; then
        log_info "未找到前端项目目录，跳过前端检查"
        return 0
    fi

    cd "smart-admin-web-javascript"

    # 检查package.json是否存在
    if [ ! -f "package.json" ]; then
        log_warning "未找到package.json，跳过前端依赖检查"
        cd "$PROJECT_ROOT"
        return 0
    fi

    # 简单检查是否有语法错误（如果有node环境）
    if command -v node >/dev/null 2>&1 && [ -d "node_modules" ]; then
        log_info "执行前端语法检查..."
        if npm run type-check >/dev/null 2>&1; then
            log_success "前端TypeScript类型检查通过"
        else
            log_warning "前端TypeScript类型检查失败，请检查"
        fi
    fi

    cd "$PROJECT_ROOT"
    log_success "前端文件检查完成"
}

# 生成检查报告
generate_report() {
    local report_file="$PROJECT_ROOT/.git/pre-commit-report.md"

    cat > "$report_file" << EOF
# Pre-commit repowiki 合规性检查报告

**检查时间**: $(date '+%Y-%m-%d %H:%M:%S')
**项目路径**: $PROJECT_ROOT

## 检查结果

✅ **所有检查项都已通过！**

- ✅ repowiki 规范合规性检查
- ✅ Java 项目编译检查
- ✅ 代码质量指标检查
- ✅ 前端文件质量检查

## 可以安全提交

代码符合 repowiki 规范要求，可以安全提交到版本库。

---

*此报告由 pre-commit hook 自动生成*
EOF

    log_info "检查报告已生成: $report_file"
}

# 主函数
main() {
    echo "🚀 IOE-DREAM Pre-commit repowiki 合规性检查"
    echo "=========================================="
    echo ""

    # 检查是否有Java文件变更
    check_java_files_changed

    # 检查脚本是否存在
    check_scripts_exist

    # 执行repowiki快速检查
    if ! run_repowiki_quick_check; then
        exit 1
    fi

    # 检查编译状态
    if ! check_compilation; then
        exit 1
    fi

    # 检查代码质量指标
    if ! check_code_quality_metrics; then
        exit 1
    fi

    # 检查前端文件
    check_frontend_files

    # 生成报告
    generate_report

    echo ""
    log_header "检查完成"
    echo "🎉 ${GREEN}所有 repowiki 合规性检查都已通过！${NC}"
    echo "✅ 代码可以安全提交"
    echo ""

    exit 0
}

# 执行主函数
main "$@"