# ============================================================
# IOE-DREAM Zipkin分布式追踪系统部署脚本 (PowerShell版本)
#
# @Author:    IOE-DREAM Team
# @Date:      2025-12-09
# @Description: 自动化部署和管理Zipkin分布式追踪系统
# ============================================================

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("deploy", "start", "stop", "restart", "status", "logs", "health", "clean", "backup", "help")]
    [string]$Action = "deploy",

    [Parameter(Mandatory=$false)]
    [string]$Service = ""
)

# 颜色定义
$Colors = @{
    Red = "Red"
    Green = "Green"
    Yellow = "Yellow"
    Blue = "Blue"
    Cyan = "Cyan"
    White = "White"
}

# 日志函数
function Write-Log {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host "[$([datetime]::Now.ToString('HH:mm:ss'))] $Message" -ForegroundColor $Colors[$Color]
}

function Write-Info {
    param([string]$Message)
    Write-Log $Message "Blue"
}

function Write-Success {
    param([string]$Message)
    Write-Log $Message "Green"
}

function Write-Warning {
    param([string]$Message)
    Write-Log $Message "Yellow"
}

function Write-Error {
    param([string]$Message)
    Write-Log $Message "Red"
}

# 项目配置
$ProjectName = "ioedream"
$DeploymentDir = Join-Path $PSScriptRoot "deployments\zipkin"
$DockerComposeFile = Join-Path $DeploymentDir "docker-compose.yml"
$ZipkinConfig = Join-Path $DeploymentDir "zipkin-config.properties"

# 服务端口配置
$ZipkinWebPort = 9411
$ZipkinApiPort = 9410
$ElasticsearchPort = 9200
$KibanaPort = 5601
$PrometheusPort = 9943

# 健康检查URLs
$ZipkinHealthUrl = "http://localhost:$ZipkinWebPort/health"
$ElasticsearchHealthUrl = "http://localhost:$ElasticsearchPort/_cluster/health"
$KibanaHealthUrl = "http://localhost:$KibanaPort/api/status"

# 检查Docker环境
function Test-DockerEnvironment {
    Write-Info "检查Docker环境..."

    try {
        # 检查Docker是否安装
        $null = Get-Command docker -ErrorAction Stop
        Write-Success "Docker已安装"

        # 检查Docker Compose是否安装
        $null = Get-Command docker-compose -ErrorAction Stop
        Write-Success "Docker Compose已安装"

        # 检查Docker服务是否运行
        $dockerInfo = docker info 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Docker服务运行正常"
        } else {
            Write-Error "Docker服务未运行，请启动Docker服务"
            exit 1
        }
    }
    catch {
        Write-Error "Docker或Docker Compose未安装或未配置到PATH"
        Write-Error "请安装Docker Desktop并确保Docker Compose已安装"
        exit 1
    }
}

# 检查端口占用
function Test-PortAvailability {
    Write-Info "检查端口占用情况..."

    $ports = @{
        $ZipkinWebPort = "Zipkin Web UI"
        $ElasticsearchPort = "Elasticsearch"
        $KibanaPort = "Kibana"
        $PrometheusPort = "Prometheus"
    }

    foreach ($port in $ports.Keys) {
        $serviceName = $ports[$port]
        $connection = Get-NetTCPConnection -LocalAddress "127.0.0.1" -LocalPort $port -ErrorAction SilentlyContinue

        if ($connection) {
            Write-Warning "$serviceName 端口 $port 已被占用，PID: $($connection.OwningProcess.Id)"
            Write-Info "尝试终止占用进程..."

            try {
                Stop-Process -Id $connection.OwningProcess.Id -Force -ErrorAction SilentlyContinue
                Start-Sleep -Seconds 2

                # 再次检查端口
                $checkConnection = Get-NetTCPConnection -LocalAddress "127.0.0.1" -LocalPort $port -ErrorAction SilentlyContinue
                if (-not $checkConnection) {
                    Write-Success "端口 $port 已释放"
                } else {
                    Write-Error "无法释放端口 $port，请手动处理"
                    exit 1
                }
            }
            catch {
                Write-Error "无法终止占用端口 $port 的进程，请手动处理"
                exit 1
            }
        }
    }

    Write-Success "端口检查完成"
}

