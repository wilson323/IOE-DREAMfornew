# =====================================================
# IOE-DREAM 服务启动测试脚本
# 版本: v1.0.0
# 描述: 测试启动单个服务，验证环境变量和配置
# =====================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$ServiceName,
    
    [Parameter(Mandatory=$false)]
    [int]$WaitSeconds = 60
)

$ErrorActionPreference = "Stop"

# 加载环境变量
$loadEnvScript = Join-Path $PSScriptRoot "load-env.ps1"
& $loadEnvScript -Silent

# 服务配置
$services = @{
    "gateway" = @{ Port = 8080; Path = "microservices\ioedream-gateway-service" }
    "common" = @{ Port = 8088; Path = "microservices\ioedream-common-service" }
    "device" = @{ Port = 8087; Path = "microservices\ioedream-device-comm-service" }
    "oa" = @{ Port = 8089; Path = "microservices\ioedream-oa-service" }
    "access" = @{ Port = 8090; Path = "microservices\ioedream-access-service" }
    "attendance" = @{ Port = 8091; Path = "microservices\ioedream-attendance-service" }
    "video" = @{ Port = 8092; Path = "microservices\ioedream-video-service" }
    "consume" = @{ Port = 8094; Path = "microservices\ioedream-consume-service" }
    "visitor" = @{ Port = 8095; Path = "microservices\ioedream-visitor-service" }
}

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$service = $services[$ServiceName.ToLower()]

if (-not $service) {
    Write-Host "[ERROR] Unknown service: $ServiceName" -ForegroundColor Red
    Write-Host "Available services: $($services.Keys -join ', ')" -ForegroundColor Yellow
    exit 1
}

$servicePath = Join-Path $ProjectRoot $service.Path

if (-not (Test-Path $servicePath)) {
    Write-Host "[ERROR] Service path not found: $servicePath" -ForegroundColor Red
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting $ServiceName Service" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Service: $ServiceName" -ForegroundColor Yellow
Write-Host "Port: $($service.Port)" -ForegroundColor Yellow
Write-Host "Path: $servicePath" -ForegroundColor Yellow
Write-Host ""

# 检查端口是否被占用
$portInUse = Test-NetConnection -ComputerName localhost -Port $service.Port -InformationLevel Quiet -WarningAction SilentlyContinue
if ($portInUse) {
    Write-Host "[WARN] Port $($service.Port) is already in use" -ForegroundColor Yellow
    Write-Host "[INFO] Service may already be running" -ForegroundColor Gray
    exit 0
}

# 构建环境变量命令
$envVars = @()
Get-ChildItem Env: | Where-Object { 
    $_.Name -match '^(MYSQL_|REDIS_|NACOS_|SPRING_|GATEWAY_|JAVA_|MAVEN_)' 
} | ForEach-Object {
    $envVars += "set $($_.Name)=$($_.Value)"
}

$envVarsCmd = $envVars -join " && "

# 启动服务
Write-Host "[INFO] Starting service..." -ForegroundColor Cyan
Write-Host ""

try {
    Push-Location $servicePath
    
    # 使用Start-Process启动，捕获输出
    $process = Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -NoNewWindow -PassThru -RedirectStandardOutput "startup-output.log" -RedirectStandardError "startup-error.log"
    
    Write-Host "[OK] Service process started (PID: $($process.Id))" -ForegroundColor Green
    Write-Host "[INFO] Waiting for service to start (max $WaitSeconds seconds)..." -ForegroundColor Yellow
    Write-Host ""
    
    # 等待服务启动
    $started = $false
    $elapsed = 0
    $checkInterval = 2
    
    while ($elapsed -lt $WaitSeconds) {
        Start-Sleep -Seconds $checkInterval
        $elapsed += $checkInterval
        
        # 检查端口
        $portOpen = Test-NetConnection -ComputerName localhost -Port $service.Port -InformationLevel Quiet -WarningAction SilentlyContinue
        if ($portOpen) {
            Write-Host "[SUCCESS] Service is running on port $($service.Port)!" -ForegroundColor Green
            $started = $true
            break
        }
        
        # 检查进程是否还在运行
        if (-not (Get-Process -Id $process.Id -ErrorAction SilentlyContinue)) {
            Write-Host "[ERROR] Service process exited unexpectedly" -ForegroundColor Red
            Write-Host "[INFO] Check startup-error.log for details" -ForegroundColor Yellow
            break
        }
        
        Write-Host "  Waiting... ($elapsed/$WaitSeconds seconds)" -ForegroundColor Gray
    }
    
    if (-not $started) {
        Write-Host "[WARN] Service did not start within $WaitSeconds seconds" -ForegroundColor Yellow
        Write-Host "[INFO] Check startup-output.log and startup-error.log for details" -ForegroundColor Yellow
    }
    
    # 显示日志最后几行
    if (Test-Path "startup-error.log") {
        Write-Host ""
        Write-Host "Last 10 lines of error log:" -ForegroundColor Yellow
        Get-Content "startup-error.log" -Tail 10 | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
    }
    
    Pop-Location
    
} catch {
    Write-Host "[ERROR] Failed to start service: $($_.Exception.Message)" -ForegroundColor Red
    Pop-Location
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

