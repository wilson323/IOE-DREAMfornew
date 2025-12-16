# =====================================================
# 修复测试文件导入语句
# 版本: v1.0.0
# 描述: 自动为测试文件添加缺失的导入语句
# =====================================================

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Green
Write-Host "Fix Test File Imports" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""

# 导入映射表
$importMappings = @{
    "GatewayServiceClient" = "net.lab1024.sa.common.gateway.GatewayServiceClient"
    "AuditLogDao" = "net.lab1024.sa.common.audit.dao.AuditLogDao"
    "AuditArchiveDao" = "net.lab1024.sa.common.audit.dao.AuditArchiveDao"
    "ObjectMapper" = "com.fasterxml.jackson.databind.ObjectMapper"
    "ConfigChangeAuditDao" = "net.lab1024.sa.common.audit.dao.ConfigChangeAuditDao"
    "UserSessionDao" = "net.lab1024.sa.common.auth.dao.UserSessionDao"
    "MenuDao" = "net.lab1024.sa.common.menu.dao.MenuDao"
    "AlertDao" = "net.lab1024.sa.common.monitoring.dao.AlertDao"
    "HealthCheckManager" = "net.lab1024.sa.common.monitor.manager.HealthCheckManager"
    "NotificationConfigService" = "net.lab1024.sa.common.notification.service.NotificationConfigService"
    "NotificationConfigDao" = "net.lab1024.sa.common.notification.dao.NotificationConfigDao"
    "AreaDeviceService" = "net.lab1024.sa.common.organization.service.AreaDeviceService"
    "AreaDeviceServiceImpl" = "net.lab1024.sa.common.organization.service.impl.AreaDeviceServiceImpl"
    "AreaUnifiedServiceImpl" = "net.lab1024.sa.common.organization.service.impl.AreaUnifiedServiceImpl"
    "CacheManager" = "org.springframework.cache.CacheManager"
    "SystemService" = "net.lab1024.sa.common.system.service.SystemService"
    "SystemServiceImpl" = "net.lab1024.sa.common.system.service.impl.SystemServiceImpl"
    "EmployeeService" = "net.lab1024.sa.common.system.employee.service.EmployeeService"
    "CacheServiceImpl" = "net.lab1024.sa.common.cache.CacheServiceImpl"
}

$fixedCount = 0
$errorCount = 0

# 查找所有测试文件
$testFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Test.java" | Where-Object {
    $_.FullName -match "test"
}

foreach ($file in $testFiles) {
    try {
        $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
        $originalContent = $content
        $modified = $false

        # 检查每个可能的导入
        foreach ($className in $importMappings.Keys) {
            $fullImport = $importMappings[$className]

            # 检查文件中是否使用了该类但没有导入
            if ($content -match "\b$className\b" -and $content -notmatch "import\s+$([regex]::Escape($fullImport))") {
                # 找到最后一个import语句的位置
                $lastImportMatch = [regex]::Match($content, "import\s+[^;]+;", [System.Text.RegularExpressions.RegexOptions]::Multiline)
                if ($lastImportMatch.Success) {
                    $insertPosition = $lastImportMatch.Index + $lastImportMatch.Length
                    $newImport = "`nimport $fullImport;"
                    $content = $content.Insert($insertPosition, $newImport)
                    $modified = $true
                    Write-Host "[ADD] Added import for $className in $($file.Name)" -ForegroundColor Yellow
                }
            }
        }

        if ($modified) {
            $utf8NoBom = New-Object System.Text.UTF8Encoding $false
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            Write-Host "[OK] Fixed imports: $($file.Name)" -ForegroundColor Green
            $fixedCount++
        }
    } catch {
        Write-Host "[ERROR] Failed to fix $($file.Name): $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Green
Write-Host "Summary" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host "Total files checked: $($testFiles.Count)" -ForegroundColor Cyan
Write-Host "Fixed: $fixedCount" -ForegroundColor Green
Write-Host "Errors: $errorCount" -ForegroundColor Red
Write-Host ""
