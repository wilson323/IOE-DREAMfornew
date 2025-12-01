#!/bin/bash

# ===================================================================
# IOE-DREAM 微服务监控系统验证脚本
# 功能：验证Prometheus、Grafana、日志收集、告警系统等监控组件
# 确保监控系统正常运行，数据收集完整，告警规则有效
#
# 使用方法:
#   ./monitoring-validation.sh [check|prometheus|grafana|logs|alerts|report]
#
# 参数说明:
#   check     - 执行全面监控验证 (默认)
#   prometheus- 验证Prometheus配置和数据收集
#   grafana   - 验证Grafana仪表板和可视化
#   logs      - 验证日志收集和查询功能
#   alerts    - 验证告警规则和通知
#   report    - 生成监控验证报告
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
REPORT_DIR="$VERIFICATION_DIR/reports"

# 监控系统URL配置
PROMETHEUS_URL="http://localhost:9090"
GRAFANA_URL="http://localhost:3000"
NACOS_URL="http://localhost:8848"

# 验证结果统计
declare -A MONITORING_STATUS=()
declare -A METRIC_COUNTS=()
declare -A ALERT_STATUS=()

# 日志记录函数
log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "[$timestamp] [$level] $message" | tee -a "$LOG_DIR/monitoring-validation.log"

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

# 检查服务可访问性
check_service_access() {
    local service_name=$1
    local service_url=$2
    local health_endpoint=$3

    log "DEBUG" "检查 $service_name 可访问性: $service_url$health_endpoint"

    local full_url="$service_url$health_endpoint"
    local response_code=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 10 "$full_url" 2>/dev/null || echo "000")

    if [ "$response_code" = "200" ]; then
        log "INFO" "$service_name 可访问: $full_url"
        MONITORING_STATUS["$service_name"]="ACCESSIBLE"
        return 0
    else
        log "ERROR" "$service_name 不可访问: $full_url (HTTP $response_code)"
        MONITORING_STATUS["$service_name"]="INACCESSIBLE"
        return 1
    fi
}

