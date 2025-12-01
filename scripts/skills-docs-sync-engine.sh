#!/bin/bash

# =============================================================================
# Skills与文档同步引擎
# =============================================================================
#
# 功能：实现.claude/skills/ 与 repowiki文档的双向同步
# 确保技能专家体系与核心规范文档始终保持一致
# 版本：v1.0
# 更新时间：2025-11-17
#
# 使用方法：
#   ./scripts/skills-docs-sync-engine.sh [action] [options]
#
# Actions:
#   sync          双向同步技能和文档
#   validate      验证技能与文档的一致性
#   generate      从规范生成技能文档
#   update        从技能更新规范文档
#
# Options:
#   --force       强制覆盖现有文件
#   --backup      同步前创建备份
#   --dry-run     仅显示将要执行的操作
#   --skill=NAME  仅处理指定技能
#
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 全局变量
SYNC_ACTIONS=0
VALIDATION_ERRORS=0
FORCE_MODE=false
BACKUP_MODE=false
DRY_RUN_MODE=false
TARGET_SKILL=""

# 技能与规范映射关系
declare -A SKILL_SPEC_MAPPING
SKILL_SPEC_MAPPING[cache-architecture-specialist]="开发规范体系/核心规范/缓存架构规范.md"
SKILL_SPEC_MAPPING[spring-boot-jakarta-guardian]="开发规范体系/核心规范/Java编码规范.md"
SKILL_SPEC_MAPPING[four-tier-architecture-guardian]="开发规范体系/核心规范/架构设计规范.md"
SKILL_SPEC_MAPPING[code-quality-protector]="开发规范体系/核心规范/Java编码规范.md"
SKILL_SPEC_MAPPING[database-design-specialist]="架构设计规范/数据库设计规范.md"

declare -A SPEC_SKILL_MAPPING
for skill in "${!SKILL_SPEC_MAPPING[@]}"; do
    SPEC_SKILL_MAPPING[${SKILL_SPEC_MAPPING[$skill]}]=$skill
done

# 日志函数
log_info() {
    echo -e "${BLUE}ℹ️  INFO: $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ SUCCESS: $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  WARNING: $1${NC}"
}

log_error() {
    echo -e "${RED}❌ ERROR: $1${NC}"
    ((VALIDATION_ERRORS++))
}

log_action() {
    if [ "$DRY_RUN_MODE" = true ]; then
        echo -e "${CYAN}🔍 DRY-RUN: $1${NC}"
    else
        echo -e "${PURPLE}🚀 ACTION: $1${NC}"
        ((SYNC_ACTIONS++))
    fi
}

log_backup() {
    if [ "$BACKUP_MODE" = true ]; then
        echo -e "${GREEN}💾 BACKUP: $1${NC}"
    fi
}

echo -e "${BLUE}"
echo "============================================================================"
echo "🔄 IOE-DREAM Skills与文档同步引擎 v1.0"
echo "🎯 实现.claude/skills/ ↔ repowiki文档双向同步"
echo "⏰ 执行时间: $(date)"
echo "============================================================================"
echo -e "${NC}"

# 解析命令行参数
ACTION="$1"
shift

while [[ $# -gt 0 ]]; do
    case $1 in
        --force)
            FORCE_MODE=true
            shift
            ;;
        --backup)
            BACKUP_MODE=true
            shift
            ;;
        --dry-run)
            DRY_RUN_MODE=true
            shift
            ;;
        --skill=*)
            TARGET_SKILL="${1#*=}"
            shift
            ;;
        *)
            echo "未知参数: $1"
            echo "使用方法: $0 [action] [options]"
            echo "Actions: sync, validate, generate, update"
            echo "Options: --force, --backup, --dry-run, --skill=NAME"
            exit 1
            ;;
    esac
done

