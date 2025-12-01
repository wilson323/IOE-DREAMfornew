#!/bin/bash

# ===================================================================
# IOE-DREAM 系统健康检查脚本
# 版本: v2.0.0
# 更新时间: 2025-11-29
# ===================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
MONITORING_DIR="$PROJECT_ROOT/monitoring"

# 微服务端口配置
declare -A SERVICES=(
    ["gateway"]="8888"
    ["auth-service"]="8881"
    ["identity-service"]="8882"
    ["device-service"]="8883"
    ["access-service"]="8884"
    ["consume-service"]="8885"
    ["attendance-service"]="8886"
    ["video-service"]="8887"
    ["visitor-service"]="8888"
    ["notification-service"]="8889"
    ["file-service"]="8890"
    ["report-service"]="8891"
    ["monitor-service"]="8892"
    ["system-service"]="8893"
    ["hr-service"]="8894"
    ["oa-service"]="8895"
    ["smart-service"]="8896"
)

# 监控组件配置
declare -A MONITORING_COMPONENTS=(
    ["prometheus"]="9090"
    ["grafana"]="3000"
    ["alertmanager"]="9093"
    ["elasticsearch"]="9200"
    ["kibana"]="5601"
    ["node-exporter"]="9100"
    ["cadvisor"]="8080"
)

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

# 检查服务是否运行
check_service_status() {
    local service_name=$1
    local port=$2
    local host=${3:-localhost}

    if nc -z "$host" "$port" 2>/dev/null; then
        return 0
    else
        return 1
    fi
}

# 检查HTTP服务响应
check_http_response() {
    local url=$1
    local expected_status=${2:-200}

    local status_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")

    if [ "$status_code" = "$expected_status" ]; then
        return 0
    else
        return 1
    fi
}

# 检查微服务健康状态
check_microservices() {
    log_info "检查微服务健康状态..."

    local healthy_count=0
    local total_count=${#SERVICES[@]}

    echo ""
    printf "%-25s %-10s %-15s %-20s\n" "服务名称" "端口" "状态" "响应时间"
    printf "%-25s %-10s %-15s %-20s\n" "---------------------" "----" "---------------" "--------------------"

    for service in "${!SERVICES[@]}"; do
        local port=${SERVICES[$service]}
        local url="http://localhost:$port/actuator/health"

        if check_service_status "$service" "$port"; then
            local start_time=$(date +%s%N)
            if check_http_response "$url"; then
                local end_time=$(date +%s%N)
                local response_time=$(( (end_time - start_time) / 1000000 ))
                printf "%-25s %-10s %-15s %-20s ms\n" "$service" "$port" "✓ 健康" "$response_time"
                ((healthy_count++))
            else
                printf "%-25s %-10s %-15s %-20s\n" "$service" "$port" "⚠ 异常" "无响应"
            fi
        else
            printf "%-25s %-10s %-15s %-20s\n" "$service" "$port" "✗ 停止" "连接拒绝"
        fi
    done

    echo ""
    local health_percentage=$((healthy_count * 100 / total_count))
    if [ $health_percentage -ge 80 ]; then
        log_success "微服务健康状态: $healthy_count/$total_count ($health_percentage%) - 良好"
    elif [ $health_percentage -ge 60 ]; then
        log_warning "微服务健康状态: $healthy_count/$total_count ($health_percentage%) - 一般"
    else
        log_error "微服务健康状态: $healthy_count/$total_count ($health_percentage%) - 异常"
    fi

    return $((total_count - healthy_count))
}

# 检查监控组件状态
check_monitoring_components() {
    log_info "检查监控组件状态..."

    local healthy_count=0
    local total_count=${#MONITORING_COMPONENTS[@]}

    echo ""
    printf "%-20s %-10s %-15s %-30s\n" "组件名称" "端口" "状态" "访问地址"
    printf "%-20s %-10s %-15s %-30s\n" "--------------------" "----" "---------------" "------------------------------"

    for component in "${!MONITORING_COMPONENTS[@]}"; do
        local port=${MONITORING_COMPONENTS[$component]}
        local access_url="http://localhost:$port"

        case $component in
            "prometheus")
                health_url="$access_url/api/v1/status/config"
                ;;
            "grafana")
                health_url="$access_url/api/health"
                ;;
            "elasticsearch")
                health_url="$access_url/_cluster/health"
                ;;
            "kibana")
                health_url="$access_url/api/status"
                ;;
            *)
                health_url="$access_url"
                ;;
        esac

        if check_service_status "$component" "$port"; then
            if check_http_response "$health_url"; then
                printf "%-20s %-10s %-15s %-30s\n" "$component" "$port" "✓ 运行" "$access_url"
                ((healthy_count++))
            else
                printf "%-20s %-10s %-15s %-30s\n" "$component" "$port" "⚠ 异常" "$access_url"
            fi
        else
            printf "%-20s %-10s %-15s %-30s\n" "$component" "$port" "✗ 停止" "$access_url"
        fi
    done

    echo ""
    local health_percentage=$((healthy_count * 100 / total_count))
    if [ $health_percentage -ge 80 ]; then
        log_success "监控组件健康状态: $healthy_count/$total_count ($health_percentage%) - 良好"
    elif [ $health_percentage -ge 60 ]; then
        log_warning "监控组件健康状态: $healthy_count/$total_count ($health_percentage%) - 一般"
    else
        log_error "监控组件健康状态: $healthy_count/$total_count ($health_percentage%) - 异常"
    fi
}

