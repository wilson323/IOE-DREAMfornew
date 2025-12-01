#!/bin/bash

# ===================================================================
# IOE-DREAM 项目 repowiki 综合合规性检查工具
#
# 功能:
# 1. Jakarta包名合规性检查
# 2. 项目结构规范化检查
# 3. DAO命名规范检查
# 4. 依赖注入规范检查
# 5. 编码规范检查
# 6. 自动生成检查报告
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
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 配置
PROJECT_ROOT=$(pwd)
REPORT_DIR="${PROJECT_ROOT}/compliance-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="${REPORT_DIR}/repowiki-compliance-report-${TIMESTAMP}.json"
HTML_REPORT="${REPORT_DIR}/repowiki-compliance-report-${TIMESTAMP}.html"

# 创建报告目录
mkdir -p "$REPORT_DIR"

# 检查结果统计
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0
WARNING_CHECKS=0

# 结果数组
declare -a RESULTS

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
    ((PASSED_CHECKS++))
}

log_warning() {
    echo -e "${YELLOW}[⚠]${NC} $1"
    ((WARNING_CHECKS++))
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
    ((FAILED_CHECKS++))
}

log_header() {
    echo -e "${PURPLE}=== $1 ===${NC}"
}

# 检查函数: Jakarta包名合规性
check_jakarta_compliance() {
    log_header "Jakarta包名合规性检查"
    ((TOTAL_CHECKS++))

    local jakarta_violations=0
    local jakarta_files=()

    # 查找违规的javax EE包
    local ee_packages=(
        "javax\\.annotation"
        "javax\\.validation"
        "javax\\.persistence"
        "javax\\.servlet"
        "javax\\.jms"
        "javax\\.transaction"
        "javax\\.ejb"
        "javax\\.xml\\.bind"
    )

    for package_pattern in "${ee_packages[@]}"; do
        local files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "$package_pattern" {} \; 2>/dev/null || true)
        if [[ -n "$files" ]]; then
            while IFS= read -r file; do
                jakarta_files+=("$file")
                ((jakarta_violations++))
            done <<< "$files"
        fi
    done

    if [[ $jakarta_violations -eq 0 ]]; then
        log_success "Jakarta包名合规性检查通过 (0个违规文件)"
        RESULTS+=("\"jakarta_compliance\": {\"status\": \"PASS\", \"violations\": 0, \"files\": []}")
    else
        log_error "Jakarta包名合规性检查失败 (发现 $jakarta_violations 个违规文件)"
        log_error "违规文件: $(printf '%s ' "${jakarta_files[@]}")"
        RESULTS+=("\"jakarta_compliance\": {\"status\": \"FAIL\", \"violations\": $jakarta_violations, \"files\": $(printf '[%s]' "$(printf '"%s",' "${jakarta_files[@]}" | sed 's/,$//')")}")
    fi
}

# 检查函数: 项目结构规范化
check_project_structure() {
    log_header "项目结构规范化检查"
    ((TOTAL_CHECKS++))

    local structure_violations=0
    local violations=()

    # 检查异常嵌套路径
    local nested_paths=$(find "$PROJECT_ROOT" -path "*module/*/net/*" -type f 2>/dev/null | wc -l)
    if [[ $nested_paths -gt 0 ]]; then
        ((structure_violations++))
        violations+=("异常嵌套路径: $nested_paths 个文件")
    fi

    # 检查目录命名规范
    local invalid_dirs=$(find "$PROJECT_ROOT" -type d -name "* *" 2>/dev/null | wc -l)
    if [[ $invalid_dirs -gt 0 ]]; then
        log_warning "发现 $invalid_dirs 个目录名包含空格"
    fi

    if [[ $structure_violations -eq 0 ]]; then
        log_success "项目结构规范化检查通过"
        RESULTS+=("\"project_structure\": {\"status\": \"PASS\", \"violations\": 0, \"details\": []}")
    else
        log_error "项目结构规范化检查失败 (发现 $structure_violations 个违规)"
        for violation in "${violations[@]}"; do
            log_error "- $violation"
        done
        RESULTS+=("\"project_structure\": {\"status\": \"FAIL\", \"violations\": $structure_violations, \"details\": $(printf '[%s]' "$(printf '"%s",' "${violations[@]}" | sed 's/,$//')")}")
    fi
}

# 检查函数: DAO命名规范
check_dao_naming() {
    log_header "DAO命名规范检查"
    ((TOTAL_CHECKS++))

    local repository_files=()
    local repository_count=0

    # 查找Repository文件
    local repo_files=$(find "$PROJECT_ROOT" -name "*Repository.java" 2>/dev/null)
    for file in $repo_files; do
        if [[ -f "$file" ]]; then
            repository_files+=("$file")
            ((repository_count++))
        fi
    done

    if [[ $repository_count -eq 0 ]]; then
        log_success "DAO命名规范检查通过 (0个Repository文件)"
        RESULTS+=("\"dao_naming\": {\"status\": \"PASS\", \"repository_count\": 0, \"files\": []}")
    else
        log_error "DAO命名规范检查失败 (发现 $repository_count 个Repository文件)"
        log_error "需要重命名的文件: $(printf '%s ' "${repository_files[@]}")"
        RESULTS+=("\"dao_naming\": {\"status\": \"FAIL\", \"repository_count\": $repository_count, \"files\": $(printf '[%s]' "$(printf '"%s",' "${repository_files[@]}" | sed 's/,$//')")}")
    fi
}

