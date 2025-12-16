# =====================================================
# 修复剩余测试文件导入问题脚本
# Version: v1.0.0
# Description: 自动修复所有测试文件中剩余的导入问题
# Created: 2025-01-30
# =====================================================

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Fix Remaining Test Imports" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path -Parent $PSScriptRoot
$testFiles = Get-ChildItem -Path (Join-Path $projectRoot "microservices") -Recurse -Filter "*ServiceImplTest.java"

$fixedCount = 0
$errorCount = 0
$skippedCount = 0

foreach ($testFile in $testFiles) {
    try {
        $content = Get-Content $testFile.FullName -Raw -Encoding UTF8
        $originalContent = $content

        # 提取包路径
        $packageMatch = [regex]::Match($content, "package\s+([^;]+);")
        if (-not $packageMatch.Success) {
            Write-Host "[SKIP] Cannot extract package from: $($testFile.Name)" -ForegroundColor Gray
            $skippedCount++
            continue
        }

        $testPackage = $packageMatch.Groups[1].Value
        $servicePackage = $testPackage -replace "\.service$", ".service.impl"
        $testFileName = $testFile.BaseName -replace "Test$", ""

        # 构建可能的ServiceImpl路径
        $basePath = $testFile.FullName -replace "\\src\\test\\java.*$", ""
        $serviceImplPath = Join-Path $basePath "src\main\java\$($servicePackage -replace '\.', '\')\${testFileName}Impl.java"

        if (-not (Test-Path $serviceImplPath)) {
            # 尝试其他可能的路径
            $altPath = Join-Path $basePath "src\main\java\$($testPackage -replace '\.', '\')\impl\${testFileName}Impl.java"
            if (Test-Path $altPath) {
                $serviceImplPath = $altPath
            } else {
                Write-Host "[SKIP] ServiceImpl not found for: $($testFile.Name)" -ForegroundColor Gray
                $skippedCount++
                continue
            }
        }

        $serviceImplContent = Get-Content $serviceImplPath -Raw -Encoding UTF8

        # 提取@Resource依赖
        $resourceMatches = [regex]::Matches($serviceImplContent, "@Resource\s+private\s+([\w.]+)\s+(\w+);")

        $importsToAdd = @()
        $fieldsToFix = @()

        foreach ($match in $resourceMatches) {
            $fullType = $match.Groups[1].Value
            $fieldName = $match.Groups[2].Value
            $shortType = $fullType.Split('.')[-1]

            # 检查测试文件中是否有@Mock字段使用这个字段名
            if ($content -match "@Mock\s+private\s+(\w+)\s+$fieldName;") {
                $mockType = $matches[1]

                # 如果类型不匹配或缺少导入，需要修复
                if ($mockType -ne $shortType) {
                    if ($content -notmatch "import\s+$([regex]::Escape($fullType));") {
                        $importsToAdd += $fullType
                    }
                    $fieldsToFix += @{
                        FieldName = $fieldName
                        OldType = $mockType
                        NewType = $shortType
                        FullType = $fullType
                    }
                } elseif ($content -notmatch "import\s+$([regex]::Escape($fullType));") {
                    # 类型匹配但缺少导入
                    $importsToAdd += $fullType
                }
            }
        }

        # 添加缺失的导入
        if ($importsToAdd.Count -gt 0) {
            $packageMatch = [regex]::Match($content, "(package\s+[^;]+;)")
            if ($packageMatch.Success) {
                $importSection = ""
                foreach ($import in $importsToAdd | Sort-Object -Unique) {
                    $importSection += "import $import;`n"
                }
                $content = $content -replace "($($packageMatch.Groups[1].Value))", "`$1`n`n$importSection"
                Write-Host "[FIX] Added imports in $($testFile.Name)" -ForegroundColor Yellow
            }
        }

        # 修复@Mock字段类型
        foreach ($field in $fieldsToFix) {
            $oldPattern = "@Mock\s+private\s+$($field.OldType)\s+$($field.FieldName);"
            $newReplacement = "@Mock`n    private $($field.NewType) $($field.FieldName);"
            if ($content -match $oldPattern) {
                $content = $content -replace $oldPattern, $newReplacement
                Write-Host "[FIX] Fixed @Mock type: $($field.FieldName) in $($testFile.Name)" -ForegroundColor Yellow
            }
        }

        # 如果内容有变化，写入文件
        if ($content -ne $originalContent) {
            [System.IO.File]::WriteAllText($testFile.FullName, $content, [System.Text.UTF8Encoding]::new($false))
            $fixedCount++
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
Write-Host "[SKIP] Skipped: $skippedCount" -ForegroundColor Yellow
Write-Host "[ERROR] Errors: $errorCount" -ForegroundColor Red
Write-Host ""

