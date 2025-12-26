# IOE-DREAM SonarQube 代码质量分析脚本
# 用于自动化代码质量分析和持续集成
#
# 使用方法:
# 1. 本地分析: .\scripts\sonar-analysis.ps1
# 2. 指定模块: .\scripts\sonar-analysis.ps1 -Module microservices-common
# 3. 跳过测试: .\scripts\sonar-analysis.ps1 -SkipTests
# 4. CI模式: .\scripts\sonar-analysis.ps1 -CI
#
# 参数说明:
# -Module: 指定要分析的模块 (默认分析所有模块)
# -SkipTests: 跳过单元测试执行
# -CI: CI/CD模式，自动上传到SonarQube服务器
# -SonarUrl: SonarQube服务器地址
# -ProjectKey: 项目键名
# -Token: 认证令牌

param(
    [string]$Module = "",
    [switch]$SkipTests = $false,
    [switch]$CI = $false,
    [string]$SonarUrl = "http://localhost:9000",
    [string]$ProjectKey = "ioedream-microservices",
    [string]$Token = ""
)

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )

    $colors = @{
        "Red" = "Red"
        "Green" = "Green"
        "Yellow" = "Yellow"
        "Blue" = "Blue"
        "Cyan" = "Cyan"
        "White" = "White"
    }

    if ($colors.ContainsKey($Color)) {
        Write-Host $Message -ForegroundColor $colors[$Color]
    } else {
        Write-Host $Message
    }
}

# 输出标题
function Write-Title {
    param([string]$Title)
    Write-ColorOutput "`n" + "=" * 80 - "Cyan"
    Write-ColorOutput $Title - "Yellow"
    Write-ColorOutput "=" * 80 - "Cyan"
}

# 输出章节
function Write-Section {
    param([string]$Section)
    Write-ColorOutput "`n--- $Section ---" - "Green"
}

# 输出成功信息
function Write-Success {
    param([string]$Message)
    Write-ColorOutput "✅ $Message" - "Green"
}

# 输出错误信息
function Write-Error {
    param([string]$Message)
    Write-ColorOutput "❌ $Message" - "Red"
}

# 输出警告信息
function Write-Warning {
    param([string]$Message)
    Write-ColorOutput "⚠️  $Message" - "Yellow"
}

# 输出信息
function Write-Info {
    param([string]$Message)
    Write-ColorOutput "ℹ️  $Message" - "Blue"
}

