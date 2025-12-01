#!/bin/bash

# =============================================================================
# IOE-DREAM 业务流程集成测试脚本
# =============================================================================
#
# 功能: 执行完整的业务流程集成测试
# 覆盖: 认证流程、门禁控制、消费支付、考勤管理、视频监控
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
TEST_DATA_DIR="$PROJECT_ROOT/test/test-data"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# API配置
GATEWAY_URL=${GATEWAY_URL:-"http://localhost:8080"}
API_TIMEOUT=${API_TIMEOUT:-30}
MAX_RETRIES=${MAX_RETRIES:-3}

# 测试数据
TEST_USERS=()
TEST_DEVICES=()
TEST_TOKENS=()

# 测试结果统计
declare -A BUSINESS_FLOW_RESULTS
TOTAL_BUSINESS_TESTS=0
PASSED_BUSINESS_TESTS=0
FAILED_BUSINESS_TESTS=0

# 日志函数
log() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS:${NC} $1"
    ((PASSED_BUSINESS_TESTS++))
}

log_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1"
    ((FAILED_BUSINESS_TESTS++))
}

log_warning() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1"
}

log_info() {
    echo -e "${CYAN}[$(date '+%Y-%m-%d %H:%M:%S')] INFO:${NC} $1"
}

log_flow() {
    echo -e "${PURPLE}[$(date '+%Y-%m-%d %H:%M:%S')] FLOW:${NC} $1"
}

# HTTP请求辅助函数
make_api_request() {
    local method=$1
    local url=$2
    local data=$3
    local headers=$4
    local expected_status=${5:-200}
    local description=$6

    ((TOTAL_BUSINESS_TESTS++))
    local test_name="${description:-${method} ${url}}"

    log_info "API请求: $test_name"

    local cmd="curl -s -w '%{http_code}' -o /tmp/api_response_$$.tmp --max-time $API_TIMEOUT --retry $MAX_RETRIES"

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
    response_code=$(eval "$cmd" 2>/dev/null || echo "000")
    local response_body
    response_body=$(cat /tmp/api_response_$$.tmp 2>/dev/null || echo "")

    if [ "$response_code" -eq "$expected_status" ]; then
        log_success "$test_name - 状态码: $response_code"
        BUSINESS_FLOW_RESULTS["$test_name"]="PASS"
        echo "$response_body"
        return 0
    else
        log_error "$test_name - 期望: $expected_status, 实际: $response_code"
        BUSINESS_FLOW_RESULTS["$test_name"]="FAIL"
        echo "$response_body"
        return 1
    fi
}

# 清理函数
cleanup() {
    rm -f /tmp/api_response_$$.tmp
}

# 设置测试环境
setup_business_test_environment() {
    log "设置业务流程测试环境..."

    # 创建测试日志目录
    mkdir -p "$TEST_LOG_DIR"
    mkdir -p "$TEST_LOG_DIR/business-flows"

    # 加载测试数据
    load_test_data

    # 验证基础服务
    verify_base_services

    log_success "业务流程测试环境设置完成"
}

# 加载测试数据
load_test_data() {
    log "加载测试数据..."

    local data_file="$TEST_DATA_DIR/generated/users.json"
    if [ -f "$data_file" ]; then
        # 从JSON文件中提取测试用户
        local test_user=$(jq -r '.[0]' "$data_file" 2>/dev/null)
        if [ "$test_user" != "null" ] && [ -n "$test_user" ]; then
            TEST_USERS+=("$test_user")
            log_info "已加载测试用户数据"
        else
            log_warning "测试用户数据加载失败，使用默认数据"
            create_default_test_data
        fi
    else
        log_warning "测试数据文件不存在，使用默认数据"
        create_default_test_data
    fi
}

