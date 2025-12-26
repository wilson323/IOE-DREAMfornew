#!/bin/bash

# IOE-DREAM SonarQube 代码质量分析脚本
# 用于自动化代码质量分析和持续集成
#
# 使用方法:
# 1. 本地分析: ./scripts/sonar-analysis.sh
# 2. 指定模块: ./scripts/sonar-analysis.sh -m microservices-common
# 3. 跳过测试: ./scripts/sonar-analysis.sh -s
# 4. CI模式: ./scripts/sonar-analysis.sh -c
#
# 参数说明:
# -m: 指定要分析的模块 (默认分析所有模块)
# -s: 跳过单元测试执行
# -c: CI/CD模式，自动上传到SonarQube服务器
# -u: SonarQube服务器地址
# -k: 项目键名
# -t: 认证令牌

set -e

# 颜色输出函数
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
WHITE='\033[0;37m'
NC='\033[0m' # No Color

# 输出函数
output() {
    echo -e "${NC}$1${NC}"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

error() {
    echo -e "${RED}❌ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

title() {
    echo -e "\n${CYAN}================================================================${NC}"
    echo -e "${YELLOW}$1${NC}"
    echo -e "${CYAN}================================================================${NC}"
}

section() {
    echo -e "\n${GREEN}--- $1 ---${NC}"
}

# 默认参数
MODULE=""
SKIP_TESTS=false
CI_MODE=false
SONAR_URL="http://localhost:9000"
PROJECT_KEY="ioedream-microservices"
TOKEN=""

# 解析命令行参数
while getopts "m:scu:k:t:" opt; do
    case $opt in
        m) MODULE="$OPTARG" ;;
        s) SKIP_TESTS=true ;;
        c) CI_MODE=true ;;
        u) SONAR_URL="$OPTARG" ;;
        k) PROJECT_KEY="$OPTARG" ;;
        t) TOKEN="$OPTARG" ;;
        \?) echo "无效选项: -$OPTARG" >&2; exit 1 ;;
    esac
done

# 检查依赖
check_dependencies() {
    section "检查分析依赖"

    # 检查Java
    if command -v java &> /dev/null; then
        success "Java 已安装"
    else
        error "Java 未安装"
        exit 1
    fi

    # 检查Maven
    if command -v mvn &> /dev/null; then
        success "Maven 已安装"
    else
        error "Maven 未安装"
        exit 1
    fi

    # 检查SonarQube服务器连接
    if curl -s --connect-timeout 10 "$SONAR_URL/api/system/status" | grep -q "UP"; then
        success "SonarQube服务器可访问: $SONAR_URL"
    else
        warning "无法连接到SonarQube服务器: $SONAR_URL"
    fi
}

# 构建项目
build_project() {
    section "构建项目"

    BUILD_ARGS=("clean" "compile")

    if [ "$SKIP_TESTS" = false ]; then
        BUILD_ARGS+=("test")
    fi

    if [ -n "$MODULE" ]; then
        info "构建指定模块: $MODULE"
        BUILD_ARGS+=("-pl" "$MODULE" "-am")
    fi

    info "执行 Maven 构建..."

    if [ "$SKIP_TESTS" = true ]; then
        if mvn "${BUILD_ARGS[@]}" -DskipTests=true; then
            success "项目构建成功 (跳过测试)"
        else
            error "项目构建失败"
            exit 1
        fi
    else
        if mvn "${BUILD_ARGS[@]}"; then
            success "项目构建成功"
        else
            error "项目构建失败"
            exit 1
        fi
    fi
}