# 创建必要目录
function New-DirectoryStructure {
    Write-Info "创建必要的目录结构..."

    $directories = @(
        $DeploymentDir
        "C:\opt\ioedream\data\elasticsearch-data"
        "C:\opt\ioedream\logs\elasticsearch"
        "C:\opt\ioedream\logs\zipkin"
        "$DeploymentDir\fluentd\conf"
        "$DeploymentDir\kibana"
    )

    foreach ($dir in $directories) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
            Write-Info "创建目录: $dir"
        }
    }

    # 设置目录权限
    Write-Info "设置目录权限..."
    try {
        # 在Windows上，目录权限通常不需要特殊设置
        # 但可以检查目录是否可访问
        foreach ($dir in $directories) {
            if (Test-Path $dir) {
                $acl = Get-Acl $dir
                # 这里可以添加特定的权限设置如果需要
            }
        }
    }
    catch {
        Write-Warning "设置目录权限时出现问题: $_"
    }

    Write-Success "目录结构创建完成"
}

# 创建Fluentd配置
function New-FluentdConfiguration {
    Write-Info "创建Fluentd配置文件..."

    $fluentConfig = @'
<source>
  @type tail
  path /var/log/zipkin
  pos_file /var/log/fluentd/zipkin.log.pos
  tag zipkin.logs
  read_from_head true
</source>

<filter zipkin.logs>
  @type record_transformer
  <record>
    hostname "#{Socket.gethostname}"
    service_name "zipkin"
  </record>
</filter>

<match zipkin.**>
  @type elasticsearch
  host elasticsearch
  port 9200
  index_name zipkin-logs
  type_name _doc
  include_tag_key true
  tag_key @log_name
</match>
'@

    $fluentConfig | Out-File -FilePath "$DeploymentDir\fluentd\conf\fluent.conf" -Encoding UTF8
    Write-Success "Fluentd配置文件创建完成"
}

# 创建环境变量文件
function New-EnvironmentFile {
    Write-Info "创建环境变量配置文件..."

    $envContent = @'
# ============================================================
# IOE-DREAM Zipkin环境变量配置
# ============================================================

# 项目标识
COMPOSE_PROJECT_NAME=ioedream-zipkin

# Zipkin版本
ZIPKIN_VERSION=2.24.1

# Elasticsearch版本
ELASTICSEARCH_VERSION=7.17.9

# 存储配置
STORAGE_TYPE=elasticsearch
ES_HOSTS=elasticsearch:9200
ES_INDEX=zipkin

# JVM配置
ZIPKIN_JAVA_OPTS=-Xms512m -Xmx1024m
ELASTICSEARCH_JAVA_OPTS=-Xms512m -Xmx1024m

# 采样配置
SAMPLE_RATE=0.1

# 日志级别
LOG_LEVEL=INFO

# 网络配置
SUBNET=172.20.0.0/16
GATEWAY=172.20.0.1

# 数据存储路径
ELASTICSEARCH_DATA_PATH=C:\opt\ioedream\data\elasticsearch-data
ELASTICSEARCH_LOGS_PATH=C:\opt\ioedream\logs\elasticsearch
ZIPKIN_LOGS_PATH=C:\opt\ioedream\logs\zipkin

# 健康检查间隔（秒）
HEALTH_CHECK_INTERVAL=30
HEALTH_CHECK_TIMEOUT=10
HEALTH_CHECK_RETRIES=5
HEALTH_CHECK_START_PERIOD=60

# 监控配置
PROMETHEUS_ENABLED=true
GRAFANA_ENABLED=true

# 备份配置
BACKUP_ENABLED=false
BACKUP_SCHEDULE="0 2 * * *"
BACKUP_RETENTION_DAYS=7

# 安全配置
SECURITY_ENABLED=false
CORS_ENABLED=true
CORS_ALLOWED_ORIGINS="*"
'@

    $envContent | Out-File -FilePath "$DeploymentDir\.env" -Encoding UTF8
    Write-Success "环境变量配置文件创建完成"
}

