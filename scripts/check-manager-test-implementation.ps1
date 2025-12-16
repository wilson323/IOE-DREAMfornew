# =====================================================
# 检查Manager测试文件实现状态
# 版本: v1.0.0
# 描述: 检查Manager测试文件是否需要补充测试逻辑
# =====================================================

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Green
Write-Host "Check Manager Test Implementation Status" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""

$managerTestFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*ManagerTest.java" | Where-Object {
    $_.FullName -match "test"
}

Write-Host "Found $($managerTestFiles.Count) Manager test files" -ForegroundColor Cyan
Write-Host ""

$emptyTests = 0
$implementedTests = 0
$totalTestMethods = 0
$emptyTestMethods = 0

foreach ($testFile in $managerTestFiles) {
    $content = Get-Content $testFile.FullName -Raw -ErrorAction SilentlyContinue

    if ($null -eq $content) {
        continue
    }

    # 检查是否有空的测试方法（只有Given/When/Then注释，没有实际代码）
    $emptyPattern = [regex]::new("void\s+test_\w+\(\)\s*\{[^}]*//\s*Given[^}]*//\s*When[^}]*//\s*Then[^}]*\}", [System.Text.RegularExpressions.RegexOptions]::Singleline)
    $hasEmptyTests = $emptyPattern.IsMatch($content)

    # 检查是否有TODO注释
    $hasTodo = $content -match "TODO"

    # 统计测试方法数量
    $testMethodPattern = [regex]::new("void\s+test_\w+\(\)", [System.Text.RegularExpressions.RegexOptions]::Singleline)
    $testMethods = $testMethodPattern.Matches($content)
    $totalTestMethods += $testMethods.Count

    if ($hasEmptyTests -or $hasTodo) {
        $emptyTests++
        $emptyTestMethods += $testMethods.Count
        Write-Host "[EMPTY] $($testFile.Name)" -ForegroundColor Yellow
    } else {
        $implementedTests++
        Write-Host "[OK] $($testFile.Name)" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Green
Write-Host "Summary" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host "Total Manager test files: $($managerTestFiles.Count)" -ForegroundColor Cyan
Write-Host "Files with implemented tests: $implementedTests" -ForegroundColor Green
Write-Host "Files with empty tests: $emptyTests" -ForegroundColor Yellow
Write-Host "Total test methods: $totalTestMethods" -ForegroundColor Cyan
Write-Host "Empty test methods: $emptyTestMethods" -ForegroundColor Yellow
Write-Host ""

if ($emptyTests -gt 0) {
    $percentage = [math]::Round(($implementedTests / $managerTestFiles.Count) * 100, 1)
    Write-Host "Implementation progress: $percentage%" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "  1. Implement test logic for empty test methods" -ForegroundColor White
    Write-Host "  2. Remove TODO comments" -ForegroundColor White
    Write-Host "  3. Add assertions and verifications" -ForegroundColor White
}

