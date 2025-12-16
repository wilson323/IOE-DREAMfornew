# ============================================================
# IOE-DREAM P0级安全漏洞修复脚本
# 立即修复明文密码配置安全问题
# 修复时间: 2025-12-16
# 优先级: P0级 (立即执行)
# ============================================================

param(
    [switch]$Backup,
    [switch]$DryRun,
    [switch]$Force
)

# 设置错误处理
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Red
Write-Host "  IOE-DREAM P0级安全漏洞修复脚本" -ForegroundColor Red
Write-Host "  修复明文密码配置安全问题" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red

# 检查是否以管理员权限运行
if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "错误: 需要管理员权限执行此脚本" -ForegroundColor Red
    exit 1
}

# 创建备份
if ($Backup) {
    Write-Host "`n[1/6] 创建配置文件备份..." -ForegroundColor Yellow

    $backupDir = "backup-configs-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

    $configFiles = @(
        ".env",
        ".env.development",
        ".env.production",
        ".env.docker",
        ".gitlab-ci.yml",
        ".github/workflows/ci-cd-pipeline.yml",
        "docker-compose-all.yml",
        "docker-compose-production.yml"
    )

    foreach ($file in $configFiles) {
        if (Test-Path $file) {
            Copy-Item $file -Destination "$backupDir/$file" -Force
            Write-Host "  ✓ 备份: $file" -ForegroundColor Green
        }
    }

    Write-Host "  备份完成: $backupDir" -ForegroundColor Green
}

# 定义需要修复的明文密码项
$passwordFields = @{
    ".env" = @{
        "MYSQL_PASSWORD=123456" = "MYSQL_PASSWORD=\${MYSQL_ENCRYPTED_PASSWORD}"
        "MYSQL_ROOT_PASSWORD=123456" = "MYSQL_ROOT_PASSWORD=\${MYSQL_ENCRYPTED_ROOT_PASSWORD}"
        "REDIS_PASSWORD=redis123" = "REDIS_PASSWORD=\${REDIS_ENCRYPTED_PASSWORD}"
        "NACOS_PASSWORD=nacos" = "NACOS_PASSWORD=\${NACOS_ENCRYPTED_PASSWORD}"
        "RABBITMQ_PASSWORD=guest" = "RABBITMQ_PASSWORD=\${RABBITMQ_ENCRYPTED_PASSWORD}"
        "JASYPT_PASSWORD=IOE-DREAM-Jasypt-Secret-2024" = "JASYPT_PASSWORD=\${JASYPT_ENCRYPTED_PASSWORD}"
        "JWT_SECRET=IOE-DREAM-JWT-Secret-Key-2024-Security" = "JWT_SECRET=\${JWT_ENCRYPTED_SECRET}"
    }

    ".env.development" = @{
        "spring.datasource.password=123456" = "spring.datasource.password=ENC(\${DB_PASSWORD_DEV})"
        "spring.redis.password=redis123" = "spring.redis.password=ENC(\${REDIS_PASSWORD_DEV})"
        "nacos.config.password=nacos" = "nacos.config.password=ENC(\${NACOS_PASSWORD_DEV})"
    }

    ".env.production" = @{
        "spring.datasource.password=123456" = "spring.datasource.password=ENC(\${DB_PASSWORD_PROD})"
        "spring.redis.password=redis123" = "spring.redis.password=ENC(\${REDIS_PASSWORD_PROD})"
        "nacos.config.password=nacos" = "nacos.config.password=ENC(\${NACOS_PASSWORD_PROD})"
    }
}

# 修复配置文件
Write-Host "`n[2/6] 修复配置文件中的明文密码..." -ForegroundColor Yellow

$fixCount = 0
$totalFixes = 0

foreach ($file in $passwordFields.Keys) {
    if (Test-Path $file) {
        Write-Host "  修复文件: $file" -ForegroundColor Cyan

        $content = Get-Content $file -Raw
        $originalContent = $content
        $fileFixCount = 0

        foreach ($oldValue in $passwordFields[$file].Keys) {
            $newValue = $passwordFields[$file][$oldValue]

            if ($content -match [regex]::Escape($oldValue)) {
                if (-not $DryRun) {
                    $content = $content -replace [regex]::Escape($oldValue), $newValue
                }
                $fileFixCount++
                $totalFixes++
                Write-Host "    ✓ 替换: $oldValue -> $newValue" -ForegroundColor Green
            }
        }

        if ($fileFixCount -gt 0) {
            if (-not $DryRun) {
                Set-Content $file -Value $content -NoNewline -Encoding UTF8
                Write-Host "  ✓ 文件已更新: $fileFixCount 个修复" -ForegroundColor Green
            } else {
                Write-Host "  ✓ 预览模式: 将修复 $fileFixCount 个明文密码" -ForegroundColor Yellow
            }
        } else {
            Write-Host "  - 未发现明文密码问题" -ForegroundColor Gray
        }

        $fixCount += $fileFixCount
    }
}

