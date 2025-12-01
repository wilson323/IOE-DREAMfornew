#!/bin/bash

# =============================================================================
# 缓存架构规范验证脚本
# =============================================================================
#
# 功能：验证代码是否严格遵循repowiki缓存架构规范
# 基于文档：docs/repowiki/zh/content/开发规范体系/核心规范/缓存架构规范.md
# 版本：v1.0
# 更新时间：2025-11-17
#
# 使用方法：
#   ./scripts/cache-architecture-validation.sh
#
# 返回值：
#   0 - 验证通过（符合规范）
#   1 - 验证失败（违反规范）
#
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 计数器
VIOLATION_COUNT=0
WARNING_COUNT=0
TOTAL_FILES_CHECKED=0

# 日志函数
log_info() {
    echo -e "${BLUE}ℹ️  INFO: $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ SUCCESS: $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  WARNING: $1${NC}"
    ((WARNING_COUNT++))
}

log_error() {
    echo -e "${RED}❌ ERROR: $1${NC}"
    ((VIOLATION_COUNT++))
}

log_header() {
    echo -e "${BLUE}🔍 ===== $1 =====${NC}"
}

# 检查结果统计
PASS_COUNT=0
FAIL_COUNT=0

echo -e "${BLUE}"
echo "============================================================"
echo "🏗️  IOE-DREAM 缓存架构规范验证脚本"
echo "📋 基于repowiki缓存架构规范 v1.0 (2025-11-17)"
echo "🎯 验证范围：${1:-"src/main/java"}"
echo "⏰ 执行时间: $(date)"
echo "============================================================"
echo -e "${NC}"

# 验证函数
validate_java_files() {
    local search_dir="$1"

    log_header "Phase 1: 搜索Java文件"

    # 查找所有Java文件
    JAVA_FILES=$(find "$search_dir" -name "*.java" -type f | head -200)
    TOTAL_FILES_CHECKED=$(echo "$JAVA_FILES" | wc -l)

    log_info "找到 $TOTAL_FILES_CHECKED 个Java文件待检查"

    if [ "$TOTAL_FILES_CHECKED" -eq 0 ]; then
        log_warning "未找到Java文件，请检查路径"
        return 1
    fi

    echo ""
    log_header "Phase 2: 执行缓存架构规范验证"

    local file_count=0

    for java_file in $JAVA_FILES; do
        ((file_count++))

        # 显示进度
        if [ $((file_count % 20)) -eq 0 ]; then
            log_info "进度: $file_count/$TOTAL_FILES_CHECKED"
        fi

        validate_single_file "$java_file"
    done

    echo ""
    log_info "验证完成，共检查 $file_count 个文件"
}

