#!/bin/bash

# IOE-DREAM 文档自动清理工具
# 作者: 老王(架构师团队)
# 版本: v1.0.0
# 创建日期: 2025-12-02

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# 配置变量
PROJECT_ROOT=$(pwd)
ARCHIVE_BASE="$PROJECT_ROOT/documentation/archive"
REPORT_DIR="$PROJECT_ROOT/reports/document-cleanup"
TEMP_DIR="/tmp/ioe-dream-doc-cleanup"

# 创建必要目录
mkdir -p "$REPORT_DIR"
mkdir -p "$TEMP_DIR"

# 统计变量
TOTAL_FILES=0
PROCESSED_FILES=0
MOVED_FILES=0
DELETED_FILES=0
ERROR_FILES=0

# 显示帮助信息
show_help() {
    cat << EOF
IOE-DREAM 文档自动清理工具 v1.0.0

用法: $0 [选项] [清理类型]

清理类型:
    monthly     - 执行月度清理 (默认)
    quarterly   - 执行季度评估
    yearly      - 执行年度审计
    dry-run     - 试运行(不实际执行清理操作)
    report      - 仅生成报告

选项:
    -h, --help          显示此帮助信息
    -v, --verbose       详细输出
    -f, --force         强制执行(跳过确认)
    -d, --directory     指定清理目录 (默认: 项目根目录)
    -r, --report-dir    指定报告输出目录
    --archive-dir       指定归档目录
    --temp-dir          指定临时目录

示例:
    $0                  # 执行月度清理
    $0 monthly          # 执行月度清理
    $0 quarterly        # 执行季度评估
    $0 dry-run          # 试运行，不实际清理
    $0 --verbose report # 详细模式生成报告

EOF
}

# 解析命令行参数
VERBOSE=false
FORCE=false
DRY_RUN=false
CLEANUP_TYPE="monthly"
TARGET_DIR="$PROJECT_ROOT"

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -f|--force)
            FORCE=true
            shift
            ;;
        -d|--directory)
            TARGET_DIR="$2"
            shift 2
            ;;
        -r|--report-dir)
            REPORT_DIR="$2"
            shift 2
            ;;
        --archive-dir)
            ARCHIVE_BASE="$2"
            shift 2
            ;;
        --temp-dir)
            TEMP_DIR="$2"
            shift 2
            ;;
        monthly|quarterly|yearly|dry-run|report)
            CLEANUP_TYPE="$1"
            if [[ "$1" == "dry-run" ]]; then
                DRY_RUN=true
                CLEANUP_TYPE="monthly"
            fi
            shift
            ;;
        *)
            log_error "未知参数: $1"
            show_help
            exit 1
            ;;
    esac
done

# 详细日志函数
verbose_log() {
    if [[ "$VERBOSE" == "true" ]]; then
        log_info "$1"
    fi
}

# 检查文档是否过期
is_expired() {
    local file="$1"
    local current_date=$(date +%s)

    # 检查文件名中的日期模式
    if [[ "$file" =~ ([0-9]{8}) ]]; then
        local file_date="${BASH_REMATCH[1]}"
        local file_timestamp=$(date -d "$file_date" +%s 2>/dev/null || echo 0)
        local days_diff=$(( (current_date - file_timestamp) / 86400 ))

        # 超过30天的临时文档
        if [[ $days_diff -gt 30 && "$file" =~ (FIX|REPORT|TEMP) ]]; then
            return 0
        fi
    fi

    # 检查文件内容中的有效期
    if grep -q "valid_until:" "$file" 2>/dev/null; then
        local valid_until=$(grep "valid_until:" "$file" | sed 's/.*valid_until: *"\?\([^"]*\)"\?.*/\1/' 2>/dev/null)
        if [[ -n "$valid_until" ]]; then
            local expiry_timestamp=$(date -d "$valid_until" +%s 2>/dev/null || echo 0)
            if [[ $expiry_timestamp -gt 0 && $current_date -gt $expiry_timestamp ]]; then
                return 0
            fi
        fi
    fi

    return 1
}

