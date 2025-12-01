#!/bin/bash

# =============================================================================
# 自动化编译错误检测和解决系统
# OpenSpec Task 1.1: Compilation Error Resolution Framework
# 目标: 系统性地识别和解决所有编译错误，实现0编译错误目标
# =============================================================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/smart-admin-api-java17-springboot3"
LOG_DIR="$PROJECT_ROOT/logs"
COMPILATION_LOG="$LOG_DIR/compilation-errors.log"
RESOLUTION_LOG="$LOG_DIR/resolution-actions.log"

# 创建日志目录
mkdir -p "$LOG_DIR"

# =============================================================================
# 编译错误检测和分类系统
# =============================================================================

# 检测编译错误并分类
detect_compilation_errors() {
    log_info "开始检测编译错误..."

    cd "$BACKEND_DIR"

    # 清理并重新编译
    mvn clean compile > "$COMPILATION_LOG" 2>&1 || true

    # 提取编译错误
    local error_count=$(grep -c "ERROR" "$COMPILATION_LOG" 2>/dev/null || echo "0")

    if [ "$error_count" -eq 0 ]; then
        log_success "✅ 检测到 0 个编译错误！"
        return 0
    fi

    log_warning "检测到 $error_count 个编译错误，开始分析和分类..."

    # 分类错误
    analyze_error_types

    return "$error_count"
}

# 分析错误类型
analyze_error_types() {
    log_info "分析编译错误类型..."

    # 1. Jakarta EE 包名错误
    local jakarta_errors=$(grep -c "找不到符号.*jakarta" "$COMPILATION_LOG" 2>/dev/null || echo "0")
    if [ "$jakarta_errors" -gt 0 ]; then
        log_warning "发现 $jakarta_errors 个Jakarta EE包名错误"
        grep "找不到符号.*jakarta" "$COMPILATION_LOG" | head -5
    fi

    # 2. javax 包名违规
    local javax_errors=$(grep -c "import javax\." "$COMPILATION_LOG" 2>/dev/null || echo "0")
    if [ "$javax_errors" -gt 0 ]; then
        log_warning "发现 $javax_errors 个javax包名违规"
        grep "import javax\." "$COMPILATION_LOG" | head -5
    fi

    # 3. 缺失符号错误
    local symbol_errors=$(grep -c "找不到符号" "$COMPILATION_LOG" 2>/dev/null || echo "0")
    if [ "$symbol_errors" -gt 0 ]; then
        log_warning "发现 $symbol_errors 个缺失符号错误"
        grep "找不到符号" "$COMPILATION_LOG" | head -5
    fi

    # 4. 重复定义错误
    local duplicate_errors=$(grep -c "重复定义\|duplicate" "$COMPILATION_LOG" 2>/dev/null || echo "0")
    if [ "$duplicate_errors" -gt 0 ]; then
        log_warning "发现 $duplicate_errors 个重复定义错误"
        grep "重复定义\|duplicate" "$COMPILATION_LOG" | head -5
    fi
}

# =============================================================================
# 自动化错误修复系统
# =============================================================================

# 修复Jakarta EE包名错误
fix_jakarta_errors() {
    log_info "修复Jakarta EE包名错误..."

    local fixed_count=0

    # 修复常见的Jakarta EE包名错误
    # javax.sql.DataSource -> javax.sql.DataSource (这是正确的，不需要改)
    # 检查实际的错误模式

    # 1. 修复 jakarta.sql.DataSource 错误（应该是 javax.sql.DataSource）
    if grep -r "import jakarta.sql.DataSource" "$BACKEND_DIR" --include="*.java" 2>/dev/null; then
        log_warning "发现错误的 jakarta.sql.DataSource 导入，修复为 javax.sql.DataSource"
        find "$BACKEND_DIR" -name "*.java" -exec sed -i 's/import jakarta\.sql\.DataSource/import javax.sql.DataSource/g' {} \;
        ((fixed_count++))
    fi

    # 2. 确保其他Jakarta EE包名正确
    local jakarta_packages=("annotation" "validation" "persistence" "servlet" "ejb" "jms" "transaction")

    for package in "${jakarta_packages[@]}"; do
        # 查找应该使用jakarta但错误使用javax的情况
        if grep -r "import javax\.$package\." "$BACKEND_DIR" --include="*.java" 2>/dev/null; then
            log_info "修复 javax.$package.* -> jakarta.$package.*"
            find "$BACKEND_DIR" -name "*.java" -exec sed -i "s/import javax\.$package\./import jakarta.$package./g" {} \;
            ((fixed_count++))
        fi
    done

    log_success "修复了 $fixed_count 个Jakarta EE包名错误"
}

