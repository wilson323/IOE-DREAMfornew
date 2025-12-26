# IOE-DREAM 重复文档整合脚本
# 基于DUPLICATE_DOCS_INTEGRATION_PLAN.md的分析结果

param(
    [switch]$DryRun = $false,
    [switch]$Confirm = $false,
    [string]$Phase = "all"
)

Write-Host @"
========================================
IOE-DREAM 重复文档整合
========================================
执行时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
模式: $(if ($DryRun) { "预览模式" } else { "实际执行" })
阶段: $Phase
"@ -ForegroundColor Cyan

if (-not $Confirm -and -not $DryRun) {
    Write-Host "`n⚠️  警告: 此操作将删除/移动文件！请使用 -Confirm 参数确认。" -ForegroundColor Red
    Write-Host "预览模式: .\scripts\integrate-duplicate-docs.ps1 -DryRun" -ForegroundColor Yellow
    Write-Host "确认执行: .\scripts\integrate-duplicate-docs.ps1 -Confirm" -ForegroundColor Yellow
    exit 0
}

# 统计变量
$totalFilesProcessed = 0

# ========== Phase 1: 安全删除 ==========
function Invoke-Phase1 {
    Write-Host "`n🗑️  Phase 1: 安全删除（零风险）" -ForegroundColor Yellow
    Write-Host ("="*80) -ForegroundColor Gray

    $filesToDelete = @(
        @{Path = "smart-admin-web-javascript/README.md"; Reason = "空文件 (0 KB)"},
        @{Path = "scripts/database/versions/README.md"; Reason = "冗余的版本说明"}
    )

    $existingFiles = $filesToDelete | Where-Object { Test-Path $_.Path }

    if ($existingFiles.Count -eq 0) {
        Write-Host "✓ 没有需要删除的文件" -ForegroundColor Green
        return
    }

    Write-Host "发现 $($existingFiles.Count) 个文件需要删除:" -ForegroundColor White

    foreach ($file in $existingFiles) {
        $size = if (Test-Path $file.Path) {
            [math]::Round((Get-Item $file.Path).Length / 1KB, 2)
        } else { 0 }

        if ($DryRun) {
            Write-Host "  [预览] 删除: $($file.Path) ($size KB) - $($file.Reason)" -ForegroundColor Gray
        } else {
            Write-Host "  ✓ 删除: $($file.Path) ($size KB) - $($file.Reason)" -ForegroundColor Green
            Remove-Item $file.Path -Force
            $script:totalFilesProcessed++
        }
    }

    if (-not $DryRun) {
        Write-Host "`n✓ Phase 1 完成: 删除了 $($existingFiles.Count) 个文件" -ForegroundColor Green
    }
}

# ========== Phase 2: 整合到主文档 ==========
function Invoke-Phase2 {
    Write-Host "`n📝 Phase 2: 整合到主文档（需手动操作）" -ForegroundColor Yellow
    Write-Host ("="*80) -ForegroundColor Gray

    Write-Host "此阶段需要手动操作，自动跳过" -ForegroundColor Yellow
    Write-Host "`n需要整合的文件:" -ForegroundColor White

    $filesToIntegrate = @(
        @{Path = ".claude/skills/README.md"; Target = "主README.md - AI助手章节"},
        @{Path = ".spec-workflow/user-templates/README.md"; Target = "主README.md - 开发规范章节"},
        @{Path = "deployment/test-environment/README.md"; Target = "主README.md - 部署章节"}
    )

    foreach ($file in $filesToIntegrate | Where-Object { Test-Path $_.Path }) {
        $size = [math]::Round((Get-Item $file.Path).Length / 1KB, 2)
        Write-Host "  - $($file.Path) ($size KB) → $($file.Target)" -ForegroundColor Gray
    }

    Write-Host "`n手动操作步骤:" -ForegroundColor Yellow
    Write-Host "  1. 读取上述文件内容" -ForegroundColor White
    Write-Host "  2. 提取关键信息" -ForegroundColor White
    Write-Host "  3. 添加到主README.md的相应章节" -ForegroundColor White
    Write-Host "  4. 删除原文件" -ForegroundColor White
    Write-Host "  5. 运行: .\scripts\integrate-duplicate-docs.ps1 -Phase 2 -Confirm" -ForegroundColor White
}

# ========== Phase 3: 评估IDE工具配置 ==========
function Invoke-Phase3 {
    Write-Host "`n🔍 Phase 3: 评估IDE/AI工具配置（需团队确认）" -ForegroundColor Yellow
    Write-Host ("="*80) -ForegroundColor Gray

    $toolConfigs = @(
        @{Path = ".qoder/rules/claude.md"; Tool = "Qoder IDE"; Question = "团队是否使用Qoder IDE?"}
        @{Path = ".trae/rules/claude.md"; Tool = "Trae AI工具"; Question = "Trae工具是否仍在使用?"}
        @{Path = ".windsurf/rules/claude.md"; Tool = "Windsurf IDE"; Question = "团队是否使用Windsurf IDE?"}
    )

    foreach ($config in $toolConfigs | Where-Object { Test-Path $_.Path }) {
        $size = [math]::Round((Get-Item $config.Path).Length / 1KB, 2)
        Write-Host "`n文件: $($config.Path) ($size KB)" -ForegroundColor White
        Write-Host "工具: $($config.Tool)" -ForegroundColor Gray
        Write-Host "问题: $($config.Question)" -ForegroundColor Yellow

        if ($DryRun) {
            Write-Host "  [预览] 需要团队确认是否删除" -ForegroundColor Gray
        } else {
            Write-Host "  建议: 询问团队后决定是否删除" -ForegroundColor Yellow
        }
    }

    Write-Host "`n手动操作步骤:" -ForegroundColor Yellow
    Write-Host "  1. 创建团队问卷，确认各工具使用情况" -ForegroundColor White
    Write-Host "  2. 根据反馈删除不需要的工具配置" -ForegroundColor White
    Write-Host "  3. 运行: .\scripts\integrate-duplicate-docs.ps1 -Phase 3 -Confirm" -ForegroundColor White
}

# ========== Phase 4: 处理培训材料 ==========
function Invoke-Phase4 {
    Write-Host "`n📚 Phase 4: 处理培训材料（需对比内容）" -ForegroundColor Yellow
    Write-Host ("="*80) -ForegroundColor Gray

    $trainingFile = "training/new-developer/CLAUDE.md"
    $mainFile = "CLAUDE.md"

    if (-not (Test-Path $trainingFile)) {
        Write-Host "✓ training/new-developer/CLAUDE.md 不存在，跳过" -ForegroundColor Green
        return
    }

    $trainingSize = [math]::Round((Get-Item $trainingFile).Length / 1KB, 2)
    $mainSize = [math]::Round((Get-Item $mainFile).Length / 1KB, 2)

    Write-Host "培训文件: $trainingFile ($trainingSize KB)" -ForegroundColor White
    Write-Host "主文件: $mainFile ($mainSize KB)" -ForegroundColor White

    Write-Host "`n手动操作步骤:" -ForegroundColor Yellow
    Write-Host "  1. 对比两个文件的内容差异" -ForegroundColor White
    Write-Host "  2. 判断是否为历史版本" -ForegroundColor White
    Write-Host "  3. 如果是历史版本，删除" -ForegroundColor White
    Write-Host "  4. 如果有独特内容，整合或保留" -ForegroundColor White
    Write-Host "  5. 运行: .\scripts\integrate-duplicate-docs.ps1 -Phase 4 -Confirm" -ForegroundColor White
}

# ========== Phase 5: 最终清理 ==========
function Invoke-Phase5 {
    Write-Host "`n🧹 Phase 5: 最终清理（需逐个评估）" -ForegroundColor Yellow
    Write-Host ("="*80) -ForegroundColor Gray

    Write-Host "此阶段需要逐个评估文档价值" -ForegroundColor Yellow
    Write-Host "`n需要评估的文档类型:" -ForegroundColor White

    $docsToEvaluate = @(
        "前端页面README (smart-admin-web-javascript/src/views/**/README.md)",
        "其他子模块README"
    )

    foreach ($docType in $docsToEvaluate) {
        Write-Host "  - $docType" -ForegroundColor Gray
    }

    Write-Host "`n手动操作步骤:" -ForegroundColor Yellow
    Write-Host "  1. 列出所有需要评估的文档" -ForegroundColor White
    Write-Host "  2. 逐个评估文档价值" -ForegroundColor White
    Write-Host "  3. 删除或整合冗余文档" -ForegroundColor White
    Write-Host "  4. 运行: .\scripts\integrate-duplicate-docs.ps1 -Phase 5 -Confirm" -ForegroundColor White
}

# ========== 主执行流程 ==========
try {
    switch ($Phase) {
        "1" { Invoke-Phase1 }
        "2" { Invoke-Phase2 }
        "3" { Invoke-Phase3 }
        "4" { Invoke-Phase4 }
        "5" { Invoke-Phase5 }
        "all" {
            Invoke-Phase1
            Invoke-Phase2
            Invoke-Phase3
            Invoke-Phase4
            Invoke-Phase5
        }
        default {
            Write-Host "未知阶段: $Phase" -ForegroundColor Red
            Write-Host "可用阶段: 1, 2, 3, 4, 5, all" -ForegroundColor Yellow
            exit 1
        }
    }

    # 输出总结
    Write-Host "`n" + ("="*80) -ForegroundColor Gray
    Write-Host "📊 整合总结" -ForegroundColor Cyan
    Write-Host ("="*80) -ForegroundColor Gray

    if ($DryRun) {
        Write-Host "这是预览模式，没有实际删除文件" -ForegroundColor Yellow
        Write-Host "`n要执行实际整合，请运行:" -ForegroundColor White
        Write-Host "  .\scripts\integrate-duplicate-docs.ps1 -Phase $Phase -Confirm" -ForegroundColor Green
    } else {
        Write-Host "处理文件总数: $totalFilesProcessed" -ForegroundColor White
        Write-Host "`n✓ Phase 1 完成！Phase 2-5 需要手动操作" -ForegroundColor Green
        Write-Host "`n详细信息请查看: DUPLICATE_DOCS_INTEGRATION_PLAN.md" -ForegroundColor Yellow
    }

} catch {
    Write-Host "`n❌ 错误: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
