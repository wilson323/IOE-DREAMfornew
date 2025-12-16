# =====================================================
# 批量修复所有测试文件导入脚本（增强版）
# Version: v1.0.0
# Description: 自动修复所有测试文件中的导入问题
# Created: 2025-01-30
# =====================================================

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Fix All Test Imports (Enhanced)" -ForegroundColor Cyan
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

        # 获取ServiceImpl文件路径
        # 测试文件路径: .../service/{Service}Test.java
        # ServiceImpl路径: .../service/impl/{Service}Impl.java
        $testPath = $testFile.FullName
        $basePath = $testPath -replace "\\src\\test\\java.*$", ""
        $testPackagePath = $testPath -replace ".*\\src\\test\\java\\", "" -replace "\\[^\\]+\.java$", ""
        $testFileName = $testFile.BaseName -replace "Test$", ""

        # 尝试多个可能的路径
        $possiblePaths = @(
            (Join-Path $basePath "src\main\java\$testPackagePath\impl\${testFileName}Impl.java"),
            (Join-Path $basePath "src\main\java\$testPackagePath\${testFileName}.java"),
            ($testPath -replace "\\src\\test\\java", "\src\main\java" -replace "Test\.java$", ".java"),
            ($testPath -replace "\\src\\test\\java", "\src\main\java" -replace "Test\.java$", "Impl.java")
        )

        $serviceImplPath = $null
        foreach ($path in $possiblePaths) {
            if (Test-Path $path) {
                $serviceImplPath = $path
                break
            }
        }

        if (-not (Test-Path $serviceImplPath)) {
            Write-Host "[SKIP] ServiceImpl not found: $serviceImplPath" -ForegroundColor Gray
            $skippedCount++
            continue
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

                # 如果类型不匹配，需要修复
                if ($mockType -ne $shortType) {
                    # 检查是否已导入正确的类型
                    if ($content -notmatch "import\s+$([regex]::Escape($fullType));") {
                        $importsToAdd += $fullType
                    }

                    # 记录需要修复的字段
                    $fieldsToFix += @{
                        FieldName = $fieldName
                        OldType = $mockType
                        NewType = $shortType
                        FullType = $fullType
                    }
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
                Write-Host "[FIX] Fixed @Mock type: $($field.FieldName) ($($field.OldType) -> $($field.NewType)) in $($testFile.Name)" -ForegroundColor Yellow
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

