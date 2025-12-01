#!/bin/bash

# =============================================================================
# 多维度规范合规性检查系统
# =============================================================================
#
# 功能：提供全面的、多维度的规范合规性检查
# 覆盖代码质量、架构合规、安全性、性能、可维护性等多个维度
# 版本：v1.0
# 更新时间：2025-11-17
#
# 使用方法：
#   ./scripts/multi-dimensional-compliance-check.sh [options] [path]
#
# 选项：
#   --dimension=NAME    仅检查指定维度
#   --threshold=NUM     设置合规阈值 (默认: 90)
#   --output=FORMAT     输出格式 (json|html|markdown)
#   --severity=LEVEL    严重级别 (low|medium|high|critical)
#
# 维度列表：
#   architecture       - 架构设计合规性
#   coding            - 编码质量合规性
#   security          - 安全规范合规性
#   api              - API设计合规性
#   cache             - 缓存架构合规性
#   performance       - 性能规范合规性
#   documentation     - 文档完整性合规性
#   testing           - 测试覆盖率合规性
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

# 配置参数
TARGET_PATH="${1:-smart-admin-api-java17-springboot3}"
TARGET_DIMENSION=""
COMPLIANCE_THRESHOLD=90
OUTPUT_FORMAT="markdown"
SEVERITY_LEVEL="medium"
TOTAL_SCORE=0
DIMENSION_COUNT=0

# 维度权重
declare -A DIMENSION_WEIGHTS
DIMENSION_WEIGHTS[architecture]=25
DIMENSION_WEIGHTS[coding]=20
DIMENSION_WEIGHTS[security]=20
DIMENSION_WEIGHTS[api]=15
DIMENSION_WEIGHTS[cache]=10
DIMENSION_WEIGHTS[performance]=5
DIMENSION_WEIGHTS[documentation]=3
DIMENSION_WEIGHTS[testing]=2

# 维度得分
declare -A DIMENSION_SCORES
declare -A DIMENSION_ISSUES
declare -A DIMENSION_WARNINGS

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
}

log_dimension() {
    echo -e "${PURPLE}🎯 DIMENSION: $1${NC}"
}

log_score() {
    local score="$1"
    local dimension="$2"
    if [ "$score" -ge 90 ]; then
        echo -e "${GREEN}🏆 $dimension: $score分 (优秀)${NC}"
    elif [ "$score" -ge 80 ]; then
        echo -e "${YELLOW}🥈 $dimension: $score分 (良好)${NC}"
    elif [ "$score" -ge 70 ]; then
        echo -e "${YELLOW}🥉 $dimension: $score分 (一般)${NC}"
    else
        echo -e "${RED}❌ $dimension: $score分 (不合格)${NC}"
    fi
}

echo -e "${BLUE}"
echo "============================================================================"
echo "🔍 IOE-DREAM 多维度规范合规性检查系统 v1.0"
echo "📊 基于repowiki核心规范的全面合规性评估"
echo "🎯 检查路径: $TARGET_PATH"
echo "⏰ 执行时间: $(date)"
echo "============================================================================"
echo -e "${NC}"

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        --dimension=*)
            TARGET_DIMENSION="${1#*=}"
            shift
            ;;
        --threshold=*)
            COMPLIANCE_THRESHOLD="${1#*=}"
            shift
            ;;
        --output=*)
            OUTPUT_FORMAT="${1#*=}"
            shift
            ;;
        --severity=*)
            SEVERITY_LEVEL="${1#*=}"
            shift
            ;;
        *)
            if [ "$1" != "${TARGET_PATH}" ]; then
                TARGET_PATH="$1"
            fi
            shift
            ;;
    esac
done

# 检查目标路径
if [ ! -d "$TARGET_PATH" ]; then
    log_error "目标路径不存在: $TARGET_PATH"
    exit 1
fi

