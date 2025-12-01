#!/bin/bash

# ===================================================================
# IOE-DREAM 微服务API调用测试脚本
# 功能：全面测试微服务的API端点、认证授权、业务逻辑等
# 支持自动化测试、压力测试、API文档验证等
#
# 使用方法:
#   ./api-testing.sh [test|auth|business|stress|doc] [service_name]
#
# 参数说明:
#   test    - 执行全面的API功能测试 (默认)
#   auth    - 测试认证授权相关API
#   business- 测试业务功能API
#   stress  - 执行API压力测试
#   doc     - 验证API文档完整性
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

# API测试配置
GATEWAY_URL="http://localhost:8080"
AUTH_URL="http://localhost:8081"
IDENTITY_URL="http://localhost:8082"
TEST_TIMEOUT=10
MAX_RETRIES=3

# 测试统计数据
declare -A API_TEST_RESULTS=()
declare -A API_RESPONSE_TIMES=()
declare -A API_STATUS_CODES=()

# 认证令牌
AUTH_TOKEN=""
REFRESH_TOKEN=""

# 测试数据
TEST_USER='{"username":"test_user","password":"Test123456","email":"test@ioedream.com"}'
TEST_DEVICE='{"deviceName":"测试设备","deviceType":"ACCESS_CONTROL","location":"办公楼一楼"}'

# 日志记录函数
log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "[$timestamp] [$level] $message" | tee -a "$LOG_DIR/api-testing.log"

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

