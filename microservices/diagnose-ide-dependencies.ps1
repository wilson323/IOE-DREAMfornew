# IOE-DREAM IDE依赖诊断脚本
# 功能：诊断IDE为什么仍然报错itext-core:9.4.0
# 作者：AI Assistant
# 日期：2025-01-30

$ErrorActionPreference = "Continue"
$workspaceRoot = $PSScriptRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM IDE依赖诊断脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$diagnosisResults = @()

# 诊断1: 检查pom.xml配置
Write-Host "[诊断1] 检查pom.xml配置..." -ForegroundColor Yellow
$parentPom = Join-Path $workspaceRoot "pom.xml"
if (Test-Path $parentPom) {
    $content = Get-Content $parentPom -Raw -Encoding UTF-8

    if ($content -match "itext7-core\.version.*9\.4\.0") {
        Write-Host "  ✓ 父POM正确配置itext7-core:9.4.0" -ForegroundColor Green
        $diagnosisResults += "✓ pom.xml配置正确"
    } else {
        Write-Host "  ✗ 父POM配置有问题" -ForegroundColor Red
        $diagnosisResults += "✗ pom.xml配置错误"
    }

    if ($content -match 'artifactId\s*>\s*itext-core\s*<') {
        Write-Host "  ✗ 父POM中发现了错误的itext-core引用！" -ForegroundColor Red
        $diagnosisResults += "✗ 父POM包含错误的itext-core引用"
    } else {
        Write-Host "  ✓ 父POM没有错误的itext-core引用" -ForegroundColor Green
        $diagnosisResults += "✓ 父POM没有错误的itext-core引用"
    }
} else {
    Write-Host "  ✗ 父POM不存在" -ForegroundColor Red
    $diagnosisResults += "✗ 父POM不存在"
}

Write-Host ""

# 诊断2: 检查Maven本地仓库
Write-Host "[诊断2] 检查Maven本地仓库..." -ForegroundColor Yellow
$mavenRepo = "$env:USERPROFILE\.m2\repository\com\itextpdf"

