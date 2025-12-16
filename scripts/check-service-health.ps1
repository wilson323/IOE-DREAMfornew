# =====================================================
# IOE-DREAM 服务健康检查脚本
# 版本: v1.0.0
# 描述: 检查所有服务的健康状态（包括端口和HTTP健康端点）
# 创建时间: 2025-12-15
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$WaitForReady,

    [Parameter(Mandatory=$false)]
    [int]$MaxWaitSeconds = 300,

    [Parameter(Mandatory=$false)]
    [switch]$Verbose
)

$ErrorActionPreference = "Continue"

# =====================================================
# 服务配置
# =====================================================

$InfrastructureServices = @(
    @{ Name = "MySQL"; Port = 3306; Type = "Database"; HealthCheck = $null },
    @{ Name = "Redis"; Port = 6379; Type = "Cache"; HealthCheck = $null },
    @{ Name = "Nacos"; Port = 8848; Type = "Registry"; HealthCheck = "http://127.0.0.1:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=1" },
    @{ Name = "RabbitMQ"; Port = 5672; Type = "MQ"; HealthCheck = $null },
    @{ Name = "RabbitMQ-Management"; Port = 15672; Type = "MQ-UI"; HealthCheck = "http://127.0.0.1:15672/api/overview" }
)

$MicroServices = @(
    @{ Name = "Gateway"; Port = 8080; Type = "Backend"; HealthCheck = "http://127.0.0.1:8080/actuator/health" },
    @{ Name = "Common"; Port = 8088; Type = "Backend"; HealthCheck = "http://127.0.0.1:8088/actuator/health" },
    @{ Name = "Device-Comm"; Port = 8087; Type = "Backend"; HealthCheck = "http://127.0.0.1:8087/actuator/health" },
    @{ Name = "OA"; Port = 8089; Type = "Backend"; HealthCheck = "http://127.0.0.1:8089/actuator/health" },
    @{ Name = "Access"; Port = 8090; Type = "Backend"; HealthCheck = "http://127.0.0.1:8090/actuator/health" },
    @{ Name = "Attendance"; Port = 8091; Type = "Backend"; HealthCheck = "http://127.0.0.1:8091/actuator/health" },
    @{ Name = "Video"; Port = 8092; Type = "Backend"; HealthCheck = "http://127.0.0.1:8092/actuator/health" },
    @{ Name = "Consume"; Port = 8094; Type = "Backend"; HealthCheck = "http://127.0.0.1:8094/actuator/health" },
    @{ Name = "Visitor"; Port = 8095; Type = "Backend"; HealthCheck = "http://127.0.0.1:8095/actuator/health" }
)

$FrontendServices = @(
    @{ Name = "Web-Admin"; Port = 3000; Type = "Frontend"; HealthCheck = "http://127.0.0.1:3000" },
    @{ Name = "Mobile-H5"; Port = 8081; Type = "Frontend"; HealthCheck = "http://127.0.0.1:8081" }
)

# =====================================================
# 辅助函数
# =====================================================

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Test-PortOpen {
    param([int]$Port, [int]$TimeoutMs = 1000)

    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $asyncResult = $tcpClient.BeginConnect("127.0.0.1", $Port, $null, $null)
        $wait = $asyncResult.AsyncWaitHandle.WaitOne($TimeoutMs, $false)

        if ($wait) {
            try {
                $tcpClient.EndConnect($asyncResult)
                $tcpClient.Close()
                return $true
            } catch {
                $tcpClient.Close()
                return $false
            }
        } else {
            $tcpClient.Close()
            return $false
        }
    } catch {
        return $false
    }
}

function Test-ServiceHealth {
    param([string]$HealthUrl, [int]$TimeoutSec = 5)

    if (-not $HealthUrl) {
        return @{ Status = "N/A"; Details = "无健康检查端点" }
    }

    try {
        $response = Invoke-WebRequest -Uri $HealthUrl -Method GET -TimeoutSec $TimeoutSec -ErrorAction Stop

        if ($response.StatusCode -eq 200) {
            # 尝试解析健康状态
            try {
                $json = $response.Content | ConvertFrom-Json
                if ($json.status -eq "UP") {
                    return @{ Status = "UP"; Details = "服务健康" }
                } elseif ($json.status) {
                    return @{ Status = $json.status; Details = "状态: $($json.status)" }
                }
            } catch {
                # 非JSON响应，但200表示服务可用
            }
            return @{ Status = "UP"; Details = "HTTP 200" }
        } else {
            return @{ Status = "DOWN"; Details = "HTTP $($response.StatusCode)" }
        }
    } catch [System.Net.WebException] {
        $webEx = $_.Exception
        if ($webEx.Response) {
            $statusCode = [int]$webEx.Response.StatusCode
            return @{ Status = "DOWN"; Details = "HTTP $statusCode" }
        }
        return @{ Status = "DOWN"; Details = "连接失败" }
    } catch {
        return @{ Status = "DOWN"; Details = $_.Exception.Message }
    }
}

function Get-ServiceStatus {
    param([hashtable]$Service)

    $portOpen = Test-PortOpen -Port $Service.Port

    if (-not $portOpen) {
        return @{
            Name = $Service.Name
            Port = $Service.Port
            Type = $Service.Type
            PortOpen = $false
            HealthStatus = "OFFLINE"
            Details = "端口未开放"
        }
    }

    $healthResult = Test-ServiceHealth -HealthUrl $Service.HealthCheck

    return @{
        Name = $Service.Name
        Port = $Service.Port
        Type = $Service.Type
        PortOpen = $true
        HealthStatus = $healthResult.Status
        Details = $healthResult.Details
    }
}