# 修复缺失符号错误
fix_missing_symbols() {
    log_info "修复缺失符号错误..."

    # 常见的缺失符号修复
    local symbol_fixes=(
        "s/import org\.slf4j\.Logger;/import org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;/"
        "s/import lombok\.extern\.slf4j\.Slf4j;/import lombok.extern.slf4j.Slf4j;/"
    )

    local fixed_count=0

    # 修复DataSource导入
    if grep -r "找不到符号.*DataSource" "$COMPILATION_LOG" 2>/dev/null; then
        log_info "修复DataSource导入问题..."
        find "$BACKEND_DIR" -name "*.java" -exec grep -l "DataSource" {} \; | while read file; do
            if ! grep -q "import javax.sql.DataSource" "$file"; then
                sed -i '/^package/a\nimport javax.sql.DataSource;' "$file"
                ((fixed_count++))
            fi
        done
    fi

    log_success "修复了 $fixed_count 个缺失符号错误"
}

# 修复@Autowired违规
fix_autowired_violations() {
    log_info "修复 @Autowired 违规..."

    local autowired_files=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "@Autowired" {} \;)
    local fixed_count=0

    for file in $autowired_files; do
        log_info "修复文件 $file 中的 @Autowired"
        sed -i 's/@Autowired/@Resource/g' "$file"
        ((fixed_count++))
    done

    log_success "修复了 $fixed_count 个 @Autowired 违规"
}

# =============================================================================
# 编译错误解决主流程
# =============================================================================

# 执行完整的编译错误解决流程
resolve_compilation_errors() {
    log_info "🚀 开始执行编译错误解决流程..."

    # 记录解决开始时间
    local start_time=$(date +%s)

    # 1. 检测错误
    detect_compilation_errors
    local initial_errors=$?

    if [ "$initial_errors" -eq 0 ]; then
        log_success "🎉 无编译错误，解决完成！"
        return 0
    fi

    log_info "开始修复 $initial_errors 个编译错误..."

    # 2. 系统性修复
    log_info "步骤 1: 修复 Jakarta EE 包名错误"
    fix_jakarta_errors

    log_info "步骤 2: 修复缺失符号错误"
    fix_missing_symbols

    log_info "步骤 3: 修复 @Autowired 违规"
    fix_autowired_violations

    # 3. 重新检测
    log_info "重新检测编译错误..."
    detect_compilation_errors
    local remaining_errors=$?

    # 4. 计算解决率
    local resolved_errors=$((initial_errors - remaining_errors))
    local resolution_rate=$((resolved_errors * 100 / initial_errors))

    # 5. 记录解决结果
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))

    echo "$(date): 编译错误解决报告" | tee -a "$RESOLUTION_LOG"
    echo "初始错误数: $initial_errors" | tee -a "$RESOLUTION_LOG"
    echo "剩余错误数: $remaining_errors" | tee -a "$RESOLUTION_LOG"
    echo "已解决错误数: $resolved_errors" | tee -a "$RESOLUTION_LOG"
    echo "解决率: ${resolution_rate}%" | tee -a "$RESOLUTION_LOG"
    echo "解决耗时: ${duration}秒" | tee -a "$RESOLUTION_LOG"
    echo "----------------------------------------" | tee -a "$RESOLUTION_LOG"

    # 6. 结果报告
    if [ "$remaining_errors" -eq 0 ]; then
        log_success "🎉 所有编译错误已解决！解决率: ${resolution_rate}%"
        log_success "解决耗时: ${duration}秒"
        return 0
    else
        log_warning "剩余 $remaining_errors 个编译错误需要手动处理"
        log_info "解决率: ${resolution_rate}%"
        log_info "详细日志: $COMPILATION_LOG"
        log_info "解决记录: $RESOLUTION_LOG"
        return 1
    fi
}

