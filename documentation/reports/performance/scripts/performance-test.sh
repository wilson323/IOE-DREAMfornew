#!/bin/bash

# IOE-DREAM 性能测试脚本
# 支持多种性能测试工具和场景

set -e

# 配置参数
GATEWAY_URL="http://localhost:8100"
BASE_DIR=$(cd "$(dirname "$0")/.." && pwd)
REPORT_DIR="$BASE_DIR/reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 创建报告目录
mkdir -p "$REPORT_DIR"

# 检查服务状态
check_service() {
    log_info "检查服务状态..."

    if ! curl -s "$GATEWAY_URL/actuator/health" > /dev/null; then
        log_error "网关服务不可用: $GATEWAY_URL"
        exit 1
    fi

    log_info "✓ 网关服务正常运行"
}

# 预热服务
warm_up_service() {
    log_info "预热服务..."

    # 调用几个主要接口进行预热
    curl -s "$GATEWAY_URL/api/auth/health" > /dev/null
    curl -s "$GATEWAY_URL/api/device/health" > /dev/null
    curl -s "$GATEWAY_URL/api/consume/health" > /dev/null

    log_info "✓ 服务预热完成"
}

# K6 压力测试
run_k6_test() {
    local test_file="$1"
    local test_name="$2"

    if ! command -v k6 &> /dev/null; then
        log_warn "K6 未安装，跳过 K6 测试"
        return 1
    fi

    log_info "开始 K6 测试: $test_name"

    cd "$BASE_DIR/k6"

    # 运行测试
    k6 run --out json="$REPORT_DIR/k6_${test_name}_$TIMESTAMP.json" \
           --out html="$REPORT_DIR/k6_${test_name}_$TIMESTAMP.html" \
           "$test_file"

    log_info "✓ K6 测试完成: $test_name"
}

# JMeter 压力测试
run_jmeter_test() {
    local test_plan="$1"
    local test_name="$2"

    if ! command -v jmeter &> /dev/null; then
        log_warn "JMeter 未安装，跳过 JMeter 测试"
        return 1
    fi

    log_info "开始 JMeter 测试: $test_name"

    cd "$BASE_DIR/jmeter"

    # 运行测试
    jmeter -n -t "$test_plan" \
           -l "$REPORT_DIR/jmeter_${test_name}_$TIMESTAMP.jtl" \
           -e -o "$REPORT_DIR/jmeter_${test_name}_$TIMESTAMP"

    log_info "✓ JMeter 测试完成: $test_name"
}

# Apache Bench 测试
run_ab_test() {
    local url="$1"
    local requests="$2"
    local concurrency="$3"
    local test_name="$4"

    if ! command -v ab &> /dev/null; then
        log_warn "Apache Bench 未安装，跳过 AB 测试"
        return 1
    fi

    log_info "开始 Apache Bench 测试: $test_name"

    ab -n "$requests" -c "$concurrency" -g "$REPORT_DIR/ab_${test_name}_$TIMESTAMP.dat" \
       "$url" > "$REPORT_DIR/ab_${test_name}_$TIMESTAMP.txt"

    log_info "✓ Apache Bench 测试完成: $test_name"
}

# 生成性能报告
generate_report() {
    log_info "生成性能测试报告..."

    cat > "$REPORT_DIR/performance_report_$TIMESTAMP.md" << EOF
# IOE-DREAM 性能测试报告

## 测试时间
$(date)

## 测试环境
- 网关地址: $GATEWAY_URL
- 测试工具: K6, JMeter, Apache Bench

## 测试结果
$(ls -la "$REPORT_DIR" | grep "$TIMESTAMP")

## 性能指标
- 响应时间
- 吞吐量
- 错误率
- 并发用户数

## 建议和优化
1. 监控系统资源使用情况
2. 优化数据库查询
3. 增加缓存策略
4. 考虑负载均衡
5. 优化网络配置

## 详细报告
- K6 报告: file://$REPORT_DIR/k6_*_$TIMESTAMP.html
- JMeter 报告: file://$REPORT_DIR/jmeter_*_$TIMESTAMP/index.html
- 原始数据: $REPORT_DIR/
EOF

    log_info "✓ 性能报告生成完成: $REPORT_DIR/performance_report_$TIMESTAMP.md"
}

# 清理临时文件
cleanup() {
    log_info "清理临时文件..."
    # 清理逻辑可以根据需要添加
}

# 主函数
main() {
    local test_type="$1"

    echo "========================================"
    echo "IOE-DREAM 性能测试工具"
    echo "========================================"

    # 检查服务状态
    check_service

    # 预热服务
    warm_up_service

    case "$test_type" in
        "k6")
            run_k6_test "load-test.js" "load_test"
            run_k6_test "api-test.js" "api_test"
            ;;
        "jmeter")
            run_jmeter_test "ioe-dream-test-plan.jmx" "full_test"
            ;;
        "ab")
            run_ab_test "$GATEWAY_URL/api/auth/health" 1000 10 "health_check"
            run_ab_test "$GATEWAY_URL/api/device/list" 5000 50 "device_list"
            ;;
        "all")
            run_k6_test "load-test.js" "load_test"
            run_ab_test "$GATEWAY_URL/api/auth/health" 1000 10 "health_check"
            ;;
        *)
            echo "用法: $0 {k6|jmeter|ab|all}"
            echo "  k6    - 运行 K6 压力测试"
            echo "  jmeter - 运行 JMeter 压力测试"
            echo "  ab    - 运行 Apache Bench 测试"
            echo "  all   - 运行所有测试"
            exit 1
            ;;
    esac

    # 生成报告
    generate_report

    # 清理
    cleanup

    echo "========================================"
    echo "性能测试完成！"
    echo "报告目录: $REPORT_DIR"
    echo "========================================"
}

# 信号处理
trap cleanup EXIT

# 运行主函数
main "$@"