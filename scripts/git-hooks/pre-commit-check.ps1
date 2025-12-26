# ============================================================
# IOE-DREAM Git Pre-commit检查脚本
#
# 功能：检查staged文件的架构合规性（快速检查）
# ============================================================

param(
    [string[]]$StagedFiles = @()
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

# 如果没有提供staged文件列表，从git获取
if ($StagedFiles.Count -eq 0) {
    $gitStatus = git diff --cached --name-only --diff-filter=ACM
    $StagedFiles = $gitStatus | Where-Object { $_ -match '\.java$|\.yml$|\.yaml$' }
}

if ($StagedFiles.Count -eq 0) {
    exit 0
}

$violations = @()
$hasViolations = $false

# 快速检查staged文件
foreach ($file in $StagedFiles) {
    $fullPath = Join-Path $projectRoot $file

    if (-not (Test-Path $fullPath)) {
        continue
    }

    $content = Get-Content $fullPath -Raw

    # 检查@Repository
    if ($file -match '\.java$' -and $content -match '@Repository') {
        $lines = Get-Content $fullPath
        $lineNum = 0
        foreach ($line in $lines) {
            $lineNum++
            if ($line -match '^\s*@Repository' -and $line -notmatch '禁止|//|/\*') {
                $violations += @{
                    Type    = "@Repository违规"
                    File    = $file
                    Line    = $lineNum
                    Message = "应使用@Mapper注解而非@Repository"
                }
                $hasViolations = $true
                break  # 只报告第一个违规
            }
        }
    }

    # 检查@Autowired
    if ($file -match '\.java$' -and $content -match '@Autowired') {
        $lines = Get-Content $fullPath
        $lineNum = 0
        foreach ($line in $lines) {
            $lineNum++
            if ($line -match '^\s*@Autowired' -and $line -notmatch '禁止|//|/\*') {
                $violations += @{
                    Type    = "@Autowired违规"
                    File    = $file
                    Line    = $lineNum
                    Message = "应使用@Resource注解而非@Autowired"
                }
                $hasViolations = $true
                break
            }
        }
    }

    # 检查javax包名
    if ($file -match '\.java$' -and $content -match 'import\s+javax\.(annotation|validation|persistence|servlet|transaction|inject)') {
        $lines = Get-Content $fullPath
        $lineNum = 0
        foreach ($line in $lines) {
            $lineNum++
            if ($line -match '^import\s+javax\.(annotation|validation|persistence|servlet|transaction|inject)') {
                $violations += @{
                    Type    = "Jakarta EE迁移违规"
                    File    = $file
                    Line    = $lineNum
                    Message = "应使用jakarta包名而非javax"
                }
                $hasViolations = $true
                break
            }
        }
    }

    # 检查HikariCP配置
    if ($file -match 'application.*\.yml$' -and $content -match 'hikari:') {
        $lines = Get-Content $fullPath
        $lineNum = 0
        foreach ($line in $lines) {
            $lineNum++
            if ($line -match 'hikari:' -and $line -notmatch '#.*hikari|禁止') {
                $violations += @{
                    Type    = "连接池配置违规"
                    File    = $file
                    Line    = $lineNum
                    Message = "应使用Druid连接池而非HikariCP"
                }
                $hasViolations = $true
                break
            }
        }
    }

    # 检查已弃用的Prometheus导出键
    if ($file -match '\.(yml|yaml|properties)$' -and $content -match 'management\.metrics\.export\.prometheus\.') {
        $lines = Get-Content $fullPath
        $lineNum = 0
        foreach ($line in $lines) {
            $lineNum++
            if ($line -match 'management\.metrics\.export\.prometheus\.' -and $line -notmatch '#.*已弃用|#.*deprecated|禁止') {
                $violations += @{
                    Type    = "已弃用Prometheus键"
                    File    = $file
                    Line    = $lineNum
                    Message = "应使用 management.prometheus.metrics.export.* 替代 management.metrics.export.prometheus.*"
                }
                $hasViolations = $true
                break
            }
        }
    }

    # 检查微服务间通信规范违规（ioedream-common-service依赖）
    if ($file -match 'pom\.xml$' -and $content -match 'ioedream-common-service') {
        # 检查是否是注释
        $lines = Get-Content $fullPath
        $lineNum = 0
        foreach ($line in $lines) {
            $lineNum++
            if ($line -match 'ioedream-common-service' -and $line -notmatch '<!--.*-->' -and $line -notmatch '^\s*<!--') {
                # 检查是否是网关服务或公共服务本身（这些允许依赖）
                if ($file -notmatch 'ioedream-gateway-service' -and $file -notmatch 'ioedream-common-service') {
                    $violations += @{
                        Type    = "微服务通信规范违规"
                        File    = $file
                        Line    = $lineNum
                        Message = "业务服务禁止直接依赖ioedream-common-service，应使用GatewayServiceClient"
                    }
                    $hasViolations = $true
                    break
                }
            }
        }
    }

    # 检查UserInfoResponse位置错误
    if ($file -match '\.java$' -and $content -match 'UserInfoResponse') {
        if ($file -notmatch 'gateway-client' -and $content -match 'import.*common\.domain\.response\.UserInfoResponse') {
            $lines = Get-Content $fullPath
            $lineNum = 0
            foreach ($line in $lines) {
                $lineNum++
                if ($line -match 'import.*common\.domain\.response\.UserInfoResponse') {
                    $violations += @{
                        Type    = "响应对象位置违规"
                        File    = $file
                        Line    = $lineNum
                        Message = "UserInfoResponse应从microservices-common-gateway-client模块导入"
                    }
                    $hasViolations = $true
                    break
                }
            }
        }
    }

    # 检查YAML map key特殊字符未转义（仅检查distribution块）
    if ($file -match '\.(yml|yaml)$' -and $content -match 'distribution:') {
        $lines = Get-Content $fullPath
        $inDistribution = $false
        $currentContext = ""
        $lineNum = 0

        foreach ($line in $lines) {
            $lineNum++
            $trimmed = $line.Trim()

            if ($trimmed -match 'distribution:\s*$') {
                $inDistribution = $true
                continue
            }

            if ($inDistribution -and $trimmed -match '^\w+:\s*$' -and $trimmed -notmatch 'percentiles|sla|distribution') {
                $inDistribution = $false
                continue
            }

            if ($inDistribution) {
                if ($trimmed -match 'percentiles-histogram:\s*$') {
                    $currentContext = "percentiles-histogram"
                    continue
                }
                if ($trimmed -match 'percentiles:\s*$') {
                    $currentContext = "percentiles"
                    continue
                }
                if ($trimmed -match 'sla:\s*$') {
                    $currentContext = "sla"
                    continue
                }

                # 检查map key
                if ($trimmed -match '^\s+([^:]+):\s*(.+)$') {
                    $key = $matches[1].Trim()
                    # 检查是否包含特殊字符但未转义
                    if ($key -match '[\.:/\-]' -and $key -notmatch '^".*"$' -and $key -notmatch '^\[.*\]$') {
                        $violations += @{
                            Type    = "YAML map key未转义"
                            File    = $file
                            Line    = $lineNum
                            Message = "键 '$key' 包含特殊字符，应使用 `"[$key]`" 格式转义"
                        }
                        $hasViolations = $true
                        break
                    }
                }
            }
        }
    }

    # 快速失败：如果已有违规，不再继续检查
    if ($hasViolations) {
        break
    }
}

# 输出结果
if ($hasViolations) {
    Write-Host ""
    Write-Host "❌ 架构合规性检查失败" -ForegroundColor Red
    Write-Host ""
    Write-Host "发现以下违规：" -ForegroundColor Yellow
    Write-Host ""

    foreach ($violation in $violations) {
        Write-Host "  [$($violation.Type)]" -ForegroundColor Red
        Write-Host "    文件: $($violation.File):$($violation.Line)" -ForegroundColor Gray
        Write-Host "    说明: $($violation.Message)" -ForegroundColor Gray
        Write-Host ""
    }

    Write-Host "请修复违规后再提交。" -ForegroundColor Yellow
    Write-Host "如需跳过检查（不推荐），使用: git commit --no-verify" -ForegroundColor Gray
    Write-Host ""

    exit 1
}

exit 0