# 部署服务
function Deploy-Services {
    Write-Info "开始部署Zipkin服务..."

    Set-Location $DeploymentDir

    # 停止现有服务（如果存在）
    Write-Info "停止现有服务..."
    & docker-compose down -v 2>$null
    $null = Wait-Process -Id $LastExitCode

    # 拉取最新镜像
    Write-Info "拉取最新镜像..."
    & docker-compose pull 2>$null
    $null = Wait-Process -Id $LastExitCode

    # 启动服务
    Write-Info "启动服务..."
    & docker-compose up -d 2>$null
    $null = Wait-Process -Id $LastExitCode

    Write-Success "服务部署完成"
}

# 等待服务就绪
function Wait-ServicesReady {
    Write-Info "等待服务就绪..."

    $services = @(
        @{Url = $ElasticsearchHealthUrl; Name = "Elasticsearch"}
        @{Url = $ZipkinHealthUrl; Name = "Zipkin"}
        @{Url = $KibanaHealthUrl; Name = "Kibana"}
    )

    foreach ($service in $services) {
        $url = $service.Url
        $name = $service.Name

        Write-Info "等待 $name 服务启动..."

        $retries = 0
        $maxRetries = 60

        do {
            try {
                $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
                if ($response.StatusCode -eq 200) {
                    Write-Success "$name 服务已就绪"
                    break
                }
            }
            catch {
                # 继续重试
            }

            Write-Host "." -NoNewline
            Start-Sleep -Seconds 5
            $retries++
        } while ($retries -lt $maxRetries)

        if ($retries -eq $maxRetries) {
            Write-Error "$name 服务启动超时"
            return $false
        }
    }

    return $true
}

# 显示访问信息
function Show-AccessInfo {
    Write-Success "🎉 Zipkin分布式追踪系统部署成功！"
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "📊 服务访问地址：" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "🔍 Zipkin Web UI:     http://localhost:$ZipkinWebPort" -ForegroundColor White
    Write-Host "🔍 Zipkin API:       http://localhost:$ZipkinApiPort" -ForegroundColor White
    Write-Host "🔍 Elasticsearch:    http://localhost:$ElasticsearchPort" -ForegroundColor White
    Write-Host "🔍 Kibana:           http://localhost:$KibanaPort" -ForegroundColor White
    Write-Host "🔍 Prometheus:       http://localhost:$PrometheusPort" -ForegroundColor White
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "📖 访问说明：" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "📚 Zipkin Web UI: 查看分布式追踪链路" -ForegroundColor White
    Write-Host "📊 Elasticsearch: 直接查询追踪数据" -ForegroundColor White
    Write-Host "📈 Kibana: 数据可视化和分析" -ForegroundColor White
    Write-Host "📉 Prometheus: 监控指标收集" -ForegroundColor White
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "🔧 管理命令：" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "📦 查看服务状态: .\Deploy-Zipkin.ps1 status" -ForegroundColor White
    Write-Host "📦 查看服务日志: .\Deploy-Zipkin.ps1 logs [service-name]" -ForegroundColor White
    Write-Host "🛑 停止所有服务: .\Deploy-Zipkin.ps1 stop" -ForegroundColor White
    Write-Host "🚀 重启所有服务: .\Deploy-Zipkin.ps1 restart" -ForegroundColor White
    Write-Host "🔄 更新服务: .\Deploy-Zipkin.ps1 update" -ForegroundColor White
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "⚠️  注意事项：" -ForegroundColor Red
    Write-Host "============================================================" -ForegroundColor Red
    Write-Host "🔒 确保Windows防火墙已开放对应端口" -ForegroundColor White
    Write-Host "💾 Elasticsearch数据存储在: C:\opt\ioedream\data\" -ForegroundColor White
    Write-Host "📝 日志文件存储在: C:\opt\ioedream\logs\" -ForegroundColor White
    Write-Host "🔧 配置文件位置: $DeploymentDir" -ForegroundColor White
    Write-Host ""
}

