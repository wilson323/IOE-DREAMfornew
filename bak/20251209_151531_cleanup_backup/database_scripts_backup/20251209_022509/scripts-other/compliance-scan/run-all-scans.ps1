# 执行所有合规性扫描
# 用途: 一键执行所有扫描脚本并生成综合报告
# 执行: powershell -ExecutionPolicy Bypass -File run-all-scans.ps1

$scriptPath = $PSScriptRoot
$projectRoot = "D:\IOE-DREAM"
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$summaryFile = "$projectRoot\reports\compliance-summary-$timestamp.md"

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║       IOE-DREAM 项目合规性全面扫描                           ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# 创建报告目录
New-Item -ItemType Directory -Force -Path "$projectRoot\reports" | Out-Null

# 执行扫描
Write-Host "🔍 第1步: 扫描 @Repository 违规..." -ForegroundColor Yellow
& "$scriptPath\scan-repository-violations.ps1"
Write-Host ""

Write-Host "🔍 第2步: 扫描 @Autowired 违规..." -ForegroundColor Yellow
& "$scriptPath\scan-autowired-violations.ps1"
Write-Host ""

Write-Host "🔍 第3步: 扫描架构违规..." -ForegroundColor Yellow
& "$scriptPath\scan-architecture-violations.ps1"
Write-Host ""

# 读取各个扫描结果
$repoFile = Get-ChildItem -Path "$projectRoot\reports" -Filter "repository-violations-*.txt" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$autoFile = Get-ChildItem -Path "$projectRoot\reports" -Filter "autowired-violations-*.txt" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$archFile = Get-ChildItem -Path "$projectRoot\reports" -Filter "architecture-violations-*.txt" | Sort-Object LastWriteTime -Descending | Select-Object -First 1

# 解析统计数据
$repoContent = Get-Content $repoFile.FullName -Raw
$autoContent = Get-Content $autoFile.FullName -Raw
$archContent = Get-Content $archFile.FullName -Raw

# 提取统计数字（简化版本，实际应使用更精确的正则）
$repoViolations = ([regex]::Match($repoContent, "总违规数.*?(\d+)")).Groups[1].Value
$autoViolations = ([regex]::Match($autoContent, "违规注解总数.*?(\d+)")).Groups[1].Value
$archViolations = ([regex]::Match($archContent, "总违规数.*?(\d+)")).Groups[1].Value

# 生成综合报告
$summary = @"
# IOE-DREAM 项目合规性扫描综合报告

**扫描时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**扫描范围**: 全项目代码库
**扫描类型**: @Repository违规、@Autowired违规、架构违规

---

## 📊 执行摘要

### 整体合规性评估

| 扫描项 | 违规数量 | 状态 | 优先级 |
|--------|---------|------|--------|
| **@Repository注解违规** | $repoViolations | $(if ([int]$repoViolations -eq 0) { "✅ 合规" } else { "❌ 需要修复" }) | 🔴 P0 |
| **@Autowired注解违规** | $autoViolations | $(if ([int]$autoViolations -eq 0) { "✅ 合规" } else { "❌ 需要修复" }) | 🔴 P0 |
| **架构边界违规** | $archViolations | $(if ([int]$archViolations -eq 0) { "✅ 合规" } else { "❌ 需要修复" }) | 🔴 P0 |

### 合规性评分

``````
总违规数: $([int]$repoViolations + [int]$autoViolations + [int]$archViolations)
合规性评分: $(100 - [Math]::Min(100, ([int]$repoViolations + [int]$autoViolations + [int]$archViolations)))%
``````

---

## 📋 详细报告

### 1. @Repository违规扫描
- **详细报告**: [repository-violations-$timestamp.txt](./repository-violations-$timestamp.txt)
- **违规数量**: $repoViolations
- **修复建议**: 将@Repository替换为@Mapper，更新命名为Dao后缀

### 2. @Autowired违规扫描
- **详细报告**: [autowired-violations-$timestamp.txt](./autowired-violations-$timestamp.txt)
- **违规数量**: $autoViolations
- **修复建议**: 将@Autowired替换为@Resource（jakarta.annotation.Resource）

### 3. 架构违规扫描
- **详细报告**: [architecture-violations-$timestamp.txt](./architecture-violations-$timestamp.txt)
- **违规数量**: $archViolations
- **修复建议**: Controller层不应直接注入DAO/Manager，应通过Service层

---

## 🎯 修复优先级

### P0 - 立即修复（架构核心规范）
1. ❌ @Repository注解违规: $repoViolations 处
2. ❌ @Autowired注解违规: $autoViolations 处
3. ❌ 架构边界违规: $archViolations 处

### 预计修复时间
- @Repository修复: 2-3小时
- @Autowired修复: 2-3小时
- 架构违规修复: 4-6小时
- **总计**: 8-12小时

---

## 📈 下一步行动

### 立即行动
1. ✅ 基线扫描完成（本次扫描）
2. ⏳ 开始Phase 1修复工作
3. ⏳ 按照 [COMPREHENSIVE_REMEDIATION_PLAN_2025-12-03.md](../COMPREHENSIVE_REMEDIATION_PLAN_2025-12-03.md) 执行

### 持续监控
- 每周执行一次合规性扫描
- Git提交前自动检查（Pre-commit Hook）
- CI/CD流程中集成合规检查

---

## 📞 支持和反馈

如有问题或建议，请：
1. 查看完整修复计划: [COMPREHENSIVE_REMEDIATION_PLAN_2025-12-03.md](../COMPREHENSIVE_REMEDIATION_PLAN_2025-12-03.md)
2. 查看架构规范: [CLAUDE.md](../CLAUDE.md)
3. 联系架构团队

---

**报告生成**: IOE-DREAM 合规性检查系统  
**下次扫描**: 建议在修复完成后立即扫描验证
"@

# 保存综合报告
$summary | Out-File -FilePath $summaryFile -Encoding UTF8

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║                   扫描完成！                                  ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "📊 扫描统计:" -ForegroundColor Cyan
Write-Host "   - @Repository违规: $repoViolations" -ForegroundColor $(if ([int]$repoViolations -gt 0) { "Red" } else { "Green" })
Write-Host "   - @Autowired违规: $autoViolations" -ForegroundColor $(if ([int]$autoViolations -gt 0) { "Red" } else { "Green" })
Write-Host "   - 架构违规: $archViolations" -ForegroundColor $(if ([int]$archViolations -gt 0) { "Red" } else { "Green" })
Write-Host "   - 总违规数: $([int]$repoViolations + [int]$autoViolations + [int]$archViolations)" -ForegroundColor $(if (([int]$repoViolations + [int]$autoViolations + [int]$archViolations) -gt 0) { "Red" } else { "Green" })
Write-Host ""
Write-Host "📄 报告位置:" -ForegroundColor Cyan
Write-Host "   - 综合报告: $summaryFile" -ForegroundColor White
Write-Host "   - @Repository报告: $($repoFile.FullName)" -ForegroundColor White
Write-Host "   - @Autowired报告: $($autoFile.FullName)" -ForegroundColor White
Write-Host "   - 架构违规报告: $($archFile.FullName)" -ForegroundColor White
Write-Host ""
Write-Host "🎯 下一步: 查看 COMPREHENSIVE_REMEDIATION_PLAN_2025-12-03.md 开始修复工作" -ForegroundColor Yellow

