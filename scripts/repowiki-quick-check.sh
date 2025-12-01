#!/bin/bash

# IOE-DREAM repowiki 快速合规性检查
set -e

echo "🔍 IOE-DREAM repowiki 快速合规性检查"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PROJECT_ROOT=$(pwd)
ISSUES_FOUND=0

# 检查函数
check_jakarta() {
    echo -e "\n📦 检查 Jakarta EE 包名合规性..."

    local violations=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "javax\.\(annotation\|validation\|persistence\|servlet\)" {} \; 2>/dev/null | wc -l)

    if [ "$violations" -eq 0 ]; then
        echo -e "  ${GREEN}✅ Jakarta EE 包名合规 (0个违规文件)${NC}"
    else
        echo -e "  ${RED}❌ 发现 $violations 个 Jakarta EE 包违规文件${NC}"
        find "$PROJECT_ROOT" -name "*.java" -exec grep -l "javax\.\(annotation\|validation\|persistence\|servlet\)" {} \; 2>/dev/null | head -5
        ((ISSUES_FOUND++))
    fi
}

check_repository() {
    echo -e "\n📁 检查 DAO 命名规范..."

    local repo_files=$(find "$PROJECT_ROOT" -name "*Repository.java" 2>/dev/null | wc -l)

    if [ "$repo_files" -eq 0 ]; then
        echo -e "  ${GREEN}✅ DAO 命名规范 (0个Repository文件)${NC}"
    else
        echo -e "  ${RED}❌ 发现 $repo_files 个Repository文件需要重命名${NC}"
        find "$PROJECT_ROOT" -name "*Repository.java" 2>/dev/null | head -5
        ((ISSUES_FOUND++))
    fi
}

check_autowired() {
    echo -e "\n🔌 检查依赖注入规范..."

    local autowired_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)

    if [ "$autowired_files" -eq 0 ]; then
        echo -e "  ${GREEN}✅ 依赖注入规范 (使用@Resource)${NC}"
    else
        echo -e "  ${RED}❌ 发现 $autowired_files 个@Autowired使用${NC}"
        find "$PROJECT_ROOT" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | head -5
        ((ISSUES_FOUND++))
    fi
}

check_nested_paths() {
    echo -e "\n🗂️ 检查项目结构..."

    local nested_paths=$(find "$PROJECT_ROOT" -path "*module/*/net/*" -type f 2>/dev/null | wc -l)

    if [ "$nested_paths" -eq 0 ]; then
        echo -e "  ${GREEN}✅ 项目结构规范 (0个嵌套路径)${NC}"
    else
        echo -e "  ${RED}❌ 发现 $nested_paths 个异常嵌套路径${NC}"
        ((ISSUES_FOUND++))
    fi
}

check_encoding() {
    echo -e "\n📝 检查文件编码..."

    local bom_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; 2>/dev/null | wc -l)

    if [ "$bom_files" -eq 0 ]; then
        echo -e "  ${GREEN}✅ 文件编码规范 (无BOM标记)${NC}"
    else
        echo -e "  ${YELLOW}⚠️  发现 $bom_files 个BOM标记文件${NC}"
    fi
}

# 执行检查
check_jakarta
check_repository
check_autowired
check_nested_paths
check_encoding

echo -e "\n📊 检查总结"
echo "=========================================="

if [ "$ISSUES_FOUND" -eq 0 ]; then
    echo -e "${GREEN}🎉 所有检查项都通过！repowiki 规范合规${NC}"
    exit 0
else
    echo -e "${RED}⚠️  发现 $ISSUES_FOUND 个问题需要修复${NC}"
    exit 1
fi