# IOE-DREAM 分步部署验证脚本
# 逐步验证部署状态

$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM 部署验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 检查Docker
Write-Host "[步骤1] 检查Docker环境..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ Docker已安装: $dockerVersion" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Docker未安装或未运行" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  ❌ Docker检查失败: $_" -ForegroundColor Red
    exit 1
}

# 步骤2: 检查Docker Compose
Write-Host "`n[步骤2] 检查Docker Compose..." -ForegroundColor Yellow
try {
    $composeVersion = docker compose version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ Docker Compose已安装: $composeVersion" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Docker Compose未安装" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  ❌ Docker Compose检查失败: $_" -ForegroundColor Red
    exit 1
}

# 步骤3: 检查docker-compose-all.yml
Write-Host "`n[步骤3] 检查Docker Compose配置文件..." -ForegroundColor Yellow
$composeFile = "D:\IOE-DREAM\docker-compose-all.yml"
if (Test-Path $composeFile) {
    Write-Host "  ✅ docker-compose-all.yml文件存在" -ForegroundColor Green
} else {
    Write-Host "  ❌ docker-compose-all.yml文件不存在" -ForegroundColor Red
    exit 1
}

# 步骤4: 检查所有容器状态
Write-Host "`n[步骤4] 检查Docker容器状态..." -ForegroundColor Yellow
try {
    $allContainers = docker ps -a 2>&1
    if ($LASTEXITCODE -eq 0) {
        $ioedreamContainers = $allContainers | Select-String "ioedream"
        if ($ioedreamContainers) {
            Write-Host "  发现的IOE-DREAM容器:" -ForegroundColor Cyan
            $ioedreamContainers | ForEach-Object {
                Write-Host "    $_" -ForegroundColor Gray
            }
            
            $runningContainers = docker ps --filter "name=ioedream" --format "{{.Names}}" 2>&1
            if ($runningContainers) {
                Write-Host "`n  运行中的容器:" -ForegroundColor Green
                $runningContainers | ForEach-Object {
                    Write-Host "    ✅ $_" -ForegroundColor Green
                }
            } else {
                Write-Host "`n  ⚠️  没有运行中的容器" -ForegroundColor Yellow
            }
        } else {
            Write-Host "  ⚠️  未发现IOE-DREAM容器" -ForegroundColor Yellow
            Write-Host "  提示: 可能需要先启动Docker Compose服务" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "  ❌ 检查容器状态失败: $_" -ForegroundColor Red
}

# 步骤5: 检查服务端口
Write-Host "`n[步骤5] 检查服务端口..." -ForegroundColor Yellow
$services = @(
    @{ Name = "MySQL"; Port = 3306 }
    @{ Name = "Redis"; Port = 6379 }
    @{ Name = "Nacos"; Port = 8848 }
    @{ Name = "Gateway"; Port = 8080 }
    @{ Name = "Common"; Port = 8088 }
    @{ Name = "DeviceComm"; Port = 8087 }
    @{ Name = "OA"; Port = 8089 }
    @{ Name = "Access"; Port = 8090 }
    @{ Name = "Attendance"; Port = 8091 }
    @{ Name = "Video"; Port = 8092 }
    @{ Name = "Consume"; Port = 8094 }
    @{ Name = "Visitor"; Port = 8095 }
)

$portResults = @{}
foreach ($service in $services) {
    Write-Host "  检查 $($service.Name) (端口 $($service.Port))..." -ForegroundColor Cyan
    try {
        $connection = Test-NetConnection -ComputerName localhost -Port $service.Port -WarningAction SilentlyContinue -InformationLevel Quiet -ErrorAction SilentlyContinue
        if ($connection) {
            Write-Host "    ✅ 端口 $($service.Port) 已开放" -ForegroundColor Green
            $portResults[$service.Name] = $true
        } else {
            Write-Host "    ❌ 端口 $($service.Port) 未开放" -ForegroundColor Red
            $portResults[$service.Name] = $false
        }
    } catch {
        Write-Host "    ❌ 检查端口失败: $_" -ForegroundColor Red
        $portResults[$service.Name] = $false
    }
}

# 步骤6: 检查Nacos注册中心
Write-Host "`n[步骤6] 检查Nacos注册中心..." -ForegroundColor Yellow
if ($portResults["Nacos"]) {
    try {
        $nacosUrl = "http://localhost:8848/nacos/v1/console/health/readiness"
        $response = Invoke-WebRequest -Uri $nacosUrl -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "  ✅ Nacos健康检查通过" -ForegroundColor Green
            Write-Host "  Nacos控制台: http://localhost:8848/nacos (用户名/密码: nacos/nacos)" -ForegroundColor Gray
            
            # 尝试获取服务列表
            try {
                $serviceListUrl = "http://localhost:8848/nacos/v1/ns/service/list"
                $serviceResponse = Invoke-RestMethod -Uri $serviceListUrl -Method Get -TimeoutSec 5 -ErrorAction Stop
                if ($serviceResponse.doms) {
                    Write-Host "  已注册服务数量: $($serviceResponse.doms.Count)" -ForegroundColor Cyan
                    Write-Host "  注册的服务:" -ForegroundColor Gray
                    $serviceResponse.doms | ForEach-Object {
                        Write-Host "    - $_" -ForegroundColor Gray
                    }
                } else {
                    Write-Host "  ⚠️  暂无服务注册到Nacos" -ForegroundColor Yellow
                }
            } catch {
                Write-Host "  ⚠️  无法获取服务列表: $_" -ForegroundColor Yellow
            }
        } else {
            Write-Host "  ❌ Nacos健康检查失败 (状态码: $($response.StatusCode))" -ForegroundColor Red
        }
    } catch {
        Write-Host "  ❌ Nacos不可访问: $_" -ForegroundColor Red
    }
} else {
    Write-Host "  ⚠️  Nacos端口未开放，跳过检查" -ForegroundColor Yellow
}

# 步骤7: 检查服务健康状态
Write-Host "`n[步骤7] 检查服务健康状态..." -ForegroundColor Yellow
$healthEndpoints = @(
    @{ Name = "Gateway"; Port = 8080; Path = "/actuator/health" }
    @{ Name = "Common"; Port = 8088; Path = "/actuator/health" }
    @{ Name = "DeviceComm"; Port = 8087; Path = "/actuator/health" }
    @{ Name = "OA"; Port = 8089; Path = "/actuator/health" }
    @{ Name = "Access"; Port = 8090; Path = "/actuator/health" }
    @{ Name = "Attendance"; Port = 8091; Path = "/actuator/health" }
    @{ Name = "Video"; Port = 8092; Path = "/actuator/health" }
    @{ Name = "Consume"; Port = 8094; Path = "/actuator/health" }
    @{ Name = "Visitor"; Port = 8095; Path = "/actuator/health" }
)

$healthResults = @{}
foreach ($endpoint in $healthEndpoints) {
    if ($portResults[$endpoint.Name]) {
        Write-Host "  检查 $($endpoint.Name) 服务..." -ForegroundColor Cyan
        try {
            $url = "http://localhost:$($endpoint.Port)$($endpoint.Path)"
            $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Host "    ✅ $($endpoint.Name) 健康检查通过" -ForegroundColor Green
                $healthResults[$endpoint.Name] = $true
            } else {
                Write-Host "    ❌ $($endpoint.Name) 健康检查失败 (状态码: $($response.StatusCode))" -ForegroundColor Red
                $healthResults[$endpoint.Name] = $false
            }
        } catch {
            Write-Host "    ❌ $($endpoint.Name) 健康检查失败: $_" -ForegroundColor Red
            $healthResults[$endpoint.Name] = $false
        }
    } else {
        Write-Host "  ⚠️  $($endpoint.Name) 端口未开放，跳过健康检查" -ForegroundColor Yellow
        $healthResults[$endpoint.Name] = $false
    }
}

