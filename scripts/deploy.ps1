# IOE-DREAM 一键部署脚本 (PowerShell版本)
# 适用于Windows系统的快速部署

param(
    [switch]$SkipBuild,
    [switch]$Dev,
    [switch]$Prod,
    [switch]$Help
)

# 颜色定义
$Colors = @{
    Red = "Red"
    Green = "Green"
    Yellow = "Yellow"
    Blue = "Blue"
    Magenta = "Magenta"
    Cyan = "Cyan"
    White = "White"
}

# 项目配置
$ProjectName = "IOE-DREAM"
$ProjectVersion = "v1.0.0"
$ComposeFile = "docker-compose-all.yml"
$EnvFile = ".env.development"

# 日志函数
function Write-Log {
    param([string]$Message)
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $Message" -ForegroundColor $Colors.Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $Message" -ForegroundColor $Colors.Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $Message" -ForegroundColor $Colors.Red
}

function Write-Step {
    param([string]$StepNumber)
    Write-Host "============================================" -ForegroundColor $Colors.Blue
    Write-Host "  步骤 $StepNumber" -ForegroundColor $Colors.Blue
    Write-Host "============================================" -ForegroundColor $Colors.Blue
}

# 显示横幅
function Show-Banner {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor $Colors.Magenta
    Write-Host "    _   __      __   _    _     __   __     " -ForegroundColor $Colors.Magenta
    Write-Host "   / | / /___  / /_ | |  | |   / /  / /____  " -ForegroundColor $Colors.Magenta
    Write-Host "  /  |/ / __ \/ __/ | |  | |  / /  / __/ _ \ " -ForegroundColor $Colors.Magenta
    Write-Host " / /|  / /_/ / /_   | |  | | / /__/ /_/  __/ " -ForegroundColor $Colors.Magenta
    Write-Host "/_/ |_/\____/\__/   |_|  |_| |____/____/\___/  " -ForegroundColor $Colors.Magenta
    Write-Host ""
    Write-Host "       智慧园区一卡通管理平台" -ForegroundColor $Colors.Magenta
    Write-Host "         Docker 一键部署工具" -ForegroundColor $Colors.Magenta
    Write-Host "============================================" -ForegroundColor $Colors.Magenta
    Write-Host ""
}

# 检查系统要求
function Test-Requirements {
    Write-Step "检查系统要求"

    # 检查操作系统
    $OS = if ($IsWindows) { "Windows" } elseif ($IsLinux) { "Linux" } elseif ($IsMacOS) { "macOS" } else { "未知" }
    Write-Log "操作系统: $OS"

    # 检查Docker
    try {
        $null = Get-Command docker -ErrorAction Stop
        $null = docker info 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "Docker服务未启动"
        }
        Write-Log "Docker: 已安装并运行"
    }
    catch {
        Write-Error "Docker 未安装或未启动"
        Write-Host "安装指南: https://docs.docker.com/get-docker/" -ForegroundColor $Colors.Cyan
        exit 1
    }

    # 检查Docker Compose
    try {
        $null = Get-Command docker-compose -ErrorAction Stop
        Write-Log "Docker Compose: 已安装"
    }
    catch {
        try {
            $version = docker compose version 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-Log "Docker Compose: 已安装 (新版)"
            } else {
                throw "Docker Compose未安装"
            }
        }
        catch {
            Write-Error "Docker Compose 未安装"
            exit 1
        }
    }

    # 检查系统内存
    $ComputerInfo = Get-ComputerInfo
    $TotalMemory = [math]::Round($ComputerInfo.TotalPhysicalMemory / 1MB, 0)
    $AvailableMemory = [math]::Round($ComputerInfo.FreePhysicalMemory / 1MB, 0)
    Write-Log "总内存: ${TotalMemory}MB, 可用内存: ${AvailableMemory}MB"

    if ($TotalMemory -lt 8192) {
        Write-Warn "系统内存少于8GB，可能影响性能"
    }

    # 检查磁盘空间
    $Drive = Get-WmiObject -Class Win32_LogicalDisk -Filter "DeviceID='C:'"
    $FreeSpace = [math]::Round($Drive.FreeSpace / 1GB, 1)
    Write-Log "可用磁盘空间: ${FreeSpace}GB"

    if ($FreeSpace -lt 10) {
        Write-Error "磁盘空间不足10GB，无法部署"
        exit 1
    }

    Write-Log "✅ 系统要求检查通过"
}

