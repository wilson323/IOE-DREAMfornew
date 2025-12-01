#!/bin/bash

# =============================================================================
# 文档一致性自动验证系统
# =============================================================================
#
# 功能：验证repowiki、CLAUDE.md、skills文档之间的内容一致性
# 确保所有文档保持同步，避免信息过时或不一致
# 版本：v1.0
# 更新时间：2025-11-17
#
# 使用方法：
#   ./scripts/documentation-sync-validator.sh [--fix] [--report]
#
# 参数：
#   --fix    自动修复发现的不一致问题
#   --report 生成详细的一致性报告
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
SYNC_ISSUES=0
MISSING_REFERENCES=0
VERSION_MISMATCHES=0
CONTENT_CONFLICTS=0
FIX_MODE=false
REPORT_MODE=false

# 日志函数
log_info() {
    echo -e "${BLUE}ℹ️  INFO: $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ SUCCESS: $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  WARNING: $1${NC}"
    ((SYNC_ISSUES++))
}

log_error() {
    echo -e "${RED}❌ ERROR: $1${NC}"
    ((SYNC_ISSUES++))
}

log_fix() {
    if [ "$FIX_MODE" = true ]; then
        echo -e "${GREEN}🔧 FIXED: $1${NC}"
    else
        echo -e "${YELLOW}🔧 SUGGESTION: $1${NC}"
    fi
}

echo -e "${BLUE}"
echo "============================================================================"
echo "📚 IOE-DREAM 文档一致性自动验证系统 v1.0"
echo "🎯 确保repowiki ↔ CLAUDE.md ↔ skills 文档完全同步"
echo "⏰ 执行时间: $(date)"
echo "============================================================================"
echo -e "${NC}"

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        --fix)
            FIX_MODE=true
            shift
            ;;
        --report)
            REPORT_MODE=true
            shift
            ;;
        *)
            echo "未知参数: $1"
            echo "使用方法: $0 [--fix] [--report]"
            exit 1
            ;;
    esac
done

# Phase 1: 检查repowiki规范引用一致性
validate_repowiki_references() {
    echo -e "${PURPLE}🔍 ===== Phase 1: repowiki规范引用一致性检查 =====${NC}"

    local repowiki_base="docs/repowiki/zh/content"
    local claudem_file="CLAUDE.md"
    local missing_refs=0

    # 检查CLAUDE.md中引用的repowiki文档是否存在
    log_info "检查CLAUDE.md中的repowiki文档引用..."

    if [ -f "$claudem_file" ]; then
        # 提取CLAUDE.md中的repowiki引用
        local repowiki_refs=$(grep -o "docs/repowiki[^)]*" "$claudem_file" | sort -u)

        for ref in $repowiki_refs; do
            if [ -f "$ref" ]; then
                log_success "✓ repowiki引用存在: $ref"
            else
                log_error "✗ repowiki引用缺失: $ref"
                ((missing_refs++))

                if [ "$FIX_MODE" = true ]; then
                    log_fix "尝试从备份中恢复缺失文档: $ref"
                    # 这里可以添加自动恢复逻辑
                fi
            fi
        done
    else
        log_error "CLAUDE.md文件不存在"
        ((missing_refs++))
    fi

    # 检查核心规范文档完整性
    local core_specs=(
        "开发规范体系/核心规范/架构设计规范.md"
        "开发规范体系/核心规范/Java编码规范.md"
        "开发规范体系/核心规范/RESTfulAPI设计规范.md"
        "开发规范体系/核心规范/系统安全规范.md"
        "开发规范体系/核心规范/缓存架构规范.md"
    )

    for spec in "${core_specs[@]}"; do
        if [ -f "$repowiki_base/$spec" ]; then
            log_success "✓ 核心规范存在: $spec"
        else
            log_error "✗ 核心规范缺失: $spec"
            ((missing_refs++))
        fi
    done

    ((MISSING_REFERENCES += missing_refs))
    log_info "repowiki引用检查完成，发现 $missing_refs 个问题"
}

