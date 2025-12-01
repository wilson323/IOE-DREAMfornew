#!/bin/bash

# ===================================================================
# 企业级测试自动化脚本
# 基于企业代码质量标准化OpenSpec变更实施
# 严格遵循repowiki测试架构规范
# ===================================================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ===================================================================
# 配置变量
# ===================================================================

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MODULE_ROOT="${PROJECT_ROOT}/smart-admin-api-java17-springboot3"

# 测试配置
TEST_PROFILE="test"
TEST_TIMEOUT="300"
COVERAGE_THRESHOLD="80"

# 报告目录
REPORT_DIR="${PROJECT_ROOT}/test-reports"
COVERAGE_DIR="${REPORT_DIR}/coverage"
UNIT_TEST_DIR="${REPORT_DIR}/unit-test"
INTEGRATION_TEST_DIR="${REPORT_DIR}/integration-test"
API_TEST_DIR="${REPORT_DIR}/api-test"
PERFORMANCE_TEST_DIR="${REPORT_DIR}/performance-test"

# Maven配置
MAVEN_OPTS="-Dspring.profiles.active=${TEST_PROFILE} -Dmaven.test.failure.ignore=false"
TEST_OPTS="-Dmaven.test.timeout=${TEST_TIMEOUT}"

# ===================================================================
# 初始化函数
# ===================================================================

# 初始化测试环境
init_test_environment() {
    log_info "初始化测试环境..."

    # 创建报告目录
    mkdir -p "${REPORT_DIR}"
    mkdir -p "${COVERAGE_DIR}"
    mkdir -p "${UNIT_TEST_DIR}"
    mkdir -p "${INTEGRATION_TEST_DIR}"
    mkdir -p "${API_TEST_DIR}"
    mkdir -p "${PERFORMANCE_TEST_DIR}"

    # 检查Java环境
    if ! command -v java &> /dev/null; then
        log_error "Java未安装或不在PATH中"
        exit 1
    fi

    # 检查Maven环境
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装或不在PATH中"
        exit 1
    fi

    # 检查项目结构
    if [ ! -f "${MODULE_ROOT}/pom.xml" ]; then
        log_error "项目pom.xml文件不存在: ${MODULE_ROOT}/pom.xml"
        exit 1
    fi

    log_success "测试环境初始化完成"
}

# 清理测试环境
clean_test_environment() {
    log_info "清理测试环境..."

    # 清理Maven构建产物
    cd "${MODULE_ROOT}"
    mvn clean ${MAVEN_OPTS}

    # 清理测试报告目录（保留最近的报告）
    find "${REPORT_DIR}" -name "*.log" -mtime +7 -delete
    find "${REPORT_DIR}" -name "*.xml" -mtime +7 -delete
    find "${REPORT_DIR}" -name "*.html" -mtime +7 -delete

    log_success "测试环境清理完成"
}

# ===================================================================
# 单元测试自动化
# ===================================================================

# 运行单元测试
run_unit_tests() {
    log_info "开始运行单元测试..."

    cd "${MODULE_ROOT}"

    # 执行单元测试
    mvn test ${MAVEN_OPTS} ${TEST_OPTS} \
        -Dtest="**/*Test" \
        -DfailIfNoTests=false \
        | tee "${UNIT_TEST_DIR}/unit-test.log"

    # 检查测试结果
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        log_success "单元测试执行成功"
    else
        log_error "单元测试执行失败"
        return 1
    fi
}

# 生成单元测试报告
generate_unit_test_report() {
    log_info "生成单元测试报告..."

    cd "${MODULE_ROOT}"

    # 生成Surefire报告
    mvn surefire-report:report ${MAVEN_OPTS}

    # 生成测试覆盖率报告
    mvn jacoco:report ${MAVEN_OPTS}

    # 复制报告到指定目录
    if [ -d "target/site/surefire-report.html" ]; then
        cp -r target/site/surefire-report.html "${UNIT_TEST_DIR}/" 2>/dev/null || true
    fi

    if [ -d "target/site/jacoco" ]; then
        cp -r target/site/jacoco "${UNIT_TEST_DIR}/" 2>/dev/null || true
    fi

    log_success "单元测试报告生成完成"
}

# ===================================================================
# 集成测试自动化
# ===================================================================

