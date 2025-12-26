# Maven依赖修复脚本
# 基于分析结果自动修复Maven依赖问题

Write-Host "🔧 开始Maven依赖修复..." -ForegroundColor Green

$servicesPath = "D:\IOE-DREAM\microservices"
$fixCount = 0

# Resilience4j依赖优化模式
$resilience4jPattern = @"
    <!-- Resilience4j (容错机制) -->
    <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-spring-boot3</artifactId>
    </dependency>
"@

# 需要修复的服务及其问题
$fixesNeeded = @{
    "ioedream-access-service" = @("resilience4j-duplicate")
    "ioedream-attendance-service" = @("resilience4j-duplicate")
    "ioedream-consume-service" = @("resilience4j-duplicate", "remove-versions")
    "ioedream-video-service" = @("resilience4j-duplicate")
    "ioedream-visitor-service" = @("resilience4j-duplicate", "remove-versions")
    "ioedream-device-comm-service" = @("resilience4j-duplicate", "remove-versions")
}

function Fix-Resilience4jDuplicates {
    param([string]$Content)

    # 移除重复的Resilience4j依赖，只保留主要的spring-boot3依赖
    $pattern = "(?s)<!-- Resilience4j \(容错机制\) -->.*?(?=<!--|$)"
    $replacement = $resilience4jPattern

    return $Content -replace $pattern, $replacement
}

function Remove-UnnecessaryVersions {
    param([string]$Content)

    # 移除不必要的版本声明（这些依赖已在父POM中管理）
    $dependenciesToFix = @(
        "mybatis-plus-spring-boot3-starter",
        "druid-spring-boot-3-starter",
        "lombok",
        "junit-jupiter",
        "mockito-core",
        "mockito-junit-jupiter",
        "mysql-connector-j"
    )

    foreach ($dep in $dependenciesToFix) {
        # 匹配并移除版本声明
        $pattern = "(<artifactId>$dep</artifactId>`s*<version>.*?</version>)"
        $replacement = "<artifactId>$dep</artifactId>"
        $Content = $Content -replace $pattern, $replacement
    }

    return $Content
}

# 修复每个服务
foreach ($service in $fixesNeeded.Keys) {
    $servicePath = Join-Path $servicesPath $service
    $pomPath = Join-Path $servicePath "pom.xml"

    if (Test-Path $pomPath) {
        Write-Host "`n🔧 修复服务: $service" -ForegroundColor Cyan

        $originalContent = Get-Content -Path $pomPath -Raw -Encoding UTF8
        $fixedContent = $originalContent
        $serviceFixCount = 0

        # 应用Resilience4j修复
        if ("resilience4j-duplicate" -in $fixesNeeded[$service]) {
            $fixedContent = Fix-Resilience4jDuplicates -Content $fixedContent
            Write-Host "  ✅ 修复Resilience4j重复依赖" -ForegroundColor Green
            $serviceFixCount++
        }

        # 移除不必要的版本声明
        if ("remove-versions" -in $fixesNeeded[$service]) {
            $fixedContent = Remove-UnnecessaryVersions -Content $fixedContent
            Write-Host "  ✅ 移除冗余版本声明" -ForegroundColor Green
            $serviceFixCount++
        }

        # 如果有修改，保存文件
        if ($fixedContent -ne $originalContent) {
            [System.IO.File]::WriteAllText($pomPath, $fixedContent, [System.Text.Encoding]::UTF8)
            Write-Host "  📝 已保存修复: $serviceFixCount 个问题" -ForegroundColor Yellow
            $fixCount += $serviceFixCount
        } else {
            Write-Host "  ℹ️ 无需修复" -ForegroundColor Blue
        }
    }
}

Write-Host "`n📊 Maven依赖修复完成!" -ForegroundColor Magenta
Write-Host "  修复问题数: $fixCount" -ForegroundColor Green

# 验证修复结果
Write-Host "`n🔍 验证修复结果..." -ForegroundColor Green
$verificationPassed = $true

foreach ($service in $fixesNeeded.Keys) {
    $servicePath = Join-Path $servicesPath $service
    $pomPath = Join-Path $servicePath "pom.xml"

    if (Test-Path $pomPath) {
        $pomContent = Get-Content -Path $pomPath -Raw -Encoding UTF8
        $issuesFound = @()

        # 检查是否还有版本硬编码
        if ($pomContent -match "<version>\$\{mybatis-plus\.version\}</version>") {
            $issuesFound += "版本硬编码: mybatis-plus"
        }
        if ($pomContent -match "<version>\$\{druid\.version\}</version>") {
            $issuesFound += "版本硬编码: druid"
        }
        if ($pomContent -match "<version>\$\{lombok\.version\}</version>") {
            $issuesFound += "版本硬编码: lombok"
        }

        # 检查是否还有重复的Resilience4j依赖
        $resilience4jCount = [regex]::Matches($pomContent, "resilience4j-\w+").Count
        if ($resilience4jCount -gt 1) {
            $issuesFound += "Resilience4j重复依赖: $resilience4jCount 个"
        }

        if ($issuesFound.Count -gt 0) {
            Write-Host "  ❌ ${service}: $($issuesFound -join ', ')" -ForegroundColor Red
            $verificationPassed = $false
        } else {
            Write-Host "  ✅ ${service}: 验证通过" -ForegroundColor Green
        }
    }
}

if ($verificationPassed) {
    Write-Host "`n🎉 所有服务验证通过！" -ForegroundColor Green
    Write-Host "✅ Maven依赖架构优化完成" -ForegroundColor Green
    Write-Host "✅ 版本管理统一化" -ForegroundColor Green
    Write-Host "✅ 重复依赖清理完成" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ 部分服务验证失败，需要手动检查" -ForegroundColor Yellow
}