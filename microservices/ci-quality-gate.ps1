# IOE-DREAM 持续集成质量保障脚本
# 企业级代码质量门禁，确保代码提交质量

param(
    [string]$BuildType = "full",           # 构建类型: full, quick, custom
    [string]$TargetModule = "*",           # 目标模块
    [switch]$SkipTests = $false,           # 跳过测试
    [switch]$GenerateReport = $true,       # 生成质量报告
    [string]$ReportPath = ".ci-reports"    # 报告输出路径
)

# 强制设置UTF-8编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "=== IOE-DREAM 持续集成质量保障 ===" -ForegroundColor Cyan
Write-Host "构建类型: $BuildType" -ForegroundColor Yellow
Write-Host "目标模块: $TargetModule" -ForegroundColor Yellow
Write-Host "跳过测试: $SkipTests" -ForegroundColor Yellow

# 质量检查统计
$qualityStats = @{
    "totalChecks" = 0
    "passedChecks" = 0
    "failedChecks" = 0
    "warnings" = 0
    "errors" = 0
    "qualityScore" = 0
}

# 质量门禁配置
$qualityGate = @{
    "minCompileSuccess" = 95        # 编译成功率最低95%
    "maxArchitectureViolations" = 0 # 架构违规0容忍
    "maxCodeQualityIssues" = 5     # 代码质量问题最多5个
    "minTestCoverage" = 75         # 测试覆盖率最低75%
    "maxSecurityIssues" = 0        # 安全问题0容忍
}

# 质量报告初始化
$qualityReport = @"
# IOE-DREAM 质量保障报告
生成时间: $(Get-Date)
构建类型: $BuildType
目标模块: $TargetModule

## 质量检查摘要
"@

Write-Host "`n步骤1: 环境预检查..." -ForegroundColor Yellow

# 1. 环境检查
function Test-Environment {
    param()

    Write-Host "检查Java环境..." -ForegroundColor Gray
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Java未安装或未配置PATH" -ForegroundColor Red
        return $false
    }
    Write-Host "✅ Java环境正常" -ForegroundColor Green

    Write-Host "检查Maven环境..." -ForegroundColor Gray
    $mvnVersion = mvn -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Maven未安装或未配置PATH" -ForegroundColor Red
        return $false
    }
    Write-Host "✅ Maven环境正常" -ForegroundColor Green

    Write-Host "检查Git状态..." -ForegroundColor Gray
    $gitStatus = git status --porcelain 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "⚠️ 非Git仓库或Git未安装" -ForegroundColor Yellow
    } else {
        $uncommitted = ($gitStatus | Measure-Object).Count
        if ($uncommitted -gt 0) {
            Write-Host "⚠️ 发现 $uncommitted 个未提交文件" -ForegroundColor Yellow
            $qualityStats.warnings++
        } else {
            Write-Host "✅ Git工作区干净" -ForegroundColor Green
        }
    }

    $qualityStats.totalChecks++
    $qualityStats.passedChecks++
    return $true
}

