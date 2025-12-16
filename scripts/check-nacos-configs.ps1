# =====================================================
# IOE-DREAM Nacos配置检查脚本
# 版本: v1.0.0
# 描述: 检查Nacos中是否存在所有必需的共享配置文件
# 创建时间: 2025-12-15
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$NacosServer = "127.0.0.1:8848",

    [Parameter(Mandatory=$false)]
    [string]$Namespace = "dev",

    [Parameter(Mandatory=$false)]
    [string]$Group = "IOE-DREAM",

    [Parameter(Mandatory=$false)]
    [string]$Username = "nacos",

    [Parameter(Mandatory=$false)]
    [string]$Password = "nacos"
)

$ErrorActionPreference = "Continue"

# =====================================================
# 必需的配置文件列表
# =====================================================
$RequiredConfigs = @(
    # 共享配置
    @{ DataId = "common-gateway.yaml"; Description = "网关公共配置"; Required = $true },
    @{ DataId = "common-security.yaml"; Description = "安全公共配置"; Required = $true },
    @{ DataId = "common-monitoring.yaml"; Description = "监控公共配置"; Required = $true },
    @{ DataId = "common-config.yaml"; Description = "通用公共配置"; Required = $true },

    # 各服务配置
    @{ DataId = "ioedream-gateway-service.yaml"; Description = "网关服务配置"; Required = $true },
    @{ DataId = "ioedream-gateway-service-ext.yaml"; Description = "网关服务扩展配置"; Required = $false },
    @{ DataId = "ioedream-common-service.yaml"; Description = "通用业务服务配置"; Required = $true },
    @{ DataId = "ioedream-device-comm-service.yaml"; Description = "设备通信服务配置"; Required = $true },
    @{ DataId = "ioedream-oa-service.yaml"; Description = "OA办公服务配置"; Required = $true },
    @{ DataId = "ioedream-access-service.yaml"; Description = "门禁服务配置"; Required = $true },
    @{ DataId = "ioedream-attendance-service.yaml"; Description = "考勤服务配置"; Required = $true },
    @{ DataId = "ioedream-video-service.yaml"; Description = "视频服务配置"; Required = $true },
    @{ DataId = "ioedream-consume-service.yaml"; Description = "消费服务配置"; Required = $true },
    @{ DataId = "ioedream-visitor-service.yaml"; Description = "访客服务配置"; Required = $true }
)

# =====================================================
# 辅助函数
# =====================================================

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Get-NacosToken {
    param(
        [string]$Server,
        [string]$Username,
        [string]$Password
    )

    try {
        $loginUrl = "http://$Server/nacos/v1/auth/login"
        $body = "username=$Username&password=$Password"

        $response = Invoke-WebRequest -Uri $loginUrl -Method POST -Body $body -ContentType "application/x-www-form-urlencoded" -ErrorAction Stop

        if ($response.StatusCode -eq 200) {
            $json = $response.Content | ConvertFrom-Json
            return $json.accessToken
        }
    } catch {
        Write-ColorOutput "  [WARN] 无法获取Nacos Token: $($_.Exception.Message)" "Yellow"
        return $null
    }

    return $null
}

function Test-NacosConfig {
    param(
        [string]$Server,
        [string]$DataId,
        [string]$Group,
        [string]$Namespace,
        [string]$Token
    )

    try {
        $url = "http://$Server/nacos/v1/cs/configs?dataId=$DataId&group=$Group&tenant=$Namespace"

        if ($Token) {
            $url += "&accessToken=$Token"
        }

        $response = Invoke-WebRequest -Uri $url -Method GET -ErrorAction Stop

        if ($response.StatusCode -eq 200 -and $response.Content -and $response.Content.Length -gt 0) {
            return @{
                Exists = $true
                ContentLength = $response.Content.Length
            }
        }
    } catch [System.Net.WebException] {
        # 404表示配置不存在
        return @{
            Exists = $false
            ContentLength = 0
        }
    } catch {
        return @{
            Exists = $false
            Error = $_.Exception.Message
        }
    }

    return @{
        Exists = $false
        ContentLength = 0
    }
}

