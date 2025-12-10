# =====================================================
# IOE-DREAM 数据库脚本清理脚本
# 功能: 清理所有冗余的数据库脚本文件
# 安全: 只删除archive目录下的文件，保留ioedream-db-init
# 作者: IOE-DREAM架构团队
# 创建时间: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$Action = "dry-run",

    [Parameter(Mandatory=$false)]
    [switch]$Force,

    [Parameter(Mandatory=$false)]
    [switch]$Backup
)

# 配置
$RootPath = "D:\IOE-DREAM"
$ArchivePath = "$RootPath\archive"
$BackupPath = "$RootPath\database-scripts-backup"
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

Write-Host "🔧 IOE-DREAM 数据库脚本清理工具" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

# 检查路径是否存在
if (-not (Test-Path $RootPath)) {
    Write-Host "❌ 错误: 根路径不存在: $RootPath" -ForegroundColor Red
    exit 1
}

# 创建备份目录
if ($Backup) {
    $BackupDir = "$BackupPath\$Timestamp"
    Write-Host "📁 创建备份目录: $BackupDir" -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null

    # 备份archive目录
    if (Test-Path $ArchivePath) {
        Write-Host "📦 备份archive目录..." -ForegroundColor Yellow
        Copy-Item -Path $ArchivePath -Destination "$BackupDir\archive" -Recurse -Force
    }
}

# 统计当前文件数量
$TotalSQLFiles = 0
$ArchiveSQLFiles = 0
$IOEDreamSQLFiles = 0
$OtherSQLFiles = 0

Write-Host "📊 扫描SQL文件..." -ForegroundColor Yellow

# 扫描所有SQL文件
Get-ChildItem -Path $RootPath -Filter "*.sql" -Recurse | ForEach-Object {
    $TotalSQLFiles++

    if ($_.FullName -like "*archive*") {
        $ArchiveSQLFiles++
    } elseif ($_.FullName -like "*ioedream-db-init*") {
        $IOEDreamSQLFiles++
    } else {
        $OtherSQLFiles++
    }
}

Write-Host "📈 SQL文件统计:" -ForegroundColor Cyan
Write-Host "  总文件数: $TotalSQLFiles" -ForegroundColor White
Write-Host "  archive目录: $ArchiveSQLFiles (需要清理)" -ForegroundColor Red
Write-Host "  ioedream-db-init: $IOEDreamSQLFiles (保留)" -ForegroundColor Green
Write-Host "  其他位置: $OtherSQLFiles (需要评估)" -ForegroundColor Yellow

# 显示冗余文件详情
Write-Host "🔍 发现的冗余文件详情:" -ForegroundColor Yellow
Get-ChildItem -Path "$ArchivePath\old-database-scripts" -Filter "*.sql" -Recurse | Select-Object Name, DirectoryName, Length, LastWriteTime | ForEach-Object {
    $SizeKB = [math]::Round($_.Length / 1KB, 2)
    Write-Host "  ❌ $($_.DirectoryName)\$($_.Name) ($SizeKB KB)" -ForegroundColor Red
}

Get-ChildItem -Path "$RootPath\数据库SQL脚本\mysql" -Filter "*.sql" | ForEach-Object {
    $SizeKB = [math]::Round($_.Length / 1KB, 2)
    Write-Host "  ⚠️ 数据库SQL脚本\mysql\$($_.Name) ($SizeKB KB)" -ForegroundColor Yellow
}

Get-ChildItem -Path "$RootPath\sql" -Filter "*.sql" | ForEach-Object {
    $SizeKB = [math]::Round($_.Length / 1KB, 2)
    Write-Host "  ⚠️ sql\$($_.Name) ($SizeKB KB)" -ForegroundColor Yellow
}

