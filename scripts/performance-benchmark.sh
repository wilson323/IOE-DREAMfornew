#!/bin/bash

# ===================================================================
# IOE-DREAM 微服务性能基准测试脚本
# 对微服务架构进行全面的性能测试和基准评估
# 包括响应时间、吞吐量、并发能力、资源使用率等
#
# 使用方法:
#   ./performance-benchmark.sh [all|response|throughput|concurrent|memory|report] [service_name]
#
# 参数说明:
#   all        - 执行全面性能测试 (默认)
#   response   - 测试响应时间
#   throughput - 测试吞吐量
#   concurrent  - 测试并发处理能力
#   memory     - 测试内存使用情况
#   report     - 生成性能报告
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
VERIFICATION_DIR="$PROJECT_ROOT/verification"
LOG_DIR="$VERIFICATION_DIR/logs"
REPORT_DIR="$VERIFICATION_DIR/reports"

# 测试配置
GATEWAY_URL="http://localhost:8080"
BASE_URL="http://localhost"
TEST_TIMEOUT=30
CONCURRENT_USERS=(1 10 50 100 200)
REQUEST_COUNTS=(100 500 1000 2000)

# 微服务端口配置
declare -A SERVICE_PORTS=(
    ["smart-gateway"]="8080"
    ["ioedream-auth-service"]="8081"
    ["ioedream-identity-service"]="8082"
    ["ioedream-device-service"]="8083"
    ["ioedream-access-service"]="8084"
    ["ioedream-consume-service"]="8085"
    ["ioedream-attendance-service"]="8086"
    ["ioedream-video-service"]="8087"
    ["ioedream-oa-service"]="8088"
    ["ioedream-system-service"]="8089"
    ["ioedream-monitor-service"]="8090"
)

# 性能测试结果
declare -A PERFORMANCE_RESULTS=()
declare -A RESPONSE_TIMES=()
declare -A THROUGHPUT_RATES=()
declare -A MEMORY_USAGE=()
declare -A CPU_USAGE=()

# 日志记录函数
log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "[$timestamp] [$level] $message" | tee -a "$LOG_DIR/performance-benchmark.log"

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

# 打印分隔线
print_separator() {
    echo -e "${PURPLE}==================================================================${NC}"
}

# 打印标题
print_section() {
    echo ""
    print_separator
    echo -e "${CYAN}📋 $1${NC}"
    print_separator
}

# HTTP请求函数（用于性能测试）
make_perf_request() {
    local url=$1
    local method=${2:-"GET"}
    local data=$3
    local timeout=${4:-$TEST_TIMEOUT}

    local start_time=$(date +%s%N)
    local response_code=$(curl -s -o /dev/null -w "%{http_code}" \
        -X "$method" \
        --connect-timeout "$timeout" \
        --max-time "$timeout" \
        -H "Content-Type: application/json" \
        ${data:+-d "$data"} \
        "$url" 2>/dev/null || echo "000")
    local end_time=$(date +%s%N)
    local response_time=$(((end_time - start_time) / 1000000)) # 转换为毫秒

    echo "$response_code|$response_time"
}