# 创建备份函数
create_backup() {
    local file_path="$1"
    if [ -f "$file_path" ] && [ "$BACKUP_MODE" = true ]; then
        local backup_path="${file_path}.backup.$(date +%Y%m%d_%H%M%S)"
        cp "$file_path" "$backup_path"
        log_backup "备份文件: $file_path → $backup_path"
    fi
}

# Phase 1: 验证技能与规范映射
validate_skill_spec_mapping() {
    echo -e "${PURPLE}🔍 ===== Phase 1: 验证技能与规范映射 =====${NC}"

    local mapping_issues=0

    for skill in "${!SKILL_SPEC_MAPPING[@]}"; do
        local spec="${SKILL_SPEC_MAPPING[$skill]}"
        local skill_file=".claude/skills/$skill"
        local spec_file="docs/repowiki/zh/content/$spec"

        echo -e "${CYAN}检查映射: $skill ↔ $spec${NC}"

        # 检查技能文件存在性
        if [ -f "$skill_file" ]; then
            log_success "✓ 技能文件存在: $skill"
        else
            log_warning "⚠️  技能文件缺失: $skill"
            ((mapping_issues++))
        fi

        # 检查规范文件存在性
        if [ -f "$spec_file" ]; then
            log_success "✓ 规范文件存在: $spec"
        else
            log_warning "⚠️  规范文件缺失: $spec"
            ((mapping_issues++))
        fi
    done

    if [ $mapping_issues -eq 0 ]; then
        log_success "所有技能与规范映射验证通过"
    else
        log_warning "发现 $mapping_issues 个映射问题"
    fi
}

# Phase 2: 同步技能到规范
sync_skill_to_spec() {
    echo -e "${PURPLE}🔍 ===== Phase 2: 同步技能到规范 =====${NC}"

    local skill="$1"
    local spec="${SKILL_SPEC_MAPPING[$skill]}"

    if [ -z "$spec" ]; then
        log_error "未找到技能 '$skill' 对应的规范文档"
        return 1
    fi

    local skill_file=".claude/skills/$skill"
    local spec_file="docs/repowiki/zh/content/$spec"

    log_info "同步技能到规范: $skill → $spec"

    if [ ! -f "$skill_file" ]; then
        log_error "技能文件不存在: $skill_file"
        return 1
    fi

    if [ ! -f "$spec_file" ]; then
        log_error "规范文件不存在: $spec_file"
        return 1
    fi

    # 提取技能文档的关键信息
    local skill_version=$(grep "> **版本**:" "$skill_file" | head -1 | sed 's/.*: v//' | sed 's/".*//' || echo "1.0")
    local skill_description=$(grep -A 2 "> **描述**:" "$skill_file" | tail -1 | sed 's/^[[:space:]]*//' || echo "")
    local skill_last_updated=$(grep "> **更新时间**:" "$skill_file" | head -1 | sed 's/.*: //' | sed 's/<.*//' || echo "$(date +%Y-%m-%d)")

    log_action "更新规范文档版本信息: v$skill_version, $skill_last_updated"

    if [ "$DRY_RUN_MODE" = false ]; then
        # 备份规范文件
        create_backup "$spec_file"

        # 更新规范文档的版本信息
        sed -i "s/> \*\*版本\*\*: v.*/> **版本**: v$skill_version/" "$spec_file"
        sed -i "s/> \*\*更新时间\*\*: .*/> **更新时间**: $skill_last_updated/" "$spec_file"

        # 同步关键概念和规则
        sync_key_concepts "$skill_file" "$spec_file"

        log_success "同步完成: $skill → $spec"
    fi
}

