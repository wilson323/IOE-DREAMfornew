#!/bin/bash

# IOE-DREAM 微服务健康检查脚本
# 定期检查所有服务状态并生成报告

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

# 检查单个服务状态
check_service() {
    local service_name=$1
    local display_name=$2
    local port=$3

    if [ -z "$port" ]; then
        # 使用docker-compose检查
        if docker-compose ps 2>/dev/null | grep -q "$service_name.*Up"; then
            log_info "$display_name: 运行正常"
            return 0
        else
            log_error "$display_name: 服务未运行"
            return 1
        fi
    else
        # 使用端口检查
        if nc -z localhost "$port" 2>/dev/null; then
            log_warn "$display_name: 端口 $port 可访问但可能未完全启动"
            return 2
        else
            log_error "$display_name: 端口 $port 不可访问"
            return 1
        fi
    fi
}

# 检查Docker容器状态
check_docker_containers() {
    log_info "检查Docker容器状态..."

    local running_count=0
    local total_count=0

    for service in mysql-master redis-master nacos rabbitmq elasticsearch \
                    ioedream-auth-service ioedream-identity-service ioedream-device-service \
                    ioedream-area-service smart-gateway ioedream-access-service \
                    ioedream-consume-service ioedream-attendance-service ioedream-video-service \
                    ioedream-visitor-service ioedream-notification-service ioedream-file-service \
                    ioedream-report-service prometheus grafana zipkin jaeger minio; do

        total_count=$((total_count + 1))
        if docker ps --format "table {{.Names}}" | grep -q "$service"; then
            running_count=$((running_count + 1))
            echo "✅ $service"
        else
            echo "❌ $service"
        fi
    done

    log_info "容器状态: $running_count/$total_count 运行中"

    if [ $running_count -eq $total_count ]; then
        return 0
    else
        return 1
    fi
}

# 检查服务响应时间
check_response_time() {
    local url=$1
    local service_name=$2
    local timeout=${3:-10}

    if ! command -v curl &> /dev/null; then
        log_warn "curl 未安装，跳过响应时间检查"
        return 0
    fi

    local start_time=$(date +%s.%N)
    local status_code=$(curl -s -o /dev/null -w "%{http_code}" --max-time "$timeout" "$url" || echo "000")
    local end_time=$(date +%s.%N)
    local response_time=$(echo "$end_time - $start_time" | bc)

    if [ "$status_code" = "200" ]; then
        log_info "$service_name 响应时间: ${response_time}s"
        return 0
    else
        log_error "$service_name 响应异常 (状态码: $status_code)"
        return 1
    fi
}

# 检查数据库连接
check_database_connection() {
    log_info "检查数据库连接..."

    # 检查MySQL
    if docker exec ioedream-mysql-admin mysqladmin ping -h localhost --silent 2>/dev/null; then
        log_info "MySQL主库连接正常"
    else
        log_error "MySQL主库连接失败"
    fi

    # 检查Redis
    if docker exec redis-master redis-cli ping 2>/dev/null | grep -q "PONG"; then
        log_info "Redis主库连接正常"
    else
        log_error "Redis主库连接失败"
    fi
}

# 检查服务注册状态
check_service_registration() {
    log_info "检查服务注册状态..."

    if ! command -v curl &> /dev/null; then
        log_warn "curl 未安装，跳过服务注册检查"
        return 0
    fi

    # 检查Nacos中的服务注册
    local nacos_url="http://localhost:8848/nacos/v1/ns/instance/list"

    local services=("ioedream-auth-service" "ioedream-identity-service" "ioedream-device-service"
                 "ioedream-area-service" "smart-gateway" "ioedream-access-service"
                 "ioedream-consume-service" "ioedream-attendance-service" "ioedream-video-service"
                 "ioedream-visitor-service" "ioedream-notification-service" "ioedream-file-service"
                 "ioedream-report-service")

    for service in "${services[@]}"; do
        if curl -s "$nacos_url?serviceName=$service" | grep -q "instanceId"; then
            log_info "$service 已注册到Nacos"
        else
            log_warn "$service 未注册到Nacos"
        fi
    done
}

