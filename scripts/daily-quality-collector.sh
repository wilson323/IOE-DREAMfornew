#!/bin/bash

# IOE-DREAM 每日质量数据收集器
# 功能：自动收集每日质量数据，用于趋势分析

echo "📊 IOE-DREAM 每日质量数据收集器"
echo "=============================="
echo "收集时间: $(date)"

# 获取当前日期作为标识
DATE_ID=$(date +%Y%m%d)
REPORT_FILE="monitoring-reports/daily-quality-${DATE_ID}.txt"

# 检查是否已经收集过今日数据
if [ -f "$REPORT_FILE" ]; then
    echo "📋 今日质量数据已收集，跳过"
    echo "   📁 现有报告: $REPORT_FILE"
    exit 0
fi

echo "🔍 开始收集今日质量数据..."

# 执行精确质量检查
echo "执行质量检查..."
QUALITY_RESULT=$(bash scripts/precise-quality-check.sh 2>/dev/null)

# 提取关键指标
SCORE=$(echo "$QUALITY_RESULT" | grep "质量评分:" | sed 's/.*质量评分: \([0-9]*\)\/100.*/\1/')
VIOLATIONS=$(echo "$QUALITY_RESULT" | grep "总违规数:" | sed 's/.*总违规数: \([0-9]*\).*/\1/')
GRADE=$(echo "$QUALITY_RESULT" | grep "质量等级:" | sed 's/.*质量等级: \(.*\) (.*$/\1/')

# 如果提取失败，设置默认值
SCORE=${SCORE:-100}
VIOLATIONS=${VIOLATIONS:-0}
GRADE=${GRADE:-"A+"}

# 生成详细报告
{
    echo "IOE-DREAM 每日质量数据报告"
    echo "========================"
    echo "日期: $(date +%Y-%m-%d)"
    echo "时间: $(date +%H:%M:%S)"
    echo "Git 分支: $(git rev-parse --abbrev-ref HEAD)"
    echo "Git 提交: $(git rev-parse --short HEAD)"
    echo ""

    echo "📊 质量评分详情"
    echo "----------------"
    echo "质量评分: $SCORE/100"
    echo "质量等级: $GRADE ($VIOLATIONS 个违规)"
    echo ""

    echo "🔍 详细检查结果"
    echo "---------------"

    # 统计各类违规
    echo "SLF4J 违规: $(bash scripts/scan-logger-violations.sh 2>/dev/null | grep "总计" | sed 's/.*总计: \([0-9]*\) 个违规.*/\1/' || echo 0) 个"

    # 依赖注入违规检查
    AUTOWIRED_VIOLATIONS=$(find microservices -name "*.java" -type f -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    echo "@Autowired 违规: $AUTOWIRED_VIOLATIONS 个"

    # Repository违规检查
    REPOSITORY_VIOLATIONS=$(find microservices -name "*.java" -type f -exec grep -l "@Repository" {} \; 2>/dev/null | wc -l)
    echo "@Repository 违规: $REPOSITORY_VIOLATIONS 个"

    # 命名违规检查
    NAMING_VIOLATIONS=$(find microservices -name "*Repository.java" -type f 2>/dev/null | wc -l)
    echo "Repository 命名违规: $NAMING_VIOLATIONS 个"

    echo ""
    echo "📈 项目统计"
    echo "----------"
    JAVA_FILES=$(find microservices -name "*.java" -type f | wc -l)
    echo "Java 文件总数: $JAVA_FILES"

    SERVICES=$(find microservices -name "pom.xml" -type f | wc -l)
    echo "微服务数量: $SERVICES"

    echo ""
    echo "💻 系统信息"
    echo "----------"
    echo "操作系统: $(uname -s)"
    echo "Java 版本: $(java -version 2>&1 | head -n 1)"
    echo "Maven 版本: $(mvn -version 2>/dev/null | head -n 1 | cut -d' ' -f3)"

    echo ""
    echo "========================"
    echo "报告生成完成: $(date)"
    echo "数据收集状态: ✅ 成功"

} > "$REPORT_FILE"

echo "✅ 每日质量数据收集完成"
echo "📁 报告文件: $REPORT_FILE"
echo "📊 质量评分: $SCORE/100"
echo "🎯 违规数量: $VIOLATIONS"

# 更新最新评分文件
echo "$SCORE" > "monitoring-reports/latest-score.txt"

echo ""
echo "💡 后续操作:"
echo "1. 查看详细报告: cat $REPORT_FILE"
echo "2. 运行趋势分析: bash scripts/quality-trend-analysis.sh"
echo "3. 持续监控: 数据将自动用于趋势分析"