# 维度1: 架构设计合规性检查
check_architecture_compliance() {
    log_dimension "架构设计合规性检查"

    local java_files=$(find "$TARGET_PATH" -name "*.java" -type f | head -50)
    local total_files=$(echo "$java_files" | wc -l)
    local violations=0
    local warnings=0

    # 检查四层架构合规性
    log_info "检查四层架构合规性..."

    for java_file in $java_files; do
        local file_content=$(cat "$java_file" 2>/dev/null || continue)

        # 严重违规 (扣10分)
        if echo "$file_content" | grep -q "@Controller" && echo "$file_content" | grep -q "@Resource.*Dao"; then
            ((violations++))
            log_error "架构违规: Controller直接访问DAO - $(basename "$java_file")"
        fi

        if echo "$file_content" | grep -q "@Service" && echo "$file_content" | grep -q "@Resource.*JdbcTemplate"; then
            ((violations++))
            log_error "架构违规: Service直接访问数据库 - $(basename "$java_file")"
        fi

        # 警告 (扣5分)
        if echo "$file_content" | grep -q "@Controller" && ! echo "$file_content" | grep -q "@Resource.*Service"; then
            ((warnings++))
            log_warning "架构警告: Controller缺少Service依赖 - $(basename "$java_file")"
        fi
    done

    # 计算得分
    local max_deduction=$((violations * 10 + warnings * 5))
    local base_score=100
    local score=$((base_score - max_deduction))
    [ $score -lt 0 ] && score=0

    DIMENSION_SCORES[architecture]=$score
    DIMENSION_ISSUES[architecture]=$violations
    DIMENSION_WARNINGS[architecture]=$warnings

    log_score "$score" "架构设计合规性"
    log_info "检查了 $total_files 个Java文件，发现 $violations 个严重违规，$warnings 个警告"
}

# 维度2: 编码质量合规性检查
check_coding_compliance() {
    log_dimension "编码质量合规性检查"

    local java_files=$(find "$TARGET_PATH" -name "*.java" -type f | head -50)
    local total_files=$(echo "$java_files" | wc -l)
    local violations=0
    local warnings=0

    log_info "检查Java编码规范合规性..."

    for java_file in $java_files; do
        local file_content=$(cat "$java_file" 2>/dev/null || continue)

        # 严重违规 (扣10分)
        if echo "$file_content" | grep -q "@Autowired"; then
            ((violations++))
            log_error "编码违规: 使用@Autowired，必须使用@Resource - $(basename "$java_file")"
        fi

        if echo "$file_content" | grep -q "import javax\." && ! echo "$file_content" | grep -qE "import javax\.(crypto|net|security|naming)"; then
            ((violations++))
            log_error "编码违规: 使用javax包，必须使用jakarta - $(basename "$java_file")"
        fi

        if echo "$file_content" | grep -q "System\.out\.print"; then
            ((violations++))
            log_error "编码违规: 使用System.out.println，必须使用日志框架 - $(basename "$java_file")"
        fi

        # 警告 (扣5分)
        if echo "$file_content" | grep -E "\b(?!1|0|2|10|100)[0-9]{2,}\b" | grep -v -E "//|/\*|\*"; then
            if ! echo "$java_file" | grep -q "Test\|Config\|Constant"; then
                ((warnings++))
                log_warning "编码警告: 可能存在魔法数字 - $(basename "$java_file")"
            fi
        fi

        if echo "$file_content" | grep -E '"[^"]{50,}"' | grep -v -E "TODO|FIXME|Author|Since"; then
            ((warnings++))
            log_warning "编码警告: 存在过长的硬编码字符串 - $(basename "$java_file")"
        fi
    done

    # 计算得分
    local max_deduction=$((violations * 10 + warnings * 5))
    local base_score=100
    local score=$((base_score - max_deduction))
    [ $score -lt 0 ] && score=0

    DIMENSION_SCORES[coding]=$score
    DIMENSION_ISSUES[coding]=$violations
    DIMENSION_WARNINGS[coding]=$warnings

    log_score "$score" "编码质量合规性"
    log_info "检查了 $total_files 个Java文件，发现 $violations 个严重违规，$warnings 个警告"
}

