# IOE-DREAM 架构合规性检查脚本（改进版）
# Version: 2.0.0
# Purpose: 排除注释中的匹配，只检查实际代码
# Environment: Windows PowerShell

$ErrorActionPreference = "Continue"
$basePath = "D:\IOE-DREAM\microservices"
$violations = @()

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Architecture Compliance Check (Improved)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 排除目录
$excludeDirs = @("archive", "target", "test", "analytics", "node_modules")

# 辅助函数：检查是否在注释中
function Test-IsInComment {
    param(
        [string]$line,
        [bool]$inMultiLineComment
    )

    $trimmed = $line.Trim()

    # 检查单行注释
    if ($trimmed -match '^\s*//') {
        return $true, $false
    }

    # 检查多行注释开始
    if ($trimmed -match '/\*') {
        return $true, $true
    }

    # 检查多行注释结束
    if ($inMultiLineComment -and $trimmed -match '\*/') {
        return $true, $false
    }

    # 在多行注释中
    if ($inMultiLineComment) {
        return $true, $true
    }

    return $false, $false
}

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $basePath -Include *.java -Recurse -ErrorAction SilentlyContinue |
    Where-Object {
        $exclude = $false
        foreach ($dir in $excludeDirs) {
            if ($_.FullName -like "*\$dir\*") {
                $exclude = $true
                break
            }
        }
        -not $exclude
    }

Write-Host "Found $($javaFiles.Count) Java files to check" -ForegroundColor Yellow
Write-Host ""

# Check 1: @Autowired usage (forbidden)
Write-Host "[Check 1] Scanning @Autowired usage (excluding comments)..." -ForegroundColor Yellow

$autowiredViolations = @()
foreach ($file in $javaFiles) {
    $lines = Get-Content $file.FullName -ErrorAction SilentlyContinue
    $inMultiLineComment = $false

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $inComment, $inMultiLineComment = Test-IsInComment -line $line -inMultiLineComment $inMultiLineComment

        if (-not $inComment -and $line -match '@Autowired') {
            $autowiredViolations += [PSCustomObject]@{
                File = $file.FullName
                Line = $i + 1
                Content = $line.Trim()
            }
            break
        }
    }
}

if ($autowiredViolations.Count -eq 0) {
    Write-Host "  PASSED: No @Autowired violations found" -ForegroundColor Green
} else {
    Write-Host "  FAILED: Found $($autowiredViolations.Count) files using @Autowired" -ForegroundColor Red
    foreach ($violation in $autowiredViolations) {
        Write-Host "    - $($violation.File):$($violation.Line)" -ForegroundColor Gray
        Write-Host "      $($violation.Content)" -ForegroundColor DarkGray
    }
    $violations += $autowiredViolations
}

Write-Host ""

# Check 2: @Repository usage (forbidden)
Write-Host "[Check 2] Scanning @Repository usage (excluding comments)..." -ForegroundColor Yellow

$repositoryViolations = @()
foreach ($file in $javaFiles) {
    $lines = Get-Content $file.FullName -ErrorAction SilentlyContinue
    $inMultiLineComment = $false

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $inComment, $inMultiLineComment = Test-IsInComment -line $line -inMultiLineComment $inMultiLineComment

        if (-not $inComment -and $line -match '@Repository') {
            $repositoryViolations += [PSCustomObject]@{
                File = $file.FullName
                Line = $i + 1
                Content = $line.Trim()
            }
            break
        }
    }
}

if ($repositoryViolations.Count -eq 0) {
    Write-Host "  PASSED: No @Repository violations found" -ForegroundColor Green
} else {
    Write-Host "  FAILED: Found $($repositoryViolations.Count) files using @Repository" -ForegroundColor Red
    foreach ($violation in $repositoryViolations) {
        Write-Host "    - $($violation.File):$($violation.Line)" -ForegroundColor Gray
        Write-Host "      $($violation.Content)" -ForegroundColor DarkGray
    }
    $violations += $repositoryViolations
}

Write-Host ""

# Check 3: Repository suffix (forbidden)
Write-Host "[Check 3] Scanning Repository suffix..." -ForegroundColor Yellow

$repositorySuffixViolations = @()
foreach ($file in $javaFiles) {
    $fileName = $file.Name
    if ($fileName -match 'Repository\.java$') {
        $repositorySuffixViolations += [PSCustomObject]@{
            File = $file.FullName
        }
    }
}

if ($repositorySuffixViolations.Count -eq 0) {
    Write-Host "  PASSED: No Repository suffix found" -ForegroundColor Green
} else {
    Write-Host "  FAILED: Found $($repositorySuffixViolations.Count) files with Repository suffix" -ForegroundColor Red
    foreach ($violation in $repositorySuffixViolations) {
        Write-Host "    - $($violation.File)" -ForegroundColor Gray
    }
    $violations += $repositorySuffixViolations
}

Write-Host ""

# Check 4: Controller direct DAO access (forbidden)
Write-Host "[Check 4] Scanning Controller direct DAO access..." -ForegroundColor Yellow

$controllerDaoViolations = @()
foreach ($file in $javaFiles) {
    if ($file.Name -match 'Controller\.java$') {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        $lines = Get-Content $file.FullName -ErrorAction SilentlyContinue
        $inMultiLineComment = $false

        # 检查是否有DAO注入
        $hasDao = $false
        $daoName = ""

        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i]
            $inComment, $inMultiLineComment = Test-IsInComment -line $line -inMultiLineComment $inMultiLineComment

            if (-not $inComment) {
                if ($line -match '@Resource\s+.*Dao\s+(\w+)') {
                    $hasDao = $true
                    $daoName = $matches[1]
                    break
                }
            }
        }

        if ($hasDao) {
            $controllerDaoViolations += [PSCustomObject]@{
                File = $file.FullName
                DaoName = $daoName
            }
        }
    }
}

if ($controllerDaoViolations.Count -eq 0) {
    Write-Host "  PASSED: No Controller direct DAO access found" -ForegroundColor Green
} else {
    Write-Host "  FAILED: Found $($controllerDaoViolations.Count) Controllers with direct DAO access" -ForegroundColor Red
    foreach ($violation in $controllerDaoViolations) {
        Write-Host "    - $($violation.File) (DAO: $($violation.DaoName))" -ForegroundColor Gray
    }
    $violations += $controllerDaoViolations
}

Write-Host ""

# Check 5: javax package usage (should use jakarta)
Write-Host "[Check 5] Scanning javax package usage..." -ForegroundColor Yellow

$javaxViolations = @()
foreach ($file in $javaFiles) {
    $lines = Get-Content $file.FullName -ErrorAction SilentlyContinue
    $inMultiLineComment = $false

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $inComment, $inMultiLineComment = Test-IsInComment -line $line -inMultiLineComment $inMultiLineComment

        if (-not $inComment -and $line -match '^import\s+javax\.') {
            $javaxViolations += [PSCustomObject]@{
                File = $file.FullName
                Line = $i + 1
                Content = $line.Trim()
            }
            break
        }
    }
}

if ($javaxViolations.Count -eq 0) {
    Write-Host "  PASSED: No javax package usage found" -ForegroundColor Green
} else {
    Write-Host "  FAILED: Found $($javaxViolations.Count) files using javax package" -ForegroundColor Red
    foreach ($violation in $javaxViolations) {
        Write-Host "    - $($violation.File):$($violation.Line)" -ForegroundColor Gray
        Write-Host "      $($violation.Content)" -ForegroundColor DarkGray
    }
    $violations += $javaxViolations
}

Write-Host ""

# Check 6: System.out.println usage (forbidden, except in Application classes)
Write-Host "[Check 6] Scanning System.out.println usage..." -ForegroundColor Yellow

$systemOutViolations = @()
foreach ($file in $javaFiles) {
    # 排除Application启动类
    if ($file.Name -match 'Application\.java$') {
        continue
    }

    $lines = Get-Content $file.FullName -ErrorAction SilentlyContinue
    $inMultiLineComment = $false

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $inComment, $inMultiLineComment = Test-IsInComment -line $line -inMultiLineComment $inMultiLineComment

        if (-not $inComment -and $line -match 'System\.out\.println') {
            $systemOutViolations += [PSCustomObject]@{
                File = $file.FullName
                Line = $i + 1
                Content = $line.Trim()
            }
            break
        }
    }
}

if ($systemOutViolations.Count -eq 0) {
    Write-Host "  PASSED: No System.out.println violations found" -ForegroundColor Green
} else {
    Write-Host "  FAILED: Found $($systemOutViolations.Count) files using System.out.println" -ForegroundColor Red
    foreach ($violation in $systemOutViolations) {
        Write-Host "    - $($violation.File):$($violation.Line)" -ForegroundColor Gray
        Write-Host "      $($violation.Content)" -ForegroundColor DarkGray
    }
    $violations += $systemOutViolations
}

Write-Host ""

# 输出汇总
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Check Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$totalViolations = $autowiredViolations.Count + $repositoryViolations.Count + $repositorySuffixViolations.Count +
                   $controllerDaoViolations.Count + $javaxViolations.Count + $systemOutViolations.Count

if ($totalViolations -eq 0) {
    Write-Host "PASSED: No architecture violations found!" -ForegroundColor Green
} else {
    Write-Host "FAILED: Found $totalViolations architecture violations" -ForegroundColor Red
    Write-Host ""
    Write-Host "Violation Details:" -ForegroundColor Yellow
    Write-Host "  @Autowired: $($autowiredViolations.Count)" -ForegroundColor $(if ($autowiredViolations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host "  @Repository: $($repositoryViolations.Count)" -ForegroundColor $(if ($repositoryViolations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host "  Repository suffix: $($repositorySuffixViolations.Count)" -ForegroundColor $(if ($repositorySuffixViolations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host "  Controller DAO access: $($controllerDaoViolations.Count)" -ForegroundColor $(if ($controllerDaoViolations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host "  javax package: $($javaxViolations.Count)" -ForegroundColor $(if ($javaxViolations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host "  System.out.println: $($systemOutViolations.Count)" -ForegroundColor $(if ($systemOutViolations.Count -eq 0) { "Green" } else { "Red" })
}

# 保存报告
$reportFile = "D:\IOE-DREAM\ARCHITECTURE_COMPLIANCE_CHECK_REPORT_IMPROVED_$(Get-Date -Format 'yyyyMMdd_HHmmss').md"
$reportContent = "# Architecture Compliance Check Report (Improved)`n`n"
$reportContent += "**Check Time**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n"
$reportContent += "**Total Files Checked**: $($javaFiles.Count)`n"
$reportContent += "**Total Violations**: $totalViolations`n`n"

if ($autowiredViolations.Count -gt 0) {
    $reportContent += "## @Autowired Violations ($($autowiredViolations.Count))`n`n"
    foreach ($violation in $autowiredViolations) {
        $reportContent += "- **$($violation.File):$($violation.Line)**`n"
        $reportContent += "  ```java`n"
        $reportContent += "  $($violation.Content)`n"
        $reportContent += "  ````n`n"
    }
}

if ($repositoryViolations.Count -gt 0) {
    $reportContent += "## @Repository Violations ($($repositoryViolations.Count))`n`n"
    foreach ($violation in $repositoryViolations) {
        $reportContent += "- **$($violation.File):$($violation.Line)**`n"
        $reportContent += "  ```java`n"
        $reportContent += "  $($violation.Content)`n"
        $reportContent += "  ````n`n"
    }
}

if ($repositorySuffixViolations.Count -gt 0) {
    $reportContent += "## Repository Suffix Violations ($($repositorySuffixViolations.Count))`n`n"
    foreach ($violation in $repositorySuffixViolations) {
        $reportContent += "- **$($violation.File)**`n`n"
    }
}

if ($controllerDaoViolations.Count -gt 0) {
    $reportContent += "## Controller DAO Access Violations ($($controllerDaoViolations.Count))`n`n"
    foreach ($violation in $controllerDaoViolations) {
        $reportContent += "- **$($violation.File)** (DAO: $($violation.DaoName))`n`n"
    }
}

if ($javaxViolations.Count -gt 0) {
    $reportContent += "## javax Package Violations ($($javaxViolations.Count))`n`n"
    foreach ($violation in $javaxViolations) {
        $reportContent += "- **$($violation.File):$($violation.Line)**`n"
        $reportContent += "  ```java`n"
        $reportContent += "  $($violation.Content)`n"
        $reportContent += "  ````n`n"
    }
}

if ($systemOutViolations.Count -gt 0) {
    $reportContent += "## System.out.println Violations ($($systemOutViolations.Count))`n`n"
    foreach ($violation in $systemOutViolations) {
        $reportContent += "- **$($violation.File):$($violation.Line)**`n"
        $reportContent += "  ```java`n"
        $reportContent += "  $($violation.Content)`n"
        $reportContent += "  ````n`n"
    }
}

$reportContent | Out-File -FilePath $reportFile -Encoding UTF8
Write-Host ""
Write-Host "Report saved to: $reportFile" -ForegroundColor Cyan

