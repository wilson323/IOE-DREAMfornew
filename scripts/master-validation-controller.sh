#!/bin/bash

# =============================================================================
# IOE-DREAM 总体验证控制器
# =============================================================================
#
# 功能：统一管理和执行所有验证、监控、同步功能
# 提供一站式的规范治理和质量保证解决方案
# 版本：v1.0
# 更新时间：2025-11-17
#
# 使用方法：
#   ./scripts/master-validation-controller.sh [command] [options]
#
# Commands:
#   validate-all         - 执行所有验证检查
#   quick-scan          - 快速扫描关键违规
#   full-report         - 生成完整质量报告
#   sync-all            - 同步所有文档和技能
#   monitor-setup       - 设置监控系统
#   emergency-fix       - 紧急修复严重问题
#   health-check        - 系统健康检查
#
# 选项：
#   --fix                尝试自动修复问题
#   --force              强制执行操作
#   --report=FORMAT      生成报告格式
#   --threshold=NUM      设置合规阈值
#   --notify             发送通知
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
COMMAND="$1"
FIX_MODE=false
FORCE_MODE=false
REPORT_FORMAT="markdown"
THRESHOLD=90
NOTIFY_MODE=false
OVERALL_SUCCESS=true

# 日志函数
log_header() {
    echo -e "${PURPLE}🚀 ===== $1 =====${NC}"
}

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
    OVERALL_SUCCESS=false
}

log_critical() {
    echo -e "${RED}🚨 CRITICAL: $1${NC}"
    OVERALL_SUCCESS=false
}

log_step() {
    local step_num="$1"
    local step_desc="$2"
    echo -e "${CYAN}📍 Step $step_num: $step_desc${NC}"
}

echo -e "${BLUE}"
echo "============================================================================"
echo "🎛️  IOE-DREAM 总体验证控制器 v1.0"
echo "🔧 企业级规范治理与质量保证解决方案"
echo "📋 基于repowiki核心规范 + 全局规范矩阵"
echo "🎯 执行命令: $COMMAND"
echo "⏰ 执行时间: $(date)"
echo "============================================================================"
echo -e "${NC}"

# 解析命令行参数
shift
while [[ $# -gt 0 ]]; do
    case $1 in
        --fix)
            FIX_MODE=true
            shift
            ;;
        --force)
            FORCE_MODE=true
            shift
            ;;
        --report=*)
            REPORT_FORMAT="${1#*=}"
            shift
            ;;
        --threshold=*)
            THRESHOLD="${1#*=}"
            shift
            ;;
        --notify)
            NOTIFY_MODE=true
            shift
            ;;
        *)
            echo "未知参数: $1"
            echo "使用方法: $0 [command] [options]"
            exit 1
            ;;
    esac
done

# 执行所有验证检查
execute_all_validations() {
    log_header "执行所有验证检查"

    local step=1

    # Step 1: 基础环境检查
    log_step $((step++)) "基础环境检查"
    log_info "检查脚本文件完整性..."

    local required_scripts=(
        "scripts/comprehensive-validation.sh"
        "scripts/cache-architecture-validation.sh"
        "scripts/documentation-sync-validator.sh"
        "scripts/skills-docs-sync-engine.sh"
        "scripts/multi-dimensional-compliance-check.sh"
        "scripts/continuous-improvement-monitor.sh"
    )

    for script in "${required_scripts[@]}"; do
        if [ -f "$script" ]; then
            log_success "✓ $script"
        else
            log_error "✗ 缺失脚本: $script"
        fi
    done

    # Step 2: 文档一致性验证
    log_step $((step++)) "文档一致性验证"
    if [ "$FIX_MODE" = true ]; then
        ./scripts/documentation-sync-validator.sh --fix || log_warning "文档同步验证失败"
    else
        ./scripts/documentation-sync-validator.sh || log_warning "文档同步验证失败"
    fi

    # Step 3: Skills与文档同步
    log_step $((step++)) "Skills与文档同步"
    if [ "$FIX_MODE" = true ]; then
        ./scripts/skills-docs-sync-engine.sh sync --force || log_warning "Skills同步失败"
    else
        ./scripts/skills-docs-sync-engine.sh validate || log_warning "Skills验证失败"
    fi

    # Step 4: 缓存架构专项验证
    log_step $((step++)) "缓存架构专项验证"
    ./scripts/cache-architecture-validation.sh || log_error "缓存架构验证失败"

    # Step 5: 多维度合规性检查
    log_step $((step++)) "多维度合规性检查"
    local compliance_result=0
    ./scripts/multi-dimensional-compliance-check.sh --threshold=$THRESHOLD || compliance_result=1

    if [ $compliance_result -ne 0 ]; then
        log_error "多维度合规性检查未通过"
        if [ "$FIX_MODE" = true ]; then
            log_info "尝试自动修复合规性问题..."
            ./scripts/comprehensive-validation.sh || log_warning "自动修复失败"
        fi
    else
        log_success "多维度合规性检查通过"
    fi

    # Step 6: 生成综合报告
    log_step $((step++)) "生成综合验证报告"
    generate_master_report
}

