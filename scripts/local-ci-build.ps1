# IOE-DREAM 本地CI构建脚本
# 替代GitHub Actions，支持完整的构建、测试和部署流程

param(
    [string]$Service = "",
    [switch]$Clean,
    [switch]$SkipTests,
    [switch]$SkipPMD,
    [switch]$SkipQualityGate,
    [switch]$OnlyCompile,
    [switch]$Help
)

function Show-Usage {
    Write-Host "IOE-DREAM 本地CI构建脚本" -ForegroundColor Green
    Write-Host ""
    Write-Host "用法:" -ForegroundColor Yellow
    Write-Host "  .\scripts\local-ci-build.ps1 [参数]"
    Write-Host ""
    Write-Host "参数:" -ForegroundColor Yellow
    Write-Host "  -Service <服务名>     指定要构建的服务 (如 ioedream-access-service)"
    Write-Host "  -Clean              清理target目录后构建"
    Write-Host "  -SkipTests           跳过测试执行"
    Write-Host "  -SkipPMD             跳过PMD代码质量检查"
    Write-Host "  -SkipQualityGate     跳过质量门禁检查"
    Write-Host "  -OnlyCompile         仅编译，不执行后续步骤"
    Write-Host "  -Help                显示此帮助信息"
    Write-Host ""
    Write-Host "示例:" -ForegroundColor Yellow
    Write-Host "  .\scripts\local-ci-build.ps1                                    # 构建所有服务"
    Write-Host "  .\scripts\local-ci-build.ps1 -Service ioedream-access-service    # 构建指定服务"
    Write-Host "  .\scripts\local-ci-build.ps1 -Clean -SkipTests                  # 清理构建并跳过测试"
    Write-Host ""
}

function Write-Log {
    param([string]$Message, [string]$Level = "INFO")

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $color = switch ($Level) {
        "ERROR" { "Red" }
        "WARN" { "Yellow" }
        "SUCCESS" { "Green" }
        "INFO" { "Cyan" }
        default { "White" }
    }

    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $color
}

function Test-Prerequisites {
    Write-Log "检查构建前置条件..." "INFO"

    # 检查Java版本
    try {
        $javaVersion = & java -version 2>&1
        if ($javaVersion -match "version \"?(\d+)\.(\d+)") {
            $majorVersion = [int]$matches[1]
            if ($majorVersion -lt 17) {
                Write-Log "需要Java 17或更高版本，当前版本: $majorVersion" "ERROR"
                return $false
            }
            Write-Log "Java版本检查通过: $majorVersion" "SUCCESS"
        }
    } catch {
        Write-Log "Java未安装或不可用" "ERROR"
        return $false
    }

    # 检查Maven
    try {
        $mvnVersion = & mvn -version 2>&1
        Write-Log "Maven版本: $($mvnVersion.Split('\n')[0])" "SUCCESS"
    } catch {
        Write-Log "Maven未安装或不可用" "ERROR"
        return $false
    }

    # 检查项目结构
    if (-not (Test-Path "microservices\pom.xml")) {
        Write-Log "项目根目录结构异常，找不到 microservices\pom.xml" "ERROR"
        return $false
    }

    Write-Log "前置条件检查通过" "SUCCESS"
    return $true
}

function Invoke-QualityGate {
    Write-Log "执行质量门禁检查..." "INFO"

    $qualityIssues = @()

    # 检查Maven依赖冲突
    Write-Log "检查Maven依赖冲突..." "INFO"
    try {
        & mvn dependency:analyze -q -f "microservices\pom.xml"
        if ($LASTEXITCODE -ne 0) {
            $qualityIssues += "发现Maven依赖冲突"
        }
    } catch {
        $qualityIssues += "依赖分析失败: $($_.Exception.Message)"
    }

    # 检查代码覆盖率
    if (-not $SkipTests) {
        Write-Log "检查代码覆盖率..." "INFO"
        $jacocoFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "jacoco.exec" -File
        if ($jacocoFiles.Count -eq 0) {
            $qualityIssues += "未找到代码覆盖率报告文件"
        }
    }

    # 检查PMD违规
    if (-not $SkipPMD) {
        Write-Log "检查PMD代码质量..." "INFO"
        $pmdReports = Get-ChildItem -Path "microservices" -Recurse -Filter "pmd.xml" -File
        foreach ($report in $pmdReports) {
            try {
                [xml]$pmdXml = Get-Content $report.FullName
                $violations = $pmdXml.pmd.file.violation.Count
                if ($violations -gt 0) {
                    $qualityIssues += "发现 $violations 个PMD违规在 $($report.Name)"
                }
            } catch {
                Write-Log "解析PMD报告失败: $($report.Name)" "WARN"
            }
        }
    }

    # 输出质量门禁结果
    if ($qualityIssues.Count -eq 0) {
        Write-Log "质量门禁检查通过 ✅" "SUCCESS"
        return $true
    } else {
        Write-Log "质量门禁检查失败 ❌" "ERROR"
        foreach ($issue in $qualityIssues) {
            Write-Log "  - $issue" "ERROR"
        }
        return $false
    }
}

