# IOE-DREAM 项目结构诊断脚本
# 功能：诊断项目结构，查找POM文件位置
# 作者：IOE-DREAM Team
# 日期：2025-12-06

$ErrorActionPreference = "Continue"
$workspaceRoot = if ($PSScriptRoot) { Split-Path $PSScriptRoot -Parent } else { $PWD }

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 项目结构诊断" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查根POM
Write-Host "[1] 检查根POM..." -ForegroundColor Yellow
$rootPom = Join-Path $workspaceRoot "pom.xml"
if (Test-Path $rootPom) {
    Write-Host "  ✓ 根POM存在: $rootPom" -ForegroundColor Green
    $rootPomContent = Get-Content $rootPom -Raw -Encoding UTF-8
    if ($rootPomContent -match "<modules>") {
        Write-Host "  ✓ 包含模块声明" -ForegroundColor Green
        $rootPomContent | Select-String -Pattern "<module>.*?</module>" | ForEach-Object {
            $moduleName = $_.Matches.Value -replace "<module>|</module>", ""
            Write-Host "    - $moduleName" -ForegroundColor Gray
        }
    } else {
        Write-Host "  ⚠ 不包含模块声明" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ✗ 根POM不存在" -ForegroundColor Red
}

Write-Host ""

# 2. 检查 microservices 目录POM
Write-Host "[2] 检查 microservices 目录POM..." -ForegroundColor Yellow
$microservicesPom = Join-Path $workspaceRoot "microservices\pom.xml"
if (Test-Path $microservicesPom) {
    Write-Host "  ✓ microservices POM存在: $microservicesPom" -ForegroundColor Green
    $microservicesPomContent = Get-Content $microservicesPom -Raw -Encoding UTF-8
    if ($microservicesPomContent -match "<modules>") {
        Write-Host "  ✓ 包含模块声明" -ForegroundColor Green
        $microservicesPomContent | Select-String -Pattern "<module>.*?</module>" | ForEach-Object {
            $moduleName = $_.Matches.Value -replace "<module>|</module>", ""
            Write-Host "    - $moduleName" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "  ✗ microservices POM不存在" -ForegroundColor Red
}

Write-Host ""

# 3. 检查目标模块
Write-Host "[3] 检查目标模块..." -ForegroundColor Yellow
$targetModules = @(
    @{Name="microservices-common"; Path=Join-Path $workspaceRoot "microservices\microservices-common"},
    @{Name="ioedream-common-service"; Path=Join-Path $workspaceRoot "microservices\ioedream-common-service"}
)

foreach ($module in $targetModules) {
    $pomPath = Join-Path $module.Path "pom.xml"
    Write-Host "  检查: $($module.Name)" -ForegroundColor Gray
    
    if (Test-Path $pomPath) {
        Write-Host "    ✓ POM存在: $pomPath" -ForegroundColor Green
        
        # 读取POM基本信息
        $pomContent = Get-Content $pomPath -Raw -Encoding UTF-8
        if ($pomContent -match "<artifactId>(.*?)</artifactId>") {
            $artifactId = $matches[1]
            Write-Host "      ArtifactId: $artifactId" -ForegroundColor Gray
        }
        if ($pomContent -match "<version>(.*?)</version>") {
            $version = $matches[1]
            Write-Host "      Version: $version" -ForegroundColor Gray
        }
    } else {
        Write-Host "    ✗ POM不存在: $pomPath" -ForegroundColor Red
        if (Test-Path $module.Path) {
            Write-Host "      目录存在，但缺少 pom.xml" -ForegroundColor Yellow
            $files = Get-ChildItem -Path $module.Path -File | Select-Object -First 5 Name
            if ($files) {
                Write-Host "      目录中的文件:" -ForegroundColor Gray
                $files | ForEach-Object {
                    Write-Host "        - $($_.Name)" -ForegroundColor Gray
                }
            }
        } else {
            Write-Host "      目录不存在" -ForegroundColor Yellow
        }
    }
}

Write-Host ""

# 4. 查找所有 pom.xml 文件
Write-Host "[4] 查找所有 pom.xml 文件..." -ForegroundColor Yellow
$allPoms = Get-ChildItem -Path $workspaceRoot -Recurse -Filter "pom.xml" -ErrorAction SilentlyContinue | 
    Where-Object { $_.FullName -notlike "*target*" -and $_.FullName -notlike "*.m2*" }
Write-Host "  找到 $($allPoms.Count) 个 pom.xml 文件" -ForegroundColor Gray

# 按目录分组显示
$pomsByDir = $allPoms | Group-Object { $_.DirectoryName } | Sort-Object Count -Descending
$pomsByDir | Select-Object -First 10 | ForEach-Object {
    Write-Host "    $($_.Name) - $($_.Count) 个文件" -ForegroundColor Gray
}

Write-Host ""

# 5. 检查是否有父POM可以统一编译
Write-Host "[5] 检查编译方式..." -ForegroundColor Yellow
if (Test-Path $rootPom) {
    Write-Host "  ✓ 可以从根目录统一编译" -ForegroundColor Green
    Write-Host "    命令: cd $workspaceRoot" -ForegroundColor Gray
    Write-Host "          mvn clean install -DskipTests -U" -ForegroundColor Gray
} elseif (Test-Path $microservicesPom) {
    Write-Host "  ✓ 可以从 microservices 目录编译" -ForegroundColor Green
    Write-Host "    命令: cd $workspaceRoot\microservices" -ForegroundColor Gray
    Write-Host "          mvn clean install -DskipTests -U" -ForegroundColor Gray
} else {
    Write-Host "  ✗ 未找到父POM，需要单独编译各模块" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "诊断完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

