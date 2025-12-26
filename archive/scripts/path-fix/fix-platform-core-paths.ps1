# P1阶段：修复platform.core包路径残留问题
# 数量：171个文件，超过50个，符合脚本修复条件

Write-Host "🔧 P1阶段：修复platform.core包路径残留问题..." -ForegroundColor Green
Write-Host "发现 171 个文件需要修复，超过50个，执行脚本修复" -ForegroundColor Cyan

$servicesPath = "D:\IOE-DREAM\microservices"
$fixedFiles = 0
$totalFiles = 171

# 包路径替换规则
$replacements = @{
    # ResponseDTO已在P0阶段修复，跳过
    # 异常类包路径修复
    "net\.lab1024\.sa\.platform\.core\.exception\." = "net.lab1024.sa.common.exception."
    # 工具类包路径修复
    "net\.lab1024\.sa\.platform\.core\.util\." = "net.lab1024.sa.common.util."
    # 常量类包路径修复
    "net\.lab1024\.sa\.platform\.core\.constant\." = "net.lab1024.sa.common.constant."
    # 其他core包路径修复
    "net\.lab1024\.sa\.platform\.core\." = "net.lab1024.sa.common."
}

# 使用rg获取需要修复的文件列表
$javaFiles = @()
$rgResult = & rg "net\.lab1024\.sa\.platform\.core\." --type java -l $servicesPath 2>$null

if ($rgResult) {
    $javaFiles = $rgResult -split "`n" | Where-Object { $_ -and (Test-Path $_) }
}

Write-Host "找到 $($javaFiles.Count) 个需要修复的文件" -ForegroundColor Yellow

foreach ($file in $javaFiles) {
    try {
        $relativePath = $file.Replace("$servicesPath\", "")
        $originalContent = Get-Content -Path $file -Raw -Encoding UTF8
        $fixedContent = $originalContent
        $hasChanges = $false

        # 应用所有替换规则
        foreach ($pattern in $replacements.Keys) {
            $replacement = $replacements[$pattern]
            if ($fixedContent -match $pattern) {
                $fixedContent = $fixedContent -replace $pattern, $replacement
                $hasChanges = $true
            }
        }

        # 如果有修改，保存文件
        if ($hasChanges) {
            # 移除BOM字符并保存
            $utf8WithBOM = [System.Text.UTF8Encoding]::new($true)
            $utf8WithoutBOM = [System.Text.UTF8Encoding]::new($false)
            $bytes = $utf8WithoutBOM.GetBytes($fixedContent)
            [System.IO.File]::WriteAllBytes($file, $bytes)

            $fixedFiles++
            Write-Host "✓ 修复: $relativePath" -ForegroundColor Green
        }

    } catch {
        Write-Host "❌ 处理失败: $($file) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Platform.Core包路径修复报告 ===" -ForegroundColor Magenta
Write-Host "修复文件数: $fixedFiles" -ForegroundColor Green
Write-Host "总文件数: $totalFiles" -ForegroundColor White
Write-Host "修复率: $([math]::Round($fixedFiles * 100.0 / $totalFiles, 2))%" -ForegroundColor Cyan

# 验证修复结果
Write-Host "`n🔍 验证修复结果..." -ForegroundColor Green
$remainingFiles = @()
$rgResult = & rg "net\.lab1024\.sa\.platform\.core\." --type java -l $servicesPath 2>$null
if ($rgResult) {
    $remainingFiles = $rgResult -split "`n" | Where-Object { $_ -and (Test-Path $_) }
}

$remainingCount = if ($rgResult) { $remainingFiles.Count } else { 0 }

Write-Host "剩余未修复文件: $remainingCount" -ForegroundColor $(if ($remainingCount -eq 0) { "Green" } else { "Yellow" })

if ($remainingCount -gt 0 -and $remainingCount -le 10) {
    Write-Host "剩余文件列表:" -ForegroundColor Yellow
    foreach ($file in $remainingFiles) {
        Write-Host "  - $($file.Replace($servicesPath, [string]::Empty))" -ForegroundColor White
    }
} elseif ($remainingCount -gt 10) {
    Write-Host "剩余文件过多，需要进一步分析" -ForegroundColor Red
}

# 测试编译
Write-Host "`n🔍 测试编译..." -ForegroundColor Green
cd $servicesPath

# 选择一个服务进行测试编译
$compileResult = mvn clean compile -pl ioedream-common-service -q -DskipTests 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✅ 测试服务编译成功！" -ForegroundColor Green

    if ($remainingCount -eq 0) {
        Write-Host "🎉 Platform.Core包路径修复完成！" -ForegroundColor Green
        Write-Host "✅ P1阶段包路径清理任务完成" -ForegroundColor Green
        Write-Host "✅ 所有platform.core包路径已统一为common包路径" -ForegroundColor Green
    }
} else {
    Write-Host "  ❌ 测试服务编译失败" -ForegroundColor Red
    Write-Host "  错误信息:" -ForegroundColor Yellow

    # 显示前5个错误，重点关注platform.core相关错误
    $errorLines = $compileResult -split "`n" | Select-String "ERROR" | Select-Object -First 5
    foreach ($error in $errorLines) {
        if ($error -match "platform\.core") {
            Write-Host "    ⚠️ $error" -ForegroundColor Yellow
        } else {
            Write-Host "    $error" -ForegroundColor Red
        }
    }
}