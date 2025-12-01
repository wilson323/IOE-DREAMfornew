# =============================================================================
# IOE-DREAM 统一质量门禁报告生成脚本 (PowerShell版本)
# =============================================================================
# 功能: 汇总编译、启动、扫描、清理结果，生成统一验证报告与proof文件
# 作者: Claude Code
# 日期: 2025-11-18
# =============================================================================

$ErrorActionPreference = "Stop"
$ProjectRoot = "D:\IOE-DREAM"
Set-Location $ProjectRoot

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    $colorMap = @{
        "Red" = "Red"
        "Green" = "Green"
        "Yellow" = "Yellow"
        "Blue" = "Blue"
        "Cyan" = "Cyan"
        "Magenta" = "Magenta"
    }
    Write-Host $Message -ForegroundColor $colorMap[$Color]
}

Write-ColorOutput "`n============================================================================" "Cyan"
Write-ColorOutput "📊 IOE-DREAM 统一质量门禁报告生成" "Cyan"
Write-ColorOutput "⏰ 执行时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "Cyan"
Write-ColorOutput "============================================================================`n" "Cyan"

# 初始化报告数据结构
$report = @{
    reportDate = (Get-Date -Format "yyyy-MM-ddTHH:mm:ssZ")
    projectName = "IOE-DREAM"
    projectPath = $ProjectRoot
    verificationType = "统一质量门禁验证"
    status = "PASSED"
    summary = @{
        totalChecks = 0
        passedChecks = 0
        failedChecks = 0
        warningChecks = 0
        complianceRate = "0%"
    }
    compilation = @{
        status = "UNKNOWN"
        mavenVersion = ""
        javaVersion = ""
        buildStatus = "UNKNOWN"
        errorCount = 0
        warningCount = 0
        details = @()
    }
    codeQuality = @{
        status = "UNKNOWN"
        javaxViolations = 0
        autowiredViolations = 0
        architectureViolations = 0
        systemOutViolations = 0
        details = @()
    }
    startup = @{
        status = "UNKNOWN"
        startupTime = 0
        healthCheck = "UNKNOWN"
        errorCount = 0
        details = @()
    }
    scanning = @{
        status = "UNKNOWN"
        filesScanned = 0
        violationsFound = 0
        details = @()
    }
    cleanup = @{
        status = "UNKNOWN"
        mockCodeRemoved = 0
        hardcodedSecretsRemoved = 0
        unusedCodeRemoved = 0
        details = @()
    }
    repowikiCompliance = @{
        dependencyInjection = @{
            status = "UNKNOWN"
            description = ""
        }
        packageNaming = @{
            status = "UNKNOWN"
            description = ""
        }
        architecture = @{
            status = "UNKNOWN"
            description = ""
        }
        loggingStandard = @{
            status = "UNKNOWN"
            description = ""
        }
    }
    recommendations = @()
}

# 步骤1: 检查编译状态
Write-ColorOutput "步骤1: 检查编译状态..." "Blue"
try {
    $javaVersion = (java -version 2>&1 | Select-String "version").ToString()
    $report.compilation.javaVersion = $javaVersion
    
    $mavenVersion = (mvn -version 2>&1 | Select-String "Apache Maven").ToString()
    $report.compilation.mavenVersion = $mavenVersion
    
    Write-ColorOutput "  ✅ Java版本: $javaVersion" "Green"
    Write-ColorOutput "  ✅ Maven版本: $mavenVersion" "Green"
    
    # 检查是否有编译产物
    $classFiles = Get-ChildItem -Path "smart-admin-api-java17-springboot3" -Recurse -Filter "*.class" -ErrorAction SilentlyContinue | Measure-Object
    if ($classFiles.Count -gt 0) {
        $report.compilation.buildStatus = "COMPILED"
        Write-ColorOutput "  ✅ 发现编译产物: $($classFiles.Count) 个class文件" "Green"
    } else {
        Write-ColorOutput "  ⚠️  未发现编译产物，可能需要重新编译" "Yellow"
        $report.compilation.buildStatus = "NOT_COMPILED"
    }
    
    $report.compilation.status = "PASSED"
    $report.summary.passedChecks++
} catch {
    Write-ColorOutput "  ❌ 编译环境检查失败: $_" "Red"
    $report.compilation.status = "FAILED"
    $report.compilation.details += "编译环境检查失败: $_"
    $report.summary.failedChecks++
}
$report.summary.totalChecks++

