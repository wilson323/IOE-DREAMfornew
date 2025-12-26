# =====================================================
# IOE-DREAM 企业级Maven构建脚本
# 版本: v1.0.0
# 描述: 按正确顺序构建所有模块，避免依赖问题
# 创建时间: 2025-12-15
# =====================================================

param(
    [Parameter(Mandatory = $false)]
    [ValidateSet("full", "common", "services", "single")]
    [string]$BuildMode = "full",

    [Parameter(Mandatory = $false)]
    [string]$Service = "",

    [Parameter(Mandatory = $false)]
    [switch]$SkipTests,

    [Parameter(Mandatory = $false)]
    [switch]$SkipQuality,

    [Parameter(Mandatory = $false)]
    [switch]$Clean,

    [Parameter(Mandatory = $false)]
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$MicroservicesDir = Join-Path $ProjectRoot "microservices"

# =====================================================
# 模块构建顺序（关键！）
# =====================================================

# 第一层：无依赖的核心模块
$Layer1_CoreModules = @(
    "microservices-common-core"
)

# 第二层：依赖core的功能模块（包括entity和storage）
$Layer2_FunctionModules = @(
    "microservices-common-entity",
    "microservices-common-storage",
    "microservices-common-security",
    "microservices-common-data",
    "microservices-common-cache",
    "microservices-common-export",
    "microservices-common-workflow",
    "microservices-common-monitor",
    "microservices-common-permission"
)

# 第三层：依赖功能模块的业务公共模块
$Layer3_BusinessCommon = @(
    "microservices-common-business"
)

# 第四层：聚合公共模块
$Layer4_CommonAggregator = @(
    "microservices-common"
)

# 第五层：工具模块（无Spring Boot主类）
$Layer5_ToolModules = @(
    "ioedream-db-init"
)

# 第六层：业务服务（有Spring Boot主类）
$Layer6_Services = @(
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

# =====================================================
# 辅助函数
# =====================================================

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Build-Module {
    param(
        [string]$ModuleName,
        [string]$Goal = "install",
        [bool]$SkipTests = $true,
        [bool]$SkipQuality = $true
    )

    $modulePath = Join-Path $MicroservicesDir $ModuleName

    if (-not (Test-Path $modulePath)) {
        Write-ColorOutput "  [SKIP] $ModuleName - 目录不存在" "Yellow"
        return $true
    }

    # 使用 -pl 和 -am 确保依赖模块也被构建
    $mvnArgs = @("clean", $Goal, "-pl", $ModuleName, "-am")

    if ($SkipTests) {
        $mvnArgs += "-DskipTests"
    }

    if ($SkipQuality) {
        $mvnArgs += "-Dpmd.skip=true"
        $mvnArgs += "-Dcheckstyle.skip=true"
        $mvnArgs += "-Dspotbugs.skip=true"
        $mvnArgs += "-Djacoco.skip=true"
    }

    # 不在静默模式，显示关键信息
    # $mvnArgs += "-q"

    Write-ColorOutput "  [BUILD] $ModuleName..." "Cyan"

    try {
        Push-Location $MicroservicesDir
        $output = & mvn $mvnArgs 2>&1 | Out-String

        # 检查构建结果
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "  [OK] $ModuleName" "Green"
            Pop-Location
            return $true
        }
        else {
            Write-ColorOutput "  [FAIL] $ModuleName (exit code: $LASTEXITCODE)" "Red"
            # 显示关键错误信息
            $errorLines = $output -split "`n" | Select-String -Pattern "ERROR|FAILURE" | Select-Object -First 5
            if ($errorLines) {
                Write-ColorOutput "  错误摘要:" "Yellow"
                $errorLines | ForEach-Object { Write-ColorOutput "    $_" "Red" }
            }
            Pop-Location
            return $false
        }
    }
    catch {
        Write-ColorOutput "  [ERROR] ${ModuleName} - $($_.Exception.Message)" "Red"
        Pop-Location
        return $false
    }
}

function Build-Layer {
    param(
        [string]$LayerName,
        [string[]]$Modules,
        [string]$Goal = "install"
    )

    Write-ColorOutput "" "White"
    Write-ColorOutput "========================================" "Cyan"
    Write-ColorOutput "  $LayerName" "Cyan"
    Write-ColorOutput "========================================" "Cyan"

    $successCount = 0
    $failCount = 0

    foreach ($module in $Modules) {
        $result = Build-Module -ModuleName $module -Goal $Goal -SkipTests $SkipTests -SkipQuality $SkipQuality

        if ($result) {
            $successCount++
        }
        else {
            $failCount++
            if (-not $Verbose) {
                Write-ColorOutput "" "White"
                Write-ColorOutput "  [STOP] 构建失败，停止后续模块" "Red"
                return $false
            }
        }
    }

    Write-ColorOutput "  成功: $successCount / $($Modules.Count)" $(if ($failCount -eq 0) { "Green" } else { "Yellow" })
    return ($failCount -eq 0)
}

function Install-ParentPom {
    Write-ColorOutput "" "White"
    Write-ColorOutput "========================================" "Cyan"
    Write-ColorOutput "  安装父POM" "Cyan"
    Write-ColorOutput "========================================" "Cyan"

    try {
        Push-Location $MicroservicesDir

        $mvnArgs = @("install", "-N", "-q")
        if ($SkipQuality) {
            $mvnArgs += "-Dpmd.skip=true"
            $mvnArgs += "-Dcheckstyle.skip=true"
        }

        Write-ColorOutput "  [BUILD] ioedream-microservices-parent..." "Cyan"
        $process = Start-Process -FilePath "mvn" -ArgumentList $mvnArgs -NoNewWindow -Wait -PassThru

        if ($process.ExitCode -eq 0) {
            Write-ColorOutput "  [OK] 父POM安装成功" "Green"
            Pop-Location
            return $true
        }
        else {
            Write-ColorOutput "  [FAIL] 父POM安装失败" "Red"
            Pop-Location
            return $false
        }
    }
    catch {
        Write-ColorOutput "  [ERROR] $($_.Exception.Message)" "Red"
        Pop-Location
        return $false
    }
}

# =====================================================
# 主程序
# =====================================================

$startTime = Get-Date

Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  IOE-DREAM 企业级Maven构建" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""
Write-ColorOutput "  构建模式: $BuildMode" "Gray"
Write-ColorOutput "  跳过测试: $SkipTests" "Gray"
Write-ColorOutput "  跳过质量检查: $SkipQuality" "Gray"
Write-ColorOutput "  开始时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "Gray"

# 清理（如果需要）
if ($Clean) {
    Write-ColorOutput "" "White"
    Write-ColorOutput "  [CLEAN] 清理所有target目录..." "Yellow"
    Push-Location $MicroservicesDir
    $null = mvn clean -q 2>&1
    Pop-Location
    Write-ColorOutput "  [OK] 清理完成" "Green"
}

$success = $true

switch ($BuildMode) {
    "full" {
        # 完整构建：按顺序构建所有模块
        $success = Install-ParentPom
        if ($success) { $success = Build-Layer -LayerName "Layer 1: Core Modules" -Modules $Layer1_CoreModules }
        if ($success) { $success = Build-Layer -LayerName "Layer 2: Function Modules" -Modules $Layer2_FunctionModules }
        if ($success) { $success = Build-Layer -LayerName "Layer 3: Business Common" -Modules $Layer3_BusinessCommon }
        if ($success) { $success = Build-Layer -LayerName "Layer 4: Common Aggregator" -Modules $Layer4_CommonAggregator }
        if ($success) { $success = Build-Layer -LayerName "Layer 5: Tool Modules" -Modules $Layer5_ToolModules }
        if ($success) { $success = Build-Layer -LayerName "Layer 6: Business Services" -Modules $Layer6_Services -Goal "package" }
    }

    "common" {
        # 只构建公共模块
        $success = Install-ParentPom
        if ($success) { $success = Build-Layer -LayerName "Layer 1: Core Modules" -Modules $Layer1_CoreModules }
        if ($success) { $success = Build-Layer -LayerName "Layer 2: Function Modules" -Modules $Layer2_FunctionModules }
        if ($success) { $success = Build-Layer -LayerName "Layer 3: Business Common" -Modules $Layer3_BusinessCommon }
        if ($success) { $success = Build-Layer -LayerName "Layer 4: Common Aggregator" -Modules $Layer4_CommonAggregator }
    }

    "services" {
        # 只构建业务服务（假设公共模块已安装）
        $success = Build-Layer -LayerName "Business Services" -Modules $Layer6_Services -Goal "package"
    }

    "single" {
        # 构建单个服务
        if (-not $Service) {
            Write-ColorOutput "  [ERROR] 单服务模式需要指定 -Service 参数" "Red"
            exit 1
        }

        $allModules = $Layer1_CoreModules + $Layer2_FunctionModules + $Layer3_BusinessCommon + $Layer4_CommonAggregator + $Layer5_ToolModules + $Layer6_Services
        if ($Service -notin $allModules) {
            Write-ColorOutput "  [ERROR] 未知服务: $Service" "Red"
            Write-ColorOutput "  可用服务: $($allModules -join ', ')" "Gray"
            exit 1
        }

        $goal = if ($Service -in $Layer6_Services) { "package" } else { "install" }
        $success = Build-Module -ModuleName $Service -Goal $goal -SkipTests $SkipTests -SkipQuality $SkipQuality
    }
}

$endTime = Get-Date
$duration = $endTime - $startTime

Write-ColorOutput "" "White"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  构建结果" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""

if ($success) {
    Write-ColorOutput "  [SUCCESS] 构建成功!" "Green"
}
else {
    Write-ColorOutput "  [FAILED] 构建失败!" "Red"
}

Write-ColorOutput "  耗时: $([math]::Round($duration.TotalSeconds, 1)) 秒" "Gray"
Write-ColorOutput "  结束时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "Gray"
Write-ColorOutput ""

if (-not $success) {
    Write-ColorOutput "  常见问题排查:" "Yellow"
    Write-ColorOutput "    1. 确保JAVA_HOME指向Java 17+" "Gray"
    Write-ColorOutput "    2. 确保Maven 3.8.6+" "Gray"
    Write-ColorOutput "    3. 检查网络连接（下载依赖）" "Gray"
    Write-ColorOutput "    4. 尝试: mvn dependency:resolve" "Gray"
    Write-ColorOutput ""
    exit 1
}

exit 0
    Write-ColorOutput "    4. 尝试: mvn dependency:resolve" "Gray"
    Write-ColorOutput ""
    exit 1
}

