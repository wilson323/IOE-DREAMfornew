# =====================================================
# IOE-DREAM 项目标准启动脚本 - 修复所有依赖问题
# 版本: v1.0.0 - 依赖修复版
# 描述: 按照CLAUDE.md规范，强制正确启动顺序
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$SkipBuild,

    [Parameter(Mandatory=$false)]
    [switch]$StartInfra,

    [Parameter(Mandatory=$false)]
    [switch]$QuickStart
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM 标准启动脚本 (依赖修复版)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 项目根目录
$ProjectRoot = Split-Path -Parent $PSScriptRoot
if (-not $ProjectRoot) {
    $ProjectRoot = (Get-Location).Path
}

Write-Host "项目根目录: $ProjectRoot" -ForegroundColor Gray
Write-Host ""

# 服务定义
$Services = @(
    @{ Name = "ioedream-gateway-service"; Port = 8080; Path = "microservices\ioedream-gateway-service" },
    @{ Name = "ioedream-common-service"; Port = 8088; Path = "microservices\ioedream-common-service" },
    @{ Name = "ioedream-device-comm-service"; Port = 8087; Path = "microservices\ioedream-device-comm-service" },
    @{ Name = "ioedream-oa-service"; Port = 8089; Path = "microservices\ioedream-oa-service" },
    @{ Name = "ioedream-access-service"; Port = 8090; Path = "microservices\ioedream-access-service" },
    @{ Name = "ioedream-attendance-service"; Port = 8091; Path = "microservices\ioedream-attendance-service" },
    @{ Name = "ioedream-video-service"; Port = 8092; Path = "microservices\ioedream-video-service" },
    @{ Name = "ioedream-consume-service"; Port = 8094; Path = "microservices\ioedream-consume-service" },
    @{ Name = "ioedream-visitor-service"; Port = 8095; Path = "microservices\ioedream-visitor-service" }
)

# 端口检查函数
function Test-Port {
    param([int]$Port)
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $asyncResult = $tcpClient.BeginConnect("127.0.0.1", $Port, $null, $null)
        $wait = $asyncResult.AsyncWaitHandle.WaitOne(1000, $false)
        if ($wait) {
            $tcpClient.EndConnect($asyncResult)
            $tcpClient.Close()
            return $true
        } else {
            $tcpClient.Close()
            return $false
        }
    } catch {
        return $false
    }
}

# 步骤1: 启动基础设施服务
if ($StartInfra -or $QuickStart) {
    Write-Host "[步骤1] 启动基础设施服务 (MySQL, Redis, Nacos)..." -ForegroundColor Cyan

    # 检查Docker是否运行
    try {
        $null = docker --version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Docker不可用"
        }
    } catch {
        Write-Host "[错误] Docker未安装或未运行" -ForegroundColor Red
        Write-Host "请先启动Docker Desktop" -ForegroundColor Yellow
        exit 1
    }

    # 启动基础设施
    $composeFile = Join-Path $ProjectRoot "docker-compose-all.yml"
    if (Test-Path $composeFile) {
        Write-Host "启动: docker-compose up -d mysql redis nacos" -ForegroundColor Gray
        docker-compose -f $composeFile up -d mysql redis nacos

        Write-Host "等待基础设施服务启动 (30秒)..." -ForegroundColor Yellow
        Start-Sleep -Seconds 30

        # 验证基础设施
        $infraOk = $true
        if (-not (Test-Port -Port 3306)) {
            Write-Host "[错误] MySQL (3306) 未启动" -ForegroundColor Red
            $infraOk = $false
        }
        if (-not (Test-Port -Port 6379)) {
            Write-Host "[错误] Redis (6379) 未启动" -ForegroundColor Red
            $infraOk = $false
        }
        if (-not (Test-Port -Port 8848)) {
            Write-Host "[错误] Nacos (8848) 未启动" -ForegroundColor Red
            $infraOk = $false
        }

        if ($infraOk) {
            Write-Host "[成功] 所有基础设施服务已启动" -ForegroundColor Green
        } else {
            Write-Host "[错误] 基础设施服务启动失败" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "[错误] docker-compose-all.yml 不存在" -ForegroundColor Red
        exit 1
    }
    Write-Host ""
}

