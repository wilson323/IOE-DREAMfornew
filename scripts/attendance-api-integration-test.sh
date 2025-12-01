#!/bin/bash

# 考勤模块API集成测试脚本
# 用于验证前后端接口的正确性和数据传输

echo "🚀 开始考勤模块API集成测试..."

# 设置环境变量
export BASE_URL="http://localhost:1024"
export API_BASE="${BASE_URL}/api"

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 日志文件
LOG_FILE="attendance-api-test-$(date +%Y%m%d_%H%M%S).log"

# 日志记录函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# 测试函数
test_api() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_status="$5"

    ((TOTAL_TESTS++))

    log "📝 测试: $test_name"
    log "🔗 请求: $method $endpoint"

    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer test-token" \
            -d "$data" \
            "${API_BASE}${endpoint}")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer test-token" \
            "${API_BASE}${endpoint}")
    fi

    # 分离响应体和状态码
    http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    log "📊 响应状态码: $http_code"
    log "📄 响应体: $response_body"

    if [ "$http_code" -eq "$expected_status" ]; then
        ((PASSED_TESTS++))
        log "✅ 测试通过: $test_name"
    else
        ((FAILED_TESTS++))
        log "❌ 测试失败: $test_name - 期望状态码 $expected_status，实际 $http_code"
    fi

    echo "----------------------------------------"
    echo ""
}

# 1. 考勤打卡接口测试
echo "🔥 开始考勤打卡接口测试..."

test_api "获取今日打卡记录" "GET" "/attendance/today-punch" "" 200

test_api "上班打卡" "POST" "/attendance/punch-in" '{
    "employeeId": 1,
    "punchType": "上班",
    "punchTime": "2025-11-17 09:00:00",
    "latitude": 39.9042,
    "longitude": 116.4074,
    "deviceId": "MOBILE_001",
    "location": "北京市朝阳区",
    "remark": "正常上班打卡"
}' 200

test_api "下班打卡" "POST" "/attendance/punch-out" '{
    "employeeId": 1,
    "punchType": "下班",
    "punchTime": "2025-11-17 18:00:00",
    "latitude": 39.9042,
    "longitude": 116.4074,
    "deviceId": "MOBILE_001",
    "location": "北京市朝阳区",
    "remark": "正常下班打卡"
}' 200

# 2. 考勤记录查询接口测试
echo "🔥 开始考勤记录查询接口测试..."

test_api "查询考勤记录列表" "GET" "/attendance/records?pageNum=1&pageSize=10" "" 200

test_api "获取考勤记录详情" "GET" "/attendance/record/1" "" 200

test_api "获取指定日期打卡记录" "GET" "/attendance/punch-record/2025-11-17" "" 200

# 3. 考勤统计接口测试
echo "🔥 开始考勤统计接口测试..."

test_api "获取个人考勤统计" "GET" "/attendance/personal-statistics?employeeId=1&startDate=2025-11-01&endDate=2025-11-17" "" 200

test_api "获取部门考勤统计" "GET" "/attendance/department-statistics?departmentId=1&startDate=2025-11-01&endDate=2025-11-17" "" 200

test_api "获取考勤趋势数据" "GET" "/attendance/trends?startDate=2025-11-01&endDate=2025-11-17" "" 200

test_api "获取考勤日历数据" "GET" "/attendance/calendar?year=2025&month=11&employeeId=1" "" 200

# 4. 排班管理接口测试
echo "🔥 开始排班管理接口测试..."

test_api "获取员工排班" "GET" "/attendance/schedule/employee?employeeId=1&startDate=2025-11-01&endDate=2025-11-17" "" 200

test_api "获取部门排班" "GET" "/attendance/schedule/department?departmentId=1&startDate=2025-11-01&endDate=2025-11-17" "" 200

test_api "创建排班" "POST" "/attendance/schedule" '{
    "employeeId": 1,
    "scheduleDate": "2025-11-18",
    "scheduleType": "FIXED",
    "workStartTime": "09:00",
    "workEndTime": "18:00",
    "breakStartTime": "12:00",
    "breakEndTime": "13:00",
    "remark": "固定排班"
}' 200