# 运行SonarQube分析
run_sonar_analysis() {
    section "运行SonarQube代码分析"

    ANALYSIS_ARGS=("sonar:sonar")

    if [ "$CI_MODE" = true ]; then
        ANALYSIS_ARGS+=("-Dsonar.host.url=$SONAR_URL" "-Dsonar.login=$TOKEN")
        info "CI模式: 分析完成后自动上传到SonarQube服务器"
        export SONAR_HOST_URL="$SONAR_URL"
        export SONAR_AUTH_TOKEN="$TOKEN"
    else
        ANALYSIS_ARGS+=("-Dsonar.host.url=$SONAR_URL")
        info "本地模式: 分析完成后在浏览器中查看报告"
    fi

    if [ -n "$MODULE" ]; then
        ANALYSIS_ARGS+=("-pl" "$MODULE")
    fi

    info "开始SonarQube分析..."
    info "分析参数: ${ANALYSIS_ARGS[*]}"

    if mvn "${ANALYSIS_ARGS[@]}"; then
        success "SonarQube分析完成"

        if [ "$CI_MODE" = true ]; then
            info "分析报告已上传到SonarQube服务器"
            info "访问地址: $SONAR_URL/dashboard?id=$PROJECT_KEY"
        else
            info "分析报告已生成本地"
            info "访问地址: $SONAR_URL"
        fi
    else
        error "SonarQube分析失败"
        exit 1
    fi
}

# 生成分析报告
generate_analysis_report() {
    section "生成分析报告"

    REPORT_DIR="target/sonar-reports"
    mkdir -p "$REPORT_DIR"

    REPORT_FILE="$REPORT_DIR/analysis-summary-$(date '+%Y%m%d-%H%M%S').md"

    cat > "$REPORT_FILE" << EOF
# IOE-DREAM 代码质量分析报告

**分析时间**: $(date '+%Y-%m-%d %H:%M:%S')
**分析模式**: $([ "$CI_MODE" = true ] && echo "CI/CD模式" || echo "本地模式")
**分析模块**: $([ -n "$MODULE" ] && echo "$MODULE" || echo "全部模块")
**SonarQube地址**: $SONAR_URL

## 分析结果摘要

### 质量指标
- 代码覆盖率: 计算中...
- 重复代码率: 计算中...
- 技术债务: 计算中...
- 安全漏洞: 计算中...

### 主要问题
分析进行中，详细结果请查看SonarQube仪表板。

### 改进建议
1. 提高单元测试覆盖率至85%以上
2. 减少代码重复
3. 修复安全漏洞
4. 降低代码复杂度

## 下一步行动

1. 登录SonarQube仪表板查看详细分析结果
2. 优先修复高优先级问题
3. 持续改进代码质量

---
*此报告由SonarQube分析脚本自动生成*
EOF

    success "分析报告已生成: $REPORT_FILE"
}

# 显示使用帮助
show_help() {
    cat << EOF
IOE-DREAM SonarQube 代码质量分析脚本

使用方法:
  $0 [选项]

选项:
  -m <module>     指定要分析的模块 (默认分析所有模块)
  -s               跳过单元测试执行
  -c               CI/CD模式，自动上传到SonarQube服务器
  -u <url>         SonarQube服务器地址 (默认: http://localhost:9000)
  -k <key>         项目键名 (默认: ioedream-microservices)
  -t <token>       认证令牌

示例:
  # 分析所有模块
  $0

  # 分析指定模块
  $0 -m microservices-common

  # 跳过测试
  $0 -s

  # CI模式
  $0 -c -u http://sonar.example.com -k my-project -sqt

EOF
}

# 主执行流程
main() {
    title "IOE-DREAM SonarQube 代码质量分析"

    info "分析配置:"
    info "  - 模块: $([ -n "$MODULE" ] && echo "$MODULE" || echo "全部模块")"
    info "  - 跳过测试: $SKIP_TESTS"
    info "  - CI模式: $CI_MODE"
    info "  - SonarQube地址: $SONAR_URL"
    info "  - 项目键名: $PROJECT_KEY"

    # 显示帮助
    if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
        show_help
        exit 0
    fi

    # 检查依赖
    check_dependencies

    # 构建项目
    build_project

    # 运行SonarQube分析
    run_sonar_analysis

    # 生成报告
    generate_analysis_report

    section "分析完成"
    success "SonarQube代码质量分析已成功完成！"

    if [ "$CI_MODE" = true ]; then
        info "📊 详细报告: $SONAR_URL/dashboard?id=$PROJECT_KEY"
    else
        info "📊 SonarQube仪表板: $SONAR_URL"
    fi

    info "📄 分析报告: ./target/sonar-reports/"
    info "🔧 配置文件: ./sonar-project.properties"
}

# 执行主函数
main "$@"