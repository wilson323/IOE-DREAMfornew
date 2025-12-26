# 紧急修复Logger脚本造成的文件损坏问题
# 作者: IOE-DREAM Team
# 用途: 修复UTF-8 BOM问题和缺失的类声明

param(
    [string]$ServicePath = "microservices/ioedream-consume-service"
)

Write-Host "=== 紧急修复Logger脚本造成的文件损坏问题 ===" -ForegroundColor Red

$serviceFullPath = Join-Path $PSScriptRoot ".." $ServicePath
if (-not (Test-Path $serviceFullPath)) {
    Write-Host "错误: 找不到服务路径 $serviceFullPath" -ForegroundColor Red
    exit 1
}

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $serviceFullPath -Recurse -Filter "*.java" -Exclude "*Test.java"

Write-Host "找到 $($javaFiles.Count) 个Java文件，开始修复损坏问题..." -ForegroundColor Yellow

$fixedFiles = 0
$ bomFixedFiles = 0
$ classFixedFiles = 0

foreach ($file in $javaFiles) {
    $originalContent = Get-Content -Path $file.FullName -Raw
    if (-not $originalContent) { continue }

    $content = $originalContent
    $needsFix = $false
    $fixReasons = @()

    # 1. 修复UTF-8 BOM问题
    if ($content.StartsWith("﻿")) {
        Write-Host "修复 BOM: $($file.Name)" -ForegroundColor Cyan
        $content = $content.Substring(1)
        $needsFix = $true
        $fixReasons += "BOM"
        $bomFixedFiles++
    }

    # 2. 修复缺失的类声明（Controller文件）
    if ($file.Name -like "*Controller.java") {
        # 检查是否在@RestController注解后缺少类声明
        if ($content -match '@RestController\r?\n.*?\r?\n@RequestMapping\([^\)]+\)\r?\n[^\r\n]*\r?\n@\w+\([^)]+\)\r?\n\{') {
            Write-Host "修复 Controller 类声明: $($file.Name)" -ForegroundColor Green
            $content = $content -replace '(\@(?:PermissionCheck|Tag|Tag)\([^)]+\))\r?\n\{', '$1' + [Environment]::NewLine + 'public class ' + ($file.BaseName) + ' {'
            $needsFix = $true
            $fixReasons += "类声明"
            $classFixedFiles++
        }

        # 检查其他常见的类声明缺失模式
        elseif ($content -match '@RestController\r?\n.*?\r?\n@RequestMapping\([^\)]+\)\r?\n\{') {
            Write-Host "修复 Controller 基础类声明: $($file.Name)" -ForegroundColor Green
            $content = $content -replace '(@RequestMapping\([^\)]+\))\r?\n\{', '$1' + [Environment]::NewLine + 'public class ' + ($file.BaseName) + ' {'
            $needsFix = $true
            $fixReasons += "基础类声明"
            $classFixedFiles++
        }
    }

    # 3. 修复重复的类声明
    if ($content -match 'public class \w+\r?\npublic class \w+') {
        Write-Host "修复重复类声明: $($file.Name)" -ForegroundColor Yellow
        $content = $content -replace 'public class \w+\r?\n(public class \w+)', '$1'
        $needsFix = $true
        $fixReasons += "重复类声明"
    }

    # 4. 修复语法错误 - 移除孤立的字符
    $content = $content -replace '\r?\n[nN]public class', [Environment]::NewLine + 'public class'
    $content = $content -replace '\r?\nppublic class', [Environment]::NewLine + 'public class'

    if ($needsFix -and $originalContent -ne $content) {
        # 确保文件以换行符结尾
        if (-not $content.EndsWith("`n")) {
            $content += "`n"
        }

        # 以UTF-8无BOM格式保存
        [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
        Write-Host "✓ 已修复: $($file.Name) - 原因: $($fixReasons -join ', ')" -ForegroundColor Green
        $fixedFiles++
    }
}

Write-Host "`n=== 紧急修复完成 ===" -ForegroundColor Green
Write-Host "修复文件数: $fixedFiles" -ForegroundColor Green
Write-Host "BOM修复数: $bomFixedFiles" -ForegroundColor Cyan
Write-Host "类声明修复数: $classFixedFiles" -ForegroundColor Yellow

# 快速编译检查
Write-Host "`n正在快速编译检查..." -ForegroundColor Yellow
Set-Location $serviceFullPath

# 先检查语法错误
$compileResult = mvn compile -q 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 编译成功！所有文件损坏问题已修复" -ForegroundColor Green
} else {
    Write-Host "⚠️ 仍有编译问题，正在详细分析..." -ForegroundColor Yellow

    # 显示前20个错误
    $errors = $compileResult -split "`n" | Where-Object { $_ -match "ERROR" } | Select-Object -First 20
    foreach ($error in $errors) {
        Write-Host "  $error" -ForegroundColor Red
    }

    Write-Host "`n建议手动检查以下文件类型:" -ForegroundColor Yellow
    Write-Host "1. Controller文件 - 检查类声明" -ForegroundColor Yellow
    Write-Host "2. Manager文件 - 检查构造函数" -ForegroundColor Yellow
    Write-Host "3. Entity文件 - 检查注解语法" -ForegroundColor Yellow
}

Write-Host "=== 脚本执行完成 ===" -ForegroundColor Green