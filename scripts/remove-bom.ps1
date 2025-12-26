# PowerShell脚本：批量移除Java文件的BOM字符
# 作者：IOE-DREAM架构团队
# 日期：2025-12-26
# 用途：移除Java文件开头的UTF-8 BOM字符

$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  移除Java文件BOM字符工具" -ForegroundColor Cyan
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

# 递归查找所有Java文件
$javaFiles = Get-ChildItem -Path $projectRoot -Filter "*.java" -Recurse -File

Write-Host "📊 找到 $($javaFiles.Count) 个Java文件" -ForegroundColor Cyan
Write-Host ""

$bomCount = 0
$fixedCount = 0

# 遍历所有Java文件
foreach ($file in $javaFiles) {
    # 读取文件内容并检查BOM
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $firstByte = [char]($content[0])

    # UTF-8 BOM是 EF BB BF，在C#中会显示为 '\ufeff'
    if ($firstByte -eq '\ufeff') {
        $bomCount++

        # 读取文件内容（不包含BOM）
        $utf8NoBom = New-Object System.Text.UTF8Encoding $false
        $fileContent = [System.IO.File]::ReadAllBytes($file.FullName)

        # 检查是否有BOM (EF BB BF)
        if ($fileContent.Length -ge 3 -and
            $fileContent[0] -eq 0xEF -and
            $fileContent[1] -eq 0xBB -and
            $fileContent[2] -eq 0xBF) {

            # 移除BOM并保存
            $contentWithoutBom = [System.IO.File]::WriteAllBytes(
                $file.FullName,
                $fileContent[3..($fileContent.Length-1)]
            )

            $fixedCount++
            Write-Host "✅ 移除BOM: $($file.Name.Replace($projectRoot, ''))" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  修复完成统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📊 发现BOM文件: $bomCount" -ForegroundColor Yellow
Write-Host "✅ 已修复文件: $fixedCount" -ForegroundColor Green
Write-Host ""

if ($fixedCount -gt 0) {
    Write-Host "✅ BOM字符移除完成!" -ForegroundColor Green
} else {
    Write-Host "ℹ️  没有发现BOM字符" -ForegroundColor Gray
}

Write-Host ""
