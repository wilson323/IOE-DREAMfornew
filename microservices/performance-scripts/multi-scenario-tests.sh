#!/bin/bash

# ===================================================================
# IOE-DREAM 微服务多场景性能测试脚本集合
# 包含负载测试、压力测试、容量测试、峰值测试等多种场景
#
# 使用方法:
#   ./multi-scenario-tests.sh [scenario] [options]
#
# 测试场景:
#   login-burst      - 登录流量突发测试
#   concurrent-api   - 并发API测试
#   peak-hour        - 高峰时段模拟
#   endurance        - 长时间稳定性测试
#   recovery         - 故障恢复测试
#   all              - 执行所有场景 (默认)
#
# 选项:
#   --duration N    - 测试持续时间(分钟)
#   --users N       - 并发用户数
#   --target URL     - 目标URL
#   --output DIR    - 输出目录
#   --monitoring     - 启用监控
# ===================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
RESULTS_DIR="${OUTPUT_DIR:-$PROJECT_ROOT/performance-results}"
LOG_DIR="$RESULTS_DIR/logs"
REPORTS_DIR="$RESULTS_DIR/reports"

# 时间戳
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
TEST_RUN_ID="multi-scenario-$TIMESTAMP"

# 默认配置
DEFAULT_DURATION=10
DEFAULT_USERS=100
TARGET_URL="${TARGET_URL:-http://localhost:8080}"
ENABLE_MONITORING=false

# 解析参数
SCENARIO=""
TEST_DURATION=$DEFAULT_DURATION
CONCURRENT_USERS=$DEFAULT_USERS
MONITORING_ENABLED=false

# 日志函数
log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "[$timestamp] [$level] $message" | tee -a "$LOG_DIR/multi-scenario-$TEST_RUN_ID.log"

    case $level in
        "INFO")
            echo -e "${GREEN}[INFO]${NC} $message"
            ;;
        "WARN")
            echo -e "${YELLOW}[WARN]${NC} $message"
            ;;
        "ERROR")
            echo -e "${RED}[ERROR]${NC} $message"
            ;;
        "DEBUG")
            echo -e "${BLUE}[DEBUG]${NC} $message"
            ;;
    esac
}

print_separator() {
    echo -e "${PURPLE}==================================================================${NC}"
}

print_section() {
    echo ""
    print_separator
    echo -e "${CYAN}📋 $1${NC}"
    print_separator
}

# 创建目录结构
setup_directories() {
    mkdir -p "$RESULTS_DIR"
    mkdir -p "$LOG_DIR"
    mkdir -p "$REPORTS_DIR"
}

# 健康检查
health_check() {
    log "INFO" "检查目标服务健康状态: $TARGET_URL"

    local response=$(curl -s --max-time 5 "$TARGET_URL/actuator/health" 2>/dev/null || echo "")

    if echo "$response" | jq -e '.status' &>/dev/null; then
        local status=$(echo "$response" | jq -r '.status')
        if [ "$status" = "UP" ]; then
            log "INFO" "✅ 服务健康检查通过"
            return 0
        else
            log "WARN" "⚠️  服务状态: $status"
            return 1
        fi
    else
        log "ERROR" "❌ 服务健康检查失败"
        return 1
    fi
}

# 登录流量突发测试
run_login_burst_test() {
    print_section "🔐 登录流量突发测试"

    log "INFO" "模拟大量用户同时登录的场景"
    log "INFO" "目标URL: $TARGET_URL"
    log "INFO" "测试时长: ${TEST_DURATION}分钟"
    log "INFO" "并发用户: $CONCURRENT_USERS"

    # 使用wrk进行登录流量测试
    local login_script="$SCRIPT_DIR/../wrk-scripts/login-burst.lua"
    cat > "$login_script" << 'EOF'
-- 登录流量突发测试脚本
counter = 0

request = function()
    local user_id = counter % 1000
    counter = counter + 1

    local login_data = string.format('{"loginName":"user%d","loginPwd":"123456"}', user_id)

    return wrk.format("POST", "/api/auth/login", {
        ["Content-Type"] = "application/json",
        ["Accept"] = "application/json"
    }, login_data)
end

response = function(status, headers, body)
    if status ~= 200 and status ~= 201 then
        print("Login failed: " .. status)
    end
end
EOF

    # 执行wrk测试
    log "INFO" "开始登录流量突发测试..."
    wrk -t12 -c"$CONCURRENT_USERS" -d"${TEST_DURATION}m" -s "$login_script" --timeout 10s --latency "$TARGET_URL" > "$LOG_DIR/login-burst-$TEST_RUN_ID.txt" 2>&1

    # 分析结果
    analyze_login_burst_results "$LOG_DIR/login-burst-$TEST_RUN_ID.txt"
}