# 检查系统资源使用情况
check_system_resources() {
    log_info "检查系统资源使用情况..."

    echo ""
    printf "%-20s %-15s %-15s %-10s\n" "资源类型" "使用情况" "总量" "使用率"
    printf "%-20s %-15s %-15s %-10s\n" "--------------------" "---------------" "---------------" "----------"

    # CPU使用率
    local cpu_usage=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | awk -F'%' '{print $1}')
    printf "%-20s %-15s %-15s %-10s\n" "CPU" "${cpu_usage}%" "100%" "$cpu_usage"

    # 内存使用情况
    local mem_info=$(free -m | awk 'NR==2{printf "%.1fGB %.1fGB %.1f%%", $3/1024, $2/1024, $3*100/$2}')
    read mem_used mem_total mem_percent <<< "$mem_info"
    printf "%-20s %-15s %-15s %-10s\n" "内存" "$mem_used" "$mem_total" "$mem_percent"

    # 磁盘使用情况
    df -h | awk 'NR>1 && $1!~/tmpfs|udev/ {
        usage = substr($5, 1, length($5)-1)
        printf "%-20s %-15s %-15s %-10s\n", $1, $3, $2, usage"%"
    }'

    # 检查资源告警阈值
    echo ""
    if (( $(echo "$cpu_usage > 80" | bc -l) )); then
        log_warning "CPU使用率过高: ${cpu_usage}%"
    fi

    if (( $(echo "$mem_percent > 85" | bc -l) )); then
        log_warning "内存使用率过高: ${mem_percent}%"
    fi

    # 检查磁盘空间
    df -h | awk 'NR>1 && $1!~/tmpfs|udev/ {
        usage = substr($5, 1, length($5)-1)
        if (usage > 90) {
            print "'$YELLOW'[WARNING]'" $1 "磁盘空间不足: " usage "%"
        }
    }'
}

