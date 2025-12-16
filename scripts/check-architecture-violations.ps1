# ============================================================
# IOE-DREAM 架构合规性检查脚本
#
# 功能：全面检查所有架构违规项（仅检查，不修改）
# ============================================================

$ErrorActionPreference = "Stop"

Write-Host "===== IOE-DREAM 架构合规性检查 =====" -ForegroundColor Cyan
Write-Host ""

# ==================== 1. 检查@Repository违规 ====================
Write-Host "[1/4] 检查@Repository违规..." -ForegroundColor Yellow

$repositoryViolations = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern '@Repository' |
    Where-Object {
        $_.Line -notmatch '禁止|禁止使用|禁止@Repository|//.*@Repository|/\*.*@Repository' -and
        $_.Line -match '^\s*@Repository'
    }

if ($repositoryViolations) {
    Write-Host "  发现 $($repositoryViolations.Count) 个违规:" -ForegroundColor Red
    $repositoryViolations | ForEach-Object {
        Write-Host "    $($_.Path):$($_.LineNumber) - $($_.Line.Trim())" -ForegroundColor Gray
    }
} else {
    Write-Host "  [OK] 未发现@Repository违规" -ForegroundColor Green
}
Write-Host ""

# ==================== 2. 检查@Autowired违规 ====================
Write-Host "[2/4] 检查@Autowired违规..." -ForegroundColor Yellow

$autowiredViolations = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern '@Autowired' |
    Where-Object {
        $_.Line -notmatch '禁止|禁止使用|禁止@Autowired|//.*@Autowired|/\*.*@Autowired' -and
        $_.Line -match '^\s*@Autowired'
    }

if ($autowiredViolations) {
    Write-Host "  发现 $($autowiredViolations.Count) 个违规:" -ForegroundColor Red
    $autowiredViolations | ForEach-Object {
        Write-Host "    $($_.Path):$($_.LineNumber) - $($_.Line.Trim())" -ForegroundColor Gray
    }
} else {
    Write-Host "  [OK] 未发现@Autowired违规" -ForegroundColor Green
}
Write-Host ""

# ==================== 3. 检查javax包名违规 ====================
Write-Host "[3/4] 检查javax包名违规..." -ForegroundColor Yellow

$javaxViolations = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern '^import\s+javax\.(annotation|validation|persistence|servlet|transaction|inject)'

if ($javaxViolations) {
    Write-Host "  发现 $($javaxViolations.Count) 个违规:" -ForegroundColor Red
    $javaxViolations | ForEach-Object {
        Write-Host "    $($_.Path):$($_.LineNumber) - $($_.Line.Trim())" -ForegroundColor Gray
    }
} else {
    Write-Host "  [OK] 未发现javax包名违规" -ForegroundColor Green
}
Write-Host ""

# ==================== 4. 检查HikariCP配置 ====================
Write-Host "[4/4] 检查HikariCP配置违规..." -ForegroundColor Yellow

$hikariViolations = Get-ChildItem -Path "microservices" -Recurse -Filter "application*.yml" |
    Select-String -Pattern 'hikari:|HikariDataSource|type:\s*.*hikari' |
    Where-Object {
        $_.Line -notmatch '禁止|禁止使用|#.*hikari|LOG_LEVEL_HIKARI'
    }

if ($hikariViolations) {
    Write-Host "  发现 $($hikariViolations.Count) 个违规:" -ForegroundColor Red
    $hikariViolations | ForEach-Object {
        Write-Host "    $($_.Path):$($_.LineNumber) - $($_.Line.Trim())" -ForegroundColor Gray
    }
} else {
    Write-Host "  [OK] 未发现HikariCP配置违规" -ForegroundColor Green
}
Write-Host ""

# ==================== 检查总结 ====================
Write-Host "===== 检查完成 =====" -ForegroundColor Cyan
$total = ($repositoryViolations.Count) + ($autowiredViolations.Count) + ($javaxViolations.Count) + ($hikariViolations.Count)
Write-Host "  总计发现: $total 个违规项" -ForegroundColor $(if ($total -eq 0) { "Green" } else { "Red" })
Write-Host ""