# 测试单个服务的响应时间
test_service_response_time() {
    local service_name=$1
    local port=${SERVICE_PORTS[$service_name]}
    local url="http://localhost:$port/actuator/health"

    log "INFO" "测试 $service_name 响应时间..."

    local total_time=0
    local success_count=0
    local min_time=999999
    local max_time=0
    local test_count=50

    for ((i=1; i<=test_count; i++)); do
        local result=$(make_perf_request "$url")
        local response_code=$(echo "$result" | cut -d'|' -f1)
        local response_time=$(echo "$result" | cut -d'|' -f2)

        if [ "$response_code" = "200" ]; then
            ((success_count++))
            total_time=$((total_time + response_time))

            if [ $response_time -lt $min_time ]; then
                min_time=$response_time
            fi

            if [ $response_time -gt $max_time ]; then
                max_time=$response_time
            fi
        fi

        # 显示进度
        if [ $((i % 10)) -eq 0 ]; then
            echo -n "."
        fi
    done

    echo ""

    if [ $success_count -gt 0 ]; then
        local avg_time=$((total_time / success_count))

        RESPONSE_TIMES["${service_name}-avg"]="$avg_time"
        RESPONSE_TIMES["${service_name}-min"]="$min_time"
        RESPONSE_TIMES["${service_name}-max"]="$max_time"
        RESPONSE_TIMES["${service_name}-success"]="$success_count"

        log "INFO" "$service_name 响应时间统计:"
        log "INFO" "  平均: ${avg_time}ms"
        log "INFO" "  最小: ${min_time}ms"
        log "INFO" "  最大: ${max_time}ms"
        log "INFO" "  成功率: $((success_count * 100 / test_count))%"

        # 性能评估
        if [ $avg_time -lt 100 ]; then
            PERFORMANCE_RESULTS["$service_name-response"]="EXCELLENT"
        elif [ $avg_time -lt 500 ]; then
            PERFORMANCE_RESULTS["$service_name-response"]="GOOD"
        elif [ $avg_time -lt 1000 ]; then
            PERFORMANCE_RESULTS["$service_name-response"]="FAIR"
        else
            PERFORMANCE_RESULTS["$service_name-response"]="POOR"
        fi
    else
        log "ERROR" "$service_name 响应时间测试失败"
        PERFORMANCE_RESULTS["$service_name-response"]="FAILED"
    fi
}

# 测试系统响应时间
test_response_times() {
    print_section "⚡ 响应时间性能测试"

    echo -e "${BLUE}1. 测试各微服务响应时间${NC}"

    for service in "${!SERVICE_PORTS[@]}"; do
        echo ""
        echo -e "${YELLOW}测试 $service 响应时间...${NC}"
        test_service_response_time "$service"
    done

    echo -e "\n${BLUE}2. 测试网关路由响应时间${NC}"

    # 测试通过网关的路由性能
    local gateway_paths=(
        "/api/auth/health"
        "/api/identity/health"
        "/api/device/health"
        "/api/access/health"
        "/api/consume/health"
    )

    for path in "${gateway_paths[@]}"; do
        echo -e "${YELLOW}测试网关路由 $path...${NC}"

        local total_time=0
        local success_count=0
        local test_count=30

        for ((i=1; i<=test_count; i++)); do
            local result=$(make_perf_request "$GATEWAY_URL$path")
            local response_code=$(echo "$result" | cut -d'|' -f1)
            local response_time=$(echo "$result" | cut -d'|' -f2)

            if [ "$response_code" = "200" ]; then
                ((success_count++))
                total_time=$((total_time + response_time))
            fi
        done

        if [ $success_count -gt 0 ]; then
            local avg_time=$((total_time / success_count))
            RESPONSE_TIMES["gateway-$(echo $path | tr '/' '-')"]="$avg_time"
            log "INFO" "网关路由 $path 平均响应时间: ${avg_time}ms"
        fi
    done

    return 0
}