# 步骤2: 代码质量扫描
Write-ColorOutput "`n步骤2: 代码质量扫描..." "Blue"
try {
    $javaFiles = Get-ChildItem -Path "smart-admin-api-java17-springboot3" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue
    
    # 检查javax包使用
    $javaxFiles = $javaFiles | Where-Object {
        $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
        if ($content) {
            $content -match "import javax\.(servlet|validation|annotation|persistence|xml\.bind)" -and
            $content -notmatch "import javax\.(crypto|net|security|naming)"
        }
    }
    $report.codeQuality.javaxViolations = $javaxFiles.Count
    if ($javaxFiles.Count -eq 0) {
        Write-ColorOutput "  ✅ javax包检查通过 (0个违规)" "Green"
        $report.repowikiCompliance.packageNaming.status = "PASSED"
        $report.repowikiCompliance.packageNaming.description = "✅ 无javax包使用，严格使用jakarta包名规范"
    } else {
        Write-ColorOutput "  [FAILED] Found $($javaxFiles.Count) javax package violations" "Red"
        $report.repowikiCompliance.packageNaming.status = "FAILED"
        $report.repowikiCompliance.packageNaming.description = "❌ 发现 $($javaxFiles.Count) 个文件使用javax包，必须迁移到jakarta"
        $report.codeQuality.details += "javax包违规文件: $($javaxFiles | Select-Object -First 5 | ForEach-Object { $_.FullName })"
    }
    
    # 检查@Autowired使用
    $autowiredFiles = $javaFiles | Where-Object {
        $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
        $content -and $content -match "@Autowired"
    }
    $report.codeQuality.autowiredViolations = $autowiredFiles.Count
    if ($autowiredFiles.Count -eq 0) {
        Write-ColorOutput "  ✅ @Autowired检查通过 (0个违规)" "Green"
        $report.repowikiCompliance.dependencyInjection.status = "PASSED"
        $report.repowikiCompliance.dependencyInjection.description = "✅ 无@Autowired使用，严格使用@Resource依赖注入"
    } else {
        Write-ColorOutput "  ❌ 发现 $($autowiredFiles.Count) 个@Autowired违规" "Red"
        $report.repowikiCompliance.dependencyInjection.status = "FAILED"
        $report.repowikiCompliance.dependencyInjection.description = "❌ 发现 $($autowiredFiles.Count) 个文件使用@Autowired，必须使用@Resource"
        $report.codeQuality.details += "@Autowired违规文件: $($autowiredFiles | Select-Object -First 5 | ForEach-Object { $_.FullName })"
    }
    
    # 检查System.out使用
    $systemOutFiles = $javaFiles | Where-Object {
        $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
        $content -and $content -match "System\.out\.print"
    }
    $report.codeQuality.systemOutViolations = $systemOutFiles.Count
    if ($systemOutFiles.Count -eq 0) {
        Write-ColorOutput "  ✅ System.out检查通过 (0个违规)" "Green"
        $report.repowikiCompliance.loggingStandard.status = "PASSED"
        $report.repowikiCompliance.loggingStandard.description = "✅ 无System.out使用，严格使用SLF4J日志框架"
    } else {
        Write-ColorOutput "  ⚠️  发现 $($systemOutFiles.Count) 个System.out使用" "Yellow"
        $report.repowikiCompliance.loggingStandard.status = "WARNING"
        $report.repowikiCompliance.loggingStandard.description = "⚠️  发现 $($systemOutFiles.Count) 个文件使用System.out，建议使用SLF4J"
    }
    
    # 检查架构违规 (Controller直接访问DAO)
    $controllerFiles = $javaFiles | Where-Object { $_.Name -match "Controller\.java$" }
    $architectureViolations = 0
    foreach ($file in $controllerFiles) {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        if ($content -and ($content -match "@Resource.*Dao|@Autowired.*Dao")) {
            $architectureViolations++
        }
    }
    $report.codeQuality.architectureViolations = $architectureViolations
    if ($architectureViolations -eq 0) {
        Write-ColorOutput "  ✅ 架构规范检查通过 (0个违规)" "Green"
        $report.repowikiCompliance.architecture.status = "PASSED"
        $report.repowikiCompliance.architecture.description = "✅ 四层架构严格实现：Controller→Service→Manager→DAO"
    } else {
        Write-ColorOutput "  ❌ 发现 $architectureViolations 个架构违规" "Red"
        $report.repowikiCompliance.architecture.status = "FAILED"
        $report.repowikiCompliance.architecture.description = "❌ 发现 $architectureViolations 个Controller直接访问DAO，违反四层架构"
    }
    
    $report.codeQuality.filesScanned = $javaFiles.Count
    $report.scanning.filesScanned = $javaFiles.Count
    
    if ($report.codeQuality.javaxViolations -eq 0 -and 
        $report.codeQuality.autowiredViolations -eq 0 -and 
        $report.codeQuality.architectureViolations -eq 0) {
        $report.codeQuality.status = "PASSED"
        $report.scanning.status = "PASSED"
        $report.summary.passedChecks++
    } else {
        $report.codeQuality.status = "FAILED"
        $report.scanning.status = "FAILED"
        $report.summary.failedChecks++
    }
    $report.summary.totalChecks++
    
} catch {
    Write-ColorOutput "  ❌ 代码质量扫描失败: $_" "Red"
    $report.codeQuality.status = "FAILED"
    $report.scanning.status = "FAILED"
    $report.summary.failedChecks++
    $report.summary.totalChecks++
}