# 检查函数: 依赖注入规范
check_dependency_injection() {
    log_header "依赖注入规范检查"
    ((TOTAL_CHECKS++))

    local autowired_files=()
    local autowired_count=0

    # 查找@Autowired使用
    local aw_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null)
    for file in $aw_files; do
        if [[ -f "$file" ]]; then
            autowired_files+=("$file")
            ((autowired_count++))
        fi
    done

    if [[ $autowired_count -eq 0 ]]; then
        log_success "依赖注入规范检查通过 (使用@Resource依赖注入)"
        RESULTS+=("\"dependency_injection\": {\"status\": \"PASS\", \"autowired_count\": 0, \"files\": []}")
    else
        log_error "依赖注入规范检查失败 (发现 $autowired_count 个@Autowired使用)"
        log_error "需要修复的文件: $(printf '%s ' "${autowired_files[@]}")"
        RESULTS+=("\"dependency_injection\": {\"status\": \"FAIL\", \"autowired_count\": $autowired_count, \"files\": $(printf '[%s]' "$(printf '"%s",' "${autowired_files[@]}" | sed 's/,$//')")}")
    fi
}

# 检查函数: 编码规范
check_coding_standards() {
    log_header "编码规范检查"
    ((TOTAL_CHECKS++))

    local coding_violations=0
    local violations=()

    # 检查System.out使用
    local systemout_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "System\\.out\\.println" {} \; 2>/dev/null)
    local systemout_count=0
    for file in $systemout_files; do
        if [[ -f "$file" ]]; then
            ((systemout_count++))
        fi
    done

    if [[ $systemout_count -gt 0 ]]; then
        ((coding_violations++))
        violations+=("System.out.println使用: $systemout_count 个文件")
    fi

    # 检查硬编码配置
    local hardcoded_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "password\\|secret\\|key.*=" {} \; 2>/dev/null | wc -l)
    if [[ $hardcoded_files -gt 5 ]]; then
        ((coding_violations++))
        violations+=("可能的硬编码敏感信息: $hardcoded_files 个文件")
    fi

    if [[ $coding_violations -eq 0 ]]; then
        log_success "编码规范检查通过"
        RESULTS+=("\"coding_standards\": {\"status\": \"PASS\", \"violations\": 0, \"details\": []}")
    else
        log_warning "编码规范检查发现 $coding_violations 个潜在问题"
        for violation in "${violations[@]}"; do
            log_warning "- $violation"
        done
        RESULTS+=("\"coding_standards\": {\"status\": \"WARNING\", \"violations\": $coding_violations, \"details\": $(printf '[%s]' "$(printf '"%s",' "${violations[@]}" | sed 's/,$//')")}")
    fi
}

# 检查函数: 文件编码规范
check_file_encoding() {
    log_header "文件编码规范检查"
    ((TOTAL_CHECKS++))

    local encoding_issues=0
    local issue_files=()

    # 检查BOM标记
    local bom_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; 2>/dev/null || true)
    for file in $bom_files; do
        if [[ -f "$file" ]]; then
            issue_files+=("$file (BOM标记)")
            ((encoding_issues++))
        fi
    done

    # 检查非UTF-8编码文件
    local non_utf8_files=$(find "$PROJECT_ROOT" -name "*.java" -exec file {} \; 2>/dev/null | grep -v "UTF-8\|ASCII" | wc -l)
    if [[ $non_utf8_files -gt 0 ]]; then
        ((encoding_issues += non_utf8_files))
    fi

    if [[ $encoding_issues -eq 0 ]]; then
        log_success "文件编码规范检查通过 (所有文件均为UTF-8编码)"
        RESULTS+=("\"file_encoding\": {\"status\": \"PASS\", \"issues\": 0, \"files\": []}")
    else
        log_error "文件编码规范检查失败 (发现 $encoding_issues 个编码问题)"
        RESULTS+=("\"file_encoding\": {\"status\": \"FAIL\", \"issues\": $encoding_issues, \"files\": $(printf '[%s]' "$(printf '"%s",' "${issue_files[@]}" | sed 's/,$//')")}")
    fi
}