# 维度3: 安全规范合规性检查
check_security_compliance() {
    log_dimension "安全规范合规性检查"

    local java_files=$(find "$TARGET_PATH" -name "*.java" -type f | head -50)
    local total_files=$(echo "$java_files" | wc -l)
    local violations=0
    local warnings=0

    log_info "检查系统安全规范合规性..."

    for java_file in $java_files; do
        local file_content=$(cat "$java_file" 2>/dev/null || continue)

        # 严重违规 (扣10分)
        if echo "$file_content" | grep -E "(password|secret|key|token).*=.*['\"]" -i; then
            if ! echo "$java_file" | grep -q "Test\|Config\|Example"; then
                ((violations++))
                log_error "安全违规: 疑似硬编码敏感信息 - $(basename "$java_file")"
            fi
        fi

        if echo "$file_content" | grep -E "(\+|concat).*['\"]\s*\+" && echo "$file_content" | grep -q "select\|UPDATE\|DELETE\|INSERT"; then
            if ! echo "$java_file" | grep -q "Test\|Example"; then
                ((violations++))
                log_error "安全违规: 存在SQL注入风险 - $(basename "$java_file")"
            fi
        fi

        # 警告 (扣5分)
        if echo "$file_content" | grep -q "@Controller\|@RestController" && ! echo "$file_content" | grep -q "@SaCheckPermission\|@SaCheckLogin"; then
            if echo "$file_content" | grep -q "@PostMapping\|@PutMapping\|@DeleteMapping"; then
                ((warnings++))
                log_warning "安全警告: Controller缺少Sa-Token权限验证 - $(basename "$java_file")"
            fi
        fi

        if echo "$file_content" | grep -E "log\.(info|debug|error|warn).*password\|secret\|token" -i; then
            ((warnings++))
            log_warning "安全警告: 日志中可能包含敏感信息 - $(basename "$java_file")"
        fi
    done

    # 计算得分
    local max_deduction=$((violations * 10 + warnings * 5))
    local base_score=100
    local score=$((base_score - max_deduction))
    [ $score -lt 0 ] && score=0

    DIMENSION_SCORES[security]=$score
    DIMENSION_ISSUES[security]=$violations
    DIMENSION_WARNINGS[security]=$warnings

    log_score "$score" "安全规范合规性"
    log_info "检查了 $total_files 个Java文件，发现 $violations 个严重违规，$warnings 个警告"
}

# 维度4: API设计合规性检查
check_api_compliance() {
    log_dimension "API设计合规性检查"

    local controller_files=$(find "$TARGET_PATH" -name "*Controller.java" -type f | head -30)
    local total_files=$(echo "$controller_files" | wc -l)
    local violations=0
    local warnings=0

    log_info "检查RESTful API设计规范合规性..."

    for controller_file in $controller_files; do
        local file_content=$(cat "$controller_file" 2>/dev/null || continue)

        # 严重违规 (扣10分)
        if echo "$file_content" | grep -E "@GetMapping.*(/save|/create|/update|/delete|/add|/remove|/edit)" >/dev/null; then
            ((violations++))
            log_error "API违规: GET方法用于数据修改 - $(basename "$controller_file")"
        fi

        if echo "$file_content" | grep -E "@(Post|Put|Delete)Mapping" && ! echo "$file_content" | grep -q "@SaCheckPermission\|@SaCheckRole"; then
            ((violations++))
            log_error "API违规: 修改接口缺少权限验证 - $(basename "$controller_file")"
        fi

        # 警告 (扣5分)
        if echo "$file_content" | grep -E "@(Post|Put)Mapping" && ! echo "$file_content" | grep -q "@Valid\|@Validated"; then
            ((warnings++))
            log_warning "API警告: POST/PUT接口缺少@Valid参数验证 - $(basename "$controller_file")"
        fi

        if ! echo "$file_content" | grep -q "@ApiOperation\|@Tag"; then
            ((warnings++))
            log_warning "API警告: 缺少Knife4j API文档注解 - $(basename "$controller_file")"
        fi

        if echo "$file_content" | grep -E "@(Get|Post|Put|Delete)Mapping" && ! echo "$file_content" | grep -q "ResponseDTO"; then
            ((warnings++))
            log_warning "API警告: 未使用统一ResponseDTO响应格式 - $(basename "$controller_file")"
        fi
    done

    # 计算得分
    local max_deduction=$((violations * 10 + warnings * 5))
    local base_score=100
    local score=$((base_score - max_deduction))
    [ $score -lt 0 ] && score=0

    DIMENSION_SCORES[api]=$score
    DIMENSION_ISSUES[api]=$violations
    DIMENSION_WARNINGS[api]=$warnings

    log_score "$score" "API设计合规性"
    log_info "检查了 $total_files 个Controller文件，发现 $violations 个严重违规，$warnings 个警告"
}

