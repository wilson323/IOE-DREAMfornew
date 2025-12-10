# 修复 ioedream-attendance-service 构建路径问题
# 确保 microservices-common 模块已正确构建并安装到本地Maven仓库

# 强制设置UTF-8编码 - 必须在脚本最开始执行
$PSDefaultParameterValues['*:Encoding'] = 'utf8'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "修复 ioedream-attendance-service 构建路径" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 步骤1: 构建 microservices-common 模块
Write-Host "`n[步骤1] 构建 microservices-common 模块..." -ForegroundColor Yellow
Set-Location "D:\IOE-DREAM\microservices\microservices-common"
mvn clean install -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "`n错误: microservices-common 模块构建失败!" -ForegroundColor Red
    exit 1
}

Write-Host "`n✓ microservices-common 模块构建成功" -ForegroundColor Green

# 步骤2: 刷新 ioedream-attendance-service 依赖
Write-Host "`n[步骤2] 刷新 ioedream-attendance-service 依赖..." -ForegroundColor Yellow
Set-Location "D:\IOE-DREAM\microservices\ioedream-attendance-service"
mvn clean compile -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "`n错误: ioedream-attendance-service 编译失败!" -ForegroundColor Red
    exit 1
}

Write-Host "`n✓ ioedream-attendance-service 编译成功" -ForegroundColor Green

# 步骤3: 验证 EmployeeEntity 类是否存在
Write-Host "`n[步骤3] 验证 EmployeeEntity 类..." -ForegroundColor Yellow
$employeeEntityPath = "D:\IOE-DREAM\microservices\microservices-common\src\main\java\net\lab1024\sa\common\hr\entity\EmployeeEntity.java"
if (Test-Path $employeeEntityPath) {
    Write-Host "✓ EmployeeEntity 类文件存在: $employeeEntityPath" -ForegroundColor Green
} else {
    Write-Host "✗ EmployeeEntity 类文件不存在!" -ForegroundColor Red
    exit 1
}

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "构建路径修复完成!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "`n提示: 如果IDE仍然报错，请执行以下操作:" -ForegroundColor Yellow
Write-Host "1. 在IDE中右键点击项目 -> Maven -> Reload Project" -ForegroundColor White
Write-Host "2. 或者执行: File -> Invalidate Caches / Restart" -ForegroundColor White
Write-Host "=========================================" -ForegroundColor Cyan
