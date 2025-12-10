# IOE-DREAM 项目安全清理脚本
# 先备份，再清理，确保项目安全

param(
    [switch]$DryRun = $false,        # 仅显示将要清理的内容，不实际执行
    [switch]$Confirm = $true,        # 执行前需要确认
    [string]$BackupDir = "bak"       # 备份目录
)

# 创建备份目录
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupPath = Join-Path $BackupDir "${timestamp}_cleanup_backup"

Write-Host "🔧 IOE-DREAM 项目安全清理工具" -ForegroundColor Green
Write-Host "⚠️ 安全第一：所有删除操作都会先备份到: $backupPath" -ForegroundColor Yellow

if (-not (Test-Path $backupPath)) {
    New-Item -ItemType Directory -Path $backupPath -Force | Out-Null
    Write-Host "✅ 备份目录已创建: $backupPath" -ForegroundColor Green
}

# 定义清理规则
$cleanupRules = @(
    # P0 - 安全清理（大文件，可重新生成）
    @{
        Priority = "P0"
        Description = "Node.js依赖（可重新安装）"
        Source = "smart-admin-web-javascript/node_modules"
        Target = "node_modules_backup"
        Safe = $true
        Size = "1.5GB"
    },

    @{
        Priority = "P0"
        Description = "Maven构建缓存"
        Source = "microservices/*/target"
        Target = "target_backup"
        Safe = $true
        Size = "300MB"
    },

    @{
        Priority = "P0"
        Description = "IDE缓存文件"
        Source = "**/.serena/cache"
        Target = "serena_cache_backup"
        Safe = $true
        Size = "50MB"
    },

    # P1 - 备份后清理（重复文件）
    @{
        Priority = "P1"
        Description = "数据库脚本备份"
        Source = "database-scripts-backup"
        Target = "database_scripts_backup"
        Safe = $true
        Size = "200MB"
    },

    @{
        Priority = "P1"
        Description = "过时的归档报告"
        Source = "documentation/archive/reports-2025-12-04"
        Target = "archive_reports_backup"
        Safe = $true
        Size = "50MB"
    },

    @{
        Priority = "P1"
        Description = "QoDER工具缓存"
        Source = "**/.qoder"
        Target = "qoder_backup"
        Safe = $true
        Size = "15MB"
    },

    # P2 - 谨慎清理（需要人工确认）
    @{
        Priority = "P2"
        Description = "重复的PowerShell脚本"
        Source = "scripts/*duplicate*.ps1"
        Target = "duplicate_scripts_backup"
        Safe = $false
        Size = "20MB"
        ConfirmRequired = $true
    }
)

# 备份函数
function Backup-ItemSafely {
    param(
        [string]$SourcePath,
        [string]$BackupName
    )

    $backupTargetPath = Join-Path $backupPath $BackupName

    if (Test-Path $SourcePath) {
        Write-Host "📦 备份: $SourcePath -> $backupTargetPath" -ForegroundColor Blue

        if (-not $DryRun) {
            # 创建备份目标目录
            New-Item -ItemType Directory -Path $backupTargetPath -Force -ErrorAction SilentlyContinue | Out-Null

            # 复制文件/文件夹
            if (Test-Path $SourcePath -PathType Container) {
                Copy-Item -Path $SourcePath -Destination $backupTargetPath -Recurse -Force
            } else {
                New-Item -ItemType Directory -Path $backupTargetPath -Force -ErrorAction SilentlyContinue | Out-Null
                Copy-Item -Path $SourcePath -Destination $backupTargetPath -Force
            }

            Write-Host "✅ 备份完成: $BackupName" -ForegroundColor Green
        }
    }
}

# 清理函数
function Remove-ItemSafely {
    param(
        [string]$SourcePath,
        [string]$Description
    )

    if (Test-Path $SourcePath) {
        Write-Host "🗑️ 准备清理: $Description ($SourcePath)" -ForegroundColor Red

        if (-not $DryRun) {
            try {
                if (Test-Path $SourcePath -PathType Container) {
                    Remove-Item -Path $SourcePath -Recurse -Force
                } else {
                    Remove-Item -Path $SourcePath -Force
                }
                Write-Host "✅ 已清理: $Description" -ForegroundColor Green
            } catch {
                Write-Host "❌ 清理失败: $Description - $($_.Exception.Message)" -ForegroundColor Red
            }
        }
    }
}

