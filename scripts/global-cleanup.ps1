# IOE-DREAM 全局项目清理脚本
# 基于GLOBAL_DEEP_CLEANUP_ANALYSIS_REPORT.md的分析结果

param(
    [switch]$DryRun = $false,
    [switch]$Confirm = $false,
    [string]$Phase = "all"
)

Write-Host @"
========================================
IOE-DREAM 全局项目清理
========================================
执行时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
模式: $(if ($DryRun) { "预览模式" } else { "实际执行" })
阶段: $Phase
"@ -ForegroundColor Cyan

if (-not $Confirm -and -not $DryRun) {
    Write-Host "`n⚠️  警告: 此操作将删除/移动文件！请使用 -Confirm 参数确认。" -ForegroundColor Red
    Write-Host "预览模式: .\scripts\global-cleanup.ps1 -DryRun" -ForegroundColor Yellow
    Write-Host "确认执行: .\scripts\global-cleanup.ps1 -Confirm" -ForegroundColor Yellow
    exit 0
}

# 统计变量
$totalFilesProcessed = 0
$totalSpaceFreed = 0

# ========== Phase 1: 清理临时文件 ==========
function Invoke-Phase1 {
    Write-Host "`n📦 Phase 1: 清理临时文件" -ForegroundColor Yellow
    Write-Host ("="*80) -ForegroundColor Gray

    $tempPatterns = @(
        "*.log",
        "*.tmp",
        "*~",
        "*.bak",
        "*.backup",
        "*.old",
        "*.swp",
        ".DS_Store",
        "Thumbs.db"
    )

    $tempFiles = @()
    foreach ($pattern in $tempPatterns) {
        $files = Get-ChildItem -Path . -Recurse -Filter $pattern -File -ErrorAction SilentlyContinue |
                 Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive|\.m2|target' }
        if ($files) {
            $tempFiles += $files
        }
    }

    $totalSize = ($tempFiles | Measure-Object -Property Length -Sum).Sum

    Write-Host "发现临时文件: $($tempFiles.Count) 个" -ForegroundColor White
    Write-Host "占用空间: $([math]::Round($totalSize / 1KB, 2)) KB" -ForegroundColor White

    if ($tempFiles.Count -eq 0) {
        Write-Host "✓ 没有需要清理的临时文件" -ForegroundColor Green
        return
    }

    if ($DryRun) {
        Write-Host "`n[预览] 将删除以下文件:" -ForegroundColor Yellow
        $tempFiles | Select-Object -First 10 | ForEach-Object {
            Write-Host "  - $($_.FullName)" -ForegroundColor Gray
        }
        if ($tempFiles.Count -gt 10) {
            Write-Host "  ... 还有 $($tempFiles.Count - 10) 个文件" -ForegroundColor Gray
        }
    } else {
        Write-Host "`n删除临时文件..." -ForegroundColor Yellow
        $tempFiles | ForEach-Object {
            Remove-Item $_.FullName -Force
            Write-Host "  ✓ 删除: $($_.Name)" -ForegroundColor Green
        }
        Write-Host "✓ Phase 1 完成: 删除了 $($tempFiles.Count) 个文件, 释放 $([math]::Round($totalSize / 1KB, 2)) KB" -ForegroundColor Green
        $script:totalFilesProcessed += $tempFiles.Count
        $script:totalSpaceFreed += $totalSize
    }
}

