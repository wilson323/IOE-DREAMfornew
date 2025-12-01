#!/bin/bash

# =============================================================================
# IOE-DREAM 综合规验证证脚本
# =============================================================================
#
# 功能：全面验证项目是否符合所有repowiki核心规范
# 基于文档：docs/GLOBAL_STANDARDS_MATRIX.md
# 覆盖范围：架构设计、Java编码、API设计、系统安全、缓存架构
# 版本：v1.0
# 更新时间：2025-11-17
#
# 使用方法：
#   ./scripts/comprehensive-validation.sh [搜索目录]
#
# 返回值：
#   0 - 验证通过（符合所有规范）
#   1 - 验证失败（存在严重违规）
#   2 - 验证通过但有警告
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
NC='\033[0m' # No Color

# 全局计数器
TOTAL_VIOLATIONS=0
TOTAL_WARNINGS=0
TOTAL_FILES_CHECKED=0
VALIDATION_PHASES=0

# 规范违规分类统计
declare -A VIOLATION_CATEGORIES
VIOLATION_CATEGORIES[architecture]=0
VIOLATION_CATEGORIES[coding]=0
VIOLATION_CATEGORIES[api]=0
VIOLATION_CATEGORIES[security]=0
VIOLATION_CATEGORIES[cache]=0
VIOLATION_CATEGORIES[documentation]=0

# 日志函数
log_header() {
    echo -e "${PURPLE}🔍 ===== $1 =====${NC}"
}

log_info() {
    echo -e "${BLUE}ℹ️  INFO: $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ SUCCESS: $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  WARNING: $1${NC}"
    ((TOTAL_WARNINGS++))
}

log_error() {
    echo -e "${RED}❌ ERROR: $1${NC}"
    ((TOTAL_VIOLATIONS++))
}

log_phase() {
    ((VALIDATION_PHASES++))
    echo -e "${CYAN}🚀 Phase $VALIDATION_PHASES: $1${NC}"
}

log_category_error() {
    local category="$1"
    local message="$2"
    log_error "[$category] $message"
    ((VIOLATION_CATEGORIES[$category]++))
}

# 验证结果统计
PASS_COUNT=0
FAIL_COUNT=0

echo -e "${BLUE}"
echo "============================================================================"
echo "🏗️  IOE-DREAM 综合规验证证脚本 v1.0"
echo "📋 基于repowiki五大核心规范 + 全局规范矩阵"
echo "🎯 验证范围: ${1:-"src/main/java"}"
echo "⏰ 执行时间: $(date)"
echo "============================================================================"
echo -e "${NC}"

# Phase 1: 架构设计规范验证
validate_architecture_standards() {
    log_phase "架构设计规范验证"

    local search_dir="$1"
    local java_files=$(find "$search_dir" -name "*.java" -type f | head -50)
    local checked_files=0

    log_info "检查四层架构合规性..."

    for java_file in $java_files; do
        ((checked_files++))

        if [ $((checked_files % 10)) -eq 0 ]; then
            log_info "架构检查进度: $checked_files"
        fi

        local file_content
        file_content=$(cat "$java_file" 2>/dev/null || continue)

        # 检查1: Controller层直接访问DAO（一级违规）
        if echo "$file_content" | grep -q "@Controller\|@RestController" && echo "$file_content" | grep -q "@Resource.*Dao\|@Autowired.*Dao"; then
            log_category_error "architecture" "[$java_file] ❌ Controller层直接访问DAO，违反一级规范"
            echo "$file_content" | grep -n "@Resource.*Dao\|@Autowired.*Dao" | head -2 | while read -r line; do
                echo "    $line"
            done
        fi

        # 检查2: Service层直接访问数据库（一级违规）
        if echo "$file_content" | grep -q "@Service" && echo "$file_content" | grep -q "@Resource.*JdbcTemplate\|@Resource.*DataSource"; then
            log_category_error "architecture" "[$java_file] ❌ Service层直接访问数据库，违反一级规范"
        fi

        # 检查3: 缺少proper分层（二级违规）
        if echo "$file_content" | grep -q "@Controller" && ! echo "$file_content" | grep -q "@Resource.*Service"; then
            log_warning "[$java_file] ⚠️  Controller缺少Service层依赖，可能违反分层架构"
        fi

        # 检查4: 跨层访问模式（二级违规）
        if echo "$file_content" | grep -q "@Controller" && echo "$file_content" | grep -q "@Resource.*Manager\|@Resource.*Repository"; then
            log_warning "[$java_file] ⚠️  可能存在跨层访问，建议检查架构设计"
        fi
    done

    log_success "架构设计规范验证完成，检查了 $checked_files 个文件"
}

