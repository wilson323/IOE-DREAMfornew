#!/bin/bash

echo "=========================================="
echo "IOE-DREAM 整体架构健康度检查"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 健康检查结果统计
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0

# 创建临时报告文件
REPORT_FILE="architecture-health-report-$(date +%Y%m%d_%H%M%S).md"
cat > "$REPORT_FILE" << EOF
# IOE-DREAM 架构健康度检查报告

**检查时间**: $(date '+%Y-%m-%d %H:%M:%S')
**检查范围**: 整体项目架构合规性

## 📊 检查结果概览

EOF

echo -e "${BLUE}开始执行整体架构健康度检查...${NC}"

# 1. 检查Entity使用违规
echo -e "\n${YELLOW}1. Entity使用违规检查${NC}"
echo "----------------------------------------"

# 查找所有直接使用Entity的公开方法
ENTITY_VIOLATIONS=$(find microservices -name "*.java" -exec grep -l "public.*Entity.*{" {} \; | grep -v "dao\|DAO" | wc -l)
echo "📌 发现 $ENTITY_VIOLATIONS 个可能存在Entity违规的文件"

if [ "$ENTITY_VIOLATIONS" -eq 0 ]; then
    echo -e "${GREEN}✅ 未发现Entity使用违规${NC}"
    ((PASSED_CHECKS++))
    echo "✅ Entity使用合规" >> "$REPORT_FILE"
else
    echo -e "${YELLOW}⚠️ 发现 $ENTITY_VIOLATIONS 个需要检查的文件${NC}"
    echo "⚠️ $ENTITY_VIOLATIONS 个文件需要Entity使用检查" >> "$REPORT_FILE"
fi
((TOTAL_CHECKS++))

# 2. 检查跨服务传递违规
echo -e "\n${YELLOW}2. 跨服务传递违规检查${NC}"
echo "----------------------------------------"

# 查找直接返回Entity的Service方法
CROSS_SERVICE_VIOLATIONS=$(find microservices -name "*.java" -path "*/service/*" -exec grep -l "ResponseDTO<.*Entity>" {} \; | wc -l)
echo "📌 发现 $CROSS_SERVICE_VIOLATIONS 个可能存在跨服务传递违规的Service方法"

if [ "$CROSS_SERVICE_VIOLATIONS" -eq 0 ]; then
    echo -e "${GREEN}✅ 未发现跨服务传递违规${NC}"
    ((PASSED_CHECKS++))
    echo "✅ 跨服务传递合规" >> "$REPORT_FILE"
else
    echo -e "${YELLOW}⚠️ 发现 $CROSS_SERVICE_VIOLATIONS 个需要检查的Service方法${NC}"
    echo "⚠️ $CROSS_SERVICE_VIOLATIONS 个Service方法需要检查" >> "$REPORT_FILE"
fi
((TOTAL_CHECKS++))

# 3. 检查Response对象使用情况
echo -e "\n${YELLOW}3. Response对象使用情况检查${NC}"
echo "----------------------------------------"

# 检查是否创建了必要的Response对象
DEVICE_RESPONSE_EXISTS="microservices/microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/gateway/domain/response/DeviceResponse.java"
USERINFO_RESPONSE_EXISTS="microservices/microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/gateway/domain/response/UserInfoResponse.java"
EMPLOYEE_RESPONSE_EXISTS="microservices/microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/gateway/domain/response/EmployeeResponse.java"

RESPONSE_OBJECTS_COUNT=0
if [ -f "$DEVICE_RESPONSE_EXISTS" ]; then
    echo -e "${GREEN}✅ DeviceResponse已创建${NC}"
    ((RESPONSE_OBJECTS_COUNT++))
else
    echo -e "${RED}❌ DeviceResponse缺失${NC}"
fi

if [ -f "$USERINFO_RESPONSE_EXISTS" ]; then
    echo -e "${GREEN}✅ UserInfoResponse已创建${NC}"
    ((RESPONSE_OBJECTS_COUNT++))
else
    echo -e "${RED}❌ UserInfoResponse缺失${NC}"
fi

if [ -f "$EMPLOYEE_RESPONSE_EXISTS" ]; then
    echo -e "${GREEN}✅ EmployeeResponse已创建${NC}"
    ((RESPONSE_OBJECTS_COUNT++))
else
    echo -e "${RED}❌ EmployeeResponse缺失${NC}"
fi