# ========== Phase 2: 清理AI工具缓存 ==========
function Invoke-Phase2 {
    Write-Host "`n🤖 Phase 2: 清理AI工具缓存" -ForegroundColor Yellow
    Write-Host ("="*80) -ForegroundColor Gray

    $serenaPath = ".serena"
    if (-not (Test-Path $serenaPath)) {
        Write-Host "✓ .serena/ 目录不存在，跳过" -ForegroundColor Green
        return
    }

    # 计算大小
    $serenaSize = (Get-ChildItem -Path $serenaPath -Recurse -File -ErrorAction SilentlyContinue |
                   Measure-Object -Property Length -Sum).Sum / 1MB

    Write-Host ".serena/ 目录大小: $([math]::Round($serenaSize, 2)) MB" -ForegroundColor White

    if ($DryRun) {
        Write-Host "[预览] 将备份重要记忆并清理缓存" -ForegroundColor Yellow
    } else {
        # 备份重要记忆
        $archivePath = "archive/ai-tools/serena"
        if (-not (Test-Path $archivePath)) {
            New-Item -ItemType Directory -Path $archivePath -Force | Out-Null
        }

        $memoriesPath = "$serenaPath/memories"
        if (Test-Path $memoriesPath) {
            Write-Host "备份记忆文件到 $archivePath..." -ForegroundColor Yellow
            Copy-Item -Path "$memoriesPath/*.md" -Destination $archivePath -Force -ErrorAction SilentlyContinue
        }

        # 保留project.yml
        if (Test-Path "$serenaPath/project.yml") {
            Copy-Item -Path "$serenaPath/project.yml" -Destination $archivePath -Force
        }

        # 清理缓存
        Write-Host "清理 .serena/cache/..." -ForegroundColor Yellow
        Remove-Item "$serenaPath/cache" -Recurse -Force -ErrorAction SilentlyContinue

        Write-Host "✓ Phase 2 完成: 清理了AI缓存, 保留了重要配置和记忆" -ForegroundColor Green
        $script:totalSpaceFreed += $serenaSize * 1MB
    }
}

# ========== Phase 3: 清理历史备份 ==========
function Invoke-Phase3 {
    Write-Host "`n📦 Phase 3: 清理历史备份" -ForegroundColor Yellow
    Write-Host ("="*80) -ForegroundColor Gray

    $backupPath = "backup"
    if (-not (Test-Path $backupPath)) {
        Write-Host "✓ backup/ 目录不存在，跳过" -ForegroundColor Green
        return
    }

    $backupSize = (Get-ChildItem -Path $backupPath -Recurse -File -ErrorAction SilentlyContinue |
                   Measure-Object -Property Length -Sum).Sum / 1MB

    Write-Host "backup/ 目录大小: $([math]::Round($backupSize, 2)) MB" -ForegroundColor White
    Write-Host "文件数量: $((Get-ChildItem -Path $backupPath -Recurse -File -ErrorAction SilentlyContinue).Count)" -ForegroundColor White

    if ($DryRun) {
        Write-Host "[预览] 将移除 backup/ 目录（Git已有完整历史）" -ForegroundColor Yellow
    } else {
        Write-Host "移除 backup/ 目录..." -ForegroundColor Yellow
        Remove-Item $backupPath -Recurse -Force
        Write-Host "✓ Phase 3 完成: 移除了 backup/ 目录" -ForegroundColor Green
        $script:totalSpaceFreed += $backupSize * 1MB
    }
}

# ========== Phase 4: 整合重复文档 ==========
function Invoke-Phase4 {
    Write-Host "`n📄 Phase 4: 整合重复文档" -ForegroundColor Yellow
    Write-Host ("="*80) -ForegroundColor Gray

    # 查找重复的README
    $readmeFiles = Get-ChildItem -Path . -Recurse -Filter "README.md" -File -ErrorAction SilentlyContinue |
                    Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive|\.m2|target' }

    Write-Host "发现 README.md: $($readmeFiles.Count) 个" -ForegroundColor White

    # 查找重复的CLAUDE.md
    $claudeFiles = Get-ChildItem -Path . -Recurse -Filter "CLAUDE.md" -File -ErrorAction SilentlyContinue |
                    Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive|\.m2|target' }

    Write-Host "发现 CLAUDE.md: $($claudeFiles.Count) 个" -ForegroundColor White

    $duplicateCount = 0

    if ($DryRun) {
        Write-Host "`n[预览] 发现以下可能重复的文档:" -ForegroundColor Yellow

        # 列出根目录之外的README
        $readmeFiles | Where-Object { $_.DirectoryName -ne (Get-Location).Path } |
                      Select-Object -First 10 | ForEach-Object {
            Write-Host "  - $($_.FullName)" -ForegroundColor Gray
            $duplicateCount++
        }
    } else {
        Write-Host "`n注意: 此阶段需要人工审查，跳过自动删除" -ForegroundColor Yellow
        Write-Host "建议手动审查以下文件:" -ForegroundColor Yellow

        $readmeFiles | Where-Object { $_.DirectoryName -ne (Get-Location).Path } |
                      Select-Object -First 5 | ForEach-Object {
            Write-Host "  - $($_.FullName)" -ForegroundColor Gray
        }

        if ($readmeFiles.Count -gt 5) {
            Write-Host "  ... 还有 $($readmeFiles.Count - 5) 个文件" -ForegroundColor Gray
        }
    }
}

# ========== Phase 5: 清理.trae目录 ==========
function Invoke-Phase5 {
    Write-Host "`n📦 Phase 5: 清理.trae目录" -ForegroundColor Yellow
    Write-Host ("="*80) -ForegroundColor Gray

    $traePath = ".trae"
    if (-not (Test-Path $traePath)) {
        Write-Host "✓ .trae/ 目录不存在，跳过" -ForegroundColor Green
        return
    }

    $traeSize = (Get-ChildItem -Path $traePath -Recurse -File -ErrorAction SilentlyContinue |
                Measure-Object -Property Length -Sum).Sum / 1KB

    Write-Host ".trae/ 目录大小: $([math]::Round($traeSize, 2)) KB" -ForegroundColor White
    Write-Host "文件数量: $((Get-ChildItem -Path $traePath -Recurse -File -ErrorAction SilentlyContinue).Count)" -ForegroundColor White

    if ($DryRun) {
        Write-Host "[预览] 可选择归档或删除 .trae/ 目录" -ForegroundColor Yellow
    } else {
        Write-Host "注意: .trae/ 目录可能包含AI工具生成的有用文档" -ForegroundColor Yellow
        Write-Host "建议: 手动审查后再决定是否删除" -ForegroundColor Yellow
    }
}

# ========== Phase 6: 优化文档结构 ==========
function Invoke-Phase6 {
    Write-Host "`n📚 Phase 6: 优化文档结构" -ForegroundColor Yellow
    Write-Host ("="*80) -ForegroundColor Gray

    $docPath = "documentation"
    if (-not (Test-Path $docPath)) {
        Write-Host "✓ documentation/ 目录不存在，跳过" -ForegroundColor Green
        return
    }

    $docFiles = Get-ChildItem -Path $docPath -Recurse -File -ErrorAction SilentlyContinue
    Write-Host "documentation/ 文件数: $($docFiles.Count)" -ForegroundColor White

    Write-Host "`n注意: 此阶段需要人工审查" -ForegroundColor Yellow
    Write-Host "建议: 审查documentation/目录中的重复和过时文档" -ForegroundColor Yellow
}

# ========== 主执行流程 ==========
try {
    switch ($Phase) {
        "1" { Invoke-Phase1 }
        "2" { Invoke-Phase2 }
        "3" { Invoke-Phase3 }
        "4" { Invoke-Phase4 }
        "5" { Invoke-Phase5 }
        "6" { Invoke-Phase6 }
        "all" {
            Invoke-Phase1
            Invoke-Phase2
            Invoke-Phase3
            Invoke-Phase4
            Invoke-Phase5
            Invoke-Phase6
        }
        default {
            Write-Host "未知阶段: $Phase" -ForegroundColor Red
            Write-Host "可用阶段: 1, 2, 3, 4, 5, 6, all" -ForegroundColor Yellow
            exit 1
        }
    }

    # 输出总结
    Write-Host "`n" + ("="*80) -ForegroundColor Gray
    Write-Host "📊 清理总结" -ForegroundColor Cyan
    Write-Host ("="*80) -ForegroundColor Gray

    if ($DryRun) {
        Write-Host "这是预览模式，没有实际删除文件" -ForegroundColor Yellow
        Write-Host "`n要执行实际清理，请运行:" -ForegroundColor White
        Write-Host "  .\scripts\global-cleanup.ps1 -Phase $Phase -Confirm" -ForegroundColor Green
    } else {
        Write-Host "处理文件总数: $totalFilesProcessed" -ForegroundColor White
        Write-Host "释放空间: $([math]::Round($totalSpaceFreed / 1MB, 2)) MB" -ForegroundColor Green
        Write-Host "`n✓ 全局清理完成！" -ForegroundColor Green
    }

} catch {
    Write-Host "`n❌ 错误: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
