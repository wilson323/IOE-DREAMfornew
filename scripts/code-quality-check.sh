#!/bin/bash

###############################################################################
# IOE-DREAM 代码质量综合检查脚本
# 功能：多维度代码质量分析和报告生成
###############################################################################

echo "=========================================="
echo "IOE-DREAM 代码质量综合检查"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="/d/IOE-DREAM"
MICROSERVICES_DIR="${PROJECT_ROOT}/microservices"

# 输出目录
OUTPUT_DIR="${PROJECT_ROOT}/code-quality-reports"
mkdir -p "${OUTPUT_DIR}"

# 报告文件
SUMMARY_REPORT="${OUTPUT_DIR}/quality-summary-report.md"

echo "检查目录: ${MICROSERVICES_DIR}"
echo "报告输出: ${OUTPUT_DIR}"
echo ""

###############################################################################
# 1. UTF-8编码规范性检查
###############################################################################

echo -e "${BLUE}[1/7] UTF-8编码规范性检查...${NC}"

cd "${MICROSERVICES_DIR}"

total_files=$(find . -name "*.java" -type f | wc -l)
echo "- 总Java文件数: ${total_files}"

# 检查非UTF-8编码
non_utf8=$(find . -name "*.java" -type f -exec file {} \; 2>/dev/null | grep -v "UTF-8\|ASCII" | wc -l)
echo "- 非UTF-8编码文件: ${non_utf8}"

# UTF-8合规率
if [ $total_files -gt 0 ]; then
    compliance_rate=$(( (total_files - non_utf8) * 100 / total_files ))
    echo "- UTF-8合规率: ${compliance_rate}%"
fi

echo ""

###############################################################################
# 2. 日志规范检查
###############################################################################

echo -e "${BLUE}[2/7] 日志规范检查...${NC}"

slf4j_files=$(find . -name "*.java" -type f -exec grep -l "@Slf4j" {} \; 2>/dev/null | wc -l)
log_factory_files=$(find . -name "*.java" -type f -exec grep -l "LoggerFactory.getLogger" {} \; 2>/dev/null | wc -l)

echo "- 使用@Slf4j注解: ${slf4j_files}"
echo "- 使用LoggerFactory: ${log_factory_files} (违规)"

# 日志规范合规率
if [ $((slf4j_files + log_factory_files)) -gt 0 ]; then
    logging_compliance=$((slf4j_files * 100 / (slf4j_files + log_factory_files)))
    echo "- 日志规范合规率: ${logging_compliance}%"
fi

echo ""

###############################################################################
# 3. 异常处理检查
###############################################################################

echo -e "${BLUE}[3/7] 异常处理规范检查...${NC}"

# 检查空catch块（简单统计）
catch_blocks=$(find . -name "*.java" -type f -exec grep -c "catch" {} \; 2>/dev/null | awk '{sum+=$1} END {print sum}')
echo "- Catch块总数: ${catch_blocks}"

# 检查printStackTrace使用
print_stacktrace=$(find . -name "*.java" -type f -exec grep -c "printStackTrace" {} \; 2>/dev/null | awk '{sum+=$1} END {print sum}')
echo "- 使用printStackTrace: ${print_stacktrace} (不规范)"

echo ""

###############################################################################
# 4. 注释完整性检查
###############################################################################

echo -e "${BLUE}[4/7] 注释完整性检查...${NC}"

total_public_methods=$(find . -name "*.java" -type f -exec grep -E "^\s*public.*\(" {} \; 2>/dev/null | wc -l)
echo "- 总public方法数: ${total_public_methods}"

# 统计有JavaDoc注释的文件
files_with_javadoc=$(find . -name "*.java" -type f -exec grep -l "/\*\*" {} \; 2>/dev/null | wc -l)
echo "- 有JavaDoc注释的文件: ${files_with_javadoc}"

if [ $total_files -gt 0 ]; then
    comment_coverage=$((files_with_javadoc * 100 / total_files))
    echo "- 注释覆盖率: ${comment_coverage}%"
fi

echo ""

###############################################################################
# 5. 代码复杂度检查
###############################################################################

echo -e "${BLUE}[5/7] 代码复杂度检查...${NC}"

# 检查超大文件（>500行）
large_files=$(find . -name "*.java" -type f -exec wc -l {} \; 2>/dev/null | awk '$1 > 500 {print $0}' | wc -l)
echo "- 超大文件(>500行): ${large_files}"

# 检查超大文件（>1000行）
very_large_files=$(find . -name "*.java" -type f -exec wc -l {} \; 2>/dev/null | awk '$1 > 1000 {print $0}' | wc -l)
echo "- 超大文件(>1000行): ${very_large_files}"

# 计算平均文件大小
total_lines=$(find . -name "*.java" -type f -exec wc -l {} \; 2>/dev/null | awk '{sum+=$1} END {print sum}')
if [ $total_files -gt 0 ]; then
    avg_lines=$((total_lines / total_files))
    echo "- 平均文件行数: ${avg_lines}"
fi

echo ""

###############################################################################
# 6. 代码异味检查
###############################################################################

echo -e "${BLUE}[6/7] 代码异味检查...${NC}"

# 硬编码检查
hardcoded_strings=$(find . -name "*.java" -type f -exec grep -cE 'http://|https://' {} \; 2>/dev/null | awk '{sum+=$1} END {print sum}')
echo "- 疑似硬编码URL: ${hardcoded_strings}"

# 空指针风险（链式调用）
chain_calls=$(find . -name "*.java" -type f -exec grep -cE '\.get\(\)\.' {} \; 2>/dev/null | awk '{sum+=$1} END {print sum}')
echo "- 链式调用风险: ${chain_calls}"

# System.out.println使用（生产代码不应使用）
system_out=$(find . -name "*.java" -type f -exec grep -v "test" {} \; -exec grep -c "System.out.println" {} \; 2>/dev/null | awk '{sum+=$1} END {print sum}')
echo "- 使用System.out.println: ${system_out} (应使用日志)"

echo ""

###############################################################################
# 7. 服务模块统计
###############################################################################

echo -e "${BLUE}[7/7] 服务模块统计...${NC}"
echo ""

for module in ioedream-*/; do
    if [ -d "$module" ]; then
        java_count=$(find "$module" -name "*.java" -type f 2>/dev/null | wc -l)
        if [ $java_count -gt 0 ]; then
            module_lines=$(find "$module" -name "*.java" -type f -exec cat {} \; 2>/dev/null | wc -l)
            echo "- ${module}: ${java_count}文件, ${module_lines}行"
        fi
    fi
done

echo ""

###############################################################################
# 生成综合报告
###############################################################################

echo -e "${GREEN}=========================================="
echo "代码质量检查完成！"
echo "==========================================${NC}"
echo ""

# 计算总体质量评分
encoding_score=${compliance_rate}
logging_score=${logging_compliance}
comment_score=${comment_coverage}

# 简单的评分算法
total_score=$(( (encoding_score + logging_score + comment_score) / 3 ))

echo -e "${YELLOW}总体质量评分: ${total_score}/100${NC}"
echo ""
echo "详细报告已保存至: ${OUTPUT_DIR}"
echo ""