# 健康检查
function Test-ServiceHealth {
    Write-Info "执行健康检查..."

    $allHealthy = $true

    # 检查Docker容器状态
    Write-Info "检查Docker容器状态..."
    $containers = @("$ProjectName-zipkin", "$ProjectName-zipkin-elasticsearch", "$ProjectName-zipkin-kibana")

    foreach ($container in $containers) {
        try {
            $status = docker ps --filter "name=$container" --format "table {{.Status}}" | Select-String "Up"
            if ($status) {
                Write-Success "✓ 容器 $container 运行正常"
            } else {
                Write-Error "✗ 容器 $container 运行异常"
                $allHealthy = $false
            }
        }
        catch {
            Write-Error "✗ 无法检查容器 $container 状态"
            $allHealthy = $false
        }
    }

    # 检查服务健康状态
    Write-Info "检查服务健康状态..."
    $urls = @(
        @{Url = $ZipkinHealthUrl; Name = "Zipkin"}
        @{Url = $ElasticsearchHealthUrl; Name = "Elasticsearch"}
    )

    foreach ($service in $urls) {
        try {
            $response = Invoke-WebRequest -Uri $service.Url -TimeoutSec 10 -UseBasicParsing -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Success "✓ $($service.Name) 服务健康"
            } else {
                Write-Error "✗ $($service.Name) 服务异常"
                $allHealthy = $false
            }
        }
        catch {
            Write-Error "✗ $($service.Name) 服务不可达"
            $allHealthy = $false
        }
    }

    if ($allHealthy) {
        Write-Success "🎉 所有服务健康检查通过"
        return $true
    } else {
        Write-Error "❌ 部分服务存在问题"
        return $false
    }
}

# 启动服务
function Start-Services {
    Write-Info "启动Zipkin服务..."
    Set-Location $DeploymentDir

    & docker-compose up -d 2>$null
    $null = Wait-Process -Id $LastExitCode

    if ($LastExitCode -eq 0) {
        Start-Sleep -Seconds 5
        Show-AccessInfo
    } else {
        Write-Error "服务启动失败"
        exit 1
    }
}

# 停止服务
function Stop-Services {
    Write-Info "停止Zipkin服务..."
    Set-Location $DeploymentDir

    & docker-compose down 2>$null
    $null = Wait-Process -Id $LastExitCode

    Write-Success "服务已停止"
}

# 重启服务
function Restart-Services {
    Write-Info "重启Zipkin服务..."
    Stop-Services
    Start-Sleep -Seconds 5
    Start-Services
}

# 查看服务状态
function Show-ServiceStatus {
    Write-Info "Zipkin服务状态："
    Set-Location $DeploymentDir

    try {
        docker-compose ps
    }
    catch {
        Write-Error "无法获取服务状态"
    }
}

# 查看服务日志
function Show-ServiceLogs {
    param([string]$ServiceName = "")

    Set-Location $DeploymentDir

    try {
        if ([string]::IsNullOrEmpty($ServiceName)) {
            docker-compose logs -f
        } else {
            docker-compose logs -f $ServiceName
        }
    }
    catch {
        Write-Error "无法查看服务日志"
    }
}

