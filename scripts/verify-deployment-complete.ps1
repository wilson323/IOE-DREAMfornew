# IOE-DREAM 完整部署验证脚本
# 验证所有服务是否正常启动和运行
# 包括：Docker容器状态、服务健康检查、Nacos注册中心、服务间通信、前端访问

param(
    [switch]$SkipFrontend = $false,
    [int]$MaxRetries = 30,
    [int]$RetryInterval = 5
)

$ErrorActionPreference = "Continue"
$script:BaseUrl = "http://localhost"
$script:Services = @{
    "MySQL" = @{ Port = 3306; HealthCheck = $null }
    "Redis" = @{ Port = 6379; HealthCheck = $null }
    "Nacos" = @{ Port = 8848; HealthCheck = "$BaseUrl:8848/nacos/v1/console/health/readiness" }
    "Gateway" = @{ Port = 8080; HealthCheck = "$BaseUrl:8080/actuator/health" }
    "Common" = @{ Port = 8088; HealthCheck = "$BaseUrl:8088/actuator/health" }
    "DeviceComm" = @{ Port = 8087; HealthCheck = "$BaseUrl:8087/actuator/health" }
    "OA" = @{ Port = 8089; HealthCheck = "$BaseUrl:8089/actuator/health" }
    "Access" = @{ Port = 8090; HealthCheck = "$BaseUrl:8090/actuator/health" }
    "Attendance" = @{ Port = 8091; HealthCheck = "$BaseUrl:8091/actuator/health" }
    "Video" = @{ Port = 8092; HealthCheck = "$BaseUrl:8092/actuator/health" }
    "Consume" = @{ Port = 8094; HealthCheck = "$BaseUrl:8094/actuator/health" }
    "Visitor" = @{ Port = 8095; HealthCheck = "$BaseUrl:8095/actuator/health" }
}

# 颜色输出函数
function Write-Status {
    param([string]$Message, [string]$Status = "INFO")
    $colors = @{
        "SUCCESS" = "Green"
        "ERROR" = "Red"
        "WARNING" = "Yellow"
        "INFO" = "Cyan"
    }
    $color = $colors[$Status]
    Write-Host $Message -ForegroundColor $color
}

# 检查端口是否开放
function Test-Port {
    param([string]$Host, [int]$Port, [int]$Timeout = 3)
    try {
        $connection = Test-NetConnection -ComputerName $Host -Port $Port -WarningAction SilentlyContinue -InformationLevel Quiet -ErrorAction SilentlyContinue
        return $connection
    } catch {
        return $false
    }
}

# HTTP健康检查
function Test-HealthEndpoint {
    param([string]$Url, [int]$Timeout = 5)
    try {
        $response = Invoke-WebRequest -Uri $Url -TimeoutSec $Timeout -UseBasicParsing -ErrorAction Stop
        return $response.StatusCode -eq 200
    } catch {
        return $false
    }
}

# 等待服务启动
function Wait-ForService {
    param([string]$ServiceName, [string]$HealthCheckUrl, [int]$MaxRetries = 30, [int]$RetryInterval = 5)
    
    if (-not $HealthCheckUrl) {
        return $true
    }
    
    Write-Status "  等待 $ServiceName 服务启动..." "INFO"
    for ($i = 1; $i -le $MaxRetries; $i++) {
        if (Test-HealthEndpoint -Url $HealthCheckUrl) {
            Write-Status "  ✅ $ServiceName 服务已启动 (尝试 $i/$MaxRetries)" "SUCCESS"
            return $true
        }
        Start-Sleep -Seconds $RetryInterval
        Write-Host "  ⏳ 等待中... ($i/$MaxRetries)" -ForegroundColor Gray
    }
    Write-Status "  ❌ $ServiceName 服务启动超时" "ERROR"
    return $false
}

# 检查Docker容器状态
function Test-DockerContainers {
    Write-Status "`n[1] 检查Docker容器状态..." "INFO"
    
    $containers = docker ps -a --format "{{.Names}}|{{.Status}}|{{.Ports}}" 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Status "  ❌ 无法获取Docker容器列表" "ERROR"
        return $false
    }
    
    $ioedreamContainers = $containers | Where-Object { $_ -match "ioedream" }
    $runningContainers = $ioedreamContainers | Where-Object { $_ -match "Up" }
    $stoppedContainers = $ioedreamContainers | Where-Object { $_ -notmatch "Up" }
    
    Write-Status "  发现 $($ioedreamContainers.Count) 个IOE-DREAM容器" "INFO"
    Write-Status "  运行中: $($runningContainers.Count) 个" "SUCCESS"
    
    if ($stoppedContainers.Count -gt 0) {
        Write-Status "  已停止: $($stoppedContainers.Count) 个" "WARNING"
        $stoppedContainers | ForEach-Object {
            $name = ($_ -split '\|')[0]
            Write-Status "    - $name" "WARNING"
        }
    }
    
    return $runningContainers.Count -gt 0
}

