# 验证所有Dockerfile的-N参数已被移除

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "验证Dockerfile修复" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-consume-service",
    "ioedream-visitor-service"
)

$allFixed = $true

foreach ($service in $services) {
    $dockerfilePath = "microservices\$service\Dockerfile"
    
    if (Test-Path $dockerfilePath) {
        $content = Get-Content $dockerfilePath -Raw
        
        # 检查是否还存在 -N 参数
        if ($content -match 'mvn.*-N.*-DskipTests') {
            Write-Host "✗ $service - 仍包含-N参数" -ForegroundColor Red
            $allFixed = $false
        } else {
            Write-Host "✓ $service - 已正确修复" -ForegroundColor Green
        }
    } else {
        Write-Host "✗ $service - Dockerfile不存在" -ForegroundColor Red
        $allFixed = $false
    }
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan

if ($allFixed) {
    Write-Host "✓ 所有Dockerfile已正确修复!" -ForegroundColor Green
    Write-Host ""
    Write-Host "下一步: 执行Docker重建和部署" -ForegroundColor Yellow
    Write-Host "  .\scripts\rebuild-and-deploy-docker.ps1" -ForegroundColor Cyan
} else {
    Write-Host "✗ 部分Dockerfile仍需修复" -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查上述标记为失败的服务" -ForegroundColor Yellow
}

Write-Host ""