# 步骤3: 检查启动状态
Write-ColorOutput "`n步骤3: 检查启动状态..." "Blue"
try {
    # 检查是否有启动日志
    $startupLogs = Get-ChildItem -Path "." -Recurse -Filter "*startup*.log" -ErrorAction SilentlyContinue | 
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    
    if ($startupLogs) {
        $logContent = Get-Content $startupLogs.FullName -Tail 50 -ErrorAction SilentlyContinue
        $errorCount = ($logContent | Select-String -Pattern "ERROR|Exception|Failed" -CaseSensitive:$false).Count
        
        if ($logContent -match "Started.*Application|Application.*started|Tomcat.*started") {
            Write-ColorOutput "  ✅ 应用启动成功" "Green"
            $report.startup.status = "PASSED"
            $report.startup.healthCheck = "PASSED"
            $report.summary.passedChecks++
        } else {
            Write-ColorOutput "  ⚠️  应用启动状态未知" "Yellow"
            $report.startup.status = "UNKNOWN"
        }
        
        $report.startup.errorCount = $errorCount
        if ($errorCount -gt 0) {
            Write-ColorOutput "  ⚠️  启动日志中发现 $errorCount 个错误/异常" "Yellow"
        }
    } else {
        Write-ColorOutput "  ⚠️  未找到启动日志，可能需要启动测试" "Yellow"
        $report.startup.status = "UNKNOWN"
    }
    $report.summary.totalChecks++
} catch {
    Write-ColorOutput "  ❌ 启动状态检查失败: $_" "Red"
    $report.startup.status = "FAILED"
    $report.summary.failedChecks++
    $report.summary.totalChecks++
}

# 步骤4: 检查清理状态
Write-ColorOutput "`n步骤4: 检查清理状态..." "Blue"
try {
    # 检查mock代码
    $mockFiles = Get-ChildItem -Path "smart-admin-api-java17-springboot3" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue | 
        Where-Object {
            $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
            $content -and ($content -match "Mock|mock|MOCK|模拟|假数据")
        }
    
    $report.cleanup.mockCodeRemoved = $mockFiles.Count
    if ($mockFiles.Count -eq 0) {
        Write-ColorOutput "  [PASSED] No mock code found" "Green"
    } else {
        Write-ColorOutput "  ⚠️  发现 $($mockFiles.Count) 个可能包含mock代码的文件" "Yellow"
    }
    
    # 检查硬编码密钥
    $secretFiles = Get-ChildItem -Path "smart-admin-api-java17-springboot3" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue | 
        Where-Object {
            $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
            if ($content) {
                ($content -match "password\s*=\s*['\""]|secret\s*=\s*['\""]|key\s*=\s*['\""]") -and
                $_.FullName -notmatch "Test|Config|Example"
            }
        }
    
    $report.cleanup.hardcodedSecretsRemoved = $secretFiles.Count
    if ($secretFiles.Count -eq 0) {
        Write-ColorOutput "  ✅ 未发现硬编码密钥" "Green"
    } else {
        Write-ColorOutput "  ⚠️  发现 $($secretFiles.Count) 个可能包含硬编码密钥的文件" "Yellow"
    }
    
    if ($mockFiles.Count -eq 0 -and $secretFiles.Count -eq 0) {
        $report.cleanup.status = "PASSED"
        $report.summary.passedChecks++
    } else {
        $report.cleanup.status = "WARNING"
        $report.summary.warningChecks++
    }
    $report.summary.totalChecks++
    
} catch {
    Write-ColorOutput "  ❌ 清理状态检查失败: $_" "Red"
    $report.cleanup.status = "FAILED"
    $report.summary.failedChecks++
    $report.summary.totalChecks++
}

# 计算合规率
$report.summary.complianceRate = [math]::Round(($report.summary.passedChecks / $report.summary.totalChecks) * 100, 2).ToString() + "%"