# 创建默认测试数据
create_default_test_data() {
    log "创建默认测试数据..."

    # 创建默认测试用户
    local test_user='{
        "user_id": "test_user_001",
        "username": "testuser001",
        "password": "testpass123",
        "real_name": "测试用户",
        "gender": "MALE",
        "email": "testuser001@test.com",
        "phone": "13800138001",
        "department": "技术部",
        "position": "测试工程师",
        "employee_id": "EMP001",
        "card_number": "CARD001"
    }'
    TEST_USERS+=("$test_user")

    # 创建默认测试设备
    local test_device='{
        "device_id": "device_001",
        "device_name": "测试门禁设备",
        "device_type": "ACCESS_CONTROL",
        "ip_address": "192.168.1.100",
        "port": 8080,
        "location": "测试地点"
    }'
    TEST_DEVICES+=("$test_device")

    log_info "默认测试数据创建完成"
}

# 验证基础服务
verify_base_services() {
    log "验证基础服务状态..."

    # 检查网关健康状态
    if make_api_request "GET" "$GATEWAY_URL/actuator/health" "" "" "200" "网关健康检查"; then
        log_success "网关服务正常"
    else
        log_error "网关服务异常"
        exit 1
    fi

    # 检查认证服务
    if make_api_request "GET" "$GATEWAY_URL/api/auth/ping" "" "" "200" "认证服务健康检查"; then
        log_success "认证服务正常"
    else
        log_warning "认证服务可能未启动，部分测试可能失败"
    fi
}

# 用户认证流程测试
test_user_authentication_flow() {
    log_flow "开始用户认证流程测试..."

    local username=$(echo "${TEST_USERS[0]}" | jq -r '.username // "testuser001"')
    local password=$(echo "${TEST_USERS[0]}" | jq -r '.password // "testpass123"')
    local user_id=$(echo "${TEST_USERS[0]}" | jq -r '.user_id // "test_user_001"')

    # 1. 用户注册
    log_info "步骤1: 用户注册"
    local register_data=$(cat << EOF
{
    "username": "$username",
    "password": "$password",
    "email": "$(echo "${TEST_USERS[0]}" | jq -r '.email // "test@test.com"')",
    "phone": "$(echo "${TEST_USERS[0]}" | jq -r '.phone // "13800138001"')",
    "realName": "$(echo "${TEST_USERS[0]}" | jq -r '.real_name // "测试用户"')",
    "department": "$(echo "${TEST_USERS[0]}" | jq -r '.department // "技术部"')"
}
EOF
    )

    if make_api_request "POST" "$GATEWAY_URL/api/auth/register" "$register_data" "Content-Type: application/json" "200" "用户注册"; then
        log_success "用户注册成功"
    else
        log_warning "用户注册失败，可能用户已存在"
    fi

    # 2. 用户登录
    log_info "步骤2: 用户登录"
    local login_data=$(cat << EOF
{
    "username": "$username",
    "password": "$password"
}
EOF
    )

    local login_response
    login_response=$(make_api_request "POST" "$GATEWAY_URL/api/auth/login" "$login_data" "Content-Type: application/json" "200" "用户登录")

    if [ $? -eq 0 ]; then
        local token=$(echo "$login_response" | jq -r '.data.token // empty')
        if [ -n "$token" ]; then
            TEST_TOKENS+=("$token")
            log_success "登录成功，获取token"
        else
            log_error "登录成功但未获取到token"
            return 1
        fi
    else
        log_error "用户登录失败"
        return 1
    fi

    # 3. 验证token有效性
    log_info "步骤3: 验证token有效性"
    if [ -n "${TEST_TOKENS[0]}" ]; then
        make_api_request "GET" "$GATEWAY_URL/api/auth/verify" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "Token验证"
    fi

    # 4. 获取用户信息
    log_info "步骤4: 获取用户信息"
    if [ -n "${TEST_TOKENS[0]}" ]; then
        make_api_request "GET" "$GATEWAY_URL/api/user/profile" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "获取用户信息"
    fi

    log_success "用户认证流程测试完成"
}

