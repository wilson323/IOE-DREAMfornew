#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

OUTPUT_FILE="exception-handling-report.md"

echo -e "${BLUE}🔍 [异常处理检查] 开始检查...${NC}"
echo ""

cat > "$OUTPUT_FILE" << HEADER
# 异常处理规范检查报告

**生成时间**: $(date)  
**检查范围**: microservices/

---

## 🔍 检查结果

HEADER

TOTAL_ISSUES=0

echo -e "${BLUE}[检查1] 过于宽泛的异常捕获${NC}"
CATCH_ISSUES=$(find microservices/*/src/ -name "*.java" -type f -exec grep -l "catch.*Exception.*e" {} \; 2>/dev/null | xargs grep -n "catch.*Exception.*e" | grep -v "BusinessException" | grep -v "SystemException" | head -50)

if [ -n "$CATCH_ISSUES" ]; then
    CATCH_COUNT=$(echo "$CATCH_ISSUES" | wc -l)
    TOTAL_ISSUES=$((TOTAL_ISSUES + CATCH_COUNT))
    echo -e "${YELLOW}⚠️  发现 $CATCH_COUNT 处${NC}"
    echo "### 1. 过于宽泛的异常捕获 ($CATCH_COUNT 处)" >> "$OUTPUT_FILE"
    echo "$CATCH_ISSUES" | head -20 | while read line; do
        echo "- $line" >> "$OUTPUT_FILE"
    done
else
    echo -e "${GREEN}✅ 无问题${NC}"
    echo "### 1. 过于宽泛的异常捕获" >> "$OUTPUT_FILE"
    echo "✅ 未发现问题" >> "$OUTPUT_FILE"
fi

echo ""
echo -e "${BLUE}[检查2] 空catch块${NC}"
EMPTY_CATCH=$(find microservices/*/src/ -name "*.java" -type f -exec grep -A2 "catch.*Exception" {} \; 2>/dev/null | grep -E "^\s*\}\s*$" | wc -l)

if [ "$EMPTY_CATCH" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  发现约 $EMPTY_CATCH 处${NC}"
    TOTAL_ISSUES=$((TOTAL_ISSUES + EMPTY_CATCH))
    echo "### 2. 空catch块 ($EMPTY_CATCH 处)" >> "$OUTPUT_FILE"
    echo "建议使用 log.error() 记录异常" >> "$OUTPUT_FILE"
else
    echo -e "${GREEN}✅ 无问题${NC}"
    echo "### 2. 空catch块" >> "$OUTPUT_FILE"
    echo "✅ 未发现问题" >> "$OUTPUT_FILE"
fi

echo ""
echo -e "${BLUE}[检查3] printStackTrace使用${NC}"
PRINTSTACK=$(grep -rn "printStackTrace()" microservices/*/src/ --include="*.java" 2>/dev/null | wc -l)

if [ "$PRINTSTACK" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  发现 $PRINTSTACK 处${NC}"
    TOTAL_ISSUES=$((TOTAL_ISSUES + PRINTSTACK))
    echo "### 3. printStackTrace使用 ($PRINTSTACK 处)" >> "$OUTPUT_FILE"
    grep -rn "printStackTrace()" microservices/*/src/ --include="*.java" 2>/dev/null | head -10 | while read line; do
        echo "- $line" >> "$OUTPUT_FILE"
    done
else
    echo -e "${GREEN}✅ 无问题${NC}"
    echo "### 3. printStackTrace使用" >> "$OUTPUT_FILE"
    echo "✅ 未发现问题" >> "$OUTPUT_FILE"
fi

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BLUE}📊 [总结] 发现问题: $TOTAL_ISSUES 处${NC}"
echo "📄 报告文件: $OUTPUT_FILE"

exit $TOTAL_ISSUES