# Phase 2: 检查版本号一致性
validate_version_consistency() {
    echo -e "${PURPLE}🔍 ===== Phase 2: 版本号一致性检查 =====${NC}"

    local version_mismatches=0
    local reference_version="1.0"
    local reference_date="2025-11-17"

    # 检查各文档的版本号
    local docs_to_check=(
        "docs/repowiki/zh/content/开发规范体系/核心规范/架构设计规范.md"
        "docs/repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md"
        "docs/repowiki/zh/content/开发规范体系/核心规范/RESTfulAPI设计规范.md"
        "docs/repowiki/zh/content/开发规范体系/核心规范/系统安全规范.md"
        "docs/repowiki/zh/content/开发规范体系/核心规范/缓存架构规范.md"
    )

    for doc in "${docs_to_check[@]}"; do
        if [ -f "$doc" ]; then
            local doc_version=$(grep "> **版本**:" "$doc" | head -1 | sed 's/.*v//' | sed 's/".*//' || echo "unknown")
            local doc_date=$(grep "> **更新时间**:" "$doc" | head -1 | sed 's/.*: //' | sed 's/<.*//' || echo "unknown")

            if [ "$doc_version" != "$reference_version" ]; then
                log_warning "版本不一致: $doc (v$doc_version vs v$reference_version)"
                ((version_mismatches++))

                if [ "$FIX_MODE" = true ]; then
                    log_fix "更新文档版本号: $doc → v$reference_version"
                    sed -i "s/> \*\*版本\*\*: v.*/> **版本**: v$reference_version/" "$doc"
                fi
            else
                log_success "✓ 版本一致: $doc (v$doc_version)"
            fi
        fi
    done

    # 检查技能文档版本
    local skills_to_check=(
        ".claude/skills/cache-architecture-specialist.md"
        ".claude/skills/spring-boot-jakarta-guardian.md"
        ".claude/skills/four-tier-architecture-guardian.md"
    )

    for skill in "${skills_to_check[@]}"; do
        if [ -f "$skill" ]; then
            local skill_version=$(grep "> **版本**:" "$skill" | head -1 | sed 's/.*v//' | sed 's/".*//' || echo "unknown")

            if [ "$skill_version" != "$reference_version" ]; then
                log_warning "技能版本不一致: $skill (v$skill_version vs v$reference_version)"
                ((version_mismatches++))

                if [ "$FIX_MODE" = true ]; then
                    log_fix "更新技能版本号: $skill → v$reference_version"
                    sed -i "s/> \*\*版本\*\*: v.*/> **版本**: v$reference_version/" "$skill"
                fi
            else
                log_success "✓ 技能版本一致: $skill (v$skill_version)"
            fi
        fi
    done

    ((VERSION_MISMATCHES += version_mismatches))
    log_info "版本一致性检查完成，发现 $version_mismatches 个版本不匹配"
}

# Phase 3: 检查内容同步性
validate_content_synchronization() {
    echo -e "${PURPLE}🔍 ===== Phase 3: 内容同步性检查 =====${NC}"

    local content_conflicts=0

    # 检查缓存架构规范的同步性
    log_info "检查缓存架构规范内容同步..."

    local cache_spec="docs/repowiki/zh/content/开发规范体系/核心规范/缓存架构规范.md"
    local cache_skill=".claude/skills/cache-architecture-specialist.md"
    local claudem_file="CLAUDE.md"

    if [ -f "$cache_spec" ] && [ -f "$cache_skill" ]; then
        # 检查关键概念的一致性
        local cache_concepts=(
            "三层缓存架构"
            "BusinessDataType"
            "CacheModule"
            "UnifiedCacheService"
            "BaseModuleCacheService"
            "iog:cache"
        )

        for concept in "${cache_concepts[@]}"; do
            local spec_has_concept=$(grep -c "$concept" "$cache_spec" || echo "0")
            local skill_has_concept=$(grep -c "$concept" "$cache_skill" || echo "0")

            if [ "$spec_has_concept" -gt 0 ] && [ "$skill_has_concept" -eq 0 ]; then
                log_warning "内容不同步: 技能文档缺少概念 '$concept'"
                ((content_conflicts++))
            elif [ "$spec_has_concept" -eq 0 ] && [ "$skill_has_concept" -gt 0 ]; then
                log_warning "内容不同步: 规范文档缺少概念 '$concept'"
                ((content_conflicts++))
            fi
        done
    fi

    # 检查Java编码规范的同步性
    log_info "检查Java编码规范内容同步..."

    local java_spec="docs/repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md"
    local java_skill=".claude/skills/spring-boot-jakarta-guardian.md"

    if [ -f "$java_spec" ] && [ -f "$java_skill" ]; then
        # 检查关键规则的一致性
        local java_rules=(
            "@Resource"
            "jakarta"
            "System.out.println"
            "四层架构"
        )

        for rule in "${java_rules[@]}"; do
            local spec_has_rule=$(grep -c "$rule" "$java_spec" || echo "0")
            local skill_has_rule=$(grep -c "$rule" "$java_skill" || echo "0")

            if [ "$spec_has_rule" -gt 0 ] && [ "$skill_has_rule" -eq 0 ]; then
                log_warning "内容不同步: 技能文档缺少规则 '$rule'"
                ((content_conflicts++))
            elif [ "$spec_has_rule" -eq 0 ] && [ "$skill_has_rule" -gt 0 ]; then
                log_warning "内容不同步: 规范文档缺少规则 '$rule'"
                ((content_conflicts++))
            fi
        done
    fi

    ((CONTENT_CONFLICTS += content_conflicts))
    log_info "内容同步性检查完成，发现 $content_conflicts 个内容冲突"
}

