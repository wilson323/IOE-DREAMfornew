# ============================================================
# IOE-DREAM 企业级内存监控脚本
# 生产环境运行状况监控 - 持续采集和分析
#
# 功能：
# - 实时监控所有Java进程内存使用
# - 自动记录到CSV文件
# - 生成内存使用报告
# - 支持告警阈值配置
#
# 使用方法：
# .\scripts\memory-monitor-enterprise.ps1 -Mode continuous
# .\scripts\memory-monitor-enterprise.ps1 -Mode report
# .\scripts\memory-monitor-enterprise.ps1 -Mode analyze -DataFile logs\memory-data.csv
#
# @Author: IOE-DREAM架构团队
# @Version: v1.0.0
# @Date: 2025-12-15
# ============================================================

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("continuous", "snapshot", "report", "analyze")]
    [string]$Mode = "continuous",

    [Parameter(Mandatory=$false)]
    [int]$Interval = 30,  # 采集间隔（秒）

    [Parameter(Mandatory=$false)]
    [int]$Duration = 3600,  # 持续时间（秒），默认1小时

    [Parameter(Mandatory=$false)]
    [string]$DataFile = "logs\memory-data.csv",

    [Parameter(Mandatory=$false)]
    [int]$WarningThresholdMB = 1024,  # 单服务告警阈值

    [Parameter(Mandatory=$false)]
    [int]$CriticalThresholdMB = 2048,  # 单服务严重告警阈值

    [Parameter(Mandatory=$false)]
    [int]$TotalWarningThresholdGB = 12,  # 总内存告警阈值

    [Parameter(Mandatory=$false)]
    [switch]$EnableAlert = $false
)

# 服务名称映射
$ServiceMapping = @{
    "ioedream-gateway" = "Gateway"
    "ioedream-common" = "Common"
    "ioedream-access" = "Access"
    "ioedream-attendance" = "Attendance"
    "ioedream-consume" = "Consume"
    "ioedream-oa" = "OA"
    "ioedream-visitor" = "Visitor"
    "ioedream-video" = "Video"
    "ioedream-device" = "Device"
}

# 确保日志目录存在
$logDir = Split-Path -Path $DataFile -Parent
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force | Out-Null
}

function Get-JavaProcesses {
    $processes = @()
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue

    foreach ($proc in $javaProcesses) {
        try {
            $commandLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($proc.Id)").CommandLine

            $serviceName = "Unknown"
            foreach ($key in $ServiceMapping.Keys) {
                if ($commandLine -match $key) {
                    $serviceName = $ServiceMapping[$key]
                    break
                }
            }

            $memoryMB = [math]::Round($proc.WorkingSet64 / 1MB, 2)
            $cpuPercent = [math]::Round($proc.CPU, 2)
            $threadCount = $proc.Threads.Count
            $handleCount = $proc.HandleCount

            $processes += [PSCustomObject]@{
                PID = $proc.Id
                ServiceName = $serviceName
                MemoryMB = $memoryMB
                CPUPercent = $cpuPercent
                ThreadCount = $threadCount
                HandleCount = $handleCount
                StartTime = $proc.StartTime
                Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            }
        } catch {
            # 忽略无法访问的进程
        }
    }

    return $processes | Sort-Object ServiceName
}

function Write-DataToCSV {
    param(
        [array]$Data,
        [string]$FilePath
    )

    $fileExists = Test-Path $FilePath

    foreach ($item in $Data) {
        $line = "$($item.Timestamp),$($item.PID),$($item.ServiceName),$($item.MemoryMB),$($item.CPUPercent),$($item.ThreadCount),$($item.HandleCount)"

        if (-not $fileExists) {
            "Timestamp,PID,ServiceName,MemoryMB,CPUPercent,ThreadCount,HandleCount" | Out-File -FilePath $FilePath -Encoding UTF8
            $fileExists = $true
        }

        $line | Out-File -FilePath $FilePath -Append -Encoding UTF8
    }
}

