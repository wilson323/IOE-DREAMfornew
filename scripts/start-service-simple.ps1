# =====================================================
# IOE-DREAM 简单服务启动脚本
# 版本: v1.0.0
# 描述: 简化版服务启动脚本，确保环境变量正确传递
# =====================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$ServiceName,
    
    [Parameter(Mandatory=$false)]
    [switch]$WaitForReady
)

$ErrorActionPreference = "Stop"

# 加载环境变量
$loadEnvScript = Join-Path $PSScriptRoot "load-env.ps1"
if (Test-Path $loadEnvScript) {
    & $loadEnvScript -Silent
} else {
    Write-Host "[WARN] load-env.ps1 not found" -ForegroundColor Yellow
}

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

# 检查端口
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $asyncResult = $tcpClient.BeginConnect("127.0.0.1", $service.Port, $null, $null)
    $wait = $asyncResult.AsyncWaitHandle.WaitOne(1000, $false)
    if ($wait) {
        $tcpClient.EndConnect($asyncResult)
        $tcpClient.Close()
        Write-Host "[WARN] Port $($service.Port) is already in use" -ForegroundColor Yellow
        Write-Host "[INFO] Service may already be running" -ForegroundColor Gray
        exit 0
    }
    $tcpClient.Close()
} catch {
    # Port is not in use, continue
}

# 创建批处理文件来设置环境变量并启动服务
$batFile = Join-Path $env:TEMP "start-$ServiceName-$(Get-Date -Format 'yyyyMMddHHmmss').bat"
$batContent = @"
@echo off
cd /d "$servicePath"

REM 设置环境变量
"@
        
        # 添加所有环境变量，正确处理特殊字符
        Get-ChildItem Env: | Where-Object { 
            $_.Name -match '^(MYSQL_|REDIS_|NACOS_|SPRING_|GATEWAY_|JAVA_|MAVEN_|LOG_|MANAGEMENT_|DOCKER_|DRUID_|REDIS_POOL_|.*_SERVICE_PORT)' 
        } | ForEach-Object {
            $value = $_.Value
            # 转义特殊字符：双引号、&、|、<、>、^
            $value = $value -replace '"', '""'  # 转义双引号
            $value = $value -replace '&', '^&'   # 转义&符号（命令分隔符）
            $value = $value -replace '\|', '^|'  # 转义|符号（管道符）
            $value = $value -replace '<', '^<'   # 转义<符号
            $value = $value -replace '>', '^>'   # 转义>符号
            $value = $value -replace '\^', '^^'  # 转义^符号（需要双写）
            # 如果值包含空格或特殊字符，用引号包裹
            if ($value -match '[ &|<>^]' -or $value.Contains(' ')) {
                $batContent += "set `"$($_.Name)=$value`"`r`n"
            } else {
                $batContent += "set $($_.Name)=$value`r`n"
            }
        }
        
        $batContent += @"

REM 显示环境变量（用于调试）
echo [INFO] Starting $ServiceName service...
echo [INFO] MYSQL_HOST=%MYSQL_HOST%
echo [INFO] NACOS_SERVER_ADDR=%NACOS_SERVER_ADDR%
echo [INFO] REDIS_HOST=%REDIS_HOST%
echo.

REM 启动服务
mvn spring-boot:run

pause
"@

$batContent | Out-File -FilePath $batFile -Encoding ASCII

Write-Host "[INFO] Created batch file: $batFile" -ForegroundColor Cyan
Write-Host "[INFO] Starting service in new window..." -ForegroundColor Cyan
Write-Host ""

try {
    # 启动CMD窗口执行批处理文件
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = "cmd.exe"
    $startInfo.Arguments = "/k `"$batFile`""
    $startInfo.UseShellExecute = $true
    $startInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Normal

    $process = [System.Diagnostics.Process]::Start($startInfo)

    Write-Host "[OK] Service process started (PID: $($process.Id))" -ForegroundColor Green
    Write-Host "[INFO] Check the new CMD window for startup logs" -ForegroundColor Yellow
    Write-Host ""

    if ($WaitForReady) {
        Write-Host "[INFO] Waiting for service to be ready..." -ForegroundColor Cyan
        $maxWait = 120
        $elapsed = 0
        $checkInterval = 2

        while ($elapsed -lt $maxWait) {
            Start-Sleep -Seconds $checkInterval
            $elapsed += $checkInterval

            try {
                $tcpClient = New-Object System.Net.Sockets.TcpClient
                $asyncResult = $tcpClient.BeginConnect("127.0.0.1", $service.Port, $null, $null)
                $wait = $asyncResult.AsyncWaitHandle.WaitOne(1000, $false)
                if ($wait) {
                    $tcpClient.EndConnect($asyncResult)
                    $tcpClient.Close()
                    Write-Host "[SUCCESS] Service is running on port $($service.Port)!" -ForegroundColor Green
                    return
                }
                $tcpClient.Close()
            } catch {
                # Port not open yet
            }

            if ($elapsed % 10 -eq 0) {
                Write-Host "  Waiting... ($elapsed/$maxWait seconds)" -ForegroundColor Gray
            }
        }

        Write-Host "[WARN] Service did not start within $maxWait seconds" -ForegroundColor Yellow
    }

} catch {
    Write-Host "[ERROR] Failed to start service: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "[ERROR] Stack trace: $($_.ScriptStackTrace)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Startup Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