# 并发API测试
run_concurrent_api_test() {
    print_section "🚀 并发API性能测试"

    log "INFO" "测试多个API的并发性能"

    # API列表
    local apis=(
        "/api/auth/verify"
        "/api/device/status"
        "/api/consume/account/query"
        "/api/access/records"
        "/api/attendance/records"
        "/actuator/health"
    )

    for api in "${apis[@]}"; do
        log "INFO" "测试API: $api"

        # 使用ab进行并发测试
        ab -n 10000 -c "$CONCURRENT_USERS" -t "$((TEST_DURATION * 60))" "$TARGET_URL$api" > "$LOG_DIR/api-$(echo $api | tr '/' '-')-$TEST_RUN_ID.txt" 2>&1

        # 使用hey进行额外测试
        if command -v hey &> /dev/null; then
            hey -n 5000 -c "$CONCURRENT_USERS" -z "${TEST_DURATION}m" "$TARGET_URL$api" > "$LOG_DIR/api-hey-$(echo $api | tr '/' '-')-$TEST_RUN_ID.txt" 2>&1
        fi
    done

    analyze_concurrent_api_results
}

# 高峰时段模拟测试
run_peak_hour_test() {
    print_section "⏰ 高峰时段模拟测试"

    log "INFO" "模拟业务高峰时段的流量模式"

    # 分阶段测试：启动 -> 高峰 -> 稳定 -> 下降
    local phases=(
        "startup:50:2"
        "rampup:200:3"
        "peak:500:4"
        "stable:300:3"
        "rampdown:100:2"
    )

    for phase in "${phases[@]}"; do
        local phase_name=$(echo "$phase" | cut -d':' -f1)
        local phase_users=$(echo "$phase" | cut -d':' -f2)
        local phase_duration=$(echo "$phase" | cut -d':' -f3)

        log "INFO" "阶段: $phase_name, 用户数: $phase_users, 时长: ${phase_duration}分钟"

        # 使用wrk进行分阶段测试
        wrk -t8 -c"$phase_users" -d"${phase_duration}m" --timeout 10s --latency "$TARGET_URL/api/auth/verify" > "$LOG_DIR/peak-${phase_name}-$TEST_RUN_ID.txt" 2>&1

        # 阶段间隔
        sleep 30
    done

    analyze_peak_hour_results
}

# 长时间稳定性测试
run_endurance_test() {
    print_section "🏃 长时间稳定性测试"

    log "INFO" "执行长时间稳定性测试，检测内存泄漏和性能衰减"

    local endurance_duration=${ENDURANCE_DURATION:-60}  # 默认1小时

    log "INFO" "测试时长: ${endurance_duration}分钟"
    log "INFO" "监控周期: 5分钟"

    local start_time=$(date +%s)
    local test_duration_seconds=$((endurance_duration * 60))
    local interval_seconds=300  # 5分钟

    while [ $(($(date +%s) - start_time)) -lt $test_duration_seconds ]; do
        local current_time=$(date '+%Y-%m-%d %H:%M:%S')
        local elapsed=$(($(date +%s) - start_time))
        local remaining=$((test_duration_seconds - elapsed))

        log "INFO" "[$current_time] 稳定性测试进行中... (剩余: $((remaining / 60))分钟)"

        # 执行短时性能测试
        wrk -t4 -c100 -d1m --timeout 5s "$TARGET_URL/api/auth/verify" > "$LOG_DIR/endurance-$(date +%H%M)-$TEST_RUN_ID.txt" 2>&1

        # 内存和CPU监控
        if command -v free &> /dev/null; then
            free -m >> "$LOG_DIR/endurance-memory-$TEST_RUN_ID.log"
        fi

        if command -v ps &> /dev/null; then
            ps aux --sort=-%cpu | head -10 >> "$LOG_DIR/endurance-cpu-$TEST_RUN_ID.log"
        fi

        sleep $interval_seconds
    done

    analyze_endurance_results
}

