# =====================================================
# 版本 V2.0.0 处理脚本
# 描述: 消费记录表增强
# =====================================================

param(
    [Parameter(Mandatory=$true)]
    [hashtable]$Config
)

function Invoke-VersionV200 {
    Write-Host "执行版本 V2.0.0: 消费记录表增强" -ForegroundColor Cyan

    $scriptPath = "deployment\mysql\init\04-v2.0.0__enhance-consume-record.sql"

    if (-not (Test-Path $scriptPath)) {
        Write-Host "❌ 脚本文件不存在: $scriptPath" -ForegroundColor Red
        return $false
    }

    # 检查版本是否已执行
    $checkQuery = "SELECT COUNT(*) as count FROM t_migration_history WHERE version = 'V2.0.0' AND status = 'SUCCESS';"
    $checkResult = & mysql $Config.Args $Config.Database -e $checkQuery 2>&1

    if ($checkResult -match "count\s+1") {
        Write-Host "✅ 版本 V2.0.0 已执行，跳过" -ForegroundColor Green
        return $true
    }

    # 执行脚本
    Write-Host "执行脚本: $scriptPath" -ForegroundColor Yellow
    $result = Get-Content $scriptPath -Raw | & mysql $Config.Args $Config.Database 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 版本 V2.0.0 执行成功" -ForegroundColor Green
        return $true
    } else {
        Write-Host "❌ 版本 V2.0.0 执行失败: $result" -ForegroundColor Red
        return $false
    }
}

Export-ModuleMember -Function Invoke-VersionV200