# 检查依赖
function Test-Dependencies {
    Write-Section "检查分析依赖"

    $dependencies = @(
        @{Name = "Java"; Command = "java -version"},
        @{Name = "Maven"; Command = "mvn --version"},
        @{Name = "SonarQube"; Url = "$SonarUrl"}
    )

    foreach ($dep in $dependencies) {
        if ($dep.Command) {
            try {
                $null = & cmd /c $dep.Command 2`>$null
                if ($LASTEXITCODE -eq 0) {
                    Write-Success "$($dep.Name) 已安装"
                } else {
                    Write-Error "$($dep.Name) 未安装或配置错误"
                    return $false
                }
            } catch {
                Write-Error "检查 $($dep.Name) 时出错: $($_.Exception.Message)"
                return $false
            }
        }

        if ($dep.Url) {
            try {
                $response = Invoke-WebRequest -Uri $dep.Url -TimeoutSec 10 -UseBasicParsing
                if ($response.StatusCode -eq 200) {
                    Write-Success "SonarQube服务器可访问: $SonarUrl"
                } else {
                    Write-Warning "SonarQube服务器响应异常: $($response.StatusCode)"
                }
            } catch {
                Write-Warning "无法连接到SonarQube服务器: $SonarUrl"
            }
        }
    }

    return $true
}

# 构建项目
function Build-Project {
    Write-Section "构建项目"

    $buildArgs = @("clean", "compile")
    if (-not $SkipTests) {
        $buildArgs += "test"
    }

    if ($Module) {
        Write-Info "构建指定模块: $Module"
        $buildArgs += "-pl", $Module, "-am"
    }

    try {
        Write-Info "执行 Maven 构建..."
        $buildOutput = & mvn $buildArgs -DskipTests=$SkipTests 2>&1

        if ($LASTEXITCODE -ne 0) {
            Write-Error "项目构建失败"
            Write-ColorOutput "构建输出: $buildOutput" - "Red"
            return $false
        }

        Write-Success "项目构建成功"
    } catch {
        Write-Error "构建过程中发生异常: $($_.Exception.Message)"
        return $false
    }

    return $true
}

# 运行SonarQube分析
function Invoke-SonarAnalysis {
    Write-Section "运行SonarQube代码分析"

    $analysisArgs = @()

    if ($CI) {
        $analysisArgs += "sonar:sonar"
        Write-Info "CI模式: 分析完成后自动上传到SonarQube服务器"

        # 添加CI特定的Sonar参数
        $env:SONAR_HOST_URL = $SonarUrl
        if ($Token) {
            $env:SONAR_AUTH_TOKEN = $Token
        }
    } else {
        $analysisArgs += "sonar:sonar", "-Dsonar.host.url=$SonarUrl"
        Write-Info "本地模式: 分析完成后在浏览器中查看报告"
    }

    if ($Module) {
        $analysisArgs += "-pl", $Module
    }

    try {
        Write-Info "开始SonarQube分析..."
        Write-Info "分析参数: $($analysisArgs -join ' ')"

        $analysisOutput = & mvn $analysisArgs 2>&1

        Write-ColorOutput "`n分析输出:` - "Cyan"
        Write-ColorOutput $analysisOutput - "White"

        if ($LASTEXITCODE -eq 0) {
            Write-Success "SonarQube分析完成"

            if ($CI) {
                Write-Info "分析报告已上传到SonarQube服务器"
                Write-Info "访问地址: $SonarUrl/dashboard?id=$ProjectKey"
            } else {
                Write-Info "分析报告已生成本地"
                Write-Info "访问地址: $SonarUrl"
            }
        } else {
            Write-Error "SonarQube分析失败"
            return $false
        }
    } catch {
        Write-Error "SonarQube分析过程中发生异常: $($_.Exception.Message)"
        return $false
    }

    return $true
}

# 生成分析报告
function New-AnalysisReport {
    Write-Section "生成分析报告"

    $reportPath = "target\sonar-reports"
    if (-not (Test-Path $reportPath)) {
        New-Item -ItemType Directory -Path $reportPath -Force | Out-Null
    }

    $reportFile = "$reportPath\analysis-summary-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"

    $reportContent = @"
# IOE-DREAM 代码质量分析报告

**分析时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**分析模式**: $(if ($CI) { 'CI/CD模式' } else { '本地模式' })
**分析模块**: $(if ($Module) { $Module } else { '全部模块' })
**SonarQube地址**: $SonarUrl

## 分析结果摘要

### 质量指标
- 代码覆盖率: 计算中...
- 重复代码率: 计算中...
- 技术债务: 计算中...
- 安全漏洞: 计算中...

### 主要问题
分析进行中，详细结果请查看SonarQube仪表板。

### 改进建议
1. 提高单元测试覆盖率至85%以上
2. 减少代码重复
3. 修复安全漏洞
4. 降低代码复杂度

## 下一步行动

1. 登录SonarQube仪表板查看详细分析结果
2. 优先修复高优先级问题
3. 持续改进代码质量

---
*此报告由SonarQube分析脚本自动生成*
"@

    try {
        $reportContent | Out-File -FilePath $reportFile -Encoding UTF8
        Write-Success "分析报告已生成: $reportFile"
    } catch {
        Write-Error "生成分析报告失败: $($_.Exception.Message)"
    }
}

# 主执行流程
function Main {
    Write-Title "IOE-DREAM SonarQube 代码质量分析"

    Write-Info "分析配置:"
    Write-Info "  - 模块: $(if ($Module) { $Module } else { '全部模块' })"
    Write-Info "  - 跳过测试: $SkipTests"
    Write-Info "  - CI模式: $CI"
    Write-Info "  - SonarQube地址: $SonarUrl"
    Write-Info "  - 项目键名: $ProjectKey"

    # 检查依赖
    if (-not (Test-Dependencies)) {
        Write-Error "依赖检查失败，请先安装必要的工具"
        exit 1
    }

    # 构建项目
    if (-not (Build-Project)) {
        Write-Error "项目构建失败，请检查代码和配置"
        exit 1
    }

    # 运行SonarQube分析
    if (-not (Invoke-SonarAnalysis)) {
        Write-Error "SonarQube分析失败，请检查配置和网络连接"
        exit 1
    }

    # 生成报告
    New-AnalysisReport

    Write-Section "分析完成"
    Write-Success "SonarQube代码质量分析已成功完成！"

    if ($CI) {
        Write-Info "📊 详细报告: $SonarUrl/dashboard?id=$ProjectKey"
    } else {
        Write-Info "📊 SonarQube仪表板: $SonarUrl"
    }

    Write-Info "📄 分析报告: ./target/sonar-reports/"
    Write-Info "🔧 配置文件: ./sonar-project.properties"
}

# 执行主函数
Main