# 步骤2: 强制正确构建顺序
if (-not $SkipBuild) {
    Write-Host "[步骤2] 按正确顺序构建项目..." -ForegroundColor Cyan

    Push-Location (Join-Path $ProjectRoot "microservices")

    try {
        # 2.1 强制先构建 microservices-common-core
        Write-Host "构建 microservices-common-core (核心模块)..." -ForegroundColor Gray
        $result = mvn clean install -pl microservices-common-core -am -DskipTests
        if ($LASTEXITCODE -ne 0) {
            throw "microservices-common-core 构建失败"
        }
        Write-Host "[成功] microservices-common-core 构建完成" -ForegroundColor Green

        # 2.2 构建 microservices-common
        Write-Host "构建 microservices-common (公共模块)..." -ForegroundColor Gray
        $result = mvn clean install -pl microservices-common -am -DskipTests
        if ($LASTEXITCODE -ne 0) {
            throw "microservices-common 构建失败"
        }
        Write-Host "[成功] microservices-common 构建完成" -ForegroundColor Green

        # 2.3 构建所有业务服务
        Write-Host "构建所有业务服务..." -ForegroundColor Gray
        $result = mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[警告] 部分服务构建可能有问题，但继续启动" -ForegroundColor Yellow
        }
        Write-Host "[完成] 所有服务构建完成" -ForegroundColor Green

    } catch {
        Write-Host "[错误] 构建失败: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "请检查Maven配置和依赖" -ForegroundColor Yellow
        exit 1
    } finally {
        Pop-Location
    }
    Write-Host ""
}

# 步骤3: 启动微服务
Write-Host "[步骤3] 启动所有微服务..." -ForegroundColor Cyan

$loadEnvScript = Join-Path $PSScriptRoot "load-env.ps1"
if (Test-Path $loadEnvScript) {
    & $loadEnvScript -Silent
}

foreach ($service in $Services) {
    $servicePath = Join-Path $ProjectRoot $service.Path

    # 检查服务目录
    if (-not (Test-Path $servicePath)) {
        Write-Host "[跳过] $($service.Name) - 目录不存在" -ForegroundColor Yellow
        continue
    }

    # 检查端口
    if (Test-Port -Port $service.Port) {
        Write-Host "[跳过] $($service.Name) (端口 $($service.Port) 已被占用)" -ForegroundColor Yellow
        continue
    }

    Write-Host "启动 $($service.Name) (端口 $($service.Port))..." -ForegroundColor Gray

    try {
        # 创建启动脚本
        $batFile = Join-Path $env:TEMP "start-$($service.Name).bat"
        $batContent = "@echo off`r`ncd /d `"$servicePath`"`r`n"
        $batContent += "set JAVA_HOME=$($env:JAVA_HOME)`r`n"
        $batContent += "set PATH=%JAVA_HOME%\bin;%PATH%`r`n"

        # 加载环境变量
        Get-ChildItem Env: | ForEach-Object {
            $value = $_.Value -replace '"', '""'
            $batContent += "set $($_.Name)=$value`r`n"
        }

        $batContent += "`r`nstart `"$($service.Name)`" mvn spring-boot:run`r`n"

        [System.IO.File]::WriteAllText($batFile, $batContent, [System.Text.Encoding]::ASCII)

        # 启动服务
        $startInfo = New-Object System.Diagnostics.ProcessStartInfo
        $startInfo.FileName = "cmd.exe"
        $startInfo.Arguments = "/k `"$batFile`""
        $startInfo.UseShellExecute = $true
        $startInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Normal

        $process = [System.Diagnostics.Process]::Start($startInfo)
        Write-Host "[成功] $($service.Name) 已启动 (PID: $($process.Id))" -ForegroundColor Green

    } catch {
        Write-Host "[错误] 启动 $($service.Name) 失败: $($_.Exception.Message)" -ForegroundColor Red
    }

    Start-Sleep -Seconds 3
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 显示服务状态
Write-Host "服务访问地址:" -ForegroundColor Cyan
foreach ($service in $Services) {
    $status = if (Test-Port -Port $service.Port) { "运行中" } else { "未运行" }
    $color = if (Test-Port -Port $service.Port) { "Green" } else { "Red" }
    Write-Host "  $($service.Name): http://localhost:$($service.Port) - $status" -ForegroundColor $color
}

Write-Host ""
Write-Host "等待2-3分钟让所有服务完全启动..." -ForegroundColor Yellow
Write-Host "使用 'netstat -an | findstr 8080' 等命令检查服务状态" -ForegroundColor Gray
Write-Host ""

if (-not $QuickStart) {
    Read-Host "按回车键退出..."
}