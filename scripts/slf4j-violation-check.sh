#!/bin/bash

# =============================================================================
# IOE-DREAM SLF4J 违规检查脚本
# =============================================================================
# 版本: v1.0.0
# 作者: IOE-DREAM架构委员会
# 功能: 自动检查SLF4J使用规范违规情况
# 使用: ./scripts/slf4j-violation-check.sh
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 统计变量
TOTAL_VIOLATIONS=0
FORMAT_VIOLATIONS=0
LOGGER_FACTORY_VIOLATIONS=0
STRING_CONCAT_VIOLATIONS=0
LOG_LEVEL_VIOLATIONS=0

# 输出函数
print_header() {
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${BLUE}🔍 IOE-DREAM SLF4J 违规检查报告${NC}"
    echo -e "${BLUE}============================================================================${NC}"
    echo ""
}

print_section() {
    echo -e "${YELLOW}📊 $1${NC}"
    echo -e "${YELLOW}----------------------------------------${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 检查1: LoggerFactory违规使用
check_logger_factory() {
    print_section "检查LoggerFactory违规使用"

    local files=$(find ./microservices -name "*.java" -exec grep -l "LoggerFactory.getLogger" {} \; 2>/dev/null)
    local count=$(echo "$files" | grep -c . || echo 0)

    if [ "$count" -eq 0 ]; then
        print_success "未发现LoggerFactory违规使用 (0个文件)"
    else
        print_error "发现${count}个文件仍在使用LoggerFactory"
        echo "$files" | while read -r file; do
            echo "  🔍 $file"
            # 查找具体行
            grep -n "LoggerFactory.getLogger" "$file" | sed 's/^/    /'
        done
        LOGGER_FACTORY_VIOLATIONS=$count
        TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + count))
    fi
    echo ""
}

# 检查2: getLogger格式问题（空格错误）
check_format_issues() {
    print_section "检查getLogger格式问题"

    local files=$(find ./microservices -name "*.java" -exec grep -l "getLogger\s*(" {} \; 2>/dev/null)
    local count=$(echo "$files" | grep -c . || echo 0)

    if [ "$count" -eq 0 ]; then
        print_success "未发现getLogger格式问题 (0个文件)"
    else
        print_warning "发现${count}个文件存在格式问题"
        echo "$files" | while read -r file; do
            echo "  🔍 $file"
            grep -n "getLogger\s*(" "$file" | sed 's/^/    /'
        done
        FORMAT_VIOLATIONS=$count
        TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + count))
    fi
    echo ""
}

# 检查3: 字符串拼接违规
check_string_concatenation() {
    print_section "检查字符串拼接违规"

    local files=$(find ./microservices -name "*.java" -exec grep -l "log\.\w\+.*\+.*log\." {} \; 2>/dev/null)
    local count=$(echo "$files" | grep -c . || echo 0)

    if [ "$count" -eq 0 ]; then
        print_success "未发现字符串拼接违规 (0个文件)"
    else
        print_error "发现${count}个文件存在字符串拼接"
        echo "$files" | while read -r file; do
            echo "  🔍 $file"
            grep -n "log\.\w\+.*\+.*log\." "$file" | sed 's/^/    /'
        done
        STRING_CONCAT_VIOLATIONS=$count
        TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + count))
    fi
    echo ""
}

# 检查4: 缺少模块标识的日志
check_missing_module_identifier() {
    print_section "检查缺少模块标识的日志"

    # 查找不以[开头的日志调用
    local files=$(find ./microservices -name "*.java" -exec grep -l "log\.\w\+\s*(" {} \; 2>/dev/null | xargs grep -L "log\.\w\+\s*(\"\[" 2>/dev/null)
    local count=$(echo "$files" | grep -c . || echo 0)

    if [ "$count" -eq 0 ]; then
        print_success "未发现缺少模块标识的日志 (0个文件)"
    else
        print_warning "发现${count}个文件可能缺少模块标识"
        echo "$files" | head -10 | while read -r file; do
            echo "  🔍 $file"
            # 查找可能的违规日志（不以[开头的）
            grep -n "log\.\w\+\s*(\"[^\[]" "$file" | head -5 | sed 's/^/    /'
        done

        if [ "$count" -gt 10 ]; then
            echo "  ... 还有$((count - 10))个文件未显示"
        fi
        LOG_LEVEL_VIOLATIONS=$count
        TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + count))
    fi
    echo ""
}

# 检查5: @Slf4j注解使用情况
check_slf4j_annotation() {
    print_section "@Slf4j注解使用统计"

    local slf4j_files=$(find ./microservices -name "*.java" -exec grep -l "@Slf4j" {} \; 2>/dev/null)
    local slf4j_count=$(echo "$slf4j_files" | grep -c . || echo 0)

    local total_java_files=$(find ./microservices -name "*.java" -type f 2>/dev/null | wc -l)
    local usage_rate=$((slf4j_count * 100 / total_java_files))

    echo "  📈 总Java文件数: $total_java_files"
    echo "  📈 使用@Slf4j文件数: $slf4j_count"
    echo "  📈 使用率: ${usage_rate}%"

    if [ "$usage_rate" -ge 90 ]; then
        print_success "@Slf4j使用率达到企业级标准 (${usage_rate}%)"
    elif [ "$usage_rate" -ge 80 ]; then
        print_warning "@Slf4j使用率良好，但仍有提升空间 (${usage_rate}%)"
    else
        print_error "@Slf4j使用率偏低，需要改进 (${usage_rate}%)"
    fi
    echo ""
}

