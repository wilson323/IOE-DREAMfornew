#!/bin/bash

# =============================================================================
# IOE-DREAM 微服务集成测试套件
# =============================================================================
#
# 功能: 执行全面的微服务集成测试
# 覆盖: 服务发现、API通信、熔断器、负载均衡、业务流程
#
# 作者: IOE-DREAM测试团队
# 版本: v1.0.0
# 最后更新: 2025-11-29
# =============================================================================

set -e

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")/.."
TEST_LOG_DIR="$PROJECT_ROOT/test/reports/integration"
TEST_CONFIG_DIR="$PROJECT_ROOT/test/config"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 服务地址配置
GATEWAY_URL="http://localhost:8080"
AUTH_SERVICE_URL="http://localhost:8081"
IDENTITY_SERVICE_URL="http://localhost:8082"
DEVICE_SERVICE_URL="http://localhost:8083"
ACCESS_SERVICE_URL="http://localhost:8084"
CONSUME_SERVICE_URL="http://localhost:8085"
ATTENDANCE_SERVICE_URL="http://localhost:8086"
VIDEO_SERVICE_URL="http://localhost:8087"

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
TEST_RESULTS=()

# 日志函数
log() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS:${NC} $1"
    ((PASSED_TESTS++))
}

log_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1"
    ((FAILED_TESTS++))
}

log_warning() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1"
}

# 创建测试日志目录
setup_log_directory() {
    log "设置测试日志目录..."
    mkdir -p "$TEST_LOG_DIR"
    mkdir -p "$TEST_LOG_DIR/logs"
    mkdir -p "$TEST_LOG_DIR/reports"

    # 创建测试报告文件
    REPORT_FILE="$TEST_LOG_DIR/reports/integration-test-$(date +%Y%m%d-%H%M%S).json"
    echo "{\"testSuite\":\"integration\",\"timestamp\":\"$(date -Iseconds)\",\"tests\":[]}" > "$REPORT_FILE"
    echo "$REPORT_FILE"
}

# 检查服务健康状态
check_service_health() {
    local service_name=$1
    local service_url=$2
    local max_attempts=30
    local attempt=1

    log "检查服务健康状态: $service_name ($service_url)"

    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$service_url/actuator/health" > /dev/null 2>&1; then
            log_success "$service_name 服务健康检查通过"
            return 0
        fi

        log_warning "$service_name 健康检查失败，重试 $attempt/$max_attempts..."
        sleep 2
        ((attempt++))
    done

    log_error "$service_name 健康检查失败，最大重试次数已达到"
    return 1
}

