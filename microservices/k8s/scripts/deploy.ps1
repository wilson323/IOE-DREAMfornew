# IOE-DREAM微服务集群部署脚本 (PowerShell)
# 支持：Kubernetes、Helm、监控、日志完整部署

param(
    [string]$Environment = "production",
    [string]$Namespace = "ioedream-prod",
    [string]$HelmReleaseName = "ioedream",
    [switch]$DryRun = $false,
    [switch]$SkipDependencies = $false,
    [switch]$SkipMonitoring = $false,
    [switch]$SkipTests = $false,
    [string]$Timeout = "600s",
    [switch]$ValidateOnly = $false,
    [switch]$Status = $false,
    [switch]$Rollback = $false,
    [switch]$Upgrade = $false,
    [switch]$Uninstall = $false,
    [switch]$Logs = $false,
    [switch]$Test = $false,
    [switch]$Help = $false
)

# 脚本配置
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$K8sDir = $ProjectRoot
$HelmDir = "$K8sDir\helm"

# 颜色定义
$Colors = @{
    Red = "Red"
    Green = "Green"
    Yellow = "Yellow"
    Blue = "Blue"
    Cyan = "Cyan"
    Magenta = "Magenta"
    White = "White"
}

# 日志函数
function Write-Log {
    param([string]$Message, [string]$Color = "White")
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $Message" -ForegroundColor $Colors[$Color]
}

function Write-Info { Write-Log "[INFO] $args" -Color "Blue" }
function Write-Success { Write-Log "[SUCCESS] $args" -Color "Green" }
function Write-Warning { Write-Log "[WARNING] $args" -Color "Yellow" }
function Write-Error { Write-Log "[ERROR] $args" -Color "Red" }

function Write-Header {
    Write-Log "========================================" -Color "Magenta"
    Write-Log "$args" -Color "Magenta"
    Write-Log "========================================" -Color "Magenta"
}

# 显示帮助信息
function Show-Help {
    @"
IOE-DREAM微服务集群部署脚本 (PowerShell)

用法: .\deploy.ps1 [参数]

参数:
    -Environment <env>        部署环境 (production|staging) [默认: production]
    -Namespace <ns>          Kubernetes命名空间 [默认: ioedream-prod]
    -HelmReleaseName <name>   Helm发布名称 [默认: ioedream]
    -DryRun                  干运行模式
    -SkipDependencies        跳过依赖服务部署
    -SkipMonitoring          跳过监控组件部署
    -SkipTests               跳过部署后测试
    -Timeout <time>          操作超时时间 [默认: 600s]
    -ValidateOnly            仅验证配置
    -Status                  查看部署状态
    -Rollback                回滚部署
    -Upgrade                 升级部署
    -Uninstall               卸载部署
    -Logs                    查看日志
    -Test                    运行测试
    -Help                    显示帮助信息

环境变量:
    $env:ENVIRONMENT         部署环境
    $env:NAMESPACE           命名空间
    $env:HELM_RELEASE_NAME   Helm发布名称

命令示例:
    .\deploy.ps1                                   # 生产环境完整部署
    .\deploy.ps1 -Environment staging -Namespace ioedream-staging  # 预发布环境
    .\deploy.ps1 -DryRun                           # 干运行验证
    .\deploy.ps1 -Status                           # 查看部署状态
    .\deploy.ps1 -Rollback                         # 回滚部署
    .\deploy.ps1 -Uninstall                        # 卸载部署

环境要求:
    - Kubernetes 1.24+
    - Helm 3.8+
    - kubectl configured
    - PowerShell 5.1+ or PowerShell Core
    - 足够的集群资源

"@
}

# 检查依赖
function Test-Dependencies {
    Write-Info "检查系统依赖..."

    # 检查kubectl
    try {
        kubectl version --client = $null 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "kubectl未安装或不可用"
        }
        Write-Success "kubectl检查通过"
    } catch {
        Write-Error "kubectl检查失败: $($_.Exception.Message)"
        exit 1
    }

    # 检查Helm
    try {
        helm version = $null 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Helm未安装或不可用"
        }
        Write-Success "Helm检查通过"
    } catch {
        Write-Error "Helm检查失败: $($_.Exception.Message)"
        exit 1
    }

    # 检查集群连接
    try {
        kubectl cluster-info = $null 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "无法连接到Kubernetes集群"
        }
        Write-Success "集群连接检查通过"
    } catch {
        Write-Error "集群连接检查失败: $($_.Exception.Message)"
        exit 1
    }

    # 更新Helm仓库
    Write-Info "更新Helm仓库..."
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
    helm repo add grafana https://grafana.github.io/helm-charts
    helm repo add bitnami https://charts.bitnami.com/bitnami
    helm repo update

    Write-Success "依赖检查完成"
}

