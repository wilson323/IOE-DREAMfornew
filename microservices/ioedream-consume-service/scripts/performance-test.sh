#!/bin/bash
# ============================================================
# 消费服务性能测试脚本
# ============================================================
# 功能：使用JMeter进行性能测试
# 目标：TPS ≥ 1000, 平均响应时间 ≤ 50ms
# ============================================================

set -e

# 配置变量
THREADS=100          # 并发线程数
RAMP_UP=10           # 启动时间（秒）
DURATION=300         # 测试持续时间（秒）
TARGET_TPS=1000      # 目标TPS
TARGET_RESPONSE=50   # 目标响应时间（ms）
TARGET_P95=100       # 目标P95响应时间（ms）

# 服务器配置
HOST="${HOST:-localhost}"
PORT="${PORT:-8094}"
BASE_URL="http://${HOST}:${PORT}"

# 日志目录
LOG_DIR="./performance-test-logs"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
TEST_LOG_DIR="${LOG_DIR}/${TIMESTAMP}"

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# ============================================================
# 步骤1：环境检查
# ============================================================
check_environment() {
    log_info "步骤1：检查测试环境..."

    # 检查JMeter是否安装
    if ! command -v jmeter &> /dev/null; then
        log_error "JMeter未安装！请先安装JMeter："
        echo "  Ubuntu/Debian: sudo apt-get install jmeter"
        echo "  Mac: brew install jmeter"
        echo "  Windows: 从 https://jmeter.apache.org/ 下载"
        exit 1
    fi

    log_info "✓ JMeter已安装"

    # 检查curl是否可用
    if ! command -v curl &> /dev/null; then
        log_error "curl未安装！"
        exit 1
    fi

    log_info "✓ curl已安装"

    # 检查服务是否运行
    if ! curl -s "${BASE_URL}/actuator/health" > /dev/null; then
        log_error "消费服务未运行或无法访问！URL: ${BASE_URL}"
        exit 1
    fi

    log_info "✓ 消费服务运行正常: ${BASE_URL}"

    # 创建日志目录
    mkdir -p "$TEST_LOG_DIR"

    log_info "✓ 日志目录创建成功: $TEST_LOG_DIR"
}

# ============================================================
# 步骤2：创建JMeter测试计划
# ============================================================
create_jmeter_test_plan() {
    log_info "步骤2：创建JMeter测试计划..."

    cat > "${TEST_LOG_DIR}/consume-test.jmx" <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="消费服务性能测试">
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="HOST" elementType="Argument">
            <stringProp name="Argument.name">HOST</stringProp>
            <stringProp name="Argument.value">localhost</stringProp>
          </elementProp>
          <elementProp name="PORT" elementType="Argument">
            <stringProp name="Argument.name">PORT</stringProp>
            <stringProp name="Argument.value">8094</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="消费请求线程组">
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <longProp name="ThreadGroup.duration">300</longProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.scheduler">true</stringProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="消费请求">
          <stringProp name="HTTPSampler.domain">${HOST}</stringProp>
          <stringProp name="HTTPSampler.port">${PORT}</stringProp>
          <stringProp name="HTTPSampler.path">/api/consume/transaction</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
        </HTTPSamplerProxy>
        <hashTree/>
        <ResultCollector guiclass="ViewResultsFullVisualizer" testclass="ResultCollector" testname="查看结果树">
          <boolProp name="ResultCollector.error_logging">true</boolProp>
          <objProp>
            <name>saveConfig</name>
            <value class="SampleSaveConfiguration">
              <time>true</time>
              <latency>true</latency>
              <success>true</success>
              <responseCode>true</responseCode>
            </value>
          </objProp>
          <stringProp name="filename">${TEST_LOG_DIR}/results.jtl</stringProp>
        </ResultCollector>
        <hashTree/>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
EOF

    log_info "✓ JMeter测试计划创建成功"
}

# ============================================================
# 步骤3：执行性能测试
# ============================================================
run_performance_test() {
    log_info "步骤3：执行性能测试..."
    log_info "测试参数："
    log_info "  并发线程数: $THREADS"
    log_info "  启动时间: ${RAMP_UP}秒"
    log_info "  测试时长: ${DURATION}秒"
    log_info "  目标TPS: $TARGET_TPS"
    log_info "  目标响应时间: ${TARGET_RESPONSE}ms"

    # 执行JMeter测试
    jmeter -n -t "${TEST_LOG_DIR}/consume-test.jmx" \
           -l "${TEST_LOG_DIR}/test-results.jtl" \
           -e -o "${TEST_LOG_DIR}/html-report" \
           -JHOST=$HOST -JPORT=$PORT

    log_info "✓ 性能测试完成"
}