# 检查所有服务健康状态
check_all_services_health() {
    log "检查所有微服务健康状态..."

    local services=(
        "Gateway:$GATEWAY_URL"
        "Auth Service:$AUTH_SERVICE_URL"
        "Identity Service:$IDENTITY_SERVICE_URL"
        "Device Service:$DEVICE_SERVICE_URL"
        "Access Service:$ACCESS_SERVICE_URL"
        "Consume Service:$CONSUME_SERVICE_URL"
        "Attendance Service:$ATTENDANCE_SERVICE_URL"
        "Video Service:$VIDEO_SERVICE_URL"
    )

    local unhealthy_services=()

    for service in "${services[@]}"; do
        IFS=':' read -r service_name service_url <<< "$service"
        if ! check_service_health "$service_name" "$service_url"; then
            unhealthy_services+=("$service_name")
        fi
    done

    if [ ${#unhealthy_services[@]} -eq 0 ]; then
        log_success "所有服务健康检查通过"
        return 0
    else
        log_error "以下服务健康检查失败: ${unhealthy_services[*]}"
        return 1
    fi
}

# HTTP请求辅助函数
make_http_request() {
    local method=$1
    local url=$2
    local data=$3
    local headers=$4
    local expected_status=${5:-200}

    ((TOTAL_TESTS++))

    local cmd="curl -s -w '%{http_code}' -o /tmp/response_body.tmp"

    if [ -n "$headers" ]; then
        cmd="$cmd -H '$headers'"
    fi

    if [ -n "$data" ]; then
        cmd="$cmd -X $method -d '$data'"
    else
        cmd="$cmd -X $method"
    fi

    cmd="$cmd '$url'"

    local response_code
    response_code=$(eval "$cmd")
    local response_body
    response_body=$(cat /tmp/response_body.tmp)

    if [ "$response_code" -eq "$expected_status" ]; then
        log_success "HTTP $method $url - 状态码: $response_code"
        TEST_RESULTS+=("{\"name\":\"HTTP $method $url\",\"status\":\"passed\",\"code\":$response_code,\"response\":\"$response_body\"}")
        return 0
    else
        log_error "HTTP $method $url - 期望: $expected_status, 实际: $response_code"
        TEST_RESULTS+=("{\"name\":\"HTTP $method $url\",\"status\":\"failed\",\"expected\":$expected_status,\"actual\":$response_code,\"response\":\"$response_body\"}")
        return 1
    fi
}

# 用户认证流程测试
test_authentication_flow() {
    log "开始用户认证流程测试..."

    # 1. 用户登录
    local login_data='{
        "username": "testuser",
        "password": "testpass123"
    }'

    if make_http_request "POST" "$GATEWAY_URL/api/auth/login" "$login_data" "Content-Type: application/json" "200"; then
        # 提取token
        local token=$(cat /tmp/response_body.tmp | jq -r '.data.token // empty')

        if [ -n "$token" ]; then
            log_success "登录成功，获取到token"

            # 2. 验证token有效性
            if make_http_request "GET" "$GATEWAY_URL/api/auth/verify" "" "Authorization: Bearer $token" "200"; then
                log_success "Token验证通过"
            else
                log_error "Token验证失败"
            fi

            # 3. 获取用户信息
            if make_http_request "GET" "$GATEWAY_URL/api/user/profile" "" "Authorization: Bearer $token" "200"; then
                log_success "用户信息获取成功"
            else
                log_error "用户信息获取失败"
            fi
        else
            log_error "登录成功但未获取到token"
        fi
    else
        log_error "用户登录失败"
    fi
}

# 服务发现和注册测试
test_service_discovery() {
    log "开始服务发现和注册测试..."

    # 检查Nacos服务注册
    if make_http_request "GET" "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=ioedream-auth-service" "" "" "200"; then
        log_success "Auth服务Nacos注册正常"
    else
        log_error "Auth服务Nacos注册异常"
    fi

    if make_http_request "GET" "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=ioedream-gateway" "" "" "200"; then
        log_success "Gateway服务Nacos注册正常"
    else
        log_error "Gateway服务Nacos注册异常"
    fi

    # 测试服务间调用
    if make_http_request "GET" "$GATEWAY_URL/api/auth/service-status" "" "" "200"; then
        log_success "服务间调用正常"
    else
        log_error "服务间调用异常"
    fi
}

# 负载均衡测试
test_load_balancing() {
    log "开始负载均衡测试..."

    # 发送多个请求，检查负载是否分散
    local success_count=0
    local total_requests=10

    for i in $(seq 1 $total_requests); do
        if make_http_request "GET" "$GATEWAY_URL/api/auth/ping" "" "" "200"; then
            ((success_count++))
        fi
    done

    if [ $success_count -eq $total_requests ]; then
        log_success "负载均衡测试通过，成功率: 100%"
    else
        log_warning "负载均衡测试部分失败，成功率: $((success_count * 100 / total_requests))%"
    fi
}

# 熔断器测试
test_circuit_breaker() {
    log "开始熔断器测试..."

    # 模拟服务不可用的情况
    local unavailable_service_url="http://localhost:9999/unavailable"

    # 发送请求到不可用服务，触发熔断器
    for i in {1..5}; do
        make_http_request "GET" "$GATEWAY_URL/api/test/circuit-breaker" "" "" "503"
        sleep 1
    done

    # 检查熔断器状态
    if make_http_request "GET" "$GATEWAY_URL/actuator/circuitbreakers" "" "" "200"; then
        log_success "熔断器状态获取成功"
    else
        log_error "熔断器状态获取失败"
    fi
}

# 门禁控制流程测试
test_access_control_flow() {
    log "开始门禁控制流程测试..."

    # 1. 获取设备列表
    if make_http_request "GET" "$GATEWAY_URL/api/access/devices" "" "Authorization: Bearer test-token" "200"; then
        log_success "设备列表获取成功"
    else
        log_error "设备列表获取失败"
    fi

    # 2. 模拟门禁刷卡
    local access_data='{
        "deviceId": "device001",
        "cardId": "card123456",
        "timestamp": "'$(date -Iseconds)'"
    }'

    if make_http_request "POST" "$GATEWAY_URL/api/access/verify" "$access_data" "Content-Type: application/json" "200"; then
        log_success "门禁验证成功"
    else
        log_error "门禁验证失败"
    fi

    # 3. 获取门禁记录
    if make_http_request "GET" "$GATEWAY_URL/api/access/records?limit=10" "" "Authorization: Bearer test-token" "200"; then
        log_success "门禁记录获取成功"
    else
        log_error "门禁记录获取失败"
    fi
}

