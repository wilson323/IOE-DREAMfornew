# IOE-DREAM 技术栈一致性检查脚本
# Technology Stack Consistency Check Script

param(
    [string]$SkillsPath = ".claude/skills",
    [switch]$Fix,
    [switch]$Report,
    [switch]$Verbose,
    [string]$OutputPath = "."
)

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )

    switch ($Color) {
        "Red" { Write-Host $Message -ForegroundColor Red }
        "Green" { Write-Host $Message -ForegroundColor Green }
        "Yellow" { Write-Host $Message -ForegroundColor Yellow }
        "Blue" { Write-Host $Message -ForegroundColor Blue }
        "Cyan" { Write-Host $Message -ForegroundColor Cyan }
        "Magenta" { Write-Host $Message -ForegroundColor Magenta }
        default { Write-Host $Message }
    }
}

# 检查技术栈一致性
function Test-TechnologyStackConsistency {
    param(
        [string]$Path,
        [bool]$Detailed = $false
    )

    Write-ColorOutput "🔍 开始检查技术栈一致性..." "Cyan"
    Write-ColorOutput "📁 检查路径: $Path" "Blue"

    $issues = @()
    $files = Get-ChildItem -Path $Path -Filter "*.md" -Recurse
    $totalFiles = $files.Count
    $processedFiles = 0

    Write-ColorOutput "📊 发现 $totalFiles 个技能文档" "Blue"

    foreach ($file in $files) {
        $processedFiles++
        $relativePath = $file.FullName.Replace((Get-Location).Path, "").TrimStart('\', '/')
        Write-Progress -Activity "检查技能文档" -Status "处理: $relativePath" -PercentComplete (($processedFiles / $totalFiles) * 100)

        if ($Verbose) {
            Write-ColorOutput "🔍 检查文件: $relativePath" "Blue"
        }

        try {
            $content = Get-Content $file.FullName -Raw -Encoding UTF8
            $lines = Get-Content $file.FullName -Encoding UTF8
            $fileIssues = @()

            # 检查P0级禁止项
            if ($content -match "@Autowired") {
                $lineNumbers = @()
                for ($i = 0; $i -lt $lines.Count; $i++) {
                    if ($lines[$i] -match "@Autowired") {
                        $lineNumbers += ($i + 1)
                    }
                }
                $fileIssues += @{
                    File = $relativePath
                    Type = "P0禁止项"
                    Issue = "使用@Autowired注解"
                    Severity = "P0"
                    LineNumbers = $lineNumbers
                    Recommendation = "必须使用@Resource注解替换@Autowired"
                }
                if ($Verbose) {
                    Write-ColorOutput "  ❌ 发现@Autowired注解 (行: $($lineNumbers -join ', '))" "Red"
                }
            }

            if ($content -match "@Repository") {
                $lineNumbers = @()
                for ($i = 0; $i -lt $lines.Count; $i++) {
                    if ($lines[$i] -match "@Repository") {
                        $lineNumbers += ($i + 1)
                    }
                }
                $fileIssues += @{
                    File = $relativePath
                    Type = "P0禁止项"
                    Issue = "使用@Repository注解"
                    Severity = "P0"
                    LineNumbers = $lineNumbers
                    Recommendation = "必须使用@Mapper注解替换@Repository，并使用Dao后缀命名"
                }
                if ($Verbose) {
                    Write-ColorOutput "  ❌ 发现@Repository注解 (行: $($lineNumbers -join ', '))" "Red"
                }
            }

            if ($content -match "javax\.(annotation|validation|persistence|transaction|servlet|ej|jms|mail)") {
                $lineNumbers = @()
                for ($i = 0; $i -lt $lines.Count; $i++) {
                    if ($lines[$i] -match "javax\.(annotation|validation|persistence|transaction|servlet|ej|jms|mail)") {
                        $lineNumbers += ($i + 1)
                    }
                }
                $fileIssues += @{
                    File = $relativePath
                    Type = "P0禁止项"
                    Issue = "使用javax包名"
                    Severity = "P0"
                    LineNumbers = $lineNumbers
                    Recommendation = "必须使用jakarta包名替换javax包名"
                }
                if ($Verbose) {
                    Write-ColorOutput "  ❌ 发现javax包名 (行: $($lineNumbers -join ', '))" "Red"
                }
            }

            # 检查P1级问题
            if ($content -match "HikariCP|hikari") {
                $lineNumbers = @()
                for ($i = 0; $i -lt $lines.Count; $i++) {
                    if ($lines[$i] -match "HikariCP|hikari") {
                        $lineNumbers += ($i + 1)
                    }
                }
                $fileIssues += @{
                    File = $relativePath
                    Type = "P1问题"
                    Issue = "使用HikariCP连接池"
                    Severity = "P1"
                    LineNumbers = $lineNumbers
                    Recommendation = "必须使用Druid连接池替换HikariCP"
                }
                if ($Verbose) {
                    Write-ColorOutput "  ⚠️ 发现HikariCP引用 (行: $($lineNumbers -join ', '))" "Yellow"
                }
            }

            if ($content -match "JPA|jpa|Hibernate|hibernate") {
                $lineNumbers = @()
                for ($i = 0; $i -lt $lines.Count; $i++) {
                    if ($lines[$i] -match "JPA|jpa|Hibernate|hibernate") {
                        $lineNumbers += ($i + 1)
                    }
                }
                $fileIssues += @{
                    File = $relativePath
                    Type = "P1问题"
                    Issue = "使用JPA或Hibernate"
                    Severity = "P1"
                    LineNumbers = $lineNumbers
                    Recommendation = "必须使用MyBatis-Plus替换JPA/Hibernate"
                }
                if ($Verbose) {
                    Write-ColorOutput "  ⚠️ 发现JPA/Hibernate引用 (行: $($lineNumbers -join ', '))" "Yellow"
                }
            }

            # 检查缺失项
            if ($content -notmatch "技术栈要求|Technology Stack|推荐技术栈|技术栈标准") {
                $fileIssues += @{
                    File = $relativePath
                    Type = "缺失项"
                    Issue = "缺少技术栈声明"
                    Severity = "P1"
                    LineNumbers = @()
                    Recommendation = "必须添加完整的技术栈要求和标准声明"
                }
                if ($Verbose) {
                    Write-ColorOutput "  ⚠️ 缺少技术栈声明" "Yellow"
                }
            }

            if ($content -notmatch "Spring Boot 3\.5\.8|3\.5\.\d+") {
                $fileIssues += @{
                    File = $relativePath
                    Type = "缺失项"
                    Issue = "缺少Spring Boot版本要求"
                    Severity = "P2"
                    LineNumbers = @()
                    Recommendation = "必须明确指定Spring Boot 3.5.8+版本要求"
                }
                if ($Verbose) {
                    Write-ColorOutput "  ⚠️ 缺少Spring Boot版本要求" "Yellow"
                }
            }

            $issues += $fileIssues

            if ($fileIssues.Count -eq 0 -and $Verbose) {
                Write-ColorOutput "  ✅ 检查通过，无问题" "Green"
            }

        } catch {
            Write-ColorOutput "  ❌ 读取文件失败: $($_.Exception.Message)" "Red"
            $issues += @{
                File = $relativePath
                Type = "系统错误"
                Issue = "文件读取失败"
                Severity = "P0"
                LineNumbers = @()
                Recommendation = "检查文件编码和权限"
            }
        }
    }

    Write-Progress -Activity "检查技能文档" -Completed

    return $issues
}

# 生成详细报告
function New-TechnologyStackReport {
    param(
        [array]$Issues,
        [string]$OutputPath,
        [bool]$Detailed = $false
    )

    $timestamp = Get-Date -Format "yyyy-MM-dd-HH-mm-ss"
    $reportPath = Join-Path $OutputPath "technology-stack-consistency-report-$timestamp.html"

    $p0Issues = $issues | Where-Object { $_.Severity -eq "P0" }
    $p1Issues = $issues | Where-Object { $_.Severity -eq "P1" }
    $p2Issues = $issues | Where-Object { $_.Severity -eq "P2" }

    $html = @"
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IOE-DREAM 技术栈一致性检查报告</title>
    <style>
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #2c3e50; text-align: center; margin-bottom: 30px; }
        h2 { color: #34495e; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
        h3 { color: #2980b9; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .summary-card { background: #ecf0f1; padding: 20px; border-radius: 8px; text-align: center; }
        .summary-card h3 { margin: 0 0 10px 0; color: #2c3e50; }
        .summary-card .number { font-size: 2em; font-weight: bold; }
        .p0 { color: #e74c3c; }
        .p1 { color: #f39c12; }
        .p2 { color: #3498db; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #3498db; color: white; }
        tr:hover { background-color: #f5f5f5; }
        .severity-p0 { background-color: #fadbd8; }
        .severity-p1 { background-color: #fdebd0; }
        .severity-p2 { background-color: #d6eaf8; }
        .footer { margin-top: 30px; text-align: center; color: #7f8c8d; }
        .recommendation { background: #e8f5e8; padding: 10px; border-radius: 5px; margin-top: 5px; }
        @media (max-width: 768px) {
            .summary { grid-template-columns: 1fr; }
            body { margin: 10px; }
            .container { padding: 15px; }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🔍 IOE-DREAM 技术栈一致性检查报告</h1>
        <p style="text-align: center; color: #7f8c8d;">生成时间: $timestamp</p>

        <div class="summary">
            <div class="summary-card">
                <h3>总问题数</h3>
                <div class="number">$($issues.Count)</div>
            </div>
            <div class="summary-card">
                <h3>P0级问题</h3>
                <div class="number p0">$($p0Issues.Count)</div>
            </div>
            <div class="summary-card">
                <h3>P1级问题</h3>
                <div class="number p1">$($p1Issues.Count)</div>
            </div>
            <div class="summary-card">
                <h3>P2级问题</h3>
                <div class="number p2">$($p2Issues.Count)</div>
            </div>
        </div>

        <h2>📊 问题统计</h2>
        <table>
            <thead>
                <tr>
                    <th>问题类型</th>
                    <th>数量</th>
                    <th>占比</th>
                    <th>说明</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>P0禁止项</td>
                    <td class="p0">$($p0Issues.Count)</td>
                    <td>$([math]::Round(($p0Issues.Count / $issues.Count) * 100, 1))%</td>
                    <td>严重问题，必须立即修复</td>
                </tr>
                <tr>
                    <td>P1问题</td>
                    <td class="p1">$($p1Issues.Count)</td>
                    <td>$([math]::Round(($p1Issues.Count / $issues.Count) * 100, 1))%</td>
                    <td>重要问题，建议尽快修复</td>
                </tr>
                <tr>
                    <td>P2问题</td>
                    <td class="p2">$($p2Issues.Count)</td>
                    <td>$([math]::Round(($p2Issues.Count / $issues.Count) * 100, 1))%</td>
                    <td>一般问题，可以后续修复</td>
                </tr>
            </tbody>
        </table>

        <h2>🚨 P0级问题详情</h2>
        <table>
            <thead>
                <tr>
                    <th>文件</th>
                    <th>问题类型</th>
                    <th>问题描述</th>
                    <th>行号</th>
                    <th>建议修复</th>
                </tr>
            </thead>
            <tbody>
"@

    foreach ($issue in $p0Issues) {
        $lineNumbersText = if ($issue.LineNumbers.Count -gt 0) { $issue.LineNumbers -join ", " } else { "N/A" }
        $html += @"
                <tr class="severity-p0">
                    <td>$($issue.File)</td>
                    <td>$($issue.Type)</td>
                    <td>$($issue.Issue)</td>
                    <td>$lineNumbersText</td>
                    <td>
                        <div class="recommendation">
                            <strong>修复建议:</strong> $($issue.Recommendation)
                        </div>
                    </td>
                </tr>
"@
    }

    $html += @"
            </tbody>
        </table>

        <h2>⚠️ P1级问题详情</h2>
        <table>
            <thead>
                <tr>
                    <th>文件</th>
                    <th>问题类型</th>
                    <th>问题描述</th>
                    <th>行号</th>
                    <th>建议修复</th>
                </tr>
            </thead>
            <tbody>
"@

    foreach ($issue in $p1Issues) {
        $lineNumbersText = if ($issue.LineNumbers.Count -gt 0) { $issue.LineNumbers -join ", " } else { "N/A" }
        $html += @"
                <tr class="severity-p1">
                    <td>$($issue.File)</td>
                    <td>$($issue.Type)</td>
                    <td>$($issue.Issue)</td>
                    <td>$lineNumbersText</td>
                    <td>
                        <div class="recommendation">
                            <strong>修复建议:</strong> $($issue.Recommendation)
                        </div>
                    </td>
                </tr>
"@
    }

    $html += @"
            </tbody>
        </table>

        <h2>📋 P2级问题详情</h2>
        <table>
            <thead>
                <tr>
                    <th>文件</th>
                    <th>问题类型</th>
                    <th>问题描述</th>
                    <th>行号</th>
                    <th>建议修复</th>
                </tr>
            </thead>
            <tbody>
"@

    foreach ($issue in $p2Issues) {
        $lineNumbersText = if ($issue.LineNumbers.Count -gt 0) { $issue.LineNumbers -join ", " } else { "N/A" }
        $html += @"
                <tr class="severity-p2">
                    <td>$($issue.File)</td>
                    <td>$($issue.Type)</td>
                    <td>$($issue.Issue)</td>
                    <td>$lineNumbersText</td>
                    <td>
                        <div class="recommendation">
                            <strong>修复建议:</strong> $($issue.Recommendation)
                        </div>
                    </td>
                </tr>
"@
    }

    $html += @"
            </tbody>
        </table>

        <div class="footer">
            <p>📝 本报告由 IOE-DREAM 技术栈一致性检查工具自动生成</p>
            <p>🔧 如有问题，请联系架构委员会或技术栈专家</p>
        </div>
    </div>
</body>
</html>
"@

    try {
        $html | Out-File -FilePath $reportPath -Encoding UTF8
        Write-ColorOutput "📄 HTML报告已生成: $reportPath" "Green"

        # 同时生成CSV报告
        $csvPath = Join-Path $OutputPath "technology-stack-consistency-report-$timestamp.csv"
        $issues | Export-Csv -Path $csvPath -NoTypeInformation -Encoding UTF8
        Write-ColorOutput "📊 CSV报告已生成: $csvPath" "Green"

        return $reportPath
    } catch {
        Write-ColorOutput "❌ 生成报告失败: $($_.Exception.Message)" "Red"
        return $null
    }
}

# 自动修复问题
function Repair-TechnologyStackIssues {
    param(
        [array]$Issues,
        [string]$SkillsPath
    )

    Write-ColorOutput "🔧 开始自动修复技术栈问题..." "Yellow"

    $fixedCount = 0
    $errorCount = 0

    foreach ($issue in $Issues) {
        if ($issue.Severity -eq "P0") {
            $filePath = Join-Path $SkillsPath $issue.File

            if (Test-Path $filePath) {
                try {
                    $content = Get-Content $filePath -Raw -Encoding UTF8
                    $originalContent = $content

                    # 修复@Autowired问题
                    if ($issue.Issue -eq "使用@Autowired注解") {
                        $content = $content -replace "@Autowired", "@Resource"
                        Write-ColorOutput "  ✅ 已修复 @Autowired → @Resource: $($issue.File)" "Green"
                    }

                    # 修复@Repository问题
                    if ($issue.Issue -eq "使用@Repository注解") {
                        $content = $content -replace "@Repository", "@Mapper"
                        Write-ColorOutput "  ✅ 已修复 @Repository → @Mapper: $($issue.File)" "Green"
                    }

                    # 修复javax包名问题
                    if ($issue.Issue -eq "使用javax包名") {
                        # 基本的javax到jakarta替换
                        $content = $content -replace "javax\.annotation\.Resource", "jakarta.annotation.Resource"
                        $content = $content -replace "javax\.validation\.Valid", "jakarta.validation.Valid"
                        $content = $content -replace "javax\.validation\.constraints", "jakarta.validation.constraints"
                        $content = $content -replace "javax\.persistence\.Entity", "jakarta.persistence.Entity"
                        $content = $content -replace "javax\.persistence\.Table", "jakarta.persistence.Table"
                        $content = $content -replace "javax\.persistence\.Column", "jakarta.persistence.Column"
                        $content = $content -replace "javax\.persistence\.Id", "jakarta.persistence.Id"
                        $content = $content -replace "javax\.transaction\.Transactional", "jakarta.transaction.Transactional"
                        $content = $content -replace "javax\.servlet\.http\.HttpServletRequest", "jakarta.servlet.http.HttpServletRequest"
                        $content = $content -replace "javax\.servlet\.http\.HttpServletResponse", "jakarta.servlet.http.HttpServletResponse"
                        Write-ColorOutput "  ✅ 已修复 javax → jakarta: $($issue.File)" "Green"
                    }

                    # 只有内容发生变化时才写入文件
                    if ($content -ne $originalContent) {
                        $content | Out-File -FilePath $filePath -Encoding UTF8
                        $fixedCount++
                    }

                } catch {
                    Write-ColorOutput "  ❌ 修复失败 $($issue.File): $($_.Exception.Message)" "Red"
                    $errorCount++
                }
            } else {
                Write-ColorOutput "  ❌ 文件不存在: $filePath" "Red"
                $errorCount++
            }
        }
    }

    Write-ColorOutput "🎯 修复完成: 成功 $fixedCount 个，失败 $errorCount 个" "Cyan"
    return $fixedCount, $errorCount
}

# 主执行逻辑
try {
    Write-ColorOutput "🚀 IOE-DREAM 技术栈一致性检查工具" "Cyan"
    Write-ColorOutput "========================================" "Cyan"

    # 检查路径是否存在
    if (-not (Test-Path $SkillsPath)) {
        Write-ColorOutput "❌ 错误: 技能文档路径不存在: $SkillsPath" "Red"
        exit 1
    }

    # 执行检查
    $issues = Test-TechnologyStackConsistency -Path $SkillsPath -Detailed $Verbose

    # 显示检查结果
    Write-ColorOutput "📊 检查结果统计:" "Cyan"
    $p0Count = ($issues | Where-Object { $_.Severity -eq "P0" }).Count
    $p1Count = ($issues | Where-Object { $_.Severity -eq "P1" }).Count
    $p2Count = ($issues | Where-Object { $_.Severity -eq "P2" }).Count

    Write-ColorOutput "  🔴 P0级问题: $p0Count 个" $(if($p0Count -gt 0){"Red"} else {"Green"})
    Write-ColorOutput "  🟡 P1级问题: $p1Count 个" $(if($p1Count -gt 0){"Yellow"} else {"Green"})
    Write-ColorOutput "  🔵 P2级问题: $p2Count 个" $(if($p2Count -gt 0){"Blue"} else {"Green"})
    Write-ColorOutput "  📋 总计: $($issues.Count) 个问题" "Cyan"

    # 显示P0级问题详情
    if ($p0Count -gt 0) {
        Write-ColorOutput "`n🚨 P0级问题详情 (需要立即修复):" "Red"
        $p0Issues = $issues | Where-Object { $_.Severity -eq "P0" }
        foreach ($issue in $p0Issues) {
            Write-ColorOutput "  ❌ $($issue.File): $($issue.Issue)" "Red"
            if ($Verbose) {
                Write-ColorOutput "     建议: $($issue.Recommendation)" "Yellow"
            }
        }
    }

    # 生成报告
    if ($Report) {
        $reportPath = New-TechnologyStackReport -Issues $issues -OutputPath $OutputPath -Detailed $Verbose
        if ($reportPath) {
            Write-ColorOutput "`n📄 详细报告已生成" "Green"
        }
    }

    # 自动修复
    if ($Fix -and $p0Count -gt 0) {
        Write-ColorOutput "`n🔧 开始自动修复 P0 级问题..." "Yellow"
        $fixed, $errors = Repair-TechnologyStackIssues -Issues $issues -SkillsPath $SkillsPath

        if ($errors -eq 0 -and $fixed -gt 0) {
            Write-ColorOutput "✅ 自动修复完成，建议重新运行检查验证结果" "Green"
        }
    }

    # 返回适当的退出码
    if ($p0Count -gt 0) {
        Write-ColorOutput "`n❌ 检查失败：发现 $p0Count 个P0级问题，需要立即修复" "Red"
        exit 1
    } elseif ($p1Count -gt 0) {
        Write-ColorOutput "`n⚠️ 检查通过但存在建议修复项：发现 $p1Count 个P1级问题" "Yellow"
        exit 0
    } else {
        Write-ColorOutput "`n✅ 检查完全通过：所有技术栈符合规范" "Green"
        exit 0
    }

} catch {
    Write-ColorOutput "❌ 脚本执行异常: $($_.Exception.Message)" "Red"
    if ($Verbose) {
        Write-ColorOutput "异常详情: $($_.Exception.ToString())" "Red"
    }
    exit 1
}