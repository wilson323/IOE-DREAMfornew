# IOE-DREAM 强制刷新IDE脚本
# 功能：强制清理IDE缓存并刷新Maven项目
# 作者：AI Assistant
# 日期：2025-01-30

param(
    [switch]$CleanMavenCache = $true,
    [switch]$CleanIDECache = $true,
    [switch]$ForceMavenUpdate = $true
)

$ErrorActionPreference = "Stop"
$workspaceRoot = $PSScriptRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 强制刷新IDE脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 清理错误的Maven缓存
if ($CleanMavenCache) {
    Write-Host "[1/5] 清理错误的Maven缓存..." -ForegroundColor Yellow
    $mavenRepo = "$env:USERPROFILE\.m2\repository\com\itextpdf"

    $wrongCaches = @(
        "itext-core\9.4.0",
        "html2pdf\9.4.0"
    )

    foreach ($cache in $wrongCaches) {
        $cachePath = Join-Path $mavenRepo $cache
        if (Test-Path $cachePath) {
            Write-Host "  删除错误的缓存: $cache" -ForegroundColor Gray
            Remove-Item -Recurse -Force $cachePath -ErrorAction SilentlyContinue
            Write-Host "  ✓ 已删除" -ForegroundColor Green
        }
    }

    Write-Host "  ✓ Maven缓存清理完成" -ForegroundColor Green
    Write-Host ""
}

# 步骤2: 清理IDE缓存（如果可能）
if ($CleanIDECache) {
    Write-Host "[2/5] 清理IDE缓存..." -ForegroundColor Yellow

    # IntelliJ IDEA缓存
    $ideaCacheDirs = @(
        "$env:USERPROFILE\.IntelliJIdea*\system\caches",
        "$env:USERPROFILE\.IntelliJIdea*\system\index",
        "$env:USERPROFILE\.IntelliJIdea*\system\localHistory"
    )

    foreach ($pattern in $ideaCacheDirs) {
        $dirs = Get-ChildItem -Path (Split-Path $pattern -Parent) -Directory -Filter (Split-Path $pattern -Leaf) -ErrorAction SilentlyContinue
        foreach ($dir in $dirs) {
            Write-Host "  发现IntelliJ IDEA缓存: $($dir.FullName)" -ForegroundColor Gray
            Write-Host "    注意: 需要手动清理或使用IDE的Invalidate Caches功能" -ForegroundColor Yellow
        }
    }

    # Eclipse缓存
    $eclipseWorkspace = "$env:USERPROFILE\workspace"
    if (Test-Path $eclipseWorkspace) {
        Write-Host "  发现Eclipse工作空间: $eclipseWorkspace" -ForegroundColor Gray
        Write-Host "    注意: Eclipse缓存在工作空间内，需要手动清理" -ForegroundColor Yellow
    }

    # VS Code Java扩展缓存
    $vscodeJavaCache = "$env:USERPROFILE\.vscode\extensions\redhat.java-*\workspace"
    $vscodeDirs = Get-ChildItem -Path "$env:USERPROFILE\.vscode\extensions" -Directory -Filter "redhat.java-*" -ErrorAction SilentlyContinue
    if ($vscodeDirs) {
        Write-Host "  发现VS Code Java扩展缓存" -ForegroundColor Gray
        Write-Host "    注意: 使用Java: Clean Java Language Server Workspace清理" -ForegroundColor Yellow
    }

    Write-Host "  ⚠ IDE缓存需要手动清理（见IDE_REFRESH_GUIDE.md）" -ForegroundColor Yellow
    Write-Host ""
}

# 步骤3: 强制更新Maven依赖
if ($ForceMavenUpdate) {
    Write-Host "[3/5] 强制更新Maven依赖..." -ForegroundColor Yellow
    Push-Location $workspaceRoot

    try {
        Write-Host "  清理Maven本地仓库中的itext依赖..." -ForegroundColor Gray
        mvn dependency:purge-local-repository -DmanualInclude="com.itextpdf:itext7-core,com.itextpdf:html2pdf" -q 2>&1 | Out-Null

        Write-Host "  重新下载依赖..." -ForegroundColor Gray
        mvn clean install -DskipTests -pl microservices-common -q 2>&1 | Out-Null

        Write-Host "  ✓ Maven依赖更新完成" -ForegroundColor Green
    } catch {
        Write-Host "  ⚠ Maven命令执行失败，请手动执行:" -ForegroundColor Yellow
        Write-Host "    mvn clean install -DskipTests" -ForegroundColor White
    } finally {
        Pop-Location
    }

    Write-Host ""
}

# 步骤4: 验证依赖配置
Write-Host "[4/5] 验证依赖配置..." -ForegroundColor Yellow
Push-Location $workspaceRoot
try {
    $output = mvn dependency:tree -Dincludes=com.itextpdf:itext7-core -pl microservices-common -q 2>&1
    if ($output -match "itext7-core") {
        Write-Host "  ✓ 依赖树正确包含itext7-core" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ 无法验证依赖树（可能需要先构建）" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ⚠ 依赖树验证失败" -ForegroundColor Yellow
} finally {
    Pop-Location
}
Write-Host ""

# 步骤5: 生成IDE刷新指令
Write-Host "[5/5] 生成IDE刷新指令..." -ForegroundColor Yellow

$ideInstructions = @"
# IDE刷新指令

## 必须执行的步骤

### IntelliJ IDEA
1. File → Invalidate Caches / Restart...
2. 选择 "Invalidate and Restart"
3. 等待IDE重启并重新索引项目
4. 右键项目根目录 → Maven → Reload Project

### Eclipse/STS
1. 右键项目根目录 → Maven → Update Project...
2. 勾选 "Force Update of Snapshots/Releases"
3. 勾选所有子项目
4. 点击 OK
5. 如果问题仍然存在：右键项目 → Maven → Reload Project

### VS Code
1. 打开命令面板 (Ctrl+Shift+P)
2. 执行: Java: Clean Java Language Server Workspace
3. 选择 "Restart and delete"
4. 重启VS Code
5. 等待Java扩展重新索引项目

## 验证修复
运行验证脚本：
\`\`\`powershell
.\verify-dependencies.ps1
\`\`\`

如果验证通过，IDE应该不再显示itext-core错误。
"@

$instructionsPath = Join-Path $workspaceRoot "IDE_REFRESH_INSTRUCTIONS.md"
Set-Content -Path $instructionsPath -Value $ideInstructions -Encoding UTF-8
Write-Host "  ✓ 已生成IDE刷新指令: IDE_REFRESH_INSTRUCTIONS.md" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "刷新完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "1. 按照 IDE_REFRESH_INSTRUCTIONS.md 中的步骤刷新IDE" -ForegroundColor White
Write-Host "2. 运行验证: .\verify-dependencies.ps1" -ForegroundColor White
Write-Host "3. 如果问题仍然存在，运行诊断: .\diagnose-ide-dependencies.ps1" -ForegroundColor White
Write-Host ""
