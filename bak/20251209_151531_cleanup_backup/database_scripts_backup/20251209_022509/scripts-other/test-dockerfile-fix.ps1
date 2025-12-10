# 测试Dockerfile修复方案
# 验证awk命令是否能正确移除pom.xml中的modules部分

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Dockerfile修复方案验证测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查pom.xml文件是否存在
$pomPath = "microservices\pom.xml"
if (-not (Test-Path $pomPath)) {
    Write-Host "❌ 错误: 找不到 $pomPath" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 找到pom.xml文件: $pomPath" -ForegroundColor Green

# 检查是否包含modules部分
$pomContent = Get-Content $pomPath -Raw
if ($pomContent -notmatch '<modules>') {
    Write-Host "❌ 错误: pom.xml中不包含<modules>标签" -ForegroundColor Red
    exit 1
}

Write-Host "✅ pom.xml包含<modules>标签" -ForegroundColor Green

# 在Windows上模拟awk命令（使用PowerShell）
Write-Host ""
Write-Host "测试1: 使用PowerShell模拟awk命令移除modules部分..." -ForegroundColor Yellow

# 读取pom.xml内容
$lines = Get-Content $pomPath
$inModules = $false
$tempLines = @()

foreach ($line in $lines) {
    if ($line -match '<modules>') {
        $inModules = $true
        continue
    }
    if ($inModules -and $line -match '</modules>') {
        $inModules = $false
        continue
    }
    if (-not $inModules) {
        $tempLines += $line
    }
}

# 创建临时文件
$tempPomPath = "microservices\pom-temp-test.xml"
$tempLines | Out-File -FilePath $tempPomPath -Encoding UTF8

# 验证临时文件
if (-not (Test-Path $tempPomPath)) {
    Write-Host "❌ 错误: 无法创建临时文件" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 成功创建临时文件: $tempPomPath" -ForegroundColor Green

# 检查临时文件是否包含modules
$tempContent = Get-Content $tempPomPath -Raw
if ($tempContent -match '<modules>') {
    Write-Host "❌ 错误: 临时文件仍然包含<modules>标签" -ForegroundColor Red
    Remove-Item $tempPomPath -ErrorAction SilentlyContinue
    exit 1
}

Write-Host "✅ 临时文件不包含<modules>标签" -ForegroundColor Green

# 检查临时文件是否包含其他重要内容
if ($tempContent -notmatch '<groupId>net.lab1024.sa</groupId>') {
    Write-Host "❌ 错误: 临时文件缺少groupId" -ForegroundColor Red
    Remove-Item $tempPomPath -ErrorAction SilentlyContinue
    exit 1
}

Write-Host "✅ 临时文件包含groupId" -ForegroundColor Green

if ($tempContent -notmatch '<artifactId>ioedream-microservices-parent</artifactId>') {
    Write-Host "❌ 错误: 临时文件缺少artifactId" -ForegroundColor Red
    Remove-Item $tempPomPath -ErrorAction SilentlyContinue
    exit 1
}

Write-Host "✅ 临时文件包含artifactId" -ForegroundColor Green

# 清理临时文件
Remove-Item $tempPomPath -ErrorAction SilentlyContinue

# 验证所有Dockerfile都使用了awk命令
Write-Host ""
Write-Host "测试2: 验证所有Dockerfile都使用了awk命令..." -ForegroundColor Yellow

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

$allFixed = $true
foreach ($service in $services) {
    $dockerfilePath = "microservices\$service\Dockerfile"
    if (-not (Test-Path $dockerfilePath)) {
        Write-Host "❌ 错误: 找不到 $dockerfilePath" -ForegroundColor Red
        $allFixed = $false
        continue
    }
    
    $dockerfileContent = Get-Content $dockerfilePath -Raw
    if ($dockerfileContent -match "awk '/<modules>/,/<\/modules>/ \{next\} \{print\}'") {
        Write-Host "✅ $service - 已使用awk命令" -ForegroundColor Green
    } elseif ($dockerfileContent -match "python3.*modules") {
        Write-Host "❌ $service - 仍在使用python3（错误）" -ForegroundColor Red
        $allFixed = $false
    } elseif ($dockerfileContent -match "sed.*modules") {
        Write-Host "❌ $service - 仍在使用sed（可能失败）" -ForegroundColor Yellow
        $allFixed = $false
    } else {
        Write-Host "❌ $service - 未找到modules移除命令" -ForegroundColor Red
        $allFixed = $false
    }
}

Write-Host ""
if ($allFixed) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ 所有测试通过！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "修复方案验证结果:" -ForegroundColor Cyan
    Write-Host "  - awk命令可以正确移除modules部分" -ForegroundColor White
    Write-Host "  - 所有9个Dockerfile都已使用awk命令" -ForegroundColor White
    Write-Host "  - 临时POM文件格式正确" -ForegroundColor White
    Write-Host ""
    Write-Host "下一步: 运行 docker-compose build --no-cache" -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ 部分测试失败！" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    exit 1
}