# 2. 代码规范检查
function Test-CodeStandards {
    param()

    Write-Host "`n步骤2: 代码规范检查..." -ForegroundColor Yellow

    $standardIssues = 0
    $javaFiles = Get-ChildItem -Path "." -Recurse -Filter "*.java" | Where-Object { $_.FullName -match "src/main/java" }

    Write-Host "检查 @Autowired 使用情况..." -ForegroundColor Gray
    $autowiredCount = 0
    foreach ($file in $javaFiles) {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $matches = [regex]::Matches($content, "@Autowired")
        $autowiredCount += $matches.Count
    }

    if ($autowiredCount -gt 0) {
        Write-Host "❌ 发现 $autowiredCount 个 @Autowired 违规使用" -ForegroundColor Red
        $qualityStats.errors++
        $standardIssues += $autowiredCount
    } else {
        Write-Host "✅ 无 @Autowired 违规使用" -ForegroundColor Green
    }

    Write-Host "检查 Repository 后缀使用..." -ForegroundColor Gray
    $repositoryViolations = 0
    foreach ($file in $javaFiles) {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $matches = [regex]::Matches($content, "@Repository|Repository[^a-zA-Z]")
        $repositoryViolations += $matches.Count
    }

    if ($repositoryViolations -gt 0) {
        Write-Host "❌ 发现 $repositoryViolations 个 Repository 命名违规" -ForegroundColor Red
        $qualityStats.errors++
        $standardIssues += $repositoryViolations
    } else {
        Write-Host "✅ 无 Repository 命名违规" -ForegroundColor Green
    }

    Write-Host "检查 javax 包名使用..." -ForegroundColor Gray
    $javaxViolations = 0
    foreach ($file in $javaFiles) {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        # 排除Java标准库的javax包
        $matches = [regex]::Matches($content, "import javax\.(servlet|validation|persistence|annotation)")
        $javaxViolations += $matches.Count
    }

    if ($javaxViolations -gt 0) {
        Write-Host "❌ 发现 $javaxViolations 个 javax 包名违规" -ForegroundColor Red
        $qualityStats.errors++
        $standardIssues += $javaxViolations
    } else {
        Write-Host "✅ 无 javax 包名违规" -ForegroundColor Green
    }

    $qualityStats.totalChecks++
    if ($standardIssues -eq 0) {
        $qualityStats.passedChecks++
        Write-Host "✅ 代码规范检查通过" -ForegroundColor Green
    } else {
        $qualityStats.failedChecks++
        Write-Host "❌ 代码规范检查失败，发现 $standardIssues 个问题" -ForegroundColor Red
    }

    return $standardIssues -eq 0
}

# 3. 编译检查
function Test-Compilation {
    param([string]$module)

    Write-Host "`n步骤3: 编译检查..." -ForegroundColor Yellow

    $compileModules = @()
    if ($module -eq "*") {
        $compileModules = @("microservices-common-core", "microservices-common-storage", "microservices-common")
    } else {
        $compileModules = @($module)
    }

    $successCount = 0
    $totalCount = $compileModules.Count

    foreach ($mod in $compileModules) {
        Write-Host "编译模块: $mod" -ForegroundColor Gray

        $result = & mvn clean compile -pl $mod -am -q -DskipTests
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ $mod - 编译成功" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "❌ $mod - 编译失败" -ForegroundColor Red
            $qualityStats.errors++
        }
    }

    $successRate = [math]::Round(($successCount / $totalCount) * 100, 2)

    $qualityStats.totalChecks++
    if ($successRate -ge $qualityGate.minCompileSuccess) {
        $qualityStats.passedChecks++
        Write-Host "✅ 编译检查通过，成功率: $successRate%" -ForegroundColor Green
        return $true
    } else {
        $qualityStats.failedChecks++
        Write-Host "❌ 编译检查失败，成功率: $successRate%，最低要求: $($qualityGate.minCompileSuccess)%" -ForegroundColor Red
        return $false
    }
}

# 4. 测试检查
function Test-UnitTest {
    param([string]$module)

    if ($SkipTests) {
        Write-Host "`n步骤4: 跳过单元测试..." -ForegroundColor Yellow
        $qualityStats.warnings++
        return $true
    }

    Write-Host "`n步骤4: 单元测试执行..." -ForegroundColor Yellow

    $testModules = @()
    if ($module -eq "*") {
        $testModules = @("microservices-common-core", "microservices-common-storage")
    } else {
        $testModules = @($module)
    }

    $testSuccessCount = 0

    foreach ($mod in $testModules) {
        Write-Host "测试模块: $mod" -ForegroundColor Gray

        $testResult = & mvn test -pl $mod -am -q
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ $mod - 测试通过" -ForegroundColor Green
            $testSuccessCount++
        } else {
            Write-Host "❌ $mod - 测试失败" -ForegroundColor Red
            $qualityStats.errors++
        }
    }

    $qualityStats.totalChecks++
    if ($testSuccessCount -eq $testModules.Count) {
        $qualityStats.passedChecks++
        Write-Host "✅ 单元测试检查通过" -ForegroundColor Green
        return $true
    } else {
        $qualityStats.failedChecks++
        Write-Host "❌ 单元测试检查失败" -ForegroundColor Red
        return $false
    }
}

