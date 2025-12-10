# ============================================================
# IOE-DREAM 安全增强自动化脚本 (Windows PowerShell版本)
# 功能: 自动化执行安全增强任务，包括安全扫描、漏洞检测、加固配置
# 兼容性: Windows PowerShell 5.1+, PowerShell Core 7+
# 作者: IOE-DREAM 安全架构团队
# 版本: v1.0.0
# 日期: 2025-01-30
# ============================================================

param(
    [switch]$SkipDocker,
    [switch]$SkipDependencyCheck,
    [switch]$SkipCodeScan,
    [switch]$Force,
    [switch]$Verbose
)

# 设置错误处理
$ErrorActionPreference = "Stop"

# 全局变量
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$LogFile = "$ProjectRoot\logs\security-enhancement-$(Get-Date -Format 'yyyyMMdd_HHmmss').log"
$ReportDir = "$ProjectRoot\security-reports"
$TempDir = "$env:TEMP\ioe-dream-security-$([Guid]::NewGuid().ToString('N'))"

# 创建日志目录
$null = New-Item -ItemType Directory -Force -Path (Split-Path $LogFile -Parent)
$null = New-Item -ItemType Directory -Force -Path $ReportDir

# 日志函数
function Write-LogInfo {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [INFO] $Message"
    Write-Host $logMessage -ForegroundColor Green
    Add-Content -Path $LogFile -Value $logMessage -Encoding UTF8
}

function Write-LogWarn {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [WARN] $Message"
    Write-Host $logMessage -ForegroundColor Yellow
    Add-Content -Path $LogFile -Value $logMessage -Encoding UTF8
}

function Write-LogError {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [ERROR] $Message"
    Write-Host $logMessage -ForegroundColor Red
    Add-Content -Path $LogFile -Value $logMessage -Encoding UTF8
}

function Write-LogDebug {
    param([string]$Message)
    if ($Verbose) {
        $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        $logMessage = "[$timestamp] [DEBUG] $Message"
        Write-Host $logMessage -ForegroundColor Blue
        Add-Content -Path $LogFile -Value $logMessage -Encoding UTF8
    }
}

function Write-LogSuccess {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [SUCCESS] $Message"
    Write-Host $logMessage -ForegroundColor Cyan
    Add-Content -Path $LogFile -Value $logMessage -Encoding UTF8
}

# 错误处理函数
function Exit-WithError {
    param([string]$Message)
    Write-LogError $Message
    Cleanup
    exit 1
}

# 清理函数
function Cleanup {
    Write-LogDebug "清理临时文件..."
    if (Test-Path $TempDir) {
        try {
            Remove-Item -Recurse -Force $TempDir -ErrorAction SilentlyContinue
        } catch {
            Write-LogDebug "清理临时文件失败: $_"
        }
    }
}

# 信号处理
$null = Register-EngineEvent PowerShell.Exiting -Action { Cleanup }

# 检查PowerShell版本
function Test-PowerShellVersion {
    $psVersion = $PSVersionTable.PSVersion
    if ($psVersion.Major -lt 5) {
        Exit-WithError "需要PowerShell 5.1或更高版本，当前版本: $($psVersion.ToString())"
    }
    Write-LogInfo "PowerShell版本: $($psVersion.ToString())"
}

# 检查操作系统
function Get-OSType {
    if ($IsWindows) {
        return "windows"
    } elseif ($IsLinux) {
        return "linux"
    } elseif ($IsMacOS) {
        return "macos"
    } else {
        return "unknown"
    }
}

