# =====================================================
# IOE-DREAM 内存泄漏检测工具
# 用于检测和预防Java应用程序内存泄漏
#
# 使用方法:
# .\scripts\memory-leak-detection.ps1
# .\scripts\memory-leak-detection.ps1 -ServiceName ioedream-access-service
# .\scripts\memory-leak-detection.ps1 -Continuous -Interval 300
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$ServiceName = "all",

    [Parameter(Mandatory=$false)]
    [switch]$Continuous,

    [Parameter(Mandatory=$false)]
    [int]$Interval = 300,

    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport,

    [Parameter(Mandatory=$false)]
    [string]$OutputPath = "./memory-reports"
)

# 工具函数
function Write-MemoryLog {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $color = switch ($Level) {
        "INFO" { "Green" }
        "WARN" { "Yellow" }
        "ERROR" { "Red" }
        "CRITICAL" { "Magenta" }
        "SUCCESS" { "Cyan" }
        default { "White" }
    }
    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $color
}

function Get-ServiceMemoryInfo {
    param([string]$Service)

    try {
        # 获取服务的JVM进程信息
        $process = Get-JavaProcess -ServiceName $Service
        if (-not $process) {
            Write-MemoryLog "未找到服务 $Service 的Java进程" "WARN"
            return $null
        }

        # 使用jstat获取内存信息
        $jstatOutput = & jstat -gc $process.Id 2>$null
        $memoryInfo = Parse-JstatOutput $jstatOutput

        # 使用jmap获取堆内存信息
        $heapInfo = Get-HeapMemoryInfo $process.Id

        # 获取非堆内存信息
        $nonHeapInfo = Get-NonHeapMemoryInfo $process.Id

        return @{
            ServiceName = $Service
            ProcessId = $process.Id
            Timestamp = Get-Date
            HeapMemory = $heapInfo
            NonHeapMemory = $nonHeapInfo
            GCInfo = $memoryInfo
        }
    }
    catch {
        Write-MemoryLog "获取服务 $Service 内存信息失败: $($_.Exception.Message)" "ERROR"
        return $null
    }
}

function Get-JavaProcess {
    param([string]$ServiceName)

    $processes = Get-Process -Name "java" -ErrorAction SilentlyContinue
    foreach ($process in $processes) {
        try {
            $cmdLine = (Get-WmiObject Win32_Process -Filter "ProcessId=$($process.Id)").CommandLine
            if ($cmdLine -match $ServiceName) {
                return $process
            }
        }
        catch {
            continue
        }
    }
    return $null
}

function Parse-JstatOutput {
    param([string]$JstatOutput)

    if (-not $JstatOutput) {
        return $null
    }

    $lines = $JstatOutput -split "`n"
    $header = $lines[0] -split "\s+" | Where-Object { $_ -ne "" }
    $data = $lines[1] -split "\s+" | Where-Object { $_ -ne "" }

    $memoryInfo = @{}
    for ($i = 0; $i -lt $header.Count; $i++) {
        if ($i -lt $data.Count) {
            $memoryInfo[$header[$i]] = [double]$data[$i]
        }
    }

    return $memoryInfo
}

function Get-HeapMemoryInfo {
    param([int]$ProcessId)

    try {
        $jmapOutput = & jmap -heap $ProcessId 2>$null
        return Parse-HeapOutput $jmapOutput
    }
    catch {
        return $null
    }
}

function Get-NonHeapMemoryInfo {
    param([int]$ProcessId)

    try {
        $jcmdOutput = & jcmd $ProcessId VM.native_memory summary 2>$null
        return Parse-NativeMemoryOutput $jcmdOutput
    }
    catch {
        return $null
    }
}

function Parse-HeapOutput {
    param([string]$JmapOutput)

    if (-not $JmapOutput) {
        return $null
    }

    $heapInfo = @{}

    # 解析堆内存配置
    if ($JmapOutput -match "MaxHeapSize\s*=\s*(\d+)") {
        $heapInfo.MaxHeapSize = [long]$matches[1]
    }

    if ($JmapOutput -match "used:\s*(\d+)") {
        $heapInfo.UsedHeap = [long]$matches[1]
    }

    # 解析新生代和老年代信息
    if ($JmapOutput -match "PS Young Generation.*used:\s*(\d+).*total:\s*(\d+)") {
        $heapInfo.YoungUsed = [long]$matches[1]
        $heapInfo.YoungTotal = [long]$matches[2]
    }

    if ($JmapOutput -match "PS Old Generation.*used:\s*(\d+).*total:\s*(\d+)") {
        $heapInfo.OldUsed = [long]$matches[1]
        $heapInfo.OldTotal = [long]$matches[2]
    }

    return $heapInfo
}

