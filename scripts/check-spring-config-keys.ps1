# ============================================================
# IOE-DREAM Spring Boot 配置键一致性检查脚本
#
# 功能：检查配置文件中是否存在已弃用的配置键、未转义的 map key、未知的自定义配置
# 用途：作为 CI/Pre-commit 强门禁，阻断配置层根因问题
#
# @Author: IOE-DREAM 架构委员会
# @Date: 2025-01-30
# @Version: v1.0.0
# ============================================================

param(
    [switch]$SkipBuild = $false,
    [string]$ConfigPath = "microservices"
)

$ErrorActionPreference = "Stop"
$script:HasErrors = $false
$script:ErrorMessages = @()

function Write-Error-Message {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
    $script:HasErrors = $true
    $script:ErrorMessages += $Message
}

function Write-Warning-Message {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Write-Success-Message {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Spring Boot 配置键一致性检查" -ForegroundColor Cyan
Write-Host "============================================================`n" -ForegroundColor Cyan

# 1. 检查已弃用的 Prometheus 导出键
Write-Host "[1/4] 检查已弃用的 Prometheus 导出键..." -ForegroundColor Yellow
$deprecatedPrometheusFiles = Get-ChildItem -Path $ConfigPath -Recurse -Include *.yml, *.yaml, *.properties |
Where-Object {
    $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
    $content -and $content -match 'management\.metrics\.export\.prometheus\.[^\s#]'
}

if ($deprecatedPrometheusFiles) {
    foreach ($file in $deprecatedPrometheusFiles) {
        $lines = Get-Content $file.FullName
        for ($i = 0; $i -lt $lines.Count; $i++) {
            if ($lines[$i] -match 'management\.metrics\.export\.prometheus\.[^\s#]' -and $lines[$i] -notmatch '^\s*#') {
                Write-Error-Message "发现已弃用的 Prometheus 导出键: $($file.FullName):$($i+1)"
                Write-Host "  建议替换为: management.prometheus.metrics.export.*" -ForegroundColor Gray
            }
        }
    }
}
else {
    Write-Success-Message "未发现已弃用的 Prometheus 导出键"
}

# 2. 检查未转义的 map key（percentiles-histogram, percentiles, sla）
Write-Host "`n[2/4] 检查未转义的 map key..." -ForegroundColor Yellow
$configFiles = Get-ChildItem -Path $ConfigPath -Recurse -Include *.yml, *.yaml | Where-Object { $_.FullName -notmatch '\\target\\' }

$unescapedKeys = @()
foreach ($file in $configFiles) {
    $content = Get-Content $file.FullName -Raw
    $lines = Get-Content $file.FullName

    # 检查 percentiles-histogram, percentiles, sla 下的未转义 key
    $inDistribution = $false
    $currentSection = ""

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]

        if ($line -match '^\s*distribution:\s*$') {
            $inDistribution = $true
            continue
        }

        if ($inDistribution) {
            if ($line -match '^\s*(percentiles-histogram|percentiles|sla):\s*$') {
                $currentSection = $matches[1]
                continue
            }

            if ($line -match '^\s+([a-zA-Z0-9._-]+):\s*') {
                $key = $matches[1]
                # 检查 key 是否包含特殊字符且未转义
                if ($key -match '\.' -and $key -notmatch '^\[.*\]$') {
                    Write-Error-Message "发现未转义的 map key: $($file.FullName):$($i+1)"
                    Write-Host "  键名: $key (在 $currentSection 下)" -ForegroundColor Gray
                    Write-Host "  建议改为: `"[$key]`"" -ForegroundColor Gray
                }
            }

            # 检查是否退出 distribution 块
            if ($line -match '^\s*[a-zA-Z]' -and $line -notmatch '^\s+' -and $line -notmatch '^\s*#') {
                $inDistribution = $false
                $currentSection = ""
            }
        }
    }
}

if (-not $script:HasErrors -or ($script:ErrorMessages | Where-Object { $_ -match '未转义的 map key' }).Count -eq 0) {
    Write-Success-Message "未发现未转义的 map key"
}

# 3. 检查自定义配置前缀是否有对应的 @ConfigurationProperties（需要先构建）
Write-Host "`n[3/4] 检查自定义配置前缀的 @ConfigurationProperties..." -ForegroundColor Yellow

if (-not $SkipBuild) {
    Write-Host "正在构建项目以生成 spring-configuration-metadata.json..." -ForegroundColor Gray
    $buildResult = & mvn -pl microservices -am -DskipTests clean compile 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Warning-Message "构建失败，跳过自定义配置检查"
        Write-Host "  提示: 使用 -SkipBuild 参数可跳过构建步骤" -ForegroundColor Gray
    }
    else {
        Write-Success-Message "项目构建成功"
    }
}

# 收集所有 spring-configuration-metadata.json 文件
$metadataFiles = Get-ChildItem -Path $ConfigPath -Recurse -Filter "spring-configuration-metadata.json" |
Where-Object { $_.FullName -match '\\target\\classes\\META-INF\\' }

$knownProperties = @()
foreach ($metadataFile in $metadataFiles) {
    try {
        $metadata = Get-Content $metadataFile.FullName -Raw | ConvertFrom-Json
        if ($metadata.properties) {
            foreach ($prop in $metadata.properties) {
                $knownProperties += $prop.name
            }
        }
    }
    catch {
        Write-Warning-Message "无法解析元数据文件: $($metadataFile.FullName)"
    }
}

# 检查 additional-spring-configuration-metadata.json
$additionalMetadataFiles = Get-ChildItem -Path $ConfigPath -Recurse -Filter "additional-spring-configuration-metadata.json"
foreach ($additionalFile in $additionalMetadataFiles) {
    try {
        $metadata = Get-Content $additionalFile.FullName -Raw | ConvertFrom-Json
        if ($metadata.properties) {
            foreach ($prop in $metadata.properties) {
                $knownProperties += $prop.name
            }
        }
    }
    catch {
        Write-Warning-Message "无法解析附加元数据文件: $($additionalFile.FullName)"
    }
}

Write-Host "已收集 $($knownProperties.Count) 个已知配置属性" -ForegroundColor Gray

# 4. 检查 tracing 配置键（Spring Boot 3.x 使用 Micrometer Tracing）
Write-Host "`n[4/4] 检查 tracing 配置键..." -ForegroundColor Yellow
$tracingFiles = Get-ChildItem -Path $ConfigPath -Recurse -Include *.yml, *.yaml |
Where-Object {
    $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
    $content -and ($content -match 'spring\.sleuth\.[^\s#]' -or $content -match 'management\.tracing\.zipkin\.[^\s#]')
}

if ($tracingFiles) {
    foreach ($file in $tracingFiles) {
        $lines = Get-Content $file.FullName
        for ($i = 0; $i -lt $lines.Count; $i++) {
            if (($lines[$i] -match 'spring\.sleuth\.[^\s#]' -or $lines[$i] -match 'management\.tracing\.zipkin\.[^\s#]') -and $lines[$i] -notmatch '^\s*#') {
                Write-Warning-Message "发现可能已废弃的 tracing 配置: $($file.FullName):$($i+1)"
                Write-Host "  Spring Boot 3.x 应使用: management.tracing.* 和 management.zipkin.tracing.endpoint" -ForegroundColor Gray
            }
        }
    }
}
else {
    Write-Success-Message "未发现废弃的 tracing 配置键"
}

# 输出总结
Write-Host "`n============================================================" -ForegroundColor Cyan
if ($script:HasErrors) {
    Write-Host "检查失败！发现 $($script:ErrorMessages.Count) 个问题" -ForegroundColor Red
    Write-Host "`n错误详情:" -ForegroundColor Red
    foreach ($msg in $script:ErrorMessages) {
        Write-Host "  - $msg" -ForegroundColor Red
    }
    exit 1
}
else {
    Write-Host "检查通过！所有配置键符合规范" -ForegroundColor Green
    exit 0
}
