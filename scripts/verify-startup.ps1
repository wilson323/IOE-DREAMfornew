# IOE-DREAM Startup Script Verification Tool
# Verify all prerequisites for starting services

# Set UTF-8 encoding
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

$ErrorActionPreference = "Continue"
$ProjectRoot = "D:\IOE-DREAM"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 启动脚本验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$allChecksPassed = $true

# 1. 检查Maven
Write-Host "[1] 检查Maven..." -ForegroundColor Yellow
try {
    $mvnCheck = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mvnCheck) {
        $mvnVersion = & mvn -version 2>&1 | Select-Object -First 1
        Write-Host "  ✓ Maven已安装: $mvnVersion" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Maven未安装或未配置到PATH" -ForegroundColor Red
        $allChecksPassed = $false
    }
} catch {
    Write-Host "  ✗ Maven检查失败: $_" -ForegroundColor Red
    $allChecksPassed = $false
}

# 2. 检查microservices-common
Write-Host "[2] 检查microservices-common..." -ForegroundColor Yellow
$commonPath = "$ProjectRoot\microservices\microservices-common"
if (Test-Path $commonPath) {
    Write-Host "  ✓ 目录存在: $commonPath" -ForegroundColor Green
    
    # 检查pom.xml
    $pomPath = Join-Path $commonPath "pom.xml"
    if (Test-Path $pomPath) {
        Write-Host "  ✓ pom.xml存在" -ForegroundColor Green
    } else {
        Write-Host "  ✗ pom.xml不存在" -ForegroundColor Red
        $allChecksPassed = $false
    }
} else {
    Write-Host "  ✗ 目录不存在: $commonPath" -ForegroundColor Red
    $allChecksPassed = $false
}

# 3. 检查依赖服务
Write-Host "[3] 检查依赖服务..." -ForegroundColor Yellow

function Test-Port {
    param([int]$Port, [string]$ServiceName)
    try {
        $result = Test-NetConnection -ComputerName localhost -Port $Port -InformationLevel Quiet -WarningAction SilentlyContinue
        if ($result) {
            Write-Host "  ✓ $ServiceName (端口$Port) - 运行中" -ForegroundColor Green
            return $true
        } else {
            Write-Host "  ⚠ $ServiceName (端口$Port) - 未运行" -ForegroundColor Yellow
            return $false
        }
    } catch {
        Write-Host "  ⚠ $ServiceName (端口$Port) - 检查失败" -ForegroundColor Yellow
        return $false
    }
}

$nacosRunning = Test-Port -Port 8848 -ServiceName "Nacos"
$mysqlRunning = Test-Port -Port 3306 -ServiceName "MySQL"
$redisRunning = Test-Port -Port 6379 -ServiceName "Redis"

if (-not $nacosRunning) {
    Write-Host "    警告: Nacos未运行，服务将无法注册到注册中心" -ForegroundColor Yellow
}

# 4. 检查服务目录
Write-Host "[4] 检查服务目录..." -ForegroundColor Yellow
$services = @(
    @{Name="ioedream-gateway-service"; Port=8080},
    @{Name="ioedream-common-service"; Port=8088},
    @{Name="ioedream-device-comm-service"; Port=8087},
    @{Name="ioedream-oa-service"; Port=8089},
    @{Name="ioedream-access-service"; Port=8090},
    @{Name="ioedream-attendance-service"; Port=8091},
    @{Name="ioedream-video-service"; Port=8092},
    @{Name="ioedream-consume-service"; Port=8094},
    @{Name="ioedream-visitor-service"; Port=8095}
)

foreach ($service in $services) {
    $servicePath = "$ProjectRoot\microservices\$($service.Name)"
    if (Test-Path $servicePath) {
        $pomPath = Join-Path $servicePath "pom.xml"
        if (Test-Path $pomPath) {
            Write-Host "  ✓ $($service.Name) (端口$($service.Port))" -ForegroundColor Green
        } else {
            Write-Host "  ⚠ $($service.Name) - 目录存在但缺少pom.xml" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ✗ $($service.Name) - 目录不存在" -ForegroundColor Red
        $allChecksPassed = $false
    }
}

# 5. 检查端口占用
Write-Host "[5] 检查端口占用..." -ForegroundColor Yellow
foreach ($service in $services) {
    $portInUse = Get-NetTCPConnection -LocalPort $service.Port -ErrorAction SilentlyContinue
    if ($portInUse) {
        Write-Host "  ⚠ 端口 $($service.Port) ($($service.Name)) 已被占用" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
if ($allChecksPassed) {
    Write-Host "[OK] All checks passed, ready to start services" -ForegroundColor Green
    Write-Host ""
    Write-Host "Run the following command to start services:" -ForegroundColor Cyan
    Write-Host "  .\scripts\start-all-complete.ps1 -BackendOnly" -ForegroundColor White
    Write-Host "  .\scripts\start-all-complete.ps1" -ForegroundColor White
    exit 0
} else {
    Write-Host "[ERROR] Some checks failed, please fix and retry" -ForegroundColor Red
    exit 1
}