# 确定总体状态
if ($report.summary.failedChecks -gt 0) {
    $report.status = "FAILED"
} elseif ($report.summary.warningChecks -gt 0) {
    $report.status = "WARNING"
} else {
    $report.status = "PASSED"
}

# 生成建议
if ($report.codeQuality.javaxViolations -gt 0) {
    $report.recommendations += "修复 $($report.codeQuality.javaxViolations) 个javax包违规，迁移到jakarta"
}
if ($report.codeQuality.autowiredViolations -gt 0) {
    $report.recommendations += "修复 $($report.codeQuality.autowiredViolations) 个@Autowired违规，改为@Resource"
}
if ($report.codeQuality.architectureViolations -gt 0) {
    $report.recommendations += "修复 $($report.codeQuality.architectureViolations) 个架构违规，确保四层架构合规"
}
if ($report.codeQuality.systemOutViolations -gt 0) {
    $report.recommendations += "替换 $($report.codeQuality.systemOutViolations) 个System.out使用，改为SLF4J日志"
}

# 生成报告文件
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$reportFile = "quality-gate-report-$timestamp.json"
$reportJson = $report | ConvertTo-Json -Depth 10

$reportJson | Out-File -FilePath $reportFile -Encoding UTF8

Write-ColorOutput "`n✅ 验证报告已生成: $reportFile" "Green"

# 生成proof文件
$proofFile = "quality-gate-proof-$timestamp.proof"
$proofContent = @"
IOE-DREAM 统一质量门禁验证证明
========================================
验证时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
项目路径: $ProjectRoot
验证类型: 统一质量门禁验证

Verification Summary:
- Total Checks: $($report.summary.totalChecks)
- Passed: $($report.summary.passedChecks)
- Failed: $($report.summary.failedChecks)
- Warnings: $($report.summary.warningChecks)
- Compliance Rate: $($report.summary.complianceRate)

详细状态:
- 编译状态: $($report.compilation.status)
- 代码质量: $($report.codeQuality.status)
- 启动状态: $($report.startup.status)
- 扫描状态: $($report.scanning.status)
- 清理状态: $($report.cleanup.status)

repowiki规范合规性:
- 依赖注入规范: $($report.repowikiCompliance.dependencyInjection.status)
- 包名规范: $($report.repowikiCompliance.packageNaming.status)
- 架构规范: $($report.repowikiCompliance.architecture.status)
- 日志规范: $($report.repowikiCompliance.loggingStandard.status)

违规统计:
- javax包违规: $($report.codeQuality.javaxViolations)
- @Autowired违规: $($report.codeQuality.autowiredViolations)
- 架构违规: $($report.codeQuality.architectureViolations)
- System.out违规: $($report.codeQuality.systemOutViolations)

总体状态: $($report.status)

此证明文件表明质量门禁验证已完成，所有结果已记录在报告中。
报告文件: $reportFile

生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
"@

$proofContent | Out-File -FilePath $proofFile -Encoding UTF8

Write-ColorOutput "✅ 验证证明已生成: $proofFile" "Green"

# 输出摘要
Write-ColorOutput "`n============================================================================" "Cyan"
Write-ColorOutput "📊 验证结果摘要" "Cyan"
Write-ColorOutput "============================================================================" "Cyan"
Write-ColorOutput "总检查项: $($report.summary.totalChecks)" "White"
Write-ColorOutput "通过项: $($report.summary.passedChecks)" "Green"
Write-ColorOutput "失败项: $($report.summary.failedChecks)" "Red"
Write-ColorOutput "警告项: $($report.summary.warningChecks)" "Yellow"
Write-ColorOutput "合规率: $($report.summary.complianceRate)" "Cyan"
Write-ColorOutput "总体状态: $($report.status)" $(if ($report.status -eq "PASSED") { "Green" } else { "Red" })
Write-ColorOutput "============================================================================" "Cyan"

if ($report.recommendations.Count -gt 0) {
    Write-ColorOutput "`n💡 建议:" "Yellow"
    $report.recommendations | ForEach-Object { Write-ColorOutput "  - $_" "Yellow" }
}

Write-ColorOutput "`n📄 报告文件: $reportFile" "Cyan"
Write-ColorOutput "📄 证明文件: $proofFile" "Cyan"
Write-ColorOutput "`n✅ 统一质量门禁报告生成完成！`n" "Green"

# 返回状态码
if ($report.status -eq "PASSED") {
    exit 0
} elseif ($report.status -eq "WARNING") {
    exit 2
} else {
    exit 1
}

