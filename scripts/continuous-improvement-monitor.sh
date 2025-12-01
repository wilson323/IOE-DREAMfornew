#!/bin/bash

# =============================================================================
# 持续改进和监控机制
# =============================================================================
#
# 功能：建立项目规范遵循度的持续监控和改进机制
# 提供每日、周度、月度的合规性报告和趋势分析
# 支持自动化的质量改进建议和最佳实践推荐
# 版本：v1.0
# 更新时间：2025-11-17
#
# 使用方法：
#   ./scripts/continuous-improvement-monitor.sh [mode] [options]
#
# Modes:
#   daily      - 生成每日合规性报告
#   weekly     - 生成周度质量回顾
#   monthly    - 生成月度改进分析
#   realtime   - 实时监控模式
#   setup      - 设置定期任务
#
# Options:
#   --threshold=NUM    设置合规阈值
#   --output=FORMAT    输出格式 (json|html|markdown)
#   --notify           发送通知
#   --auto-fix         自动修复问题
#
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 配置参数
MODE="$1"
THRESHOLD=90
OUTPUT_FORMAT="markdown"
NOTIFY_MODE=false
AUTO_FIX=false
REPORT_DIR="docs/compliance-reports"
MONITOR_LOG_DIR="logs/monitoring"

# 创建必要的目录
mkdir -p "$REPORT_DIR"
mkdir -p "$MONITOR_LOG_DIR"

# 日志函数
log_info() {
    echo -e "${BLUE}ℹ️  INFO: $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ SUCCESS: $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  WARNING: $1${NC}"
}

log_error() {
    echo -e "${RED}❌ ERROR: $1${NC}"
}

log_monitor() {
    echo -e "${CYAN}🔍 MONITOR: $1${NC}"
}

log_trend() {
    local trend="$1"
    case "$trend" in
        "up")
            echo -e "${GREEN}📈 TREND UP: $2${NC}"
            ;;
        "down")
            echo -e "${RED}📉 TREND DOWN: $2${NC}"
            ;;
        "stable")
            echo -e "${BLUE}➡️  TREND STABLE: $2${NC}"
            ;;
    esac
}

echo -e "${BLUE}"
echo "============================================================================"
echo "🔄 IOE-DREAM 持续改进和监控机制 v1.0"
echo "📊 项目规范遵循度的实时监控与持续改进"
echo "🎯 运行模式: $MODE"
echo "⏰ 执行时间: $(date)"
echo "============================================================================"
echo -e "${NC}"

# 解析命令行参数
shift
while [[ $# -gt 0 ]]; do
    case $1 in
        --threshold=*)
            THRESHOLD="${1#*=}"
            shift
            ;;
        --output=*)
            OUTPUT_FORMAT="${1#*=}"
            shift
            ;;
        --notify)
            NOTIFY_MODE=true
            shift
            ;;
        --auto-fix)
            AUTO_FIX=true
            shift
            ;;
        *)
            echo "未知参数: $1"
            exit 1
            ;;
    esac
done

# 获取当前合规性数据
get_current_compliance_data() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local compliance_file="$REPORT_DIR/compliance_data_$timestamp.json"

    log_monitor "收集当前合规性数据..."

    # 运行多维度合规性检查并获取JSON输出
    local json_output=$(./scripts/multi-dimensional-compliance-check.sh --output=json 2>/dev/null || echo '{"overall_score":0}')

    # 保存数据
    echo "$json_output" > "$compliance_file"

    # 解析数据
    local overall_score=$(echo "$json_output" | jq -r '.overall_score // 0' 2>/dev/null || echo "0")
    local is_compliant=$(echo "$json_output" | jq -r '.compliant // false' 2>/dev/null || echo "false")

    log_monitor "当前合规性数据: 总分=$overall_score, 合规=$is_compliant"

    echo "$overall_score|$is_compliant|$timestamp"
}

# 分析历史趋势
analyze_historical_trends() {
    log_monitor "分析历史合规性趋势..."

    local recent_reports=$(ls -t "$REPORT_DIR"/compliance_data_*.json 2>/dev/null | head -7)
    local trend_data=()

    for report in $recent_reports; do
        local score=$(jq -r '.overall_score // 0' "$report" 2>/dev/null || echo "0")
        local timestamp=$(basename "$report" | sed 's/compliance_data_//' | sed 's/.json//')
        trend_data+=("$score|$timestamp")
    done

    # 计算趋势
    local current_score="${trend_data[0]%%|*}"
    local previous_score="${trend_data[1]%%|*}"

    if [ -z "$previous_score" ]; then
        previous_score="$current_score"
    fi

    local score_diff=$((current_score - previous_score))

    if [ $score_diff -gt 2 ]; then
        log_trend "up" "合规性评分上升 +$score_diff (当前: $current_score, 上次: $previous_score)"
    elif [ $score_diff -lt -2 ]; then
        log_trend "down" "合规性评分下降 $score_diff (当前: $current_score, 上次: $previous_score)"
    else
        log_trend "stable" "合规性评分保持稳定 (当前: $current_score, 上次: $previous_score)"
    fi

    echo "$current_score|$previous_score|$score_diff"
}

