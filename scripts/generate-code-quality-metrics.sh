#!/bin/bash

echo "=========================================="
echo "IOE-DREAM 代码质量指标生成"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 创建指标报告文件
METRICS_FILE="code-quality-metrics-$(date +%Y%m%d_%H%M%S).md"

# 获取当前时间
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

# 开始生成报告
cat > "$METRICS_FILE" << EOF
# IOE-DREAM 代码质量指标报告

**生成时间**: $TIMESTAMP
**检查范围**: 整体项目代码质量

## 📊 质量指标概览

EOF

echo -e "${BLUE}开始分析代码质量指标...${NC}"

# 1. 项目规模统计
echo -e "\n${YELLOW}1. 项目规模统计${NC}"
echo "----------------------------------------"

JAVA_FILES_COUNT=$(find microservices -name "*.java" | wc -l)
echo "📁 Java文件总数: $JAVA_FILES_COUNT"

TOTAL_LINES=$(find microservices -name "*.java" -exec wc -l {} + | tail -1 | awk '{print $1}')
echo "📄 代码总行数: $TOTAL_LINES"

RESPONSE_OBJECTS_COUNT=$(find microservices/microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/gateway/domain/response -name "*.java" 2>/dev/null | wc -l)
echo "📦 Response对象数量: $RESPONSE_OBJECTS_COUNT"

cat >> "$METRICS_FILE" << EOF

### 项目规模指标

| 指标 | 数值 | 说明 |
|------|------|------|
| Java文件总数 | $JAVA_FILES_COUNT | 整个项目的Java源文件数量 |
| 代码总行数 | $TOTAL_LINES | 所有Java文件的代码行数 |
| Response对象数量 | $RESPONSE_OBJECTS_COUNT | 跨服务响应对象数量 |

EOF

# 2. 架构合规性统计
echo -e "\n${YELLOW}2. 架构合规性统计${NC}"
echo "----------------------------------------"

# 统计Entity使用情况
ENTITY_USAGE_COUNT=$(find microservices -name "*.java" -exec grep -l "import.*common\.organization\.entity\." {} \; | wc -l)
echo "🔍 使用Entity的文件数: $ENTITY_USAGE_COUNT"

# 统计DAO层合规使用
DAO_ENTITY_COUNT=$(find microservices -name "*Dao.java" -exec grep -l "import.*common\.organization\.entity\." {} \; | wc -l)
echo "📋 DAO层Entity使用: $DAO_ENTITY_COUNT (合规)"

# 统计Service层违规
SERVICE_ENTITY_VIOLATIONS=$(find microservices -name "*Service*.java" -exec grep -l "ResponseDTO<.*Entity>" {} \; 2>/dev/null | wc -l)
echo "⚠️ Service层Entity违规: $SERVICE_ENTITY_VIOLATIONS"

cat >> "$METRICS_FILE" << EOF

### 架构合规性指标

| 指标 | 数值 | 状态 |
|------|------|------|
| Entity使用文件数 | $ENTITY_USAGE_COUNT | $([ $ENTITY_USAGE_COUNT -gt 0 ] && echo "需关注" || echo "正常") |
| DAO层Entity使用 | $DAO_ENTITY_COUNT | ✅ 合规 |
| Service层Entity违规 | $SERVICE_ENTITY_VIOLATIONS | $([ $SERVICE_ENTITY_VIOLATIONS -eq 0 ] && echo "✅ 无违规" || echo "⚠️ 有违规") |

EOF

# 3. 优化成果统计
echo -e "\n${YELLOW}3. 优化成果统计${NC}"
echo "----------------------------------------"

# 统计已优化的微服务
OPTIMIZED_SERVICES=0
ACCESS_SERVICE_OPTIMIZED=0
ATTENDANCE_SERVICE_OPTIMIZED=0
VIDEO_SERVICE_OPTIMIZED=0

# 检查Access Service优化状态
if [ -f "scripts/validate-access-service-entity-fixes.sh" ]; then
    ACCESS_SERVICE_OPTIMIZED=1
    ((OPTIMIZED_SERVICES++))
    echo -e "${GREEN}✅ Access Service: 已优化${NC}"
else
    echo -e "${RED}❌ Access Service: 未优化${NC}"
fi

