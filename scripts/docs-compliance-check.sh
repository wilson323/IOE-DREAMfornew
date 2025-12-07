#!/bin/bash

# IOE-DREAM 文档合规性自动检查脚本
# 版本: v1.0.0
# 用途: 检查所有技术文档是否符合IOE-DREAM架构规范

echo "🔍 开始IOE-DREAM文档合规性检查..."
echo "检查时间: $(date)"
echo "=================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 统计变量
TOTAL_DOCS=0
P0_ERRORS=0
P1_ERRORS=0
P2_ERRORS=0
COMPLIANT_DOCS=0

# 检查函数
check_compliance() {
    local file=$1
    local filename=$(basename "$file")

    echo "📄 检查文档: $filename"
    TOTAL_DOCS=$((TOTAL_DOCS + 1))

    local has_p0_error=false
    local has_p1_error=false
    local has_p2_error=false

    # P0级检查 - 架构合规性
    if ! grep -q "七微服务" "$file" 2>/dev/null; then
        echo -e "  ${RED}❌ P0错误: 缺少七微服务架构描述${NC}"
        P0_ERRORS=$((P0_ERRORS + 1))
        has_p0_error=true
    fi

    if ! grep -q "四层架构" "$file" 2>/dev/null; then
        echo -e "  ${RED}❌ P0错误: 缺少四层架构规范${NC}"
        P0_ERRORS=$((P0_ERRORS + 1))
        has_p0_error=true
    fi

    # P0级检查 - 零容忍规则
    if grep -q "@Autowired" "$file" 2>/dev/null && ! grep -q "禁止.*@Autowired" "$file" 2>/dev/null; then
        echo -e "  ${RED}❌ P0错误: 发现@Autowired违规且未标记为禁止${NC}"
        P0_ERRORS=$((P0_ERRORS + 1))
        has_p0_error=true
    fi

    if grep -q "@Repository" "$file" 2>/dev/null && ! grep -q "禁止.*@Repository" "$file" 2>/dev/null; then
        echo -e "  ${RED}❌ P0错误: 发现@Repository违规且未标记为禁止${NC}"
        P0_ERRORS=$((P0_ERRORS + 1))
        has_p0_error=true
    fi

    # P1级检查 - 文档质量
    if ! grep -q "```mermaid" "$file" 2>/dev/null && [[ "$file" == *"架构"* ]]; then
        echo -e "  ${YELLOW}⚠️  P1问题: 架构文档缺少图表${NC}"
        P1_ERRORS=$((P1_ERRORS + 1))
        has_p1_error=true
    fi

    if ! grep -q "```java" "$file" 2>/dev/null && [[ "$file" == *"模板"* || "$file" == *"规范"* ]]; then
        echo -e "  ${YELLOW}⚠️  P1问题: 规范文档缺少代码示例${NC}"
        P1_ERRORS=$((P1_ERRORS + 1))
        has_p1_error=true
    fi

    # P2级检查 - 文档完整性
    if ! grep -q "检查清单" "$file" 2>/dev/null; then
        echo -e "  ${YELLOW}ℹ️  P2建议: 可添加合规检查清单${NC}"
        P2_ERRORS=$((P2_ERRORS + 1))
        has_p2_error=true
    fi

    # 判断合规状态
    if [ "$has_p0_error" = false ] && [ "$has_p1_error" = false ]; then
        echo -e "  ${GREEN}✅ 文档合规${NC}"
        COMPLIANT_DOCS=$((COMPLIANT_DOCS + 1))
    fi

    echo ""
}

# 查找所有Markdown文档
echo "🔍 扫描技术文档..."
find documentation/ -name "*.md" -type f | while read -r file; do
    # 跳过README和特定文件
    if [[ "$file" == *"README.md" ]] || [[ "$file" == *"LICENSE"* ]]; then
        continue
    fi
    check_compliance "$file"
done

# 生成报告
echo "=================================="
echo "📊 检查结果统计:"
echo -e "总文档数量: ${GREEN}$TOTAL_DOCS${NC}"
echo -e "合规文档数量: ${GREEN}$COMPLIANT_DOCS${NC}"
echo -e "合规率: ${GREEN}$(( COMPLIANT_DOCS * 100 / TOTAL_DOCS ))%${NC}"
echo ""
echo -e "P0级错误: ${RED}$P0_ERRORS${NC} (必须修复)"
echo -e "P1级问题: ${YELLOW}$P1_ERRORS${NC} (建议修复)"
echo -e "P2级建议: ${YELLOW}$P2_ERRORS${NC} (可选修复)"

# 设置退出码
if [ $P0_ERRORS -gt 0 ]; then
    echo -e "\n${RED}❌ 检查失败: 发现P0级问题，必须立即修复${NC}"
    exit 1
elif [ $P1_ERRORS -gt 0 ]; then
    echo -e "\n${YELLOW}⚠️  检查通过但有P1级问题建议修复${NC}"
    exit 0
else
    echo -e "\n${GREEN}✅ 检查通过: 所有文档符合基本要求${NC}"
    exit 0
fi