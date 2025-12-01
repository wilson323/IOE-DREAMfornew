# Fix garbled characters in all Java files
$ErrorActionPreference = "Stop"
$ProjectRoot = "D:\IOE-DREAM"
Set-Location $ProjectRoot

Write-Host "Starting garbled character fix..." -ForegroundColor Cyan

$fixedFiles = 0
$errorFiles = 0
$totalFiles = 0

# Get all Java files
$javaFiles = Get-ChildItem -Path "smart-admin-api-java17-springboot3" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue

Write-Host "Found $($javaFiles.Count) Java files to check`n" -ForegroundColor Blue

foreach ($file in $javaFiles) {
    $totalFiles++
    
    try {
        # Read file content
        $content = $null
        $encoding = $null
        
        # Try UTF-8 first
        try {
            $content = Get-Content $file.FullName -Raw -Encoding UTF8 -ErrorAction Stop
            $encoding = "UTF-8"
        } catch {
            # Try GBK
            try {
                $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
                $content = [System.Text.Encoding]::GetEncoding("GBK").GetString($bytes)
                $encoding = "GBK"
            } catch {
                Write-Host "  [WARNING] Cannot read file: $($file.FullName)" -ForegroundColor Yellow
                $errorFiles++
                continue
            }
        }
        
        if ($null -eq $content) {
            continue
        }
        
        $originalContent = $content
        $hasChanges = $false
        
        # Remove BOM
        if ($content.StartsWith([char]0xFEFF)) {
            $content = $content.Substring(1)
            $hasChanges = $true
        }
        
        # Fix common garbled patterns using regex
        $patterns = @{
            "检\?" = "检查"
            "结\?" = "结果"
            "不一\?" = "不一致"
            "时\?" = "时间"
            "处\?" = "处理"
            "不能为\?" = "不能为空"
            "长度不能超\?" = "长度不能超过"
            "格式：YYYY-MM\?" = "格式：YYYY-MM）"
            "一致\?" = "一致性"
            "完整\?" = "完整性"
            "对\?" = "对账"
            "并行处\?" = "并行处理"
            "检查结\?" = "检查结果"
            "批量检查结\?" = "批量检查结果"
            "DAILY/MONTHLY/CUSTOM\?" = "DAILY/MONTHLY/CUSTOM）"
        }
        
        foreach ($pattern in $patterns.Keys) {
            if ($content -match $pattern) {
                $content = $content -replace $pattern, $patterns[$pattern]
                $hasChanges = $true
            }
        }
        
        # Remove other garbled characters
        $content = $content -replace "", ""
        $content = $content -replace "", ""
        
        # Save if changed or encoding needs conversion
        if ($hasChanges -or $encoding -ne "UTF-8") {
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            $fixedFiles++
            
            if ($encoding -ne "UTF-8") {
                Write-Host "  [FIXED] $($file.Name) (Encoding: $encoding -> UTF-8)" -ForegroundColor Green
            } else {
                Write-Host "  [FIXED] $($file.Name) (Garbled characters fixed)" -ForegroundColor Green
            }
        }
        
        # Show progress every 100 files
        if ($totalFiles % 100 -eq 0) {
            Write-Host "Progress: $totalFiles / $($javaFiles.Count) files processed..." -ForegroundColor Cyan
        }
        
    } catch {
        Write-Host "  [ERROR] $($file.FullName): $_" -ForegroundColor Red
        $errorFiles++
    }
}

Write-Host "`n============================================================================" -ForegroundColor Cyan
Write-Host "Summary" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "Total files: $totalFiles" -ForegroundColor White
Write-Host "Fixed files: $fixedFiles" -ForegroundColor Green
Write-Host "Error files: $errorFiles" -ForegroundColor $(if ($errorFiles -gt 0) { "Red" } else { "Green" })
Write-Host "============================================================================`n" -ForegroundColor Cyan

if ($errorFiles -eq 0) {
    Write-Host "[SUCCESS] All files fixed!`n" -ForegroundColor Green
    exit 0
} else {
    Write-Host "[WARNING] Some files failed, please check errors`n" -ForegroundColor Yellow
    exit 1
}