# 快速扫描关键违规
quick_scan() {
    log_header "快速扫描关键违规"

    local step=1

    # 快速检查最严重的一级违规
    log_step $((step++)) "快速扫描一级违规"

    local critical_issues=0

    # 检查@Autowired使用
    log_info "检查@Autowired使用情况..."
    local autowired_count=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    if [ $autowired_count -gt 0 ]; then
        log_critical "发现 $autowired_count 个文件使用@Autowired，违反一级规范"
        ((critical_issues++))
    else
        log_success "✅ 未发现@Autowired使用"
    fi

    # 检查javax包使用
    log_info "检查javax包使用情况..."
    local javax_count=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "import javax\." {} \; 2>/dev/null | wc -l)
    if [ $javax_count -gt 0 ]; then
        log_critical "发现 $javax_count 个文件使用javax包，违反一级规范"
        ((critical_issues++))
    else
        log_success "✅ 未发现javax包使用"
    fi

    # 检查跨层访问
    log_info "检查跨层访问情况..."
    local direct_dao_count=$(find smart-admin-api-java17-springboot3 -name "*Controller.java" -exec grep -l "@Resource.*Dao\|@Autowired.*Dao" {} \; 2>/dev/null | wc -l)
    if [ $direct_dao_count -gt 0 ]; then
        log_critical "发现 $direct_dao_count 个Controller直接访问DAO，违反架构规范"
        ((critical_issues++))
    else
        log_success "✅ 未发现跨层访问问题"
    fi

    # 检查缓存架构违规
    log_info "检查缓存架构违规..."
    local cache_violations=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "RedisUtil\|RedisTemplate" {} \; 2>/dev/null | wc -l)
    if [ $cache_violations -gt 5 ]; then
        log_warning "发现 $cache_violations 个文件直接使用缓存工具，建议迁移到统一架构"
    else
        log_success "✅ 缓存架构使用情况良好"
    fi

    echo ""
    log_header "快速扫描结果总结"
    echo "=========================================="
    if [ $critical_issues -eq 0 ]; then
        log_success "🎉 快速扫描通过！未发现一级严重违规"
    else
        log_error "🚨 快速扫描发现 $critical_issues 个严重违规"
        echo ""
        echo "🔧 建议立即执行:"
        echo "   1. ./scripts/master-validation-controller.sh emergency-fix"
        echo "   2. ./scripts/master-validation-controller.sh validate-all --fix"
        echo "   3. 参考 docs/GLOBAL_STANDARDS_MATRIX.md"
    fi
}