echo "📊 Response对象完成度: $RESPONSE_OBJECTS_COUNT/3"

if [ "$RESPONSE_OBJECTS_COUNT" -eq 3 ]; then
    ((PASSED_CHECKS++))
    echo "✅ Response对象完整" >> "$REPORT_FILE"
else
    echo "❌ Response对象不完整 ($RESPONSE_OBJECTS_COUNT/3)" >> "$REPORT_FILE"
fi
((TOTAL_CHECKS++))

# 4. 检查验证脚本完整性
echo -e "\n${YELLOW}4. 验证脚本完整性检查${NC}"
echo "----------------------------------------"

VALIDATION_SCRIPTS=(
    "scripts/validate-access-service-entity-fixes.sh"
    "scripts/validate-attendance-service-entity-fixes.sh"
    "scripts/validate-video-service-architecture-fixes.sh"
)

SCRIPTS_COUNT=0
for script in "${VALIDATION_SCRIPTS[@]}"; do
    if [ -f "$script" ]; then
        echo -e "${GREEN}✅ $(basename $script)${NC}"
        ((SCRIPTS_COUNT++))
    else
        echo -e "${RED}❌ $(basename $script) 缺失${NC}"
    fi
done

echo "📊 验证脚本完整度: $SCRIPTS_COUNT/3"

if [ "$SCRIPTS_COUNT" -eq 3 ]; then
    ((PASSED_CHECKS++))
    echo "✅ 验证脚本完整" >> "$REPORT_FILE"
else
    echo "❌ 验证脚本不完整 ($SCRIPTS_COUNT/3)" >> "$REPORT_FILE"
fi
((TOTAL_CHECKS++))

# 5. 检查项目文档完整性
echo -e "\n${YELLOW}5. 项目文档完整性检查${NC}"
echo "----------------------------------------"

DOCUMENTS=(
    "ACCESS_SERVICE_ENTITY_OPTIMIZATION_REPORT.md"
    "ATTENDANCE_SERVICE_ENTITY_OPTIMIZATION_REPORT.md"
    "VIDEO_SERVICE_ARCHITECTURE_OPTIMIZATION_REPORT.md"
)

DOCS_COUNT=0
for doc in "${DOCUMENTS[@]}"; do
    if [ -f "$doc" ]; then
        echo -e "${GREEN}✅ $(basename $doc)${NC}"
        ((DOCS_COUNT++))
    else
        echo -e "${RED}❌ $(basename $doc) 缺失${NC}"
    fi
done

echo "📊 项目文档完整度: $DOCS_COUNT/3"

if [ "$DOCS_COUNT" -eq 3 ]; then
    ((PASSED_CHECKS++))
    echo "✅ 项目文档完整" >> "$REPORT_FILE"
else
    echo "❌ 项目文档不完整 ($DOCS_COUNT/3)" >> "$REPORT_FILE"
fi
((TOTAL_CHECKS++))

# 6. 计算健康分数
HEALTH_SCORE=$((PASSED_CHECKS * 100 / TOTAL_CHECKS))

# 完成报告
cat >> "$REPORT_FILE" << EOF

## 📈 架构健康度评估

### 总体评分: $HEALTH_SCORE/100

**通过检查**: $PASSED_CHECKS/$TOTAL_CHECKS

### 检查项目详情

| 检查项目 | 状态 | 说明 |
|---------|------|------|
| Entity使用合规 | $([ $ENTITY_VIOLATIONS -eq 0 ] && echo "✅ 通过" || echo "⚠️ 需关注") | $ENTITY_VIOLATIONS 个文件需要检查 |
| 跨服务传递合规 | $([ $CROSS_SERVICE_VIOLATIONS -eq 0 ] && echo "✅ 通过" || echo "⚠️ 需关注") | $CROSS_SERVICE_VIOLATIONS 个方法需要检查 |
| Response对象完整 | $([ $RESPONSE_OBJECTS_COUNT -eq 3 ] && echo "✅ 通过" || echo "❌ 未通过") | $RESPONSE_OBJECTS_COUNT/3 个对象已创建 |
| 验证脚本完整 | $([ $SCRIPTS_COUNT -eq 3 ] && echo "✅ 通过" || echo "❌ 未通过") | $SCRIPTS_COUNT/3 个脚本已创建 |
| 项目文档完整 | $([ $DOCS_COUNT -eq 3 ] && echo "✅ 通过" || echo "❌ 未通过") | $DOCS_COUNT/3 个文档已创建 |

