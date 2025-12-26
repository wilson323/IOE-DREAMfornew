# ============================================================
# IOE-DREAM 明文密码扫描脚本
# 扫描所有配置文件中的明文密码，生成详细报告
# 重要：本脚本仅扫描，不修改任何文件
# ============================================================

param(
    [string]$OutputFile = "plaintext-passwords-report.txt",
    [switch]$Detailed,
    [switch]$Json
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM 明文密码扫描脚本" -ForegroundColor Cyan
Write-Host "  仅扫描，不修改任何文件" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

# 定义扫描的文件类型
$configFilePatterns = @(
    "**/application*.yml",
    "**/application*.yaml",
    "**/bootstrap*.yml",
    "**/bootstrap*.yaml",
    "**/.env*",
    "**/docker-compose*.yml",
    "**/docker-compose*.yaml",
    "**/*.properties"
)

# 定义明文密码模式（排除已加密的格式）
$plaintextPatterns = @(
    @{
        Name           = "数据库密码"
        Pattern        = '(?i)(?:password|pwd)\s*[:=]\s*["'']?([^"''\s${}ENC()]+)["'']?'
        ExcludePattern = '(?i)ENC\(|^\$\{|^\{|^null$|^empty$|^""$|^''''$'
    },
    @{
        Name           = "Redis密码"
        Pattern        = '(?i)redis.*password\s*[:=]\s*["'']?([^"''\s${}ENC()]+)["'']?'
        ExcludePattern = '(?i)ENC\(|^\$\{|^\{|^null$|^empty$|^""$|^''''$'
    },
    @{
        Name           = "Nacos密码"
        Pattern        = '(?i)nacos.*password\s*[:=]\s*["'']?([^"''\s${}ENC()]+)["'']?'
        ExcludePattern = '(?i)ENC\(|^\$\{|^\{|^null$|^empty$|^""$|^''''$'
    },
    @{
        Name           = "RabbitMQ密码"
        Pattern        = '(?i)rabbitmq.*password\s*[:=]\s*["'']?([^"''\s${}ENC()]+)["'']?'
        ExcludePattern = '(?i)ENC\(|^\$\{|^\{|^null$|^empty$|^""$|^''''$'
    },
    @{
        Name           = "JWT密钥"
        Pattern        = '(?i)jwt.*secret\s*[:=]\s*["'']?([^"''\s${}ENC()]+)["'']?'
        ExcludePattern = '(?i)ENC\(|^\$\{|^\{|^null$|^empty$|^""$|^''''$'
    },
    @{
        Name           = "API密钥"
        Pattern        = '(?i)(?:api[_-]?key|access[_-]?key|secret[_-]?key)\s*[:=]\s*["'']?([^"''\s${}ENC()]+)["'']?'
        ExcludePattern = '(?i)ENC\(|^\$\{|^\{|^null$|^empty$|^""$|^''''$'
    }
)

# 排除的目录
$excludeDirs = @(
    "target",
    "node_modules",
    ".git",
    ".idea",
    "logs",
    "backup-*",
    "documentation-backup-*"
)

# 收集所有配置文件
Write-Host "`n[1/4] 扫描配置文件..." -ForegroundColor Yellow
$configFiles = @()

# 确保在项目根目录执行
$projectRoot = if (Test-Path "microservices") { Get-Location } else { Split-Path -Parent $PSScriptRoot }
Set-Location $projectRoot

foreach ($pattern in $configFilePatterns) {
    # 处理通配符模式
    $searchPath = if ($pattern -like "**/*") {
        $pattern -replace '\*\*/', ''
    }
    else {
        "microservices"
    }

    $files = Get-ChildItem -Path $searchPath -Include $pattern -Recurse -ErrorAction SilentlyContinue |
    Where-Object {
        $excluded = $false
        foreach ($excludeDir in $excludeDirs) {
            if ($_.FullName -like "*\$excludeDir\*") {
                $excluded = $true
                break
            }
        }
        -not $excluded -and $_.FullName -notlike "*\target\*"
    }
    $configFiles += $files
}

# 去重
$configFiles = $configFiles | Select-Object -Unique FullName

Write-Host "  找到 $($configFiles.Count) 个配置文件" -ForegroundColor Green

# 扫描明文密码
Write-Host "`n[2/4] 扫描明文密码..." -ForegroundColor Yellow

$findings = @()
$totalCount = 0

foreach ($file in $configFiles) {
    try {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        if (-not $content) { continue }

        $fileFindings = @()
        $lineNumber = 0

        foreach ($line in ($content -split "`r?`n")) {
            $lineNumber++

            foreach ($patternInfo in $plaintextPatterns) {
                $matches = [regex]::Matches($line, $patternInfo.Pattern)

                foreach ($match in $matches) {
                    if ($match.Groups.Count -gt 1) {
                        $passwordValue = $match.Groups[1].Value.Trim()

                        # 排除已加密的格式
                        if ($passwordValue -notmatch $patternInfo.ExcludePattern) {
                            # 排除常见的占位符和空值
                            if ($passwordValue.Length -gt 2 -and
                                $passwordValue -notmatch '^(?:null|empty|""|''''|\$\{.*\})$') {

                                $finding = @{
                                    File    = $file.FullName.Replace((Get-Location).Path + "\", "")
                                    Line    = $lineNumber
                                    Type    = $patternInfo.Name
                                    Pattern = $match.Value
                                    Value   = $passwordValue
                                    Context = $line.Trim()
                                }

                                $fileFindings += $finding
                                $findings += $finding
                                $totalCount++
                            }
                        }
                    }
                }
            }
        }

        if ($fileFindings.Count -gt 0) {
            Write-Host "  ⚠ 发现 $($fileFindings.Count) 个问题: $($file.Name)" -ForegroundColor Red
        }
    }
    catch {
        Write-Host "  ✗ 读取文件失败: $($file.FullName) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n[3/4] 生成扫描报告..." -ForegroundColor Yellow

# 生成报告
$report = @"
============================================================
IOE-DREAM 明文密码扫描报告
生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
============================================================

扫描统计:
- 扫描文件数: $($configFiles.Count)
- 发现问题数: $totalCount
- 涉及文件数: $(($findings | Select-Object -Unique File).Count)

============================================================
详细问题列表
============================================================

"@

# 按文件分组
$groupedFindings = $findings | Group-Object File | Sort-Object Name

foreach ($group in $groupedFindings) {
    $report += "`n文件: $($group.Name)`n"
    $report += "问题数: $($group.Count)`n"
    $report += "-" * 80 + "`n"

    foreach ($finding in $group.Group) {
        $report += "`n  类型: $($finding.Type)`n"
        $report += "  行号: $($finding.Line)`n"
        $report += "  内容: $($finding.Context)`n"
        if ($Detailed) {
            $report += "  值: $($finding.Value)`n"
        }
        $report += "`n"
    }
}

# 生成修复建议
$report += "`n============================================================`n"
$report += "修复建议`n"
$report += "============================================================`n"
$report += "`n1. 使用Nacos加密配置工具生成加密值`n"
$report += "   脚本: scripts/security/nacos-encrypt-password.ps1`n"
$report += "`n2. 参考手动修复指南进行修复`n"
$report += "   文档: documentation/security/NACOS_ENCRYPTION_MANUAL_FIX_GUIDE.md`n"
$report += "`n3. 修复后运行验证脚本确认`n"
$report += "   脚本: scripts/security/verify-encrypted-config.ps1`n"
$report += "`n============================================================`n"

# 输出报告
if ($Json) {
    $jsonReport = @{
        scanTime      = (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
        totalFiles    = $configFiles.Count
        totalFindings = $totalCount
        findings      = $findings
    } | ConvertTo-Json -Depth 10

    $jsonOutputFile = $OutputFile -replace '\.txt$', '.json'
    Set-Content -Path $jsonOutputFile -Value $jsonReport -Encoding UTF8
    Write-Host "  ✓ JSON报告已生成: $jsonOutputFile" -ForegroundColor Green
}

Set-Content -Path $OutputFile -Value $report -Encoding UTF8
Write-Host "  ✓ 扫描报告已生成: $OutputFile" -ForegroundColor Green

# 显示摘要
Write-Host "`n[4/4] 扫描完成" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "扫描结果摘要" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "扫描文件数: $($configFiles.Count)" -ForegroundColor White
Write-Host "发现问题数: $totalCount" -ForegroundColor $(if ($totalCount -eq 0) { "Green" } else { "Red" })
Write-Host "涉及文件数: $(($findings | Select-Object -Unique File).Count)" -ForegroundColor White

if ($totalCount -gt 0) {
    Write-Host "`n⚠️  发现 $totalCount 个明文密码安全问题！" -ForegroundColor Red
    Write-Host "请查看报告文件: $OutputFile" -ForegroundColor Yellow
    Write-Host "参考修复指南: documentation/security/NACOS_ENCRYPTION_MANUAL_FIX_GUIDE.md" -ForegroundColor Yellow
    exit 1
}
else {
    Write-Host "`n✓ 未发现明文密码问题" -ForegroundColor Green
    exit 0
}

