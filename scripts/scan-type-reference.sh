#!/bin/bash
################################################################################
# TypeReference使用规范扫描脚本
#
# 用途：
#   扫描代码中未使用TypeReference的泛型类型转换，识别潜在的类型安全问题
#
# 使用方法：
#   chmod +x scripts/scan-type-reference.sh
#   ./scripts/scan-type-reference.sh
#
# 输出：
#   控制台输出扫描结果，高亮显示问题代码位置
#
# @author IOE-DREAM架构团队
# @version 1.0.0
# @since 2025-12-25
################################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🔍 [TypeReference扫描] 开始扫描未使用TypeReference的泛型类型转换...${NC}"
echo ""

# 统计变量
TOTAL_VIOLATIONS=0

echo -e "${BLUE}[扫描1] Map类型转换检查${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

MAP_VIOLATIONS=$(grep -rn "objectMapper\.readValue.*Map\.class" microservices/*/src/ --include="*.java" 2>/dev/null | \
    grep -v "TypeReference" | wc -l)

if [ "$MAP_VIOLATIONS" -gt 0 ]; then
    echo -e "${RED}❌ 发现 $MAP_VIOLATIONS 处Map类型转换未使用TypeReference${NC}"
    echo ""
    echo "问题代码示例："
    grep -rn "objectMapper\.readValue.*Map\.class" microservices/*/src/ --include="*.java" 2>/dev/null | \
        grep -v "TypeReference" | head -10 | while read line; do
        echo -e "${YELLOW}  $line${NC}"
    done
    echo ""
    echo "正确写法示例："
    echo -e "${GREEN}  objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {})${NC}"
    echo ""
    TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + MAP_VIOLATIONS))
else
    echo -e "${GREEN}✅ 无Map类型转换问题${NC}"
fi

echo ""

echo -e "${BLUE}[扫描2] List类型转换检查${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

LIST_VIOLATIONS=$(grep -rn "objectMapper\.readValue.*List\.class" microservices/*/src/ --include="*.java" 2>/dev/null | \
    grep -v "TypeReference" | wc -l)

if [ "$LIST_VIOLATIONS" -gt 0 ]; then
    echo -e "${RED}❌ 发现 $LIST_VIOLATIONS 处List类型转换未使用TypeReference${NC}"
    echo ""
    echo "问题代码示例："
    grep -rn "objectMapper\.readValue.*List\.class" microservices/*/src/ --include="*.java" 2>/dev/null | \
        grep -v "TypeReference" | head -10 | while read line; do
        echo -e "${YELLOW}  $line${NC}"
    done
    echo ""
    echo "正确写法示例："
    echo -e "${GREEN}  objectMapper.readValue(json, new TypeReference<List<Object>>() {})${NC}"
    echo ""
    TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + LIST_VIOLATIONS))
else
    echo -e "${GREEN}✅ 无List类型转换问题${NC}"
fi

echo ""

echo -e "${BLUE}[扫描3] Set类型转换检查${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

SET_VIOLATIONS=$(grep -rn "objectMapper\.readValue.*Set\.class" microservices/*/src/ --include="*.java" 2>/dev/null | \
    grep -v "TypeReference" | wc -l)

if [ "$SET_VIOLATIONS" -gt 0 ]; then
    echo -e "${RED}❌ 发现 $SET_VIOLATIONS 处Set类型转换未使用TypeReference${NC}"
    echo ""
    echo "问题代码示例："
    grep -rn "objectMapper\.readValue.*Set\.class" microservices/*/src/ --include="*.java" 2>/dev/null | \
        grep -v "TypeReference" | head -10 | while read line; do
        echo -e "${YELLOW}  $line${NC}"
    done
    echo ""
    echo "正确写法示例："
    echo -e "${GREEN}  objectMapper.readValue(json, new TypeReference<Set<Object>>() {})${NC}"
    echo ""
    TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + SET_VIOLATIONS))
else
    echo -e "${GREEN}✅ 无Set类型转换问题${NC}"
fi

echo ""

echo -e "${BLUE}[扫描4] Collection类型转换检查${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