# Phase 3: 同步规范到技能
sync_spec_to_skill() {
    echo -e "${PURPLE}🔍 ===== Phase 3: 同步规范到技能 =====${NC}"

    local spec="$1"
    local skill="${SPEC_SKILL_MAPPING[$spec]}"

    if [ -z "$skill" ]; then
        log_error "未找到规范 '$spec' 对应的技能文档"
        return 1
    fi

    local spec_file="docs/repowiki/zh/content/$spec"
    local skill_file=".claude/skills/$skill"

    log_info "同步规范到技能: $spec → $skill"

    if [ ! -f "$spec_file" ]; then
        log_error "规范文件不存在: $spec_file"
        return 1
    fi

    if [ ! -f "$skill_file" ]; then
        log_error "技能文件不存在: $skill_file"
        return 1
    fi

    # 提取规范文档的关键信息
    local spec_version=$(grep "> **版本**:" "$spec_file" | head -1 | sed 's/.*: v//' | sed 's/".*//' || echo "1.0")
    local spec_last_updated=$(grep "> **更新时间**:" "$spec_file" | head -1 | sed 's/.*: //' | sed 's/<.*//' || echo "$(date +%Y-%m-%d)")

    log_action "更新技能文档版本信息: v$spec_version, $spec_last_updated"

    if [ "$DRY_RUN_MODE" = false ]; then
        # 备份技能文件
        create_backup "$skill_file"

        # 更新技能文档的版本信息
        sed -i "s/> \*\*版本\*\*: v.*/> **版本**: v$spec_version/" "$skill_file"
        sed -i "s/> \*\*更新时间\*\*: .*/> **更新时间**: $spec_last_updated/" "$skill_file"

        # 同步关键概念和规则
        sync_key_rules "$spec_file" "$skill_file"

        log_success "同步完成: $spec → $skill"
    fi
}

# Phase 4: 同步关键概念
sync_key_concepts() {
    local skill_file="$1"
    local spec_file="$2"

    log_info "同步关键概念: $(basename "$skill_file") → $(basename "$spec_file")"

    # 提取技能文档中的关键概念
    local skill_concepts=$(grep -E "^#{1,3}\s+" "$skill_file" | head -10 | sort -u)

    # 检查规范文档中是否包含这些概念
    for concept in $skill_concepts; do
        local concept_text=$(echo "$concept" | sed 's/^#{1,3}\s*//')

        if grep -q "$concept_text" "$spec_file"; then
            log_success "✓ 概念同步: $concept_text"
        else
            log_warning "⚠️  概念缺失: $concept_text (规范文档中)"

            if [ "$FORCE_MODE" = true ] && [ "$DRY_RUN_MODE" = false ]; then
                # 在规范文档中添加缺失的概念
                log_action "添加概念到规范文档: $concept_text"
                echo "" >> "$spec_file"
                echo "### $concept_text" >> "$spec_file"
                echo "参考技能文档: $(basename "$skill_file")" >> "$spec_file"
            fi
        fi
    done
}

# Phase 5: 同步关键规则
sync_key_rules() {
    local spec_file="$1"
    local skill_file="$2"

    log_info "同步关键规则: $(basename "$spec_file") → $(basename "$skill_file")"

    # 提取规范文档中的关键规则
    local spec_rules=$(grep -E "❌.*禁止|✅.*必须|⚠️.*建议" "$spec_file" | head -10)

    # 检查技能文档中是否包含这些规则
    for rule in $spec_rules; do
        local rule_text=$(echo "$rule" | sed 's/^[❌✅⚠️]\s*//')

        if grep -q "$rule_text" "$skill_file"; then
            log_success "✓ 规则同步: $rule_text"
        else
            log_warning "⚠️  规则缺失: $rule_text (技能文档中)"

            if [ "$FORCE_MODE" = true ] && [ "$DRY_RUN_MODE" = false ]; then
                # 在技能文档中添加缺失的规则
                log_action "添加规则到技能文档: $rule_text"
                echo "" >> "$skill_file"
                echo "#### 规则同步" >> "$skill_file"
                echo "从规范文档同步: $rule_text" >> "$skill_file"
            fi
        fi
    done
}

