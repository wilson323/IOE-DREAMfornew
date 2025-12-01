#!/bin/bash

# =============================================================================
# IOE-DREAM 项目质量监控仪表板
# 功能：399编译错误的实时跟踪和量化监控体系
# 创建时间：2025-11-18
# 版本：v1.0.0
# =============================================================================

# 配置参数
PROJECT_ROOT="D:\IOE-DREAM"
BACKEND_DIR="$PROJECT_ROOT/smart-admin-api-java17-springboot3"
MONITORING_DIR="$PROJECT_ROOT/monitoring"
HISTORY_FILE="$MONITORING_DIR/quality_history.json"
TRENDS_FILE="$MONITORING_DIR/quality_trends.json"
ALERTS_FILE="$MONITORING_DIR/quality_alerts.json"
METRICS_FILE="$MONITORING_DIR/current_metrics.json"

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
NC='\033[0m' # No Color

# =============================================================================
# 质量监控核心功能
# =============================================================================

# 1. 实时编译错误统计
get_compilation_errors() {
    cd "$BACKEND_DIR"

    # 获取详细编译输出
    mvn compile -q 2>&1 > temp_compile.log

    # 统计各类错误
    local total_errors=$(grep -c "ERROR" temp_compile.log)
    local cannot_find_symbol=$(grep -c "cannot find symbol" temp_compile.log)
    local package_not_found=$(grep -c "package.*does not exist" temp_compile.log)
    local duplicate_method=$(grep -c "duplicate method" temp_compile.log)
    local cannot_resolve=$(grep -c "cannot resolve" temp_compile.log)
    local jakarta_issues=$(grep -c "javax\." temp_compile.log)
    local autowired_issues=$(grep -c "@Autowired" temp_compile.log)

    # 清理临时文件
    rm -f temp_compile.log

    # 返回JSON格式数据
    cat <<EOF
{
  "timestamp": "$(date -Iseconds)",
  "total_errors": $total_errors,
  "error_breakdown": {
    "cannot_find_symbol": $cannot_find_symbol,
    "package_not_found": $package_not_found,
    "duplicate_method": $duplicate_method,
    "cannot_resolve": $cannot_resolve,
    "jakarta_issues": $jakarta_issues,
    "autowired_issues": $autowired_issues,
    "other_errors": $((total_errors - cannot_find_symbol - package_not_found - duplicate_method - cannot_resolve - jakarta_issues - autowired_issues))
  }
}
EOF
}

# 2. 错误趋势分析
analyze_trends() {
    local current_time=$(date -Iseconds)
    local current_metrics=$(get_compilation_errors)

    # 初始化历史文件
    if [ ! -f "$HISTORY_FILE" ]; then
        echo "[]" > "$HISTORY_FILE"
    fi

    # 添加当前数据到历史记录
    local temp_file=$(mktemp)
    jq ". + [$current_metrics]" "$HISTORY_FILE" > "$temp_file" && mv "$temp_file" "$HISTORY_FILE"

    # 计算趋势（最近10次记录）
    local recent_data=$(jq '.[-10:]' "$HISTORY_FILE")
    local data_points=$(echo "$recent_data" | jq 'length')

    if [ "$data_points" -gt 1 ]; then
        local first_error=$(echo "$recent_data" | jq '.[0].total_errors')
        local last_error=$(echo "$recent_data" | jq '.[-1].total_errors')
        local trend_direction="stable"

        if [ "$last_error" -gt "$first_error" ]; then
            trend_direction="increasing"
        elif [ "$last_error" -lt "$first_error" ]; then
            trend_direction="decreasing"
        fi

        local change_rate=$(echo "scale=2; ($last_error - $first_error) / $first_error * 100" | bc -l)

        cat <<EOF
{
  "timestamp": "$current_time",
  "data_points": $data_points,
  "trend_direction": "$trend_direction",
  "change_rate": $change_rate,
  "first_measurement": {
    "errors": $first_error,
    "timestamp": $(echo "$recent_data" | jq '.[0].timestamp')
  },
  "last_measurement": {
    "errors": $last_error,
    "timestamp": $(echo "$recent_data" | jq '.[-1].timestamp')
  }
}
EOF
    else
        echo '{"status": "insufficient_data"}'
    fi
}