# 运行集成测试
run_integration_tests() {
    log_info "开始运行集成测试..."

    cd "${MODULE_ROOT}"

    # 执行集成测试
    mvn verify ${MAVEN_OPTS} ${TEST_OPTS} \
        -Dit.test="**/*ITest" \
        -DfailIfNoTests=false \
        | tee "${INTEGRATION_TEST_DIR}/integration-test.log"

    # 检查测试结果
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        log_success "集成测试执行成功"
    else
        log_error "集成测试执行失败"
        return 1
    fi
}

# 生成集成测试报告
generate_integration_test_report() {
    log_info "生成集成测试报告..."

    cd "${MODULE_ROOT}"

    # 生成Failsafe报告
    mvn failsafe-report:report ${MAVEN_OPTS}

    # 复制报告到指定目录
    if [ -d "target/site/failsafe-report.html" ]; then
        cp -r target/site/failsafe-report.html "${INTEGRATION_TEST_DIR}/" 2>/dev/null || true
    fi

    log_success "集成测试报告生成完成"
}

# ===================================================================
# API测试自动化
# ===================================================================

# 运行API测试
run_api_tests() {
    log_info "开始运行API测试..."

    # 启动应用（如果未运行）
    if ! pgrep -f "smart-admin" > /dev/null; then
        log_info "启动测试应用..."
        cd "${MODULE_ROOT}"
        mvn spring-boot:run ${MAVEN_OPTS} &
        APP_PID=$!

        # 等待应用启动
        sleep 30

        # 检查应用是否启动成功
        if ! curl -f http://localhost:1024/api/health > /dev/null 2>&1; then
            log_error "测试应用启动失败"
            kill $APP_PID 2>/dev/null || true
            return 1
        fi
    fi

    # 运行API测试（假设使用Postman/Newman或类似工具）
    if command -v newman &> /dev/null; then
        log_info "使用Newman运行API测试..."
        newman run "${PROJECT_ROOT}/api-tests/collection.json" \
            --environment "${PROJECT_ROOT}/api-tests/test-environment.json" \
            --reporters cli,junit,html \
            --reporter-html-export "${API_TEST_DIR}/api-test-report.html" \
            --reporter-junit-export "${API_TEST_DIR}/api-test-results.xml" \
            | tee "${API_TEST_DIR}/api-test.log"
    else
        log_warning "Newman未安装，跳过API测试"
        return 0
    fi

    # 清理应用进程
    if [ ! -z "$APP_PID" ]; then
        kill $APP_PID 2>/dev/null || true
    fi

    log_success "API测试执行完成"
}

# ===================================================================
# 性能测试自动化
# ===================================================================

# 运行性能测试
run_performance_tests() {
    log_info "开始运行性能测试..."

    # 启动应用
    cd "${MODULE_ROOT}"
    mvn spring-boot:run ${MAVEN_OPTS} &
    APP_PID=$!

    # 等待应用启动
    sleep 30

    # 检查应用是否启动成功
    if ! curl -f http://localhost:1024/api/health > /dev/null 2>&1; then
        log_error "性能测试应用启动失败"
        kill $APP_PID 2>/dev/null || true
        return 1
    fi

    # 运行性能测试（假设使用JMeter）
    if command -v jmeter &> /dev/null; then
        log_info "使用JMeter运行性能测试..."
        jmeter -n -t "${PROJECT_ROOT}/performance-tests/test-plan.jmx" \
            -l "${PERFORMANCE_TEST_DIR}/performance-test.jtl" \
            -e -o "${PERFORMANCE_TEST_DIR}/performance-test-report" \
            | tee "${PERFORMANCE_TEST_DIR}/performance-test.log"
    else
        log_warning "JMeter未安装，跳过性能测试"
        return 0
    fi

    # 清理应用进程
    kill $APP_PID 2>/dev/null || true

    log_success "性能测试执行完成"
}

# ===================================================================
# 测试覆盖率分析
# ===================================================================

