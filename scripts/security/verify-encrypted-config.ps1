# ============================================================
# IOE-DREAM 加密配置验证脚本
# 验证所有配置文件是否已正确加密
# 重要：本脚本仅验证，不修改任何文件
# ============================================================

param(
    [switch]$Detailed,
    [string]$OutputFile = "encryption-verification-report.txt"
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM 加密配置验证脚本" -ForegroundColor Cyan
Write-Host "  验证配置文件加密状态" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

# 定义扫描的文件类型
$configFilePatterns = @(
    "**/application*.yml",
    "**/application*.yaml",
    "**/bootstrap*.yml",
    "**/bootstrap*.yaml"
)

# 排除的目录
$excludeDirs = @(
    "target",
    "node_modules",
    ".git",
    ".idea",
    "logs",
    "backup-*"
)

# 收集所有配置文件
Write-Host "`n[1/3] 扫描配置文件..." -ForegroundColor Yellow
$configFiles = @()

foreach ($pattern in $configFilePatterns) {
    $files = Get-ChildItem -Path "microservices" -Filter $pattern -Recurse -ErrorAction SilentlyContinue |
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

$configFiles = $configFiles | Select-Object -Unique FullName
Write-Host "  找到 $($configFiles.Count) 个配置文件" -ForegroundColor Green

# 验证加密状态
Write-Host "`n[2/3] 验证加密状态..." -ForegroundColor Yellow

$verificationResults = @{
    TotalFiles               = $configFiles.Count
    EncryptedCount           = 0
    PlaintextCount           = 0
    EnvironmentVariableCount = 0
    Files                    = @()
}

foreach ($file in $configFiles) {
    try {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        if (-not $content) { continue }

        $fileResult = @{
            File                 = $file.FullName.Replace((Get-Location).Path + "\", "")
            Encrypted            = @()
            Plaintext            = @()
            EnvironmentVariables = @()
        }

        # 检查加密格式
        $encryptedMatches = [regex]::Matches($content, '(?i)(?:password|secret|key)\s*[:=]\s*ENC\([^)]+\)', [System.Text.RegularExpressions.RegexOptions]::Multiline)
        $fileResult.Encrypted = $encryptedMatches | ForEach-Object { $_.Value }
        $verificationResults.EncryptedCount += $encryptedMatches.Count

        # 检查环境变量格式
        $envMatches = [regex]::Matches($content, '(?i)(?:password|secret|key)\s*[:=]\s*\$\{[^}]+\}', [System.Text.RegularExpressions.RegexOptions]::Multiline)
        $fileResult.EnvironmentVariables = $envMatches | ForEach-Object { $_.Value }
        $verificationResults.EnvironmentVariableCount += $envMatches.Count

        # 检查明文密码（排除注释和示例）
        $plaintextPattern = '(?i)(?:password|secret|key)\s*[:=]\s*["'']?([^"''\s${}ENC()]{3,})["'']?'
        $plaintextMatches = [regex]::Matches($content, $plaintextPattern)

        foreach ($match in $plaintextMatches) {
            $value = $match.Groups[1].Value.Trim()
            # 排除注释、示例和空值
            if ($value -notmatch '^(?:null|empty|""|''''|ENC\(|\$\{|#|//|示例|example)' -and
                $value.Length -gt 2) {
                $fileResult.Plaintext += $match.Value
                $verificationResults.PlaintextCount++
            }
        }

        if ($fileResult.Encrypted.Count -gt 0 -or
            $fileResult.Plaintext.Count -gt 0 -or
            $fileResult.EnvironmentVariables.Count -gt 0) {
            $verificationResults.Files += $fileResult
        }
    }
    catch {
        Write-Host "  ✗ 读取文件失败: $($file.FullName)" -ForegroundColor Red
    }
}

# 生成验证报告
Write-Host "`n[3/3] 生成验证报告..." -ForegroundColor Yellow

$report = @"
============================================================
IOE-DREAM 加密配置验证报告
生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
============================================================

验证统计:
- 扫描文件数: $($verificationResults.TotalFiles)
- 已加密配置: $($verificationResults.EncryptedCount)
- 使用环境变量: $($verificationResults.EnvironmentVariableCount)
- 明文密码: $($verificationResults.PlaintextCount)

加密覆盖率: $([math]::Round(($verificationResults.EncryptedCount + $verificationResults.EnvironmentVariableCount) / ([math]::Max(1, $verificationResults.EncryptedCount + $verificationResults.EnvironmentVariableCount + $verificationResults.PlaintextCount)) * 100, 2))%

============================================================
详细验证结果
============================================================

"@

foreach ($fileResult in $verificationResults.Files) {
    $report += "`n文件: $($fileResult.File)`n"
    $report += "-" * 80 + "`n"

    if ($fileResult.Encrypted.Count -gt 0) {
        $report += "✓ 已加密配置 ($($fileResult.Encrypted.Count) 个):`n"
        foreach ($enc in $fileResult.Encrypted) {
            $report += "  - $enc`n"
        }
    }

    if ($fileResult.EnvironmentVariables.Count -gt 0) {
        $report += "✓ 使用环境变量 ($($fileResult.EnvironmentVariables.Count) 个):`n"
        foreach ($env in $fileResult.EnvironmentVariables) {
            $report += "  - $env`n"
        }
    }

    if ($fileResult.Plaintext.Count -gt 0) {
        $report += "✗ 明文密码 ($($fileResult.Plaintext.Count) 个):`n"
        foreach ($plain in $fileResult.Plaintext) {
            $report += "  - $plain`n"
        }
    }

    $report += "`n"
}

# 生成建议
$report += "`n============================================================`n"
$report += "验证结果和建议`n"
$report += "============================================================`n"

if ($verificationResults.PlaintextCount -eq 0) {
    $report += "`n✓ 恭喜！所有密码配置已正确加密。`n"
    $report += "加密覆盖率: 100%`n"
}
else {
    $report += "`n⚠️  发现 $($verificationResults.PlaintextCount) 个明文密码需要修复。`n"
    $report += "请参考修复指南: documentation/security/NACOS_ENCRYPTION_MANUAL_FIX_GUIDE.md`n"
}

$report += "`n============================================================`n"

Set-Content -Path $OutputFile -Value $report -Encoding UTF8
Write-Host "  ✓ 验证报告已生成: $OutputFile" -ForegroundColor Green

# 显示摘要
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "验证结果摘要" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "扫描文件数: $($verificationResults.TotalFiles)" -ForegroundColor White
Write-Host "已加密配置: $($verificationResults.EncryptedCount)" -ForegroundColor Green
Write-Host "环境变量配置: $($verificationResults.EnvironmentVariableCount)" -ForegroundColor Green
Write-Host "明文密码: $($verificationResults.PlaintextCount)" -ForegroundColor $(if ($verificationResults.PlaintextCount -eq 0) { "Green" } else { "Red" })

$coverage = [math]::Round(($verificationResults.EncryptedCount + $verificationResults.EnvironmentVariableCount) / ([math]::Max(1, $verificationResults.EncryptedCount + $verificationResults.EnvironmentVariableCount + $verificationResults.PlaintextCount)) * 100, 2)
Write-Host "加密覆盖率: $coverage%" -ForegroundColor $(if ($coverage -eq 100) { "Green" } else { "Yellow" })

if ($verificationResults.PlaintextCount -eq 0) {
    Write-Host "`n✓ 所有配置已正确加密！" -ForegroundColor Green
    exit 0
}
else {
    Write-Host "`n⚠️  需要修复 $($verificationResults.PlaintextCount) 个明文密码" -ForegroundColor Red
    Write-Host "请查看报告: $OutputFile" -ForegroundColor Yellow
    exit 1
}

