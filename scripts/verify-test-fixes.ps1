# =====================================================
# 测试修复验证脚本
# 版本: v1.0.0
# 描述: 验证所有测试错误修复是否成功
# 创建时间: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$RunTests = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport = $false
)

$ErrorActionPreference = "Stop"

# 颜色输出函数
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  测试修复验证脚本" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 切换到microservices-common目录
$commonPath = "microservices\microservices-common"
if (-not (Test-Path $commonPath)) {
    Write-Host "[ERROR] 未找到microservices-common目录" -ForegroundColor Red
    exit 1
}

Push-Location $commonPath

try {
    # 1. 检查修复的文件是否存在
    Write-Host "[1/5] 检查修复的文件..." -ForegroundColor Cyan
    
    $fixedFiles = @(
        "src\test\java\net\lab1024\sa\common\visitor\dao\VehicleDaoTest.java",
        "src\test\java\net\lab1024\sa\common\organization\manager\AreaDeviceManagerTest.java",
        "src\test\java\net\lab1024\sa\common\workflow\manager\WorkflowManagerTest.java",
        "src\test\java\net\lab1024\sa\common\auth\manager\AuthManagerTest.java",
        "src\test\java\net\lab1024\sa\common\workflow\manager\ExpressionEngineManagerTest.java",
        "src\test\java\net\lab1024\sa\common\monitor\manager\HealthCheckManagerTest.java",
        "src\test\java\net\lab1024\sa\common\organization\manager\AreaManagerTest.java",
        "src\main\java\net\lab1024\sa\common\organization\manager\AreaDeviceManager.java",
        "src\main\java\net\lab1024\sa\common\workflow\manager\WorkflowManager.java"
    )
    
    $allFilesExist = $true
    foreach ($file in $fixedFiles) {
        if (Test-Path $file) {
            Write-Host "  [OK] $file" -ForegroundColor Green
        } else {
            Write-Host "  [WARN] $file 不存在" -ForegroundColor Yellow
            $allFilesExist = $false
        }
    }
    
    if ($allFilesExist) {
        Write-Host "`n[OK] 所有修复文件都存在" -ForegroundColor Green
    } else {
        Write-Host "`n[WARN] 部分修复文件缺失" -ForegroundColor Yellow
    }
    
    # 2. 检查关键修复点
    Write-Host "`n[2/5] 检查关键修复点..." -ForegroundColor Cyan
    
    # 检查VehicleDaoTest的@AutoConfigureTestDatabase注解
    $vehicleTestContent = Get-Content "src\test\java\net\lab1024\sa\common\visitor\dao\VehicleDaoTest.java" -Raw
    if ($vehicleTestContent -match "@AutoConfigureTestDatabase") {
        Write-Host "  [OK] VehicleDaoTest已添加@AutoConfigureTestDatabase" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] VehicleDaoTest缺少@AutoConfigureTestDatabase注解" -ForegroundColor Red
    }
    
    # 检查AuthManagerTest的@MockitoSettings注解
    $authTestContent = Get-Content "src\test\java\net\lab1024\sa\common\auth\manager\AuthManagerTest.java" -Raw
    if ($authTestContent -match "@MockitoSettings.*LENIENT") {
        Write-Host "  [OK] AuthManagerTest已添加@MockitoSettings(strictness = Strictness.LENIENT)" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] AuthManagerTest缺少@MockitoSettings注解" -ForegroundColor Red
    }
    
    # 检查AreaDeviceManager的selectByDeviceCode修复
    $areaDeviceManagerContent = Get-Content "src\main\java\net\lab1024\sa\common\organization\manager\AreaDeviceManager.java" -Raw
    if ($areaDeviceManagerContent -match "selectByDeviceCode") {
        Write-Host "  [OK] AreaDeviceManager已使用selectByDeviceCode" -ForegroundColor Green
    } else {
        Write-Host "  [WARN] AreaDeviceManager可能未使用selectByDeviceCode" -ForegroundColor Yellow
    }
    
    # 检查WorkflowManager的globalConfig null检查
    $workflowManagerContent = Get-Content "src\main\java\net\lab1024\sa\common\workflow\manager\WorkflowManager.java" -Raw
    if ($workflowManagerContent -match "globalConfig.*!=.*null") {
        Write-Host "  [OK] WorkflowManager已添加globalConfig null检查" -ForegroundColor Green
    } else {
        Write-Host "  [WARN] WorkflowManager可能缺少globalConfig null检查" -ForegroundColor Yellow
    }
    
    # 3. 编译检查
    Write-Host "`n[3/5] 编译检查..." -ForegroundColor Cyan
    
    Write-Host "  正在编译microservices-common..." -ForegroundColor White
    $compileResult = mvn clean compile -DskipTests 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] 编译成功" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] 编译失败" -ForegroundColor Red
        Write-Host "  编译输出:" -ForegroundColor Yellow
        $compileResult | Select-String -Pattern "ERROR" | Select-Object -First 10
    }
    
    # 4. 运行测试（如果指定）
    if ($RunTests) {
        Write-Host "`n[4/5] 运行测试..." -ForegroundColor Cyan
        
        # 运行修复的测试类
        $testClasses = @(
            "net.lab1024.sa.common.visitor.dao.VehicleDaoTest",
            "net.lab1024.sa.common.organization.manager.AreaDeviceManagerTest",
            "net.lab1024.sa.common.workflow.manager.WorkflowManagerTest",
            "net.lab1024.sa.common.auth.manager.AuthManagerTest",
            "net.lab1024.sa.common.workflow.manager.ExpressionEngineManagerTest",
            "net.lab1024.sa.common.monitor.manager.HealthCheckManagerTest",
            "net.lab1024.sa.common.organization.manager.AreaManagerTest"
        )
        
        $totalTests = 0
        $passedTests = 0
        $failedTests = 0
        
        foreach ($testClass in $testClasses) {
            Write-Host "  运行测试: $testClass" -ForegroundColor White
            $testResult = mvn test -Dtest=$testClass 2>&1
            
            $testOutput = $testResult | Select-String -Pattern "Tests run:|FAILURE|ERROR" | Select-Object -Last 5
            Write-Host "  测试结果:" -ForegroundColor White
            $testOutput | ForEach-Object { Write-Host "    $_" -ForegroundColor White }
            
            if ($testResult -match "Tests run:.*Failures: 0.*Errors: 0") {
                $passedTests++
                Write-Host "  [OK] $testClass 通过" -ForegroundColor Green
            } else {
                $failedTests++
                Write-Host "  [ERROR] $testClass 失败" -ForegroundColor Red
            }
            $totalTests++
        }
        
        Write-Host "`n测试统计:" -ForegroundColor Cyan
        Write-Host "  总计: $totalTests" -ForegroundColor White
        Write-Host "  通过: $passedTests" -ForegroundColor Green
        Write-Host "  失败: $failedTests" -ForegroundColor $(if ($failedTests -eq 0) { "Green" } else { "Red" })
    } else {
        Write-Host "`n[4/5] 跳过测试运行（使用-RunTests参数运行测试）" -ForegroundColor Yellow
    }
    
    # 5. 生成报告（如果指定）
    if ($GenerateReport) {
        Write-Host "`n[5/5] 生成测试报告..." -ForegroundColor Cyan
        
        if ($RunTests) {
            Write-Host "  生成JaCoCo覆盖率报告..." -ForegroundColor White
            mvn test jacoco:report -DskipTests=false 2>&1 | Out-Null
            
            $reportPath = "target\site\jacoco\index.html"
            if (Test-Path $reportPath) {
                Write-Host "  [OK] 覆盖率报告已生成: $reportPath" -ForegroundColor Green
            } else {
                Write-Host "  [WARN] 覆盖率报告未生成" -ForegroundColor Yellow
            }
        } else {
            Write-Host "  [INFO] 需要先运行测试才能生成覆盖率报告" -ForegroundColor Yellow
        }
    } else {
        Write-Host "`n[5/5] 跳过报告生成（使用-GenerateReport参数生成报告）" -ForegroundColor Yellow
    }
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  验证完成" -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan
    
    Write-Host "使用说明:" -ForegroundColor Cyan
    Write-Host "  - 运行测试: .\scripts\verify-test-fixes.ps1 -RunTests" -ForegroundColor White
    Write-Host "  - 生成报告: .\scripts\verify-test-fixes.ps1 -RunTests -GenerateReport" -ForegroundColor White
    Write-Host "`n详细修复报告:" -ForegroundColor Cyan
    Write-Host "  documentation\technical\TEST_ERROR_FIX_SUMMARY_2025-01-30.md" -ForegroundColor White
    Write-Host "  documentation\technical\TEST_FIX_COMPLETION_REPORT_2025-01-30.md" -ForegroundColor White
    
} finally {
    Pop-Location
}



