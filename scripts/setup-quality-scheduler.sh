#!/bin/bash

# IOE-DREAM 质量监控调度器安装脚本
# 功能：设置自动化的质量数据收集和监控任务

echo "⚙️ IOE-DREAM 质量监控调度器安装"
echo "============================="
echo "安装时间: $(date)"

# 检查操作系统
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="Linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macOS"
elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
    OS="Windows"
else
    OS="Unknown"
fi

echo "🖥️ 检测到操作系统: $OS"

# 创建日志目录
mkdir -p logs/quality-monitor

# 函数：安装Linux/macOS Cron任务
install_unix_cron() {
    echo ""
    echo "📅 设置 Unix/Linux Cron 任务..."

    # 获取当前脚本路径
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    COLLECTOR_SCRIPT="$SCRIPT_DIR/daily-quality-collector.sh"
    TREND_ANALYSIS_SCRIPT="$SCRIPT_DIR/quality-trend-analysis.sh"

    # 创建临时Cron配置文件
    TEMP_CRON="/tmp/ioe-dream-cron.$$"

    # 添加Cron任务
    {
        echo "# IOE-DREAM 质量监控自动化任务"
        echo "# 每日上午9:00收集质量数据"
        echo "0 9 * * * cd $SCRIPT_DIR && ./daily-quality-collector.sh >> logs/quality-monitor/daily-collector.log 2>&1"
        echo ""
        echo "# 每周一下午4:00生成趋势分析报告"
        echo "0 16 * * 1 cd $SCRIPT_DIR && ./quality-trend-analysis.sh >> logs/quality-monitor/trend-analysis.log 2>&1"
        echo ""
        echo "# 每小时检查关键质量指标（如果有违规则立即通知）"
        echo "0 * * * * cd $SCRIPT_DIR && ./precise-quality-check.sh | grep -q '违规数: [1-9]' && echo '⚠️ 质量检查发现问题' | mail -s 'IOE-DREAM 质量告警' your-email@example.com"
    } > "$TEMP_CRON"

    echo "📋 建议的Cron任务配置："
    cat "$TEMP_CRON"
    echo ""
    echo "💡 安装方法："
    echo "   crontab $TEMP_CRON"
    echo ""
    echo "🔍 查看现有Cron任务："
    echo "   crontab -l"
    echo ""
    echo "❌ 删除所有Cron任务（如需要）："
    echo "   crontab -r"

    # 清理临时文件
    rm -f "$TEMP_CRON"
}