# 检查Prometheus
check_prometheus() {
    print_section "📊 验证Prometheus监控系统"

    echo -e "${BLUE}1. 检查Prometheus服务状态${NC}"

    # 检查基础健康状态
    if check_service_access "Prometheus" "$PROMETHEUS_URL" "/-/healthy"; then
        log "INFO" "Prometheus健康检查通过"
    else
        log "ERROR" "Prometheus健康检查失败"
        return 1
    fi

    # 检查Prometheus配置
    echo -e "\n${BLUE}2. 检查Prometheus配置${NC}"

    local config_response=$(curl -s "$PROMETHEUS_URL/api/v1/status/config" 2>/dev/null || echo "")
    if echo "$config_response" | grep -q '"status":"success"'; then
        log "INFO" "Prometheus配置加载成功"
        MONITORING_STATUS["Prometheus-Config"]="VALID"
    else
        log "ERROR" "Prometheus配置加载失败"
        MONITORING_STATUS["Prometheus-Config"]="INVALID"
    fi

    # 检查目标服务发现
    echo -e "\n${BLUE}3. 检查服务发现状态${NC}"

    local targets_response=$(curl -s "$PROMETHEUS_URL/api/v1/targets" 2>/dev/null || echo "")
    local total_targets=0
    local up_targets=0

    if echo "$targets_response" | grep -q '"status":"success"'; then
        # 解析目标状态
        total_targets=$(echo "$targets_response" | grep -o '"health":"[^"]*"' | wc -l)
        up_targets=$(echo "$targets_response" | grep -o '"health":"up"' | wc -l)

        log "INFO" "Prometheus目标发现: $up_targets/$total_targets 个服务在线"
        METRIC_COUNTS["Targets-Total"]="$total_targets"
        METRIC_COUNTS["Targets-Up"]="$up_targets"

        if [ $up_targets -gt 0 ]; then
            MONITORING_STATUS["Prometheus-Targets"]="ACTIVE"
        else
            MONITORING_STATUS["Prometheus-Targets"]="NO_TARGETS"
        fi
    else
        log "ERROR" "无法获取Prometheus目标状态"
        MONITORING_STATUS["Prometheus-Targets"]="ERROR"
    fi

    # 检查指标数据收集
    echo -e "\n${BLUE}4. 检查指标数据收集${NC}"

    local metrics=(
        "up"
        "http_requests_total"
        "jvm_memory_used_bytes"
        "system_cpu_usage"
        "process_cpu_seconds_total"
    )

    for metric in "${metrics[@]}"; do
        local metric_response=$(curl -s "$PROMETHEUS_URL/api/v1/query?query=$metric" 2>/dev/null || echo "")

        if echo "$metric_response" | grep -q '"status":"success"'; then
            local data_points=$(echo "$metric_response" | grep -o '"result":\[' | wc -l)
            log "INFO" "指标 $metric: $data_points 个数据点"
            METRIC_COUNTS["Metric-$metric"]="$data_points"
        else
            log "WARN" "指标 $metric 无数据"
            METRIC_COUNTS["Metric-$metric"]="0"
        fi
    done

    # 检查Prometheus规则
    echo -e "\n${BLUE}5. 检查告警规则${NC}"

    local rules_response=$(curl -s "$PROMETHEUS_URL/api/v1/rules" 2>/dev/null || echo "")
    if echo "$rules_response" | grep -q '"status":"success"'; then
        local rules_count=$(echo "$rules_response" | grep -o '"name":"[^"]*"' | wc -l)
        log "INFO" "Prometheus告警规则: $rules_count 条"
        METRIC_COUNTS["Rules-Total"]="$rules_count"
        MONITORING_STATUS["Prometheus-Rules"]="LOADED"
    else
        log "ERROR" "无法获取Prometheus告警规则"
        MONITORING_STATUS["Prometheus-Rules"]="ERROR"
    fi

    # 检查当前活跃告警
    echo -e "\n${BLUE}6. 检查当前告警${NC}"

    local alerts_response=$(curl -s "$PROMETHEUS_URL/api/v1/alerts" 2>/dev/null || echo "")
    if echo "$alerts_response" | grep -q '"status":"success"'; then
        local active_alerts=$(echo "$alerts_response" | grep -o '"state":"firing"' | wc -l)
        log "INFO" "当前活跃告警: $active_alerts 个"
        METRIC_COUNTS["Alerts-Active"]="$active_alerts"

        if [ $active_alerts -gt 0 ]; then
            log "WARN" "存在活跃告警，需要关注"
            MONITORING_STATUS["Prometheus-Alerts"]="ACTIVE"
        else
            log "INFO" "无活跃告警"
            MONITORING_STATUS["Prometheus-Alerts"]="CLEAR"
        fi
    else
        log "ERROR" "无法获取告警状态"
        MONITORING_STATUS["Prometheus-Alerts"]="ERROR"
    fi

    return 0
}

