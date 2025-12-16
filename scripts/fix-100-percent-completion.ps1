# =====================================================
# IOE-DREAM 100%完成修复脚本（务实版）
# 版本: v1.0.0
# 描述: 批量修复所有架构合规性问题，达到100%完成
# 原则: 务实、高效、可验证，避免过度工程化
# =====================================================

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 100%完成修复脚本" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

$rootPath = $PSScriptRoot
if (-not $rootPath) {
    $rootPath = Split-Path -Parent $MyInvocation.MyCommand.Path
}
$microservicesPath = Join-Path $rootPath "microservices"

if (-not (Test-Path $microservicesPath)) {
    Write-Host "[ERROR] 未找到microservices目录: $microservicesPath" -ForegroundColor Red
    exit 1
}

$fixedFiles = @()
$errorFiles = @()

# =====================================================
# 1. 修复@Autowired → @Resource
# =====================================================
Write-Host "[1/5] 修复@Autowired → @Resource..." -ForegroundColor Yellow

Get-ChildItem -Path $microservicesPath -Filter "*.java" -Recurse | 
Where-Object { 
    $_.FullName -notmatch "\\target\\|\\test\\|documentation|docs" 
} | ForEach-Object {
    try {
        $content = Get-Content $_.FullName -Raw -Encoding UTF8
        $originalContent = $content
        $modified = $false
        
        # 替换@Autowired为@Resource
        if ($content -match "@Autowired") {
            $content = $content -replace "@Autowired", "@Resource"
            $modified = $true
        }
        
        # 替换import语句
        if ($content -match "import org\.springframework\.beans\.factory\.annotation\.Autowired;") {
            $content = $content -replace "import org\.springframework\.beans\.factory\.annotation\.Autowired;", "import jakarta.annotation.Resource;"
            $modified = $true
        }
        
        if ($modified) {
            Set-Content -Path $_.FullName -Value $content -Encoding UTF8 -NoNewline
            $fixedFiles += $_.FullName
            Write-Host "  [FIXED] $($_.Name)" -ForegroundColor Green
        }
    } catch {
        $errorFiles += $_.FullName
        Write-Host "  [ERROR] $($_.FullName): $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "  [完成] 修复了 $($fixedFiles.Count) 个文件" -ForegroundColor Green
Write-Host ""

# =====================================================
# 2. 修复@Repository → @Mapper
# =====================================================
Write-Host "[2/5] 修复@Repository → @Mapper..." -ForegroundColor Yellow

$fixedFiles = @()

Get-ChildItem -Path $microservicesPath -Filter "*Dao.java" -Recurse | 
Where-Object { 
    $_.FullName -notmatch "\\target\\|\\test\\|documentation|docs" 
} | ForEach-Object {
    try {
        $content = Get-Content $_.FullName -Raw -Encoding UTF8
        $modified = $false
        
        # 替换@Repository为@Mapper
        if ($content -match "@Repository") {
            $content = $content -replace "@Repository", "@Mapper"
            $modified = $true
        }
        
        # 替换import语句
        if ($content -match "import org\.springframework\.stereotype\.Repository;") {
            $content = $content -replace "import org\.springframework\.stereotype\.Repository;", "import org.apache.ibatis.annotations.Mapper;"
            $modified = $true
        }
        
        if ($modified) {
            Set-Content -Path $_.FullName -Value $content -Encoding UTF8 -NoNewline
            $fixedFiles += $_.FullName
            Write-Host "  [FIXED] $($_.Name)" -ForegroundColor Green
        }
    } catch {
        $errorFiles += $_.FullName
        Write-Host "  [ERROR] $($_.FullName): $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "  [完成] 修复了 $($fixedFiles.Count) 个文件" -ForegroundColor Green
Write-Host ""

# =====================================================
# 3. 修复javax.* → jakarta.*
# =====================================================
Write-Host "[3/5] 修复javax.* → jakarta.*..." -ForegroundColor Yellow

$javaxPatterns = @{
    "javax.annotation.Resource" = "jakarta.annotation.Resource"
    "javax.validation.Valid" = "jakarta.validation.Valid"
    "javax.persistence.Entity" = "jakarta.persistence.Entity"
    "javax.servlet.http.HttpServletRequest" = "jakarta.servlet.http.HttpServletRequest"
    "javax.transaction.Transactional" = "jakarta.transaction.Transactional"
}

$fixedFiles = @()

Get-ChildItem -Path $microservicesPath -Filter "*.java" -Recurse | 
Where-Object { 
    $_.FullName -notmatch "\\target\\|\\test\\|documentation|docs" 
} | ForEach-Object {
    try {
        $content = Get-Content $_.FullName -Raw -Encoding UTF8
        $modified = $false
        
        foreach ($pattern in $javaxPatterns.GetEnumerator()) {
            $escapedKey = [regex]::Escape($pattern.Key)
            if ($content -match $escapedKey) {
                $content = $content -replace $escapedKey, $pattern.Value
                $modified = $true
            }
        }
        
        if ($modified) {
            Set-Content -Path $_.FullName -Value $content -Encoding UTF8 -NoNewline
            $fixedFiles += $_.FullName
            Write-Host "  [FIXED] $($_.Name)" -ForegroundColor Green
        }
    } catch {
        $errorFiles += $_.FullName
        Write-Host "  [ERROR] $($_.FullName): $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "  [完成] 修复了 $($fixedFiles.Count) 个文件" -ForegroundColor Green
Write-Host ""

# =====================================================
# 4. 验证修复结果
# =====================================================
Write-Host "[4/5] 验证修复结果..." -ForegroundColor Yellow

$autowiredCount = (Get-ChildItem -Path $microservicesPath -Filter "*.java" -Recurse | 
    Where-Object { $_.FullName -notmatch "\\target\\|\\test\\|documentation|docs" } |
    Select-String -Pattern "@Autowired" | Measure-Object).Count

$repositoryCount = (Get-ChildItem -Path $microservicesPath -Filter "*Dao.java" -Recurse | 
    Where-Object { $_.FullName -notmatch "\\target\\|\\test\\|documentation|docs" } |
    Select-String -Pattern "@Repository" | Measure-Object).Count

$javaxCount = (Get-ChildItem -Path $microservicesPath -Filter "*.java" -Recurse | 
    Where-Object { $_.FullName -notmatch "\\target\\|\\test\\|documentation|docs" } |
    Select-String -Pattern "import javax\." | Measure-Object).Count

Write-Host "  剩余@Autowired: $autowiredCount" -ForegroundColor $(if ($autowiredCount -eq 0) { "Green" } else { "Red" })
Write-Host "  剩余@Repository: $repositoryCount" -ForegroundColor $(if ($repositoryCount -eq 0) { "Green" } else { "Red" })
Write-Host "  剩余javax.*: $javaxCount" -ForegroundColor $(if ($javaxCount -eq 0) { "Green" } else { "Red" })
Write-Host ""

# =====================================================
# 5. 生成修复报告
# =====================================================
Write-Host "[5/5] 生成修复报告..." -ForegroundColor Yellow

$reportPath = Join-Path $rootPath "documentation\technical\100_PERCENT_FIX_REPORT.md"
$reportContent = @"
# 100%完成修复报告

> **修复时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
> **修复脚本**: fix-100-percent-completion.ps1

## 修复统计

| 修复项 | 修复前 | 修复后 | 状态 |
|--------|--------|--------|------|
| @Autowired | 26 | $autowiredCount | $(if ($autowiredCount -eq 0) { "✅ 100%完成" } else { "⚠️ 待完成" }) |
| @Repository | 34 | $repositoryCount | $(if ($repositoryCount -eq 0) { "✅ 100%完成" } else { "⚠️ 待完成" }) |
| javax.* | 11 | $javaxCount | $(if ($javaxCount -eq 0) { "✅ 100%完成" } else { "⚠️ 待完成" }) |

## 错误文件

$(if ($errorFiles.Count -eq 0) { "无错误文件" } else { $errorFiles | ForEach-Object { "- $_" } | Out-String })

## 下一步

$(if ($autowiredCount -eq 0 -and $repositoryCount -eq 0 -and $javaxCount -eq 0) {
    "✅ 所有代码规范问题已100%修复！"
} else {
    "⚠️ 仍有部分问题需要手动修复，请检查上述统计"
})
"@

Set-Content -Path $reportPath -Value $reportContent -Encoding UTF8
Write-Host "  [完成] 报告已生成: $reportPath" -ForegroundColor Green
Write-Host ""

# =====================================================
# 完成
# =====================================================
Write-Host "================================================" -ForegroundColor Cyan
if ($autowiredCount -eq 0 -and $repositoryCount -eq 0 -and $javaxCount -eq 0) {
    Write-Host "✅ 100%完成修复成功！" -ForegroundColor Green
} else {
    Write-Host "⚠️ 部分修复完成，请检查报告" -ForegroundColor Yellow
}
Write-Host "================================================" -ForegroundColor Cyan

