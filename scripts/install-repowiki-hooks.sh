#!/bin/bash

# ===================================================================
# IOE-DREAM 项目 repowiki Git Hooks 安装脚本
#
# 功能:
# 1. 安装repowiki合规性检查的Git hooks
# 2. 配置pre-commit检查
# 3. 设置Git配置选项
#
# 作者: IOE-DREAM Team
# 版本: v1.0
# 日期: 2025-11-24
# ===================================================================

set -euo pipefail

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT=$(pwd)
HOOKS_DIR="$PROJECT_ROOT/.git/hooks"
SCRIPTS_DIR="$PROJECT_ROOT/scripts"

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[⚠]${NC} $1"
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# 检查是否在Git仓库中
check_git_repo() {
    if [ ! -d ".git" ]; then
        log_error "当前目录不是Git仓库"
        exit 1
    fi
    log_success "Git仓库检查通过"
}

# 创建hooks目录
create_hooks_dir() {
    mkdir -p "$HOOKS_DIR"
    log_success "Git hooks目录已准备: $HOOKS_DIR"
}

# 安装pre-commit hook
install_pre_commit_hook() {
    local pre_commit_file="$HOOKS_DIR/pre-commit"
    local source_file="$SCRIPTS_DIR/pre-commit-repowiki-check.sh"

    if [ ! -f "$source_file" ]; then
        log_error "找不到pre-commit检查脚本: $source_file"
        exit 1
    fi

    # 备份现有的pre-commit hook
    if [ -f "$pre_commit_file" ]; then
        backup_file="$pre_commit_file.backup.$(date +%Y%m%d_%H%M%S)"
        cp "$pre_commit_file" "$backup_file"
        log_warning "已备份现有pre-commit hook: $backup_file"
    fi

    # 安装新的pre-commit hook
    cp "$source_file" "$pre_commit_file"
    chmod +x "$pre_commit_file"

    log_success "pre-commit hook已安装"
}

# 安装commit-msg hook（可选）
install_commit_msg_hook() {
    local commit_msg_file="$HOOKS_DIR/commit-msg"

    # 创建简单的commit-msg hook检查
    cat > "$commit_msg_file" << 'EOF'
#!/bin/bash

# 简单的commit message检查
commit_regex='^(feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert)(\(.+\))?: .{1,50}'

if ! grep -qE "$commit_regex" "$1"; then
    echo "❌ Commit message 格式不正确！"
    echo ""
    echo "请使用以下格式之一："
    echo "  feat: 新功能"
    echo "  fix: 修复问题"
    echo "  docs: 文档更新"
    echo "  style: 代码格式化"
    echo "  refactor: 重构代码"
    echo "  test: 测试相关"
    echo "  chore: 构建/工具相关"
    echo ""
    echo "示例: feat: 添加用户登录功能"
    echo "示例: fix: 修复密码验证bug"
    exit 1
fi

exit 0
EOF

    chmod +x "$commit_msg_file"
    log_success "commit-msg hook已安装"
}

# 配置Git设置
configure_git_settings() {
    # 设置Git core.autocrlf为false（避免跨平台问题）
    git config core.autocrlf false

    # 设置Git hooks路径
    git config core.hooksPath "$HOOKS_DIR"

    log_success "Git配置已完成"
}

# 测试hooks安装
test_hooks_installation() {
    log_info "测试hooks安装..."

    # 检查pre-commit hook
    if [ -f "$HOOKS_DIR/pre-commit" ] && [ -x "$HOOKS_DIR/pre-commit" ]; then
        log_success "pre-commit hook安装成功"
    else
        log_error "pre-commit hook安装失败"
        return 1
    fi

    # 检查commit-msg hook
    if [ -f "$HOOKS_DIR/commit-msg" ] && [ -x "$HOOKS_DIR/commit-msg" ]; then
        log_success "commit-msg hook安装成功"
    else
        log_warning "commit-msg hook安装失败"
    fi

    return 0
}

# 显示安装完成信息
show_completion_info() {
    echo ""
    echo "🎉 repowiki Git Hooks 安装完成！"
    echo "=================================="
    echo ""
    echo "已安装的hooks："
    echo "  ✅ pre-commit: 提交前repowiki合规性检查"
    echo "  ✅ commit-msg: 提交信息格式检查"
    echo ""
    echo "下次提交时，hooks会自动运行："
    echo "  git add ."
    echo "  git commit -m 'feat: 添加新功能'"
    echo ""
    echo "如需跳过检查（不推荐）："
    echo "  git commit --no-verify -m 'commit message'"
    echo ""
    echo "如需卸载hooks："
    echo "  rm -f .git/hooks/pre-commit .git/hooks/commit-msg"
    echo ""
}

# 主函数
main() {
    echo "🚀 安装 IOE-DREAM repowiki Git Hooks"
    echo "===================================="
    echo ""

    # 检查Git仓库
    check_git_repo

    # 创建hooks目录
    create_hooks_dir

    # 安装pre-commit hook
    install_pre_commit_hook

    # 安装commit-msg hook
    install_commit_msg_hook

    # 配置Git设置
    configure_git_settings

    # 测试安装
    if test_hooks_installation; then
        # 显示完成信息
        show_completion_info
        exit 0
    else
        log_error "Hooks安装失败"
        exit 1
    fi
}

# 执行主函数
main "$@"