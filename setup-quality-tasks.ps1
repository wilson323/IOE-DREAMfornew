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