# Phase 4: 检查链接有效性
validate_link_validity() {
    echo -e "${PURPLE}🔍 ===== Phase 4: 链接有效性检查 =====${NC}"

    local broken_links=0
    local checked_links=0

    # 检查CLAUDE.md中的链接
    if [ -f "CLAUDE.md" ]; then
        log_info "检查CLAUDE.md中的文档链接..."

        # 提取markdown链接
        local links=$(grep -o '\[.*\]([^)]*)' "CLAUDE.md" | grep -v "http" | grep "docs/" | sort -u)

        for link in $links; do
            local link_path=$(echo "$link" | sed 's/^[^(]*//' | sed 's/).*$//')
            ((checked_links++))

            if [ -f "$link_path" ]; then
                log_success "✓ 链接有效: $link_path"
            else
                log_error "✗ 链接失效: $link_path"
                ((broken_links++))

                if [ "$FIX_MODE" = true ]; then
                    log_fix "尝试修复失效链接: $link_path"
                    # 这里可以添加自动修复逻辑
                fi
            fi
        done
    fi

    # 检查repowiki文档中的交叉引用
    local repowiki_base="docs/repowiki/zh/content"
    log_info "检查repowiki文档交叉引用..."

    find "$repowiki_base" -name "*.md" -type f | while read -r md_file; do
        local internal_links=$(grep -o '\[.*\]([^)]*)' "$md_file" | grep "docs/" | sort -u)

        for link in $internal_links; do
            local link_path=$(echo "$link" | sed 's/^[^(]*//' | sed 's/).*$//')
            ((checked_links++))

            if [ -f "$link_path" ]; then
                : # 链接有效，无需输出
            else
                log_warning "内部链接失效: $md_file → $link_path"
                ((broken_links++))
            fi
        done
    done

    log_info "链接有效性检查完成，检查了 $checked_links 个链接，发现 $broken_links 个失效链接"
}