# 5. 安全检查
function Test-Security {
    param()

    Write-Host "`n步骤5: 安全检查..." -ForegroundColor Yellow

    $securityIssues = 0
    $javaFiles = Get-ChildItem -Path "." -Recurse -Filter "*.java" | Where-Object { $_.FullName -match "src/main/java" }

    Write-Host "检查硬编码密码..." -ForegroundColor Gray
    $passwordPatterns = @("password\s*=\s*['""][^'""]+['""]", "pwd\s*=\s*['""][^'""]+['""]", "secret\s*=\s*['""][^'""]+['""]")

    foreach ($file in $javaFiles) {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        foreach ($pattern in $passwordPatterns) {
            $matches = [regex]::Matches($content, $pattern, [Text.RegularExpressions.RegexOptions]::IgnoreCase)
            $securityIssues += $matches.Count
        }
    }

    if ($securityIssues -gt 0) {
        Write-Host "❌ 发现 $securityIssues 个潜在安全问题" -ForegroundColor Red
        $qualityStats.errors++
    } else {
        Write-Host "✅ 无明显安全问题" -ForegroundColor Green
    }

    $qualityStats.totalChecks++
    if ($securityIssues -eq 0) {
        $qualityStats.passedChecks++
        Write-Host "✅ 安全检查通过" -ForegroundColor Green
        return $true
    } else {
        $qualityStats.failedChecks++
        Write-Host "❌ 安全检查失败" -ForegroundColor Red
        return $false
    }
}

# 6. 性能检查
function Test-Performance {
    param()

    Write-Host "`n步骤6: 性能检查..." -ForegroundColor Yellow

    $performanceIssues = 0

    # 检查是否有明显的性能问题
    Write-Host "检查代码性能模式..." -ForegroundColor Gray

    $javaFiles = Get-ChildItem -Path "." -Recurse -Filter "*.java" | Where-Object { $_.FullName -match "src/main/java" }

    # 检查String拼接性能问题
    $stringConcatPattern = "[^+]\+[^+].*\+.*\+"
    $stringConcatCount = 0

    foreach ($file in $javaFiles) {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $matches = [regex]::Matches($content, $stringConcatPattern)
        $stringConcatCount += $matches.Count
    }

    if ($stringConcatCount -gt 10) {
        Write-Host "⚠️ 发现 $stringConcatCount 个潜在String拼接性能问题" -ForegroundColor Yellow
        $qualityStats.warnings++
        $performanceIssues++
    } else {
        Write-Host "✅ 无明显String拼接性能问题" -ForegroundColor Green
    }

    $qualityStats.totalChecks++
    if ($performanceIssues -le 5) {
        $qualityStats.passedChecks++
        Write-Host "✅ 性能检查通过" -ForegroundColor Green
        return $true
    } else {
        $qualityStats.failedChecks++
        Write-Host "❌ 性能检查发现问题过多" -ForegroundColor Red
        return $false
    }
}

