#!/bin/bash

# =============================================================================
# IOE-DREAM 零技术债务持续监控脚本
# 功能: 持续监控项目全局一致性，确保零技术债务目标
# 作者: System Optimization Specialist (老王)
# 版本: v1.0.0
# 创建时间: 2025-11-18
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

# 项目根目录
PROJECT_ROOT="D:/IOE-DREAM/smart-admin-api-java17-springboot3"
LOG_FILE="./logs/zero_debt_monitor_$(date +%Y%m%d).log"

# 创建日志目录
mkdir -p ./logs

# 打印标题
print_header() {
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${BLUE}🔍 IOE-DREAM 零技术债务监控${NC}"
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${CYAN}监控时间: $(date)${NC}"
    echo -e "${CYAN}项目路径: $PROJECT_ROOT${NC}"
    echo -e "${BLUE}============================================================================${NC}"
}

# 核心一致性检查
check_core_consistency() {
    echo -e "${YELLOW}📊 核心一致性检查${NC}"

    local annotation_errors=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "annoation" {} \; | wc -l)
    local autowired_errors=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
    local jakarta_errors=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "javax\.(servlet|validation|persistence|ejb|jms|xml\.bind|jws|jta|annotation)" {} \; | wc -l)

    echo -e "   - 包名错误 (annoation): ${RED}$annotation_errors${NC} 个"
    echo -e "   - 依赖注入不统一 (@Autowired): ${RED}$autowired_errors${NC} 个"
    echo -e "   - Jakarta未迁移 (javax): ${RED}$jakarta_errors${NC} 个"

    # 计算健康度
    local total_errors=$((annotation_errors + autowired_errors + jakarta_errors))
    local health_score=$((100 - total_errors * 10))
    [ $health_score -lt 0 ] && health_score=0

    echo -e "   - ${CYAN}项目健康度: $health_score%${NC}"

    if [ $total_errors -eq 0 ]; then
        echo -e "   - ${GREEN}✅ 零技术债务达成！${NC}"
        return 0
    else
        echo -e "   - ${RED}❌ 发现技术债务，需要立即修复${NC}"
        return 1
    fi
}

# 编译状态检查
check_compilation_status() {
    echo -e "${YELLOW}🔧 编译状态检查${NC}"

    cd "$PROJECT_ROOT" > /dev/null 2>&1 || {
        echo -e "${RED}❌ 无法访问项目目录${NC}"
        return 1
    }

    if timeout 120 mvn clean compile -q > /dev/null 2>&1; then
        echo -e "   - ${GREEN}✅ 编译成功${NC}"
        return 0
    else
        echo -e "   - ${RED}❌ 编译失败${NC}"
        local error_count=$(timeout 30 mvn clean compile 2>&1 | grep -c "ERROR" 2>/dev/null || echo "未知")
        echo -e "   - 错误数量: ${RED}$error_count${NC}"
        return 1
    fi
}

# 架构合规性检查
check_architecture_compliance() {
    echo -e "${YELLOW}🏗️ 架构合规性检查${NC}"

    # 检查跨层访问违规
    local controller_direct_dao=$(grep -r "@Resource.*Dao" "$PROJECT_ROOT" --include="*Controller.java" 2>/dev/null | wc -l)
    local service_direct_dao=$(grep -r "@Resource.*Dao" "$PROJECT_ROOT" --include="*Service*.java" 2>/dev/null | wc -l)

    echo -e "   - Controller直连DAO违规: ${RED}$controller_direct_dao${NC} 个"
    echo -e "   - Service直连DAO违规: ${YELLOW}$service_direct_dao${NC} 个 (通过Manager层是正常的)"

    if [ $controller_direct_dao -eq 0 ]; then
        echo -e "   - ${GREEN}✅ 架构合规性良好${NC}"
        return 0
    else
        echo -e "   - ${RED}❌ 存在架构违规${NC}"
        return 1
    fi
}

# 代码质量检查
check_code_quality() {
    echo -e "${YELLOW}📊 代码质量检查${NC}"

    # 检查System.out使用
    local system_out_count=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "System\.out\." {} \; | wc -l)

    # 检查空包
    local empty_packages=$(find "$PROJECT_ROOT/src" -type d -empty 2>/dev/null | wc -l)

    # 检查重复文件
    local total_files=$(find "$PROJECT_ROOT" -name "*.java" | wc -l)
    local unique_files=$(find "$PROJECT_ROOT" -name "*.java" -exec basename {} \; | sort -u | wc -l)
    local duplicate_files=$((total_files - unique_files))

    echo -e "   - System.out使用: ${RED}$system_out_count${NC} 个文件"
    echo -e "   - 空包数量: ${YELLOW}$empty_packages${NC} 个"
    echo -e "   - 重复文件名: ${YELLOW}$duplicate_files${NC} 个"

    # 计算质量分数
    local quality_score=100
    quality_score=$((quality_score - system_out_count))
    quality_score=$((quality_score - empty_packages))
    quality_score=$((quality_score - duplicate_files / 5))
    [ $quality_score -lt 0 ] && quality_score=0

    echo -e "   - ${CYAN}代码质量评分: $quality_score%${NC}"

    if [ $quality_score -ge 90 ]; then
        echo -e "   - ${GREEN}✅ 代码质量优秀${NC}"
        return 0
    else
        echo -e "   - ${YELLOW}⚠️ 代码质量需要改进${NC}"
        return 1
    fi
}

# 生成监控报告
generate_monitoring_report() {
    local status="$1"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "[$timestamp] 零技术债务监控报告" >> "$LOG_FILE"
    echo "状态: $status" >> "$LOG_FILE"
    echo "核心一致性: $(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "annoation\|@Autowired\|javax\.(servlet|validation|persistence)" {} \; | wc -l) 个问题" >> "$LOG_FILE"
    echo "编译状态: $(cd "$PROJECT_ROOT" && timeout 30 mvn clean compile -q > /dev/null 2>&1 && echo "成功" || echo "失败")" >> "$LOG_FILE"
    echo "---" >> "$LOG_FILE"
}

# 主执行流程
main() {
    print_header

    local overall_status="PASS"

    echo -e "${CYAN}开始执行零技术债务检查...${NC}"
    echo ""

    # 核心一致性检查
    if ! check_core_consistency; then
        overall_status="FAIL"
    fi
    echo ""

    # 编译状态检查
    if ! check_compilation_status; then
        overall_status="FAIL"
    fi
    echo ""

    # 架构合规性检查
    if ! check_architecture_compliance; then
        overall_status="FAIL"
    fi
    echo ""

    # 代码质量检查
    if ! check_code_quality; then
        overall_status="WARN"
    fi
    echo ""

    # 生成最终报告
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${BLUE}📊 监控结果汇总${NC}"
    echo -e "${BLUE}============================================================================${NC}"

    case "$overall_status" in
        "PASS")
            echo -e "${GREEN}🎉 恭喜！项目达到零技术债务标准${NC}"
            ;;
        "FAIL")
            echo -e "${RED}⚠️ 项目存在技术债务，需要立即修复${NC}"
            ;;
        "WARN")
            echo -e "${YELLOW}💡 项目基本健康，建议进一步优化${NC}"
            ;;
    esac

    echo -e "${CYAN}详细日志: $LOG_FILE${NC}"
    echo -e "${BLUE}============================================================================${NC}"

    # 生成监控报告
    generate_monitoring_report "$overall_status"

    return 0
}

# 执行主函数
main "$@"