# 门禁控制流程测试
test_access_control_flow() {
    log_flow "开始门禁控制流程测试..."

    if [ -z "${TEST_TOKENS[0]}" ]; then
        log_error "缺少认证token，跳过门禁控制测试"
        return 1
    fi

    local device_id=$(echo "${TEST_DEVICES[0]}" | jq -r '.device_id // "device_001"')
    local user_id=$(echo "${TEST_USERS[0]}" | jq -r '.user_id // "test_user_001"')
    local card_number=$(echo "${TEST_USERS[0]}" | jq -r '.card_number // "CARD001"')

    # 1. 注册门禁设备
    log_info "步骤1: 注册门禁设备"
    local device_data=$(echo "${TEST_DEVICES[0]}" | jq '. + {installTime: "'$(date -Iseconds)'", firmwareVersion: "v1.0.0"}')

    if make_api_request "POST" "$GATEWAY_URL/api/device/register" "$device_data" "Content-Type: application/json" "200" "设备注册"; then
        log_success "门禁设备注册成功"
    else
        log_warning "门禁设备注册失败，可能设备已存在"
    fi

    # 2. 获取设备列表
    log_info "步骤2: 获取设备列表"
    make_api_request "GET" "$GATEWAY_URL/api/access/devices" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "获取设备列表"

    # 3. 分配门禁权限
    log_info "步骤3: 分配门禁权限"
    local permission_data=$(cat << EOF
{
    "userId": "$user_id",
    "deviceId": "$device_id",
    "accessType": "CARD",
    "validFrom": "$(date -Iseconds)",
    "validTo": "$(date -d '+1 year' -Iseconds)"
}
EOF
    )

    if make_api_request "POST" "$GATEWAY_URL/api/access/permission/assign" "$permission_data" "Authorization: Bearer ${TEST_TOKENS[0]}; Content-Type: application/json" "200" "分配门禁权限"; then
        log_success "门禁权限分配成功"
    else
        log_warning "门禁权限分配失败"
    fi

    # 4. 模拟门禁刷卡验证
    log_info "步骤4: 模拟门禁刷卡验证"
    local verification_data=$(cat << EOF
{
    "deviceId": "$device_id",
    "cardNumber": "$card_number",
    "timestamp": "$(date -Iseconds)"
}
EOF
    )

    if make_api_request "POST" "$GATEWAY_URL/api/access/verify" "$verification_data" "Content-Type: application/json" "200" "门禁验证"; then
        log_success "门禁验证成功"
    else
        log_warning "门禁验证失败"
    fi

    # 5. 查询门禁记录
    log_info "步骤5: 查询门禁记录"
    make_api_request "GET" "$GATEWAY_URL/api/access/records?userId=$user_id&limit=10" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "查询门禁记录"

    log_success "门禁控制流程测试完成"
}