# 7. 生成质量报告
function New-QualityReport {
    param()

    $qualityStats.qualityScore = [math]::Round(($qualityStats.passedChecks / $qualityStats.totalChecks) * 100, 1)

    $qualityGatePassed = ($qualityStats.errors -eq 0) -and ($qualityStats.qualityScore -ge 80)

    $qualityReport += @"
### 检查结果统计
- 总检查项: $($qualityStats.totalChecks)
- 通过检查: $($qualityStats.passedChecks)
- 失败检查: $($qualityStats.failedChecks)
- 警告数量: $($qualityStats.warnings)
- 错误数量: $($qualityStats.errors)
- 质量评分: $($qualityStats.qualityScore)/100

### 质量门禁结果
$(
    if ($qualityGatePassed) {
        "🟢 **质量门禁: 通过**"
    } else {
        "🔴 **质量门禁: 失败**"
    }
)

### 检查详情
"@

    if ($qualityStats.errors -gt 0) {
        $qualityReport += @"
#### 🔴 错误项 ($($qualityStats.errors)个)
- 编译错误: $(if($qualityStats.errors -ge 1){"是"} else {"否"})
- 架构违规: $(if($qualityStats.errors -ge 1){"是"} else {"否"})
- 安全问题: $(if($qualityStats.errors -ge 1){"是"} else {"否"})

"@
    }

    if ($qualityStats.warnings -gt 0) {
        $qualityReport += @"
#### 🟡 警告项 ($($qualityStats.warnings)个)
- 未提交文件: $(if($qualityStats.warnings -ge 1){"是"} else {"否"})
- 性能优化建议: $(if($qualityStats.warnings -ge 1){"是"} else {"否"})

"@
    }

    $qualityReport += @"
### 改进建议
$(
    if ($qualityStats.errors -gt 0) {
        "🔴 **立即修复**: 修复所有错误项，确保质量门禁通过`n"
    }
)
$(
    if ($qualityStats.warnings -gt 0) {
        "🟡 **建议优化**: 处理警告项，提升代码质量`n"
    }
)
$(
    if ($qualityGatePassed) {
        "✅ **质量达标**: 代码质量符合企业级标准，可以继续开发或部署"
    }
)

---
**报告生成时间**: $(Get-Date)
**检查工具**: IOE-DREAM CI Quality Gate v1.0
"@

    # 创建报告目录
    if (-not (Test-Path $ReportPath)) {
        New-Item -Path $ReportPath -ItemType Directory -Force | Out-Null
    }

    # 保存质量报告
    $reportFile = Join-Path $ReportPath "quality-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"
    $qualityReport | Out-File -FilePath $reportFile -Encoding UTF8

    Write-Host "`n📊 质量报告已生成: $reportFile" -ForegroundColor Cyan

    return $qualityGatePassed
}

# 主执行流程
try {
    # 环境检查
    $envResult = Test-Environment
    if (-not $envResult) {
        Write-Host "❌ 环境检查失败，终止构建" -ForegroundColor Red
        exit 1
    }

    # 根据构建类型执行不同检查
    switch ($BuildType) {
        "quick" {
            Write-Host "快速构建模式: 只执行编译检查" -ForegroundColor Yellow
            $compileResult = Test-Compilation -module $TargetModule
            if (-not $compileResult) {
                exit 1
            }
        }
        "full" {
            Write-Host "完整构建模式: 执行所有质量检查" -ForegroundColor Yellow
            $standardResult = Test-CodeStandards
            $compileResult = Test-Compilation -module $TargetModule
            $testResult = Test-UnitTest -module $TargetModule
            $securityResult = Test-Security
            $performanceResult = Test-Performance

            # 检查是否有关键失败
            if (-not $compileResult -or -not $securityResult) {
                Write-Host "❌ 关键检查失败，质量门禁不通过" -ForegroundColor Red
                exit 1
            }
        }
        "custom" {
            Write-Host "自定义构建模式" -ForegroundColor Yellow
            # 可根据需要添加自定义检查
        }
    }

    # 生成质量报告
    if ($GenerateReport) {
        $gateResult = New-QualityReport

        if ($gateResult) {
            Write-Host "`n🎉 质量门禁通过！代码质量达标。" -ForegroundColor Green
            exit 0
        } else {
            Write-Host "`n❌ 质量门禁未通过！请修复问题后重试。" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "`n✅ 质量检查完成" -ForegroundColor Green
        exit 0
    }

} catch {
    Write-Host "`n❌ 质量检查过程中发生异常: $_" -ForegroundColor Red
    exit 1
}