function Parse-NativeMemoryOutput {
    param([string]$JcmdOutput)

    if (-not $JcmdOutput) {
        return $null
    }

    $nativeInfo = @{}

    # 解析各个内存区域的 usage
    $lines = $JcmdOutput -split "`n"
    foreach ($line in $lines) {
        if ($line -match "(\w+)\s+\(reserved=\d+KB, committed=(\d+)KB, (\d+)KB\)")) {
            $category = $matches[1]
            $committed = [long]$matches[2]
            $used = [long]$matches[3]
            $nativeInfo[$category] = @{
                Committed = $committed * 1024
                Used = $used * 1024
            }
        }
    }

    return $nativeInfo
}

function Detect-MemoryLeaks {
    param([array]$MemorySnapshots)

    Write-MemoryLog "开始内存泄漏分析..." "INFO"

    if ($MemorySnapshots.Count -lt 2) {
        Write-MemoryLog "内存快照数据不足，无法进行泄漏分析" "WARN"
        return
    }

    $latest = $MemorySnapshots[-1]
    $previous = $MemorySnapshots[-2]

    # 分析堆内存增长趋势
    $heapGrowthRate = Calculate-HeapGrowthRate $previous $latest
    Write-MemoryLog "堆内存增长率: $($heapGrowthRate.ToString('F2'))%" "INFO"

    # 分析GC效率
    $gcEfficiency = Calculate-GCEfficiency $latest.GCInfo
    Write-MemoryLog "GC效率: $($gcEfficiency.ToString('F2'))%" "INFO"

    # 分析非堆内存使用
    $nonHeapUsage = Calculate-NonHeapUsage $latest.NonHeapMemory
    Write-MemoryLog "非堆内存使用率: $($nonHeapUsage.ToString('F2'))%" "INFO"

    # 检测潜在内存泄漏
    $leakIndicators = @()

    # 1. 堆内存持续增长
    if ($heapGrowthRate -gt 20) {
        $leakIndicators += @{
            Type = "HEAP_GROWTH"
            Severity = "HIGH"
            Description = "堆内存增长率过高 ($($heapGrowthRate.ToString('F2'))%)"
            Recommendation = "检查是否存在内存泄漏，考虑进行堆转储分析"
        }
    }

    # 2. 老年代使用率过高
    if ($latest.HeapMemory.OldTotal -gt 0) {
        $oldGenUsage = ($latest.HeapMemory.OldUsed / $latest.HeapMemory.OldTotal) * 100
        if ($oldGenUsage -gt 80) {
            $leakIndicators += @{
                Type = "OLD_GENERATION_HIGH"
                Severity = "MEDIUM"
                Description = "老年代使用率过高 ($($oldGenUsage.ToString('F2'))%)"
                Recommendation = "检查是否存在长生命周期对象无法回收"
            }
        }
    }

    # 3. GC效率低下
    if ($gcEfficiency -lt 80) {
        $leakIndicators += @{
            Type = "GC_INEFFICIENCY"
            Severity = "MEDIUM"
            Description = "GC效率偏低 ($($gcEfficiency.ToString('F2'))%)"
            Recommendation = "优化GC参数，检查是否存在内存碎片"
        }
    }

    # 4. 元空间使用率过高
    if ($latest.NonHeapMemory.ContainsKey("Metaspace")) {
        $metaspaceUsage = ($latest.NonHeapMemory["Metaspace"].Used / $latest.NonHeapMemory["Metaspace"].Committed) * 100
        if ($metaspaceUsage -gt 90) {
            $leakIndicators += @{
                Type = "METASPACE_HIGH"
                Severity = "HIGH"
                Description = "元空间使用率过高 ($($metaspaceUsage.ToString('F2'))%)"
                Recommendation = "检查类加载器和动态代理使用情况"
            }
        }
    }

    # 输出检测结果
    if ($leakIndicators.Count -gt 0) {
        Write-MemoryLog "检测到 $($leakIndicators.Count) 个内存泄漏风险指标:" "WARN"
        foreach ($indicator in $leakIndicators) {
            Write-MemoryLog "- $($indicator.Type) ($($indicator.Severity)): $($indicator.Description)" "WARN"
            Write-MemoryLog "  建议: $($indicator.Recommendation)" "INFO"
        }
    } else {
        Write-MemoryLog "未检测到明显的内存泄漏风险" "SUCCESS"
    }

    return $leakIndicators
}