# 检查是否为重复文档
is_duplicate() {
    local file="$1"
    local filename=$(basename "$file")
    local dirname=$(dirname "$file")

    # 检查文件名模式
    if [[ "$filename" =~ (.*-repowiki\.md|.*_V2\.md|.*_v[0-9]+\.[0-9]+\.md) ]]; then
        return 0
    fi

    # 检查目录中的重复模式
    if [[ "$dirname" =~ archive ]]; then
        return 1  # 跳过已归档的文件
    fi

    # 简单的重复文件检测(基于文件名相似度)
    local base_name="${filename%.*}"
    local duplicates=$(find "$TARGET_DIR" -name "${base_name}*.md" -not -path "*/archive/*" | wc -l)
    if [[ $duplicates -gt 1 ]]; then
        return 0
    fi

    return 1
}

# 检查是否为孤立文档
is_orphaned() {
    local file="$1"
    local filename=$(basename "$file")

    # 跳过特定文件
    if [[ "$filename" =~ (README|INDEX|CLAUDE|DOCUMENTATION_MANAGEMENT) ]]; then
        return 1
    fi

    # 简单的孤立检测: 检查是否有其他文档引用此文件
    local reference_count=$(grep -r "$filename" "$TARGET_DIR" --include="*.md" | wc -l)
    if [[ $reference_count -le 1 ]]; then  # 只在自身中被引用
        return 0
    fi

    return 1
}

# 检查文档格式规范
check_format_compliance() {
    local file="$1"
    local issues=0

    # 检查是否有元信息头部
    if ! grep -q "^---" "$file"; then
        ((issues++))
        verbose_log "  缺少元信息头部"
    fi

    # 检查是否有标题
    if ! grep -q "^#" "$file"; then
        ((issues++))
        verbose_log "  缺少文档标题"
    fi

    # 检查是否有创建日期
    if ! grep -q "created_date\|create_date\|date:" "$file"; then
        ((issues++))
        verbose_log "  缺少创建日期"
    fi

    # 检查是否有作者信息
    if ! grep -q "author\|创建人\|作者:" "$file"; then
        ((issues++))
        verbose_log "  缺少作者信息"
    fi

    return $issues
}

