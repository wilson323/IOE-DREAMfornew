# 文档一致性检查脚本
# 用途：检查所有文档中关于依赖架构的描述是否一致
# 执行：.\scripts\check-document-consistency.ps1

param(
    [switch]$FailOnError = $false,
    [switch]$Verbose = $false
)

$ErrorActionPreference = "Stop"
$script:HasError = $false
$script:Warnings = @()
$script:Inconsistencies = @()

# 颜色输出函数
function Write-ColorOutput($ForegroundColor, $Message) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    Write-Output $Message
    $host.UI.RawUI.ForegroundColor = $fc
}

function Write-Success($Message) {
    Write-ColorOutput Green "✅ $Message"
}

function Write-Error($Message) {
    Write-ColorOutput Red "❌ $Message"
    $script:HasError = $true
}

function Write-Warning($Message) {
    Write-ColorOutput Yellow "⚠️  $Message"
    $script:Warnings += $Message
}

function Write-Info($Message) {
    if ($Verbose) {
        Write-Output "ℹ️  $Message"
    }
}

Write-Output "=========================================="
Write-Output "文档一致性检查脚本"
Write-Output "=========================================="
Write-Output ""

# 检查项1：模块状态一致性
Write-Output "📋 检查项1：模块状态一致性"
Write-Output "----------------------------------------"

$moduleStatusPatterns = @(
    @{
        Name = "规划/未落地描述"
        Pattern = "规划/历史遗留命名|未落地|规划中的模块"
        Expected = $false
        Message = "发现过时的'规划/未落地'描述，所有细粒度模块已真实落地"
    },
    @{
        Name = "模块已落地描述"
        Pattern = "已真实落地|已落地并纳入|已全部落地"
        Expected = $true
        Message = "验证模块落地状态描述"
    }
)

$docFiles = Get-ChildItem -Path "documentation" -Filter "*.md" -Recurse | Where-Object { $_.FullName -notmatch "archive|backup" }

foreach ($pattern in $moduleStatusPatterns) {
    $found = @()
    foreach ($file in $docFiles) {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        if ($content -and $content -match $pattern.Pattern) {
            $found += $file.FullName
        }
    }

    if ($pattern.Expected) {
        if ($found.Count -gt 0) {
            Write-Success "找到 $($found.Count) 个文档包含正确的模块落地状态描述"
        } else {
            Write-Warning "未找到模块落地状态描述，建议添加"
        }
    } else {
        if ($found.Count -gt 0) {
            Write-Error "$($pattern.Message) - 发现 $($found.Count) 个文件"
            foreach ($file in $found) {
                Write-Info "  - $file"
                $script:Inconsistencies += @{
                    File = $file
                    Issue = $pattern.Message
                    Type = "模块状态不一致"
                }
            }
        } else {
            Write-Success "未发现过时的模块状态描述"
        }
    }
}

Write-Output ""

# 检查项2：依赖架构一致性
Write-Output "📋 检查项2：依赖架构一致性"
Write-Output "----------------------------------------"

$architecturePatterns = @(
    @{
        Name = "聚合模块描述"
        Pattern = "microservices-common.*聚合|公共库聚合|聚合模块"
        Expected = $false
        Message = "发现过时的'聚合模块'描述，microservices-common已重构为配置类容器"
    },
    @{
        Name = "配置类容器描述"
        Pattern = "配置类容器|配置类和工具类容器"
        Expected = $true
        Message = "验证microservices-common新定位描述"
    }
)

foreach ($pattern in $architecturePatterns) {
    $found = @()
    foreach ($file in $docFiles) {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        if ($content -and $content -match $pattern.Pattern) {
            $found += $file.FullName
        }
    }

    if ($pattern.Expected) {
        if ($found.Count -gt 0) {
            Write-Success "找到 $($found.Count) 个文档包含正确的配置类容器描述"
        } else {
            Write-Warning "未找到配置类容器描述，建议添加"
        }
    } else {
        if ($found.Count -gt 0) {
            Write-Error "$($pattern.Message) - 发现 $($found.Count) 个文件"
            foreach ($file in $found) {
                Write-Info "  - $file"
                $script:Inconsistencies += @{
                    File = $file
                    Issue = $pattern.Message
                    Type = "依赖架构不一致"
                }
            }
        } else {
            Write-Success "未发现过时的聚合模块描述"
        }
    }
}

Write-Output ""

# 检查项3：执行状态一致性
Write-Output "📋 检查项3：执行状态一致性"
Write-Output "----------------------------------------"

$executionStatusPatterns = @(
    @{
        Name = "待执行状态"
        Pattern = "待执行|待修复|待重构|计划执行"
        Expected = $false
        Message = "发现过时的'待执行'状态标记，方案C已执行完成"
    },
    @{
        Name = "已执行完成"
        Pattern = "已执行完成|执行完成|已完成|✅.*完成"
        Expected = $true
        Message = "验证方案C执行状态标记"
    }
)

foreach ($pattern in $executionStatusPatterns) {
    $found = @()
    foreach ($file in $docFiles) {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        if ($content -and $content -match $pattern.Pattern) {
            $found += $file.FullName
        }
    }

    if ($pattern.Expected) {
        if ($found.Count -gt 0) {
            Write-Success "找到 $($found.Count) 个文档包含正确的执行完成状态"
        } else {
            Write-Warning "未找到执行完成状态标记，建议添加"
        }
    } else {
        if ($found.Count -gt 0) {
            Write-Error "$($pattern.Message) - 发现 $($found.Count) 个文件"
            foreach ($file in $found) {
                Write-Info "  - $file"
                $script:Inconsistencies += @{
                    File = $file
                    Issue = $pattern.Message
                    Type = "执行状态不一致"
                }
            }
        } else {
            Write-Success "未发现过时的待执行状态标记"
        }
    }
}

Write-Output ""

# 生成检查报告
Write-Output "=========================================="
Write-Output "检查报告"
Write-Output "=========================================="
Write-Output ""

if ($script:Inconsistencies.Count -eq 0) {
    Write-Success "所有文档一致性检查通过！"
    Write-Output ""
    Write-Output "✅ 模块状态描述一致"
    Write-Output "✅ 依赖架构描述一致"
    Write-Output "✅ 执行状态标记一致"
} else {
    Write-Error "发现 $($script:Inconsistencies.Count) 个不一致问题"
    Write-Output ""
    Write-Output "不一致问题列表："
    Write-Output "----------------------------------------"
    
    $grouped = $script:Inconsistencies | Group-Object -Property Type
    foreach ($group in $grouped) {
        Write-Output ""
        Write-ColorOutput Yellow "【$($group.Name)】"
        foreach ($item in $group.Group) {
            Write-Output "  - $($item.File)"
            Write-Output "    问题: $($item.Issue)"
        }
    }
    
    Write-Output ""
    Write-Output "修复建议："
    Write-Output "1. 更新所有包含过时描述的文档"
    Write-Output "2. 统一使用'配置类容器'描述microservices-common"
    Write-Output "3. 统一标记方案C执行状态为'已执行完成'"
    Write-Output "4. 运行脚本验证修复效果"
}

Write-Output ""
Write-Output "警告数量: $($script:Warnings.Count)"
if ($script:Warnings.Count -gt 0 -and $Verbose) {
    Write-Output ""
    Write-Output "警告列表："
    foreach ($warning in $script:Warnings) {
        Write-Warning $warning
    }
}

Write-Output ""

if ($script:HasError) {
    if ($FailOnError) {
        exit 1
    } else {
        Write-Warning "发现不一致问题，但未设置-FailOnError，继续执行"
        exit 0
    }
} else {
    exit 0
}

