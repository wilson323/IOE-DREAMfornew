# IOE-DREAM Docker 构建脚本 (PowerShell版本)
# 解决构建顺序依赖问题，确保microservices-common先构建

param(
    [switch]$SkipCommon,
    [switch]$SkipMicroservices,
    [switch]$SkipFrontend,
    [switch]$Cleanup,
    [switch]$Help
)

# 颜色定义
$Colors = @{
    Red = "Red"
    Green = "Green"
    Yellow = "Yellow"
    Blue = "Blue"
    White = "White"
}

# 日志函数
function Write-LogInfo {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor $Colors.Green
}

function Write-LogWarn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor $Colors.Yellow
}

function Write-LogError {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor $Colors.Red
}

function Write-LogStep {
    param([string]$Message)
    Write-Host "[STEP] $Message" -ForegroundColor $Colors.Blue
}

# 检查Docker环境
function Test-DockerEnvironment {
    Write-LogStep "检查Docker环境..."

    try {
        $null = Get-Command docker -ErrorAction Stop
        $null = docker info 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "Docker服务未启动"
        }
        Write-LogInfo "Docker环境检查通过"
    }
    catch {
        Write-LogError "Docker环境检查失败: $($_.Exception.Message)"
        exit 1
    }
}

# 检查Docker Compose环境
function Test-DockerComposeEnvironment {
    Write-LogStep "检查Docker Compose环境..."

    try {
        $null = Get-Command docker-compose -ErrorAction Stop
        # 或者检查 docker compose
        $version = docker compose version 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "Docker Compose未安装"
        }
        Write-LogInfo "Docker Compose环境检查通过"
    }
    catch {
        try {
            $null = docker-compose --version 2>$null
            Write-LogInfo "Docker Compose环境检查通过"
        }
        catch {
            Write-LogError "Docker Compose未安装，请先安装Docker Compose"
            exit 1
        }
    }
}

# 创建Maven配置文件
function New-MavenSettings {
    Write-LogStep "创建Maven配置文件..."

    $m2Dir = "$env:USERPROFILE\.m2"
    if (-not (Test-Path $m2Dir)) {
        New-Item -ItemType Directory -Path $m2Dir -Force | Out-Null
    }

    $settingsContent = @'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0
          http://maven.apache.org/xsd/settings-1.2.0.xsd">

  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>

  <profiles>
    <profile>
      <id>aliyun</id>
      <repositories>
        <repository>
          <id>aliyun-central</id>
          <name>阿里云中央仓库</name>
          <url>https://maven.aliyun.com/repository/central</url>
        </repository>
        <repository>
          <id>aliyun-public</id>
          <name>阿里云公共仓库</name>
          <url>https://maven.aliyun.com/repository/public</url>
        </repository>
        <repository>
          <id>aliyun-spring</id>
          <name>阿里云Spring仓库</name>
          <url>https://maven.aliyun.com/repository/spring</url>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>aliyun-plugin</id>
          <name>阿里云插件仓库</name>
          <url>https://maven.aliyun.com/repository/public</url>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>

  <activeProfiles>
    <activeProfile>aliyun</activeProfile>
  </activeProfiles>
</settings>
'@

    $settingsFile = "$m2Dir\settings.xml"
    Set-Content -Path $settingsFile -Value $settingsContent -Encoding UTF8
    Write-LogInfo "Maven配置文件创建完成"
}

# 构建microservices-common（关键步骤）
function Build-CommonModule {
    Write-LogStep "构建microservices-common模块（关键依赖）..."

    # 检查项目结构
    $commonDir = "microservices\microservices-common"
    if (-not (Test-Path $commonDir)) {
        Write-LogError "microservices-common目录不存在"
        exit 1
    }

    try {
        # 构建父POM
        Write-LogInfo "构建父POM..."
        Set-Location "microservices"
        mvn clean install -N -DskipTests -Dmaven.test.skip=true
        if ($LASTEXITCODE -ne 0) {
            throw "父POM构建失败"
        }

        # 构建common模块
        Write-LogInfo "构建microservices-common模块..."
        Set-Location "microservices-common"
        mvn clean install -DskipTests -Dmaven.test.skip=true
        if ($LASTEXITCODE -ne 0) {
            throw "microservices-common模块构建失败"
        }

        Set-Location "..\"
        Set-Location "..\"
        Write-LogInfo "microservices-common模块构建成功"
    }
    catch {
        Set-Location "..\"
        Set-Location "..\"
        Write-LogError "构建失败: $($_.Exception.Message)"
        exit 1
    }
}

# 验证common模块是否安装成功
function Test-CommonModule {
    Write-LogStep "验证microservices-common模块安装..."

    # 检查本地Maven仓库
    $commonJar = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"
    if (-not (Test-Path $commonJar)) {
        Write-LogError "microservices-common模块未安装到本地仓库"
        exit 1
    }

    Write-LogInfo "microservices-common模块验证成功"
}