function Calculate-HeapGrowthRate {
    param($Previous, $Current)

    if (-not $Previous.HeapMemory.UsedHeap -or -not $Current.HeapMemory.UsedHeap) {
        return 0
    }

    $growth = $Current.HeapMemory.UsedHeap - $Previous.HeapMemory.UsedHeap
    $rate = ($growth / $Previous.HeapMemory.UsedHeap) * 100
    return $rate
}

function Calculate-GCEfficiency {
    param($GCInfo)

    if (-not $GCInfo) {
        return 0
    }

    # 计算GC效率：年轻代GC时间占比越低越好
    $ygcTime = if ($GCInfo.ContainsKey("YGCT")) { $GCInfo["YGCT"] } else { 0 }
    $fgcTime = if ($GCInfo.ContainsKey("FGCT")) { $GCInfo["FGCT"] } else { 0 }
    $totalTime = $ygcTime + $fgcTime

    if ($totalTime -eq 0) {
        return 100
    }

    # 简化的GC效率计算
    return [math]::Max(0, 100 - ($totalTime * 10))
}

function Calculate-NonHeapUsage {
    param($NonHeapMemory)

    if (-not $NonHeapMemory) {
        return 0
    }

    $totalCommitted = 0
    $totalUsed = 0

    foreach ($category in $NonHeapMemory.Values) {
        $totalCommitted += $category.Committed
        $totalUsed += $category.Used
    }

    if ($totalCommitted -eq 0) {
        return 0
    }

    return ($totalUsed / $totalCommitted) * 100
}

