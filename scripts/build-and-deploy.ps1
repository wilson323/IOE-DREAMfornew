# =====================================================
# IOE-DREAM 构建和部署PowerShell脚本
# 支持完整的CI/CD流水线
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("staging", "production")]
    [string]$Environment = "staging",

    [Parameter(Mandatory=$false)]
    [ValidateSet("all", "gateway", "common", "device", "oa", "access", "attendance", "video", "consume", "visitor")]
    [string]$Service = "all",

    [Parameter(Mandatory=$false)]
    [string]$ImageTag = "latest",

    [Parameter(Mandatory=$false)]
    [switch]$SkipTests = $false,

    [Parameter(Mandatory=$false)]
    [switch]$SkipBuild = $false,

    [Parameter(Mandatory=$false)]
    [switch]$SkipDeploy = $false,

    [Parameter(Mandatory=$false)]
    [switch]$DryRun = $false,

    [Parameter(Mandatory=$false)]
    [int]$Timeout = 600
)

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Color,
        [string]$Message
    )

    $colors = @{
        "red" = "Red"
        "green" = "Green"
        "yellow" = "Yellow"
        "blue" = "Blue"
        "purple" = "Magenta"
        "cyan" = "Cyan"
    }

    if ($colors.ContainsKey($Color)) {
        Write-Host $Message -ForegroundColor $colors[$Color]
    } else {
        Write-Host $Message
    }
}

# 打印标题
function Write-Title {
    Write-ColorOutput "blue" "===================================================="
    Write-ColorOutput "blue" "🚀 IOE-DREAM 构建和部署脚本"
    Write-ColorOutput "blue" "===================================================="
    Write-ColorOutput "cyan" "环境: $Environment"
    Write-ColorOutput "cyan" "服务: $Service"
    Write-ColorOutput "cyan" "镜像标签: $ImageTag"
    Write-ColorOutput "cyan" "跳过测试: $SkipTests"
    Write-ColorOutput "cyan" "跳过构建: $SkipBuild"
    Write-ColorOutput "cyan" "跳过部署: $SkipDeploy"
    Write-ColorOutput "cyan" "预览模式: $DryRun"
    Write-ColorOutput "blue" "===================================================="
}

# 检查依赖
function Test-Dependencies {
    Write-ColorOutput "yellow" "🔍 检查构建依赖..."

    # 检查Java
    try {
        $javaVersion = java -version 2>&1
        Write-ColorOutput "green" "✅ Java: $javaVersion"
    } catch {
        Write-ColorOutput "red" "❌ Java未安装或不在PATH中"
        exit 1
    }

    # 检查Maven
    try {
        $mavenVersion = mvn --version
        Write-ColorOutput "green" "✅ Maven: $($mavenVersion.Split()[2])"
    } catch {
        Write-ColorOutput "red" "❌ Maven未安装或不在PATH中"
        exit 1
    }

    # 检查Docker
    try {
        $dockerVersion = docker --version
        Write-ColorOutput "green" "✅ Docker: $dockerVersion"
    } catch {
        Write-ColorOutput "red" "❌ Docker未安装或不在PATH中"
        exit 1
    }

    # 检查kubectl
    try {
        $kubectlVersion = kubectl version --client --short 2>$null
        Write-ColorOutput "green" "✅ kubectl: $kubectlVersion"
    } catch {
        Write-ColorOutput "yellow" "⚠️  kubectl未安装或不在PATH中，跳过部署"
        $script:SkipDeploy = $true
    }

    Write-ColorOutput "green" "✅ 依赖检查完成"
}