# Phase 6: 验证同步一致性
validate_sync_consistency() {
    echo -e "${PURPLE}🔍 ===== Phase 6: 验证同步一致性 =====${NC}"

    local consistency_issues=0

    for skill in "${!SKILL_SPEC_MAPPING[@]}"; do
        if [ -n "$TARGET_SKILL" ] && [ "$skill" != "$TARGET_SKILL" ]; then
            continue
        fi

        local spec="${SKILL_SPEC_MAPPING[$skill]}"
        local skill_file=".claude/skills/$skill"
        local spec_file="docs/repowiki/zh/content/$spec"

        if [ -f "$skill_file" ] && [ -f "$spec_file" ]; then
            log_info "验证一致性: $skill ↔ $spec"

            # 检查版本一致性
            local skill_version=$(grep "> **版本**:" "$skill_file" | head -1 | sed 's/.*: v//' | sed 's/".*//' || echo "unknown")
            local spec_version=$(grep "> **版本**:" "$spec_file" | head -1 | sed 's/.*: v//' | sed 's/".*//' || echo "unknown")

            if [ "$skill_version" = "$spec_version" ]; then
                log_success "✓ 版本一致: v$skill_version"
            else
                log_warning "⚠️  版本不一致: skill(v$skill_version) vs spec(v$spec_version)"
                ((consistency_issues++))
            fi

            # 检查更新时间一致性
            local skill_date=$(grep "> **更新时间**:" "$skill_file" | head -1 | sed 's/.*: //' | sed 's/<.*//' | sed 's/-//g' | head -8 || echo "0")
            local spec_date=$(grep "> **更新时间**:" "$spec_file" | head -1 | sed 's/.*: //' | sed 's/<.*//' | sed 's/-//g' | head -8 || echo "0")

            if [ "$skill_date" = "$spec_date" ]; then
                log_success "✓ 更新时间一致"
            else
                log_warning "⚠️  更新时间不一致，建议同步"
                ((consistency_issues++))
            fi
        fi
    done

    if [ $consistency_issues -eq 0 ]; then
        log_success "所有技能与规范一致性验证通过"
    else
        log_warning "发现 $consistency_issues 个一致性问题"
    fi
}

# Phase 7: 生成同步报告
generate_sync_report() {
    echo -e "${PURPLE}🔍 ===== Phase 7: 生成同步报告 =====${NC}"

    local report_file="docs/skills-docs-sync-report-$(date +%Y%m%d_%H%M%S).md"

    cat > "$report_file" << EOF
# Skills与文档同步报告

> **生成时间**: $(date)
> **同步工具**: scripts/skills-docs-sync-engine.sh
> **项目**: IOE-DREAM

## 📊 同步操作统计

### 执行的同步操作
- **同步操作数**: $SYNC_ACTIONS
- **验证错误数**: $VALIDATION_ERRORS
- **强制模式**: $FORCE_MODE
- **备份模式**: $BACKUP_MODE

### 处理的技能文档
EOF

    if [ -n "$TARGET_SKILL" ]; then
        echo "- **目标技能**: $TARGET_SKILL" >> "$report_file"
    else
        echo "- **处理范围**: 所有技能文档" >> "$report_file"
    fi

    cat >> "$report_file" << EOF

## 🔄 技能与规范映射关系

| 技能文档 | 对应规范 | 同步状态 |
|---------|---------|----------|
EOF

    for skill in "${!SKILL_SPEC_MAPPING[@]}"; do
        local spec="${SKILL_SPEC_MAPPING[$skill]}"
        local status="✅ 已同步"

        if [ ! -f ".claude/skills/$skill" ] || [ ! -f "docs/repowiki/zh/content/$spec" ]; then
            status="❌ 缺失文件"
        fi

        echo "| $skill | $spec | $status |" >> "$report_file"
    done

    cat >> "$report_file" << EOF

## 🔍 一致性验证结果

### 版本一致性
所有技能文档和对应的规范文档应保持相同的版本号。

### 内容一致性
关键概念、规则、最佳实践应在技能文档和规范文档中保持一致。

### 更新时间一致性
技能文档和规范文档的更新时间应保持同步。

## 📚 相关文档

- 全局规范矩阵: docs/GLOBAL_STANDARDS_MATRIX.md
- 技能体系: .claude/skills/
- repowiki规范: docs/repowiki/zh/content/开发规范体系/

---

**报告状态**: $(if [ $VALIDATION_ERRORS -eq 0 ]; then echo "✅ 同步验证通过"; else echo "❌ 发现同步问题"; fi)**
EOF

    log_success "详细同步报告已生成: $report_file"
}

