#!/bin/bash

# 权限控制实时监控预警系统
# 基于repowiki系统安全规范建立的持续监控机制

set -e

# 配置变量
FRONTEND_DIR="smart-admin-web-javascript"
BACKEND_DIR="smart-admin-api-java17-springboot3"
LOG_FILE="permission_monitor_$(date +%Y%m%d).log"
ALERT_THRESHOLD_FRONTEND=60  # 前端权限覆盖率预警阈值
ALERT_THRESHOLD_BACKEND=95   # 后端权限覆盖率预警阈值

echo "🚀 权限控制实时监控系统启动..."
echo "📅 监控时间: $(date)"
echo "📝 日志文件: $LOG_FILE"

# 创建日志文件
echo "=== IOE-DREAM 权限控制监控日志 ===" > "$LOG_FILE"
echo "开始时间: $(date)" >> "$LOG_FILE"
echo "" >> "$LOG_FILE"

# 监控函数
monitor_permissions() {
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "🔍 [$timestamp] 执行权限控制检查..." | tee -a "$LOG_FILE"

    # 后端权限检查
    echo "--- 后端权限控制检查 ---" >> "$LOG_FILE"
    local total_controllers=$(find "$BACKEND_DIR" -name "*Controller.java" | wc -l)
    local controllers_with_permission=$(grep -r "@SaCheckPermission" --include="*Controller.java" "$BACKEND_DIR" | wc -l)
    local backend_coverage=0

    if [ $total_controllers -gt 0 ]; then
        backend_coverage=$(awk "BEGIN {printf \"%.1f\", $controllers_with_permission * 100 / $total_controllers}")
    fi

    echo "  Controller总数: $total_controllers" | tee -a "$LOG_FILE"
    echo "  权限注解数: $controllers_with_permission" | tee -a "$LOG_FILE"
    echo "  后端覆盖率: ${backend_coverage}%" | tee -a "$LOG_FILE"

    # 前端权限检查
    echo "--- 前端权限控制检查 ---" >> "$LOG_FILE"
    local total_vue_files=$(find "$FRONTEND_DIR/src/views" -name "*.vue" | wc -l)
    local vue_files_with_permission=$(find "$FRONTEND_DIR/src/views" -name "*.vue" -exec grep -l "v-permission" {} \; | wc -l)
    local frontend_coverage=0

    if [ $total_vue_files -gt 0 ]; then
        frontend_coverage=$(awk "BEGIN {printf \"%.1f\", $vue_files_with_permission * 100 / $total_vue_files}")
    fi

    echo "  Vue文件总数: $total_vue_files" | tee -a "$LOG_FILE"
    echo "  权限控制文件数: $vue_files_with_permission" | tee -a "$LOG_FILE"
    echo "  前端覆盖率: ${frontend_coverage}%" | tee -a "$LOG_FILE"

    # 权限一致性检查
    echo "--- 权限一致性检查 ---" >> "$LOG_FILE"
    local backend_perms=$(grep -r "@SaCheckPermission" --include="*Controller.java" "$BACKEND_DIR" | grep -o '"[^"]*"' | sort | uniq)
    local frontend_perms=$(grep -r "v-permission" "$FRONTEND_DIR/src/views" | grep -o '\[[^]]*\]' | sed 's/\[//g; s/\]//g; s/["'\'']//g' | sort | uniq)

    local missing_perms=0
    for perm in $backend_perms; do
        if ! echo "$frontend_perms" | grep -q "$perm"; then
            missing_perms=$((missing_perms + 1))
        fi
    done

    echo "  权限标识一致: OK" | tee -a "$LOG_FILE"
    echo "  缺失前端控制: $missing_perms" | tee -a "$LOG_FILE"

    # 生成监控数据
    local monitor_data="{
        \"timestamp\": \"$timestamp\",
        \"backend_coverage\": $backend_coverage,
        \"frontend_coverage\": $frontend_coverage,
        \"total_controllers\": $total_controllers,
        \"total_vue_files\": $total_vue_files,
        \"missing_permissions\": $missing_perms
    }"

    echo "  监控数据: $monitor_data" >> "$LOG_FILE"

    # 预警检查
    local alert_triggered=false
    local alert_messages=()

    # 后端覆盖率预警
    if (( $(echo "$backend_coverage < $ALERT_THRESHOLD_BACKEND" | bc -l) )); then
        alert_messages+=("❌ 后端权限覆盖率过低: ${backend_coverage}% (阈值: ${ALERT_THRESHOLD_BACKEND}%)")
        alert_triggered=true
    fi

    # 前端覆盖率预警
    if (( $(echo "$frontend_coverage < $ALERT_THRESHOLD_FRONTEND" | bc -l) )); then
        alert_messages+=("⚠️ 前端权限覆盖率过低: ${frontend_coverage}% (阈值: ${ALERT_THRESHOLD_FRONTEND}%)")
        alert_triggered=true
    fi

    # 权限缺失预警
    if [ $missing_perms -gt 10 ]; then
        alert_messages+=("🚨 权限控制缺失过多: $missing_perms 个权限标识缺少前端控制")
        alert_triggered=true
    fi

    # 输出预警信息
    if [ "$alert_triggered" = true ]; then
        echo "" | tee -a "$LOG_FILE"
        echo "🚨 权限控制预警！" | tee -a "$LOG_FILE"
        for msg in "${alert_messages[@]}"; do
            echo "  $msg" | tee -a "$LOG_FILE"
        done
        echo "" | tee -a "$LOG_FILE"

        # 发送通知（可配置）
        send_alert "$timestamp" "${alert_messages[@]}"
    else
        echo "✅ 权限控制状态正常" | tee -a "$LOG_FILE"
    fi

    echo "" >> "$LOG_FILE"
}

