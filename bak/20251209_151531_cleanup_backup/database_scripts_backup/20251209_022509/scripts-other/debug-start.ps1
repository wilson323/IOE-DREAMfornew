# 调试启动脚本 - 找出根本问题
param(
    [switch]$BackendOnly = $false
)

# 设置编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# 不要使用Stop，使用Continue
$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  调试启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ProjectRoot = "D:\IOE-DREAM"
Write-Host "项目根目录: $ProjectRoot" -ForegroundColor Yellow

# 测试服务配置
$backendServices = @(
    @{Name="ioedream-gateway-service"; Port=8080; Order=1; Type="基础设施"; Path="$ProjectRoot\microservices\ioedream-gateway-service"}
)

Write-Host "服务数量: $($backendServices.Count)" -ForegroundColor Yellow

foreach ($service in $backendServices) {
    Write-Host "检查服务: $($service.Name)" -ForegroundColor Cyan
    Write-Host "  路径: $($service.Path)" -ForegroundColor Gray
    
    if (Test-Path $service.Path) {
        Write-Host "  ✓ 目录存在" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 目录不存在" -ForegroundColor Red
    }
    
    # 检查Maven
    Write-Host "  检查Maven..." -ForegroundColor Gray
    $mvnCheck = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mvnCheck) {
        Write-Host "  ✓ Maven已安装" -ForegroundColor Green
        $mvnVersion = & mvn -version 2>&1 | Select-Object -First 1
        Write-Host "    版本: $mvnVersion" -ForegroundColor Gray
    } else {
        Write-Host "  ✗ Maven未安装" -ForegroundColor Red
    }
    
    # 尝试启动
    Write-Host "  准备启动服务..." -ForegroundColor Yellow
    $servicePath = $service.Path
    $serviceName = $service.Name
    
    $tempScript = [System.IO.Path]::GetTempFileName() + ".ps1"
    Write-Host "  临时脚本: $tempScript" -ForegroundColor Gray
    
    $scriptContent = @"
cd '$servicePath'
Write-Host 'Starting $serviceName...' -ForegroundColor Cyan
mvn spring-boot:run
"@
    
    try {
        [System.IO.File]::WriteAllText($tempScript, $scriptContent, [System.Text.Encoding]::UTF8)
        Write-Host "  ✓ 临时脚本已创建" -ForegroundColor Green
        
        Write-Host "  启动新窗口..." -ForegroundColor Yellow
        Start-Process powershell.exe -ArgumentList "-NoExit", "-File", $tempScript
        Write-Host "  ✓ 新窗口已启动" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ 启动失败: $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "调试脚本执行完成" -ForegroundColor Green