# 验证集群资源
function Test-ClusterResources {
    Write-Info "验证集群资源..."

    # 检查可用节点
    $readyNodes = (kubectl get nodes --no-headers | Where-Object { $_ -match " Ready " }).Count
    if ($readyNodes -lt 2) {
        Write-Warning "可用节点较少: $readyNodes，建议至少2个节点"
    } else {
        Write-Success "可用节点: $readyNodes"
    }

    # 检查存储类
    $storageClasses = (kubectl get storageclass --no-headers).Count
    if ($storageClasses -eq 0) {
        Write-Warning "未找到存储类，持久卷可能无法正常工作"
    } else {
        Write-Success "存储类: $storageClasses"
    }

    Write-Success "集群资源验证完成"
}

# 创建命名空间
function New-Namespace {
    Write-Info "创建命名空间: $Namespace"

    $nsExists = kubectl get namespace $Namespace 2>$null
    if ($LASTEXITCODE -ne 0) {
        kubectl create namespace $Namespace
        Write-Success "命名空间创建成功: $Namespace"
    } else {
        Write-Info "命名空间已存在: $Namespace"
    }

    # 添加标签
    kubectl label namespace $Namespace `
        environment=$Environment `
        project="ioedream" `
        managed-by="helm" `
        --overwrite
}