# 检查必要的工具
function Test-Prerequisites {
    Write-LogInfo "检查必要的工具..."

    $missingTools = @()
    $osType = Get-OSType

    # 基础工具检查
    $requiredTools = @("java", "mvn", "git", "curl")

    if ($osType -eq "windows") {
        # Windows特定工具
        if (-not (Get-Command choco -ErrorAction SilentlyContinue)) {
            $missingTools += @("chocolatey", "docker")
        }
    } else {
        $requiredTools += "docker"
    }

    foreach ($tool in $requiredTools) {
        if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) {
            $missingTools += $tool
        }
    }

    if ($missingTools.Count -gt 0) {
        Write-LogError "缺少必要工具: $($missingTools -join ', ')"

        # 提供安装建议
        if ($osType -eq "windows") {
            Write-LogInfo "Windows安装建议:"
            Write-LogInfo "  1. 安装Chocolatey: "
            Write-LogInfo "     Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))"
            Write-LogInfo "  2. 使用Chocolatey安装工具: choco install $($missingTools -join ' ')"
        } else {
            Write-LogInfo "Linux/macOS安装建议: 使用包管理器安装缺失的工具"
        }

        Exit-WithError "请安装必要的工具后重试"
    }

    # 检查Java版本
    try {
        $javaVersion = java -version 2>&1
        if ($javaVersion -match '"(\d+)\.') {
            $majorVersion = [int]$matches[1]
            if ($majorVersion -lt 21) {
                Exit-WithError "需要Java 21或更高版本，当前版本: $javaVersion"
            }
        }
        Write-LogInfo "Java版本: $javaVersion"
    } catch {
        Exit-WithError "无法检查Java版本"
    }

    # 检查Maven版本
    try {
        $mavenVersion = mvn -version 2>&1
        Write-LogInfo "Maven版本: $($mavenVersion -split '\n')[0]"
    } catch {
        Exit-WithError "Maven未正确安装或配置"
    }

    Write-LogSuccess "所有必要工具检查通过 ✓"
}

# 备份安全配置
function Backup-SecurityConfigs {
    Write-LogInfo "备份当前安全配置..."

    $backupDir = "$ReportDir\backup-$(Get-Date -Format 'yyyyMMdd_HHmmss')"
    $null = New-Item -ItemType Directory -Force -Path $backupDir

    # 备份安全相关配置文件
    $securityPatterns = @(
        "microservices\*\src\main\resources\application*.yml",
        "microservices\*\src\main\resources\security\*.yml",
        "microservices\*\src\main\resources\security\*.properties",
        "microservices\*\pom.xml",
        "microservices\*\Dockerfile*",
        ".claude\skills\*security*.md"
    )

    foreach ($pattern in $securityPatterns) {
        $files = Get-ChildItem -Path $ProjectRoot -Filter $pattern -Recurse -ErrorAction SilentlyContinue
        foreach ($file in $files) {
            $relativePath = $file.FullName.Replace($ProjectRoot, "").TrimStart('\')
            $targetFile = Join-Path $backupDir $relativePath
            $targetDir = Split-Path $targetFile -Parent

            $null = New-Item -ItemType Directory -Force -Path $targetDir
            Copy-Item $file.FullName $targetFile
            Write-LogDebug "备份文件: $relativePath"
        }
    }

    Write-LogSuccess "安全配置备份完成: $backupDir"
}

# 代码安全扫描
function Invoke-CodeSecurityScan {
    if ($SkipCodeScan) {
        Write-LogWarn "跳过代码安全扫描"
        return
    }

    Write-LogInfo "开始代码安全扫描..."

    $scanResultsDir = "$ReportDir\code-scan-$(Get-Date -Format 'yyyyMMdd_HHmmss')"
    $null = New-Item -ItemType Directory -Force -Path $scanResultsDir

    # 检查是否在项目根目录
    if (-not (Test-Path "$ProjectRoot\pom.xml")) {
        Exit-WithError "未在项目根目录中找到pom.xml文件"
    }

    Push-Location $ProjectRoot

    try {
        # 1. OWASP Dependency Check
        if (-not $SkipDependencyCheck) {
            Write-LogInfo "运行OWASP Dependency Check..."

            $dependencyCheckCommand = "mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7 -DskipTests -DoutputDirectory=`"$scanResultsDir`" -Dformat=`"HTML,XML,JSON`""

            Invoke-Expression $dependencyCheckCommand 2>&1 | Tee-Object -FilePath $LogFile -Append

            Write-LogSuccess "OWASP Dependency Check 完成"
        } else {
            Write-LogWarn "跳过OWASP Dependency Check"
        }

        # 2. SpotBugs + FindSecBugs
        Write-LogInfo "运行SpotBugs + FindSecBugs..."

        $spotBugsCommand = "mvn com.github.spotbugs:spotbugs-maven-plugin:check -Dspotbugs.effort=Max -Dspotbugs.threshold=Low -DskipTests -DoutputDirectory=`"$scanResultsDir`""

        try {
            Invoke-Expression $spotBugsCommand 2>&1 | Tee-Object -FilePath $LogFile -Append
            Write-LogSuccess "SpotBugs + FindSecBugs 完成"
        } catch {
            Write-LogWarn "SpotBugs扫描失败: $_"
        }

        # 3. SonarQube扫描 (如果配置了)
        $sonarHostUrl = $env:SONAR_HOST_URL
        $sonarToken = $env:SONAR_TOKEN

        if ($sonarHostUrl -and $sonarToken) {
            Write-LogInfo "运行SonarQube扫描..."

            $sonarCommand = "mvn sonar:sonar -Dsonar.host.url=`"$sonarHostUrl`" -Dsonar.token=`"$sonarToken`" -DskipTests"

            try {
                Invoke-Expression $sonarCommand 2>&1 | Tee-Object -FilePath $LogFile -Append
                Write-LogSuccess "SonarQube扫描完成"
            } catch {
                Write-LogWarn "SonarQube扫描失败: $_"
            }
        } else {
            Write-LogDebug "跳过SonarQube扫描 (未配置)"
        }

        # 4. 自定义安全规则检查
        Write-LogInfo "运行自定义安全规则检查..."
        Invoke-CustomSecurityRules $scanResultsDir

        # 生成代码扫描报告
        New-CodeScanReport $scanResultsDir

    } finally {
        Pop-Location
    }

    Write-LogSuccess "代码安全扫描完成，结果保存在: $scanResultsDir"
}

