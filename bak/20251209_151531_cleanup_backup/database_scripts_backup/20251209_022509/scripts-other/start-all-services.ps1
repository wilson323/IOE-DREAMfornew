# IOE-DREAM所有微服务启动脚本
# 按照依赖顺序启动服务

param(
    [switch]$CheckOnly = $false
)

$ErrorActionPreference = "Stop"

Write-Host "===== IOE-DREAM微服务启动管理 =====" -ForegroundColor Cyan

# 定义服务启动顺序
$services = @(
    @{Name="ioedream-config-service"; Port=8888; Order=1; Type="基础设施"},
    @{Name="ioedream-gateway-service"; Port=8080; Order=2; Type="基础设施"},
    @{Name="ioedream-common-service"; Port=8088; Order=3; Type="公共模块"},
    @{Name="ioedream-device-comm-service"; Port=8087; Order=3; Type="设备通讯"},
    @{Name="ioedream-auth-service"; Port=8083; Order=4; Type="认证服务"},
    @{Name="ioedream-identity-service"; Port=8084; Order=4; Type="身份服务"},
    @{Name="ioedream-oa-service"; Port=8089; Order=5; Type="OA服务"},
    @{Name="ioedream-access-service"; Port=8090; Order=5; Type="门禁服务"},
    @{Name="ioedream-attendance-service"; Port=8091; Order=5; Type="考勤服务"},
    @{Name="ioedream-video-service"; Port=8092; Order=5; Type="视频服务"},
    @{Name="ioedream-consume-service"; Port=8094; Order=5; Type="消费服务"},
    @{Name="ioedream-visitor-service"; Port=8095; Order=5; Type="访客服务"}
)

# 检查模式
if ($CheckOnly) {
    Write-Host "`n检查服务状态...`n" -ForegroundColor Yellow

    $runningCount = 0
    $stoppedCount = 0

    foreach ($service in $services) {
        $port = $service.Port
        $name = $service.Name

        try {
            $connection = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue
            if ($connection.TcpTestSucceeded) {
                Write-Host "✅ $name (端口$port) - 运行中" -ForegroundColor Green
                $runningCount++
            } else {
                Write-Host "⭕ $name (端口$port) - 未运行" -ForegroundColor Gray
                $stoppedCount++
            }
        } catch {
            Write-Host "⭕ $name (端口$port) - 未运行" -ForegroundColor Gray
            $stoppedCount++
        }
    }

    Write-Host "`n统计: $runningCount 个运行中, $stoppedCount 个未运行" -ForegroundColor Cyan
    exit 0
}

# 启动模式
Write-Host "`n准备启动 $($services.Count) 个微服务...`n" -ForegroundColor Yellow
Write-Host "按照依赖顺序启动服务:" -ForegroundColor Gray

# 按Order分组启动
$maxOrder = ($services | Measure-Object -Property Order -Maximum).Maximum

for ($order = 1; $order -le $maxOrder; $order++) {
    $groupServices = $services | Where-Object { $_.Order -eq $order }

    Write-Host "`n【第 $order 批次】启动 $($groupServices.Count) 个服务" -ForegroundColor Cyan

    foreach ($service in $groupServices) {
        $servicePath = "D:\IOE-DREAM\microservices\$($service.Name)"

        if (-not (Test-Path $servicePath)) {
            Write-Host "  ⚠️  $($service.Name) - 目录不存在" -ForegroundColor Yellow
            continue
        }

        Write-Host "  启动: $($service.Name) [$($service.Type)] 端口:$($service.Port)" -ForegroundColor Gray

        # 在新窗口启动服务
        $startCmd = "cd '$servicePath'; mvn spring-boot:run"
        Start-Process powershell -ArgumentList "-NoExit", "-Command", $startCmd

        Write-Host "    已在新窗口启动" -ForegroundColor Green
        Start-Sleep -Seconds 2
    }

    # 等待该批次服务启动
    if ($order -lt $maxOrder) {
        Write-Host "  等待第 $order 批次服务启动（30秒）..." -ForegroundColor Gray
        Start-Sleep -Seconds 30
    }
}

Write-Host "`n===== 所有服务启动完成 =====" -ForegroundColor Green
Write-Host "使用 -CheckOnly 参数检查服务状态" -ForegroundColor Yellow
Write-Host "示例: .\start-all-services.ps1 -CheckOnly" -ForegroundColor Gray