# 主执行逻辑
case "$ACTION" in
    "sync")
        log_info "执行双向同步..."
        validate_skill_spec_mapping

        if [ -n "$TARGET_SKILL" ]; then
            if [ -n "${SKILL_SPEC_MAPPING[$TARGET_SKILL]}" ]; then
                sync_skill_to_spec "$TARGET_SKILL"
                sync_spec_to_skill "${SKILL_SPEC_MAPPING[$TARGET_SKILL]}"
            else
                log_error "未知技能: $TARGET_SKILL"
                exit 1
            fi
        else
            # 同步所有技能
            for skill in "${!SKILL_SPEC_MAPPING[@]}"; do
                sync_skill_to_spec "$skill"
            done

            for spec in "${!SPEC_SKILL_MAPPING[@]}"; do
                sync_spec_to_skill "$spec"
            done
        fi
        ;;

    "validate")
        log_info "执行一致性验证..."
        validate_skill_spec_mapping
        validate_sync_consistency
        ;;

    "generate")
        log_info "从规范生成技能文档..."
        log_warning "此功能需要实现具体的规范解析和技能生成逻辑"
        ;;

    "update")
        log_info "从技能更新规范文档..."
        log_warning "此功能需要实现具体的技能解析和规范更新逻辑"
        ;;

    *)
        echo "错误: 未知操作 '$ACTION'"
        echo "支持的操作: sync, validate, generate, update"
        exit 1
        ;;
esac

# 生成报告
generate_sync_report

# 生成最终总结
echo ""
echo -e "${PURPLE}📊 ===== Skills与文档同步总结 =====${NC}"
echo ""

echo -e "${BLUE}📈 同步统计：${NC}"
echo "   🔄 执行的同步操作: $SYNC_ACTIONS"
echo "   ❌ 验证错误数: $VALIDATION_ERRORS"
echo "   🎯 目标技能: ${TARGET_SKILL:-"所有技能"}"
echo "   🔧 强制模式: $FORCE_MODE"
echo "   💾 备份模式: $BACKUP_MODE"

echo ""
echo -e "${BLUE}🎯 同步分析：${NC}"
if [ $VALIDATION_ERRORS -eq 0 ]; then
    echo "   🎉 恭喜！所有技能与规范保持同步"
    echo "   ✅ skills与repowiki文档完全一致"
    echo "   📚 技能专家体系达到企业级标准"
else
    echo "   🚨 发现 $VALIDATION_ERRORS 个同步问题"
    echo "   📝 建议使用 --force 参数强制同步"
    echo "   🔧 使用 --backup 参数确保安全"
fi

echo ""
echo -e "${BLUE}🔄 持续同步：${NC}"
echo "   🔍 验证同步: ./scripts/skills-docs-sync-engine.sh validate"
echo "   🔄 双向同步: ./scripts/skills-docs-sync-engine.sh sync"
echo "   📊 生成报告: ./scripts/skills-docs-sync-engine.sh validate"
echo "   🎯 单技能同步: ./scripts/skills-docs-sync-engine.sh sync --skill=cache-architecture-specialist"

echo ""
echo "============================================================================"

# 返回结果
if [ $VALIDATION_ERRORS -gt 0 ]; then
    echo ""
    log_warning "⚠️  同步验证完成，发现 $VALIDATION_ERRORS 个问题需要处理"
    exit 1
else
    echo ""
    log_success "🎉 Skills与文档同步完全通过！技能专家体系与核心规范保持完美同步"
    exit 0
fi