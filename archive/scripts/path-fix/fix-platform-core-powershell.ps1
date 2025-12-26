# P1阶段：使用PowerShell原生方法修复platform.core包路径残留
Write-Host "🔧 P1阶段：修复platform.core包路径残留问题..." -ForegroundColor Green

$servicesPath = "D:\IOE-DREAM\microservices"
$fixedFiles = 0
$totalFiles = 0

# 包路径替换规则
$replacements = @{
    "net\.lab1024\.sa\.platform\.core\.exception\." = "net.lab1024.sa.common.exception."
    "net\.lab1024\.sa\.platform\.core\.util\." = "net.lab1024.sa.common.util."
    "net\.lab1024\.sa\.platform\.core\.constant\." = "net.lab1024.sa.common.constant."
    "net\.lab1024\.sa\.platform\.core\." = "net.lab1024.sa.common."
}

Write-Host "📂 扫描Java文件..." -ForegroundColor Cyan

# 使用Get-ChildItem递归查找所有Java文件
$javaFiles = Get-ChildItem -Path $servicesPath -Recurse -Filter "*.java" -File

Write-Host "找到 $($javaFiles.Count) 个Java文件，开始检查platform.core包路径..." -ForegroundColor White

foreach ($file in $javaFiles) {
    try {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $hasPlatformCore = $false

        # 检查是否包含platform.core包路径
        foreach ($pattern in $replacements.Keys) {
            if ($content -match $pattern) {
                $hasPlatformCore = $true
                break
            }
        }

        if ($hasPlatformCore) {
            $totalFiles++
            $originalContent = $content
            $fixedContent = $content
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
                [System.IO.File]::WriteAllBytes($file.FullName, $bytes)

                $fixedFiles++
                $relativePath = $file.FullName.Replace("$servicesPath\", "")
                Write-Host "✓ 修复: $relativePath" -ForegroundColor Green
            }
        }

    } catch {
        Write-Host "❌ 处理失败: $($file.FullName) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Platform.Core包路径修复报告 ===" -ForegroundColor Magenta
Write-Host "扫描文件总数: $($javaFiles.Count)" -ForegroundColor White
Write-Host "需要修复文件数: $totalFiles" -ForegroundColor Yellow
Write-Host "实际修复文件数: $fixedFiles" -ForegroundColor Green

if ($totalFiles -gt 0) {
    $fixRate = [math]::Round($fixedFiles * 100.0 / $totalFiles, 2)
    Write-Host "修复率: $fixRate%" -ForegroundColor Cyan
} else {
    Write-Host "无需修复：没有发现platform.core包路径" -ForegroundColor Green
}

# 验证修复结果
Write-Host "`n🔍 验证修复结果..." -ForegroundColor Green
$remainingCount = 0
$remainingFiles = @()

foreach ($file in $javaFiles) {
    try {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $hasPlatformCore = $false

        foreach ($pattern in $replacements.Keys) {
            if ($content -match $pattern) {
                $hasPlatformCore = $true
                break
            }
        }

        if ($hasPlatformCore) {
            $remainingCount++
            if ($remainingCount -le 5) {
                $relativePath = $file.FullName.Replace("$servicesPath\", "")
                $remainingFiles += $relativePath
            }
        }
    } catch {
        # 忽略读取错误
    }
}

Write-Host "剩余未修复文件: $remainingCount" -ForegroundColor $(if ($remainingCount -eq 0) { "Green" } else { "Yellow" })

if ($remainingCount -gt 0 -and $remainingCount -le 5) {
    Write-Host "剩余文件列表:" -ForegroundColor Yellow
    foreach ($file in $remainingFiles) {
        Write-Host "  - $file" -ForegroundColor White
    }
} elseif ($remainingCount -gt 5) {
    Write-Host "剩余文件过多（超过5个），需要进一步分析" -ForegroundColor Red
}

# 测试编译
Write-Host "`n🔍 测试编译..." -ForegroundColor Green
cd $servicesPath

# 选择common-service进行测试编译
$compileResult = mvn clean compile -pl ioedream-common-service -q -DskipTests 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✅ common-service编译成功！" -ForegroundColor Green

    # 测试另一个服务
    $compileResult2 = mvn clean compile -pl ioedream-access-service -q -DskipTests 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ access-service编译成功！" -ForegroundColor Green
    } else {
        Write-Host "  ❌ access-service编译失败" -ForegroundColor Red
    }

    if ($remainingCount -eq 0) {
        Write-Host "🎉 Platform.Core包路径修复完成！" -ForegroundColor Green
        Write-Host "✅ P1阶段包路径清理任务完成" -ForegroundColor Green
        Write-Host "✅ 所有platform.core包路径已统一为common包路径" -ForegroundColor Green
    }
} else {
    Write-Host "  ❌ common-service编译失败" -ForegroundColor Red

    # 显示相关错误信息
    $errorOutput = $compileResult -split "`n" | Select-String "ERROR"
    if ($errorOutput) {
        Write-Host "  错误信息:" -ForegroundColor Yellow
        foreach ($error in $errorOutput | Select-Object -First 5) {
            if ($error -match "platform\.core") {
                Write-Host "    ⚠️ $error" -ForegroundColor Yellow
            } else {
                Write-Host "    $error" -ForegroundColor Red
            }
        }
    }
}

# 生成修复报告
$reportPath = "D:\IOE-DREAM\P1-platform-core-fix-report.md"
$reportContent = @"
# P1阶段Platform.Core包路径修复报告

**生成时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**修复范围**: IOE-DREAM微服务项目

## 修复统计

- **扫描文件总数**: $($javaFiles.Count)
- **需要修复文件数**: $totalFiles
- **实际修复文件数**: $fixedFiles
- **剩余未修复**: $remainingCount

## 修复内容

### 包路径替换规则
- `net.lab1024.sa.platform.core.exception.*` → `net.lab1024.sa.common.exception.*`
- `net.lab1024.sa.platform.core.util.*` → `net.lab1024.sa.common.util.*`
- `net.lab1024.sa.platform.core.constant.*` → `net.lab1024.sa.common.constant.*`
- `net.lab1024.sa.platform.core.*` → `net.lab1024.sa.common.*`

### 修复状态
$(
if ($fixedFiles -gt 0) {
    "✅ 成功修复 $fixedFiles 个文件的platform.core包路径"
} else {
    "⚠️ 没有发现需要修复的platform.core包路径文件"
}
)
$(
if ($remainingCount -gt 0) {
    "❌ 仍有 $remainingCount 个文件包含platform.core包路径，需要手动处理"
} else {
    "✅ 所有platform.core包路径已修复完成"
}
)
"@

[System.IO.File]::WriteAllText($reportPath, $reportContent, [System.Text.Encoding]::UTF8)
Write-Host "`n📄 详细修复报告已生成: $reportPath" -ForegroundColor Green