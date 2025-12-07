# IOE-DREAM 项目结构修复和编译脚本
# 功能：诊断项目结构，修复缺失的POM，执行正确编译顺序
# 作者：IOE-DREAM Team
# 日期：2025-12-06

$ErrorActionPreference = "Continue"
$workspaceRoot = if ($PSScriptRoot) { Split-Path $PSScriptRoot -Parent } else { $PWD }

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 项目结构修复和编译脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ==================== 阶段1: 项目结构诊断 ====================
Write-Host "[阶段1] 项目结构诊断..." -ForegroundColor Yellow
Write-Host ""

# 1.1 检查根POM
$rootPom = Join-Path $workspaceRoot "pom.xml"
$hasRootPom = Test-Path $rootPom
if ($hasRootPom) {
    Write-Host "  ✓ 根POM存在: $rootPom" -ForegroundColor Green
    $rootPomContent = Get-Content $rootPom -Raw -Encoding UTF-8 -ErrorAction SilentlyContinue
    if ($rootPomContent -and $rootPomContent -match "<modules>") {
        Write-Host "  ✓ 包含模块声明" -ForegroundColor Green
    }
} else {
    Write-Host "  ✗ 根POM不存在" -ForegroundColor Red
}

# 1.2 检查microservices POM
$microservicesPom = Join-Path $workspaceRoot "microservices\pom.xml"
$hasMicroservicesPom = Test-Path $microservicesPom
if ($hasMicroservicesPom) {
    Write-Host "  ✓ microservices POM存在: $microservicesPom" -ForegroundColor Green
} else {
    Write-Host "  ✗ microservices POM不存在" -ForegroundColor Red
}

# 1.3 检查关键模块POM
$commonPom = Join-Path $workspaceRoot "microservices\microservices-common\pom.xml"
$servicePom = Join-Path $workspaceRoot "microservices\ioedream-common-service\pom.xml"

$hasCommonPom = Test-Path $commonPom
$hasServicePom = Test-Path $servicePom

Write-Host ""
Write-Host "  关键模块POM状态:" -ForegroundColor Gray
if ($hasCommonPom) {
    Write-Host "    ✓ microservices-common/pom.xml 存在" -ForegroundColor Green
} else {
    Write-Host "    ✗ microservices-common/pom.xml 缺失" -ForegroundColor Red
}

if ($hasServicePom) {
    Write-Host "    ✓ ioedream-common-service/pom.xml 存在" -ForegroundColor Green
} else {
    Write-Host "    ✗ ioedream-common-service/pom.xml 缺失" -ForegroundColor Red
}

Write-Host ""

# ==================== 阶段2: 确定构建策略 ====================
Write-Host "[阶段2] 确定构建策略..." -ForegroundColor Yellow

$buildStrategy = "unknown"

if ($hasRootPom -and $hasCommonPom -and $hasServicePom) {
    $buildStrategy = "from-root"
    Write-Host "  ✓ 策略: 从根POM统一构建" -ForegroundColor Green
    Write-Host "    命令: cd $workspaceRoot" -ForegroundColor Gray
    Write-Host "          mvn clean install -DskipTests -U" -ForegroundColor Gray
} elseif ($hasMicroservicesPom -and $hasCommonPom -and $hasServicePom) {
    $buildStrategy = "from-microservices"
    Write-Host "  ✓ 策略: 从microservices POM构建" -ForegroundColor Green
    Write-Host "    命令: cd $workspaceRoot\microservices" -ForegroundColor Gray
    Write-Host "          mvn clean install -DskipTests -U" -ForegroundColor Gray
} elseif (-not $hasCommonPom -or -not $hasServicePom) {
    $buildStrategy = "need-create-pom"
    Write-Host "  ⚠ 策略: 需要创建缺失的POM文件" -ForegroundColor Yellow
    Write-Host "    缺失文件:" -ForegroundColor Gray
    if (-not $hasCommonPom) {
        Write-Host "      - microservices/microservices-common/pom.xml" -ForegroundColor Red
    }
    if (-not $hasServicePom) {
        Write-Host "      - microservices/ioedream-common-service/pom.xml" -ForegroundColor Red
    }
} else {
    Write-Host "  ✗ 无法确定构建策略" -ForegroundColor Red
    Write-Host "    请检查项目结构" -ForegroundColor Yellow
}

Write-Host ""

# ==================== 阶段3: 执行构建 ====================
if ($buildStrategy -eq "from-root") {
    Write-Host "[阶段3] 从根POM构建..." -ForegroundColor Yellow
    Push-Location $workspaceRoot
    
    Write-Host "  执行: mvn clean install -DskipTests -U" -ForegroundColor Gray
    mvn clean install -DskipTests -U
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ 构建成功" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 构建失败" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    Pop-Location
    
} elseif ($buildStrategy -eq "from-microservices") {
    Write-Host "[阶段3] 从microservices POM构建..." -ForegroundColor Yellow
    $microservicesPath = Join-Path $workspaceRoot "microservices"
    Push-Location $microservicesPath
    
    Write-Host "  执行: mvn clean install -DskipTests -U" -ForegroundColor Gray
    mvn clean install -DskipTests -U
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ 构建成功" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 构建失败" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    Pop-Location
    
} elseif ($buildStrategy -eq "need-create-pom") {
    Write-Host "[阶段3] 需要创建POM文件..." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  ⚠ 检测到缺失的POM文件，需要手动创建" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  请参考以下文档创建POM文件:" -ForegroundColor Yellow
    Write-Host "    - documentation/technical/GLOBAL_PROJECT_STRUCTURE_ROOT_CAUSE_ANALYSIS.md" -ForegroundColor White
    Write-Host "    - documentation/technical/MODULE_LOCATION_FIX.md" -ForegroundColor White
    Write-Host ""
    Write-Host "  创建POM后，重新运行此脚本" -ForegroundColor Yellow
    exit 1
    
} else {
    Write-Host "[阶段3] 无法执行构建" -ForegroundColor Red
    Write-Host "  原因: 无法确定构建策略" -ForegroundColor Yellow
    Write-Host "  请检查项目结构并创建必要的POM文件" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# ==================== 阶段4: 验证构建结果 ====================
Write-Host "[阶段4] 验证构建结果..." -ForegroundColor Yellow

# 检查microservices-common是否已安装
$jarPath = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common"
$versionDirs = Get-ChildItem -Path $jarPath -Directory -ErrorAction SilentlyContinue
if ($versionDirs) {
    $latestVersion = $versionDirs | Sort-Object Name -Descending | Select-Object -First 1
    $jarFile = Join-Path $latestVersion.FullName "microservices-common-$($latestVersion.Name).jar"
    if (Test-Path $jarFile) {
        Write-Host "  ✓ microservices-common 已安装到本地仓库" -ForegroundColor Green
        Write-Host "    位置: $jarFile" -ForegroundColor Gray
    } else {
        Write-Host "  ⚠ microservices-common JAR文件未找到" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ✗ microservices-common 未安装到本地仓库" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "处理完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "如果仍有问题，请查看详细分析报告:" -ForegroundColor Yellow
Write-Host "  documentation\technical\GLOBAL_PROJECT_STRUCTURE_ROOT_CAUSE_ANALYSIS.md" -ForegroundColor White
