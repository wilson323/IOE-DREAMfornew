#!/bin/bash

# ===================================================================
# IOE-DREAM 微服务健康检查脚本
# 功能：检查所有微服务的健康状态、资源使用情况、依赖连接等
# 支持详细健康检查报告和实时监控
#
# 使用方法:
#   ./health-check.sh [check|monitor|report|detail] [service_name]
#
# 参数说明:
#   check   - 执行一次全面健康检查 (默认)
#   monitor - 实时监控模式
#   report  - 生成健康检查报告
#   detail  - 检查指定服务的详细信息
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
CONFIG_DIR="$VERIFICATION_DIR/config"

# 微服务配置
declare -A MICROSERVICES=(
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

# 健康检查统计
declare -A SERVICE_STATUS=()
declare -A SERVICE_RESPONSE_TIME=()
declare -A SERVICE_CPU_USAGE=()
declare -A SERVICE_MEMORY_USAGE=()
declare -A SERVICE_DEPENDENCIES=()

# 日志记录函数
log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "[$timestamp] [$level] $message" | tee -a "$LOG_DIR/health-check.log"

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

# 检查Docker容器状态
check_container_health() {
    local service=$1
    local port=$2
    local container_name="ioedream-${service#ioedream-}"

    log "DEBUG" "检查容器状态: $container_name"

    # 检查容器是否存在
    if ! docker ps -a --format "{{.Names}}" | grep -q "^$container_name$"; then
        log "ERROR" "容器不存在: $container_name"
        SERVICE_STATUS[$service]="CONTAINER_NOT_FOUND"
        return 1
    fi

    # 检查容器运行状态
    local container_status=$(docker inspect --format="{{.State.Status}}" "$container_name" 2>/dev/null || echo "not_found")
    local health_status=$(docker inspect --format="{{.State.Health.Status}}" "$container_name" 2>/dev/null || echo "none")

    if [ "$container_status" != "running" ]; then
        log "ERROR" "容器未运行: $container_name (状态: $container_status)"
        SERVICE_STATUS[$service]="CONTAINER_STOPPED"
        return 1
    fi

    # 检查健康检查状态
    if [ "$health_status" = "healthy" ]; then
        log "INFO" "容器健康状态正常: $container_name"
        SERVICE_STATUS[$service]="HEALTHY"
        return 0
    elif [ "$health_status" = "unhealthy" ]; then
        log "WARN" "容器健康检查失败: $container_name"
        SERVICE_STATUS[$service]="UNHEALTHY"
        return 1
    elif [ "$health_status" = "starting" ]; then
        log "INFO" "容器启动中: $container_name"
        SERVICE_STATUS[$service]="STARTING"
        return 1
    else
        log "INFO" "容器无健康检查配置: $container_name"
        SERVICE_STATUS[$service]="NO_HEALTH_CHECK"
        return 0
    fi
}

# 检查HTTP健康端点
check_http_health() {
    local service=$1
    local port=$2
    local endpoint="/actuator/health"
    local url="http://localhost:$port$endpoint"
    local timeout=10

    log "DEBUG" "检查HTTP健康端点: $url"

    # 测量响应时间
    local start_time=$(date +%s%N)
    local response_code=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout "$timeout" "$url" 2>/dev/null || echo "000")
    local end_time=$(date +%s%N)
    local response_time=$(( (end_time - start_time) / 1000000 )) # 转换为毫秒

    SERVICE_RESPONSE_TIME[$service]=$response_time

    if [ "$response_code" = "200" ]; then
        log "INFO" "$service HTTP健康检查通过 (${response_time}ms): $url"
        return 0
    elif [ "$response_code" = "000" ]; then
        log "ERROR" "$service HTTP健康检查失败 (连接超时): $url"
        return 1
    else
        log "ERROR" "$service HTTP健康检查失败 (HTTP $response_code): $url"
        return 1
    fi
}