function Show-MemorySnapshot {
    param(
        [array]$Data
    )

    Clear-Host
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host " IOE-DREAM 内存监控报告 - $timestamp" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host ""

    $totalMemory = 0
    $totalThreads = 0

    Write-Host ("{0,-15} {1,-8} {2,-12} {3,-10} {4,-10} {5,-10}" -f "服务名", "PID", "内存(MB)", "CPU(%)", "线程数", "状态") -ForegroundColor Yellow
    Write-Host ("-" * 70)

    foreach ($item in $Data) {
        $totalMemory += $item.MemoryMB
        $totalThreads += $item.ThreadCount

        $status = "正常"
        $color = "Green"

        if ($item.MemoryMB -ge $CriticalThresholdMB) {
            $status = "严重"
            $color = "Red"
        } elseif ($item.MemoryMB -ge $WarningThresholdMB) {
            $status = "警告"
            $color = "Yellow"
        }

        Write-Host ("{0,-15} {1,-8} {2,-12} {3,-10} {4,-10} " -f $item.ServiceName, $item.PID, $item.MemoryMB, $item.CPUPercent, $item.ThreadCount) -NoNewline
        Write-Host $status -ForegroundColor $color
    }

    Write-Host ("-" * 70)

    $totalMemoryGB = [math]::Round($totalMemory / 1024, 2)
    $totalColor = "Green"
    $totalStatus = "正常"

    if ($totalMemoryGB -ge $TotalWarningThresholdGB) {
        $totalColor = "Red"
        $totalStatus = "超限"
    }

    Write-Host ""
    Write-Host "总计内存使用: " -NoNewline
    Write-Host "$totalMemoryGB GB " -ForegroundColor $totalColor -NoNewline
    Write-Host "($totalStatus) | 总线程数: $totalThreads" -ForegroundColor $totalColor
    Write-Host ""

    # 优化效果对比
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host " 优化效果对比" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "优化前基准: 15-20 GB"
    Write-Host "优化目标:   8-12 GB"
    Write-Host "当前使用:   $totalMemoryGB GB"

    $savingsPercent = [math]::Round((1 - $totalMemoryGB / 17.5) * 100, 1)
    if ($savingsPercent -gt 0) {
        Write-Host "节省比例:   $savingsPercent%" -ForegroundColor Green
    } else {
        Write-Host "节省比例:   $savingsPercent% (需要进一步优化)" -ForegroundColor Yellow
    }

    Write-Host ""
}

function Send-Alert {
    param(
        [string]$Message,
        [string]$Level
    )

    if ($EnableAlert) {
        $alertTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        $alertFile = "logs\memory-alerts.log"
        "[$alertTime] [$Level] $Message" | Out-File -FilePath $alertFile -Append -Encoding UTF8

        Write-Host "[$Level] $Message" -ForegroundColor $(if ($Level -eq "CRITICAL") { "Red" } else { "Yellow" })
    }
}

function Analyze-MemoryData {
    param(
        [string]$FilePath
    )

    if (-not (Test-Path $FilePath)) {
        Write-Host "数据文件不存在: $FilePath" -ForegroundColor Red
        return
    }

    $data = Import-Csv -Path $FilePath

    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host " 内存数据分析报告" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host ""

    $services = $data | Group-Object ServiceName

    Write-Host ("{0,-15} {1,-12} {2,-12} {3,-12} {4,-12}" -f "服务名", "最小(MB)", "最大(MB)", "平均(MB)", "建议配置") -ForegroundColor Yellow
    Write-Host ("-" * 70)

    foreach ($service in $services) {
        $memValues = $service.Group | ForEach-Object { [double]$_.MemoryMB }
        $min = [math]::Round(($memValues | Measure-Object -Minimum).Minimum, 0)
        $max = [math]::Round(($memValues | Measure-Object -Maximum).Maximum, 0)
        $avg = [math]::Round(($memValues | Measure-Object -Average).Average, 0)

        # 建议配置：平均值的1.5倍，向上取整到256MB的倍数
        $recommended = [math]::Ceiling($avg * 1.5 / 256) * 256

        Write-Host ("{0,-15} {1,-12} {2,-12} {3,-12} {4,-12}" -f $service.Name, $min, $max, $avg, "$recommended MB")
    }

    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host " 配置建议" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan

    foreach ($service in $services) {
        $memValues = $service.Group | ForEach-Object { [double]$_.MemoryMB }
        $avg = [math]::Round(($memValues | Measure-Object -Average).Average, 0)
        $recommended = [math]::Ceiling($avg * 1.5 / 256) * 256

        Write-Host ""
        Write-Host "# $($service.Name) 服务配置建议："
        Write-Host "spring:"
        Write-Host "  application:"
        Write-Host "    java-opts: >-"
        Write-Host "      -Xms$([math]::Floor($recommended / 2))m"
        Write-Host "      -Xmx${recommended}m"
    }
}