# 通用API调用函数
call_api() {
    local method=$1
    local url=$2
    local data=$3
    local headers=$4
    local timeout=$5

    local curl_cmd="curl -s -w '%{http_code}|%{time_total}' -X $method"

    if [ -n "$timeout" ]; then
        curl_cmd+=" --connect-timeout $timeout --max-time $timeout"
    else
        curl_cmd+=" --connect-timeout $TEST_TIMEOUT --max-time $TEST_TIMEOUT"
    fi

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

# 解析API响应
parse_api_response() {
    local response=$1
    local var_name=$2

    local response_code=$(echo "$response" | cut -d'|' -f1)
    local response_time=$(echo "$response" | cut -d'|' -f2)
    local response_body=$(echo "$response" | cut -d'|' -f3-)

    eval "${var_name}_CODE='$response_code'"
    eval "${var_name}_TIME='$response_time'"
    eval "${var_name}_BODY='$response_body'"
}

# 记录API测试结果
record_api_test() {
    local api_name=$1
    local response_code=$2
    local response_time=$3
    local success=$4
    local details=$5

    API_TEST_RESULTS[$api_name]="$success"
    API_RESPONSE_TIMES[$api_name]="$response_time"
    API_STATUS_CODES[$api_name]="$response_code"

    local status_color=$GREEN
    local status_text="通过"

    if [ "$success" = "false" ]; then
        status_color=$RED
        status_text="失败"
    fi

    echo -e "  $api_name: ${status_color}$status_text${NC} (${response_code} ${response_time}s)"
    if [ -n "$details" ]; then
        echo -e "    详情: $details"
    fi
}

# 测试服务可用性
test_service_availability() {
    print_section "🔍 测试服务可用性"

    local services=(
        "smart-gateway:8080"
        "ioedream-auth-service:8081"
        "ioedream-identity-service:8082"
        "ioedream-device-service:8083"
        "ioedream-access-service:8084"
        "ioedream-consume-service:8085"
        "ioedream-attendance-service:8086"
        "ioedream-video-service:8087"
        "ioedream-oa-service:8088"
        "ioedream-system-service:8089"
        "ioedream-monitor-service:8090"
    )

    for service_info in "${services[@]}"; do
        IFS=':' read -r service port <<< "$service_info"
        local health_url="http://localhost:$port/actuator/health"

        local response=$(call_api "GET" "$health_url" "" "" "5")
        parse_api_response "$response" "health"

        if [ "$health_CODE" = "200" ]; then
            record_api_test "$service-health" "$health_CODE" "$health_TIME" "true" "服务健康"
        else
            record_api_test "$service-health" "$health_CODE" "$health_TIME" "false" "服务不可用"
        fi
    done
}

# 测试认证相关API
test_authentication_apis() {
    print_section "🔐 测试认证相关API"

    echo -e "${BLUE}1. 测试用户登录${NC}"
    local login_data='{"username":"admin","password":"admin123"}'
    local response=$(call_api "POST" "$AUTH_URL/api/auth/login" "$login_data" "" "15")
    parse_api_response "$response" "login"

    if [ "$login_CODE" = "200" ] || [ "$login_CODE" = "201" ]; then
        record_api_test "用户登录" "$login_CODE" "$login_TIME" "true" "登录成功"

        # 提取认证令牌
        AUTH_TOKEN=$(echo "$login_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        if [ -n "$AUTH_TOKEN" ]; then
            log "INFO" "成功获取认证令牌"
        fi
    else
        record_api_test "用户登录" "$login_CODE" "$login_TIME" "false" "登录失败: $login_BODY"
    fi

    echo -e "\n${BLUE}2. 测试用户注册${NC}"
    local response=$(call_api "POST" "$AUTH_URL/api/auth/register" "$TEST_USER" "" "15")
    parse_api_response "$response" "register"

    if [ "$register_CODE" = "200" ] || [ "$register_CODE" = "201" ] || [ "$register_CODE" = "409" ]; then
        record_api_test "用户注册" "$register_CODE" "$register_TIME" "true" "注册成功或用户已存在"
    else
        record_api_test "用户注册" "$register_CODE" "$register_TIME" "false" "注册失败: $register_BODY"
    fi

    echo -e "\n${BLUE}3. 测试令牌刷新${NC}"
    if [ -n "$AUTH_TOKEN" ]; then
        local refresh_data='{"refreshToken":"test_refresh_token"}'
        local response=$(call_api "POST" "$AUTH_URL/api/auth/refresh" "$refresh_data" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
        parse_api_response "$response" "refresh"

        if [ "$refresh_CODE" = "200" ]; then
            record_api_test "令牌刷新" "$refresh_CODE" "$refresh_TIME" "true" "刷新成功"
        else
            record_api_test "令牌刷新" "$refresh_CODE" "$refresh_TIME" "false" "刷新失败"
        fi
    else
        record_api_test "令牌刷新" "000" "0" "false" "缺少认证令牌"
    fi

    echo -e "\n${BLUE}4. 测试登出${NC}"
    if [ -n "$AUTH_TOKEN" ]; then
        local response=$(call_api "POST" "$AUTH_URL/api/auth/logout" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
        parse_api_response "$response" "logout"

        if [ "$logout_CODE" = "200" ]; then
            record_api_test "用户登出" "$logout_CODE" "$logout_TIME" "true" "登出成功"
        else
            record_api_test "用户登出" "$logout_CODE" "$logout_TIME" "false" "登出失败"
        fi
    else
        record_api_test "用户登出" "000" "0" "false" "缺少认证令牌"
    fi
}

# 测试身份权限API
test_identity_apis() {
    print_section "👥 测试身份权限API"

    echo -e "${BLUE}1. 测试获取用户列表${NC}"
    local response=$(call_api "GET" "$IDENTITY_URL/api/identity/users" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "users"

    if [ "$users_CODE" = "200" ]; then
        local user_count=$(echo "$users_BODY" | grep -o '"total":[0-9]*' | cut -d':' -f2)
        record_api_test "获取用户列表" "$users_CODE" "$users_TIME" "true" "共${user_count:-0}个用户"
    else
        record_api_test "获取用户列表" "$users_CODE" "$users_TIME" "false" "权限不足或服务异常"
    fi

    echo -e "\n${BLUE}2. 测试获取角色列表${NC}"
    local response=$(call_api "GET" "$IDENTITY_URL/api/identity/roles" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "roles"

    if [ "$roles_CODE" = "200" ]; then
        record_api_test "获取角色列表" "$roles_CODE" "$roles_TIME" "true" "角色列表获取成功"
    else
        record_api_test "获取角色列表" "$roles_CODE" "$roles_TIME" "false" "角色列表获取失败"
    fi

    echo -e "\n${BLUE}3. 测试获取权限列表${NC}"
    local response=$(call_api "GET" "$IDENTITY_URL/api/identity/permissions" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "permissions"

    if [ "$permissions_CODE" = "200" ]; then
        record_api_test "获取权限列表" "$permissions_CODE" "$permissions_TIME" "true" "权限列表获取成功"
    else
        record_api_test "获取权限列表" "$permissions_CODE" "$permissions_TIME" "false" "权限列表获取失败"
    fi
}

# 测试设备管理API
test_device_apis() {
    print_section "🔧 测试设备管理API"

    local device_url="http://localhost:8083"

    echo -e "${BLUE}1. 测试获取设备列表${NC}"
    local response=$(call_api "GET" "$device_url/api/device/list" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "devices"

    if [ "$devices_CODE" = "200" ]; then
        record_api_test "获取设备列表" "$devices_CODE" "$devices_TIME" "true" "设备列表获取成功"
    else
        record_api_test "获取设备列表" "$devices_CODE" "$devices_TIME" "false" "设备列表获取失败"
    fi

    echo -e "\n${BLUE}2. 测试添加设备${NC}"
    local response=$(call_api "POST" "$device_url/api/device/add" "$TEST_DEVICE" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "add_device"

    if [ "$add_device_CODE" = "200" ] || [ "$add_device_CODE" = "201" ]; then
        record_api_test "添加设备" "$add_device_CODE" "$add_device_TIME" "true" "设备添加成功"
    else
        record_api_test "添加设备" "$add_device_CODE" "$add_device_TIME" "false" "设备添加失败"
    fi

    echo -e "\n${BLUE}3. 测试设备状态查询${NC}"
    local response=$(call_api "GET" "$device_url/api/device/status" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "device_status"

    if [ "$device_status_CODE" = "200" ]; then
        record_api_test "设备状态查询" "$device_status_CODE" "$device_status_TIME" "true" "设备状态获取成功"
    else
        record_api_test "设备状态查询" "$device_status_CODE" "$device_status_TIME" "false" "设备状态获取失败"
    fi
}

# 测试门禁管理API
test_access_apis() {
    print_section "🚪 测试门禁管理API"

    local access_url="http://localhost:8084"

    echo -e "${BLUE}1. 测试获取门禁记录${NC}"
    local response=$(call_api "GET" "$access_url/api/access/records" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "access_records"

    if [ "$access_records_CODE" = "200" ]; then
        record_api_test "获取门禁记录" "$access_records_CODE" "$access_records_TIME" "true" "门禁记录获取成功"
    else
        record_api_test "获取门禁记录" "$access_records_CODE" "$access_records_TIME" "false" "门禁记录获取失败"
    fi

    echo -e "\n${BLUE}2. 测试门禁设备控制${NC}"
    local control_data='{"deviceId":"test_device","action":"open","location":"办公楼一楼"}'
    local response=$(call_api "POST" "$access_url/api/access/control" "$control_data" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "access_control"

    if [ "$access_control_CODE" = "200" ] || [ "$access_control_CODE" = "404" ]; then
        record_api_test "门禁设备控制" "$access_control_CODE" "$access_control_TIME" "true" "控制指令发送成功或设备不存在"
    else
        record_api_test "门禁设备控制" "$access_control_CODE" "$access_control_TIME" "false" "门禁控制失败"
    fi

    echo -e "\n${BLUE}3. 测试访客管理${NC}"
    local visitor_data='{"visitorName":"测试访客","visitTime":"2024-01-01 10:00:00","purpose":"商务洽谈"}'
    local response=$(call_api "POST" "$access_url/api/access/visitor/register" "$visitor_data" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "visitor_register"

    if [ "$visitor_register_CODE" = "200" ] || [ "$visitor_register_CODE" = "201" ]; then
        record_api_test "访客登记" "$visitor_register_CODE" "$visitor_register_TIME" "true" "访客登记成功"
    else
        record_api_test "访客登记" "$visitor_register_CODE" "$visitor_register_TIME" "false" "访客登记失败"
    fi
}

# 测试消费管理API
test_consume_apis() {
    print_section "💰 测试消费管理API"

    local consume_url="http://localhost:8085"

    echo -e "${BLUE}1. 测试获取消费记录${NC}"
    local response=$(call_api "GET" "$consume_url/api/consume/records" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "consume_records"

    if [ "$consume_records_CODE" = "200" ]; then
        record_api_test "获取消费记录" "$consume_records_CODE" "$consume_records_TIME" "true" "消费记录获取成功"
    else
        record_api_test "获取消费记录" "$consume_records_CODE" "$consume_records_TIME" "false" "消费记录获取失败"
    fi

    echo -e "\n${BLUE}2. 测试账户余额查询${NC}"
    local response=$(call_api "GET" "$consume_url/api/consume/account/balance" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "account_balance"

    if [ "$account_balance_CODE" = "200" ]; then
        record_api_test "账户余额查询" "$account_balance_CODE" "$account_balance_TIME" "true" "余额查询成功"
    else
        record_api_test "账户余额查询" "$account_balance_CODE" "$account_balance_TIME" "false" "余额查询失败"
    fi

    echo -e "\n${BLUE}3. 测试消费统计${NC}"
    local response=$(call_api "GET" "$consume_url/api/consume/statistics" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "consume_statistics"

    if [ "$consume_statistics_CODE" = "200" ]; then
        record_api_test "消费统计" "$consume_statistics_CODE" "$consume_statistics_TIME" "true" "消费统计获取成功"
    else
        record_api_test "消费统计" "$consume_statistics_CODE" "$consume_statistics_TIME" "false" "消费统计获取失败"
    fi
}

# 测试考勤管理API
test_attendance_apis() {
    print_section "⏰ 测试考勤管理API"

    local attendance_url="http://localhost:8086"

    echo -e "${BLUE}1. 测试获取考勤记录${NC}"
    local response=$(call_api "GET" "$attendance_url/api/attendance/records" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "attendance_records"

    if [ "$attendance_records_CODE" = "200" ]; then
        record_api_test "获取考勤记录" "$attendance_records_CODE" "$attendance_records_TIME" "true" "考勤记录获取成功"
    else
        record_api_test "获取考勤记录" "$attendance_records_CODE" "$attendance_records_TIME" "false" "考勤记录获取失败"
    fi

    echo -e "\n${BLUE}2. 测试考勤统计${NC}"
    local response=$(call_api "GET" "$attendance_url/api/attendance/statistics" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "attendance_statistics"

    if [ "$attendance_statistics_CODE" = "200" ]; then
        record_api_test "考勤统计" "$attendance_statistics_CODE" "$attendance_statistics_TIME" "true" "考勤统计获取成功"
    else
        record_api_test "考勤统计" "$attendance_statistics_CODE" "$attendance_statistics_TIME" "false" "考勤统计获取失败"
    fi

    echo -e "\n${BLUE}3. 测试排班管理${NC}"
    local schedule_data='{"userId":"test_user","shiftType":"normal","workDate":"2024-01-01","startTime":"09:00","endTime":"18:00"}'
    local response=$(call_api "POST" "$attendance_url/api/attendance/schedule" "$schedule_data" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "schedule"

    if [ "$schedule_CODE" = "200" ] || [ "$schedule_CODE" = "201" ]; then
        record_api_test "排班管理" "$schedule_CODE" "$schedule_TIME" "true" "排班设置成功"
    else
        record_api_test "排班管理" "$schedule_CODE" "$schedule_TIME" "false" "排班设置失败"
    fi
}

# 测试视频监控API
test_video_apis() {
    print_section "📹 测试视频监控API"

    local video_url="http://localhost:8087"

    echo -e "${BLUE}1. 测试获取设备列表${NC}"
    local response=$(call_api "GET" "$video_url/api/video/devices" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "video_devices"

    if [ "$video_devices_CODE" = "200" ]; then
        record_api_test "获取视频设备" "$video_devices_CODE" "$video_devices_TIME" "true" "视频设备列表获取成功"
    else
        record_api_test "获取视频设备" "$video_devices_CODE" "$video_devices_TIME" "false" "视频设备列表获取失败"
    fi

    echo -e "\n${BLUE}2. 测试实时视频流${NC}"
    local response=$(call_api "GET" "$video_url/api/video/stream/live?deviceId=test_device" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "live_stream"

    if [ "$live_stream_CODE" = "200" ] || [ "$live_stream_CODE" = "404" ]; then
        record_api_test "实时视频流" "$live_stream_CODE" "$live_stream_TIME" "true" "视频流获取成功或设备不存在"
    else
        record_api_test "实时视频流" "$live_stream_CODE" "$live_stream_TIME" "false" "视频流获取失败"
    fi

    echo -e "\n${BLUE}3. 测试录像回放${NC}"
    local response=$(call_api "GET" "$video_url/api/video/playback?deviceId=test_device&startTime=2024-01-01T00:00:00&endTime=2024-01-01T23:59:59" "" "-H 'Authorization: Bearer $AUTH_TOKEN'" "15")
    parse_api_response "$response" "playback"

    if [ "$playback_CODE" = "200" ] || [ "$playback_CODE" = "404" ]; then
        record_api_test "录像回放" "$playback_CODE" "$playback_TIME" "true" "录像获取成功或无录像数据"
    else
        record_api_test "录像回放" "$playback_CODE" "$playback_TIME" "false" "录像获取失败"
    fi
}

# 测试网关路由
test_gateway_routing() {
    print_section "🌐 测试网关路由功能"

    local gateway_routes=(
        "/api/auth/health:认证服务健康检查"
        "/api/identity/health:身份服务健康检查"
        "/api/device/health:设备服务健康检查"
        "/api/access/health:门禁服务健康检查"
        "/api/consume/health:消费服务健康检查"
        "/api/attendance/health:考勤服务健康检查"
        "/api/video/health:视频服务健康检查"
        "/api/system/health:系统服务健康检查"
    )

    for route_info in "${gateway_routes[@]}"; do
        IFS=':' read -r route description <<< "$route_info"
        local url="$GATEWAY_URL$route"

        local response=$(call_api "GET" "$url" "" "" "10")
        parse_api_response "$response" "gateway_route"

        if [ "$gateway_route_CODE" = "200" ]; then
            record_api_test "网关路由-$description" "$gateway_route_CODE" "$gateway_route_TIME" "true" "路由正常"
        elif [ "$gateway_route_CODE" = "404" ]; then
            record_api_test "网关路由-$description" "$gateway_route_CODE" "$gateway_route_TIME" "true" "路由不存在，但网关正常"
        else
            record_api_test "网关路由-$description" "$gateway_route_CODE" "$gateway_route_TIME" "false" "路由异常"
        fi
    done
}

# API压力测试
run_stress_test() {
    print_section "⚡ API压力测试"

    local test_endpoints=(
        "$AUTH_URL/api/auth/login:登录接口:POST:$TEST_USER"
        "$IDENTITY_URL/api/identity/users:用户列表:GET:"
        "$GATEWAY_URL/api/auth/health:网关认证健康:GET:"
    )

    local concurrent_requests=10
    local total_requests=100

    echo -e "${BLUE}压力测试参数:${NC}"
    echo "  并发请求数: $concurrent_requests"
    echo "  总请求数: $total_requests"
    echo "  测试端点数: ${#test_endpoints[@]}"

    for endpoint_info in "${test_endpoints[@]}"; do
        IFS=':' read -r url description method data <<< "$endpoint_info"

        echo -e "\n${YELLOW}测试端点: $description${NC}"

        local successful_requests=0
        local failed_requests=0
        local total_response_time=0
        local min_response_time=999999
        local max_response_time=0

        # 执行压力测试
        for ((i=1; i<=total_requests; i++)); do
            local response
            if [ "$method" = "POST" ] && [ -n "$data" ]; then
                response=$(call_api "$method" "$url" "$data" "" "15")
            else
                response=$(call_api "$method" "$url" "" "" "15")
            fi

            local response_code=$(echo "$response" | cut -d'|' -f1)
            local response_time=$(echo "$response" | cut -d'|' -f2)

            # 移除单位，转换为数字
            response_time=$(echo "$response_time" | sed 's/[^0-9.]//g')

            if [ "$response_code" = "200" ] || [ "$response_code" = "201" ]; then
                ((successful_requests++))
            else
                ((failed_requests++))
            fi

            if [ -n "$response_time" ] && [[ "$response_time" =~ ^[0-9.]+$ ]]; then
                total_response_time=$(echo "$total_response_time + $response_time" | bc -l)
                if (( $(echo "$response_time < $min_response_time" | bc -l) )); then
                    min_response_time=$response_time
                fi
                if (( $(echo "$response_time > $max_response_time" | bc -l) )); then
                    max_response_time=$response_time
                fi
            fi

            # 进度显示
            if [ $((i % 20)) -eq 0 ]; then
                echo -n "."
            fi
        done

        echo ""

        # 计算统计数据
        local success_rate=0
        local avg_response_time=0
        if [ $successful_requests -gt 0 ]; then
            success_rate=$((successful_requests * 100 / total_requests))
            avg_response_time=$(echo "scale=3; $total_response_time / $successful_requests" | bc -l)
        fi

        echo -e "  成功请求: ${GREEN}$successful_requests${NC}/$total_requests ($success_rate%)"
        echo -e "  失败请求: ${RED}$failed_requests${NC}/$total_requests"
        echo -e "  平均响应时间: ${BLUE}${avg_response_time}s${NC}"
        echo -e "  最小响应时间: ${BLUE}${min_response_time}s${NC}"
        echo -e "  最大响应时间: ${BLUE}${max_response_time}s${NC}"

        # 记录压力测试结果
        local test_status="true"
        if [ $success_rate -lt 95 ]; then
            test_status="false"
        fi

        record_api_test "压力测试-$description" "$success_rate" "$avg_response_time" "$test_status" "成功率${success_rate}%"
    done
}

# 验证API文档
validate_api_documentation() {
    print_section "📚 验证API文档完整性"

    echo -e "${BLUE}检查Swagger/OpenAPI文档${NC}"

    local swagger_endpoints=(
        "http://localhost:8080/swagger-ui.html:网关Swagger"
        "http://localhost:8081/swagger-ui.html:认证服务Swagger"
        "http://localhost:8082/swagger-ui.html:身份服务Swagger"
        "http://localhost:8083/swagger-ui.html:设备服务Swagger"
        "http://localhost:8084/swagger-ui.html:门禁服务Swagger"
        "http://localhost:8085/swagger-ui.html:消费服务Swagger"
        "http://localhost:8086/swagger-ui.html:考勤服务Swagger"
        "http://localhost:8087/swagger-ui.html:视频服务Swagger"
        "http://localhost:8088/swagger-ui.html:OA服务Swagger"
        "http://localhost:8089/swagger-ui.html:系统服务Swagger"
    )

    local available_docs=0
    local total_docs=${#swagger_endpoints[@]}

    for doc_info in "${swagger_endpoints[@]}"; do
        IFS=':' read -r url description <<< "$doc_info"

        local response_code=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 "$url" 2>/dev/null || echo "000")

        if [ "$response_code" = "200" ]; then
            echo -e "  $description: ${GREEN}✅ 可用${NC} ($url)"
            ((available_docs++))
        else
            echo -e "  $description: ${RED}❌ 不可用${NC} (HTTP $response_code)"
        fi
    done

    local doc_coverage=$((available_docs * 100 / total_docs))
    echo -e "\n📊 API文档覆盖率: ${BLUE}${doc_coverage}%${NC} ($available_docs/$total_docs)"

    if [ $doc_coverage -ge 80 ]; then
        record_api_test "API文档验证" "$doc_coverage" "N/A" "true" "文档覆盖率良好"
    else
        record_api_test "API文档验证" "$doc_coverage" "N/A" "false" "文档覆盖率不足"
    fi

    echo -e "\n${BLUE}检查Actuator端点${NC}"

    local actuator_endpoints=(
        "http://localhost:8080/actuator:网关Actuator"
        "http://localhost:8081/actuator:认证服务Actuator"
        "http://localhost:8082/actuator:身份服务Actuator"
    )

    for actuator_info in "${actuator_endpoints[@]}"; do
        IFS=':' read -r url description <<< "$actuator_info"

        local response=$(call_api "GET" "$url/health" "" "" "5")
        parse_api_response "$response" "actuator_health"

        if [ "$actuator_health_CODE" = "200" ]; then
            echo -e "  $description: ${GREEN}✅ 健康检查可用${NC}"
        else
            echo -e "  $description: ${RED}❌ 健康检查不可用${NC}"
        fi
    done
}

# 生成API测试报告
generate_api_test_report() {
    print_section "📋 生成API测试报告"

    local report_file="$VERIFICATION_DIR/reports/api-test-report-$(date +%Y%m%d_%H%M%S).html"

    log "INFO" "生成API测试报告: $report_file"

    # 计算统计数据
    local total_tests=${#API_TEST_RESULTS[@]}
    local passed_tests=0
    local failed_tests=0

    for test_name in "${!API_TEST_RESULTS[@]}"; do
        if [ "${API_TEST_RESULTS[$test_name]}" = "true" ]; then
            ((passed_tests++))
        else
            ((failed_tests++))
        fi
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
    <title>IOE-DREAM 微服务API测试报告</title>
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
        .status.passed { background: #d4edda; color: #155724; }
        .status.failed { background: #f8d7da; color: #721c24; }
        .response-time { font-family: monospace; }
        .progress-bar { width: 100%; height: 20px; background: #e0e0e0; border-radius: 10px; overflow: hidden; margin: 20px 0; }
        .progress-fill { height: 100%; background: linear-gradient(90deg, #4CAF50, #45a049); transition: width 0.3s ease; }
        .timestamp { color: #666; font-size: 14px; margin-top: 20px; text-align: right; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1 class="title">🔧 IOE-DREAM 微服务API测试报告</h1>
            <p class="subtitle">API功能验证与性能测试 • $(date)</p>
        </div>

        <div class="summary">
            <div class="card">
                <h3>$total_tests</h3>
                <p>总测试数</p>
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

        <div class="progress-bar">
            <div class="progress-fill" style="width: ${success_rate}%"></div>
        </div>

        <div class="section">
            <h2>📋 测试结果详情</h2>
            <table>
                <thead>
                    <tr>
                        <th>测试名称</th>
                        <th>状态</th>
                        <th>响应时间</th>
                        <th>HTTP状态码</th>
                    </tr>
                </thead>
                <tbody>
EOF

    # 添加测试结果详情
    for test_name in "${!API_TEST_RESULTS[@]}"; do
        local status="${API_TEST_RESULTS[$test_name]}"
        local response_time="${API_RESPONSE_TIMES[$test_name]:-N/A}"
        local status_code="${API_STATUS_CODES[$test_name]:-N/A}"

        local status_class="failed"
        if [ "$status" = "true" ]; then
            status_class="passed"
        fi

        cat >> "$report_file" << EOF
                    <tr>
                        <td>$test_name</td>
                        <td><span class="status $status_class">$status</span></td>
                        <td class="response-time">$response_time</td>
                        <td>$status_code</td>
                    </tr>
EOF
    done

    cat >> "$report_file" << EOF
                </tbody>
            </table>
        </div>

        <div class="section">
            <h2>📊 性能指标</h2>
            <table>
                <thead>
                    <tr>
                        <th>指标名称</th>
                        <th>数值</th>
                        <th>评估</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>测试执行时间</td>
                        <td>$(date '+%H:%M:%S')</td>
                        <td>-</td>
                    </tr>
                    <tr>
                        <td>平均响应时间</td>
                        <td>-</td>
                        <td>-</td>
                    </tr>
                    <tr>
                        <td>最大响应时间</td>
                        <td>-</td>
                        <td>-</td>
                    </tr>
                    <tr>
                        <td>API可用性</td>
                        <td>${success_rate}%</td>
                        <td>$(if [ $success_rate -ge 95 ]; then echo "优秀"; elif [ $success_rate -ge 80 ]; then echo "良好"; else echo "需要改进"; fi)</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="section">
            <h2>🔗 服务端点</h2>
            <ul>
                <li><strong>API网关:</strong> <a href="http://localhost:8080" target="_blank">http://localhost:8080</a></li>
                <li><strong>认证服务:</strong> <a href="http://localhost:8081" target="_blank">http://localhost:8081</a></li>
                <li><strong>身份服务:</strong> <a href="http://localhost:8082" target="_blank">http://localhost:8082</a></li>
                <li><strong>设备服务:</strong> <a href="http://localhost:8083" target="_blank">http://localhost:8083</a></li>
                <li><strong>门禁服务:</strong> <a href="http://localhost:8084" target="_blank">http://localhost:8084</a></li>
                <li><strong>消费服务:</strong> <a href="http://localhost:8085" target="_blank">http://localhost:8085</a></li>
                <li><strong>考勤服务:</strong> <a href="http://localhost:8086" target="_blank">http://localhost:8086</a></li>
                <li><strong>视频服务:</strong> <a href="http://localhost:8087" target="_blank">http://localhost:8087</a></li>
                <li><strong>OA服务:</strong> <a href="http://localhost:8088" target="_blank">http://localhost:8088</a></li>
                <li><strong>系统服务:</strong> <a href="http://localhost:8089" target="_blank">http://localhost:8089</a></li>
            </ul>
        </div>

        <div class="timestamp">
            报告生成时间：$(date) <br>
            API测试工具版本：v1.0.0
        </div>
    </div>
</body>
</html>
EOF

    log "INFO" "API测试报告已生成: $report_file"
    echo -e "\n${GREEN}✅ API测试报告生成完成${NC}"
    echo -e "报告路径: ${BLUE}$report_file${NC}"

    return 0
}

# 显示测试结果摘要
show_test_summary() {
    print_section "📊 API测试结果摘要"

    local total_tests=${#API_TEST_RESULTS[@]}
    local passed_tests=0
    local failed_tests=0

    for test_name in "${!API_TEST_RESULTS[@]}"; do
        if [ "${API_TEST_RESULTS[$test_name]}" = "true" ]; then
            ((passed_tests++))
        else
            ((failed_tests++))
        fi
    done

    local success_rate=0
    if [ $total_tests -gt 0 ]; then
        success_rate=$((passed_tests * 100 / total_tests))
    fi

    echo -e "总测试数量: ${YELLOW}$total_tests${NC}"
    echo -e "通过测试:   ${GREEN}$passed_tests${NC}"
    echo -e "失败测试:   ${RED}$failed_tests${NC}"
    echo -e "通过率:     ${BLUE}${success_rate}%${NC}"

    if [ $success_rate -ge 95 ]; then
        echo -e "测试结果: ${GREEN}✅ 优秀${NC}"
    elif [ $success_rate -ge 80 ]; then
        echo -e "测试结果: ${YELLOW}⚠️ 良好${NC}"
    else
        echo -e "测试结果: ${RED}❌ 需要改进${NC}"
    fi

    return 0
}

# 主函数
main() {
    local command=${1:-"test"}

    case $command in
        "test")
            print_section "🚀 开始IOE-DREAM微服务API测试"
            test_service_availability
            test_authentication_apis
            test_identity_apis
            test_device_apis
            test_access_apis
            test_consume_apis
            test_attendance_apis
            test_video_apis
            test_gateway_routing
            show_test_summary
            ;;
        "auth")
            print_section "🔐 认证相关API专项测试"
            test_authentication_apis
            test_identity_apis
            show_test_summary
            ;;
        "business")
            print_section "💼 业务功能API专项测试"
            test_device_apis
            test_access_apis
            test_consume_apis
            test_attendance_apis
            test_video_apis
            show_test_summary
            ;;
        "stress")
            print_section "⚡ API压力测试"
            run_stress_test
            show_test_summary
            ;;
        "doc")
            print_section "📚 API文档验证"
            validate_api_documentation
            show_test_summary
            ;;
        "report")
            generate_api_test_report
            ;;
        "help"|"--help"|"-h")
            echo "IOE-DREAM 微服务API测试工具"
            echo ""
            echo "使用方法:"
            echo "  $0 [命令]"
            echo ""
            echo "命令:"
            echo "  test     - 执行全面的API功能测试 (默认)"
            echo "  auth     - 测试认证授权相关API"
            echo "  business - 测试业务功能API"
            echo "  stress   - 执行API压力测试"
            echo "  doc      - 验证API文档完整性"
            echo "  report   - 生成API测试报告"
            echo "  help     - 显示帮助信息"
            echo ""
            echo "示例:"
            echo "  $0 test          # 执行完整API测试"
            echo "  $0 auth          # 仅测试认证API"
            echo "  $0 stress        # 执行压力测试"
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