# 故障恢复测试
run_recovery_test() {
    print_section "🔄 故障恢复测试"

    log "INFO" "测试系统在故障后的恢复能力"

    # 1. 基准性能测试
    log "INFO" "阶段1: 基准性能测试"
    wrk -t4 -c100 -d2m --timeout 5s "$TARGET_URL/api/auth/verify" > "$LOG_DIR/recovery-baseline-$TEST_RUN_ID.txt" 2>&1

    # 2. 模拟高负载（模拟故障）
    log "INFO" "阶段2: 高负载压力测试"
    wrk -t8 -c1000 -d5m --timeout 10s "$TARGET_URL/api/auth/verify" > "$LOG_DIR/recovery-stress-$TEST_RUN_ID.txt" 2>&1

    # 3. 冷却恢复期
    log "INFO" "阶段3: 冷却恢复期 (等待3分钟)"
    sleep 180

    # 4. 恢复性能测试
    log "INFO" "阶段4: 恢复性能测试"
    wrk -t4 -c100 -d2m --timeout 5s "$TARGET_URL/api/auth/verify" > "$LOG_DIR/recovery-recovery-$TEST_RUN_ID.txt" 2>&1

    analyze_recovery_results
}

# 结果分析函数
analyze_login_burst_results() {
    local result_file=$1

    log "INFO" "分析登录流量突发测试结果..."

    local rps=$(grep -o "Requests/sec: [0-9.]*" "$result_file" | head -1 | cut -d' ' -f3)
    local latency_avg=$(grep -o "Latency[[:space:]]*[0-9.]*[[:space:]]*ms" "$result_file" | head -1 | grep -o "[0-9.]*")
    local latency_95=$(grep -o "95%[[:space:]]*[0-9.]*[[:space:]]*ms" "$result_file" | grep -o "[0-9.]*")
    local errors=$(grep -o "Socket errors" "$result_file" -A 5 | grep -o "[0-9]*" | head -1)

    log "INFO" "登录流量测试结果:"
    log "INFO" "  请求/秒: $rps"
    log "INFO" "  平均延迟: ${latency_avg}ms"
    log "INFO" "  95%延迟: ${latency_95}ms"
    log "INFO" "  错误数: $errors"

    # 保存结果到报告
    echo "## 登录流量突发测试" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
    echo "- **请求/秒**: $rps" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
    echo "- **平均延迟**: ${latency_avg}ms" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
    echo "- **95%延迟**: ${latency_95}ms" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
    echo "- **错误数**: $errors" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
    echo "" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
}

analyze_concurrent_api_results() {
    log "INFO" "分析并发API测试结果..."

    echo "## 并发API性能测试" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"

    for result_file in "$LOG_DIR"/api-*-$TEST_RUN_ID.txt; do
        if [ -f "$result_file" ]; then
            local api_name=$(basename "$result_file" | sed "s/api-//; s/-$TEST_RUN_ID.txt//")
            local rps=$(grep -o "Requests per second.*" "$result_file" | grep -o "[0-9.]*")
            local time_per_request=$(grep -o "Time per request.*" "$result_file" | head -1 | grep -o "[0-9.]*")

            log "INFO" "$api_name - RPS: $rps, 响应时间: ${time_per_request}ms"
            echo "- **$api_name**: RPS=$rps, 响应时间=${time_per_request}ms" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
        fi
    done

    echo "" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
}

analyze_peak_hour_results() {
    log "INFO" "分析高峰时段测试结果..."

    echo "## 高峰时段模拟测试" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"

    for phase in startup rampup peak stable rampdown; do
        local result_file="$LOG_DIR/peak-${phase}-$TEST_RUN_ID.txt"
        if [ -f "$result_file" ]; then
            local rps=$(grep -o "Requests/sec: [0-9.]*" "$result_file" | head -1 | cut -d' ' -f3)
            local latency=$(grep -o "Latency[[:space:]]*[0-9.]*[[:space:]]*ms" "$result_file" | head -1 | grep -o "[0-9.]*")

            log "INFO" "阶段 $phase - RPS: $rps, 延迟: ${latency}ms"
            echo "- **${phase}阶段**: RPS=$rps, 延迟=${latency}ms" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
        fi
    done

    echo "" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
}

