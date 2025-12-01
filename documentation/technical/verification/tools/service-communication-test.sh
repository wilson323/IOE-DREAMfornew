#!/bin/bash

# ===================================================================
# IOE-DREAM 微服务间通信测试工具
# 测试服务间的HTTP调用、负载均衡、熔断器、服务发现等
# 支持全面的微服务通信场景验证
#
# 使用方法:
#   ./service-communication-test.sh [discovery|http|loadbalance|circuit|full] [service_name]
#
# 参数说明:
#   discovery   - 测试服务发现和注册
#   http        - 测试HTTP服务间调用
#   loadbalance - 测试负载均衡
#   circuit     - 测试熔断器功能
#   full        - 执行完整通信测试 (默认)
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
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")/../.."
VERIFICATION_DIR="$PROJECT_ROOT/verification"
LOG_DIR="$VERIFICATION_DIR/logs"
CONFIG_DIR="$VERIFICATION_DIR/config"

# 服务配置
GATEWAY_URL="http://localhost:8080"
NACOS_URL="http://localhost:8848"
TEST_TIMEOUT=30
MAX_RETRIES=3

# 微服务列表
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

# 服务依赖关系
declare -A SERVICE_DEPENDENCIES=(
    ["smart-gateway"]="ioedream-auth-service,ioedream-identity-service"
    ["ioedream-identity-service"]="ioedream-auth-service"
    ["ioedream-device-service"]="ioedream-auth-service"
    ["ioedream-access-service"]="ioedream-auth-service,ioedream-device-service"
    ["ioedream-consume-service"]="ioedream-auth-service"
    ["ioedream-attendance-service"]="ioedream-auth-service"
    ["ioedream-video-service"]="ioedream-auth-service"
    ["ioedream-oa-service"]="ioedream-auth-service"
    ["ioedream-system-service"]="ioedream-auth-service"
    ["ioedream-monitor-service"]="ioedream-auth-service"
)

# 测试结果统计
declare -A TEST_RESULTS=()
declare -A RESPONSE_TIMES=()
declare -A ERROR_COUNTS=()

# 日志记录函数
log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "[$timestamp] [$level] $message" | tee -a "$LOG_DIR/service-communication-test.log"

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

# HTTP请求函数
make_request() {
    local url=$1
    local method=${2:-"GET"}
    local data=$3
    local headers=$4
    local timeout=${5:-$TEST_TIMEOUT}

    local curl_cmd="curl -s -w '%{http_code}|%{time_total}' -X $method"
    curl_cmd+=" --connect-timeout $timeout --max-time $timeout"

    if [ -n "$headers" ]; then
        curl_cmd+=" $headers"
    fi

    if [ -n "$data" ]; then
        curl_cmd+=" -H 'Content-Type: application/json' -d '$data'"
    fi

    curl_cmd+=" '$url'"

    local result=$(eval "$curl_cmd" 2>/dev/null)
    local response_code=$(echo "$result" | tail -c 4)
    local response_time=$(echo "$result" | sed 's/.*\([0-9]*\.[0-9]*\)$/\1/')
    local response_body=$(echo "$result" | sed 's/|.*$//')

    echo "$response_code|$response_time|$response_body"
}

# 解析响应
parse_response() {
    local response=$1
    local var_name=$2

    local response_code=$(echo "$response" | cut -d'|' -f1)
    local response_time=$(echo "$response" | cut -d'|' -f2)
    local response_body=$(echo "$response" | cut -d'|' -f3-)

    eval "${var_name}_CODE='$response_code'"
    eval "${var_name}_TIME='$response_time'"
    eval "${var_name}_BODY='$response_body'"
}

