# 修复包声明损坏问题
# 专门修复Logger脚本造成的package关键字损坏

param(
    [string]$ServicePath = "microservices/ioedream-consume-service"
)

Write-Host "=== 修复包声明损坏问题 ===" -ForegroundColor Red

$serviceFullPath = Join-Path $PSScriptRoot ".." $ServicePath
if (-not (Test-Path $serviceFullPath)) {
    Write-Host "错误: 找不到服务路径 $serviceFullPath" -ForegroundColor Red
    exit 1
}

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $serviceFullPath -Recurse -Filter "*.java" -Exclude "*Test.java"

Write-Host "找到 $($javaFiles.Count) 个Java文件，开始修复包声明问题..." -ForegroundColor Yellow

$fixedFiles = 0

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    if (-not $content) { continue }

    $originalContent = $content
    $needsFix = $false

    # 修复损坏的package声明
    if ($content -match '^﻿?ackage') {
        Write-Host "修复包声明: $($file.Name)" -ForegroundColor Green
        $content = $content -replace '^﻿?ackage', 'package'
        $needsFix = $true
    }

    # 修复损坏的import声明
    if ($content -match '﻿?mport') {
        Write-Host "修复import声明: $($file.Name)" -ForegroundColor Cyan
        $content = $content -replace '﻿?mport', 'import'
        $needsFix = $true
    }

    if ($needsFix -and $originalContent -ne $content) {
        # 以UTF-8无BOM格式保存
        [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
        Write-Host "✓ 已修复: $($file.Name)" -ForegroundColor Green
        $fixedFiles++
    }
}

Write-Host "`n=== 包声明修复完成 ===" -ForegroundColor Green
Write-Host "修复文件数: $fixedFiles" -ForegroundColor Green

# 快速编译检查几个关键文件
Write-Host "`n正在快速检查关键文件..." -ForegroundColor Yellow

# 检查AccountController
$accountController = Join-Path $serviceFullPath "src/main/java/net/lab1024/sa/consume/controller/AccountController.java"
if (Test-Path $accountController) {
    $lines = Get-Content $accountController | Select-Object -First 5
    Write-Host "AccountController 前5行:" -ForegroundColor Yellow
    $lines | ForEach-Object { Write-Host "  $_" }
}

Write-Host "=== 修复完成 ===" -ForegroundColor Green