# 检查项目文件
function Test-ProjectFiles {
    Write-Step "检查项目文件"

    # 检查关键文件
    $RequiredFiles = @(
        "docker-compose-all.yml",
        "scripts/docker-build.ps1",
        ".env.development",
        "microservices/pom.xml",
        "microservices/microservices-common/pom.xml"
    )

    foreach ($file in $RequiredFiles) {
        if (-not (Test-Path $file)) {
            Write-Error "缺少关键文件: $file"
            exit 1
        }
    }

    # 检查微服务目录
    $Services = @(
        "gateway-service",
        "common-service",
        "device-comm-service",
        "oa-service",
        "access-service",
        "attendance-service",
        "video-service",
        "consume-service",
        "visitor-service"
    )

    foreach ($service in $Services) {
        $ServiceDir = "microservices/ioedream-${service}"
        if (-not (Test-Path $ServiceDir)) {
            Write-Error "缺少微服务目录: $ServiceDir"
            exit 1
        }

        $Dockerfile = "$ServiceDir/Dockerfile"
        if (-not (Test-Path $Dockerfile)) {
            Write-Error "缺少Dockerfile: $Dockerfile"
            exit 1
        }
    }

    Write-Log "✅ 项目文件检查通过"
}

# 配置环境
function Initialize-Environment {
    Write-Step "配置环境"

    # 复制环境变量文件
    if (-not (Test-Path ".env")) {
        Copy-Item $EnvFile ".env"
        Write-Log "已创建环境变量文件 .env"
    } else {
        Write-Warn "环境变量文件 .env 已存在，跳过创建"
    }

    # 创建数据目录
    $DataDirs = @(
        "data/mysql",
        "data/redis",
        "data/nacos",
        "logs/nginx",
        "logs/nacos",
        "logs/services"
    )

    foreach ($dir in $DataDirs) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
    }

    Write-Log "✅ 环境配置完成"
}

# 构建Docker镜像
function Build-DockerImages {
    Write-Step "构建Docker镜像"

    if (-not (Test-Path "scripts/docker-build.ps1")) {
        Write-Error "构建脚本不存在: scripts/docker-build.ps1"
        exit 1
    }

    Write-Log "开始构建Docker镜像..."

    try {
        & ".\scripts\docker-build.ps1"
        if ($LASTEXITCODE -eq 0) {
            Write-Log "✅ Docker镜像构建成功"
        } else {
            throw "构建脚本执行失败"
        }
    }
    catch {
        Write-Error "Docker镜像构建失败: $($_.Exception.Message)"
        exit 1
    }
}

# 自动检测并初始化数据库
function Initialize-Databases {
    Write-Step "自动检测并初始化数据库"

    # 1. 启动MySQL和Redis
    Write-Log "启动MySQL和Redis..."
    docker-compose -f $ComposeFile up -d mysql redis

    if ($LASTEXITCODE -ne 0) {
        Write-Error "启动基础设施失败"
        exit 1
    }

    # 2. 等待MySQL就绪
    Write-Log "等待MySQL就绪..."
    $maxWait = 120
    $waited = 0
    $mysqlReady = $false

    while ($waited -lt $maxWait) {
        Start-Sleep -Seconds 5
        $waited += 5
        
        $mysqlHealth = docker inspect ioedream-mysql --format='{{.State.Health.Status}}' 2>&1
        if ($mysqlHealth -eq "healthy") {
            Write-Log "✅ MySQL已就绪 ($waited秒)"
            $mysqlReady = $true
            break
        }
        
        Write-Log "  等待中... ($waited/$maxWait秒)"
    }

    if (-not $mysqlReady) {
        Write-Error "MySQL启动超时"
        exit 1
    }

    # 3. 检测nacos数据库
    Write-Log "检测nacos数据库..."
    $dbCheck = docker exec ioedream-mysql mysql -uroot -proot -e "SHOW DATABASES LIKE 'nacos';" 2>&1
    $nacosDbExists = $dbCheck | Select-String "nacos"
    
    $needInitNacos = $false
    
    if ($nacosDbExists) {
        Write-Log "✓ nacos数据库已存在"
        
        # 检查表数量
        $tableCheck = docker exec ioedream-mysql mysql -uroot -proot -e "SELECT COUNT(*) as cnt FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';" 2>&1
        $tableCount = [regex]::Match($tableCheck, '\d+').Value
        
        if ($tableCount -and [int]$tableCount -gt 0) {
            Write-Log "✅ nacos数据库已初始化 (表数量: $tableCount)"
        } else {
            Write-Warn "nacos数据库为空，需要初始化表结构"
            $needInitNacos = $true
        }
    } else {
        Write-Warn "nacos数据库不存在，需要创建并初始化"
        $needInitNacos = $true
    }
    
    # 4. 自动初始化nacos数据库（如果需要）
    if ($needInitNacos) {
        Write-Log "自动初始化nacos数据库..."
        
        $nacosSchema = "deployment\mysql\init\nacos-schema.sql"
        if (-not (Test-Path $nacosSchema)) {
            Write-Error "找不到nacos初始化脚本: $nacosSchema"
            exit 1
        }
        
        try {
            # 创建数据库（如果不存在）
            Write-Log "创建nacos数据库..."
            docker exec ioedream-mysql mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS nacos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>&1 | Out-Null
            
            # 初始化表结构
            Write-Log "执行SQL初始化脚本..."
            Get-Content $nacosSchema -Raw | docker exec -i ioedream-mysql mysql -uroot -proot nacos 2>&1 | Out-Null
            
            if ($LASTEXITCODE -eq 0) {
                Write-Log "✅ nacos数据库初始化成功"
                
                # 验证
                $tableCheck = docker exec ioedream-mysql mysql -uroot -proot -e "SELECT COUNT(*) as cnt FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';" 2>&1
                $tableCount = [regex]::Match($tableCheck, '\d+').Value
                Write-Log "  表数量: $tableCount"
            } else {
                Write-Error "初始化失败，错误代码: $LASTEXITCODE"
                exit 1
            }
        }
        catch {
            Write-Error "初始化异常: $($_.Exception.Message)"
            exit 1
        }
    }
}

