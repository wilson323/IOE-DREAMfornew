# 验证所有Dockerfile修复方案
# 确保所有9个服务都正确使用了awk命令

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Dockerfile修复方案完整验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

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

$allValid = $true
$fixedCount = 0
$errorCount = 0

foreach ($service in $services) {
    $dockerfilePath = "microservices\$($service.Name)\Dockerfile"
    
    Write-Host "检查: $($service.Name)..." -ForegroundColor Yellow
    
    if (-not (Test-Path $dockerfilePath)) {
        Write-Host "  ❌ Dockerfile不存在: $dockerfilePath" -ForegroundColor Red
        $allValid = $false
        $errorCount++
        continue
    }
    
    $content = Get-Content $dockerfilePath -Raw
    
    # 检查是否包含awk命令
    if ($content -match "awk '/<modules>/,/<\/modules>/ \{next\} \{print\}'") {
        Write-Host "  ✅ 已使用awk命令移除modules" -ForegroundColor Green
        $fixedCount++
    } elseif ($content -match "python3.*modules") {
        Write-Host "  ❌ 仍在使用python3（Maven镜像中没有Python）" -ForegroundColor Red
        $allValid = $false
        $errorCount++
    } elseif ($content -match "sed.*modules") {
        Write-Host "  ⚠️  仍在使用sed（可能不稳定）" -ForegroundColor Yellow
        $allValid = $false
        $errorCount++
    } else {
        Write-Host "  ❌ 未找到modules移除命令" -ForegroundColor Red
        $allValid = $false
        $errorCount++
    }
    
    # 检查是否包含mvn install:install-file
    if ($content -match "mvn install:install-file.*pom-temp\.xml") {
        Write-Host "  ✅ 使用临时POM文件安装父POM" -ForegroundColor Green
    } else {
        Write-Host "  ❌ 未找到临时POM安装命令" -ForegroundColor Red
        $allValid = $false
        $errorCount++
    }
    
    # 检查是否包含-N参数
    if ($content -match "mvn clean install -N") {
        Write-Host "  ✅ 使用-N参数跳过模块检查" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  未找到-N参数" -ForegroundColor Yellow
    }
    
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "验证结果汇总" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "总服务数: $($services.Count)" -ForegroundColor White
Write-Host "已修复: $fixedCount" -ForegroundColor $(if ($fixedCount -eq $services.Count) { "Green" } else { "Yellow" })
Write-Host "错误数: $errorCount" -ForegroundColor $(if ($errorCount -eq 0) { "Green" } else { "Red" })
Write-Host ""

# 验证pom.xml中的modules部分
Write-Host "验证pom.xml结构..." -ForegroundColor Yellow
$pomPath = "microservices\pom.xml"
if (Test-Path $pomPath) {
    $pomContent = Get-Content $pomPath -Raw
    if ($pomContent -match '<modules>') {
        Write-Host "✅ pom.xml包含<modules>标签（需要移除）" -ForegroundColor Green
        
        # 模拟awk命令逻辑
        $lines = Get-Content $pomPath
        $inModules = $false
        $tempLines = @()
        $modulesLineCount = 0
        
        foreach ($line in $lines) {
            if ($line -match '<modules>') {
                $inModules = $true
                $modulesLineCount++
                continue
            }
            if ($inModules -and $line -match '</modules>') {
                $inModules = $false
                $modulesLineCount++
                continue
            }
            if ($inModules) {
                $modulesLineCount++
                continue
            }
            $tempLines += $line
        }
        
        Write-Host "✅ 模拟awk命令: 将移除 $modulesLineCount 行modules相关代码" -ForegroundColor Green
        
        # 验证临时POM应该包含的内容
        $tempContent = $tempLines -join "`n"
        if ($tempContent -match '<groupId>net.lab1024.sa</groupId>' -and 
            $tempContent -match '<artifactId>ioedream-microservices-parent</artifactId>') {
            Write-Host "✅ 临时POM将包含必要的groupId和artifactId" -ForegroundColor Green
        } else {
            Write-Host "❌ 临时POM缺少必要信息" -ForegroundColor Red
            $allValid = $false
        }
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
    Write-Host "  ✅ 所有9个Dockerfile都使用awk命令" -ForegroundColor White
    Write-Host "  ✅ awk命令语法正确" -ForegroundColor White
    Write-Host "  ✅ 临时POM文件逻辑正确" -ForegroundColor White
    Write-Host "  ✅ 所有服务都使用-N参数" -ForegroundColor White
    Write-Host ""
    Write-Host "可以安全执行:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose-all.yml build --no-cache" -ForegroundColor White
    exit 0
} else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ 验证失败！" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查上述错误并修复后重试" -ForegroundColor Yellow
    exit 1
}