# 3. 质量改进预测模型
predict_improvement() {
    local trends=$(analyze_trends)

    if [ "$(echo "$trends" | jq -r '.status')" = "insufficient_data" ]; then
        echo '{"prediction": "insufficient_data", "confidence": 0}'
        return
    fi

    local trend_direction=$(echo "$trends" | jq -r '.trend_direction')
    local change_rate=$(echo "$trends" | jq -r '.change_rate')
    local current_errors=$(echo "$trends" | jq -r '.last_measurement.errors')

    # 简单线性预测模型
    local prediction_hours=20  # 剩余时间窗口
    local predicted_errors=0
    local confidence=0

    case "$trend_direction" in
        "decreasing")
            # 基于当前减少速度预测
            predicted_errors=$(echo "scale=0; $current_errors * (1 + $change_rate / 100 * ($prediction_hours / 10))" | bc -l)
            if [ "$predicted_errors" -lt 0 ]; then predicted_errors=0; fi
            confidence=$(echo "scale=2; 80 - $change_rate * 2" | bc -l)
            ;;
        "increasing")
            predicted_errors=$(echo "scale=0; $current_errors * (1 + $change_rate / 100 * ($prediction_hours / 10))" | bc -l)
            confidence=$(echo "scale=2; 60 + $change_rate" | bc -l)
            ;;
        *)
            predicted_errors=$current_errors
            confidence=50
            ;;
    esac

    # 确保置信度在合理范围内
    if [ "$(echo "$confidence > 95" | bc -l)" -eq 1 ]; then confidence=95; fi
    if [ "$(echo "$confidence < 10" | bc -l)" -eq 1 ]; then confidence=10; fi

    cat <<EOF
{
  "timestamp": "$(date -Iseconds)",
  "prediction": {
    "predicted_errors_after_20h": $predicted_errors,
    "current_errors": $current_errors,
    "improvement_needed": $((current_errors - 120)),
    "target_achievable": $([ "$predicted_errors" -le 120 ] && echo "true" || echo "false"),
    "confidence": $confidence,
    "trend_direction": "$trend_direction",
    "change_rate_percent": $change_rate
  }
}
EOF
}

# 4. 生成质量报告
generate_quality_report() {
    local current_metrics=$(get_compilation_errors)
    local trends=$(analyze_trends)
    local prediction=$(predict_improvement)

    echo -e "\n${CYAN}========================================${NC}"
    echo -e "${WHITE}📊 IOE-DREAM 项目质量监控仪表板${NC}"
    echo -e "${CYAN}========================================${NC}"

    # 当前状态
    local total_errors=$(echo "$current_metrics" | jq '.total_errors')
    local timestamp=$(echo "$current_metrics" | jq -r '.timestamp')

    echo -e "\n${BLUE}📈 当前质量状态 (时间: $timestamp)${NC}"
    echo -e "总编译错误数: ${RED}$total_errors${NC}"

    # 错误分类统计
    echo -e "\n${YELLOW}🔍 错误分类详情:${NC}"
    echo "$current_metrics" | jq -r '.error_breakdown | to_entries[] | "  • \(.key): \(.value)"'

    # 趋势分析
    if [ "$(echo "$trends" | jq -r '.status')" != "insufficient_data" ]; then
        local trend_direction=$(echo "$trends" | jq -r '.trend_direction')
        local change_rate=$(echo "$trends" | jq -r '.change_rate')
        local trend_icon=""

        case "$trend_direction" in
            "decreasing") trend_icon="📉";;
            "increasing") trend_icon="📈";;
            *) trend_icon="➡️";;
        esac

        echo -e "\n${BLUE}📊 趋势分析 $trend_icon${NC}"
        echo -e "趋势方向: ${WHITE}$trend_direction${NC}"
        echo -e "变化率: ${WHITE}$change_rate%${NC}"
    fi

    # 预测分析
    echo -e "\n${PURPLE}🔮 质量改进预测 (20小时窗口)${NC}"
    local predicted_errors=$(echo "$prediction" | jq '.prediction.predicted_errors_after_20h')
    local confidence=$(echo "$prediction" | jq '.prediction.confidence')
    local target_achievable=$(echo "$prediction" | jq -r '.prediction.target_achievable')
    local improvement_needed=$(echo "$prediction" | jq '.prediction.improvement_needed')

    echo -e "预测错误数: ${WHITE}$predicted_errors${NC} (目标: 120)"
    echo -e "需要改进: ${RED}$improvement_needed${NC} 个错误"
    echo -e "目标可达成: ${WHITE}$target_achievable${NC}"
    echo -e "预测置信度: ${WHITE}$confidence%${NC}"

    # 质量评级
    local quality_level="🔴 严重"
    if [ "$total_errors" -le 200 ]; then
        quality_level="🟡 中等"
    elif [ "$total_errors" -le 100 ]; then
        quality_level="🟢 良好"
    elif [ "$total_errors" -le 50 ]; then
        quality_level="✅ 优秀"
    fi

    echo -e "\n${BLUE}🏆 质量评级: $quality_level${NC}"

    # 建议行动
    echo -e "\n${GREEN}💡 优化建议:${NC}"
    if [ "$total_errors" -gt 400 ]; then
        echo -e "• ${RED}紧急：执行系统性批量修复${NC}"
        echo -e "• 优先修复 javax → jakarta 包名问题"
        echo -e "• 批量替换 @Autowired → @Resource"
    elif [ "$total_errors" -gt 200 ]; then
        echo -e "• ${YELLOW}重要：继续分类批量修复${NC}"
        echo -e "• 重点关注 cannot find symbol 错误"
        echo -e "• 补充缺失的实体类和依赖"
    else
        echo -e "• ${GREEN}优化：进行精细化修复${NC}"
        echo -e "• 修复重复方法定义"
        echo -e "• 完善单元测试覆盖"
    fi

    echo -e "\n${CYAN}========================================${NC}"
}