COLLECTION_VIOLATIONS=$(grep -rn "objectMapper\.readValue.*Collection\.class" microservices/*/src/ --include="*.java" 2>/dev/null | \
    grep -v "TypeReference" | wc -l)

if [ "$COLLECTION_VIOLATIONS" -gt 0 ]; then
    echo -e "${RED}❌ 发现 $COLLECTION_VIOLATIONS 处Collection类型转换未使用TypeReference${NC}"
    echo ""
    echo "问题代码示例："
    grep -rn "objectMapper\.readValue.*Collection\.class" microservices/*/src/ --include="*.java" 2>/dev/null | \
        grep -v "TypeReference" | head -10 | while read line; do
        echo -e "${YELLOW}  $line${NC}"
    done
    echo ""
    echo "正确写法示例："
    echo -e "${GREEN}  objectMapper.readValue(json, new TypeReference<List<Object>>() {})${NC}"
    echo ""
    TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + COLLECTION_VIOLATIONS))
else
    echo -e "${GREEN}✅ 无Collection类型转换问题${NC}"
fi

echo ""

echo -e "${BLUE}[扫描5] 泛型ResponseDTO类型转换检查${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

RESPONSE_VIOLATIONS=$(grep -rn "objectMapper\.readValue.*ResponseDTO\.class" microservices/*/src/ --include="*.java" 2>/dev/null | \
    grep -v "TypeReference" | wc -l)

if [ "$RESPONSE_VIOLATIONS" -gt 0 ]; then
    echo -e "${RED}❌ 发现 $RESPONSE_VIOLATIONS 处ResponseDTO类型转换未使用TypeReference${NC}"
    echo ""
    echo "问题代码示例："
    grep -rn "objectMapper\.readValue.*ResponseDTO\.class" microservices/*/src/ --include="*.java" 2>/dev/null | \
        grep -v "TypeReference" | head -10 | while read line; do
        echo -e "${YELLOW}  $line${NC}"
    done
    echo ""
    echo "正确写法示例："
    echo -e "${GREEN}  objectMapper.readValue(json, new TypeReference<ResponseDTO<UserVO>>() {})${NC}"
    echo ""
    TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + RESPONSE_VIOLATIONS))
else
    echo -e "${GREEN}✅ 无ResponseDTO类型转换问题${NC}"
fi

echo ""

echo -e "${BLUE}[扫描6] PageResult类型转换检查${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

PAGERESULT_VIOLATIONS=$(grep -rn "objectMapper\.readValue.*PageResult\.class" microservices/*/src/ --include="*.java" 2>/dev/null | \
    grep -v "TypeReference" | wc -l)

if [ "$PAGERESULT_VIOLATIONS" -gt 0 ]; then
    echo -e "${RED}❌ 发现 $PAGERESULT_VIOLATIONS 处PageResult类型转换未使用TypeReference${NC}"
    echo ""
    echo "问题代码示例："
    grep -rn "objectMapper\.readValue.*PageResult\.class" microservices/*/src/ --include="*.java" 2>/dev/null | \
        grep -v "TypeReference" | head -10 | while read line; do
        echo -e "${YELLOW}  $line${NC}"
    done
    echo ""
    echo "正确写法示例："
    echo -e "${GREEN}  objectMapper.readValue(json, new TypeReference<PageResult<UserVO>>() {})${NC}"
    echo ""
    TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + PAGERESULT_VIOLATIONS))
else
    echo -e "${GREEN}✅ 无PageResult类型转换问题${NC}"
fi

echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BLUE}📊 [扫描总结]${NC}"
echo ""

if [ $TOTAL_VIOLATIONS -eq 0 ]; then
    echo -e "${GREEN}✅ 恭喜！未发现TypeReference使用问题${NC}"
    echo ""
    echo "所有泛型类型转换都正确使用了TypeReference，类型安全性良好！"
else
    echo -e "${RED}❌ 发现 $TOTAL_VIOLATIONS 处类型安全问题${NC}"
    echo ""
    echo "问题分布："
    echo "  - Map类型: $MAP_VIOLATIONS 处"
    echo "  - List类型: $LIST_VIOLATIONS 处"
    echo "  - Set类型: $SET_VIOLATIONS 处"
    echo "  - Collection类型: $COLLECTION_VIOLATIONS 处"
    echo "  - ResponseDTO类型: $RESPONSE_VIOLATIONS 处"
    echo "  - PageResult类型: $PAGERESULT_VIOLATIONS 处"
    echo ""
    echo -e "${YELLOW}⚠️  建议措施：${NC}"
    echo "  1. 运行 ./scripts/fix-type-reference.sh 自动修复"
    echo "  2. 或手动修改为使用TypeReference的正确写法"
    echo "  3. 修复后运行单元测试验证"
fi

echo ""
echo -e "${BLUE}🔗 相关文档：${NC}"
echo "  - CLAUDE.md → API设计规范 → 类型转换章节"
echo ""

exit $TOTAL_VIOLATIONS
