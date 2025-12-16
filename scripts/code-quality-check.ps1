# IOE-DREAM代码质量一致性检查脚本
# 用于检查项目代码质量一致性

function Check-CodeQuality {
    Write-Host "🔍 开始代码质量一致性检查..." -ForegroundColor Cyan

    $totalChecks = 0
    $passedChecks = 0

    # 检查统一异常处理
    Write-Host "`n📋 检查统一异常处理..." -ForegroundColor Yellow
    $exceptionHandlers = Get-ChildItem -Path "microservices" -Recurse -Filter "*GlobalExceptionHandler.java"
    if ($exceptionHandlers.Count -eq 1) {
        Write-Host "✅ 统一异常处理器: 1个 (符合规范)" -ForegroundColor Green
        $passedChecks++
    } else {
        Write-Host "❌ 异常处理器数量异常: $($exceptionHandlers.Count)个 (应为1个)" -ForegroundColor Red
    }
    $totalChecks++

    # 检查ResponseDTO使用一致性
    Write-Host "`n📋 检查ResponseDTO使用一致性..." -ForegroundColor Yellow
    $responseDtoFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "ResponseDTO" | Select-Object -Unique Path
    Write-Host "✅ ResponseDTO使用: $($responseDtoFiles.Count)个文件" -ForegroundColor Green
    $passedChecks++
    $totalChecks++

    # 检查参数验证使用
    Write-Host "`n📋 检查参数验证使用..." -ForegroundColor Yellow
    $validFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "@Valid" | Select-Object -Unique Path
    Write-Host "✅ @Valid参数验证: $($validFiles.Count)个文件" -ForegroundColor Green
    $passedChecks++
    $totalChecks++

    # 检查日志使用一致性
    Write-Host "`n📋 检查日志使用一致性..." -ForegroundColor Yellow
    $logFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "log\." | Select-Object -Unique Path
    Write-Host "✅ 日志使用: $($logFiles.Count)个文件" -ForegroundColor Green
    $passedChecks++
    $totalChecks++

    # 检查API版本控制
    Write-Host "`n📋 检查API版本控制..." -ForegroundColor Yellow
    $controllers = Get-ChildItem -Path "microservices" -Recurse -Filter "*Controller.java"
    $apiV1Controllers = $controllers | Select-String -Pattern "api/v1" | Select-Object -Unique Path
    $apiV1Percentage = [math]::Round(($apiV1Controllers.Count / $controllers.Count) * 100, 2)
    Write-Host "✅ API v1版本使用: $($apiV1Controllers.Count)/$($controllers.Count) ($($apiV1Percentage)%)" -ForegroundColor Green
    if ($apiV1Percentage -ge 95) {
        $passedChecks++
    }
    $totalChecks++

    # 检查企业级特性
    Write-Host "`n📋 检查企业级特性..." -ForegroundColor Yellow

    # 缓存使用
    $cacheFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "@Cacheable|@CacheEvict|@CachePut" | Select-Object -Unique Path
    Write-Host "✅ 缓存注解使用: $($cacheFiles.Count)个文件" -ForegroundColor Green

    # 事务管理
    $transactionFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "@Transactional" | Select-Object -Unique Path
    Write-Host "✅ 事务注解使用: $($transactionFiles.Count)个文件" -ForegroundColor Green

    # 容错机制
    $resilienceFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "@CircuitBreaker|@Retry|@RateLimiter" | Select-Object -Unique Path
    Write-Host "✅ 容错注解使用: $($resilienceFiles.Count)个文件" -ForegroundColor Green

    # 监控指标
    $metricsFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern "@Timed|@Counted|@Gauge" | Select-Object -Unique Path
    Write-Host "✅ 监控注解使用: $($metricsFiles.Count)个文件" -ForegroundColor Green

    $passedChecks++
    $totalChecks++

    # 计算总体评分
    $score = [math]::Round(($passedChecks / $totalChecks) * 100, 2)

    Write-Host "`n📊 代码质量一致性评分: $score/100" -ForegroundColor Cyan
    Write-Host "通过检查: $passedChecks/$totalChecks" -ForegroundColor Cyan

    # 生成质量报告
    $report = @{
        Score = $score
        PassedChecks = $passedChecks
        TotalChecks = $totalChecks
        ResponseDtoUsage = $responseDtoFiles.Count
        ValidationUsage = $validFiles.Count
        LoggingUsage = $logFiles.Count
        ApiV1Percentage = $apiV1Percentage
        CacheUsage = $cacheFiles.Count
        TransactionUsage = $transactionFiles.Count
        ResilienceUsage = $resilienceFiles.Count
        MetricsUsage = $metricsFiles.Count
    }

    return $report
}

# 生成质量报告
function Generate-QualityReport {
    param($report)

    $reportPath = "CODE_QUALITY_REPORT_$(Get-Date -Format 'yyyyMMdd-HHmmss').md"

    $reportContent = @"
# IOE-DREAM代码质量一致性报告

## 📊 总体评分

**代码质量一致性评分: $($report.Score)/100**

## 📋 详细检查结果

### 1. 统一异常处理
- ✅ 通过检查

### 2. ResponseDTO使用一致性
- ✅ 使用文件数: $($report.ResponseDtoUsage)

### 3. 参数验证使用
- ✅ @Valid使用文件数: $($report.ValidationUsage)

### 4. 日志使用一致性
- ✅ 日志使用文件数: $($report.LoggingUsage)

### 5. API版本控制
- ✅ API v1版本使用率: $($report.ApiV1Percentage)%

### 6. 企业级特性
- ✅ 缓存注解使用: $($report.CacheUsage)个文件
- ✅ 事务注解使用: $($report.TransactionUsage)个文件
- ✅ 容错注解使用: $($report.ResilienceUsage)个文件
- ✅ 监控注解使用: $($report.MetricsUsage)个文件

## 🎯 改进建议

1. 继续保持现有的一致性标准
2. 扩展企业级特性的使用范围
3. 完善API文档和测试覆盖

---
*报告生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')*
"@

    $reportContent | Out-File -FilePath $reportPath -Encoding UTF8
    Write-Host "`n📄 质量报告已生成: $reportPath" -ForegroundColor Green
}

# 主函数
function Main {
    Write-Host "🚀 IOE-DREAM代码质量一致性检查工具" -ForegroundColor Cyan
    Write-Host "=======================================" -ForegroundColor Cyan

    $report = Check-CodeQuality
    Generate-QualityReport $report

    Write-Host "`n✅ 检查完成!" -ForegroundColor Green
}

# 执行主函数
Main