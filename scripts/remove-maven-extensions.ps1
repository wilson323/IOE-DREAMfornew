# PowerShell脚本：移除Maven冲突扩展
# 作者：IOE-DREAM架构团队
# 日期：2025-12-26
# 用途：移除hazelcast和redisson扩展解决Maven编译冲突
# 注意：需要以管理员身份运行

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "❌ 错误: 此脚本需要管理员权限运行" -ForegroundColor Red
    Write-Host ""
    Write-Host "请按以下步骤操作:" -ForegroundColor Yellow
    Write-Host "1. 右键点击 PowerShell" -ForegroundColor White
    Write-Host "2. 选择 '以管理员身份运行'" -ForegroundColor White
    Write-Host "3. 在管理员PowerShell中执行:" -ForegroundColor White
    Write-Host "   Set-ExecutionPolicy Bypass -Scope Process -Force" -ForegroundColor Cyan
    Write-Host "   D:\IOE-DREAM\scripts\remove-maven-extensions.ps1" -ForegroundColor Cyan
    Write-Host ""
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  移除Maven冲突扩展工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ 检测到管理员权限" -ForegroundColor Green
Write-Host ""

# 定义Maven扩展目录
$mavenExtDir = "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.11\lib\ext"
$hazelcastDir = Join-Path $mavenExtDir "hazelcast"
$redissonDir = Join-Path $mavenExtDir "redisson"

# 检查目录是否存在
Write-Host "🔍 检查Maven扩展目录..." -ForegroundColor Cyan
Write-Host ""

$hazelcastExists = Test-Path $hazelcastDir
$redissonExists = Test-Path $redissonDir

if ($hazelcastExists) {
    Write-Host "✓ 找到Hazelcast扩展: $hazelcastDir" -ForegroundColor Yellow
} else {
    Write-Host "✗ Hazelcast扩展不存在" -ForegroundColor Gray
}

if ($redissonExists) {
    Write-Host "✓ 找到Redisson扩展: $redissonDir" -ForegroundColor Yellow
} else {
    Write-Host "✗ Redisson扩展不存在" -ForegroundColor Gray
}

Write-Host ""

# 询问用户确认
if ($hazelcastExists -or $redissonExists) {
    Write-Host "⚠️  警告: 此操作将删除Maven扩展目录" -ForegroundColor Yellow
    Write-Host ""
    $confirmation = Read-Host "确认删除? (Y/N)"

    if ($confirmation -ne "Y" -and $confirmation -ne "y") {
        Write-Host ""
        Write-Host "❌ 操作已取消" -ForegroundColor Red
        exit 0
    }

    Write-Host ""
    Write-Host "🔧 开始删除扩展..." -ForegroundColor Cyan
    Write-Host ""

    # 删除hazelcast
    if ($hazelcastExists) {
        try {
            Remove-Item -Path $hazelcastDir -Recurse -Force -ErrorAction Stop
            Write-Host "✅ 成功删除Hazelcast扩展" -ForegroundColor Green
        } catch {
            Write-Host "❌ 删除Hazelcast扩展失败: $_" -ForegroundColor Red
        }
    }

    # 删除redisson
    if ($redissonExists) {
        try {
            Remove-Item -Path $redissonDir -Recurse -Force -ErrorAction Stop
            Write-Host "✅ 成功删除Redisson扩展" -ForegroundColor Green
        } catch {
            Write-Host "❌ 删除Redisson扩展失败: $_" -ForegroundColor Red
        }
    }

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  删除完成验证" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""

    # 验证删除结果
    $hazelcastStillExists = Test-Path $hazelcastDir
    $redissonStillExists = Test-Path $redissonDir

    if (-not $hazelcastStillExists -and -not $redissonStillExists) {
        Write-Host "✅ 扩展删除成功! Maven编译环境已修复" -ForegroundColor Green
        Write-Host ""
        Write-Host "下一步: 验证Maven编译" -ForegroundColor Cyan
        Write-Host "执行命令: mvn clean compile -pl ioedream-attendance-service -am -DskipTests" -ForegroundColor White
    } else {
        Write-Host "⚠️  扩展可能未完全删除" -ForegroundColor Yellow
        if ($hazelcastStillExists) {
            Write-Host "  - Hazelcast仍然存在" -ForegroundColor Red
        }
        if ($redissonStillExists) {
            Write-Host "  - Redisson仍然存在" -ForegroundColor Red
        }
    }

} else {
    Write-Host "ℹ️  没有发现需要删除的扩展" -ForegroundColor Gray
    Write-Host "Maven环境已经是干净的" -ForegroundColor Green
}

Write-Host ""