# 步骤8: 检查服务间通信
Write-Host "`n[步骤8] 检查服务间通信..." -ForegroundColor Yellow
if ($healthResults["Gateway"]) {
    Write-Host "  通过API网关测试服务间通信..." -ForegroundColor Cyan
    try {
        # 测试网关路由
        $gatewayUrl = "http://localhost:8080/actuator/health"
        $response = Invoke-WebRequest -Uri $gatewayUrl -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "    ✅ API网关正常" -ForegroundColor Green
        }
    } catch {
        Write-Host "    ❌ API网关测试失败: $_" -ForegroundColor Red
    }
} else {
    Write-Host "  ⚠️  API网关不可用，无法测试服务间通信" -ForegroundColor Yellow
}

# 步骤9: 检查前端应用
Write-Host "`n[步骤9] 检查前端应用..." -ForegroundColor Yellow
$frontendPorts = @(3000, 8081)
$frontendFound = $false

foreach ($port in $frontendPorts) {
    $connection = $null
    try {
        $connection = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue -InformationLevel Quiet -ErrorAction SilentlyContinue
    } catch {
        # 端口检查失败，继续下一个
        continue
    }
    
    if ($connection) {
        Write-Host "  ✅ 前端端口 $port 已开放" -ForegroundColor Green
        $frontendFound = $true
        
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$port" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Host "    ✅ 前端应用可访问: http://localhost:$port" -ForegroundColor Green
            }
        } catch {
            Write-Host "    ⚠️  前端端口开放但无法访问" -ForegroundColor Yellow
        }
    }
}

if (-not $frontendFound) {
    Write-Host "  ⚠️  未发现运行中的前端应用" -ForegroundColor Yellow
    Write-Host "  提示: 前端需要单独启动 (npm run dev 或 npm run localhost)" -ForegroundColor Gray
}

# 生成总结报告
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  验证总结" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$portCount = ($portResults.Values | Where-Object { $_ -eq $true }).Count
$healthCount = ($healthResults.Values | Where-Object { $_ -eq $true }).Count

Write-Host "端口检查: $portCount/$($services.Count) 个服务端口已开放" -ForegroundColor $(if ($portCount -eq $services.Count) { "Green" } else { "Yellow" })
Write-Host "健康检查: $healthCount/$($healthEndpoints.Count) 个服务健康检查通过" -ForegroundColor $(if ($healthCount -eq $healthEndpoints.Count) { "Green" } else { "Yellow" })

if ($portCount -eq $services.Count -and $healthCount -eq $healthEndpoints.Count) {
    Write-Host "`n🎉 所有服务验证通过！" -ForegroundColor Green
} else {
    Write-Host "`n⚠️  部分服务未通过验证，请检查上述问题" -ForegroundColor Yellow
    Write-Host "`n建议操作:" -ForegroundColor Cyan
    Write-Host "  1. 检查Docker Compose服务是否已启动: docker-compose -f docker-compose-all.yml ps" -ForegroundColor Gray
    Write-Host "  2. 查看服务日志: docker-compose -f docker-compose-all.yml logs [服务名]" -ForegroundColor Gray
    Write-Host "  3. 重启服务: docker-compose -f docker-compose-all.yml restart [服务名]" -ForegroundColor Gray
}