# Phase 2: Java编码规范验证
validate_java_coding_standards() {
    log_phase "Java编码规范验证"

    local search_dir="$1"
    local java_files=$(find "$search_dir" -name "*.java" -type f | head -50)
    local checked_files=0

    log_info "检查Java编码规范合规性..."

    for java_file in $java_files; do
        ((checked_files++))

        if [ $((checked_files % 10)) -eq 0 ]; then
            log_info "编码检查进度: $checked_files"
        fi

        local file_content
        file_content=$(cat "$java_file" 2>/dev/null || continue)

        # 检查1: @Autowired使用（一级违规）
        if echo "$file_content" | grep -q "@Autowired"; then
            log_category_error "coding" "[$java_file] ❌ 使用@Autowired，必须使用@Resource（一级规范）"
            echo "$file_content" | grep -n "@Autowired" | head -3 | while read -r line; do
                echo "    $line"
            done
        fi

        # 检查2: javax包使用（一级违规）
        if echo "$file_content" | grep -q "import javax\."; then
            # 检查是否是允许的JDK标准包
            if echo "$file_content" | grep -qE "import javax\.(crypto|net|security|naming)"; then
                log_info "[$java_file] ℹ️  使用JDK标准javax包，符合规范"
            else
                log_category_error "coding" "[$java_file] ❌ 使用javax包，必须迁移到jakarta（一级规范）"
                echo "$file_content" | grep -n "import javax\." | head -3 | while read -r line; do
                    echo "    $line"
                done
            fi
        fi

        # 检查3: System.out.println使用（一级违规）
        if echo "$file_content" | grep -q "System\.out\.print"; then
            log_category_error "coding" "[$java_file] ❌ 使用System.out.println，必须使用日志框架（一级规范）"
            echo "$file_content" | grep -n "System\.out\.print" | head -3 | while read -r line; do
                echo "    $line"
            done
        fi

        # 检查4: 硬编码字符串（二级违规）
        if echo "$file_content" | grep -E '"[^"]{20,}"' | grep -v -E "TODO|FIXME|Author|Since|@Param|@ApiOperation"; then
            if ! echo "$java_file" | grep -q "Test\|Config"; then
                log_warning "[$java_file] ⚠️  可能存在硬编码长字符串，建议定义为常量（二级规范）"
            fi
        fi

        # 检查5: 魔法数字（二级违规）
        if echo "$file_content" | grep -E "\b(?!1|0|2|10|100)[0-9]{2,}\b" | grep -v -E "//|/\*|\*"; then
            if ! echo "$java_file" | grep -q "Test\|Config\|Constant"; then
                log_warning "[$java_file] ⚠️  可能存在魔法数字，建议定义为常量（二级规范）"
            fi
        fi

        # 检查6: 异常处理完整性（二级违规）
        if echo "$file_content" | grep -q "catch.*Exception.*{" && echo "$file_content" | grep -q "catch.*{.*}" && ! echo "$file_content" | grep -A2 -B1 "catch.*Exception" | grep -q "log\|LOG"; then
            log_warning "[$java_file] ⚠️  异常处理可能缺少日志记录（二级规范）"
        fi
    done

    log_success "Java编码规范验证完成，检查了 $checked_files 个文件"
}