function Generate-Report {
    param(
        [string]$FilePath
    )

    if (-not (Test-Path $FilePath)) {
        Write-Host "数据文件不存在: $FilePath" -ForegroundColor Red
        return
    }

    $data = Import-Csv -Path $FilePath
    $reportFile = "logs\memory-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"

    $report = @"
# 内存监控报告

**生成时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**数据文件**: $FilePath
**数据条数**: $($data.Count)

## 概览

| 指标 | 值 |
|------|-----|
| 监控时长 | $(($data | Select-Object -Last 1).Timestamp) - $(($data | Select-Object -First 1).Timestamp) |
| 采样次数 | $($data.Count / ($data | Group-Object ServiceName).Count) |
| 服务数量 | $($data | Group-Object ServiceName | Measure-Object).Count |

## 各服务内存使用统计

| 服务名 | 最小(MB) | 最大(MB) | 平均(MB) | 标准差 |
|--------|----------|----------|----------|--------|
"@

    $services = $data | Group-Object ServiceName
    foreach ($service in $services) {
        $memValues = $service.Group | ForEach-Object { [double]$_.MemoryMB }
        $min = [math]::Round(($memValues | Measure-Object -Minimum).Minimum, 0)
        $max = [math]::Round(($memValues | Measure-Object -Maximum).Maximum, 0)
        $avg = [math]::Round(($memValues | Measure-Object -Average).Average, 0)
        $stdDev = [math]::Round(($memValues | Measure-Object -StandardDeviation).StandardDeviation, 0)

        $report += "| $($service.Name) | $min | $max | $avg | $stdDev |`n"
    }

    $report += @"

## 配置建议

基于监控数据，建议各服务JVM配置如下：

"@

    foreach ($service in $services) {
        $memValues = $service.Group | ForEach-Object { [double]$_.MemoryMB }
        $avg = [math]::Round(($memValues | Measure-Object -Average).Average, 0)
        $recommended = [math]::Ceiling($avg * 1.5 / 256) * 256

        $report += @"

### $($service.Name)

```yaml
spring:
  application:
    java-opts: >-
      -Xms$([math]::Floor($recommended / 2))m
      -Xmx${recommended}m
      -XX:MaxMetaspaceSize=256m
      -XX:+UseG1GC
      -XX:+UseStringDeduplication
```

"@
    }

    $report | Out-File -FilePath $reportFile -Encoding UTF8
    Write-Host "报告已生成: $reportFile" -ForegroundColor Green
}

# 主程序
switch ($Mode) {
    "continuous" {
        Write-Host "开始持续监控模式..." -ForegroundColor Cyan
        Write-Host "采集间隔: ${Interval}秒, 持续时间: ${Duration}秒" -ForegroundColor Cyan
        Write-Host "数据保存: $DataFile" -ForegroundColor Cyan
        Write-Host "按 Ctrl+C 停止监控" -ForegroundColor Yellow
        Write-Host ""

        $endTime = (Get-Date).AddSeconds($Duration)

        while ((Get-Date) -lt $endTime) {
            $data = Get-JavaProcesses

            if ($data.Count -gt 0) {
                Show-MemorySnapshot -Data $data
                Write-DataToCSV -Data $data -FilePath $DataFile

                # 检查告警
                foreach ($item in $data) {
                    if ($item.MemoryMB -ge $CriticalThresholdMB) {
                        Send-Alert -Message "$($item.ServiceName) 内存使用严重超限: $($item.MemoryMB) MB" -Level "CRITICAL"
                    } elseif ($item.MemoryMB -ge $WarningThresholdMB) {
                        Send-Alert -Message "$($item.ServiceName) 内存使用警告: $($item.MemoryMB) MB" -Level "WARNING"
                    }
                }

                $totalMemory = ($data | Measure-Object -Property MemoryMB -Sum).Sum / 1024
                if ($totalMemory -ge $TotalWarningThresholdGB) {
                    Send-Alert -Message "总内存使用超限: $([math]::Round($totalMemory, 2)) GB" -Level "CRITICAL"
                }
            } else {
                Write-Host "未检测到Java进程..." -ForegroundColor Yellow
            }

            Start-Sleep -Seconds $Interval
        }

        Write-Host "监控结束，正在生成报告..." -ForegroundColor Cyan
        Generate-Report -FilePath $DataFile
    }

    "snapshot" {
        $data = Get-JavaProcesses
        if ($data.Count -gt 0) {
            Show-MemorySnapshot -Data $data
        } else {
            Write-Host "未检测到Java进程" -ForegroundColor Yellow
        }
    }

    "report" {
        Generate-Report -FilePath $DataFile
    }

    "analyze" {
        Analyze-MemoryData -FilePath $DataFile
    }
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host " 监控完成" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