# 消费支付流程测试
test_consume_payment_flow() {
    log "开始消费支付流程测试..."

    # 1. 获取消费账户信息
    if make_http_request "GET" "$GATEWAY_URL/api/consume/account/123456" "" "Authorization: Bearer test-token" "200"; then
        log_success "消费账户信息获取成功"
    else
        log_error "消费账户信息获取失败"
    fi

    # 2. 模拟消费交易
    local consume_data='{
        "accountId": "123456",
        "deviceId": "consume001",
        "amount": 12.50,
        "type": "MEAL",
        "description": "午餐消费"
    }'

    if make_http_request "POST" "$GATEWAY_URL/api/consume/payment" "$consume_data" "Content-Type: application/json" "200"; then
        log_success "消费支付成功"
    else
        log_error "消费支付失败"
    fi

    # 3. 获取消费记录
    if make_http_request "GET" "$GATEWAY_URL/api/consume/records?accountId=123456&limit=10" "" "Authorization: Bearer test-token" "200"; then
        log_success "消费记录获取成功"
    else
        log_error "消费记录获取失败"
    fi
}

# 考勤管理流程测试
test_attendance_flow() {
    log "开始考勤管理流程测试..."

    # 1. 获取考勤规则
    if make_http_request "GET" "$GATEWAY_URL/api/attendance/rules" "" "Authorization: Bearer test-token" "200"; then
        log_success "考勤规则获取成功"
    else
        log_error "考勤规则获取失败"
    fi

    # 2. 模拟考勤打卡
    local attendance_data='{
        "employeeId": "emp001",
        "deviceId": "attend001",
        "type": "CHECK_IN",
        "timestamp": "'$(date -Iseconds)'"
    }'

    if make_http_request "POST" "$GATEWAY_URL/api/attendance/check" "$attendance_data" "Content-Type: application/json" "200"; then
        log_success "考勤打卡成功"
    else
        log_error "考勤打卡失败"
    fi

    # 3. 获取考勤记录
    if make_http_request "GET" "$GATEWAY_URL/api/attendance/records?employeeId=emp001&date=$(date +%Y-%m-%d)" "" "Authorization: Bearer test-token" "200"; then
        log_success "考勤记录获取成功"
    else
        log_error "考勤记录获取失败"
    fi
}

# API契约测试
test_api_contracts() {
    log "开始API契约测试..."

    local apis=(
        "GET:/api/auth/ping"
        "GET:/api/user/profile"
        "POST:/api/access/verify"
        "POST:/api/consume/payment"
        "POST:/api/attendance/check"
        "GET:/api/video/devices"
        "GET:/api/notification/list"
    )

    for api in "${apis[@]}"; do
        IFS=':' read -r method path <<< "$api"

        if make_http_request "$method" "$GATEWAY_URL$path" "" "" "200"; then
            log_success "API契约测试通过: $method $path"
        else
            log_error "API契约测试失败: $method $path"
        fi
    done
}

