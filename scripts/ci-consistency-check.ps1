# IOE-DREAM持续集成一致性检查脚本
# 用于CI/CD流水线中的代码一致性检查

param(
    [switch]$Strict,
    [string]$OutputFormat = "console"
)

# 错误计数器
$script:ErrorCount = 0
$script:WarningCount = 0

function Write-Error-Message {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
    $script:ErrorCount++
}

function Write-Warning-Message {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
    $script:WarningCount++
}

function Write-Success-Message {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Info-Message {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Cyan
}

# 架构合规性检查
function Test-ArchitectureCompliance {
    Write-Info-Message "架构合规性检查开始..."

    # 检查@Autowired使用
    $autowiredFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "@Autowired" | Select-Object -Unique Path
    if ($autowiredFiles.Count -gt 0) {
        Write-Error-Message "发现 @Autowired 违规: $($autowiredFiles.Count) 个文件"
        if ($Strict) {
            $autowiredFiles | ForEach-Object { Write-Host "  - $($_.Path)" -ForegroundColor Gray }
        }
        if ($script:ErrorCount -gt 0) { return $false }
    } else {
        Write-Success-Message "@Autowired 检查通过"
    }

    # 检查@Repository使用
    $repositoryFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "@Repository" | Select-Object -Unique Path
    if ($repositoryFiles.Count -gt 0) {
        Write-Error-Message "发现 @Repository 违规: $($repositoryFiles.Count) 个文件"
        if ($Strict) {
            $repositoryFiles | ForEach-Object { Write-Host "  - $($_.Path)" -ForegroundColor Gray }
        }
        if ($script:ErrorCount -gt 0) { return $false }
    } else {
        Write-Success-Message "@Repository 检查通过"
    }

    # 检查javax包名使用
    $javaxFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "javax\." | Select-Object -Unique Path
    if ($javaxFiles.Count -gt 0) {
        Write-Error-Message "发现 javax 包名违规: $($javaxFiles.Count) 个文件"
        if ($Strict) {
            $javaxFiles | ForEach-Object { Write-Host "  - $($_.Path)" -ForegroundColor Gray }
        }
        if ($script:ErrorCount -gt 0) { return $false }
    } else {
        Write-Success-Message "javax 包名检查通过"
    }

    # 检查DAO命名规范
    $daoFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Dao.java"
    $repositoryNamedFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Repository.java"
    if ($repositoryNamedFiles.Count -gt 0) {
        Write-Error-Message "发现 Repository 命名违规: $($repositoryNamedFiles.Count) 个文件"
        if ($script:ErrorCount -gt 0) { return $false }
    } else {
        Write-Success-Message "DAO 命名规范检查通过"
    }

    return $true
}

# 代码质量检查
function Test-CodeQuality {
    Write-Info-Message "代码质量检查开始..."

    # 检查全局异常处理器
    $exceptionHandlers = Get-ChildItem -Path "microservices" -Recurse -Filter "*GlobalExceptionHandler.java"
    if ($exceptionHandlers.Count -ne 1) {
        Write-Error-Message "全局异常处理器数量异常: $($exceptionHandlers.Count) (应为1)"
        if ($script:ErrorCount -gt 0) { return $false }
    } else {
        Write-Success-Message "全局异常处理器检查通过"
    }

    # 检查ResponseDTO使用
    $controllers = Get-ChildItem -Path "microservices" -Recurse -Filter "*Controller.java"
    $responseDtoIssues = 0
    foreach ($controller in $controllers) {
        $content = Get-Content $controller.FullName -Raw
        if ($content -match "@RestController" -and $content -notmatch "ResponseDTO") {
            $responseDtoIssues++
        }
    }
    if ($responseDtoIssues -gt 0) {
        Write-Error-Message "发现 $responseDtoIssues 个控制器未使用ResponseDTO"
        if ($script:ErrorCount -gt 0) { return $false }
    } else {
        Write-Success-Message "ResponseDTO使用检查通过"
    }

    # 检查参数验证
    $controllersWithoutValidation = 0
    foreach ($controller in $controllers) {
        $content = Get-Content $controller.FullName -Raw
        if ($content -match "@RequestBody" -and $content -notmatch "@Valid") {
            $controllersWithoutValidation++
        }
    }
    if ($controllersWithoutValidation -gt 0) {
        Write-Warning-Message "发现 $controllersWithoutValidation 个控制器缺少参数验证"
    } else {
        Write-Success-Message "参数验证检查通过"
    }

    return $true
}

# API一致性检查
function Test-ApiConsistency {
    Write-Info-Message "API一致性检查开始..."

    # 检查API版本控制
    $apiControllers = $controllers | Select-String -Pattern "@RestController"
    $apiV1Controllers = $apiControllers | Select-String -Pattern "api/v1"
    $apiV1Percentage = if ($apiControllers.Count -gt 0) { [math]::Round(($apiV1Controllers.Count / $apiControllers.Count) * 100, 2) } else { 0 }

    if ($apiV1Percentage -lt 95) {
        Write-Warning-Message "API v1版本使用率: $apiV1Percentage% (建议≥95%)"
    } else {
        Write-Success-Message "API版本控制检查通过: $apiV1Percentage%"
    }

    # 检查RESTful设计
    $nonRestfulApis = 0
    foreach ($controller in $controllers) {
        $content = Get-Content $controller.FullName -Raw
        if ($content -match "@PostMapping.*get|@GetMapping.*create|@PutMapping.*delete") {
            $nonRestfulApis++
        }
    }
    if ($nonRestfulApis -gt 0) {
        Write-Warning-Message "发现 $nonRestfulApis 个可能的RESTful设计问题"
    } else {
        Write-Success-Message "RESTful设计检查通过"
    }

    return $true
}

# 企业级特性检查
function Test-EnterpriseFeatures {
    Write-Info-Message "企业级特性检查开始..."

    # 检查缓存使用
    $cacheFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "@Cacheable|@CacheEvict|@CachePut" | Select-Object -Unique Path
    if ($cacheFiles.Count -eq 0) {
        Write-Warning-Message "未发现缓存使用，建议实施缓存策略"
    } else {
        Write-Success-Message "缓存使用检查通过: $($cacheFiles.Count) 个文件"
    }

    # 检查事务管理
    $transactionFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Service*.java" | Select-String -Pattern "@Transactional" | Select-Object -Unique Path
    $serviceFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Service*.java"
    $transactionPercentage = if ($serviceFiles.Count -gt 0) { [math]::Round(($transactionFiles.Count / $serviceFiles.Count) * 100, 2) } else { 0 }

    if ($transactionPercentage -lt 50) {
        Write-Warning-Message "事务管理使用率: $transactionPercentage% (建议≥50%)"
    } else {
        Write-Success-Message "事务管理检查通过: $transactionPercentage%"
    }

    # 检查容错机制
    $resilienceFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "@CircuitBreaker|@Retry|@RateLimiter" | Select-Object -Unique Path
    if ($resilienceFiles.Count -eq 0) {
        Write-Warning-Message "未发现容错机制使用，建议实施熔断和重试策略"
    } else {
        Write-Success-Message "容错机制检查通过: $($resilienceFiles.Count) 个文件"
    }

    return $true
}

# 生成检查报告
function New-ConsistencyReport {
    param(
        [bool]$ArchitecturePassed,
        [bool]$CodeQualityPassed,
        [bool]$ApiConsistencyPassed,
        [bool]$EnterpriseFeaturesPassed
    )

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $allPassed = $ArchitecturePassed -and $CodeQualityPassed -and $ApiConsistencyPassed -and $EnterpriseFeaturesPassed

    if ($OutputFormat -eq "json") {
        $report = @{
            timestamp = $timestamp
            passed = $allPassed
            errorCount = $script:ErrorCount
            warningCount = $script:WarningCount
            checks = @{
                architecture = $ArchitecturePassed
                codeQuality = $CodeQualityPassed
                apiConsistency = $ApiConsistencyPassed
                enterpriseFeatures = $EnterpriseFeaturesPassed
            }
        }
        return $report | ConvertTo-Json -Depth 3
    } else {
        $report = @"
# IOE-DREAM代码一致性检查报告

**检查时间**: $timestamp
**总体结果**: $(if ($allPassed) { "✅ 通过" } else { "❌ 失败" })
**错误数量**: $script:ErrorCount
**警告数量**: $script:WarningCount

## 检查结果

- 架构合规性: $(if ($ArchitecturePassed) { "✅ 通过" } else { "❌ 失败" })
- 代码质量: $(if ($CodeQualityPassed) { "✅ 通过" } else { "❌ 失败" })
- API一致性: $(if ($ApiConsistencyPassed) { "✅ 通过" } else { "❌ 失败" })
- 企业级特性: $(if ($EnterpriseFeaturesPassed) { "✅ 通过" } else { "❌ 失败" })
"@
        return $report
    }
}

# 主函数
function Main {
    Write-Host "🚀 IOE-DREAM持续集成一致性检查" -ForegroundColor Cyan
    Write-Host "===============================" -ForegroundColor Cyan

    if ($Strict) {
        Write-Warning-Message "严格模式已启用，将显示详细错误信息"
    }

    $architecturePassed = Test-ArchitectureCompliance
    $codeQualityPassed = Test-CodeQuality
    $apiConsistencyPassed = Test-ApiConsistency
    $enterpriseFeaturesPassed = Test-EnterpriseFeatures

    $report = New-ConsistencyReport $architecturePassed $codeQualityPassed $apiConsistencyPassed $enterpriseFeaturesPassed

    Write-Host "`n📊 检查报告:" -ForegroundColor Cyan
    Write-Host $report

    # 设置退出码
    if ($script:ErrorCount -gt 0) {
        Write-Host "`n❌ 检查失败，发现 $script:ErrorCount 个错误" -ForegroundColor Red
        exit 1
    } elseif ($script:WarningCount -gt 0) {
        Write-Host "`n⚠️ 检查通过，但有 $script:WarningCount 个警告" -ForegroundColor Yellow
        exit 0
    } else {
        Write-Host "`n✅ 所有检查通过" -ForegroundColor Green
        exit 0
    }
}

# 执行主函数
Main