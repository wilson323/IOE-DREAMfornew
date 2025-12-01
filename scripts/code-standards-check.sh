#!/bin/bash

# IOE-DREAM 代码规范检查脚本
# 检查Jakarta包名、依赖注入、四层架构等规范

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 统计变量
TOTAL_JAVA_FILES=0
JAVAX_VIOLATIONS=0
AUTOWIRED_VIOLATIONS=0
ARCHITECTURE_VIOLATIONS=0
COMPLIANT_FILES=0

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

# 检查是否在项目根目录
check_project_root() {
    if [ ! -f "pom.xml" ] && [ ! -d "microservices" ]; then
        log_error "请在项目根目录执行此脚本"
        exit 1
    fi
    log_success "项目根目录验证通过"
}

# 1. 检查Jakarta包名规范
check_jakarta_compliance() {
    log_info "检查Jakarta包名合规性..."

    local javax_files=()

    # 查找所有Java文件
    while IFS= read -r -d '' java_file; do
        ((TOTAL_JAVA_FILES++))

        # 检查javax包名违规
        if grep -q "javax\.\(annotation\|validation\|servlet\|inject\|persistence\)" "$java_file"; then
            ((JAVAX_VIOLATIONS++))
            javax_files+=("$java_file")
            log_error "Javax违规: $java_file"
            # 显示具体的违规行
            grep -n "javax\.\(annotation\|validation\|servlet\|inject\|persistence\)" "$java_file" | head -3
        fi
    done < <(find . -name "*.java" -type f -print0)

    if [ ${#javax_files[@]} -eq 0 ]; then
        log_success "✅ 所有文件Jakarta包名合规"
    else
        log_error "❌ 发现 ${#javax_files[@]} 个文件存在Javax包名违规"
    fi
}

# 2. 检查依赖注入规范
check_dependency_injection() {
    log_info "检查依赖注入规范..."

    local autowired_files=()

    # 重新查找Java文件
    while IFS= read -r -d '' java_file; do
        # 检查@Autowired违规
        if grep -q "@Autowired" "$java_file"; then
            ((AUTOWIRED_VIOLATIONS++))
            autowired_files+=("$java_file")
            log_error "@Autowired违规: $java_file"
            # 显示具体的违规行
            grep -n "@Autowired" "$java_file" | head -3
        fi
    done < <(find . -name "*.java" -type f -print0)

    if [ ${#autowired_files[@]} -eq 0 ]; then
        log_success "✅ 所有文件依赖注入规范合规"
    else
        log_error "❌ 发现 ${#autowired_files[@]} 个文件存在@Autowired违规"
    fi
}

# 3. 检查四层架构规范
check_four_tier_architecture() {
    log_info "检查四层架构调用规范..."

    local architecture_violations=()

    # 重新查找Java文件
    while IFS= read -r -d '' java_file; do
        # 检查Controller直接调用DAO
        if [[ "$java_file" =~ .*Controller\.java$ ]]; then
            if grep -q ".*Dao\|.*Mapper" "$java_file"; then
                ((ARCHITECTURE_VIOLATIONS++))
                architecture_violations+=("$java_file")
                log_error "架构违规 - Controller直接访问DAO: $java_file"
                grep -n ".*Dao\|.*Mapper" "$java_file" | head -3
            fi
        fi

        # 检查Service直接调用底层框架
        if [[ "$java_file" =~ .*Service\.java$ ]]; then
            if grep -q "@Repository\|.*Mapper.*" "$java_file"; then
                ((ARCHITECTURE_VIOLATIONS++))
                architecture_violations+=("$java_file")
                log_warning "架构警告 - Service直接使用Repository: $java_file"
            fi
        fi
    done < <(find . -name "*.java" -type f -print0)

    if [ ${#architecture_violations[@]} -eq 0 ]; then
        log_success "✅ 所有文件四层架构规范合规"
    else
        log_warning "⚠️ 发现 ${#architecture_violations[@]} 个文件可能存在架构问题"
    fi
}

# 4. 检查命名规范
check_naming_conventions() {
    log_info "检查命名规范..."

    local naming_issues=()

    # 检查类命名规范
    while IFS= read -r -d '' java_file; do
        # 检查类名是否遵循大驼峰命名
        class_name=$(basename "$java_file" .java)
        if [[ ! "$class_name" =~ ^[A-Z][a-zA-Z0-9]*$ ]]; then
            naming_issues+=("类名不规范: $java_file (类名: $class_name)")
        fi

        # 检查Controller命名
        if [[ "$java_file" =~ .*Controller\.java$ ]]; then
            if [[ ! "$class_name" =~ .*Controller$ ]]; then
                naming_issues+=("Controller类名应以Controller结尾: $java_file")
            fi
        fi

        # 检查Service命名
        if [[ "$java_file" =~ .*Service\.java$ ]]; then
            if [[ ! "$class_name" =~ .*Service$ ]]; then
                naming_issues+=("Service类名应以Service结尾: $java_file")
            fi
        fi

        # 检查DAO命名
        if [[ "$java_file" =~ .*Dao\.java$ ]] || [[ "$java_file" =~ .*Mapper\.java$ ]]; then
            if [[ ! "$class_name" =~ (Dao|Mapper)$ ]]; then
                naming_issues+=("DAO类名应以Dao或Mapper结尾: $java_file")
            fi
        fi

        # 检查Entity命名
        if [[ "$java_file" =~ .*entity/.*\.java$ ]]; then
            if [[ ! "$class_name" =~ (Entity|VO|DTO|Form)$ ]]; then
                naming_issues+=("实体类名应以Entity、VO、DTO或Form结尾: $java_file")
            fi
        fi
    done < <(find . -name "*.java" -type f -print0)

    if [ ${#naming_issues[@]} -eq 0 ]; then
        log_success "✅ 所有文件命名规范合规"
    else
        log_warning "⚠️ 发现 ${#naming_issues[@]} 个命名规范问题"
        for issue in "${naming_issues[@]}"; do
            log_warning "$issue"
        done
    fi
}

# 5. 检查注释规范
check_comment_standards() {
    log_info "检查注释规范..."

    local comment_issues=()

    while IFS= read -r -d '' java_file; do
        # 检查public类是否有类注释
        if grep -q "public class" "$java_file"; then
            class_line=$(grep -n "public class" "$java_file" | head -1 | cut -d: -f1)
            if [ $class_line -gt 1 ]; then
                # 检查类注释
                prev_line=$((class_line - 1))
                comment_line=$(sed -n "${prev_line}p" "$java_file")
                if [[ ! "$comment_line" =~ /\*\* ]] && [[ ! "$comment_line" =~ /\* ]]; then
                    comment_issues+=("缺少类注释: $java_file")
                fi
            fi
        fi

        # 检查public方法是否有方法注释
        grep -n "public.*(" "$java_file" | while read line; do
            line_num=$(echo "$line" | cut -d: -f1)
            if [ $line_num -gt 1 ]; then
                prev_line=$((line_num - 1))
                comment_line=$(sed -n "${prev_line}p" "$java_file")
                if [[ ! "$comment_line" =~ /\*\* ]] && [[ ! "$comment_line" =~ /\* ]]; then
                    comment_issues+=("缺少方法注释: $java_file:$line_num")
                fi
            fi
        done
    done < <(find . -name "*.java" -type f -print0)

    if [ ${#comment_issues[@]} -eq 0 ]; then
        log_success "✅ 注释规范检查通过"
    else
        log_warning "⚠️ 发现 ${#comment_issues[@]} 个注释规范问题"
        # 只显示前10个问题
        for ((i=0; i<${#comment_issues[@]} && i<10; i++)); do
            log_warning "${comment_issues[i]}"
        done
        if [ ${#comment_issues[@]} -gt 10 ]; then
            log_warning "... 还有 $((${#comment_issues[@]} - 10)) 个注释问题"
        fi
    fi
}

# 6. 生成修复建议
generate_fix_suggestions() {
    log_info "生成修复建议..."

    cat > CODE_STANDARDS_FIX_REPORT.md << EOF
# IOE-DREAM 代码规范修复建议报告

**生成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**检查文件数**: $TOTAL_JAVA_FILES

## 📊 检查结果统计

| 检查项目 | 合规数 | 违规数 | 合规率 |
|---------|--------|--------|--------|
| Jakarta包名 | $((TOTAL_JAVA_FILES - JAVAX_VIOLATIONS)) | $JAVAX_VIOLATIONS | $(((TOTAL_JAVA_FILES - JAVAX_VIOLATIONS) * 100 / TOTAL_JAVA_FILES))% |
| 依赖注入 | $((TOTAL_JAVA_FILES - AUTOWIRED_VIOLATIONS)) | $AUTOWIRED_VIOLATIONS | $(((TOTAL_JAVA_FILES - AUTOWIRED_VIOLATIONS) * 100 / TOTAL_JAVA_FILES))% |
| 四层架构 | $((TOTAL_JAVA_FILES - ARCHITECTURE_VIOLATIONS)) | $ARCHITECTURE_VIOLATIONS | $(((TOTAL_JAVA_FILES - ARCHITECTURE_VIOLATIONS) * 100 / TOTAL_JAVA_FILES))% |

## 🔧 修复建议

### 1. Jakarta包名修复

EOF

    if [ $JAVAX_VIOLATIONS -gt 0 ]; then
        cat >> CODE_STANDARDS_FIX_REPORT.md << EOF
**需要修复的文件**: $JAVAX_VIOLATIONS 个

**修复命令**:
\`\`\`bash
# 自动修复javax包名
find . -name "*.java" -exec sed -i 's/javax\.annotation/jakarta.annotation/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.validation/jakarta.validation/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.servlet/jakarta.servlet/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.inject/jakarta.inject/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.persistence/jakarta.persistence/g' {} \;
\`\`\`

**常见替换**:
- \`javax.annotation.Resource\` → \`jakarta.annotation.Resource\`
- \`javax.validation.Valid\` → \`jakarta.validation.Valid\`
- \`javax.servlet.http.HttpServletRequest\` → \`jakarta.servlet.http.HttpServletRequest\`

EOF
    else
        echo "- ✅ 所有文件Jakarta包名合规，无需修复" >> CODE_STANDARDS_FIX_REPORT.md
    fi

    cat >> CODE_STANDARDS_FIX_REPORT.md << EOF

### 2. 依赖注入修复

EOF

    if [ $AUTOWIRED_VIOLATIONS -gt 0 ]; then
        cat >> CODE_STANDARDS_FIX_REPORT.md << EOF
**需要修复的文件**: $AUTOWIRED_VIOLATIONS 个

**修复方案**:
1. 推荐使用构造器注入:
\`\`\`java
// ❌ 错误方式
@Autowired
private UserService userService;

// ✅ 推荐方式
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
}
\`\`\`

2. 或使用@Resource注解:
\`\`\`java
// ✅ 可接受方式
@Resource
private UserService userService;
\`\`\`

**修复命令**:
\`\`\`bash
# 替换@Autowired为@Resource (临时方案)
find . -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;
\`\`\`

EOF
    else
        echo "- ✅ 所有文件依赖注入规范合规，无需修复" >> CODE_STANDARDS_FIX_REPORT.md
    fi

    cat >> CODE_STANDARDS_FIX_REPORT.md << EOF

### 3. 架构规范修复

EOF

    if [ $ARCHITECTURE_VIOLATIONS -gt 0 ]; then
        cat >> CODE_STANDARDS_FIX_REPORT.md << EOF
**需要修复的文件**: $ARCHITECTURE_VIOLATIONS 个

**标准四层架构调用链**:
\`\`\`
Controller → Service → Manager → DAO
\`\`\`

**修复建议**:
1. Controller不应直接调用DAO，应通过Service层
2. Service不应直接使用@Repository注解，应通过Manager层调用DAO
3. 确保分层清晰，避免跨层调用

EOF
    else
        echo "- ✅ 所有文件四层架构规范合规，无需修复" >> CODE_STANDARDS_FIX_REPORT.md
    fi

    cat >> CODE_STANDARDS_FIX_REPORT.md << EOF

## 🎯 质量目标

- Jakarta包名合规率: 100%
- 依赖注入规范率: 100%
- 四层架构合规率: 100%
- 代码注释覆盖率: ≥ 80%

## 📝 持续改进建议

1. **IDE配置**: 在IDE中配置代码规范检查
2. **Git Hooks**: 在提交前自动检查代码规范
3. **CI/CD集成**: 在构建流程中加入规范检查
4. **定期审查**: 每周进行代码质量审查

---

**报告生成**: $(date '+%Y-%m-%d %H:%M:%S')
**下次检查**: 建议一周后再次检查
EOF

    log_success "修复建议报告已生成: CODE_STANDARDS_FIX_REPORT.md"
}

# 7. 计算合规率
calculate_compliance_rate() {
    local total_violations=$((JAVAX_VIOLATIONS + AUTOWIRED_VIOLATIONS + ARCHITECTURE_VIOLATIONS))

    if [ $TOTAL_JAVA_FILES -gt 0 ]; then
        local compliant_files=$((TOTAL_JAVA_FILES - total_violations))
        local compliance_rate=$((compliant_files * 100 / TOTAL_JAVA_FILES))

        log_info "📊 代码质量统计:"
        log_info "   - 检查文件总数: $TOTAL_JAVA_FILES"
        log_info "   - 合规文件数: $compliant_files"
        log_info "   - 违规文件数: $total_violations"
        log_info "   - 整体合规率: ${compliance_rate}%"

        if [ $compliance_rate -ge 95 ]; then
            log_success "🎉 代码质量优秀！"
        elif [ $compliance_rate -ge 85 ]; then
            log_success "✅ 代码质量良好"
        elif [ $compliance_rate -ge 70 ]; then
            log_warning "⚠️ 代码质量一般，需要改进"
        else
            log_error "❌ 代码质量较差，需要重点改进"
        fi

        return $compliance_rate
    else
        log_warning "未找到Java文件"
        return 0
    fi
}

# 主函数
main() {
    log_info "开始IOE-DREAM代码规范检查..."

    check_project_root

    # 执行各项检查
    check_jakarta_compliance
    check_dependency_injection
    check_four_tier_architecture
    check_naming_conventions
    check_comment_standards

    # 生成报告
    generate_fix_suggestions

    # 计算合规率
    calculate_compliance_rate

    local exit_code=$?

    log_success "代码规范检查完成！"
    log_info "详细报告请查看: CODE_STANDARDS_FIX_REPORT.md"

    # 如果合规率低于80%，返回非0退出码
    if [ $exit_code -lt 80 ]; then
        log_error "代码合规率低于80%，请修复后重新检查"
        exit 1
    fi

    exit 0
}

# 执行主函数
main "$@"