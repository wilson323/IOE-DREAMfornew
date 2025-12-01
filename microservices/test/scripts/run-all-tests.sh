#!/bin/bash

# =============================================================================
# IOE-DREAM 微服务完整测试套件执行脚本
# =============================================================================
#
# 功能: 一键执行所有测试套件，包括集成测试、性能测试和业务流程测试
# 支持: 自动化测试环境准备、测试执行、报告生成
#
# 作者: IOE-DREAM测试团队
# 版本: v1.0.0
# 最后更新: 2025-11-29
# =============================================================================

set -e

# 脚本配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")/../.."
TEST_ROOT="$PROJECT_ROOT/test"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
PURPLE='\033[0;35m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# 测试配置
SETUP_ENV=${SETUP_ENV:-true}
RUN_INTEGRATION=${RUN_INTEGRATION:-true}
RUN_PERFORMANCE=${RUN_PERFORMANCE:-true}
RUN_BUSINESS_FLOW=${RUN_BUSINESS_FLOW:-true}
GENERATE_REPORTS=${GENERATE_REPORTS:-true}
DEPLOYMENT_MODE=${DEPLOYMENT_MODE:-docker}
CLEAN_AFTER=${CLEAN_AFTER:-false}

# 测试结果统计
declare -A OVERALL_RESULTS
TOTAL_SUITES=0
PASSED_SUITES=0
FAILED_SUITES=0

# 时间戳
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
REPORT_DIR="$TEST_ROOT/reports/overall-$TIMESTAMP"

# 日志函数
log() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS:${NC} $1"
}

log_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1"
}

log_info() {
    echo -e "${CYAN}[$(date '+%Y-%m-%d %H:%M:%S')] INFO:${NC} $1"
}

log_header() {
    echo
    echo -e "${BOLD}${PURPLE}==============================================="
    echo "$1"
    echo "===============================================${NC}"
}

# 显示帮助信息
show_help() {
    cat << EOF
IOE-DREAM 微服务完整测试套件执行脚本

用法: $0 [选项]

选项:
    --mode MODE         部署模式 (docker|local) [默认: docker]
    --skip-setup        跳过环境设置
    --skip-integration  跳过集成测试
    --skip-performance  跳过性能测试
    --skip-business     跳过业务流程测试
    --skip-reports      跳过报告生成
    --clean-after       测试完成后清理环境
    --help              显示此帮助信息

测试类型说明:
    - 集成测试: 验证微服务间通信和协作
    - 性能测试: 负载、压力和容量测试
    - 业务流程测试: 完整业务场景端到端测试

示例:
    $0                           # 执行所有测试
    $0 --mode local              # 本地模式执行测试
    $0 --skip-performance        # 跳过性能测试
    $0 --skip-setup --clean-after # 跳过环境设置，测试后清理

EOF
}

# 解析命令行参数
parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --mode)
                DEPLOYMENT_MODE="$2"
                shift 2
                ;;
            --skip-setup)
                SETUP_ENV=false
                shift
                ;;
            --skip-integration)
                RUN_INTEGRATION=false
                shift
                ;;
            --skip-performance)
                RUN_PERFORMANCE=false
                shift
                ;;
            --skip-business)
                RUN_BUSINESS_FLOW=false
                shift
                ;;
            --skip-reports)
                GENERATE_REPORTS=false
                shift
                ;;
            --clean-after)
                CLEAN_AFTER=true
                shift
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# 显示测试配置
show_configuration() {
    log_header "测试配置"
    echo "部署模式: $DEPLOYMENT_MODE"
    echo "设置环境: $SETUP_ENV"
    echo "运行集成测试: $RUN_INTEGRATION"
    echo "运行性能测试: $RUN_PERFORMANCE"
    echo "运行业务流程测试: $RUN_BUSINESS_FLOW"
    echo "生成报告: $GENERATE_REPORTS"
    echo "测试后清理: $CLEAN_AFTER"
    echo "报告目录: $REPORT_DIR"
}

