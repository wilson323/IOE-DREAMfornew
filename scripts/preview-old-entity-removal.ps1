# 预览要删除的旧Entity文件
# 用途：显示将要删除的Entity文件，供用户确认

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "预览：要删除的旧Entity文件" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseDir = "D:\IOE-DREAM\microservices"

$modules = @(
    @{Service="ioedream-access-service"; Module="access"},
    @{Service="ioedream-attendance-service"; Module="attendance"},
    @{Service="ioedream-visitor-service"; Module="visitor"}
)

$totalFiles = 0

foreach ($info in $modules) {
    $serviceName = $info.Service
    $moduleName = $info.Module
    $entityDir = "$baseDir\$serviceName\src\main\java\net\lab1024\sa\$moduleName\entity"

    Write-Host "【$serviceName】" -ForegroundColor Yellow

    if (Test-Path $entityDir) {
        $files = Get-ChildItem -Path $entityDir -Filter "*.java"

        if ($files.Count -gt 0) {
            Write-Host "  📁 目录: $entityDir" -ForegroundColor Cyan
            Write-Host "  📊 文件数: $($files.Count)" -ForegroundColor Cyan
            Write-Host "  📄 文件列表:" -ForegroundColor Cyan

            # 显示前10个文件
            $files | Select-Object -First 10 | ForEach-Object {
                Write-Host "    - $($_.Name)" -ForegroundColor Gray
            }

            if ($files.Count -gt 10) {
                Write-Host "    ... 还有 $($files.Count - 10) 个文件" -ForegroundColor Gray
            }

            $totalFiles += $files.Count
        } else {
            Write-Host "  ℹ️  目录为空" -ForegroundColor Gray
        }
    } else {
        Write-Host "  ℹ️  目录不存在" -ForegroundColor Gray
    }

    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📁 总计将删除: $totalFiles 个Entity文件" -ForegroundColor Yellow
Write-Host ""

Write-Host "⚠️  注意：这些文件已迁移到 microservices-common-entity 模块" -ForegroundColor Yellow
Write-Host "✅ 运行 remove-old-entity-files.ps1 以执行删除" -ForegroundColor Green
Write-Host ""