# 检查数据库连接
check_databases() {
    log_info "检查数据库连接状态..."

    echo ""
    printf "%-15s %-15s %-15s\n" "数据库类型" "连接状态" "响应时间"
    printf "%-15s %-15s %-15s\n" "---------------" "---------------" "---------------"

    # MySQL连接检查
    if command -v mysql &> /dev/null; then
        local start_time=$(date +%s%N)
        if mysql -h localhost -u root -p'password' -e "SELECT 1" &>/dev/null; then
            local end_time=$(date +%s%N)
            local response_time=$(( (end_time - start_time) / 1000000 ))
            printf "%-15s %-15s %-15s ms\n" "MySQL" "✓ 连接正常" "$response_time"
        else
            printf "%-15s %-15s %-15s\n" "MySQL" "✗ 连接失败" "-"
        fi
    else
        printf "%-15s %-15s %-15s\n" "MySQL" "- 未安装" "-"
    fi

    # Redis连接检查
    if command -v redis-cli &> /dev/null; then
        local start_time=$(date +%s%N)
        if redis-cli ping &>/dev/null; then
            local end_time=$(date +%s%N)
            local response_time=$(( (end_time - start_time) / 1000000 ))
            printf "%-15s %-15s %-15s ms\n" "Redis" "✓ 连接正常" "$response_time"
        else
            printf "%-15s %-15s %-15s\n" "Redis" "✗ 连接失败" "-"
        fi
    else
        printf "%-15s %-15s %-15s\n" "Redis" "- 未安装" "-"
    fi
}

# 检查网络连接
check_network_connectivity() {
    log_info "检查网络连接状态..."

    echo ""
    printf "%-25s %-15s\n" "网络目标" "连接状态"
    printf "%-25s %-15s\n" "-------------------------" "---------------"

    # 检查外网连接
    if ping -c 1 8.8.8.8 &>/dev/null; then
        printf "%-25s %-15s\n" "外网(8.8.8.8)" "✓ 连接正常"
    else
        printf "%-25s %-15s\n" "外网(8.8.8.8)" "✗ 连接失败"
    fi

    # 检查DNS解析
    if nslookup www.baidu.com &>/dev/null; then
        printf "%-25s %-15s\n" "DNS(www.baidu.com)" "✓ 解析正常"
    else
        printf "%-25s %-15s\n" "DNS(www.baidu.com)" "✗ 解析失败"
    fi
}

# 生成健康检查报告
generate_health_report() {
    local report_file="$MONITORING_DIR/logs/health-report-$(date +%Y%m%d-%H%M%S).log"

    log_info "生成健康检查报告..."

    {
        echo "=================================================================="
        echo "IOE-DREAM 系统健康检查报告"
        echo "生成时间: $(date '+%Y-%m-%d %H:%M:%S')"
        echo "检查主机: $(hostname)"
        echo "=================================================================="
        echo ""

        echo "1. 微服务健康状态"
        check_microservices
        echo ""

        echo "2. 监控组件状态"
        check_monitoring_components
        echo ""

        echo "3. 系统资源使用"
        check_system_resources
        echo ""

        echo "4. 数据库连接状态"
        check_databases
        echo ""

        echo "5. 网络连接状态"
        check_network_connectivity
        echo ""

        echo "=================================================================="
        echo "健康检查完成"
        echo "=================================================================="

    } | tee "$report_file"

    log_success "健康检查报告已生成: $report_file"
}

# 显示帮助信息
show_help() {
    echo "IOE-DREAM 系统健康检查脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help           显示帮助信息"
    echo "  -s, --services       只检查微服务状态"
    echo "  -m, --monitoring     只检查监控组件状态"
    echo "  -r, --resources      只检查系统资源"
    echo "  -d, --databases      只检查数据库连接"
    echo "  -n, --network        只检查网络连接"
    echo "  -f, --full           执行完整健康检查并生成报告"
    echo ""
    echo "示例:"
    echo "  $0 -f                执行完整健康检查"
    echo "  $0 -s                只检查微服务"
    echo "  $0 -m                只检查监控组件"
}

# 主函数
main() {
    case "${1:-full}" in
        -h|--help)
            show_help
            ;;
        -s|--services)
            check_microservices
            ;;
        -m|--monitoring)
            check_monitoring_components
            ;;
        -r|--resources)
            check_system_resources
            ;;
        -d|--databases)
            check_databases
            ;;
        -n|--network)
            check_network_connectivity
            ;;
        -f|--full|full)
            generate_health_report
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"