# 测试吞吐量
test_throughput() {
    print_section "📊 吞吐量性能测试"

    # 使用ab (Apache Bench) 或 wrk进行吞吐量测试
    if ! command -v ab &> /dev/null && ! command -v wrk &> /dev/null; then
        log "WARN" "未找到ab或wrk工具，使用简单的吞吐量测试"
        test_throughput_simple
        return 0
    fi

    local test_urls=(
        "http://localhost:8081/api/auth/health"
        "http://localhost:8080/api/auth/health"
    )

    for url in "${test_urls[@]}"; do
        echo -e "${BLUE}测试 $url 吞吐量...${NC}"

        if command -v wrk &> /dev/null; then
            # 使用wrk进行测试
            local wrk_result=$(wrk -t4 -c100 -d30s --timeout 10s "$url" 2>/dev/null || echo "")

            local rps=$(echo "$wrk_result" | grep -o "Requests/sec: [0-9.]*" | cut -d' ' -f2)
            local latency_avg=$(echo "$wrk_result" | grep -o "Latency[[:space:]]*[0-9.]*[[:space:]]*ms" | grep -o "[0-9.]*")

            if [ -n "$rps" ]; then
                THROUGHPUT_RATES["$url"]="$rps"
                log "INFO" "$url 吞吐量: ${rps} req/s"
            fi
        elif command -v ab &> /dev/null; then
            # 使用ab进行测试
            local ab_result=$(ab -n 1000 -c 50 "$url" 2>/dev/null || echo "")

            local rps=$(echo "$ab_result" | grep "Requests per second" | grep -o "[0-9.]*")

            if [ -n "$rps" ]; then
                THROUGHPUT_RATES["$url"]="$rps"
                log "INFO" "$url 吞吐量: ${rps} req/s"
            fi
        fi
    done

    return 0
}

# 简单吞吐量测试（当没有wrk/ab时）
test_throughput_simple() {
    log "INFO" "使用简单方法进行吞吐量测试"

    local test_url="http://localhost:8081/api/auth/health"
    local duration=30  # 测试30秒
    local start_time=$(date +%s)
    local request_count=0

    while [ $(($(date +%s) - start_time)) -lt $duration ]; do
        local result=$(make_perf_request "$test_url")
        local response_code=$(echo "$result" | cut -d'|' -f1)

        if [ "$response_code" = "200" ]; then
            ((request_count++))
        fi

        # 避免过于频繁的请求
        sleep 0.01
    done

    local rps=$((request_count / duration))
    THROUGHPUT_RATES["$test_url"]="$rps"
    log "INFO" "$test_url 简单吞吐量: ${rps} req/s"

    return 0
}

# 测试并发处理能力
test_concurrent_performance() {
    print_section "🔥 并发性能测试"

    log "INFO" "测试不同并发级别下的性能表现..."

    local test_url="http://localhost:8081/api/auth/health"

    for concurrent in "${CONCURRENT_USERS[@]}"; do
        echo -e "${YELLOW}测试并发用户数: $concurrent${NC}"

        # 启动并发进程
        local pids=()
        local start_time=$(date +%s)

        for ((i=1; i<=concurrent; i++)); do
            {
                local requests=0
                local successes=0
                local local_start=$(date +%s%N)

                # 每个进程发送10个请求
                for ((j=1; j<=10; j++)); do
                    local result=$(make_perf_request "$test_url" "GET" "" "15")
                    local response_code=$(echo "$result" | cut -d'|' -f1)

                    ((requests++))
                    if [ "$response_code" = "200" ]; then
                        ((successes++))
                    fi
                done

                local local_end=$(date +%s%N)
                local duration=$(((local_end - local_start) / 1000000000))

                echo "$concurrent,$i,$requests,$successes,$duration" >> "$LOG_DIR/concurrent-test-$concurrent.log"
            } &
            pids+=($!)
        done

        # 等待所有进程完成
        for pid in "${pids[@]}"; do
            wait "$pid"
        done

        local end_time=$(date +%s)
        local total_time=$((end_time - start_time))

        # 分析结果
        if [ -f "$LOG_DIR/concurrent-test-$concurrent.log" ]; then
            local total_requests=$(awk -F',' '{sum+=$3} END {print sum}' "$LOG_DIR/concurrent-test-$concurrent.log")
            local total_successes=$(awk -F',' '{sum+=$4} END {print sum}' "$LOG_DIR/concurrent-test-$concurrent.log")

            local rps=$((total_requests / total_time))
            local success_rate=$((total_successes * 100 / total_requests))

            log "INFO" "并发 $concurrent 用户结果:"
            log "INFO" "  总请求数: $total_requests"
            log "INFO" "  成功请求: $total_successes"
            log "INFO" "  成功率: ${success_rate}%"
            log "INFO "  吞吐量: ${rps} req/s"

            THROUGHPUT_RATES["concurrent-$concurrent"]="$rps"
            PERFORMANCE_RESULTS["concurrent-$concurrent"]="$success_rate"
        fi
    done

    return 0
}