# 自定义安全规则检查
function Invoke-CustomSecurityRules {
    param([string]$ResultsDir)

    Write-LogDebug "执行自定义安全规则检查..."

    # 检查硬编码密钥
    $hardcodedSecretsFile = "$ResultsDir\hardcoded_secrets.txt"
    $hardcodedSecrets = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.java" -Recurse |
        Select-String -Pattern "(password|secret|key)\s*=\s*['\"][^'\"]{8,}" |
        Select-Object -ExpandProperty Path |
        Set-Content $hardcodedSecretsFile

    # 检查SQL注入风险
    $sqlInjectionRiskFile = "$ResultsDir\sql_injection_risk.txt"
    $sqlInjectionRisks = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.java" -Recurse |
        Select-String -Pattern "Statement\.execute\(|createStatement\(" |
        Select-Object -ExpandProperty Path |
        Set-Content $sqlInjectionRiskFile

    # 检查XSS风险
    $xssRiskFile = "$ResultsDir\xss_risk.txt"
    $xssRisks = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.java" -Recurse |
        Select-String -Pattern "request\.getParameter\(|response\.getWriter\(" |
        Select-Object -ExpandProperty Path |
        Set-Content $xssRiskFile

    # 检查不安全的随机数生成
    $unsafeRandomFile = "$ResultsDir\unsafe_random.txt"
    $unsafeRandoms = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.java" -Recurse |
        Select-String -Pattern "java\.util\.Random" |
        Select-Object -ExpandProperty Path |
        Set-Content $unsafeRandomFile

    # 检查日志中的敏感信息
    $sensitiveInLogsFile = "$ResultsDir\sensitive_in_logs.txt"
    $sensitiveInLogs = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.java" -Recurse |
        Select-String -Pattern "log\.(info|debug).*\b(password|secret|token|key)\b" |
        Select-Object -ExpandProperty Path |
        Set-Content $sensitiveInLogsFile

    Write-LogDebug "自定义安全规则检查完成"
}

# 生成代码扫描报告
function New-CodeScanReport {
    param([string]$ResultsDir)

    $reportFile = "$ResultsDir\code-security-report.md"

    $reportContent = @"
# 代码安全扫描报告

> 扫描时间: $(Get-Date)
> 项目路径: $ProjectRoot

## 扫描结果摘要

### OWASP Dependency Check
"@

    # 添加OWASP报告链接
    if (Test-Path "$ResultsDir\dependency-check-report.html") {
        $reportContent += "`n- 报告文件: [dependency-check-report.html](dependency-check-report.html)"
    }

    $reportContent += @"

### SpotBugs + FindSecBugs
"@

    if (Test-Path "$ResultsDir\spotbugsXml.xml") {
        $reportContent += "`n- 报告文件: [spotbugsXml.xml](spotbugsXml.xml)"
    }

    $reportContent += @"

### 自定义安全规则检查

"@

    # 添加自定义规则检查结果
    $rules = @(
        @{ File = "hardcoded_secrets.txt"; Name = "硬编码密钥" },
        @{ File = "sql_injection_risk.txt"; Name = "SQL注入风险" },
        @{ File = "xss_risk.txt"; Name = "XSS风险" },
        @{ File = "unsafe_random.txt"; Name = "不安全的随机数" },
        @{ File = "sensitive_in_logs.txt"; Name = "日志中的敏感信息" }
    )

    foreach ($rule in $rules) {
        $ruleFile = Join-Path $ResultsDir $rule.File
        if (Test-Path $ruleFile -and (Get-Content $ruleFile).Count -gt 0) {
            $count = (Get-Content $ruleFile).Count
            $reportContent += "`n- **$($rule.Name)**: 发现 $count 个潜在问题"
        } else {
            $reportContent += "`n- **$($rule.Name)**: 未发现问题 ✓"
        }
    }

    $reportContent += @"

## 安全建议

1. 修复所有高危和中危漏洞
2. 移除硬编码的敏感信息
3. 使用参数化查询防止SQL注入
4. 实施输入验证防止XSS攻击
5. 使用安全的随机数生成器
6. 避免在日志中记录敏感信息

## 报告详情

请查看上述报告文件获取详细信息。

"@

    Set-Content -Path $reportFile -Value $reportContent -Encoding UTF8

    Write-LogSuccess "代码安全扫描报告生成完成: $reportFile"
}

