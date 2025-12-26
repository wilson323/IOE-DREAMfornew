#!/bin/bash

# =====================================================
# 智能排班引擎 - API联调测试脚本
# 用途：快速测试后端API接口是否正常工作
# =====================================================

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
BASE_URL="http://localhost:8091"
CONTENT_TYPE="Content-Type: application/json"

echo -e "${GREEN}========================================${NC}"
echo -e "  智能排班引擎 - API联调测试"
echo -e "${GREEN}========================================${NC}"
echo ""

# 测试统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 测试结果记录
declare -a FAILED_TESTS=()

# =====================================================
# 辅助函数
# =====================================================

# 打印测试标题
print_test_title() {
    echo -e "\n${BLUE}[$((TOTAL_TESTS + 1))] $1${NC}"
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
}

# 打印测试结果
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ 通过: $2${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ 失败: $2${NC}"
        FAILED_TESTS+=("$2")
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# 执行GET请求
test_get() {
    local endpoint=$1
    local description=$2

    print_test_title "$description"

    response=$(curl -s -w "\n%{http_code}" -X GET \
        -H "$CONTENT_TYPE" \
        "$BASE_URL$endpoint")

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "200" ]; then
        print_result 0 "$description (HTTP $http_code)"
        echo -e "${GREEN}响应数据:${NC} $body" | head -c 200
        echo "..."
    else
        print_result 1 "$description (HTTP $http_code)"
        echo -e "${RED}错误响应:${NC} $body"
    fi
}

# 执行POST请求
test_post() {
    local endpoint=$1
    local data=$2
    local description=$3

    print_test_title "$description"

    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "$CONTENT_TYPE" \
        -d "$data" \
        "$BASE_URL$endpoint")

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ]; then
        print_result 0 "$description (HTTP $http_code)"
        echo -e "${GREEN}响应数据:${NC} $body" | head -c 200
        echo "..."
    else
        print_result 1 "$description (HTTP $http_code)"
        echo -e "${RED}错误响应:${NC} $body"
    fi
}

# =====================================================
# 开始测试
# =====================================================

echo -e "${YELLOW}测试配置:${NC}"
echo -e "后端服务地址: $BASE_URL"
echo ""

# 检查服务是否启动
echo -e "${BLUE}[0/26] 检查后端服务状态...${NC}"
health_check=$(curl -s -w "\n%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null)
health_code=$(echo "$health_check" | tail -n1)

if [ "$health_code" = "200" ]; then
    echo -e "${GREEN}✓ 后端服务运行正常${NC}"
else
    echo -e "${RED}✗ 后端服务未启动或不可访问 (HTTP $health_code)${NC}"
    echo -e "${YELLOW}请先启动后端服务:${NC}"
    echo -e "  cd microservices/ioedream-attendance-service"
    echo -e "  mvn spring-boot:run"
    exit 1
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}开始API接口测试${NC}"
echo -e "${GREEN}========================================${NC}"

# =====================================================
# SmartScheduleController API测试
# =====================================================

echo ""
echo -e "${BLUE}--- SmartScheduleController (12个接口) ---${NC}"

# 1. 查询计划列表
test_get "/api/v1/schedule/plan/page?pageNum=1&pageSize=10" \
    "查询计划列表"

# 2. 创建排班计划
test_post "/api/v1/schedule/plan" \
    '{
      "planName": "API测试计划",
      "description": "集成测试用排班计划",
      "startDate": "2025-01-27",
      "endDate": "2025-02-02",
      "employeeIds": [1, 2, 3],
      "shiftIds": [1, 2, 3],
      "optimizationGoal": 5,
      "maxConsecutiveWorkDays": 7,
      "minRestDays": 2,
      "minDailyStaff": 2,
      "fairnessWeight": 0.4,
      "costWeight": 0.3,
      "efficiencyWeight": 0.2,
      "satisfactionWeight": 0.1,
      "algorithmType": 4,
      "populationSize": 20,
      "maxGenerations": 50,
      "crossoverRate": 0.8,
      "mutationRate": 0.1
    }' \
    "创建排班计划"

# 保存创建的计划ID（假设为1）
PLAN_ID=1

# 3. 查询计划详情
test_get "/api/v1/schedule/plan/$PLAN_ID" \
    "查询计划详情"