# 生成完整质量报告
generate_full_report() {
    log_header "生成完整质量报告"

    local report_file="docs/master-validation-report-$(date +%Y%m%d_%H%M%S).md"

    log_info "生成详细报告: $report_file"

    cat > "$report_file" << EOF
# IOE-DREAM 总体验证报告

> **生成时间**: $(date)
> **报告版本**: v1.0
> **执行工具**: master-validation-controller.sh
> **合规阈值**: $THRESHOLD%

## 📊 执行概览

### 验证范围
- **代码验证**: 全面扫描所有Java代码文件
- **架构验证**: 检查四层架构合规性
- **规范验证**: 验证所有repowiki核心规范
- **文档验证**: 检查文档一致性
- **技能验证**: 验证skills体系同步

### 验证结果
EOF

    # 添加多维度检查结果
    echo "### 多维度合规性检查" >> "$report_file"
    ./scripts/multi-dimensional-compliance-check.sh --output=markdown >> "$report_file" 2>/dev/null || echo "- 多维度检查执行失败" >> "$report_file"

    # 添加缓存架构检查结果
    echo "" >> "$report_file"
    echo "### 缓存架构专项检查" >> "$report_file"
    ./scripts/cache-architecture-validation.sh >> "$report_file" 2>/dev/null || echo "- 缓存架构检查执行失败" >> "$report_file"

    # 添加文档同步结果
    echo "" >> "$report_file"
    echo "### 文档一致性验证" >> "$report_file"
    ./scripts/documentation-sync-validator.sh --report >> "$report_file" 2>/dev/null || echo "- 文档同步检查执行失败" >> "$report_file"

    cat >> "$report_file" << EOF

## 🎯 质量评估

### 合规性评级
$(./scripts/multi-dimensional-compliance-check.sh --output=json 2>/dev/null | jq -r '.compliant // false' | sed 's/true/✅ 通过 (优秀)/; s/false/❌ 不通过 (需改进)/')

### 改进建议
1. **立即处理**: 一级规范严重违规
2. **计划改进**: 二级规范警告问题
3. **持续优化**: 三级规范最佳实践
4. **定期检查**: 建立持续监控机制

## 📚 相关资源

### 核心文档
- **全局规范矩阵**: [docs/GLOBAL_STANDARDS_MATRIX.md](../GLOBAL_STANDARDS_MATRIX.md)
- **repowiki规范**: [docs/repowiki/zh/content/开发规范体系/](../repowiki/zh/content/开发规范体系/)
- **开发指南**: [CLAUDE.md](../CLAUDE.md)

### 验证工具
- **综合验证**: [scripts/comprehensive-validation.sh](../scripts/comprehensive-validation.sh)
- **多维度检查**: [scripts/multi-dimensional-compliance-check.sh](../scripts/multi-dimensional-compliance-check.sh)
- **文档同步**: [scripts/documentation-sync-validator.sh](../scripts/documentation-sync-validator.sh)
- **技能同步**: [scripts/skills-docs-sync-engine.sh](../scripts/skills-docs-sync-engine.sh)

### 监控系统
- **持续监控**: [scripts/continuous-improvement-monitor.sh](../scripts/continuous-improvement-monitor.sh)
- **缓存验证**: [scripts/cache-architecture-validation.sh](../scripts/cache-architecture-validation.sh)

---

**报告生成时间**: $(date)
**下次验证**: 建议每日执行
**维护团队**: SmartAdmin规范治理委员会
EOF

    log_success "完整质量报告已生成: $report_file"
}