# 根据操作类型执行
if ($Action -eq "dry-run") {
    Write-Host "🔍 干运行模式 - 不会删除任何文件" -ForegroundColor Green
    Write-Host "" -ForegroundColor Green
    Write-Host "📋 建议的清理操作:" -ForegroundColor Cyan
    Write-Host "  1. 删除 archive\old-database-scripts\ 下的所有SQL文件" -ForegroundColor Red
    Write-Host "  2. 评估其他位置的SQL文件是否需要迁移或删除" -ForegroundColor Yellow
    Write-Host "  3. 保留 ioedream-db-init\ 下的所有文件" -ForegroundColor Green
    Write-Host "" -ForegroundColor Green
    Write-Host "💡 实际清理命令:" -ForegroundColor Cyan
    Write-Host "  .\scripts\cleanup-database-scripts.ps1 -Action clean -Backup" -ForegroundColor White
    Write-Host "  .\scripts\cleanup-database-scripts.ps1 -Action clean -Force" -ForegroundColor White

} elseif ($Action -eq "clean") {
    if (-not $Force) {
        Write-Host "⚠️ 警告: 即将删除数据库脚本文件！" -ForegroundColor Yellow
        Write-Host "   这个操作不可逆，请确认要继续吗？" -ForegroundColor Yellow
        $confirmation = Read-Host "输入 'YES' 确认删除 (输入任何其他值将取消操作)"
        if ($confirmation -ne "YES") {
            Write-Host "❌ 操作已取消" -ForegroundColor Red
            exit 0
        }
    }

    Write-Host "🗑️ 开始清理数据库脚本..." -ForegroundColor Red

    # 清理archive目录下的SQL文件
    if (Test-Path "$ArchivePath\old-database-scripts") {
        Write-Host "📁 清理 archive\old-database-scripts 目录..." -ForegroundColor Yellow
        $FilesToDelete = Get-ChildItem -Path "$ArchivePath\old-database-scripts" -Filter "*.sql" -Recurse

        foreach ($File in $FilesToDelete) {
            try {
                Write-Host "  🗑️ 删除: $($File.FullName)" -ForegroundColor Red
                Remove-Item -Path $File.FullName -Force -Recurse
            } catch {
                Write-Host "  ❌ 删除失败: $($File.FullName) - $($_.Exception.Message)" -ForegroundColor Red
            }
        }

        # 删除空目录
        try {
            $EmptyDirs = Get-ChildItem -Path "$ArchivePath\old-database-scripts" -Directory | Where-Object { -not (Get-ChildItem $_.FullName -Recurse -Force) }
            foreach ($Dir in $EmptyDirs) {
                Write-Host "  📁 删除空目录: $($Dir.FullName)" -ForegroundColor Yellow
                Remove-Item -Path $Dir.FullName -Force -Recurse
            }
        } catch {
            Write-Host "  ⚠️ 删除空目录时出错: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }

    # 评估其他位置的文件
    Write-Host "🔍 评估其他位置的SQL文件..." -ForegroundColor Yellow
    $OtherLocations = @(
        "$RootPath\数据库SQL脚本",
        "$RootPath\sql",
        "$RootPath\scripts"
    )

    foreach ($Location in $OtherLocations) {
        if (Test-Path $Location) {
            Write-Host "📂 检查位置: $Location" -ForegroundColor Yellow
            $FilesInLocation = Get-ChildItem -Path $Location -Filter "*.sql" -Recurse

            foreach ($File in $FilesInLocation) {
                $SizeKB = [math]::Round($File.Length / 1KB, 2)
                Write-Host "  📄 评估: $($File.Name) ($SizeKB KB)" -ForegroundColor Yellow

                # 检查是否与ioedream-db-init中的内容重复
                $DuplicateCheck = Select-String -Path "$RootPath\microservices\ioedream-db-init\src\main\resources\db\migration\*.sql" -Pattern $File.Name -Quiet
                if ($DuplicateCheck) {
                    Write-Host "    ⚠️  发现重复文件，建议删除" -ForegroundColor Red
                } else {
                    Write-Host "    ℹ️  未发现重复，建议手动评估" -ForegroundColor Cyan
                }
            }
        }
    }

} else {
    Write-Host "❌ 未知操作: $Action" -ForegroundColor Red
    Write-Host "   支持的操作: dry-run, clean" -ForegroundColor White
    exit 1
}

# 显示ioedream-db-init目录内容
Write-Host "✅ 保留的ioedream-db-init目录结构:" -ForegroundColor Green
if (Test-Path "$RootPath\microservices\ioedream-db-init\src\main\resources\db\migration") {
    Get-ChildItem -Path "$RootPath\microservices\ioedream-db-init\src\main\resources\db\migration" -Filter "*.sql" | ForEach-Object {
        $SizeKB = [math]::Round($_.Length / 1KB, 2)
        Write-Host "  ✅ $($_.Name) ($SizeKB KB)" -ForegroundColor Green
    }
}

Write-Host "📋 统一初始化脚本:" -ForegroundColor Green
if (Test-Path "$RootPath\microservices\ioedream-db-init\src\main\resources\db\ALL_IN_ONE_INIT.sql") {
    $AllInOneSize = [math]::Round((Get-Item "$RootPath\microservices\ioedream-db-init\src\main\resources\db\ALL_IN_ONE_INIT.sql").Length / 1KB, 2)
    Write-Host "  🚀 ALL_IN_ONE_INIT.sql ($AllInOneSize KB) - 一键初始化脚本" -ForegroundColor Green
}

Write-Host "=================================" -ForegroundColor Green
Write-Host "✅ 数据库脚本清理完成!" -ForegroundColor Green

# 提供后续建议
Write-Host "" -ForegroundColor Green
Write-Host "📋 后续建议:" -ForegroundColor Cyan
Write-Host "1. 🧪 测试 ALL_IN_ONE_INIT.sql 脚本" -ForegroundColor White
Write-Host "2. 🔧 验证数据库初始化功能" -ForegroundColor White
Write-Host "3. 🚀 部署到测试环境验证" -ForegroundColor White
Write-Host "4. 📊 监控API兼容性验证结果" -ForegroundColor White
Write-Host "5. 📖 更新项目文档" -ForegroundColor White