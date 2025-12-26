#!/bin/bash

# =============================================================================
# IOE-DREAM Git Pre-commit Hook for SLF4J 规范检查
# =============================================================================
# 版本: v1.0.0
# 作者: IOE-DREAM架构委员会
# 功能: Git提交前自动检查SLF4J使用规范
# 安装: cp scripts/git-pre-commit-hook.sh .git/hooks/pre-commit
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🔍 IOE-DREAM SLF4J Pre-commit 检查${NC}"
echo -e "${BLUE}===============================================${NC}"
echo ""

# 获取即将提交的Java文件
STAGED_JAVA_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep '\.java$' || true)

if [ -z "$STAGED_JAVA_FILES" ]; then
    echo -e "${GREEN}✅ 没有Java文件需要检查${NC}"
    exit 0
fi

echo -e "${YELLOW}📝 检查以下文件:${NC}"
echo "$STAGED_JAVA_FILES"
echo ""

# 违规计数
VIOLATIONS=0

# 检查每个文件
for file in $STAGED_JAVA_FILES; do
    if [ -f "$file" ]; then
        echo -e "${YELLOW}🔍 检查: $file${NC}"

        # 检查LoggerFactory违规
        if grep -q "LoggerFactory.getLogger" "$file"; then
            echo -e "  ${RED}❌ 发现LoggerFactory使用${NC}"
            grep -n "LoggerFactory.getLogger" "$file" | sed 's/^/    /'
            VIOLATIONS=$((VIOLATIONS + 1))
        fi

        # 检查格式问题
        if grep -q "getLogger\s*(" "$file"; then
            echo -e "  ${RED}❌ 发现格式问题${NC}"
            grep -n "getLogger\s*(" "$file" | sed 's/^/    /'
            VIOLATIONS=$((VIOLATIONS + 1))
        fi

        # 检查字符串拼接
        if grep -q "log\.\w\+.*\+.*log\." "$file"; then
            echo -e "  ${RED}❌ 发现字符串拼接${NC}"
            grep -n "log\.\w\+.*\+.*log\." "$file" | sed 's/^/    /'
            VIOLATIONS=$((VIOLATIONS + 1))
        fi

        # 如果没有违规，显示成功
        if ! grep -q "LoggerFactory.getLogger\|getLogger\s*(\|log\.\w\+.*\+.*log\." "$file"; then
            echo -e "  ${GREEN}✅ 符合规范${NC}"
        fi
    fi
done

echo ""
echo -e "${BLUE}===============================================${NC}"

# 结果处理
if [ "$VIOLATIONS" -gt 0 ]; then
    echo -e "${RED}❌ 发现 ${VIOLATIONS} 个SLF4J规范违规${NC}"
    echo ""
    echo -e "${YELLOW}🔧 修复建议:${NC}"
    echo "1. 参考: documentation/technical/SLF4J_UNIFIED_STANDARD.md"
    echo "2. 使用脚本: ./scripts/slf4j-violation-check.sh"
    echo "3. 修复完成后重新提交"
    echo ""
    echo -e "${RED}🚫 提交被阻止，请修复违规问题${NC}"
    exit 1
else
    echo -e "${GREEN}✅ 所有文件符合SLF4J使用规范${NC}"
    echo -e "${GREEN}🎉 可以安全提交${NC}"
    exit 0
fi