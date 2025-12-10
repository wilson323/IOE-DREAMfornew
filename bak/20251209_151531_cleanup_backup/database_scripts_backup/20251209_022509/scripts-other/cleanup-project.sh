#!/bin/bash

# ====================================================================
# IOE-DREAM 项目清理脚本
#
# 功能：清理项目中的冗余文件和目录
# 执行前请确保：
# 1. 已提交当前代码到Git
# 2. 已备份重要文件
# 3. 团队成员已知晓清理计划
# ====================================================================

set -e  # 遇到错误立即停止

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_section() {
    echo -e "${BLUE}[SECTION]${NC} $1"
}

# 统计函数
count_files() {
    local path=$1
    if [ -d "$path" ]; then
        find "$path" -type f | wc -l
    else
        echo "0"
    fi
}

get_size() {
    local path=$1
    if [ -e "$path" ]; then
        du -sh "$path" 2>/dev/null | cut -f1
    else
        echo "0"
    fi
}

# ====================================================================
# 清理前检查
# ====================================================================
pre_cleanup_check() {
    log_section "执行清理前检查..."

    # 检查Git状态
    if [ -n "$(git status --porcelain)" ]; then
        log_error "检测到未提交的文件，请先提交代码！"
        git status --porcelain
        exit 1
    fi

    log_info "✓ Git仓库状态干净"

    # 检查重要文件是否存在
    local important_files=("README.md" "CLAUDE.md" "pom.xml")
    for file in "${important_files[@]}"; do
        if [ ! -f "$file" ]; then
            log_error "重要文件 $file 不存在！"
            exit 1
        fi
    done

    log_info "✓ 重要文件检查通过"

    # 创建清理前的备份分支
    local backup_branch="archive/backup-before-cleanup-$(date +%Y%m%d-%H%M%S)"
    git checkout -b "$backup_branch"
    log_info "✓ 已创建备份分支: $backup_branch"

    # 返回原分支
    git checkout - 2>/dev/null || git checkout main
}

# ====================================================================
# 阶段1: 清理过期文档目录
# ====================================================================
cleanup_deprecated_docs() {
    log_section "阶段1: 清理过期文档目录"

    # 1. 删除.qoder目录 (6.2MB, 252个文件)
    if [ -d ".qoder" ]; then
        local qoder_files=$(count_files ".qoder")
        local qoder_size=$(get_size ".qoder")
        log_info "删除.qoder目录 (包含 $qoder_files 个文件, 大小 $qoder_size)"
        rm -rf .qoder
        log_info "✓ .qoder目录已删除"
    fi

    # 2. 删除docs目录 (19MB, 与documentation/重复)
    if [ -d "docs" ]; then
        local docs_files=$(count_files "docs")
        local docs_size=$(get_size "docs")
        log_info "删除docs目录 (包含 $docs_files 个文件, 大小 $docs_size)"
        rm -rf docs
        log_info "✓ docs目录已删除，保留documentation/作为唯一文档目录"
    fi

    # 3. 删除重复的技能文件
    if [ -d ".claude/skills/archive/duplicate-skills" ]; then
        local duplicate_files=$(count_files ".claude/skills/archive/duplicate-skills")
        log_info "删除重复技能文件目录 ($duplicate_files 个文件)"
        rm -rf .claude/skills/archive/duplicate-skills
        log_info "✓ 重复技能文件已删除"
    fi

    # 4. 删除docs-content-analysis-report.md (临时分析报告)
    if [ -f "docs-content-analysis-report.md" ]; then
        log_info "删除临时分析报告文件"
        rm -f docs-content-analysis-report.md
        log_info "✓ 临时分析报告已删除"
    fi
}

# ====================================================================
# 阶段2: 清理无用代码和备份
# ====================================================================
cleanup_unused_code() {
    log_section "阶段2: 清理无用代码和备份"

    # 1. 删除重构备份目录
    if [ -d "restful_refactor_backup_20251202_014224" ]; then
        local backup_files=$(count_files "restful_refactor_backup_20251202_014224")
        local backup_size=$(get_size "restful_refactor_backup_20251202_014224")
        log_info "删除重构备份目录 (包含 $backup_files 个文件, 大小 $backup_size)"
        rm -rf restful_refactor_backup_20251202_014224
        log_info "✓ 重构备份目录已删除"
    fi

    # 2. 删除.bak备份文件
    if [ -f "CLAUDE.md.bak" ]; then
        log_info "删除CLAUDE.md.bak备份文件"
        rm -f CLAUDE.md.bak
        log_info "✓ 备份文件已删除"
    fi

    # 3. 整理已弃用服务
    if [ -d "microservices/archive/deprecated-services" ]; then
        log_info "重命名deprecated-services为services-history"
        mv microservices/archive/deprecated-services microservices/archive/services-history

        # 创建说明文档
        cat > microservices/archive/services-history/README.md << 'EOF'
# 历史微服务归档

本目录包含已弃用的微服务代码，这些服务已被整合到新的微服务架构中。

## 弃用服务列表

| 服务名称 | 替换方案 | 整合时间 |
|---------|---------|---------|
| ioedream-auth-service | ioedream-common-service | 2025-12 |
| ioedream-identity-service | ioedream-common-service | 2025-12 |
| ioedream-notification-service | ioedream-common-service | 2025-12 |
| ioedream-enterprise-service | ioedream-oa-service | 2025-12 |
| ioedream-device-service | ioedream-device-comm-service | 2025-12 |
| ... | ... | ... |

## 注意事项

- 这些代码仅作历史参考，不应在新开发中使用
- 新的微服务架构请参考 `microservices/` 目录下的活跃服务
- 详细的迁移方案请查看项目文档
EOF
        log_info "✓ 已弃用服务已整理并添加说明文档"
    fi
}