# 检查Grafana
check_grafana() {
    print_section "📈 验证Grafana可视化系统"

    echo -e "${BLUE}1. 检查Grafana服务状态${NC}"

    # 检查Grafana健康状态
    if check_service_access "Grafana" "$GRAFANA_URL" "/api/health"; then
        log "INFO" "Grafana健康检查通过"
    else
        log "ERROR" "Grafana健康检查失败"
        return 1
    fi

    # 检查Grafana认证
    echo -e "\n${BLUE}2. 检查Grafana认证${NC}"

    local auth_response=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d '{"user":"admin","password":"admin123"}' \
        "$GRAFANA_URL/api/login" 2>/dev/null || echo "")

    local auth_token=$(echo "$auth_response" | grep -o '"[a-zA-Z0-9_.-]*"' | head -1 | tr -d '"')

    if [ -n "$auth_token" ]; then
        log "INFO" "Grafana认证成功"
        MONITORING_STATUS["Grafana-Auth"]="SUCCESS"
        GRAFANA_TOKEN="$auth_token"
    else
        log "ERROR" "Grafana认证失败"
        MONITORING_STATUS["Grafana-Auth"]="FAILED"
        return 1
    fi

    # 检查数据源配置
    echo -e "\n${BLUE}3. 检查数据源配置${NC}"

    local datasources_response=$(curl -s \
        -H "Authorization: Bearer $GRAFANA_TOKEN" \
        "$GRAFANA_URL/api/datasources" 2>/dev/null || echo "")

    if echo "$datasources_response" | grep -q '"name":"Prometheus"'; then
        local prometheus_ds_count=$(echo "$datasources_response" | grep -o '"name":"Prometheus"' | wc -l)
        log "INFO" "Prometheus数据源: $prometheus_ds_count 个"
        METRIC_COUNTS["Grafana-Datasources"]="$prometheus_ds_count"
        MONITORING_STATUS["Grafana-Datasources"]="CONFIGURED"
    else
        log "ERROR" "未找到Prometheus数据源"
        MONITORING_STATUS["Grafana-Datasources"]="MISSING"
    fi

    # 检查仪表板
    echo -e "\n${BLUE}4. 检查仪表板${NC}"

    local dashboards_response=$(curl -s \
        -H "Authorization: Bearer $GRAFANA_TOKEN" \
        "$GRAFANA_URL/api/search" 2>/dev/null || echo "")

    if echo "$dashboards_response" | grep -q '"title"'; then
        local dashboard_count=$(echo "$dashboards_response" | grep -o '"title":"[^"]*"' | wc -l)
        log "INFO" "Grafana仪表板: $dashboard_count 个"
        METRIC_COUNTS["Grafana-Dashboards"]="$dashboard_count"
        MONITORING_STATUS["Grafana-Dashboards"]="AVAILABLE"

        # 列出主要仪表板
        echo "主要仪表板:"
        echo "$dashboards_response" | grep -o '"title":"[^"]*"' | head -5 | sed 's/"title":"/  - /g; s/"//g'
    else
        log "WARN" "未找到仪表板"
        MONITORING_STATUS["Grafana-Dashboards"]="EMPTY"
    fi

    # 检查用户和组织
    echo -e "\n${BLUE}5. 检查用户和组织${NC}"

    local users_response=$(curl -s \
        -H "Authorization: Bearer $GRAFANA_TOKEN" \
        "$GRAFANA_URL/api/users" 2>/dev/null || echo "")

    if echo "$users_response" | grep -q '"id"'; then
        local user_count=$(echo "$users_response" | grep -o '"id":[0-9]*' | wc -l)
        log "INFO" "Grafana用户: $user_count 个"
        METRIC_COUNTS["Grafana-Users"]="$user_count"
        MONITORING_STATUS["Grafana-Users"]="ACTIVE"
    else
        log "WARN" "无法获取用户信息"
        MONITORING_STATUS["Grafana-Users"]="ERROR"
    fi

    return 0
}

# 检查日志收集系统
check_log_collection() {
    print_section "📝 验证日志收集系统"

    echo -e "${BLUE}1. 检查Docker日志收集${NC}"

    # 检查微服务容器日志
    local services=(
        "ioedream-gateway"
        "ioedream-auth"
        "ioedream-identity"
        "ioedream-device"
        "ioedream-access"
        "ioedream-consume"
        "ioedream-attendance"
        "ioedream-video"
        "ioedream-oa"
        "ioedream-system"
    )

    local logging_containers=0
    local total_log_entries=0

    for service in "${services[@]}"; do
        if docker ps --format "{{.Names}}" | grep -q "$service"; then
            local log_entries=$(docker logs --since=1h "$service" 2>/dev/null | wc -l)
            ((logging_containers++))
            ((total_log_entries += log_entries))

            log "DEBUG" "$service 日志条数: $log_entries"
        fi
    done

    log "INFO" "日志收集容器: $logging_containers/${#services[@]}"
    log "INFO" "最近1小时日志条数: $total_log_entries"

    METRIC_COUNTS["Log-Containers"]="$logging_containers"
    METRIC_COUNTS["Log-Entries"]="$total_log_entries"

    if [ $logging_containers -gt 0 ]; then
        MONITORING_STATUS["Log-Collection"]="ACTIVE"
    else
        MONITORING_STATUS["Log-Collection"]="INACTIVE"
    fi

    # 检查日志错误率
    echo -e "\n${BLUE}2. 检查日志错误率${NC}"

    local error_count=0
    local warning_count=0

    for service in "${services[@]}"; do
        if docker ps --format "{{.Names}}" | grep -q "$service"; then
            local service_errors=$(docker logs --since=1h "$service" 2>&1 | grep -i -c "error\|exception\|failed" || echo "0")
            local service_warnings=$(docker logs --since=1h "$service" 2>&1 | grep -i -c "warn\|warning" || echo "0")

            ((error_count += service_errors))
            ((warning_count += service_warnings))

            if [ $service_errors -gt 5 ]; then
                log "WARN" "$service 错误日志较多: $service_errors"
            fi
        fi
    done

    log "INFO" "最近1小时错误日志: $error_count"
    log "INFO" "最近1小时警告日志: $warning_count"

    METRIC_COUNTS["Log-Errors"]="$error_count"
    METRIC_COUNTS["Log-Warnings"]="$warning_count"

    # 检查日志轮转和存储
    echo -e "\n${BLUE}3. 检查日志存储状态${NC}"

    local log_size=$(du -sh "$LOG_DIR" 2>/dev/null | cut -f1 || echo "0")
    log "INFO" "日志目录大小: $log_size"

    METRIC_COUNTS["Log-Storage"]="$log_size"

    return 0
}