# 检查服务资源使用情况
check_resource_usage() {
    local service=$1
    local container_name="ioedream-${service#ioedream-}"

    log "DEBUG" "检查资源使用情况: $container_name"

    # 获取CPU使用率
    local cpu_usage=$(docker stats --no-stream --format "table {{.CPUPerc}}" "$container_name" | tail -n 1 | sed 's/%//')
    SERVICE_CPU_USAGE[$service]="${cpu_usage}%"

    # 获取内存使用情况
    local memory_usage=$(docker stats --no-stream --format "table {{.MemUsage}} {{.MemPerc}}" "$container_name" | tail -n 1)
    SERVICE_MEMORY_USAGE[$service]="$memory_usage"

    # 检查资源使用是否异常
    local cpu_num=$(echo "$cpu_usage" | sed 's/[^0-9.]//g')
    if [ -n "$cpu_num" ] && (( $(echo "$cpu_num > 80" | bc -l) )); then
        log "WARN" "$service CPU使用率过高: ${cpu_usage}%"
    fi

    log "DEBUG" "$service 资源使用 - CPU: ${cpu_usage}%, 内存: $memory_usage"
}

# 检查服务依赖连接
check_service_dependencies() {
    local service=$1
    local port=$2
    local container_name="ioedream-${service#ioedream-}"

    log "DEBUG" "检查服务依赖: $service"

    local dependencies=()
    local dependency_status=()

    # 检查数据库连接
    if docker exec "$container_name" netstat -tuln 2>/dev/null | grep -q ":3306"; then
        dependencies+=("MySQL")
        dependency_status+=("CONNECTED")
    else
        dependencies+=("MySQL")
        dependency_status+=("DISCONNECTED")
    fi

    # 检查Redis连接
    if docker exec "$container_name" netstat -tuln 2>/dev/null | grep -q ":6379"; then
        dependencies+=("Redis")
        dependency_status+=("CONNECTED")
    else
        dependencies+=("Redis")
        dependency_status+=("DISCONNECTED")
    fi

    # 检查Nacos连接
    if docker exec "$container_name" netstat -tuln 2>/dev/null | grep -q ":8848"; then
        dependencies+=("Nacos")
        dependency_status+=("CONNECTED")
    else
        dependencies+=("Nacos")
        dependency_status+=("DISCONNECTED")
    fi

    # 记录依赖状态
    local dep_info=""
    for i in "${!dependencies[@]}"; do
        if [ -n "$dep_info" ]; then
            dep_info+=", "
        fi
        dep_info+="${dependencies[$i]}:${dependency_status[$i]}"
    done
    SERVICE_DEPENDENCIES[$service]="$dep_info"

    log "DEBUG" "$service 依赖状态: $dep_info"
}