# 启动服务
function Start-Services {
    Write-Step "启动服务"

    # 检查端口占用
    $Ports = @(80, 8080, 8848, 3306, 6379)
    foreach ($port in $Ports) {
        $Connections = netstat -ano | findstr ":$port"
        if ($Connections) {
            Write-Warn "端口 $port 已被占用，可能导致服务启动失败"
        }
    }

    Write-Log "启动所有服务..."

    try {
        docker-compose -f $ComposeFile up -d
        if ($LASTEXITCODE -eq 0) {
            Write-Log "✅ 服务启动成功"
        } else {
            throw "服务启动失败"
        }
    }
    catch {
        Write-Error "启动服务时出错: $($_.Exception.Message)"
        exit 1
    }
}

# 等待服务就绪
function Wait-ServicesReady {
    Write-Step "等待服务启动"

    Write-Log "等待基础设施服务启动..."
    Start-Sleep -Seconds 30

    Write-Log "等待应用服务启动..."
    Start-Sleep -Seconds 60

    Write-Log "等待所有服务就绪..."
    Start-Sleep -Seconds 30

    # 检查关键服务状态
    $CriticalServices = @(
        "ioedream-mysql",
        "ioedream-redis",
        "ioedream-nacos",
        "ioedream-gateway-service"
    )

    foreach ($service in $CriticalServices) {
        $Status = docker ps --filter "name=$service" --format "{{.Status}}"
        if ($Status -match "Up") {
            Write-Log "✅ $service 运行正常"
        } else {
            Write-Warn "⚠️ $service 状态异常: $Status"
        }
    }
}

# 验证部署
function Test-Deployment {
    Write-Step "验证部署"

    Write-Log "执行健康检查..."

    # 检查HTTP响应
    $MaxRetries = 10
    $RetryCount = 0

    while ($RetryCount -lt $MaxRetries) {
        try {
            $Response = Invoke-WebRequest -Uri "http://localhost/health" -UseBasicParsing -TimeoutSec 10
            if ($Response.StatusCode -eq 200) {
                Write-Log "✅ 健康检查通过"
                break
            }
        }
        catch {
            if ($RetryCount -eq ($MaxRetries - 1)) {
                Write-Error "健康检查失败"
                return $false
            }
            Write-Log "等待服务启动... ($($RetryCount + 1)/$MaxRetries)"
            Start-Sleep -Seconds 10
            $RetryCount++
        }
    }

    # 检查服务端口
    Write-Log "检查服务端口..."
    $ServicePorts = @(
        @{Port = 80; Name = "Nginx"},
        @{Port = 8080; Name = "Gateway"},
        @{Port = 8848; Name = "Nacos"},
        @{Port = 3306; Name = "MySQL"},
        @{Port = 6379; Name = "Redis"}
    )

    foreach ($ServicePort in $ServicePorts) {
        try {
            $TCPClient = New-Object System.Net.Sockets.TcpClient
            $TCPClient.Connect("localhost", $ServicePort.Port)
            $TCPClient.Close()
            Write-Log "✅ $($ServicePort.Name) 端口 $($ServicePort.Port) 可访问"
        }
        catch {
            Write-Warn "⚠️ $($ServicePort.Name) 端口 $($ServicePort.Port) 不可访问"
        }
    }

    return $true
}