# 检查系统资源使用情况
check_system_resources() {
    log_info "检查系统资源使用情况..."

    # CPU使用率
    if command -v top &> /dev/null; then
        local cpu_usage=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | tail -n1)
        log_info "CPU使用率: $cpu_usage"
    fi

    # 内存使用率
    if command -v free &> /dev/null; then
        local mem_usage=$(free | grep Mem | awk '{printf "%.1f", $3/$2 * 100.0}')
        log_info "内存使用率: ${mem_usage}%"
    fi

    # 磁盘使用率
    if command -v df &> /dev/null; then
        local disk_usage=$(df -h / | tail -1 | awk '{print $5}')
        log_info "磁盘使用率: $disk_usage"
    fi

    # Docker容器资源使用
    if command -v docker &> /dev/null; then
        log_info "Docker容器资源使用:"
        docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}"
    fi
}

# 生成健康检查报告
generate_health_report() {
    local report_file="./health-report-$(date +%Y%m%d-%H%M%S).html"

    log_info "生成健康检查报告: $report_file"

    cat > "$report_file" << EOF
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IOE-DREAM 微服务健康检查报告</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; color: #333; border-bottom: 2px solid #007bff; padding-bottom: 20px; margin-bottom: 30px; }
        .service-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .service-card { padding: 15px; border-radius: 5px; border-left: 4px solid #007bff; }
        .service-healthy { border-left-color: #28a745; background-color: #d4edda; }
        .service-warning { border-left-color: #ffc107; background-color: #fff3cd; }
        .service-error { border-left-color: #dc3545; background-color: #f8d7da; }
        .service-name { font-weight: bold; margin-bottom: 10px; }
        .service-status { margin-bottom: 5px; }
        .timestamp { text-align: right; color: #666; margin-top: 20px; font-size: 0.9em; }
        .summary { background: #e9ecef; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>IOE-DREAM 微服务健康检查报告</h1>
            <p>生成时间: $(date)</p>
        </div>

        <div class="summary">
            <h2>检查概要</h2>
            <p>此报告显示IOE-DREAM微服务集群的健康状态，包括服务可用性、响应时间和资源使用情况。</p>
        </div>

        <div class="service-grid">
EOF

    # 添加服务状态到报告
    local services=(
        "MySQL数据库:mysql-master:3306"
        "Redis缓存:redis-master:6379"
        "Nacos注册中心:nacos:8848"
        "智能网关:smart-gateway:8080"
        "认证服务:ioedream-auth-service:8081"
        "身份服务:ioedream-identity-service:8082"
        "设备服务:ioedream-device-service:8083"
        "区域服务:ioedream-area-service:8084"
        "门禁服务:ioedream-access-service:8085"
        "消费服务:ioedream-consume-service:8086"
        "考勤服务:ioedream-attendance-service:8087"
        "视频服务:ioedream-video-service:8088"
        "访客服务:ioedream-visitor-service:8089"
        "通知服务:ioedream-notification-service:8090"
        "文件服务:ioedream-file-service:8091"
        "报表服务:ioedream-report-service:8092"
    )

    local healthy_count=0
    local total_count=${#services[@]}

    for service_info in "${services[@]}"; do
        IFS=':' read -r display_name service_name port <<< "$service_info"

        if nc -z localhost "$port" 2>/dev/null; then
            status_class="service-healthy"
            status_text="✅ 健康"
            healthy_count=$((healthy_count + 1))
        else
            status_class="service-error"
            status_text="❌ 不可访问"
        fi

        cat >> "$report_file" << EOF
            <div class="service-card $status_class">
                <div class="service-name">$display_name</div>
                <div class="service-status">状态: $status_text</div>
                <div class="service-status">端口: $port</div>
                <div class="service-status">服务名: $service_name</div>
            </div>
EOF
    done

    cat >> "$report_file" << EOF
        </div>

        <div class="summary">
            <h2>统计信息</h2>
            <p>总服务数: $total_count</p>
            <p>健康服务数: $healthy_count</p>
            <p>健康率: $(( healthy_count * 100 / total_count ))%</p>
        </div>

        <div class="timestamp">
            报告生成时间: $(date)
        </div>
    </div>
</body>
</html>
EOF

    log_info "健康检查报告已生成: $report_file"
}

# 发送告警通知
send_alert() {
    local message="$1"
    local severity="$2"

    # 这里可以集成Slack、微信、邮件等告警方式
    # 示例：发送到Webhook
    # curl -X POST -H 'Content-Type: application/json' \
    #      -d "{\"text\":\"$message\",\"severity\":\"$severity\"}" \
    #      https://hooks.slack.com/services/YOUR/WEBHOOK/URL

    log_warn "告警: $message (严重程度: $severity)"
}

# 主检查函数
main_health_check() {
    log_info "开始IOE-DREAM微服务健康检查..."

    local error_count=0

    # 检查容器状态
    check_docker_containers || error_count=$((error_count + 1))

    # 检查关键服务端口
    check_service "MySQL数据库" "MySQL" "3306" || error_count=$((error_count + 1))
    check_service "Redis缓存" "Redis" "6379" || error_count=$((error_count + 1))
    check_service "Nacos" "Nacos" "8848" || error_count=$((error_count + 1))
    check_service "智能网关" "Gateway" "8080" || error_count=$((error_count + 1))

    # 检查响应时间
    check_response_time "http://localhost:8080/actuator/health" "智能网关" || error_count=$((error_count + 1))
    check_response_time "http://localhost:8848/nacos" "Nacos" || error_count=$((error_count + 1))

    # 检查数据库连接
    check_database_connection

    # 检查服务注册
    check_service_registration

    # 检查系统资源
    check_system_resources

    # 生成报告
    generate_health_report

    # 发送告警
    if [ $error_count -gt 0 ]; then
        send_alert "发现 $error_count 个健康检查问题" "ERROR"
        return 1
    fi

    log_info "健康检查完成，所有服务正常运行"
    return 0
}

# 快速检查模式
quick_check() {
    log_info "执行快速健康检查..."

    local critical_services=(
        "nacos:Nacos注册中心:8848"
        "smart-gateway:智能网关:8080"
        "ioedream-auth-service:认证服务:8081"
    )

    local issues=0

    for service_info in "${critical_services[@]}; do
        IFS=':' read -r service_name display_name port <<< "$service_info"
        if ! nc -z localhost "$port" 2>/dev/null; then
            log_error "关键服务不可访问: $display_name"
            issues=$((issues + 1))
        else
            log_info "$display_name: 运行正常"
        fi
    done

    if [ $issues -gt 0 ]; then
        log_error "快速检查发现问题: $issues 个关键服务异常"
        return 1
    fi

    log_info "快速检查通过，所有关键服务正常"
    return 0
}

# 显示帮助信息
show_help() {
    echo "IOE-DREAM 微服务健康检查脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [选项]"
    echo ""
    echo "选项:"
    echo "  --quick     快速检查关键服务"
    echo "  --report    只生成报告"
    echo "  --services  检查指定服务 (逗号分隔)"
    echo "  --help      显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0                    # 完整健康检查"
    echo "  $0 --quick           # 快速检查"
    echo "  $0 --report           # 只生成报告"
    echo "  $0 --services nacos,gateway  # 检查指定服务"
}

# 解析命令行参数
case "$1" in
    --quick)
        quick_check
        ;;
    --report)
        generate_health_report
        ;;
    --services)
        IFS=',' read -ra service_list <<< "$2"
        for service in "${service_list[@]}"; do
            check_service "$service" "$service"
        done
        ;;
    --help|"-h"|"")
        show_help
        ;;
    "")
        main_health_check
        ;;
    *)
        log_error "未知参数: $1"
        show_help
        exit 1
        ;;
esac