#!/bin/bash

###############################################################################
# IOE-DREAM 代码质量快速检查脚本
###############################################################################

PROJECT_ROOT="/d/IOE-DREAM"
MICROSERVICES_DIR="${PROJECT_ROOT}/microservices"
OUTPUT_DIR="${PROJECT_ROOT}/code-quality-reports"
REPORT_FILE="${OUTPUT_DIR}/quality-summary-report.md"

mkdir -p "${OUTPUT_DIR}"

echo "# IOE-DREAM 代码质量综合分析报告" > "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"
echo "**分析时间**: $(date '+%Y-%m-%d %H:%M:%S')" >> "${REPORT_FILE}"
echo "---" >> "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"

cd "${MICROSERVICES_DIR}"

# 获取总Java文件数
total_java=$(find . -name "*.java" -type f 2>/dev/null | wc -l)
echo "## 项目概览" >> "${REPORT_FILE}"
echo "- 总Java文件数: **${total_java}**" >> "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"

# 1. UTF-8编码检查
echo "## 1. UTF-8编码规范性" >> "${REPORT_FILE}"
utf8_count=$(find . -name "*.java" -type f -exec file -i {} \; 2>/dev/null | grep -c "utf-8" || echo 0)
encoding_rate=$((utf8_count * 100 / total_java))
echo "- 合规率: **${encoding_rate}%**" >> "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"

# 2. 日志规范检查
echo "## 2. 日志规范检查" >> "${REPORT_FILE}"
slf4j_count=$(find . -name "*.java" -type f -print0 2>/dev/null | xargs -0 grep -l "@Slf4j" 2>/dev/null | wc -l)
loggerfactory_count=$(find . -name "*.java" -type f -print0 2>/dev/null | xargs -0 grep -l "LoggerFactory.getLogger" 2>/dev/null | wc -l)
log_total=$((slf4j_count + loggerfactory_count))
if [ $log_total -gt 0 ]; then
    logging_compliance=$((slf4j_count * 100 / log_total))
else
    logging_compliance=0
fi
echo "- @Slf4j使用: **${slf4j_count}**" >> "${REPORT_FILE}"
echo "- LoggerFactory使用: **${loggerfactory_count}** (违规)" >> "${REPORT_FILE}"
echo "- 合规率: **${logging_compliance}%**" >> "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"

# 3. 异常处理检查
echo "## 3. 异常处理规范" >> "${REPORT_FILE}"
printstack_count=$(find . -name "*.java" -type f -print0 2>/dev/null | xargs -0 grep -c "printStackTrace" 2>/dev/null | awk '{s+=$1} END {print s+0}')
echo "- printStackTrace使用: **${printstack_count}** (不规范)" >> "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"

# 4. 注释完整性
echo "## 4. 注释完整性" >> "${REPORT_FILE}"
javadoc_files=$(find . -name "*.java" -type f -print0 2>/dev/null | xargs -0 grep -l "/**" 2>/dev/null | wc -l)
comment_rate=$((javadoc_files * 100 / total_java))
echo "- 注释覆盖率: **${comment_rate}%**" >> "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"

# 5. 代码复杂度
echo "## 5. 代码复杂度分析" >> "${REPORT_FILE}"
total_lines=$(find . -name "*.java" -type f -exec cat {} \; 2>/dev/null | wc -l)
avg_lines=$((total_lines / total_java))
large_500=$(find . -name "*.java" -type f -exec wc -l {} \; 2>/dev/null | awk '$1>500 {c++} END {print c+0}')
large_1000=$(find . -name "*.java" -type f -exec wc -l {} \; 2>/dev/null | awk '$1>1000 {c++} END {print c+0}')
echo "- 总代码行数: **${total_lines}**" >> "${REPORT_FILE}"
echo "- 平均文件行数: **${avg_lines}**" >> "${REPORT_FILE}"
echo "- 超大文件(>500行): **${large_500}**" >> "${REPORT_FILE}"
echo "- 超大文件(>1000行): **${large_1000}**" >> "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"

