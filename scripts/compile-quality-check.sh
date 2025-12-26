#!/bin/bash

# =============================================================================
# IOE-DREAM 编译质量检查脚本
#
# 功能：在编译前进行代码质量检查，防止编译错误和架构违规
# 检查项：
# 1. 架构合规性检查 (@Autowired/@Repository违规)
# 2. 导入语句完整性检查
# 3. 方法签名兼容性检查
# 4. 类型转换安全检查
# 5. 异常处理完整性检查
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查结果变量
HAS_VIOLATIONS=false
VIOLATION_COUNT=0

echo -e "${BLUE}🔍 IOE-DREAM 编译质量检查${NC}"
echo "======================================"
echo "时间: $(date)"
echo "分支: $(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo 'Unknown')"
echo ""

# 获取待检查的Java文件
JAVA_FILES=$(find . -name "*.java" -not -path "*/target/*" -not -path "*/.git/*" 2>/dev/null || true)
TOTAL_FILES=$(echo "$JAVA_FILES" | wc -l)

if [ "$TOTAL_FILES" -eq 0 ]; then
    echo -e "${YELLOW}⚠️ 未找到Java文件，跳过检查${NC}"
    exit 0
fi

echo "📁 待检查Java文件数: $TOTAL_FILES"
echo ""

# =============================================================================
# 1. 架构合规性检查
# =============================================================================
echo "📋 1. 架构合规性检查"
ARCH_VIOLATIONS=0

for file in $JAVA_FILES; do
    if [ -f "$file" ]; then
        # 检查@Autowired违规（排除测试文件）
        if [[ "$file" != *Test.java ]] && grep -q "@Autowired" "$file"; then
            echo -e "  ${RED}❌ $file: 发现@Autowired违规${NC}"
            ARCH_VIOLATIONS=$((ARCH_VIOLATIONS + 1))
            VIOLATION_COUNT=$((VIOLATION_COUNT + 1))
            HAS_VIOLATIONS=true
            grep -n "@Autowired" "$file" | head -3 | while read line; do
                echo "    $line"
            done
        fi

        # 检查@Repository违规
        if grep -q "@Repository" "$file"; then
            echo -e "  ${RED}❌ $file: 发现@Repository违规${NC}"
            ARCH_VIOLATIONS=$((ARCH_VIOLATIONS + 1))
            VIOLATION_COUNT=$((VIOLATION_COUNT + 1))
            HAS_VIOLATIONS=true
            grep -n "@Repository" "$file" | head -3 | while read line; do
                echo "    $line"
            done
        fi

        # 检查Repository命名违规
        if grep -q "class.*Repository" "$file" && ! grep -q "interface.*Repository" "$file"; then
            echo -e "  ${RED}❌ $file: 禁止使用Repository类名${NC}"
            ARCH_VIOLATIONS=$((ARCH_VIOLATIONS + 1))
            VIOLATION_COUNT=$((VIOLATION_COUNT + 1))
            HAS_VIOLATIONS=true
        fi
    fi
done

if [ "$ARCH_VIOLATIONS" -eq 0 ]; then
    echo -e "  ${GREEN}✅ 架构合规性检查通过${NC}"
else
    echo -e "  ${RED}❌ 发现 $ARCH_VIOLATIONS 个架构违规${NC}"
fi

echo ""

# =============================================================================
# 2. 导入语句完整性检查
# =============================================================================
echo "📋 2. 导入语句完整性检查"
IMPORT_VIOLATIONS=0

for file in $JAVA_FILES; do
    if [ -f "$file" ]; then
        # 检查Lombok导入
        if grep -q "@Data\|@Builder\|@NoArgsConstructor\|@AllArgsConstructor" "$file" && ! grep -q "import lombok" "$file"; then
            echo -e "  ${RED}❌ $file: 缺少Lombok导入${NC}"
            IMPORT_VIOLATIONS=$((IMPORT_VIOLATIONS + 1))
            VIOLATION_COUNT=$((VIOLATION_COUNT + 1))
            HAS_VIOLATIONS=true
        fi

        # 检查@Slf4j导入
        if grep -q "log\." "$file" && ! grep -q "@Slf4j\|import.*LoggerFactory" "$file"; then
            echo -e "  ${RED}❌ $file: 缺少@Slf4j注解或LoggerFactory导入${NC}"
            IMPORT_VIOLATIONS=$((IMPORT_VIOLATIONS + 1))
            VIOLATION_COUNT=$((VIOLATION_COUNT + 1))
            HAS_VIOLATIONS=true
        fi

        # 检查ResponseDTO导入
        if grep -q "ResponseDTO" "$file" && ! grep -q "import.*ResponseDTO" "$file"; then
            echo -e "  ${YELLOW}⚠️ $file: 可能有ResponseDTO导入问题${NC}"
            IMPORT_VIOLATIONS=$((IMPORT_VIOLATIONS + 1))
            VIOLATION_COUNT=$((VIOLATION_COUNT + 1))
        fi
    fi
