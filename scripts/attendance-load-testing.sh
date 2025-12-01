#!/bin/bash

# 考勤模块压力测试脚本
# 使用Artillery进行API压力测试

echo "🚀 开始考勤模块压力测试..."

# 设置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
TEST_RESULTS_DIR="$PROJECT_ROOT/test-results/load-test"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="$TEST_RESULTS_DIR/load-test-report-$TIMESTAMP.md"

# 创建测试结果目录
mkdir -p "$TEST_RESULTS_DIR"

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# 检查Artillery是否安装
if ! command -v artillery &> /dev/null; then
    log "❌ Artillery未安装，正在安装..."
    npm install -g artillery
    if ! command -v artillery &> /dev/null; then
        log "❌ Artillery安装失败"
        exit 1
    fi
    log "✅ Artillery安装成功"
fi

# 创建压力测试配置文件
cat > "$TEST_RESULTS_DIR/attendance-load-test.yaml" << 'EOF'
config:
  target: "http://localhost:1024"
  phases:
    - duration: 60
      arrivalRate: 5
    - duration: 120
      arrivalRate: 10
    - duration: 60
      arrivalRate: 15
  defaults:
    headers:
      Content-Type: "application/json"
      Authorization: "Bearer test-token"

scenarios:
  - name: "考勤打卡场景"
    flow:
      - get:
          url: "/api/attendance/today-punch"
      - post:
          url: "/api/attendance/punch-in"
          json:
            employeeId: "{{ random(1, 1000) }}"
            punchTime: "{{ now('YYYY-MM-DD HH:mm:ss') }}"
            location: "测试地点"
      - get:
          url: "/api/attendance/records"
          qs:
            employeeId: "{{ random(1, 1000) }}"
            startDate: "{{ now('YYYY-MM-DD') }}"
            endDate: "{{ now('YYYY-MM-DD') }}"

  - name: "排班查询场景"
    flow:
      - get:
          url: "/api/attendance/schedule"
          qs:
            employeeId: "{{ random(1, 1000) }}"
            date: "{{ now('YYYY-MM-DD') }}"
      - get:
          url: "/api/attendance/schedule/month"
          qs:
            employeeId: "{{ random(1, 1000) }}"
            year: "{{ now('YYYY') }}"
            month: "{{ now('MM') }}"

  - name: "统计查询场景"
    flow:
      - get:
          url: "/api/attendance/statistics/personal"
          qs:
            employeeId: "{{ random(1, 1000) }}"
            startDate: "{{ now('YYYY-MM-01') }}"
            endDate: "{{ now('YYYY-MM-DD') }}"
      - get:
          url: "/api/attendance/statistics/department"
          qs:
            departmentId: "{{ random(1, 50) }}"
            startDate: "{{ now('YYYY-MM-01') }}"
            endDate: "{{ now('YYYY-MM-DD') }}"
EOF

# 执行压力测试
log "🔥 执行压力测试..."
artillery run "$TEST_RESULTS_DIR/attendance-load-test.yaml" \
    --output "$TEST_RESULTS_DIR/load-test-results-$TIMESTAMP.json"

# 生成测试报告
log "📊 生成测试报告..."

# 从测试结果中提取关键指标
if [ -f "$TEST_RESULTS_DIR/load-test-results-$TIMESTAMP.json" ]; then
    # 使用jq提取关键指标（如果安装了jq）
    if command -v jq &> /dev/null; then
        total_requests=$(jq -r '.aggregate.counters."vusers.created" // 0' "$TEST_RESULTS_DIR/load-test-results-$TIMESTAMP.json")
        failed_requests=$(jq -r '.aggregate.counters."errors" // 0' "$TEST_RESULTS_DIR/load-test-results-$TIMESTAMP.json")
        avg_response_time=$(jq -r '.aggregate.latency.mean // 0' "$TEST_RESULTS_DIR/load-test-results-$TIMESTAMP.json")
        max_response_time=$(jq -r '.aggregate.latency.max // 0' "$TEST_RESULTS_DIR/load-test-results-$TIMESTAMP.json")
        min_response_time=$(jq -r '.aggregate.latency.min // 0' "$TEST_RESULTS_DIR/load-test-results-$TIMESTAMP.json")
        percentile_95=$(jq -r '.aggregate.latency.p95 // 0' "$TEST_RESULTS_DIR/load-test-results-$TIMESTAMP.json")
        percentile_99=$(jq -r '.aggregate.latency.p99 // 0' "$TEST_RESULTS_DIR/load-test-results-$TIMESTAMP.json")
        rps=$(jq -r '.aggregate.rates."http.request_rate" // 0' "$TEST_RESULTS_DIR/load-test-results-$TIMESTAMP.json")
    else
        # 如果没有jq，使用基本的文本处理
        total_requests=0
        failed_requests=0
        avg_response_time=0
        max_response_time=0
        min_response_time=0
        percentile_95=0
        percentile_99=0
        rps=0
    fi
