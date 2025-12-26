# ============================================================
# IOE-DREAM Jakarta EE迁移违规检查脚本
#
# 功能：检查javax包名违规（应使用jakarta包名）
# 输出：JSON/CSV报告 + 修复建议
# ============================================================

param(
    [switch]$Detailed = $false,
    [string]$OutputFormat = "json",
    [string]$OutputDir = "reports",
    [switch]$CI = $false
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$microservicesDir = Join-Path $projectRoot "microservices"
$reportsDir = Join-Path $projectRoot $OutputDir

if (-not (Test-Path $reportsDir)) {
    New-Item -ItemType Directory -Path $reportsDir -Force | Out-Null
}

$violations = @()
$exitCode = 0

# Jakarta EE迁移映射
$jakartaMapping = @{
    "javax.annotation"  = "jakarta.annotation"
    "javax.validation"  = "jakarta.validation"
    "javax.persistence" = "jakarta.persistence"
    "javax.servlet"     = "jakarta.servlet"
    "javax.transaction" = "jakarta.transaction"
    "javax.inject"      = "jakarta.inject"
}

# 允许的javax包（Java SE标准库）
$allowedJavaxPackages = @(
    "javax.crypto",
    "javax.sql",
    "javax.imageio",
    "javax.net.ssl"
)

if (-not $CI) {
    Write-Host "===== 检查Jakarta EE迁移违规 =====" -ForegroundColor Cyan
    Write-Host ""
}

# 检查javax包名违规
$javaFiles = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*.java" |
Where-Object { $_.FullName -notlike "*\target\*" -and $_.FullName -notlike "*\test\*" }

foreach ($file in $javaFiles) {
    $lines = Get-Content $file.FullName

    $lineNumber = 0
    foreach ($line in $lines) {
        $lineNumber++

        # 检查import语句中的javax包
        if ($line -match '^import\s+javax\.([\w.]+)') {
            $packagePath = "javax.$($matches[1])"

            # 检查是否在允许列表中
            $isAllowed = $false
            foreach ($allowed in $allowedJavaxPackages) {
                if ($packagePath.StartsWith($allowed)) {
                    $isAllowed = $true
                    break
                }
            }

            # 检查是否需要迁移到jakarta
            if (-not $isAllowed) {
                $jakartaPackage = $null
                foreach ($key in $jakartaMapping.Keys) {
                    if ($packagePath.StartsWith($key)) {
                        $jakartaPackage = $packagePath.Replace($key, $jakartaMapping[$key])
                        break
                    }
                }

                if ($jakartaPackage) {
                    $violation = @{
                        File           = $file.FullName.Replace($projectRoot, "").TrimStart('\', '/')
                        LineNumber     = $lineNumber
                        Line           = $line.Trim()
                        CurrentPackage = $packagePath
                        TargetPackage  = $jakartaPackage
                        Recommendation = "将 $packagePath 替换为 $jakartaPackage"
                        FixCommand     = "替换行 $lineNumber 的 import 语句"
                    }
                    $violations += $violation

                    if (-not $CI -and $Detailed) {
                        Write-Host "  [违规] $($violation.File):$lineNumber" -ForegroundColor Red
                        Write-Host "    当前: $packagePath" -ForegroundColor Gray
                        Write-Host "    应改为: $jakartaPackage" -ForegroundColor Yellow
                    }
                }
            }
        }
    }
}

# 生成报告
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportData = @{
    CheckType        = "JakartaEEViolations"
    Timestamp        = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    TotalViolations  = $violations.Count
    Violations       = $violations
    MigrationMapping = $jakartaMapping
}

# JSON报告
if ($OutputFormat -eq "json" -or $OutputFormat -eq "both") {
    $jsonPath = Join-Path $reportsDir "jakarta-violations_$timestamp.json"
    $reportData | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonPath -Encoding UTF8
    if (-not $CI) {
        Write-Host "JSON报告已生成: $jsonPath" -ForegroundColor Green
    }
}

# CSV报告
if ($OutputFormat -eq "csv" -or $OutputFormat -eq "both") {
    $csvPath = Join-Path $reportsDir "jakarta-violations_$timestamp.csv"
    $violations | Select-Object File, LineNumber, CurrentPackage, TargetPackage, Recommendation |
    Export-Csv -Path $csvPath -Encoding UTF8 -NoTypeInformation
    if (-not $CI) {
        Write-Host "CSV报告已生成: $csvPath" -ForegroundColor Green
    }
}

if (-not $CI) {
    Write-Host ""
    Write-Host "===== 检查完成 =====" -ForegroundColor Cyan
    Write-Host "  发现违规: $($violations.Count) 个" -ForegroundColor $(if ($violations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host ""
}

if ($violations.Count -gt 0) {
    $exitCode = 1
}

exit $exitCode

