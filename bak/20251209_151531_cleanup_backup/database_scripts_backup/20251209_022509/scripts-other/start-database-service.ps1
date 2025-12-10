# =====================================================
# IOE-DREAM 数据库服务启动脚本 (PowerShell)
# 版本: 1.0.0
# 说明: 启动数据库初始化和同步服务
# =====================================================

# 设置错误处理
$ErrorActionPreference = "Stop"

# 服务配置
$serviceName = "ioedream-database-service"
$servicePort = "8889"
$jarFile = "ioedream-database-service-1.0.0.jar"
$appHome = "D:\IOE-DREAM"
$logDir = "$appHome\logs"
$pidFile = "$appHome\$serviceName.pid"
$jvmOpts = "-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$logDir"

# 环境变量配置
$env:SPRING_PROFILES_ACTIVE = if ($env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE } else { "dev" }
$env:NACOS_SERVER_ADDR = if ($env:NACOS_SERVER_ADDR) { $env:NACOS_SERVER_ADDR } else { "127.0.0.1:8848" }
$env:NACOS_NAMESPACE = if ($env:NACOS_NAMESPACE) { $env:NACOS_NAMESPACE } else { "dev" }
$env:NACOS_GROUP = if ($env:NACOS_GROUP) { $env:NACOS_GROUP } else { "IOE-DATABASE" }

# 数据库连接配置
$env:DATABASE_URL = if ($env:DATABASE_URL) { $env:DATABASE_URL } else { "jdbc:mysql://127.0.0.1:3306/ioedream_database?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai" }
$env:DATABASE_USERNAME = if ($env:DATABASE_USERNAME) { $env:DATABASE_USERNAME } else { "root" }
$env:DATABASE_PASSWORD = if ($env:DATABASE_PASSWORD) { $env:DATABASE_PASSWORD } else { "123456" }

# 应用配置
$env:DATABASE_SYNC_ENABLED = if ($env:DATABASE_SYNC_ENABLED) { $env:DATABASE_SYNC_ENABLED } else { "true" }
$env:DATABASE_SYNC_AUTO_STARTUP = if ($env:DATABASE_SYNC_AUTO_STARTUP) { $env:DATABASE_SYNC_AUTO_STARTUP } else { "true" }
$env:DATABASE_SYNC_CHECK_INTERVAL = if ($env:DATABASE_SYNC_CHECK_INTERVAL) { $env:DATABASE_SYNC_CHECK_INTERVAL } else { "30000" }

Write-Host "🚀 [启动脚本] 开始启动 $serviceName..." -ForegroundColor Green
Write-Host "📋 [启动脚本] 服务配置信息:" -ForegroundColor Cyan
Write-Host "   - 服务名称: $serviceName" -ForegroundColor White
Write-Host "   - 服务端口: $servicePort" -ForegroundColor White
Write-Host "   - Spring环境: $env:SPRING_PROFILES_ACTIVE" -ForegroundColor White
Write-Host "   - Nacos地址: $env:NACOS_SERVER_ADDR" -ForegroundColor White
Write-Host "   - 数据库同步: $env:DATABASE_SYNC_ENABLED" -ForegroundColor White
Write-Host "   - 自动启动同步: $env:DATABASE_SYNC_AUTO_STARTUP" -ForegroundColor White

# 创建必要的目录
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force | Out-Null
    Write-Host "📁 [启动脚本] 创建日志目录: $logDir" -ForegroundColor Yellow
}

if (-not (Test-Path "$appHome\temp")) {
    New-Item -ItemType Directory -Path "$appHome\temp" -Force | Out-Null
}

# 检查JAR文件是否存在
$jarPath = Join-Path $appHome $jarFile
if (-not (Test-Path $jarPath)) {
    # 尝试从微服务目录查找
    $jarPath = Join-Path $appHome "microservices\ioedream-database-service\target\$jarFile"
    if (-not (Test-Path $jarPath)) {
        Write-Host "❌ [启动脚本] JAR文件不存在: $jarPath" -ForegroundColor Red
        Write-Host "💡 [启动脚本] 请先构建项目: mvn clean package -pl microservices/ioedream-database-service" -ForegroundColor Yellow
        exit 1
    }
}

# 检查服务是否已经运行
if (Test-Path $pidFile) {
    try {
        $oldPid = Get-Content $pidFile -ErrorAction SilentlyContinue
        if ($oldPid) {
            $process = Get-Process -Id $oldPid -ErrorAction SilentlyContinue
            if ($process -and $process.ProcessName -eq "java") {
                Write-Host "⚠️ [启动脚本] 服务已在运行中，PID: $oldPid" -ForegroundColor Yellow
                exit 1
            }
        }
        Write-Host "🧹 [启动脚本] 清理旧的PID文件" -ForegroundColor Yellow
        Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
    } catch {
        Write-Host "🧹 [启动脚本] 清理旧的PID文件" -ForegroundColor Yellow
        Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
    }
}

