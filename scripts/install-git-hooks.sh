#!/bin/bash
################################################################################
# Git Pre-commit Hook 安装脚本
#
# 用途：将pre-commit hook安装到.git/hooks目录
#
# 使用方法：
#   chmod +x scripts/install-git-hooks.sh
#   ./scripts/install-git-hooks.sh
#
# @author IOE-DREAM架构团队
# @version 1.0.0
# @since 2025-12-25
################################################################################

set -e

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🔧 [安装] 安装Git Pre-commit Hook...${NC}"

# 检查.git目录是否存在
if [ ! -d ".git" ]; then
    echo "❌ 错误：当前目录不是Git仓库根目录"
    echo "请切换到项目根目录执行此脚本"
    exit 1
fi

# 复制hook文件
if [ -f ".git/hooks/pre-commit-hook.sh" ]; then
    cp .git/hooks/pre-commit-hook.sh .git/hooks/pre-commit
    chmod +x .git/hooks/pre-commit

    echo -e "${GREEN}✅ [安装] Pre-commit Hook安装成功${NC}"
    echo ""
    echo "📋 Hook功能："
    echo "  - 检查包路径规范（禁止重复common等）"
    echo "  - 检查不存在的类（如AtomicDouble）"
    echo "  - 检查API使用规范（建议使用TypeReference）"
    echo "  - 检查@TableId注解规范"
    echo "  - 检查@Repository注解使用"
    echo ""
    echo "🔍 验证安装："
    echo "  ls -la .git/hooks/pre-commit"

    if [ -f ".git/hooks/pre-commit" ]; then
        echo -e "${GREEN}  ✓ Hook文件存在${NC}"
    else
        echo -e "${RED}  ✗ Hook文件不存在${NC}"
        exit 1
    fi
else
    echo "❌ 错误：pre-commit-hook.sh文件不存在"
    exit 1
fi

echo ""
echo -e "${GREEN}🎉 安装完成！每次提交代码时将自动执行架构合规性检查${NC}"