# 创建必要的Secret
function New-Secrets {
    Write-Info "创建必要的Secret..."

    # 镜像拉取密钥
    $secretExists = kubectl get secret ioedream-registry-secret -n $Namespace 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "镜像拉取密钥不存在，请手动创建:"
        Write-Warning "kubectl create secret docker-registry ioedream-registry-secret --docker-server=<registry> --docker-username=<username> --docker-password=<password> -n $Namespace"
    }

    # 数据库密码
    $secretExists = kubectl get secret ioedream-db-secrets -n $Namespace 2>$null
    if ($LASTEXITCODE -ne 0) {
        $timestamp = Get-Date -Format "yyyyMMddHHmmss"
        kubectl create secret generic ioedream-db-secrets `
            --from-literal=mysql-root-password="ioedream_root_password_$timestamp" `
            --from-literal=mysql-password="ioedream_password_$timestamp" `
            -n $Namespace
        Write-Success "数据库密钥创建成功"
    }

    # JWT密钥
    $secretExists = kubectl get secret ioedream-jwt-secrets -n $Namespace 2>$null
    if ($LASTEXITCODE -ne 0) {
        $jwtSecret = -join ((65..90) + (97..122) | Get-Random -Count 32 | ForEach-Object {[char]$_})
        kubectl create secret generic ioedream-jwt-secrets `
            --from-literal=jwt-secret="ioedream_jwt_secret_$jwtSecret" `
            -n $Namespace
        Write-Success "JWT密钥创建成功"
    }

    Write-Success "Secret创建完成"
}

# 部署基础设施
function Deploy-Infrastructure {
    Write-Header "部署基础设施组件"

    if (-not $SkipDependencies) {
        # MySQL数据库
        Write-Info "部署MySQL数据库..."
        $helmArgs = @(
            "upgrade", "--install", "mysql-ioedream", "bitnami/mysql",
            "--namespace", $Namespace,
            "--set", "auth.rootPassword=ioedream_root_password",
            "--set", "auth.database=ioedream_$Environment",
            "--set", "primary.persistence.size=100Gi",
            "--set", "architecture=replication",
            "--set", "secondary.replicaCount=2",
            "--timeout", $Timeout,
            "--wait"
        )
        if ($DryRun) { $helmArgs += "--dry-run" }

        helm @helmArgs
        Write-Success "MySQL数据库部署完成"

        # Redis缓存
        Write-Info "部署Redis缓存..."
        $helmArgs = @(
            "upgrade", "--install", "redis-ioedream", "bitnami/redis",
            "--namespace", $Namespace,
            "--set", "auth.password=ioedream_redis_password",
            "--set", "master.persistence.size=20Gi",
            "--set", "replica.replicaCount=3",
            "--set", "architecture=replication",
            "--timeout", $Timeout,
            "--wait"
        )
        if ($DryRun) { $helmArgs += "--dry-run" }

        helm @helmArgs
        Write-Success "Redis缓存部署完成"

        # Nacos服务发现
        Write-Info "部署Nacos服务发现..."
        $helmArgs = @(
            "upgrade", "--install", "nacos-ioedream", "bitnami/nacos",
            "--namespace", $Namespace,
            "--set", "auth.enabled=true",
            "--set", "auth.adminUser=nacos",
            "--set", "auth.adminPassword=ioedream_nacos_password",
            "--set", "persistence.size=20Gi",
            "--set", "replicaCount=3",
            "--timeout", $Timeout,
            "--wait"
        )
        if ($DryRun) { $helmArgs += "--dry-run" }

        helm @helmArgs
        Write-Success "Nacos服务发现部署完成"
    } else {
        Write-Info "跳过基础设施部署"
    }
}

# 部署监控组件
function Deploy-Monitoring {
    if ($SkipMonitoring) {
        Write-Info "跳过监控组件部署"
        return
    }

    Write-Header "部署监控组件"

    # Prometheus
    Write-Info "部署Prometheus..."
    $helmArgs = @(
        "upgrade", "--install", "prometheus-ioedream", "prometheus-community/kube-prometheus-stack",
        "--namespace", $Namespace,
        "--set", "prometheus.prometheusSpec.retention=30d",
        "--set", "prometheus.prometheusSpec.storageSpec.volumeClaimTemplate.spec.resources.requests.storage=50Gi",
        "--set", "grafana.adminPassword=ioedream_grafana_password",
        "--set", "grafana.persistence.size=10Gi",
        "--timeout", $Timeout,
        "--wait"
    )
    if ($DryRun) { $helmArgs += "--dry-run" }

    helm @helmArgs
    Write-Success "Prometheus和Grafana部署完成"

    # ELK Stack (可选)
    if ($Environment -eq "production") {
        Write-Info "部署ELK Stack..."
        # 这里可以添加ELK部署逻辑
        Write-Warning "ELK Stack部署待完善"
    }
}

# 部署应用服务
function Deploy-Application {
    Write-Header "部署IOE-DREAM微服务应用"

    Push-Location "$HelmDir\ioedream"

    # 环境特定的values文件
    $valuesFile = "values.yaml"
    $envValuesFile = "values-$Environment.yaml"
    if (Test-Path $envValuesFile) {
        $valuesFile = $envValuesFile
    }

    Write-Info "使用配置文件: $valuesFile"

    # 检查Helm发布是否存在
    $releaseExists = helm status $HelmReleaseName -n $Namespace 2>$null
    $helmAction = if ($LASTEXITCODE -eq 0) { "upgrade" } else { "install" }

    # 部署或升级应用
    $helmArgs = @(
        $helmAction, $HelmReleaseName, ".",
        "--namespace", $Namespace,
        "--values", $valuesFile,
        "--set", "global.environment=$Environment",
        "--set", "global.namespace=$Namespace",
        "--timeout", $Timeout,
        "--wait"
    )
    if ($DryRun) { $helmArgs += "--dry-run" }

    helm @helmArgs
    Write-Success "IOE-DREAM微服务应用部署完成"

    Pop-Location
}

# 运行部署后测试
function Invoke-Tests {
    if ($SkipTests) {
        Write-Info "跳过部署后测试"
        return
    }

    Write-Header "运行部署后测试"

    # 等待Pod就绪
    Write-Info "等待所有Pod就绪..."
    kubectl wait --for=condition=ready pod `
        -l app.kubernetes.io/instance=$HelmReleaseName `
        -n $Namespace `
        --timeout=300s

    # 健康检查
    Write-Info "执行健康检查..."

    # 网关健康检查
    $gatewayUrl = kubectl get ingress -l app.kubernetes.io/name=smart-gateway -n $Namespace -o jsonpath='{.items[0].spec.rules[0].host}' 2>$null
    if ($gatewayUrl) {
        try {
            $response = Invoke-WebRequest -Uri "https://$gatewayUrl/health" -UseBasicParsing -TimeoutSec 10
            if ($response.StatusCode -eq 200) {
                Write-Success "网关健康检查通过: $gatewayUrl"
            } else {
                Write-Error "网关健康检查失败: $gatewayUrl (HTTP $($response.StatusCode))"
            }
        } catch {
            Write-Error "网关健康检查失败: $gatewayUrl ($($_.Exception.Message))"
        }
    }

    # 服务连通性测试
    Write-Info "测试服务连通性..."
    $services = @("smart-gateway", "ioedream-auth-service", "ioedream-consume-service")
    foreach ($service in $services) {
        $pods = kubectl get pod -l app=$service -n $Namespace --no-headers 2>$null
        if ($pods -and $pods -match "Running") {
            Write-Success "服务 $service 运行正常"
        } else {
            Write-Error "服务 $service 未正常运行"
        }
    }

    Write-Success "部署后测试完成"
}