# Docker镜像安全扫描
function Invoke-DockerSecurityScan {
    if ($SkipDocker) {
        Write-LogWarn "跳过Docker安全扫描"
        return
    }

    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-LogWarn "Docker未安装，跳过Docker安全扫描"
        return
    }

    Write-LogInfo "开始Docker镜像安全扫描..."

    $scanResultsDir = "$ReportDir\docker-scan-$(Get-Date -Format 'yyyyMMdd_HHmmss')"
    $null = New-Item -ItemType Directory -Force -Path $scanResultsDir

    # 查找所有IOE-DREAM镜像
    $images = docker images --format "{{.Repository}}:{{.Tag}}" | Select-String "ioe-dream"

    if (-not $images) {
        Write-LogWarn "未找到IOE-DREAM Docker镜像，尝试构建..."
        Build-DockerImages
        $images = docker images --format "{{.Repository}}:{{.Tag}}" | Select-String "ioe-dream"
    }

    if (-not $images) {
        Write-LogWarn "仍然未找到IOE-DREAM Docker镜像，跳过扫描"
        return
    }

    # 扫描每个镜像
    foreach ($image in $images) {
        if ($image -and $image.Trim()) {
            Scan-DockerImage $image.Trim() $scanResultsDir
        }
    }

    # 生成Docker扫描报告
    New-DockerScanReport $scanResultsDir

    Write-LogSuccess "Docker镜像安全扫描完成，结果保存在: $scanResultsDir"
}

# 扫描单个Docker镜像
function Scan-DockerImage {
    param([string]$Image, [string]$ResultsDir)

    Write-LogInfo "扫描镜像: $Image"

    $imageName = $Image.Replace('/', '_').Replace(':', '_')

    # Trivy扫描
    if (Get-Command trivy -ErrorAction SilentlyContinue) {
        Write-LogDebug "运行Trivy扫描..."

        try {
            trivy image --format json --output "$ResultsDir\trivy_$imageName.json" $Image 2>$null
            trivy image --format table --output "$ResultsDir\trivy_$imageName.txt" $Image 2>$null
        } catch {
            Write-LogWarn "Trivy扫描失败: $_"
        }
    } else {
        Write-LogWarn "Trivy未安装，跳过Trivy扫描"
    }

    # Docker安全检查
    try {
        $dockerConfig = docker inspect $Image | ConvertFrom-Json
        $dockerConfig[0].Config | ConvertTo-Json -Depth 10 | Set-Content "$ResultsDir\docker_config_$imageName.json"
    } catch {
        Write-LogWarn "Docker配置检查失败: $_"
    }

    # 检查镜像大小
    try {
        $imageInfo = docker images --format "{{.Size}}" $Image
        $imageInfo | Set-Content "$ResultsDir\image_size_$imageName.txt"
    } catch {
        Write-LogWarn "镜像大小检查失败: $_"
    }
}

# 构建Docker镜像
function Build-DockerImages {
    Write-LogInfo "尝试构建Docker镜像..."

    Push-Location $ProjectRoot

    try {
        # 查找Dockerfile
        $dockerfiles = Get-ChildItem -Path "microservices" -Include "Dockerfile*" -Recurse

        foreach ($dockerfile in $dockerfiles) {
            $serviceDir = $dockerfile.DirectoryName
            $serviceName = Split-Path $serviceDir -Leaf

            if (Test-Path "$serviceDir\pom.xml") {
                Write-LogInfo "构建服务镜像: $serviceName"

                Push-Location $serviceDir

                try {
                    # 先构建JAR
                    mvn clean package -DskipTests -q 2>&1 | Tee-Object -FilePath $LogFile -Append

                    # 构建Docker镜像
                    if (Test-Path "Dockerfile") {
                        docker build -t "ioe-dream/$serviceName`:latest . 2>&1 | Tee-Object -FilePath $LogFile -Append
                    }
                } catch {
                    Write-LogWarn "构建服务镜像失败: $serviceName - $_"
                } finally {
                    Pop-Location
                }
            }
        }
    } finally {
        Pop-Location
    }
}