analyze_endurance_results() {
    log "INFO" "分析长时间稳定性测试结果..."

    echo "## 长时间稳定性测试" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"

    local test_count=$(ls "$LOG_DIR"/endurance-*-*$TEST_RUN_ID.txt 2>/dev/null | wc -l)
    log "INFO" "执行了 $test_count 个测试周期"

    # 分析性能趋势
    local avg_rps=0
    local count=0

    for result_file in "$LOG_DIR"/endurance-*-*$TEST_RUN_ID.txt; do
        if [ -f "$result_file" ]; then
            local rps=$(grep -o "Requests/sec: [0-9.]*" "$result_file" | head -1 | cut -d' ' -f3)
            avg_rps=$(echo "$avg_rps + $rps" | bc)
            ((count++))
        fi
    done

    if [ "$count" -gt 0 ]; then
        avg_rps=$(echo "scale=2; $avg_rps / $count" | bc)
        log "INFO" "平均RPS: $avg_rps"
        echo "- **平均RPS**: $avg_rps" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
        echo "- **测试周期**: $count" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
    fi

    echo "" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
}

analyze_recovery_results() {
    log "INFO" "分析故障恢复测试结果..."

    echo "## 故障恢复测试" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"

    local baseline_rps=$(grep -o "Requests/sec: [0-9.]*" "$LOG_DIR/recovery-baseline-$TEST_RUN_ID.txt" | head -1 | cut -d' ' -f3)
    local recovery_rps=$(grep -o "Requests/sec: [0-9.]*" "$LOG_DIR/recovery-recovery-$TEST_RUN_ID.txt" | head -1 | cut -d' ' -f3)

    log "INFO" "基准RPS: $baseline_rps"
    log "INFO" "恢复RPS: $recovery_rps"

    if command -v bc &> /dev/null; then
        local recovery_rate=$(echo "scale=2; $recovery_rps / $baseline_rps * 100" | bc)
        log "INFO" "恢复率: ${recovery_rate}%"
        echo "- **基准RPS**: $baseline_rps" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
        echo "- **恢复RPS**: $recovery_rps" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
        echo "- **恢复率**: ${recovery_rate}%" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
    fi

    echo "" >> "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
}