# 检查告警系统
check_alert_system() {
    print_section "🚨 验证告警系统"

    echo -e "${BLUE}1. 检查Prometheus告警规则${NC}"

    local rules_response=$(curl -s "$PROMETHEUS_URL/api/v1/rules" 2>/dev/null || echo "")

    if echo "$rules_response" | grep -q '"status":"success"'; then
        local alerting_rules=$(echo "$rules_response" | grep -o '"type":"alerting"' | wc -l)
        log "INFO" "告警规则总数: $alerting_rules"
        METRIC_COUNTS["Alerting-Rules"]="$alerting_rules"

        # 检查关键告警规则
        local critical_rules=(
            "InstanceDown"
            "HighCPUUsage"
            "HighMemoryUsage"
            "ServiceDown"
            "DiskSpaceAlert"
        )

        local existing_rules=0
        for rule in "${critical_rules[@]}"; do
            if echo "$rules_response" | grep -q "$rule"; then
                ((existing_rules++))
                log "INFO" "发现告警规则: $rule"
            fi
        done

        log "INFO" "关键告警规则: $existing_rules/${#critical_rules[@]}"
        METRIC_COUNTS["Critical-Rules"]="$existing_rules"

        if [ $existing_rules -ge 3 ]; then
            MONITORING_STATUS["Alert-Rules"]="ADEQUATE"
        else
            MONITORING_STATUS["Alert-Rules"]="INSUFFICIENT"
        fi
    else
        log "ERROR" "无法获取告警规则"
        MONITORING_STATUS["Alert-Rules"]="ERROR"
    fi

    # 检查当前活跃告警
    echo -e "\n${BLUE}2. 检查当前活跃告警${NC}"

    local alerts_response=$(curl -s "$PROMETHEUS_URL/api/v1/alerts" 2>/dev/null || echo "")

    if echo "$alerts_response" | grep -q '"status":"success"'; then
        local firing_alerts=$(echo "$alerts_response" | grep -o '"state":"firing"' | wc -l)
        local pending_alerts=$(echo "$alerts_response" | grep -o '"state":"pending"' | wc -l)

        log "INFO" "活跃告警: $firing_alerts 个"
        log "INFO" "待处理告警: $pending_alerts 个"

        METRIC_COUNTS["Active-Alerts"]="$firing_alerts"
        METRIC_COUNTS["Pending-Alerts"]="$pending_alerts"

        if [ $firing_alerts -gt 0 ]; then
            log "WARN" "存在活跃告警，建议检查"
            MONITORING_STATUS["Current-Alerts"]="ACTIVE"
        else
            log "INFO" "无活跃告警"
            MONITORING_STATUS["Current-Alerts"]="CLEAR"
        fi

        # 显示告警详情（如果有的话）
        if [ $firing_alerts -gt 0 ]; then
            echo "活跃告警详情:"
            echo "$alerts_response" | grep -A 5 '"state":"firing"' | grep -E '"labels"|"annotations"' | head -10
        fi
    else
        log "ERROR" "无法获取告警状态"
        MONITORING_STATUS["Current-Alerts"]="ERROR"
    fi

    # 检查告警通知配置
    echo -e "\n${BLUE}3. 检查告警通知配置${NC}"

    # 这里可以检查AlertManager配置
    local alertmanager_url="http://localhost:9093"
    local am_response=$(curl -s "$alertmanager_url/api/v1/status" 2>/dev/null || echo "")

    if echo "$am_response" | grep -q '"status":"success"'; then
        log "INFO" "AlertManager运行正常"
        MONITORING_STATUS["AlertManager"]="ACTIVE"
    else
        log "WARN" "AlertManager未运行或不可访问"
        MONITORING_STATUS["AlertManager"]="INACTIVE"
    fi

    return 0
}