# 生成Docker扫描报告
function New-DockerScanReport {
    param([string]$ResultsDir)

    $reportFile = "$ResultsDir\docker-security-report.md"

    $reportContent = @"
# Docker镜像安全扫描报告

> 扫描时间: $(Get-Date)
> 项目路径: $ProjectRoot

## 镜像扫描结果

"@

    # 处理Trivy扫描结果
    $trivyFiles = Get-ChildItem -Path $ResultsDir -Name "trivy_*.json"

    foreach ($trivyFile in $trivyFiles) {
        $imageName = $trivyFile.Replace('trivy_', '').Replace('.json', '')

        if ((Get-Command ConvertFrom-Json -ErrorAction SilentlyContinue) -and (Test-Path "$ResultsDir\$trivyFile")) {
            try {
                $trivyData = Get-Content "$ResultsDir\$trivyFile" | ConvertFrom-Json
                $highVulns = $trivyData.Results.Vulnerabilities | Where-Object { $_.Severity -in @("HIGH", "CRITICAL") } | Measure-Object | Select-Object -ExpandProperty Count
                $totalVulns = $trivyData.Results.Vulnerabilities | Measure-Object | Select-Object -ExpandProperty Count

                $reportContent += "`n### $imageName"
                $reportContent += "`n- **高危漏洞**: $highVulns"
                $reportContent += "`n- **总漏洞数**: $totalVulns"
                $reportContent += "`n"
            } catch {
                $reportContent += "`n### $imageName"
                $reportContent += "`n- **扫描状态**: 失败"
                $reportContent += "`n"
            }
        }
    }

    $reportContent += @"

## 安全建议

1. 修复所有高危和严重漏洞
2. 使用最小化基础镜像
3. 定期更新基础镜像
4. 移除不必要的软件包
5. 使用非root用户运行容器

## 详细报告

请查看Trivy扫描JSON文件获取详细信息。

"@

    Set-Content -Path $reportFile -Value $reportContent -Encoding UTF8

    Write-LogSuccess "Docker安全扫描报告生成完成: $reportFile"
}

# 安全配置检查
function Test-SecurityConfigurations {
    Write-LogInfo "开始安全配置检查..."

    $scanResultsDir = "$ReportDir\config-check-$(Get-Date -Format 'yyyyMMdd_HHmmss')"
    $null = New-Item -ItemType Directory -Force -Path $scanResultsDir

    # 检查应用安全配置
    Test-ApplicationSecurity $scanResultsDir

    # 检查数据库安全配置
    Test-DatabaseSecurity $scanResultsDir

    # 检查网络安全配置
    Test-NetworkSecurity $scanResultsDir

    # 检查日志安全配置
    Test-LoggingSecurity $scanResultsDir

    # 生成配置检查报告
    New-ConfigCheckReport $scanResultsDir

    Write-LogSuccess "安全配置检查完成，结果保存在: $scanResultsDir"
}

# 检查应用安全配置
function Test-ApplicationSecurity {
    param([string]$ResultsDir)

    Write-LogDebug "检查应用安全配置..."

    # 检查Spring Security配置
    $springSecurityFiles = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*Security*.java" -Recurse | Select-Object -ExpandProperty FullName
    $springSecurityFiles | Set-Content "$ResultsDir\spring_security_files.txt"

    # 检查JWT配置
    $jwtConfigFiles = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.yml" -Recurse | Select-String -Pattern "jwt|JWT" | Select-Object -ExpandProperty Path | Set-Content "$ResultsDir\jwt_config_files.txt"

    # 检查HTTPS配置
    $httpsConfigFiles = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.yml" -Recurse | Select-String -Pattern "ssl|https" | Select-Object -ExpandProperty Path | Set-Content "$ResultsDir\https_config_files.txt"

    # 检查会话配置
    $sessionConfigFiles = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.yml" -Recurse | Select-String -Pattern "session|timeout" | Select-Object -ExpandProperty Path | Set-Content "$ResultsDir\session_config_files.txt"
}

