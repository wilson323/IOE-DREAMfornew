# ============================================================
# IOE-DREAM @Repository违规检查脚本
#
# 功能：专门检查@Repository注解违规（应使用@Mapper）
# 输出：JSON/CSV报告 + 修复建议
# ============================================================

param(
    [switch]$Detailed = $false,
    [string]$OutputFormat = "json",  # json, csv, both
    [string]$OutputDir = "reports",
    [switch]$CI = $false  # CI/CD模式（非交互）
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$microservicesDir = Join-Path $projectRoot "microservices"
$reportsDir = Join-Path $projectRoot $OutputDir

# 创建报告目录
if (-not (Test-Path $reportsDir)) {
    New-Item -ItemType Directory -Path $reportsDir -Force | Out-Null
}

$violations = @()
$exitCode = 0

if (-not $CI) {
    Write-Host "===== 检查@Repository违规 =====" -ForegroundColor Cyan
    Write-Host ""
}

# 检查@Repository违规
$repositoryFiles = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*.java" |
Where-Object { $_.FullName -notlike "*\target\*" -and $_.FullName -notlike "*\test\*" }

foreach ($file in $repositoryFiles) {
    $content = Get-Content $file.FullName -Raw
    $lines = Get-Content $file.FullName

    # 检查@Repository注解
    $lineNumber = 0
    foreach ($line in $lines) {
        $lineNumber++

        # 跳过注释和文档中的@Repository
        if ($line -match '^\s*@Repository' -and
            $line -notmatch '禁止|禁止使用|禁止@Repository|//.*@Repository|/\*.*@Repository|\*.*@Repository') {

            # 获取类名
            $className = ""
            $interfaceName = ""
            $nextLines = $lines[$lineNumber..([Math]::Min($lineNumber + 10, $lines.Length - 1))]
            foreach ($nextLine in $nextLines) {
                if ($nextLine -match '^\s*(public\s+)?(abstract\s+)?(interface|class)\s+(\w+)') {
                    if ($matches[3] -eq "interface") {
                        $interfaceName = $matches[4]
                    }
                    else {
                        $className = $matches[4]
                    }
                    break
                }
            }

            $violation = @{
                File           = $file.FullName.Replace($projectRoot, "").TrimStart('\', '/')
                LineNumber     = $lineNumber
                Line           = $line.Trim()
                ClassName      = if ($className) { $className } else { $interfaceName }
                Recommendation = "将 @Repository 替换为 @Mapper"
                FixCommand     = "替换行 $lineNumber 的 @Repository 为 @Mapper"
            }
            $violations += $violation

            if (-not $CI -and $Detailed) {
                Write-Host "  [违规] $($violation.File):$lineNumber" -ForegroundColor Red
                Write-Host "    代码: $($line.Trim())" -ForegroundColor Gray
                Write-Host "    建议: $($violation.Recommendation)" -ForegroundColor Yellow
            }
        }
    }
}

# 生成报告
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportData = @{
    CheckType       = "RepositoryViolations"
    Timestamp       = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    TotalViolations = $violations.Count
    Violations      = $violations
}

# JSON报告
if ($OutputFormat -eq "json" -or $OutputFormat -eq "both") {
    $jsonPath = Join-Path $reportsDir "repository-violations_$timestamp.json"
    $reportData | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonPath -Encoding UTF8
    if (-not $CI) {
        Write-Host "JSON报告已生成: $jsonPath" -ForegroundColor Green
    }
}

# CSV报告
if ($OutputFormat -eq "csv" -or $OutputFormat -eq "both") {
    $csvPath = Join-Path $reportsDir "repository-violations_$timestamp.csv"
    $violations | Select-Object File, LineNumber, Line, ClassName, Recommendation |
    Export-Csv -Path $csvPath -Encoding UTF8 -NoTypeInformation
    if (-not $CI) {
        Write-Host "CSV报告已生成: $csvPath" -ForegroundColor Green
    }
}

# 输出摘要
if (-not $CI) {
    Write-Host ""
    Write-Host "===== 检查完成 =====" -ForegroundColor Cyan
    Write-Host "  发现违规: $($violations.Count) 个" -ForegroundColor $(if ($violations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host ""
}

# 设置退出码
if ($violations.Count -gt 0) {
    $exitCode = 1
}

exit $exitCode

