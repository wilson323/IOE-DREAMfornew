# IOE-DREAM 本地微服务启动脚本
# 用途: 在本地运行所有微服务(不使用Docker容器)
# 前提: Docker中MySQL、Redis、Nacos已启动

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 本地微服务启动脚本" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 检查Java版本
Write-Host "[1/5] 检查Java环境..." -ForegroundColor Yellow
$javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_ -replace '.*"(.*)".*','$1' }
if ($javaVersion -match "^17") {
    Write-Host "✓ Java版本: $javaVersion" -ForegroundColor Green
} else {
    Write-Host "✗ 需要Java 17，当前版本: $javaVersion" -ForegroundColor Red
    exit 1
}

# 检查Docker基础设施
Write-Host "`n[2/5] 检查Docker基础设施..." -ForegroundColor Yellow
$containers = docker ps --filter "name=ioedream" --format "{{.Names}}" 2>$null

$requiredContainers = @("ioedream-mysql", "ioedream-redis", "ioedream-nacos")
$missingContainers = @()

foreach ($container in $requiredContainers) {
    if ($containers -contains $container) {
        Write-Host "✓ $container 运行中" -ForegroundColor Green
    } else {
        $missingContainers += $container
        Write-Host "✗ $container 未运行" -ForegroundColor Red
    }
}

if ($missingContainers.Count -gt 0) {
    Write-Host "`n需要先启动Docker基础设施:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose-all.yml up -d mysql redis nacos" -ForegroundColor Cyan
    exit 1
}

# 构建JAR包
Write-Host "`n[3/5] 构建微服务JAR包..." -ForegroundColor Yellow
Push-Location "microservices"

Write-Host "开始Maven构建..." -ForegroundColor Cyan
mvn clean package -DskipTests -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Maven构建失败" -ForegroundColor Red
    Pop-Location
    exit 1
}

Write-Host "✓ Maven构建成功" -ForegroundColor Green
Pop-Location

# 验证JAR包
Write-Host "`n[4/5] 验证JAR包..." -ForegroundColor Yellow
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

$missingJars = @()
foreach ($service in $services) {
    $jarPath = "microservices\$service\target\$service-1.0.0.jar"
    if (Test-Path $jarPath) {
        $size = (Get-Item $jarPath).Length / 1MB
        Write-Host "✓ $service.jar ($([math]::Round($size, 2)) MB)" -ForegroundColor Green
    } else {
        $missingJars += $service
        Write-Host "✗ $service.jar 不存在" -ForegroundColor Red
    }
}

if ($missingJars.Count -gt 0) {
    Write-Host "`n构建失败的服务: $($missingJars -join ', ')" -ForegroundColor Red
    exit 1
}

# 启动说明
Write-Host "`n[5/5] 启动微服务" -ForegroundColor Yellow
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "微服务启动方式 (选择一种):" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

Write-Host "`n方式1: 使用Maven (推荐用于开发调试)" -ForegroundColor Yellow
Write-Host "打开多个终端窗口,分别执行:" -ForegroundColor White
Write-Host ""
Write-Host "  # 终端1: Gateway Service" -ForegroundColor Gray
Write-Host "  cd microservices\ioedream-gateway-service" -ForegroundColor Cyan
Write-Host "  mvn spring-boot:run" -ForegroundColor Cyan
Write-Host ""
Write-Host "  # 终端2: Common Service" -ForegroundColor Gray
Write-Host "  cd microservices\ioedream-common-service" -ForegroundColor Cyan
Write-Host "  mvn spring-boot:run" -ForegroundColor Cyan
Write-Host ""
Write-Host "  # 终端3-10: 其他服务..." -ForegroundColor Gray
Write-Host ""

Write-Host "方式2: 直接运行JAR包" -ForegroundColor Yellow
Write-Host "打开多个终端窗口,分别执行:" -ForegroundColor White
Write-Host ""
Write-Host "  # 终端1: Gateway Service (端口8080)" -ForegroundColor Gray
Write-Host "  java -jar microservices\ioedream-gateway-service\target\ioedream-gateway-service-1.0.0.jar" -ForegroundColor Cyan
Write-Host ""
Write-Host "  # 终端2: Common Service (端口8088)" -ForegroundColor Gray
Write-Host "  java -jar microservices\ioedream-common-service\target\ioedream-common-service-1.0.0.jar" -ForegroundColor Cyan
Write-Host ""
Write-Host "  # 终端3-10: 其他服务..." -ForegroundColor Gray
Write-Host ""

Write-Host "方式3: 使用IDE (最便捷)" -ForegroundColor Yellow
Write-Host "  1. 在IntelliJ IDEA或VSCode中打开项目" -ForegroundColor White
Write-Host "  2. 找到各服务的主类 (XxxApplication.java)" -ForegroundColor White
Write-Host "  3. 右键点击 -> Run/Debug" -ForegroundColor White
Write-Host ""

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "服务端口分配:" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Gateway Service:      8080" -ForegroundColor White
Write-Host "  Common Service:       8088" -ForegroundColor White
Write-Host "  Device Comm Service:  8087" -ForegroundColor White
Write-Host "  OA Service:           8089" -ForegroundColor White
Write-Host "  Access Service:       8090" -ForegroundColor White
Write-Host "  Attendance Service:   8091" -ForegroundColor White
Write-Host "  Video Service:        8092" -ForegroundColor White
Write-Host "  Consume Service:      8094" -ForegroundColor White
Write-Host "  Visitor Service:      8095" -ForegroundColor White
Write-Host ""

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "健康检查:" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Nacos:    http://localhost:8848/nacos" -ForegroundColor White
Write-Host "  Gateway:  http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host "  Common:   http://localhost:8088/actuator/health" -ForegroundColor White
Write-Host ""

Write-Host "✓ 准备完成! 请选择一种方式启动微服务。" -ForegroundColor Green
Write-Host ""
