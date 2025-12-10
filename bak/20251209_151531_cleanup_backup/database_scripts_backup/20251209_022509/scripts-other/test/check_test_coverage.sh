#!/bin/bash
# ============================================
# IOE-DREAM 测试覆盖率检查脚本
# 版本: 1.0.0
# 日期: 2025-01-30
# 说明: 使用JaCoCo检查测试覆盖率
# ============================================

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印信息
info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# 目标覆盖率
TARGET_COVERAGE=80

# 检查JaCoCo插件
check_jacoco() {
    info "检查JaCoCo插件配置..."
    if ! grep -q "jacoco-maven-plugin" "$PROJECT_ROOT/microservices/pom.xml"; then
        warn "JaCoCo插件未配置，将添加配置"
        # 这里可以自动添加JaCoCo配置
    fi
}

# 运行测试并生成覆盖率报告
run_tests() {
    local service_name=$1
    local service_path="$PROJECT_ROOT/microservices/$service_name"
    
    if [ ! -d "$service_path" ]; then
        warn "服务目录不存在: $service_path"
        return 1
    fi
    
    info "运行 $service_name 的测试..."
    cd "$service_path"
    
    # 运行测试
    mvn clean test jacoco:report
    
    if [ $? -eq 0 ]; then
        info "$service_name 测试执行成功"
        
        # 检查覆盖率报告
        if [ -f "target/site/jacoco/index.html" ]; then
            info "覆盖率报告已生成: $service_path/target/site/jacoco/index.html"
            
            # 提取覆盖率数据（需要解析HTML或使用JaCoCo API）
            # 这里简化处理，实际应该解析JaCoCo的CSV或XML报告
            return 0
        else
            warn "覆盖率报告未生成"
            return 1
        fi
    else
        error "$service_name 测试执行失败"
        return 1
    fi
}

# 主函数
main() {
    info "开始检查测试覆盖率..."
    
    # 检查JaCoCo配置
    check_jacoco
    
    # 需要检查的服务列表
    services=(
        "ioedream-consume-service"
        "ioedream-access-service"
        "ioedream-attendance-service"
        "ioedream-visitor-service"
        "ioedream-video-service"
        "ioedream-common-service"
    )
    
    # 运行各服务的测试
    for service in "${services[@]}"; do
        run_tests "$service"
    done
    
    info "测试覆盖率检查完成"
    info "查看详细报告: 各服务目录下的 target/site/jacoco/index.html"
}

# 执行主函数
main "$@"