# 显示清理计划
Write-Host "`n📋 清理计划：" -ForegroundColor Cyan
$totalSize = 0

foreach ($rule in $cleanupRules) {
    Write-Host "[$($rule.Priority)] $($rule.Description) - 大小约 $($rule.Size)" -ForegroundColor White
    if ($rule.Safe) {
        Write-Host "  ✅ 安全清理，可重新生成" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️ 需要人工确认" -ForegroundColor Yellow
    }
    $totalSize += [int]($rule.Size -replace '[^0-9]', '')
}

Write-Host "`n💾 预计节省空间: ${totalSize}MB" -ForegroundColor Magenta

# 用户确认
if ($Confirm -and -not $DryRun) {
    $response = Read-Host "`n🤔 是否继续执行备份和清理？(y/N)"
    if ($response -ne 'y' -and $response -ne 'Y') {
        Write-Host "❌ 操作已取消" -ForegroundColor Red
        exit 1
    }
}

# 执行清理
Write-Host "`n🚀 开始执行清理..." -ForegroundColor Cyan

foreach ($rule in $cleanupRules) {
    Write-Host "`n--- $($rule.Priority): $($rule.Description) ---" -ForegroundColor Yellow

    # 需要确认的项
    if ($rule.ConfirmRequired -and -not $DryRun) {
        $response = Read-Host "⚠️ 此操作需要确认，是否清理 $($rule.Description)？(y/N)"
        if ($response -ne 'y' -and $response -ne 'Y') {
            Write-Host "⏭️ 跳过: $($rule.Description)" -ForegroundColor Yellow
            continue
        }
    }

    # 执行备份
    Backup-ItemSafely -SourcePath $rule.Source -BackupName $rule.Target

    # 等待备份完成
    if (-not $DryRun) {
        Start-Sleep -Seconds 2
    }

    # 执行清理
    Remove-ItemSafely -SourcePath $rule.Source -Description $rule.Description
}

# 生成清理报告
$reportPath = Join-Path $backupPath "cleanup_report.txt"
@"
IOE-DREAM 项目清理报告
=====================

清理时间: $(Get-Date)
备份目录: $backupPath
清理模式: $(if ($DryRun) { "模拟运行" } else { "实际执行" })

清理项目:
$($cleanupRules | ForEach-Object { "[$($_.Priority)] $($_.Description) - $($_.Size)" })

注意事项:
1. 所有删除的文件都已备份到: $backupPath
2. 如需恢复，请从备份目录复制回原位置
3. Node.js依赖可通过 npm install 重新安装
4. Maven缓存可通过 mvn clean install 重新生成

恢复命令示例:
- 恢复node_modules: Copy-Item "$backupPath\node_modules_backup\*" "smart-admin-web-javascript\" -Recurse
- 恢复target目录: Copy-Item "$backupPath\target_backup\*" "microservices\" -Recurse
"@ | Out-File -FilePath $reportPath -Encoding UTF8

Write-Host "`n✅ 清理完成！" -ForegroundColor Green
Write-Host "📄 清理报告已保存到: $reportPath" -ForegroundColor Cyan
Write-Host "💾 备份文件位置: $backupPath" -ForegroundColor Blue
Write-Host "`n🔄 如需恢复文件，请运行:" -ForegroundColor Yellow
Write-Host "   Copy-Item `"$backupPath\<backup_folder>\*`" `<target_path>` -Recurse" -ForegroundColor Gray

if ($DryRun) {
    Write-Host "`n🧪 这是模拟运行，实际未删除任何文件" -ForegroundColor Magenta
    Write-Host "   如需实际执行，请去除 -DryRun 参数" -ForegroundColor Gray
}