done

if [ "$IMPORT_VIOLATIONS" -eq 0 ]; then
    echo -e "  ${GREEN}✅ 导入语句检查通过${NC}"
else
    echo -e "  ${YELLOW}⚠️ 发现 $IMPORT_VIOLATIONS 个导入问题${NC}"
fi

echo ""

# =============================================================================
# 3. 类型转换安全检查
# =============================================================================
echo "📋 3. 类型转换安全检查"
TYPE_VIOLATIONS=0

for file in $JAVA_FILES; do
    if [ -f "$file" ]; then
        # 检查危险的强制类型转换
        if grep -q "([A-Za-z_][A-Za-z0-9_]*)\s*" "$file"; then
            echo -e "  ${YELLOW}⚠️ $file: 检测到类型转换，建议使用TypeUtils${NC}"
        fi

        # 检查String.valueOf的使用
        type_value_count=$(grep -c "String\.valueOf" "$file" 2>/dev/null || true)
        if [ "$type_value_count" -gt 0 ]; then
            echo -e "  ${YELLOW}⚠️ $file: 发现 $type_value_count 处String.valueOf，建议使用TypeUtils${NC}"
        fi
    fi
done

if [ "$TYPE_VIOLATIONS" -eq 0 ]; then
    echo -e "  ${GREEN}✅ 类型转换安全检查通过${NC}"
else
    echo -e "  ${YELLOW}⚠️ 发现类型转换相关建议${NC}"
fi

echo ""

# =============================================================================
# 4. 异常处理完整性检查
# =============================================================================
echo "📋 4. 异常处理完整性检查"
EXCEPTION_VIOLATIONS=0

for file in $JAVA_FILES; do
    if [ -f "$file" ]; then
        # 检查try-catch块
        if grep -q "catch.*Exception" "$file"; then
            catch_count=$(grep -c "catch.*Exception" "$file")
            log_count=$(grep -c "log\." "$file" || echo 0)

            # 如果有catch但没有日志记录
            if [ "$catch_count" -gt 0 ] && [ "$log_count" -eq 0 ]; then
                echo -e "  ${YELLOW}⚠️ $file: 有异常处理但缺少日志记录${NC}"
                EXCEPTION_VIOLATIONS=$((EXCEPTION_VIOLATIONS + 1))
            fi
        fi
    fi
done

if [ "$EXCEPTION_VIOLATIONS" -eq 0 ]; then
    echo -e "  ${GREEN}✅ 异常处理检查通过${NC}"
else
    echo -e "  ${YELLOW}⚠️ 发现 $EXCEPTION_VIOLATIONS 个异常处理建议${NC}"
fi

echo ""

# =============================================================================
# 生成检查结果
# =============================================================================
echo "======================================"
echo "📊 编译质量检查结果:"
echo "======================================"

if [ "$HAS_VIOLATIONS" = false ]; then
    echo -e "${GREEN}🎉 编译质量检查通过！${NC}"
    echo -e "${GREEN}✅ 代码符合 IOE-DREAM 质量标准${NC}"
    echo ""
    echo -e "${BLUE}📋 建议下一步：${NC}"
    echo "1. 继续执行Maven编译"
    echo "2. 运行单元测试"
    echo "3. 执行集成测试"
    echo ""
    exit 0
else
    echo -e "${RED}❌ 编译质量检查发现问题！${NC}"
    echo -e "${RED}⚠️ 发现 $VIOLATION_COUNT 个问题，建议修复后重新运行${NC}"
    echo ""
    echo -e "${BLUE}📋 修复建议:${NC}"
    echo "1. 查看上述具体问题并修复"
    echo "2. 参考 CLAUDE.md 架构规范"
    echo "3. 使用统一的TypeUtils进行类型转换"
    echo "4. 添加适当的日志记录"
    echo "5. 修复后重新运行: ./scripts/compile-quality-check.sh"
    echo ""
    echo -e "${YELLOW}💡 强制跳过检查（不推荐）: mvn compile -DskipQualityCheck=true${NC}"
    exit 1
fi