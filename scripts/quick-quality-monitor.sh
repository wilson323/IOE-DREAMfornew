#!/bin/bash

# =============================================================================
# IOE-DREAM 快速质量监控脚本（无依赖版本）
# 功能：实时监控编译错误变化，生成质量报告
# 创建时间：2025-11-18
# 版本：v1.0.0
# =============================================================================

PROJECT_ROOT="D:\IOE-DREAM"
BACKEND_DIR="$PROJECT_ROOT/smart-admin-api-java17-springboot3"
MONITORING_DIR="$PROJECT_ROOT/monitoring"
QUALITY_REPORT="$MONITORING_DIR/quality_report_$(date +%Y%m%d_%H%M%S).json"

# 创建监控目录
mkdir -p "$MONITORING_DIR"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m'

echo -e "${CYAN}========================================${NC}"
echo -e "${WHITE}📊 IOE-DREAM 实时质量监控系统${NC}"
echo -e "${CYAN}========================================${NC}"

# 1. 获取当前编译错误统计
get_current_stats() {
    cd "$BACKEND_DIR"

    # 执行编译并获取输出
    mvn compile -q 2>&1 > temp_compile.log
    local compile_result=$?

    # 统计各类错误
    local total_errors=$(grep -c "ERROR" temp_compile.log 2>/dev/null || echo 0)
    local cannot_find_symbol=$(grep -c "cannot find symbol" temp_compile.log 2>/dev/null || echo 0)
    local package_not_found=$(grep -c "package.*does not exist" temp_compile.log 2>/dev/null || echo 0)
    local duplicate_method=$(grep -c "duplicate method" temp_compile.log 2>/dev/null || echo 0)
    local cannot_resolve=$(grep -c "cannot resolve" temp_compile.log 2>/dev/null || echo 0)
    local jakarta_issues=$(grep -c "javax\." temp_compile.log 2>/dev/null || echo 0)
    local autowired_issues=$(grep -c "@Autowired" temp_compile.log 2>/dev/null || echo 0)

    # 统计Java文件数量
    local java_files=$(find . -name "*.java" | wc -l)

    # 计算错误密度
    local error_density=$(echo "scale=3; $total_errors / $java_files" 2>/dev/null | bc -l || echo "0")

    # 清理临时文件
    rm -f temp_compile.log

    cat << EOF
{
  "timestamp": "$(date -Iseconds)",
  "total_errors": $total_errors,
  "java_files": $java_files,
  "error_density": $error_density,
  "error_breakdown": {
    "cannot_find_symbol": $cannot_find_symbol,
    "package_not_found": $package_not_found,
    "duplicate_method": $duplicate_method,
    "cannot_resolve": $cannot_resolve,
    "jakarta_issues": $jakarta_issues,
    "autowired_issues": $autowired_issues,
    "other_errors": $((total_errors - cannot_find_symbol - package_not_found - duplicate_method - cannot_resolve - jakarta_issues - autowired_issues))
  },
  "compile_success": $([ $compile_result -eq 0 ] && echo "true" || echo "false")
}
EOF
}

# 2. 分析错误分布
analyze_error_distribution() {
    echo -e "\n${BLUE}📈 当前质量状态${NC}"

    local stats=$(get_current_stats)
    local total_errors=$(echo "$stats" | grep -o '"total_errors": [0-9]*' | cut -d: -f2 | tr -d ' ')
    local java_files=$(echo "$stats" | grep -o '"java_files": [0-9]*' | cut -d: -f2 | tr -d ' ')
    local error_density=$(echo "$stats" | grep -o '"error_density": [0-9.]*' | cut -d: -f2 | tr -d ' ')

    echo -e "当前时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo -e "Java文件总数: ${WHITE}$java_files${NC}"
    echo -e "总编译错误数: ${RED}$total_errors${NC}"
    echo -e "错误密度: ${WHITE}$error_density${NC} 错误/文件"

    # 错误分类统计
    echo -e "\n${YELLOW}🔍 错误分类详情${NC}"
    echo "$stats" | grep -o '"[^"]*": [0-9]*' | grep -v "total_errors\|java_files\|error_density\|compile_success" | while read line; do
        local error_type=$(echo "$line" | cut -d: -f1 | tr -d '"' | sed 's/_/ /g')
        local error_count=$(echo "$line" | cut -d: -f2 | tr -d ' ')
        if [ "$error_count" -gt 0 ]; then
            echo -e "  • ${WHITE}$error_type${NC}: ${RED}$error_count${NC}"
        fi
    done
}