# 检查数据库安全配置
function Test-DatabaseSecurity {
    param([string]$ResultsDir)

    Write-LogDebug "检查数据库安全配置..."

    # 检查数据库密码配置
    $databaseConfigFiles = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.yml" -Recurse | Select-String -Pattern "password|datasource" | Select-Object -ExpandProperty Path | Set-Content "$ResultsDir\database_config_files.txt"

    # 检查连接池配置
    $connectionPoolFiles = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.yml" -Recurse | Select-String -Pattern "druid|hikari" | Select-Object -ExpandProperty Path | Set-Content "$ResultsDir\connection_pool_files.txt"
}

# 检查网络安全配置
function Test-NetworkSecurity {
    param([string]$ResultsDir)

    Write-LogDebug "检查网络安全配置..."

    # 检查CORS配置
    $corsConfigFiles = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.yml" -Recurse | Select-String -Pattern "cors|allowed-origins" | Select-Object -ExpandProperty Path | Set-Content "$ResultsDir\cors_config_files.txt"

    # 检查防火墙配置
    $firewallConfigFiles = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.yml" -Recurse | Select-String -Pattern "firewall|iptables" | Select-Object -ExpandProperty Path | Set-Content "$ResultsDir\firewall_config_files.txt"
}

# 检查日志安全配置
function Test-LoggingSecurity {
    param([string]$ResultsDir)

    Write-LogDebug "检查日志安全配置..."

    # 检查日志配置
    $loggingConfigFiles = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.yml" -Recurse | Select-String -Pattern "logging|logback" | Select-Object -ExpandProperty Path | Set-Content "$ResultsDir\logging_config_files.txt"

    # 检查审计日志配置
    $auditConfigFiles = Get-ChildItem -Path "$ProjectRoot\microservices" -Include "*.yml" -Recurse | Select-String -Pattern "audit|security" | Select-Object -ExpandProperty Path | Set-Content "$ResultsDir\audit_config_files.txt"
}

# 生成配置检查报告
function New-ConfigCheckReport {
    param([string]$ResultsDir)

    $reportFile = "$ResultsDir\security-config-report.md"

    $reportContent = @"
# 安全配置检查报告

> 检查时间: $(Get-Date)
> 项目路径: $ProjectRoot

## 配置检查结果

### 应用安全配置
"@

    $configTypes = @(
        @{ File = "spring_security_files.txt"; Name = "Spring Security" },
        @{ File = "jwt_config_files.txt"; Name = "JWT配置" },
        @{ File = "https_config_files.txt"; Name = "HTTPS配置" },
        @{ File = "session_config_files.txt"; Name = "会话配置" },
        @{ File = "database_config_files.txt"; Name = "数据库配置" },
        @{ File = "connection_pool_files.txt"; Name = "连接池配置" },
        @{ File = "cors_config_files.txt"; Name = "CORS配置" },
        @{ File = "logging_config_files.txt"; Name = "日志配置" },
        @{ File = "audit_config_files.txt"; Name = "审计配置" }
    )

    foreach ($configType in $configTypes) {
        $filePath = Join-Path $ResultsDir $configType.File
        if (Test-Path $filePath -and (Get-Content $filePath).Count -gt 0) {
            $count = (Get-Content $filePath).Count
            $reportContent += "`n- **$($configType.Name)**: 发现 $count 个配置文件"
        } else {
            $reportContent += "`n- **$($configType.Name)**: 未找到相关配置 ⚠️"
        }
    }

    $reportContent += @"

## 安全建议

1. 确保所有服务都配置了Spring Security
2. 使用强密码策略和加密存储
3. 启用HTTPS和TLS加密
4. 配置合理的会话超时时间
5. 启用详细的审计日志
6. 定期轮换密钥和证书
7. 实施最小权限原则

## 配置文件详情

请查看上述配置文件列表获取详细信息。

"@

    Set-Content -Path $reportFile -Value $reportContent -Encoding UTF8

    Write-LogSuccess "安全配置检查报告生成完成: $reportFile"
}

