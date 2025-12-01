#!/bin/bash

# 🔒 安全文件删除脚本 - 双重验证流程
# 基于文件重要性分级机制的安全删除系统
# 创建时间: 2025-11-25 23:05:00

set -euo pipefail

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_critical() {
    echo -e "${PURPLE}[CRITICAL]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# 第一步: 自动化工具识别
identify_file_risk_level() {
    local file_path="$1"
    local file_name=$(basename "$file_path")

    log_info "开始识别文件风险等级: $file_path"

    # 标准化路径处理
    if [[ "$file_path" != /* ]]; then
        file_path="$(pwd)/$file_path"
    fi

    # P0级文件检测 - 绝对禁止删除
    if [[ "$file_path" == *"AGENTS.md"* ]] ||
       [[ "$file_path" == *"CLAUDE.md"* ]] ||
       [[ "$file_path" == *"QUICK_REFERENCE.md"* ]] ||
       [[ "$file_name" == *"BaseEntity.java"* ]] ||
       [[ "$file_path" == *".claude/skills/"* ]] ||
       [[ "$file_path" == *".qoder/"* ]] ||
       [[ "$file_path" == *"pom.xml"* ]] ||
       [[ "$file_path" == *"sa-base/pom.xml"* ]] ||
       [[ "$file_path" == *"sa-admin/pom.xml"* ]]; then
        echo "P0"
        return 0
    fi

    # P1级文件检测 - 重要业务文件，需要验证
    if [[ "$file_name" == *Entity.java ]] ||
       [[ "$file_name" == *ServiceImpl.java ]] ||
       [[ "$file_name" == *Manager.java ]] ||
       [[ "$file_name" == *Dao.java ]] ||
       [[ "$file_name" == *Controller.java ]] ||
       [[ "$file_path" == *"src/main/java/"* ]] ||
       [[ "$file_path" == *".sql"* ]] ||
       [[ "$file_path" == *".yaml"* ]] ||
       [[ "$file_path" == *".properties"* ]] ||
       [[ "$file_path" == *"docs/"* ]] ||
       [[ "$file_path" == *"README.md"* ]]; then
        echo "P1"
        return 1
    fi

    # P2级文件检测 - 一般文件，可安全删除
    if [[ "$file_name" == *.log ]] ||
       [[ "$file_name" == *.tmp ]] ||
       [[ "$file_name" == *.cache ]] ||
       [[ "$file_name" == *.backup ]] ||
       [[ "$file_name" == *.utf8 ]] ||
       [[ "$file_path" == *"target/"* ]] ||
       [[ "$file_path" == *"build/"* ]] ||
       [[ "$file_path" == *".idea/"* ]] ||
       [[ "$file_path" == *".vscode/"* ]] ||
       [[ "$file_path" == *"node_modules/"* ]]; then
        echo "P2"
        return 2
    fi

    # 未知类型文件
    echo "UNKNOWN"
    return 3
}

# 第二步: 人工重要性确认
manual_importance_confirmation() {
    local file_path="$1"
    local risk_level=$(identify_file_risk_level "$file_path")

    log_warn "开始人工重要性确认流程..."

    case $risk_level in
        "P0")
            log_error "❌ 拒绝删除: P0级核心文件 - $file_path"
            echo "🛡️ P0级文件保护: 此文件对项目运行至关重要，绝对禁止删除"
            return 1
            ;;
        "P1")
            log_warn "⚠️ 警告: P1级重要文件 - $file_path"
            echo "请确认此文件的重要性:"
            echo "  1. 检查是否有其他文件引用此文件"
            echo "  2. 确认是否有替代方案"
            echo "  3. 评估删除可能造成的影响"
            echo ""
            read -p "🤔 您是否已充分验证此文件可以安全删除? (输入 'CONFIRM' 确认): " choice

            if [[ "$choice" != "CONFIRM" ]]; then
                echo "❌ 操作已取消 - 用户未确认删除"
                return 1
            fi

            # 再次确认
            echo ""
            echo "🚨 最后确认机会"
            echo "文件: $file_path"
            echo "类型: P1级重要文件"
            echo "删除此文件可能影响项目功能"
            read -p "💀 确定要删除吗? (输入 'DELETE' 执行删除): " final_choice

            if [[ "$final_choice" != "DELETE" ]]; then
                echo "❌ 操作已取消 - 用户最终确认失败"
                return 1
            fi
            ;;
        "P2")
            log_success "✅ 安全: P2级一般文件 - $file_path"
            echo "此类文件可以安全删除，但仍建议创建备份"
            return 0
            ;;
        *)
            log_warn "🔍 未知: 需要进一步分析 - $file_path"
            echo "文件类型未知，建议仔细检查后再决定"

            # 检查文件基本信息
            if [[ -f "$file_path" ]]; then
                echo "文件信息:"
                echo "  大小: $(du -h "$file_path" | cut -f1)"
                echo "  类型: $(file "$file_path")"
                echo "  修改时间: $(stat -c %y "$file_path")"
            fi

            read -p "🤔 请手动判断此文件是否可以删除? (yes/no): " manual_choice

            if [[ "$manual_choice" != "yes" ]] && [[ "$manual_choice" != "y" ]]; then
                echo "❌ 操作已取消 - 用户手动判断为不可删除"
                return 1
            fi
            ;;
    esac

    return 0
}

# 第三步: 影响范围评估
impact_assessment() {
    local file_path="$1"

    log_info "🔍 正在评估删除 $file_path 的影响范围..."

    if [[ ! -f "$file_path" ]] && [[ ! -d "$file_path" ]]; then
        log_warn "⚠️ 文件不存在，跳过影响评估"
        return 0
    fi

    echo "📋 影响范围评估报告:"

    # 检查文件引用（仅对源代码文件）
    if [[ "$file_path" == *.java ]] || [[ "$file_path" == *.xml ]] || [[ "$file_path" == *.yaml ]]; then
        echo "🔍 检查文件引用..."
        local file_name=$(basename "$file_path" | sed 's/\.[^.]*$//')

        # 查找Java文件中的引用
        local java_refs=$(find . -name "*.java" -not -path "*/target/*" -not -path "*/.git/*" -exec grep -l "$file_name" {} \; 2>/dev/null | head -5)
        if [[ -n "$java_refs" ]]; then
            echo "  ⚠️ 发现Java文件引用:"
            echo "$java_refs" | sed 's/^/    /'
        fi

        # 查找XML配置文件中的引用
        local xml_refs=$(find . -name "*.xml" -not -path "*/target/*" -not -path "*/.git/*" -exec grep -l "$file_name" {} \; 2>/dev/null | head -3)
        if [[ -n "$xml_refs" ]]; then
            echo "  ⚠️ 发现XML配置引用:"
            echo "$xml_refs" | sed 's/^/    /'
        fi
    fi

    # 检查Git状态
    if git rev-parse --git-dir > /dev/null 2>&1; then
        echo "📊 Git状态检查..."

        if git status --porcelain "$file_path" 2>/dev/null | grep -q "^M "; then
            echo "  ⚠️ 警告: 文件有未提交的修改"
        elif git status --porcelain "$file_path" 2>/dev/null | grep -q "^?? "; then
            echo "  ℹ️ 信息: 文件是新增的，尚未提交"
        else
            echo "  ✅ 文件状态正常，已纳入版本控制"
        fi

        # 检查文件历史
        local commit_count=$(git log --oneline --follow "$file_path" 2>/dev/null | wc -l)
        if [[ $commit_count -gt 0 ]]; then
            echo "  📈 文件变更历史: $commit_count 次提交"
        fi
    fi

    # 检查文件大小和重要性
    if [[ -f "$file_path" ]]; then
        local file_size=$(stat -c%s "$file_path")
        echo "📏 文件属性:"
        echo "  大小: $file_size 字节"

        if [[ $file_size -gt 1048576 ]]; then  # 1MB
            echo "  ⚠️ 大文件警告: 文件超过1MB，删除前请确认"
        fi
    fi

    # 检查可执行文件
    if [[ -x "$file_path" ]]; then
        echo "  ⚠️ 可执行文件: 删除前请确认不影响构建或部署"
    fi

    echo "✅ 影响范围评估完成"
}

# 第四步: 备份机制激活
activate_backup() {
    local file_path="$1"
    local backup_dir="backup/auto-backup-$(date +%Y%m%d-%H%M%S)"

    log_info "💾 创建安全备份..."

    # 创建备份目录
    mkdir -p "$backup_dir"

    if [[ -f "$file_path" ]]; then
        # 备份文件，保持相对路径结构
        local relative_path=$(realpath --relative-to="$(pwd)" "$file_path")
        local backup_path="$backup_dir/$relative_path"
        local backup_file_dir=$(dirname "$backup_path")

        mkdir -p "$backup_file_dir"
        cp --parents "$file_path" "$backup_dir/"

        local file_size=$(stat -c%s "$file_path")
        echo "✅ 文件已备份: $relative_path ($file_size 字节) → $backup_path"

        # 创建备份元数据
        local metadata_file="$backup_dir/backup-metadata.json"
        cat > "$metadata_file" << EOF
{
  "backup_time": "$(date -Iseconds)",
  "original_path": "$file_path",
  "relative_path": "$relative_path",
  "backup_path": "$backup_path",
  "file_size": $file_size,
  "operation": "delete",
  "reason": "安全删除备份",
  "git_commit": "$(git rev-parse HEAD 2>/dev/null || echo 'N/A')"
}
EOF

        echo "📄 备份元数据已创建: $metadata_file"
    fi

    # Git备份（如果可用）
    if git rev-parse --git-dir > /dev/null 2>&1; then
        log_info "📝 创建Git备份..."

        # 添加文件到暂存区（如果存在）
        if [[ -f "$file_path" ]]; then
            git add "$file_path" 2>/dev/null || true
        fi

        # 创建备份提交
        local backup_msg="Auto backup before deletion: $(basename "$file_path")"
        if git commit -m "$backup_msg" 2>/dev/null; then
            echo "✅ Git备份提交成功: $backup_msg"
        else
            echo "ℹ️ Git备份提交失败（可能没有变更需要提交），但文件已本地备份"
        fi
    fi

    echo "🎯 备份完成位置: $backup_dir"
}

# 第五步: 执行删除
execute_deletion() {
    local file_path="$1"

    log_info "🗑️ 执行删除操作..."

    if [[ -f "$file_path" ]]; then
        local file_size=$(stat -c%s "$file_path")
        rm -f "$file_path"
        echo "✅ 文件已删除: $(basename "$file_path") ($file_size 字节)"

    elif [[ -d "$file_path" ]]; then
        local dir_size=$(du -sb "$file_path" | cut -f1)
        rm -rf "$file_path"
        echo "✅ 目录已删除: $(basename "$file_path") ($dir_size 字节)"

    else
        log_warn "⚠️ 文件/目录不存在，无需删除: $file_path"
        return 0
    fi

    # 同步文件系统（确保删除立即生效）
    sync 2>/dev/null || true
}

# 第六步: 删除后验证
post_delete_verification() {
    local file_path="$1"

    log_info "🔍 执行删除后验证..."

    if [[ ! -e "$file_path" ]]; then
        log_success "✅ 删除验证通过: 文件已成功删除"
        return 0
    else
        log_error "❌ 删除验证失败: 文件仍然存在"
        return 1
    fi
}

# 完整的安全文件删除流程
safe_delete_file() {
    local file_path="$1"

    echo "🔒 开始安全文件删除流程: $file_path"
    echo "$(date '+%Y-%m-%d %H:%M:%S') - 开始删除流程" >> "safe-delete.log"

    # 参数验证
    if [[ -z "$file_path" ]]; then
        log_error "❌ 错误: 未提供文件路径"
        return 1
    fi

    # 第一步: 自动化风险识别
    echo ""
    log_info "🔍 第一步: 自动化风险识别"
    local risk_level=$(identify_file_risk_level "$file_path")
    echo "📊 风险等级: P$risk_level"

    # 第二步: 人工重要性确认
    echo ""
    log_info "👥 第二步: 人工重要性确认"
    if ! manual_importance_confirmation "$file_path"; then
        log_error "❌ 人工确认失败，删除操作已取消"
        echo "$(date '+%Y-%m-%d %H:%M:%S') - 删除取消: 人工确认失败 - $file_path" >> "safe-delete.log"
        return 1
    fi

    # 第三步: 影响范围评估
    echo ""
    log_info "🎯 第三步: 影响范围评估"
    impact_assessment "$file_path"

    # 第四步: 激活备份机制
    echo ""
    log_info "💾 第四步: 备份机制激活"
    activate_backup "$file_path"

    # 最终确认
    echo ""
    log_info "🎯 最终确认"
    read -p "💀 所有验证已完成，确定执行删除操作吗? (yes/no): " final_confirm

    if [[ "$final_confirm" != "yes" ]] && [[ "$final_confirm" != "y" ]]; then
        log_warn "⚠️ 用户取消删除操作"
        echo "$(date '+%Y-%m-%d %H:%M:%S') - 删除取消: 用户最终确认失败 - $file_path" >> "safe-delete.log"
        return 1
    fi

    # 第五步: 执行删除
    echo ""
    log_info "🗑️ 第五步: 执行删除操作"
    execute_deletion "$file_path"

    # 第六步: 删除后验证
    echo ""
    log_info "🔍 第六步: 删除后验证"
    if ! post_delete_verification "$file_path"; then
        log_error "❌ 删除后验证失败"
        echo "$(date '+%Y-%m-%d %H:%M:%S') - 删除失败: 验证失败 - $file_path" >> "safe-delete.log"
        return 1
    fi

    # 记录成功删除
    echo "$(date '+%Y-%m-%d %H:%M:%S') - 删除成功: $file_path" >> "safe-delete.log"
    log_success "🎉 安全文件删除流程完成: $file_path"

    return 0
}

# 批量安全删除（增强版）
batch_safe_delete() {
    local file_list="$1"

    if [[ ! -f "$file_list" ]]; then
        log_error "❌ 文件列表不存在: $file_list"
        return 1
    fi

    local total_files=$(wc -l < "$file_list")
    log_info "📋 开始批量安全删除，共 $total_files 个文件"

    local success_count=0
    local failure_count=0
    local cancelled_count=0

    while IFS= read -r file_path; do
        # 跳过空行和注释
        if [[ -z "$file_path" ]] || [[ "$file_path" == \#* ]]; then
            continue
        fi

        echo ""
        log_info "📁 处理文件: $file_path"

        if safe_delete_file "$file_path"; then
            ((success_count++))
        else
            local exit_code=$?
            if [[ $exit_code -eq 1 ]]; then
                ((cancelled_count++))
            else
                ((failure_count++))
            fi
        fi

        # 询问是否继续
        if [[ $success_count -gt 0 ]] && [[ $(($success_count + $failure_count + $cancelled_count)) -lt $total_files ]]; then
            read -p "📋 是否继续处理下一个文件? (yes/no): " continue_choice
            if [[ "$continue_choice" != "yes" ]] && [[ "$continue_choice" != "y" ]]; then
                log_warn "⚠️ 用户选择停止批量删除"
                break
            fi
        fi
    done < "$file_list"

    echo ""
    log_info "📊 批量删除统计:"
    echo "  成功删除: $success_count 个"
    echo "  删除失败: $failure_count 个"
    echo "  用户取消: $cancelled_count 个"
    echo "  总计处理: $(($success_count + $failure_count + $cancelled_count)) 个"

    if [[ $failure_count -eq 0 ]] && [[ $cancelled_count -eq 0 ]]; then
        log_success "🎉 批量删除全部成功完成"
        return 0
    else
        log_warn "⚠️ 批量删除部分完成，请检查失败的文件"
        return 1
    fi
}

# 主函数
main() {
    echo "🛡️ IOE-DREAM 项目安全文件删除系统"
    echo "📅 版本: v1.0.0 (2025-11-25)"
    echo "🎯 功能: 基于文件重要性分级的安全删除，双重验证保护"
    echo ""

    # 检查参数
    if [[ $# -eq 0 ]]; then
        echo "📖 使用方法:"
        echo "  $0 <文件路径>                    # 删除单个文件"
        echo "  $0 --batch <文件列表文件>          # 批量删除文件"
        echo "  $0 --help                       # 显示帮助信息"
        echo ""
        echo "📋 示例:"
        echo "  $0 './temp.log'                 # 删除临时日志文件"
        echo "  $0 --batch delete-list.txt      # 批量删除"
        echo ""
        return 1
    fi

    case "$1" in
        --help|-h)
            echo "🛡️ 安全文件删除系统帮助信息"
            echo ""
            echo "🎯 核心特性:"
            echo "  • P0级文件: 绝对禁止删除 (AGENTS.md, CLAUDE.md 等)"
            echo "  • P1级文件: 需要人工验证 (Java源码, 配置文件等)"
            echo "  • P2级文件: 可安全删除 (临时文件, 缓存文件等)"
            echo "  • 双重验证: 自动识别 + 人工确认"
            echo "  • 自动备份: Git备份 + 本地备份"
            echo ""
            echo "🔧 文件类型示例:"
            echo "  P0级: AGENTS.md, CLAUDE.md, QUICK_REFERENCE.md, pom.xml"
            echo "  P1级: *.java, *.sql, *.yaml, docs/*.md"
            echo "  P2级: *.log, *.tmp, *.cache, target/, node_modules/"
            echo ""
            echo "⚠️ 安全提醒:"
            echo "  • 每次删除都会创建完整备份"
            echo "  • P0级文件无法删除，保护AI开发系统"
            echo "  • 所有操作都会记录到 safe-delete.log"
            ;;
        --batch)
            if [[ $# -lt 2 ]]; then
                log_error "❌ 批量删除需要提供文件列表"
                return 1
            fi
            batch_safe_delete "$2"
            ;;
        *)
            safe_delete_file "$1"
            ;;
    esac
}

# 启动脚本
main "$@"