# 维度5: 缓存架构合规性检查
check_cache_compliance() {
    log_dimension "缓存架构合规性检查"

    local java_files=$(find "$TARGET_PATH" -name "*.java" -type f | head -50)
    local total_files=$(echo "$java_files" | wc -l)
    local violations=0
    local warnings=0

    log_info "检查缓存架构规范合规性..."

    for java_file in $java_files; do
        local file_content=$(cat "$java_file" 2>/dev/null || continue)

        # 严重违规 (扣10分)
        if echo "$file_content" | grep -q "RedisUtil"; then
            if ! echo "$java_file" | grep -q "UnifiedCacheManager\|Test\|Config"; then
                ((violations++))
                log_error "缓存违规: 直接使用RedisUtil - $(basename "$java_file")"
            fi
        fi

        if echo "$file_content" | grep -q "RedisTemplate"; then
            if ! echo "$java_file" | grep -q "UnifiedCacheManager\|BaseModuleCacheService\|Test\|Config"; then
                ((violations++))
                log_error "缓存违规: 直接使用RedisTemplate - $(basename "$java_file")"
            fi
        fi

        # 警告 (扣5分)
        if echo "$file_content" | grep -E "set\(.*,\".*:.*:.*\"\)|get\(.*,\".*:.*:.*\"\)" >/dev/null; then
            if ! echo "$file_content" | grep -q "iog:cache:" && ! echo "$java_file" | grep -q "UnifiedCacheManager\|Test\|Config"; then
                ((warnings++))
                log_warning "缓存警告: 缓存键格式不符合统一命名规范 - $(basename "$java_file")"
            fi
        fi
    done

    # 计算得分
    local max_deduction=$((violations * 10 + warnings * 5))
    local base_score=100
    local score=$((base_score - max_deduction))
    [ $score -lt 0 ] && score=0

    DIMENSION_SCORES[cache]=$score
    DIMENSION_ISSUES[cache]=$violations
    DIMENSION_WARNINGS[cache]=$warnings

    log_score "$score" "缓存架构合规性"
    log_info "检查了 $total_files 个Java文件，发现 $violations 个严重违规，$warnings 个警告"
}