# =====================================================
# 主程序
# =====================================================

Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  IOE-DREAM Nacos 配置检查工具" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""
Write-ColorOutput "检查参数:" "Gray"
Write-ColorOutput "  Nacos Server: $NacosServer" "Gray"
Write-ColorOutput "  Namespace: $Namespace" "Gray"
Write-ColorOutput "  Group: $Group" "Gray"
Write-ColorOutput ""

# 1. 检查Nacos服务是否可用
Write-ColorOutput "[Step 1] 检查Nacos服务连接..." "Cyan"

try {
    $healthUrl = "http://$NacosServer/nacos/v1/ns/service/list?pageNo=1&pageSize=1"
    $null = Invoke-WebRequest -Uri $healthUrl -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-ColorOutput "  [OK] Nacos服务可用" "Green"
} catch {
    Write-ColorOutput "  [ERROR] 无法连接Nacos服务: $($_.Exception.Message)" "Red"
    Write-ColorOutput ""
    Write-ColorOutput "请确保:" "Yellow"
    Write-ColorOutput "  1. Nacos服务已启动" "Yellow"
    Write-ColorOutput "  2. 服务地址正确: $NacosServer" "Yellow"
    Write-ColorOutput "  3. 网络连接正常" "Yellow"
    Write-ColorOutput ""
    exit 1
}

# 2. 获取Token
Write-ColorOutput ""
Write-ColorOutput "[Step 2] 获取认证Token..." "Cyan"
$token = Get-NacosToken -Server $NacosServer -Username $Username -Password $Password

if ($token) {
    Write-ColorOutput "  [OK] Token获取成功" "Green"
} else {
    Write-ColorOutput "  [WARN] Token获取失败，尝试无认证访问" "Yellow"
}

# 3. 检查配置文件
Write-ColorOutput ""
Write-ColorOutput "[Step 3] 检查配置文件..." "Cyan"
Write-ColorOutput ""

$existCount = 0
$missingCount = 0
$missingRequired = @()

foreach ($config in $RequiredConfigs) {
    $result = Test-NacosConfig -Server $NacosServer -DataId $config.DataId -Group $Group -Namespace $Namespace -Token $token

    $statusIcon = if ($result.Exists) { "[OK]" } else { "[XX]" }
    $statusColor = if ($result.Exists) { "Green" } else { if ($config.Required) { "Red" } else { "Yellow" } }
    $requiredTag = if ($config.Required) { "*" } else { " " }

    if ($result.Exists) {
        Write-ColorOutput "  $statusIcon$requiredTag $($config.DataId) - $($config.Description) ($($result.ContentLength) bytes)" $statusColor
        $existCount++
    } else {
        Write-ColorOutput "  $statusIcon$requiredTag $($config.DataId) - $($config.Description)" $statusColor
        $missingCount++
        if ($config.Required) {
            $missingRequired += $config
        }
    }
}

# 4. 输出汇总
Write-ColorOutput ""
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  检查结果汇总" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""
Write-ColorOutput "  总配置数: $($RequiredConfigs.Count)" "White"
Write-ColorOutput "  已存在: $existCount" "Green"
Write-ColorOutput "  缺失: $missingCount" $(if ($missingCount -gt 0) { "Yellow" } else { "Green" })
Write-ColorOutput ""

if ($missingRequired.Count -gt 0) {
    Write-ColorOutput "  [警告] 以下必需配置缺失:" "Red"
    foreach ($config in $missingRequired) {
        Write-ColorOutput "    - $($config.DataId): $($config.Description)" "Red"
    }
    Write-ColorOutput ""
    Write-ColorOutput "  解决方案:" "Yellow"
    Write-ColorOutput "    1. 登录Nacos控制台: http://$NacosServer/nacos" "Yellow"
    Write-ColorOutput "    2. 进入 配置管理 -> 配置列表" "Yellow"
    Write-ColorOutput "    3. 创建缺失的配置文件" "Yellow"
    Write-ColorOutput "    4. 参考模板: microservices/config-templates/" "Yellow"
    Write-ColorOutput ""
} else {
    Write-ColorOutput "  [成功] 所有必需配置已就绪!" "Green"
    Write-ColorOutput ""
}

Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  检查完成" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""

# 返回缺失必需配置的数量作为退出码
exit $missingRequired.Count
