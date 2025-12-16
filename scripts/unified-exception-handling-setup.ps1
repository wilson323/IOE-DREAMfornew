# IOE-DREAM 统一异常处理自动化部署脚本
# 版本: v1.0.0
# 功能: 统一配置所有微服务的异常处理

param(
    [Parameter(Mandatory=$false)]
    [string]$ServiceName = "",

    [Parameter(Mandatory=$false)]
    [switch]$DryRun,

    [Parameter(Mandatory=$false)]
    [switch]$Force,

    [Parameter(Mandatory=$false)]
    [switch]$Verify
)

Write-Host "======================================" -ForegroundColor Green
Write-Host "IOE-DREAM 统一异常处理自动化部署脚本" -ForegroundColor Green
Write-Host "版本: v1.0.0" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green

# 全局变量
$PROJECT_ROOT = Split-Path -Parent $PSScriptRoot
$MICROSERVICES_DIR = "$PROJECT_ROOT/microservices"
$CONFIG_TEMPLATES_DIR = "$PROJECT_ROOT/microservices/config-templates"
$SERVICES = @(
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

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

function Write-Success {
    param([string]$Message)
    Write-ColorOutput "✅ $Message" "Green"
}

function Write-Warning {
    param([string]$Message)
    Write-ColorOutput "⚠️  $Message" "Yellow"
}

function Write-Error {
    param([string]$Message)
    Write-ColorOutput "❌ $Message" "Red"
}

function Write-Info {
    param([string]$Message)
    Write-ColorOutput "ℹ️  $Message" "Cyan"
}

# 检查脚本权限
function Test-ScriptPermissions {
    Write-Info "检查脚本执行权限..."
    if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
        Write-Warning "建议以管理员权限运行此脚本"
        return $false
    }
    return $true
}

# 验证服务目录
function Test-ServiceDirectory {
    param([string]$ServiceName)
    $servicePath = "$MICROSERVICES_DIR/$ServiceName"
    if (-NOT (Test-Path $servicePath)) {
        Write-Error "服务目录不存在: $servicePath"
        return $false
    }
    return $true
}

# 检查服务依赖
function Test-ServiceDependencies {
    param([string]$ServiceName)
    $pomPath = "$MICROSERVICES_DIR/$ServiceName/pom.xml"

    if (-NOT (Test-Path $pomPath)) {
        Write-Error "pom.xml文件不存在: $pomPath"
        return $false
    }

    $pomContent = Get-Content $pomPath -Raw
    if ($pomContent -match "ioedream-common-service") {
        Write-Success "$ServiceName 已依赖 common-service"
        return $true
    } else {
        Write-Warning "$ServiceName 缺少 common-service 依赖"
        return $false
    }
}

# 检查重复的异常处理器
function Test-DuplicateExceptionHandlers {
    param([string]$ServiceName)
    $serviceDir = "$MICROSERVICES_DIR/$ServiceName"
    $duplicateHandlers = Get-ChildItem -Path $serviceDir -Recurse -Filter "*.java" |
        Select-String -Pattern "@RestControllerAdvice" |
        Measure-Object

    if ($duplicateHandlers.Count -gt 0) {
        Write-Warning "$ServiceName 发现重复的异常处理器"
        Get-ChildItem -Path $serviceDir -Recurse -Filter "*.java" |
            Select-String -Pattern "@RestControllerAdvice" |
            ForEach-Object {
                Write-Error "  发现重复处理器: $($_.Path):$($_.LineNumber)"
            }
        return $false
    } else {
        Write-Success "$ServiceName 无重复异常处理器"
        return $true
    }
}

