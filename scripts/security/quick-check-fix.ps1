# ============================================================
# IOE-DREAM 快速检查修复进度脚本
# 检查已修复的明文密码数量
# ============================================================

param(
    [switch]$Detailed
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  快速检查修复进度" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 需要检查的文件
$filesToCheck = @(
    "microservices\ioedream-gateway-service\src\main\resources\application.yml",
    "microservices\ioedream-gateway-service\src\main\resources\bootstrap.yml",
    "microservices\ioedream-consume-service\src\main\resources\application.yml",
    "microservices\ioedream-consume-service\src\main\resources\bootstrap.yml",
    "microservices\ioedream-access-service\src\main\resources\application.yml",
    "microservices\ioedream-attendance-service\src\main\resources\bootstrap.yml",
    "microservices\ioedream-video-service\src\main\resources\bootstrap.yml",
    "microservices\ioedream-visitor-service\src\main\resources\bootstrap.yml",
    "microservices\ioedream-device-comm-service\src\main\resources\bootstrap.yml",
    "microservices\ioedream-oa-service\src\main\resources\bootstrap.yml",
    "microservices\ioedream-database-service\src\main\resources\application.yml",
    "microservices\common-config\seata\application-seata.yml"
)

$totalPlaintext = 0
$totalEncrypted = 0
$fileResults = @()

foreach ($file in $filesToCheck) {
    if (-not (Test-Path $file)) {
        continue
    }
    
    $content = Get-Content $file -Raw
    $plaintextCount = ([regex]::Matches($content, 'password:\s*\$\{NACOS_PASSWORD:nacos\}')).Count
    $encryptedCount = ([regex]::Matches($content, 'password:\s*\$\{NACOS_PASSWORD:ENC\([^)]+\)\}')).Count
    
    $totalPlaintext += $plaintextCount
    $totalEncrypted += $encryptedCount
    
    if ($plaintextCount -gt 0 -or $encryptedCount -gt 0) {
        $fileResults += @{
            File = $file
            Plaintext = $plaintextCount
            Encrypted = $encryptedCount
            Status = if ($plaintextCount -eq 0) { "✅ 已修复" } else { "❌ 待修复" }
        }
    }
}

Write-Host "`n修复进度统计:" -ForegroundColor Yellow
Write-Host "  待修复: $totalPlaintext 个" -ForegroundColor $(if ($totalPlaintext -eq 0) { "Green" } else { "Red" })
Write-Host "  已修复: $totalEncrypted 个" -ForegroundColor Green
Write-Host "  完成率: $([math]::Round($totalEncrypted / ($totalPlaintext + $totalEncrypted) * 100, 1))%" -ForegroundColor Cyan

if ($Detailed) {
    Write-Host "`n详细状态:" -ForegroundColor Yellow
    foreach ($result in $fileResults) {
        Write-Host "  $($result.Status) $($result.File)" -ForegroundColor $(if ($result.Plaintext -eq 0) { "Green" } else { "Red" })
        if ($result.Plaintext -gt 0) {
            Write-Host "    待修复: $($result.Plaintext) 个" -ForegroundColor Red
        }
        if ($result.Encrypted -gt 0) {
            Write-Host "    已修复: $($result.Encrypted) 个" -ForegroundColor Green
        }
    }
}

if ($totalPlaintext -eq 0) {
    Write-Host "`n✅ 所有明文密码已修复！" -ForegroundColor Green
    Write-Host "请运行完整验证: .\scripts\security\verify-encrypted-config.ps1" -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "`n⚠️  还有 $totalPlaintext 个明文密码需要修复" -ForegroundColor Red
    Write-Host "参考修复清单: NACOS_PASSWORD_ENCRYPTION_FIX_LIST.md" -ForegroundColor Yellow
    exit 1
}

