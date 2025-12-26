# ============================================================
# IOE-DREAM @Autowired违规检查脚本
#
# 功能：专门检查@Autowired注解违规（应使用@Resource）
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

if (-not $CI) {
    Write-Host "===== 检查@Autowired违规 =====" -ForegroundColor Cyan
    Write-Host ""
}

# 检查@Autowired违规
$javaFiles = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*.java" | 
    Where-Object { $_.FullName -notlike "*\target\*" -and $_.FullName -notlike "*\test\*" }

foreach ($file in $javaFiles) {
    $lines = Get-Content $file.FullName
    
    $lineNumber = 0
    foreach ($line in $lines) {
        $lineNumber++
        
        # 检查@Autowired注解
        if ($line -match '^\s*@Autowired' -and 
            $line -notmatch '禁止|禁止使用|禁止@Autowired|//.*@Autowired|/\*.*@Autowired|\*.*@Autowired') {
            
            # 获取字段名
            $fieldName = ""
            $nextLines = $lines[$lineNumber..([Math]::Min($lineNumber + 3, $lines.Length - 1))]
            foreach ($nextLine in $nextLines) {
                if ($nextLine -match '^\s*(private|protected|public)\s+.*?(\w+)\s*;') {
                    $fieldName = $matches[2]
                    break
                }
            }
            
            $violation = @{
                File = $file.FullName.Replace($projectRoot, "").TrimStart('\', '/')
                LineNumber = $lineNumber
                Line = $line.Trim()
                FieldName = $fieldName
                Recommendation = "将 @Autowired 替换为 @Resource"
                FixCommand = "替换行 $lineNumber 的 @Autowired 为 @Resource"
            }
            $violations += $violation
            
            if (-not $CI -and $Detailed) {
                Write-Host "  [违规] $($violation.File):$lineNumber" -ForegroundColor Red
                Write-Host "    代码: $($line.Trim())" -ForegroundColor Gray
                if ($fieldName) {
                    Write-Host "    字段: $fieldName" -ForegroundColor Gray
                }
                Write-Host "    建议: $($violation.Recommendation)" -ForegroundColor Yellow
            }
        }
    }
}

# 生成报告
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportData = @{
    CheckType = "AutowiredViolations"
    Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    TotalViolations = $violations.Count
    Violations = $violations
}

# JSON报告
if ($OutputFormat -eq "json" -or $OutputFormat -eq "both") {
    $jsonPath = Join-Path $reportsDir "autowired-violations_$timestamp.json"
    $reportData | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonPath -Encoding UTF8
    if (-not $CI) {
        Write-Host "JSON报告已生成: $jsonPath" -ForegroundColor Green
    }
}

# CSV报告
if ($OutputFormat -eq "csv" -or $OutputFormat -eq "both") {
    $csvPath = Join-Path $reportsDir "autowired-violations_$timestamp.csv"
    $violations | Select-Object File, LineNumber, Line, FieldName, Recommendation | 
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