function Generate-MemoryReport {
    param([array]$MemorySnapshots, [array]$LeakIndicators)

    $reportPath = Join-Path $OutputPath "memory-leak-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').html"

    # 确保输出目录存在
    if (-not (Test-Path $OutputPath)) {
        New-Item -ItemType Directory -Path $OutputPath -Force | Out-Null
    }

    $html = @"
<!DOCTYPE html>
<html>
<head>
    <title>IOE-DREAM 内存泄漏检测报告</title>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f0f0f0; padding: 20px; border-radius: 5px; }
        .section { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }
        .warning { background-color: #fff3cd; border-color: #ffeaa7; }
        .error { background-color: #f8d7da; border-color: #f5c6cb; }
        .success { background-color: #d4edda; border-color: #c3e6cb; }
        table { width: 100%; border-collapse: collapse; margin: 10px 0; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .metric { display: inline-block; margin: 10px; padding: 10px; background-color: #f9f9f9; border-radius: 3px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>IOE-DREAM 内存泄漏检测报告</h1>
        <p>生成时间: $(Get-Date)</p>
        <p>检测服务: $ServiceName</p>
    </div>

    <div class="section">
        <h2>内存概览</h2>
        <div class="metric">
            <strong>内存快照数量:</strong> $($MemorySnapshots.Count)
        </div>
        <div class="metric">
            <strong>检测到的问题:</strong> $($LeakIndicators.Count)
        </div>
    </div>

    <div class="section">
        <h2>检测结果</h2>
"@

    if ($LeakIndicators.Count -gt 0) {
        $html += @"
        <div class="warning">
            <h3>⚠️ 发现 $($LeakIndicators.Count) 个内存泄漏风险</h3>
            <table>
                <tr><th>风险类型</th><th>严重程度</th><th>描述</th><th>建议</th></tr>
"@
        foreach ($indicator in $LeakIndicators) {
            $severityClass = switch ($indicator.Severity) {
                "HIGH" { "error" }
                "MEDIUM" { "warning" }
                default { "" }
            }
            $html += @"
                <tr>
                    <td>$($indicator.Type)</td>
                    <td class="$severityClass">$($indicator.Severity)</td>
                    <td>$($indicator.Description)</td>
                    <td>$($indicator.Recommendation)</td>
                </tr>
"@
        }
        $html += "</table></div>"
    } else {
        $html += '<div class="success">✅ 未检测到明显的内存泄漏风险</div>'
    }

    $html += @"
    </div>

    <div class="section">
        <h2>内存趋势图</h2>
        <p>建议使用 JVisualVM 或 JProfiler 进行详细内存分析</p>
        <p>命令参考:</p>
        <ul>
            <li><code>jvisualvm</code> - 启动VisualVM</li>
            <li><code>jmap -dump:format=b,file=heap.dump &lt;pid&gt;</code> - 生成堆转储</li>
            <li><code>jcmd &lt;pid&gt; GC.run_finalization</code> - 强制GC</li>
        </ul>
    </div>
</body>
</html>
"@

    $html | Out-File -FilePath $reportPath -Encoding UTF8
    Write-MemoryLog "内存检测报告已生成: $reportPath" "SUCCESS"

    return $reportPath
}

# 主执行流程
function Main {
    Write-MemoryLog "========================================" "INFO"
    Write-MemoryLog "IOE-DREAM 内存泄漏检测工具" "INFO"
    Write-MemoryLog "服务: $ServiceName" "INFO"
    Write-MemoryLog "连续监控: $Continuous" "INFO"
    Write-MemoryLog "监控间隔: $Interval 秒" "INFO"
    Write-MemoryLog "输出路径: $OutputPath" "INFO"
    Write-MemoryLog "========================================" "INFO"

    $memorySnapshots = @()
    $services = if ($ServiceName -eq "all") {
        @("ioedream-gateway-service", "ioedream-common-service", "ioedream-access-service",
          "ioedream-attendance-service", "ioedream-consume-service", "ioedream-visitor-service",
          "ioedream-video-service", "ioedream-oa-service", "ioedream-device-comm-service")
    } else {
        @($ServiceName)
    }

    try {
        do {
            foreach ($service in $services) {
                Write-MemoryLog "正在检测服务: $service" "INFO"

                $memoryInfo = Get-ServiceMemoryInfo -Service $service
                if ($memoryInfo) {
                    $memorySnapshots += $memoryInfo

                    Write-MemoryLog "服务: $($memoryInfo.ServiceName)" "INFO"
                    if ($memoryInfo.HeapMemory.UsedHeap) {
                        $heapUsedMB = [math]::Round($memoryInfo.HeapMemory.UsedHeap / 1MB, 2)
                        Write-MemoryLog "  堆内存使用: $heapUsedMB MB" "INFO"
                    }

                    if ($memoryInfo.GCInfo -and $memoryInfo.GCInfo.ContainsKey("YGC")) {
                        Write-MemoryLog "  年轻代GC次数: $($memoryInfo.GCInfo["YGC"])" "INFO"
                    }
                    if ($memoryInfo.GCInfo -and $memoryInfo.GCInfo.ContainsKey("FGC")) {
                        Write-MemoryLog "  老年代GC次数: $($memoryInfo.GCInfo["FGC"])" "INFO"
                    }
                }
            }

            # 分析内存泄漏风险
            $leakIndicators = Detect-MemoryLeaks -MemorySnapshots $memorySnapshots

            # 生成报告
            if ($GenerateReport -or $leakIndicators.Count -gt 0) {
                Generate-MemoryReport -MemorySnapshots $memorySnapshots -LeakIndicators $leakIndicators
            }

            if ($Continuous) {
                Write-MemoryLog "等待 $Interval 秒后进行下一次检测..." "INFO"
                Start-Sleep -Seconds $Interval
            }

        } while ($Continuous)

        Write-MemoryLog "内存泄漏检测完成" "SUCCESS"

    } catch {
        Write-MemoryLog "内存泄漏检测过程中发生错误: $($_.Exception.Message)" "ERROR"
        exit 1
    }
}

# 执行主函数
Main