# 5. 数据驱动的优化建议
generate_optimization_strategy() {
    local current_metrics=$(get_compilation_errors)
    local total_errors=$(echo "$current_metrics" | jq '.total_errors')

    echo -e "\n${BLUE}🚀 数据驱动的优化策略${NC}"

    # 基于错误数量的策略选择
    if [ "$total_errors" -gt 400 ]; then
        echo -e "\n${RED}🔥 紧急修复策略 (当前: $total_errors 错误)${NC}"
        echo -e "优先级1: ${WHITE}jakarta 包名批量修复${NC}"
        echo -e "  预期减少: 60-80个错误"
        echo -e "  执行时间: 30分钟"
        echo -e ""
        echo -e "优先级2: ${WHITE}@Autowired 批量替换${NC}"
        echo -e "  预期减少: 40-60个错误"
        echo -e "  执行时间: 15分钟"

    elif [ "$total_errors" -gt 200 ]; then
        echo -e "\n${YELLOW}⚡ 快速修复策略 (当前: $total_errors 错误)${NC}"
        echo -e "优先级1: ${WHITE}缺失类和依赖补充${NC}"
        echo -e "  预期减少: 100-150个错误"
        echo -e "  执行时间: 2小时"
        echo -e ""
        echo -e "优先级2: ${WHITE}Entity类规范化${NC}"
        echo -e "  预期减少: 50-80个错误"
        echo -e "  执行时间: 1小时"

    else
        echo -e "\n${GREEN}🎯 精确修复策略 (当前: $total_errors 错误)${NC}"
        echo -e "优先级1: ${WHITE}重复方法定义修复${NC}"
        echo -e "  预期减少: 20-40个错误"
        echo -e "  执行时间: 30分钟"
        echo -e ""
        echo -e "优先级2: ${WHITE}代码质量优化${NC}"
        echo -e "  预期减少: 10-20个错误"
        echo -e "  执行时间: 1小时"
    fi

    # 资源分配建议
    echo -e "\n${PURPLE}📋 资源分配建议${NC}"
    local required_developers=1
    if [ "$total_errors" -gt 300 ]; then
        required_developers=2
    elif [ "$total_errors" -gt 500 ]; then
        required_developers=3
    fi

    echo -e "建议开发人员: ${WHITE}$required_developers${NC} 人"
    echo -e "预计总工时: ${WHITE}$(echo "scale=1; $total_errors / 10" | bc -l)${NC} 小时"
    echo -e "目标完成时间: ${WHITE}20${NC} 小时"

    # 风险评估
    echo -e "\n${YELLOW}⚠️ 风险评估${NC}"
    local risk_level="低"
    if [ "$total_errors" -gt 400 ]; then
        risk_level="高"
    elif [ "$total_errors" -gt 250 ]; then
        risk_level="中"
    fi

    echo -e "风险等级: ${WHITE}$risk_level${NC}"
    echo -e "主要风险: "
    echo -e "• 修复过程中可能引入新错误"
    echo -e "• 部分错误修复需要重新设计"
    echo -e "• 时间窗口紧张的挑战"
}

