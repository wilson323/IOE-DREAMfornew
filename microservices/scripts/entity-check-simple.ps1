# Entity规范检查脚本（简化版）

param(
    [string]$ProjectRoot = ".",
    [switch]$Report = $true
)

Write-Host "Entity Specification Check" -ForegroundColor Green
Write-Host "Checking project: $ProjectRoot" -ForegroundColor Cyan
Write-Host ""

# 统计变量
$totalFiles = 0
$largeEntities = 0
$violations = 0

# 获取所有Entity文件
$entityFiles = Get-ChildItem -Path $ProjectRoot -Recurse -Filter "*Entity.java" | Where-Object { $_.FullName -notlike "*test*" }

Write-Host "Found $($entityFiles.Count) Entity files..." -ForegroundColor Yellow

foreach ($file in $entityFiles) {
    $totalFiles++
    $lineCount = (Get-Content $file.FullName | Measure-Object -Line).Lines
    $relativePath = $file.FullName.Replace($ProjectRoot, "").Replace("\", "/").TrimStart("/")

    # 检查超大实体
    if ($lineCount -gt 400) {
        Write-Host "ERROR: $relativePath - Large entity: $lineCount lines (exceeds 400 limit)" -ForegroundColor Red
        $largeEntities++
        $violations++
    }
    elseif ($lineCount -gt 200) {
        Write-Host "WARN:  $relativePath - Large entity: $lineCount lines (recommend <=200)" -ForegroundColor Yellow
        $violations++
    }
    else {
        Write-Host "OK:    $relativePath - $lineCount lines" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "Total files: $totalFiles" -ForegroundColor White
Write-Host "Large entities (>400 lines): $largeEntities" -ForegroundColor Red
Write-Host "Total violations: $violations" -ForegroundColor Yellow

$complianceRate = if ($totalFiles -gt 0) { [math]::Round((($totalFiles - $largeEntities) / $totalFiles) * 100, 2) } else { 0 }
Write-Host "Compliance rate: $complianceRate%" -ForegroundColor $(if ($complianceRate -ge 80) { "Green" } else { "Red" })

if ($Report) {
    $reportPath = Join-Path $ProjectRoot "entity-check-report.txt"
    "Entity Specification Check Report`r`n" +
    "Date: $(Get-Date)`r`n" +
    "Total files: $totalFiles`r`n" +
    "Large entities: $largeEntities`r`n" +
    "Compliance rate: $complianceRate%" | Out-File -FilePath $reportPath
    Write-Host "Report saved to: $reportPath" -ForegroundColor Green
}

if ($complianceRate -ge 80) {
    Write-Host "PASS: Entity specification check passed!" -ForegroundColor Green
} else {
    Write-Host "FAIL: Entity specification needs improvement!" -ForegroundColor Red
}