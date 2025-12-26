#!/bin/bash
# IOE-DREAM 持续日志规范监控脚本
# 用于CI/CD流水线和开发环境持续监控

echo "🔍 IOE-DREAM 持续日志规范监控" -ForegroundColor Blue

# 配置
MONITOR_MODE=${1:-"check"}  # check, fix, report
PROJECT_ROOT=${2:-"."}
LOG_FILE="logging-monitor-$(date +%Y%m%d).log"

# 创建日志目录
mkdir -p logs

# 日志记录函数
log_message() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "logs/$LOG_FILE"
}

# 核心监控函数
monitor_logging_standards() {
    log_message "开始执行日志规范监控..."

    # 扫描结果
    TOTAL_FILES=$(find "$PROJECT_ROOT" -name "*.java" 2>/dev/null | wc -l)
    SLF4J_IMPORTS=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "import lombok\.extern\.slf4j\.Slf4j" {} \; 2>/dev/null | wc -l)
    TRADITIONAL_LOGGER=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "import org\.slf4j\.Logger" {} \; 2>/dev/null | wc -l)
    TRADITIONAL_FACTORY=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "import org\.slf4j\.LoggerFactory" {} \; 2>/dev/null | wc -l)
    SLF4J_ANNOTATIONS=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "@Slf4j" {} \; 2>/dev/null | wc -l)

    # 计算合规指标
    TOTAL_ISSUES=$((TRADITIONAL_LOGGER + TRADITIONAL_FACTORY))
    COMPLIANCE_RATE=$((100 - TOTAL_ISSUES))

    log_message "监控结果统计:"
    log_message "  总Java文件: $TOTAL_FILES"
    log_message "  @Slf4j导入: $SLF4J_IMPORTS"
    log_message "  @Slf4j注解: $SLF4J_ANNOTATIONS"
    log_message "  传统Logger: $TRADITIONAL_LOGGER"
    log_message "  LoggerFactory: $TRADITIONAL_FACTORY"
    log_message "  合规率: $COMPLIANCE_RATE%"

    return $TOTAL_ISSUES
}

# 详细问题报告
report_issues() {
    log_message "=== 详细问题报告 ==="

    # 传统Logger问题
    if [ $(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "import org\.slf4j\.Logger" {} \; 2>/dev/null | wc -l) -gt 0 ]; then
        log_message "❌ 发现传统Logger导入:"
        find "$PROJECT_ROOT" -name "*.java" -exec grep -l "import org\.slf4j\.Logger" {} \; 2>/dev/null | while read file; do
            log_message "  - $file"
        done
    fi

    # LoggerFactory问题
    if [ $(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "import org\.slf4j\.LoggerFactory" {} \; 2>/dev/null | wc -l) -gt 0 ]; then
        log_message "❌ 发现LoggerFactory导入:"
        find "$PROJECT_ROOT" -name "*.java" -exec grep -l "import org\.slf4j\.LoggerFactory" {} \; 2>/dev/null | while read file; do
            log_message "  - $file"
        done
    fi

    # DAO接口错误
    if [ $(find "$PROJECT_ROOT" -name "*Dao.java" -o -name "*DAO.java" | xargs grep -l "@Slf4j" 2>/dev/null | wc -l) -gt 0 ]; then
        log_message "❌ 发现DAO接口错误使用@Slf4j:"
        find "$PROJECT_ROOT" -name "*Dao.java" -o -name "*DAO.java" | xargs grep -l "@Slf4j" 2>/dev/null | while read file; do
            log_message "  - $file"
        done
    fi
}

