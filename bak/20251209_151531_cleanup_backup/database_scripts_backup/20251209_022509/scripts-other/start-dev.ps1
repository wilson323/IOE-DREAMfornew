# ================================================================================
# IOE-DREAM 开发环境启动脚本
# ================================================================================
# 功能：按正确顺序启动后端网关 → 前端服务
# ================================================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 开发环境启动" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1️⃣ 检查依赖服务
Write-Host "[1/4] 检查依赖服务..." -ForegroundColor Yellow

# 检查MySQL
$mysqlRunning = docker ps --filter "name=ioedream-mysql" --format "{{.Names}}"
if ($mysqlRunning -ne "ioedream-mysql") {
    Write-Host "❌ MySQL未运行，启动中..." -ForegroundColor Red
    docker-compose -f docker-compose-services.yml up -d mysql
    Start-Sleep -Seconds 10
} else {
    Write-Host "✅ MySQL运行中" -ForegroundColor Green
}

# 检查Redis
$redisRunning = docker ps --filter "name=ioedream-redis" --format "{{.Names}}"
if ($redisRunning -ne "ioedream-redis") {
    Write-Host "❌ Redis未运行，启动中..." -ForegroundColor Red
    docker-compose -f docker-compose-services.yml up -d redis
    Start-Sleep -Seconds 5
} else {
    Write-Host "✅ Redis运行中" -ForegroundColor Green
}

# 2️⃣ 启动后端网关
Write-Host "`n[2/4] 启动后端网关服务..." -ForegroundColor Yellow
Set-Location -Path "d:\IOE-DREAM\microservices\ioedream-gateway-service"

$gatewayProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c mvn spring-boot:run -DskipTests" -PassThru -WindowStyle Normal
Write-Host "✅ 网关服务启动中... PID: $($gatewayProcess.Id)" -ForegroundColor Green

# 3️⃣ 等待网关健康检查
Write-Host "`n[3/4] 等待网关服务就绪..." -ForegroundColor Yellow
$maxRetry = 30
$retryCount = 0
$gatewayReady = $false

while ($retryCount -lt $maxRetry -and -not $gatewayReady) {
    try {
        $response = Invoke-WebRequest -Uri "http://127.0.0.1:8080/actuator/health" -Method Get -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $gatewayReady = $true
            Write-Host "✅ 网关服务就绪！" -ForegroundColor Green
        }
    } catch {
        $retryCount++
        Write-Host "等待网关启动... ($retryCount/$maxRetry)" -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
}

if (-not $gatewayReady) {
    Write-Host "❌ 网关启动超时，请检查日志" -ForegroundColor Red
    exit 1
}

# 4️⃣ 启动前端服务
Write-Host "`n[4/4] 启动前端服务..." -ForegroundColor Yellow
Set-Location -Path "d:\IOE-DREAM\smart-admin-web-javascript"

$frontendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c npm run dev" -PassThru -WindowStyle Normal
Write-Host "✅ 前端服务启动中... PID: $($frontendProcess.Id)" -ForegroundColor Green

# 5️⃣ 启动完成
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "🚀 IOE-DREAM 开发环境启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📱 前端访问: http://localhost:8081" -ForegroundColor Yellow
Write-Host "🌐 网关API: http://localhost:8080" -ForegroundColor Yellow
Write-Host "📊 监控面板: http://localhost:8080/actuator" -ForegroundColor Yellow
Write-Host "`n💡 按 Ctrl+C 停止所有服务" -ForegroundColor Gray
Write-Host "========================================`n" -ForegroundColor Cyan

# 等待用户中断
try {
    while ($true) {
        Start-Sleep -Seconds 1
    }
} finally {
    Write-Host "`n🛑 停止服务..." -ForegroundColor Yellow
    Stop-Process -Id $gatewayProcess.Id -Force -ErrorAction SilentlyContinue
    Stop-Process -Id $frontendProcess.Id -Force -ErrorAction SilentlyContinue
    Write-Host "✅ 服务已停止" -ForegroundColor Green
}
