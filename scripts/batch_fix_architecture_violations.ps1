# IOE-DREAM 批量修复架构违规脚本
# Version: 1.0.0
# Purpose: 批量修复@Autowired和@Repository违规
# Environment: Windows PowerShell

$ErrorActionPreference = "Continue"
$basePath = "D:\IOE-DREAM\microservices"
$fixedFiles = @()
$failedFiles = @()

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Architecture Violations Fix" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 排除目录
$excludeDirs = @("archive", "target", "test", "analytics")

# 1. 修复@Autowired -> @Resource
Write-Host "[Fix 1] Replacing @Autowired with @Resource..." -ForegroundColor Yellow

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

$autowiredFiles = $javaFiles | Select-String -Pattern "@Autowired" | Select-Object -ExpandProperty Path -Unique

foreach ($file in $autowiredFiles) {
    try {
        $content = Get-Content $file -Raw -Encoding UTF8
        $originalContent = $content

        # 替换import语句
        $content = $content -replace "import org\.springframework\.beans\.factory\.annotation\.Autowired;", "import jakarta.annotation.Resource;"

        # 替换注解
        $content = $content -replace "@Autowired\s+", "@Resource`n    "

        if ($content -ne $originalContent) {
            $content | Set-Content $file -Encoding UTF8 -NoNewline
            $fixedFiles += "FIXED: @Autowired -> @Resource in $file"
            Write-Host "  FIXED: $file" -ForegroundColor Green
        }
    } catch {
        $failedFiles += "FAILED: $file - $($_.Exception.Message)"
        Write-Host "  FAILED: $file - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "  Fixed @Autowired: $($fixedFiles.Count) files" -ForegroundColor Green
Write-Host ""

# 2. 修复@Repository -> @Mapper
Write-Host "[Fix 2] Replacing @Repository with @Mapper..." -ForegroundColor Yellow

$repositoryFiles = $javaFiles | Select-String -Pattern "@Repository" | Select-Object -ExpandProperty Path -Unique

$fixedCount = 0
foreach ($file in $repositoryFiles) {
    try {
        $content = Get-Content $file -Raw -Encoding UTF8
        $originalContent = $content

        # 替换import语句
        $content = $content -replace "import org\.springframework\.stereotype\.Repository;", "import org.apache.ibatis.annotations.Mapper;"

        # 替换注解（只替换在接口或类定义前的@Repository）
        $content = $content -replace "@Repository\s+", "@Mapper`n"

        if ($content -ne $originalContent) {
            $content | Set-Content $file -Encoding UTF8 -NoNewline
            $fixedFiles += "FIXED: @Repository -> @Mapper in $file"
            $fixedCount++
            Write-Host "  FIXED: $file" -ForegroundColor Green
        }
    } catch {
        $failedFiles += "FAILED: $file - $($_.Exception.Message)"
        Write-Host "  FAILED: $file - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "  Fixed @Repository: $fixedCount files" -ForegroundColor Green
Write-Host ""

# 输出汇总
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fix Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total Fixed: $($fixedFiles.Count)" -ForegroundColor Green
Write-Host "Total Failed: $($failedFiles.Count)" -ForegroundColor $(if ($failedFiles.Count -eq 0) { "Green" } else { "Red" })

if ($failedFiles.Count -gt 0) {
    Write-Host ""
    Write-Host "Failed Files:" -ForegroundColor Yellow
    foreach ($failed in $failedFiles) {
        Write-Host "  $failed" -ForegroundColor Red
    }
}

# 保存报告
$reportFile = "D:\IOE-DREAM\ARCHITECTURE_FIX_REPORT_$(Get-Date -Format 'yyyyMMdd_HHmmss').md"
$reportContent = "# Architecture Violations Fix Report`n`n"
$reportContent += "**Fix Time**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n"
$reportContent += "**Total Fixed**: $($fixedFiles.Count)`n"
$reportContent += "**Total Failed**: $($failedFiles.Count)`n`n"
$reportContent += "## Fixed Files`n`n"

foreach ($fixed in $fixedFiles) {
    $reportContent += "- $fixed`n"
}

if ($failedFiles.Count -gt 0) {
    $reportContent += "`n## Failed Files`n`n"
    foreach ($failed in $failedFiles) {
        $reportContent += "- $failed`n"
    }
}

$reportContent | Out-File -FilePath $reportFile -Encoding UTF8
Write-Host ""
Write-Host "Report saved to: $reportFile" -ForegroundColor Cyan