# 生成监控报告
generate_monitoring_report() {
    REPORT_FILE="LOGGING_MONITORING_REPORT-$(date +%Y%m%d_%H%M%S).md"

    monitor_logging_standards
    TOTAL_ISSUES=$?

    cat > "$REPORT_FILE" << EOF
# IOE-DREAM 日志规范持续监控报告

**监控时间**: $(date)
**监控模式**: $MONITOR_MODE
**监控范围**: $PROJECT_ROOT

## 📊 监控统计

**基础指标**:
- 总Java文件: $(find "$PROJECT_ROOT" -name "*.java" 2>/dev/null | wc -l)
- @Slf4j导入文件: $(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "import lombok\.extern\.slf4j\.Slf4j" {} \; 2>/dev/null | wc -l)
- @Slf4j注解文件: $(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "@Slf4j" {} \; 2>/dev/null | wc -l)

**问题指标**:
- 传统Logger导入: $(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "import org\.slf4j\.Logger" {} \; 2>/dev/null | wc -l)
- LoggerFactory导入: $(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "import org\.slf4j\.LoggerFactory" {} \; 2>/dev/null | wc -l)
- DAO接口@Slf4j错误: $(find "$PROJECT_ROOT" -name "*Dao.java" -o -name "*DAO.java" | xargs grep -l "@Slf4j" 2>/dev/null | wc -l)

**合规性评估**:
- 总问题数: $TOTAL_ISSUES
- 合规率: $((100 - TOTAL_ISSUES))%
- 质量等级: $(if [ $TOTAL_ISSUES -eq 0 ]; then echo "🏆 企业级A+"; elif [ $TOTAL_ISSUES -le 5 ]; then echo "⭐ A级"; elif [ $TOTAL_ISSUES -le 20 ]; then echo "✅ B级"; else echo "⚠️ 需要改进"; fi)

## 🔍 监控建议

$(if [ $TOTAL_ISSUES -eq 0 ]; then
    echo "✅ **优秀**: 日志规范完全合规，继续保持！";
elif [ $TOTAL_ISSUES -le 5 ]; then
    echo "⭐ **良好**: 少量问题，建议及时修复";
elif [ $TOTAL_ISSUES -le 20 ]; then
    echo "✅ **一般**: 需要关注和修复发现的问题";
else
    echo "⚠️ **需要改进**: 问题较多，建议制定修复计划";
fi)

### 自动化建议
1. **集成CI/CD**: 将此监控脚本集成到CI/CD流水线
2. **Git Hooks**: 配置pre-commit hooks进行代码提交前检查
3. **定期执行**: 建议每日或每周定期执行监控
4. **告警机制**: 当合规率低于95%时触发告警

---

**生成时间**: $(date)
**监控工具**: IOE-DREAM日志规范监控系统
**质量认证**: $(if [ $TOTAL_ISSUES -eq 0 ]; then echo "企业级A+ 🏆"; else echo "持续监控中 📊"; fi)
EOF

    log_message "监控报告已生成: $REPORT_FILE"
}

# 执行监控
case $MONITOR_MODE in
    "check")
        log_message "=== 执行检查模式 ==="
        monitor_logging_standards
        ISSUES=$?

        if [ $ISSUES -eq 0 ]; then
            log_message "✅ 监控通过: 日志规范完全合规"
            exit 0
        else
            log_message "❌ 监控失败: 发现 $ISSUES 个问题"
            report_issues
            exit 1
        fi
        ;;
    "report")
        log_message "=== 生成监控报告 ==="
        generate_monitoring_report
        ;;
    "fix")
        log_message "=== 执行修复模式 ==="
        # 调用修复脚本
        if [ -f "./fix-logging-patterns.sh" ]; then
            ./fix-logging-patterns.sh
            log_message "修复脚本执行完成"
        else
            log_message "❌ 未找到修复脚本: fix-logging-patterns.sh"
            exit 1
        fi
        ;;
    *)
        echo "用法: $0 {check|report|fix} [project_root]"
        echo "  check    - 检查日志规范合规性"
        echo "  report   - 生成详细监控报告"
        echo "  fix      - 执行自动修复"
        exit 1
        ;;
esac

log_message "日志规范监控完成"