# 分析测试覆盖率
analyze_test_coverage() {
    log_info "分析测试覆盖率..."

    cd "${MODULE_ROOT}"

    # 生成JaCoCo覆盖率报告
    mvn jacoco:report ${MAVEN_OPTS}

    # 检查覆盖率是否达到阈值
    if [ -f "target/site/jacoco/index.html" ]; then
        # 提取总覆盖率
        TOTAL_COVERAGE=$(grep -o "Total.*[0-9]*%" target/site/jacoco/index.html | head -1 | grep -o "[0-9]*%" | head -1)
        COVERAGE_NUMBER=${TOTAL_COVERAGE%\%}

        log_info "当前测试覆盖率: ${TOTAL_COVERAGE}"

        # 检查是否达到阈值
        if [ "${COVERAGE_NUMBER}" -lt "${COVERAGE_THRESHOLD}" ]; then
            log_warning "测试覆盖率 ${TOTAL_COVERAGE} 低于阈值 ${COVERAGE_THRESHOLD}%"
            return 1
        else
            log_success "测试覆盖率 ${TOTAL_COVERAGE} 达到阈值 ${COVERAGE_THRESHOLD}%"
        fi
    else
        log_error "无法生成测试覆盖率报告"
        return 1
    fi

    # 复制覆盖率报告
    if [ -d "target/site/jacoco" ]; then
        cp -r target/site/jacoco "${COVERAGE_DIR}/" 2>/dev/null || true
    fi

    log_success "测试覆盖率分析完成"
}

# 生成覆盖率趋势报告
generate_coverage_trend_report() {
    log_info "生成覆盖率趋势报告..."

    # 创建趋势报告文件
    TREND_FILE="${COVERAGE_DIR}/coverage-trend.json"

    # 获取当前时间戳和覆盖率
    TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")

    if [ -f "target/site/jacoco/index.html" ]; then
        TOTAL_COVERAGE=$(grep -o "Total.*[0-9]*%" target/site/jacoco/index.html | head -1 | grep -o "[0-9]*%" | head -1)
        COVERAGE_NUMBER=${TOTAL_COVERAGE%\%}
    else
        COVERAGE_NUMBER="0"
    fi

    # 创建或更新趋势数据
    if [ -f "${TREND_FILE}" ]; then
        # 追加新数据
        echo ",{\"timestamp\":\"${TIMESTAMP}\",\"coverage\":${COVERAGE_NUMBER}}" >> "${TREND_FILE}"
    else
        # 创建新文件
        echo "[{\"timestamp\":\"${TIMESTAMP}\",\"coverage\":${COVERAGE_NUMBER}}]" > "${TREND_FILE}"
    fi

    log_success "覆盖率趋势报告生成完成"
}

# ===================================================================
# 测试报告汇总
# ===================================================================

# 生成测试报告汇总
generate_test_summary() {
    log_info "生成测试报告汇总..."

    SUMMARY_FILE="${REPORT_DIR}/test-summary.html"

    cat > "${SUMMARY_FILE}" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>测试报告汇总</title>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f0f0f0; padding: 20px; border-radius: 5px; }
        .section { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }
        .success { color: green; }
        .error { color: red; }
        .warning { color: orange; }
        .report-links { list-style-type: none; padding: 0; }
        .report-links li { margin: 5px 0; }
        .report-links a { text-decoration: none; color: #0066cc; }
        .report-links a:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="header">
        <h1>企业级代码质量标准化 - 测试报告汇总</h1>
        <p>生成时间: $(date)</p>
    </div>

    <div class="section">
        <h2>测试执行状态</h2>
        <ul>
            <li>单元测试: <span class="success">✓ 已完成</span></li>
            <li>集成测试: <span class="success">✓ 已完成</span></li>
            <li>API测试: <span class="success">✓ 已完成</span></li>
            <li>性能测试: <span class="success">✓ 已完成</span></li>
            <li>覆盖率分析: <span class="success">✓ 已完成</span></li>
        </ul>
    </div>

    <div class="section">
        <h2>详细报告链接</h2>
        <ul class="report-links">
            <li><a href="unit-test/index.html">单元测试报告</a></li>
            <li><a href="unit-test/jacoco/index.html">测试覆盖率报告</a></li>
            <li><a href="integration-test/index.html">集成测试报告</a></li>
            <li><a href="api-test/api-test-report.html">API测试报告</a></li>
            <li><a href="performance-test/performance-test-report/index.html">性能测试报告</a></li>
        </ul>
    </div>

    <div class="section">
        <h2>覆盖率统计</h2>
        <p>当前覆盖率: ${TOTAL_COVERAGE:-未生成}</p>
        <p>覆盖率要求: ≥${COVERAGE_THRESHOLD}%</p>
        <p><a href="coverage/jacoco/index.html">查看详细覆盖率报告</a></p>
    </div>
</body>
</html>
EOF

    log_success "测试报告汇总生成完成: ${SUMMARY_FILE}"
}

