# 安全删除旧Entity文件脚本（带备份和验证）
# 用途：备份旧Entity文件，然后删除，最后验证

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "安全删除旧Entity文件（带备份和验证）" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseDir = "D:\IOE-DREAM"
$backupDir = "$baseDir\backup\old-entities-backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"

# 创建备份目录
Write-Host "📦 创建备份目录..." -ForegroundColor Cyan
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
Write-Host "  ✅ 备份目录: $backupDir" -ForegroundColor Green
Write-Host ""

$modules = @(
    @{Service="ioedream-access-service"; Module="access"},
    @{Service="ioedream-attendance-service"; Module="attendance"},
    @{Service="ioedream-visitor-service"; Module="visitor"}
)

$totalBackedUp = 0
$totalDeleted = 0

# ========== 第1步：备份 ==========
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "第1步：备份旧Entity文件" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

foreach ($info in $modules) {
    $serviceName = $info.Service
    $moduleName = $info.Module
    $entityDir = "$baseDir\microservices\$serviceName\src\main\java\net\lab1024\sa\$moduleName\entity"

    Write-Host "【$serviceName】备份中..." -ForegroundColor Yellow

    if (Test-Path $entityDir) {
        $moduleBackupDir = "$backupDir\$serviceName\entity"
        New-Item -ItemType Directory -Path $moduleBackupDir -Force | Out-Null

        # 复制文件到备份目录
        Copy-Item -Path $entityDir -Destination $moduleBackupDir -Recurse -Force

        $files = Get-ChildItem -Path $entityDir -Filter "*.java"
        Write-Host "  ✅ 已备份 $($files.Count) 个文件到: $moduleBackupDir" -ForegroundColor Green
        $totalBackedUp += $files.Count
    } else {
        Write-Host "  ℹ️  目录不存在，跳过" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "✅ 备份完成！总计备份: $totalBackedUp 个文件" -ForegroundColor Green
Write-Host ""

# ========== 第2步：删除 ==========
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "第2步：删除旧Entity文件" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

foreach ($info in $modules) {
    $serviceName = $info.Service
    $moduleName = $info.Module
    $entityDir = "$baseDir\microservices\$serviceName\src\main\java\net\lab1024\sa\$moduleName\entity"

    Write-Host "【$serviceName】删除中..." -ForegroundColor Yellow

    if (Test-Path $entityDir) {
        $files = Get-ChildItem -Path $entityDir -Filter "*.java"

        try {
            # 删除整个entity目录
            Remove-Item -Path $entityDir -Recurse -Force
            Write-Host "  ✅ 已删除 $($files.Count) 个文件" -ForegroundColor Green
            $totalDeleted += $files.Count
        } catch {
            Write-Host "  ❌ 删除失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "  ℹ️  目录不存在，跳过" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "✅ 删除完成！总计删除: $totalDeleted 个文件" -ForegroundColor Green
Write-Host ""

# ========== 第3步：验证 ==========
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "第3步：验证删除结果" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$validationPassed = $true

foreach ($info in $modules) {
    $serviceName = $info.Service
    $moduleName = $info.Module
    $entityDir = "$baseDir\microservices\$serviceName\src\main\java\net\lab1024\sa\$moduleName\entity"
    $commonEntityDir = "$baseDir\microservices\microservices-common-entity\src\main\java\net\lab1024\sa\common\entity\$moduleName"

    Write-Host "【$serviceName】验证中..." -ForegroundColor Yellow

    # 检查旧entity目录是否已删除
    if (Test-Path $entityDir) {
        $remainingFiles = Get-ChildItem -Path $entityDir -Filter "*.java"
        if ($remainingFiles.Count -gt 0) {
            Write-Host "  ❌ 旧entity目录仍有 $($remainingFiles.Count) 个文件" -ForegroundColor Red
            $validationPassed = $false
        } else {
            Write-Host "  ⚠️  旧entity目录为空但存在" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ✅ 旧entity目录已删除" -ForegroundColor Green
    }

    # 检查新Entity文件是否存在
    if (Test-Path $commonEntityDir) {
        $newFiles = Get-ChildItem -Path $commonEntityDir -Filter "*.java"
        Write-Host "  ✅ common-entity中有 $($newFiles.Count) 个Entity文件" -ForegroundColor Green
    } else {
        Write-Host "  ❌ common-entity中找不到Entity目录" -ForegroundColor Red
        $validationPassed = $false
    }

    Write-Host ""
}

# ========== 最终总结 ==========
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "操作总结" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📦 备份文件数: $totalBackedUp" -ForegroundColor Green
Write-Host "🗑️  删除文件数: $totalDeleted" -ForegroundColor Green
Write-Host "📁 备份位置: $backupDir" -ForegroundColor Cyan
Write-Host ""

if ($validationPassed) {
    Write-Host "🎉 所有操作成功！验证通过！" -ForegroundColor Green
    Write-Host ""
    Write-Host "📝 下一步：运行编译验证" -ForegroundColor Yellow
    Write-Host "   mvn clean install -DskipTests" -ForegroundColor Gray
    exit 0
} else {
    Write-Host "⚠️  验证未完全通过，请检查警告信息" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "📝 如需恢复备份，运行：" -ForegroundColor Yellow
    Write-Host "   Copy-Item -Path '$backupDir\*' -Destination 'D:\IOE-DREAM\microservices\' -Recurse -Force" -ForegroundColor Gray
    exit 1
}