function Show-ServiceStatusTable {
    param([array]$Services, [string]$Title)

    Write-ColorOutput ""
    Write-ColorOutput "  $Title" "Cyan"
    Write-ColorOutput "  ----------------------------------------" "Gray"

    foreach ($svc in $Services) {
        $status = Get-ServiceStatus -Service $svc

        $statusIcon = switch ($status.HealthStatus) {
            "UP" { "[OK]"; break }
            "N/A" { "[--]"; break }
            default { "[XX]" }
        }

        $statusColor = switch ($status.HealthStatus) {
            "UP" { "Green"; break }
            "N/A" { if ($status.PortOpen) { "Green" } else { "Red" }; break }
            default { "Red" }
        }

        $portStatus = if ($status.PortOpen) { "OPEN" } else { "CLOSED" }
        $line = "  $statusIcon $($status.Name.PadRight(20)) Port:$($status.Port.ToString().PadRight(6)) $portStatus"

        if ($Verbose) {
            $line += "  $($status.Details)"
        }

        Write-ColorOutput $line $statusColor
    }
}

function Wait-ForAllServices {
    param([int]$MaxSeconds = 300)

    Write-ColorOutput ""
    Write-ColorOutput "  等待所有服务就绪 (最长 $MaxSeconds 秒)..." "Yellow"
    Write-ColorOutput ""

    $startTime = Get-Date
    $checkInterval = 10
    $allServices = $InfrastructureServices + $MicroServices

    while ($true) {
        $elapsed = (Get-Date) - $startTime
        if ($elapsed.TotalSeconds -gt $MaxSeconds) {
            Write-ColorOutput "  [超时] 等待服务超时" "Red"
            return $false
        }

        $readyCount = 0
        $totalCount = $allServices.Count

        foreach ($svc in $allServices) {
            if (Test-PortOpen -Port $svc.Port) {
                $readyCount++
            }
        }

        Write-ColorOutput "  [$([math]::Round($elapsed.TotalSeconds))s] 就绪: $readyCount/$totalCount 服务" "Gray"

        if ($readyCount -eq $totalCount) {
            Write-ColorOutput ""
            Write-ColorOutput "  [成功] 所有服务已就绪!" "Green"
            return $true
        }

        Start-Sleep -Seconds $checkInterval
    }
}

# =====================================================
# 主程序
# =====================================================

Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  IOE-DREAM 服务健康检查" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""
Write-ColorOutput "  检查时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "Gray"
Write-ColorOutput ""

# 基础设施服务
Show-ServiceStatusTable -Services $InfrastructureServices -Title "基础设施服务"

# 检查基础设施是否就绪
$infraReady = $true
foreach ($svc in $InfrastructureServices) {
    if (-not (Test-PortOpen -Port $svc.Port)) {
        $infraReady = $false
        break
    }
}

if (-not $infraReady) {
    Write-ColorOutput ""
    Write-ColorOutput "  [警告] 基础设施服务未完全就绪!" "Yellow"
    Write-ColorOutput "  微服务可能无法正常启动" "Yellow"
    Write-ColorOutput ""
    Write-ColorOutput "  解决方案:" "Yellow"
    Write-ColorOutput "    docker-compose -f docker-compose-all.yml up -d mysql redis nacos rabbitmq" "Gray"
    Write-ColorOutput ""
}

# 微服务
Show-ServiceStatusTable -Services $MicroServices -Title "微服务"

# 前端服务
Show-ServiceStatusTable -Services $FrontendServices -Title "前端服务"

# 等待模式
if ($WaitForReady) {
    $result = Wait-ForAllServices -MaxSeconds $MaxWaitSeconds
    if (-not $result) {
        exit 1
    }
}

# 汇总统计
Write-ColorOutput ""
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  汇总统计" "Cyan"
Write-ColorOutput "======================================================" "Cyan"

$allServices = $InfrastructureServices + $MicroServices + $FrontendServices
$onlineCount = 0
$offlineCount = 0

foreach ($svc in $allServices) {
    if (Test-PortOpen -Port $svc.Port) {
        $onlineCount++
    } else {
        $offlineCount++
    }
}

Write-ColorOutput ""
Write-ColorOutput "  总服务数: $($allServices.Count)" "White"
Write-ColorOutput "  在线: $onlineCount" "Green"
Write-ColorOutput "  离线: $offlineCount" $(if ($offlineCount -gt 0) { "Red" } else { "Green" })
Write-ColorOutput ""

# 显示访问地址
if ($onlineCount -gt 0) {
    Write-ColorOutput "======================================================" "Cyan"
    Write-ColorOutput "  服务访问地址" "Cyan"
    Write-ColorOutput "======================================================" "Cyan"
    Write-ColorOutput ""

    if (Test-PortOpen -Port 8848) {
        Write-ColorOutput "  Nacos控制台:   http://localhost:8848/nacos" "White"
    }
    if (Test-PortOpen -Port 15672) {
        Write-ColorOutput "  RabbitMQ控制台: http://localhost:15672" "White"
    }
    if (Test-PortOpen -Port 8080) {
        Write-ColorOutput "  API网关:       http://localhost:8080" "White"
    }
    if (Test-PortOpen -Port 3000) {
        Write-ColorOutput "  Web管理后台:   http://localhost:3000" "White"
    }
    if (Test-PortOpen -Port 8081) {
        Write-ColorOutput "  移动端H5:      http://localhost:8081" "White"
    }
    Write-ColorOutput ""
}

Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  检查完成" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""

exit $offlineCount