# 检查性能指标
check_performance_metrics() {
    print_section "⚡ 验证性能指标收集"

    echo -e "${BLUE}1. 检查系统资源指标${NC}"

    local system_metrics=(
        "node_cpu_seconds_total"
        "node_memory_MemAvailable_bytes"
        "node_filesystem_avail_bytes"
        "node_network_receive_bytes_total"
    )

    for metric in "${system_metrics[@]}"; do
        local metric_response=$(curl -s "$PROMETHEUS_URL/api/v1/query?query=$metric" 2>/dev/null || echo "")

        if echo "$metric_response" | grep -q '"status":"success"'; then
            local data_count=$(echo "$metric_response" | grep -o '"result":\[' | wc -l)
            if [ $data_count -gt 0 ]; then
                log "INFO" "系统指标 $metric: 有数据 ($data_count 个实例)"
            else
                log "WARN" "系统指标 $metric: 无数据"
            fi
        else
            log "ERROR" "无法获取系统指标 $metric"
        fi
    done

    echo -e "\n${BLUE}2. 检查应用性能指标${NC}"

    local app_metrics=(
        "http_requests_total"
        "http_request_duration_seconds"
        "jvm_memory_used_bytes"
        "jvm_threads_live_threads"
        "spring_boot_actuator_metrics"
    )

    for metric in "${app_metrics[@]}"; do
        local metric_response=$(curl -s "$PROMETHEUS_URL/api/v1/query?query=$metric" 2>/dev/null || echo "")

        if echo "$metric_response" | grep -q '"status":"success"'; then
            local data_count=$(echo "$metric_response" | grep -o '"result":\[' | wc -l)
            if [ $data_count -gt 0 ]; then
                log "INFO" "应用指标 $metric: 有数据 ($data_count 个实例)"
            else
                log "WARN" "应用指标 $metric: 无数据"
            fi
        else
            log "WARN" "应用指标 $metric: 未配置"
        fi
    done

    echo -e "\n${BLUE}3. 检查业务指标${NC}"

    local business_metrics=(
        "user_login_total"
        "access_control_total"
        "consume_transaction_total"
        "attendance_record_total"
        "device_status_total"
    )

    for metric in "${business_metrics[@]}"; do
        local metric_response=$(curl -s "$PROMETHEUS_URL/api/v1/query?query=$metric" 2>/dev/null || echo "")

        if echo "$metric_response" | grep -q '"status":"success"'; then
            local data_count=$(echo "$metric_response" | grep -o '"result":\[' | wc -l)
            if [ $data_count -gt 0 ]; then
                log "INFO" "业务指标 $metric: 有数据 ($data_count 个实例)"
            else
                log "WARN" "业务指标 $metric: 无数据"
            fi
        else
            log "INFO" "业务指标 $metric: 未启用"
        fi
    done

    return 0
}