# 3. 质量评级
get_quality_rating() {
    local stats=$(get_current_stats)
    local total_errors=$(echo "$stats" | grep -o '"total_errors": [0-9]*' | cut -d: -f2 | tr -d ' ')

    local rating="🔴 严重"
    local color="$RED"
    local recommendation="紧急修复：执行系统性批量修复"

    if [ "$total_errors" -le 50 ]; then
        rating="✅ 优秀"
        color="$GREEN"
        recommendation="保持现状，进行代码优化"
    elif [ "$total_errors" -le 100 ]; then
        rating="🟢 良好"
        color="$GREEN"
        recommendation="继续精细化修复"
    elif [ "$total_errors" -le 200 ]; then
        rating="🟡 中等"
        color="$YELLOW"
        recommendation="加强修复力度，优先批量处理"
    elif [ "$total_errors" -le 350 ]; then
        rating="🟠 需要改进"
        color="$YELLOW"
        recommendation="启动快速批量修复流程"
    else
        rating="🔴 严重"
        color="$RED"
        recommendation="立即执行紧急批量修复"
    fi

    echo -e "\n${BLUE}🏆 质量评级${NC}"
    echo -e "当前评级: ${color}$rating${NC}"
    echo -e "建议措施: ${WHITE}$recommendation${NC}"
}

# 4. 目标达成分析
analyze_target_achievement() {
    local stats=$(get_current_stats)
    local total_errors=$(echo "$stats" | grep -o '"total_errors": [0-9]*' | cut -d: -f2 | tr -d ' ')

    local current_stage=1
    local target_errors=120
    local improvement_needed=$((total_errors - target_errors))
    local remaining_hours=20

    echo -e "\n${PURPLE}🎯 目标达成分析${NC}"
    echo -e "当前阶段: ${WHITE}第一阶段完成${NC}"
    echo -e "第二阶段目标: ${GREEN}$target_errors${NC} 个错误"
    echo -e "当前错误数: ${RED}$total_errors${NC} 个"
    echo -e "需要改进: ${RED}$improvement_needed${NC} 个错误"
    echo -e "剩余时间: ${WHITE}$remaining_hours${NC} 小时"

    # 计算需要的小时修复率
    local required_rate=$((improvement_needed / remaining_hours))
    echo -e "需要修复率: ${WHITE}$required_rate${NC} 错误/小时"

    # 评估可行性
    if [ "$required_rate" -le 5 ]; then
        echo -e "可行性: ${GREEN}✅ 容易达成${NC}"
    elif [ "$required_rate" -le 15 ]; then
        echo -e "可行性: ${YELLOW}⚠️ 可以达成${NC}"
    elif [ "$required_rate" -le 30 ]; then
        echo -e "可行性: ${YELLOW}⚡ 需要加强力度${NC}"
    else
        echo -e "可行性: ${RED}❌ 需要额外资源${NC}"
    fi
}

# 5. 优化建议
generate_optimization_suggestions() {
    local stats=$(get_current_stats)
    local total_errors=$(echo "$stats" | grep -o '"total_errors": [0-9]*' | cut -d: -f2 | tr -d ' ')
    local jakarta_issues=$(echo "$stats" | grep -o '"jakarta_issues": [0-9]*' | cut -d: -f2 | tr -d ' ')
    local autowired_issues=$(echo "$stats" | grep -o '"autowired_issues": [0-9]*' | cut -d: -f2 | tr -d ' ')
    local cannot_find_symbol=$(echo "$stats" | grep -o '"cannot_find_symbol": [0-9]*' | cut -d: -f2 | tr -d ' ')

    echo -e "\n${GREEN}💡 优化建议${NC}"

    # 根据错误类型生成建议
    if [ "$jakarta_issues" -gt 10 ]; then
        echo -e "优先级1: ${WHITE}批量修复Jakarta包名问题${NC}"
        echo -e "  预期减少: $((jakarta_issues * 80 / 100)) 个错误"
        echo -e "  执行命令: find . -name '*.java' -exec sed -i 's/javax\\./jakarta\\./g' {} \\;"
        echo -e "  预计时间: 30分钟"
        echo ""
    fi

    if [ "$autowired_issues" -gt 5 ]; then
        echo -e "优先级2: ${WHITE}批量替换@Autowired为@Resource${NC}"
        echo -e "  预期减少: $((autowired_issues * 90 / 100)) 个错误"
        echo -e "  执行命令: find . -name '*.java' -exec sed -i 's/@Autowired/@Resource/g' {} \\;"
        echo -e "  预计时间: 15分钟"
        echo ""
    fi

    if [ "$cannot_find_symbol" -gt 50 ]; then
        echo -e "优先级3: ${WHITE}补充缺失的类和依赖${NC}"
        echo -e "  预期减少: $((cannot_find_symbol * 60 / 100)) 个错误"
        echo -e "  需要分析每个符号缺失的原因"
        echo -e "  预计时间: 2小时"
        echo ""
    fi

    # 资源建议
    local estimated_developers=1
    if [ "$total_errors" -gt 300 ]; then
        estimated_developers=2
    elif [ "$total_errors" -gt 500 ]; then
        estimated_developers=3
    fi

    echo -e "${BLUE}📋 资源分配建议${NC}"
    echo -e "建议开发人员: ${WHITE}$estimated_developers${NC} 人"
    echo -e "预计总工时: ${WHITE}$((total_errors / 10))${NC} 小时"
}