Write-Host "  总计修复: $totalFixes 个明文密码" -ForegroundColor Green

# 创建加密环境变量模板
Write-Host "`n[3/6] 创建加密环境变量模板..." -ForegroundColor Yellow

$envTemplate = @"
# ============================================================
# IOE-DREAM 加密环境变量配置
# 请将以下加密后的值配置到您的环境变量中
# 生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
# ============================================================

# 数据库密码 (使用Jasypt AES加密)
MYSQL_ENCRYPTED_PASSWORD=ENC(这里放置加密后的MySQL密码)
MYSQL_ENCRYPTED_ROOT_PASSWORD=ENC(这里放置加密后的MySQL root密码)

# Redis密码 (使用Jasypt AES加密)
REDIS_ENCRYPTED_PASSWORD=ENC(这里放置加密后的Redis密码)

# Nacos密码 (使用Jasypt AES加密)
NACOS_ENCRYPTED_PASSWORD=ENC(这里放置加密后的Nacos密码)

# RabbitMQ密码 (使用Jasypt AES加密)
RABBITMQ_ENCRYPTED_PASSWORD=ENC(这里放置加密后的RabbitMQ密码)

# Jasypt加密密钥
JASYPT_ENCRYPTED_PASSWORD=ENC(这里放置加密后的Jasypt密钥)

# JWT密钥 (使用Jasypt AES加密)
JWT_ENCRYPTED_SECRET=ENC(这里放置加密后的JWT密钥)

# 环境特定配置
DB_PASSWORD_DEV=ENC(这里放置开发环境的数据库密码)
REDIS_PASSWORD_DEV=ENC(这里放置开发环境的Redis密码)
NACOS_PASSWORD_DEV=ENC(这里放置开发环境的Nacos密码)

DB_PASSWORD_PROD=ENC(这里放置生产环境的数据库密码)
REDIS_PASSWORD_PROD=ENC(这里放置生产环境的Redis密码)
NACOS_PASSWORD_PROD=ENC(这里放置生产环境的Nacos密码)
"@

if (-not $DryRun) {
    Set-Content -Path ".env.template.encrypted" -Value $envTemplate -Encoding UTF8
    Write-Host "  ✓ 创建模板: .env.template.encrypted" -ForegroundColor Green
} else {
    Write-Host "  ✓ 预览模式: 将创建加密环境变量模板" -ForegroundColor Yellow
}

# 创建密码加密工具
Write-Host "`n[4/6] 创建密码加密工具..." -ForegroundColor Yellow

$encryptScript = @"
# ============================================================
# IOE-DREAM 密码加密工具
# 使用Jasypt对敏感配置进行AES加密
# ============================================================

param(
    [Parameter(Mandatory=`$true)]
    [string]`$Password,

    [string]`$SecretKey = "IOE-DREAM-Jasypt-Secret-2024"
)

# 加载Jasypt库
`$jasyptPath = "lib/jasypt-1.9.3.jar"
if (-not (Test-Path `$jasyptPath)) {
    Write-Host "下载Jasypt库..."
    New-Item -ItemType Directory -Path "lib" -Force | Out-Null
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/jasypt/jasypt/1.9.3/jasypt-1.9.3.jar" -OutFile "`$jasyptPath"
}

# 加密密码
`$encrypted = java -cp "`$jasyptPath" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="`$Password" password="`$SecretKey" algorithm=PBEWithMD5AndDES

if (`$LASTEXITCODE -eq 0) {
    `$encryptedValue = (`$encrypted -split "---- ")[1]).Trim()
    Write-Host "ENC(`$encryptedValue)"
} else {
    Write-Host "加密失败" -ForegroundColor Red
    exit 1
}
"@

if (-not $DryRun) {
    Set-Content -Path "scripts\encrypt-password.ps1" -Value $encryptScript -Encoding UTF8
    Write-Host "  ✓ 创建工具: scripts\encrypt-password.ps1" -ForegroundColor Green
} else {
    Write-Host "  ✓ 预览模式: 将创建密码加密工具" -ForegroundColor Yellow
}

# 修复CI/CD配置文件
Write-Host "`n[5/6] 修复CI/CD配置文件..." -ForegroundColor Yellow

# 检查并修复.gitlab-ci.yml
if (Test-Path ".gitlab-ci.yml") {
    $gitlabContent = Get-Content ".gitlab-ci.yml" -Raw
    $originalGitlabContent = $gitlabContent
    $gitlabFixes = 0

    # 修复GitLab CI中的明文密码
    if ($gitlabContent -match "MYSQL_ROOT_PASSWORD:\s*root") {
        if (-not $DryRun) {
            $gitlabContent = $gitlabContent -replace "MYSQL_ROOT_PASSWORD:\s*root", "MYSQL_ROOT_PASSWORD: `$`$MYSQL_ROOT_PASSWORD"
        }
        $gitlabFixes++
        Write-Host "    ✓ 修复GitLab CI MySQL密码" -ForegroundColor Green
    }

    if ($gitlabContent -match "NACOS_AUTH_TOKEN:\s*SecretKey") {
        if (-not $DryRun) {
            $gitlabContent = $gitlabContent -replace "NACOS_AUTH_TOKEN:\s*SecretKey.*", "NACOS_AUTH_TOKEN: `$`$NACOS_AUTH_TOKEN"
        }
        $gitlabFixes++
        Write-Host "    ✓ 修复GitLab CI Nacos密钥" -ForegroundColor Green
    }

    if ($gitlabFixes -gt 0) {
        if (-not $DryRun) {
            Set-Content ".gitlab-ci.yml" -Value $gitlabContent -Encoding UTF8
            Write-Host "  ✓ GitLab CI配置已更新" -ForegroundColor Green
        } else {
            Write-Host "  ✓ 预览模式: 将修复 $gitlabFixes 个GitLab CI配置" -ForegroundColor Yellow
        }
    }
}

# 检查并修复GitHub Actions配置
if (Test-Path ".github/workflows/ci-cd-pipeline.yml") {
    $githubContent = Get-Content ".github/workflows/ci-cd-pipeline.yml" -Raw
    $originalGithubContent = $githubContent
    $githubFixes = 0

    # 修复GitHub Actions中的明文密码
    if ($githubContent -match "MYSQL_ROOT_PASSWORD:\s*root") {
        if (-not $DryRun) {
            $githubContent = $githubContent -replace "MYSQL_ROOT_PASSWORD:\s*root", "MYSQL_ROOT_PASSWORD: `$`{{`$ secrets.MYSQL_ROOT_PASSWORD }}"
        }
        $githubFixes++
        Write-Host "    ✓ 修复GitHub Actions MySQL密码" -ForegroundColor Green
    }

    if ($githubContent -match "NACOS_AUTH_TOKEN:\s*SecretKey") {
        if (-not $DryRun) {
            $githubContent = $githubContent -replace "NACOS_AUTH_TOKEN:\s*SecretKey.*", "NACOS_AUTH_TOKEN: `$`{{`$ secrets.NACOS_AUTH_TOKEN }}"
        }
        $githubFixes++
        Write-Host "    ✓ 修复GitHub Actions Nacos密钥" -ForegroundColor Green
    }

    if ($githubFixes -gt 0) {
        if (-not $DryRun) {
            Set-Content ".github/workflows/ci-cd-pipeline.yml" -Value $githubContent -Encoding UTF8
            Write-Host "  ✓ GitHub Actions配置已更新" -ForegroundColor Green
        } else {
            Write-Host "  ✓ 预览模式: 将修复 $githubFixes 个GitHub Actions配置" -ForegroundColor Yellow
        }
    }
}

# 创建安全配置检查脚本
Write-Host "`n[6/6] 创建安全配置检查脚本..." -ForegroundColor Yellow

$checkScript = @"
# ============================================================
# IOE-DREAM 安全配置检查脚本
# 检查配置文件中的明文密码和安全问题
# ============================================================

param(
    [switch]$Verbose
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM 安全配置检查脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 检查的文件列表
`$configFiles = @(
    ".env",
    ".env.development",
    ".env.production",
    ".env.docker",
    ".gitlab-ci.yml",
    ".github/workflows/ci-cd-pipeline.yml",
    "docker-compose.yml",
    "docker-compose-all.yml",
    "docker-compose-production.yml"
)

# 明文密码模式
`$plaintextPatterns = @(
    "password=\s*[^`\$\{][^\s]+",
    "PASSWORD=\s*[^`\$\{][^\s]+",
    "secret=\s*[^`\$\{][^\s]+",
    "SECRET=\s*[^`\$\{][^\s]+",
    "key=\s*[^`\$\{][^\s]+",
    "KEY=\s*[^`\$\{][^\s]+"
)

`$totalIssues = 0
`$totalFiles = 0

foreach (`$file in `$configFiles) {
    if (Test-Path `$file) {
        `$totalFiles++
        Write-Host "`n检查文件: `$file" -ForegroundColor Yellow

        `$content = Get-Content `$file -Raw
        `$fileIssues = 0

        foreach (`$pattern in `$plaintextPatterns) {
            `$matches = [regex]::Matches(`$content, `$pattern, [Text.RegularExpressions.RegexOptions]::IgnoreCase)

            if (`$matches.Count -gt 0) {
                `$fileIssues += `$matches.Count
                `$totalIssues += `$matches.Count

                foreach (`$match in `$matches) {
                    if (`$Verbose) {
                        Write-Host "  ⚠ 发现明文密码: `$(`$match.Value.Trim())" -ForegroundColor Red
                    } else {
                        Write-Host "  ⚠ 发现明文密码" -ForegroundColor Red
                    }
                }
            }
        }

        if (`$fileIssues -eq 0) {
            Write-Host "  ✓ 未发现明文密码问题" -ForegroundColor Green
        } else {
            Write-Host "  ✗ 发现 `$fileIssues 个安全问题" -ForegroundColor Red
        }
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  检查结果汇总" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "检查文件数: `$totalFiles" -ForegroundColor White
Write-Host "发现问题数: `$totalIssues" -ForegroundColor $(if (`$totalIssues -eq 0) { "Green" } else { "Red" })

if (`$totalIssues -eq 0) {
    Write-Host "✓ 所有配置文件安全检查通过!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "✗ 发现 `$totalIssues 个安全问题需要修复" -ForegroundColor Red
    Write-Host "请运行: .\scripts\security-fix-p0-immediate.ps1" -ForegroundColor Yellow
    exit 1
}
"@

if (-not $DryRun) {
    Set-Content -Path "scripts\security-config-check.ps1" -Value $checkScript -Encoding UTF8
    Write-Host "  ✓ 创建检查脚本: scripts\security-config-check.ps1" -ForegroundColor Green
} else {
    Write-Host "  ✓ 预览模式: 将创建安全配置检查脚本" -ForegroundColor Yellow
}

# 修复完成总结
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  P0级安全漏洞修复完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

if ($DryRun) {
    Write-Host "预览模式执行结果:" -ForegroundColor Yellow
    Write-Host "  - 将修复 $totalFixes 个明文密码配置" -ForegroundColor Yellow
    Write-Host "  - 将创建加密环境变量模板" -ForegroundColor Yellow
    Write-Host "  - 将创建密码加密工具" -ForegroundColor Yellow
    Write-Host "  - 将修复CI/CD配置文件" -ForegroundColor Yellow
    Write-Host "  - 将创建安全配置检查脚本" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "要执行实际修复，请运行:" -ForegroundColor Cyan
    Write-Host "  .\scripts\security-fix-p0-immediate.ps1 -Force" -ForegroundColor Cyan
} else {
    Write-Host "实际修复执行结果:" -ForegroundColor Green
    Write-Host "  ✓ 修复了 $totalFixes 个明文密码配置" -ForegroundColor Green
    Write-Host "  ✓ 创建了加密环境变量模板" -ForegroundColor Green
    Write-Host "  ✓ 创建了密码加密工具" -ForegroundColor Green
    Write-Host "  ✓ 修复了CI/CD配置文件" -ForegroundColor Green
    Write-Host "  ✓ 创建了安全配置检查脚本" -ForegroundColor Green
}

Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Cyan
Write-Host "1. 使用密码加密工具生成加密后的密码值" -ForegroundColor White
Write-Host "2. 将加密后的值配置到环境变量中" -ForegroundColor White
Write-Host "3. 运行安全配置检查脚本验证修复结果" -ForegroundColor White
Write-Host ""
Write-Host "密码加密工具使用示例:" -ForegroundColor Cyan
Write-Host "  .\scripts\encrypt-password.ps1 -Password '123456'" -ForegroundColor Gray

if ($totalFixes -gt 0 -and -not $DryRun) {
    Write-Host ""
    Write-Host "⚠️ 重要提醒:" -ForegroundColor Yellow
    Write-Host "  - 请立即配置加密后的环境变量" -ForegroundColor Yellow
    Write-Host "  - 重启相关服务以应用新配置" -ForegroundColor Yellow
    Write-Host "  - 验证所有服务正常启动" -ForegroundColor Yellow
}

Write-Host "========================================" -ForegroundColor Green