# 维度6: 性能规范合规性检查
check_performance_compliance() {
    log_dimension "性能规范合规性检查"

    local java_files=$(find "$TARGET_PATH" -name "*.java" -type f | head -30)
    local violations=0
    local warnings=0

    log_info "检查性能规范合规性..."

    for java_file in $java_files; do
        local file_content=$(cat "$java_file" 2>/dev/null || continue)

        # 警告 (扣5分)
        if echo "$file_content" | grep -q "for.*:" && echo "$file_content" | grep -q "SELECT.*\*"; then
            if ! echo "$java_file" | grep -q "Test"; then
                ((warnings++))
                log_warning "性能警告: 循环中可能存在N+1查询问题 - $(basename "$java_file")"
            fi
        fi

        if echo "$file_content" | grep -q "String.*+\+.*String.*+" && ! echo "$file_content" | grep -q "StringBuilder\|StringBuffer"; then
            ((warnings++))
            log_warning "性能警告: 字符串拼接可能存在性能问题 - $(basename "$java_file")"
        fi
    done

    # 计算得分
    local max_deduction=$((violations * 10 + warnings * 5))
    local base_score=100
    local score=$((base_score - max_deduction))
    [ $score -lt 0 ] && score=0

    DIMENSION_SCORES[performance]=$score
    DIMENSION_ISSUES[performance]=$violations
    DIMENSION_WARNINGS[performance]=$warnings

    log_score "$score" "性能规范合规性"
    log_info "检查性能相关问题，发现 $violations 个严重违规，$warnings 个警告"
}

# 维度7: 文档完整性合规性检查
check_documentation_compliance() {
    log_dimension "文档完整性合规性检查"

    local violations=0
    local warnings=0

    log_info "检查文档完整性..."

    # 检查核心文档存在性
    local required_docs=(
        "docs/repowiki/zh/content/开发规范体系/核心规范/架构设计规范.md"
        "docs/repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md"
        "docs/repowiki/zh/content/开发规范体系/核心规范/系统安全规范.md"
        "docs/repowiki/zh/content/开发规范体系/核心规范/缓存架构规范.md"
        "docs/GLOBAL_STANDARDS_MATRIX.md"
        "CLAUDE.md"
    )

    for doc in "${required_docs[@]}"; do
        if [ -f "$doc" ]; then
            log_success "✓ $doc"
        else
            ((violations++))
            log_error "缺失核心文档: $doc"
        fi
    done

    # 检查技能文档存在性
    local required_skills=(
        ".claude/skills/cache-architecture-specialist.md"
        ".claude/skills/spring-boot-jakarta-guardian.md"
        ".claude/skills/four-tier-architecture-guardian.md"
        ".claude/skills/code-quality-protector.md"
    )

    for skill in "${required_skills[@]}"; do
        if [ -f "$skill" ]; then
            log_success "✓ $skill"
        else
            ((warnings++))
            log_warning "缺失技能文档: $skill"
        fi
    done

    # 计算得分
    local max_deduction=$((violations * 10 + warnings * 5))
    local base_score=100
    local score=$((base_score - max_deduction))
    [ $score -lt 0 ] && score=0

    DIMENSION_SCORES[documentation]=$score
    DIMENSION_ISSUES[documentation]=$violations
    DIMENSION_WARNINGS[documentation]=$warnings

    log_score "$score" "文档完整性合规性"
    log_info "检查文档完整性，发现 $violations 个严重违规，$warnings 个警告"
}

# 维度8: 测试覆盖率合规性检查
check_testing_compliance() {
    log_dimension "测试覆盖率合规性检查"

    local violations=0
    local warnings=0

    log_info "检查测试覆盖率..."

    # 检查测试文件存在性
    local main_classes=$(find "$TARGET_PATH" -name "*.java" -path "*/src/main/*" | grep -v Test | wc -l)
    local test_classes=$(find "$TARGET_PATH" -name "*Test.java" -path "*/src/test/*" 2>/dev/null | wc -l)

    log_info "主要类数量: $main_classes"
    log_info "测试类数量: $test_classes"

    # 计算测试覆盖率
    local coverage=0
    if [ $main_classes -gt 0 ]; then
        coverage=$((test_classes * 100 / main_classes))
    fi

    log_info "测试覆盖率: $coverage%"

    if [ $coverage -lt 80 ]; then
        ((violations++))
        log_error "测试覆盖率低于80%: $coverage%"
    elif [ $coverage -lt 90 ]; then
        ((warnings++))
        log_warning "测试覆盖率未达90%: $coverage%"
    fi

    # 计算得分
    local score=$coverage
    [ $score -gt 100 ] && score=100

    DIMENSION_SCORES[testing]=$score
    DIMENSION_ISSUES[testing]=$violations
    DIMENSION_WARNINGS[testing]=$warnings

    log_score "$score" "测试覆盖率合规性"
    log_info "测试覆盖率检查，发现 $violations 个严重违规，$warnings 个警告"
}