# 6. 保存质量指标
save_quality_metrics() {
    local current_metrics=$(get_compilation_errors)
    local trends=$(analyze_trends)
    local prediction=$(predict_improvement)

    # 保存当前指标
    cat <<EOF > "$METRICS_FILE"
{
  "last_updated": "$(date -Iseconds)",
  "current_metrics": $current_metrics,
  "trend_analysis": $trends,
  "prediction": $prediction
}
EOF

    # 生成趋势数据
    if [ ! -f "$TRENDS_FILE" ]; then
        echo '{"trends": []}' > "$TRENDS_FILE"
    fi

    local temp_file=$(mktemp)
    jq ".trends += [{
      timestamp: $(echo "$current_metrics" | jq '.timestamp'),
      total_errors: $(echo "$current_metrics" | jq '.total_errors'),
      cannot_find_symbol: $(echo "$current_metrics" | jq '.error_breakdown.cannot_find_symbol'),
      package_not_found: $(echo "$current_metrics" | jq '.error_breakdown.package_not_found'),
      duplicate_method: $(echo "$current_metrics" | jq '.error_breakdown.duplicate_method')
    }]" "$TRENDS_FILE" > "$temp_file" && mv "$temp_file" "$TRENDS_FILE"
}

# 7. 质量警报系统
check_quality_alerts() {
    local current_metrics=$(get_compilation_errors)
    local total_errors=$(echo "$current_metrics" | jq '.total_errors')
    local current_time=$(date -Iseconds)

    # 警报阈值
    local critical_threshold=500
    local warning_threshold=300

    local alerts=[]
    local alert_level="normal"

    if [ "$total_errors" -gt "$critical_threshold" ]; then
        alert_level="critical"
        alerts=$(jq ". + [{
          level: \"critical\",
          type: \"error_count\",
          message: \"编译错误数量严重超标: $total_errors (阈值: $critical_threshold)\",
          timestamp: \"$current_time\",
          action: \"立即执行批量修复\"
        }]" <<< "$alerts")

    elif [ "$total_errors" -gt "$warning_threshold" ]; then
        alert_level="warning"
        alerts=$(jq ". + [{
          level: \"warning\",
          type: \"error_count\",
          message: \"编译错误数量超标: $total_errors (阈值: $warning_threshold)\",
          timestamp: \"$current_time\",
          action: \"加强修复力度\"
        }]" <<< "$alerts")
    fi

    # 保存警报
    echo "{\"timestamp\": \"$current_time\", \"level\": \"$alert_level\", \"alerts\": $alerts}" > "$ALERTS_FILE"

    # 显示警报
    if [ "$alert_level" != "normal" ]; then
        echo -e "\n${RED}🚨 质量警报${NC}"
        echo "$alerts" | jq -r '.[] | "• \(.message) (建议: \(.action))"'
    fi
}

# =============================================================================
# 主程序入口
# =============================================================================

main() {
    local action="${1:-dashboard}"

    case "$action" in
        "dashboard"|"")
            generate_quality_report
            ;;
        "metrics")
            get_compilation_errors
            ;;
        "trends")
            analyze_trends
            ;;
        "predict")
            predict_improvement
            ;;
        "strategy")
            generate_optimization_strategy
            ;;
        "save")
            save_quality_metrics
            echo "质量指标已保存到 $MONITORING_DIR/"
            ;;
        "alerts")
            check_quality_alerts
            ;;
        "full")
            echo -e "${CYAN}🔄 执行完整质量分析...${NC}\n"
            generate_quality_report
            echo -e "\n${CYAN}💾 保存质量指标...${NC}"
            save_quality_metrics
            echo -e "\n${CYAN}🚨 检查质量警报...${NC}"
            check_quality_alerts
            generate_optimization_strategy
            echo -e "\n${GREEN}✅ 完整质量分析完成！${NC}"
            ;;
        *)
            echo "用法: $0 [dashboard|metrics|trends|predict|strategy|save|alerts|full]"
            exit 1
            ;;
    esac
}

# 检查依赖
check_dependencies() {
    local missing_deps=()

    if ! command -v jq &> /dev/null; then
        missing_deps+=("jq")
    fi

    if ! command -v bc &> /dev/null; then
        missing_deps+=("bc")
    fi

    if [ ${#missing_deps[@]} -ne 0 ]; then
        echo "❌ 缺少依赖: ${missing_deps[*]}"
        echo "请安装缺少的依赖后重试"
        exit 1
    fi
}

# 执行主程序
check_dependencies
main "$@"