# 检查错误的缓存
$wrongCachePath = Join-Path $mavenRepo "itext-core\9.4.0"
if (Test-Path $wrongCachePath) {
    Write-Host "  ✗ 发现错误的itext-core缓存: $wrongCachePath" -ForegroundColor Red
    Write-Host "    这是IDE报错的根源！" -ForegroundColor Red
    $diagnosisResults += "✗ 发现错误的itext-core缓存（IDE报错根源）"

    # 检查缓存内容
    $jarFile = Join-Path $wrongCachePath "itext-core-9.4.0.jar"
    if (Test-Path $jarFile) {
        Write-Host "    包含JAR文件（但这是错误的artifact）" -ForegroundColor Yellow
    } else {
        Write-Host "    只有元数据文件（下载失败）" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ✓ 没有发现错误的itext-core缓存" -ForegroundColor Green
    $diagnosisResults += "✓ 没有错误的itext-core缓存"
}

# 检查正确的依赖
$correctCachePath = Join-Path $mavenRepo "itext7-core\9.4.0"
if (Test-Path $correctCachePath) {
    Write-Host "  ✓ 正确的itext7-core缓存存在" -ForegroundColor Green
    $diagnosisResults += "✓ 正确的itext7-core缓存存在"
} else {
    Write-Host "  ⚠ itext7-core缓存不存在（将在首次构建时下载）" -ForegroundColor Yellow
    $diagnosisResults += "⚠ itext7-core缓存不存在"
}

Write-Host ""

# 诊断3: 检查IDE配置文件
Write-Host "[诊断3] 检查IDE配置文件..." -ForegroundColor Yellow

# 检查.classpath文件
$classpathFiles = Get-ChildItem -Path $workspaceRoot -Filter ".classpath" -Recurse -ErrorAction SilentlyContinue
if ($classpathFiles) {
    Write-Host "  发现.classpath文件:" -ForegroundColor Yellow
    foreach ($file in $classpathFiles) {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        if ($content -match "itext-core") {
            Write-Host "    ✗ $($file.FullName) 包含错误的itext-core引用" -ForegroundColor Red
            $diagnosisResults += "✗ $($file.Name) 包含错误的itext-core引用"
        } else {
            Write-Host "    ✓ $($file.FullName) 没有错误的引用" -ForegroundColor Green
        }
    }
} else {
    Write-Host "  ✓ 没有发现.classpath文件（这是正常的，现代IDE不需要）" -ForegroundColor Green
    $diagnosisResults += "✓ 没有.classpath文件"
}

# 检查.settings目录
$settingsDirs = Get-ChildItem -Path $workspaceRoot -Filter ".settings" -Directory -Recurse -ErrorAction SilentlyContinue
if ($settingsDirs) {
    Write-Host "  发现.settings目录:" -ForegroundColor Yellow
    foreach ($dir in $settingsDirs) {
        Write-Host "    ⚠ $($dir.FullName) (Eclipse配置文件)" -ForegroundColor Yellow
        $diagnosisResults += "⚠ 发现.settings目录: $($dir.Name)"
    }
} else {
    Write-Host "  ✓ 没有发现.settings目录" -ForegroundColor Green
}

Write-Host ""

# 诊断4: 检查Maven有效POM
Write-Host "[诊断4] 检查Maven有效POM..." -ForegroundColor Yellow
Push-Location $workspaceRoot
try {
    $effectivePomFile = Join-Path $workspaceRoot "microservices-common" "effective-pom.xml"
    if (Test-Path $effectivePomFile) {
        Remove-Item $effectivePomFile -Force -ErrorAction SilentlyContinue
    }

    Write-Host "  生成有效POM..." -ForegroundColor Gray
    mvn help:effective-pom -pl microservices-common -Doutput=effective-pom.xml -q 2>&1 | Out-Null

    if (Test-Path $effectivePomFile) {
        $effectiveContent = Get-Content $effectivePomFile -Raw -Encoding UTF-8

        if ($effectiveContent -match 'artifactId\s*>\s*itext7-core\s*<') {
            Write-Host "  ✓ 有效POM正确包含itext7-core" -ForegroundColor Green
            $diagnosisResults += "✓ 有效POM正确"

            # 提取版本号
            if ($effectiveContent -match 'itext7-core.*?version\s*>\s*([\d.]+)\s*<') {
                $version = $matches[1]
                Write-Host "    版本: $version" -ForegroundColor Gray
            }
        } else {
            Write-Host "  ✗ 有效POM没有包含itext7-core" -ForegroundColor Red
            $diagnosisResults += "✗ 有效POM缺少itext7-core"
        }

        if ($effectiveContent -match 'artifactId\s*>\s*itext-core\s*<') {
            Write-Host "  ✗ 有效POM包含错误的itext-core！" -ForegroundColor Red
            $diagnosisResults += "✗ 有效POM包含错误的itext-core"
        }

        Remove-Item $effectivePomFile -Force -ErrorAction SilentlyContinue
    } else {
        Write-Host "  ⚠ 无法生成有效POM（Maven可能未正确配置）" -ForegroundColor Yellow
        $diagnosisResults += "⚠ 无法生成有效POM"
    }
} catch {
    Write-Host "  ⚠ Maven命令执行失败: $_" -ForegroundColor Yellow
    $diagnosisResults += "⚠ Maven命令失败"
} finally {
    Pop-Location
}

Write-Host ""

# 诊断5: 检查IDE类型
Write-Host "[诊断5] 检测IDE类型..." -ForegroundColor Yellow
$ideType = "Unknown"

# 检查IntelliJ IDEA
$ideaFiles = Get-ChildItem -Path $workspaceRoot -Filter "*.iml" -Recurse -ErrorAction SilentlyContinue
if ($ideaFiles) {
    $ideType = "IntelliJ IDEA"
    Write-Host "  ✓ 检测到IntelliJ IDEA项目" -ForegroundColor Green
    Write-Host "    建议操作: File → Invalidate Caches / Restart" -ForegroundColor Yellow
}

# 检查Eclipse
if ($settingsDirs) {
    $ideType = "Eclipse"
    Write-Host "  ✓ 检测到Eclipse项目" -ForegroundColor Green
    Write-Host "    建议操作: Maven → Update Project (Force Update)" -ForegroundColor Yellow
}

# 检查VS Code
$vscodeDir = Join-Path $workspaceRoot ".vscode"
if (Test-Path $vscodeDir) {
    if ($ideType -eq "Unknown") {
        $ideType = "VS Code"
    }
    Write-Host "  ✓ 检测到VS Code配置" -ForegroundColor Green
    Write-Host "    建议操作: Java: Clean Java Language Server Workspace" -ForegroundColor Yellow
}

if ($ideType -eq "Unknown") {
    Write-Host "  ⚠ 无法确定IDE类型" -ForegroundColor Yellow
}

$diagnosisResults += "IDE类型: $ideType"

Write-Host ""

# 生成诊断报告
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "诊断报告" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

foreach ($result in $diagnosisResults) {
    if ($result -match "✓") {
        Write-Host $result -ForegroundColor Green
    } elseif ($result -match "✗") {
        Write-Host $result -ForegroundColor Red
    } else {
        Write-Host $result -ForegroundColor Yellow
    }
}

Write-Host ""

# 生成修复建议
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复建议" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($diagnosisResults -match "✗.*itext-core缓存") {
    Write-Host "1. 清理错误的Maven缓存（必须）:" -ForegroundColor Yellow
    Write-Host "   .\fix-itext-dependencies.ps1 -CleanCache -ForceUpdate" -ForegroundColor White
    Write-Host ""
}

Write-Host "2. 刷新IDE（必须）:" -ForegroundColor Yellow
switch ($ideType) {
    "IntelliJ IDEA" {
        Write-Host "   - File → Invalidate Caches / Restart..." -ForegroundColor White
        Write-Host "   - 选择 'Invalidate and Restart'" -ForegroundColor White
        Write-Host "   - 等待IDE重启并重新索引" -ForegroundColor White
    }
    "Eclipse" {
        Write-Host "   - 右键项目 → Maven → Update Project..." -ForegroundColor White
        Write-Host "   - 勾选 'Force Update of Snapshots/Releases'" -ForegroundColor White
        Write-Host "   - 勾选所有子项目，点击OK" -ForegroundColor White
    }
    "VS Code" {
        Write-Host "   - 打开命令面板 (Ctrl+Shift+P)" -ForegroundColor White
        Write-Host "   - 执行: Java: Clean Java Language Server Workspace" -ForegroundColor White
        Write-Host "   - 选择 'Restart and delete'" -ForegroundColor White
    }
    default {
        Write-Host "   - 参考 microservices/IDE_REFRESH_GUIDE.md" -ForegroundColor White
    }
}
Write-Host ""

Write-Host "3. 验证修复:" -ForegroundColor Yellow
Write-Host "   .\verify-dependencies.ps1" -ForegroundColor White
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