# 测试内存和CPU使用情况
test_resource_usage() {
    print_section "💾 资源使用情况测试"

    echo -e "${BLUE}1. 测试内存使用情况${NC}"

    for service in "${!SERVICE_PORTS[@]}"; do
        # 获取容器ID（如果服务在容器中运行）
        local container_name="ioedream-${service#ioedream-}"
        local container_id=$(docker ps -q --filter "name=$container_name" 2>/dev/null || echo "")

        if [ -n "$container_id" ]; then
            # 获取内存使用情况
            local memory_stats=$(docker stats --no-stream --format "table {{.MemUsage}} {{.MemPerc}}" "$container_id" 2>/dev/null | tail -1)
            local cpu_stats=$(docker stats --no-stream --format "{{.CPUPerc}}" "$container_id" 2>/dev/null)

            if [ -n "$memory_stats" ]; then
                MEMORY_USAGE["$service"]="$memory_stats"
                log "INFO" "$service 内存使用: $memory_stats"
            fi

            if [ -n "$cpu_stats" ]; then
                CPU_USAGE["$service"]="$cpu_stats"
                log "INFO" "$service CPU使用: $cpu_stats"
            fi
        else
            log "DEBUG" "$service 未在Docker容器中运行"
        fi
    done

    echo -e "\n${BLUE}2. 测试系统资源监控${NC}"

    # 获取系统整体资源使用情况
    if command -v free &> /dev/null; then
        local mem_info=$(free -h | grep "Mem:")
        log "INFO" "系统内存: $mem_info"
    fi

    if command -v uptime &> /dev/null; then
        local load_avg=$(uptime | grep -o "load average:.*" | cut -d' ' -f3-)
        log "INFO" "系统负载: $load_avg"
    fi

    return 0
}