# 构建微服务
function Build-Services {
    Write-ColorOutput "yellow" "🔨 开始构建微服务..."

    $servicesToBuild = @()

    if ($Service -eq "all") {
        $servicesToBuild = @(
            "microservices/microservices-common",
            "microservices/ioedream-gateway-service",
            "microservices/ioedream-common-service",
            "microservices/ioedream-device-comm-service",
            "microservices/ioedream-oa-service",
            "microservices/ioedream-access-service",
            "microservices/ioedream-attendance-service",
            "microservices/ioedream-video-service",
            "microservices/ioedream-consume-service",
            "microservices/ioedream-visitor-service"
        )
    } else {
        $servicesToBuild = @("microservices/ioedream-${Service}-service")
    }

    foreach ($service in $servicesToBuild) {
        Write-ColorOutput "cyan" "构建服务: $service"

        if ($DryRun) {
            Write-ColorOutput "cyan" "[预览] 将执行: mvn clean package -pl $service -am"
            continue
        }

        try {
            $buildArgs = @("clean", "package", "-pl", $service, "-am", "-DskipTests")
            if ($SkipTests) {
                $buildArgs += "-DskipTests"
            }

            $buildResult = mvn $buildArgs
            if ($LASTEXITCODE -eq 0) {
                Write-ColorOutput "green" "✅ $service 构建成功"
            } else {
                Write-ColorOutput "red" "❌ $service 构建失败"
                throw "构建失败: $service"
            }
        } catch {
            Write-ColorOutput "red" "❌ 构建过程出现错误: $($_.Exception.Message)"
            exit 1
        }
    }
}

# 运行测试
function Invoke-Tests {
    if ($SkipTests) {
        Write-ColorOutput "yellow" "⚠️  跳过测试执行"
        return
    }

    Write-ColorOutput "yellow" "🧪 开始运行测试..."

    $servicesToTest = @()

    if ($Service -eq "all") {
        $servicesToTest = @(
            "microservices/microservices-common",
            "microservices/ioedream-common-service",
            "microservices/ioedream-access-service",
            "microservices/ioedream-attendance-service",
            "microservices/ioedream-consume-service",
            "microservices/ioedream-visitor-service"
        )
    } else {
        $servicesToTest = @("microservices/ioedream-${Service}-service")
    }

    foreach ($service in $servicesToTest) {
        Write-ColorOutput "cyan" "测试服务: $service"

        if ($DryRun) {
            Write-ColorOutput "cyan" "[预览] 将执行: mvn test -pl $service"
            continue
        }

        try {
            $testResult = mvn test -pl $service
            if ($LASTEXITCODE -eq 0) {
                Write-ColorOutput "green" "✅ $service 测试通过"
            } else {
                Write-ColorOutput "red" "❌ $service 测试失败"
                throw "测试失败: $service"
            }
        } catch {
            Write-ColorOutput "red" "❌ 测试过程出现错误: $($_.Exception.Message)"
            exit 1
        }
    }
}