## 🎯 健康等级

EOF

# 根据分数确定健康等级
if [ "$HEALTH_SCORE" -ge 90 ]; then
    HEALTH_LEVEL="优秀 🟢"
    echo -e "${GREEN}🏆 架构健康度: 优秀 ($HEALTH_SCORE/100)${NC}"
    echo "### 🏆 优秀 ($HEALTH_SCORE/100分)" >> "$REPORT_FILE"
    echo "- 架构质量优秀，符合企业级标准" >> "$REPORT_FILE"
    echo "- 建议持续保持并定期检查" >> "$REPORT_FILE"
elif [ "$HEALTH_SCORE" -ge 70 ]; then
    HEALTH_LEVEL="良好 🟡"
    echo -e "${YELLOW}📈 架构健康度: 良好 ($HEALTH_SCORE/100)${NC}"
    echo "### 📈 良好 ($HEALTH_SCORE/100分)" >> "$REPORT_FILE"
    echo "- 架构质量良好，存在少量改进空间" >> "$REPORT_FILE"
    echo "- 建议修复发现的问题项" >> "$REPORT_FILE"
elif [ "$HEALTH_SCORE" -ge 50 ]; then
    HEALTH_LEVEL="一般 🟠"
    echo -e "${YELLOW}⚠️ 架构健康度: 一般 ($HEALTH_SCORE/100)${NC}"
    echo "### ⚠️ 一般 ($HEALTH_SCORE/100分)" >> "$REPORT_FILE"
    echo "- 架构质量一般，需要重点改进" >> "$REPORT_FILE"
    echo "- 建议立即处理发现的问题" >> "$REPORT_FILE"
else
    HEALTH_LEVEL="较差 🔴"
    echo -e "${RED}🚨 架构健康度: 较差 ($HEALTH_SCORE/100)${NC}"
    echo "### 🚨 较差 ($HEALTH_SCORE/100分)" >> "$REPORT_FILE"
    echo "- 架构质量较差，存在较多问题" >> "$REPORT_FILE"
    echo "- 建议立即启动架构优化计划" >> "$REPORT_FILE"
fi

# 添加改进建议
cat >> "$REPORT_FILE" << EOF

## 🔧 改进建议

### 立即行动项
1. 修复所有标记为 ❌ 的检查项
2. 补充缺失的Response对象
3. 完善验证脚本和文档

### 持续改进项
1. 建立定期架构健康检查机制
2. 集成到CI/CD流水线
3. 培训团队遵循架构规范

### 长期规划项
1. 建立架构质量指标体系
2. 引入自动化架构检查工具
3. 制定架构演进路线图

---
**报告生成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**检查工具**: IOE-DREAM 架构健康检查脚本
**下次检查建议**: 1周后或重大变更后
EOF

# 显示最终结果
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                    📊 检查结果汇总${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "📈 架构健康度: ${GREEN}$HEALTH_LEVEL${NC}"
echo -e "✅ 通过检查: ${GREEN}$PASSED_CHECKS${NC}/${NC}$TOTAL_CHECKS"
echo -e "📋 完整报告: ${CYAN}$REPORT_FILE${NC}"
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

echo ""
echo -e "${GREEN}🎯 下一步行动建议:${NC}"
if [ "$ENTITY_VIOLATIONS" -gt 0 ]; then
    echo "• 检查并修复 $ENTITY_VIOLATIONS 个Entity使用违规文件"
fi
if [ "$CROSS_SERVICE_VIOLATIONS" -gt 0 ]; then
    echo "• 检查并修复 $CROSS_SERVICE_VIOLATIONS 个跨服务传递违规方法"
fi
if [ "$RESPONSE_OBJECTS_COUNT" -lt 3 ]; then
    echo "• 补充缺失的Response对象 ($((3 - RESPONSE_OBJECTS_COUNT)) 个)"
fi
if [ "$SCRIPTS_COUNT" -lt 3 ]; then
    echo "• 创建缺失的验证脚本 ($((3 - SCRIPTS_COUNT)) 个)"
fi
if [ "$DOCS_COUNT" -lt 3 ]; then
    echo "• 补充缺失的项目文档 ($((3 - DOCS_COUNT)) 个)"
fi

echo ""
echo -e "${YELLOW}💡 定期运行 'bash scripts/overall-architecture-health-check.sh' 进行健康检查${NC}"