# 紧急修复严重问题
emergency_fix() {
    log_header "紧急修复严重问题"

    log_warning "⚠️  紧急修复模式启动！"
    log_info "将尝试自动修复最严重的一级规范违规"

    local step=1

    # Step 1: 修复Autowired问题
    log_step $((step++)) "修复@Autowired问题"
    local autowired_files=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null)

    if [ -n "$autowired_files" ]; then
        log_info "发现 $(echo "$autowired_files" | wc -l) 个文件使用@Autowired"
        for file in $autowired_files; do
            log_info "修复文件: $file"
            sed -i 's/@Autowired/@Resource/g' "$file"
        done
        log_success "Autowired → Resource 修复完成"
    else
        log_success "✅ 无需修复@Autowired"
    fi

    # Step 2: 修复javax包问题
    log_step $((step++)) "修复javax包问题"
    local javax_files=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "import javax\." {} \; 2>/dev/null)

    if [ -n "$javax_files" ]; then
        log_info "发现 $(echo "$javax_files" | wc -l) 个文件使用javax包"
        for file in $javax_files; do
            log_info "修复文件: $file"
            # 修复常见的javax包名到jakarta
            sed -i 's/import javax\.servlet/import jakarta.servlet/g' "$file"
            sed -i 's/import javax\.validation/import jakarta.validation/g' "$file"
            sed -i 's/import javax\.annotation/import jakarta.annotation/g' "$file"
            sed -i 's/import javax\.persistence/import jakarta.persistence/g' "$file"
        done
        log_success "javax → jakarta 修复完成"
    else
        log_success "✅ 无需修复javax包"
    fi

    # Step 3: 验证修复效果
    log_step $((step++)) "验证修复效果"
    local remaining_autowired=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    local remaining_javax=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "import javax\." {} \; 2>/dev/null | wc -l)

    log_info "剩余问题统计:"
    log_info "  @Autowired: $remaining_autowired 个文件"
    log_info "  javax包: $remaining_javax 个文件"

    if [ $remaining_autowired -eq 0 ] && [ $remaining_javax -eq 0 ]; then
        log_success "🎉 紧急修复完成！所有一级违规已修复"
    else
        log_warning "仍有部分问题需要手动处理，建议使用专业工具进一步修复"
    fi
}

# 系统健康检查
health_check() {
    log_header "系统健康检查"

    local step=1

    # Step 1: 脚本完整性检查
    log_step $((step++)) "脚本完整性检查"
    local script_files=(
        "scripts/comprehensive-validation.sh"
        "scripts/cache-architecture-validation.sh"
        "scripts/documentation-sync-validator.sh"
        "scripts/skills-docs-sync-engine.sh"
        "scripts/multi-dimensional-compliance-check.sh"
        "scripts/continuous-improvement-monitor.sh"
        "scripts/master-validation-controller.sh"
    )

    local healthy_scripts=0
    for script in "${script_files[@]}"; do
        if [ -x "$script" ]; then
            ((healthy_scripts++))
            log_success "✓ $script"
        else
            log_error "✗ $script"
        fi
    done

    # Step 2: 文档完整性检查
    log_step $((step++)) "文档完整性检查"
    local doc_files=(
        "docs/GLOBAL_STANDARDS_MATRIX.md"
        "CLAUDE.md"
        "docs/repowiki/zh/content/开发规范体系.md"
        ".claude/skills/cache-architecture-specialist.md"
        ".claude/skills/spring-boot-jakarta-guardian.md"
    )

    local healthy_docs=0
    for doc in "${doc_files[@]}"; do
        if [ -f "$doc" ]; then
            ((healthy_docs++))
            log_success "✓ $doc"
        else
            log_error "✗ $doc"
        fi
    done

    # Step 3: 权限检查
    log_step $((step++)) "权限检查"
    if [ -w "scripts/" ] && [ -w "docs/" ]; then
        log_success "✓ 目录写权限正常"
    else
        log_error "✗ 目录权限异常"
    fi

    # Step 4: 依赖检查
    log_step $((step++)) "依赖检查"
    if command -v jq >/dev/null 2>&1; then
        log_success "✓ jq工具可用"
    else
        log_warning "⚠️  jq工具缺失，部分功能受限"
    fi

    # 生成健康报告
    echo ""
    log_header "系统健康报告"
    echo "=========================================="
    echo "📊 脚本完整性: $healthy_scripts/7"
    echo "📚 文档完整性: $healthy_docs/5"
    echo "🔧 系统权限: $(if [ -w "scripts/" ]; then echo "✅ 正常"; else echo "❌ 异常"; fi)"
    echo "📦 依赖工具: $(if command -v jq >/dev/null 2>&1; then echo "✅ 完整"; else echo "⚠️  部分缺失"; fi)"

    local total_checks=7
    local healthy_count=$((healthy_scripts + healthy_docs))

    if [ $healthy_count -ge $((total_checks - 1)) ]; then
        log_success "🎉 系统健康状况: 优秀 ($healthy_count/$total_checks)"
    else
        log_warning "⚠️  系统健康状况: 需要改进 ($healthy_count/$total_checks)"
    fi
}

