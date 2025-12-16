# =====================================================
# SonarQube扫描脚本
# 版本: v1.0.0
# 描述: 运行SonarQube扫描，获取重复代码报告
# 创建时间: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$SonarHostUrl = "http://localhost:9000",
    
    [Parameter(Mandatory=$false)]
    [string]$SonarToken = "",
    
    [Parameter(Mandatory=$false)]
    [string]$ProjectKey = "ioe-dream"
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "SonarQube代码扫描" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 检查SonarQube服务状态
Write-Host "[1/4] 检查SonarQube服务状态..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$SonarHostUrl/api/system/status" -Method GET -ErrorAction Stop
    Write-Host "[OK] SonarQube服务正常" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] SonarQube服务不可用: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "请确保SonarQube服务已启动: docker run -d --name sonarqube -p 9000:9000 sonarqube:community" -ForegroundColor Yellow
    exit 1
}

# 切换到项目根目录
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

# 切换到microservices目录
Set-Location "microservices"

# 检查sonar-project.properties
Write-Host "[2/4] 检查SonarQube配置..." -ForegroundColor Yellow
$sonarConfigPath = Join-Path $projectRoot "sonar-project.properties"
if (-not (Test-Path $sonarConfigPath)) {
    Write-Host "[WARN] 未找到sonar-project.properties，将使用默认配置" -ForegroundColor Yellow
} else {
    Write-Host "[OK] 找到SonarQube配置文件" -ForegroundColor Green
}

# 构建Maven命令
Write-Host "[3/4] 运行SonarQube扫描..." -ForegroundColor Yellow
$mvnCommand = "mvn sonar:sonar -Dsonar.projectKey=$ProjectKey -Dsonar.host.url=$SonarHostUrl"

if ($SonarToken -ne "") {
    $mvnCommand += " -Dsonar.login=$SonarToken"
}

Write-Host "命令: $mvnCommand" -ForegroundColor Gray
Write-Host ""

# 运行扫描
Invoke-Expression $mvnCommand

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] SonarQube扫描失败" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[4/4] 扫描完成，查看报告..." -ForegroundColor Yellow
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "[OK] SonarQube扫描完成" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "查看报告:" -ForegroundColor White
Write-Host "  访问SonarQube UI: $SonarHostUrl" -ForegroundColor Gray
Write-Host "  项目Key: $ProjectKey" -ForegroundColor Gray
Write-Host ""
Write-Host "查看重复代码:" -ForegroundColor White
Write-Host "  1. 登录SonarQube UI" -ForegroundColor Gray
Write-Host "  2. 选择项目: $ProjectKey" -ForegroundColor Gray
Write-Host "  3. 导航到: Measures → Duplications" -ForegroundColor Gray
Write-Host "  4. 或导航到: Code → Duplications" -ForegroundColor Gray
Write-Host ""

