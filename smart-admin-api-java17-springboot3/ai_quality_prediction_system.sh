#!/bin/bash
# AI驱动的SmartAdmin v4质量预测和预警系统
# 版本: v2.0
# 实时监控代码质量，预测潜在风险

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 配置参数
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
QUALITY_DB="$PROJECT_ROOT/.ai_quality_metrics"
ALERT_THRESHOLD_FILE="$PROJECT_ROOT/.ai_quality_thresholds"
PREDICTION_MODEL="$PROJECT_ROOT/.ai_prediction_model"

# 创建质量数据库目录
mkdir -p "$QUALITY_DB"

# 默认质量阈值
DEFAULT_THRESHOLDS='{
    "compilation_success_rate": 80,
    "compliance_rate": 90,
    "architecture_compliance_rate": 85,
    "test_coverage_rate": 70,
    "code_duplication_rate": 15,
    "cyclomatic_complexity": 10,
    "maintainability_index": 70
}'

# 初始化阈值文件
if [ ! -f "$ALERT_THRESHOLD_FILE" ]; then
    echo "$DEFAULT_THRESHOLDS" > "$ALERT_THRESHOLD_FILE"
fi

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

log_alert() {
    echo -e "${PURPLE}[ALERT]${NC} $1"
}

log_prediction() {
    echo -e "${CYAN}[PREDICTION]${NC} $1"
}

# AI质量指标收集器
collect_quality_metrics() {
    local timestamp=$(date -Iseconds)
    local metrics_file="$QUALITY_DB/metrics_$(date +%Y%m%d).json"

    log_info "🔍 收集AI质量指标..."

    # 1. 编译成功率指标
    local total_java_files=$(find "$PROJECT_ROOT" -name "*.java" | wc -l)
    mvn clean compile -q 2>&1 | grep -o "ERROR" > /tmp/compile_errors.txt
    local compile_errors=$(wc -l < /tmp/compile_errors.txt 2>/dev/null || echo "0")
    local compilation_success_rate=$(echo "scale=2; (1 - $compile_errors / $total_java_files) * 100" | bc -l 2>/dev/null || echo "0")

    # 2. SmartAdmin v4规范合规性指标
    local javax_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
    local autowired_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
    local compliance_rate=$(echo "scale=2; (1 - ($javax_files + $autowired_files) / $total_java_files) * 100" | bc -l 2>/dev/null || echo "0")

    # 3. 架构合规性指标
    local entity_files=$(find "$PROJECT_ROOT" -name "*Entity.java" | wc -l)
    local base_entity_files=$(find "$PROJECT_ROOT" -name "*Entity.java" -exec grep -l "extends BaseEntity" {} \; | wc -l)
    local architecture_compliance_rate=$(echo "scale=2; $base_entity_files / $entity_files * 100" | bc -l 2>/dev/null || echo "0")

    # 4. 测试覆盖率指标
    local test_files=$(find "$PROJECT_ROOT" -name "*Test.java" | wc -l)
    local test_coverage_rate=$(echo "scale=2; $test_files / $total_java_files * 100" | bc -l 2>/dev/null || echo "0")

    # 5. 代码重复率指标（基于相似文件名）
    local duplicate_files=$(find "$PROJECT_ROOT" -name "*.java" | xargs basename -a | sort | uniq -d | wc -l)
    local code_duplication_rate=$(echo "scale=2; $duplicate_files / $total_java_files * 100" | bc -l 2>/dev/null || echo "0")

    # 6. 复杂度指标（基于方法数量）
    local total_methods=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -c "public.*(" {} \; | awk '{sum += $1} END {print sum}' 2>/dev/null || echo "0")
    local cyclomatic_complexity=$(echo "scale=2; $total_methods / $total_java_files" | bc -l 2>/dev/null || echo "0")

    # 7. 可维护性指标（综合评分）
    local maintainability_index=$(echo "scale=2; ($compilation_success_rate * 0.3 + $compliance_rate * 0.25 + $architecture_compliance_rate * 0.2 + $test_coverage_rate * 0.15 + (100 - $code_duplication_rate) * 0.1)" | bc -l 2>/dev/null || echo "0")

    # 生成质量指标JSON
    cat > "$metrics_file" << EOF
{
    "timestamp": "$timestamp",
    "metrics": {
        "compilation_success_rate": $compilation_success_rate,
        "compliance_rate": $compliance_rate,
        "architecture_compliance_rate": $architecture_compliance_rate,
        "test_coverage_rate": $test_coverage_rate,
        "code_duplication_rate": $code_duplication_rate,
        "cyclomatic_complexity": $cyclomatic_complexity,
        "maintainability_index": $maintability_index
    },
    "details": {
        "total_java_files": $total_java_files,
        "compile_errors": $compile_errors,
        "javax_files": $javax_files,
        "autowired_files": $autowired_files,
        "entity_files": $entity_files,
        "base_entity_files": $base_entity_files,
        "test_files": $test_files,
        "total_methods": $total_methods
    }
}
EOF

    log_success "✅ 质量指标已收集: $metrics_file"
    echo "$compilation_success_rate,$compliance_rate,$architecture_compliance_rate,$test_coverage_rate,$code_duplication_rate,$cyclomatic_complexity,$maintainability_index"
}