# 检查服务端口
function Test-ServicePorts {
    Write-Status "`n[2] 检查服务端口..." "INFO"
    
    $allPassed = $true
    foreach ($serviceName in $script:Services.Keys) {
        $service = $script:Services[$serviceName]
        $port = $service.Port
        
        Write-Host "  检查 $serviceName (端口 $port)..." -ForegroundColor Cyan
        if (Test-Port -Host "localhost" -Port $port) {
            Write-Status "    ✅ 端口 $port 已开放" "SUCCESS"
        } else {
            Write-Status "    ❌ 端口 $port 未开放" "ERROR"
            $allPassed = $false
        }
    }
    
    return $allPassed
}

# 检查服务健康状态
function Test-ServiceHealth {
    Write-Status "`n[3] 检查服务健康状态..." "INFO"
    
    $allHealthy = $true
    foreach ($serviceName in $script:Services.Keys) {
        $service = $script:Services[$serviceName]
        $healthCheck = $service.HealthCheck
        
        if (-not $healthCheck) {
            continue
        }
        
        Write-Host "  检查 $serviceName 健康状态..." -ForegroundColor Cyan
        if (Test-HealthEndpoint -Url $healthCheck) {
            Write-Status "    ✅ $serviceName 健康检查通过" "SUCCESS"
        } else {
            Write-Status "    ❌ $serviceName 健康检查失败" "ERROR"
            $allHealthy = $false
        }
    }
    
    return $allHealthy
}