# 显示部署状态
function Show-Status {
    Write-Header "部署状态信息"

    # Helm发布状态
    Write-Info "Helm发布状态:"
    helm list -n $Namespace

    # Pod状态
    Write-Info "Pod状态:"
    kubectl get pods -n $Namespace -l app.kubernetes.io/instance=$HelmReleaseName -o wide

    # Service状态
    Write-Info "Service状态:"
    kubectl get services -n $Namespace -l app.kubernetes.io/instance=$HelmReleaseName

    # Ingress状态
    Write-Info "Ingress状态:"
    kubectl get ingress -n $Namespace -l app.kubernetes.io/instance=$HelmReleaseName

    # HPA状态
    Write-Info "HPA状态:"
    kubectl get hpa -n $Namespace -l app.kubernetes.io/instance=$HelmReleaseName

    # 事件信息
    Write-Info "最近事件:"
    kubectl get events -n $Namespace --sort-by='.lastTimestamp' | Select-Object -Last 10
}

# 回滚部署
function Invoke-Rollback {
    Write-Warning "回滚部署..."

    # 检查历史版本
    $history = helm history $HelmReleaseName -n $Namespace
    if (-not $history) {
        Write-Error "未找到部署历史，无法回滚"
        exit 1
    }

    Write-Log "部署历史:" -Color "Cyan"
    $history

    $revision = Read-Host "请输入要回滚到的版本号"
    if ($revision) {
        helm rollback $HelmReleaseName $revision -n $Namespace --timeout $Timeout
        Write-Success "回滚完成，版本: $revision"
    } else {
        Write-Error "无效的版本号"
    }
}

# 升级部署
function Invoke-Upgrade {
    Write-Info "升级部署..."

    Push-Location "$HelmDir\ioedream"

    helm upgrade $HelmReleaseName . `
        --namespace $Namespace `
        --values "values-$Environment.yaml" `
        --timeout $Timeout `
        --wait

    Pop-Location

    Write-Success "部署升级完成"
}

# 卸载部署
function Invoke-Uninstall {
    Write-Warning "卸载IOE-DREAM微服务集群..."

    $confirm = Read-Host "确认卸载部署? 这将删除所有相关资源 [y/N]"
    if ($confirm -ne "y" -and $confirm -ne "Y") {
        Write-Info "取消卸载操作"
        exit 0
    }

    # 卸载应用
    $releaseExists = helm status $HelmReleaseName -n $Namespace 2>$null
    if ($LASTEXITCODE -eq 0) {
        helm uninstall $HelmReleaseName -n $Namespace
        Write-Success "应用卸载完成"
    }

    # 卸载监控组件
    if (-not $SkipMonitoring) {
        helm uninstall prometheus-ioedream -n $Namespace 2>$null
        Write-Success "监控组件卸载完成"
    }

    # 卸载基础设施
    if (-not $SkipDependencies) {
        helm uninstall mysql-ioedream -n $Namespace 2>$null
        helm uninstall redis-ioedream -n $Namespace 2>$null
        helm uninstall nacos-ioedream -n $Namespace 2>$null
        Write-Success "基础设施卸载完成"
    }

    # 删除PVC (可选)
    $deletePvc = Read-Host "是否删除持久卷数据? [y/N]"
    if ($deletePvc -eq "y" -or $deletePvc -eq "Y") {
        kubectl delete pvc --all -n $Namespace --timeout=60s
        Write-Warning "持久卷数据已删除"
    }

    Write-Success "集群卸载完成"
}

# 查看日志
function Show-Logs {
    Write-Header "查看服务日志"

    $service = Read-Host "请输入服务名称 (默认: gateway)"
    if (-not $service) {
        $service = "smart-gateway"
    }

    Write-Info "查看 $service 日志..."
    kubectl logs -l app=$service -n $Namespace --tail=100 -f
}

