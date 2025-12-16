# =====================================================
# 批量生成Controller层测试文件
# 版本: v1.0.0
# 描述: 为所有Controller类批量生成测试文件模板
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$Force = $false
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Green
Write-Host "Batch Generate Controller Test Files" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""

# 查找所有Controller类
$controllerFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Controller.java" | Where-Object {
    $_.FullName -match "controller" -and
    $_.FullName -notmatch "test"
}

Write-Host "Found $($controllerFiles.Count) Controller classes" -ForegroundColor Cyan
Write-Host ""

$generatedCount = 0
$skippedCount = 0
$errorCount = 0

foreach ($controllerFile in $controllerFiles) {
    try {
        $testFile = $controllerFile.FullName -replace "\\main\\java", "\test\java" -replace "Controller\.java$", "ControllerTest.java"

        # 检查文件是否已存在
        if ((Test-Path $testFile) -and -not $Force) {
            Write-Host "[SKIP] Test file already exists: $($controllerFile.Name)" -ForegroundColor Yellow
            $skippedCount++
            continue
        }

        # 调用生成脚本
        $result = & ".\scripts\generate-controller-test-template.ps1" -ControllerPath $controllerFile.FullName -Force:$Force 2>&1

        if ($LASTEXITCODE -eq 0) {
            $generatedCount++
        } else {
            Write-Host "[ERROR] Failed to generate test for: $($controllerFile.Name)" -ForegroundColor Red
            $errorCount++
        }
    } catch {
        Write-Host "[ERROR] Exception generating test for $($controllerFile.Name): $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Green
Write-Host "Summary" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host "Total Controller classes: $($controllerFiles.Count)" -ForegroundColor Cyan
Write-Host "Generated test files: $generatedCount" -ForegroundColor Green
Write-Host "Skipped (already exist): $skippedCount" -ForegroundColor Yellow
Write-Host "Errors: $errorCount" -ForegroundColor Red
Write-Host ""