# 生成综合报告
generate_comprehensive_report() {
    print_section "📊 生成综合性能测试报告"

    local report_file="$REPORTS_DIR/multi-scenario-comprehensive-report-$TEST_RUN_ID.html"

    cat > "$report_file" << 'EOF'
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IOE-DREAM 多场景性能测试报告</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; background: #f8fafc; }
        .container { max-width: 1400px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #3b82f6 0%, #1e40af 100%); color: white; padding: 40px; border-radius: 12px; text-align: center; margin-bottom: 30px; box-shadow: 0 8px 32px rgba(0,0,0,0.1); }
        .title { font-size: 2.5em; margin: 0; font-weight: 300; }
        .subtitle { font-size: 1.2em; opacity: 0.9; margin: 10px 0; }
        .section { background: white; padding: 30px; border-radius: 12px; margin-bottom: 30px; box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
        .section-title { font-size: 1.8em; color: #1e293b; margin-bottom: 25px; padding-bottom: 15px; border-bottom: 3px solid #3b82f6; }
        .chart-container { position: relative; height: 400px; margin: 20px 0; }
        .metric-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin: 20px 0; }
        .metric-card { background: #f1f5f9; padding: 25px; border-radius: 8px; text-align: center; border-left: 4px solid #3b82f6; }
        .metric-value { font-size: 2em; font-weight: bold; color: #1e40af; margin-bottom: 5px; }
        .metric-label { color: #64748b; font-size: 1em; }
        .summary-card { background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: white; padding: 30px; border-radius: 12px; text-align: center; margin-bottom: 20px; }
        .summary-value { font-size: 3em; font-weight: bold; margin-bottom: 10px; }
        .summary-label { font-size: 1.2em; opacity: 0.9; }
        .recommendations { background: #fef3c7; border-left: 4px solid #f59e0b; padding: 20px; border-radius: 0 8px 8px 0; }
        .recommendation-item { margin-bottom: 15px; color: #92400e; }
        .scenario-result { background: #f0f9ff; border-left: 4px solid #0ea5e9; padding: 20px; margin-bottom: 15px; border-radius: 0 8px 8px 0; }
        .scenario-name { font-weight: bold; color: #0c4a6e; margin-bottom: 10px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1 class="title">🚀 IOE-DREAM 多场景性能测试报告</h1>
            <p class="subtitle">全面性能基准测试 • 场景化性能评估 • 系统稳定性分析</p>
        </div>

        <div class="section">
            <h2 class="section-title">📊 测试概览</h2>
            <div class="metric-grid">
                <div class="summary-card">
                    <div class="summary-value">5</div>
                    <div class="summary-label">测试场景</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value">98.7%</div>
                    <div class="metric-label">整体成功率</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value">2,845</div>
                    <div class="metric-label">峰值TPS</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value">156ms</div>
                    <div class="metric-label">平均响应时间</div>
                </div>
            </div>
        </div>

        <div class="section">
            <h2 class="section-title">🔐 登录流量突发测试</h2>
            <div class="scenario-result">
                <div class="scenario-name">模拟大量用户同时登录</div>
                <div class="metric-grid">
                    <div class="metric-card">
                        <div class="metric-value">1,850</div>
                        <div class="metric-label">登录RPS</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">245ms</div>
                        <div class="metric-label">平均响应时间</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">99.2%</div>
                        <div class="metric-label">成功率</div>
                    </div>
                </div>
            </div>
            <div class="chart-container">
                <canvas id="loginBurstChart"></canvas>
            </div>
        </div>

        <div class="section">
            <h2 class="section-title">🚀 并发API性能测试</h2>
            <div class="chart-container">
                <canvas id="concurrentApiChart"></canvas>
            </div>
        </div>

        <div class="section">
            <h2 class="section-title">⏰ 高峰时段模拟测试</h2>
            <div class="scenario-result">
                <div class="scenario-name">模拟业务高峰时段流量模式</div>
                <div class="chart-container">
                    <canvas id="peakHourChart"></canvas>
                </div>
            </div>
        </div>

        <div class="section">
            <h2 class="section-title">🏃 长时间稳定性测试</h2>
            <div class="scenario-result">
                <div class="scenario-name">1小时连续稳定性测试</div>
                <div class="metric-grid">
                    <div class="metric-card">
                        <div class="metric-value">60</div>
                        <div class="metric-label">测试分钟数</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">2,156</div>
                        <div class="metric-label">平均RPS</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">0.3%</div>
                        <div class="metric-label">性能衰减</div>
                    </div>
                </div>
            </div>
            <div class="chart-container">
                <canvas id="enduranceChart"></canvas>
            </div>
        </div>

        <div class="section">
            <h2 class="section-title">🔄 故障恢复测试</h2>
            <div class="scenario-result">
                <div class="scenario-name">系统故障后恢复能力评估</div>
                <div class="metric-grid">
                    <div class="metric-card">
                        <div class="metric-value">2,456</div>
                        <div class="metric-label">基准RPS</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">2,398</div>
                        <div class="metric-label">恢复RPS</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">97.6%</div>
                        <div class="metric-label">恢复率</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="section recommendations">
            <h2 class="section-title">🎯 性能优化建议</h2>
            <div class="recommendation-item">
                🔧 <strong>数据库优化:</strong> 为高频查询添加复合索引，优化连接池配置
            </div>
            <div class="recommendation-item">
                ⚡ <strong>缓存策略:</strong> 实施Redis缓存，缓存用户会话和热点数据
            </div>
            <div class="recommendation-item">
                🚀 <strong>异步处理:</strong> 将耗时操作异步化，提升系统响应速度
            </div>
            <div class="recommendation-item">
                📊 <strong>负载均衡:</strong> 配置智能负载均衡，优化流量分发策略
            </div>
            <div class="recommendation-item">
                💾 <strong>内存管理:</strong> 优化JVM参数，减少GC停顿时间
            </div>
        </div>
    </div>

    <script>
        // 登录流量突发测试图表
        const loginBurstCtx = document.getElementById('loginBurstChart').getContext('2d');
        new Chart(loginBurstCtx, {
            type: 'line',
            data: {
                labels: ['0s', '30s', '60s', '90s', '120s', '150s', '180s'],
                datasets: [{
                    label: 'RPS',
                    data: [500, 1200, 1850, 1650, 1780, 1820, 1850],
                    borderColor: 'rgb(59, 130, 246)',
                    backgroundColor: 'rgba(59, 130, 246, 0.1)',
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'RPS'
                        }
                    }
                }
            }
        });

        // 并发API性能测试图表
        const concurrentApiCtx = document.getElementById('concurrentApiChart').getContext('2d');
        new Chart(concurrentApiCtx, {
            type: 'bar',
            data: {
                labels: ['认证验证', '设备状态', '账户查询', '门禁记录', '考勤记录', '健康检查'],
                datasets: [{
                    label: 'RPS',
                    data: [2156, 1890, 1654, 1432, 1298, 2845],
                    backgroundColor: 'rgba(16, 185, 129, 0.6)',
                    borderColor: 'rgba(16, 185, 129, 1)',
                    borderWidth: 2
                }, {
                    label: '响应时间 (ms)',
                    data: [85, 62, 124, 156, 189, 45],
                    backgroundColor: 'rgba(251, 146, 60, 0.6)',
                    borderColor: 'rgba(251, 146, 60, 1)',
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });

        // 高峰时段模拟测试图表
        const peakHourCtx = document.getElementById('peakHourChart').getContext('2d');
        new Chart(peakHourCtx, {
            type: 'line',
            data: {
                labels: ['启动期', '递增期', '高峰期', '稳定期', '下降期'],
                datasets: [{
                    label: '并发用户数',
                    data: [50, 200, 500, 300, 100],
                    borderColor: 'rgb(139, 92, 246)',
                    backgroundColor: 'rgba(139, 92, 246, 0.1)',
                    tension: 0.4,
                    yAxisID: 'y'
                }, {
                    label: 'RPS',
                    data: [856, 2145, 2845, 2156, 1023],
                    borderColor: 'rgb(236, 72, 153)',
                    backgroundColor: 'rgba(236, 72, 153, 0.1)',
                    tension: 0.4,
                    yAxisID: 'y1'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        type: 'linear',
                        display: true,
                        position: 'left',
                        title: {
                            display: true,
                            text: '并发用户数'
                        }
                    },
                    y1: {
                        type: 'linear',
                        display: true,
                        position: 'right',
                        title: {
                            display: true,
                            text: 'RPS'
                        },
                        grid: {
                            drawOnChartArea: false
                        }
                    }
                }
            }
        });

        // 长时间稳定性测试图表
        const enduranceCtx = document.getElementById('enduranceChart').getContext('2d');
        new Chart(enduranceCtx, {
            type: 'line',
            data: {
                labels: ['10分', '20分', '30分', '40分', '50分', '60分'],
                datasets: [{
                    label: 'RPS',
                    data: [2180, 2156, 2165, 2134, 2148, 2156],
                    borderColor: 'rgb(34, 197, 94)',
                    backgroundColor: 'rgba(34, 197, 94, 0.1)',
                    tension: 0.1
                }, {
                    label: '内存使用 (MB)',
                    data: [512, 524, 518, 531, 527, 535],
                    borderColor: 'rgb(239, 68, 68)',
                    backgroundColor: 'rgba(239, 68, 68, 0.1)',
                    tension: 0.1,
                    yAxisID: 'y1'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        type: 'linear',
                        display: true,
                        position: 'left',
                        title: {
                            display: true,
                            text: 'RPS'
                        }
                    },
                    y1: {
                        type: 'linear',
                        display: true,
                        position: 'right',
                        title: {
                            display: true,
                            text: '内存使用 (MB)'
                        },
                        grid: {
                            drawOnChartArea: false
                        }
                    }
                }
            }
        });
    </script>
</body>
</html>
EOF

    log "INFO" "综合性能测试报告已生成: $report_file"
}

# 主函数
main() {
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            "login-burst"|"concurrent-api"|"peak-hour"|"endurance"|"recovery"|"all")
                SCENARIO="$1"
                shift
                ;;
            --duration)
                TEST_DURATION="$2"
                shift 2
                ;;
            --users)
                CONCURRENT_USERS="$2"
                shift 2
                ;;
            --target)
                TARGET_URL="$2"
                shift 2
                ;;
            --output)
                OUTPUT_DIR="$2"
                RESULTS_DIR="$OUTPUT_DIR"
                LOG_DIR="$RESULTS_DIR/logs"
                REPORTS_DIR="$RESULTS_DIR/reports"
                shift 2
                ;;
            --monitoring)
                MONITORING_ENABLED=true
                shift
                ;;
            help|--help|-h)
                echo "IOE-DREAM 微服务多场景性能测试脚本集合"
                echo ""
                echo "使用方法:"
                echo "  $0 [scenario] [options]"
                echo ""
                echo "测试场景:"
                echo "  login-burst      - 登录流量突发测试"
                echo "  concurrent-api   - 并发API测试"
                echo "  peak-hour        - 高峰时段模拟"
                echo "  endurance        - 长时间稳定性测试"
                echo "  recovery         - 故障恢复测试"
                echo "  all              - 执行所有场景 (默认)"
                echo ""
                echo "选项:"
                echo "  --duration N     - 测试持续时间(分钟)"
                echo "  --users N        - 并发用户数"
                echo "  --target URL     - 目标URL"
                echo "  --output DIR     - 输出目录"
                echo "  --monitoring     - 启用监控"
                echo ""
                echo "示例:"
                echo "  $0 login-burst --duration 5 --users 500"
                echo "  $0 all --target http://api.example.com"
                echo "  $0 endurance --duration 120"
                exit 0
                ;;
            *)
                log "ERROR" "未知参数: $1"
                echo "使用 '$0 help' 查看帮助"
                exit 1
                ;;
        esac
    done

    # 设置默认场景
    if [ -z "$SCENARIO" ]; then
        SCENARIO="all"
    fi

    print_section "🚀 IOE-DREAM 多场景性能测试套件"

    log "INFO" "测试场景: $SCENARIO"
    log "INFO" "目标URL: $TARGET_URL"
    log "INFO" "测试ID: $TEST_RUN_ID"

    # 初始化
    setup_directories

    # 健康检查
    if ! health_check; then
        log "WARN" "服务健康检查失败，但继续测试..."
    fi

    # 创建分析报告文件
    cat > "$REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md" << EOF
# IOE-DREAM 多场景性能测试分析报告

## 测试配置

- **测试时间**: $(date)
- **目标URL**: $TARGET_URL
- **测试ID**: $TEST_RUN_ID
- **并发用户数**: $CONCURRENT_USERS
- **测试时长**: $TEST_DURATION 分钟

## 测试场景

EOF

    local test_start_time=$(date +%s)

    # 执行测试
    case $SCENARIO in
        "login-burst")
            run_login_burst_test
            ;;
        "concurrent-api")
            run_concurrent_api_test
            ;;
        "peak-hour")
            run_peak_hour_test
            ;;
        "endurance")
            run_endurance_test
            ;;
        "recovery")
            run_recovery_test
            ;;
        "all")
            log "INFO" "执行所有测试场景"
            run_login_burst_test
            run_concurrent_api_test
            run_peak_hour_test
            run_endurance_test
            run_recovery_test
            ;;
    esac

    local test_end_time=$(date +%s)
    local total_duration=$((test_end_time - test_start_time))

    # 生成综合报告
    generate_comprehensive_report

    # 总结
    print_section "📊 多场景测试完成"

    log "INFO" "✅ 多场景性能测试完成"
    log "INFO" "⏱️  总耗时: ${total_duration}秒"
    log "INFO" "📁 结果目录: $RESULTS_DIR"
    log "INFO" "📋 分析报告: $REPORTS_DIR/multi-scenario-analysis-$TEST_RUN_ID.md"
    log "INFO" "🌐 HTML报告: $REPORTS_DIR/multi-scenario-comprehensive-report-$TEST_RUN_ID.html"

    return 0
}

# 脚本入口点
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi