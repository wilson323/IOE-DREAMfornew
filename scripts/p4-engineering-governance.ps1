# P4: 工程治理脚本 - 建立CI检查和防护机制
# 作者: IOE-DREAM架构团队
# 目的: 建立编译质量门禁、IDE配置检查、代码质量保障

param(
    [switch]$SkipBuild,
    [switch]$SkipIdeCheck,
    [switch]$FixIssues,
    [string]$ServiceFilter = ""
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "P4: 工程治理 - CI检查和防护机制" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

# 1. 编译质量门禁检查
function Test-CompilationQualityGate {
    Write-Host "`n## 1. 编译质量门禁检查 ##" -ForegroundColor Yellow

    $services = @(
        "ioedream-gateway-service",
        "ioedream-common-service",
        "ioedream-device-comm-service",
        "ioedream-oa-service",
        "ioedream-access-service",
        "ioedream-attendance-service",
        "ioedream-video-service",
        "ioedream-consume-service",
        "ioedream-visitor-service",
        "ioedream-biometric-service",
        "ioedream-database-service"
    )

    if ($ServiceFilter) {
        $services = $services | Where-Object { $_ -like "*$ServiceFilter*" }
    }

    $successCount = 0
    $totalCount = $services.Count

    foreach ($service in $services) {
        Write-Host "检查服务: $service" -ForegroundColor White

        # 跳过common模块，只检查服务
        if ($service -like "*common*") {
            continue
        }

        try {
            # 使用Maven编译检查
            $result = & mvn clean compile -pl $service -am -Dmaven.test.skip=true -Dmaven.clean.failOnError=false -q

            if ($LASTEXITCODE -eq 0) {
                Write-Host "  ✅ $service 编译成功" -ForegroundColor Green
                $successCount++
            } else {
                Write-Host "  ❌ $service 编译失败" -ForegroundColor Red

                # 获取详细错误信息
                $errorDetails = & mvn clean compile -pl $service -am -Dmaven.test.skip=true -Dmaven.clean.failOnError=false 2>&1 |
                    Select-String -Pattern "ERROR.*找不到符号|ERROR.*无法访问|ERROR.*不存在" |
                    Select-Object -First 5

                if ($errorDetails) {
                    Write-Host "     主要错误:" -ForegroundColor Red
                    $errorDetails | ForEach-Object { Write-Host "     - $_" -ForegroundColor DarkRed }
                }
            }
        } catch {
            Write-Host "  ❌ $service 检查异常: $($_.Exception.Message)" -ForegroundColor Red
        }
    }

    $passRate = [math]::Round(($successCount / $totalCount) * 100, 2)
    Write-Host "`n编译通过率: $successCount/$totalCount ($passRate%)" -ForegroundColor $(if($passRate -ge 80) {"Green"} else {"Red"})

    return $passRate -ge 80
}

# 2. IDE配置检查
function Test-IdeConfiguration {
    Write-Host "`n## 2. IDE配置检查 ##" -ForegroundColor Yellow

    $issues = @()

    # 检查VSCode设置
    $vscodeSettingsPath = ".vscode/settings.json"
    if (Test-Path $vscodeSettingsPath) {
        Write-Host "检查VSCode配置..." -ForegroundColor White

        try {
            $settings = Get-Content $vscodeSettingsPath -Raw | ConvertFrom-Json

            # 检查Java配置
            if ($settings."java.home") {
                Write-Host "  ✅ Java Home 配置: $($settings."java.home")" -ForegroundColor Green
            } else {
                $issues += "VSCode Java Home 未配置"
                Write-Host "  ❌ VSCode Java Home 未配置" -ForegroundColor Red
            }

            # 检查编译器配置
            if ($settings."java.compile.nullAnalysis.mode" -eq "automatic") {
                Write-Host "  ✅ 空值分析已启用" -ForegroundColor Green
            } else {
                $issues += "建议启用Java空值分析"
                Write-Host "  ⚠️ 建议启用Java空值分析" -ForegroundColor Yellow
            }

            # 检查格式化配置
            if ($settings."editor.formatOnSave" -eq $true) {
                Write-Host "  ✅ 保存时格式化已启用" -ForegroundColor Green
            } else {
                $issues += "建议启用保存时格式化"
                Write-Host "  ⚠️ 建议启用保存时格式化" -ForegroundColor Yellow
            }

        } catch {
            $issues += "VSCode settings.json 格式错误"
            Write-Host "  ❌ VSCode settings.json 格式错误" -ForegroundColor Red
        }
    } else {
        $issues += "VSCode settings.json 文件不存在"
        Write-Host "  ⚠️ VSCode settings.json 文件不存在" -ForegroundColor Yellow
    }

    # 检查Maven配置
    Write-Host "检查Maven配置..." -ForegroundColor White

    # 检查父POM中的Lombok版本
    try {
        $parentPom = Get-Content "microservices/pom.xml" -Raw
        if ($parentPom -match 'lombok\.version>([^<]+)') {
            $lombokVersion = $matches[1]
            Write-Host "  ✅ Lombok版本: $lombokVersion" -ForegroundColor Green

            # 检查Lombok版本是否为最新稳定版
            if ([version]$lombokVersion -ge [version]"1.18.30") {
                Write-Host "  ✅ Lombok版本较新" -ForegroundColor Green
            } else {
                $issues += "建议升级Lombok到1.18.30+"
                Write-Host "  ⚠️ 建议升级Lombok到1.18.30+" -ForegroundColor Yellow
            }
        }
    } catch {
        $issues += "无法读取Maven配置"
        Write-Host "  ❌ 无法读取Maven配置" -ForegroundColor Red
    }

    # 检查编码设置
    try {
        $compilerConfig = Select-String -Path "microservices/pom.xml" -Pattern "encoding.*UTF-8"
        if ($compilerConfig) {
            Write-Host "  ✅ UTF-8编码配置正确" -ForegroundColor Green
        } else {
            $issues += "Maven编码配置可能有问题"
            Write-Host "  ⚠️ Maven编码配置可能有问题" -ForegroundColor Yellow
        }
    } catch {
        $issues += "无法检查编码配置"
        Write-Host "  ❌ 无法检查编码配置" -ForegroundColor Red
    }

    Write-Host "`n发现的问题: $($issues.Count)" -ForegroundColor $(if($issues.Count -eq 0) {"Green"} else {"Yellow"})
    if ($issues.Count -gt 0) {
        $issues | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
    }

    return $issues.Count -le 2  # 允许少量警告
}

# 3. 代码质量保障检查
function Test-CodeQuality {
    Write-Host "`n## 3. 代码质量保障检查 ##" -ForegroundColor Yellow

    # 检查是否使用了@Repository违规注解
    Write-Host "检查@Repository违规使用..." -ForegroundColor White

    try {
        $repositoryViolations = Select-String -Path "microservices/**/*.java" -Pattern "@Repository" -Exclude "target/**"
        if ($repositoryViolations) {
            Write-Host "  ❌ 发现 $($repositoryViolations.Count) 处@Repository违规" -ForegroundColor Red
            $repositoryViolations | Select-Object -First 3 | ForEach-Object {
                Write-Host "     - $($_.Path):$($_.LineNumber)" -ForegroundColor DarkRed
            }
        } else {
            Write-Host "  ✅ 未发现@Repository违规" -ForegroundColor Green
        }
    } catch {
        Write-Host "  ❌ 检查@Repository违规时出错" -ForegroundColor Red
    }

    # 检查是否使用了@Autowired违规注解
    Write-Host "检查@Autowired违规使用..." -ForegroundColor White

    try {
        $autowiredViolations = Select-String -Path "microservices/**/*.java" -Pattern "@Autowired" -Exclude "target/**"
        if ($autowiredViolations) {
            Write-Host "  ❌ 发现 $($autowiredViolations.Count) 处@Autowired违规" -ForegroundColor Red
            $autowiredViolations | Select-Object -First 3 | ForEach-Object {
                Write-Host "     - $($_.Path):$($_.LineNumber)" -ForegroundColor DarkRed
            }
        } else {
            Write-Host "  ✅ 未发现@Autowired违规" -ForegroundColor Green
        }
    } catch {
        Write-Host "  ❌ 检查@Autowired违规时出错" -ForegroundColor Red
    }

    # 检查包结构一致性
    Write-Host "检查包结构一致性..." -ForegroundColor White

    try {
        $duplicatePackages = @()
        $javaFiles = Get-ChildItem -Path "microservices/**/*.java" -Exclude "target/**"

        foreach ($file in $javaFiles) {
            $content = Get-Content $file -Raw
            if ($content -match 'package\s+([^;]+);') {
                $package = $matches[1]
                $relativePath = $file.FullName.Replace((Get-Location).Path, "").Replace("\", "/")

                # 检查包路径是否与文件路径匹配
                if ($relativePath -notlike "*$($package.Replace('.', '/'))*") {
                    $duplicatePackages += "$($file.Name): $package"
                }
            }
        }

        if ($duplicatePackages) {
            Write-Host "  ❌ 发现 $($duplicatePackages.Count) 处包路径不匹配" -ForegroundColor Red
            $duplicatePackages | Select-Object -First 3 | ForEach-Object {
                Write-Host "     - $_" -ForegroundColor DarkRed
            }
        } else {
            Write-Host "  ✅ 包结构一致性检查通过" -ForegroundColor Green
        }
    } catch {
        Write-Host "  ❌ 检查包结构时出错" -ForegroundColor Red
    }
}

# 4. 生成工程治理报告
function New-GovernanceReport {
    Write-Host "`n## 4. 生成工程治理报告 ##" -ForegroundColor Yellow

    $reportPath = "scripts/reports/p4-governance-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"
    $reportDir = Split-Path $reportPath -Parent

    if (!(Test-Path $reportDir)) {
        New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
    }

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

    $report = @"
# P4: 工程治理报告

**生成时间**: $timestamp
**执行人**: IOE-DREAM架构团队

## 执行摘要

本文档记录了IOE-DREAM微服务项目的工程治理检查结果，包括编译质量门禁、IDE配置检查和代码质量保障。

## 检查项目

### 1. 编译质量门禁
- 检查所有微服务的编译状态
- 识别编译失败的服务和原因
- 计算编译通过率

### 2. IDE配置检查
- VSCode设置验证
- Maven配置检查
- 编码设置确认

### 3. 代码质量保障
- @Repository违规检查
- @Autowired违规检查
- 包结构一致性验证

## 结果详情

详细的检查结果请参考脚本执行输出。

## 改进建议

1. 确保所有微服务都能成功编译
2. 统一IDE配置，提升开发效率
3. 持续监控代码质量，防止架构违规
4. 建立CI/CD质量门禁，自动化检查流程

---
*本报告由P4工程治理脚本自动生成*
"@

    $report | Out-File -FilePath $reportPath -Encoding UTF8
    Write-Host "报告已生成: $reportPath" -ForegroundColor Green
}

# 执行主流程
try {
    Write-Host "开始P4工程治理检查..." -ForegroundColor Cyan

    # 1. 编译质量门禁检查
    $buildGate = $true
    if (-not $SkipBuild) {
        $buildGate = Test-CompilationQualityGate
    } else {
        Write-Host "跳过编译质量门禁检查" -ForegroundColor Yellow
    }

    # 2. IDE配置检查
    $ideConfig = $true
    if (-not $SkipIdeCheck) {
        $ideConfig = Test-IdeConfiguration
    } else {
        Write-Host "跳过IDE配置检查" -ForegroundColor Yellow
    }

    # 3. 代码质量保障检查
    Test-CodeQuality

    # 4. 生成报告
    New-GovernanceReport

    # 总结
    Write-Host "`n====================================" -ForegroundColor Cyan
    Write-Host "P4: 工程治理检查完成" -ForegroundColor Cyan
    Write-Host "====================================" -ForegroundColor Cyan

    if ($buildGate) {
        Write-Host "编译质量门禁: ✅ 通过" -ForegroundColor Green
    } else {
        Write-Host "编译质量门禁: ❌ 失败" -ForegroundColor Red
    }

    if ($ideConfig) {
        Write-Host "IDE配置检查: ✅ 通过" -ForegroundColor Green
    } else {
        Write-Host "IDE配置检查: ⚠️ 需要改进" -ForegroundColor Yellow
    }

    if ($buildGate -and $ideConfig) {
        Write-Host "`n🎉 P4工程治理检查全部通过！" -ForegroundColor Green
        exit 0
    } else {
        Write-Host "`n⚠️ P4工程治理检查发现需要改进的地方，请参考上述输出" -ForegroundColor Yellow
        exit 1
    }

} catch {
    Write-Host "`n❌ P4工程治理检查执行失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "详细错误信息: $($_.Exception.StackTrace)" -ForegroundColor DarkRed
    exit 2
}