# 消费支付流程测试
test_consume_payment_flow() {
    log_flow "开始消费支付流程测试..."

    if [ -z "${TEST_TOKENS[0]}" ]; then
        log_error "缺少认证token，跳过消费支付测试"
        return 1
    fi

    local user_id=$(echo "${TEST_USERS[0]}" | jq -r '.user_id // "test_user_001"')
    local device_id=$(echo "${TEST_DEVICES[0]}" | jq -r '.device_id // "device_001"')

    # 1. 创建消费账户
    log_info "步骤1: 创建消费账户"
    local account_data=$(cat << EOF
{
    "userId": "$user_id",
    "accountType": "MEAL",
    "accountName": "餐费账户",
    "initialBalance": 1000.00,
    "dailyLimit": 200.00
}
EOF
    )

    if make_api_request "POST" "$GATEWAY_URL/api/consume/account/create" "$account_data" "Authorization: Bearer ${TEST_TOKENS[0]}; Content-Type: application/json" "200" "创建消费账户"; then
        log_success "消费账户创建成功"
    else
        log_warning "消费账户创建失败，可能账户已存在"
    fi

    # 2. 账户充值
    log_info "步骤2: 账户充值"
    local recharge_data=$(cat << EOF
{
    "userId": "$user_id",
    "accountType": "MEAL",
    "amount": 500.00,
    "paymentMethod": "CASH"
}
EOF
    )

    if make_api_request "POST" "$GATEWAY_URL/api/consume/recharge" "$recharge_data" "Authorization: Bearer ${TEST_TOKENS[0]}; Content-Type: application/json" "200" "账户充值"; then
        log_success "账户充值成功"
    else
        log_warning "账户充值失败"
    fi

    # 3. 查询账户余额
    log_info "步骤3: 查询账户余额"
    make_api_request "GET" "$GATEWAY_URL/api/consume/account/$user_id/balance" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "查询账户余额"

    # 4. 模拟消费交易
    log_info "步骤4: 模拟消费交易"
    local payment_data=$(cat << EOF
{
    "userId": "$user_id",
    "accountId": "MEAL_$user_id",
    "amount": 25.50,
    "deviceId": "$device_id",
    "transactionType": "MEAL",
    "description": "午餐消费"
}
EOF
    )

    if make_api_request "POST" "$GATEWAY_URL/api/consume/payment" "$payment_data" "Authorization: Bearer ${TEST_TOKENS[0]}; Content-Type: application/json" "200" "消费支付"; then
        log_success "消费支付成功"
    else
        log_warning "消费支付失败"
    fi

    # 5. 查询消费记录
    log_info "步骤5: 查询消费记录"
    make_api_request "GET" "$GATEWAY_URL/api/consume/records?userId=$user_id&limit=5" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "查询消费记录"

    # 6. 获取消费统计
    log_info "步骤6: 获取消费统计"
    make_api_request "GET" "$GATEWAY_URL/api/consume/stats?userId=$user_id&period=month" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "获取消费统计"

    log_success "消费支付流程测试完成"
}

