# 最终验证所有Dockerfile修复
# 确保所有9个服务都正确使用了V5方案（直接替换pom.xml）

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Dockerfile修复最终验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

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

$allValid = $true
$fixedCount = 0
$errorCount = 0

Write-Host "检查所有Dockerfile..." -ForegroundColor Yellow
Write-Host ""

foreach ($service in $services) {
    $dockerfilePath = "microservices\$service\Dockerfile"
    
    if (-not (Test-Path $dockerfilePath)) {
        Write-Host "  ❌ $service - Dockerfile不存在" -ForegroundColor Red
        $allValid = $false
        $errorCount++
        continue
    }
    
    $content = Get-Content $dockerfilePath -Raw
    
    # 检查V5方案的关键特征
    $hasBackup = $content -match "cp pom\.xml pom-original\.xml"
    $hasReplace = $content -match "awk '/<modules>/,/<\/modules>/ \{next\} \{print\}' pom-original\.xml > pom\.xml"
    $hasInstall = $content -match "mvn install:install-file -Dfile=pom\.xml"
    $hasNFlag = $content -match "mvn clean install -N" -or $content -match "mvn clean package -N"
    
    # 检查是否还有旧的方案
    $hasPython = $content -match "python3.*modules"
    $hasTempFile = $content -match "pom-temp\.xml" -and -not $hasReplace
    
    if ($hasBackup -and $hasReplace -and $hasInstall -and $hasNFlag) {
        Write-Host "  ✅ $service - V5方案完整" -ForegroundColor Green
        $fixedCount++
    } elseif ($hasPython) {
        Write-Host "  ❌ $service - 仍使用python3（错误）" -ForegroundColor Red
        $allValid = $false
        $errorCount++
    } elseif ($hasTempFile) {
        Write-Host "  ❌ $service - 仍使用临时文件方案（V4，已过时）" -ForegroundColor Red
        $allValid = $false
        $errorCount++
    } else {
        Write-Host "  ⚠️  $service - 修复不完整" -ForegroundColor Yellow
        if (-not $hasBackup) { Write-Host "     - 缺少备份命令" -ForegroundColor Yellow }
        if (-not $hasReplace) { Write-Host "     - 缺少直接替换命令" -ForegroundColor Yellow }
        if (-not $hasInstall) { Write-Host "     - 缺少安装命令" -ForegroundColor Yellow }
        if (-not $hasNFlag) { Write-Host "     - 缺少-N参数" -ForegroundColor Yellow }
        $allValid = $false
        $errorCount++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "验证结果汇总" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "总服务数: $($services.Count)" -ForegroundColor White
Write-Host "已修复: $fixedCount" -ForegroundColor $(if ($fixedCount -eq $services.Count) { "Green" } else { "Yellow" })
Write-Host "错误数: $errorCount" -ForegroundColor $(if ($errorCount -eq 0) { "Green" } else { "Red" })
Write-Host ""

# 验证pom.xml结构
Write-Host "验证pom.xml结构..." -ForegroundColor Yellow
$pomPath = "microservices\pom.xml"
if (Test-Path $pomPath) {
    $pomContent = Get-Content $pomPath -Raw
    if ($pomContent -match '<modules>') {
        Write-Host "✅ pom.xml包含<modules>标签（需要移除）" -ForegroundColor Green
        
        # 计算modules部分的行数
        $lines = Get-Content $pomPath
        $inModules = $false
        $modulesLineCount = 0
        
        foreach ($line in $lines) {
            if ($line -match '<modules>') {
                $inModules = $true
                $modulesLineCount++
            } elseif ($inModules -and $line -match '</modules>') {
                $inModules = $false
                $modulesLineCount++
            } elseif ($inModules) {
                $modulesLineCount++
            }
        }
        
        Write-Host "✅ modules部分包含 $modulesLineCount 行（将被移除）" -ForegroundColor Green
    } else {
        Write-Host "⚠️  pom.xml不包含<modules>标签" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ 找不到pom.xml文件" -ForegroundColor Red
    $allValid = $false
}

Write-Host ""

if ($allValid -and $fixedCount -eq $services.Count) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ 所有验证通过！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "修复方案验证结果:" -ForegroundColor Cyan
    Write-Host "  ✅ 所有9个Dockerfile都使用V5方案（直接替换pom.xml）" -ForegroundColor White
    Write-Host "  ✅ 所有Dockerfile都备份原始文件" -ForegroundColor White
    Write-Host "  ✅ 所有Dockerfile都使用awk命令" -ForegroundColor White
    Write-Host "  ✅ 所有Dockerfile都使用-N参数" -ForegroundColor White
    Write-Host "  ✅ 无python3引用" -ForegroundColor White
    Write-Host "  ✅ 无临时文件方案（V4）" -ForegroundColor White
    Write-Host ""
    Write-Host "可以安全执行:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose-all.yml build --no-cache" -ForegroundColor White
    Write-Host ""
    exit 0
} else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ 验证失败！" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查上述错误并修复后重试" -ForegroundColor Yellow
    exit 1
}