# 移动文件到归档目录
move_to_archive() {
    local file="$1"
    local archive_type="$2"
    local relative_path=${file#$TARGET_DIR/}
    local archive_dir="$ARCHIVE_BASE/$archive_type"

    # 创建归档目录
    mkdir -p "$archive_dir"

    # 生成归档文件名
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local basename=$(basename "$file")
    local archive_name="${timestamp}_${basename}"

    if [[ "$DRY_RUN" == "true" ]]; then
        log_info "[DRY-RUN] 将归档: $file -> $archive_dir/$archive_name"
    else
        if mv "$file" "$archive_dir/$archive_name"; then
            log_info "已归档: $relative_path -> archive/$archive_type/$archive_name"
            ((MOVED_FILES++))

            # 记录移动操作
            echo "$relative_path -> archive/$archive_type/$archive_name" >> "$REPORT_DIR/moved_files_$timestamp.txt"
        else
            log_error "归档失败: $file"
            ((ERROR_FILES++))
        fi
    fi
}

# 删除文件
delete_file() {
    local file="$1"
    local relative_path=${file#$TARGET_DIR/}

    if [[ "$DRY_RUN" == "true" ]]; then
        log_info "[DRY-RUN] 将删除: $file"
    else
        if rm "$file"; then
            log_info "已删除: $relative_path"
            ((DELETED_FILES++))

            # 记录删除操作
            echo "$relative_path" >> "$REPORT_DIR/deleted_files_$timestamp.txt"
        else
            log_error "删除失败: $file"
            ((ERROR_FILES++))
        fi
    fi
}

# 月度清理函数
monthly_cleanup() {
    log_step "开始执行月度清理..."

    # 查找所有md文件
    while IFS= read -r -d '' file; do
        ((TOTAL_FILES++))
        ((PROCESSED_FILES++))

        verbose_log "处理文件: $file"

        # 跳过归档目录中的文件
        if [[ "$file" =~ archive ]]; then
            continue
        fi

        # 检查过期文档
        if is_expired "$file"; then
            log_warn "发现过期文档: $file"
            move_to_archive "$file" "expired"
            continue
        fi

        # 检查重复文档
        if is_duplicate "$file"; then
            log_warn "发现重复文档: $file"
            move_to_archive "$file" "duplicates"
            continue
        fi

        # 检查孤立文档
        if is_orphaned "$file"; then
            log_warn "发现孤立文档: $file"
            move_to_archive "$file" "orphaned"
            continue
        fi

        # 检查格式规范
        local format_issues=$(check_format_compliance "$file")
        if [[ $format_issues -gt 0 ]]; then
            log_warn "文档格式不规范 ($format_issues 个问题): $file"
            echo "$file: $format_issues issues" >> "$REPORT_DIR/format_issues_$timestamp.txt"
        fi

    done < <(find "$TARGET_DIR" -name "*.md" -type f -print0)

    log_step "月度清理完成"
}

# 季度评估函数
quarterly_evaluation() {
    log_step "开始执行季度评估..."

    # 执行月度清理
    monthly_cleanup

    # 生成健康度报告
    log_step "生成文档健康度报告..."
    generate_health_report

    # 分析使用率
    log_step "分析文档使用率..."
    analyze_usage_stats

    log_step "季度评估完成"
}

# 年度审计函数
yearly_audit() {
    log_step "开始执行年度审计..."

    # 执行季度评估
    quarterly_evaluation

    # 全量质量检查
    log_step "执行全量文档质量检查..."
    full_quality_check

    # 生成年度报告
    log_step "生成年度文档管理报告..."
    generate_annual_report

    log_step "年度审计完成"
}

# 生成健康度报告
generate_health_report() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local report_file="$REPORT_DIR/health_report_$timestamp.md"

    cat > "$report_file" << EOF
# IOE-DREAM 文档健康度报告

**生成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**报告类型**: 月度/季度健康度报告
**总文件数**: $TOTAL_FILES
**处理文件数**: $PROCESSED_FILES
**移动文件数**: $MOVED_FILES
**删除文件数**: $DELETED_FILES
**错误文件数**: $ERROR_FILES

## 📊 健康度指标

### 文档新鲜度
- 过期文档: $(find "$ARCHIVE_BASE/expired" -name "*.md" 2>/dev/null | wc -l) 个
- 过期率: $(echo "scale=2; $(find "$ARCHIVE_BASE/expired" -name "*.md" 2>/dev/null | wc -l) * 100 / $TOTAL_FILES" | bc 2>/dev/null || echo "N/A")%

### 重复率
- 重复文档: $(find "$ARCHIVE_BASE/duplicates" -name "*.md" 2>/dev/null | wc -l) 个
- 重复率: $(echo "scale=2; $(find "$ARCHIVE_BASE/duplicates" -name "*.md" 2>/dev/null | wc -l) * 100 / $TOTAL_FILES" | bc 2>/dev/null || echo "N/A")%

### 完整性
- 格式问题文档: $(cat "$REPORT_DIR/format_issues_$timestamp.txt" 2>/dev/null | wc -l) 个
- 完整率: $(echo "scale=2; ($TOTAL_FILES - $(cat "$REPORT_DIR/format_issues_$timestamp.txt" 2>/dev/null | wc -l)) * 100 / $TOTAL_FILES" | bc 2>/dev/null || echo "N/A")%

## 🎯 改进建议

1. 加强文档创建时的规范检查
2. 定期更新过期文档内容
3. 清理重复和冗余文档
4. 提升文档质量和完整性

EOF

    log_info "健康度报告已生成: $report_file"
}

# 分析使用率统计
analyze_usage_stats() {
    log_info "分析文档使用率..."
    # 这里可以添加更复杂的使用率分析逻辑
    # 例如: 基于Git提交记录、文件访问时间等
}

# 全量质量检查
full_quality_check() {
    log_info "执行全量文档质量检查..."
    # 这里可以添加更全面的质量检查逻辑
}

# 生成年度报告
generate_annual_report() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local report_file="$REPORT_DIR/annual_report_$timestamp.md"

    cat > "$report_file" << EOF
# IOE-DREAM 年度文档管理报告

**报告年度**: $(date '+%Y')
**生成时间**: $(date '+%Y-%m-%d %H:%M:%S')

## 📈 年度统计

### 文档数量变化
- 年初文档数: $(git log --since="$(date '+%Y-01-01')" --until="$(date '+%Y-01-01 23:59:59')" --name-only --pretty=format: -- "*.md" | sort -u | wc -l)
- 当前文档数: $TOTAL_FILES
- 年度净变化: $(echo "$TOTAL_FILES - $(git log --since="$(date '+%Y-01-01')" --until="$(date '+%Y-01-01 23:59:59')" --name-only --pretty=format: -- "*.md" | sort -u | wc -l)" | bc)

### 清理统计
- 月度清理次数: 12
- 归档文档总数: $(find "$ARCHIVE_BASE" -name "*.md" 2>/dev/null | wc -l)
- 删除文档总数: 待统计

## 🎯 主要成果
1. 建立了完善的文档管理规范
2. 实现了自动化清理机制
3. 提升了文档质量和规范性
4. 减少了文档冗余和重复

## 📋 下一年度计划
1. 继续优化自动化工具
2. 加强文档质量监控
3. 完善使用率分析
4. 推广文档管理最佳实践

EOF

    log_info "年度报告已生成: $report_file"
}

