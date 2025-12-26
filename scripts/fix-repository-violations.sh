#!/bin/bash

# ==============================================================================
# IOE-DREAM @Repository 违规修复脚本
#
# 功能: 批量将@Repository注解替换为@Mapper注解
# 目标: 修复11个违规的DAO接口文件
# 标准: MyBatis-Plus规范要求使用@Mapper而非@Repository
# ==============================================================================

echo "🔧 IOE-DREAM @Repository 违规修复脚本"
echo "====================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 统计变量
TOTAL_FILES=0
FIXED_FILES=0
FAILED_FILES=0

# 检查是否在项目根目录
if [ ! -f "microservices/pom.xml" ]; then
    echo -e "${RED}❌ 错误: 请在IOE-DREAM项目根目录执行此脚本${NC}"
    exit 1
fi

echo ""
echo "📋 开始扫描@Repository违规文件..."

# 查找所有包含@Repository的Java文件
REPOSITORY_FILES=$(find . -name "*.java" -type f -exec grep -l "@Repository" {} \;)

if [ -z "$REPOSITORY_FILES" ]; then
    echo -e "${GREEN}✅ 未发现@Repository违规文件，项目完全合规！${NC}"
    exit 0
fi

echo ""
echo "📊 发现违规文件列表:"
echo "======================"

# 显示违规文件列表
for file in $REPOSITORY_FILES; do
    echo -e "${YELLOW}⚠️  ${file}${NC}"
    TOTAL_FILES=$((TOTAL_FILES + 1))
done

echo ""
echo "🔨 开始修复违规文件..."

# 修复每个文件
for file in $REPOSITORY_FILES; do
    echo -n "正在修复: $(basename $file) ... "

    # 检查文件是否同时包含@Mapper和@Repository
    if grep -q "@Mapper" "$file"; then
        # 如果同时存在，只需删除@Repository
        sed -i.bak '/^@Repository$/d' "$file"

        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ 删除@Repository成功${NC}"
            FIXED_FILES=$((FIXED_FILES + 1))
        else
            echo -e "${RED}❌ 删除@Repository失败${NC}"
            FAILED_FILES=$((FAILED_FILES + 1))
        fi
    else
        # 如果只有@Repository，替换为@Mapper
        sed -i.bak 's/^@Repository$/@Mapper/' "$file"

        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ 替换为@Mapper成功${NC}"
            FIXED_FILES=$((FIXED_FILES + 1))
        else
            echo -e "${RED}❌ 替换为@Mapper失败${NC}"
            FAILED_FILES=$((FAILED_FILES + 1))
        fi
    fi
done

echo ""
echo "📊 修复结果统计:"
echo "=================="
echo -e "总文件数: ${BLUE}${TOTAL_FILES}${NC}"
echo -e "修复成功: ${GREEN}${FIXED_FILES}${NC}"
echo -e "修复失败: ${RED}${FAILED_FILES}${NC}"

echo ""
echo "🔍 验证修复结果..."

# 再次检查是否还有@Repository违规
REMAINING_FILES=$(find . -name "*.java" -type f -exec grep -l "@Repository" {} \;)

if [ -z "$REMAINING_FILES" ]; then
    echo -e "${GREEN}✅ 验证通过: 所有@Repository违规已修复完成！${NC}"
else
    echo -e "${RED}⚠️  仍有${REMAINING_FILES}个文件包含@Repository，请手动检查${NC}"
    for file in $REMAINING_FILES; do
        echo -e "${RED}   - ${file}${NC}"
    done
fi

echo ""
echo "🔍 验证@Mapper使用情况..."

# 统计@Mapper使用情况
MAPPER_COUNT=$(find . -name "*.java" -type f -exec grep -l "@Mapper" {} \; | wc -l)
echo -e "当前@Mapper使用数量: ${GREEN}${MAPPER_COUNT}${NC}"

echo ""
echo "📝 生成的备份文件:"
echo "==================="

# 显示备份文件
for file in $REPOSITORY_FILES; do
    if [ -f "${file}.bak" ]; then
        echo -e "${BLUE}📄 ${file}.bak${NC}"
    fi
done

echo ""
echo "🧹 清理建议:"
echo "============"
echo "修复验证无误后，可执行以下命令清理备份文件:"
echo "find . -name \"*.bak\" -delete"

echo ""
echo "📋 后续建议:"
echo "============"
echo "1. 运行 Maven 编译测试: mvn clean compile -DskipTests"
echo "2. 运行单元测试: mvn test"
echo "3. 检查IDE中的错误提示"
echo "4. 提交代码到版本控制"

echo ""
echo "✨ 修复脚本执行完成！"
echo "===================="

# 生成修复报告
REPORT_FILE="repository-violation-fix-report.txt"
cat > "$REPORT_FILE" << EOF
IOE-DREAM @Repository 违规修复报告
生成时间: $(date)
修复脚本: scripts/fix-repository-violations.sh

修复统计:
- 发现违规文件: $TOTAL_FILES
- 成功修复: $FIXED_FILES
- 修复失败: $FAILED_FILES

修复文件列表:
$REPOSITORY_FILES

验证结果:
- 剩余@Repository文件数量: $(echo "$REMAINING_FILES" | wc -l)
- 当前@Mapper文件数量: $MAPPER_COUNT

EOF

echo -e "📄 修复报告已生成: ${BLUE}${REPORT_FILE}${NC}"

if [ $FAILED_FILES -eq 0 ]; then
    echo -e "${GREEN}🎉 所有违规文件修复成功！${NC}"
    exit 0
else
    echo -e "${RED}⚠️  部分文件修复失败，请手动检查${NC}"
    exit 1
fi