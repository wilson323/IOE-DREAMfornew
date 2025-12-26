#!/bin/bash
# =====================================================
# Entity位置验证脚本
# 版本: v1.0.0
# 用途: 检查是否有Entity违规存储在业务服务中
# 执行: ./scripts/verify-entity-locations.sh
# =====================================================

set -e

echo "========================================="
echo "🔍 Entity位置规范检查"
echo "========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 定义业务服务列表
BUSINESS_SERVICES=(
    "ioedream-access-service"
    "ioedream-attendance-service"
    "ioedream-consume-service"
    "ioedream-video-service"
    "ioedream-visitor-service"
    "ioedream-device-comm-service"
    "ioedream-oa-service"
)

# 检查标志
VIOLATIONS_FOUND=false
TOTAL_VIOLATIONS=0

# 遍历所有业务服务
for service in "${BUSINESS_SERVICES[@]}"; do
    echo -n "检查服务: $service ... "

    # 查找该服务中的Entity文件（排除备份文件）
    entities=$(find microservices/$service/src -name "*Entity.java" -type f \
        ! -name "*.disabled*" \
        ! -name "*.before*" \
        ! -name "*.after*" \
        ! -name "*.backup*" \
        2>/dev/null || true)

    if [ -n "$entities" ]; then
        echo -e "${RED}❌ 发现违规${NC}"
        echo "$entities" | while read -r file; do
            echo "  - $file"
            ((TOTAL_VIOLATIONS++))
        done
        VIOLATIONS_FOUND=true
    else
        echo -e "${GREEN}✅ 无Entity重复${NC}"
    fi
done

echo ""
echo "========================================="

# 检查导入语句
echo ""
echo "检查错误导入语句..."
WRONG_IMPORTS=0

for service in "${BUSINESS_SERVICES[@]}"; do
    if [ -d "microservices/$service" ]; then
        service_name=$(echo $service | sed 's/ioedream-//g' | sed 's/-service//g')
        wrong=$(find microservices/$service/src -name "*.java" -type f \
            ! -name "*.disabled*" \
            ! -name "*.before*" \
            ! -name "*.after*" \
            2>/dev/null | xargs grep -l "import net.lab1024.sa.$service_name.entity" 2>/dev/null || true)

        if [ -n "$wrong" ]; then
            echo -e "${YELLOW}⚠️  $service: 发现错误导入${NC}"
            echo "$wrong" | while read -r file; do
                echo "  - $file"
                ((WRONG_IMPORTS++))
            done
        fi
    fi
done

if [ $WRONG_IMPORTS -eq 0 ]; then
    echo -e "${GREEN}✅ 无错误导入${NC}"
fi

echo ""
echo "========================================="
echo "检查结果"
echo "========================================="

if [ "$VIOLATIONS_FOUND" = true ] || [ $WRONG_IMPORTS -gt 0 ]; then
    echo -e "${RED}❌ 检查失败：发现规范违规！${NC}"
    echo ""
    echo "违规统计："
    echo "  - Entity重复文件: $TOTAL_VIOLATIONS 个"
    echo "  - 错误导入语句: $WRONG_IMPORTS 个"
    echo ""
    echo "📋 规范要求："
    echo "   - 所有Entity必须存储在 microservices-common-entity 模块"
    echo "   - 业务服务中禁止存储Entity类"
    echo "   - 导入语句必须使用: import net.lab1024.sa.common.entity.xxx.YyyEntity"
    echo ""
    echo "📖 详细规范："
    echo "   参考 CLAUDE.md - Entity存储规范（P0级强制）"
    echo ""
    exit 1
else
    echo -e "${GREEN}✅ 检查通过：所有Entity存储位置正确${NC}"
    echo ""
    echo "📊 检查统计："
    echo "  - 检查服务数: ${#BUSINESS_SERVICES[@]} 个"
    echo "  - 违规文件数: 0 个"
    echo "  - 错误导入数: 0 个"
    echo ""
    exit 0
fi