# 执行单个服务的全面健康检查
check_single_service() {
    local service=$1
    local port=$2

    log "INFO" "开始检查服务: $service"

    local overall_status="HEALTHY"
    local issues=()

    # 检查容器状态
    if ! check_container_health "$service" "$port"; then
        overall_status="UNHEALTHY"
        issues+=("Container issue: ${SERVICE_STATUS[$service]}")
    fi

    # 检查HTTP健康端点
    if ! check_http_health "$service" "$port"; then
        overall_status="UNHEALTHY"
        issues+=("HTTP health check failed")
    fi

    # 检查资源使用情况
    check_resource_usage "$service" "$port"

    # 检查服务依赖
    check_service_dependencies "$service" "$port"

    # 检查服务日志中是否有错误
    local container_name="ioedream-${service#ioedream-}"
    local error_count=$(docker logs --since=1h "$container_name" 2>&1 | grep -i -c "error\|exception\|failed" || echo "0")
    if [ "$error_count" -gt 5 ]; then
        overall_status="DEGRADED"
        issues+=("High error count in logs: $error_count")
    fi

    # 记录最终状态
    SERVICE_STATUS[$service]="$overall_status"

    # 输出检查结果
    local status_color=$GREEN
    case $overall_status in
        "UNHEALTHY") status_color=$RED ;;
        "DEGRADED") status_color=$YELLOW ;;
        "STARTING") status_color=$BLUE ;;
    esac

    echo -e "  $service (${port}): ${status_color}$overall_status${NC}"
    if [ ${#issues[@]} -gt 0 ]; then
        echo -e "    问题: ${YELLOW}${issues[*]}${NC}"
    fi

    # 输出详细信息
    echo -e "    响应时间: ${SERVICE_RESPONSE_TIME[$service]}ms"
    echo -e "    CPU使用: ${SERVICE_CPU_USAGE[$service]}"
    echo -e "    内存使用: ${SERVICE_MEMORY_USAGE[$service]}"
    echo -e "    依赖状态: ${SERVICE_DEPENDENCIES[$service]}"

    return 0
}

# 执行全面健康检查
run_comprehensive_health_check() {
    print_section "🏥 执行全面健康检查"

    local total_services=${#MICROSERVICES[@]}
    local healthy_services=0
    local unhealthy_services=0
    local degraded_services=0

    log "INFO" "开始检查 $total_services 个微服务..."

    for service in "${!MICROSERVICES[@]}"; do
        local port=${MICROSERVICES[$service]}

        echo ""
        echo -e "${BLUE}检查服务: $service${NC}"
        echo "─────────────────────────────────────────────────────────"

        check_single_service "$service" "$port"

        # 统计健康状态
        case "${SERVICE_STATUS[$service]}" in
            "HEALTHY") ((healthy_services++)) ;;
            "UNHEALTHY"|"CONTAINER_STOPPED"|"CONTAINER_NOT_FOUND") ((unhealthy_services++)) ;;
            "DEGRADED"|"STARTING") ((degraded_services++)) ;;
        esac
    done

    # 输出汇总
    echo ""
    print_separator
    echo -e "${CYAN}📊 健康检查汇总${NC}"
    print_separator
    echo -e "健康服务:   ${GREEN}$healthy_services${NC}/$total_services"
    echo -e "不健康服务: ${RED}$unhealthy_services${NC}/$total_services"
    echo -e "降级服务:   ${YELLOW}$degraded_services${NC}/$total_services"

    local health_rate=$((healthy_services * 100 / total_services))
    echo -e "健康率:     ${BLUE}${health_rate}%${NC}"

    if [ $health_rate -ge 90 ]; then
        echo -e "整体状态: ${GREEN}✅ 优秀${NC}"
    elif [ $health_rate -ge 70 ]; then
        echo -e "整体状态: ${YELLOW}⚠️ 良好${NC}"
    else
        echo -e "整体状态: ${RED}❌ 需要关注${NC}"
    fi

    return 0
}

# 实时监控模式
run_monitoring_mode() {
    print_section "📡 实时健康监控模式"
    log "INFO" "启动实时监控，按 Ctrl+C 停止..."

    while true; do
        clear
        print_section "📡 IOE-DREAM 微服务实时监控 - $(date)"

        # 显示实时状态表格
        printf "%-25s %-8s %-12s %-10s %-15s %-20s\n" "服务名称" "端口" "状态" "响应时间" "CPU使用率" "内存使用"
        print_separator

        for service in "${!MICROSERVICES[@]}"; do
            local port=${MICROSERVICES[$service]}
            local status="${SERVICE_STATUS[$service]:-UNKNOWN}"
            local response_time="${SERVICE_RESPONSE_TIME[$service]:-0}ms"
            local cpu_usage="${SERVICE_CPU_USAGE[$service]:-0%}"
            local memory_usage="${SERVICE_MEMORY_USAGE[$service]:-0B/0%}"

            # 根据状态设置颜色
            local status_color=$GREEN
            case $status in
                "UNHEALTHY"|"CONTAINER_STOPPED"|"CONTAINER_NOT_FOUND") status_color=$RED ;;
                "DEGRADED"|"STARTING") status_color=$YELLOW ;;
                "NO_HEALTH_CHECK"|"UNKNOWN") status_color=$BLUE ;;
            esac

            printf "%-25s %-8s ${status_color}%-12s${NC} %-10s %-15s %-20s\n" \
                "$service" "$port" "$status" "$response_time" "$cpu_usage" "$memory_usage"

            # 快速健康检查
            check_single_service "$service" "$port" > /dev/null 2>&1
        done

        print_separator
        echo -e "监控间隔: 30秒 | 最后更新: $(date '+%H:%M:%S')"
        echo -e "按 ${YELLOW}Ctrl+C${NC} 停止监控"

        sleep 30
    done
}

