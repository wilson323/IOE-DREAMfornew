# =====================================================
# 修复测试文件生成错误
# 版本: v1.0.0
# 描述: 修复测试文件中的BOM和变量名缺失问题
# =====================================================

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Green
Write-Host "Fix Test Files" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""

$fixedCount = 0
$errorCount = 0

# 查找所有测试文件
$testFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Test.java" | Where-Object {
    $_.FullName -match "test"
}

foreach ($file in $testFiles) {
    try {
        $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
        $originalContent = $content
        $modified = $false

        # 修复1: "ackage" -> "package"
        if ($content -match "^ackage ") {
            $content = $content -replace "^ackage ", "package "
            $modified = $true
        }

        # 修复2: 修复缺少变量名的声明
        # 匹配模式: "private ClassName ;" -> "private ClassName className;"
        $content = [regex]::Replace($content, 'private\s+(\w+Manager)\s+;', {
            param($match)
            $className = $match.Groups[1].Value
            $varName = $className.Substring(0, 1).ToLower() + $className.Substring(1)
            "private $className $varName;"
        })
        if ($content -ne $originalContent) {
            $modified = $true
        }

        # 修复3: 修复缺少变量名的赋值
        # 匹配模式: "= new ClassName(" -> "className = new ClassName("
        $content = [regex]::Replace($content, '=\s+new\s+(\w+Manager)\s*\(', {
            param($match)
            $className = $match.Groups[1].Value
            $varName = $className.Substring(0, 1).ToLower() + $className.Substring(1)
            "$varName = new $className("
        })
        if ($content -ne $originalContent) {
            $modified = $true
        }

        # 修复4: 修复String类型的@Mock（不应该Mock String）
        $content = [regex]::Replace($content, '@Mock\s+private String (\w+);', {
            param($match)
            $varName = $match.Groups[1].Value
            "private String $varName = `"test`";"
        })
        if ($content -ne $originalContent) {
            $modified = $true
        }

        if ($modified) {
            $utf8NoBom = New-Object System.Text.UTF8Encoding $false
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            Write-Host "[OK] Fixed: $($file.Name)" -ForegroundColor Green
            $fixedCount++
        }
    } catch {
        Write-Host "[ERROR] Failed to fix $($file.Name): $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Green
Write-Host "Summary" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host "Total files: $($testFiles.Count)" -ForegroundColor Cyan
Write-Host "Fixed: $fixedCount" -ForegroundColor Green
Write-Host "Errors: $errorCount" -ForegroundColor Red
Write-Host ""

