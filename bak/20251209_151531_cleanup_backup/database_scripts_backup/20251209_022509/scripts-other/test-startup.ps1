# 测试启动脚本
# 验证所有关键步骤是否正常

$ErrorActionPreference = "Continue"
$ProjectRoot = "D:\IOE-DREAM"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 启动脚本测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查Maven
Write-Host "[1] 检查Maven..." -ForegroundColor Yellow
try {
    $mvnCheck = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mvnCheck) {
        $mvnVersion = & mvn -version 2>&1 | Select-Object -First 1
        Write-Host "  ✓ Maven已安装: $mvnVersion" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Maven未安装" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  ❌ Maven检查失败: $_" -ForegroundColor Red
    exit 1
}

# 2. 检查microservices-common
Write-Host "[2] 检查microservices-common..." -ForegroundColor Yellow
$commonPath = "$ProjectRoot\microservices\microservices-common"
if (Test-Path $commonPath) {
    Write-Host "  ✓ 目录存在: $commonPath" -ForegroundColor Green
} else {
    Write-Host "  ❌ 目录不存在: $commonPath" -ForegroundColor Red
    exit 1
}

# 3. 检查依赖服务
Write-Host "[3] 检查依赖服务..." -ForegroundColor Yellow

function Test-Port {
    param([int]$Port)
    try {
        $result = Test-NetConnection -ComputerName localhost -Port $Port -InformationLevel Quiet -WarningAction SilentlyContinue
        return $result
    } catch {
        return $false
    }
}

$nacosRunning = Test-Port -Port 8848
$mysqlRunning = Test-Port -Port 3306
$redisRunning = Test-Port -Port 6379

Write-Host "  Nacos (8848): $(if ($nacosRunning) { '✓ 运行中' } else { '✗ 未运行' })" -ForegroundColor $(if ($nacosRunning) { 'Green' } else { 'Red' })
Write-Host "  MySQL (3306): $(if ($mysqlRunning) { '✓ 运行中' } else { '✗ 未运行' })" -ForegroundColor $(if ($mysqlRunning) { 'Green' } else { 'Red' })
Write-Host "  Redis (6379): $(if ($redisRunning) { '✓ 运行中' } else { '✗ 未运行' })" -ForegroundColor $(if ($redisRunning) { 'Green' } else { 'Red' })

# 4. 检查服务目录
Write-Host "[4] 检查服务目录..." -ForegroundColor Yellow
$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-consume-service",
    "ioedream-visitor-service"
)

$allServicesExist = $true
foreach ($service in $services) {
    $servicePath = "$ProjectRoot\microservices\$service"
    if (Test-Path $servicePath) {
        Write-Host "  ✓ $service" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $service (不存在)" -ForegroundColor Red
        $allServicesExist = $false
    }
}

Write-Host ""
if ($allServicesExist) {
    Write-Host "✅ 所有检查通过，可以启动服务" -ForegroundColor Green
    Write-Host ""
    Write-Host "运行以下命令启动服务:" -ForegroundColor Cyan
    Write-Host "  .\scripts\start-all-complete.ps1 -BackendOnly" -ForegroundColor White
} else {
    Write-Host "❌ 部分服务目录不存在，请检查" -ForegroundColor Red
    exit 1
}