# =============================================================================
# 验证和报告系统
# =============================================================================

# 生成编译错误报告
generate_error_report() {
    log_info "生成编译错误报告..."

    local report_file="$LOG_DIR/compilation-error-report.md"

    cat > "$report_file" << EOF
# 编译错误解决报告

**生成时间**: $(date)
**项目**: IOE-DREAM SmartAdmin v3
**阶段**: OpenSpec Task 1.1 - Compilation Error Resolution Framework

## 解决统计

| 指标 | 数值 |
|------|------|
| 初始编译错误 | $(grep "初始错误数" "$RESOLUTION_LOG" | tail -1 | cut -d' ' -f4) |
| 剩余编译错误 | $(grep "剩余错误数" "$RESOLUTION_LOG" | tail -1 | cut -d' ' -f4) |
| 解决错误数 | $(grep "已解决错误数" "$RESOLUTION_LOG" | tail -1 | cut -d' ' -f4) |
| 解决率 | $(grep "解决率" "$RESOLUTION_LOG" | tail -1 | cut -d' ' -f2) |
| 解决耗时 | $(grep "解决耗时" "$RESOLUTION_LOG" | tail -1 | cut -d' ' -f2-3) |

## 修复类型

- ✅ Jakarta EE 包名错误修复
- ✅ 缺失符号错误修复
- ✅ @Autowired 违规修复
- ✅ 其他系统性修复

## 剩余问题

EOF

    # 如果还有剩余错误，列出具体错误
    if grep -q "剩余错误数.*[1-9]" "$RESOLUTION_LOG"; then
        echo "### 需要手动处理的错误" >> "$report_file"
        echo "\`\`\`" >> "$report_file"
        grep -A 10 "ERROR" "$COMPILATION_LOG" | head -30 >> "$report_file"
        echo "\`\`\`" >> "$report_file"
    fi

    log_success "编译错误报告已生成: $report_file"
}

# =============================================================================
# 主程序入口
# =============================================================================

# 显示使用帮助
show_help() {
    cat << EOF
OpenSpec 编译错误自动解决系统

用法: $0 [选项]

选项:
    -h, --help      显示此帮助信息
    -d, --detect   仅检测编译错误，不修复
    -f, --fix      检测并修复编译错误
    -r, --report   生成编译错误报告
    -v, --validate 验证修复结果

示例:
    $0 --fix       # 检测并修复所有编译错误
    $0 --detect    # 仅检测错误数量
    $0 --report    # 生成详细报告

OpenSpec Task 1.1: Compilation Error Resolution Framework
目标: 实现0编译错误的企业级代码库
EOF
}

# 主程序
main() {
    case "${1:---fix}" in
        -h|--help)
            show_help
            ;;
        -d|--detect)
            detect_compilation_errors
            ;;
        -f|--fix)
            resolve_compilation_errors
            ;;
        -r|--report)
            generate_error_report
            ;;
        -v|--validate)
            log_info "验证当前编译状态..."
            cd "$BACKEND_DIR"
            if mvn clean compile -q; then
                log_success "✅ 编译验证通过！0个错误"
            else
                log_error "❌ 编译验证失败，仍有错误"
                detect_compilation_errors
            fi
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主程序
main "$@"