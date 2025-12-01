#!/bin/bash

# ==============================================================================
# IOE-DREAM 脚本权限设置和初始化
# ==============================================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

log_info "开始设置脚本权限..."

# 1. 为Shell脚本添加执行权限
log_info "为Shell脚本添加执行权限..."
find scripts/ -name "*.sh" -exec chmod +x {} \;
log_success "Shell脚本权限设置完成"

# 2. 为Python脚本添加执行权限
log_info "为Python脚本添加执行权限..."
find scripts/ -name "*.py" -exec chmod +x {} \;
log_success "Python脚本权限设置完成"

# 3. 创建智能开发助手的软链接（可选）
if [ -f "scripts/smart-dev-helper.sh" ]; then
    if [ ! -L "smart-dev" ]; then
        ln -s scripts/smart-dev-helper.sh smart-dev
        log_success "创建智能开发助手快捷命令: ./smart-dev"
    fi
fi

# 4. 验证关键脚本
log_info "验证关键脚本..."

# 检查智能开发助手
if [ -x "scripts/smart-dev-helper.sh" ]; then
    log_success "智能开发助手脚本可执行"
else
    log_error "智能开发助手脚本不可执行"
fi

# 检查质量检查脚本
if [ -x "scripts/enforce-standards.sh" ]; then
    log_success "质量检查脚本可执行"
else
    log_warning "质量检查脚本不存在或不可执行"
fi

# 5. 测试智能开发助手
log_info "测试智能开发助手..."
if ./scripts/smart-dev-helper.sh help > /dev/null 2>&1; then
    log_success "智能开发助手测试通过"
else
    log_error "智能开发助手测试失败"
fi

echo ""
log_success "脚本权限设置完成！"
echo ""
echo "现在可以使用以下命令："
echo "  ./scripts/smart-dev-helper.sh help      # 查看帮助"
echo "  ./scripts/smart-dev-helper.sh quick-check # 快速检查"
echo "  ./smart-dev help                        # 快捷方式（如果创建了软链接）"
echo ""
log_info "建议运行 './scripts/smart-dev-helper.sh quick-check' 验证环境配置"