# 检查6: 各微服务详细统计
check_microservice_stats() {
    print_section "各微服务SLF4J使用统计"

    local services=("access-service" "attendance-service" "consume-service" "oa-service" "video-service" "visitor-service")

    printf "%-20s %-10s %-10s %-15s %-10s\n" "微服务" "@Slf4j" "LoggerFactory" "一致性" "评分"
    echo "--------------------------------------------------------------------------------"

    for service in "${services[@]}"; do
        local slf4j_count=$(find "./microservices/ioedream-$service" -name "*.java" -exec grep -l "@Slf4j" {} \; 2>/dev/null | wc -l)
        local factory_count=$(find "./microservices/ioedream-$service" -name "*.Java" -exec grep -l "LoggerFactory.getLogger" {} \; 2>/dev/null | wc -l)
        local total=$((slf4j_count + factory_count))

        local consistency_rate=0
        local score="A+"

        if [ "$total" -gt 0 ]; then
            consistency_rate=$((slf4j_count * 100 / total))

            if [ "$consistency_rate" -eq 100 ]; then
                score="A+"
            elif [ "$consistency_rate" -ge 95 ]; then
                score="A"
            elif [ "$consistency_rate" -ge 90 ]; then
                score="B+"
            elif [ "$consistency_rate" -ge 80 ]; then
                score="B"
            else
                score="C"
            fi
        else
            consistency_rate=100
            score="N/A"
        fi

        printf "%-20s %-10s %-10s %-15s %-10s\n" "$service" "$slf4j_count" "$factory_count" "${consistency_rate}%" "$score"
    done
    echo ""
}

# 生成最终报告
generate_final_report() {
    print_section "最终检查报告"

    echo "  📊 总违规数量: $TOTAL_VIOLATIONS"
    echo "  📊 LoggerFactory违规: $LOGGER_FACTORY_VIOLATIONS"
    echo "  📊 格式问题违规: $FORMAT_VIOLATIONS"
    echo "  📊 字符串拼接违规: $STRING_CONCAT_VIOLATIONS"
    echo "  📊 模块标识问题: $LOG_LEVEL_VIOLATIONS"
    echo ""

    if [ "$TOTAL_VIOLATIONS" -eq 0 ]; then
        print_success "🎉 恭喜！项目完全符合SLF4J使用规范"
        echo "    - 无任何违规使用"
        echo "    - 代码质量达到企业级标准"
        echo "    - 可以安全提交到主分支"
    elif [ "$TOTAL_VIOLATIONS" -le 5 ]; then
        print_warning "项目基本符合规范，存在少量违规需要修复"
        echo "    - 违规数量: $TOTAL_VIOLATIONS"
        echo "    - 建议修复后提交"
    else
        print_error "项目存在较多违规，必须修复后才能提交"
        echo "    - 违规数量: $TOTAL_VIOLATIONS"
        echo "    - 请立即修复所有违规问题"
        echo "    - 参考: documentation/technical/SLF4J_UNIFIED_STANDARD.md"
    fi
    echo ""
}

# 输出修复建议
show_fix_suggestions() {
    print_section "修复建议"

    if [ "$LOGGER_FACTORY_VIOLATIONS" -gt 0 ]; then
        echo "🔧 LoggerFactory修复建议:"
        echo "   1. 删除 import org.slf4j.Logger;"
        echo "   2. 删除 import org.slf4j.LoggerFactory;"
        echo "   3. 添加 import lombok.extern.slf4j.Slf4j;"
        echo "   4. 删除 private static final Logger log = LoggerFactory.getLogger(...);"
        echo "   5. 在类上添加 @Slf4j 注解"
        echo ""
    fi

    if [ "$FORMAT_VIOLATIONS" -gt 0 ]; then
        echo "🔧 格式问题修复建议:"
        echo "   1. 修复 getLogger ( ) 为 getLogger()"
        echo "   2. 删除方法名和括号间的不必要空格"
        echo ""
    fi

    if [ "$STRING_CONCAT_VIOLATIONS" -gt 0 ]; then
        echo "🔧 字符串拼接修复建议:"
        echo "   1. 使用参数化日志: log.info(\"message: {}\", value)"
        echo "   2. 避免字符串拼接: log.info(\"message: \" + value)"
        echo ""
    fi
}

# 主函数
main() {
    print_header

    echo "🚀 开始检查..."
    echo ""

    # 执行各项检查
    check_logger_factory
    check_format_issues
    check_string_concatenation
    check_missing_module_identifier
    check_slf4j_annotation
    check_microservice_stats

    # 生成报告
    generate_final_report
    show_fix_suggestions

    print_header
    echo "📅 检查完成时间: $(date)"
    echo "📋 详细规范文档: documentation/technical/SLF4J_UNIFIED_STANDARD.md"
    echo ""

    # 返回适当的退出码
    if [ "$TOTAL_VIOLATIONS" -gt 0 ]; then
        exit 1
    else
        exit 0
    fi
}

# 执行主函数
main "$@"