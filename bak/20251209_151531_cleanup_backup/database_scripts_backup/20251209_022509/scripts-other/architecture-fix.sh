#!/bin/bash

# =============================================================================
# IOE-DREAM 架构规范修复脚本
# 功能：自动修复@Repository、@Autowired违规，统一架构规范
# 使用方法：./architecture-fix.sh [microservice-name]
# =============================================================================

set -e

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

# 统计函数
count_violations() {
    local service_path=$1
    local repository_count=$(grep -r "@Repository" "$service_path" --include="*.java" | wc -l)
    local autowired_count=$(grep -r "@Autowired" "$service_path" --include="*.java" | wc -l)
    local post_count=$(grep -r "@PostMapping.*query\|@PostMapping.*list\|@PostMapping.*get" "$service_path" --include="*.java" | wc -l)

    echo "Repository违规: $repository_count, Autowired违规: $autowired_count, API设计违规: $post_count"
}

# 修复Repository违规
fix_repository_violations() {
    local service_path=$1
    log_info "正在修复 @Repository 违规..."

    local files=$(grep -r -l "@Repository" "$service_path" --include="*.java" || true)

    if [ -z "$files" ]; then
        log_info "未发现 @Repository 违规"
        return 0
    fi

    echo "$files" | while read file; do
        log_info "修复文件: $file"
        sed -i 's/@Repository/@Mapper/g' "$file"
        log_success "✓ $file 修复完成"
    done
}

# 修复Autowired违规
fix_autowired_violations() {
    local service_path=$1
    log_info "正在修复 @Autowired 违规..."

    local files=$(grep -r -l "@Autowired" "$service_path" --include="*.java" || true)

    if [ -z "$files" ]; then
        log_info "未发现 @Autowired 违规"
        return 0
    fi

    echo "$files" | while read file; do
        log_info "修复文件: $file"
        sed -i 's/@Autowired/@Resource/g' "$file"
        log_success "✓ $file 修复完成"
    done
}

# 修复API设计违规
fix_api_violations() {
    local service_path=$1
    log_info "正在检查 API 设计违规..."

    local violations=$(grep -r -n "@PostMapping.*query\|@PostMapping.*list\|@PostMapping.*get" "$service_path" --include="*.java" || true)

    if [ -z "$violations" ]; then
        log_info "未发现 API 设计违规"
        return 0
    fi

    log_warning "发现以下 API 设计违规，需要手动修复："
    echo "$violations"

    # 生成修复建议文件
    local fix_suggestions="$service_path/api-fix-suggestions.txt"
    echo "# API 设计修复建议" > "$fix_suggestions"
    echo "$violations" | while read violation; do
        echo "$violation" >> "$fix_suggestions"
    done

    log_warning "修复建议已保存到: $fix_suggestions"
}

# 修复配置安全问题
fix_config_security() {
    local service_path=$1
    log_info "正在检查配置安全问题..."

    local config_files=$(find "$service_path" -name "*.yml" -o -name "*.yaml" -o -name "*.properties")

    for config_file in $config_files; do
        if grep -q "password.*[0-9]\|username.*root\|admin.*123" "$config_file"; then
            log_warning "发现配置安全问题: $config_file"
            log_warning "请手动检查并修复明文密码问题"
        fi
    done
}

# 主修复函数
fix_microservice() {
    local service_name=$1
    local service_path="microservices/$service_name"

    if [ ! -d "$service_path" ]; then
        log_error "微服务目录不存在: $service_path"
        return 1
    fi

    log_info "开始修复微服务: $service_name"
    echo "========================================"

    # 修复前统计
    log_info "修复前违规统计:"
    count_violations "$service_path"
    echo ""

    # 执行修复
    fix_repository_violations "$service_path"
    fix_autowired_violations "$service_path"
    fix_api_violations "$service_path"
    fix_config_security "$service_path"

    echo ""
    # 修复后统计
    log_info "修复后违规统计:"
    count_violations "$service_path"
    echo ""

    log_success "微服务 $service_name 修复完成！"
    echo "========================================"
    echo ""
}

# 修复所有微服务
fix_all_services() {
    log_info "开始修复所有微服务..."

    local services=(
        "ioedream-common-service"
        "ioedream-access-service"
        "ioedream-attendance-service"
        "ioedream-consume-service"
        "ioedream-video-service"
        "ioedream-visitor-service"
        "ioedream-oa-service"
        "ioedream-device-comm-service"
        "ioedream-gateway-service"
        "microservices-common"
    )

    for service in "${services[@]}"; do
        if [ -d "microservices/$service" ]; then
            fix_microservice "$service"
        else
            log_warning "微服务目录不存在: microservices/$service"
        fi
    done
}

# 显示帮助信息
show_help() {
    echo "IOE-DREAM 架构规范修复工具"
    echo ""
    echo "使用方法:"
    echo "  $0 [service-name]    修复指定微服务"
    echo "  $0 --all            修复所有微服务"
    echo "  $0 --help           显示帮助信息"
    echo ""
    echo "支持的微服务:"
    echo "  ioedream-common-service"
    echo "  ioedream-access-service"
    echo "  ioedream-attendance-service"
    echo "  ioedream-consume-service"
    echo "  ioedream-video-service"
    echo "  ioedream-visitor-service"
    echo "  ioedream-oa-service"
    echo "  ioedream-device-comm-service"
    echo "  ioedream-gateway-service"
    echo "  microservices-common"
}

# 主程序
main() {
    echo "========================================"
    echo "     IOE-DREAM 架构规范修复工具"
    echo "========================================"
    echo ""

    case $1 in
        --help|-h)
            show_help
            ;;
        --all)
            fix_all_services
            ;;
        "")
            log_error "请指定要修复的微服务名称或使用 --all 修复所有服务"
            show_help
            exit 1
            ;;
        *)
            fix_microservice "$1"
            ;;
    esac

    log_success "所有修复任务完成！"
    log_info "建议运行 'mvn clean compile' 验证修复结果"
}

# 执行主程序
main "$@"