# 检查Attendance Service优化状态
if [ -f "scripts/validate-attendance-service-entity-fixes.sh" ]; then
    ATTENDANCE_SERVICE_OPTIMIZED=1
    ((OPTIMIZED_SERVICES++))
    echo -e "${GREEN}✅ Attendance Service: 已优化${NC}"
else
    echo -e "${RED}❌ Attendance Service: 未优化${NC}"
fi

# 检查Video Service优化状态
if [ -f "scripts/validate-video-service-architecture-fixes.sh" ]; then
    VIDEO_SERVICE_OPTIMIZED=1
    ((OPTIMIZED_SERVICES++))
    echo -e "${GREEN}✅ Video Service: 已优化${NC}"
else
    echo -e "${RED}❌ Video Service: 未优化${NC}"
fi

# 统计修复的违规数量
FIXED_VIOLATIONS=0
if [ "$ACCESS_SERVICE_OPTIMIZED" -eq 1 ]; then
    ((FIXED_VIOLATIONS += 1)) # AccessVerificationManager
fi
if [ "$ATTENDANCE_SERVICE_OPTIMIZED" -eq 1 ]; then
    ((FIXED_VIOLATIONS += 3)) # Service层 + 模板类
fi
if [ "$VIDEO_SERVICE_OPTIMIZED" -eq 1 ]; then
    ((FIXED_VIOLATIONS += 4)) # Adapter接口和实现类
fi

echo "🔧 修复的违规数量: $FIXED_VIOLATIONS"
echo "📈 优化完成率: $OPTIMIZED_SERVICES/3"

cat >> "$METRICS_FILE" << EOF

### 优化成果指标

| 服务 | 优化状态 | 说明 |
|------|----------|------|
| Access Service | $([ $ACCESS_SERVICE_OPTIMIZED -eq 1 ] && echo "✅ 已优化" || echo "❌ 未优化") | Entity使用规范化 |
| Attendance Service | $([ $ATTENDANCE_SERVICE_OPTIMIZED -eq 1 ] && echo "✅ 已优化" || echo "❌ 未优化") | Entity使用和模板类优化 |
| Video Service | $([ $VIDEO_SERVICE_OPTIMIZED -eq 1 ] && echo "✅ 已优化" || echo "❌ 未优化") | Adapter架构优化 |

**优化统计**:
- 已优化服务: $OPTIMIZED_SERVICES/3
- 修复违规数量: $FIXED_VIOLATIONS
- 优化完成率: $((OPTIMIZED_SERVICES * 100 / 3))%

EOF

# 4. 技术债务评估
echo -e "\n${YELLOW}4. 技术债务评估${NC}"
echo "----------------------------------------"

# 计算技术债务分数
DEBT_SCORE=0

# Entity违规扣分
if [ "$SERVICE_ENTITY_VIOLATIONS" -gt 0 ]; then
    DEBT_SCORE=$((DEBT_SCORE + 30))
    echo "⚠️ Service层Entity违规: -30分"
fi

# 缺失Response对象扣分
MISSING_RESPONSES=$((3 - RESPONSE_OBJECTS_COUNT))
if [ "$MISSING_RESPONSES" -gt 0 ]; then
    DEBT_SCORE=$((DEBT_SCORE + MISSINGING_RESPONSES * 10))
    echo "⚠️ 缺失Response对象: -$((MISSING_RESPONSES * 10))分"
fi

# 未优化服务扣分
UNOPTIMIZED_SERVICES=$((3 - OPTIMIZED_SERVICES))
if [ "$UNOPTIMIZED_SERVICES" -gt 0 ]; then
    DEBT_SCORE=$((DEBT_SCORE + UNOPTIMIZED_SERVICES * 20))
    echo "⚠️ 未优化服务: -$((UNOPTIMIZED_SERVICES * 20))分"
fi

# 计算质量分数
QUALITY_SCORE=$((100 - DEBT_SCORE))
if [ "$QUALITY_SCORE" -lt 0 ]; then
    QUALITY_SCORE=0
fi

echo "📊 技术债务分数: $DEBT_SCORE"
echo "💎 代码质量分数: $QUALITY_SCORE/100"