function Invoke-Build {
    param([string]$ServiceName)

    Write-Log "开始构建服务: $ServiceName" "INFO"

    $mavenArgs = @()
    if ($ServiceName) {
        $mavenArgs += "-pl", $ServiceName, "-am"
    }

    if ($Clean) {
        $mavenArgs += "clean"
    }

    $mavenArgs += "install"

    if ($SkipTests) {
        $mavenArgs += "-DskipTests"
    }

    if ($SkipPMD) {
        $mavenArgs += "-Dpmd.skip=true"
    }

    $mavenArgs += "-q"  # 安静模式，减少输出

    try {
        Write-Log "执行Maven构建命令: mvn $($mavenArgs -join ' ')" "INFO"
        & mvn $mavenArgs -f "microservices\pom.xml"

        if ($LASTEXITCODE -eq 0) {
            Write-Log "构建成功 ✅" "SUCCESS"
            return $true
        } else {
            Write-Log "构建失败 ❌ (退出码: $LASTEXITCODE)" "ERROR"
            return $false
        }
    } catch {
        Write-Log "构建过程中发生异常: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Generate-BuildReport {
    Write-Log "生成构建报告..." "INFO"

    $reportPath = "build-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"

    $report = @"
# IOE-DREAM 构建报告

**构建时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**构建参数**: $PSBoundParameters

## 构建统计

- 处理的服务: $($Service ? $Service : '所有服务')
- 清理构建: $($Clean ? '是' : '否')
- 跳过测试: $($SkipTests ? '是' : '否')
- 跳过PMD: $($SkipPMD ? '是' : '否')
- 仅编译: $($OnlyCompile ? '是' : '否')

## 构建结果

$(@"
- 构建状态: $buildSuccess
- 质量门禁: $qualityGateSuccess
"@)

## 生成的产物

"@

    # 列出生成的JAR文件
    $jarFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.jar" -File
    if ($jarFiles.Count -gt 0) {
        $report += "`n### JAR文件`n`n"
        foreach ($jar in $jarFiles) {
            $report += "- $($jar.FullName)`n"
        }
    }

    # 列出测试报告
    $testReports = Get-ChildItem -Path "microservices" -Recurse -Filter "surefire-reports" -Directory
    if ($testReports.Count -gt 0) {
        $report += "`n### 测试报告`n`n"
        foreach ($reportDir in $testReports) {
            $report += "- $($reportDir.FullName)`n"
        }
    }

    # 列出代码覆盖率报告
    $jacocoReports = Get-ChildItem -Path "microservices" -Recurse -Filter "index.html" -File | Where-Object { $_.FullName -match "jacoco" }
    if ($jacocoReports.Count -gt 0) {
        $report += "`n### 代码覆盖率报告`n`n"
        foreach ($jacoco in $jacocoReports) {
            $report += "- $($jacoco.FullName)`n"
        }
    }

    $report | Out-File -FilePath $reportPath -Encoding UTF8
    Write-Log "构建报告已生成: $reportPath" "SUCCESS"
}

# 主程序入口
if ($Help) {
    Show-Usage
    exit 0
}

Write-Log "🚀 开始 IOE-DREAM 本地CI构建" "INFO"
Write-Log "构建参数: $($PSBoundParameters | ConvertTo-Json -Compress)" "INFO"

# 执行前置条件检查
if (-not (Test-Prerequisites)) {
    Write-Log "前置条件检查失败，终止构建" "ERROR"
    exit 1
}

# 执行构建
$buildSuccess = Invoke-Build -ServiceName $Service

# 如果构建成功且不跳过质量门禁，执行质量检查
$qualityGateSuccess = $true
if ($buildSuccess -and -not $SkipQualityGate -and -not $OnlyCompile) {
    $qualityGateSuccess = Invoke-QualityGate
}

# 生成构建报告
Generate-BuildReport

# 最终结果判断
if ($buildSuccess -and $qualityGateSuccess) {
    Write-Log "🎉 CI构建完成！所有检查通过" "SUCCESS"
    exit 0
} else {
    Write-Log "❌ CI构建失败！请检查上述错误信息" "ERROR"
    exit 1
}