# 生成JSON报告
generate_json_report() {
    local json_report="{
  \"timestamp\": \"$(date -Iseconds)\",
  \"project_root\": \"$PROJECT_ROOT\",
  \"summary\": {
    \"total_checks\": $TOTAL_CHECKS,
    \"passed\": $PASSED_CHECKS,
    \"failed\": $FAILED_CHECKS,
    \"warnings\": $WARNING_CHECKS,
    \"pass_rate\": \"$(echo "scale=1; $PASSED_CHECKS * 100 / $TOTAL_CHECKS" | bc 2>/dev/null || echo "0")%\"
  },
  \"checks\": {
    $(IFS=','; echo "${RESULTS[*]}")
  }
}"

    echo "$json_report" > "$REPORT_FILE"
    log_info "JSON报告已生成: $REPORT_FILE"
}

# 生成HTML报告
generate_html_report() {
    local html_content="<!DOCTYPE html>
<html lang=\"zh-CN\">
<head>
    <meta charset=\"UTF-8\">
    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">
    <title>IOE-DREAM repowiki 合规性检查报告</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 20px; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; margin-bottom: 40px; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 40px; }
        .summary-item { background: #f8f9fa; padding: 20px; border-radius: 6px; text-align: center; }
        .summary-item h3 { margin: 0 0 10px 0; color: #333; }
        .summary-item .number { font-size: 2em; font-weight: bold; margin: 10px 0; }
        .pass { color: #28a745; }
        .fail { color: #dc3545; }
        .warning { color: #ffc107; }
        .check-section { margin: 30px 0; padding: 20px; border-left: 4px solid #007bff; background: #f8f9fa; }
        .check-section h2 { margin-top: 0; color: #007bff; }
        .status-pass { border-left-color: #28a745; }
        .status-fail { border-left-color: #dc3545; }
        .status-warning { border-left-color: #ffc107; }
        .file-list { background: white; padding: 15px; border-radius: 4px; margin-top: 10px; font-family: monospace; font-size: 0.9em; }
        .file-item { margin: 5px 0; color: #dc3545; }
        .timestamp { text-align: center; color: #666; margin-top: 40px; font-size: 0.9em; }
    </style>
</head>
<body>
    <div class=\"container\">
        <div class=\"header\">
            <h1>🛡️ IOE-DREAM repowiki 合规性检查报告</h1>
            <p>生成时间: $(date)</p>
        </div>

        <div class=\"summary\">
            <div class=\"summary-item\">
                <h3>总检查项</h3>
                <div class=\"number\">$TOTAL_CHECKS</div>
            </div>
            <div class=\"summary-item\">
                <h3>通过</h3>
                <div class=\"number pass\">$PASSED_CHECKS</div>
            </div>
            <div class=\"summary-item\">
                <h3>失败</h3>
                <div class=\"number fail\">$FAILED_CHECKS</div>
            </div>
            <div class=\"summary-item\">
                <h3>警告</h3>
                <div class=\"number warning\">$WARNING_CHECKS</div>
            </div>
            <div class=\"summary-item\">
                <h3>通过率</h3>
                <div class=\"number\">$((PASSED_CHECKS * 100 / TOTAL_CHECKS))%</div>
            </div>
        </div>

        <div class=\"check-details\">
"

    # 简化的检查详情展示
    echo "<h2>详细检查结果</h2>"
    echo "<p>详细的JSON报告请查看: <a href=\"$(basename $REPORT_FILE)\">JSON报告文件</a></p>"

    echo "</div>
        <div class=\"timestamp\">
            报告生成时间: $(date '+%Y-%m-%d %H:%M:%S')<br>
            检查项目路径: $PROJECT_ROOT
        </div>
    </div>
</body>
</html>"

    echo "$html_content" > "$HTML_REPORT"
    log_info "HTML报告已生成: $HTML_REPORT"
}

# 主函数
main() {
    log_info "开始执行 IOE-DREAM repowiki 综合合规性检查..."
    log_info "项目根目录: $PROJECT_ROOT"

    # 执行各项检查
    check_jakarta_compliance
    check_project_structure
    check_dao_naming
    check_dependency_injection
    check_coding_standards
    check_file_encoding

    # 生成报告
    log_header "生成合规性检查报告"
    generate_json_report
    generate_html_report

    # 输出总结
    log_header "合规性检查完成"
    log_info "总检查项: $TOTAL_CHECKS"
    log_success "通过: $PASSED_CHECKS"

    if [[ $FAILED_CHECKS -gt 0 ]]; then
        log_error "失败: $FAILED_CHECKS"
    fi

    if [[ $WARNING_CHECKS -gt 0 ]]; then
        log_warning "警告: $WARNING_CHECKS"
    fi

    local success_rate=0
    if [[ $TOTAL_CHECKS -gt 0 ]]; then
        success_rate=$((PASSED_CHECKS * 100 / TOTAL_CHECKS))
    fi
    log_info "合规性通过率: ${success_rate}%"

    # 返回适当的退出码
    if [[ $FAILED_CHECKS -gt 0 ]]; then
        exit 1
    else
        exit 0
    fi
}

# 执行主函数
main "$@"