# AI质量趋势分析器
analyze_quality_trends() {
    log_info "📈 执行AI质量趋势分析..."

    local current_metrics=$(collect_quality_metrics)
    IFS=',' read -r compilation compliance arch_test test_cov duplicate complex maintainability <<< "$current_metrics"

    # 查找历史数据进行趋势分析
    local trend_analysis_file="$QUALITY_DB/trend_analysis_$(date +%Y%m).json"

    # 如果存在历史数据，进行趋势对比
    if [ -f "$trend_analysis_file" ]; then
        log_info "📊 分析质量变化趋势..."

        # 简单的趋势计算（实际AI模型会更复杂）
        local trend_impact="stable"
        if (( $(echo "$compilation < 80" | bc -l 2>/dev/null || echo "1") )); then
            trend_impact="declining"
        elif (( $(echo "$compilation > 95" | bc -l 2>/dev/null || echo "0") )); then
            trend_impact="improving"
        fi

        log_prediction "🔮 质量趋势预测: $trend_impact"
        log_prediction "   编译成功率: ${compilation}%"
        log_prediction "   规范合规率: ${compliance}%"
        log_prediction "   架构合规率: ${arch_test}%"
    fi

    # 更新趋势分析文件
    cat > "$trend_analysis_file" << EOF
{
    "timestamp": "$(date -Iseconds)",
    "current_metrics": {
        "compilation_success_rate": $compilation,
        "compliance_rate": $compliance,
        "architecture_compliance_rate": $arch_test,
        "test_coverage_rate": $test_cov,
        "code_duplication_rate": $duplicate,
        "cyclomatic_complexity": $complex,
        "maintainability_index": $maintainability
    },
    "trend_impact": "$trend_impact"
}
EOF
}

