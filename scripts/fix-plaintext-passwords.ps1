# IOE-DREAM 明文密码修复脚本
param(
    [switch]$DryRun,
    [switch]$Backup
)

Write-Host "========================================" -ForegroundColor Red
Write-Host "  IOE-DREAM 明文密码修复脚本" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red

# 创建备份
if ($Backup) {
    Write-Host "创建备份..." -ForegroundColor Yellow
    $backupDir = "backup-configs-$(Get-Date -Format yyyyMMdd-HHmmss)"
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
    
    if (Test-Path ".env") { Copy-Item ".env" "$backupDir/.env" -Force }
    Write-Host "备份完成: $backupDir" -ForegroundColor Green
}

# 修复.env文件
Write-Host "`n修复 .env 文件..." -ForegroundColor Yellow
if (Test-Path ".env") {
    $content = Get-Content ".env" -Raw
    $fixCount = 0
    
    # 修复明文密码
    if ($content -match "MYSQL_PASSWORD=123456") {
        if (-not $DryRun) {
            $content = $content -replace "MYSQL_PASSWORD=123456", "MYSQL_PASSWORD=`$`{MYSQL_ENCRYPTED_PASSWORD}"
        }
        $fixCount++
        Write-Host "  ✓ 修复: MYSQL_PASSWORD=123456" -ForegroundColor Green
    }
    
    if ($content -match "MYSQL_ROOT_PASSWORD=123456") {
        if (-not $DryRun) {
            $content = $content -replace "MYSQL_ROOT_PASSWORD=123456", "MYSQL_ROOT_PASSWORD=`$`{MYSQL_ENCRYPTED_ROOT_PASSWORD}"
        }
        $fixCount++
        Write-Host "  ✓ 修复: MYSQL_ROOT_PASSWORD=123456" -ForegroundColor Green
    }
    
    if ($content -match "REDIS_PASSWORD=redis123") {
        if (-not $DryRun) {
            $content = $content -replace "REDIS_PASSWORD=redis123", "REDIS_PASSWORD=`$`{REDIS_ENCRYPTED_PASSWORD}"
        }
        $fixCount++
        Write-Host "  ✓ 修复: REDIS_PASSWORD=redis123" -ForegroundColor Green
    }
    
    if ($fixCount -gt 0 -and -not $DryRun) {
        Set-Content ".env" -Value $content -NoNewline -Encoding UTF8
        Write-Host "  ✓ .env 已更新，修复 $fixCount 个问题" -ForegroundColor Green
    } elseif ($DryRun) {
        Write-Host "  ✓ 预览模式: 将修复 $fixCount 个问题" -ForegroundColor Yellow
    }
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  明文密码修复完成\!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

