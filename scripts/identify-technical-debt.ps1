# ============================================================
# IOE-DREAM 技术债识别脚本
#
# 功能：识别技术债务并生成管理清单
# ============================================================

param(
    [switch]$Detailed = $false,
    [string]$OutputDir = "documentation/project",
    [switch]$CI = $false
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$microservicesDir = Join-Path $projectRoot "microservices"
$outputPath = Join-Path $projectRoot $OutputDir

if (-not (Test-Path $outputPath)) {
    New-Item -ItemType Directory -Path $outputPath -Force | Out-Null
}

$technicalDebt = @{
    Timestamp  = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Categories = @{
        Architecture  = @()
        CodeQuality   = @()
        Performance   = @()
        Security      = @()
        Documentation = @()
        Testing       = @()
        Dependencies  = @()
    }
    Summary    = @{
        Total      = 0
        ByPriority = @{
            P0 = 0
            P1 = 0
            P2 = 0
        }
        ByCategory = @{}
    }
}

if (-not $CI) {
    Write-Host "===== 技术债识别 =====" -ForegroundColor Cyan
    Write-Host ""
}

# ==================== 1. 架构违规技术债 ====================
if (-not $CI) {
    Write-Host "[1/6] 识别架构违规技术债..." -ForegroundColor Yellow
}

$architectureDebt = @()

# 检查@Repository违规
$repoViolations = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*.java" |
Select-String -Pattern '@Repository' |
Where-Object { $_.Line -match '^\s*@Repository' -and $_.Line -notmatch '禁止|//|/\*' }

foreach ($v in $repoViolations) {
    $architectureDebt += @{
        Type        = "@Repository违规"
        File        = $v.Path.Replace($projectRoot, "").TrimStart('\', '/')
        Line        = $v.LineNumber
        Description = "应使用@Mapper注解而非@Repository"
        Priority    = "P0"
        Effort      = "Low"
        Impact      = "High"
    }
}

# 检查@Autowired违规
$autowiredViolations = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*.java" |
Select-String -Pattern '@Autowired' |
Where-Object { $_.Line -match '^\s*@Autowired' -and $_.Line -notmatch '禁止|//|/\*' }

foreach ($v in $autowiredViolations) {
    $architectureDebt += @{
        Type        = "@Autowired违规"
        File        = $v.Path.Replace($projectRoot, "").TrimStart('\', '/')
        Line        = $v.LineNumber
        Description = "应使用@Resource注解而非@Autowired"
        Priority    = "P0"
        Effort      = "Low"
        Impact      = "Medium"
    }
}

# 检查Jakarta EE迁移
$jakartaViolations = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*.java" |
Select-String -Pattern '^import\s+javax\.(annotation|validation|persistence|servlet|transaction|inject)'

foreach ($v in $jakartaViolations) {
    $architectureDebt += @{
        Type        = "Jakarta EE迁移"
        File        = $v.Path.Replace($projectRoot, "").TrimStart('\', '/')
        Line        = $v.LineNumber
        Description = "应迁移到jakarta包名"
        Priority    = "P1"
        Effort      = "Low"
        Impact      = "Medium"
    }
}

$technicalDebt.Categories.Architecture = $architectureDebt
$technicalDebt.Summary.Total += $architectureDebt.Count

if (-not $CI) {
    Write-Host "  发现架构技术债: $($architectureDebt.Count) 项" -ForegroundColor $(if ($architectureDebt.Count -eq 0) { "Green" } else { "Yellow" })
    Write-Host ""
}

# ==================== 2. 代码质量技术债 ====================
if (-not $CI) {
    Write-Host "[2/6] 识别代码质量技术债..." -ForegroundColor Yellow
}

$codeQualityDebt = @()

# 检查测试覆盖率（简化检查）
$testFiles = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*Test.java" | Measure-Object
$javaFiles = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*.java" |
Where-Object { $_.FullName -notlike "*\test\*" -and $_.FullName -notlike "*\target\*" } | Measure-Object

if ($javaFiles.Count -gt 0) {
    $testRatio = ($testFiles.Count / $javaFiles.Count) * 100
    if ($testRatio -lt 50) {
        $codeQualityDebt += @{
            Type        = "测试覆盖率不足"
            File        = "全局"
            Line        = 0
            Description = "测试覆盖率低于50%，目标应为80%+"
            Priority    = "P1"
            Effort      = "High"
            Impact      = "High"
            Metric      = "$([Math]::Round($testRatio, 2))%"
        }
    }
}

# 检查大文件（>500行）
$largeFiles = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*.java" |
Where-Object { $_.FullName -notlike "*\target\*" } |
Where-Object {
    $lineCount = (Get-Content $_.FullName | Measure-Object -Line).Lines
    $lineCount -gt 500
}

foreach ($file in $largeFiles) {
    $lineCount = (Get-Content $file.FullName | Measure-Object -Line).Lines
    $codeQualityDebt += @{
        Type        = "文件过大"
        File        = $file.FullName.Replace($projectRoot, "").TrimStart('\', '/')
        Line        = 0
        Description = "文件行数($lineCount)超过500行，建议拆分"
        Priority    = "P2"
        Effort      = "Medium"
        Impact      = "Medium"
        Metric      = "$lineCount 行"
    }
}

$technicalDebt.Categories.CodeQuality = $codeQualityDebt
$technicalDebt.Summary.Total += $codeQualityDebt.Count

if (-not $CI) {
    Write-Host "  发现代码质量技术债: $($codeQualityDebt.Count) 项" -ForegroundColor $(if ($codeQualityDebt.Count -eq 0) { "Green" } else { "Yellow" })
    Write-Host ""
}

# ==================== 3. 性能技术债 ====================
if (-not $CI) {
    Write-Host "[3/6] 识别性能技术债..." -ForegroundColor Yellow
}

$performanceDebt = @()

# 检查HikariCP使用
$hikariConfigs = Get-ChildItem -Path $microservicesDir -Recurse -Filter "application*.yml" |
Select-String -Pattern 'hikari:' |
Where-Object { $_.Line -notmatch '#.*hikari|禁止' }

foreach ($v in $hikariConfigs) {
    $performanceDebt += @{
        Type        = "连接池不统一"
        File        = $v.Path.Replace($projectRoot, "").TrimStart('\', '/')
        Line        = $v.LineNumber
        Description = "应统一使用Druid连接池"
        Priority    = "P1"
        Effort      = "Low"
        Impact      = "Medium"
    }
}

$technicalDebt.Categories.Performance = $performanceDebt
$technicalDebt.Summary.Total += $performanceDebt.Count

if (-not $CI) {
    Write-Host "  发现性能技术债: $($performanceDebt.Count) 项" -ForegroundColor $(if ($performanceDebt.Count -eq 0) { "Green" } else { "Yellow" })
    Write-Host ""
}

# ==================== 4. 安全技术债 ====================
if (-not $CI) {
    Write-Host "[4/6] 识别安全技术债..." -ForegroundColor Yellow
}

$securityDebt = @()

# 检查明文密码（配置文件）
$plaintextPasswords = Get-ChildItem -Path $microservicesDir -Recurse -Filter "*.yml" |
Select-String -Pattern 'password:\s*(nacos|123456|password|admin)' -CaseSensitive:$false |
Where-Object { $_.Line -notmatch 'ENC\(|加密|encrypted' }

foreach ($v in $plaintextPasswords) {
    $securityDebt += @{
        Type        = "明文密码"
        File        = $v.Path.Replace($projectRoot, "").TrimStart('\', '/')
        Line        = $v.LineNumber
        Description = "配置文件中存在明文密码，应使用加密配置"
        Priority    = "P0"
        Effort      = "Low"
        Impact      = "Critical"
    }
}

$technicalDebt.Categories.Security = $securityDebt
$technicalDebt.Summary.Total += $securityDebt.Count

if (-not $CI) {
    Write-Host "  发现安全技术债: $($securityDebt.Count) 项" -ForegroundColor $(if ($securityDebt.Count -eq 0) { "Green" } else { "Red" })
    Write-Host ""
}

# ==================== 5. 文档技术债 ====================
if (-not $CI) {
    Write-Host "[5/6] 识别文档技术债..." -ForegroundColor Yellow
}

$documentationDebt = @()

# 检查根目录临时文档
$rootDocs = Get-ChildItem -Path $projectRoot -File -Filter "*.md" |
Where-Object { $_.Name -match '-report|NACOS_|ENCRYPTION' }

foreach ($doc in $rootDocs) {
    $documentationDebt += @{
        Type        = "临时文档未归档"
        File        = $doc.Name
        Line        = 0
        Description = "临时文档应在documentation目录管理"
        Priority    = "P2"
        Effort      = "Low"
        Impact      = "Low"
    }
}

$technicalDebt.Categories.Documentation = $documentationDebt
$technicalDebt.Summary.Total += $documentationDebt.Count

if (-not $CI) {
    Write-Host "  发现文档技术债: $($documentationDebt.Count) 项" -ForegroundColor $(if ($documentationDebt.Count -eq 0) { "Green" } else { "Yellow" })
    Write-Host ""
}

# ==================== 6. 依赖技术债 ====================
if (-not $CI) {
    Write-Host "[6/6] 识别依赖技术债..." -ForegroundColor Yellow
}

$dependenciesDebt = @()

# 检查硬编码版本
$pomFiles = Get-ChildItem -Path $microservicesDir -Filter "pom.xml" -Recurse |
Where-Object { $_.FullName -notlike "*\target\*" }

foreach ($pom in $pomFiles) {
    $content = Get-Content $pom.FullName -Raw
    $hardcodedVersions = [regex]::Matches($content, '<version>(\d+\.\d+\.\d+)</version>') |
    Where-Object { $_.Groups[1].Value -notmatch '\$\{' }

    if ($hardcodedVersions.Count -gt 0) {
        $dependenciesDebt += @{
            Type        = "硬编码版本"
            File        = $pom.FullName.Replace($projectRoot, "").TrimStart('\', '/')
            Line        = 0
            Description = "应使用父POM的properties引用版本"
            Priority    = "P2"
            Effort      = "Low"
            Impact      = "Low"
            Metric      = "$($hardcodedVersions.Count) 个硬编码版本"
        }
    }
}

$technicalDebt.Categories.Dependencies = $dependenciesDebt
$technicalDebt.Summary.Total += $dependenciesDebt.Count

if (-not $CI) {
    Write-Host "  发现依赖技术债: $($dependenciesDebt.Count) 项" -ForegroundColor $(if ($dependenciesDebt.Count -eq 0) { "Green" } else { "Yellow" })
    Write-Host ""
}

# ==================== 统计优先级 ====================
foreach ($category in $technicalDebt.Categories.PSObject.Properties) {
    foreach ($debt in $category.Value) {
        $priority = $debt.Priority
        if ($technicalDebt.Summary.ByPriority.ContainsKey($priority)) {
            $technicalDebt.Summary.ByPriority[$priority]++
        }
        else {
            $technicalDebt.Summary.ByPriority[$priority] = 1
        }
    }

    $technicalDebt.Summary.ByCategory[$category.Name] = $category.Value.Count
}

# ==================== 生成技术债清单文档 ====================
$debtMd = @"
# IOE-DREAM 技术债清单

**生成时间**: $($technicalDebt.Timestamp)
**总技术债数**: $($technicalDebt.Summary.Total)

## 技术债统计

### 按优先级

| 优先级 | 数量 | 占比 |
|--------|------|------|
| P0 (紧急) | $($technicalDebt.Summary.ByPriority.P0) | $([Math]::Round(($technicalDebt.Summary.ByPriority.P0 / [Math]::Max($technicalDebt.Summary.Total, 1)) * 100, 1))% |
| P1 (重要) | $($technicalDebt.Summary.ByPriority.P1) | $([Math]::Round(($technicalDebt.Summary.ByPriority.P1 / [Math]::Max($technicalDebt.Summary.Total, 1)) * 100, 1))% |
| P2 (一般) | $($technicalDebt.Summary.ByPriority.P2) | $([Math]::Round(($technicalDebt.Summary.ByPriority.P2 / [Math]::Max($technicalDebt.Summary.Total, 1)) * 100, 1))% |

### 按类别

| 类别 | 数量 |
|------|------|
| 架构违规 | $($technicalDebt.Summary.ByCategory.Architecture) |
| 代码质量 | $($technicalDebt.Summary.ByCategory.CodeQuality) |
| 性能 | $($technicalDebt.Summary.ByCategory.Performance) |
| 安全 | $($technicalDebt.Summary.ByCategory.Security) |
| 文档 | $($technicalDebt.Summary.ByCategory.Documentation) |
| 依赖 | $($technicalDebt.Summary.ByCategory.Dependencies) |

## 详细技术债清单

"@

foreach ($category in $technicalDebt.Categories.PSObject.Properties) {
    if ($category.Value.Count -gt 0) {
        $debtMd += "`n### $($category.Name)`n`n"
        $debtMd += "| 类型 | 文件 | 行号 | 描述 | 优先级 | 工作量 | 影响 |`n"
        $debtMd += "|------|------|------|------|--------|--------|------|`n"

        foreach ($debt in $category.Value) {
            $file = $debt.File -replace '\|', '\|'
            $line = if ($debt.Line -gt 0) { $debt.Line.ToString() } else { "-" }
            $desc = $debt.Description -replace '\|', '\|'
            $debtMd += "| $($debt.Type) | $file | $line | $desc | $($debt.Priority) | $($debt.Effort) | $($debt.Impact) |`n"
        }
    }
}

$debtMd += @"

## 偿还计划建议

### 第1周（P0紧急修复）
- 修复所有安全技术债（明文密码）
- 修复P0级架构违规（@Repository、@Autowired）

### 第2-4周（P1重要修复）
- 修复性能技术债（连接池统一）
- 修复Jakarta EE迁移
- 提升测试覆盖率

### 第5-8周（P2持续优化）
- 代码质量优化（大文件拆分）
- 文档规范化
- 依赖版本统一

---

**下次审查时间**: $(Get-Date -Date ((Get-Date).AddDays(7)) -Format "yyyy-MM-dd")
**审查责任人**: IOE-DREAM 架构委员会

"@

$debtListPath = Join-Path $outputPath "TECHNICAL_DEBT.md"
$debtMd | Out-File -FilePath $debtListPath -Encoding UTF8

# 生成JSON报告
$jsonPath = Join-Path $outputPath "technical-debt_$(Get-Date -Format "yyyyMMdd_HHmmss").json"
$technicalDebt | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonPath -Encoding UTF8

if (-not $CI) {
    Write-Host ""
    Write-Host "===== 识别完成 =====" -ForegroundColor Cyan
    Write-Host "总技术债数: $($technicalDebt.Summary.Total)" -ForegroundColor $(if ($technicalDebt.Summary.Total -eq 0) { "Green" } else { "Yellow" })
    Write-Host ""
    Write-Host "按优先级:" -ForegroundColor Cyan
    Write-Host "  P0: $($technicalDebt.Summary.ByPriority.P0)" -ForegroundColor Red
    Write-Host "  P1: $($technicalDebt.Summary.ByPriority.P1)" -ForegroundColor Yellow
    Write-Host "  P2: $($technicalDebt.Summary.ByPriority.P2)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "技术债清单已生成:" -ForegroundColor Cyan
    Write-Host "  Markdown: $($debtListPath.Replace($projectRoot, "").TrimStart('\', '/'))" -ForegroundColor Gray
    Write-Host "  JSON: $(Split-Path $jsonPath -Leaf)" -ForegroundColor Gray
    Write-Host ""
}

exit 0