# AI智能预警系统
ai_quality_alerts() {
    local current_metrics=$(collect_quality_metrics)
    IFS=',' read -r compilation compliance arch_test test_cov duplicate complex maintainability <<< "$current_metrics"

    log_info "🚨 执行AI智能预警检查..."

    # 加载预警阈值
    local thresholds=$(cat "$ALERT_THRESHOLD_FILE")

    # 提取阈值
    local compilation_threshold=$(echo "$thresholds" | jq -r '.compilation_success_rate // 80')
    local compliance_threshold=$(echo "$thresholds" | jq -r '.compliance_rate // 90')
    local arch_threshold=$(echo "$thresholds" | jq -r '.architecture_compliance_rate // 85')
    local test_threshold=$(echo "$thresholds" | jq -r '.test_coverage_rate // 70')
    local duplicate_threshold=$(echo "$thresholds" | jq -r '.code_duplication_rate // 15')
    local complexity_threshold=$(echo "$thresholds" | jq -r '.cyclomatic_complexity // 10')
    local maintainability_threshold=$(echo "$thresholds" | jq -r '.maintainability_index // 70')

    local alerts_triggered=0

    # 🔴 严重预警
    if (( $(echo "$compilation < $compilation_threshold" | bc -l 2>/dev/null || echo "1") )); then
        log_alert "🔴 严重预警: 编译成功率过低 (${compilation}% < ${compilation_threshold}%)"
        log_alert "   建议立即执行: ./ai_smart_fix.sh"
        alerts_triggered=$((alerts_triggered + 1))
    fi

    if (( $(echo "$compliance < $compliance_threshold" | bc -l 2>/dev/null || echo "1") )); then
        log_alert "🔴 严重预警: SmartAdmin v4规范合规率过低 (${compliance}% < ${compliance_threshold}%)"
        log_alert "   建议检查: javax包名、@Autowired使用情况"
        alerts_triggered=$((alerts_triggered + 1))
    fi

    # 🟡 中等预警
    if (( $(echo "$arch_test < $arch_threshold" | bc -l 2>/dev/null || echo "1") )); then
        log_warning "🟡 中等预警: 架构合规率偏低 (${arch_test}% < ${arch_threshold}%)"
        log_warning "   建议检查Entity类继承BaseEntity情况"
        alerts_triggered=$((alerts_triggered + 1))
    fi

    if (( $(echo "$test_cov < $test_threshold" | bc -l 2>/dev/null || echo "1") )); then
        log_warning "🟡 中等预警: 测试覆盖率过低 (${test_cov}% < ${test_threshold}%)"
        log_warning "   建议增加单元测试和集成测试"
        alerts_triggered=$((alerts_triggered + 1))
    fi

    # 🟢 轻微预警
    if (( $(echo "$duplicate > $duplicate_threshold" | bc -l 2>/dev/null || echo "0") )); then
        log_warning "🟢 轻微预警: 代码重复率偏高 (${duplicate}% > ${duplicate_threshold}%)"
        log_warning "   建议进行代码重构和优化"
        alerts_triggered=$((alerts_triggered + 1))
    fi

    if (( $(echo "$complex > $complexity_threshold" | bc -l 2>/dev/null || echo "0") )); then
        log_warning "🟢 轻微预警: 圈复杂度过高 (${complex} > ${complexity_threshold})"
        log_warning "   建议简化复杂方法"
        alerts_triggered=$((alerts_triggered + 1))
    fi

    # 📊 综合质量评估
    if (( $(echo "$maintainability < $maintainability_threshold" | bc -l 2>/dev/null || echo "1") )); then
        log_alert "🔴 综合质量预警: 可维护性指数过低 (${maintainability} < ${maintainability_threshold})"
        alerts_triggered=$((alerts_triggered + 1))
    fi

    if [ "$alerts_triggered" -eq 0 ]; then
        log_success "✅ 质量检查通过，未触发预警"
    else
        log_warning "⚠️ 总计触发 $alerts_triggered 个质量预警"
    fi

    return $alerts_triggered
}

