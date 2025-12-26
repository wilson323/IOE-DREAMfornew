# ============================================================
# IOE-DREAM 技术栈统一性检查脚本
#
# 功能：检查技术栈迁移完整性和统一性
# 包括：Jakarta EE、MyBatis-Plus、连接池、依赖注入
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

$report = @{
    CheckType           = "TechStackConsistency"
    Timestamp           = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    JakartaEE           = @{ Status = "pending"; TotalFiles = 0; MigratedFiles = 0; Violations = @() }
    MyBatisPlus         = @{ Status = "pending"; TotalFiles = 0; Violations = @() }
    ConnectionPool      = @{ Status = "pending"; ServicesUsingDruid = 0; ServicesUsingHikariCP = 0; Violations = @() }
    DependencyInjection = @{ Status = "pending"; UsingResource = 0; UsingAutowired = 0; Violations = @() }
}

$exitCode = 0

if (-not $CI) {
    Write-Host "===== 技术栈统一性检查 =====" -ForegroundColor Cyan
    Write-Host ""
}

# ==================== 1. Jakarta EE迁移检查 ====================
if (-not $CI) {
    Write-Host "[1/4] 检查Jakarta EE迁移..." -ForegroundColor Yellow
}

$javaFiles = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*.java" |
Where-Object { $_.FullName -notlike "*\target\*" -and $_.FullName -notlike "*\test\*" }

$report.JakartaEE.TotalFiles = $javaFiles.Count
$jakartaViolations = @()

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw
    $hasJavax = $content -match 'import\s+javax\.(annotation|validation|persistence|servlet|transaction|inject)'
    $hasJakarta = $content -match 'import\s+jakarta\.(annotation|validation|persistence|servlet|transaction|inject)'

    if ($hasJakarta) {
        $report.JakartaEE.MigratedFiles++
    }

    if ($hasJavax -and -not ($content -match 'javax\.(crypto|sql|imageio|net\.ssl)')) {
        $lines = Get-Content $file.FullName
        $lineNum = 0
        foreach ($line in $lines) {
            $lineNum++
            if ($line -match '^import\s+javax\.(annotation|validation|persistence|servlet|transaction|inject)') {
                $jakartaViolations += @{
                    File       = $file.FullName.Replace($projectRoot, "").TrimStart('\', '/')
                    LineNumber = $lineNum
                    Package    = $matches[0]
                }
            }
        }
    }
}

$report.JakartaEE.Violations = $jakartaViolations
$report.JakartaEE.Status = if ($jakartaViolations.Count -eq 0) { "complete" } else { "incomplete" }

$migrationRate = if ($report.JakartaEE.TotalFiles -gt 0) {
    [Math]::Round(($report.JakartaEE.MigratedFiles / $report.JakartaEE.TotalFiles) * 100, 2)
}
else { 0 }