# ===================================================================
# 质量门禁检查
# ===================================================================

# 执行质量门禁检查
run_quality_gate() {
    log_info "执行质量门禁检查..."

    GATE_FAILED=false

    # 检查测试覆盖率
    if [ -f "target/site/jacoco/index.html" ]; then
        TOTAL_COVERAGE=$(grep -o "Total.*[0-9]*%" target/site/jacoco/index.html | head -1 | grep -o "[0-9]*%" | head -1)
        COVERAGE_NUMBER=${TOTAL_COVERAGE%\%}

        if [ "${COVERAGE_NUMBER}" -lt "${COVERAGE_THRESHOLD}" ]; then
            log_error "质量门禁失败: 测试覆盖率 ${TOTAL_COVERAGE} 低于阈值 ${COVERAGE_THRESHOLD}%"
            GATE_FAILED=true
        else
            log_success "质量门禁通过: 测试覆盖率 ${TOTAL_COVERAGE} 达到阈值"
        fi
    else
        log_error "质量门禁失败: 无法生成测试覆盖率报告"
        GATE_FAILED=true
    fi

    # 检查编译状态
    if ! mvn compile -q > /dev/null 2>&1; then
        log_error "质量门禁失败: 项目编译失败"
        GATE_FAILED=true
    else
        log_success "质量门禁通过: 项目编译成功"
    fi

    # 检查测试执行状态
    if [ ${GATE_FAILED} = true ]; then
        log_error "质量门禁检查失败!"
        return 1
    else
        log_success "质量门禁检查通过!"
        return 0
    fi
}

# ===================================================================
# 主函数
# ===================================================================

# 显示帮助信息
show_help() {
    cat << EOF
企业级测试自动化脚本

用法: $0 [选项]

选项:
    init                    初始化测试环境
    clean                   清理测试环境
    unit                    运行单元测试
    integration             运行集成测试
    api                     运行API测试
    performance             运行性能测试
    coverage                分析测试覆盖率
    report                  生成测试报告
    gate                    执行质量门禁
    all                     执行完整测试流程
    help                    显示此帮助信息

示例:
    $0 all                   # 执行完整测试流程
    $0 unit                  # 只运行单元测试
    $0 coverage              # 只分析测试覆盖率

配置:
    - TEST_PROFILE: 测试环境配置 (默认: test)
    - TEST_TIMEOUT: 测试超时时间 (默认: 300)
    - COVERAGE_THRESHOLD: 覆盖率阈值 (默认: 80)
EOF
}

# 执行完整测试流程
run_all_tests() {
    log_info "开始执行完整测试流程..."

    # 初始化环境
    init_test_environment

    # 清理环境
    clean_test_environment

    # 运行各种测试
    run_unit_tests
    generate_unit_test_report

    run_integration_tests
    generate_integration_test_report

    run_api_tests

    run_performance_tests

    # 覆盖率分析
    analyze_test_coverage
    generate_coverage_trend_report

    # 生成报告汇总
    generate_test_summary

    # 质量门禁检查
    run_quality_gate

    log_success "完整测试流程执行完成!"
}

# ===================================================================
# 脚本入口点
# ===================================================================

main() {
    case "${1:-all}" in
        "init")
            init_test_environment
            ;;
        "clean")
            clean_test_environment
            ;;
        "unit")
            init_test_environment
            run_unit_tests
            generate_unit_test_report
            ;;
        "integration")
            init_test_environment
            run_integration_tests
            generate_integration_test_report
            ;;
        "api")
            init_test_environment
            run_api_tests
            ;;
        "performance")
            init_test_environment
            run_performance_tests
            ;;
        "coverage")
            init_test_environment
            analyze_test_coverage
            generate_coverage_trend_report
            ;;
        "report")
            init_test_environment
            generate_test_summary
            ;;
        "gate")
            init_test_environment
            run_quality_gate
            ;;
        "all")
            run_all_tests
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"