# 计算总分
calculate_overall_score() {
    log_dimension "计算总分"

    local weighted_sum=0
    local total_weight=0

    for dimension in "${!DIMENSION_SCORES[@]}"; do
        local score=${DIMENSION_SCORES[$dimension]}
        local weight=${DIMENSION_WEIGHTS[$dimension]}

        local weighted_score=$((score * weight / 100))
        weighted_sum=$((weighted_sum + weighted_score))
        total_weight=$((total_weight + weight))

        log_info "$dimension: 得分=$score, 权重=$weight%, 加权分=$weighted_score"
    done

    if [ $total_weight -gt 0 ]; then
        TOTAL_SCORE=$((weighted_sum * 100 / total_weight))
    else
        TOTAL_SCORE=0
    fi

    DIMENSION_COUNT=${#DIMENSION_SCORES[@]}

    log_score "$TOTAL_SCORE" "总分"
}

# 生成合规性报告
generate_compliance_report() {
    echo ""
    echo -e "${PURPLE}📊 ===== 多维度规范合规性报告 =====${NC}"
    echo ""

    echo -e "${BLUE}🎯 总体评分: ${TOTAL_SCORE}/100${NC}"

    if [ $TOTAL_SCORE -ge $COMPLIANCE_THRESHOLD ]; then
        echo -e "${GREEN}🎉 合规状态: 通过 (阈值: $COMPLIANCE_THRESHOLD%)${NC}"
    else
        echo -e "${RED}❌ 合规状态: 不通过 (阈值: $COMPLIANCE_THRESHOLD%)${NC}"
    fi

    echo ""
    echo -e "${BLUE}📋 各维度详细评分:${NC}"

    for dimension in "${!DIMENSION_SCORES[@]}"; do
        local score=${DIMENSION_SCORES[$dimension]}
        local issues=${DIMENSION_ISSUES[$dimension]}
        local warnings=${DIMENSION_WARNINGS[$dimension]}
        local weight=${DIMENSION_WEIGHTS[$dimension]}

        echo "   🎯 $dimension: $score分 (权重: $weight%, 严重违规: $issues, 警告: $warnings)"
    done

    echo ""
    echo -e "${BLUE}📈 统计信息:${NC}"
    echo "   📊 检查维度数: $DIMENSION_COUNT"
    echo "   ❌ 总严重违规数: $(for v in "${DIMENSION_ISSUES[@]}"; do echo -n "$v "; done | wc -w)"
    echo "   ⚠️  总警告数: $(for w in "${DIMENSION_WARNINGS[@]}"; do echo -n "$w "; done | wc -w)"
    echo "   🎯 合规阈值: $COMPLIANCE_THRESHOLD%"

    # 输出改进建议
    echo ""
    echo -e "${BLUE}💡 改进建议:${NC}"

    for dimension in "${!DIMENSION_SCORES[@]}"; do
        local score=${DIMENSION_SCORES[$dimension]}
        local issues=${DIMENSION_ISSUES[$dimension]}

        if [ $score -lt 80 ]; then
            echo "   🚨 $dimension 需要立即改进 (得分: $score)"
            echo "      建议: 重点关注严重违规问题，遵循对应的repowiki核心规范"
        elif [ $score -lt 90 ]; then
            echo "   ⚠️  $dimension 建议优化 (得分: $score)"
            echo "      建议: 处理警告问题，提升代码质量和规范遵循度"
        fi
    done

    echo ""
    echo -e "${BLUE}📚 相关文档:${NC}"
    echo "   📖 全局规范矩阵: docs/GLOBAL_STANDARDS_MATRIX.md"
    echo "   🎓 repowiki规范: docs/repowiki/zh/content/开发规范体系/"
    echo "   💻 开发指南: CLAUDE.md"
    echo "   🛠️  技能体系: .claude/skills/"
    echo "   🔧 验证工具: scripts/"

    echo "============================================================================"
}

# 输出到不同格式
output_results() {
    case "$OUTPUT_FORMAT" in
        "json")
            echo '{"overall_score":'$TOTAL_SCORE',"threshold":'$COMPLIANCE_THRESHOLD',"compliant":'$([ $TOTAL_SCORE -ge $COMPLIANCE_THRESHOLD ] && echo "true" || echo "false")',"dimensions":{'
            local first=true
            for dimension in "${!DIMENSION_SCORES[@]}"; do
                if [ "$first" = false ]; then echo ","; fi
                echo -n '"'$dimension'":{'"score":${DIMENSION_SCORES[$dimension]}',"issues":${DIMENSION_ISSUES[$dimension]}',"warnings":${DIMENSION_WARNINGS[$dimension]}',"weight":${DIMENSION_WEIGHTS[$dimension]}}'
                first=false
            done
            echo '}}'
            ;;
        "html")
            echo "<html><head><title>合规性报告</title></head><body>"
            echo "<h1>IOE-DREAM 多维度规范合规性报告</h1>"
            echo "<h2>总体评分: $TOTAL_SCORE/100</h2>"
            echo "<h2>各维度评分:</h2><ul>"
            for dimension in "${!DIMENSION_SCORES[@]}"; do
                echo "<li>$dimension: ${DIMENSION_SCORES[$dimension]}分</li>"
            done
            echo "</ul></body></html>"
            ;;
        *)
            # 默认markdown格式，已在generate_compliance_report中实现
            ;;
    esac
}