# 检查监控配置文件
check_monitoring_config() {
    print_section "🔧 验证监控配置文件"

    echo -e "${BLUE}1. 检查Prometheus配置${NC}"

    local prometheus_config_file="$CONFIG_DIR/mysql/prometheus.yml"
    if [ -f "$prometheus_config_file" ]; then
        log "INFO" "Prometheus配置文件存在: $prometheus_config_file"

        # 检查配置文件语法
        local config_check=$(promtool check config "$prometheus_config_file" 2>/dev/null || echo "FAILED")
        if [ "$config_check" = "SUCCESS" ]; then
            log "INFO" "Prometheus配置文件语法正确"
            MONITORING_STATUS["Prometheus-Config-File"]="VALID"
        else
            log "ERROR" "Prometheus配置文件语法错误"
            MONITORING_STATUS["Prometheus-Config-File"]="INVALID"
        fi

        # 检查监控目标配置
        local job_count=$(grep -c "job_name" "$prometheus_config_file" || echo "0")
        log "INFO" "Prometheus监控任务: $job_count 个"
        METRIC_COUNTS["Prometheus-Jobs"]="$job_count"
    else
        log "ERROR" "Prometheus配置文件不存在"
        MONITORING_STATUS["Prometheus-Config-File"]="MISSING"
    fi

    echo -e "\n${BLUE}2. 检查告警规则文件${NC}"

    local rules_dir="$CONFIG_DIR/prometheus_rules"
    if [ -d "$rules_dir" ]; then
        local rules_files=$(find "$rules_dir" -name "*.yml" -o -name "*.yaml" | wc -l)
        log "INFO" "告警规则文件: $rules_files 个"
        METRIC_COUNTS["Rules-Files"]="$rules_files"

        if [ $rules_files -gt 0 ]; then
            MONITORING_STATUS["Alert-Rules-Files"]="EXIST"
        else
            MONITORING_STATUS["Alert-Rules-Files"]="EMPTY"
        fi
    else
        log "WARN" "告警规则目录不存在: $rules_dir"
        MONITORING_STATUS["Alert-Rules-Files"]="MISSING"
    fi

    echo -e "\n${BLUE}3. 检查Grafana配置${NC}"

    local grafana_data_dir="$CONFIG_DIR/mysql/grafana"
    if [ -d "$grafana_data_dir" ]; then
        local dashboard_files=$(find "$grafana_data_dir" -name "*.json" | wc -l)
        local datasource_files=$(find "$grafana_data_dir" -name "*datasource*" | wc -l)

        log "INFO" "Grafana仪表板文件: $dashboard_files 个"
        log "INFO" "Grafana数据源配置: $datasource_files 个"

        METRIC_COUNTS["Grafana-Dashboard-Files"]="$dashboard_files"
        METRIC_COUNTS["Grafana-Datasource-Files"]="$datasource_files"

        MONITORING_STATUS["Grafana-Config"]="EXIST"
    else
        log "WARN" "Grafana配置目录不存在"
        MONITORING_STATUS["Grafana-Config"]="MISSING"
    fi

    return 0
}