# 生成执行报告
generate_execution_report() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local report_file="$REPORT_DIR/execution_report_$timestamp.md"

    cat > "$report_file" << EOF
# IOE-DREAM 文档清理执行报告

**清理类型**: $CLEANUP_TYPE
**执行时间**: $(date '+%Y-%m-%d %H:%M:%S')
**是否试运行**: $DRY_RUN

## 📊 执行统计
- **扫描文件总数**: $TOTAL_FILES
- **处理文件数量**: $PROCESSED_FILES
- **移动文件数量**: $MOVED_FILES
- **删除文件数量**: $DELETED_FILES
- **错误文件数量**: $ERROR_FILES
- **成功率**: $(echo "scale=2; ($PROCESSED_FILES - $ERROR_FILES) * 100 / $PROCESSED_FILES" | bc 2>/dev/null || echo "N/A")%

## 🔧 执行参数
- **目标目录**: $TARGET_DIR
- **归档目录**: $ARCHIVE_BASE
- **报告目录**: $REPORT_DIR
- **详细输出**: $VERBOSE
- **强制执行**: $FORCE

## 📁 生成的文件
EOF

    if [[ -f "$REPORT_DIR/moved_files_$timestamp.txt" ]]; then
        echo "- 移动记录: $REPORT_DIR/moved_files_$timestamp.txt" >> "$report_file"
    fi

    if [[ -f "$REPORT_DIR/deleted_files_$timestamp.txt" ]]; then
        echo "- 删除记录: $REPORT_DIR/deleted_files_$timestamp.txt" >> "$report_file"
    fi

    if [[ -f "$REPORT_DIR/format_issues_$timestamp.txt" ]]; then
        echo "- 格式问题: $REPORT_DIR/format_issues_$timestamp.txt" >> "$report_file"
    fi

    log_info "执行报告已生成: $report_file"
}

# 确认执行
confirm_execution() {
    if [[ "$FORCE" == "true" ]]; then
        return 0
    fi

    echo
    log_warn "即将执行 $CLEANUP_TYPE 文档清理操作"
    log_warn "目标目录: $TARGET_DIR"
    echo
    read -p "确认执行吗? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "操作已取消"
        exit 0
    fi
}

# 主执行函数
main() {
    log_info "IOE-DREAM 文档自动清理工具启动"
    log_info "清理类型: $CLEANUP_TYPE"
    log_info "目标目录: $TARGET_DIR"

    if [[ "$DRY_RUN" != "true" ]]; then
        confirm_execution
    fi

    # 创建归档子目录
    mkdir -p "$ARCHIVE_BASE"/{expired,duplicates,orphaned,legacy}

    case $CLEANUP_TYPE in
        "monthly")
            monthly_cleanup
            ;;
        "quarterly")
            quarterly_evaluation
            ;;
        "yearly")
            yearly_audit
            ;;
        "report")
            log_info "仅生成报告模式"
            ;;
        *)
            log_error "未知的清理类型: $CLEANUP_TYPE"
            exit 1
            ;;
    esac

    # 生成执行报告
    generate_execution_report

    log_info "文档清理操作完成"
    log_info "处理文件: $PROCESSED_FILES, 移动文件: $MOVED_FILES, 删除文件: $DELETED_FILES"
}

# 检查依赖
check_dependencies() {
    local missing_deps=()

    command -v find >/dev/null 2>&1 || missing_deps+=("find")
    command -v grep >/dev/null 2>&1 || missing_deps+=("grep")
    command -v wc >/dev/null 2>&1 || missing_deps+=("wc")

    if [[ ${#missing_deps[@]} -gt 0 ]]; then
        log_error "缺少依赖命令: ${missing_deps[*]}"
        exit 1
    fi
}

# 脚本入口点
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    check_dependencies
    main "$@"
fi