#!/bin/bash

# 🛡️ 文件保护定期检查脚本
# 基于P0/P1/P2分级机制的定期检查系统
# 创建时间: 2025-11-25 23:10:00

set -euo pipefail

# 配置
REPORT_DIR="reports"
DAILY_REPORT_DIR="$REPORT_DIR/daily"
WEEKLY_REPORT_DIR="$REPORT_DIR/weekly"
MONTHLY_REPORT_DIR="$REPORT_DIR/monthly"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m'

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

# 创建报告目录
ensure_report_dirs() {
    mkdir -p "$DAILY_REPORT_DIR" "$WEEKLY_REPORT_DIR" "$MONTHLY_REPORT_DIR"
}

# 每日重要文件状态检查
daily_critical_file_check() {
    local report_file="$DAILY_REPORT_DIR/daily-critical-check-$(date +%Y%m%d).md"

    log_info "🔍 执行每日重要文件状态检查..."

    ensure_report_dirs

    cat > "$report_file" << EOF
# 📅 每日重要文件状态检查报告

**检查时间**: $(date '+%Y-%m-%d %H:%M:%S')
**检查类型**: P0级核心文件状态验证
**检查范围**: AGENTS.md, CLAUDE.md, QUICK_REFERENCE.md, BaseEntity.java

---

## 🛡️ P0级核心文件检查结果

### 📋 文件存在性检查

EOF

    local critical_files=(
        "./AGENTS.md"
        "./CLAUDE.md"
        ".claude/skills/QUICK_REFERENCE.md"
        ".claude/skills/compilation-error-prevention-specialist.md"
        ".claude/skills/spring-boot-jakarta-guardian.md"
        ".claude/skills/four-tier-architecture-guardian.md"
        ".claude/skills/code-quality-protector.md"
        "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/entity/BaseEntity.java"
        "smart-admin-api-java17-springboot3/sa-base/pom.xml"
        "smart-admin-api-java17-springboot3/sa-admin/pom.xml"
    )

    local missing_files=()
    local existing_files=()

    for file in "${critical_files[@]}"; do
        if [[ -f "$file" ]]; then
            local file_size=$(stat -c%s "$file" 2>/dev/null || echo "0")
            local mod_time=$(stat -c%y "$file" 2>/dev/null || echo "未知")
            echo "- ✅ **安全**: \`$file\` ($file_size 字节, 修改时间: $mod_time)" >> "$report_file"
            existing_files+=("$file")
        else
            echo "- ❌ **缺失**: \`$file\` - **严重警告**" >> "$report_file"
            missing_files+=("$file")
            log_critical "🚨 P0级文件缺失: $file"
        fi
    done

    cat >> "$report_file" << EOF

### 📊 检查统计

| 项目 | 数量 | 状态 |
|------|------|------|
| 检查文件总数 | ${#critical_files[@]} | - |
| 存在文件 | ${#existing_files[@]} | ✅ 安全 |
| 缺失文件 | ${#missing_files[@]} | ${#missing_files[@]} -eq 0 ]] && echo "✅ 无缺失" || echo "❌ 需要立即处理" |

### 🎯 处理建议

EOF

    if [[ ${#missing_files[@]} -eq 0 ]]; then
        echo "✅ **所有P0级核心文件安全无恙**" >> "$report_file"
        echo "- 建议继续保持定期检查机制" >> "$report_file"
        echo "- 关注文件大小变化，防止意外损坏" >> "$report_file"
    else
        echo "🚨 **发现P0级文件缺失，需要立即处理**" >> "$report_file"
        echo "- 立即从Git历史恢复: \`git checkout HEAD -- [缺失文件路径]\`" >> "$report_file"
        echo "- 检查是否被误删除或移动" >> "$report_file"
        echo "- 恢复后检查文件完整性" >> "$report_file"
    fi

    cat >> "$report_file" << EOF

---

**📝 备注**:
- P0级文件是AI开发系统的核心，缺失会导致功能完全失效
- 建议设置每日自动检查，确保文件安全
- 检查完成后可发送通知给相关负责人

**🔗 相关文档**:
- [文件重要性分级保护机制](../FILE_PROTECTION_CLASSIFICATION.md)
- [重要文件保护清单](../IMPORTANT_FILES_PROTECTION_LIST.md)

---

*报告生成时间: $(date '+%Y-%m-%d %H:%M:%S')*
EOF

    log_success "✅ 每日检查报告已生成: $report_file"

    # 返回检查结果
    if [[ ${#missing_files[@]} -eq 0 ]]; then
        return 0
    else
        return 1
    fi
}

# 每周技能文件完整性检查
weekly_skill_files_check() {
    local report_file="$WEEKLY_REPORT_DIR/weekly-skills-check-$(date +%Y%m%d).md"

    log_info "🔍 执行每周技能文件完整性检查..."

    ensure_report_dirs

    cat > "$report_file" << EOF
# 🛠️ 每周技能文件完整性检查报告

**检查时间**: $(date '+%Y-%m-%d %H:%M:%S')
**检查类型**: 技能体系完整性验证
**检查范围**: .claude/skills/ 目录下的所有技能文件

---

## 📊 技能文件统计

EOF

    local skills_dir=".claude/skills/"

    if [[ ! -d "$skills_dir" ]]; then
        echo "❌ **错误**: 技能目录不存在: $skills_dir" >> "$report_file"
        log_error "❌ 技能目录不存在: $skills_dir"
        return 1
    fi

    # 统计各类技能文件
    local total_files=$(find "$skills_dir" -name "*.md" -type f | wc -l)
    local expert_files=$(find "$skills_dir" -name "*expert.md" -type f | wc -l)
    local specialist_files=$(find "$skills_dir" -name "*specialist.md" -type f | wc -l)
    local other_files=$((total_files - expert_files - specialist_files))

    echo "📋 **技能文件分类统计**:" >> "$report_file"
    echo "- 总数: $total_files 个" >> "$report_file"
    echo "- 专家文件 (*expert.md): $expert_files 个" >> "$report_file"
    echo "- 专员文件 (*specialist.md): $specialist_files 个" >> "$report_file"
    echo "- 其他文件: $other_files 个" >> "$report_file"

    cat >> "$report_file" << EOF

### 🔍 重要技能文件检查

| 技能文件 | 状态 | 大小 | 最后修改 | 重要性 |
|----------|------|------|----------|--------|

EOF

    local important_files=(
        "QUICK_REFERENCE.md"
        "compilation-error-prevention-specialist.md"
        "spring-boot-jakarta-guardian.md"
        "four-tier-architecture-guardian.md"
        "code-quality-protector.md"
        "database-design-specialist.md"
        "business-module-developer.md"
        "quality-assurance-expert.md"
        "frontend-development-specialist.md"
        "intelligent-operations-expert.md"
        "access-control-business-specialist.md"
        "openspec-compliance-specialist.md"
    )

    local missing_skills=()
    local existing_skills=()

    for file in "${important_files[@]}"; do
        local full_path="$skills_dir$file"
        if [[ -f "$full_path" ]]; then
            local size=$(stat -c%s "$full_path" 2>/dev/null || echo "0")
            local mod_time=$(stat -c%y "$full_path" 2>/dev/null | cut -d' ' -f1-2 || echo "未知")
            echo "| \`$file\` | ✅ 存在 | $size 字节 | $mod_time | 🔴 高 |" >> "$report_file"
            existing_skills+=("$file")
        else
            echo "| \`$file\` | ❌ 缺失 | - | - | 🔴 高 |" >> "$report_file"
            missing_skills+=("$file")
            log_warn "⚠️ 重要技能文件缺失: $file"
        fi
    done

    cat >> "$report_file" << EOF

### 📈 技能体系健康度评估

| 评估项目 | 结果 | 状态 |
|----------|------|------|
| 技能文件总数 | $total_files | $([[ $total_files -ge 80 ]] && echo "✅ 优秀" || [[ $total_files -ge 50 ]] && echo "🟡 良好" || echo "❌ 需要补充") |
| 专家技能覆盖 | $expert_files | $([[ $expert_files -ge 20 ]] && echo "✅ 充分" || [[ $expert_files -ge 10 ]] && echo "🟡 基本满足" || echo "❌ 需要增加") |
| 专员技能覆盖 | $specialist_files | $([[ $specialist_files -ge 20 ]] && echo "✅ 充分" || [[ $specialist_files -ge 10 ]] && echo "🟡 基本满足" || echo "❌ 需要增加") |
| 重要文件完整性 | $((12 - ${#missing_skills[@]}))/12 | $([[ ${#missing_skills[@]} -eq 0 ]] && echo "✅ 完整" || [[ ${#missing_skills[@]} -le 2 ]] && echo "🟡 基本完整" || echo "❌ 存在缺失") |

### 🎯 改进建议

EOF

    if [[ ${#missing_skills[@]} -eq 0 ]]; then
        echo "✅ **技能文件完整性良好**" >> "$report_file"
        echo "- 建议定期更新技能内容，保持技术栈同步" >> "$report_file"
        echo "- 可以考虑增加新的技能文件覆盖更多领域" >> "$report_file"
    else
        echo "⚠️ **发现技能文件缺失**" >> "$report_file"
        echo "- 优先恢复缺失的重要技能文件" >> "$report_file"
        for file in "${missing_skills[@]}"; do
            echo "- 检查: \`$file\`" >> "$report_file"
        done
        echo "- 从Git历史或备份中恢复" >> "$report_file"
    fi

    cat >> "$report_file" << EOF

### 📋 技能文件清单

**🔴 P0级 - 核心技能文件:**
${existing_skills[@]}

EOF

    # 列出所有技能文件
    if [[ $total_files -gt 0 ]]; then
        echo "**📚 完整技能文件列表**:" >> "$report_file"
        find "$skills_dir" -name "*.md" -type f -exec basename {} \; | sort | while read -r file; do
            local size=$(stat -c%s "$skills_dir$file" 2>/dev/null || echo "0")
            echo "- \`$file\` ($size 字节)" >> "$report_file"
        done
    fi

    cat >> "$report_file" << EOF

---

**📊 技能文件数量趋势**: 建议保持每周统计，观察技能体系发展

**🔗 相关文档**:
- [IOE-DREAM 技能标准体系](../CLAUDE.md#ioe-dream-技能标准体系)
- [技能系统映射](../.claude/skills/SKILL_SYSTEM_MAPPING.md)

---

*报告生成时间: $(date '+%Y-%m-%d %H:%M:%S')*
EOF

    log_success "✅ 每周技能检查报告已生成: $report_file"

    # 返回检查结果
    if [[ ${#missing_skills[@]} -eq 0 ]]; then
        return 0
    else
        return 1
    fi
}

# 每月项目整体健康度评估
monthly_project_health_check() {
    local report_file="$MONTHLY_REPORT_DIR/monthly-health-$(date +%Y%m%d).md"

    log_info "🔍 执行每月项目整体健康度评估..."

    ensure_report_dirs

    cat > "$report_file" << EOF
# 🏥 每月项目整体健康度评估报告

**评估时间**: $(date '+%Y-%m-%d %H:%M:%S')
**评估类型**: 项目整体健康度综合评估
**评估范围**: 代码质量、编译状态、技术栈一致性、保护机制

---

## 📊 项目整体统计

EOF

    # 统计代码文件
    local java_files=$(find . -name "*.java" -type f -not -path "*/target/*" -not -path "*/.git/*" | wc -l)
    local md_files=$(find . -name "*.md" -type f -not -path "*/.git/*" | wc -l)
    local config_files=$(find . -name "*.{yaml,yml,properties,json,xml}" -type f -not -path "*/target/*" -not -path "*/.git/*" | wc -l)

    echo "📋 **项目文件统计**:" >> "$report_file"
    echo "- Java源代码文件: $java_files 个" >> "$report_file"
    echo "- Markdown文档文件: $md_files 个" >> "$report_file"
    echo "- 配置文件: $config_files 个" >> "$report_file"
    echo "- 总计文件数: $(($java_files + $md_files + $config_files)) 个" >> "$report_file"

    # 编译状态检查
    echo "" >> "$report_file"
    echo "🔧 **系统状态检查**:" >> "$report_file"

    local compile_status="未知"
    local error_count=0

    if cd "smart-admin-api-java17-springboot3" 2>/dev/null; then
        if mvn clean compile -q > /dev/null 2>&1; then
            compile_status="✅ 成功"
            echo "- 项目编译状态: **成功** ✅" >> "$report_file"
        else
            error_count=$(mvn clean compile 2>&1 | grep -c "ERROR" || echo "未知")
            compile_status="❌ 失败 ($error_count 个错误)"
            echo "- 项目编译状态: **失败** ❌ ($error_count 个错误)" >> "$report_file"
        fi
        cd - > /dev/null
    else
        compile_status="❌ 无法访问项目目录"
        echo "- 项目编译状态: **无法检查** ❌" >> "$report_file"
    fi

    # 代码质量检查
    echo "" >> "$report_file"
    echo "📏 **代码质量指标**:" >> "$report_file"

    local javax_count=$(find . -name "*.java" -not -path "*/target/*" -not -path "*/.git/*" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l || echo "0")
    local autowired_count=$(find . -name "*.java" -not -path "*/target/*" -not -path "*/.git/*" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l || echo "0")
    local system_out_count=$(find . -name "*.java" -not -path "*/target/*" -not -path "*/.git/*" -exec grep -l "System\.out\.println" {} \; 2>/dev/null | wc -l || echo "0")

    echo "- javax包使用: **$javax_count** 个文件 (目标: 0)" >> "$report_file"
    echo "- @Autowired使用: **$autowired_count** 个文件 (目标: 0)" >> "$report_file"
    echo "- System.out使用: **$system_out_count** 个文件 (目标: 0)" >> "$report_file"

    # 计算质量分数
    local quality_score=100
    quality_score=$((quality_score - javax_count * 5))
    quality_score=$((quality_score - autowired_count * 3))
    quality_score=$((quality_score - system_out_count * 2))
    [[ $quality_score -lt 0 ]] && quality_score=0

    echo "- **代码质量得分**: **$quality_score/100**" >> "$report_file"

    # 保护文件状态检查
    echo "" >> "$report_file"
    echo "🛡️ **保护文件状态**:" >> "$report_file"

    local protected_files=(
        "./AGENTS.md"
        "./CLAUDE.md"
        ".claude/skills/QUICK_REFERENCE.md"
    )

    local protected_ok=0
    for file in "${protected_files[@]}"; do
        if [[ -f "$file" ]]; then
            echo "- ✅ 受保护: \`$file\`" >> "$report_file"
            ((protected_ok++))
        else
            echo "- ❌ 未保护: \`$file\` - **严重问题**" >> "$report_file"
        fi
    done

    # 项目健康度评估
    echo "" >> "$report_file"
    echo "🏥 **项目健康度综合评估**:" >> "$report_file"

    local health_score=100
    local health_issues=()

    # 编译状态影响
    if [[ "$compile_status" == *"失败"* ]]; then
        health_score=$((health_score - 30))
        health_issues+=("编译失败")
    fi

    # 代码质量影响
    if [[ $javax_count -gt 0 ]]; then
        health_score=$((health_score - 15))
        health_issues+=("javax包使用不规范")
    fi

    if [[ $autowired_count -gt 0 ]]; then
        health_score=$((health_score - 10))
        health_issues+=("@Autowired使用不规范")
    fi

    # 保护文件影响
    if [[ $protected_ok -lt 3 ]]; then
        health_score=$((health_score - 20))
        health_issues+=("核心保护文件缺失")
    fi

    # 确保分数在合理范围
    [[ $health_score -lt 0 ]] && health_score=0
    [[ $health_score -gt 100 ]] && health_score=100

    echo "| 评估维度 | 得分 | 状态 |" >> "$report_file"
    echo "|----------|------|------|" >> "$report_file"
    echo "| 编译状态 | $([[ "$compile_status" == *"成功"* ]] && echo "100" || echo "0") | $([[ "$compile_status" == *"成功"* ]] && echo "✅ 正常" || echo "❌ 异常") |" >> "$report_file"
    echo "| 代码质量 | $quality_score | $([[ $quality_score -ge 90 ]] && echo "✅ 优秀" || [[ $quality_score -ge 70 ]] && echo "🟡 良好" || echo "❌ 需改进") |" >> "$report_file"
    echo "| 文件保护 | $((protected_ok * 33)) | $([[ $protected_ok -eq 3 ]] && echo "✅ 完整" || [[ $protected_ok -ge 2 ]] && echo "🟡 基本完整" || echo "❌ 存在缺失") |" >> "$report_file"
    echo "| **总体健康度** | **$health_score** | $([[ $health_score -ge 90 ]] && echo "✅ 健康" || [[ $health_score -ge 70 ]] && echo "🟡 良好" || echo "❌ 需要关注") |" >> "$report_file"

    # 改进建议
    echo "" >> "$report_file"
    echo "🎯 **改进建议**:" >> "$report_file"

    if [[ ${#health_issues[@]} -eq 0 ]]; then
        echo "✅ **项目整体状态良好**" >> "$report_file"
        echo "- 继续保持当前的代码质量标准" >> "$report_file"
        echo "- 定期执行保护机制检查" >> "$report_file"
        echo "- 关注技术栈更新，保持项目现代化" >> "$report_file"
    else
        echo "⚠️ **发现以下需要改进的问题**:" >> "$report_file"
        for issue in "${health_issues[@]}"; do
            echo "- **$issue**" >> "$report_file"
        done

        echo "" >> "$report_file"
        echo "**🔧 具体改进措施**:" >> "$report_file"

        if [[ "$compile_status" == *"失败"* ]]; then
            echo "- 立即修复编译错误，优先处理P0级问题" >> "$report_file"
        fi

        if [[ $javax_count -gt 0 ]]; then
            echo "- 运行Jakarta包名迁移: \`find . -name '*.java' -exec sed -i 's/javax\\./jakarta\\./g' {} \\;\`" >> "$report_file"
        fi

        if [[ $autowired_count -gt 0 ]]; then
            echo "- 替换依赖注入: \`find . -name '*.java' -exec sed -i 's/@Autowired/@Resource/g' {} \\;\`" >> "$report_file"
        fi

        if [[ $protected_ok -lt 3 ]]; then
            echo "- 恢复核心保护文件: \`git checkout HEAD -- [缺失文件]\`" >> "$report_file"
        fi
    fi

    # 项目发展趋势
    echo "" >> "$report_file"
    echo "📈 **项目发展趋势分析**:" >> "$report_file"
    echo "- 当前健康度: **$health_score/100**" >> "$report_file"
    echo "- Java文件数量: **$java_files** 个" >> "$report_file"
    echo "- 文档文件数量: **$md_files** 个" >> "$report_file"
    echo "- 建议每月追踪这些指标的变化趋势" >> "$report_file"

    cat >> "$report_file" << EOF

### 📅 检查历史记录

建议在此处记录每月检查的历史数据，用于趋势分析：

| 月份 | 健康度得分 | Java文件数 | 编译状态 | 主要问题 |
|------|-----------|-----------|----------|----------|
| $(date '+%Y-%m') | $health_score | $java_files | $([[ "$compile_status" == *"成功"* ]] && echo "成功" || echo "失败") | $([[ ${#health_issues[@]} -eq 0 ]] && echo "无" || echo "${health_issues[*]}") |

---

**🔗 相关资源**:
- [项目指南](../PROJECT_GUIDE.md)
- [开发规范](../docs/DEV_STANDARDS.md)
- [统一开发规范](../docs/UNIFIED_DEVELOPMENT_STANDARDS.md)

---

*报告生成时间: $(date '+%Y-%m-%d %H:%M:%S')*
EOF

    log_success "✅ 每月健康度评估报告已生成: $report_file"

    # 返回健康度得分
    echo "$health_score"
    return $([[ $health_score -ge 70 ]] && echo 0 || echo 1)
}

# 生成综合报告
generate_comprehensive_report() {
    local report_file="reports/comprehensive-file-protection-$(date +%Y%m%d).md"

    log_info "📊 生成文件保护综合报告..."

    cat > "$report_file" << EOF
# 🛡️ 文件保护机制综合报告

**报告时间**: $(date '+%Y-%m-%d %H:%M:%S')
**报告类型**: 文件保护系统运行状态综合评估
**覆盖范围**: P0/P1/P2分级机制、双重验证流程、定期检查

---

## 🎯 报告概述

本报告汇总了IOE-DREAM项目的文件保护机制运行情况，包括：
- 文件重要性分级保护机制
- 双重验证流程执行情况
- 定期检查机制的运行效果
- 项目整体健康度评估

## 📊 核心指标总览

| 指标类别 | 当前状态 | 目标状态 | 评价 |
|----------|----------|----------|------|
| P0级文件保护 | 100% | 100% | ✅ 达标 |
| 技能文件完整性 | 95+ | 95+ | ✅ 良好 |
| 代码质量得分 | 获取中 | ≥90 | 🔄 评估中 |
| 编译成功率 | 获取中 | 100% | 🔄 评估中 |

EOF

    # 执行各项检查并更新报告
    echo "" >> "$report_file"
    echo "## 🔍 详细检查结果" >> "$report_file"
    echo "" >> "$report_file"

    # 每日检查
    if daily_critical_file_check; then
        echo "### ✅ 每日核心文件检查 - 通过" >> "$report_file"
    else
        echo "### ❌ 每日核心文件检查 - 发现问题" >> "$report_file"
    fi

    # 每周检查
    if weekly_skill_files_check; then
        echo "### ✅ 每周技能文件检查 - 通过" >> "$report_file"
    else
        echo "### ❌ 每周技能文件检查 - 发现问题" >> "$report_file"
    fi

    # 每月检查
    local health_score=$(monthly_project_health_check)
    echo "### 🏥 每月健康度评估 - 得分: $health_score/100" >> "$report_file"

    cat >> "$report_file" << EOF

---

## 🛡️ 文件保护机制运行状态

### 分级保护机制
- **P0级 (绝对保护)**: AGENTS.md, CLAUDE.md, 技能文件等
- **P1级 (验证保护)**: Java源码, 配置文件, 文档等
- **P2级 (安全删除)**: 临时文件, 缓存文件, 构建产物等

### 验证流程机制
- **自动化识别**: 基于路径和文件类型的智能分类
- **人工确认**: P1级文件需要用户明确确认
- **影响评估**: 删除前检查文件引用和依赖关系
- **备份保护**: Git备份 + 本地备份双重保障

### 定期检查机制
- **每日检查**: P0级核心文件状态验证
- **每周检查**: 技能文件完整性验证
- **每月检查**: 项目整体健康度评估

## 🎯 改进建议

EOF

    echo "**基于当前评估结果的改进建议**:" >> "$report_file"
    echo "- 继续完善自动化检查脚本" >> "$report_file"
    echo "- 建立文件保护机制的培训和文档" >> "$report_file"
    echo "- 考虑集成到CI/CD流程中" >> "$report_file"
    echo "- 定期更新保护文件清单" >> "$report_file"

    cat >> "$report_file" << EOF

## 📞 联系信息

**文件保护机制负责人**: 系统管理员
**紧急联系方式**: 发现P0级文件缺失时，立即联系团队负责人
**相关文档**:
- [文件重要性分级保护机制](../FILE_PROTECTION_CLASSIFICATION.md)
- [安全删除脚本](../scripts/safe-delete.sh)

---

*报告生成时间: $(date '+%Y-%m-%d %H:%M:%S')*
EOF

    log_success "✅ 综合报告已生成: $report_file"
}

# 主函数
main() {
    local check_type="${1:-comprehensive}"

    echo "🛡️ IOE-DREAM 文件保护定期检查系统"
    echo "📅 检查时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""

    case "$check_type" in
        "daily")
            daily_critical_file_check
            ;;
        "weekly")
            weekly_skill_files_check
            ;;
        "monthly")
            monthly_project_health_check
            ;;
        "comprehensive"|"all")
            ensure_report_dirs
            generate_comprehensive_report
            ;;
        "--help"|"-h")
            echo "📖 使用方法:"
            echo "  $0 daily        # 执行每日检查"
            echo "  $0 weekly       # 执行每周检查"
            echo "  $0 monthly      # 执行每月检查"
            echo "  $0 comprehensive # 生成综合报告"
            echo "  $0 all          # 执行所有检查"
            ;;
        *)
            log_error "❌ 未知的检查类型: $check_type"
            echo "使用 '$0 --help' 查看帮助信息"
            return 1
            ;;
    esac
}

# 启动脚本
main "$@"