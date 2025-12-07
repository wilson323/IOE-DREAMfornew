# IOE-DREAM iText依赖统一修复脚本
# 功能：修复所有微服务的itext依赖问题，清理IDE缓存，刷新Maven项目
# 作者：AI Assistant
# 日期：2025-01-30

param(
    [switch]$CleanCache = $false,
    [switch]$ForceUpdate = $false
)

$ErrorActionPreference = "Stop"
$workspaceRoot = $PSScriptRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM iText依赖统一修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 检查父POM配置
Write-Host "[1/6] 检查父POM配置..." -ForegroundColor Yellow
$parentPom = Join-Path $workspaceRoot "pom.xml"
if (-not (Test-Path $parentPom)) {
    Write-Host "  ✗ 父POM不存在: $parentPom" -ForegroundColor Red
    exit 1
}

$parentContent = Get-Content $parentPom -Raw -Encoding UTF-8
if ($parentContent -match "itext7-core\.version.*9\.4\.0" -and $parentContent -match "html2pdf\.version.*6\.3\.0") {
    Write-Host "  ✓ 父POM配置正确" -ForegroundColor Green
} else {
    Write-Host "  ✗ 父POM配置有问题，请检查itext7-core和html2pdf版本" -ForegroundColor Red
    exit 1
}

# 步骤2: 清理错误的Maven本地仓库缓存
Write-Host "[2/6] 清理错误的Maven本地仓库缓存..." -ForegroundColor Yellow
$mavenRepo = "$env:USERPROFILE\.m2\repository\com\itextpdf"

# 清理错误的itext-core缓存（如果存在）
$wrongItextCorePath = Join-Path $mavenRepo "itext-core\9.4.0"
if (Test-Path $wrongItextCorePath) {
    Write-Host "  发现错误的itext-core缓存，正在清理..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force $wrongItextCorePath -ErrorAction SilentlyContinue
    Write-Host "  ✓ 已清理错误的itext-core缓存" -ForegroundColor Green
}

# 清理错误的html2pdf缓存（如果存在）
$wrongHtml2pdfPath = Join-Path $mavenRepo "html2pdf\9.4.0"
if (Test-Path $wrongHtml2pdfPath) {
    Write-Host "  发现错误的html2pdf缓存，正在清理..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force $wrongHtml2pdfPath -ErrorAction SilentlyContinue
    Write-Host "  ✓ 已清理错误的html2pdf缓存" -ForegroundColor Green
}

# 步骤3: 验证正确的依赖是否存在
Write-Host "[3/6] 验证正确的依赖..." -ForegroundColor Yellow
$itext7CorePath = Join-Path $mavenRepo "itext7-core\9.4.0"
$html2pdfPath = Join-Path $mavenRepo "html2pdf\6.3.0"

if (-not (Test-Path $itext7CorePath)) {
    Write-Host "  ⚠ itext7-core:9.4.0 不在本地仓库，将在Maven构建时下载" -ForegroundColor Yellow
} else {
    Write-Host "  ✓ itext7-core:9.4.0 已存在" -ForegroundColor Green
}

if (-not (Test-Path $html2pdfPath)) {
    Write-Host "  ⚠ html2pdf:6.3.0 不在本地仓库，将在Maven构建时下载" -ForegroundColor Yellow
} else {
    Write-Host "  ✓ html2pdf:6.3.0 已存在" -ForegroundColor Green
}

# 步骤4: 检查所有微服务的pom.xml
Write-Host "[4/6] 检查所有微服务的pom.xml..." -ForegroundColor Yellow
$services = @(
    "microservices-common",
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-consume-service",
    "ioedream-visitor-service",
    "ioedream-video-service",
    "analytics"
)

$hasErrors = $false
foreach ($service in $services) {
    $servicePom = Join-Path $workspaceRoot $service "pom.xml"
    if (-not (Test-Path $servicePom)) {
        Write-Host "  ⚠ $service/pom.xml 不存在，跳过" -ForegroundColor Yellow
        continue
    }

    $serviceContent = Get-Content $servicePom -Raw -Encoding UTF-8

    # 检查是否有错误的itext-core引用
    if ($serviceContent -match 'artifactId\s*>\s*itext-core\s*<') {
        Write-Host "  ✗ $service 使用了错误的itext-core依赖" -ForegroundColor Red
        $hasErrors = $true
    } elseif ($serviceContent -match 'itext7-core') {
        Write-Host "  ✓ $service 配置正确" -ForegroundColor Green
    } else {
        # 如果没有直接引用itext，可能是通过microservices-common间接依赖，这是正常的
        if ($serviceContent -match 'microservices-common') {
            Write-Host "  ✓ $service 通过microservices-common间接依赖itext7-core" -ForegroundColor Green
        } else {
            Write-Host "  ⚠ $service 未引用itext相关依赖" -ForegroundColor Yellow
        }
    }
}

