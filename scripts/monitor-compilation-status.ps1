# 微服务编译状态监控脚本
# 目的: 持续监控所有微服务的编译状态，及时发现和解决问题

param(
    [switch]$Continuous,      # 持续监控模式
    [int]$Interval = 60,       # 监控间隔（秒）
    [switch]$Detailed,        # 详细模式
    [switch]$SaveReport,      # 保存报告到文件
    [string]$LogFile = "compilation-status.log"  # 日志文件
)

$ErrorActionPreference = "Stop"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "微服务编译状态监控" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

# 微服务列表
$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-database-service",
    "ioedream-consume-service",
    "ioedream-visitor-service",
    "ioedream-biometric-service"
)

# 构建结果数据结构
$buildResults = @{}

# 函数：获取服务编译状态
function Get-CompilationStatus {
    param(
        [string]$ServiceName
    )

    $servicePath = "D:/IOE-DREAM/microservices/$ServiceName"

    if (-not (Test-Path $servicePath)) {
        return @{
            Status = "NOT_FOUND"
            Errors = 0
            Warnings = 0
            Time = 0
            Message = "服务目录不存在"
        }
    }

    try {
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()

        # 执行编译
        $result = mvn -f "$servicePath/pom.xml" clean compile -q 2>&1
        $exitCode = $LASTEXITCODE

        $stopwatch.Stop()

        # 分析输出
        $errorCount = 0
        $warningCount = 0
        $hasCompilationError = $false

        foreach ($line in $result -split "`n") {
            if ($line -match "ERROR") {
                $errorCount++
                $hasCompilationError = $true
            }
            elseif ($line -match "WARN") {
                $warningCount++
            }
        }

        # 检查构建是否成功
        if ($exitCode -eq 0 -and -not $hasCompilationError -and $result -match "BUILD SUCCESS") {
            $status = "SUCCESS"
            $message = "编译成功"
        }
        elseif ($hasCompilationError -or $errorCount -gt 0) {
            $status = "FAILED"
            $message = "编译失败 - $errorCount 个错误"
        }
        else {
            $status = "FAILED"
            $message = "构建失败 - 退出码: $exitCode"
        }

        return @{
            Status = $status
            Errors = $errorCount
            Warnings = $warningCount
            Time = $stopwatch.ElapsedMilliseconds
            Message = $message
            Output = $result
        }
    }
    catch {
        return @{
            Status = "ERROR"
            Errors = 1
            Warnings = 0
            Time = 0
            Message = "编译过程异常: $($_.Exception.Message)"
        }
    }
}

# 函数：格式化时间
function Format-Time {
    param([int]$Milliseconds)

    if ($Milliseconds -lt 1000) {
        return "${Milliseconds}ms"
    }
    elseif ($Milliseconds -lt 60000) {
        return "$([math]::Round($Milliseconds / 1000, 1))s"
    }
    else {
        return "$([math]::Round($Milliseconds / 60000, 1))m"
    }
}

# 函数：显示状态图标
function Get-StatusIcon {
    param([string]$Status)

    switch ($Status) {
        "SUCCESS" { return "✅" }
        "FAILED" { return "❌" }
        "ERROR" { return "💥" }
        "NOT_FOUND" { return "🔍" }
        default { return "❓" }
    }
}

# 函数：生成报告
function Generate-Report {
    param([hashtable]$Results)

    $report = @()

    foreach ($serviceName in $services) {
        $result = $Results[$serviceName]
        if ($result) {
            $icon = Get-StatusIcon -Status $result.Status
            $timeStr = Format-Time -Milliseconds $result.Time

            $report += [PSCustomObject]@{
                Service = $serviceName
                Status = $result.Status
                Icon = $icon
                Errors = $result.Errors
                Warnings = $result.Warnings
                Time = $timeStr
                Message = $result.Message
            }
        }
    }

    return $report
}

# 函数：显示报告
function Show-Report {
    param([array]$Report)

    Write-Host "`n编译状态报告 $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Yellow
    Write-Host "=" * 80 -ForegroundColor Gray

    $totalServices = $Report.Count
    $successCount = ($Report | Where-Object { $_.Status -eq "SUCCESS" }).Count
    $failedCount = $totalServices - $successCount
    $totalErrors = ($Report | Measure-Object -Property Errors -Sum).Sum
    $totalWarnings = ($Report | Measure-Object -Property Warnings -Sum).Sum

    Write-Host "服务总数: $totalServices | 成功: $successCount | 失败: $failedCount | 错误: $totalErrors | 警告: $totalWarnings" -ForegroundColor White

    Write-Host "`n详细状态:" -ForegroundColor Cyan

    foreach ($item in $Report) {
        $color = switch ($item.Status) {
            "SUCCESS" { "Green" }
            "FAILED" { "Red" }
            "ERROR" { "Red" }
            "NOT_FOUND" { "Yellow" }
            default { "White" }
        }

        Write-Host "  $($item.Icon) $($item.Service.PadRight(25)) $($item.Status.PadRight(10)) $($item.Time.PadRight(8)) Errors:$($item.Errors) Warnings:$($item.Warnings)" -ForegroundColor $color
    }

    if ($Detailed -and $totalErrors -gt 0) {
        Write-Host "`n错误详情:" -ForegroundColor Red
        foreach ($serviceName in $services) {
            $result = $buildResults[$serviceName]
            if ($result.Errors -gt 0) {
                Write-Host "`n$serviceName 错误信息:" -ForegroundColor Red
                $errorLines = $result.Output -split "`n" | Where-Object { $_ -match "ERROR" } | Select-Object -First 5
                foreach ($line in $errorLines) {
                    Write-Host "  $line" -ForegroundColor DarkRed
                }
                if ($result.Errors -gt 5) {
                    Write-Host "  ... 还有 $($result.Errors - 5) 个错误" -ForegroundColor DarkRed
                }
            }
        }
    }

    Write-Host "`n" + ("=" * 80) -ForegroundColor Gray
}