validate_single_file() {
    local java_file="$1"
    local file_violations=0

    # 读取文件内容
    local file_content
    if ! file_content=$(cat "$java_file" 2>/dev/null); then
        log_warning "无法读取文件: $java_file"
        return
    fi

    # 检查1：禁止直接使用RedisUtil（一级规范）
    if echo "$file_content" | grep -q "RedisUtil"; then
        log_error "[$java_file] ❌ 检测到RedisUtil使用，违反一级规范（禁止直接使用底层缓存工具）"
        echo "$file_content" | grep -n "RedisUtil" | head -3 | while read -r line; do
            echo "    $line"
        done
        ((file_violations++))
    fi

    # 检查2：禁止直接使用RedisTemplate（一级规范）
    if echo "$file_content" | grep -q "RedisTemplate"; then
        # 排除配置文件和统一缓存管理器本身
        if ! echo "$java_file" | grep -q "UnifiedCacheManager\|Config\|Test"; then
            log_error "[$java_file] ❌ 检测到RedisTemplate使用，违反一级规范（禁止直接使用底层缓存工具）"
            echo "$file_content" | grep -n "RedisTemplate" | head -3 | while read -r line; do
                echo "    $line"
            done
            ((file_violations++))
        fi
    fi

    # 检查3：必须使用UnifiedCacheService（一级规范）
    if echo "$file_content" | grep -q "Cache.*Service\|cache.*service"; then
        if echo "$file_content" | grep -q "CacheService" && ! echo "$file_content" | grep -q "UnifiedCacheService"; then
            if ! echo "$java_file" | grep -q "BaseModuleCacheService\|Test\|Config"; then
                log_error "[$java_file] ❌ 检测到非标准缓存服务使用，违反一级规范（必须使用UnifiedCacheService或BaseModuleCacheService）"
                echo "$file_content" | grep -n "CacheService" | head -3 | while read -r line; do
                    echo "    $line"
                done
                ((file_violations++))
            fi
        fi
    fi

    # 检查4：禁止硬编码TTL时间（二级规范）
    if echo "$file_content" | grep -E "setEx.*[0-9]+\.*[0-9]*|set.*[0-9]+.*TimeUnit|expire.*[0-9]+" >/dev/null; then
        if ! echo "$java_file" | grep -q "UnifiedCacheManager\|BaseModuleCacheService\|Test"; then
            log_warning "[$java_file] ⚠️  检测到可能的硬编码TTL时间，违反二级规范（应使用BusinessDataType驱动TTL）"
            echo "$file_content" | grep -n -E "setEx.*[0-9]+|set.*[0-9]+.*TimeUnit|expire.*[0-9]+" | head -2 | while read -r line; do
                echo "    $line"
            done
        fi
    fi

    # 检查5：缓存键命名规范（二级规范）
    if echo "$file_content" | grep -E "set\(.*,\".*:.*:.*\"\)|get\(.*,\".*:.*:.*\"\)" >/dev/null; then
        if ! echo "$file_content" | grep -q "iog:cache:" && ! echo "$java_file" | grep -q "UnifiedCacheManager\|Test\|Config"; then
            log_warning "[$java_file] ⚠️  检测到非标准缓存键格式，建议遵循统一命名规范：iog:cache:{module}:{namespace}:{key}"
            echo "$file_content" | grep -n -E "set\(|get\(" | grep ":" | head -2 | while read -r line; do
                echo "    $line"
            done
        fi
    fi

    # 检查6：继承BaseModuleCacheService（二级规范）
    if echo "$file_content" | grep -E "class.*Cache.*Service.*implements|class.*Cache.*Service.*extends" >/dev/null; then
        if ! echo "$file_content" | grep -q "extends BaseModuleCacheService" && ! echo "$java_file" | grep -q "UnifiedCacheService\|Test\|Config"; then
            log_warning "[$java_file] ⚠️  检测到缓存服务未继承BaseModuleCacheService，建议遵循模块化缓存治理规范"
            echo "$file_content" | grep -n -E "class.*Cache.*Service" | head -2 | while read -r line; do
                echo "    $line"
            done
        fi
    fi

    # 统计结果
    if [ $file_violations -eq 0 ]; then
        ((PASS_COUNT++))
    else
        ((FAIL_COUNT++))
    fi
}

# 检查核心缓存组件存在性
check_cache_components() {
    log_header "Phase 3: 检查核心缓存组件存在性"

    local required_files=(
        "sa-base/src/main/java/net/lab1024/sa/base/common/cache/UnifiedCacheService.java"
        "sa-base/src/main/java/net/lab1024/sa/base/common/cache/BaseModuleCacheService.java"
        "sa-base/src/main/java/net/lab1024/sa/base/common/cache/BusinessDataType.java"
        "sa-base/src/main/java/net/lab1024/sa/base/common/cache/CacheModule.java"
        "sa-base/src/main/java/net/lab1024/sa/base/common/cache/UnifiedCacheManager.java"
        "sa-base/src/main/java/net/lab1024/sa/base/common/cache/EnhancedCacheMetricsCollector.java"
        "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/ConsumeCacheServiceV2.java"
    )

    local missing_files=0

    for file in "${required_files[@]}"; do
        if [ -f "$file" ]; then
            log_success "✓ $file"
        else
            log_error "✗ $file - 缺失核心缓存组件"
            ((missing_files++))
        fi
    done

    if [ $missing_files -gt 0 ]; then
        log_error "缺失 $missing_files 个核心缓存组件，请检查缓存架构实现"
        return 1
    fi

    return 0
}