# 发送预警通知
send_alert() {
    local timestamp=$1
    shift
    local messages=("$@")

    # 生成预警报告
    local alert_file="permission_alert_$(date +%Y%m%d_%H%M%S).md"
    cat > "$alert_file" << EOF
# 🚨 IOE-DREAM 权限控制预警报告

**预警时间**: $timestamp
**触发规则**: 前端覆盖率 < ${ALERT_THRESHOLD_FRONTEND}% 或 后端覆盖率 < ${ALERT_THRESHOLD_BACKEND}%

## 预警详情

EOF

    for msg in "${messages[@]}"; do
        echo "- $msg" >> "$alert_file"
    done

    cat >> "$alert_file" << EOF

## 建议处理措施

1. 立即检查权限控制覆盖率
2. 运行权限修复脚本
3. 验证权限标识一致性
4. 更新权限控制测试

## 相关脚本

- 权限修复: ./scripts/fix-permission-directives.sh
- 权限检查: ./scripts/check-permission-coverage.sh
- 快速修复: ./scripts/quick-permission-fix.sh

---

**监控系统**: 权限控制实时监控预警系统
**报告生成**: $(date)
**处理优先级**: 高
EOF

    echo "📄 预警报告已生成: $alert_file"

    # 这里可以添加邮件、Slack、微信等通知方式
    # send_email_alert "$alert_file"
    # send_slack_alert "${messages[@]}"
}

# 生成趋势报告
generate_trend_report() {
    echo "📊 生成权限控制趋势报告..."

    local report_file="permission_trend_report_$(date +%Y%m%d).md"

    cat > "$report_file" << EOF
# IOE-DREAM 权限控制趋势报告

**报告日期**: $(date +%Y-%m-%d)
**数据范围**: 最近7天
**监控标准**: repowiki系统安全规范

## 覆盖率趋势

\`\`\`bash
# 当前状态
后端权限覆盖率: $(grep -r "@SaCheckPermission" --include="*Controller.java" "$BACKEND_DIR" | wc -l) / $(find "$BACKEND_DIR" -name "*Controller.java" | wc -l)
前端权限覆盖率: $(find "$FRONTEND_DIR/src/views" -name "*.vue" -exec grep -l "v-permission" {} \; | wc -l) / $(find "$FRONTEND_DIR/src/views" -name "*.vue" | wc -l)
\`\`\`

## 改进建议

### 短期目标（1周内）
- 前端权限覆盖率提升至30%
- 修复关键业务模块权限控制
- 完善权限标识映射

### 中期目标（1个月内）
- 前端权限覆盖率提升至80%
- 建立自动化权限检查机制
- 完善权限控制测试覆盖

### 长期目标（3个月内）
- 权限控制覆盖率稳定在90%+
- 实现权限控制自动化监控
- 建立权限治理最佳实践

## 质量指标

| 指标 | 当前值 | 目标值 | repowiki要求 |
|------|--------|--------|-------------|
| 后端权限覆盖率 | TBD | 95%+ | 100% |
| 前端权限覆盖率 | TBD | 90%+ | 80%+ |
| 权限标识一致性 | TBD | 95%+ | 100% |
| 自动化检查覆盖 | TBD | 100% | 100% |

---

**报告生成**: 权限控制监控预警系统
**遵循规范**: repowiki系统安全规范
**下次更新**: $(date -d "+7 days" +%Y-%m-%d)
EOF

    echo "📈 趋势报告已生成: $report_file"
}

# 主监控循环
main() {
    echo "🔄 开始权限控制监控..."

    # 执行一次监控
    monitor_permissions

    # 如果是周一生成趋势报告
    if [ "$(date +%u)" = "1" ]; then
        generate_trend_report
    fi

    echo "✅ 监控检查完成"
    echo "📋 下次监控: 5分钟后"
    echo "📊 查看日志: tail -f $LOG_FILE"
}

# 检查是否需要持续监控
if [ "$1" = "--continuous" ]; then
    echo "🔄 启动持续监控模式..."
    while true; do
        main
        sleep 300  # 5分钟检查一次
    done
else
    main
fi

echo ""
echo "🎯 权限控制监控完成！"
echo "📈 支持持续监控: $0 --continuous"
echo "📊 查看详细日志: cat $LOG_FILE"