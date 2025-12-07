# 监控告警机制部署脚本
# <p>
# 用于一键部署Prometheus、AlertManager和Grafana监控告警系统
# 支持Windows环境部署
# </p>
#
# @Author:    IOE-DREAM Team
# @Date:      2025-01-30
# @Copyright  IOE-DREAM智慧园区一卡通管理平台

param(
    [string]$Action = "start",
    [switch]$Stop,
    [switch]$Restart,
    [switch]$Status,
    [switch]$Logs
)

# 设置错误处理
$ErrorActionPreference = "Stop"

# 脚本配置
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$MonitoringDir = Join-Path $ProjectRoot "deployment\monitoring"
$DockerComposeFile = Join-Path $MonitoringDir "docker-compose-monitoring.yml"

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# 检查Docker是否运行
function Test-DockerRunning {
    try {
        docker info | Out-Null
        return $true
    } catch {
        return $false
    }
}

# 检查Docker Compose文件是否存在
function Test-DockerComposeFile {
    if (-not (Test-Path $DockerComposeFile)) {
        Write-ColorOutput "错误: Docker Compose文件不存在: $DockerComposeFile" "Red"
        return $false
    }
    return $true
}

# 启动监控服务
function Start-Monitoring {
    Write-ColorOutput "`n=== 启动监控告警系统 ===" "Cyan"
    
    if (-not (Test-DockerRunning)) {
        Write-ColorOutput "错误: Docker未运行，请先启动Docker Desktop" "Red"
        exit 1
    }
    
    if (-not (Test-DockerComposeFile)) {
        exit 1
    }
    
    Write-ColorOutput "正在启动Prometheus、AlertManager和Grafana..." "Yellow"
    
    try {
        Push-Location $MonitoringDir
        docker-compose -f docker-compose-monitoring.yml up -d
        
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "`n✅ 监控告警系统启动成功！" "Green"
            Write-ColorOutput "`n访问地址:" "Cyan"
            Write-ColorOutput "  - Prometheus: http://localhost:9090" "White"
            Write-ColorOutput "  - AlertManager: http://localhost:9093" "White"
            Write-ColorOutput "  - Grafana: http://localhost:3000 (默认账号: admin/admin)" "White"
            Write-ColorOutput "`n提示: 首次启动Grafana需要修改默认密码" "Yellow"
        } else {
            Write-ColorOutput "错误: 启动失败" "Red"
            exit 1
        }
    } catch {
        Write-ColorOutput "错误: $($_.Exception.Message)" "Red"
        exit 1
    } finally {
        Pop-Location
    }
}

# 停止监控服务
function Stop-Monitoring {
    Write-ColorOutput "`n=== 停止监控告警系统 ===" "Cyan"
    
    if (-not (Test-DockerComposeFile)) {
        exit 1
    }
    
    try {
        Push-Location $MonitoringDir
        docker-compose -f docker-compose-monitoring.yml down
        
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "✅ 监控告警系统已停止" "Green"
        } else {
            Write-ColorOutput "错误: 停止失败" "Red"
            exit 1
        }
    } catch {
        Write-ColorOutput "错误: $($_.Exception.Message)" "Red"
        exit 1
    } finally {
        Pop-Location
    }
}

# 重启监控服务
function Restart-Monitoring {
    Write-ColorOutput "`n=== 重启监控告警系统 ===" "Cyan"
    Stop-Monitoring
    Start-Sleep -Seconds 2
    Start-Monitoring
}

# 查看服务状态
function Show-Status {
    Write-ColorOutput "`n=== 监控告警系统状态 ===" "Cyan"
    
    if (-not (Test-DockerComposeFile)) {
        exit 1
    }
    
    try {
        Push-Location $MonitoringDir
        docker-compose -f docker-compose-monitoring.yml ps
    } catch {
        Write-ColorOutput "错误: $($_.Exception.Message)" "Red"
        exit 1
    } finally {
        Pop-Location
    }
}

# 查看服务日志
function Show-Logs {
    Write-ColorOutput "`n=== 监控告警系统日志 ===" "Cyan"
    
    if (-not (Test-DockerComposeFile)) {
        exit 1
    }
    
    param(
        [string]$Service = ""
    )
    
    try {
        Push-Location $MonitoringDir
        if ($Service) {
            docker-compose -f docker-compose-monitoring.yml logs -f $Service
        } else {
            docker-compose -f docker-compose-monitoring.yml logs -f
        }
    } catch {
        Write-ColorOutput "错误: $($_.Exception.Message)" "Red"
        exit 1
    } finally {
        Pop-Location
    }
}

# 主逻辑
Write-ColorOutput "`n========================================" "Cyan"
Write-ColorOutput "  IOE-DREAM 监控告警系统部署脚本" "Cyan"
Write-ColorOutput "========================================`n" "Cyan"

if ($Stop) {
    Stop-Monitoring
} elseif ($Restart) {
    Restart-Monitoring
} elseif ($Status) {
    Show-Status
} elseif ($Logs) {
    Show-Logs
} else {
    Start-Monitoring
}

Write-ColorOutput "`n脚本执行完成" "Green"