if ($hasErrors) {
    Write-Host "  ✗ 发现配置错误，请修复后重试" -ForegroundColor Red
    exit 1
}

# 步骤5: 强制更新Maven依赖（如果需要）
if ($ForceUpdate -or $CleanCache) {
    Write-Host "[5/6] 强制更新Maven依赖..." -ForegroundColor Yellow
    Push-Location $workspaceRoot

    try {
        Write-Host "  执行: mvn dependency:purge-local-repository -DmanualInclude=com.itextpdf:itext7-core,com.itextpdf:html2pdf" -ForegroundColor Gray
        mvn dependency:purge-local-repository -DmanualInclude="com.itextpdf:itext7-core,com.itextpdf:html2pdf" 2>&1 | Out-Null

        Write-Host "  执行: mvn clean install -DskipTests -pl microservices-common" -ForegroundColor Gray
        mvn clean install -DskipTests -pl microservices-common 2>&1 | Out-Null

        Write-Host "  ✓ Maven依赖更新完成" -ForegroundColor Green
    } catch {
        Write-Host "  ⚠ Maven命令执行失败，请手动执行: mvn clean install -DskipTests" -ForegroundColor Yellow
    } finally {
        Pop-Location
    }
} else {
    Write-Host "[5/6] 跳过Maven依赖更新（使用 -ForceUpdate 参数强制更新）" -ForegroundColor Yellow
}

# 步骤6: 生成IDE刷新指南
Write-Host "[6/6] 生成IDE刷新指南..." -ForegroundColor Yellow
$guidePath = Join-Path $workspaceRoot "IDE_REFRESH_GUIDE.md"
$guideContent = @"
# IDE刷新指南 - iText依赖修复

## 问题说明
IDE可能缓存了错误的依赖信息（itext-core:9.4.0），实际项目使用的是itext7-core:9.4.0。

## 刷新步骤

### IntelliJ IDEA
1. 右键项目根目录 → **Maven** → **Reload Project**
2. 或者：**File** → **Invalidate Caches / Restart...**
3. 选择 **Invalidate and Restart**
4. 等待IDE重新索引项目

### Eclipse/STS
1. 右键项目根目录 → **Maven** → **Update Project...**
2. 勾选 **Force Update of Snapshots/Releases**
3. 勾选所有子项目
4. 点击 **OK**
5. 如果问题仍然存在：右键项目 → **Maven** → **Reload Project**

### VS Code
1. 打开命令面板（Ctrl+Shift+P）
2. 执行：**Java: Clean Java Language Server Workspace**
3. 选择 **Restart and delete**
4. 重启VS Code

### 通用方法（命令行）
\`\`\`powershell
# 清理并重新构建
cd microservices
mvn clean install -DskipTests
\`\`\`

## 验证修复
修复后，IDE应该不再显示以下错误：
- ❌ Missing artifact com.itextpdf:itext-core:jar:9.4.0
- ✅ 应该正确识别 com.itextpdf:itext7-core:jar:9.4.0

## 如果问题仍然存在
1. 检查本地Maven仓库：\`$env:USERPROFILE\.m2\repository\com\itextpdf\`
2. 删除错误的缓存目录
3. 重新运行本脚本：\`.\fix-itext-dependencies.ps1 -ForceUpdate\`

---
生成时间: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
"@

Set-Content -Path $guidePath -Value $guideContent -Encoding UTF-8
Write-Host "  ✓ 已生成IDE刷新指南: IDE_REFRESH_GUIDE.md" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "1. 按照 IDE_REFRESH_GUIDE.md 中的步骤刷新IDE" -ForegroundColor White
Write-Host "2. 如果问题仍然存在，运行: .\fix-itext-dependencies.ps1 -ForceUpdate" -ForegroundColor White
Write-Host "3. 验证构建: mvn clean compile -DskipTests" -ForegroundColor White
Write-Host ""