exit 0

        $allModules = $Layer1_CoreModules + $Layer2_FunctionModules + $Layer3_BusinessCommon + $Layer4_CommonAggregator + $Layer5_ToolModules + $Layer6_Services
        if ($Service -notin $allModules) {
            Write-ColorOutput "  [ERROR] 未知服务: $Service" "Red"
            Write-ColorOutput "  可用服务: $($allModules -join ', ')" "Gray"
            exit 1
        }

        $goal = if ($Service -in $Layer6_Services) { "package" } else { "install" }
        $success = Build-Module -ModuleName $Service -Goal $goal -SkipTests $SkipTests -SkipQuality $SkipQuality
    }
}

$endTime = Get-Date
$duration = $endTime - $startTime

Write-ColorOutput "" "White"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  构建结果" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""

if ($success) {
    Write-ColorOutput "  [SUCCESS] 构建成功!" "Green"
} else {
    Write-ColorOutput "  [FAILED] 构建失败!" "Red"
}

Write-ColorOutput "  耗时: $([math]::Round($duration.TotalSeconds, 1)) 秒" "Gray"
Write-ColorOutput "  结束时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "Gray"
Write-ColorOutput ""

if (-not $success) {
    Write-ColorOutput "  常见问题排查:" "Yellow"
    Write-ColorOutput "    1. 确保JAVA_HOME指向Java 17+" "Gray"
    Write-ColorOutput "    2. 确保Maven 3.8.6+" "Gray"
    Write-ColorOutput "    3. 检查网络连接（下载依赖）" "Gray"
    Write-ColorOutput "    4. 尝试: mvn dependency:resolve" "Gray"
    Write-ColorOutput ""
    exit 1
}

exit 0