# 函数：保存报告到文件
function Save-ReportToFile {
    param([array]$Report, [string]$FileName)

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

    $content = @"
微服务编译状态报告 - $timestamp
========================================

摘要:
服务总数: $($Report.Count)
成功数: $($Report | Where-Object { $_.Status -eq "SUCCESS" }).Count
失败数: $($Report | Where-Object { $_.Status -ne "SUCCESS" }).Count
总错误数: $($Report | Measure-Object -Property Errors -Sum).Sum
总警告数: $($Report | Measure-Object -Property Warnings -Sum).Sum

详细状态:
"@

    foreach ($item in $Report) {
        $content += "`n$($item.Icon) $($item.Service) - $($item.Status) - $($item.Time) - Errors:$($item.Errors) Warnings:$($item.Warnings)"
        $content += "`n    Message: $($item.Message)"
    }

    $content | Out-File -FilePath $FileName -Encoding UTF8
    Write-Host "报告已保存到: $FileName" -ForegroundColor Green
}

# 主监控循环
do {
    $buildResults.Clear()

    Write-Host "开始编译检查... $(Get-Date -Format 'HH:mm:ss')" -ForegroundColor White

    # 并行编译所有服务
    $jobs = @()
    foreach ($serviceName in $services) {
        $job = Start-Job -ScriptBlock {
            param($ServiceName, $ServicePath)

            # 简化版本，直接在Job中实现编译逻辑
            try {
                $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
                $result = mvn -f "$ServicePath/pom.xml" clean compile -q 2>&1
                $exitCode = $LASTEXITCODE
                $stopwatch.Stop()

                $errorCount = 0
                $warningCount = 0
                $hasCompilationError = $false

                foreach ($line in $result -split "`n") {
                    if ($line -match "ERROR") {
                        $errorCount++
                        $hasCompilationError = $true
                    }
                    elseif ($line -match "WARN") {
                        $warningCount++
                    }
                }

                if ($exitCode -eq 0 -and -not $hasCompilationError -and $result -match "BUILD SUCCESS") {
                    $status = "SUCCESS"
                    $message = "编译成功"
                }
                elseif ($hasCompilationError -or $errorCount -gt 0) {
                    $status = "FAILED"
                    $message = "编译失败 - $errorCount 个错误"
                }
                else {
                    $status = "FAILED"
                    $message = "构建失败 - 退出码: $exitCode"
                }

                return @{
                    Status = $status
                    Errors = $errorCount
                    Warnings = $warningCount
                    Time = $stopwatch.ElapsedMilliseconds
                    Message = $message
                    ServiceName = $ServiceName
                }
            }
            catch {
                return @{
                    Status = "ERROR"
                    Errors = 1
                    Warnings = 0
                    Time = 0
                    Message = "编译过程异常: $($_.Exception.Message)"
                    ServiceName = $ServiceName
                }
            }
        } -ArgumentList $serviceName, "D:/IOE-DREAM/microservices/$serviceName"

        $jobs += $job
    }

    # 等待所有Job完成
    $null = $jobs | Wait-Job -Timeout 300

    # 收集结果
    foreach ($job in $jobs) {
        if ($job.State -eq "Completed") {
            $result = Receive-Job $job
            if ($result -and $result.ServiceName) {
                $buildResults[$result.ServiceName] = $result
            }
        }
        else {
            Write-Host "Job超时或失败: $($job.Name)" -ForegroundColor Yellow
        }
        Remove-Job $job -Force
    }

    # 生成和显示报告
    $report = Generate-Report -Results $buildResults
    Show-Report -Report $report

    if ($SaveReport) {
        $reportFile = "D:/IOE-DREAM/logs/$(Get-Date -Format 'yyyyMMdd-HHmmss')-compilation-report.txt"
        $reportDir = Split-Path $reportFile -Parent
        if (-not (Test-Path $reportDir)) {
            New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
        }
        Save-ReportToFile -Report $report -FileName $reportFile
    }

    # 检查是否需要继续监控
    if ($Continuous) {
        Write-Host "等待 $Interval 秒后进行下一次检查..." -ForegroundColor Gray
        Start-Sleep -Seconds $Interval
    }

} while ($Continuous)

Write-Host "编译状态监控完成" -ForegroundColor Green
exit 0