# 构建微服务镜像
function Build-MicroserviceImages {
    Write-LogStep "构建微服务Docker镜像..."

    # 微服务列表（按依赖顺序）
    $services = @(
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

    foreach ($service in $services) {
        Write-LogInfo "构建 ${service} 镜像..."

        # 检查Dockerfile是否存在
        $dockerfile = "microservices\ioedream-${service}\Dockerfile"
        if (-not (Test-Path $dockerfile)) {
            Write-LogError "${service} 的Dockerfile不存在"
            continue
        }

        # 构建镜像
        $buildCommand = "docker build -f `"$dockerfile`" -t `"ioedream/${service}:latest`" ."
        Write-LogInfo "执行: $buildCommand"

        Invoke-Expression $buildCommand
        if ($LASTEXITCODE -ne 0) {
            Write-LogError "${service} 镜像构建失败"
            continue
        }

        Write-LogInfo "${service} 镜像构建成功"
    }
}

# 构建前端镜像
function Build-FrontendImages {
    Write-LogStep "构建前端应用镜像..."

    # Web管理后台
    if (Test-Path "smart-admin-web-javascript") {
        Write-LogInfo "构建Web管理后台镜像..."
        docker build -t "ioedream/web-admin:latest" "./smart-admin-web-javascript/"
        if ($LASTEXITCODE -eq 0) {
            Write-LogInfo "Web管理后台镜像构建成功"
        } else {
            Write-LogWarn "Web管理后台镜像构建失败（可选组件）"
        }
    }

    # 移动端应用
    if (Test-Path "smart-app") {
        Write-LogInfo "构建移动端应用镜像..."
        Write-LogWarn "移动端应用（uni-app）通常不需要Docker镜像部署"
    }
}

# 清理构建缓存
function Clear-BuildCache {
    Write-LogStep "清理构建缓存..."

    # 清理Maven缓存
    mvn clean *>$null 2>&1

    # 清理Docker构建缓存
    $choice = Read-Host "是否清理Docker构建缓存？这将释放大量磁盘空间 (y/N)"
    if ($choice -eq 'y' -or $choice -eq 'Y') {
        docker builder prune -f
        Write-LogInfo "Docker构建缓存已清理"
    }
}

# 显示构建结果
function Show-BuildResults {
    Write-LogStep "显示构建结果..."

    Write-Host ""
    Write-Host "构建完成的镜像：" -ForegroundColor $Colors.Green
    docker images | Where-Object { $_ -match "ioedream" }

    Write-Host ""
    Write-Host "镜像大小统计：" -ForegroundColor $Colors.Green
    docker images ioedream/ --format "table {{.Repository}}`t{{.Tag}}`t{{.Size}}`t{{.CreatedAt}}"

    Write-Host ""
    Write-Host "构建日志位置：" -ForegroundColor $Colors.Green
    Write-Host "- Maven日志: ./target/"
    Write-Host "- Docker构建日志: docker logs <container_id>"
}

# 显示帮助信息
function Show-Help {
    Write-Host "IOE-DREAM Docker 构建脚本 v1.0.0" -ForegroundColor $Colors.Green
    Write-Host ""
    Write-Host "用法:" -ForegroundColor $Colors.Blue
    Write-Host "    .\scripts\docker-build.ps1 [选项]"
    Write-Host ""
    Write-Host "选项:" -ForegroundColor $Colors.Blue
    Write-Host "    -SkipCommon         跳过构建common模块"
    Write-Host "    -SkipMicroservices  跳过构建微服务"
    Write-Host "    -SkipFrontend       跳过构建前端"
    Write-Host "    -Cleanup            构建完成后清理缓存"
    Write-Host "    -Help               显示此帮助信息"
    Write-Host ""
    Write-Host "示例:" -ForegroundColor $Colors.Blue
    Write-Host "    .\scripts\docker-build.ps1"
    Write-Host "    .\scripts\docker-build.ps1 -SkipFrontend -Cleanup"
    Write-Host ""
}

# 主函数
function Main {
    Write-Host ""
    Write-Host "=============================================" -ForegroundColor $Colors.Green
    Write-Host "    IOE-DREAM Docker 构建 v1.0.0" -ForegroundColor $Colors.Green
    Write-Host "=============================================" -ForegroundColor $Colors.Green
    Write-Host ""

    # 检查帮助选项
    if ($Help) {
        Show-Help
        return
    }

    # 执行构建步骤
    Test-DockerEnvironment
    Test-DockerComposeEnvironment
    New-MavenSettings

    if (-not $SkipCommon) {
        Build-CommonModule
        Test-CommonModule
    } else {
        Write-LogWarn "跳过构建common模块"
    }

    if (-not $SkipMicroservices) {
        Build-MicroserviceImages
    } else {
        Write-LogWarn "跳过构建微服务"
    }

    if (-not $SkipFrontend) {
        Build-FrontendImages
    } else {
        Write-LogWarn "跳过构建前端"
    }

    if ($Cleanup) {
        Clear-BuildCache
    }

    Show-BuildResults

    Write-Host ""
    Write-Host "🎉 Docker镜像构建完成！" -ForegroundColor $Colors.Green
    Write-Host ""
    Write-Host "下一步操作：" -ForegroundColor $Colors.Blue
    Write-Host "1. 配置环境变量文件: .env.production"
    Write-Host "2. 启动服务: docker-compose -f docker-compose-production.yml up -d"
    Write-Host "3. 查看服务状态: docker-compose -f docker-compose-production.yml ps"
    Write-Host "4. 查看日志: docker-compose -f docker-compose-production.yml logs -f [service_name]"
    Write-Host ""
}

# 错误处理
trap {
    Write-LogError "构建过程中发生错误: $($_.Exception.Message)"
    exit 1
}

# 记录当前目录
$originalLocation = Get-Location

try {
    # 执行主函数
    Main
}
finally {
    # 恢复到原始目录
    Set-Location $originalLocation
}