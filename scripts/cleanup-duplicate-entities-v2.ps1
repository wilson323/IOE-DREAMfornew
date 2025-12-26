# Entity重复目录清理脚本（修正版）
# 策略：删除domain/entity或entity其中一个重复目录，保留Entity定义

Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "  Entity重复目录清理"  -ForegroundColor Cyan
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host ""

$services = @(
    @{Name="ioedream-access-service"; Keep="entity"}
    @{Name="ioedream-attendance-service"; Keep="entity"}
    @{Name="ioedream-consume-service"; Keep="entity"}
    @{Name="ioedream-video-service"; Keep="entity"}
    @{Name="ioedream-visitor-service"; Keep="entity"}
)

Write-Host "[执行策略]"  -ForegroundColor Yellow
Write-Host "  保留: /entity/ 目录"  -ForegroundColor Green
Write-Host "  删除: /domain/entity/ 目录（重复）"  -ForegroundColor Red
Write-Host ""

$confirmation = Read-Host "是否继续执行? (输入 YES 确认)"
if ($confirmation -ne "YES") {
    Write-Host "操作已取消"  -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "[开始清理]"  -ForegroundColor Yellow

foreach ($service in $services) {
    $servicePath = "D:\IOE-DREAM\microservices\$($service.Name)"
    $domainEntityPath = "$servicePath\src\main\java\net\lab1024\sa\$($service.Name -replace 'ioedream-', '')\domain\entity"

    if (Test-Path $domainEntityPath) {
        Write-Host "处理: $($service.Name)"  -ForegroundColor Cyan

        try {
            # 统计文件数量
            $fileCount = (Get-ChildItem -Path $domainEntityPath -Filter "*.java").Count
            Write-Host "  删除domain/entity/: $fileCount 个文件"  -ForegroundColor Yellow

            # 删除目录
            Remove-Item $domainEntityPath -Force -Recurse
            Write-Host "  ✓ 已删除: domain/entity/"  -ForegroundColor Green
        }
        catch {
            Write-Host "  ✗ 删除失败: $($_.Exception.Message)"  -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "[清理空domain目录]"  -ForegroundColor Yellow

foreach ($service in $services) {
    $servicePath = "D:\IOE-DREAM\microservices\$($service.Name)"
    $domainPath = "$servicePath\src\main\java\net\lab1024\sa\$($service.Name -replace 'ioedream-', '')\domain"

    if (Test-Path $domainPath) {
        try {
            # 检查目录是否为空
            $itemCount = (Get-ChildItem -Path $domainPath -Recurse).Count
            if ($itemCount -eq 0) {
                Remove-Item $domainPath -Force -Recurse
                Write-Host "  ✓ 已删除空domain目录: $($service.Name)"  -ForegroundColor Green
            } else {
                Write-Host "  ! domain目录不为空，保留: $($service.Name)"  -ForegroundColor Yellow
            }
        }
        catch {
            Write-Host "  ! 检查目录失败: $($_.Exception.Message)"  -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "  清理完成"  -ForegroundColor Cyan
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "  Entity位置: /entity/ 目录（统一）"  -ForegroundColor Green
Write-Host "  下一步: 验证编译"  -ForegroundColor Yellow
Write-Host "========================================"  -ForegroundColor Cyan
