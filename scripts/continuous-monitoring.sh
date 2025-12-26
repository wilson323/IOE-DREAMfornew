#!/bin/bash

# 持续监控脚本 - 自动化监控代码质量
# 可以作为 cron job 或 CI/CD pipeline 的一部分

MONITORING_DIR="monitoring-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="$MONITORING_DIR/quality-report-$TIMESTAMP.txt"

# 创建监控报告目录
mkdir -p "$MONITORING_DIR"

echo "🔍 IOE-DREAM 持续质量监控"
echo "================================"
echo "时间: $(date)"
echo "报告文件: $REPORT_FILE"
echo ""

# 开始写入报告
{
    echo "IOE-DREAM 代码质量监控报告"
    echo "============================"
    echo "生成时间: $(date)"
    echo "Git 分支: ${GIT_BRANCH:-$(git rev-parse --abbrev-ref HEAD)}"
    echo "Git 提交: ${GIT_COMMIT:-$(git rev-parse --short HEAD)}"
    echo ""

    echo "📊 SLF4J 规范监控"
    echo "-----------------"

    # 检查各服务的 LoggerFactory 违规
    services=("access-service" "attendance-service" "oa-service" "video-service" "visitor-service" "device-comm-service" "biometric-service" "common-service")

    total_violations=0
    for service in "${services[@]}"; do
        violations=$(find microservices/$service -name "*.java" -type f -exec grep -l "LoggerFactory.getLogger" {} \; 2>/dev/null | wc -l)
        echo "📁 $service: $violations 个违规"
        total_violations=$((total_violations + violations))

        # 如果有违规，列出前3个文件
        if [ $violations -gt 0 ]; then
            echo "   违规文件:"
            find microservices/$service -name "*.java" -type f -exec grep -l "LoggerFactory.getLogger" {} \; 2>/dev/null | head -3 | while read file; do
                echo "   - $(basename "$file")"
            done
            if [ $violations -gt 3 ]; then
                echo "   ... 还有 $((violations - 3)) 个文件"
            fi
        fi
        echo ""
    done

    echo "📈 SLF4J 总计: $total_violations 个违规"
    echo ""

    echo "🔧 依赖注入规范监控"
    echo "-------------------"

    autowired_violations=$(find microservices -name "*.java" -type f -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    echo "📊 @Autowired 违规: $autowired_violations 个"
    if [ $autowired_violations -gt 0 ]; then
        echo "   详情: 需要修复为 @Resource 注解"
    fi
    echo ""

    repository_violations=$(find microservices -name "*.java" -type f -exec grep -l "@Repository" {} \; 2>/dev/null | wc -l)
    echo "📊 @Repository 违规: $repository_violations 个"
    if [ $repository_violations -gt 0 ]; then
        echo "   详情: 需要修复为 @Mapper 注解"
    fi
    echo ""

    naming_violations=$(find microservices -name "*Repository.java" -type f 2>/dev/null | wc -l)
    echo "📊 Repository 命名违规: $naming_violations 个"
    if [ $naming_violations -gt 0 ]; then
        echo "   详情: 需要重命名为 *Dao.java"
    fi
    echo ""

    echo "🔒 安全配置监控"
    echo "---------------"

    plain_passwords=$(grep -r "password.*=" microservices --include="*.yml" --include="*.properties" --include="*.yaml" 2>/dev/null | grep -v "ENC(" | wc -l)
    echo "📊 明文密码: $plain_passwords 个"
    if [ $plain_passwords -gt 0 ]; then
        echo "   ⚠️ 发现明文密码，需要加密存储"
    fi
    echo ""

    echo "📁 项目结构监控"
    echo "---------------"

    java_files=$(find microservices -name "*.java" -type f 2>/dev/null | wc -l)
    echo "📊 Java 文件总数: $java_files"

    pom_files=$(find microservices -name "pom.xml" -type f 2>/dev/null | wc -l)
    echo "📊 POM 文件数: $pom_files"
    echo ""

    echo "🎯 质量评分"
    echo "----------"

    # 计算质量评分
    total_issues=$((total_violations + autowired_violations + repository_violations + naming_violations + plain_passwords))

    if [ $total_issues -eq 0 ]; then
        score=100
        grade="A+"
        status="✅ 优秀"
    elif [ $total_issues -le 5 ]; then
        score=90
        grade="A"
        status="✅ 良好"
    elif [ $total_issues -le 20 ]; then
        score=75
        grade="B"
        status="⚠️ 一般"
    elif [ $total_issues -le 50 ]; then
        score=60
        grade="C"
        status="❌ 需改进"
    else
        score=40
        grade="D"
        status="❌ 较差"
    fi

    echo "📊 质量评分: $score/100"
    echo "📊 质量等级: $grade"
    echo "📊 总问题数: $total_issues"
    echo "📊 状态: $status"
    echo ""

    echo "📋 改进建议"
    echo "----------"

    if [ $total_violations -gt 0 ]; then
        echo "1. 修复 LoggerFactory 违规: bash scripts/fix-logger-violations.sh"
    fi

    if [ $autowired_violations -gt 0 ]; then
        echo "2. 修复 @Autowired 违规: bash scripts/fix-autowired-violations.sh"
    fi

    if [ $repository_violations -gt 0 ]; then
        echo "3. 修复 @Repository 违规: bash scripts/fix-repository-violations.sh"
    fi

    if [ $naming_violations -gt 0 ]; then
        echo "4. 修复命名违规: bash scripts/fix-naming-violations.sh"
    fi

    if [ $plain_passwords -gt 0 ]; then
        echo "5. 加密敏感配置: 使用 Nacos 加密配置"
    fi

    echo ""
    echo "📊 历史趋势分析"
    echo "--------------"

    # 分析历史报告（如果存在）
    if [ -f "$MONITORING_DIR/latest-score.txt" ]; then
        previous_score=$(cat "$MONITORING_DIR/latest-score.txt")
        score_change=$((score - previous_score))

        if [ $score_change -gt 0 ]; then
            echo "📈 质量提升: +$score_change 分"
        elif [ $score_change -lt 0 ]; then
            echo "📉 质量下降: $score_change 分"
        else
            echo "➡️ 质量稳定: 无变化"
        fi
    fi

    # 保存当前评分作为历史记录
    echo "$score" > "$MONITORING_DIR/latest-score.txt"

    echo ""
    echo "============================"
    echo "报告生成完成"

} > "$REPORT_FILE"

# 显示报告摘要
echo "📊 监控摘要:"
echo "   SLF4J 违规: $total_violations 个"
echo "   @Autowired 违规: $autowired_violations 个"
echo "   @Repository 违规: $repository_violations 个"
echo "   Repository 命名违规: $naming_violations 个"
echo "   明文密码: $plain_passwords 个"
echo "   质量评分: $score/100 ($grade)"
echo ""

if [ $score -ge 90 ]; then
    echo "🎉 代码质量优秀！"
    exit 0
elif [ $score -ge 75 ]; then
    echo "✅ 代码质量良好"
    exit 0
elif [ $score -ge 60 ]; then
    echo "⚠️ 代码质量一般，建议改进"
    exit 1
else
    echo "❌ 代码质量较差，需要立即改进"
    exit 2
fi