# 4. 执行优化
test_post "/api/v1/schedule/plan/$PLAN_ID/execute" \
    '{}' \
    "执行排班优化"

# 5. 查询排班结果
test_get "/api/v1/schedule/result/page?planId=$PLAN_ID&pageNum=1&pageSize=20" \
    "查询排班结果分页"

# 6. 查询结果列表
test_get "/api/v1/schedule/result/list/$PLAN_ID" \
    "查询排班结果列表"

# 7. 确认计划
test_post "/api/v1/schedule/plan/$PLAN_ID/confirm" \
    '{}' \
    "确认排班计划"

# =====================================================
# ScheduleRuleController API测试
# =====================================================

echo ""
echo -e "${BLUE}--- ScheduleRuleController (14个接口) ---${NC}"

# 8. 查询规则列表
test_get "/api/v1/schedule/rule/page?pageNum=1&pageSize=10" \
    "查询规则列表"

# 9. 查询规则列表（不分页）
test_get "/api/v1/schedule/rule/list" \
    "查询规则列表（不分页）"

# 10. 验证规则表达式
test_post "/api/v1/schedule/rule/validate?expression=consecutiveWorkDays+%3E+3" \
    '{}' \
    "验证规则表达式"

# 11. 获取规则优先级
test_get "/api/v1/schedule/rule/priority" \
    "获取规则优先级"

# 12. 测试规则执行
test_post "/api/v1/schedule/rule/test" \
    '{
      "expression": "consecutiveWorkDays >= 3",
      "context": {
        "employeeId": 1,
        "date": "2025-01-30",
        "shiftId": 1,
        "consecutiveWorkDays": 5
      }
    }' \
    "测试规则执行"

# =====================================================
# 优化算法API测试
# =====================================================

echo ""
echo -e "${BLUE}--- 优化算法API (4个接口) ---${NC}"

# 13. 遗传算法优化
test_post "/api/v1/schedule/optimize/genetic" \
    '{
      "employeeIds": [1, 2, 3],
      "startDate": "2025-01-27",
      "endDate": "2025-02-02",
      "shiftIds": [1, 2, 3]
    }' \
    "遗传算法优化"

# 14. 模拟退火优化
test_post "/api/v1/schedule/optimize/annealing" \
    '{
      "employeeIds": [1, 2, 3],
      "startDate": "2025-01-27",
      "endDate": "2025-02-02",
      "shiftIds": [1, 2, 3]
    }' \
    "模拟退火优化"

# 15. 混合算法优化
test_post "/api/v1/schedule/optimize/hybrid" \
    '{
      "employeeIds": [1, 2, 3],
      "startDate": "2025-01-27",
      "endDate": "2025-02-02",
      "shiftIds": [1, 2, 3]
    }' \
    "混合算法优化"

# 16. 自动算法选择
test_post "/api/v1/schedule/optimize/auto" \
    '{
      "employeeIds": [1, 2, 3],
      "startDate": "2025-01-27",
      "endDate": "2025-02-02",
      "shiftIds": [1, 2, 3]
    }' \
    "自动算法选择"

# =====================================================
# 测试总结
# =====================================================

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}测试总结${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "总测试数: $TOTAL_TESTS"
echo -e "${GREEN}通过: $PASSED_TESTS${NC}"
if [ $FAILED_TESTS -gt 0 ]; then
    echo -e "${RED}失败: $FAILED_TESTS${NC}"
    echo ""
    echo -e "${RED}失败的测试:${NC}"
    for test in "${FAILED_TESTS[@]}"; do
        echo -e "  - $test"
    done
else
    echo -e "${RED}失败: 0${NC}"
fi
echo ""

# 计算通过率
if [ $TOTAL_TESTS -gt 0 ]; then
    pass_rate=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo -e "通过率: $pass_rate%"
    echo ""

    if [ $pass_rate -ge 95 ]; then
        echo -e "${GREEN}✓ API联调测试通过！可以进行下一步测试。${NC}"
    elif [ $pass_rate -ge 80 ]; then
        echo -e "${YELLOW}⚠ API联调测试基本通过，但仍有部分问题需要修复。${NC}"
    else
        echo -e "${RED}✗ API联调测试未通过，请修复问题后重试。${NC}"
    fi
fi

echo ""
echo -e "${GREEN}========================================${NC}"