# Phase 3: API设计规范验证
validate_api_design_standards() {
    log_phase "API设计规范验证"

    local search_dir="$1"
    local controller_files=$(find "$search_dir" -name "*Controller.java" -type f | head -30)
    local checked_files=0

    log_info "检查RESTful API设计规范合规性..."

    for controller_file in $controller_files; do
        ((checked_files++))

        local file_content
        file_content=$(cat "$controller_file" 2>/dev/null || continue)

        # 检查1: GET方法进行数据修改（一级违规）
        if echo "$file_content" | grep -E "@GetMapping.*(/save|/create|/update|/delete|/add|/remove|/edit)" >/dev/null; then
            log_category_error "api" "[$controller_file] ❌ GET方法用于数据修改，违反RESTful规范（一级规范）"
            echo "$file_content" | grep -n -E "@GetMapping.*(/save|/create|/update|/delete|/add|/remove|/edit)" | head -2 | while read -r line; do
                echo "    $line"
            done
        fi

        # 检查2: 缺少权限验证（一级违规）
        local public_apis=$(echo "$file_content" | grep -E "@(Get|Post|Put|Delete)Mapping" | wc -l)
        local protected_apis=$(echo "$file_content" | grep -E "@(Get|Post|Put|Delete)Mapping" -A5 | grep -c "@SaCheckPermission\|@SaCheckLogin")

        if [ "$public_apis" -gt 0 ] && [ "$protected_apis" -lt "$((public_apis / 2))" ]; then
            log_category_error "api" "[$controller_file] ❌ 缺少权限验证，违反安全规范（一级规范）"
            log_info "    发现 $public_apis 个API，其中只有 $protected_apis 个有权限控制"
        fi

        # 检查3: 缺少参数验证（二级违规）
        if echo "$file_content" | grep -E "@(Post|Put)Mapping" && ! echo "$file_content" | grep -q "@Valid\|@Validated"; then
            log_warning "[$controller_file] ⚠️  POST/PUT接口缺少@Valid参数验证（二级规范）"
        fi

        # 检查4: 缺少Knife4j注解（二级违规）
        if ! echo "$file_content" | grep -q "@ApiOperation\|@Tag"; then
            log_warning "[$controller_file] ⚠️  缺少Knife4j API文档注解（二级规范）"
        fi

        # 检查5: ResponseDTO使用（二级违规）
        if echo "$file_content" | grep -E "@(Get|Post|Put|Delete)Mapping" && ! echo "$file_content" | grep -q "ResponseDTO"; then
            log_warning "[$controller_file] ⚠️  未使用统一ResponseDTO响应格式（二级规范）"
        fi
    done

    log_success "API设计规范验证完成，检查了 $checked_files 个Controller文件"
}