# ============================================================
# 步骤4：分析测试结果
# ============================================================
analyze_results() {
    log_info "步骤4：分析测试结果..."

    # 检查测试结果文件是否存在
    if [ ! -f "${TEST_LOG_DIR}/test-results.jtl" ]; then
        log_error "测试结果文件不存在！"
        exit 1
    fi

    # 使用Python脚本解析JTL文件
    python3 <<PYTHON_SCRIPT
import xml.etree.ElementTree as ET
import sys

# 解析JTL文件
tree = ET.parse('${TEST_LOG_DIR}/test-results.jtl')
root = tree.getroot()

# 统计指标
total_samples = 0
successful_samples = 0
failed_samples = 0
total_time = 0
min_time = float('inf')
max_time = 0

# 收集所有响应时间
response_times = []

for sample in root.findall('httpSample'):
    total_samples += 1
    success = sample.get('s')
    time = int(sample.get('t'))

    response_times.append(time)
    total_time += time

    if time < min_time:
        min_time = time
    if time > max_time:
        max_time = time

    if success == 'true':
        successful_samples += 1
    else:
        failed_samples += 1

# 计算指标
if total_samples > 0:
    avg_time = total_time / total_samples
    success_rate = (successful_samples / total_samples) * 100
    tps = total_samples / ${DURATION}

    # 计算P95响应时间
    response_times.sort()
    p95_index = int(len(response_times) * 0.95)
    p95_time = response_times[p95_index] if p95_index < len(response_times) else max_time

    # 输出结果
    print("\\n" + "="*60)
    print("性能测试结果汇总")
    print("="*60)
    print(f"总请求数: {total_samples}")
    print(f"成功请求数: {successful_samples}")
    print(f"失败请求数: {failed_samples}")
    print(f"成功率: {success_rate:.2f}%")
    print(f"TPS: {tps:.2f}")
    print(f"平均响应时间: {avg_time:.2f}ms")
    print(f"最小响应时间: {min_time}ms")
    print(f"最大响应时间: {max_time}ms")
    print(f"P95响应时间: {p95_time}ms")
    print("="*60)

    # 验证是否达标
    print("\\n验证结果：")
    tps_pass = tps >= ${TARGET_TPS}
    response_pass = avg_time <= ${TARGET_RESPONSE}
    p95_pass = p95_time <= ${TARGET_P95}

    if tps_pass:
        print(f"✓ TPS达标: {tps:.2f} >= ${TARGET_TPS}")
    else:
        print(f"✗ TPS未达标: {tps:.2f} < ${TARGET_TPS}")

    if response_pass:
        print(f"✓ 平均响应时间达标: {avg_time:.2f}ms <= ${TARGET_RESPONSE}ms")
    else:
        print(f"✗ 平均响应时间未达标: {avg_time:.2f}ms > ${TARGET_RESPONSE}ms")

    if p95_pass:
        print(f"✓ P95响应时间达标: {p95_time}ms <= ${TARGET_P95}ms")
    else:
        print(f"✗ P95响应时间未达标: {p95_time}ms > ${TARGET_P95}ms")

    if tps_pass and response_pass and p95_pass:
        print("\\n🎉 性能测试全部通过！")
        sys.exit(0)
    else:
        print("\\n⚠️ 性能测试未全部通过，请优化！")
        sys.exit(1)

PYTHON_SCRIPT

    PYTHON_EXIT_CODE=$?

    if [ $PYTHON_EXIT_CODE -eq 0 ]; then
        log_info "✓ 性能测试全部通过！"
        return 0
    else
        log_error "性能测试未全部通过！"
        return 1
    fi
}

# ============================================================
# 主流程
# ============================================================
main() {
    log_info "========================================"
    log_info "消费服务性能测试"
    log_info "========================================"
    log_info "服务器: ${BASE_URL}"
    log_info "日志目录: ${TEST_LOG_DIR}"
    log_info "========================================"
    echo ""

    # 执行测试
    check_environment
    create_jmeter_test_plan
    run_performance_test
    analyze_results

    log_info ""
    log_info "========================================"
    log_info "性能测试完成！"
    log_info "详细报告: ${TEST_LOG_DIR}/html-report/index.html"
    log_info "========================================"
}

# 执行主流程
main
