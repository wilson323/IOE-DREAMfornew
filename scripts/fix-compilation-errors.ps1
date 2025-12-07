# IOE-DREAM 编译错误修复脚本
# 功能：自动修复 IdentityServiceImpl 编译错误
# 作者：IOE-DREAM Team
# 日期：2025-12-06

$ErrorActionPreference = "Continue"
$workspaceRoot = if ($PSScriptRoot) { Split-Path $PSScriptRoot -Parent } else { $PWD }

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 编译错误修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 先编译 microservices-common（关键步骤！）
Write-Host "[步骤1/5] 编译 microservices-common 模块（必须先编译）..." -ForegroundColor Yellow
Write-Host "  原因: ioedream-common-service 依赖 microservices-common，必须先编译安装" -ForegroundColor Gray
$commonPath = Join-Path $workspaceRoot "microservices\microservices-common"
if (Test-Path $commonPath) {
    Push-Location $commonPath
    Write-Host "  执行: mvn clean install -DskipTests -U" -ForegroundColor Gray
    mvn clean install -DskipTests -U
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ microservices-common 构建成功" -ForegroundColor Green
        
        # 验证已安装到本地仓库
        $jarPath = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common"
        $versionDirs = Get-ChildItem -Path $jarPath -Directory -ErrorAction SilentlyContinue
        if ($versionDirs) {
            $latestVersion = $versionDirs | Sort-Object Name -Descending | Select-Object -First 1
            $jarFile = Join-Path $latestVersion.FullName "microservices-common-$($latestVersion.Name).jar"
            if (Test-Path $jarFile) {
                Write-Host "  ✓ 已安装到本地Maven仓库: $jarFile" -ForegroundColor Green
            } else {
                Write-Host "  ⚠ JAR文件未找到，但构建成功" -ForegroundColor Yellow
            }
        }
    } else {
        Write-Host "  ✗ microservices-common 构建失败" -ForegroundColor Red
        Write-Host "  错误: 必须先修复 microservices-common 的编译错误" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    Pop-Location
} else {
    Write-Host "  ✗ 找不到 microservices-common 目录" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 步骤2: 检查 ioedream-common-service 依赖
Write-Host "[步骤2/5] 检查 ioedream-common-service 依赖..." -ForegroundColor Yellow
$servicePath = Join-Path $workspaceRoot "microservices\ioedream-common-service"
$pomPath = Join-Path $servicePath "pom.xml"

if (Test-Path $pomPath) {
    $pomContent = Get-Content $pomPath -Raw -Encoding UTF-8
    
    # 检查是否有 microservices-common 依赖
    if ($pomContent -match "microservices-common") {
        Write-Host "  ✓ 已找到 microservices-common 依赖" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ 未找到 microservices-common 依赖，需要手动添加" -ForegroundColor Yellow
        Write-Host "    请在 pom.xml 的 <dependencies> 中添加:" -ForegroundColor Yellow
        Write-Host "    <dependency>" -ForegroundColor Gray
        Write-Host "        <groupId>net.lab1024.sa</groupId>" -ForegroundColor Gray
        Write-Host "        <artifactId>microservices-common</artifactId>" -ForegroundColor Gray
        Write-Host "        <version>`${project.version}</version>" -ForegroundColor Gray
        Write-Host "    </dependency>" -ForegroundColor Gray
    }
} else {
    Write-Host "  ✗ 找不到 pom.xml 文件" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 步骤3: 验证 Lombok 配置
Write-Host "[步骤3/5] 验证 Lombok 配置..." -ForegroundColor Yellow
$rootPomPath = Join-Path $workspaceRoot "pom.xml"
if (Test-Path $rootPomPath) {
    $rootPomContent = Get-Content $rootPomPath -Raw -Encoding UTF-8
    
    if ($rootPomContent -match "lombok\.version") {
        Write-Host "  ✓ Lombok 版本已在父POM中定义" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ Lombok 版本未在父POM中定义" -ForegroundColor Yellow
    }
    
    if ($rootPomContent -match "dependencyManagement.*lombok") {
        Write-Host "  ✓ Lombok 已在 dependencyManagement 中管理" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ Lombok 未在 dependencyManagement 中管理" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ⚠ 找不到根 pom.xml" -ForegroundColor Yellow
}

Write-Host ""

# 步骤4: 清理并重新编译
Write-Host "[步骤4/5] 清理并重新编译 ioedream-common-service..." -ForegroundColor Yellow
if (Test-Path $servicePath) {
    Push-Location $servicePath
    
    # 清理编译产物
    Write-Host "  执行: mvn clean" -ForegroundColor Gray
    mvn clean | Out-Null
    
    # 清理Maven本地缓存（可选）
    $cachePath = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common"
    if (Test-Path $cachePath) {
        Write-Host "  清理本地Maven缓存..." -ForegroundColor Gray
        Remove-Item -Path $cachePath -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    # 重新编译
    Write-Host "  执行: mvn clean compile -DskipTests -U" -ForegroundColor Gray
    mvn clean compile -DskipTests -U
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ 编译成功" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 编译失败，请检查错误信息" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    
    Pop-Location
} else {
    Write-Host "  ✗ 找不到 ioedream-common-service 目录" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 步骤5: 验证修复结果
Write-Host "[步骤5/5] 验证修复结果..." -ForegroundColor Yellow
if (Test-Path $servicePath) {
    Push-Location $servicePath
    
    # 检查编译错误
    $compileOutput = mvn compile -DskipTests 2>&1 | Out-String
    
    if ($compileOutput -match "BUILD SUCCESS") {
        Write-Host "  ✓ 编译成功，无错误" -ForegroundColor Green
    } elseif ($compileOutput -match "找不到符号.*setEmployeeNo|找不到符号.*setDepartmentName") {
        Write-Host "  ✗ 仍然存在编译错误" -ForegroundColor Red
        Write-Host "  可能原因:" -ForegroundColor Yellow
        Write-Host "  1. microservices-common 未正确编译" -ForegroundColor Yellow
        Write-Host "  2. Lombok 注解处理器未正确配置" -ForegroundColor Yellow
        Write-Host "  3. 模块依赖关系不正确" -ForegroundColor Yellow
        Pop-Location
        exit 1
    } else {
        Write-Host "  ⚠ 编译结果未知，请手动检查" -ForegroundColor Yellow
    }
    
    Pop-Location
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "如果仍有问题，请查看详细分析报告:" -ForegroundColor Yellow
Write-Host "  documentation\technical\COMPILATION_ERROR_ROOT_CAUSE_ANALYSIS.md" -ForegroundColor White
Write-Host "  documentation\technical\COMPILATION_FIX_EXECUTION_PLAN.md" -ForegroundColor White
