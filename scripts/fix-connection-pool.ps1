# IOE-DREAM 连接池修复脚本
# 修复 UV_HANDLE_CLOSING 错误

Write-Host "🔧 开始修复连接池和句柄问题..." -ForegroundColor Green

# 1. 强制关闭所有Java进程（防止句柄占用）
Write-Host "🛑 关闭所有Java进程..." -ForegroundColor Yellow
try {
    Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
    Write-Host "✅ Java进程已关闭" -ForegroundColor Green
} catch {
    Write-Host "⚠️ 没有运行的Java进程" -ForegroundColor Yellow
}

# 2. 关闭Node.js进程
Write-Host "🛑 关闭Node.js进程..." -ForegroundColor Yellow
try {
    Get-Process node -ErrorAction SilentlyContinue | Stop-Process -Force
    Write-Host "✅ Node.js进程已关闭" -ForegroundColor Green
} catch {
    Write-Host "⚠️ 没有运行的Node.js进程" -ForegroundColor Yellow
}

# 3. 清理Windows句柄
Write-Host "🧹 清理Windows句柄..." -ForegroundColor Yellow
# 强制垃圾回收
[System.GC]::Collect()
[System.GC]::WaitForPendingFinalizers()
Write-Host "✅ 句柄清理完成" -ForegroundColor Green

# 4. 检查并修复Redis连接配置
Write-Host "🔍 检查Redis连接配置..." -ForegroundColor Yellow
$redisConfigPath = ".env.development"
if (Test-Path $redisConfigPath) {
    $content = Get-Content $redisConfigPath
    if ($content -match "REDIS_HOST=redis") {
        Write-Host "✅ Redis配置正确" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Redis配置可能需要调整" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ Redis配置文件不存在" -ForegroundColor Red
}

# 5. 修复建议
Write-Host "💡 修复建议：" -ForegroundColor Cyan
Write-Host "1. 如果问题持续，请重启电脑（最彻底的解决方案）" -ForegroundColor White
Write-Host "2. 检查Docker服务是否正常运行" -ForegroundColor White
Write-Host "3. 确保Redis和MySQL服务已启动" -ForegroundColor White
Write-Host "4. 如果使用Docker，运行: docker-compose restart" -ForegroundColor White

# 6. 启动开发环境
Write-Host "🚀 是否立即启动开发环境？(y/n)" -ForegroundColor Cyan
$answer = Read-Host
if ($answer -eq 'y' -or $answer -eq 'Y') {
    Write-Host "🚀 启动开发环境..." -ForegroundColor Green
    & .\scripts\start-all-services.ps1
}

Write-Host "✅ 连接池修复完成！" -ForegroundColor Green