# 更新POM依赖
function Update-PomDependencies {
    param([string]$ServiceName)

    $pomPath = "$MICROSERVICES_DIR/$ServiceName/pom.xml"
    $pomContent = Get-Content $pomPath -Raw

    # 检查是否已存在依赖
    if ($pomContent -match "ioedream-common-service") {
        Write-Info "$ServiceName 已包含 common-service 依赖，跳过更新"
        return
    }

    # 查找dependencies标签位置
    $dependencyBlock = @"

    <dependency>
        <groupId>net.lab1024.sa</groupId>
        <artifactId>ioedream-common-service</artifactId>
        <version>1.0.0</version>
    </dependency>"

    if ($pomContent -match "<dependencies>") {
        $updatedContent = $pomContent -replace "<dependencies>", "<dependencies>$dependencyBlock"

        if (-NOT $DryRun) {
            Set-Content -Path $pomPath -Value $updatedContent -Encoding UTF8
            Write-Success "已更新 $ServiceName 的 pom.xml 依赖"
        } else {
            Write-Info "[DryRun] 将更新 $ServiceName 的 pom.xml 依赖"
        }
    }
}

# 移除重复的异常处理器
function Remove-DuplicateExceptionHandlers {
    param([string]$ServiceName)

    $serviceDir = "$MICROSERVICES_DIR/$ServiceName"
    $exceptionHandlers = Get-ChildItem -Path $serviceDir -Recurse -Filter "*.java" |
        Select-String -Pattern "@RestControllerAdvice"

    if ($exceptionHandlers.Count -eq 0) {
        Write-Info "$ServiceName 无重复异常处理器需要移除"
        return
    }

    foreach ($handler in $exceptionHandlers) {
        $filePath = $handler.Path
        Write-Warning "发现重复异常处理器: $filePath"

        if ($Force -or $PSCmdlet.ShouldContinue("是否要删除重复的异常处理器文件?", "确认删除")) {
            if (-NOT $DryRun) {
                # 备份文件
                $backupPath = "$filePath.bak.$(Get-Date -Format 'yyyyMMddHHmmss')"
                Copy-Item $filePath $backupPath
                Write-Info "已备份文件到: $backupPath"

                # 删除文件
                Remove-Item $filePath -Force
                Write-Success "已删除重复异常处理器: $filePath"
            } else {
                Write-Info "[DryRun] 将删除重复异常处理器: $filePath"
            }
        }
    }
}

# 配置Nacos配置
function Setup-NacosConfiguration {
    param([string]$ServiceName)

    $configPath = "$CONFIG_TEMPLATES_DIR/exception-handling-template.yml"

    if (-NOT (Test-Path $configPath)) {
        Write-Error "配置模板文件不存在: $configPath"
        return $false
    }

    $configContent = Get-Content $configPath -Raw
    $serviceConfigContent = $configContent -replace "\$\{SERVICE_NAME:ioedream-xxx-service\}", $ServiceName

    Write-Info "为 $ServiceName 准备异常处理配置"
    Write-Info "配置大小: $($serviceConfigContent.Length) 字符"

    if (-NOT $DryRun) {
        # 这里可以添加实际的Nacos配置推送逻辑
        # 例如: curl -X POST "http://nacos-server:8848/nacos/v1/cs/configs" ...
        Write-Info "请手动将配置推送到Nacos配置中心"
        Write-Info "配置组: IOE-DREAM"
        Write-Info "配置ID: $ServiceName-exception-handling.yml"
    } else {
        Write-Info "[DryRun] 将为 $ServiceName 创建Nacos配置"
    }

    return $true
}

# 验证配置
function Test-Configuration {
    param([string]$ServiceName)

    Write-Info "验证 $ServiceName 的异常处理配置..."

    $allChecksPassed = $true

    # 检查依赖
    if (-NOT (Test-ServiceDependencies $ServiceName)) {
        $allChecksPassed = $false
    }

    # 检查重复处理器
    if (-NOT (Test-DuplicateExceptionHandlers $ServiceName)) {
        $allChecksPassed = $false
    }

    # 检查配置文件
    $configPath = "$CONFIG_TEMPLATES_DIR/exception-handling-template.yml"
    if (-NOT (Test-Path $configPath)) {
        Write-Error "配置模板文件不存在"
        $allChecksPassed = $false
    }

    if ($allChecksPassed) {
        Write-Success "$ServiceName 配置验证通过"
        return $true
    } else {
        Write-Error "$ServiceName 配置验证失败"
        return $false
    }
}