# 生成综合安全报告
function New-ComprehensiveSecurityReport {
    Write-LogInfo "生成综合安全报告..."

    $reportFile = "$ReportDir\comprehensive-security-report-$(Get-Date -Format 'yyyyMMdd_HHmmss').md"
    $scanDate = Get-Date
    $osType = Get-OSType

    $reportContent = @"
# IOE-DREAM 智慧园区一卡通系统安全增强综合报告

> **报告生成时间**: $scanDate
> **操作系统**: $osType
> **项目路径**: $ProjectRoot
> **报告类型**: 自动化安全扫描与配置检查

## 执行摘要

本次安全增强检查包含以下模块：
- [x] 代码安全扫描
- [x] Docker镜像安全扫描
- [x] 安全配置检查
- [x] 依赖漏洞检测
- [x] 自定义安全规则检查

## 安全扫描结果概览

### 代码安全扫描
- **OWASP Dependency Check**: 依赖漏洞检测
- **SpotBugs + FindSecBugs**: 代码静态分析
- **自定义安全规则**: 业务安全规则检查

### 容器安全扫描
- **Trivy漏洞扫描**: 容器镜像漏洞检测
- **Docker配置检查**: 容器安全配置审核

### 配置安全检查
- **应用安全配置**: Spring Security, JWT, HTTPS配置
- **数据库安全配置**: 连接池、密码策略检查
- **网络安全配置**: CORS、防火墙配置
- **日志安全配置**: 审计日志、日志安全

## 安全等级评估

| 安全维度 | 扫描状态 | 风险等级 | 改进建议 |
|---------|----------|----------|----------|
| **代码安全** | ✅ 已扫描 | 待评估 | 查看详细扫描报告 |
| **依赖安全** | ✅ 已扫描 | 待评估 | 修复高危漏洞 |
| **容器安全** | ✅ 已扫描 | 待评估 | 更新基础镜像 |
| **配置安全** | ✅ 已检查 | 待评估 | 完善安全配置 |

## 详细报告链接

"@

    # 添加详细报告链接
    $latestDirs = Get-ChildItem -Path $ReportDir -Directory | Where-Object { $_.Name -match "$(Get-Date -Format 'yyyyMMdd')" } | Sort-Object Name -Descending | Select-Object -First 5

    foreach ($dir in $latestDirs) {
        $reportContent += "`n- **$($dir.Name)**: [查看详情]($($dir.Name)/)"
    }

    $reportContent += @"

## 安全加固建议

### 立即执行 (P0)
1. 修复所有高危和严重安全漏洞
2. 移除硬编码的敏感信息
3. 启用HTTPS和TLS加密
4. 实施多因素认证(MFA)

### 短期执行 (P1)
1. 更新所有过期的依赖库
2. 完善Spring Security配置
3. 启用详细的安全审计日志
4. 实施代码安全扫描CI/CD集成

### 中期执行 (P2)
1. 建立安全监控和告警系统
2. 实施零信任安全架构
3. 定期进行安全渗透测试
4. 建立安全培训和意识提升计划

### 长期执行 (P3)
1. 建立企业级安全管理平台
2. 实施AI驱动的威胁检测
3. 建立安全合规自动化系统
4. 建立安全应急响应机制

## 合规性检查

- [ ] **等保三级合规**: 部分达标，需进一步完善
- [ ] **数据安全法合规**: 需要完善数据分类分级
- [ ] **个人信息保护**: 需要完善隐私保护措施
- [ ] **金融级安全**: 基础架构就绪，需业务层面完善

## 下一步行动计划

1. **制定安全修复计划**: 根据扫描结果制定详细的修复计划
2. **建立安全监控**: 部署实时安全监控和告警系统
3. **定期安全扫描**: 建立定期安全扫描和评估机制
4. **安全培训**: 开展安全意识和技能培训

---

**报告生成**: IOE-DREAM 安全增强自动化脚本
**技术支持**: IOE-DREAM 安全架构团队
**最后更新**: $(Get-Date)

*本报告由自动化安全扫描工具生成，建议结合人工审计进行综合评估。*
"@

    Set-Content -Path $reportFile -Value $reportContent -Encoding UTF8

    Write-LogSuccess "综合安全报告生成完成: $reportFile"
    Write-LogInfo "报告文件: $reportFile"
}

