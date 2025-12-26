# 删除旧Entity文件脚本
# 用途：删除各业务服务中已迁移到common-entity的旧Entity文件

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "删除旧Entity文件" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseDir = "D:\IOE-DREAM\microservices"

# 需要删除Entity的模块
$modules = @(
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-visitor-service"
    # biometric-service 没有entity目录，跳过
)

$totalDeleted = 0
$totalErrors = 0

Write-Host "⚠️  警告：即将删除以下模块的旧entity目录" -ForegroundColor Yellow
Write-Host ""

foreach ($module in $modules) {
    $entityDir = "$baseDir\$module\src\main\java\net\lab1024\sa\$($module -replace 'ioedream-', '-replace '-service')\entity"

    Write-Host "【$module】" -ForegroundColor Cyan
    Write-Host "  目标目录: $entityDir" -ForegroundColor Gray

    if (Test-Path $entityDir) {
        $files = Get-ChildItem -Path $entityDir -Filter "*.java"
        Write-Host "  📁 发现 $($files.Count) 个Entity文件" -ForegroundColor Yellow

        # 询问用户确认
        $confirm = Read-Host "  确认删除? (Y/N)"
        if ($confirm -eq "Y" -or $confirm -eq "y") {
            try {
                # 删除整个entity目录
                Remove-Item -Path $entityDir -Recurse -Force
                Write-Host "  ✅ 已删除entity目录" -ForegroundColor Green
                $totalDeleted += $files.Count
            } catch {
                Write-Host "  ❌ 删除失败: $($_.Exception.Message)" -ForegroundColor Red
                $totalErrors++
            }
        } else {
            Write-Host "  ⏭️  跳过删除" -ForegroundColor Gray
        }
    } else {
        Write-Host "  ℹ️  entity目录不存在，跳过" -ForegroundColor Gray
    }

    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "删除完成统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📁 已删除文件数: $totalDeleted" -ForegroundColor Green
Write-Host "❌ 错误数: $totalErrors" -ForegroundColor Red
Write-Host ""

# 验证删除结果
Write-Host "🔍 验证删除结果..." -ForegroundColor Cyan
Write-Host ""

$remainingEntities = 0
foreach ($module in $modules) {
    $entityDir = "$baseDir\$module\src\main\java\net\lab1024\sa\$($module -replace 'ioedream-', '-replace '-service')\entity"

    if (Test-Path $entityDir) {
        $files = Get-ChildItem -Path $entityDir -Filter "*.java"
        if ($files.Count -gt 0) {
            Write-Host "  ⚠️  $module`: 还有 $($files.Count) 个Entity文件残留" -ForegroundColor Yellow
            $remainingEntities += $files.Count
        }
    }
}

if ($remainingEntities -eq 0) {
    Write-Host "✅ 所有旧Entity文件已成功删除！" -ForegroundColor Green
} else {
    Write-Host "⚠️  仍有 $remainingEntities 个Entity文件未删除" -ForegroundColor Yellow
}

Write-Host ""
