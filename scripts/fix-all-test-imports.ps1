# =====================================================
# 批量修复所有测试文件导入脚本
# Version: v1.0.0
# Description: 自动修复所有测试文件中的导入问题
# Created: 2025-01-30
# =====================================================

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Fix All Test Imports" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path -Parent $PSScriptRoot
$testFiles = Get-ChildItem -Path (Join-Path $projectRoot "microservices") -Recurse -Filter "*ServiceImplTest.java"

$fixedCount = 0
$errorCount = 0

foreach ($testFile in $testFiles) {
    try {
        $content = Get-Content $testFile.FullName -Raw -Encoding UTF8
        $originalContent = $content
        $needsFix = $false

        # 获取ServiceImpl文件路径
        $serviceImplPath = $testFile.FullName -replace "\\src\\test\\java", "\src\main\java" -replace "Test\.java$", ".java"

        if (Test-Path $serviceImplPath) {
            $serviceImplContent = Get-Content $serviceImplPath -Raw -Encoding UTF8

            # 提取@Resource依赖
            $resourceMatches = [regex]::Matches($serviceImplContent, "@Resource\s+private\s+([\w.]+)\s+(\w+);")

            foreach ($match in $resourceMatches) {
                $type = $match.Groups[1].Value
                $name = $match.Groups[2].Value

                # 检查测试文件中是否有@Mock字段使用这个类型
                if ($content -match "@Mock\s+private\s+(\w+)\s+$name;") {
                    $mockType = $matches[1]

                    # 如果类型不匹配，需要修复
                    if ($mockType -ne $type.Split('.')[-1]) {
                        # 检查是否已导入正确的类型
                        if ($content -notmatch "import\s+$([regex]::Escape($type));") {
                            # 添加导入
                            $importLine = "import $type;"
                            if ($content -match "(package\s+[^;]+;)") {
                                $content = $content -replace "($1)", "`$1`n`n$importLine"
                                Write-Host "[FIX] Added import: $type in $($testFile.Name)" -ForegroundColor Yellow
                                $needsFix = $true
                            }
                        }

                        # 修复@Mock字段类型
                        $content = $content -replace "@Mock\s+private\s+\w+\s+$name;", "@Mock`n    private $($type.Split('.')[-1]) $name;"
                        Write-Host "[FIX] Fixed @Mock type: $name in $($testFile.Name)" -ForegroundColor Yellow
                        $needsFix = $true
                    }
                }
            }

            # 如果内容有变化，写入文件
            if ($needsFix) {
                [System.IO.File]::WriteAllText($testFile.FullName, $content, [System.Text.UTF8Encoding]::new($false))
                $fixedCount++
            }
        }
    } catch {
        Write-Host "[ERROR] Failed to fix: $($testFile.Name) - $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Summary" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "[OK] Fixed: $fixedCount" -ForegroundColor Green
Write-Host "[ERROR] Errors: $errorCount" -ForegroundColor Red
Write-Host ""

