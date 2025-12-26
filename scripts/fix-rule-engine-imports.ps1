# PowerShell脚本：批量修复考勤规则引擎导入路径
# 作者：IOE-DREAM架构团队
# 日期：2025-12-26
# 用途：修复attendance-service中rule引擎相关的错误导入路径

$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  规则引擎导入路径修复工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 定义attendance-service目录
$attendanceServicePath = "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java"

# 检查目录是否存在
if (-not (Test-Path $attendanceServicePath)) {
    Write-Host "❌ 错误: 目录不存在 - $attendanceServicePath" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 找到目录: $attendanceServicePath" -ForegroundColor Green
Write-Host ""

# 定义导入路径映射
$importMappings = @{
    # 错误的导入路径 → 正确的导入路径
    "import net.lab1024.sa.attendance.engine.RuleExecutor;" = "import net.lab1024.sa.attendance.engine.rule.executor.RuleExecutor;"
    "import net.lab1024.sa.attendance.engine.RuleEvaluatorFactory;" = "import net.lab1024.sa.attendance.engine.rule.evaluator.RuleEvaluatorFactory;"
    "import net.lab1024.sa.attendance.engine.RuleLoader;" = "import net.lab1024.sa.attendance.engine.rule.loader.RuleLoader;"
    "import net.lab1024.sa.attendance.engine.RuleValidator;" = "import net.lab1024.sa.attendance.engine.rule.validator.RuleValidator;"
    "import net.lab1024.sa.attendance.engine.cache.RuleCacheManager;" = "import net.lab1024.sa.attendance.engine.rule.cache.RuleCacheManager;"
    "import net.lab1024.sa.attendance.engine.cache;" = "import net.lab1024.sa.attendance.engine.rule.cache;"
    "import net.lab1024.sa.attendance.engine.model.RuleEvaluationResult;" = "import net.lab1024.sa.attendance.engine.rule.model.RuleEvaluationResult;"
    "import net.lab1024.sa.attendance.engine.model.RuleExecutionContext;" = "import net.lab1024.sa.attendance.engine.rule.model.RuleExecutionContext;"
    "import net.lab1024.sa.attendance.engine.model.RuleValidationResult;" = "import net.lab1024.sa.attendance.engine.rule.model.RuleValidationResult;"
    "import net.lab1024.sa.attendance.engine.model.CompiledRule;" = "import net.lab1024.sa.attendance.engine.rule.model.CompiledRule;"
    "import net.lab1024.sa.attendance.engine.model.CompiledAction;" = "import net.lab1024.sa.attendance.engine.rule.model.CompiledAction;"
    "import net.lab1024.sa.attendance.engine.model.RuleExecutionStatistics;" = "import net.lab1024.sa.attendance.engine.rule.model.RuleExecutionStatistics;"
    "import net.lab1024.sa.attendance.engine.model." = "import net.lab1024.sa.attendance.engine.rule.model."
}

# 递归查找所有Java文件
$javaFiles = Get-ChildItem -Path $attendanceServicePath -Filter "*.java" -Recurse -File

Write-Host "📊 找到 $($javaFiles.Count) 个Java文件" -ForegroundColor Cyan
Write-Host ""

$fixedCount = 0
$skippedCount = 0

# 遍历所有Java文件
foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $originalContent = $content
    $fileFixed = $false

    # 应用每个导入路径映射
    foreach ($mapping in $importMappings.GetEnumerator()) {
        $wrongImport = $mapping.Key
        $correctImport = $mapping.Value

        if ($content -match [regex]::Escape($wrongImport)) {
            $content = $content -replace [regex]::Escape($wrongImport), $correctImport
            $fileFixed = $true

            if (-not $global:fixedFiles.ContainsKey($file.Name)) {
                $global:fixedFiles[$file.Name] = @()
            }
            $global:fixedFiles[$file.Name] += $wrongImport
        }
    }

    # 如果文件被修改，保存更改
    if ($fileFixed) {
        $content | Set-Content $file.FullName -Encoding UTF8 -NoNewline
        $fixedCount++
        Write-Host "✅ 修复: $($file.Name)" -ForegroundColor Green
    } else {
        $skippedCount++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  修复完成统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ 已修复文件: $fixedCount" -ForegroundColor Green
Write-Host "⏭️  跳过文件: $skippedCount" -ForegroundColor Gray
Write-Host "📊 修复率: $($fixedCount / $javaFiles.Count * 100):F2%" -ForegroundColor Cyan
Write-Host ""

# 详细修复信息
if ($fixedCount -gt 0) {
    Write-Host "📝 修复详情:" -ForegroundColor Yellow
    foreach ($fileName in $global:fixedFiles.Keys) {
        Write-Host "  📄 $fileName" -ForegroundColor White
        foreach ($wrongImport in $global:fixedFiles[$fileName]) {
            Write-Host "    ❌ $wrongImport" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "✅ 导入路径修复完成!" -ForegroundColor Green
Write-Host ""

# 清理变量
Remove-Variable -Name importMappings, javaFiles, fixedCount, skippedCount -ErrorAction SilentlyContinue
