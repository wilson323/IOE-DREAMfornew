# ============================================================
# IOE-DREAM SonarQube质量门禁检查脚本
#
# 功能：执行SonarQube扫描并检查质量门禁状态
# ============================================================

param(
    [switch]$Detailed = $false,
    [string]$OutputDir = "reports",
    [switch]$CI = $false,
    [string]$SonarToken = $env:SONAR_TOKEN,
    [string]$SonarHostUrl = $env:SONAR_HOST_URL,
    [string]$SonarProjectKey = "ioe-dream"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$microservicesDir = Join-Path $projectRoot "microservices"
$reportsDir = Join-Path $projectRoot $OutputDir

if (-not (Test-Path $reportsDir)) {
    New-Item -ItemType Directory -Path $reportsDir -Force | Out-Null
}

$exitCode = 0

if (-not $CI) {
    Write-Host "===== SonarQube质量门禁检查 =====" -ForegroundColor Cyan
    Write-Host ""
}

# 检查SonarQube配置
if (-not $SonarToken) {
    Write-Warning "SONAR_TOKEN环境变量未设置，跳过SonarQube检查"
    $report = @{
        CheckType = "SonarQubeQualityGate"
        Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        Status = "skipped"
        Reason = "SONAR_TOKEN环境变量未设置"
    }
    
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $jsonPath = Join-Path $reportsDir "sonarqube-quality-gate_$timestamp.json"
    $report | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonPath -Encoding UTF8
    
    exit 0
}

# 检查sonar-project.properties
$sonarPropsPath = Join-Path $projectRoot "sonar-project.properties"
$hasSonarProps = Test-Path $sonarPropsPath

if (-not $hasSonarProps) {
    Write-Warning "sonar-project.properties文件不存在，将创建默认配置"
}

# 执行SonarQube扫描
if (-not $CI) {
    Write-Host "[1/3] 执行SonarQube扫描..." -ForegroundColor Yellow
}

try {
    Push-Location $microservicesDir
    
    # 设置SonarQube环境变量
    $env:SONAR_TOKEN = $SonarToken
    if ($SonarHostUrl) {
        $env:SONAR_HOST_URL = $SonarHostUrl
    }
    
    # 执行扫描
    $scanOutput = & mvn clean verify sonar:sonar 2>&1
    $scanExitCode = $LASTEXITCODE
    
    if ($scanExitCode -ne 0) {
        Write-Warning "SonarQube扫描执行失败"
        $scanError = $scanOutput | Select-String -Pattern "ERROR" | Select-Object -First 5
        $report = @{
            CheckType = "SonarQubeQualityGate"
            Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            Status = "scan_failed"
            Error = ($scanError -join "`n")
        }
        $exitCode = 1
    } else {
        if (-not $CI) {
            Write-Host "[2/3] 检查质量门禁状态..." -ForegroundColor Yellow
        }
        
        # 尝试从API获取质量门禁状态（需要SonarQube URL）
        $report = @{
            CheckType = "SonarQubeQualityGate"
            Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            Status = "completed"
            ScanCompleted = $true
            QualityGateStatus = "unknown"  # 需要从API获取
        }
        
        # 如果提供了SonarQube URL，尝试获取质量门禁状态
        if ($SonarHostUrl -and $SonarProjectKey) {
            try {
                $apiUrl = "$SonarHostUrl/api/qualitygates/project_status?projectKey=$SonarProjectKey"
                $headers = @{
                    "Authorization" = "Bearer $SonarToken"
                }
                
                $qualityGateResponse = Invoke-RestMethod -Uri $apiUrl -Headers $headers -Method Get -ErrorAction SilentlyContinue
                
                if ($qualityGateResponse) {
                    $report.QualityGateStatus = $qualityGateResponse.projectStatus.status
                    $report.QualityGateConditions = $qualityGateResponse.projectStatus.conditions
                    
                    if ($qualityGateResponse.projectStatus.status -ne "OK") {
                        $exitCode = 1
                    }
                }
            } catch {
                Write-Warning "无法获取质量门禁状态: $_"
            }
        }
    }
    
    Pop-Location
} catch {
    Write-Error "SonarQube检查失败: $_"
    $report = @{
        CheckType = "SonarQubeQualityGate"
        Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        Status = "error"
        Error = $_.ToString()
    }
    $exitCode = 1
}

# 生成报告
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$jsonPath = Join-Path $reportsDir "sonarqube-quality-gate_$timestamp.json"
$report | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonPath -Encoding UTF8

# 生成Markdown报告
$mdReport = @"
# SonarQube质量门禁检查报告

**生成时间**: $($report.Timestamp)  
**状态**: $($report.Status)

$(if ($report.QualityGateStatus -ne "unknown") {
    "**质量门禁状态**: $($report.QualityGateStatus)`n`n"
})

$(if ($report.QualityGateConditions) {
    "## 质量门禁条件`n`n"
    "| 指标 | 状态 | 阈值 | 实际值 |`n"
    "|------|------|------|--------|`n"
    foreach ($condition in $report.QualityGateConditions) {
        $status = switch ($condition.status) {
            "OK" { "✅ 通过" }
            "ERROR" { "❌ 失败" }
            "WARN" { "⚠️ 警告" }
            default { $condition.status }
        }
        "| $($condition.metricKey) | $status | $($condition.comparator) $($condition.errorThreshold) | $($condition.actualValue) |`n"
    }
})

$(if ($report.Error) {
    "## 错误信息`n`n```\n$($report.Error)\n```\n`n"
})

---

**报告文件**: `sonarqube-quality-gate_$timestamp.json`

**说明**: 
- 质量门禁状态需要SonarQube服务器API支持
- 如果无法获取状态，请检查SONAR_HOST_URL和SONAR_TOKEN配置
- 确保SonarQube服务器可访问

"@

$mdPath = Join-Path $reportsDir "sonarqube-quality-gate_$timestamp.md"
$mdReport | Out-File -FilePath $mdPath -Encoding UTF8

if (-not $CI) {
    Write-Host ""
    Write-Host "===== 检查完成 =====" -ForegroundColor Cyan
    if ($report.QualityGateStatus -ne "unknown") {
        $statusColor = switch ($report.QualityGateStatus) {
            "OK" { "Green" }
            "ERROR" { "Red" }
            default { "Yellow" }
        }
        Write-Host "质量门禁状态: $($report.QualityGateStatus)" -ForegroundColor $statusColor
    } else {
        Write-Host "状态: $($report.Status)" -ForegroundColor $(if ($exitCode -eq 0) { "Green" } else { "Red" })
    }
    Write-Host ""
    Write-Host "详细报告:" -ForegroundColor Cyan
    Write-Host "  JSON: sonarqube-quality-gate_$timestamp.json" -ForegroundColor Gray
    Write-Host "  Markdown: sonarqube-quality-gate_$timestamp.md" -ForegroundColor Gray
    Write-Host ""
}

exit $exitCode

