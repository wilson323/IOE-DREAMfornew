# IOE-DREAM 文档质量自动化检查脚本
# 版本: v1.0.0
# 创建时间: 2025-12-16
# 功能: 自动检查文档质量和规范合规性

param(
    [string]$DocumentationPath = "D:\IOE-DREAM\documentation",
    [switch]$FixIssues,
    [switch]$GenerateReport
)

Write-Host "🔍 IOE-DREAM 文档质量自动化检查开始..." -ForegroundColor Green
Write-Host "📁 检查路径: $DocumentationPath" -ForegroundColor Cyan

# 初始化统计信息
$Stats = @{
    TotalFiles = 0
    ValidFiles = 0
    Issues = @()
    FixedIssues = 0
}

# 检查结果类型
$IssueTypes = @{
    "MissingMetadata" = "缺少文档元信息"
    "IncorrectTechStack" = "技术栈版本不正确"
    "BrokenLinks" = "失效链接"
    "DuplicateContent" = "重复内容"
    "NonStandardNaming" = "命名不规范"
    "OutdatedContent" = "内容过期"
}

# 检查函数
function Test-DocumentMetadata {
    param([string]$FilePath)

    $content = Get-Content $FilePath -Raw
    $issues = @()

    # 检查是否有元信息头部
    if (-not $content.StartsWith("---")) {
        $issues += "文档缺少元信息头部"
        return $issues
    }

    # 提取元信息
    $metadataEnd = $content.IndexOf("---", 3)
    if ($metadataEnd -eq -1) {
        $issues += "元信息格式不正确"
        return $issues
    }

    $metadata = $content.Substring(3, $metadataEnd - 3)

    # 检查必需字段
    $requiredFields = @("version", "created_date", "last_updated", "文档负责人")
    foreach ($field in $requiredFields) {
        if ($metadata -notmatch "$field\s*:") {
            $issues += "缺少必需字段: $field"
        }
    }

    # 检查技术栈版本
    if ($content -match "Spring Boot\s+([\d.]+)") {
        $version = $matches[1]
        if ($version -ne "3.5.8") {
            $issues += "Spring Boot版本不正确: $version，应为3.5.8"
        }
    }

    return $issues
}

function Test-BrokenLinks {
    param([string]$Content, [string]$BasePath)

    $issues = @()
    # 匹配Markdown链接
    $linkPattern = "\[([^\]]+)\]\(([^)]+)\)"
    $matches = [regex]::Matches($Content, $linkPattern)

    foreach ($match in $matches) {
        $link = $match.Groups[2].Value
        # 跳过外部链接
        if ($link.StartsWith("http")) { continue }

        $targetPath = Join-Path $BasePath $link
        if (-not (Test-Path $targetPath)) {
            $issues += "失效链接: $link"
        }
    }

    return $issues
}

function Test-DuplicateContent {
    param([string]$Content, [hashtable]$ContentHashes)

    $hash = [System.Security.Cryptography.MD5]::Create().ComputeHash([System.Text.Encoding]::UTF8.GetBytes($Content))
    $hashString = [System.Convert]::ToBase64String($hash)

    if ($ContentHashes.ContainsKey($hashString)) {
        return "重复内容，与文件: $($ContentHashes[$hashString])"
    }

    $ContentHashes[$hashString] = $FilePath
    return $null
}

function Test-FileNaming {
    param([string]$FilePath)

    $fileName = Split-Path $FilePath -Leaf
    $issues = @()

    # 检查文件名规范
    if ($fileName -match " " ) {
        $issues += "文件名包含空格"
    }

    if ($fileName -cmatch "[A-Z]" -and $fileName -notmatch "README\.md|CLAUDE\.md") {
        $issues += "文件名包含大写字母（除README.md和CLAUDE.md外）"
    }

    return $issues
}

function Test-ContentFreshness {
    param([string]$FilePath, [datetime]$LastUpdated)

    $issues = @()
    $threshold = (Get-Date).AddDays(-90)  # 90天为过期阈值

    if ($LastUpdated -lt $threshold) {
        $daysOld = (Get-Date) - $LastUpdated
        $issues += "内容可能过期，最后更新: $($daysOld.Days)天前"
    }

    return $issues
}

# 主检查逻辑
Write-Host "📋 开始扫描文档文件..." -ForegroundColor Yellow

$mdFiles = Get-ChildItem -Path $DocumentationPath -Recurse -Filter "*.md"
$contentHashes = @{}

