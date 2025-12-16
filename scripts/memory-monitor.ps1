# ============================================================
# IOE-DREAM 内存监控脚本
# 
# 功能：监控Java进程内存使用情况，验证优化效果
# 使用方法：.\scripts\memory-monitor.ps1
# ============================================================

param(
    [switch]$Continuous,
    [int]$Interval = 5
)

$ErrorActionPreference = "Stop"

# 获取Java进程内存使用情况
function Get-JavaProcessMemory {
    [CmdletBinding()]
    param()
    
    $processes = Get-Process -Name java -ErrorAction SilentlyContinue
    $results = @()
    $totalMemoryMB = 0
    
    if (-not $processes) {
        Write-Host "[WARN] No Java processes found" -ForegroundColor Yellow
        return $null
    }
    
    foreach ($proc in $processes) {
        $memoryMB = [math]::Round($proc.WorkingSet64 / 1MB, 2)
        $virtualMB = [math]::Round($proc.VirtualMemorySize64 / 1MB, 2)
        $privateMB = [math]::Round($proc.PrivateMemorySize64 / 1MB, 2)
        
        # 尝试获取进程命令行来识别服务
        $serviceName = "unknown"
        try {
            $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($proc.Id)" -ErrorAction SilentlyContinue).CommandLine
            if ($cmdLine -match "ioedream-(\w+)-service") {
                $serviceName = $Matches[1]
            } elseif ($cmdLine -match "spring\.application\.name=ioedream-(\w+)-service") {
                $serviceName = $Matches[1]
            }
        } catch {
            # 忽略错误
        }
        
        $results += [PSCustomObject]@{
            PID = $proc.Id
            Service = $serviceName
            WorkingSetMB = $memoryMB
            VirtualMB = $virtualMB
            PrivateMB = $privateMB
        }
        
        $totalMemoryMB += $memoryMB
    }
    
    return @{
        Processes = $results
        TotalMemoryMB = $totalMemoryMB
        ProcessCount = $processes.Count
    }
}

# 格式化输出
function Show-MemoryReport {
    param($report)
    
    if (-not $report) {
        return
    }
    
    Write-Host ""
    Write-Host "=" * 70 -ForegroundColor Cyan
    Write-Host "IOE-DREAM Memory Usage Report - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
    Write-Host "=" * 70 -ForegroundColor Cyan
    Write-Host ""
    
    # 表头
    Write-Host ("{0,-8} {1,-15} {2,12} {3,12} {4,12}" -f "PID", "Service", "WorkingSet", "Virtual", "Private") -ForegroundColor White
    Write-Host ("-" * 70) -ForegroundColor Gray
    
    # 按内存使用量排序
    $sorted = $report.Processes | Sort-Object -Property WorkingSetMB -Descending
    
    foreach ($proc in $sorted) {
        $color = "Green"
        if ($proc.WorkingSetMB -gt 1024) {
            $color = "Yellow"
        }
        if ($proc.WorkingSetMB -gt 2048) {
            $color = "Red"
        }
        
        Write-Host ("{0,-8} {1,-15} {2,10} MB {3,10} MB {4,10} MB" -f `
            $proc.PID, $proc.Service, $proc.WorkingSetMB, $proc.VirtualMB, $proc.PrivateMB) -ForegroundColor $color
    }
    
    Write-Host ("-" * 70) -ForegroundColor Gray
    
    # 汇总
    $totalGB = [math]::Round($report.TotalMemoryMB / 1024, 2)
    $summaryColor = "Green"
    if ($totalGB -gt 10) {
        $summaryColor = "Yellow"
    }
    if ($totalGB -gt 15) {
        $summaryColor = "Red"
    }
    
    Write-Host ""
    Write-Host ("Total: {0} processes, {1} MB ({2} GB)" -f `
        $report.ProcessCount, $report.TotalMemoryMB, $totalGB) -ForegroundColor $summaryColor
    
    # 优化效果评估
    Write-Host ""
    if ($totalGB -le 10) {
        Write-Host "[OK] Memory usage is within target range (<=10GB)" -ForegroundColor Green
        $savedPercent = [math]::Round((1 - $totalGB / 17.5) * 100, 1)
        Write-Host "[OK] Estimated savings: ~${savedPercent}% compared to baseline (17.5GB)" -ForegroundColor Green
    } elseif ($totalGB -le 12) {
        Write-Host "[OK] Memory usage is acceptable (10-12GB)" -ForegroundColor Yellow
        $savedPercent = [math]::Round((1 - $totalGB / 17.5) * 100, 1)
        Write-Host "[OK] Estimated savings: ~${savedPercent}% compared to baseline (17.5GB)" -ForegroundColor Yellow
    } else {
        Write-Host "[WARN] Memory usage exceeds target (>12GB)" -ForegroundColor Red
        Write-Host "[WARN] Consider checking JVM configurations" -ForegroundColor Red
    }
    
    Write-Host ""
}

# 主程序
Write-Host ""
Write-Host "IOE-DREAM Memory Monitor" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor Cyan

if ($Continuous) {
    Write-Host "Running in continuous mode (interval: ${Interval}s, Ctrl+C to stop)" -ForegroundColor Yellow
    while ($true) {
        Clear-Host
        $report = Get-JavaProcessMemory
        Show-MemoryReport $report
        Start-Sleep -Seconds $Interval
    }
} else {
    $report = Get-JavaProcessMemory
    Show-MemoryReport $report
}
