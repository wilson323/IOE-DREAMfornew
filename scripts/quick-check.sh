#!/bin/bash
# SmartAdmin 快速检查脚本
# 老王的快速检查工具 - 每次提交前必须执行
# 版本: v1.0.0
# 更新: 2025-11-14

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 项目路径
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)
BACKEND_DIR="$PROJECT_ROOT/smart-admin-api-java17-springboot3"

echo -e "${BLUE}🚀 SmartAdmin 快速检查${NC}"
echo -e "${BLUE}$(printf '─%.0s' {1..40})${NC}"

ERROR_COUNT=0

# 1. 检查Jakarta规范（EE命名空间禁用 + @Autowired禁用）
echo -e "\n🔍 检查 Jakarta 规范守卫..."
if bash "$PROJECT_ROOT/scripts/jakarta-guard.sh"; then
    echo -e "${GREEN}✅ Jakarta 规范守卫通过${NC}"
else
    echo -e "${RED}❌ Jakarta 规范守卫失败${NC}"
    ERROR_COUNT=$((ERROR_COUNT + 1))
fi

# 2. 保留兼容输出（历史统计，仅提示，不影响守卫判断）
echo -e "\nℹ️ 兼容统计（不作为守卫判定，仅提示）..."
javax_count=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "import javax\." {} \; 2>/dev/null | wc -l)
echo -e " - import javax.* 引用统计: ${YELLOW}${javax_count}${NC}"
autowired_count=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
echo -e " - @Autowired 引用统计: ${YELLOW}${autowired_count}${NC}"

# 3. 检查System.out.println使用（必须为0）
echo -e "\n🔍 检查 System.out.println 使用..."
system_out_count=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "System\.out\.println\|System\.err\.println" {} \; 2>/dev/null | wc -l)
if [ "$system_out_count" -eq 0 ]; then
    echo -e "${GREEN}✅ System.out.println 检查通过 (0个文件)${NC}"
else
    echo -e "${RED}❌ 发现 $system_out_count 个文件使用 System.out.println${NC}"
    ERROR_COUNT=$((ERROR_COUNT + 1))
fi

# 4. 检查编译状态
echo -e "\n🔍 检查编译状态..."
cd "$BACKEND_DIR"
if mvn clean compile -q -DskipTests 2>/dev/null; then
    echo -e "${GREEN}✅ 编译检查通过${NC}"
else
    echo -e "${RED}❌ 编译失败${NC}"
    ERROR_COUNT=$((ERROR_COUNT + 1))
fi

# 结果输出
echo -e "\n${BLUE}📊 快速检查结果${NC}"
echo -e "${BLUE}$(printf '─%.0s' {1..40})${NC}"

if [ $ERROR_COUNT -eq 0 ]; then
    echo -e "${GREEN}🎉 恭喜！快速检查全部通过，可以提交代码！${NC}"
    exit 0
else
    echo -e "${RED}🚨 发现 $ERROR_COUNT 个问题，禁止提交代码！${NC}"
    echo -e "${YELLOW}💡 运行 ./scripts/enforce-standards.sh 查看详细问题和修复指南${NC}"
    exit 1
fi