test_api "检查排班冲突" "POST" "/attendance/schedule/check-conflict" '{
    "employeeId": 1,
    "scheduleDate": "2025-11-18",
    "workStartTime": "09:00",
    "workEndTime": "18:00"
}' 200

# 5. 考勤规则接口测试
echo "🔥 开始考勤规则接口测试..."

test_api "获取考勤规则列表" "GET" "/attendance/rules?pageNum=1&pageSize=10" "" 200

test_api "获取员工适用考勤规则" "GET" "/attendance/rules/employee/1" "" 200

test_api "创建考勤规则" "POST" "/attendance/rules" '{
    "ruleName": "标准工作制",
    "ruleCode": "STANDARD_WORK",
    "workStartTime": "09:00",
    "workEndTime": "18:00",
    "breakStartTime": "12:00",
    "breakEndTime": "13:00",
    "lateGraceMinutes": 5,
    "earlyLeaveGraceMinutes": 5,
    "scheduleType": "FIXED",
    "enabled": true,
    "priority": 1
}' 200

# 6. 异常管理接口测试
echo "🔥 开始异常管理接口测试..."

test_api "获取异常考勤记录" "GET" "/attendance/abnormal-records?pageNum=1&pageSize=10" "" 200

test_api "获取异常统计数据" "GET" "/attendance/abnormal-statistics?startDate=2025-11-01&endDate=2025-11-17" "" 200

test_api "处理异常记录" "POST" "/attendance/handle-abnormal" '{
    "recordId": 1,
    "handleType": "APPROVED",
    "handleRemark": "特殊情况已批准"
}' 200

# 7. 补卡申请接口测试
echo "🔥 开始补卡申请接口测试..."

test_api "提交补卡申请" "POST" "/attendance/punch-correction" '{
    "employeeId": 1,
    "correctionDate": "2025-11-16",
    "correctionType": "PUNCH_IN",
    "correctionTime": "09:00",
    "correctionReason": "忘记打卡"
}' 200

test_api "获取补卡申请列表" "GET" "/attendance/punch-corrections?pageNum=1&pageSize=10" "" 200

# 8. 报表接口测试
echo "🔥 开始报表接口测试..."

test_api "生成考勤报表" "POST" "/attendance/reports/generate" '{
    "reportType": "monthly",
    "startDate": "2025-11-01",
    "endDate": "2025-11-30",
    "employeeIds": [1, 2, 3]
}' 200

# 9. 设置接口测试
echo "🔥 开始设置接口测试..."

test_api "获取考勤设置" "GET" "/attendance/settings" "" 200

test_api "获取考勤设备配置" "GET" "/attendance/settings/device-config" "" 200

# 10. 移动端API测试
echo "🔥 开始移动端API测试..."

test_api "移动端获取今日考勤信息" "GET" "/attendance/mobile/today-info" "" 200

test_api "移动端快速打卡" "POST" "/attendance/mobile/quick-punch" '{
    "employeeId": 1,
    "punchType": "上班",
    "latitude": 39.9042,
    "longitude": 116.4074,
    "deviceId": "MOBILE_001"
}' 200

test_api "移动端获取历史记录" "GET" "/attendance/mobile/history?pageNum=1&pageSize=10" "" 200

test_api "获取移动端考勤统计" "GET" "/attendance/mobile/statistics?startDate=2025-11-01&endDate=2025-11-17" "" 200

# 11. 实时数据API测试
echo "🔥 开始实时数据API测试..."

test_api "获取实时考勤状态" "GET" "/attendance/realtime/status" "" 200

test_api "获取实时考勤统计" "GET" "/attendance/realtime/statistics" "" 200

test_api "获取正在打卡的员工列表" "GET" "/attendance/realtime/active-employees" "" 200

# 输出测试结果
echo ""
echo "🎉 API集成测试完成！"
echo "📊 测试统计:"
echo "   总测试数: $TOTAL_TESTS"
echo "   通过测试: $PASSED_TESTS"
echo "   失败测试: $FAILED_TESTS"

if [ $FAILED_TESTS -eq 0 ]; then
    echo "🎉 所有测试通过！"
    exit 0
else
    echo "⚠️  有 $FAILED_TESTS 个测试失败，请检查日志文件: $LOG_FILE"
    exit 1
fi