# 构建Docker镜像
function Build-DockerImages {
    Write-ColorOutput "yellow" "🐳 开始构建Docker镜像..."

    $servicesToBuild = @()

    if ($Service -eq "all") {
        $servicesToBuild = @(
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
    } else {
        $servicesToBuild = @("ioedream-${Service}-service")
    }

    foreach ($serviceName in $servicesToBuild) {
        Write-ColorOutput "cyan" "构建镜像: $serviceName"

        $dockerfilePath = "microservices/${serviceName}/Dockerfile.optimized"
        $contextPath = "."
        $imageName = "ioe-dream/${serviceName}:${ImageTag}"

        if (-not (Test-Path $dockerfilePath)) {
            Write-ColorOutput "yellow" "⚠️  Dockerfile不存在: $dockerfilePath，跳过"
            continue
        }

        if ($DryRun) {
            Write-ColorOutput "cyan" "[预览] 将执行: docker build -f $dockerfilePath -t $imageName $contextPath"
            continue
        }

        try {
            $buildArgs = @(
                "build",
                "-f", $dockerfilePath,
                "-t", $imageName,
                $contextPath
            )

            $buildResult = docker $buildArgs
            if ($LASTEXITCODE -eq 0) {
                Write-ColorOutput "green" "✅ $serviceName 镜像构建成功"

                # 推送到镜像仓库（如果配置了）
                if ($env:REGISTRY_URL) {
                    $fullImageName = "$($env:REGISTRY_URL)/$imageName"
                    docker tag $imageName $fullImageName
                    docker push $fullImageName
                    Write-ColorOutput "green" "✅ $serviceName 镜像推送成功"
                }
            } else {
                Write-ColorOutput "red" "❌ $serviceName 镜像构建失败"
                throw "Docker构建失败: $serviceName"
            }
        } catch {
            Write-ColorOutput "red" "❌ Docker构建过程出现错误: $($_.Exception.Message)"
            exit 1
        }
    }
}

# 部署到Kubernetes
function Deploy-ToKubernetes {
    if ($SkipDeploy) {
        Write-ColorOutput "yellow" "⚠️  跳过Kubernetes部署"
        return
    }

    Write-ColorOutput "yellow" "🚀 开始部署到Kubernetes..."

    $namespace = "ioe-dream-$Environment"

    # 检查kubectl是否可用
    try {
        kubectl cluster-info > $null 2>&1
    } catch {
        Write-ColorOutput "red" "❌ 无法连接到Kubernetes集群"
        exit 1
    }

    # 创建命名空间
    if ($DryRun) {
        Write-ColorOutput "cyan" "[预览] 将创建命名空间: $namespace"
    } else {
        $namespaceExists = kubectl get namespace $namespace 2>$null
        if (-not $namespaceExists) {
            kubectl create namespace $namespace
            Write-ColorOutput "green" "✅ 命名空间 $namespace 创建成功"
        }
    }

    # 应用ConfigMap和Secret
    $configFiles = @(
        "deployment/kubernetes/namespace.yaml",
        "deployment/kubernetes/configmap.yaml",
        "deployment/kubernetes/secrets.yaml"
    )

    foreach ($configFile in $configFiles) {
        if (Test-Path $configFile) {
            if ($DryRun) {
                Write-ColorOutput "cyan" "[预览] 将应用配置: $configFile"
            } else {
                kubectl apply -f $configFile
                Write-ColorOutput "green" "✅ 配置应用成功: $configFile"
            }
        } else {
            Write-ColorOutput "yellow" "⚠️  配置文件不存在: $configFile"
        }
    }

    # 部署服务
    $servicesToDeploy = @()

    if ($Service -eq "all") {
        $servicesToDeploy = @(
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
    } else {
        $servicesToDeploy = @("${Service}-service")
    }

    foreach ($serviceName in $servicesToDeploy) {
        $serviceFile = "deployment/kubernetes/services/${serviceName}.yaml"

        if (Test-Path $serviceFile) {
            Write-ColorOutput "cyan" "部署服务: $serviceName"

            if ($DryRun) {
                Write-ColorOutput "cyan" "[预览] 将应用: $serviceFile"
                continue
            }

            # 更新镜像标签
            $tempFile = "$serviceFile.tmp"
            Copy-Item $serviceFile $tempFile

            try {
                (Get-Content $tempFile) -replace 'image: .*:latest', "image: ioe-dream/${serviceName}:$ImageTag" | Set-Content $tempFile

                kubectl apply -f $tempFile -n $namespace
                Write-ColorOutput "green" "✅ $serviceName 部署成功"
            } finally {
                Remove-Item $tempFile -Force -ErrorAction SilentlyContinue
            }
        } else {
            Write-ColorOutput "yellow" "⚠️  服务文件不存在: $serviceFile"
        }
    }

    # 等待部署完成
    if (-not $DryRun) {
        Write-ColorOutput "yellow" "⏳ 等待部署完成..."

        foreach ($serviceName in $servicesToDeploy) {
            $deploymentName = "ioedream-$serviceName"

            $timeoutSeconds = $Timeout
            $startTime = Get-Date

            while ((Get-Date) -lt $startTime.AddSeconds($timeoutSeconds)) {
                $status = kubectl rollout status deployment/$deploymentName -n $namespace --timeout=30s 2>$null
                if ($LASTEXITCODE -eq 0) {
                    Write-ColorOutput "green" "✅ $deploymentName 部署完成"
                    break
                }

                Write-ColorOutput "yellow" "⏳ 等待 $deploymentName 部署中..."
                Start-Sleep 10
            }
        }
    }
}

# 健康检查
function Invoke-HealthCheck {
    if ($SkipDeploy -or $DryRun) {
        Write-ColorOutput "yellow" "⚠️  跳过健康检查"
        return
    }

    Write-ColorOutput "yellow" "🔍 执行健康检查..."

    $namespace = "ioe-dream-$Environment"
    $servicesToCheck = @()

    if ($Service -eq "all") {
        $servicesToCheck = @(
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
    } else {
        $servicesToCheck = @("ioedream-${Service}-service")
    }

    foreach ($serviceName in $servicesToCheck) {
        Write-ColorOutput "cyan" "检查服务: $serviceName"

        # 检查Pod状态
        $podStatus = kubectl get pods -n $namespace -l app=$serviceName -o jsonpath='{.items[*].status.phase}' 2>$null
        if ($podStatus -and $podStatus -contains "Running") {
            Write-ColorOutput "green" "✅ $serviceName Pod运行正常"
        } else {
            Write-ColorOutput "red" "❌ $serviceName Pod状态异常: $podStatus"
        }

        # 检查服务端点
        $serviceInfo = kubectl get service $serviceName -n $namespace -o jsonpath='{.spec.clusterIP}' 2>$null
        if ($serviceInfo -and $serviceInfo -ne "None") {
            Write-ColorOutput "green" "✅ $serviceName 服务端点: $serviceInfo"
        } else {
            Write-ColorOutput "yellow" "⚠️  $serviceName 服务端点未找到"
        }
    }
}

# 显示部署状态
function Show-DeploymentStatus {
    Write-ColorOutput "blue" "===================================================="
    Write-ColorOutput "blue" "📊 部署状态概览"
    Write-ColorOutput "blue" "===================================================="

    if ($SkipDeploy) {
        Write-ColorOutput "yellow" "⚠️  跳过部署，显示构建状态"
        return
    }

    if ($DryRun) {
        Write-ColorOutput "cyan" "[预览] 将显示部署状态..."
        return
    }

    $namespace = "ioe-dream-$Environment"
    Write-ColorOutput "cyan" "命名空间: $namespace"
    Write-ColorOutput "cyan" "环境: $Environment"
    Write-ColorOutput ""

    # 显示Pod状态
    Write-ColorOutput "yellow" "📦 Pod状态:"
    kubectl get pods -n $namespace -o wide
    Write-ColorOutput ""

    # 显示Service状态
    Write-ColorOutput "yellow" "🌐 Service状态:"
    kubectl get services -n $namespace
    Write-ColorOutput ""

    # 显示Deployment状态
    Write-ColorOutput "yellow" "🚀 Deployment状态:"
    kubectl get deployments -n $namespace
}

# 清理临时文件
function Remove-TemporaryFiles {
    Write-ColorOutput "cyan" "🧹 清理临时文件..."

    # 清理临时文件
    Get-ChildItem -Path "." -Filter "*.tmp" -Recurse | Remove-Item -Force -ErrorAction SilentlyContinue
}

# 错误处理
function Handle-Error {
    param(
        [string]$ErrorMessage,
        [Exception]$Exception
    )

    Write-ColorOutput "red" "❌ $ErrorMessage"
    Write-ColorOutput "red" "错误详情: $($Exception.Message)"

    # 清理
    Remove-TemporaryFiles

    exit 1
}

# 主执行函数
function Main {
    try {
        # 打印标题
        Write-Title

        # 检查依赖
        Test-Dependencies

        # 构建阶段
        if (-not $SkipBuild) {
            Build-Services
            Invoke-Tests
            Build-DockerImages
        }

        # 部署阶段
        Deploy-ToKubernetes

        # 健康检查
        if (-not $SkipDeploy) {
            Invoke-HealthCheck
        }

        # 显示状态
        Show-DeploymentStatus

        Write-ColorOutput "green" "🎉 构建和部署完成!"

    } catch {
        Handle-Error "构建和部署过程出现错误" $_
    } finally {
        # 清理
        Remove-TemporaryFiles
    }
}

# 执行主函数
Main