# 检查Java环境
try {
    $javaVersion = & java -version 2>&1 | Select-Object -First 1
    if ($javaVersion -match '"(\d+)\.') {
        $majorVersion = [int]$matches[1]
        if ($majorVersion -lt 17) {
            Write-Host "❌ [启动脚本] 需要Java 17或更高版本，当前版本: $majorVersion" -ForegroundColor Red
            Write-Host "💡 [启动脚本] 请安装Java 17或更新版本" -ForegroundColor Yellow
            exit 1
        }
        Write-Host "✅ [启动脚本] Java版本检查通过: $majorVersion" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ [启动脚本] Java环境未配置或无法访问" -ForegroundColor Red
    Write-Host "💡 [启动脚本] 请确保Java 17+已安装并配置到PATH" -ForegroundColor Yellow
    exit 1
}

# 查找JAR文件
$targetJar = Get-ChildItem -Path $appHome -Recurse -Filter $jarFile | Where-Object { $_.FullName -like "*target*" } | Select-Object -First 1
if ($targetJar) {
    $jarPath = $targetJar.FullName
    Write-Host "✅ [启动脚本] 找到JAR文件: $jarPath" -ForegroundColor Green
} else {
    Write-Host "❌ [启动脚本] 在项目中未找到JAR文件: $jarFile" -ForegroundColor Red
    Write-Host "💡 [启动脚本] 请先构建项目: mvn clean package -pl microservices/ioedream-database-service" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ [启动脚本] 环境检查通过，开始启动服务..." -ForegroundColor Green

# 启动服务
Write-Host "🔄 [启动脚本] 正在启动 $serviceName..." -ForegroundColor Cyan

$logFile = "$logDir\$serviceName.out"

try {
    # 使用Start-Process启动Java进程
    $processArgs = @(
        $jvmOpts,
        "-Dspring.profiles.active=$($env:SPRING_PROFILES_ACTIVE)",
        "-Dserver.port=$servicePort",
        "-jar", "`"$jarPath`""
    )

    $process = Start-Process -FilePath "java" -ArgumentList $processArgs -NoNewWindow -PassThru -RedirectStandardOutput $logFile -RedirectStandardError $logFile

    # 获取进程ID并保存到文件
    $pid = $process.Id
    $pid | Out-File -FilePath $pidFile -Encoding UTF8

    Write-Host "⏳ [启动脚本] 等待服务启动..." -ForegroundColor Yellow

    # 等待服务启动
    Start-Sleep -Seconds 15

    # 检查进程是否还在运行
    try {
        $runningProcess = Get-Process -Id $pid -ErrorAction SilentlyContinue
        if ($runningProcess) {
            Write-Host "✅ [启动脚本] $serviceName 启动成功，PID: $pid" -ForegroundColor Green
            Write-Host "📊 [启动脚本] 服务信息:" -ForegroundColor Cyan
            Write-Host "   - PID: $pid" -ForegroundColor White
            Write-Host "   - 端口: $servicePort" -ForegroundColor White
            Write-Host "   - 日志文件: $logFile" -ForegroundColor White
            Write-Host "   - 健康检查: http://localhost:$servicePort/database/api/v1/database/sync/health" -ForegroundColor White
            Write-Host ""
            Write-Host "🔍 [启动脚本] 查看日志: Get-Content '$logFile' -Wait" -ForegroundColor Yellow
            Write-Host "🛑 [启动脚本] 停止服务: Stop-Process -Id $pid" -ForegroundColor Yellow
            Write-Host "🗂️ [启动脚本] 清理PID: Remove-Item '$pidFile'" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "🎉 [启动脚本] $serviceName 启动完成！" -ForegroundColor Green

            # 执行健康检查
            Write-Host "🔍 [启动脚本] 执行健康检查..." -ForegroundColor Yellow
            Start-Sleep -Seconds 5

            try {
                $response = Invoke-WebRequest -Uri "http://localhost:$servicePort/database/api/v1/database/sync/health" -TimeoutSec 10
                Write-Host "✅ [启动脚本] 健康检查通过: $($response.StatusCode)" -ForegroundColor Green
            } catch {
                Write-Host "⚠️ [启动脚本] 健康检查失败，但服务可能仍在启动中: $($_.Exception.Message)" -ForegroundColor Yellow
            }
        } else {
            Write-Host "❌ [启动脚本] $serviceName 启动失败，进程意外退出" -ForegroundColor Red
            Write-Host "📄 [启动脚本] 请查看日志文件: $logFile" -ForegroundColor Yellow
            Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
            exit 1
        }
    } catch {
        Write-Host "❌ [启动脚本] 检查进程状态时发生异常: $($_.Exception.Message)" -ForegroundColor Red
        Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
        exit 1
    }

} catch {
    Write-Host "❌ [启动脚本] 启动 $serviceName 时发生异常: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "📄 [启动脚本] 请查看日志文件: $logFile" -ForegroundColor Yellow
    if (Test-Path $pidFile) {
        Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
    }
    exit 1
}