# 运行测试
function Invoke-IntegrationTests {
    Write-Header "运行集成测试"

    # 创建测试Job YAML
    $testJobYaml = @"
apiVersion: batch/v1
kind: Job
metadata:
  name: ioedream-integration-test
  namespace: $Namespace
spec:
  template:
    spec:
      restartPolicy: Never
      containers:
      - name: test
        image: curlimages/curl:latest
        command:
        - /bin/sh
        - -c
        - |
          echo "开始集成测试..."

          # 测试网关健康检查
          if curl -f http://smart-gateway:8080/actuator/health; then
            echo "✓ 网关健康检查通过"
          else
            echo "✗ 网关健康检查失败"
            exit 1
          fi

          # 测试认证服务
          if curl -f http://ioedream-auth-service:8081/actuator/health; then
            echo "✓ 认证服务健康检查通过"
          else
            echo "✗ 认证服务健康检查失败"
            exit 1
          fi

          echo "所有集成测试通过!"
"@

    # 应用测试Job
    $testJobYaml | kubectl apply -f -

    # 等待测试完成
    kubectl wait --for=condition=complete job/ioedream-integration-test -n $Namespace --timeout=300s

    # 显示测试结果
    kubectl logs job/ioedream-integration-test -n $Namespace

    # 清理测试Job
    kubectl delete job ioedream-integration-test -n $Namespace

    Write-Success "集成测试完成"
}

# 验证配置
function Test-Configuration {
    Write-Header "验证配置文件"

    Push-Location "$HelmDir\ioedream"

    # 验证Helm Chart
    Write-Info "验证Helm Chart..."
    helm lint . --values "values-$Environment.yaml"

    # 模拟渲染
    Write-Info "模拟渲染模板..."
    helm template $HelmReleaseName . `
        --namespace $Namespace `
        --values "values-$Environment.yaml" `
        --dry-run

    Pop-Location

    Write-Success "配置验证完成"
}

# 主函数
function Main {
    Write-Header "IOE-DREAM微服务集群部署工具 (PowerShell)"
    Write-Log "环境: $Environment" -Color "Cyan"
    Write-Log "命名空间: $Namespace" -Color "Cyan"
    Write-Log "Helm发布: $HelmReleaseName" -Color "Cyan"

    if ($Help) {
        Show-Help
        return
    }

    # 干运行模式
    if ($DryRun) {
        Write-Warning "干运行模式 - 不会实际部署资源"
    }

    # 特定功能执行
    if ($ValidateOnly) {
        Test-Configuration
        return
    }

    if ($Status) {
        Show-Status
        return
    }

    if ($Rollback) {
        Invoke-Rollback
        return
    }

    if ($Upgrade) {
        Invoke-Upgrade
        return
    }

    if ($Uninstall) {
        Invoke-Uninstall
        return
    }

    if ($Logs) {
        Show-Logs
        return
    }

    if ($Test) {
        Invoke-IntegrationTests
        return
    }

    # 完整部署流程
    Write-Info "开始部署流程..."

    Test-Dependencies
    Test-ClusterResources
    New-Namespace
    New-Secrets
    Deploy-Infrastructure
    Deploy-Monitoring
    Deploy-Application
    Invoke-Tests
    Show-Status

    Write-Header "部署完成"
    Write-Success "IOE-DREAM微服务集群部署成功!"

    if (-not $DryRun) {
        Write-Info "访问地址:"
        Write-Log "  - 网关: https://api.ioedream.com" -Color "Green"
        Write-Log "  - Grafana: https://monitor.ioedream.com/grafana" -Color "Green"
        Write-Log "  - 管理后台: https://admin.ioedream.com" -Color "Green"
        Write-Log ""
        Write-Info "管理命令:"
        Write-Log "  - 查看状态: .\deploy.ps1 -Status" -Color "Cyan"
        Write-Log "  - 查看日志: .\deploy.ps1 -Logs" -Color "Cyan"
        Write-Log "  - 回滚部署: .\deploy.ps1 -Rollback" -Color "Cyan"
        Write-Log "  - 升级部署: .\deploy.ps1 -Upgrade" -Color "Cyan"
        Write-Log "  - 卸载集群: .\deploy.ps1 -Uninstall" -Color "Cyan"
    }
}

# 执行主函数
Main