# 快速编码修复脚本
$ErrorActionPreference = "Stop"
$ProjectRoot = "D:\IOE-DREAM"
Set-Location $ProjectRoot

Write-Host "开始修复Java文件编码..." -ForegroundColor Cyan

$javaFiles = Get-ChildItem -Path "smart-admin-api-java17-springboot3" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue
$fixedCount = 0

foreach ($file in $javaFiles) {
    try {
        # 读取文件内容（自动检测编码）
        $content = Get-Content $file.FullName -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
        if ($null -eq $content) {
            $content = Get-Content $file.FullName -Raw -Encoding Default
        }

        # 移除BOM标记
        if ($content.StartsWith([char]0xFEFF)) {
            $content = $content.Substring(1)
        }

        # 保存为UTF-8无BOM
        $utf8NoBom = New-Object System.Text.UTF8Encoding $false
        [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)

        $fixedCount++
        if ($fixedCount % 100 -eq 0) {
            Write-Host "已修复 $fixedCount 个文件..." -ForegroundColor Yellow
        }
    } catch {
        Write-Host "修复失败: $($file.FullName) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "完成！共修复 $fixedCount 个文件" -ForegroundColor Green