# AI质量预测模型
predict_quality_risks() {
    log_info "🔮 执行AI质量风险预测..."

    # 基于历史数据和当前指标预测未来质量趋势
    local current_metrics=$(collect_quality_metrics)
    IFS=',' read -r compilation compliance arch_test test_cov duplicate complex maintainability <<< "$current_metrics"

    # AI预测逻辑（简化版）
    local risk_level="LOW"
    local risk_factors=()
    local recommendations=()

    # 风险因子分析
    if (( $(echo "$compilation < 70" | bc -l 2>/dev/null || echo "1") )); then
        risk_factors+=("编译成功率过低")
        risk_level="HIGH"
    fi

    if (( $(echo "$compliance < 80" | bc -l 2>/dev/null || echo "1") )); then
        risk_factors+=("规范合规性不足")
        risk_level="HIGH"
    fi

    if (( $(echo "$test_cov < 30" | bc -l 2>/dev/null || echo "1") )); then
        risk_factors+=("测试覆盖率严重不足")
        risk_level="HIGH"
    fi

    if (( $(echo "$arch_test < 60" | bc -l 2>/dev/null || echo "1") )); then
        risk_factors+=("架构规范遵循度低")
        [ "$risk_level" = "LOW" ] && risk_level="MEDIUM"
    fi

    # 生成AI建议
    if [ "$risk_level" = "HIGH" ]; then
        recommendations+=("🚨 立即执行全面代码审查")
        recommendations+=("🔧 运行AI自动修复脚本")
        recommendations+=("📋 制定质量改进计划")
    elif [ "$risk_level" = "MEDIUM" ]; then
        recommendations+=("⚠️ 加强代码质量监控")
        recommendations+=("🎯 制定阶段性改进目标")
        recommendations+=("📚 团队培训和质量意识提升")
    else
        recommendations+=("✅ 保持当前质量水准")
        recommendations+=("📈 持续优化和改进")
    fi

    # 输出预测结果
    log_prediction "🎯 AI质量风险预测结果:"
    log_prediction "   风险等级: $risk_level"

    if [ ${#risk_factors[@]} -gt 0 ]; then
        log_prediction "   风险因子:"
        for factor in "${risk_factors[@]}"; do
            log_prediction "     • $factor"
        done
    fi

    log_prediction "   AI建议:"
    for recommendation in "${recommendations[@]}"; do
        log_prediction "     $recommendation"
    done

    # 保存预测结果
    local prediction_file="$QUALITY_DB/prediction_$(date +%Y%m%d_%H%M%S).json"
    cat > "$prediction_file" << EOF
{
    "timestamp": "$(date -Iseconds)",
    "risk_level": "$risk_level",
    "risk_factors": [$(printf '"%s",' "${risk_factors[@]}" | sed 's/,$//')],
    "recommendations": [$(printf '"%s",' "${recommendations[@]}" | sed 's/,$//')],
    "current_metrics": {
        "compilation_success_rate": $compilation,
        "compliance_rate": $compliance,
        "architecture_compliance_rate": $arch_test,
        "test_coverage_rate": $test_cov,
        "code_duplication_rate": $duplicate,
        "cyclomatic_complexity": $complex,
        "maintainability_index": $maintainability
    }
}
EOF

    return 0
}

# AI质量监控仪表板
generate_quality_dashboard() {
    log_info "📊 生成AI质量监控仪表板..."

    local dashboard_file="$PROJECT_ROOT/ai_quality_dashboard.html"

    # 获取最新质量指标
    local current_metrics=$(collect_quality_metrics)
    IFS=',' read -r compilation compliance arch_test test_cov duplicate complex maintainability <<< "$current_metrics"

    # 生成HTML仪表板
    cat > "$dashboard_file" << 'EOF'
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartAdmin v4 AI质量监控仪表板</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .dashboard { max-width: 1200px; margin: 0 auto; }
        .header { text-align: center; color: #333; margin-bottom: 30px; }
        .metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .metric-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .metric-title { font-size: 18px; color: #666; margin-bottom: 10px; }
        .metric-value { font-size: 36px; font-weight: bold; margin-bottom: 10px; }
        .metric-change { font-size: 14px; }
        .positive { color: #28a745; }
        .negative { color: #dc3545; }
        .neutral { color: #ffc107; }
        .chart-container { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
    </style>
</head>
<body>
    <div class="dashboard">
        <div class="header">
            <h1>🤖 SmartAdmin v4 AI质量监控仪表板</h1>
            <p>实时监控代码质量，预测潜在风险</p>
        </div>

        <div class="metrics-grid">
EOF

    # 添加指标卡片
    cat >> "$dashboard_file" << EOF
            <div class="metric-card">
                <div class="metric-title">编译成功率</div>
                <div class="metric-value $(echo "$compilation" | awk '{if ($1>=80) print "positive"; else if ($1>=60) print "neutral"; else print "negative"}')">${compilation}%</div>
                <div class="metric-change">反映代码基础质量</div>
            </div>

            <div class="metric-card">
                <div class="metric-title">规范合规率</div>
                <div class="metric-value $(echo "$compliance" | awk '{if ($1>=90) print "positive"; else if ($1>=70) print "neutral"; else print "negative"}')">${compliance}%</div>
                <div class="metric-change">SmartAdmin v4规范遵循度</div>
            </div>

            <div class="metric-card">
                <div class="metric-title">架构合规率</div>
                <div class="metric-value $(echo "$arch_test" | awk '{if ($1>=85) print "positive"; else if ($1>=70) print "neutral"; else print "negative"}')">${arch_test}%</div>
                <div class="metric-change">四层架构规范遵循度</div>
            </div>

            <div class="metric-card">
                <div class="metric-title">测试覆盖率</div>
                <div class="metric-value $(echo "$test_cov" | awk '{if ($1>=70) print "positive"; else if ($1>=40) print "neutral"; else print "negative"}')">${test_cov}%</div>
                <div class="metric-change">单元测试和集成测试覆盖度</div>
            </div>

            <div class="metric-card">
                <div class="metric-title">代码重复率</div>
                <div class="metric-value $(echo "$duplicate" | awk '{if ($1<=10) print "positive"; else if ($1<=20) print "neutral"; else print "negative"}')">${duplicate}%</div>
                <div class="metric-change">代码重复度监控</div>
            </div>

            <div class="metric-card">
                <div class="metric-title">可维护性指数</div>
                <div class="metric-value $(echo "$maintainability" | awk '{if ($1>=80) print "positive"; else if ($1>=60) print "neutral"; else print "negative"}')">${maintainability}</div>
                <div class="metric-change">综合可维护性评分</div>
            </div>
        </div>

        <div class="chart-container">
            <h3>质量趋势图</h3>
            <canvas id="qualityChart" width="400" height="200"></canvas>
        </div>
    </div>

    <script>
        // 质量趋势图配置
        const ctx = document.getElementById('qualityChart').getContext('2d');
        const qualityChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: ['当前指标'],
                datasets: [
                    {
                        label: '编译成功率',
                        data: [$compilation],
                        borderColor: '#28a745',
                        backgroundColor: 'rgba(40, 167, 69, 0.1)',
                        tension: 0.1
                    },
                    {
                        label: '规范合规率',
                        data: [$compliance],
                        borderColor: '#007bff',
                        backgroundColor: 'rgba(0, 123, 255, 0.1)',
                        tension: 0.1
                    },
                    {
                        label: '架构合规率',
                        data: [$arch_test],
                        borderColor: '#ffc107',
                        backgroundColor: 'rgba(255, 193, 7, 0.1)',
                        tension: 0.1
                    },
                    {
                        label: '测试覆盖率',
                        data: [$test_cov],
                        borderColor: '#17a2b8',
                        backgroundColor: 'rgba(23, 162, 184, 0.1)',
                        tension: 0.1
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                        display: true,
                        text: 'SmartAdmin v4 质量指标趋势'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100
                    }
                }
            }
        });

        // 自动刷新仪表板（每30秒）
        setTimeout(() => {
            location.reload();
        }, 30000);
    </script>
</body>
</html>
EOF

    log_success "✅ AI质量监控仪表板已生成: $dashboard_file"
    log_info "💡 在浏览器中打开查看: file://$dashboard_file"
}

# 主监控流程
main() {
    log_info "🚀 启动AI驱动的SmartAdmin v4质量预测和预警系统..."

    # 1. 收集质量指标
    collect_quality_metrics

    # 2. 分析质量趋势
    analyze_quality_trends

    # 3. 执行智能预警
    ai_quality_alerts
    local alert_count=$?

    # 4. 预测质量风险
    predict_quality_risks

    # 5. 生成监控仪表板
    generate_quality_dashboard

    # 输出总结
    echo ""
    log_info "📊 AI质量监控总结:"
    log_info "   当前质量指标已收集并分析"
    log_info "   质量趋势已预测"
    if [ $alert_count -gt 0 ]; then
        log_warning "   触发 $alert_count 个质量预警，建议及时处理"
    else
        log_success "   质量状态良好，持续监控中"
    fi
    log_info "   监控仪表板已生成，可在浏览器中查看"

    # 返回预警数量
    return $alert_count
}

# 执行主流程
main "$@"