# Phase 4: 系统安全规范验证
validate_security_standards() {
    log_phase "系统安全规范验证"

    local search_dir="$1"
    local java_files=$(find "$search_dir" -name "*.java" -type f | head -50)
    local checked_files=0

    log_info "检查系统安全规范合规性..."

    for java_file in $java_files; do
        ((checked_files++))

        if [ $((checked_files % 10)) -eq 0 ]; then
            log_info "安全检查进度: $checked_files"
        fi

        local file_content
        file_content=$(cat "$java_file" 2>/dev/null || continue)

        # 检查1: 硬编码敏感信息（一级违规）
        if echo "$file_content" | grep -E "(password|secret|key|token).*=.*['\"]" -i; then
            if ! echo "$java_file" | grep -q "Test\|Config\|Example"; then
                log_category_error "security" "[$java_file] ❌ 疑似硬编码敏感信息，违反安全规范（一级规范）"
                echo "$file_content" | grep -n -E "(password|secret|key|token).*=.*['\"]" -i | head -2 | while read -r line; do
                    echo "    $line"
                done
            fi
        fi

        # 检查2: SQL注入风险（一级违规）
        if echo "$file_content" | grep -E "(\+|concat).*['\"]\s*\+" && echo "$file_content" | grep -q "select\|UPDATE\|DELETE\|INSERT"; then
            if ! echo "$java_file" | grep -q "Test\|Example"; then
                log_category_error "security" "[$java_file] ❌ 存在SQL注入风险，违反安全规范（一级规范）"
                echo "$file_content" | grep -n -E "(\+|concat).*['\"]\s*\+" | head -2 | while read -r line; do
                    echo "    $line"
                done
            fi
        fi

        # 检查3: 敏感信息日志输出（一级违规）
        if echo "$file_content" | grep -E "log\.(info|debug|error|warn).*password\|secret\|token" -i; then
            log_category_error "security" "[$java_file] ❌ 日志中输出敏感信息，违反安全规范（一级规范）"
            echo "$file_content" | grep -n -E "log\.(info|debug|error|warn).*password\|secret\|token" -i | head -2 | while read -r line; do
                echo "    $line"
            done
        fi

        # 检查4: 缺少Sa-Token权限验证（二级违规）
        if echo "$file_content" | grep -q "@Controller\|@RestController" && ! echo "$file_content" | grep -q "@SaCheckPermission\|@SaCheckRole\|@SaCheckLogin"; then
            if echo "$file_content" | grep -q "@PostMapping\|@PutMapping\|@DeleteMapping"; then
                log_warning "[$java_file] ⚠️  Controller缺少Sa-Token权限验证（二级规范）"
            fi
        fi
    done

    log_success "系统安全规范验证完成，检查了 $checked_files 个文件"
}

# Phase 5: 缓存架构规范验证
validate_cache_architecture_standards() {
    log_phase "缓存架构规范验证"

    local search_dir="$1"
    local java_files=$(find "$search_dir" -name "*.java" -type f | head -50)
    local checked_files=0

    log_info "检查缓存架构规范合规性..."

    for java_file in $java_files; do
        ((checked_files++))

        if [ $((checked_files % 10)) -eq 0 ]; then
            log_info "缓存检查进度: $checked_files"
        fi

        local file_content
        file_content=$(cat "$java_file" 2>/dev/null || continue)

        # 检查1: 直接使用RedisUtil（一级违规）
        if echo "$file_content" | grep -q "RedisUtil"; then
            if ! echo "$java_file" | grep -q "UnifiedCacheManager\|Test\|Config"; then
                log_category_error "cache" "[$java_file] ❌ 直接使用RedisUtil，违反缓存架构规范（一级规范）"
                echo "$file_content" | grep -n "RedisUtil" | head -2 | while read -r line; do
                    echo "    $line"
                done
            fi
        fi

        # 检查2: 直接使用RedisTemplate（一级违规）
        if echo "$file_content" | grep -q "RedisTemplate"; then
            if ! echo "$java_file" | grep -q "UnifiedCacheManager\|BaseModuleCacheService\|Test\|Config"; then
                log_category_error "cache" "[$java_file] ❌ 直接使用RedisTemplate，违反缓存架构规范（一级规范）"
                echo "$file_content" | grep -n "RedisTemplate" | head -2 | while read -r line; do
                    echo "    $line"
                done
            fi
        fi

        # 检查3: 缓存键命名规范（二级违规）
        if echo "$file_content" | grep -E "set\(.*,\".*:.*:.*\"\)|get\(.*,\".*:.*:.*\"\)" >/dev/null; then
            if ! echo "$file_content" | grep -q "iog:cache:" && ! echo "$java_file" | grep -q "UnifiedCacheManager\|Test\|Config"; then
                log_warning "[$java_file] ⚠️  缓存键格式不符合统一命名规范（二级规范）"
                echo "$file_content" | grep -n -E "set\(|get\(" | grep ":" | head -2 | while read -r line; do
                    echo "    $line"
                done
            fi
        fi
    done

    log_success "缓存架构规范验证完成，检查了 $checked_files 个文件"
}