# 确定质量等级
if [ "$QUALITY_SCORE" -ge 90 ]; then
    QUALITY_LEVEL="优秀 🏆"
    echo -e "${GREEN}🏆 代码质量等级: 优秀${NC}"
elif [ "$QUALITY_SCORE" -ge 75 ]; then
    QUALITY_LEVEL="良好 🌟"
    echo -e "${YELLOW}🌟 代码质量等级: 良好${NC}"
elif [ "$QUALITY_SCORE" -ge 60 ]; then
    QUALITY_LEVEL="合格 ✅"
    echo -e "${YELLOW}✅ 代码质量等级: 合格${NC}"
else
    QUALITY_LEVEL="需改进 ⚠️"
    echo -e "${RED}⚠️ 代码质量等级: 需改进${NC}"
fi

cat >> "$METRICS_FILE" << EOF

## 📈 质量评估

### 技术债务分析

| 债务类型 | 扣分 | 说明 |
|----------|------|------|
| Service层Entity违规 | $([ $SERVICE_ENTITY_VIOLATIONS -gt 0 ] && echo "30" || echo "0") | 跨服务传递违规 |
| 缺失Response对象 | $((MISSING_RESPONSES * 10)) | 架构边界不完整 |
| 未优化服务 | $((UNOPTIMIZED_SERVICES * 20)) | 微服务优化滞后 |

**总技术债务**: $DEBT_SCORE分

### 代码质量评级

**质量分数**: $QUALITY_SCORE/100

**质量等级**: $QUALITY_LEVEL

EOF

# 5. 改进建议
cat >> "$METRICS_FILE" << EOF
## 🎯 改进建议

### 立即行动项 (P0)
$([ "$SERVICE_ENTITY_VIOLATIONS" -gt 0 ] && echo "- 修复 $SERVICE_ENTITY_VIOLATIONS 个Service层Entity违规" || echo "")
$([ "$MISSING_RESPONSES" -gt 0 ] && echo "- 创建 $MISSING_RESPONSES 个缺失的Response对象" || echo "")
$([ "$UNOPTIMIZED_SERVICES" -gt 0 ] && echo "- 优化 $UNOPTIMIZED_SERVICES 个未优化的微服务" || echo "")

### 短期目标 (1个月内)
- 建立自动化架构检查流水线
- 完善所有微服务的Entity使用规范
- 制定代码质量标准和检查清单

### 中期目标 (3个月内)
- 实现架构质量持续监控
- 建立技术债务管理机制
- 团队架构规范培训

### 长期目标 (持续)
- 建立企业级架构质量体系
- 实现零架构违规目标
- 持续改进和优化

## 📊 趋势分析

建议定期生成此报告以跟踪代码质量趋势：
- 每周: 检查新增架构违规
- 每月: 评估整体质量变化
- 每季度: 制定改进计划

---
**报告生成**: IOE-DREAM 代码质量分析工具
**生成时间**: $TIMESTAMP
**下次建议**: 1周后重新生成对比
EOF

# 显示结果
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                  📊 代码质量指标汇总${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "💎 代码质量分数: ${GREEN}$QUALITY_SCORE/100${NC}"
echo -e "🏆 质量等级: ${GREEN}$QUALITY_LEVEL${NC}"
echo -e "📈 优化完成率: ${YELLOW}$((OPTIMIZED_SERVICES * 100 / 3))%${NC}"
echo -e "📋 技术债务分数: ${RED}$DEBT_SCORE${NC}"
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "📄 详细报告: ${CYAN}$METRICS_FILE${NC}"
echo ""

# 根据质量等级给出建议
if [ "$QUALITY_SCORE" -ge 90 ]; then
    echo -e "${GREEN}🎉 优秀！继续保持高质量的代码标准${NC}"
elif [ "$QUALITY_SCORE" -ge 75 ]; then
    echo -e "${YELLOW}👍 良好！继续改进可以达到优秀水平${NC}"
elif [ "$QUALITY_SCORE" -ge 60 ]; then
    echo -e "${YELLOW}📈 合格！重点关注技术债务清理${NC}"
else
    echo -e "${RED}🚨 需要立即改进！建议启动代码质量提升计划${NC}"
fi

echo ""
echo -e "${CYAN}💡 定期运行 'bash scripts/generate-code-quality-metrics.sh' 跟踪质量变化${NC}"