# 生成总结报告
generate_summary() {
    echo ""
    log_header "执行总结"
    echo "=========================================="

    if [ "$OVERALL_SUCCESS" = true ]; then
        echo -e "${GREEN}🎉 执行状态: 成功${NC}"
        echo -e "${GREEN}✅ 所有验证检查通过${NC}"
        echo -e "${GREEN}📊 系统质量良好${NC}"
        echo -e "${GREEN}🔧 建议: 保持当前质量水平，持续改进${NC}"
    else
        echo -e "${RED}🚨 执行状态: 失败${NC}"
        echo -e "${RED}❌ 发现严重问题需要处理${NC}"
        echo -e "${RED}📊 系统质量需要提升${NC}"
        echo -e "${RED}🔧 建议: 立即执行紧急修复${NC}"
        echo ""
        echo "🚨 立即行动步骤:"
        echo "   1. ./scripts/master-validation-controller.sh emergency-fix"
        echo "   2. ./scripts/master-validation-controller.sh validate-all --fix"
        echo "   3. 查看详细报告: docs/master-validation-report-*.md"
    fi

    echo ""
    echo -e "${BLUE}📚 相关资源:${NC}"
    echo "   📖 全局规范矩阵: docs/GLOBAL_STANDARDS_MATRIX.md"
    echo "   🎯 repowiki规范: docs/repowiki/zh/content/开发规范体系/"
    echo "   💻 开发指南: CLAUDE.md"
    echo "   🛠️  验证工具: scripts/"
    echo "   📊 监控报告: docs/compliance-reports/"

    echo ""
    echo -e "${BLUE}🔄 持续改进:${NC}"
    echo "   🔍 每日监控: ./scripts/continuous-improvement-monitor.sh daily"
    echo "   📊 周度回顾: ./scripts/continuous-improvement-monitor.sh weekly"
    echo "   📈 月度分析: ./scripts/continuous-improvement-monitor.sh monthly"
    echo "   ⚙️  系统设置: ./scripts/continuous-improvement-monitor.sh setup"

    echo "============================================================================"
}

# 主执行逻辑
case "$COMMAND" in
    "validate-all")
        execute_all_validations
        ;;
    "quick-scan")
        quick_scan
        ;;
    "full-report")
        generate_full_report
        ;;
    "sync-all")
        log_header "同步所有文档和技能"
        ./scripts/documentation-sync-validator.sh --fix
        ./scripts/skills-docs-sync-engine.sh sync --force
        ;;
    "monitor-setup")
        log_header "设置监控系统"
        ./scripts/continuous-improvement-monitor.sh setup
        ;;
    "emergency-fix")
        emergency_fix
        ;;
    "health-check")
        health_check
        ;;
    *)
        echo "错误: 未知命令 '$COMMAND'"
        echo ""
        echo "支持命令:"
        echo "  validate-all         - 执行所有验证检查"
        echo "  quick-scan          - 快速扫描关键违规"
        echo "  full-report         - 生成完整质量报告"
        echo "  sync-all            - 同步所有文档和技能"
        echo "  monitor-setup       - 设置监控系统"
        echo "  emergency-fix       - 紧急修复严重问题"
        echo "  health-check        - 系统健康检查"
        echo ""
        echo "选项:"
        echo "  --fix                尝试自动修复问题"
        echo "  --force              强制执行操作"
        echo "  --report=FORMAT      生成报告格式"
        echo "  --threshold=NUM      设置合规阈值"
        echo "  --notify             发送通知"
        echo ""
        echo "使用示例:"
        echo "  $0 validate-all              # 执行所有验证"
        echo "  $0 quick-scan --fix          # 快速扫描并修复"
        echo "  $0 full-report --notify      # 生成报告并通知"
        exit 1
        ;;
esac

# 生成总结报告
generate_summary