# 清理服务
function Clear-Services {
    Write-Warning "这将删除所有服务、配置和数据！"
    $choice = Read-Host "确定要继续吗？(y/N) "

    if ($choice -eq "y" -or $choice -eq "Y") {
        Set-Location $DeploymentDir

        Write-Info "停止并删除容器..."
        & docker-compose down -v 2>$null
        $null = Wait-Process -Id $LastExitCode

        Write-Info "清理Docker资源..."
        & docker system prune -f 2>$null
        $null = Wait-Process -Id $LastExitCode

        # 清理数据目录
        if (Test-Path "C:\opt\ioedream\data\") {
            Write-Info "清理数据目录..."
            Remove-Item -Recurse -Force "C:\opt\ioedream\data\" -ErrorAction SilentlyContinue
        }

        if (Test-Path "C:\opt\ioedream\logs\") {
            Write-Info "清理日志目录..."
            Remove-Item -Recurse -Force "C:\opt\ioedream\logs\" -ErrorAction SilentlyContinue
        }

        Write-Success "清理完成"
    } else {
        Write-Info "取消清理操作"
    }
}

# 备份配置和数据
function Backup-Services {
    Write-Info "备份Zipkin配置和数据..."

    $backupDir = "C:\opt\ioedream\backup\zipkin-$(Get-Date -Format 'yyyyMMdd_HHmmss')"
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

    # 备份配置文件
    Write-Info "备份配置文件..."
    Copy-Item -Path "$DeploymentDir\*" -Destination "$backupDir\config\" -Recurse -Force -ErrorAction SilentlyContinue

    # 备份数据
    if (Test-Path "C:\opt\ioedream\data\") {
        Write-Info "备份数据目录..."
        Copy-Item -Path "C:\opt\ioedream\data" -Destination "$backupDir\data" -Recurse -Force -ErrorAction SilentlyContinue
    }

    # 备份日志
    if (Test-Path "C:\opt\ioedream\logs") {
        Write-Info "备份日志目录..."
        Copy-Item -Path "C:\opt\ioedream\logs" -Destination "$backupDir\logs" -Recurse -Force -ErrorAction SilentlyContinue
    }

    Write-Success "备份完成，备份位置: $backupDir"
}

# 显示帮助信息
function Show-Help {
    Write-Host "用法: .\Deploy-Zipkin.ps1 [命令]" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "命令:" -ForegroundColor Yellow
    Write-Host "  deploy     部署Zipkin服务"
    Write-Host "  start      启动Zipkin服务"
    Write-Host "  stop       停止Zipkin服务"
    Write-Host "  restart    重启Zipkin服务"
    Write-Host "  status     查看服务状态"
    Write-Host "  logs       查看服务日志"
    Write-Host "  health     执行健康检查"
    Write-Host "  clean      清理数据和服务"
    Write-Host "  backup     备份配置和数据"
    Write-Host "  help       显示帮助信息"
    Write-Host ""
    Write-Host "示例:" -ForegroundColor Green
    Write-Host "  .\Deploy-Zipkin.ps1 deploy    # 完整部署"
    Write-Host "  .\Deploy-Zipkin.ps1 start     # 启动服务"
    Write-Host "  .\Deploy-Zipkin.ps1 status    # 查看状态"
    Write-Host "  .\Deploy-Zipkin.ps1 health    # 健康检查"
}

# 主函数
function Main {
    try {
        switch ($Action) {
            "deploy" {
                Test-DockerEnvironment
                Test-PortAvailability
                New-DirectoryStructure
                New-FluentdConfiguration
                New-EnvironmentFile
                Deploy-Services
                if (Wait-ServicesReady) {
                    Show-AccessInfo
                }
            }
            "start" {
                Start-Services
            }
            "stop" {
                Stop-Services
            }
            "restart" {
                Restart-Services
            }
            "status" {
                Show-ServiceStatus
            }
            "logs" {
                Show-ServiceLogs $Service
            }
            "health" {
                Test-ServiceHealth
            }
            "clean" {
                Clear-Services
            }
            "backup" {
                Backup-Services
            }
            "help" {
                Show-Help
            }
            default {
                Write-Error "未知命令: $Action"
                Show-Help
                exit 1
            }
        }
    }
    catch {
        Write-Error "执行命令时发生错误: $_"
        exit 1
    }
}

# 执行主函数
Main