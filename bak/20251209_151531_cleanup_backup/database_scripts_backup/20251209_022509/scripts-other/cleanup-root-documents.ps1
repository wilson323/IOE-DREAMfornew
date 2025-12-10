# IOE-DREAM 根目录文档清理脚本
# 功能：清理根目录下的临时报告文档，移动到归档目录
# 作者：IOE-DREAM Team
# 日期：2025-01-30

param(
    [switch]$DryRun = $false  # 干运行模式，只显示不执行
)

$RootPath = "d:\IOE-DREAM"
$ArchivePath = "$RootPath\documentation\archive\root-reports"

# 确保归档目录存在
if (-not (Test-Path $ArchivePath)) {
    Write-Host "创建归档目录: $ArchivePath"
    if (-not $DryRun) {
        New-Item -ItemType Directory -Path $ArchivePath -Force | Out-Null
    }
}

# 定义文件分类规则
$TempReports = @(
    "*FINAL*.md",
    "*COMPLETE*.md",
    "*COMPLETE_*.md",
    "MERGE_*.md",
    "*REPORT*.md",
    "*FIX*.md",
    "*ERROR*.md",
    "*COMPILATION*.md",
    "*SUMMARY*.md",
    "TEST_*.md",
    "*ANALYSIS*.md",
    "*VERIFICATION*.md",
    "*EXECUTION*.md",
    "*PROGRESS*.md",
    "*IMPLEMENTATION*.md",
    "FIX_NOW.md",
    "EXECUTE_NOW.md",
    "START_BUILD.md",
    "README_BUILD.md",
    "*业务模块*.md",
    "*工作流*.md",
    "*全局*.md",
    "代码质量修复报告*.md",
    "紧急*.md",
    "区域管理*.md",
    "TODO_*.md",
    "*UNIT_TEST*.md",
    "*USER_ROLE*.md"
)

# 有用文档移动到对应目录
$UsefulDocs = @{
    "DEPLOYMENT.md" = "$RootPath\documentation\deployment\DEPLOYMENT.md"
    "MCP配置说明.md" = "$RootPath\documentation\development\MCP配置说明.md"
}

# 统计信息
$stats = @{
    Moved = 0
    Deleted = 0
    Skipped = 0
}

Write-Host "========================================"
Write-Host "IOE-DREAM 根目录文档清理脚本"
Write-Host "========================================"
Write-Host ""

if ($DryRun) {
    Write-Host "⚠️  干运行模式 - 不会实际移动或删除文件" -ForegroundColor Yellow
    Write-Host ""
}

# 处理临时报告文件
Write-Host "处理临时报告文件..."
Write-Host ""

Get-ChildItem -Path $RootPath -Filter "*.md" -File | Where-Object {
    $_.Name -ne "CLAUDE.md" -and 
    $_.Name -ne "CLAUDE.md.bak"
} | ForEach-Object {
    $file = $_
    $shouldMove = $false
    
    # 检查是否匹配临时报告模式
    foreach ($pattern in $TempReports) {
        if ($file.Name -like $pattern) {
            $shouldMove = $true
            break
        }
    }
    
    # 检查是否是有用文档
    $isUseful = $UsefulDocs.ContainsKey($file.Name)
    
    if ($shouldMove -and -not $isUseful) {
        $targetPath = Join-Path $ArchivePath $file.Name
        Write-Host "  📦 归档: $($file.Name) -> root-reports/" -ForegroundColor Cyan
        
        if (-not $DryRun) {
            try {
                Move-Item -Path $file.FullName -Destination $targetPath -Force
                $stats.Moved++
            } catch {
                Write-Host "    ❌ 移动失败: $_" -ForegroundColor Red
            }
        }
    }
    elseif ($isUseful) {
        $targetPath = $UsefulDocs[$file.Name]
        Write-Host "  📁 移动: $($file.Name) -> $($targetPath.Replace($RootPath + '\', ''))" -ForegroundColor Green
        
        if (-not $DryRun) {
            try {
                $targetDir = Split-Path $targetPath -Parent
                if (-not (Test-Path $targetDir)) {
                    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
                }
                
                # 如果目标文件存在，先备份
                if (Test-Path $targetPath) {
                    $backupPath = $targetPath + ".bak"
                    Copy-Item -Path $targetPath -Destination $backupPath -Force
                    Write-Host "    ⚠️  目标文件已存在，已备份为 .bak" -ForegroundColor Yellow
                }
                
                Move-Item -Path $file.FullName -Destination $targetPath -Force
                $stats.Moved++
            } catch {
                Write-Host "    ❌ 移动失败: $_" -ForegroundColor Red
            }
        }
    }
    else {
        Write-Host "  ⏭️  跳过: $($file.Name)" -ForegroundColor Gray
        $stats.Skipped++
    }
}

Write-Host ""
Write-Host "========================================"
Write-Host "清理完成统计"
Write-Host "========================================"
Write-Host "  归档文件: $($stats.Moved)" -ForegroundColor Cyan
Write-Host "  跳过文件: $($stats.Skipped)" -ForegroundColor Gray
Write-Host ""

if ($DryRun) {
    Write-Host "💡 这是干运行模式，没有实际执行移动操作" -ForegroundColor Yellow
    Write-Host "   运行脚本时不加 -DryRun 参数来实际执行清理" -ForegroundColor Yellow
} else {
    Write-Host "✅ 清理完成！" -ForegroundColor Green
}