# 生成监控验证报告
generate_monitoring_report() {
    print_section "📋 生成监控验证报告"

    local report_file="$REPORT_DIR/monitoring-validation-report-$(date +%Y%m%d_%H%M%S).html"

    log "INFO" "生成监控验证报告: $report_file"

    # 计算整体状态
    local total_checks=${#MONITORING_STATUS[@]}
    local passed_checks=0
    local failed_checks=0

    for status in "${MONITORING_STATUS[@]}"; do
        case $status in
            "ACCESSIBLE"|"VALID"|"ACTIVE"|"SUCCESS"|"CONFIGURED"|"AVAILABLE"|"ADEQUATE"|"CLEAR"|"EXIST")
                ((passed_checks++))
                ;;
            *)
                ((failed_checks++))
                ;;
        esac
    done

    local health_rate=0
    if [ $total_checks -gt 0 ]; then
        health_rate=$((passed_checks * 100 / total_checks))
    fi

    # 生成HTML报告
    cat > "$report_file" << EOF
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IOE-DREAM 监控系统验证报告</title>
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
        .status.good { background: #d4edda; color: #155724; }
        .status.warning { background: #fff3cd; color: #856404; }
        .status.error { background: #f8d7da; color: #721c24; }
        .metric-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; }
        .metric-card { border: 1px solid #ddd; border-radius: 8px; padding: 20px; }
        .metric-card h4 { margin: 0 0 10px 0; color: #007acc; }
        .progress-bar { width: 100%; height: 20px; background: #e0e0e0; border-radius: 10px; overflow: hidden; margin: 20px 0; }
        .progress-fill { height: 100%; background: linear-gradient(90deg, #4CAF50, #45a049); transition: width 0.3s ease; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; }
        .timestamp { color: #666; font-size: 14px; margin-top: 20px; text-align: right; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1 class="title">📊 IOE-DREAM 监控系统验证报告</h1>
            <p class="subtitle">监控系统健康检查与配置验证 • $(date)</p>
        </div>

        <div class="summary">
            <div class="card">
                <h3>$total_checks</h3>
                <p>总检查项</p>
            </div>
            <div class="card success">
                <h3>$passed_checks</h3>
                <p>通过检查</p>
            </div>
            <div class="card error">
                <h3>$failed_checks</h3>
                <p>失败检查</p>
            </div>
            <div class="card warning">
                <h3>${health_rate}%</h3>
                <p>健康率</p>
            </div>
        </div>

        <div class="progress-bar">
            <div class="progress-fill" style="width: ${health_rate}%">${health_rate}%</div>
        </div>

        <div class="section">
            <h2>🏥 监控系统状态检查</h2>
            <table>
                <thead>
                    <tr>
                        <th>检查项目</th>
                        <th>状态</th>
                        <th>说明</th>
                    </tr>
                </thead>
                <tbody>
EOF

    # 添加监控状态检查结果
    for check_name in "${!MONITORING_STATUS[@]}"; do
        local status="${MONITORING_STATUS[$check_name]}"
        local status_class="warning"
        local status_desc="未知状态"

        case $status in
            "ACCESSIBLE"|"VALID"|"ACTIVE"|"SUCCESS"|"CONFIGURED"|"AVAILABLE"|"ADEQUATE"|"CLEAR"|"EXIST")
                status_class="good"
                status_desc="正常"
                ;;
            "INACCESSIBLE"|"INVALID"|"INACTIVE"|"FAILED"|"MISSING"|"ERROR"|"INSUFFICIENT")
                status_class="error"
                status_desc="异常"
                ;;
            *)
                status_class="warning"
                status_desc="需要关注"
                ;;
        esac

        cat >> "$report_file" << EOF
                    <tr>
                        <td>$check_name</td>
                        <td><span class="status $status_class">$status</span></td>
                        <td>$status_desc</td>
                    </tr>
EOF
    done

    cat >> "$report_file" << EOF
                </tbody>
            </table>
        </div>

        <div class="section">
            <h2>📊 监控指标统计</h2>
            <div class="metric-grid">
EOF

    # 添加指标统计
    for metric_name in "${!METRIC_COUNTS[@]}"; do
        local metric_value="${METRIC_COUNTS[$metric_name]}"

        cat >> "$report_file" << EOF
                <div class="metric-card">
                    <h4>$metric_name</h4>
                    <p style="font-size: 24px; font-weight: bold; color: #007acc;">$metric_value</p>
                </div>
EOF
    done

    cat >> "$report_file" << EOF
            </div>
        </div>

        <div class="section">
            <h2>🔗 监控系统访问</h2>
            <ul>
                <li><strong>Prometheus:</strong> <a href="$PROMETHEUS_URL" target="_blank">$PROMETHEUS_URL</a></li>
                <li><strong>Grafana:</strong> <a href="$GRAFANA_URL" target="_blank">$GRAFANA_URL</a> (admin/admin123)</li>
                <li><strong>Nacos:</strong> <a href="$NACOS_URL" target="_blank">$NACOS_URL</a></li>
                <li><strong>AlertManager:</strong> <a href="http://localhost:9093" target="_blank">http://localhost:9093</a></li>
            </ul>
        </div>

        <div class="section">
            <h2>💡 监控优化建议</h2>
            <ul>
EOF

    # 根据检查结果添加建议
    if [ ${METRIC_COUNTS["Targets-Up"]:-0} -lt 8 ]; then
        cat >> "$report_file" << EOF
                <li>🔧 建议检查微服务启动状态，确保所有服务都被Prometheus发现</li>
EOF
    fi

    if [ ${METRIC_COUNTS["Grafana-Dashboards"]:-0} -lt 3 ]; then
        cat >> "$report_file" << EOF
                <li>📈 建议在Grafana中创建更多监控仪表板</li>
EOF
    fi

    if [ ${METRIC_COUNTS["Critical-Rules"]:-0} -lt 3 ]; then
        cat >> "$report_file" << EOF
                <li>🚨 建议配置更多关键告警规则</li>
EOF
    fi

    if [ ${METRIC_COUNTS["Log-Errors"]:-0} -gt 20 ]; then
        cat >> "$report_file" << EOF
                <li>📝 建议关注日志中的错误信息，及时处理系统异常</li>
EOF
    fi

    cat >> "$report_file" << EOF
                <li>🔄 建议定期备份监控配置和仪表板</li>
                <li>📊 建议定期审查和优化告警规则</li>
                <li>💾 建议配置数据保留策略，避免磁盘空间不足</li>
            </ul>
        </div>

        <div class="timestamp">
            报告生成时间：$(date) <br>
            监控验证工具版本：v1.0.0
        </div>
    </div>
</body>
</html>
EOF

    log "INFO" "监控系统验证报告已生成: $report_file"
    echo -e "\n${GREEN}✅ 监控系统验证报告生成完成${NC}"
    echo -e "报告路径: ${BLUE}$report_file${NC}"

    return 0
}

# 显示验证结果摘要
show_monitoring_summary() {
    print_section "📊 监控系统验证结果摘要"

    local total_checks=${#MONITORING_STATUS[@]}
    local passed_checks=0
    local failed_checks=0

    for status in "${MONITORING_STATUS[@]}"; do
        case $status in
            "ACCESSIBLE"|"VALID"|"ACTIVE"|"SUCCESS"|"CONFIGURED"|"AVAILABLE"|"ADEQUATE"|"CLEAR"|"EXIST")
                ((passed_checks++))
                ;;
            *)
                ((failed_checks++))
                ;;
        esac
    done

    local health_rate=0
    if [ $total_checks -gt 0 ]; then
        health_rate=$((passed_checks * 100 / total_checks))
    fi

    echo -e "总检查项目: ${YELLOW}$total_checks${NC}"
    echo -e "通过检查:   ${GREEN}$passed_checks${NC}"
    echo -e "失败检查:   ${RED}$failed_checks${NC}"
    echo -e "健康率:     ${BLUE}${health_rate}%${NC}"

    if [ $health_rate -ge 90 ]; then
        echo -e "监控系统状态: ${GREEN}✅ 优秀${NC}"
    elif [ $health_rate -ge 70 ]; then
        echo -e "监控系统状态: ${YELLOW}⚠️ 良好${NC}"
    else
        echo -e "监控系统状态: ${RED}❌ 需要改进${NC}"
    fi

    echo ""
    echo -e "${CYAN}关键指标:${NC}"
    echo -e "Prometheus目标: ${METRIC_COUNTS["Targets-Up"]:0}/${METRIC_COUNTS["Targets-Total"]:0}"
    echo -e "Grafana仪表板: ${METRIC_COUNTS["Grafana-Dashboards"]:0} 个"
    echo -e "告警规则: ${METRIC_COUNTS["Alerting-Rules"]:0} 条"
    echo -e "活跃告警: ${METRIC_COUNTS["Active-Alerts"]:0} 个"
    echo -e "日志条数: ${METRIC_COUNTS["Log-Entries"]:0} 条"

    return 0
}

# 主函数
main() {
    local command=${1:-"check"}

    case $command in
        "check")
            print_section "🚀 开始IOE-DREAM监控系统验证"
            check_prometheus
            check_grafana
            check_log_collection
            check_alert_system
            check_performance_metrics
            check_monitoring_config
            show_monitoring_summary
            ;;
        "prometheus")
            print_section "📊 Prometheus专项验证"
            check_prometheus
            show_monitoring_summary
            ;;
        "grafana")
            print_section "📈 Grafana专项验证"
            check_grafana
            show_monitoring_summary
            ;;
        "logs")
            print_section "📝 日志收集专项验证"
            check_log_collection
            show_monitoring_summary
            ;;
        "alerts")
            print_section "🚨 告警系统专项验证"
            check_alert_system
            show_monitoring_summary
            ;;
        "report")
            generate_monitoring_report
            ;;
        "help"|"--help"|"-h")
            echo "IOE-DREAM 监控系统验证工具"
            echo ""
            echo "使用方法:"
            echo "  $0 [命令]"
            echo ""
            echo "命令:"
            echo "  check     - 执行全面监控验证 (默认)"
            echo "  prometheus- 验证Prometheus配置和数据收集"
            echo "  grafana   - 验证Grafana仪表板和可视化"
            echo "  logs      - 验证日志收集和查询功能"
            echo "  alerts    - 验证告警规则和通知"
            echo "  report    - 生成监控验证报告"
            echo "  help      - 显示帮助信息"
            echo ""
            echo "示例:"
            echo "  $0 check          # 执行完整监控验证"
            echo "  $0 prometheus    # 仅验证Prometheus"
            echo "  $0 grafana       # 仅验证Grafana"
            echo "  $0 report        # 生成HTML报告"
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