# 处理单个服务
function Process-Service {
    param([string]$ServiceName)

    Write-Info "======================================" -ForegroundColor Cyan
    Write-Info "处理服务: $ServiceName" -ForegroundColor Cyan
    Write-Info "======================================" -ForegroundColor Cyan

    # 验证服务目录
    if (-NOT (Test-ServiceDirectory $ServiceName)) {
        return $false
    }

    $success = $true

    # 验证配置
    if ($Verify) {
        return Test-Configuration $ServiceName
    }

    # 更新POM依赖
    try {
        Update-PomDependencies $ServiceName
    } catch {
        Write-Error "更新POM依赖失败: $_"
        $success = $false
    }

    # 移除重复异常处理器
    try {
        Remove-DuplicateExceptionHandlers $ServiceName
    } catch {
        Write-Error "移除重复异常处理器失败: $_"
        $success = $false
    }

    # 配置Nacos
    try {
        Setup-NacosConfiguration $ServiceName
    } catch {
        Write-Error "配置Nacos失败: $_"
        $success = $false
    }

    if ($success) {
        Write-Success "$ServiceName 处理完成"
    } else {
        Write-Error "$ServiceName 处理失败"
    }

    return $success
}

# 生成配置报告
function Generate-Report {
    $reportPath = "$PROJECT_ROOT/scripts/reports/exception-handling-setup-report-$(Get-Date -Format 'yyyyMMddHHmmss').md"

    $report = @"
# IOE-DREAM 统一异常处理部署报告

**生成时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**执行模式**: $(if ($DryRun) { "DryRun" } else { "生产执行" })

## 处理结果

### 服务处理状态
"

    $processedServices = 0
    $successfulServices = 0

    foreach ($service in $SERVICES) {
        if ($ServiceName -eq "" -or $ServiceName -eq $service) {
            $processedServices++
            # 这里可以添加实际的处理结果统计
        }
    }

    $report += @"

- 总计处理服务: $processedServices
- 成功处理服务: $successfulServices
- 处理成功率: $([math]::Round($successfulServices / $processedServices * 100, 2))%

## 配置文件位置

- 异常处理配置模板: `microservices/config-templates/exception-handling-template.yml`
- 详细规范文档: `documentation/technical/UNIFIED_EXCEPTION_HANDLING_SPECIFICATION.md`

## 后续操作

1. 将配置推送到Nacos配置中心
2. 重启相关微服务
3. 验证异常处理功能
4. 配置监控和告警

## 注意事项

- 确保所有服务依赖 `ioedream-common-service`
- 验证GlobalExceptionHandler正确加载
- 检查异常响应格式统一
- 确认TraceId在异常中正常传递

---
*报告由统一异常处理自动化部署脚本生成*
"

    if (-NOT $DryRun) {
        $reportDir = Split-Path $reportPath -Parent
        if (-NOT (Test-Path $reportDir)) {
            New-Item -ItemType Directory -Path $reportDir -Force
        }
        Set-Content -Path $reportPath -Value $report -Encoding UTF8
        Write-Success "已生成部署报告: $reportPath"
    } else {
        Write-Info "[DryRun] 将生成部署报告: $reportPath"
        Write-Info $report
    }
}

# 主执行流程
function Main {
    # 检查脚本权限
    if (-NOT (Test-ScriptPermissions)) {
        return
    }

    Write-Info "开始执行统一异常处理配置..."
    Write-Info "项目根目录: $PROJECT_ROOT"
    Write-Info "微服务目录: $MICROSERVICES_DIR"

    if ($DryRun) {
        Write-Warning "DryRun模式 - 不会执行实际更改"
    }

    if ($ServiceName -ne "") {
        # 处理单个服务
        Write-Info "指定服务模式: $ServiceName"
        Process-Service $ServiceName
    } else {
        # 处理所有服务
        Write-Info "批量处理模式"
        $successCount = 0
        $totalCount = $SERVICES.Count

        foreach ($service in $SERVICES) {
            if (Process-Service $service) {
                $successCount++
            }
        }

        Write-Info "======================================" -ForegroundColor Green
        Write-Info "批量处理完成" -ForegroundColor Green
        Write-Success "成功: $successCount/$totalCount 个服务"
        Write-Info "======================================" -ForegroundColor Green
    }

    # 生成报告
    Generate-Report
}

# 执行主流程
Main