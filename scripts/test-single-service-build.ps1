# 测试单个服务的Docker构建
# 用于快速验证修复方案是否有效

param(
    [Parameter(Mandatory=$false)]
    [string]$ServiceName = "gateway-service"
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "单服务Docker构建测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试服务: $ServiceName" -ForegroundColor Yellow
Write-Host ""

# 检查Docker是否运行
Write-Host "检查Docker状态..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version
    Write-Host "✅ Docker已安装: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker未安装或未运行" -ForegroundColor Red
    exit 1
}

# 检查docker-compose是否可用
try {
    $composeVersion = docker-compose --version
    Write-Host "✅ Docker Compose已安装: $composeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker Compose未安装" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 检查Dockerfile是否存在
$dockerfilePath = "microservices\ioedream-$ServiceName\Dockerfile"
if (-not (Test-Path $dockerfilePath)) {
    Write-Host "❌ 错误: 找不到Dockerfile: $dockerfilePath" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 找到Dockerfile: $dockerfilePath" -ForegroundColor Green

# 验证Dockerfile使用V5方案
$dockerfileContent = Get-Content $dockerfilePath -Raw
if ($dockerfileContent -match "cp pom\.xml pom-original\.xml" -and 
    $dockerfileContent -match "awk '/<modules>/,/<\/modules>/ \{next\} \{print\}' pom-original\.xml > pom\.xml") {
    Write-Host "✅ Dockerfile使用V5方案（直接替换pom.xml）" -ForegroundColor Green
} else {
    Write-Host "❌ Dockerfile未使用V5方案" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "开始构建测试..." -ForegroundColor Yellow
Write-Host "命令: docker-compose -f docker-compose-all.yml build --no-cache $ServiceName" -ForegroundColor Cyan
Write-Host ""

# 执行构建
$buildOutput = docker-compose -f docker-compose-all.yml build --no-cache $ServiceName 2>&1

# 检查构建结果
if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ 构建成功！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "修复方案验证通过:" -ForegroundColor Cyan
    Write-Host "  - V5方案（直接替换pom.xml）有效" -ForegroundColor White
    Write-Host "  - 无Maven模块检查错误" -ForegroundColor White
    Write-Host "  - 服务镜像构建成功" -ForegroundColor White
    Write-Host ""
    Write-Host "可以继续构建所有服务:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose-all.yml build --no-cache" -ForegroundColor White
    exit 0
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ 构建失败！" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "构建输出:" -ForegroundColor Yellow
    Write-Host $buildOutput -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查错误信息并修复" -ForegroundColor Yellow
    exit 1
}
