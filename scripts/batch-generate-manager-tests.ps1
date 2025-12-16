# =====================================================
# 批量生成Manager层测试文件
# 版本: v1.0.0
# 描述: 为所有Manager类批量生成测试文件模板
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$Force = $false
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Green
Write-Host "Batch Generate Manager Test Files" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""

# 查找所有Manager类
$managerFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Manager.java" | Where-Object {
    $_.FullName -match "manager" -and
    $_.FullName -notmatch "test" -and
    $_.FullName -notmatch "impl"
}

Write-Host "Found $($managerFiles.Count) Manager classes" -ForegroundColor Cyan
Write-Host ""

$generatedCount = 0
$skippedCount = 0
$errorCount = 0

foreach ($managerFile in $managerFiles) {
    try {
        $testFile = $managerFile.FullName -replace "\\main\\java", "\test\java" -replace "Manager\.java$", "ManagerTest.java"

        # 检查文件是否已存在
        if ((Test-Path $testFile) -and -not $Force) {
            Write-Host "[SKIP] Test file already exists: $($managerFile.Name)" -ForegroundColor Yellow
            $skippedCount++
            continue
        }

        # 调用生成脚本
        $result = & ".\scripts\generate-manager-test-template.ps1" -ManagerPath $managerFile.FullName -Force:$Force 2>&1

        if ($LASTEXITCODE -eq 0) {
            $generatedCount++
        } else {
            Write-Host "[ERROR] Failed to generate test for: $($managerFile.Name)" -ForegroundColor Red
            $errorCount++
        }
    } catch {
        Write-Host "[ERROR] Exception generating test for $($managerFile.Name): $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Green
Write-Host "Summary" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host "Total Manager classes: $($managerFiles.Count)" -ForegroundColor Cyan
Write-Host "Generated test files: $generatedCount" -ForegroundColor Green
Write-Host "Skipped (already exist): $skippedCount" -ForegroundColor Yellow
Write-Host "Errors: $errorCount" -ForegroundColor Red
Write-Host ""