# 检查规范文档存在性
check_specification_documents() {
    log_header "Phase 4: 检查规范文档存在性"

    local required_docs=(
        "docs/repowiki/zh/content/开发规范体系/核心规范/缓存架构规范.md"
        ".claude/skills/cache-architecture-specialist.md"
        "CLAUDE.md"
    )

    local missing_docs=0

    for doc in "${required_docs[@]}"; do
        if [ -f "$doc" ]; then
            log_success "✓ $doc"
        else
            log_warning "✗ $doc - 缺失规范文档"
            ((missing_docs++))
        fi
    done

    if [ $missing_docs -gt 0 ]; then
        log_warning "缺失 $missing_docs 个规范文档，建议完善文档体系"
    fi
}

# 生成验证报告
generate_report() {
    echo ""
    log_header "📊 验证报告"
    echo "============================================================"

    echo -e "${BLUE}📋 统计信息：${NC}"
    echo "   📁 检查文件数: $TOTAL_FILES_CHECKED"
    echo "   ✅ 通过文件数: $PASS_COUNT"
    echo "   ❌ 失败文件数: $FAIL_COUNT"
    echo "   ⚠️  警告数量: $WARNING_COUNT"
    echo "   ❌ 规范违规数: $VIOLATION_COUNT"

    echo ""
    echo -e "${BLUE}📈 合规率分析：${NC}"
    if [ $TOTAL_FILES_CHECKED -gt 0 ]; then
        local compliance_rate=$(echo "scale=1; $PASS_COUNT * 100 / $TOTAL_FILES_CHECKED" | bc 2>/dev/null || echo "0")
        echo "   🎯 整体合规率: ${compliance_rate}%"
    fi

    echo ""
    echo -e "${BLUE}🔍 问题分析：${NC}"
    if [ $VIOLATION_COUNT -eq 0 ]; then
        echo "   🎉 恭喜！未发现一级规范违规"
        if [ $WARNING_COUNT -eq 0 ]; then
            echo "   🏆 完美！未发现任何问题"
        else
            echo "   💡 建议：修复 $WARNING_COUNT 个警告问题以获得最佳实践"
        fi
    else
        echo "   🚨 发现 $VIOLATION_COUNT 个严重违规，必须立即修复"
        echo "   📝 建议：参考 docs/repowiki/zh/content/开发规范体系/核心规范/缓存架构规范.md"
    fi

    echo ""
    echo -e "${BLUE}📚 相关文档：${NC}"
    echo "   📖 缓存架构规范: docs/repowiki/zh/content/开发规范体系/核心规范/缓存架构规范.md"
    echo "   🎓 缓存架构技能: .claude/skills/cache-architecture-specialist.md"
    echo "   📋 开发指南: CLAUDE.md"

    echo "============================================================"
}

# 主执行流程
main() {
    local search_dir="${1:-"smart-admin-api-java17-springboot3"}"

    # 检查搜索目录
    if [ ! -d "$search_dir" ]; then
        log_error "搜索目录不存在: $search_dir"
        exit 1
    fi

    # 执行验证
    validate_java_files "$search_dir"
    check_cache_components
    check_specification_documents

    # 生成报告
    generate_report

    # 返回结果
    if [ $VIOLATION_COUNT -gt 0 ]; then
        echo ""
        log_error "🚨 验证失败！发现 $VIOLATION_COUNT 个严重违规，请修复后重新验证"
        exit 1
    elif [ $WARNING_COUNT -gt 0 ]; then
        echo ""
        log_warning "⚠️  验证通过但有警告，建议处理警告问题以获得最佳实践"
        exit 0
    else
        echo ""
        log_success "🎉 验证完全通过！代码完全符合缓存架构规范"
        exit 0
    fi
}

# 执行主函数
main "$@"