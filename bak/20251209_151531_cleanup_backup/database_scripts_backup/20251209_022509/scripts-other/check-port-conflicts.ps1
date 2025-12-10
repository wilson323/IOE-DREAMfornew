# 端口冲突检查脚本
# 用途：全面检查所有端口冲突问题

Write-Host "=== 端口冲突全面检查 ===" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Get-Location
Set-Location $projectRoot

$conflicts = @()
$allPorts = @{}

# 1. 检查docker-compose-all.yml中的所有端口
Write-Host "1. 检查docker-compose-all.yml端口映射..." -ForegroundColor Yellow

if (Test-Path "docker-compose-all.yml") {
    $content = Get-Content "docker-compose-all.yml" -Raw
    
    # 提取所有端口映射
    $portMatches = [regex]::Matches($content, 'ports:\s*\r?\n\s*-\s*"(\d+):(\d+)"')
    
    foreach ($match in $portMatches) {
        $hostPort = $match.Groups[1].Value
        $containerPort = $match.Groups[2].Value
        
        if ($allPorts.ContainsKey($hostPort)) {
            $conflicts += "端口 $hostPort 冲突: $($allPorts[$hostPort]) 与 docker-compose映射冲突"
        } else {
            $allPorts[$hostPort] = "docker-compose映射 (容器端口: $containerPort)"
        }
        
        Write-Host "  ✓ 端口映射: $hostPort -> $containerPort" -ForegroundColor Gray
    }
}

# 2. 检查设备通讯服务的TCP/UDP端口
Write-Host "`n2. 检查设备通讯服务内部端口..." -ForegroundColor Yellow

$deviceCommConfig = "microservices\ioedream-device-comm-service\src\main\resources\application.yml"
if (Test-Path $deviceCommConfig) {
    $content = Get-Content $deviceCommConfig -Raw
    
    # 检查TCP端口
    if ($content -match "tcp:\s*\r?\n\s*port:\s*\$\{DEVICE_PROTOCOL_TCP_PORT:(\d+)\}") {
        $tcpPort = $matches[1]
        Write-Host "  ✓ TCP端口: $tcpPort" -ForegroundColor Gray
        
        if ($allPorts.ContainsKey($tcpPort)) {
            $conflicts += "设备通讯服务TCP端口 $tcpPort 与 $($allPorts[$tcpPort]) 冲突"
        } else {
            $allPorts[$tcpPort] = "设备通讯服务TCP端口"
        }
    }
    
    # 检查UDP端口
    if ($content -match "udp:\s*\r?\n\s*port:\s*\$\{DEVICE_PROTOCOL_UDP_PORT:(\d+)\}") {
        $udpPort = $matches[1]
        Write-Host "  ✓ UDP端口: $udpPort" -ForegroundColor Gray
        
        if ($allPorts.ContainsKey($udpPort)) {
            $conflicts += "设备通讯服务UDP端口 $udpPort 与 $($allPorts[$udpPort]) 冲突"
        } else {
            $allPorts[$udpPort] = "设备通讯服务UDP端口"
        }
    }
}

# 3. 检查所有微服务的HTTP端口
Write-Host "`n3. 检查微服务HTTP端口..." -ForegroundColor Yellow

$services = @{
    "ioedream-gateway-service" = "8080"
    "ioedream-device-comm-service" = "8087"
    "ioedream-common-service" = "8088"
    "ioedream-oa-service" = "8089"
    "ioedream-access-service" = "8090"
    "ioedream-attendance-service" = "8091"
    "ioedream-video-service" = "8092"
    "ioedream-consume-service" = "8094"
    "ioedream-visitor-service" = "8095"
}

foreach ($service in $services.Keys) {
    $port = $services[$service]
    $configFile = "microservices\$service\src\main\resources\application.yml"
    
    if (Test-Path $configFile) {
        $content = Get-Content $configFile -Raw
        if ($content -match "port:\s*(\d+)") {
            $configPort = $matches[1]
            if ($configPort -ne $port) {
                $conflicts += "$service 配置端口 $configPort 与标准端口 $port 不一致"
            }
        }
    }
    
    if ($allPorts.ContainsKey($port)) {
        $conflicts += "$service HTTP端口 $port 与 $($allPorts[$port]) 冲突"
    } else {
        $allPorts[$port] = "$service HTTP端口"
    }
    
    Write-Host "  ✓ $service : $port" -ForegroundColor Gray
}

# 4. 检查监控服务的端口
Write-Host "`n4. 检查监控服务端口..." -ForegroundColor Yellow

$monitoringFile = "deployment\monitoring\docker-compose-monitoring.yml"
if (Test-Path $monitoringFile) {
    $content = Get-Content $monitoringFile -Raw
    $monitoringPorts = @("9090", "9093", "3000", "9100")
    
    foreach ($port in $monitoringPorts) {
        # 使用${}避免PowerShell将冒号后的内容解析为变量名
        if ($content -match "${port}:${port}") {
            if ($allPorts.ContainsKey($port)) {
                $conflicts += "监控服务端口 $port 与 $($allPorts[$port]) 冲突"
            } else {
                $allPorts[$port] = "监控服务端口"
            }
            Write-Host "  ✓ 监控端口: $port" -ForegroundColor Gray
        }
    }
}

# 5. 检查系统已占用端口
Write-Host "`n5. 检查系统已占用端口..." -ForegroundColor Yellow

$usedPorts = netstat -ano | Select-String "LISTENING" | ForEach-Object {
    if ($_ -match ":(\d+)\s+.*LISTENING") {
        $matches[1]
    }
} | Sort-Object -Unique

foreach ($port in $allPorts.Keys) {
    if ($usedPorts -contains $port) {
        # 使用${}避免PowerShell将冒号后的内容解析为变量名
        $processInfo = netstat -ano | Select-String ":${port}\s+.*LISTENING" | Select-Object -First 1
        Write-Host "  ⚠ 端口 $port 已被占用: $processInfo" -ForegroundColor Yellow
    }
}

# 6. 生成报告
Write-Host "`n=== 检查结果 ===" -ForegroundColor Cyan

if ($conflicts.Count -eq 0) {
    Write-Host "✓ 未发现端口冲突" -ForegroundColor Green
    Write-Host "`n已使用的端口列表:" -ForegroundColor Cyan
    $allPorts.GetEnumerator() | Sort-Object Key | ForEach-Object {
        Write-Host "  $($_.Key) : $($_.Value)" -ForegroundColor Gray
    }
    exit 0
} else {
    Write-Host "✗ 发现 $($conflicts.Count) 个端口冲突:" -ForegroundColor Red
    foreach ($conflict in $conflicts) {
        Write-Host "  - $conflict" -ForegroundColor Red
    }
    Write-Host "`n请修复上述冲突后重新检查" -ForegroundColor Yellow
    exit 1
}