if [ ${large_1000} -gt 0 ]; then
    echo "### 超大文件列表（Top 10）" >> "${REPORT_FILE}"
    echo '```' >> "${REPORT_FILE}"
    find . -name "*.java" -type f -exec wc -l {} \; 2>/dev/null | sort -rn | head -10 | awk '{printf "%5d %s\n", $1, $2}' >> "${REPORT_FILE}"
    echo '```' >> "${REPORT_FILE}"
    echo "" >> "${REPORT_FILE}"
fi

# 6. 代码异味检查
echo "## 6. 代码异味检查" >> "${REPORT_FILE}"
system_out=$(find . -name "*.java" -type f -print0 2>/dev/null | xargs -0 grep -c "System.out.println" 2>/dev/null | awk '{s+=$1} END {print s+0}')
echo "- System.out.println使用: **${system_out}** (应使用日志)" >> "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"

# 7. 服务模块统计
echo "## 7. 服务模块统计" >> "${REPORT_FILE}"
echo "| 服务模块 | 文件数 | 代码行数 | 平均行/文件 |" >> "${REPORT_FILE}"
echo "|---------|-------|---------|------------|" >> "${REPORT_FILE}"
for module in ioedream-*/ microservices-common-*/; do
    if [ -d "$module" ]; then
        mod_name=$(echo "$module" | sed 's|/$||')
        java_count=$(find "$module" -name "*.java" -type f 2>/dev/null | wc -l)
        if [ $java_count -gt 0 ]; then
            mod_lines=$(find "$module" -name "*.java" -type f -exec cat {} \; 2>/dev/null | wc -l)
            mod_avg=$((mod_lines / java_count))
            echo "| ${mod_name} | ${java_count} | ${mod_lines} | ${mod_avg} |" >> "${REPORT_FILE}"
        fi
    fi
done
echo "" >> "${REPORT_FILE}"

# 8. 综合评分
echo "## 8. 综合质量评分" >> "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"

# 计算各维度得分
complexity_score=$((100 - (large_500 * 100 / total_java) * 2))
if [ ${complexity_score} -lt 0 ]; then complexity_score=0; fi

# 加权平均
total_score=$(( (encoding_rate * 20 + logging_compliance * 25 + comment_rate * 25 + complexity_score * 30) / 100 ))

echo "### 分项得分" >> "${REPORT_FILE}"
echo "- 编码规范（20%）: ${encoding_rate}/100" >> "${REPORT_FILE}"
echo "- 日志规范（25%）: ${logging_compliance}/100" >> "${REPORT_FILE}"
echo "- 注释完整（25%）: ${comment_rate}/100" >> "${REPORT_FILE}"
echo "- 代码复杂度（30%）: ${complexity_score}/100" >> "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"

if [ ${total_score} -ge 90 ]; then
    grade="优秀 (A)"
elif [ ${total_score} -ge 80 ]; then
    grade="良好 (B)"
elif [ ${total_score} -ge 70 ]; then
    grade="中等 (C)"
else
    grade="需改进 (D)"
fi

echo "# 总分: ${total_score}/100 - ${grade}" >> "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"

# 9. 改进建议
echo "## 9. 质量改进建议" >> "${REPORT_FILE}"
echo "" >> "${REPORT_FILE}"
echo "### P0级别（立即修复）" >> "${REPORT_FILE}"
if [ ${loggerfactory_count} -gt 0 ]; then
    echo "- 修复${loggerfactory_count}个日志规范违规文件" >> "${REPORT_FILE}"
fi
if [ ${printstack_count} -gt 0 ]; then
    echo "- 移除${printstack_count}处printStackTrace使用" >> "${REPORT_FILE}"
fi
echo "" >> "${REPORT_FILE}"

echo "### P1级别（高优先级）" >> "${REPORT_FILE}"
if [ ${large_500} -gt 0 ]; then
    echo "- 重构${large_500}个超大文件" >> "${REPORT_FILE}"
fi
if [ ${comment_rate} -lt 80 ]; then
    echo "- 提升注释覆盖率至80%以上" >> "${REPORT_FILE}"
fi
echo "" >> "${REPORT_FILE}"

echo "报告生成完成: ${REPORT_FILE}"
echo ""

# 输出报告内容
cat "${REPORT_FILE}"