foreach ($file in $mdFiles) {
    $Stats.TotalFiles++
    $relativePath = $file.FullName.Replace($DocumentationPath, "").TrimStart('\', '/')

    Write-Host "检查: $relativePath" -ForegroundColor Gray

    try {
        $content = Get-Content $file.FullName -Raw
        $fileIssues = @()

        # 跳过某些特殊文件
        if ($relativePath -match "bak|archive|node_modules") {
            continue
        }

        # 1. 检查元信息
        $metadataIssues = Test-DocumentMetadata -FilePath $file.FullName
        $fileIssues += $metadataIssues

        # 2. 检查失效链接
        $linkIssues = Test-BrokenLinks -Content $content -BasePath (Split-Path $file.FullName)
        $fileIssues += $linkIssues

        # 3. 检查重复内容
        $duplicateIssues = Test-DuplicateContent -Content $content -ContentHashes $contentHashes
        if ($duplicateIssues) {
            $fileIssues += $duplicateIssues
        }

        # 4. 检查文件命名
        $namingIssues = Test-FileNaming -FilePath $file.FullName
        $fileIssues += $namingIssues

        # 5. 检查内容新鲜度
        $lastUpdated = $file.LastWriteTime
        $freshnessIssues = Test-ContentFreshness -FilePath $file.FullName -LastUpdated $lastUpdated
        $fileIssues += $freshnessIssues

        if ($fileIssues.Count -eq 0) {
            $Stats.ValidFiles++
            Write-Host "  ✅ 通过" -ForegroundColor Green
        } else {
            Write-Host "  ❌ 发现 $($fileIssues.Count) 个问题:" -ForegroundColor Red
            foreach ($issue in $fileIssues) {
                Write-Host "    - $issue" -ForegroundColor Red
                $Stats.Issues += @{
                    File = $relativePath
                    Issue = $issue
                    Type = "Quality"
                }
            }
        }
    }
    catch {
        Write-Host "  ⚠️ 检查失败: $($_.Exception.Message)" -ForegroundColor Yellow
        $Stats.Issues += @{
            File = $relativePath
            Issue = "文件读取失败: $($_.Exception.Message)"
            Type = "Error"
        }
    }
}

# 生成报告
if ($GenerateReport) {
    $reportPath = Join-Path $DocumentationPath "documentation-quality-report.md"

    $report = @"
# IOE-DREAM 文档质量检查报告

> **检查时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
> **检查范围**: $DocumentationPath
> **脚本版本**: v1.0.0

---

## 📊 检查统计

| 指标 | 数值 |
|------|------|
| **总文档数** | $($Stats.TotalFiles) |
| **通过文档数** | $($Stats.ValidFiles) |
| **问题文档数** | $($Stats.Issues.Count) |
| **通过率** | $([math]::Round(($Stats.ValidFiles / $Stats.TotalFiles) * 100, 2))% |

---

## 🚨 问题详情

"@

    # 按类型分组问题
    $groupedIssues = $Stats.Issues | Group-Object { $_.Type }

    foreach ($group in $groupedIssues) {
        $report += "`n### $($group.Name) ($($group.Count))`n`n"

        foreach ($issue in $group.Group) {
            $report += "- **[$($issue.File)]($($issue.File))**: $($issue.Issue)`n"
        }
    }

    # 添加改进建议
    $report += @"

---

## 🎯 改进建议

### 立即修复 (P0)
1. 补充缺失的文档元信息
2. 更新不正确的技术栈版本
3. 修复失效的文档链接

### 短期优化 (P1)
1. 清理重复内容文档
2. 规范文件命名
3. 更新过期的文档内容

### 长期维护 (P2)
1. 建立定期检查机制
2. 完善文档审核流程
3. 提升文档质量标准

---

## 📞 联系方式

如有疑问，请联系IOE-DREAM架构委员会。

**📅 下次检查建议时间**: $(Get-Date).AddDays(30).ToString('yyyy-MM-dd')
"@

    $report | Out-File -FilePath $reportPath -Encoding UTF8
    Write-Host "📄 报告已生成: $reportPath" -ForegroundColor Green
}

# 自动修复
if ($FixIssues) {
    Write-Host "🔧 尝试自动修复问题..." -ForegroundColor Yellow

    # 这里可以添加自动修复逻辑
    # 例如：自动补充元信息模板、修正技术栈版本等

    $Stats.FixedIssues = 0  # 示例
    Write-Host "✅ 已修复 $($Stats.FixedIssues) 个问题" -ForegroundColor Green
}

# 总结
Write-Host "`n🎯 检查完成！" -ForegroundColor Green
Write-Host "📊 统计结果:" -ForegroundColor Cyan
Write-Host "  总文档数: $($Stats.TotalFiles)" -ForegroundColor White
Write-Host "  通过文档: $($Stats.ValidFiles)" -ForegroundColor Green
Write-Host "  问题文档: $($Stats.Issues.Count)" -ForegroundColor Red

if ($Stats.Issues.Count -gt 0) {
    Write-Host "`n⚠️ 发现问题，请查看详细报告或运行 -FixIssues 参数尝试自动修复" -ForegroundColor Yellow
} else {
    Write-Host "`n🎉 所有文档都通过了质量检查！" -ForegroundColor Green
}

Write-Host "✨ 文档质量检查脚本执行完成" -ForegroundColor Magenta