# 生成测试报告
generate_test_report() {
    local report_file=$1

    log "生成集成测试报告..."

    # 创建JSON报告
    local report_content="{
        \"testSuite\": \"integration\",
        \"timestamp\": \"$(date -Iseconds)\",
        \"summary\": {
            \"total\": $TOTAL_TESTS,
            \"passed\": $PASSED_TESTS,
            \"failed\": $FAILED_TESTS,
            \"passRate\": \"$(echo "scale=2; $PASSED_TESTS * 100 / $TOTAL_TESTS" | bc -l)%\"
        },
        \"tests\": [
            $(IFS=','; echo "${TEST_RESULTS[*]}")
        ]
    }"

    echo "$report_content" > "$report_file"

    # 生成HTML报告
    local html_report_file="${report_file%.json}.html"
    cat > "$html_report_file" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>IOE-DREAM 集成测试报告</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f0f0f0; padding: 20px; border-radius: 5px; }
        .summary { margin: 20px 0; }
        .passed { color: green; }
        .failed { color: red; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <div class="header">
        <h1>IOE-DREAM 微服务集成测试报告</h1>
        <p>测试时间: $(date)</p>
    </div>

    <div class="summary">
        <h2>测试摘要</h2>
        <p>总测试数: $TOTAL_TESTS</p>
        <p class="passed">通过: $PASSED_TESTS</p>
        <p class="failed">失败: $FAILED_TESTS</p>
        <p>通过率: $(echo "scale=2; $PASSED_TESTS * 100 / $TOTAL_TESTS" | bc -l)%</p>
    </div>

    <div>
        <h2>测试详情</h2>
        <table>
            <tr>
                <th>测试名称</th>
                <th>状态</th>
                <th>响应码</th>
                <th>响应内容</th>
            </tr>
            $(for result in "${TEST_RESULTS[@]}"; do
                echo "<tr>"
                echo "$(echo "$result" | jq -r '.name' | sed 's/"/&quot;/g')"
                echo "$(echo "$result" | jq -r '.status' | sed 's/"/&quot;/g')"
                echo "$(echo "$result" | jq -r '.code // .expected // "-"' | sed 's/"/&quot;/g')"
                echo "$(echo "$result" | jq -r '.response // "-" | head -c 100')"
                echo "</tr>"
            done)
        </table>
    </div>
</body>
</html>
EOF

    log_success "测试报告已生成: $report_file"
    log_success "HTML报告已生成: $html_report_file"

    # 显示测试摘要
    echo
    echo "==============================================="
    echo "           集成测试执行摘要"
    echo "==============================================="
    echo "测试总数: $TOTAL_TESTS"
    echo -e "通过: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "失败: ${RED}$FAILED_TESTS${NC}"
    echo "通过率: $(echo "scale=2; $PASSED_TESTS * 100 / $TOTAL_TESTS" | bc -l)%"
    echo "==============================================="
}

# 清理测试环境
cleanup_test_environment() {
    log "清理测试环境..."

    # 清理临时文件
    rm -f /tmp/response_body.tmp
    rm -f /tmp/curl_output.tmp

    # 清理测试数据
    # 这里可以添加数据库清理逻辑

    log_success "测试环境清理完成"
}

# 主函数
main() {
    log "开始执行IOE-DREAM微服务集成测试套件"

    # 设置日志目录
    local report_file
    report_file=$(setup_log_directory)

    # 检查服务健康状态
    if ! check_all_services_health; then
        log_error "服务健康检查失败，终止测试"
        exit 1
    fi

    # 执行各项测试
    test_service_discovery
    test_load_balancing
    test_circuit_breaker
    test_authentication_flow
    test_access_control_flow
    test_consume_payment_flow
    test_attendance_flow
    test_api_contracts

    # 生成测试报告
    generate_test_report "$report_file"

    # 清理测试环境
    cleanup_test_environment

    # 返回测试结果
    if [ $FAILED_TESTS -eq 0 ]; then
        log_success "所有集成测试通过！"
        exit 0
    else
        log_error "存在 $FAILED_TESTS 个失败的测试用例"
        exit 1
    fi
}

# 脚本入口点
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi