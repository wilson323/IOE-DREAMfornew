# ============================================================================
# 修复测试文件中的 getOk() 问题
# ============================================================================
# 问题说明：
# - ResponseDTO 没有 getOk() 方法
# - 应该使用 isSuccess() 方法来判断响应是否成功
# - 或者使用 assertTrue(result.isSuccess()) 替代 assertTrue(result.getOk())
#
# 修复规则：
# - result.getOk() → result.isSuccess()
# - assertTrue(result.getOk()) → assertTrue(result.isSuccess())
# - assertFalse(result.getOk()) → assertFalse(result.isSuccess())
# ============================================================================

param(
    [switch]$DryRun = $false,
    [string]$Module = ""
)

$ErrorActionPreference = "Stop"
$script:TotalFixed = 0
$script:TotalFiles = 0
$script:Errors = @()

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "修复测试文件中的 getOk() 问题" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# 读取扫描报告
$reportFile = ".\reports-test-getOk-scanned.txt"
if (-not (Test-Path $reportFile)) {
    Write-Host "❌ 错误: 找不到扫描报告文件: $reportFile" -ForegroundColor Red
    Write-Host "请先运行扫描命令生成报告" -ForegroundColor Yellow
    exit 1
}

$files = Get-Content $reportFile | ForEach-Object {
    if ($_ -match '^(.+):(\d+)$') {
        [PSCustomObject]@{
            File = $matches[1]
            Line = [int]$matches[2]
        }
    }
}

if ($Module) {
    $files = $files | Where-Object { $_.File -like "*\$Module\*" }
}

Write-Host "📋 找到 $($files.Count) 个需要修复的位置" -ForegroundColor Yellow
Write-Host ""

# 按文件分组
$filesByFile = $files | Group-Object File

foreach ($fileGroup in $filesByFile) {
    $filePath = $fileGroup.Name

    if (-not (Test-Path $filePath)) {
        Write-Host "⚠️  跳过不存在的文件: $filePath" -ForegroundColor Yellow
        continue
    }

    $script:TotalFiles++
    $lines = Get-Content $filePath -Raw
    $originalLines = $lines
    $modified = $false
    $lineNumbers = $fileGroup.Group | ForEach-Object { $_.Line } | Sort-Object -Unique

    Write-Host "📝 处理文件: $filePath" -ForegroundColor Cyan
    Write-Host "   需要修复的行: $($lineNumbers -join ', ')" -ForegroundColor Gray

    # 修复规则
    $replacements = @(
        # assertTrue(result.getOk()) → assertTrue(result.isSuccess())
        @{
            Pattern     = 'assertTrue\s*\(\s*([^)]+)\.getOk\s*\(\)\s*\)'
            Replacement = 'assertTrue($1.isSuccess())'
            Description = 'assertTrue(result.getOk()) → assertTrue(result.isSuccess())'
        },
        # assertFalse(result.getOk()) → assertFalse(result.isSuccess())
        @{
            Pattern     = 'assertFalse\s*\(\s*([^)]+)\.getOk\s*\(\)\s*\)'
            Replacement = 'assertFalse($1.isSuccess())'
            Description = 'assertFalse(result.getOk()) → assertFalse(result.isSuccess())'
        },
        # result.getOk() → result.isSuccess()
        @{
            Pattern     = '([a-zA-Z_][a-zA-Z0-9_]*)\s*\.\s*getOk\s*\(\s*\)'
            Replacement = '$1.isSuccess()'
            Description = 'result.getOk() → result.isSuccess()'
        }
    )

    foreach ($replacement in $replacements) {
        $matches = [regex]::Matches($lines, $replacement.Pattern)
        if ($matches.Count -gt 0) {
            $lines = [regex]::Replace($lines, $replacement.Pattern, $replacement.Replacement)
            $modified = $true
            Write-Host "   ✅ 应用规则: $($replacement.Description)" -ForegroundColor Green
            $script:TotalFixed += $matches.Count
        }
    }

    if ($modified) {
        if (-not $DryRun) {
            try {
                # 确保使用UTF-8编码保存
                [System.IO.File]::WriteAllText($filePath, $lines, [System.Text.Encoding]::UTF8)
                Write-Host "   ✅ 文件已更新" -ForegroundColor Green
            }
            catch {
                $script:Errors += "❌ 更新文件失败: $filePath - $($_.Exception.Message)"
                Write-Host "   ❌ 更新失败: $($_.Exception.Message)" -ForegroundColor Red
            }
        }
        else {
            Write-Host "   🔍 [DRY-RUN] 将更新文件" -ForegroundColor Yellow
        }
    }
    else {
        Write-Host "   ⚠️  未找到匹配的模式" -ForegroundColor Yellow
    }

    Write-Host ""
}

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "修复完成统计" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "处理文件数: $script:TotalFiles" -ForegroundColor White
Write-Host "修复位置数: $script:TotalFixed" -ForegroundColor White

if ($DryRun) {
    Write-Host ""
    Write-Host "🔍 这是预览模式，未实际修改文件" -ForegroundColor Yellow
    Write-Host "运行脚本时不加 -DryRun 参数将实际修改文件" -ForegroundColor Yellow
}

if ($script:Errors.Count -gt 0) {
    Write-Host ""
    Write-Host "❌ 错误列表:" -ForegroundColor Red
    foreach ($error in $script:Errors) {
        Write-Host "   $error" -ForegroundColor Red
    }
    exit 1
}

Write-Host ""
Write-Host "✅ 所有修复完成！" -ForegroundColor Green