# 函数：创建Windows任务调度脚本
install_windows_scheduler() {
    echo ""
    echo "📅 创建 Windows 任务调度脚本..."

    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

    # 创建PowerShell调度脚本
    cat > "setup-quality-tasks.ps1" << 'EOF'
# IOE-DREAM 质量监控 Windows 任务设置脚本
# 使用方法：在管理员PowerShell中运行 .\setup-quality-tasks.ps1

param(
    [string]$ScriptDir = $(Split-Path -Parent $MyInvocation.MyCommand.Path)
)

Write-Host "🔧 IOE-DREAM 质量监控任务安装 (Windows)"
Write-Host "====================================="
Write-Host "脚本目录: $ScriptDir"

# 创建日志目录
$logDir = Join-Path $ScriptDir "logs\quality-monitor"
if (!(Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force
}

# 每日质量数据收集任务
$collectorAction = New-ScheduledTaskAction -Execute "powershell.exe" -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$ScriptDir\daily-quality-collector.sh`"" -WorkingDirectory $ScriptDir
$collectorTrigger = New-ScheduledTaskTrigger -Daily -At 9AM
$collectorSettings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable

Register-ScheduledTask -TaskName "IOE-DREAM-DailyQualityCollector" -Action $collectorAction -Trigger $collectorTrigger -Settings $collectorSettings -Description "IOE-DREAM 每日质量数据收集" -Force

Write-Host "✅ 已创建每日质量收集任务 (每天9:00执行)"

# 周度趋势分析任务
$trendAction = New-ScheduledTaskAction -Execute "powershell.exe" -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$ScriptDir\quality-trend-analysis.sh`"" -WorkingDirectory $ScriptDir
$trendTrigger = New-ScheduledTaskTrigger -Weekly -DaysOfWeek Monday -At 4PM
$trendSettings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable

Register-ScheduledTask -TaskName "IOE-DREAM-WeeklyTrendAnalysis" -Action $trendAction -Trigger $trendTrigger -Settings $trendSettings -Description "IOE-DREAM 周度质量趋势分析" -Force

Write-Host "✅ 已创建周度趋势分析任务 (每周一16:00执行)"

# 实时质量监控任务 (每小时检查)
$monitorAction = New-ScheduledTaskAction -Execute "powershell.exe" -Argument "-NoProfile -ExecutionPolicy Bypass -Command `"cd '$ScriptDir'; bash .\precise-quality-check.sh | Select-String '违规数: [1-9]' | ForEach-Object { Write-Host '⚠️ IOE-DREAM 质量检查发现问题'; exit 1 }`"" -WorkingDirectory $ScriptDir
$monitorTrigger = New-ScheduledTaskTrigger -Hourly
$monitorSettings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable -ExecutionTimeLimit (New-TimeSpan -Minutes 10)

Register-ScheduledTask -TaskName "IOE-DREAM-HourlyQualityMonitor" -Action $monitorAction -Trigger $monitorTrigger -Settings $monitorSettings -Description "IOE-DREAM 每小时质量监控" -Force

Write-Host "✅ 已创建每小时质量监控任务"

Write-Host ""
Write-Host "🎉 所有质量监控任务安装完成！"
Write-Host ""
Write-Host "📋 查看已创建的任务："
Write-Host "   Get-ScheduledTask | Where-Object { \$_.TaskName -like 'IOE-DREAM*' }"
Write-Host ""
Write-Host "🗑️ 删除所有任务（如需要）："
Write-Host "   Get-ScheduledTask | Where-Object { \$_.TaskName -like 'IOE-DREAM*' } | Unregister-ScheduledTask -Confirm:\$false"
Write-Host ""
Write-Host "📊 查看任务执行日志："
Write-Host "   Get-Content '$logDir\*.log' -Tail 20"
EOF

    echo "✅ 已创建 Windows PowerShell 任务安装脚本: setup-quality-tasks.ps1"
    echo ""
    echo "💡 安装方法："
    echo "   1. 以管理员身份打开 PowerShell"
    echo "   2. 导航到项目目录: cd $(pwd)"
    echo "   3. 执行安装脚本: .\setup-quality-tasks.ps1"
}

# 函数：创建手动执行脚本
create_manual_scripts() {
    echo ""
    echo "📝 创建手动执行脚本..."

    # 创建一键质量检查脚本
    cat > "quick-quality-check.sh" << 'EOF'
#!/bin/bash
# IOE-DREAM 快速质量检查
echo "🚀 IOE-DREAM 快速质量检查"
echo "======================"
bash scripts/precise-quality-check.sh
echo ""
echo "📊 运行持续监控:"
bash scripts/continuous-monitoring.sh
EOF

    chmod +x "quick-quality-check.sh"

    # 创建质量报告生成脚本
    cat > "generate-quality-report.sh" << 'EOF'
#!/bin/bash
# IOE-DREAM 质量报告生成
echo "📄 IOE-DREAM 质量报告生成"
echo "======================="
bash scripts/quality-trend-analysis.sh
echo ""
echo "📋 查看所有报告:"
ls -la monitoring-reports/*.txt | tail -5
EOF

    chmod +x "generate-quality-report.sh"

    echo "✅ 已创建手动执行脚本:"
    echo "   - quick-quality-check.sh (快速质量检查)"
    echo "   - generate-quality-report.sh (生成质量报告)"
}

# 主执行流程
main() {
    case $OS in
        "Linux"|"macOS")
            install_unix_cron
            ;;
        "Windows")
            install_windows_scheduler
            ;;
        *)
            echo "⚠️ 不支持的操作系统: $OS"
            echo "📝 建议使用手动脚本"
            ;;
    esac

    create_manual_scripts

    echo ""
    echo "============================="
    echo "🎉 质量监控调度器安装完成！"
    echo ""
    echo "📋 下一步操作:"
    case $OS in
        "Linux"|"macOS")
            echo "1. 执行: crontab /tmp/ioe-dream-cron.$$"
            echo "2. 验证: crontab -l"
            ;;
        "Windows")
            echo "1. 以管理员身份运行 PowerShell"
            echo "2. 执行: .\setup-quality-tasks.ps1"
            ;;
    esac
    echo ""
    echo "📊 手动执行:"
    echo "   - 快速检查: ./quick-quality-check.sh"
    echo "   - 生成报告: ./generate-quality-report.sh"
    echo ""
    echo "📁 日志位置: logs/quality-monitor/"
}

# 执行主函数
main