if (-not $CI) {
    Write-Host "  总文件数: $($report.JakartaEE.TotalFiles)" -ForegroundColor Gray
    Write-Host "  已迁移: $($report.JakartaEE.MigratedFiles) ($migrationRate%)" -ForegroundColor $(if ($migrationRate -eq 100) { "Green" } else { "Yellow" })
    Write-Host "  违规数: $($jakartaViolations.Count)" -ForegroundColor $(if ($jakartaViolations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host ""
}

if ($jakartaViolations.Count -gt 0) {
    $exitCode = 1
}

# ==================== 2. MyBatis-Plus迁移检查 ====================
if (-not $CI) {
    Write-Host "[2/4] 检查MyBatis-Plus迁移..." -ForegroundColor Yellow
}

$daoFiles = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*Dao.java" |
Where-Object { $_.FullName -notlike "*\target\*" }

$report.MyBatisPlus.TotalFiles = $daoFiles.Count
$mybatisViolations = @()

foreach ($file in $daoFiles) {
    $lines = Get-Content $file.FullName
    $hasRepository = $false
    $hasMapper = $false
    $extendsBaseMapper = $false
    $extendsJpaRepository = $false

    foreach ($line in $lines) {
        $trimmedLine = $line.Trim()
        # 跳过注释行（单行注释和多行注释的开始/结束）
        if ($trimmedLine -match '^\s*//' -or $trimmedLine -match '^\s*/\*' -or $trimmedLine -match '\*/') {
            continue
        }

        # 检查是否有@Repository注解（非注释）
        if ($trimmedLine -match '^\s*@Repository' -or $trimmedLine -match '@Repository\s*$') {
            $hasRepository = $true
        }

        # 检查是否有@Mapper注解
        if ($trimmedLine -match '^\s*@Mapper') {
            $hasMapper = $true
        }

        # 检查是否继承BaseMapper
        if ($trimmedLine -match 'extends\s+BaseMapper') {
            $extendsBaseMapper = $true
        }

        # 检查是否继承JpaRepository
        if ($trimmedLine -match 'extends\s+JpaRepository') {
            $extendsJpaRepository = $true
        }
    }

    if ($hasRepository -or $extendsJpaRepository) {
        $mybatisViolations += @{
            File           = $file.FullName.Replace($projectRoot, "").TrimStart('\', '/')
            Issue          = if ($hasRepository) { "使用@Repository注解" } else { "继承JpaRepository" }
            Recommendation = "应使用@Mapper注解并继承BaseMapper"
        }
    }
}

$report.MyBatisPlus.Violations = $mybatisViolations
$report.MyBatisPlus.Status = if ($mybatisViolations.Count -eq 0) { "complete" } else { "incomplete" }

if (-not $CI) {
    Write-Host "  总DAO文件数: $($report.MyBatisPlus.TotalFiles)" -ForegroundColor Gray
    Write-Host "  违规数: $($mybatisViolations.Count)" -ForegroundColor $(if ($mybatisViolations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host ""
}

if ($mybatisViolations.Count -gt 0) {
    $exitCode = 1
}

# ==================== 3. 连接池统一性检查 ====================
if (-not $CI) {
    Write-Host "[3/4] 检查连接池统一性..." -ForegroundColor Yellow
}

$ymlFiles = Get-ChildItem -Path $microservicesDir -Recurse -Filter "application*.yml" |
Where-Object { $_.FullName -notlike "*\target\*" }

$poolViolations = @()
$servicesWithDruid = @()
$servicesWithHikari = @()

foreach ($file in $ymlFiles) {
    $content = Get-Content $file.FullName -Raw
    $serviceName = Split-Path (Split-Path $file.DirectoryName -Parent) -Leaf

    $hasDruid = $content -match 'type:\s*.*druid|DruidDataSource|com\.alibaba\.druid'
    $hasHikari = $content -match 'hikari:|HikariDataSource|type:\s*.*hikari'

    if ($hasDruid -and $content -notmatch '#.*druid|LOG_LEVEL') {
        $servicesWithDruid += $serviceName
    }

    if ($hasHikari -and $content -notmatch '#.*hikari|LOG_LEVEL_HIKARI|禁止') {
        $poolViolations += @{
            File           = $file.FullName.Replace($projectRoot, "").TrimStart('\', '/')
            Service        = $serviceName
            Issue          = "使用HikariCP连接池"
            Recommendation = "应统一使用Druid连接池"
        }
        $servicesWithHikari += $serviceName
    }
}

$report.ConnectionPool.ServicesUsingDruid = ($servicesWithDruid | Select-Object -Unique).Count
$report.ConnectionPool.ServicesUsingHikariCP = ($servicesWithHikari | Select-Object -Unique).Count
$report.ConnectionPool.Violations = $poolViolations
$report.ConnectionPool.Status = if ($poolViolations.Count -eq 0) { "consistent" } else { "inconsistent" }

if (-not $CI) {
    Write-Host "  使用Druid的服务: $($report.ConnectionPool.ServicesUsingDruid)" -ForegroundColor Green
    Write-Host "  使用HikariCP的服务: $($report.ConnectionPool.ServicesUsingHikariCP)" -ForegroundColor $(if ($poolViolations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host "  违规数: $($poolViolations.Count)" -ForegroundColor $(if ($poolViolations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host ""
}

if ($poolViolations.Count -gt 0) {
    $exitCode = 1
}

# ==================== 4. 依赖注入统一性检查 ====================
if (-not $CI) {
    Write-Host "[4/4] 检查依赖注入统一性..." -ForegroundColor Yellow
}

$serviceFiles = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*Service*.java" |
Where-Object { $_.FullName -notlike "*\target\*" -and $_.FullName -notlike "*\test\*" }

$diViolations = @()
$resourceCount = 0
$autowiredCount = 0

foreach ($file in $serviceFiles) {
    $content = Get-Content $file.FullName -Raw
    $usingResource = ($content | Select-String -Pattern '@Resource' -AllMatches).Matches.Count
    $usingAutowired = ($content | Select-String -Pattern '@Autowired(?!.*禁止|.*//)' -AllMatches).Matches.Count

    if ($usingResource -gt 0) {
        $resourceCount++
    }
    if ($usingAutowired -gt 0) {
        $autowiredCount++
        $lines = Get-Content $file.FullName
        $lineNum = 0
        foreach ($line in $lines) {
            $lineNum++
            if ($line -match '^\s*@Autowired' -and $line -notmatch '禁止|//') {
                $diViolations += @{
                    File           = $file.FullName.Replace($projectRoot, "").TrimStart('\', '/')
                    LineNumber     = $lineNum
                    Issue          = "使用@Autowired注解"
                    Recommendation = "应统一使用@Resource注解"
                }
            }
        }
    }
}

$report.DependencyInjection.UsingResource = $resourceCount
$report.DependencyInjection.UsingAutowired = $autowiredCount
$report.DependencyInjection.Violations = $diViolations
$report.DependencyInjection.Status = if ($diViolations.Count -eq 0) { "consistent" } else { "inconsistent" }

if (-not $CI) {
    Write-Host "  使用@Resource的文件: $resourceCount" -ForegroundColor Green
    Write-Host "  使用@Autowired的文件: $autowiredCount" -ForegroundColor $(if ($diViolations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host "  违规数: $($diViolations.Count)" -ForegroundColor $(if ($diViolations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host ""
}

if ($diViolations.Count -gt 0) {
    $exitCode = 1
}

# ==================== 生成报告 ====================
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

# 计算总体评分
$totalIssues = $jakartaViolations.Count + $mybatisViolations.Count + $poolViolations.Count + $diViolations.Count
$report.OverallScore = if ($totalIssues -eq 0) { 100 } else {
    [Math]::Max(0, 100 - ($totalIssues * 2))
}
$report.OverallStatus = if ($totalIssues -eq 0) { "excellent" }
elseif ($totalIssues -lt 10) { "good" }
elseif ($totalIssues -lt 50) { "fair" }
else { "poor" }

# JSON报告
if ($OutputFormat -eq "json" -or $OutputFormat -eq "both") {
    $jsonPath = Join-Path $reportsDir "tech-stack-consistency_$timestamp.json"
    $report | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonPath -Encoding UTF8
    if (-not $CI) {
        Write-Host "JSON报告已生成: $jsonPath" -ForegroundColor Green
    }
}

# Markdown报告
$mdReport = @"
# IOE-DREAM 技术栈统一性检查报告

**生成时间**: $($report.Timestamp)
**总体评分**: $($report.OverallScore)/100
**总体状态**: $($report.OverallStatus)

## 1. Jakarta EE迁移状态

- **状态**: $($report.JakartaEE.Status)
- **总文件数**: $($report.JakartaEE.TotalFiles)
- **已迁移**: $($report.JakartaEE.MigratedFiles) ($migrationRate%)
- **违规数**: $($jakartaViolations.Count)

$(if ($jakartaViolations.Count -gt 0) {
    "### 需要迁移的文件`n`n| 文件 | 行号 | 包名 |`n|------|------|------|`n"
    foreach ($v in $jakartaViolations) {
        "| $($v.File) | $($v.LineNumber) | $($v.Package) |`n"
    }
} else {
    "✅ **迁移完成**`n"
})

## 2. MyBatis-Plus迁移状态

- **状态**: $($report.MyBatisPlus.Status)
- **总DAO文件数**: $($report.MyBatisPlus.TotalFiles)
- **违规数**: $($mybatisViolations.Count)

$(if ($mybatisViolations.Count -gt 0) {
    "### 需要修复的文件`n`n| 文件 | 问题 | 建议 |`n|------|------|------|`n"
    foreach ($v in $mybatisViolations) {
        "| $($v.File) | $($v.Issue) | $($v.Recommendation) |`n"
    }
} else {
    "✅ **迁移完成**`n"
})

## 3. 连接池统一性

- **状态**: $($report.ConnectionPool.Status)
- **使用Druid的服务**: $($report.ConnectionPool.ServicesUsingDruid)
- **使用HikariCP的服务**: $($report.ConnectionPool.ServicesUsingHikariCP)
- **违规数**: $($poolViolations.Count)

$(if ($poolViolations.Count -gt 0) {
    "### 需要修复的服务`n`n| 文件 | 服务 | 问题 | 建议 |`n|------|------|------|------|`n"
    foreach ($v in $poolViolations) {
        "| $($v.File) | $($v.Service) | $($v.Issue) | $($v.Recommendation) |`n"
    }
} else {
    "✅ **统一使用Druid**`n"
})

## 4. 依赖注入统一性

- **状态**: $($report.DependencyInjection.Status)
- **使用@Resource**: $resourceCount 个文件
- **使用@Autowired**: $autowiredCount 个文件
- **违规数**: $($diViolations.Count)

$(if ($diViolations.Count -gt 0) {
    "### 需要修复的文件`n`n| 文件 | 行号 | 问题 | 建议 |`n|------|------|------|------|`n"
    foreach ($v in $diViolations) {
        "| $($v.File) | $($v.LineNumber) | $($v.Issue) | $($v.Recommendation) |`n"
    }
} else {
    "✅ **统一使用@Resource**`n"
})

## 迁移优先级建议

1. **P0 - 立即修复**: 连接池不统一、MyBatis-Plus迁移
2. **P1 - 短期修复**: Jakarta EE迁移
3. **P2 - 持续优化**: 依赖注入统一

---

**报告文件**: `tech-stack-consistency_$timestamp.json`

"@

$mdPath = Join-Path $reportsDir "tech-stack-consistency_$timestamp.md"
$mdReport | Out-File -FilePath $mdPath -Encoding UTF8

if (-not $CI) {
    Write-Host ""
    Write-Host "===== 检查完成 =====" -ForegroundColor Cyan
    Write-Host "总体评分: $($report.OverallScore)/100" -ForegroundColor $(if ($report.OverallScore -ge 90) { "Green" } elseif ($report.OverallScore -ge 70) { "Yellow" } else { "Red" })
    Write-Host "总问题数: $totalIssues" -ForegroundColor $(if ($totalIssues -eq 0) { "Green" } else { "Red" })
    Write-Host ""
}

exit $exitCode