# 生成性能基准报告
generate_performance_report() {
    print_section "📋 生成性能基准测试报告"

    local report_file="$REPORT_DIR/performance-benchmark-report-$(date +%Y%m%d_%H%M%S).html"

    log "INFO" "生成性能基准测试报告: $report_file"

    # 计算统计信息
    local total_tests=${#PERFORMANCE_RESULTS[@]}
    local excellent_count=0
    local good_count=0
    local fair_count=0
    local poor_count=0

    for result in "${PERFORMANCE_RESULTS[@]}"; do
        case $result in
            "EXCELLENT")
                ((excellent_count++))
                ;;
            "GOOD")
                ((good_count++))
                ;;
            "FAIR")
                ((fair_count++))
                ;;
            "POOR"|"FAILED")
                ((poor_count++))
                ;;
        esac
    done

    # 生成HTML报告
    cat > "$report_file" << EOF
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IOE-DREAM 微服务性能基准测试报告</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body { font-family: 'Arial', sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1400px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; border-bottom: 3px solid #007acc; padding-bottom: 20px; margin-bottom: 30px; }
        .title { color: #007acc; font-size: 28px; margin: 0; }
        .subtitle { color: #666; font-size: 16px; margin: 10px 0; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: center; }
        .card.excellent { background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); }
        .card.good { background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%); }
        .card.fair { background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%); }
        .card.poor { background: linear-gradient(135deg, #f44336 0%, #da190b 100%); }
        .card h3 { margin: 0 0 10px 0; font-size: 24px; }
        .card p { margin: 0; font-size: 16px; }
        .section { margin-bottom: 40px; }
        .section h2 { color: #333; border-left: 4px solid #007acc; padding-left: 15px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f8f9fa; font-weight: bold; }
        .performance { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
        .performance.excellent { background: #d4edda; color: #155724; }
        .performance.good { background: #d1ecf1; color: #0c5460; }
        .performance.fair { background: #fff3cd; color: #856404; }
        .performance.poor { background: #f8d7da; color: #721c24; }
        .chart-container { position: relative; height: 400px; margin: 20px 0; }
        .metric-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }
        .metric-card { border: 1px solid #ddd; border-radius: 8px; padding: 20px; background: #fafafa; }
        .metric-card h4 { margin: 0 0 15px 0; color: #007acc; }
        .timestamp { color: #666; font-size: 14px; margin-top: 20px; text-align: right; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1 class="title">🚀 IOE-DREAM 微服务性能基准测试报告</h1>
            <p class="subtitle">性能测试与基准评估 • $(date)</p>
        </div>

        <div class="summary">
            <div class="card excellent">
                <h3>$excellent_count</h3>
                <p>优秀性能</p>
            </div>
            <div class="card good">
                <h3>$good_count</h3>
                <p>良好性能</p>
            </div>
            <div class="card fair">
                <h3>$fair_count</h3>
                <p>一般性能</p>
            </div>
            <div class="card poor">
                <h3>$poor_count</h3>
                <p>需要优化</p>
            </div>
        </div>

        <div class="section">
            <h2>📊 响应时间分析</h2>
            <div class="chart-container">
                <canvas id="responseTimeChart"></canvas>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>服务名称</th>
                        <th>平均响应时间</th>
                        <th>最小响应时间</th>
                        <th>最大响应时间</th>
                        <th>成功率</th>
                        <th>性能等级</th>
                    </tr>
                </thead>
                <tbody>
EOF

    # 添加响应时间数据
    for service in "${!SERVICE_PORTS[@]}"; do
        local avg_time=${RESPONSE_TIMES["${service_name}-avg"]:-"N/A"}
        local min_time=${RESPONSE_TIMES["${service_name}-min"]:-"N/A"}
        local max_time=${RESPONSE_TIMES["${service_name}-max"]:-"N/A"}
        local success_count=${RESPONSE_TIMES["${service_name}-success"]:-0}
        local performance=${PERFORMANCE_RESULTS["${service_name-response"]:-"UNKNOWN"}

        local performance_class="fair"
        case $performance in
            "EXCELLENT") performance_class="excellent" ;;
            "GOOD") performance_class="good" ;;
            "FAIR") performance_class="fair" ;;
            "POOR"|"FAILED") performance_class="poor" ;;
        esac

        cat >> "$report_file" << EOF
                    <tr>
                        <td>$service</td>
                        <td>${avg_time}ms</td>
                        <td>${min_time}ms</td>
                        <td>${max_time}ms</td>
                        <td>${success_count}%</td>
                        <td><span class="performance $performance_class">$performance</span></td>
                    </tr>
EOF
    done

    cat >> "$report_file" << EOF
                </tbody>
            </table>
        </div>

        <div class="section">
            <h2>📈 吞吐量性能分析</h2>
            <div class="chart-container">
                <canvas id="throughputChart"></canvas>
            </div>
            <div class="metric-grid">
EOF

    # 添加吞吐量数据
    for url in "${!THROUGHPUT_RATES[@]}"; do
        local rps=${THROUGHPUT_RATES[$url]}
        cat >> "$report_file" << EOF
                <div class="metric-card">
                    <h4>吞吐量指标</h4>
                    <p><strong>URL:</strong> $url</p>
                    <p><strong>请求/秒:</strong> $rps</p>
                </div>
EOF
    done

    cat >> "$report_file" << EOF
            </div>
        </div>

        <div class="section">
            <h2>💾 资源使用情况</h2>
            <div class="chart-container">
                <canvas id="resourceChart"></canvas>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>服务名称</th>
                        <th>内存使用</th>
                        <th>CPU使用率</th>
                        <th>资源状态</th>
                    </tr>
                </thead>
                <tbody>
EOF

    # 添加资源使用数据
    for service in "${!SERVICE_PORTS[@]}"; do
        local memory=${MEMORY_USAGE[$service]:-"N/A"}
        local cpu=${CPU_USAGE[$service]:-"N/A"}

        local resource_status="NORMAL"
        if [[ "$memory" =~ ([0-9.]+)% ]] && (( $(echo "${BASH_REMATCH[1]} > 80" | bc -l) )); then
            resource_status="HIGH"
        fi

        cat >> "$report_file" << EOF
                    <tr>
                        <td>$service</td>
                        <td>$memory</td>
                        <td>$cpu</td>
                        <td>$resource_status</td>
                    </tr>
EOF
    done

    cat >> "$report_file" << EOF
                </tbody>
            </table>
        </div>

        <div class="section">
            <h2>🎯 性能优化建议</h2>
            <ul>
EOF

    # 根据测试结果生成优化建议
    if [ $poor_count -gt 0 ]; then
        cat >> "$report_file" << EOF
                <li>🔧 发现 $poor_count 个性能较差的服务，建议优先优化</li>
                <li>⚡ 考虑实施缓存策略以减少响应时间</li>
                <li>🔍 分析数据库查询性能，优化慢查询</li>
EOF
    fi

    if [ $fair_count -gt 0 ]; then
        cat >> "$report_file" << EOF
                <li>📈 $fair_count 个服务性能一般，建议进行性能调优</li>
EOF
    fi

    cat >> "$report_file" << EOF
                <li>🚀 考虑实施自动扩缩容以提高并发处理能力</li>
                <li>📊 建议建立持续的性能监控体系</li>
                <li>🛠️ 考虑使用CDN和负载均衡优化性能</li>
                <li>💾 优化数据库连接池和缓存配置</li>
            </ul>
        </div>

        <div class="timestamp">
            报告生成时间：$(date) <br>
            性能基准测试工具版本：v1.0.0
        </div>
    </div>

    <script>
        // 响应时间图表
        const responseTimeCtx = document.getElementById('responseTimeChart').getContext('2d');
        new Chart(responseTimeCtx, {
            type: 'bar',
            data: {
                labels: [$(for service in "${!SERVICE_PORTS[@]}"; do echo "'$service',"; done)],
                datasets: [{
                    label: '平均响应时间 (ms)',
                    data: [$(for service in "${!SERVICE_PORTS[@]}"; do echo "${RESPONSE_TIMES["${service_name}-avg"]:-0},"; done)],
                    backgroundColor: 'rgba(54, 162, 235, 0.6)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: '响应时间 (ms)'
                        }
                    }
                }
            }
        });

        // 吞吐量图表
        const throughputCtx = document.getElementById('throughputChart').getContext('2d');
        new Chart(throughputCtx, {
            type: 'line',
            data: {
                labels: [$(for concurrent in "${CONCURRENT_USERS[@]}"; do echo "'$concurrent users',"; done)],
                datasets: [{
                    label: '吞吐量 (req/s)',
                    data: [$(for concurrent in "${CONCURRENT_USERS[@]}"; do echo "${THROUGHPUT_RATES["concurrent-$concurrent"]:-0},"; done)],
                    backgroundColor: 'rgba(75, 192, 192, 0.6)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 2,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: '吞吐量 (req/s)'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: '并发用户数'
                        }
                    }
                }
            }
        });

        // 资源使用图表
        const resourceCtx = document.getElementById('resourceChart').getContext('2d');
        new Chart(resourceCtx, {
            type: 'doughnut',
            data: {
                labels: ['优秀', '良好', '一般', '需要优化'],
                datasets: [{
                    data: [$excellent_count, $good_count, $fair_count, $poor_count],
                    backgroundColor: [
                        'rgba(76, 175, 80, 0.8)',
                        'rgba(33, 150, 243, 0.8)',
                        'rgba(255, 152, 0, 0.8)',
                        'rgba(244, 67, 54, 0.8)'
                    ]
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'bottom'
                    },
                    title: {
                        display: true,
                        text: '性能等级分布'
                    }
                }
            }
        });
    </script>
</body>
</html>
EOF

    log "INFO" "性能基准测试报告已生成: $report_file"
    echo -e "\n${GREEN}✅ 报告生成完成${NC}"
    echo -e "报告路径: ${BLUE}$report_file${NC}"

    return 0
}

# 显示性能测试摘要
show_performance_summary() {
    print_section "📊 性能测试结果摘要"

    local total_tests=${#PERFORMANCE_RESULTS[@]}
    local excellent_count=0
    local good_count=0
    local fair_count=0
    local poor_count=0

    for result in "${PERFORMANCE_RESULTS[@]}"; do
        case $result in
            "EXCELLENT")
                ((excellent_count++))
                ;;
            "GOOD")
                ((good_count++))
                ;;
            "FAIR")
                ((fair_count++))
                ;;
            "POOR"|"FAILED")
                ((poor_count++))
                ;;
        esac
    done

    echo -e "总测试项目: ${YELLOW}$total_tests${NC}"
    echo -e "优秀性能:   ${GREEN}$excellent_count${NC}"
    echo -e "良好性能:   ${BLUE}$good_count${NC}"
    echo -e "一般性能:   ${YELLOW}$fair_count${NC}"
    echo -e "需要优化:   ${RED}$poor_count${NC}"

    # 显示关键性能指标
    echo ""
    echo -e "${CYAN}关键性能指标:${NC}"

    for service in "${!SERVICE_PORTS[@]}"; do
        if [ -n "${RESPONSE_TIMES["${service_name}-avg"]}" ]; then
            local avg_time=${RESPONSE_TIMES["${service_name}-avg"]}
            echo -e "$service: ${avg_time}ms"
        fi
    done

    return 0
}