# Phase 6: 文档一致性验证
validate_documentation_consistency() {
    log_phase "文档一致性验证"

    local required_docs=(
        "docs/repowiki/zh/content/开发规范体系/核心规范/架构设计规范.md"
        "docs/repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md"
        "docs/repowiki/zh/content/开发规范体系/核心规范/RESTfulAPI设计规范.md"
        "docs/repowiki/zh/content/开发规范体系/核心规范/系统安全规范.md"
        "docs/repowiki/zh/content/开发规范体系/核心规范/缓存架构规范.md"
        "docs/GLOBAL_STANDARDS_MATRIX.md"
        "CLAUDE.md"
        ".claude/skills/cache-architecture-specialist.md"
        ".claude/skills/spring-boot-jakarta-guardian.md"
        ".claude/skills/four-tier-architecture-guardian.md"
    )

    local missing_docs=0
    local total_docs=${#required_docs[@]}

    for doc in "${required_docs[@]}"; do
        if [ -f "$doc" ]; then
            log_success "✓ $doc"
        else
            log_category_error "documentation" "✗ $doc - 缺失关键文档"
            ((missing_docs++))
        fi
    done

    if [ $missing_docs -eq 0 ]; then
        log_success "文档一致性验证通过，所有 $total_docs 个关键文档都存在"
    else
        log_error "文档一致性验证失败，缺失 $missing_docs 个关键文档"
    fi
}

# Phase 7: 技能体系验证
validate_skills_system() {
    log_phase "技能体系验证"

    local skills_dir=".claude/skills"
    local required_skills=(
        "cache-architecture-specialist.md"
        "spring-boot-jakarta-guardian.md"
        "four-tier-architecture-guardian.md"
        "code-quality-protector.md"
    )

    local missing_skills=0

    for skill in "${required_skills[@]}"; do
        if [ -f "$skills_dir/$skill" ]; then
            log_success "✓ 技能文档: $skill"
        else
            log_category_error "documentation" "✗ 技能文档缺失: $skill"
            ((missing_skills++))
        fi
    done

    # 检查技能与规范的映射关系
    local cache_spec_exists=0
    local cache_skill_exists=0

    if [ -f "docs/repowiki/zh/content/开发规范体系/核心规范/缓存架构规范.md" ]; then
        ((cache_spec_exists++))
    fi

    if [ -f ".claude/skills/cache-architecture-specialist.md" ]; then
        ((cache_skill_exists++))
    fi

    if [ $cache_spec_exists -eq 1 ] && [ $cache_skill_exists -eq 1 ]; then
        log_success "✓ 缓存规范与技能文档映射正确"
    else
        log_warning "⚠️  缓存规范与技能文档映射不完整"
    fi

    log_success "技能体系验证完成"
}

# 生成综合验证报告
generate_comprehensive_report() {
    log_phase "生成综合验证报告"

    echo ""
    echo -e "${PURPLE}📊 ===== IOE-DREAM 综合规验证证报告 =====${NC}"
    echo ""

    echo -e "${BLUE}📈 验证统计：${NC}"
    echo "   🔍 验证阶段数: $VALIDATION_PHASES"
    echo "   📁 检查文件数: $TOTAL_FILES_CHECKED"
    echo "   ✅ 通过项目数: $PASS_COUNT"
    echo "   ❌ 失败项目数: $FAIL_COUNT"
    echo "   ⚠️  警告总数: $TOTAL_WARNINGS"
    echo "   ❌ 严重违规数: $TOTAL_VIOLATIONS"

    echo ""
    echo -e "${BLUE}📋 违规分类统计：${NC}"
    echo "   🏗️  架构设计违规: ${VIOLATION_CATEGORIES[architecture]}"
    echo "   💻 Java编码违规: ${VIOLATION_CATEGORIES[coding]}"
    echo "   🌐 API设计违规: ${VIOLATION_CATEGORIES[api]}"
    echo "   🔒 系统安全违规: ${VIOLATION_CATEGORIES[security]}"
    echo "   💾 缓存架构违规: ${VIOLATION_CATEGORIES[cache]}"
    echo "   📚 文档一致性违规: ${VIOLATION_CATEGORIES[documentation]}"

    echo ""
    echo -e "${BLUE}🎯 合规性分析：${NC}"
    if [ $TOTAL_VIOLATIONS -eq 0 ]; then
        echo "   🎉 恭喜！未发现一级规范严重违规"
        if [ $TOTAL_WARNINGS -eq 0 ]; then
            echo "   🏆 完美！未发现任何警告或问题"
            echo "   ✅ 项目完全符合所有repowiki核心规范"
        else
            echo "   💡 建议：处理 $TOTAL_WARNINGS 个警告问题以获得最佳实践"
            echo "   ✅ 项目符合所有一级规范，建议优化二级规范遵循"
        fi
    else
        echo "   🚨 严重违规！发现 $TOTAL_VIOLATIONS 个一级规范违规"
        echo "   📝 必须立即修复所有严重违规才能继续开发"
        echo "   📖 请参考 docs/GLOBAL_STANDARDS_MATRIX.md 进行修复"
    fi

    echo ""
    echo -e "${BLUE}📚 相关文档：${NC}"
    echo "   📖 全局规范矩阵: docs/GLOBAL_STANDARDS_MATRIX.md"
    echo "   🎓 repowiki规范: docs/repowiki/zh/content/开发规范体系/"
    echo "   💻 开发指南: CLAUDE.md"
    echo "   🛠️  技能体系: .claude/skills/"
    echo "   🔧 验证脚本: scripts/comprehensive-validation.sh"

    echo ""
    echo -e "${BLUE}🔄 持续改进：${NC}"
    echo "   🔍 每日执行: ./scripts/daily-compliance-report.sh"
    echo "   📊 周度回顾: ./scripts/weekly-quality-review.sh"
    echo "   🎯 季度优化: ./scripts/quarterly-standards-optimizer.sh"

    echo "============================================================================"
}

# 主执行流程
main() {
    local search_dir="${1:-"smart-admin-api-java17-springboot3"}"

    # 检查搜索目录
    if [ ! -d "$search_dir" ]; then
        log_error "搜索目录不存在: $search_dir"
        exit 1
    fi

    log_info "开始综合规验证证，搜索目录: $search_dir"

    # 执行所有验证阶段
    validate_architecture_standards "$search_dir"
    validate_java_coding_standards "$search_dir"
    validate_api_design_standards "$search_dir"
    validate_security_standards "$search_dir"
    validate_cache_architecture_standards "$search_dir"
    validate_documentation_consistency
    validate_skills_system

    # 生成综合报告
    generate_comprehensive_report

    # 返回结果
    if [ $TOTAL_VIOLATIONS -gt 0 ]; then
        echo ""
        log_error "🚨 验证失败！发现 $TOTAL_VIOLATIONS 个严重违规，请修复后重新验证"
        echo ""
        echo "📋 修复建议："
        echo "1. 查看详细违规信息"
        echo "2. 参考 docs/GLOBAL_STANDARDS_MATRIX.md"
        echo "3. 对应的repowiki核心规范文档"
        echo "4. 使用相应的技能专家工具"
        exit 1
    elif [ $TOTAL_WARNINGS -gt 0 ]; then
        echo ""
        log_warning "⚠️  验证通过但有 $TOTAL_WARNINGS 个警告，建议处理警告问题以获得最佳实践"
        exit 2
    else
        echo ""
        log_success "🎉 验证完全通过！项目100%符合所有repowiki核心规范和全局规范矩阵"
        exit 0
    fi
}

# 执行主函数
main "$@"