# 6. 保存质量报告
save_quality_report() {
    local stats=$(get_current_stats)
    local current_time=$(date '+%Y-%m-%d %H:%M:%S')

    # 创建JSON格式的报告
    cat > "$QUALITY_REPORT" << EOF
{
  "report_timestamp": "$current_time",
  "project": "IOE-DREAM",
  "current_metrics": $stats,
  "target": {
    "errors": 120,
    "stage": "second_phase",
    "deadline_hours": 20
  },
  "analysis": {
    "quality_improvement_needed": $(echo "$stats" | grep -o '"total_errors": [0-9]*' | cut -d: -f2 | tr -d ' ') - 120,
    "error_density": $(echo "$stats" | grep -o '"error_density": [0-9.]*' | cut -d: -f2 | tr -d ' ')
  },
  "recommendations": {
    "batch_fix_jakarta": $(echo "$stats" | grep -o '"jakarta_issues": [0-9]*' | cut -d: -f2 | tr -d ' '),
    "batch_fix_autowired": $(echo "$stats" | grep -o '"autowired_issues": [0-9]*' | cut -d: -f2 | tr -d ' '),
    "fix_missing_symbols": $(echo "$stats" | grep -o '"cannot_find_symbol": [0-9]*' | cut -d: -f2 | tr -d ' ')
  }
}
EOF

    echo -e "\n${CYAN}💾 质量报告已保存${NC}"
    echo -e "文件路径: ${WHITE}$QUALITY_REPORT${NC}"
}

# 7. 生成趋势跟踪
init_trend_tracking() {
    local trend_file="$MONITORING_DIR/error_trends.csv"

    # 创建CSV文件头部
    if [ ! -f "$trend_file" ]; then
        echo "timestamp,total_errors,jakarta_issues,autowired_issues,cannot_find_symbol" > "$trend_file"
    fi

    # 添加当前数据点
    local stats=$(get_current_stats)
    local timestamp=$(date -Iseconds)
    local total_errors=$(echo "$stats" | grep -o '"total_errors": [0-9]*' | cut -d: -f2 | tr -d ' ')
    local jakarta_issues=$(echo "$stats" | grep -o '"jakarta_issues": [0-9]*' | cut -d: -f2 | tr -d ' ')
    local autowired_issues=$(echo "$stats" | grep -o '"autowired_issues": [0-9]*' | cut -d: -f2 | tr -d ' ')
    local cannot_find_symbol=$(echo "$stats" | grep -o '"cannot_find_symbol": [0-9]*' | cut -d: -f2 | tr -d ' ')

    echo "$timestamp,$total_errors,$jakarta_issues,$autowired_issues,$cannot_find_symbol" >> "$trend_file"

    # 显示最近趋势
    echo -e "\n${BLUE}📈 错误趋势 (最近5次记录)${NC}"
    if [ -f "$trend_file" ]; then
        tail -6 "$trend_file" | head -5 | while IFS=',' read timestamp errors jakarta autowired symbol; do
            local time_short=$(echo "$timestamp" | cut -c12-19)
            echo "  $time_short: 错误数=$errors, Jakarta=$jakarta, Autowired=$autowired, Symbol=$symbol"
        done
    fi
}

# 主程序
main() {
    local action="${1:-full}"

    echo -e "\n${CYAN}🔄 执行质量监控分析...${NC}\n"

    case "$action" in
        "stats"|"")
            analyze_error_distribution
            ;;
        "rating")
            get_quality_rating
            ;;
        "target")
            analyze_target_achievement
            ;;
        "suggestions")
            generate_optimization_suggestions
            ;;
        "save")
            save_quality_report
            ;;
        "trends")
            init_trend_tracking
            ;;
        "full"|"")
            analyze_error_distribution
            get_quality_rating
            analyze_target_achievement
            generate_optimization_suggestions
            init_trend_tracking
            save_quality_report

            echo -e "\n${GREEN}✅ 质量监控分析完成！${NC}"
            echo -e "\n${CYAN}🚀 下一步操作建议${NC}"
            echo -e "1. 执行批量修复脚本: ${WHITE}./scripts/auto-fix-batch.sh${NC}"
            echo -e "2. 监控修复进度: ${WHITE}./scripts/quick-quality-monitor.sh trends${NC}"
            echo -e "3. 查看详细报告: ${WHITE}cat $QUALITY_REPORT${NC}"
            ;;
        *)
            echo "用法: $0 [stats|rating|target|suggestions|save|trends|full]"
            exit 1
            ;;
    esac
}

# 执行主程序
main "$@"