# 检查系统要求
check_system_requirements() {
    log_header "检查系统要求"

    # 检查必要工具
    local tools=("curl" "jq" "python3")
    local missing_tools=()

    for tool in "${tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            missing_tools+=("$tool")
        fi
    done

    if [ ${#missing_tools[@]} -gt 0 ]; then
        log_error "缺少必要工具: ${missing_tools[*]}"
        log_info "请使用以下命令安装缺失的工具:"
        log_info "Ubuntu/Debian: sudo apt-get install curl jq python3"
        log_info "CentOS/RHEL: sudo yum install curl jq python3"
        exit 1
    fi

    # 检查Docker（如果使用Docker模式）
    if [ "$DEPLOYMENT_MODE" = "docker" ]; then
        if ! command -v docker &> /dev/null; then
            log_error "Docker未安装，请先安装Docker"
            exit 1
        fi

        if ! command -v docker-compose &> /dev/null; then
            log_error "Docker Compose未安装，请先安装Docker Compose"
            exit 1
        fi
    fi

    log_success "系统要求检查通过"
}

# 设置测试环境
setup_test_environment() {
    if [ "$SETUP_ENV" = "false" ]; then
        log_info "跳过环境设置"
        return 0
    fi

    log_header "设置测试环境"

    local setup_script="$TEST_ROOT/test-data/scripts/setup-test-environment.sh"
    if [ -f "$setup_script" ]; then
        log_info "执行环境设置脚本..."
        chmod +x "$setup_script"

        if [ "$DEPLOYMENT_MODE" = "docker" ]; then
            "$setup_script" --mode docker --clean
        else
            "$setup_script" --mode local
        fi

        if [ $? -eq 0 ]; then
            OVERALL_RESULTS["environment_setup"]="PASS"
            log_success "测试环境设置成功"
        else
            OVERALL_RESULTS["environment_setup"]="FAIL"
            log_error "测试环境设置失败"
            return 1
        fi
    else
        log_error "环境设置脚本不存在: $setup_script"
        return 1
    fi
}

# 等待服务就绪
wait_for_services() {
    log_header "等待服务就绪"

    local gateway_url="http://localhost:8080"
    local max_wait=300  # 最大等待5分钟
    local wait_interval=10

    log_info "等待网关服务启动..."

    for i in $(seq 1 $((max_wait / wait_interval))); do
        if curl -s -f "$gateway_url/actuator/health" > /dev/null 2>&1; then
            log_success "网关服务已就绪"
            return 0
        fi

        log_info "等待服务启动... ($i/$((max_wait / wait_interval)))"
        sleep $wait_interval
    done

    log_error "服务启动超时"
    return 1
}

# 运行集成测试
run_integration_tests() {
    if [ "$RUN_INTEGRATION" = "false" ]; then
        log_info "跳过集成测试"
        return 0
    fi

    ((TOTAL_SUITES++))
    log_header "执行集成测试"

    local integration_script="$TEST_ROOT/integration-test/integration-test-suite.sh"
    if [ -f "$integration_script" ]; then
        log_info "执行集成测试套件..."
        chmod +x "$integration_script"

        if "$integration_script"; then
            OVERALL_RESULTS["integration_test"]="PASS"
            ((PASSED_SUITES++))
            log_success "集成测试通过"
        else
            OVERALL_RESULTS["integration_test"]="FAIL"
            ((FAILED_SUITES++))
            log_error "集成测试失败"
        fi
    else
        log_error "集成测试脚本不存在: $integration_script"
        OVERALL_RESULTS["integration_test"]="FAIL"
        ((FAILED_SUITES++))
    fi
}

# 运行性能测试
run_performance_tests() {
    if [ "$RUN_PERFORMANCE" = "false" ]; then
        log_info "跳过性能测试"
        return 0
    fi

    ((TOTAL_SUITES++))
    log_header "执行性能测试"

    local performance_script="$TEST_ROOT/performance-test/performance-test-suite.sh"
    if [ -f "$performance_script" ]; then
        log_info "执行性能测试套件..."
        chmod +x "$performance_script"

        if "$performance_script"; then
            OVERALL_RESULTS["performance_test"]="PASS"
            ((PASSED_SUITES++))
            log_success "性能测试通过"
        else
            OVERALL_RESULTS["performance_test"]="FAIL"
            ((FAILED_SUITES++))
            log_error "性能测试失败"
        fi
    else
        log_error "性能测试脚本不存在: $performance_script"
        OVERALL_RESULTS["performance_test"]="FAIL"
        ((FAILED_SUITES++))
    fi
}

# 运行业务流程测试
run_business_flow_tests() {
    if [ "$RUN_BUSINESS_FLOW" = "false" ]; then
        log_info "跳过业务流程测试"
        return 0
    fi

    ((TOTAL_SUITES++))
    log_header "执行业务流程测试"

    local business_script="$TEST_ROOT/integration-test/business-flow-tests.sh"
    if [ -f "$business_script" ]; then
        log_info "执行业务流程测试套件..."
        chmod +x "$business_script"

        if "$business_script"; then
            OVERALL_RESULTS["business_flow_test"]="PASS"
            ((PASSED_SUITES++))
            log_success "业务流程测试通过"
        else
            OVERALL_RESULTS["business_flow_test"]="FAIL"
            ((FAILED_SUITES++))
            log_error "业务流程测试失败"
        fi
    else
        log_error "业务流程测试脚本不存在: $business_script"
        OVERALL_RESULTS["business_flow_test"]="FAIL"
        ((FAILED_SUITES++))
    fi
}

# 收集测试报告
collect_test_reports() {
    if [ "$GENERATE_REPORTS" = "false" ]; then
        log_info "跳过报告收集"
        return 0
    fi

    log_header "收集测试报告"

    # 创建报告目录
    mkdir -p "$REPORT_DIR"
    mkdir -p "$REPORT_DIR/integration"
    mkdir -p "$REPORT_DIR/performance"
    mkdir -p "$REPORT_DIR/business-flows"

    # 收集集成测试报告
    local integration_report_dir="$TEST_ROOT/reports/integration"
    if [ -d "$integration_report_dir" ]; then
        cp -r "$integration_report_dir"/* "$REPORT_DIR/integration/" 2>/dev/null || true
        log_info "集成测试报告已收集"
    fi

    # 收集性能测试报告
    local performance_report_dir="$TEST_ROOT/reports/performance"
    if [ -d "$performance_report_dir" ]; then
        cp -r "$performance_report_dir"/* "$REPORT_DIR/performance/" 2>/dev/null || true
        log_info "性能测试报告已收集"
    fi

    # 收集业务流程测试报告
    local business_report_dir="$TEST_ROOT/reports/integration/business-flows"
    if [ -d "$business_report_dir" ]; then
        cp -r "$business_report_dir"/* "$REPORT_DIR/business-flows/" 2>/dev/null || true
        log_info "业务流程测试报告已收集"
    fi

    log_success "测试报告收集完成: $REPORT_DIR"
}

# 生成综合测试报告
generate_overall_report() {
    if [ "$GENERATE_REPORTS" = "false" ]; then
        log_info "跳过综合报告生成"
        return 0
    fi

    log_header "生成综合测试报告"

    local report_file="$REPORT_DIR/overall-test-report-$TIMESTAMP.json"
    local html_report_file="${report_file%.json}.html"

    # 计算总体成功率
    local overall_success_rate=0
    if [ $TOTAL_SUITES -gt 0 ]; then
        overall_success_rate=$(echo "scale=2; $PASSED_SUITES * 100 / $TOTAL_SUITES" | bc -l)
    fi

    # 生成JSON报告
    local json_report=$(cat << EOF
{
    "testSuite": "ioedream-overall",
    "timestamp": "$(date -Iseconds)",
    "configuration": {
        "deploymentMode": "$DEPLOYMENT_MODE",
        "setupEnvironment": $SETUP_ENV,
        "runIntegration": $RUN_INTEGRATION,
        "runPerformance": $RUN_PERFORMANCE,
        "runBusinessFlow": $RUN_BUSINESS_FLOW
    },
    "summary": {
        "totalSuites": $TOTAL_SUITES,
        "passedSuites": $PASSED_SUITES,
        "failedSuites": $FAILED_SUITES,
        "successRate": "$overall_success_rate%"
    },
    "testResults": [
EOF
    )

    local first=true
    for test_suite in "${!OVERALL_RESULTS[@]}"; do
        if [ "$first" = false ]; then
            json_report+=","
        fi
        first=false
        json_report+=$(cat << EOF
        {
            "suite": "$test_suite",
            "result": "${OVERALL_RESULTS[$test_suite]}"
        }
EOF
        )
    done

    json_report+=$(cat << EOF
    ],
    "reportDirectory": "$REPORT_DIR",
    "testEnvironment": {
        "hostname": "$(hostname)",
        "os": "$(uname -s)",
        "arch": "$(uname -m)",
        "javaVersion": "$(java -version 2>&1 | head -1 || echo 'N/A')",
        "pythonVersion": "$(python3 --version 2>&1 || echo 'N/A')"
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
    <title>IOE-DREAM 微服务测试综合报告</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Microsoft YaHei', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            max-width: 1400px;
            margin: 0 auto;
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
            color: white;
            padding: 40px;
            text-align: center;
            position: relative;
            overflow: hidden;
        }
        .header::before {
            content: '';
            position: absolute;
            top: -50%;
            right: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
            animation: rotate 30s linear infinite;
        }
        @keyframes rotate {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        .header h1 {
            font-size: 36px;
            margin-bottom: 10px;
            position: relative;
            z-index: 1;
        }
        .header p {
            font-size: 18px;
            opacity: 0.9;
            position: relative;
            z-index: 1;
        }
        .summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 30px;
            padding: 40px;
            background: #f8f9fa;
        }
        .summary-card {
            background: white;
            padding: 30px;
            border-radius: 10px;
            text-align: center;
            box-shadow: 0 10px 25px rgba(0,0,0,0.1);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }
        .summary-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 35px rgba(0,0,0,0.15);
        }
        .summary-card .icon {
            font-size: 48px;
            margin-bottom: 20px;
        }
        .summary-card .value {
            font-size: 36px;
            font-weight: bold;
            margin-bottom: 10px;
        }
        .summary-card .label {
            color: #666;
            font-size: 16px;
        }
        .card-pass { border-top: 4px solid #28a745; }
        .card-fail { border-top: 4px solid #dc3545; }
        .card-info { border-top: 4px solid #17a2b8; }
        .card-success { border-top: 4px solid #28a745; }
        .test-results {
            padding: 40px;
        }
        .test-suite {
            background: white;
            margin-bottom: 20px;
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            transition: transform 0.3s ease;
        }
        .test-suite:hover {
            transform: translateY(-2px);
        }
        .suite-header {
            padding: 20px 30px;
            font-size: 20px;
            font-weight: bold;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .suite-header.pass {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
        }
        .suite-header.fail {
            background: linear-gradient(135deg, #dc3545 0%, #fd7e14 100%);
            color: white;
        }
        .suite-content {
            padding: 20px 30px;
            background: #f8f9fa;
        }
        .status-badge {
            padding: 8px 20px;
            border-radius: 25px;
            font-size: 14px;
            font-weight: bold;
            text-transform: uppercase;
        }
        .status-pass {
            background: #d4edda;
            color: #155724;
        }
        .status-fail {
            background: #f8d7da;
            color: #721c24;
        }
        .environment {
            padding: 40px;
            background: #f8f9fa;
            border-top: 1px solid #e9ecef;
        }
        .environment h2 {
            margin-bottom: 20px;
            color: #495057;
        }
        .env-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
        }
        .env-item {
            background: white;
            padding: 20px;
            border-radius: 8px;
            border-left: 4px solid #007bff;
        }
        .env-item strong {
            color: #495057;
            display: block;
            margin-bottom: 5px;
        }
        .footer {
            background: #343a40;
            color: white;
            text-align: center;
            padding: 30px;
        }
        .progress-bar {
            width: 100%;
            height: 30px;
            background: #e9ecef;
            border-radius: 15px;
            overflow: hidden;
            margin: 20px 0;
        }
        .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #28a745, #20c997);
            transition: width 0.5s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🚀 IOE-DREAM 微服务测试综合报告</h1>
            <p>执行时间: $(date '+%Y-%m-%d %H:%M:%S')</p>
            <p>测试环境: $DEPLOYMENT_MODE 模式</p>
        </div>

        <div class="summary">
            <div class="summary-card card-info">
                <div class="icon">📊</div>
                <div class="value">$TOTAL_SUITES</div>
                <div class="label">测试套件总数</div>
            </div>
            <div class="summary-card card-pass">
                <div class="icon">✅</div>
                <div class="value">$PASSED_SUITES</div>
                <div class="label">通过套件</div>
            </div>
            <div class="summary-card card-fail">
                <div class="icon">❌</div>
                <div class="value">$FAILED_SUITES</div>
                <div class="label">失败套件</div>
            </div>
            <div class="summary-card card-success">
                <div class="icon">📈</div>
                <div class="value">${overall_success_rate}%</div>
                <div class="label">总体成功率</div>
            </div>
        </div>

        <div class="test-results">
            <h2 style="text-align: center; margin-bottom: 30px; color: #495057;">📋 测试套件执行结果</h2>
EOF

    # 添加测试套件结果
    local suite_names=(
        "environment_setup:环境设置"
        "integration_test:集成测试"
        "performance_test:性能测试"
        "business_flow_test:业务流程测试"
    )

    for suite_info in "${suite_names[@]}"; do
        IFS=':' read -r suite_key suite_name <<< "$suite_info"
        local result=${OVERALL_RESULTS[$suite_key]:-SKIP}
        local status_class="pass"
        local status_text="通过"
        local status_badge_class="status-pass"

        if [ "$result" = "FAIL" ]; then
            status_class="fail"
            status_text="失败"
            status_badge_class="status-fail"
        elif [ "$result" = "SKIP" ]; then
            status_class="skip"
            status_text="跳过"
            status_badge_class="status-skip"
        fi

        cat >> "$html_report_file" << EOF
            <div class="test-suite">
                <div class="suite-header $status_class">
                    <span>$suite_name</span>
                    <span class="status-badge $status_badge_class">$status_text</span>
                </div>
                <div class="suite-content">
                    <p>测试套件执行状态: <strong>$result</strong></p>
EOF

        # 添加特定测试套件的详细信息
        case $suite_key in
            "integration_test")
                echo "                    <p>验证微服务间通信、API契约一致性、熔断器机制等</p>" >> "$html_report_file"
                ;;
            "performance_test")
                echo "                    <p>负载测试、压力测试、容量测试、并发测试等性能指标验证</p>" >> "$html_report_file"
                ;;
            "business_flow_test")
                echo "                    <p>用户认证、门禁控制、消费支付、考勤管理、视频监控等完整业务流程测试</p>" >> "$html_report_file"
                ;;
        esac

        cat >> "$html_report_file" << EOF
                </div>
            </div>
EOF
    done

    cat >> "$html_report_file" << EOF
        </div>

        <div class="environment">
            <h2>🖥️ 测试环境信息</h2>
            <div class="env-grid">
                <div class="env-item">
                    <strong>主机名</strong>
                    $(hostname)
                </div>
                <div class="env-item">
                    <strong>操作系统</strong>
                    $(uname -s) $(uname -r)
                </div>
                <div class="env-item">
                    <strong>架构</strong>
                    $(uname -m)
                </div>
                <div class="env-item">
                    <strong>Java版本</strong>
                    $(java -version 2>&1 | head -1 || echo 'N/A')
                </div>
                <div class="env-item">
                    <strong>Python版本</strong>
                    $(python3 --version 2>&1 || echo 'N/A')
                </div>
                <div class="env-item">
                    <strong>报告目录</strong>
                    $REPORT_DIR
                </div>
            </div>
        </div>

        <div class="footer">
            <p>📅 报告生成时间: $(date '+%Y-%m-%d %H:%M:%S')</p>
            <p>🔗 详细报告请查看: $REPORT_DIR</p>
            <p>© 2025 IOE-DREAM 测试团队 - 专注质量，追求卓越</p>
        </div>
    </div>
</body>
</html>
EOF

    log_success "综合测试报告已生成"
    log_info "JSON报告: $report_file"
    log_info "HTML报告: $html_report_file"
}

# 清理测试环境
cleanup_test_environment() {
    if [ "$CLEAN_AFTER" = "false" ]; then
        log_info "跳过环境清理"
        return 0
    fi

    log_header "清理测试环境"

    if [ "$DEPLOYMENT_MODE" = "docker" ]; then
        local compose_file="$PROJECT_ROOT/test/docker/docker-compose.test.yml"
        if [ -f "$compose_file" ]; then
            log_info "停止Docker容器..."
            docker-compose -f "$compose_file" down -v --remove-orphans
        fi
    fi

    log_success "测试环境清理完成"
}

# 显示最终结果
show_final_result() {
    echo
    log_header "🎯 测试执行完成 - 最终结果"
    echo "==============================================="
    echo "总测试套件: $TOTAL_SUITES"
    echo -e "通过套件: ${GREEN}$PASSED_SUITES${NC}"
    echo -e "失败套件: ${RED}$FAILED_SUITES${NC}"

    local success_rate=0
    if [ $TOTAL_SUITES -gt 0 ]; then
        success_rate=$(echo "scale=2; $PASSED_SUITES * 100 / $TOTAL_SUITES" | bc -l)
    fi
    echo "总体成功率: ${success_rate}%"

    echo "==============================================="
    echo "详细报告目录: $REPORT_DIR"

    if [ $FAILED_SUITES -eq 0 ]; then
        echo -e "\n${GREEN}🎉 所有测试套件执行成功！${NC}"
        echo "系统质量符合要求，可以进入下一阶段。"
    else
        echo -e "\n${YELLOW}⚠️  存在 $FAILED_SUITES 个失败的测试套件${NC}"
        echo "请查看详细报告，修复相关问题后重新测试。"
    fi
}

# 主函数
main() {
    log "🚀 开始执行IOE-DREAM微服务完整测试套件"

    # 设置错误处理
    trap 'log_error "脚本执行被中断"; cleanup_test_environment; exit 1' INT TERM

    # 解析命令行参数
    parse_arguments "$@"

    # 显示配置
    show_configuration

    # 检查系统要求
    check_system_requirements

    # 设置测试环境
    if ! setup_test_environment; then
        log_error "环境设置失败，测试终止"
        exit 1
    fi

    # 等待服务就绪
    wait_for_services

    # 执行各类测试
    run_integration_tests
    run_performance_tests
    run_business_flow_tests

    # 收集测试报告
    collect_test_reports

    # 生成综合报告
    generate_overall_report

    # 清理测试环境
    cleanup_test_environment

    # 显示最终结果
    show_final_result

    # 根据测试结果决定退出码
    if [ $FAILED_SUITES -eq 0 ]; then
        exit 0
    else
        exit 1
    fi
}

# 脚本入口点
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi