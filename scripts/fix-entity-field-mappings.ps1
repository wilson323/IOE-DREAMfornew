# Entity字段映射批量修复脚本
# 用途：批量修复Entity字段映射问题（因linter修改导致手动修复困难）

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Entity字段映射批量修复" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$erroFile = "D:\IOE-DREAM\erro.txt"
$baseDir = "D:\IOE-DREAM"

# ========== FirmwareServiceImpl.java 修复 ==========
$firmwareServiceFile = "$baseDir\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\impl\FirmwareServiceImpl.java"

if (Test-Path $firmwareServiceFile) {
    Write-Host "修复 FirmwareServiceImpl.java..." -ForegroundColor Yellow
    $content = Get-Content $firmwareServiceFile -Raw -Encoding UTF8

    # 修复字段映射
    $content = $content -replace '\.setFirmwareFilePath\(', '.setFirmwareFile('
    $content = $content -replace '\.setFirmwareFileName\(', '# REMOVED: setFirmwareFileName (field does not exist) # '
    $content = $content -replace '\.setFirmwareFileSize\(', '.setFileSize('
    $content = $content -replace '\.setFirmwareFileMd5\(', '.setFileMd5('
    $content = $content -replace '\.setUploadTime\(', '# REMOVED: setUploadTime (use setReleaseDate) # '
    $content = $content -replace '\.setUploaderId\(', '.setPublisherId('
    $content = $content -replace '\.setUploaderName\(', '.setPublisherName('
    $content = $content -replace '\.setIsEnabled\(', '.setEnabled('
    $content = $content -replace 'firmware\.getFirmwareFilePath\(', 'firmware.getFirmwareFile('
    $content = $content -replace 'firmware\.getFirmwareFileSize\(', 'firmware.getFileSize('
    $content = $content -replace 'entity\.getFirmwareFileSize\(', 'entity.getFileSize('
    $content = $content -replace 'firmware\.getMinVersion\(', 'firmware.getMinFirmwareVersion('
    $content = $content -replace 'firmware\.getMaxVersion\(', 'firmware.getMaxHardwareVersion('
    $content = $content -replace 'firmware\.getFirmwareFileMd5\(', 'firmware.getFileMd5('
    $content = $content -replace '::getIsEnabled', '::getEnabled'
    $content = $content -replace '::getUploadTime', '::getReleaseDate'
    $content = $content -replace '\.setFirmwareFileName\(file\.getOriginalFilename\(\)\);', ''
    $content = $content -replace '\.setUploadTime\(LocalDateTime\.now\(\)\);', ''

    # 移除包含REMOVED标记的行
    $lines = $content -split "`n"
    $filteredLines = $lines | Where-Object { -not $_.Contains('# REMOVED:') }
    $content = $filteredLines -join "`n"

    Set-Content -Path $firmwareServiceFile -Value $content -Encoding UTF8
    Write-Host "  ✅ FirmwareServiceImpl.java 修复完成" -ForegroundColor Green
}

# ========== FirmwareManager.java 修复 ==========
$firmwareManagerFile = "$baseDir\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\manager\FirmwareManager.java"

if (Test-Path $firmwareManagerFile) {
    Write-Host "修复 FirmwareManager.java..." -ForegroundColor Yellow
    $content = Get-Content $firmwareManagerFile -Raw -Encoding UTF8

    # 修复isEnabled调用
    $content = $content -replace 'if \(!firmware\.getIsEnabled\(\)\.equals\(1\)\)', 'if (!firmware.isEnabled())'

    Set-Content -Path $firmwareManagerFile -Value $content -Encoding UTF8
    Write-Host "  ✅ FirmwareManager.java 修复完成" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