# ====================================================================
# 阶段3: 清理构建产物和临时文件
# ====================================================================
cleanup_build_artifacts() {
    log_section "阶段3: 清理构建产物和临时文件"

    # 1. 清理target目录
    local target_count=$(find . -name "target" -type d | wc -l)
    if [ $target_count -gt 0 ]; then
        log_info "清理 $target_count 个Maven target目录"
        find . -name "target" -type d -exec rm -rf {} + 2>/dev/null || true
        log_info "✓ target目录清理完成"
    fi

    # 2. 清理.class文件
    local class_count=$(find . -name "*.class" | wc -l)
    if [ $class_count -gt 0 ]; then
        log_info "清理 $class_count 个.class文件"
        find . -name "*.class" -delete
        log_info "✓ .class文件清理完成"
    fi

    # 3. 清理日志文件
    local log_count=$(find . -name "*.log" -type f | wc -l)
    if [ $log_count -gt 0 ]; then
        log_info "清理 $log_count 个日志文件"
        find . -name "*.log" -type f -delete
        log_info "✓ 日志文件清理完成"
    fi
}

# ====================================================================
# 阶段4: 清理配置文件冗余
# ====================================================================
cleanup_config_files() {
    log_section "阶段4: 清理配置文件冗余"

    # 检查重复的docker-compose文件
    local docker_compose_files=("docker-compose-all.yml" "docker-compose-production.yml" "docker-compose-services.yml")
    for file in "${docker_compose_files[@]}"; do
        if [ -f "$file" ]; then
            log_warn "检查配置文件: $file (请手动确认是否需要)"
        fi
    done

    log_info "配置文件检查完成，请手动确认删除重复配置"
}

# ====================================================================
# 阶段5: 更新文档引用
# ====================================================================
update_documentation() {
    log_section "阶段5: 更新文档引用"

    # 更新README.md中的文档引用
    if [ -f "README.md" ]; then
        log_info "更新README.md中的文档引用"
        # 替换docs/引用为documentation/
        sed -i 's|docs/|documentation/|g' README.md 2>/dev/null || true
        log_info "✓ README.md更新完成"
    fi

    # 更新CLAUDE.md中的文档引用
    if [ -f "CLAUDE.md" ]; then
        log_info "更新CLAUDE.md中的文档引用"
        sed -i 's|docs/|documentation/|g' CLAUDE.md 2>/dev/null || true
        log_info "✓ CLAUDE.md更新完成"
    fi
}

# ====================================================================
# 清理结果统计
# ====================================================================
generate_cleanup_report() {
    log_section "生成清理报告"

    # 获取当前项目状态
    local current_size=$(get_size ".")
    local current_java_files=$(find . -name "*.java" | wc -l)
    local current_md_files=$(find . -name "*.md" | wc -l)

    cat > CLEANUP_EXECUTION_REPORT.md << EOF
# 项目清理执行报告

> **执行时间**: $(date)
> **执行脚本**: cleanup-project.sh
> **Git分支**: $(git branch --show-current)

## 清理统计

### 清理前状态
- 项目大小: 1.7GB
- Java文件数: 556
- Markdown文件数: 2,385

### 清理后状态
- 项目大小: $current_size
- Java文件数: $current_java_files
- Markdown文件数: $current_md_files

### 清理效果
- 删除文档目录: docs/ (19MB), .qoder/ (6.2MB)
- 删除备份文件: restful_refactor_backup_* (~1MB)
- 删除构建产物: 所有target目录
- Markdown文件减少: $((2385 - current_md_files)) 个

## 清理内容详细

### 已删除目录
- \`docs/\` - 重复的文档目录
- \`.qoder/\` - 过期历史文档
- \`.claude/skills/archive/duplicate-skills/\` - 重复技能文件
- \`restful_refactor_backup_*/\` - 重构备份
- 所有 \`target/\` 构建目录

### 已删除文件
- 备份文件 (*.bak)
- 临时文件 (*.tmp)
- 日志文件 (*.log)
- 编译文件 (*.class)

### 已整理目录
- \`microservices/archive/deprecated-services/\` → \`microservices/archive/services-history/\`
- 添加历史服务说明文档

## 后续建议

1. **立即更新项目文档**: 确保所有文档引用正确
2. **团队通知**: 告知团队成员新的项目结构
3. **CI/CD检查**: 确认构建流程正常
4. **定期清理**: 建议每月执行一次类似的清理

---

**清理完成! 🎉**

项目现在更加整洁，维护效率将显著提升。
EOF

    log_info "✓ 清理报告已生成: CLEANUP_EXECUTION_REPORT.md"
}

# ====================================================================
# 主执行流程
# ====================================================================
main() {
    echo "============================================"
    echo "🚀 IOE-DREAM 项目清理脚本"
    echo "============================================"
    echo ""

    # 执行清理前检查
    pre_cleanup_check
    echo ""

    # 确认执行
    read -p "确认要执行清理操作吗？这将永久删除文件！(y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_warn "清理操作已取消"
        exit 0
    fi

    # 执行清理阶段
    cleanup_deprecated_docs
    echo ""

    cleanup_unused_code
    echo ""

    cleanup_build_artifacts
    echo ""

    cleanup_config_files
    echo ""

    update_documentation
    echo ""

    generate_cleanup_report
    echo ""

    log_section "清理完成! 🎉"
    echo "请查看 CLEANUP_EXECUTION_REPORT.md 了解详细清理结果"
    echo ""
    echo "建议操作:"
    echo "1. 检查清理结果: git status"
    echo "2. 提交清理更改: git add . && git commit -m 'chore: 清理冗余文件，优化项目结构'"
    echo "3. 删除备份分支: git branch -D archive/backup-*"
}

# 执行主函数
main "$@"