else
    total_requests=0
    failed_requests=0
    avg_response_time=0
    max_response_time=0
    min_response_time=0
    percentile_95=0
    percentile_99=0
    rps=0
fi

# 生成Markdown报告
cat > "$REPORT_FILE" << EOF
# 考勤模块压力测试报告

**测试时间**: $(date '+%Y-%m-%d %H:%M:%S')
**测试环境**: 本地开发环境
**测试工具**: Artillery

---

## 测试概述

本次压力测试模拟了考勤系统的典型使用场景，包括：
- 考勤打卡场景
- 排班查询场景
- 统计查询场景

测试持续时间：240秒
测试阶段：
1. 第1阶段：60秒，每秒5个请求
2. 第2阶段：120秒，每秒10个请求
3. 第3阶段：60秒，每秒15个请求

## 测试结果

### 请求统计
- **总请求数**: $total_requests
- **失败请求数**: $failed_requests
- **成功率**: $([ $total_requests -gt 0 ] && echo "scale=2; (1 - $failed_requests / $total_requests) * 100" | bc || echo "0")%

### 响应时间统计 (毫秒)
- **平均响应时间**: $avg_response_time ms
- **最小响应时间**: $min_response_time ms
- **最大响应时间**: $max_response_time ms
- **95%响应时间**: $percentile_95 ms
- **99%响应时间**: $percentile_99 ms

### 吞吐量统计
- **平均请求率**: $rps 请求/秒

## 性能分析

### 性能等级
$(
if [ $avg_response_time -lt 100 ] && [ $percentile_95 -lt 200 ]; then
    echo "✅ 优秀 - 系统性能良好"
elif [ $avg_response_time -lt 300 ] && [ $percentile_95 -lt 500 ]; then
    echo "⚠️ 良好 - 系统性能可接受"
elif [ $avg_response_time -lt 500 ] && [ $percentile_95 -lt 1000 ]; then
    echo "⚠️ 一般 - 建议优化系统性能"
else
    echo "❌ 较差 - 系统性能需要优化"
fi
)

### 性能建议
$(
if [ $avg_response_time -gt 500 ]; then
    echo "- ⚠️ 平均响应时间较长，建议优化数据库查询和API处理逻辑"
fi
if [ $percentile_95 -gt 1000 ]; then
    echo "- ⚠️ 95%响应时间较长，建议检查系统瓶颈"
fi
if [ $failed_requests -gt 0 ]; then
    echo "- ⚠️ 存在失败请求，建议检查错误处理和系统稳定性"
fi
if [ $rps -lt 10 ]; then
    echo "- ⚠️ 吞吐量较低，建议优化系统并发处理能力"
fi
if [ $avg_response_time -lt 100 ] && [ $percentile_95 -lt 200 ] && [ $failed_requests -eq 0 ]; then
    echo "- ✅ 系统性能优秀，可以满足生产环境需求"
fi
)

## 详细测试数据

测试结果文件已保存到: $TEST_RESULTS_DIR/load-test-results-$TIMESTAMP.json

## 后续建议

1. **持续监控**: 在生产环境中持续监控系统性能
2. **容量规划**: 根据测试结果规划系统容量
3. **性能优化**: 针对发现的性能瓶颈进行优化
4. **定期测试**: 定期执行压力测试确保系统稳定性

---
**报告生成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**测试脚本版本**: v1.0.0
EOF

# 输出结果
log "📄 测试报告已生成: $REPORT_FILE"
log "📊 测试完成时间: $(date '+%Y-%m-%d %H:%M:%S')"

# 显示简要结果
echo ""
echo "=== 压力测试结果摘要 ==="
echo "总请求数: $total_requests"
echo "失败请求数: $failed_requests"
echo "平均响应时间: $avg_response_time ms"
echo "95%响应时间: $percentile_95 ms"
echo "请求率: $rps 请求/秒"
echo "========================"

if [ $failed_requests -eq 0 ]; then
    log "🎉 压力测试完成，所有测试通过！"
    exit 0
else
    log "⚠️ 压力测试完成，存在失败请求，请查看报告进行分析。"
    exit 1
fi