# 检查Nacos注册中心
function Test-NacosRegistry {
    Write-Status "`n[4] 检查Nacos注册中心..." "INFO"
    
    # 检查Nacos控制台
    $nacosConsole = "$BaseUrl:8848/nacos"
    Write-Host "  检查Nacos控制台..." -ForegroundColor Cyan
    if (Test-HealthEndpoint -Url "$nacosConsole/v1/console/health/readiness") {
        Write-Status "    ✅ Nacos控制台可访问: $nacosConsole" "SUCCESS"
    } else {
        Write-Status "    ❌ Nacos控制台不可访问" "ERROR"
        return $false
    }
    
    # 检查Nacos API - 获取服务列表
    try {
        $nacosApi = "$BaseUrl:8848/nacos/v1/ns/service/list"
        $response = Invoke-RestMethod -Uri $nacosApi -Method Get -TimeoutSec 5 -ErrorAction Stop
        Write-Status "    ✅ Nacos API正常" "SUCCESS"
        
        # 检查注册的服务数量
        if ($response.doms) {
            $serviceCount = $response.doms.Count
            Write-Status "    已注册服务数量: $serviceCount" "INFO"
            
            # 列出注册的服务
            Write-Host "    注册的服务列表:" -ForegroundColor Gray
            $response.doms | ForEach-Object {
                Write-Host "      - $_" -ForegroundColor Gray
            }
        } else {
            Write-Status "    ⚠️  暂无服务注册" "WARNING"
        }
        
        return $true
    } catch {
        Write-Status "    ❌ Nacos API调用失败: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# 检查服务间通信
function Test-ServiceCommunication {
    Write-Status "`n[5] 检查服务间通信..." "INFO"
    
    # 通过网关测试服务间调用
    $gatewayUrl = "$BaseUrl:8080"
    
    # 测试网关是否可用
    Write-Host "  测试API网关..." -ForegroundColor Cyan
    if (-not (Test-HealthEndpoint -Url "$gatewayUrl/actuator/health")) {
        Write-Status "    ❌ API网关不可用，无法测试服务间通信" "ERROR"
        return $false
    }
    Write-Status "    ✅ API网关可用" "SUCCESS"
    
    # 测试通过网关访问公共服务
    $testEndpoints = @(
        @{ Service = "Common"; Path = "/api/v1/common/health" }
        @{ Service = "OA"; Path = "/api/v1/oa/health" }
    )
    
    $allPassed = $true
    foreach ($endpoint in $testEndpoints) {
        $url = "$gatewayUrl$($endpoint.Path)"
        Write-Host "  测试 $($endpoint.Service) 服务..." -ForegroundColor Cyan
        try {
            $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Status "    ✅ $($endpoint.Service) 服务可访问" "SUCCESS"
            } else {
                Write-Status "    ⚠️  $($endpoint.Service) 服务返回状态码: $($response.StatusCode)" "WARNING"
            }
        } catch {
            Write-Status "    ❌ $($endpoint.Service) 服务不可访问: $($_.Exception.Message)" "ERROR"
            $allPassed = $false
        }
    }
    
    return $allPassed
}

# 检查前端应用
function Test-FrontendAccess {
    if ($SkipFrontend) {
        Write-Status "`n[6] 跳过前端检查..." "INFO"
        return $true
    }
    
    Write-Status "`n[6] 检查前端应用..." "INFO"
    
    # 检查前端是否运行（通常在前端开发服务器上）
    $frontendPorts = @(3000, 8081)
    $frontendFound = $false
    
    foreach ($port in $frontendPorts) {
        Write-Host "  检查前端端口 $port..." -ForegroundColor Cyan
        if (Test-Port -Host "localhost" -Port $port) {
            Write-Status "    ✅ 前端端口 $port 已开放" "SUCCESS"
            $frontendFound = $true
            
            # 尝试访问前端
            try {
                $response = Invoke-WebRequest -Uri "$BaseUrl:$port" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
                if ($response.StatusCode -eq 200) {
                    Write-Status "    ✅ 前端应用可访问: $BaseUrl:$port" "SUCCESS"
                }
            } catch {
                Write-Status "    ⚠️  前端端口开放但无法访问" "WARNING"
            }
        }
    }
    
    if (-not $frontendFound) {
        Write-Status "    ⚠️  未发现运行中的前端应用" "WARNING"
        Write-Status "    提示: 前端可能需要单独启动" "INFO"
    }
    
    return $true
}

# 生成验证报告
function Write-VerificationReport {
    param([hashtable]$Results)
    
    Write-Status "`n========================================" "INFO"
    Write-Status "  部署验证报告" "INFO"
    Write-Status "========================================" "INFO"
    Write-Host ""
    
    $total = $Results.Count
    $passed = ($Results.Values | Where-Object { $_ -eq $true }).Count
    $failed = $total - $passed
    
    Write-Status "总检查项: $total" "INFO"
    Write-Status "通过: $passed" "SUCCESS"
    Write-Status "失败: $failed" $(if ($failed -eq 0) { "SUCCESS" } else { "ERROR" })
    Write-Host ""
    
    Write-Status "详细结果:" "INFO"
    foreach ($key in $Results.Keys) {
        $status = if ($Results[$key]) { "✅ 通过" } else { "❌ 失败" }
        $color = if ($Results[$key]) { "Green" } else { "Red" }
        Write-Host "  $key : $status" -ForegroundColor $color
    }
    
    Write-Host ""
    if ($failed -eq 0) {
        Write-Status "🎉 所有验证通过！系统已成功部署！" "SUCCESS"
        return 0
    } else {
        Write-Status "⚠️  部分验证失败，请检查上述问题" "WARNING"
        return 1
    }
}

# 主函数
function Main {
    Write-Status "========================================" "INFO"
    Write-Status "  IOE-DREAM 完整部署验证" "INFO"
    Write-Status "========================================" "INFO"
    Write-Status "执行时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "INFO"
    Write-Host ""
    
    $results = @{}
    
    # 1. 检查Docker容器
    $results["Docker容器状态"] = Test-DockerContainers
    
    # 2. 检查服务端口
    $results["服务端口"] = Test-ServicePorts
    
    # 3. 等待服务启动并检查健康状态
    Write-Status "`n等待服务启动..." "INFO"
    Start-Sleep -Seconds 10
    
    $results["服务健康状态"] = Test-ServiceHealth
    
    # 4. 检查Nacos注册中心
    $results["Nacos注册中心"] = Test-NacosRegistry
    
    # 5. 检查服务间通信
    $results["服务间通信"] = Test-ServiceCommunication
    
    # 6. 检查前端应用
    $results["前端应用"] = Test-FrontendAccess
    
    # 生成报告
    $exitCode = Write-VerificationReport -Results $results
    exit $exitCode
}

# 执行主函数
Main
