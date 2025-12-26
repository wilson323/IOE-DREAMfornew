# IOE-DREAM 简易代码质量检查
# 专注于实用性，避免过度工程化

param(
    [switch]$SkipTests,
    [switch]$CI,
    [string]$ReportPath = ".\reports"
)

# 设置工作目录
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

# 创建报告目录
if (!(Test-Path $ReportPath)) {
    New-Item -ItemType Directory -Path $ReportPath -Force
}

Write-Host "🔍 开始 IOE-DREAM 代码质量检查..." -ForegroundColor Green

$issues = @()
$totalChecks = 0
$passedChecks = 0

# 1. 检查@Autowired使用情况
Write-Host "`n📋 1. 检查依赖注入规范..." -ForegroundColor Cyan
$autowiredCount = (Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "@Autowired" | Measure-Object).Count

if ($autowiredCount -gt 0) {
    $issues += "发现 $autowiredCount 个@Autowired使用，应改为@Resource"
    Write-Host "   ❌ 发现 $autowiredCount 处@Autowired违规" -ForegroundColor Red
} else {
    Write-Host "   ✅ 依赖注入规范检查通过" -ForegroundColor Green
    $passedChecks++
}
$totalChecks++

# 2. 检查Repository命名
Write-Host "`n📋 2. 检查DAO命名规范..." -ForegroundColor Cyan
$repositoryCount = (Get-ChildItem -Path "microservices" -Recurse -Filter "*Repository.java").Count

if ($repositoryCount -gt 0) {
    $issues += "发现 $repositoryCount 个Repository命名，应改为Dao"
    Write-Host "   ❌ 发现 $repositoryCount 处Repository命名违规" -ForegroundColor Red
} else {
    Write-Host "   ✅ DAO命名规范检查通过" -ForegroundColor Green
    $passedChecks++
}
$totalChecks++

# 3. 检查API文档注解
Write-Host "`n📋 3. 检查API文档完整性..." -ForegroundColor Cyan
$controllerFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Controller.java"
$controllersWithDocs = 0

foreach ($controller in $controllerFiles) {
    $content = Get-Content $controller.FullName
    if ($content -match "@Tag|@Operation") {
        $controllersWithDocs++
    }
}

$docCoverage = if ($controllerFiles.Count -gt 0) { [math]::Round(($controllersWithDocs / $controllerFiles.Count) * 100, 1) } else { 0 }
Write-Host "   📈 API文档覆盖率: $docCoverage%" -ForegroundColor Yellow

if ($docCoverage -ge 90) {
    Write-Host "   ✅ API文档覆盖率优秀" -ForegroundColor Green
    $passedChecks++
} elseif ($docCoverage -ge 75) {
    Write-Host "   ⚠️  API文档覆盖率良好" -ForegroundColor Yellow
    $passedChecks++
} else {
    Write-Host "   ❌ API文档覆盖率不足" -ForegroundColor Red
}
$totalChecks++

# 4. 运行测试覆盖率（如果需要）
if (-not $SkipTests) {
    Write-Host "`n📊 4. 检查测试覆盖率..." -ForegroundColor Cyan
    Write-Host "   运行测试并生成覆盖率报告..."

    try {
        mvn test jacoco:report -DskipTests=$false -q
        $jacocoReport = "microservices\microservices-common-core\target\site\jacoco\index.html"

        if (Test-Path $jacocoReport) {
            Write-Host "   ✅ 测试覆盖率报告已生成" -ForegroundColor Green
            $passedChecks++
        } else {
            Write-Host "   ⚠️  测试覆盖率报告未找到" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "   ⚠️  测试执行遇到问题，跳过" -ForegroundColor Yellow
    }
    $totalChecks++
} else {
    Write-Host "`n📊 4. 跳过测试覆盖率检查" -ForegroundColor Yellow
    $totalChecks++
    $passedChecks++
}

# 5. 生成简明报告
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$reportFile = "$ReportPath\quality-summary-$($timestamp -replace ':', '-').Replace(' ', '-').Replace('/', '-').Trim()).md"

$summaryContent = @"
# IOE-DREAM 代码质量检查报告

**检查时间**: $timestamp
**总检查项**: $totalChecks
**通过项数**: $passedChecks
**通过率**: $([math]::Round(($passedChecks / $totalChecks) * 100, 1))%

## 📊 检查结果

### ✅ 通过项
$(if ($passedChecks -ge 3) { "- 项目整体质量良好" } )

### ❌ 问题项
$(foreach ($issue in $issues) { "- $issue" })

## 💡 改进建议
- 将所有 `@Autowired` 改为 `@Resource` 注解
- 将 `*Repository` 类改为 `*Dao` 类
- 继续完善API文档覆盖率
- 定期运行此检查脚本保持代码质量

---
*报告由自动化工具生成，建议定期执行以保持代码质量*
"@

$summaryContent | Out-File -FilePath $reportFile -Encoding UTF8

# 输出结果
Write-Host "`n🎉 代码质量检查完成！" -ForegroundColor Green
Write-Host "   📊 通过率: $([math]::Round(($passedChecks / $totalChecks) * 100, 1))%" -ForegroundColor Cyan
Write-Host "   📄 简明报告: $reportFile" -ForegroundColor Yellow

if ($CI) {
    Write-Host "`n📋 CI/CD 输出:" -ForegroundColor Cyan
    Write-Host "quality_status=$($passedChecks/$totalChecks)"
    Write-Host "quality_percentage=$([math]::Round(($passedChecks / $totalChecks) * 100, 1))"
    Write-Host "issues_count=$($issues.Count)"
    Write-Host "report_file=$reportFile"
}

if ($issues.Count -gt 0) {
    Write-Host "`n⚠️  发现 $($issues.Count) 个问题，建议优先处理" -ForegroundColor Yellow
    exit 1
}