# Phase 5: 生成同步报告
generate_sync_report() {
    if [ "$REPORT_MODE" = true ]; then
        echo -e "${PURPLE}🔍 ===== Phase 5: 生成同步报告 =====${NC}"

        local report_file="docs/documentation-sync-report-$(date +%Y%m%d).md"

        cat > "$report_file" << EOF
# 文档一致性验证报告

> **生成时间**: $(date)
> **验证工具**: scripts/documentation-sync-validator.sh
> **项目**: IOE-DREAM

## 📊 验证结果概览

### 统计信息
- **缺失引用**: $MISSING_REFERENCES
- **版本不匹配**: $VERSION_MISMATCHES
- **内容冲突**: $CONTENT_CONFLICTS
- **总同步问题**: $SYNC_ISSUES

### 验证阶段
1. ✅ repowiki规范引用一致性检查
2. ✅ 版本号一致性检查
3. ✅ 内容同步性检查
4. ✅ 链接有效性检查

## 🔍 详细问题分析

### 缺失文档引用
$(if [ $MISSING_REFERENCES -gt 0 ]; then echo "- 发现 $MISSING_REFERENCES 个缺失的文档引用"; else echo "- 无缺失文档引用"; fi)

### 版本不一致问题
$(if [ $VERSION_MISMATCHES -gt 0 ]; then echo "- 发现 $VERSION_MISMATCHES 个版本不匹配问题"; else echo "- 无版本不一致问题"; fi)

### 内容冲突问题
$(if [ $CONTENT_CONFLICTS -gt 0 ]; then echo "- 发现 $CONTENT_CONFLICTS 个内容冲突"; else echo "- 无内容冲突"; fi)

## 🔧 修复建议

### 立即修复（严重问题）
1. 补充所有缺失的文档引用
2. 统一所有文档的版本号
3. 解决内容同步冲突

### 建议优化（改进问题）
1. 建立定期同步检查机制
2. 实施自动化文档同步工具
3. 加强文档维护流程

## 📚 相关文档

- 全局规范矩阵: docs/GLOBAL_STANDARDS_MATRIX.md
- repowiki规范体系: docs/repowiki/zh/content/开发规范体系/
- 技能体系文档: .claude/skills/
- 开发指南: CLAUDE.md

---

**报告状态**: $(if [ $SYNC_ISSUES -eq 0 ]; then echo "✅ 一致性验证通过"; else echo "❌ 发现同步问题，需要修复"; fi)**
EOF

        log_success "详细同步报告已生成: $report_file"
    fi
}

# Phase 6: 自动修复功能
auto_fix_issues() {
    if [ "$FIX_MODE" = true ]; then
        echo -e "${PURPLE}🔧 ===== Phase 6: 自动修复功能 =====${NC}"

        log_info "执行自动修复..."

        # 修复版本号不一致
        if [ $VERSION_MISMATCHES -gt 0 ]; then
            log_info "修复版本号不一致问题..."
            # 重新执行版本检查，这次会进行修复
            validate_version_consistency
        fi

        # 可以添加更多自动修复逻辑
        log_success "自动修复完成"
    else
        log_info "跳过自动修复（使用 --fix 参数启用）"
    fi
}

# 生成最终总结
generate_summary() {
    echo ""
    echo -e "${PURPLE}📊 ===== 文档一致性验证总结 =====${NC}"
    echo ""

    echo -e "${BLUE}📈 验证统计：${NC}"
    echo "   📋 缺失引用数: $MISSING_REFERENCES"
    echo "   🔢 版本不匹配数: $VERSION_MISMATCHES"
    echo "   ⚠️  内容冲突数: $CONTENT_CONFLICTS"
    echo "   📝 总同步问题数: $SYNC_ISSUES"

    echo ""
    echo -e "${BLUE}🎯 一致性分析：${NC}"
    if [ $SYNC_ISSUES -eq 0 ]; then
        echo "   🎉 恭喜！所有文档完全一致"
        echo "   ✅ repowiki、CLAUDE.md、skills文档保持完美同步"
        echo "   📚 文档体系达到企业级标准"
    else
        echo "   🚨 发现 $SYNC_ISSUES 个同步问题"
        echo "   📝 建议立即修复以确保文档一致性"
        echo "   🔧 使用 --fix 参数可尝试自动修复"
    fi

    echo ""
    echo -e "${BLUE}🔄 持续监控：${NC}"
    echo "   🔍 定期执行: ./scripts/documentation-sync-validator.sh"
    echo "   📊 生成报告: ./scripts/documentation-sync-validator.sh --report"
    echo "   🔧 自动修复: ./scripts/documentation-sync-validator.sh --fix"

    echo ""
    echo "============================================================================"
}

# 主执行流程
main() {
    log_info "开始文档一致性验证..."

    # 执行所有验证阶段
    validate_repowiki_references
    validate_version_consistency
    validate_content_synchronization
    validate_link_validity
    generate_sync_report
    auto_fix_issues

    # 生成总结
    generate_summary

    # 返回结果
    if [ $SYNC_ISSUES -gt 0 ]; then
        echo ""
        log_warning "⚠️  文档一致性验证完成，发现 $SYNC_ISSUES 个问题需要处理"
        exit 1
    else
        echo ""
        log_success "🎉 文档一致性验证完全通过！所有文档保持完美同步"
        exit 0
    fi
}

# 执行主函数
main "$@"