# 主执行逻辑
main() {
    log_info "开始多维度规范合规性检查..."

    # 执行所有维度检查（或指定维度）
    if [ -n "$TARGET_DIMENSION" ]; then
        case "$TARGET_DIMENSION" in
            "architecture") check_architecture_compliance ;;
            "coding") check_coding_compliance ;;
            "security") check_security_compliance ;;
            "api") check_api_compliance ;;
            "cache") check_cache_compliance ;;
            "performance") check_performance_compliance ;;
            "documentation") check_documentation_compliance ;;
            "testing") check_testing_compliance ;;
            *)
                log_error "未知维度: $TARGET_DIMENSION"
                log_info "可用维度: architecture, coding, security, api, cache, performance, documentation, testing"
                exit 1
                ;;
        esac
    else
        check_architecture_compliance
        check_coding_compliance
        check_security_compliance
        check_api_compliance
        check_cache_compliance
        check_performance_compliance
        check_documentation_compliance
        check_testing_compliance
    fi

    # 计算总分
    calculate_overall_score

    # 生成报告
    generate_compliance_report

    # 输出结果
    if [ "$OUTPUT_FORMAT" != "markdown" ]; then
        output_results
    fi

    # 返回结果
    if [ $TOTAL_SCORE -ge $COMPLIANCE_THRESHOLD ]; then
        echo ""
        log_success "🎉 多维度规范合规性检查通过！总分: $TOTAL_SCORE (阈值: $COMPLIANCE_THRESHOLD)"
        exit 0
    else
        echo ""
        log_error "❌ 多维度规范合规性检查未通过！总分: $TOTAL_SCORE (阈值: $COMPLIANCE_THRESHOLD)"
        echo "请根据改进建议优化项目规范遵循度。"
        exit 1
    fi
}

# 执行主函数
main "$@"