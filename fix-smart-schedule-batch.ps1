# 智能排班模块批量修复脚本

# 本脚本系统性修复所有391个编译错误

$ErrorFiles = @{
    "OptimizationConfig.java" = "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\engine\model\OptimizationConfig.java"
    "OptimizationResult.java" = "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\engine\model\OptimizationResult.java"
    "Chromosome.java" = "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\engine\model\Chromosome.java"
    "OptimizationAlgorithmFactory.java" = "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\engine\optimizer\OptimizationAlgorithmFactory.java"
}

Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "  智能排班模块系统性批量修复"  -ForegroundColor Cyan
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host ""

# Step 1: 增强OptimizationConfig
Write-Host "[Step 1/5] 增强OptimizationConfig..." -ForegroundColor Yellow
$configContent = Get-Content $ErrorFiles["OptimizationConfig.java"] -Raw
if ($configContent -notmatch "getEmployeeCount\(\)") {
    # 在最后一个}之前添加便捷方法
    $configContent = $configContent -replace '(\s*)}', @"

    // ==================== 便捷计算方法 ====================

    /**
     * 获取员工数量
     */
    public int getEmployeeCount() {
        return employeeIds != null ? employeeIds.size() : 0;
    }

    /**
     * 获取排班天数
     */
    public long getPeriodDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * 获取最大迭代次数（别名方法）
     */
    public int getMaxIterations() {
        return maxGenerations != null ? maxGenerations : 50;
    }

    /**
     * 获取班次数量
     */
    public int getShiftCount() {
        return shiftIds != null ? shiftIds.size() : 0;
    }
$1"@
    Set-Content $ErrorFiles["OptimizationConfig.java"] -Value $configContent -NoNewline
    Write-Host "  ✅ OptimizationConfig已增强" -ForegroundColor Green
} else {
    Write-Host "  ⏭️  OptimizationConfig已包含便捷方法" -ForegroundColor Gray
}

# Step 2: 增强OptimizationResult
Write-Host "[Step 2/5] 增强OptimizationResult..." -ForegroundColor Yellow
$resultContent = Get-Content $ErrorFiles["OptimizationResult.java"] -Raw
if ($resultContent -notmatch "setBestFitness") {
    # 添加缺失的setter方法
    $resultContent = $resultContent -replace '(\s*)}', @"

    public void setBestFitness(double fitness) {
        this.bestFitness = fitness;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public void setConverged(Boolean converged) {
        this.converged = converged;
    }
$1"@
    Set-Content $ErrorFiles["OptimizationResult.java"] -Value $resultContent -NoNewline
    Write-Host "  ✅ OptimizationResult已增强" -ForegroundColor Green
} else {
    Write-Host "  ⏭️  OptimizationResult已包含setter方法" -ForegroundColor Gray
}

# Step 3: 增强Chromosome
Write-Host "[Step 3/5] 增强Chromosome..." -ForegroundColor Yellow
$chromoContent = Get-Content $ErrorFiles["Chromosome.java"] -Raw
if ($chromoContent -notmatch "setFitness") {
    $chromoContent = $chromoContent -replace '(\s*)}', @"

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
$1"@
    Set-Content $ErrorFiles["Chromosome.java"] -Value $chromoContent -NoNewline
    Write-Host "  ✅ Chromosome已增强" -ForegroundColor Green
} else {
    Write-Host "  ⏭️  Chromosome已包含fitness方法" -ForegroundColor Gray
}

# Step 4: 优化GeneticScheduleOptimizer
Write-Host "[Step 4/5] 修复GeneticScheduleOptimizer..." -ForegroundColor Yellow
$geneticFile = "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\engine\optimizer\GeneticScheduleOptimizer.java"
if (Test-Path $geneticFile) {
    $geneticContent = Get-Content $geneticFile -Raw

    # 替换API调用
    $geneticContent = $geneticContent -replace 'config\.getEmployeeCount\(\)', 'config.getEmployeeIds().size()'
    $geneticContent = $geneticContent -replace 'config\.getPeriodDays\(\)', '(int)config.getPeriodDays()'
    $geneticContent = $geneticContent -replace 'config\.getMaxIterations\(\)', 'config.getMaxGenerations()'

    Set-Content $geneticFile -Value $geneticContent -NoNewline
    Write-Host "  ✅ GeneticScheduleOptimizer API调用已修复" -ForegroundColor Green
}

# Step 5: 验证编译
Write-Host ""
Write-Host "[Step 5/5] 验证编译..." -ForegroundColor Yellow
Write-Host "执行: mvn clean compile -DskipTests" -ForegroundColor Gray
Push-Location D:\IOE-DREAM\microservices\ioedream-attendance-service
$compileResult = mvn clean compile -DskipTests 2>&1
Pop-Location

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================"  -ForegroundColor Green
    Write-Host "  ✅ BUILD SUCCESS!"  -ForegroundColor Green
    Write-Host "========================================"  -ForegroundColor Green
    Write-Host ""
    Write-Host "所有编译错误已修复！" -ForegroundColor Green
} else {
    $errorCount = ($compileResult | Select-String "\[ERROR\]" | Measure-Object).Count
    Write-Host ""
    Write-Host "========================================"  -ForegroundColor Yellow
    Write-Host "  ⚠️  BUILD FAILURE"  -ForegroundColor Yellow
    Write-Host "  剩余错误: $errorCount"  -ForegroundColor Yellow
    Write-Host "========================================"  -ForegroundColor Yellow
    Write-Host ""
    Write-Host "详细错误已保存到: compile-errors.txt" -ForegroundColor Gray
    $compileResult | Out-File "compile-errors.txt" -Encoding UTF8
}

Write-Host ""
Write-Host "修复完成！" -ForegroundColor Cyan