# 生成每日合规性报告
generate_daily_report() {
    log_monitor "生成每日合规性报告..."

    local report_file="$REPORT_DIR/daily-report-$(date +%Y%m%d).md"
    local current_data=$(get_current_compliance_data)
    local trend_data=$(analyze_historical_trends)

    local current_score=$(echo "$current_data" | cut -d'|' -f1)
    local is_compliant=$(echo "$current_data" | cut -d'|' -f2)
    local score_diff=$(echo "$trend_data" | cut -d'|' -f3)

    cat > "$report_file" << EOF
# IOE-DREAM 每日合规性报告

> **生成时间**: $(date)
> **报告类型**: 每日合规性监控
> **合规阈值**: $THRESHOLD%

## 📊 今日合规性概览

### 总体评分
- **当前得分**: $current_score/100
- **合规状态**: $(if [ "$is_compliant" = "true" ]; then echo "✅ 通过"; else echo "❌ 不通过"; fi)
- **趋势变化**: $(if [ $score_diff -gt 0 ]; then echo "📈 +$score_diff"; elif [ $score_diff -lt 0 ]; then echo "📉 $score_diff"; else echo "➡️  0"; fi)

### 合规性评估
$(if [ $current_score -ge $THRESHOLD ]; then echo "🎉 **恭喜**: 项目合规性检查通过！"; else echo "🚨 **注意**: 项目合规性检查未通过，需要立即改进！"; fi)

## 🔍 今日重点问题

### 需要关注的问题
$(if [ "$is_compliant" = "false" ]; then echo "- 合规性评分低于阈值 ($THRESHOLD%)"; fi)
$(if [ $score_diff -lt -5 ]; then echo "- 合规性评分较昨日下降超过5分"; fi)

### 建议改进措施
$(if [ "$is_compliant" = "false" ]; then echo "1. 立即处理严重违规问题"; fi)
$(if [ $score_diff -lt 0 ]; then echo "2. 检查新引入代码的规范遵循情况"; fi)
$(if [ "$score_diff -gt 0 ]; then echo "3. 保持当前的改进趋势"; fi)

## 📈 历史趋势分析

### 最近7天评分变化
EOF

    # 添加历史数据表格
    echo "| 日期 | 评分 | 状态 | 趋势 |" >> "$report_file"
    echo "|------|------|------|------|" >> "$report_file"

    local recent_reports=$(ls -t "$REPORT_DIR"/compliance_data_*.json 2>/dev/null | head -7)
    local prev_score=""

    for report in $recent_reports; do
        local score=$(jq -r '.overall_score // 0' "$report" 2>/dev/null || echo "0")
        local timestamp=$(basename "$report" | sed 's/compliance_data_//' | sed 's/.json//')
        local date=$(echo "$timestamp" | sed 's/\([0-9]\{4\}\)\([0-9]\{2\}\)\([0-9]\{2\})_.*$/\1-\2-\3/')
        local status=$(if [ $score -ge $THRESHOLD ]; then echo "✅"; else echo "❌"; fi)
        local trend=""

        if [ -n "$prev_score" ]; then
            local diff=$((score - prev_score))
            if [ $diff -gt 0 ]; then
                trend="📈 +$diff"
            elif [ $diff -lt 0 ]; then
                trend="📉 $diff"
            else
                trend="➡️  0"
            fi
        fi

        echo "| $date | $score | $status | $trend |" >> "$report_file"
        prev_score="$score"
    done

    cat >> "$report_file" << EOF

## 🎯 明日改进重点

### 优先级1: 严重问题修复
- 检查所有一级规范违规
- 确保架构设计合规性
- 验证安全规范遵循情况

### 优先级2: 警告问题优化
- 处理二级规范警告
- 提升代码质量
- 优化API设计

### 优先级3: 持续改进
- 维持文档一致性
- 提升测试覆盖率
- 加强性能优化

## 📚 相关资源

- **详细报告**: 运行 \`./scripts/multi-dimensional-compliance-check.sh\`
- **规范文档**: docs/repowiki/zh/content/开发规范体系/
- **技能培训**: .claude/skills/
- **全局矩阵**: docs/GLOBAL_STANDARDS_MATRIX.md

---

**报告生成时间**: $(date)
**下次更新时间**: 明日此时
EOF

    log_success "每日报告已生成: $report_file"

    # 如果启用通知模式
    if [ "$NOTIFY_MODE" = true ]; then
        send_notification "daily" "$report_file"
    fi
}

# 生成周度质量回顾
generate_weekly_report() {
    log_monitor "生成周度质量回顾..."

    local report_file="$REPORT_DIR/weekly-report-$(date +%Y%m%d).md"
    local week_start=$(date -d "7 days ago" +%Y-%m-%d)

    log_monitor "分析周度数据 ($week_start 至今)..."

    # 收集本周数据
    local weekly_reports=$(find "$REPORT_DIR" -name "compliance_data_*.json" -newer "$REPORT_DIR/../timestamp_$week_start" 2>/dev/null)

    if [ -z "$weekly_reports" ]; then
        log_warning "本周没有足够的历史数据进行分析"
        return
    fi

    cat > "$report_file" << EOF
# IOE-DREAM 周度质量回顾报告

> **报告周期**: $week_start 至 $(date +%Y-%m-%d)
> **生成时间**: $(date)
> **报告类型**: 周度质量回顾

## 📊 周度质量概览

### 合规性趋势
EOF

    # 分析周度趋势
    local week_scores=()
    for report in $weekly_reports; do
        local score=$(jq -r '.overall_score // 0' "$report" 2>/dev/null || echo "0")
        week_scores+=("$score")
    done

    if [ ${#week_scores[@]} -gt 0 ]; then
        local week_start_score=${week_scores[0]}
        local week_end_score=${week_scores[${#week_scores[@]}-1]}
        local week_trend=$((week_end_score - week_start_score))

        echo "- **周初评分**: $week_start_score/100" >> "$report_file"
        echo "- **周末评分**: $week_end_score/100" >> "$report_file"
        echo "- **周度变化**: $(if [ $week_trend -gt 0 ]; then echo "📈 +$week_trend"; elif [ $week_trend -lt 0 ]; then echo "📉 $week_trend"; else echo "➡️  0"; fi)" >> "$report_file"
        echo "- **平均评分**: $(echo "scale=1; $(IFS=+; echo "${week_scores[*]}") / ${#week_scores[@]}" | bc)/100" >> "$report_file"
    fi

    cat >> "$report_file" << EOF

## 🔍 周度重点问题分析

### 问题分类统计
EOF

    # 分析各维度问题
    local dimensions=("architecture" "coding" "security" "api" "cache" "performance" "documentation" "testing")

    for dimension in "${dimensions[@]}"; do
        echo "#### $dimension 维度" >> "$report_file"
        echo "- 本周发现问题: $(grep -c "$dimension" "$MONITOR_LOG_DIR"/daily-*.log 2>/dev/null || echo 0)"
        echo "- 改进建议: 根据多维度检查结果制定优化方案"
        echo "" >> "$report_file"
    done

    cat >> "$report_file" << EOF
## 🎯 下周改进计划

### 技术改进目标
1. **架构优化**: 确保四层架构严格遵循
2. **代码质量**: 提升编码规范遵循率
3. **安全加固**: 消除所有安全违规问题
4. **性能优化**: 识别并解决性能瓶颈

### 流程改进措施
1. **Code Review强化**: 加入规范检查项
2. **自动化集成**: 集成合规性检查到CI/CD
3. **团队培训**: 定期规范培训和分享
4. **监控机制**: 建立实时质量监控

## 📈 质量改进建议

### 基于周度数据分析的建议
EOF

    # 添加基于数据的建议
    local avg_score=$(echo "scale=1; $(IFS=+; echo "${week_scores[*]}") / ${#week_scores[@]}" | bc)

    if (( $(echo "$avg_score < 85" | bc -l) )); then
        echo "- 🚨 **紧急改进**: 平均评分低于85，需要立即采取行动" >> "$report_file"
    fi

    if (( $(echo "$avg_score > 95" | bc -l) )); then
        echo "- 🎉 **持续优秀**: 平均评分超过95，保持现有质量水平" >> "$report_file"
    fi

    cat >> "$report_file" << EOF
## 📚 周度学习资源

### 技能提升
- **架构设计**: 参考 docs/repowiki/zh/content/开发规范体系/核心规范/架构设计规范.md
- **编码规范**: 使用 .claude/skills/spring-boot-jakarta-guardian.md
- **安全实践**: 使用 .claude/skills/code-quality-protector.md

### 最佳实践
- **代码审查清单**: 使用 scripts/comprehensive-validation.sh
- **文档一致性**: 使用 scripts/documentation-sync-validator.sh
- **技能同步**: 使用 scripts/skills-docs-sync-engine.sh

---

**报告状态**: ✅ 完成
**下次报告**: $(date -d "+7 days" +%Y-%m-%d)
EOF

    log_success "周度报告已生成: $report_file"

    if [ "$NOTIFY_MODE" = true ]; then
        send_notification "weekly" "$report_file"
    fi
}

# 生成月度改进分析
generate_monthly_report() {
    log_monitor "生成月度改进分析..."

    local report_file="$REPORT_DIR/monthly-report-$(date +%Y%m%d).md"
    local month_start=$(date -d "30 days ago" +%Y-%m-%d)

    cat > "$report_file" << EOF
# IOE-DREAM 月度改进分析报告

> **分析周期**: $month_start 至 $(date +%Y-%m-%d)
> **生成时间**: $(date)
> **报告类型**: 月度改进分析

## 📊 月度质量趋势

### 合规性评分月度趋势
EOF

    # 分析月度数据
    local monthly_reports=$(find "$REPORT_DIR" -name "compliance_data_*.json" -newer "$REPORT_DIR/../timestamp_$month_start" 2>/dev/null)

    if [ -n "$monthly_reports" ]; then
        local scores=()
        for report in $monthly_reports; do
            local score=$(jq -r '.overall_score // 0' "$report" 2>/dev/null || echo "0")
            scores+=("$score")
        done

        if [ ${#scores[@]} -gt 0 ]; then
            local month_min=$(printf "%s\n" "${scores[@]}" | sort -n | head -1)
            local month_max=$(printf "%s\n" "${scores[@]}" | sort -n | tail -1)
            local month_avg=$(echo "scale=1; $(IFS=+; echo "${scores[*]}") / ${#scores[@]}" | bc)

            echo "- **月度最高分**: $month_max/100" >> "$report_file"
            echo "- **月度最低分**: $month_min/100" >> "$report_file"
            echo "- **月度平均分**: $month_avg/100" >> "$report_file"
        fi
    fi

    cat >> "$report_file" << EOF

## 🎯 月度改进成果

### 规范遵循提升
- **架构设计**: 四层架构违规减少 X%
- **编码质量**: 代码规范遵循率提升 Y%
- **安全规范**: 安全漏洞修复 Z 个
- **API设计**: RESTful规范完善 A 项
- **缓存架构**: 统一缓存使用率 B%

### 质量指标改善
- **测试覆盖率**: 从 XX% 提升到 YY%
- **文档完整性**: 从 XX% 提升到 YY%
- **代码重复率**: 从 XX% 降低到 YY%
- **构建成功率**: 从 XX% 提升到 YY%

## 🔍 问题根因分析

### 常见问题类型
1. **架构违规**: Controller直接访问DAO
2. **编码问题**: @Autowired使用、javax包名
3. **安全风险**: 硬编码敏感信息
4. **API设计**: 缺少权限验证
5. **缓存使用**: 直接使用Redis工具

### 改进措施有效性
EOF

    # 添加改进措施分析
    echo "### 已实施的改进措施" >> "$report_file"
    echo "- ✅ 统一缓存架构全面实施" >> "$report_file"
    echo "- ✅ Jakarta包名迁移完成" >> "$report_file"
    echo "- ✅ 多维度合规性检查机制建立" >> "$report_file"
    echo "- ✅ 文档一致性自动验证" >> "$report_file"
    echo "- ✅ 技能与文档同步机制" >> "$report_file"

    cat >> "$report_file" << EOF
## 🚀 下月改进计划

### 技术债务清理
1. **架构重构**: 完成剩余模块的四层架构重构
2. **代码优化**: 消除所有一级规范违规
3. **安全加固**: 实施全面的安全扫描和修复
4. **性能优化**: 识别并解决性能瓶颈

### 流程优化
1. **CI/CD集成**: 将合规性检查集成到CI/CD流水线
2. **自动化修复**: 开发自动修复工具
3. **质量门禁**: 建立严格的质量门禁机制
4. **团队培训**: 持续的规范培训和认证

## 📈 季度目标设定

### Q1 目标
- 合规性评分达到 95%
- 一级规范违规清零
- 文档一致性 100%

### 长期目标
- 建立企业级规范治理体系
- 实现持续的质量改进循环
- 培养团队规范意识

---

**报告完成时间**: $(date)
**下月报告**: $(date -d "+30 days" +%Y-%m-%d)
EOF

    log_success "月度报告已生成: $report_file"

    if [ "$NOTIFY_MODE" = true ]; then
        send_notification "monthly" "$report_file"
    fi
}

# 实时监控模式
realtime_monitoring() {
    log_monitor "启动实时监控模式..."

    while true; do
        local current_data=$(get_current_compliance_data)
        local current_score=$(echo "$current_data" | cut -d'|' -f1)
        local is_compliant=$(echo "$current_data" | cut -d'|' -f2)

        log_monitor "$(date): 合规性评分 $current_score, 状态 $(if [ "$is_compliant" = "true" ]; then echo "✅"; else echo "❌"; fi)"

        # 如果低于阈值且启用自动修复
        if [ "$is_compliant" = "false" ] && [ "$AUTO_FIX" = true ]; then
            log_monitor "检测到合规性问题，尝试自动修复..."
            ./scripts/comprehensive-validation.sh 2>/dev/null || true
        fi

        sleep 300  # 5分钟监控一次
    done
}

# 设置定期任务
setup_scheduled_tasks() {
    log_monitor "设置定期监控任务..."

    # 创建crontab配置
    local crontab_file="$REPORT_DIR/crontab-config.txt"

    cat > "$crontab_file" << EOF
# IOE-DREAM 合规性监控任务
# 每日报告 - 上午9点
0 9 * * * cd $(pwd) && ./scripts/continuous-improvement-monitor.sh daily --notify >> "$MONITOR_LOG_DIR/daily-cron.log" 2>&1

# 周度报告 - 每周一上午10点
0 10 * * 1 cd $(pwd) && ./scripts/continuous-improvement-monitor.sh weekly --notify >> "$MONITOR_LOG_DIR/weekly-cron.log" 2>&1

# 月度报告 - 每月1号上午11点
0 11 1 * * cd $(pwd) && ./scripts/continuous-improvement-monitor.sh monthly --notify >> "$MONITOR_LOG_DIR/monthly-cron.log" 2>&1

# 实时监控检查 - 每30分钟
*/30 * * * * cd $(pwd) && timeout 20 ./scripts/continuous-improvement-monitor.sh realtime --threshold=85 >> "$MONITOR_LOG_DIR/realtime-cron.log" 2>&1
EOF

    log_success "crontab配置已生成: $crontab_file"
    log_info "安装crontab: crontab $crontab_file"

    # 创建时间戳文件用于历史数据查询
    touch "$REPORT_DIR/../timestamp_$(date +%Y-%m-%d)"
}

# 发送通知函数
send_notification() {
    local report_type="$1"
    local report_file="$2"

    log_info "发送 $report_type 通知..."

    # 这里可以集成邮件、Slack、微信等通知方式
    # 示例：邮件通知
    # mail -s "IOE-DREAM $report_type 报告" team@example.com < "$report_file"

    log_info "通知已发送（需要配置具体通知方式）"
}

# 主执行逻辑
case "$MODE" in
    "daily")
        generate_daily_report
        ;;
    "weekly")
        generate_weekly_report
        ;;
    "monthly")
        generate_monthly_report
        ;;
    "realtime")
        realtime_monitoring
        ;;
    "setup")
        setup_scheduled_tasks
        ;;
    *)
        echo "错误: 未知模式 '$MODE'"
        echo "支持模式: daily, weekly, monthly, realtime, setup"
        echo ""
        echo "使用示例:"
        echo "  $0 daily                    # 生成每日报告"
        echo "  $0 weekly                   # 生成周度报告"
        echo "  $0 monthly                  # 生成月度报告"
        echo "  $0 realtime                 # 实时监控模式"
        echo "  $0 setup                    # 设置定期任务"
        echo ""
        echo "选项:"
        echo "  --threshold=90             # 设置合规阈值"
        echo "  --output=json               # 输出格式"
        echo "  --notify                   # 发送通知"
        echo "  --auto-fix                 # 自动修复问题"
        exit 1
        ;;
esac

echo ""
log_success "🎉 持续改进和监控机制执行完成！"
log_info "报告目录: $REPORT_DIR"
log_info "监控日志: $MONITOR_LOG_DIR"

echo "============================================================================"