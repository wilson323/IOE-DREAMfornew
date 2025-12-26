#!/bin/bash
################################################################################
# TypeReference修复建议生成脚本
#
# 用途：生成详细的修复建议报告
# 注意：不直接修改代码，只生成建议
################################################################################

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

OUTPUT_FILE="type-reference-fix-report.md"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

echo -e "${BLUE}🔧 [修复建议] 生成TypeReference修复建议报告...${NC}"

# 创建报告头
cat > "$OUTPUT_FILE" << HEADER
# TypeReference修复建议报告

**生成时间**: $TIMESTAMP  
**分析范围**: microservices/  
**重要提示**: ⚠️ 禁止脚本直接修改代码，以下建议需要手动完成修复

---

## 🔍 详细修复建议

HEADER

TOTAL_ISSUES=0

# 扫描Map类型
echo -e "${BLUE}扫描Map类型转换...${NC}"
MAP_ISSUES=$(grep -rn "objectMapper\.readValue.*Map\.class" microservices/*/src/ --include="*.java" 2>/dev/null | grep -v "TypeReference")

if [ -n "$MAP_ISSUES" ]; then
    MAP_COUNT=$(echo "$MAP_ISSUES" | wc -l)
    TOTAL_ISSUES=$((TOTAL_ISSUES + MAP_COUNT))
    
    echo "" >> "$OUTPUT_FILE"
    echo "### 1. Map类型转换 ($MAP_COUNT 处)" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
    
    echo "$MAP_ISSUES" | while read line; do
        FILE_PATH=$(echo "$line" | cut -d: -f1)
        LINE_NUM=$(echo "$line" | cut -d: -f2)
        echo "- **$FILE_PATH:$LINE_NUM**" >> "$OUTPUT_FILE"
    done
fi

# 扫描List类型
echo -e "${BLUE}扫描List类型转换...${NC}"
LIST_ISSUES=$(grep -rn "objectMapper\.readValue.*List\.class" microservices/*/src/ --include="*.java" 2>/dev/null | grep -v "TypeReference")

if [ -n "$LIST_ISSUES" ]; then
    LIST_COUNT=$(echo "$LIST_ISSUES" | wc -l)
    TOTAL_ISSUES=$((TOTAL_ISSUES + LIST_COUNT))
    
    echo "" >> "$OUTPUT_FILE"
    echo "### 2. List类型转换 ($LIST_COUNT 处)" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
    
    echo "$LIST_ISSUES" | while read line; do
        FILE_PATH=$(echo "$line" | cut -d: -f1)
        LINE_NUM=$(echo "$line" | cut -d: -f2)
        echo "- **$FILE_PATH:$LINE_NUM**" >> "$OUTPUT_FILE"
    done
fi

# 扫描ResponseDTO类型
echo -e "${BLUE}扫描ResponseDTO类型转换...${NC}"
RESPONSE_ISSUES=$(grep -rn "objectMapper\.readValue.*ResponseDTO\.class" microservices/*/src/ --include="*.java" 2>/dev/null | grep -v "TypeReference")

if [ -n "$RESPONSE_ISSUES" ]; then
    RESPONSE_COUNT=$(echo "$RESPONSE_ISSUES" | wc -l)
    TOTAL_ISSUES=$((TOTAL_ISSUES + RESPONSE_COUNT))
    
    echo "" >> "$OUTPUT_FILE"
    echo "### 3. ResponseDTO类型转换 ($RESPONSE_COUNT 处)" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
    
    echo "$RESPONSE_ISSUES" | while read line; do
        FILE_PATH=$(echo "$line" | cut -d: -f1)
        LINE_NUM=$(echo "$line" | cut -d: -f2)
        echo "- **$FILE_PATH:$LINE_NUM**" >> "$OUTPUT_FILE"
    done
fi

# 添加修复指导
cat >> "$OUTPUT_FILE" << FOOTER

---

## 🔧 修复步骤

### 步骤1: 创建修复分支

\`\`\`bash
git checkout -b fix/type-reference-\$(date +%Y%m%d)
\`\`\`

### 步骤2: 手动修复代码

1. 在IDE中打开对应文件
2. 定位到问题行号
3. 替换为正确写法：
   - Map: \`new TypeReference<Map<String, Object>>() {}\`
   - List: \`new TypeReference<List<YourType>>() {}\`
   - ResponseDTO: \`new TypeReference<ResponseDTO<YourType>>() {}\`

### 步骤3: 验证修复

\`\`\`bash
# 编译验证
mvn clean compile -DskipTests

# 运行扫描验证
./scripts/scan-type-reference.sh
\`\`\`

### 步骤4: 提交代码

\`\`\`bash
git add .
git commit -m "fix: 使用TypeReference确保泛型类型安全"
\`\`\`

---

**报告生成**: $(date)

FOOTER

echo ""
echo -e "${GREEN}✅ 报告生成完成: $OUTPUT_FILE${NC}"
echo -e "${BLUE}📊 发现问题: $TOTAL_ISSUES 处${NC}"

exit 0
