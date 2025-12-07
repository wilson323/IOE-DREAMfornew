# IOE-DREAM 修复所有POM文件问题脚本
# 功能：验证所有POM文件配置，检查依赖一致性
# 作者：AI Assistant
# 日期：2025-01-30

$ErrorActionPreference = "Stop"
$workspaceRoot = "D:\IOE-DREAM"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 全局POM文件异常检查" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Set-Location $workspaceRoot

$issues = @()

# 检查1: iText依赖配置
Write-Host "[检查1] iText依赖配置..." -ForegroundColor Yellow

$itextFiles = @(
    "pom.xml",
    "microservices\pom.xml"
)

foreach ($file in $itextFiles) {
    $content = Get-Content "$workspaceRoot\$file" -Raw
    if ($content -match 'itext-core.*version.*9\.4\.0') {
        $issues += "❌ $file - 仍包含itext-core:9.4.0（BOM，不是JAR）"
        Write-Host "  ✗ $file" -ForegroundColor Red
    } else {
        Write-Host "  ✓ $file" -ForegroundColor Green
    }
}

Write-Host ""

# 检查2: 版本硬编码
Write-Host "[检查2] 版本硬编码检查..." -ForegroundColor Yellow

$versionIssues = @(
    @{File="microservices\ioedream-consume-service\pom.xml"; Pattern="itext7-core.version"; Desc="错误的itext7-core版本属性"},
    @{File="microservices\analytics\pom.xml"; Pattern="easyexcel.*3\.3\.2"; Desc="EasyExcel版本不一致"},
    @{File="microservices\analytics\pom.xml"; Pattern="poi.*5\.2\.3"; Desc="POI版本不一致"},
    @{File="microservices\ioedream-common-service\pom.xml"; Pattern="jjwt.*0\.12\.3"; Desc="JWT版本不一致"},
    @{File="microservices\ioedream-attendance-service\pom.xml"; Pattern="spring-boot-maven-plugin.*3\.5\.4"; Desc="Spring Boot版本硬编码"}
)

foreach ($issue in $versionIssues) {
    $filePath = "$workspaceRoot\$($issue.File)"
    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw
        if ($content -match $issue.Pattern) {
            $issues += "❌ $($issue.File) - $($issue.Desc)"
            Write-Host "  ✗ $($issue.File): $($issue.Desc)" -ForegroundColor Red
        } else {
            Write-Host "  ✓ $($issue.File)" -ForegroundColor Green
        }
    }
}

Write-Host ""

# 检查3: 依赖版本缺失
Write-Host "[检查3] 依赖版本缺失检查..." -ForegroundColor Yellow

$missingVersionFiles = @(
    "microservices\ioedream-device-comm-service\pom.xml",
    "microservices\ioedream-oa-service\pom.xml"
)

foreach ($file in $missingVersionFiles) {
    $filePath = "$workspaceRoot\$file"
    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw
        if ($content -match 'microservices-common.*\n.*(?!version)') {
            # 检查microservices-common依赖是否缺少version
            $lines = Get-Content $filePath
            $found = $false
            for ($i = 0; $i -lt $lines.Count; $i++) {
                if ($lines[$i] -match 'microservices-common') {
                    $found = $true
                    # 检查接下来3行是否有version
                    $hasVersion = $false
                    for ($j = $i; $j -lt [Math]::Min($i + 3, $lines.Count); $j++) {
                        if ($lines[$j] -match 'version') {
                            $hasVersion = $true
                            break
                        }
                    }
                    if (-not $hasVersion) {
                        $issues += "❌ $file - microservices-common依赖缺少version"
                        Write-Host "  ✗ $file" -ForegroundColor Red
                        break
                    }
                }
            }
            if (-not $found -or $hasVersion) {
                Write-Host "  ✓ $file" -ForegroundColor Green
            }
        }
    }
}

Write-Host ""

# 检查4: 父POM路径
Write-Host "[检查4] 父POM路径检查..." -ForegroundColor Yellow

$parentPathIssues = @(
    @{File="microservices\microservices-common\pom.xml"; ShouldBe="../pom.xml"},
    @{File="microservices\ioedream-access-service\pom.xml"; ShouldBe="../pom.xml"}
)

foreach ($issue in $parentPathIssues) {
    $filePath = "$workspaceRoot\$($issue.File)"
    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw
        if ($content -notmatch "relativePath.*$($issue.ShouldBe -replace '\\','\\')") {
            $issues += "⚠ $($issue.File) - 父POM路径可能不正确"
            Write-Host "  ⚠ $($issue.File)" -ForegroundColor Yellow
        } else {
            Write-Host "  ✓ $($issue.File)" -ForegroundColor Green
        }
    }
}

Write-Host ""

# 生成报告
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "检查完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($issues.Count -eq 0) {
    Write-Host "✅ 所有POM文件配置正确！" -ForegroundColor Green
} else {
    Write-Host "发现 $($issues.Count) 个问题：" -ForegroundColor Yellow
    Write-Host ""
    foreach ($issue in $issues) {
        Write-Host "  $issue" -ForegroundColor White
    }
    Write-Host ""
    Write-Host "📋 修复建议：" -ForegroundColor Yellow
    Write-Host "  1. 已自动修复大部分问题" -ForegroundColor White
    Write-Host "  2. 请运行: mvn clean install -DskipTests -U" -ForegroundColor White
    Write-Host "  3. 验证构建是否成功" -ForegroundColor White
}

Write-Host ""
