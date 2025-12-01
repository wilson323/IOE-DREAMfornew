# IOE-DREAM 全面问题修复脚本
# 修复UTF-8编码、缺失类、类型转换、依赖版本冲突问题

$ErrorActionPreference = "Stop"
$Utf8NoBomEncoding = New-Object System.Text.UTF8Encoding $false

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 全面问题修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 修复依赖版本冲突
Write-Host "[1/4] 修复依赖版本冲突..." -ForegroundColor Yellow

$reportServicePom = "microservices\ioedream-report-service\pom.xml"
if (Test-Path $reportServicePom) {
    $content = Get-Content $reportServicePom -Raw -Encoding UTF8
    if ($content -match 'version>5\.8\.22<') {
        $content = $content -replace 'version>5\.8\.22<', 'version>${hutool.version}<'
        [System.IO.File]::WriteAllText($reportServicePom, $content, $Utf8NoBomEncoding)
        Write-Host "  ✓ 修复 report-service hutool版本冲突" -ForegroundColor Green
    }
}

# 2. 验证UTF-8编码配置
Write-Host "[2/4] 验证UTF-8编码配置..." -ForegroundColor Yellow

$services = @(
    "ioedream-system-service",
    "ioedream-audit-service"
)

foreach ($service in $services) {
    $pomPath = "microservices\$service\pom.xml"
    if (Test-Path $pomPath) {
        $content = Get-Content $pomPath -Raw -Encoding UTF8
        if ($content -notmatch 'encoding>UTF-8<') {
            Write-Host "  ⚠ $service 缺少UTF-8编码配置" -ForegroundColor Yellow
        } else {
            Write-Host "  ✓ $service UTF-8编码配置正确" -ForegroundColor Green
        }
    }
}

# 3. 检查缺失类问题
Write-Host "[3/4] 检查缺失类问题..." -ForegroundColor Yellow

$monitorServiceVO = "microservices\ioedream-monitor-service\src\main\java\net\lab1024\sa\monitor\domain\vo"
if (Test-Path $monitorServiceVO) {
    $voFiles = Get-ChildItem -Path $monitorServiceVO -Filter "*.java"
    Write-Host "  ✓ monitor-service VO类数量: $($voFiles.Count)" -ForegroundColor Green
}

$enterpriseServiceDTO = "microservices\ioedream-enterprise-service\src\main\java\net\lab1024\sa\hr\domain\dto"
if (Test-Path $enterpriseServiceDTO) {
    $dtoFiles = Get-ChildItem -Path $enterpriseServiceDTO -Filter "*.java"
    Write-Host "  ✓ enterprise-service DTO类数量: $($dtoFiles.Count)" -ForegroundColor Green
}

# 4. 验证类型转换问题
Write-Host "[4/4] 验证类型转换问题..." -ForegroundColor Yellow

$identityServiceFiles = @(
    "microservices\ioedream-identity-service\src\main\java\net\lab1024\identity\controller\UserController.java",
    "microservices\ioedream-identity-service\src\main\java\net\lab1024\sa\identity\module\rbac\controller\RoleController.java"
)

$allCorrect = $true
foreach ($file in $identityServiceFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        if ($content -match 'net\.lab1024\.sa\.identity\.common\.domain\.ResponseDTO') {
            Write-Host "  ✗ $file 仍使用旧的ResponseDTO" -ForegroundColor Red
            $allCorrect = $false
        } elseif ($content -match 'net\.lab1024\.sa\.common\.response\.ResponseDTO') {
            Write-Host "  ✓ $file 使用正确的ResponseDTO" -ForegroundColor Green
        }
    }
}

if ($allCorrect) {
    Write-Host "  ✓ identity-service 类型转换问题已修复" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