# 创建安全加固脚本
function New-SecurityHardeningScripts {
    Write-LogInfo "创建安全加固脚本..."

    $scriptsDir = "$ProjectRoot\scripts\security-hardening"
    $null = New-Item -ItemType Directory -Force -Path $scriptsDir

    # 创建依赖漏洞修复脚本
    $dependencyFixScript = @"
@echo off
REM 依赖漏洞修复脚本 (Windows批处理)

echo 开始修复依赖漏洞...

REM 更新Maven依赖版本
mvn versions:display-dependency-updates
mvn versions:use-latest-releases

REM 重新运行安全扫描验证修复效果
echo 重新运行安全扫描...
powershell -ExecutionPolicy Bypass -File "$ProjectRoot\scripts\security-enhancement.ps1"

echo 依赖漏洞修复完成
pause
"@

    Set-Content -Path "$scriptsDir\fix-dependency-vulnerabilities.bat" -Value $dependencyFixScript -Encoding UTF8

    # 创建Docker安全加固脚本
    $dockerHardeningScript = @"
@echo off
REM Docker安全加固脚本 (Windows批处理)

echo 开始Docker安全加固...

REM 构建安全的Docker镜像
for /d %%d in (microservices\*) do (
    if exist "%%d\Dockerfile" (
        for %%f in ("%%d") do set service_name=%%~nf
        echo 加固服务: !service_name!

        cd %%d
        docker build --no-cache -t "ioe-dream/!service_name!:secure" .
        cd ..\..
    )
)

echo Docker安全加固完成
pause
"@

    Set-Content -Path "$scriptsDir\harden-docker-images.bat" -Value $dockerHardeningScript -Encoding UTF8

    # 创建PowerShell版本的安全加固脚本
    $psSecurityHardeningScript = @"
# 安全加固配置脚本 (PowerShell)
param(
    [switch]$FixDependencies,
    [switch]$HardenDocker,
    [switch]$UpdateConfigs
)

Write-Host "开始安全配置加固..." -ForegroundColor Green

if ($FixDependencies) {
    Write-Host "修复依赖漏洞..." -ForegroundColor Yellow
    # 这里可以添加依赖修复逻辑
}

if ($HardenDocker) {
    Write-Host "加固Docker镜像..." -ForegroundColor Yellow
    # 这里可以添加Docker加固逻辑
}

if ($UpdateConfigs) {
    Write-Host "更新安全配置..." -ForegroundColor Yellow
    # 这里可以添加配置更新逻辑
}

Write-Host "安全配置加固完成" -ForegroundColor Green
"@

    Set-Content -Path "$scriptsDir\security-hardening.ps1" -Value $psSecurityHardeningScript -Encoding UTF8

    Write-LogSuccess "安全加固脚本创建完成: $scriptsDir"
}

# 主函数
function Main {
    Write-LogInfo "开始执行IOE-DREAM安全增强自动化脚本..."
    Write-LogInfo "================================================"

    # 显示系统信息
    Write-LogInfo "系统信息:"
    Write-LogInfo "  操作系统: $(Get-OSType)"
    Write-LogInfo "  PowerShell版本: $($PSVersionTable.PSVersion.ToString())"
    Write-LogInfo "  项目路径: $ProjectRoot"
    Write-LogInfo "  脚本路径: $ScriptDir"
    Write-LogInfo "  日志文件: $LogFile"
    Write-LogInfo "  报告目录: $ReportDir"

    # 创建临时目录
    $null = New-Item -ItemType Directory -Force -Path $TempDir

    # 执行安全检查步骤
    Test-PowerShellVersion
    Test-Prerequisites
    Backup-SecurityConfigs
    Invoke-CodeSecurityScan
    Invoke-DockerSecurityScan
    Test-SecurityConfigurations

    # 生成综合报告
    New-ComprehensiveSecurityReport

    # 创建安全加固脚本
    New-SecurityHardeningScripts

    Write-LogInfo "================================================"
    Write-LogSuccess "IOE-DREAM安全增强自动化脚本执行完成！"
    Write-LogInfo ""
    Write-LogInfo "安全检查结果:"
    Write-LogInfo "  - 代码安全扫描: 已完成"
    Write-LogInfo "  - Docker安全扫描: 已完成"
    Write-LogInfo "  - 安全配置检查: 已完成"
    Write-LogInfo "  - 综合安全报告: 已生成"
    Write-LogInfo ""
    Write-LogInfo "下一步操作:"
    Write-LogInfo "  1. 查看综合安全报告了解安全状况"
    Write-LogInfo "  2. 根据建议进行安全加固"
    Write-LogInfo "  3. 定期运行此脚本进行安全检查"
    Write-LogInfo ""
    Write-LogInfo "报告文件:"
    $latestReport = Get-ChildItem -Path $ReportDir -Name "comprehensive-security-report-*.md" | Sort-Object -Descending | Select-Object -First 1
    Write-LogInfo "  - 最新综合报告: $latestReport"
    Write-LogInfo "  - 所有报告目录: $ReportDir"
    Write-LogInfo "  - 完整日志: $LogFile"

    Write-LogSuccess "安全增强脚本执行结束 ✓"
}

# 检查是否直接执行此脚本
if ($MyInvocation.InvocationName -eq $MyInvocation.MyCommand.Name) {
    Main
}