# 主函数
main() {
    local command=${1:-"all"}

    case $command in
        "response")
            print_section "⚡ 响应时间专项测试"
            test_response_times
            show_performance_summary
            ;;
        "throughput")
            print_section "📊 吞吐量专项测试"
            test_throughput
            show_performance_summary
            ;;
        "concurrent")
            print_section "🔥 并发性能专项测试"
            test_concurrent_performance
            show_performance_summary
            ;;
        "memory")
            print_section "💾 资源使用专项测试"
            test_resource_usage
            show_performance_summary
            ;;
        "all")
            print_section "🚀 开始完整性能基准测试"
            test_response_times
            test_throughput
            test_concurrent_performance
            test_resource_usage
            show_performance_summary
            ;;
        "report")
            generate_performance_report
            ;;
        "help"|"--help"|"-h")
            echo "IOE-DREAM 微服务性能基准测试工具"
            echo ""
            echo "使用方法:"
            echo "  $0 [命令] [服务名称]"
            echo ""
            echo "命令:"
            echo "  response   - 测试响应时间"
            echo "  throughput - 测试吞吐量"
            echo "  concurrent  - 测试并发处理能力"
            echo "  memory     - 测试内存使用情况"
            echo "  all        - 执行全面性能测试 (默认)"
            echo "  report     - 生成HTML性能报告"
            echo "  help       - 显示帮助信息"
            echo ""
            echo "示例:"
            echo "  $0 all              # 执行完整性能测试"
            echo "  $0 response         # 仅测试响应时间"
            echo "  $0 throughput       # 仅测试吞吐量"
            echo "  $0 report           # 生成HTML报告"
            ;;
        *)
            log "ERROR" "未知命令: $command"
            echo "使用 '$0 help' 查看帮助"
            exit 1
            ;;
    esac
}

# 脚本入口点
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi