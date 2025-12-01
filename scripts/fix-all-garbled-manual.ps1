# IOE-DREAM 手动修复所有乱码脚本
# 全面扫描并修复项目中的所有乱码字符

$ErrorActionPreference = "Continue"
$ProjectRoot = "D:\IOE-DREAM"
Set-Location $ProjectRoot

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 手动乱码修复工具" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 定义乱码模式
$garbledPatterns = @(
    "",
    "",
    "涓",
    "鏂",
    "锘烘湰",
    "鑰",
    "绠",
    "悊",
    "嫟",
    "鎵",
    "崱",
    "庡",
    "煡",
    "璇",
    "娑",
    "樺",
    "煑",
    "检查",
    "结果",
    "不一致",
    "时间",
    "处理",
    "不能为空",
    "长度不能超过",
    "格式：YYYY-MM）",
    "一致性",
    "完整性",
    "对账",
    "并行处理",
    "检查结果",
    "批量检查结果",
    "DAILY/MONTHLY/CUSTOM）",
    "",
    "??",
    "?"
)

# 定义修复映射表
$fixMappings = @{
    "检查" = "检查"
    "结果" = "结果"
    "不一致" = "不一致"
    "时间" = "时间"
    "处理" = "处理"
    "不能为空" = "不能为空"
    "长度不能超过" = "长度不能超过"
    "格式：YYYY-MM）" = "格式：YYYY-MM）"
    "一致性" = "一致性"
    "完整性" = "完整性"
    "对账" = "对账"
    "并行处理" = "并行处理"
    "检查结果" = "检查结果"
    "批量检查结果" = "批量检查结果"
    "DAILY/MONTHLY/CUSTOM）" = "DAILY/MONTHLY/CUSTOM）"
}

$fixedFiles = 0
$errorFiles = 0
$totalFiles = 0
$filesWithGarbled = @()

# 获取所有需要检查的文件
Write-Host "正在扫描文件..." -ForegroundColor Blue
$fileTypes = @("*.java", "*.ps1", "*.py", "*.md", "*.sh", "*.xml", "*.yaml", "*.yml", "*.properties", "*.txt")
$allFiles = @()

foreach ($type in $fileTypes) {
    $files = Get-ChildItem -Path $ProjectRoot -Recurse -Filter $type -ErrorAction SilentlyContinue | Where-Object {
        $_.FullName -notmatch "\\node_modules\\" -and
        $_.FullName -notmatch "\\.git\\" -and
        $_.FullName -notmatch "\\target\\" -and
        $_.FullName -notmatch "\\build\\" -and
        $_.FullName -notmatch "\\venv\\" -and
        $_.FullName -notmatch "\\__pycache__\\"
    }
    $allFiles += $files
}

Write-Host "找到 $($allFiles.Count) 个文件需要检查`n" -ForegroundColor Green

# 检查每个文件
foreach ($file in $allFiles) {
    $totalFiles++
    
    try {
        # 读取文件内容
        $content = $null
        $encoding = $null
        $needsFix = $false
        
        # 尝试UTF-8读取
        try {
            $content = Get-Content $file.FullName -Raw -Encoding UTF8 -ErrorAction Stop
            $encoding = "UTF-8"
        } catch {
            # 尝试GBK读取
            try {
                $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
                $content = [System.Text.Encoding]::GetEncoding("GBK").GetString($bytes)
                $encoding = "GBK"
                $needsFix = $true
            } catch {
                Write-Host "  [WARNING] 无法读取文件: $($file.FullName)" -ForegroundColor Yellow
                $errorFiles++
                continue
            }
        }
        
        if ($null -eq $content) {
            continue
        }
        
        $originalContent = $content
        $hasChanges = $false
        
        # 检查是否包含乱码
        $hasGarbled = $false
        foreach ($pattern in $garbledPatterns) {
            if ($content.Contains($pattern)) {
                $hasGarbled = $true
                break
            }
        }
        
        if ($hasGarbled) {
            $filesWithGarbled += $file.FullName
        }
        
        # 移除BOM
        if ($content.StartsWith([char]0xFEFF)) {
            $content = $content.Substring(1)
            $hasChanges = $true
        }
        
        # 应用修复映射
        foreach ($key in $fixMappings.Keys) {
            if ($content.Contains($key)) {
                $content = $content.Replace($key, $fixMappings[$key])
                $hasChanges = $true
            }
        }
        
        # 移除其他乱码字符
        $content = $content -replace "", ""
        $content = $content -replace "", ""
        $content = $content -replace "涓", ""
        $content = $content -replace "鏂", ""
        
        # 如果有修改或需要转换编码，保存文件
        if ($hasChanges -or $encoding -ne "UTF-8" -or $hasGarbled) {
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            $fixedFiles++
            
            if ($encoding -ne "UTF-8") {
                Write-Host "  [FIXED] $($file.Name) (编码转换: $encoding -> UTF-8)" -ForegroundColor Green
            } elseif ($hasGarbled) {
                Write-Host "  [FIXED] $($file.Name) (乱码已修复)" -ForegroundColor Green
            } else {
                Write-Host "  [FIXED] $($file.Name) (BOM已移除)" -ForegroundColor Green
            }
        }
        
        # 显示进度
        if ($totalFiles % 100 -eq 0) {
            Write-Host "进度: $totalFiles / $($allFiles.Count) 文件已处理..." -ForegroundColor Cyan
        }
        
    } catch {
        Write-Host "  [ERROR] $($file.FullName): $_" -ForegroundColor Red
        $errorFiles++
    }
}

# 输出总结
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "修复总结" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "总文件数: $totalFiles" -ForegroundColor White
Write-Host "修复文件数: $fixedFiles" -ForegroundColor Green
Write-Host "错误文件数: $errorFiles" -ForegroundColor $(if ($errorFiles -gt 0) { "Red" } else { "Green" })
Write-Host "发现乱码文件数: $($filesWithGarbled.Count)" -ForegroundColor $(if ($filesWithGarbled.Count -gt 0) { "Yellow" } else { "Green" })

if ($filesWithGarbled.Count -gt 0) {
    Write-Host "`n包含乱码的文件列表:" -ForegroundColor Yellow
    foreach ($file in $filesWithGarbled) {
        Write-Host "  - $file" -ForegroundColor Yellow
    }
}

Write-Host "`n========================================`n" -ForegroundColor Cyan

if ($errorFiles -eq 0 -and $filesWithGarbled.Count -eq 0) {
    Write-Host "[SUCCESS] 所有乱码已修复完成!`n" -ForegroundColor Green
    exit 0
} elseif ($errorFiles -eq 0) {
    Write-Host "[WARNING] 部分文件仍包含乱码，请手动检查`n" -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "[ERROR] 修复过程中出现错误，请检查`n" -ForegroundColor Red
    exit 1
}