# 生成健康检查报告
generate_health_report() {
    print_section "📋 生成健康检查报告"

    local report_file="$VERIFICATION_DIR/reports/health-check-report-$(date +%Y%m%d_%H%M%S).html"

    log "INFO" "生成健康检查报告: $report_file"

    # 计算统计数据
    local total_services=${#MICROSERVICES[@]}
    local healthy_services=0
    local unhealthy_services=0
    local degraded_services=0

    for service in "${!MICROSERVICES[@]}"; do
        case "${SERVICE_STATUS[$service]:-UNKNOWN}" in
            "HEALTHY") ((healthy_services++)) ;;
            "UNHEALTHY"|"CONTAINER_STOPPED"|"CONTAINER_NOT_FOUND") ((unhealthy_services++)) ;;
            "DEGRADED"|"STARTING"|"NO_HEALTH_CHECK"|"UNKNOWN") ((degraded_services++)) ;;
        esac
    done

    # 生成HTML报告
    cat > "$report_file" << EOF
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IOE-DREAM 微服务健康检查报告</title>
    <style>
        body { font-family: 'Arial', sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1400px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; border-bottom: 3px solid #007acc; padding-bottom: 20px; margin-bottom: 30px; }
        .title { color: #007acc; font-size: 28px; margin: 0; }
        .subtitle { color: #666; font-size: 16px; margin: 10px 0; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: center; }
        .card.success { background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); }
        .card.error { background: linear-gradient(135deg, #f44336 0%, #da190b 100%); }
        .card.warning { background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%); }
        .card h3 { margin: 0 0 10px 0; font-size: 24px; }
        .card p { margin: 0; font-size: 16px; }
        .section { margin-bottom: 30px; }
        .section h2 { color: #333; border-left: 4px solid #007acc; padding-left: 15px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f8f9fa; font-weight: bold; }
        .status { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
        .status.healthy { background: #d4edda; color: #155724; }
        .status.unhealthy { background: #f8d7da; color: #721c24; }
        .status.degraded { background: #fff3cd; color: #856404; }
        .status.unknown { background: #e2e3e5; color: #383d41; }
        .timestamp { color: #666; font-size: 14px; margin-top: 20px; text-align: right; }
        .chart { margin: 20px 0; text-align: center; }
        .health-meter { width: 100%; height: 30px; background: #e0e0e0; border-radius: 15px; overflow: hidden; position: relative; }
        .health-fill { height: 100%; background: linear-gradient(90deg, #4CAF50, #45a049); transition: width 0.3s ease; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1 class="title">🏥 IOE-DREAM 微服务健康检查报告</h1>
            <p class="subtitle">生产环境健康状态监控 • $(date)</p>
        </div>

        <div class="summary">
            <div class="card">
                <h3>$total_services</h3>
                <p>总服务数</p>
            </div>
            <div class="card success">
                <h3>$healthy_services</h3>
                <p>健康服务</p>
            </div>
            <div class="card error">
                <h3>$unhealthy_services</h3>
                <p>不健康服务</p>
            </div>
            <div class="card warning">
                <h3>$degraded_services</h3>
                <p>降级/未知服务</p>
            </div>
        </div>

        <div class="chart">
            <h3>📊 整体健康度</h3>
            <div class="health-meter">
                <div class="health-fill" style="width: $((healthy_services * 100 / total_services))%">
                    $((healthy_services * 100 / total_services))%
                </div>
            </div>
        </div>

        <div class="section">
            <h2>📋 服务健康详情</h2>
            <table>
                <thead>
                    <tr>
                        <th>服务名称</th>
                        <th>端口</th>
                        <th>状态</th>
                        <th>响应时间</th>
                        <th>CPU使用率</th>
                        <th>内存使用</th>
                        <th>依赖状态</th>
                        <th>健康检查</th>
                    </tr>
                </thead>
                <tbody>
EOF

    # 添加服务详情
    for service in "${!MICROSERVICES[@]}"; do
        local port=${MICROSERVICES[$service]}
        local status="${SERVICE_STATUS[$service]:-UNKNOWN}"
        local response_time="${SERVICE_RESPONSE_TIME[$service]:-N/A}"
        local cpu_usage="${SERVICE_CPU_USAGE[$service]:-N/A}"
        local memory_usage="${SERVICE_MEMORY_USAGE[$service]:-N/A}"
        local dependencies="${SERVICE_DEPENDENCIES[$service]:-N/A}"

        # 状态样式
        local status_class="unknown"
        case $status in
            "HEALTHY") status_class="healthy" ;;
            "UNHEALTHY"|"CONTAINER_STOPPED"|"CONTAINER_NOT_FOUND") status_class="unhealthy" ;;
            "DEGRADED"|"STARTING"|"NO_HEALTH_CHECK") status_class="degraded" ;;
        esac

        # 健康检查链接
        local health_link="<a href=\"http://localhost:$port/actuator/health\" target=\"_blank\">查看</a>"

        cat >> "$report_file" << EOF
                    <tr>
                        <td>$service</td>
                        <td>$port</td>
                        <td><span class="status $status_class">$status</span></td>
                        <td>${response_time}ms</td>
                        <td>$cpu_usage</td>
                        <td>$memory_usage</td>
                        <td>$dependencies</td>
                        <td>$health_link</td>
                    </tr>
EOF
    done

    cat >> "$report_file" << EOF
                </tbody>
            </table>
        </div>

        <div class="section">
            <h2>📈 性能指标分析</h2>
            <table>
                <thead>
                    <tr>
                        <th>指标名称</th>
                        <th>平均值</th>
                        <th>最大值</th>
                        <th>最小值</th>
                        <th>标准差</th>
                    </tr>
                </thead>
                <tbody>
EOF

    # 计算性能指标统计
    local response_times=()
    for service in "${!MICROSERVICES[@]}"; do
        local rt="${SERVICE_RESPONSE_TIME[$service]:-0}"
        response_times+=("${rt%ms}")  # 移除ms单位
    done

    # 计算响应时间统计
    local sum=0
    local count=0
    local max=0
    local min=999999
    for rt in "${response_times[@]}"; do
        if [[ "$rt" =~ ^[0-9]+$ ]]; then
            sum=$((sum + rt))
            ((count++))
            if [ $rt -gt $max ]; then max=$rt; fi
            if [ $rt -lt $min ]; then min=$rt; fi
        fi
    done

    local avg=0
    if [ $count -gt 0 ]; then
        avg=$((sum / count))
    fi

    cat >> "$report_file" << EOF
                    <tr>
                        <td>响应时间 (ms)</td>
                        <td>$avg</td>
                        <td>$max</td>
                        <td>$min</td>
                        <td>-</td>
                    </tr>
                    <tr>
                        <td>服务可用性 (%)</td>
                        <td>$((healthy_services * 100 / total_services))</td>
                        <td>-</td>
                        <td>-</td>
                        <td>-</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="section">
            <h2>🔧 系统资源监控</h2>
            <p><strong>外部系统状态:</strong></p>
            <ul>
                <li>MySQL (3306): $(netstat -tuln 2>/dev/null | grep -q ":3306" && echo "✅ 运行中" || echo "❌ 未运行")</li>
                <li>Redis (6379): $(netstat -tuln 2>/dev/null | grep -q ":6379" && echo "✅ 运行中" || echo "❌ 未运行")</li>
                <li>Nacos (8848): $(netstat -tuln 2>/dev/null | grep -q ":8848" && echo "✅ 运行中" || echo "❌ 未运行")</li>
                <li>Prometheus (9090): $(netstat -tuln 2>/dev/null | grep -q ":9090" && echo "✅ 运行中" || echo "❌ 未运行")</li>
                <li>Grafana (3000): $(netstat -tuln 2>/dev/null | grep -q ":3000" && echo "✅ 运行中" || echo "❌ 未运行")</li>
            </ul>
        </div>

        <div class="timestamp">
            报告生成时间：$(date) <br>
            健康检查工具版本：v1.0.0
        </div>
    </div>
</body>
</html>
EOF

    log "INFO" "健康检查报告已生成: $report_file"
    echo -e "\n${GREEN}✅ 健康检查报告生成完成${NC}"
    echo -e "报告路径: ${BLUE}$report_file${NC}"

    return 0
}

# 检查指定服务的详细信息
check_service_detail() {
    local target_service=$1

    if [ -z "$target_service" ]; then
        log "ERROR" "请指定要检查的服务名称"
        echo "可用的服务:"
        for service in "${!MICROSERVICES[@]}"; do
            echo "  - $service"
        done
        return 1
    fi

    if [ -z "${MICROSERVICES[$target_service]}" ]; then
        log "ERROR" "未知服务: $target_service"
        return 1
    fi

    local port=${MICROSERVICES[$target_service]}
    local container_name="ioedream-${target_service#ioedream-}"

    print_section "🔍 详细检查服务: $target_service"

    echo -e "${CYAN}基本信息:${NC}"
    echo "  服务名称: $target_service"
    echo "  端口: $port"
    echo "  容器名: $container_name"

    echo -e "\n${CYAN}容器状态:${NC}"
    if docker ps -a --format "{{.Names}}" | grep -q "^$container_name$"; then
        echo "  容器存在: ✅"

        local container_status=$(docker inspect --format="{{.State.Status}}" "$container_name" 2>/dev/null || echo "not_found")
        echo "  运行状态: $container_status"

        if [ "$container_status" = "running" ]; then
            local start_time=$(docker inspect --format="{{.State.StartedAt}}" "$container_name" 2>/dev/null)
            echo "  启动时间: $start_time"

            local health_status=$(docker inspect --format="{{.State.Health.Status}}" "$container_name" 2>/dev/null || echo "none")
            echo "  健康状态: $health_status"
        fi
    else
        echo "  容器存在: ❌"
    fi

    echo -e "\n${CYAN}网络连接:${NC}"
    if docker exec "$container_name" netstat -tuln 2>/dev/null; then
        echo "  网络连接检查: ✅"
    else
        echo "  网络连接检查: ❌"
    fi

    echo -e "\n${CYAN}资源使用:${NC}"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" "$container_name" 2>/dev/null || echo "  无法获取资源信息"

    echo -e "\n${CYAN}最近日志 (最后20行):${NC}"
    echo "─────────────────────────────────────────────────────────"
    docker logs --tail 20 "$container_name" 2>/dev/null || echo "  无法获取日志信息"

    echo -e "\n${CYAN}健康检查端点:${NC}"
    local health_url="http://localhost:$port/actuator/health"
    if curl -f -s "$health_url" > /dev/null 2>&1; then
        echo "  HTTP健康检查: ✅"
        curl -s "$health_url" | jq . 2>/dev/null || curl -s "$health_url"
    else
        echo "  HTTP健康检查: ❌"
    fi

    return 0
}

# 主函数
main() {
    local command=${1:-"check"}
    local service_name=${2:-""}

    case $command in
        "check")
            run_comprehensive_health_check
            ;;
        "monitor")
            run_monitoring_mode
            ;;
        "report")
            generate_health_report
            ;;
        "detail")
            check_service_detail "$service_name"
            ;;
        "help"|"--help"|"-h")
            echo "IOE-DREAM 微服务健康检查工具"
            echo ""
            echo "使用方法:"
            echo "  $0 [命令] [服务名称]"
            echo ""
            echo "命令:"
            echo "  check   - 执行一次全面健康检查 (默认)"
            echo "  monitor - 实时监控模式"
            echo "  report  - 生成健康检查报告"
            echo "  detail  - 检查指定服务的详细信息"
            echo "  help    - 显示帮助信息"
            echo ""
            echo "示例:"
            echo "  $0 check                    # 检查所有服务"
            echo "  $0 detail ioedream-auth     # 详细检查认证服务"
            echo "  $0 monitor                  # 启动实时监控"
            echo "  $0 report                   # 生成HTML报告"
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