# 显示部署结果
function Show-DeploymentResult {
    Write-Step "部署完成"

    Write-Host ""
    Write-Host "🎉 IOE-DREAM 部署成功！" -ForegroundColor $Colors.Cyan
    Write-Host ""
    Write-Host "============================================" -ForegroundColor $Colors.Cyan
    Write-Host "📱 访问地址" -ForegroundColor $Colors.Cyan
    Write-Host "============================================" -ForegroundColor $Colors.Cyan
    Write-Host "管理后台:       http://localhost:80"
    Write-Host "API网关:        http://localhost:8080"
    Write-Host "Nacos控制台:    http://localhost:8848/nacos"
    Write-Host ""
    Write-Host "============================================" -ForegroundColor $Colors.Cyan
    Write-Host "🔑 默认账号信息" -ForegroundColor $Colors.Cyan
    Write-Host "============================================" -ForegroundColor $Colors.Cyan
    Write-Host "系统管理员:     admin / 123456"
    Write-Host "Nacos控制台:    nacos / nacos"
    Write-Host "MySQL数据库:    root / root"
    Write-Host "Redis缓存:      (无密码)"
    Write-Host ""
    Write-Host "============================================" -ForegroundColor $Colors.Cyan
    Write-Host "🔧 常用命令" -ForegroundColor $Colors.Cyan
    Write-Host "============================================" -ForegroundColor $Colors.Cyan
    Write-Host "查看服务状态:   docker-compose -f $ComposeFile ps"
    Write-Host "查看日志:       docker-compose -f $ComposeFile logs -f"
    Write-Host "停止服务:       docker-compose -f $ComposeFile down"
    Write-Host "重启服务:       docker-compose -f $ComposeFile restart"
    Write-Host ""
    Write-Host "============================================" -ForegroundColor $Colors.Cyan
    Write-Host "📚 更多帮助" -ForegroundColor $Colors.Cyan
    Write-Host "============================================" -ForegroundColor $Colors.Cyan
    Write-Host "完整文档:       ./DOCKER_DEPLOYMENT_GUIDE.md"
    Write-Host "快速指南:       ./QUICK_DOCKER_DEPLOYMENT.md"
    Write-Host "问题反馈:       https://github.com/your-org/IOE-DREAM/issues"
    Write-Host ""
}

# 显示帮助信息
function Show-Help {
    Write-Host "IOE-DREAM Docker 一键部署脚本" -ForegroundColor $Colors.Green
    Write-Host ""
    Write-Host "用法:" -ForegroundColor $Colors.Blue
    Write-Host "    .\scripts\deploy.ps1 [选项]"
    Write-Host ""
    Write-Host "选项:" -ForegroundColor $Colors.Blue
    Write-Host "    -SkipBuild     跳过镜像构建"
    Write-Host "    -Dev           使用开发环境"
    Write-Host "    -Prod          使用生产环境"
    Write-Host "    -Help          显示此帮助信息"
    Write-Host ""
    Write-Host "示例:" -ForegroundColor $Colors.Blue
    Write-Host "    .\scripts\deploy.ps1"
    Write-Host "    .\scripts\deploy.ps1 -Prod"
    Write-Host "    .\scripts\deploy.ps1 -SkipBuild"
    Write-Host ""
}

# 主函数
function Main {
    # 显示横幅
    Show-Banner

    Write-Log "开始部署 $ProjectName $ProjectVersion..."

    # 执行部署步骤
    Test-Requirements
    Test-ProjectFiles
    Initialize-Environment

    if (-not $SkipBuild) {
        Build-DockerImages
    } else {
        Write-Warn "跳过镜像构建"
    }

    # 自动检测并初始化数据库（关键步骤）
    Initialize-Databases

    Start-Services
    Wait-ServicesReady

    if (Test-Deployment) {
        Show-DeploymentResult
        Write-Log "🎉 部署完成！享受使用 IOE-DREAM 吧！"
    } else {
        Write-Error "部署验证失败"
        exit 1
    }
}

# 错误处理
trap {
    Write-Error "脚本执行失败: $($_.Exception.Message)"
    exit 1
}

# 检查帮助选项
if ($Help) {
    Show-Help
    return
}

# 解析命令行参数
if ($Prod) {
    $EnvFile = ".env.production"
    $ComposeFile = "docker-compose-production.yml"
}
if ($Dev) {
    $EnvFile = ".env.development"
    $ComposeFile = "docker-compose-all.yml"
}

# 记录当前目录
$OriginalLocation = Get-Location

try {
    # 执行主函数
    Main
}
finally {
    # 恢复到原始目录
    Set-Location $OriginalLocation
}