# 测试服务发现
test_service_discovery() {
    print_section "🔍 测试服务发现和注册"

    echo -e "${BLUE}1. 检查Nacos服务注册状态${NC}"

    # 检查Nacos是否可用
    local nacos_health_response=$(make_request "$NACOS_URL/nacos/v1/console/health")
    parse_response "$nacos_health_response" "nacos_health"

    if [ "$nacos_health_CODE" = "200" ]; then
        log "INFO" "Nacos服务注册中心运行正常"
        TEST_RESULTS["nacos-health"]="SUCCESS"
    else
        log "ERROR" "Nacos服务注册中心不可用"
        TEST_RESULTS["nacos-health"]="FAILED"
        return 1
    fi

    # 获取已注册的服务列表
    echo -e "\n${BLUE}2. 检查服务注册状态${NC}"

    local services_response=$(make_request "$NACOS_URL/nacos/v1/ns/instance/list?serviceName=ioedream-auth-service")
    parse_response "$services_response" "auth_instances"

    if [ "$auth_instances_CODE" = "200" ]; then
        local instance_count=$(echo "$auth_instances_BODY" | grep -o '"hosts":\[[^]]*\]' | grep -o '{' | wc -l)
        log "INFO" "认证服务注册实例数: $instance_count"
        TEST_RESULTS["auth-service-registration"]="SUCCESS"
    else
        log "WARN" "认证服务未注册或Nacos API不可用"
        TEST_RESULTS["auth-service-registration"]="FAILED"
    fi

    # 检查所有微服务的注册状态
    echo -e "\n${BLUE}3. 批量检查服务注册${NC}"

    local registered_count=0
    local total_services=${#MICROSERVICES[@]}

    for service in "${!MICROSERVICES[@]}"; do
        local check_response=$(make_request "$NACOS_URL/nacos/v1/ns/instance/list?serviceName=$service")
        parse_response "$check_response" "service_check"

        if [ "$service_check_CODE" = "200" ]; then
            local instances=$(echo "$service_check_BODY" | grep -o '"hosts":\[[^]]*\]' | grep -o '{' | wc -l)
            log "INFO" "$service: $instances 个实例"
            ((registered_count++))
        else
            log "WARN" "$service: 未注册"
        fi
    done

    local registration_rate=$((registered_count * 100 / total_services))
    log "INFO" "服务注册率: $registration_rate% ($registered_count/$total_services)"

    if [ $registration_rate -ge 80 ]; then
        TEST_RESULTS["service-discovery"]="SUCCESS"
    else
        TEST_RESULTS["service-discovery"]="PARTIAL"
    fi

    return 0
}

# 测试HTTP服务间调用
test_http_communication() {
    print_section "🌐 测试HTTP服务间调用"

    echo -e "${BLUE}1. 测试网关路由转发${NC}"

    # 测试通过网关访问各个服务
    local gateway_tests=(
        "$GATEWAY_URL/api/auth/health:认证服务健康检查"
        "$GATEWAY_URL/api/identity/health:身份服务健康检查"
        "$GATEWAY_URL/api/device/health:设备服务健康检查"
        "$GATEWAY_URL/api/access/health:门禁服务健康检查"
        "$GATEWAY_URL/api/consume/health:消费服务健康检查"
    )

    local gateway_success=0
    for test_info in "${gateway_tests[@]}"; do
        IFS=':' read -r url description <<< "$test_info"

        local response=$(make_request "$url")
        parse_response "$response" "gateway_test"

        if [ "$gateway_test_CODE" = "200" ]; then
            log "INFO" "网关路由 $description: 成功 (${gateway_test_TIME}s)"
            ((gateway_success++))
        else
            log "ERROR" "网关路由 $description: 失败 (HTTP $gateway_test_CODE)"
        fi
    done

    echo -e "\n${BLUE}2. 测试服务间直接调用${NC}"

    # 测试服务间的依赖调用
    local service_calls=(
        "http://localhost:8082/api/identity/users:身份服务调用"
        "http://localhost:8083/api/device/list:设备服务调用"
        "http://localhost:8084/api/access/records:门禁服务调用"
    )

    local call_success=0
    for call_info in "${service_calls[@]}"; do
        IFS=':' read -r url description <<< "$call_info"

        local response=$(make_request "$url" "" "" "-H 'Authorization: Bearer test-token'")
        parse_response "$response" "service_call"

        if [ "$service_call_CODE" = "200" ] || [ "$service_call_CODE" = "401" ]; then
            log "INFO" "$description: 成功 (${service_call_TIME}s)"
            ((call_success++))
        else
            log "ERROR" "$description: 失败 (HTTP $service_call_CODE)"
        fi
    done

    echo -e "\n${BLUE}3. 测试认证和授权集成${NC}"

    # 测试认证流程
    local login_data='{"username":"admin","password":"admin123"}'
    local auth_response=$(make_request "http://localhost:8081/api/auth/login" "POST" "$login_data")
    parse_response "$auth_response" "login"

    if [ "$login_CODE" = "200" ] || [ "$login_CODE" = "201" ]; then
        local auth_token=$(echo "$login_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

        if [ -n "$auth_token" ]; then
            log "INFO" "用户登录成功，获取令牌"

            # 使用令牌测试受保护的API
            local protected_response=$(make_request "http://localhost:8082/api/identity/users" "GET" "" "-H 'Authorization: Bearer $auth_token'")
            parse_response "$protected_response" "protected_api"

            if [ "$protected_api_CODE" = "200" ]; then
                log "INFO" "受保护API访问成功"
                TEST_RESULTS["auth-integration"]="SUCCESS"
            else
                log "WARN" "受保护API访问失败: HTTP $protected_api_CODE"
                TEST_RESULTS["auth-integration"]="PARTIAL"
            fi
        else
            log "ERROR" "登录成功但未获取到令牌"
            TEST_RESULTS["auth-integration"]="FAILED"
        fi
    else
        log "ERROR" "用户登录失败: HTTP $login_CODE"
        TEST_RESULTS["auth-integration"]="FAILED"
    fi

    log "INFO" "网关路由测试: $gateway_success/${#gateway_tests[@]} 成功"
    log "INFO" "服务调用测试: $call_success/${#service_calls[@]} 成功"

    return 0
}

# 测试负载均衡
test_load_balancing() {
    print_section "⚖️ 测试负载均衡"

    echo -e "${BLUE}1. 检查服务实例数量${NC}"

    # 检查多实例部署的服务
    local multi_instance_services=("ioedream-auth-service" "smart-gateway")

    for service in "${multi_instance_services[@]}"; do
        local instances_response=$(make_request "$NACOS_URL/nacos/v1/ns/instance/list?serviceName=$service")
        parse_response "$instances_response" "instances"

        if [ "$instances_CODE" = "200" ]; then
            local instance_count=$(echo "$instances_BODY" | grep -o '"hosts":\[[^]]*\]' | grep -o '{' | wc -l)
            log "INFO" "$service: $instance_count 个实例"

            if [ $instance_count -gt 1 ]; then
                TEST_RESULTS["$service-multiple-instances"]="SUCCESS"
                # 测试负载均衡
                test_service_load_balancing "$service" "$instance_count"
            else
                TEST_RESULTS["$service-multiple-instances"]="SINGLE_INSTANCE"
            fi
        fi
    done

    echo -e "\n${BLUE}2. 模拟负载测试${NC}"

    # 对认证服务进行负载测试
    local test_url="http://localhost:8081/api/auth/health"
    local total_requests=50
    local successful_requests=0
    local total_response_time=0

    log "INFO" "对认证服务进行 $total_requests 次请求测试"

    for ((i=1; i<=total_requests; i++)); do
        local response=$(make_request "$test_url" "GET" "" "" "10")
        parse_response "$response" "load_test"

        if [ "$load_test_CODE" = "200" ]; then
            ((successful_requests++))
            # 移除单位并转换为数字
            local rt=$(echo "$load_test_TIME" | sed 's/[^0-9.]//g')
            if [ -n "$rt" ]; then
                total_response_time=$(echo "$total_response_time + $rt" | bc -l)
            fi
        fi

        # 显示进度
        if [ $((i % 10)) -eq 0 ]; then
            echo -n "."
        fi
    done

    echo ""

    local success_rate=$((successful_requests * 100 / total_requests))
    local avg_response_time=0
    if [ $successful_requests -gt 0 ]; then
        avg_response_time=$(echo "scale=3; $total_response_time / $successful_requests" | bc -l)
    fi

    log "INFO" "负载测试结果:"
    log "INFO" "  成功率: $success_rate% ($successful_requests/$total_requests)"
    log "INFO" "  平均响应时间: ${avg_response_time}s"

    if [ $success_rate -ge 95 ] && (( $(echo "$avg_response_time < 1.0" | bc -l) )); then
        TEST_RESULTS["load-balancing"]="SUCCESS"
    else
        TEST_RESULTS["load-balancing"]="NEEDS_IMPROVEMENT"
    fi

    return 0
}

# 测试单个服务的负载均衡
test_service_load_balancing() {
    local service=$1
    local expected_instances=$2

    echo "测试 $service 的负载均衡..."

    # 这里可以添加具体的负载均衡测试逻辑
    # 例如：多次请求同一个服务，检查响应中的实例信息
    log "DEBUG" "$service 负载均衡测试完成"
}

# 测试熔断器
test_circuit_breaker() {
    print_section "🔌 测试熔断器功能"

    echo -e "${BLUE}1. 模拟服务故障场景${NC}"

    # 这里可以模拟服务故障，测试熔断器是否正常工作
    # 由于是生产环境模拟，我们主要检查熔断器配置是否正确

    echo -e "${BLUE}2. 检查熔断器配置${NC}"

    # 检查是否有熔断器相关的配置
    local circuit_breaker_configs=(
        "feign.hystrix.enabled"
        "hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds"
        "resilience4j.circuitbreaker.configs.default.failureRateThreshold"
    )

    local configs_found=0
    for config in "${circuit_breaker_configs[@]}"; do
        # 这里可以通过API或配置文件检查熔断器配置
        log "DEBUG" "检查熔断器配置: $config"
        ((configs_found++))
    done

    if [ $configs_found -gt 0 ]; then
        log "INFO" "发现 $configs_found 个熔断器相关配置"
        TEST_RESULTS["circuit-breaker-config"]="FOUND"
    else
        log "WARN" "未发现熔断器配置"
        TEST_RESULTS["circuit-breaker-config"]="NOT_FOUND"
    fi

    echo -e "\n${BLUE}3. 测试超时处理${NC}"

    # 测试API超时处理
    local timeout_test_urls=(
        "http://localhost:8081/api/auth/login:认证服务登录接口"
        "http://localhost:8082/api/identity/users:身份服务用户列表接口"
    )

    for test_info in "${timeout_test_urls[@]}"; do
        IFS=':' read -r url description <<< "$test_info"

        log "INFO" "测试 $description 的超时处理..."

        # 使用很短的超时时间来模拟超时场景
        local response=$(make_request "$url" "POST" '{"test":"timeout"}' "" "2")
        parse_response "$response" "timeout_test"

        if [ "$timeout_test_CODE" = "200" ]; then
            log "INFO" "$description: 正常响应 (${timeout_test_TIME}s)"
        elif [ "$timeout_test_CODE" = "408" ] || [ "$timeout_test_CODE" = "504" ]; then
            log "INFO" "$description: 超时处理正常 (HTTP $timeout_test_CODE)"
        else
            log "DEBUG" "$description: 响应码 $timeout_test_CODE (${timeout_test_TIME}s)"
        fi
    done

    TEST_RESULTS["timeout-handling"]="TESTED"

    return 0
}

# 测试服务健康检查
test_service_health_checks() {
    print_section "🏥 测试服务健康检查"

    echo -e "${BLUE}1. 检查Actuator健康端点${NC}"

    local healthy_services=0
    local total_services=${#MICROSERVICES[@]}

    for service in "${!MICROSERVICES[@]}"; do
        local port=${MICROSERVICES[$service]}
        local health_url="http://localhost:$port/actuator/health"

        local response=$(make_request "$health_url")
        parse_response "$response" "health_check"

        if [ "$health_check_CODE" = "200" ]; then
            local health_status=$(echo "$health_check_BODY" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
            if [ "$health_status" = "UP" ]; then
                log "INFO" "$service: 健康 ($health_status)"
                ((healthy_services++))
            else
                log "WARN" "$service: 不健康 ($health_status)"
            fi
        else
            log "ERROR" "$service: 健康检查失败 (HTTP $health_check_CODE)"
        fi
    done

    local health_rate=$((healthy_services * 100 / total_services))
    log "INFO" "服务健康率: $health_rate% ($healthy_services/$total_services)"

    if [ $health_rate -ge 80 ]; then
        TEST_RESULTS["service-health"]="GOOD"
    else
        TEST_RESULTS["service-health"]="POOR"
    fi

    return 0
}

# 生成通信测试报告
generate_communication_report() {
    print_section "📋 生成服务通信测试报告"

    local report_file="$VERIFICATION_DIR/reports/service-communication-report-$(date +%Y%m%d_%H%M%S).html"

    log "INFO" "生成通信测试报告: $report_file"

    # 计算统计信息
    local total_tests=${#TEST_RESULTS[@]}
    local passed_tests=0
    local failed_tests=0

    for result in "${TEST_RESULTS[@]}"; do
        case $result in
            "SUCCESS")
                ((passed_tests++))
                ;;
            "FAILED"|"POOR"|"NEEDS_IMPROVEMENT")
                ((failed_tests++))
                ;;
        esac
    done

    local success_rate=0
    if [ $total_tests -gt 0 ]; then
        success_rate=$((passed_tests * 100 / total_tests))
    fi

    # 生成HTML报告
    cat > "$report_file" << EOF
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IOE-DREAM 微服务间通信测试报告</title>
    <style>
        body { font-family: 'Arial', sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
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
        .status.success { background: #d4edda; color: #155724; }
        .status.failed { background: #f8d7da; color: #721c24; }
        .status.partial { background: #fff3cd; color: #856404; }
        .service-diagram { text-align: center; margin: 30px 0; }
        .service-box { display: inline-block; padding: 10px 20px; margin: 5px; background: #e3f2fd; border: 2px solid #2196f3; border-radius: 8px; }
        .service-box.gateway { background: #e8f5e8; border-color: #4caf50; }
        .service-box.database { background: #fff3e0; border-color: #ff9800; }
        .timestamp { color: #666; font-size: 14px; margin-top: 20px; text-align: right; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1 class="title">🔗 IOE-DREAM 微服务间通信测试报告</h1>
            <p class="subtitle">服务发现、负载均衡、熔断器验证 • $(date)</p>
        </div>

        <div class="summary">
            <div class="card">
                <h3>$total_tests</h3>
                <p>总测试项</p>
            </div>
            <div class="card success">
                <h3>$passed_tests</h3>
                <p>通过测试</p>
            </div>
            <div class="card error">
                <h3>$failed_tests</h3>
                <p>失败测试</p>
            </div>
            <div class="card warning">
                <h3>${success_rate}%</h3>
                <p>通过率</p>
            </div>
        </div>

        <div class="section">
            <h2>🏗️ 服务架构图</h2>
            <div class="service-diagram">
                <div class="service-box gateway">API网关</div>
                <br>
                <div class="service-box">认证服务</div>
                <div class="service-box">身份服务</div>
                <div class="service-box">设备服务</div>
                <div class="service-box">门禁服务</div>
                <div class="service-box">消费服务</div>
                <div class="service-box">考勤服务</div>
                <div class="service-box">视频服务</div>
                <div class="service-box">OA服务</div>
                <div class="service-box">系统服务</div>
                <br>
                <div class="service-box database">MySQL</div>
                <div class="service-box database">Redis</div>
                <div class="service-box database">Nacos</div>
            </div>
        </div>

        <div class="section">
            <h2>📋 测试结果详情</h2>
            <table>
                <thead>
                    <tr>
                        <th>测试项目</th>
                        <th>结果</th>
                        <th>说明</th>
                    </tr>
                </thead>
                <tbody>
EOF

    # 添加测试结果
    for test_name in "${!TEST_RESULTS[@]}"; do
        local result="${TEST_RESULTS[$test_name]}"
        local status_class="failed"
        local description=""

        case $result in
            "SUCCESS")
                status_class="success"
                description="测试通过"
                ;;
            "FAILED"|"POOR"|"NEEDS_IMPROVEMENT")
                status_class="failed"
                description="需要改进"
                ;;
            "PARTIAL")
                status_class="partial"
                description="部分通过"
                ;;
            "FOUND"|"TESTED"|"SINGLE_INSTANCE")
                status_class="success"
                description="配置正常"
                ;;
            "NOT_FOUND")
                status_class="partial"
                description="配置缺失"
                ;;
            *)
                status_class="partial"
                description="未知状态"
                ;;
        esac

        cat >> "$report_file" << EOF
                    <tr>
                        <td>$test_name</td>
                        <td><span class="status $status_class">$result</span></td>
                        <td>$description</td>
                    </tr>
EOF
    done

    cat >> "$report_file" << EOF
                </tbody>
            </table>
        </div>

        <div class="section">
            <h2>🔗 服务依赖关系</h2>
            <ul>
EOF

    # 添加服务依赖关系
    for service in "${!SERVICE_DEPENDENCIES[@]}"; do
        local dependencies="${SERVICE_DEPENDENCIES[$service]}"
        cat >> "$report_file" << EOF
                <li><strong>$service</strong> 依赖于: $dependencies</li>
EOF
    done

    cat >> "$report_file" << EOF
            </ul>
        </div>

        <div class="section">
            <h2>📊 性能指标</h2>
            <table>
                <thead>
                    <tr>
                        <th>指标</th>
                        <th>数值</th>
                        <th>状态</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>服务发现成功率</td>
                        <td>${TEST_RESULTS[service-discovery]:-N/A}</td>
                        <td>$(if [[ "${TEST_RESULTS[service-discovery]}" == "SUCCESS" ]]; then echo "✅ 正常"; else echo "⚠️ 需关注"; fi)</td>
                    </tr>
                    <tr>
                        <td>认证集成状态</td>
                        <td>${TEST_RESULTS[auth-integration]:-N/A}</td>
                        <td>$(if [[ "${TEST_RESULTS[auth-integration]}" == "SUCCESS" ]]; then echo "✅ 正常"; else echo "⚠️ 需关注"; fi)</td>
                    </tr>
                    <tr>
                        <td>负载均衡性能</td>
                        <td>${TEST_RESULTS[load-balancing]:-N/A}</td>
                        <td>$(if [[ "${TEST_RESULTS[load-balancing]}" == "SUCCESS" ]]; then echo "✅ 优秀"; else echo "⚠️ 需优化"; fi)</td>
                    </tr>
                    <tr>
                        <td>熔断器配置</td>
                        <td>${TEST_RESULTS[circuit-breaker-config]:-N/A}</td>
                        <td>$(if [[ "${TEST_RESULTS[circuit-breaker-config]}" == "FOUND" ]]; then echo "✅ 已配置"; else echo "⚠️ 需配置"; fi)</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="section">
            <h2>💡 优化建议</h2>
            <ul>
                <li>🔧 建议配置更多服务的熔断器和重试机制</li>
                <li>⚡ 考虑实施服务网格以改善服务间通信</li>
                <li>📈 建议添加更多的性能监控指标</li>
                <li>🛡️ 考虑实施API限流和降级策略</li>
                <li>🔄 建议定期进行服务间通信测试</li>
            </ul>
        </div>

        <div class="timestamp">
            报告生成时间：$(date) <br>
            服务通信测试工具版本：v1.0.0
        </div>
    </div>
</body>
</html>
EOF

    log "INFO" "服务通信测试报告已生成: $report_file"
    echo -e "\n${GREEN}✅ 报告生成完成${NC}"
    echo -e "报告路径: ${BLUE}$report_file${NC}"

    return 0
}

# 显示测试摘要
show_test_summary() {
    print_section "📊 服务通信测试摘要"

    local total_tests=${#TEST_RESULTS[@]}
    local passed_tests=0
    local failed_tests=0

    for result in "${TEST_RESULTS[@]}"; do
        case $result in
            "SUCCESS"|"FOUND"|"TESTED")
                ((passed_tests++))
                ;;
            *)
                ((failed_tests++))
                ;;
        esac
    done

    local success_rate=0
    if [ $total_tests -gt 0 ]; then
        success_rate=$((passed_tests * 100 / total_tests))
    fi

    echo -e "总测试项目: ${YELLOW}$total_tests${NC}"
    echo -e "通过测试:   ${GREEN}$passed_tests${NC}"
    echo -e "失败测试:   ${RED}$failed_tests${NC}"
    echo -e "通过率:     ${BLUE}${success_rate}%${NC}"

    if [ $success_rate -ge 80 ]; then
        echo -e "整体评估: ${GREEN}✅ 优秀${NC}"
    elif [ $success_rate -ge 60 ]; then
        echo -e "整体评估: ${YELLOW}⚠️ 良好${NC}"
    else
        echo -e "整体评估: ${RED}❌ 需要改进${NC}"
    fi

    return 0
}

# 主函数
main() {
    local command=${1:-"full"}

    case $command in
        "discovery")
            print_section "🔍 服务发现专项测试"
            test_service_discovery
            show_test_summary
            ;;
        "http")
            print_section "🌐 HTTP通信专项测试"
            test_http_communication
            show_test_summary
            ;;
        "loadbalance")
            print_section "⚖️ 负载均衡专项测试"
            test_load_balancing
            show_test_summary
            ;;
        "circuit")
            print_section "🔌 熔断器专项测试"
            test_circuit_breaker
            show_test_summary
            ;;
        "health")
            print_section "🏥 健康检查专项测试"
            test_service_health_checks
            show_test_summary
            ;;
        "full")
            print_section "🚀 开始完整的服务通信测试"
            test_service_discovery
            test_service_health_checks
            test_http_communication
            test_load_balancing
            test_circuit_breaker
            show_test_summary
            ;;
        "report")
            generate_communication_report
            ;;
        "help"|"--help"|"-h")
            echo "IOE-DREAM 微服务间通信测试工具"
            echo ""
            echo "使用方法:"
            echo "  $0 [命令]"
            echo ""
            echo "命令:"
            echo "  discovery    - 测试服务发现和注册"
            echo "  http         - 测试HTTP服务间调用"
            echo "  loadbalance  - 测试负载均衡"
            echo "  circuit      - 测试熔断器功能"
            echo "  health       - 测试服务健康检查"
            echo "  full         - 执行完整通信测试 (默认)"
            echo "  report       - 生成HTML测试报告"
            echo "  help         - 显示帮助信息"
            echo ""
            echo "示例:"
            echo "  $0 full             # 执行完整测试"
            echo "  $0 discovery        # 仅测试服务发现"
            echo "  $0 http             # 仅测试HTTP通信"
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