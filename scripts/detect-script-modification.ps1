###############################################################################
# IOE-DREAM 脚本修改检测工具 (PowerShell版本)
# 用途：检测代码是否通过脚本批量修改，确保手动修复质量
# 使用：.\scripts\detect-script-modification.ps1 [目录]
###############################################################################

param(
    [string]$TargetDirectory = "."
)

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# 输出标题
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "🔍 IOE-DREAM 脚本修改检测工具" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📁 检测目录: $TargetDirectory" -ForegroundColor Yellow
Write-Host "⏰ 检测时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Yellow
Write-Host ""

# 统计变量
$violationCount = 0
$fileCount = 0
$suspiciousFiles = @()

# 检测函数
function Test-ScriptModification {
    param([string]$File)

    $script:fileCount++

    # 检查1: 文件修改时间是否过于接近（批量修改特征）
    $fileInfo = Get-Item $File
    $timeDiff = (Get-Date) - $fileInfo.LastWriteTime

    # 如果文件在最近5分钟内修改，可能是批量修改
    if ($timeDiff.TotalMinutes -lt 5) {
        # 检查2: Git提交历史
        $gitDir = Join-Path $TargetDirectory ".git"
        if (Test-Path $gitDir) {
            $commits = git log --since="5 minutes ago" --oneline $File 2>$null
            if (-not $commits) {
                # 文件最近修改但未提交，可疑
                $script:suspiciousFiles += "$File (未提交的最近修改)"
                $script:violationCount++
            }
        }
    }

    # 检查3: 文件中是否包含大量相同模式的修改
    if (Test-Path $File) {
        $content = Get-Content $File -Raw

        # 检查是否有连续多个@Resource替换
        $resourceCount = ([regex]::Matches($content, "@Resource")).Count
        if ($resourceCount -gt 10) {
            # 进一步检查：是否同时缺少日志
            $logCount = ([regex]::Matches($content, "log\.`")).Count
            if ($logCount -lt $resourceCount) {
                $script:suspiciousFiles += "$File (大量@Resource但缺少日志: $resourceCount 个@Resource, $logCount 个日志)"
                $script:violationCount++
            }
        }

        # 检查4: 检查是否有大量@Mapper替换
        $mapperCount = ([regex]::Matches($content, "@Mapper")).Count
        if ($mapperCount -gt 5) {
            $repositoryCount = ([regex]::Matches($content, "@Repository")).Count
            if ($repositoryCount -eq 0 -and $mapperCount -gt 5) {
                # 短时间内添加大量@Mapper但没有@Repository，可疑
                $script:suspiciousFiles += "$File (大量新增@Mapper: $mapperCount 个)"
                $script:violationCount++
            }
        }
    }
}

# 遍历所有Java文件
Write-Host "🔍 开始扫描Java文件..." -ForegroundColor Green
Write-Host ""

$javaFiles = Get-ChildItem -Path $TargetDirectory -Filter "*.java" -Recurse -File

foreach ($file in $javaFiles) {
    Test-ScriptModification -File $file.FullName
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "📊 检测结果汇总" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📁 扫描文件数: $fileCount" -ForegroundColor Yellow
Write-Host "❌ 发现可疑文件: $($suspiciousFiles.Count)" -ForegroundColor Red
Write-Host "🔴 违规计数: $violationCount" -ForegroundColor Red
Write-Host ""

if ($violationCount -gt 0) {
    Write-Host "=========================================" -ForegroundColor Red
    Write-Host "🚨 检测到可能的脚本修改！" -ForegroundColor Red
    Write-Host "=========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "以下文件存在脚本修改嫌疑：" -ForegroundColor Yellow
    Write-Host ""

    foreach ($file in $suspiciousFiles) {
        Write-Host "  ❌ $file" -ForegroundColor Red
    }

    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Yellow
    Write-Host "📋 违规处理流程" -ForegroundColor Yellow
    Write-Host "=========================================" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. 📧 立即通知架构委员会"
    Write-Host "2. 🔍 回顾所有相关代码修改"
    Write-Host "3. 📝 提交手动修复说明"
    Write-Host "4. ✅ 代码审查验证"
    Write-Host "5. 🔒 重新提交代码"
    Write-Host ""
    Write-Host "⚠️  警告：此代码将被拒绝合并！" -ForegroundColor Red
    Write-Host ""

    # 返回错误码，阻止提交
    exit 1
} else {
    Write-Host "✅ 未检测到脚本修改痕迹" -ForegroundColor Green
    Write-Host "✅ 代码修改符合手动修复规范" -ForegroundColor Green
    Write-Host ""
    Write-Host "🎉 可以继续提交代码！" -ForegroundColor Green
    Write-Host ""

    # 返回成功码
    exit 0
}
