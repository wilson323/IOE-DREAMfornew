# Entity规范检查脚本
# 严格遵循CLAUDE.md架构规范，检测Entity设计违规

param(
    [string]$ProjectRoot = ".",
    [switch]$Fix = $false,
    [switch]$Report = $true
)

Write-Host "🔍 IOE-DREAM Entity规范检查" -ForegroundColor Green
Write-Host "检查项目: $ProjectRoot" -ForegroundColor Cyan
Write-Host "自动修复: $Fix" -ForegroundColor Cyan
Write-Host "生成报告: $Report" -ForegroundColor Cyan
Write-Host ""

# 统计变量
$totalFiles = 0
$violations = 0
$largeEntities = 0
$businessLogicViolations = 0
$staticMethodViolations = 0
$commentViolations = 0
$compliantFiles = 0

$reportData = @()

# 获取所有Entity文件
$entityFiles = Get-ChildItem -Path $ProjectRoot -Recurse -Filter "*Entity.java" | Where-Object { $_.FullName -notlike "*test*" }

Write-Host "📊 检查 $($entityFiles.Count) 个Entity文件..." -ForegroundColor Yellow

foreach ($file in $entityFiles) {
    $totalFiles++
    $fileViolations = @()
    $lineCount = (Get-Content $file.FullName | Measure-Object -Line).Lines
    $content = Get-Content $file.FullName -Raw

    # 检查1: 超大实体
    if ($lineCount -gt 400) {
        $fileViolations += "超大实体: $lineCount 行 (超过400行限制)"
        $largeEntities++
        $violations++
    }
    elseif ($lineCount -gt 200) {
        $fileViolations += "大型实体: $lineCount 行 (建议≤200行)"
        $violations++
    }

    # 检查2: 业务逻辑方法
    if ($content -match "public.*\{.*return.*[^;]*;" -or $content -match "private.*\{.*return.*[^;]*;") {
        $fileViolations += "包含业务逻辑方法 (Entity应纯数据模型)"
        $businessLogicViolations++
        $violations++
    }

    # 检查3: Static方法
    if ($content -match "static.*\(") {
        $fileViolations += "包含Static方法 (Entity禁止包含)"
        $staticMethodViolations++
        $violations++
    }

    # 检查4: Repository违规
    if ($content -match "@Repository" -or $content -match "extends JpaRepository") {
        $fileViolations += "Repository违规 (应使用@Mapper)"
        $violations++
    }

    # 检查5: 继承BaseEntity
    if ($content -notmatch "extends BaseEntity") {
        $fileViolations += "未继承BaseEntity (必须继承)"
        $violations++
    }

    # 检查6: 冗余注释
    $commentLines = [regex]::Matches($content, "/\*\*[\s\S]*?\*/").Count
    $fieldCount = [regex]::Matches($content, "@TableField").Count
    if ($commentLines -gt 0 -and $fieldCount -gt 0) {
        $avgCommentLength = $commentLines / $fieldCount
        if ($avgCommentLength -gt 5) {
            $fileViolations += "注释冗余 (平均每个字段$avgCommentLength行，建议≤3行)"
            $commentViolations++
        }
    }

    # 输出结果
    $relativePath = $file.FullName.Replace($ProjectRoot, "").Replace("\", "/").TrimStart("/")

    if ($fileViolations.Count -eq 0) {
        Write-Host "✅ $relativePath" -ForegroundColor Green
        $compliantFiles++
    } else {
        Write-Host "❌ $relativePath ($lineCount 行)" -ForegroundColor Red
        foreach ($violation in $fileViolations) {
            Write-Host "   → $violation" -ForegroundColor Yellow
        }
    }

    # 记录报告数据
    $reportData += @{
        File = $relativePath
        Lines = $lineCount
        Violations = $fileViolations
        Status = if ($fileViolations.Count -eq 0) { "Compliant" } else { "Violation" }
    }
}

# 输出统计信息
Write-Host ""
Write-Host "📈 检查统计:" -ForegroundColor Cyan
Write-Host "总文件数: $totalFiles" -ForegroundColor White
Write-Host "合规文件: $compliantFiles" -ForegroundColor Green
Write-Host "违规文件: $($totalFiles - $compliantFiles)" -ForegroundColor Red
Write-Host "总违规数: $violations" -ForegroundColor Red
Write-Host "- 超大实体: $largeEntities" -ForegroundColor Yellow
Write-Host "- 业务逻辑违规: $businessLogicViolations" -ForegroundColor Yellow
Write-Host "- Static方法违规: $staticMethodViolations" -ForegroundColor Yellow
Write-Host "- 注释冗余: $commentViolations" -ForegroundColor Yellow

# 计算合规率
$complianceRate = [math]::Round(($compliantFiles / $totalFiles) * 100, 2)
Write-Host "合规率: $complianceRate%" -ForegroundColor $(if ($complianceRate -ge 80) { "Green" } elseif ($complianceRate -ge 60) { "Yellow" } else { "Red" })

# 生成详细报告
if ($Report) {
    $reportPath = Join-Path $ProjectRoot "documentation\technical\ENTITY_SPECIFICATION_CHECK_REPORT.md"

    $reportContent = @"
# Entity规范检查报告

**检查时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**检查项目**: IOE-DREAM
**检查范围**: $($entityFiles.Count) 个Entity文件
**合规率**: $complianceRate%

## 📊 统计概览

| 指标 | 数量 | 占比 |
|------|------|------|
| 总文件数 | $totalFiles | 100% |
| 合规文件 | $compliantFiles | $complianceRate% |
| 违规文件 | $($totalFiles - $compliantFiles) | $([math]::Round((($totalFiles - $compliantFiles) / $totalFiles) * 100, 2))% |
| 总违规数 | $violations | - |

### 违规类型分布

| 违规类型 | 数量 | 优先级 |
|---------|------|--------|
| 超大实体(>400行) | $largeEntities | 🔴 P0 |
| 业务逻辑违规 | $businessLogicViolations | 🔴 P0 |
| Static方法违规 | $staticMethodViolations | 🟠 P1 |
| 注释冗余 | $commentViolations | 🟡 P2 |

## 📋 详细检查结果

| 文件路径 | 行数 | 违规数 | 状态 |
|---------|------|--------|------|
"@

    foreach ($item in $reportData) {
        $violationsText = if ($item.Violations.Count -eq 0) { "无" } else { $item.Violations.Count }
        $reportContent += "`n| $($item.File) | $($item.Lines) | $violationsText | $($item.Status) |"
    }

    $reportContent += @"

## 🔧 优化建议

### P0级立即处理
1. **超大实体拆分**: $largeEntities 个Entity超过400行，必须拆分
2. **业务逻辑迁移**: 将Entity中的业务逻辑迁移到Manager层
3. **Static方法移除**: 将Static工具方法移到Utils类

### P1级计划处理
1. **注释优化**: $commentViolations 个Entity需要简化注释
2. **Repository检查**: 确保所有DAO使用@Mapper而非@Repository

## 📚 相关规范

- **CLAUDE.md**: [全局架构规范](../../../CLAUDE.md)
- **Entity设计规范**: [Entity规范要求](./ENTITY_SPECIFICATION_ANALYSIS_REPORT.md)

---

**🎯 目标合规率**: ≥95%
**📅 下次检查**: 1周后
"@

    # 确保目录存在
    $reportDir = Split-Path $reportPath -Parent
    if (-not (Test-Path $reportDir)) {
        New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
    }

    $reportContent | Out-File -FilePath $reportPath -Encoding UTF8
    Write-Host ""
    Write-Host "📄 详细报告已生成: $reportPath" -ForegroundColor Green
}

# 输出结论
Write-Host ""
if ($complianceRate -ge 80) {
    Write-Host "🎉 Entity规范检查通过！合规率 $complianceRate% >= 80%" -ForegroundColor Green
} else {
    Write-Host "⚠️ Entity规范需要改进！合规率 $complianceRate% < 80%" -ForegroundColor Red
    Write-Host "请参考CLAUDE.md规范要求，优先处理P0级违规问题" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "💡 建议: 定期运行此检查脚本，确保持续符合规范要求" -ForegroundColor Cyan