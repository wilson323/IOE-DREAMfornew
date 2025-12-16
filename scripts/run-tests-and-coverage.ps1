# =====================================================
# 运行测试并生成覆盖率报告
# 版本: v1.0.0
# 描述: 运行所有测试并生成JaCoCo覆盖率报告
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$ServiceName = "",

    [Parameter(Mandatory=$false)]
    [switch]$SkipTests = $false
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Green
Write-Host "Run Tests and Generate Coverage Report" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""

$microservicesPath = "microservices"

if ($SkipTests) {
    Write-Host "Skipping tests, generating coverage report only..." -ForegroundColor Yellow
    $mvnCommand = "mvn jacoco:report"
} else {
    Write-Host "Running tests and generating coverage report..." -ForegroundColor Cyan
    $mvnCommand = "mvn clean test jacoco:report"
}

if ($ServiceName) {
    Write-Host "Target service: $ServiceName" -ForegroundColor Cyan
    $mvnCommand = $mvnCommand + " -pl " + $ServiceName
}

Write-Host ""
Write-Host "Command: $mvnCommand" -ForegroundColor Gray
Write-Host ""

# 切换到microservices目录
Push-Location $microservicesPath

try {
    # 运行Maven命令
    Invoke-Expression $mvnCommand

    Write-Host ""
    Write-Host "================================================" -ForegroundColor Green
    Write-Host "Coverage Report Generated" -ForegroundColor Green
    Write-Host "================================================" -ForegroundColor Green
    Write-Host ""

    # 查找覆盖率报告
    $coverageReports = Get-ChildItem -Path . -Recurse -Filter "index.html" | Where-Object {
        $_.FullName -match "jacoco" -and $_.FullName -match "site"
    }

    if ($coverageReports.Count -gt 0) {
        Write-Host "Coverage reports found:" -ForegroundColor Cyan
        foreach ($report in $coverageReports) {
            Write-Host "  - $($report.FullName)" -ForegroundColor White
        }
        Write-Host ""
        Write-Host "Open reports in browser to view coverage details" -ForegroundColor Yellow
    } else {
        Write-Host "No coverage reports found. Make sure tests ran successfully." -ForegroundColor Yellow
    }

} catch {
    Write-Host "Error running tests: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    Pop-Location
}

Write-Host ""

