# Git 提交状态验证脚本

Write-Host "=== Git 提交状态检查 ===" -ForegroundColor Cyan

# 检查是否有暂存的文件
Write-Host "`n1. 检查暂存区状态:" -ForegroundColor Yellow
$stagedFiles = git diff --cached --name-only
if ($stagedFiles) {
    $count = ($stagedFiles | Measure-Object).Count
    Write-Host "   ✗ 仍有 $count 个文件在暂存区，未提交" -ForegroundColor Red
    Write-Host "   前 10 个文件:" -ForegroundColor Yellow
    $stagedFiles | Select-Object -First 10 | ForEach-Object { Write-Host "     - $_" -ForegroundColor Gray }
} else {
    Write-Host "   ✓ 暂存区为空，所有文件已提交" -ForegroundColor Green
}

# 检查最新提交
Write-Host "`n2. 检查最新提交:" -ForegroundColor Yellow
$lastCommit = git log -1 --oneline 2>&1
if ($LASTEXITCODE -eq 0 -and $lastCommit) {
    Write-Host "   最新提交: $lastCommit" -ForegroundColor Green
    $commitDate = git log -1 --format="%cd" --date=format:"%Y-%m-%d %H:%M:%S"
    Write-Host "   提交时间: $commitDate" -ForegroundColor Cyan
} else {
    Write-Host "   ✗ 无法获取最新提交信息" -ForegroundColor Red
}

# 检查未跟踪的文件
Write-Host "`n3. 检查未跟踪的文件:" -ForegroundColor Yellow
$untrackedFiles = git status --porcelain | Where-Object { $_ -match "^\?\?" }
if ($untrackedFiles) {
    $count = ($untrackedFiles | Measure-Object).Count
    Write-Host "   有 $count 个未跟踪的文件" -ForegroundColor Yellow
    $untrackedFiles | Select-Object -First 5 | ForEach-Object { 
        $file = ($_ -replace "^\?\?\s+", "")
        Write-Host "     - $file" -ForegroundColor Gray 
    }
} else {
    Write-Host "   ✓ 没有未跟踪的文件" -ForegroundColor Green
}

# 检查工作区状态
Write-Host "`n4. 检查工作区状态:" -ForegroundColor Yellow
$modifiedFiles = git status --porcelain | Where-Object { $_ -match "^\s[MADRC]" }
if ($modifiedFiles) {
    $count = ($modifiedFiles | Measure-Object).Count
    Write-Host "   有 $count 个已修改但未暂存的文件" -ForegroundColor Yellow
} else {
    Write-Host "   ✓ 工作区干净，没有未暂存的修改" -ForegroundColor Green
}

Write-Host "`n=== 检查完成 ===" -ForegroundColor Cyan
