# PowerShell脚本：批量修复Entity类导入路径
# 作者：IOE-DREAM架构团队
# 日期：2025-12-26
# 用途：修复Entity类的错误导入路径

$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Entity类导入路径修复工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 定义项目根目录
$projectRoot = "D:\IOE-DREAM\microservices"

# 检查目录是否存在
if (-not (Test-Path $projectRoot)) {
    Write-Host "❌ 错误: 目录不存在 - $projectRoot" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 找到目录: $projectRoot" -ForegroundColor Green
Write-Host ""

# 定义Entity导入路径映射
$entityImportMappings = @{
    # UserEntity相关
    "import net.lab1024.sa.common.entity.UserEntity;" = "import net.lab1024.sa.common.organization.entity.UserEntity;"
    "import net.lab1024.sa.base.entity.UserEntity;" = "import net.lab1024.sa.common.organization.entity.UserEntity;"

    # 其他常见Entity路径映射（根据实际情况添加）
    # "import net.lab1024.sa.common.entity.XEntity;" = "import net.lab1024.sa.common.xxx.entity.XEntity;"
}

# 递归查找所有Java文件
$javaFiles = Get-ChildItem -Path $projectRoot -Filter "*.java" -Recurse -File

Write-Host "📊 找到 $($javaFiles.Count) 个Java文件" -ForegroundColor Cyan
Write-Host ""

$fixedCount = 0
$skippedCount = 0
$fixedFiles = @{}

# 遍历所有Java文件
foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $originalContent = $content
    $fileFixed = $false

    # 应用每个导入路径映射
    foreach ($mapping in $entityImportMappings.GetEnumerator()) {
        $wrongImport = $mapping.Key
        $correctImport = $mapping.Value

        if ($content -match [regex]::Escape($wrongImport)) {
            $content = $content -replace [regex]::Escape($wrongImport), $correctImport
            $fileFixed = $true

            if (-not $fixedFiles.ContainsKey($file.Name)) {
                $fixedFiles[$file.Name] = @()
            }
            $fixedFiles[$file.Name] += $wrongImport
        }
    }

    # 如果文件被修改，保存更改
    if ($fileFixed) {
        $content | Set-Content $file.FullName -Encoding UTF8 -NoNewline
        $fixedCount++
        Write-Host "✅ 修复: $($file.Name.Replace($projectRoot, ''))" -ForegroundColor Green
    } else {
        $skippedCount++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  修复完成统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ 已修复文件: $fixedCount" -ForegroundColor Green
Write-Host "⏭️  跳过文件: $skippedCount" -ForegroundColor Gray
Write-Host "📊 修复率: $($fixedCount / $javaFiles.Count * 100):F2%" -ForegroundColor Cyan
Write-Host ""

# 详细修复信息
if ($fixedCount -gt 0) {
    Write-Host "📝 修复详情:" -ForegroundColor Yellow
    foreach ($fileName in $fixedFiles.Keys) {
        Write-Host "  📄 $fileName" -ForegroundColor White
        foreach ($wrongImport in $fixedFiles[$fileName]) {
            Write-Host "    ❌ $wrongImport" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "✅ Entity类导入路径修复完成!" -ForegroundColor Green
Write-Host ""

# 清理变量
Remove-Variable -Name entityImportMappings, javaFiles, fixedCount, skippedCount, fixedFiles -ErrorAction SilentlyContinue
