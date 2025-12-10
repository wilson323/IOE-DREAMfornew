# =====================================================
# 版本 V1.1.0 处理脚本
# 描述: 初始数据插入
# =====================================================

param(
    [Parameter(Mandatory=$true)]
    [hashtable]$Config
)

function Invoke-VersionV110 {
    param(
        [string]$Environment = "dev"
    )

    Write-Host "执行版本 V1.1.0: 初始数据插入 (环境: $Environment)" -ForegroundColor Cyan

    # 根据环境选择脚本
    $scriptPath = switch ($Environment.ToLower()) {
        "dev" { "deployment\mysql\init\02-ioedream-data-dev.sql" }
        "test" { "deployment\mysql\init\02-ioedream-data-test.sql" }
        "prod" { "deployment\mysql\init\02-ioedream-data-prod.sql" }
        default { "deployment\mysql\init\02-ioedream-data.sql" }
    }

    if (-not (Test-Path $scriptPath)) {
        Write-Host "❌ 脚本文件不存在: $scriptPath" -ForegroundColor Red
        return $false
    }

    # 检查版本是否已执行（环境特定）
    $version = if ($Environment -ne "default") { "V1.1.0-$($Environment.ToUpper())" } else { "V1.1.0" }
    $checkQuery = "SELECT COUNT(*) as count FROM t_migration_history WHERE version = '$version' AND status = 'SUCCESS';"
    $checkResult = & mysql $Config.Args $Config.Database -e $checkQuery 2>&1

    if ($checkResult -match "count\s+1") {
        Write-Host "✅ 版本 $version 已执行，跳过" -ForegroundColor Green
        return $true
    }

    # 执行脚本
    Write-Host "执行脚本: $scriptPath" -ForegroundColor Yellow
    $result = Get-Content $scriptPath -Raw | & mysql $Config.Args $Config.Database 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 版本 $version 执行成功" -ForegroundColor Green
        return $true
    } else {
        Write-Host "❌ 版本 $version 执行失败: $result" -ForegroundColor Red
        return $false
    }
}

Export-ModuleMember -Function Invoke-VersionV110