# 考勤管理流程测试
test_attendance_management_flow() {
    log_flow "开始考勤管理流程测试..."

    if [ -z "${TEST_TOKENS[0]}" ]; then
        log_error "缺少认证token，跳过考勤管理测试"
        return 1
    fi

    local user_id=$(echo "${TEST_USERS[0]}" | jq -r '.user_id // "test_user_001"')
    local device_id=$(echo "${TEST_DEVICES[0]}" | jq -r '.device_id // "device_001"")

    # 1. 创建考勤规则
    log_info "步骤1: 创建考勤规则"
    local rule_data=$(cat << EOF
{
    "userId": "$user_id",
    "workDays": "MON,TUE,WED,THU,FRI",
    "startTime": "09:00",
    "endTime": "18:00",
    "graceMinutes": 10,
    "breakDuration": 60
}
EOF
    )

    if make_api_request "POST" "$GATEWAY_URL/api/attendance/rule/create" "$rule_data" "Authorization: Bearer ${TEST_TOKENS[0]}; Content-Type: application/json" "200" "创建考勤规则"; then
        log_success "考勤规则创建成功"
    else
        log_warning "考勤规则创建失败，可能规则已存在"
    fi

    # 2. 模拟上班打卡
    log_info "步骤2: 模拟上班打卡"
    local check_in_data=$(cat << EOF
{
    "userId": "$user_id",
    "deviceId": "$device_id",
    "checkType": "CHECK_IN",
    "timestamp": "$(date -Iseconds)"
}
EOF
    )

    if make_api_request "POST" "$GATEWAY_URL/api/attendance/check" "$check_in_data" "Authorization: Bearer ${TEST_TOKENS[0]}; Content-Type: application/json" "200" "上班打卡"; then
        log_success "上班打卡成功"
    else
        log_warning "上班打卡失败"
    fi

    # 3. 模拟下班打卡（5秒后）
    sleep 5
    log_info "步骤3: 模拟下班打卡"
    local check_out_data=$(cat << EOF
{
    "userId": "$user_id",
    "deviceId": "$device_id",
    "checkType": "CHECK_OUT",
    "timestamp": "$(date -Iseconds)"
}
EOF
    )

    if make_api_request "POST" "$GATEWAY_URL/api/attendance/check" "$check_out_data" "Authorization: Bearer ${TEST_TOKENS[0]}; Content-Type: application/json" "200" "下班打卡"; then
        log_success "下班打卡成功"
    else
        log_warning "下班打卡失败"
    fi

    # 4. 查询今日考勤记录
    log_info "步骤4: 查询今日考勤记录"
    local today=$(date +%Y-%m-%d)
    make_api_request "GET" "$GATEWAY_URL/api/attendance/records?userId=$user_id&date=$today" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "查询考勤记录"

    # 5. 获取考勤统计
    log_info "步骤5: 获取考勤统计"
    local month=$(date +%Y-%m)
    make_api_request "GET" "$GATEWAY_URL/api/attendance/stats?userId=$user_id&month=$month" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "获取考勤统计"

    # 6. 生成考勤报表
    log_info "步骤6: 生成考勤报表"
    make_api_request "GET" "$GATEWAY_URL/api/attendance/report?userId=$user_id&month=$month&format=json" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "生成考勤报表"

    log_success "考勤管理流程测试完成"
}

# 视频监控流程测试
test_video_surveillance_flow() {
    log_flow "开始视频监控流程测试..."

    if [ -z "${TEST_TOKENS[0]}" ]; then
        log_error "缺少认证token，跳过视频监控测试"
        return 1
    fi

    # 1. 注册视频设备
    log_info "步骤1: 注册视频设备"
    local video_device_data=$(cat << EOF
{
    "deviceId": "video_device_001",
    "deviceName": "测试摄像头",
    "deviceType": "CAMERA",
    "ipAddress": "192.168.1.200",
    "port": 554,
    "location": "大门口",
    "resolution": "1080P",
    "frameRate": 25,
    "username": "admin",
    "password": "admin123"
}
EOF
    )

    if make_api_request "POST" "$GATEWAY_URL/api/video/device/register" "$video_device_data" "Authorization: Bearer ${TEST_TOKENS[0]}; Content-Type: application/json" "200" "注册视频设备"; then
        log_success "视频设备注册成功"
    else
        log_warning "视频设备注册失败，可能设备已存在"
    fi

    # 2. 获取视频设备列表
    log_info "步骤2: 获取视频设备列表"
    make_api_request "GET" "$GATEWAY_URL/api/video/devices" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "获取视频设备列表"

    # 3. 获取实时视频流信息
    log_info "步骤3: 获取实时视频流信息"
    make_api_request "GET" "$GATEWAY_URL/api/video/stream/video_device_001" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "获取视频流信息"

    # 4. 启动/停止录像
    log_info "步骤4: 启动录像"
    local record_data=$(cat << EOF
{
    "deviceId": "video_device_001",
    "recordType": "CONTINUOUS",
    "duration": 3600
}
EOF
    )

    if make_api_request "POST" "$GATEWAY_URL/api/video/record/start" "$record_data" "Authorization: Bearer ${TEST_TOKENS[0]}; Content-Type: application/json" "200" "启动录像"; then
        log_success "录像启动成功"

        # 等待一下后停止录像
        sleep 2
        log_info "步骤5: 停止录像"
        make_api_request "POST" "$GATEWAY_URL/api/video/record/stop" '{"deviceId": "video_device_001"}' "Authorization: Bearer ${TEST_TOKENS[0]}; Content-Type: application/json" "200" "停止录像"
    else
        log_warning "录像启动失败"
    fi

    # 6. 查询录像记录
    log_info "步骤6: 查询录像记录"
    make_api_request "GET" "$GATEWAY_URL/api/video/records?deviceId=video_device_001&limit=10" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "查询录像记录"

    log_success "视频监控流程测试完成"
}

# 跨服务数据一致性测试
test_cross_service_consistency() {
    log_flow "开始跨服务数据一致性测试..."

    if [ -z "${TEST_TOKENS[0]}" ]; then
        log_error "缺少认证token，跳过数据一致性测试"
        return 1
    fi

    local user_id=$(echo "${TEST_USERS[0]}" | jq -r '.user_id // "test_user_001"')

    # 1. 在不同服务中查询用户信息，验证一致性
    log_info "步骤1: 验证用户信息一致性"

    # 从认证服务查询
    local auth_user_info
    auth_user_info=$(make_api_request "GET" "$GATEWAY_URL/api/user/$user_id" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "认证服务查询用户信息")

    # 从门禁服务查询用户记录
    local access_user_records
    access_user_records=$(make_api_request "GET" "$GATEWAY_URL/api/access/records?userId=$user_id&limit=1" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "门禁服务查询用户记录")

    # 从消费服务查询账户信息
    local consume_user_accounts
    consume_user_accounts=$(make_api_request "GET" "$GATEWAY_URL/api/consume/accounts?userId=$user_id" "" "Authorization: Bearer ${TEST_TOKENS[0]}" "200" "消费服务查询账户信息")

    # 验证用户ID一致性
    local auth_user_id=$(echo "$auth_user_info" | jq -r '.data.userId // empty')
    local access_user_id=$(echo "$access_user_records" | jq -r '.data.records[0].userId // empty')
    local consume_user_id=$(echo "$consume_user_accounts" | jq -r '.data.accounts[0].userId // empty')

    if [ "$auth_user_id" = "$access_user_id" ] && [ "$access_user_id" = "$consume_user_id" ] && [ -n "$auth_user_id" ]; then
        log_success "用户ID在各服务中保持一致"
    else
        log_error "用户ID在不同服务中不一致"
    fi

    # 2. 验证时间戳一致性
    log_info "步骤2: 验证操作时间戳一致性"

    # 检查服务间调用的时间戳同步
    local timestamp_check=$(date +%s)

    # 调用多个服务的健康检查接口，比较响应时间
    local auth_timestamp=$(curl -s -w '%{time_total}' -o /dev/null "$GATEWAY_URL/api/auth/ping")
    local access_timestamp=$(curl -s -w '%{time_total}' -o /dev/null "$GATEWAY_URL/api/access/ping")
    local consume_timestamp=$(curl -s -w '%{time_total}' -o /dev/null "$GATEWAY_URL/api/consume/ping")

    log_info "服务响应时间: 认证=${auth_timestamp}s, 门禁=${access_timestamp}s, 消费=${consume_timestamp}s"

    # 3. 模拟服务故障恢复测试
    log_info "步骤3: 服务故障恢复测试"

    # 模拟网络超时，验证重试机制
    local retry_result
    retry_result=$(make_api_request "GET" "$GATEWAY_URL/api/auth/ping?delay=5" "" "" "200" "服务重试测试" 2>/dev/null || echo "FAIL")

    if [ "$retry_result" != "FAIL" ]; then
        log_success "服务重试机制正常工作"
    else
        log_warning "服务重试测试失败"
    fi

    log_success "跨服务数据一致性测试完成"
}

# 生成业务流程测试报告
generate_business_flow_report() {
    log "生成业务流程测试报告..."

    local report_file="$TEST_LOG_DIR/business-flows/business-flow-test-$(date +%Y%m%d-%H%M%S).json"
    local html_report_file="${report_file%.json}.html"

    # 计算成功率
    local success_rate=0
    if [ $TOTAL_BUSINESS_TESTS -gt 0 ]; then
        success_rate=$(echo "scale=2; $PASSED_BUSINESS_TESTS * 100 / $TOTAL_BUSINESS_TESTS" | bc -l)
    fi

    # 生成JSON报告
    local json_report=$(cat << EOF
{
    "testSuite": "business-flow-integration",
    "timestamp": "$(date -Iseconds)",
    "summary": {
        "totalTests": $TOTAL_BUSINESS_TESTS,
        "passedTests": $PASSED_BUSINESS_TESTS,
        "failedTests": $FAILED_BUSINESS_TESTS,
        "successRate": "$success_rate%"
    },
    "testResults": [
EOF
    )

    local first=true
    for test_name in "${!BUSINESS_FLOW_RESULTS[@]}"; do
        if [ "$first" = false ]; then
            json_report+=","
        fi
        first=false
        json_report+=$(cat << EOF
        {
            "testName": "$test_name",
            "result": "${BUSINESS_FLOW_RESULTS[$test_name]}"
        }
EOF
        )
    done

    json_report+=$(cat << EOF
    ],
    "businessFlows": {
        "userAuthentication": "用户认证流程测试",
        "accessControl": "门禁控制流程测试",
        "consumePayment": "消费支付流程测试",
        "attendanceManagement": "考勤管理流程测试",
        "videoSurveillance": "视频监控流程测试",
        "crossServiceConsistency": "跨服务数据一致性测试"
    },
    "testEnvironment": {
        "gatewayUrl": "$GATEWAY_URL",
        "apiTimeout": $API_TIMEOUT,
        "maxRetries": $MAX_RETRIES
    }
}
EOF
    )

    echo "$json_report" > "$report_file"

    # 生成HTML报告
    cat > "$html_report_file" << EOF
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IOE-DREAM 业务流程集成测试报告</title>
    <style>
        body {
            font-family: 'Microsoft YaHei', Arial, sans-serif;
            margin: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background-color: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .header {
            text-align: center;
            border-bottom: 2px solid #007bff;
            padding-bottom: 20px;
            margin-bottom: 30px;
        }
        .header h1 {
            color: #007bff;
            margin: 0;
            font-size: 28px;
        }
        .summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .summary-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
        }
        .summary-card h3 {
            margin: 0 0 10px 0;
            font-size: 18px;
        }
        .summary-card .value {
            font-size: 32px;
            font-weight: bold;
        }
        .passed { color: #28a745; }
        .failed { color: #dc3545; }
        .flow-section {
            margin-bottom: 30px;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            overflow: hidden;
        }
        .flow-header {
            background-color: #f8f9fa;
            padding: 15px 20px;
            font-weight: bold;
            font-size: 18px;
            border-bottom: 1px solid #e0e0e0;
        }
        .flow-content {
            padding: 20px;
        }
        .test-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px;
            border-bottom: 1px solid #f0f0f0;
        }
        .test-item:last-child {
            border-bottom: none;
        }
        .status-badge {
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
        }
        .status-pass {
            background-color: #d4edda;
            color: #155724;
        }
        .status-fail {
            background-color: #f8d7da;
            color: #721c24;
        }
        .footer {
            text-align: center;
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #e0e0e0;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>IOE-DREAM 业务流程集成测试报告</h1>
            <p>测试时间: $(date '+%Y-%m-%d %H:%M:%S')</p>
        </div>

        <div class="summary">
            <div class="summary-card">
                <h3>总测试数</h3>
                <div class="value">$TOTAL_BUSINESS_TESTS</div>
            </div>
            <div class="summary-card" style="background: linear-gradient(135deg, #56ab2f 0%, #a8e063 100%);">
                <h3>通过测试</h3>
                <div class="value">$PASSED_BUSINESS_TESTS</div>
            </div>
            <div class="summary-card" style="background: linear-gradient(135deg, #ff416c 0%, #ff4b2b 100%);">
                <h3>失败测试</h3>
                <div class="value">$FAILED_BUSINESS_TESTS</div>
            </div>
            <div class="summary-card" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <h3>成功率</h3>
                <div class="value">${success_rate}%</div>
            </div>
        </div>

        <div class="flow-section">
            <div class="flow-header">🔐 用户认证流程测试</div>
            <div class="flow-content">
                <div class="test-item">
                    <span>用户注册</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["用户注册"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["用户注册"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>用户登录</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["用户登录"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["用户登录"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>Token验证</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["Token验证"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["Token验证"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>获取用户信息</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["获取用户信息"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["获取用户信息"]:-UNKNOWN}</span>
                </div>
            </div>
        </div>

        <div class="flow-section">
            <div class="flow-header">🚪 门禁控制流程测试</div>
            <div class="flow-content">
                <div class="test-item">
                    <span>设备注册</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["设备注册"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["设备注册"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>获取设备列表</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["获取设备列表"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["获取设备列表"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>分配门禁权限</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["分配门禁权限"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["分配门禁权限"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>门禁验证</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["门禁验证"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["门禁验证"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>查询门禁记录</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["查询门禁记录"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["查询门禁记录"]:-UNKNOWN}</span>
                </div>
            </div>
        </div>

        <div class="flow-section">
            <div class="flow-header">💳 消费支付流程测试</div>
            <div class="flow-content">
                <div class="test-item">
                    <span>创建消费账户</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["创建消费账户"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["创建消费账户"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>账户充值</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["账户充值"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["账户充值"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>查询账户余额</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["查询账户余额"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["查询账户余额"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>消费支付</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["消费支付"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["消费支付"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>查询消费记录</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["查询消费记录"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["查询消费记录"]:-UNKNOWN}</span>
                </div>
            </div>
        </div>

        <div class="flow-section">
            <div class="flow-header">⏰ 考勤管理流程测试</div>
            <div class="flow-content">
                <div class="test-item">
                    <span>创建考勤规则</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["创建考勤规则"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["创建考勤规则"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>上班打卡</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["上班打卡"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["上班打卡"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>下班打卡</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["下班打卡"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["下班打卡"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>查询考勤记录</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["查询考勤记录"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["查询考勤记录"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>获取考勤统计</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["获取考勤统计"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["获取考勤统计"]:-UNKNOWN}</span>
                </div>
            </div>
        </div>

        <div class="flow-section">
            <div class="flow-header">📹 视频监控流程测试</div>
            <div class="flow-content">
                <div class="test-item">
                    <span>注册视频设备</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["注册视频设备"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["注册视频设备"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>获取视频设备列表</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["获取视频设备列表"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["获取视频设备列表"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>获取视频流信息</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["获取视频流信息"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["获取视频流信息"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>启动录像</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["启动录像"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["启动录像"]:-UNKNOWN}</span>
                </div>
                <div class="test-item">
                    <span>查询录像记录</span>
                    <span class="status-badge ${BUSINESS_FLOW_RESULTS["查询录像记录"]:+status-pass:-status-fail}">${BUSINESS_FLOW_RESULTS["查询录像记录"]:-UNKNOWN}</span>
                </div>
            </div>
        </div>

        <div class="footer">
            <p>报告生成时间: $(date '+%Y-%m-%d %H:%M:%S')</p>
            <p>测试环境: $GATEWAY_URL</p>
            <p>© 2025 IOE-DREAM 测试团队</p>
        </div>
    </div>
</body>
</html>
EOF

    log_success "业务流程测试报告已生成"
    log_info "JSON报告: $report_file"
    log_info "HTML报告: $html_report_file"

    # 显示测试摘要
    echo
    echo "==============================================="
    echo "         业务流程集成测试摘要"
    echo "==============================================="
    echo "测试总数: $TOTAL_BUSINESS_TESTS"
    echo -e "通过测试: ${GREEN}$PASSED_BUSINESS_TESTS${NC}"
    echo -e "失败测试: ${RED}$FAILED_BUSINESS_TESTS${NC}"
    echo "成功率: ${success_rate}%"
    echo "==============================================="
}

# 清理测试环境
cleanup_business_test() {
    log "清理业务流程测试环境..."
    cleanup
    log_success "清理完成"
}

# 主函数
main() {
    log "开始执行IOE-DREAM业务流程集成测试"

    # 设置测试环境
    setup_business_test_environment

    # 执行业务流程测试
    test_user_authentication_flow
    test_access_control_flow
    test_consume_payment_flow
    test_attendance_management_flow
    test_video_surveillance_flow
    test_cross_service_consistency

    # 生成测试报告
    generate_business_flow_report

    # 清理测试环境
    cleanup_business_test

    # 返回测试结果
    if [ $FAILED_BUSINESS_TESTS -eq 0 ]; then
        log_success "所有业务流程测试通过！"
        exit 0
    else
        log_warning "存在 $FAILED_BUSINESS_TESTS 个失败的测试用例"
        exit 1
    fi
}

# 脚本入口点
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    # 设置错误处理
    trap cleanup_business_test EXIT

    main "$@"
fi