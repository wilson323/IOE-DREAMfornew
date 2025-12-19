# 快速修复缺失类脚本
# 目的: 简化版本，快速分析和修复缺失类问题

param(
    [string]$ServiceName = "",
    [switch]$RemoveProblematicFiles
)

$ErrorActionPreference = "Stop"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "快速修复缺失类" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

# 分析指定服务或所有服务
function Quick-AnalyzeService {
    param([string]$Service)

    Write-Host "`n分析服务: $Service" -ForegroundColor Yellow

    try {
        # 获取编译错误
        $errorOutput = & mvn clean compile -pl $Service -am -Dmaven.test.skip=true -Dmaven.clean.failOnError=false 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✅ $Service 编译成功" -ForegroundColor Green
            return @{
                Service = $Service
                Status = "Success"
                Issues = @()
            }
        }

        # 提取问题文件
        $problematicFiles = @()
        $errorOutput | Select-String -Pattern "([^/]+\.java):\[\d]+" | ForEach-Object {
            if ($_ -match '([^/]+\.java):\[\d]+') {
                $filePath = $matches[1]
                if (Test-Path $filePath) {
                    $problematicFiles += $filePath
                }
            }
        }

        # 提取缺失类
        $missingClasses = @()
        $errorOutput | Select-String -Pattern "找不到符号.*类\s+(\w+)" | ForEach-Object {
            if ($_ -match '找不到符号.*类\s+(\w+)') {
                $missingClasses += $matches[1]
            }
        }

        Write-Host "  ❌ 编译失败" -ForegroundColor Red
        Write-Host "  问题文件: $($problematicFiles.Count) 个" -ForegroundColor Yellow
        Write-Host "  缺失类: $($missingClasses.Count) 个" -ForegroundColor Yellow

        # 建议删除的文件（基于错误密度）
        $filesToDelete = $problematicFiles | Group-Object | Where-Object { $_.Count -gt 5 } | Select-Object -ExpandProperty Name | Get-Unique

        return @{
            Service = $Service
            Status = "Error"
            ProblematicFiles = $problematicFiles
            MissingClasses = $missingClasses
            FilesToDelete = $filesToDelete
        }

    } catch {
        Write-Host "  ❌ 分析 $Service 时出错: $($_.Exception.Message)" -ForegroundColor Red
        return @{
            Service = $Service
            Status = "Error"
            Error = $_.Exception.Message
        }
    }
}

# 删除问题文件
function Remove-ProblematicFiles {
    param([array]$Files, [string]$ServiceName)

    if ($Files.Count -eq 0) {
        return
    }

    Write-Host "`n删除问题文件..." -ForegroundColor Yellow

    $backupDir = "scripts/backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

    foreach ($file in $Files) {
        if (Test-Path $file) {
            $fileName = Split-Path $file -Leaf
            $backupPath = Join-Path $backupDir $fileName

            Write-Host "  备份: $file → $backupPath" -ForegroundColor Gray
            Copy-Item $file $backupPath -Force

            Write-Host "  删除: $file" -ForegroundColor Red
            Remove-Item $file -Force
        }
    }

    Write-Host "  ✅ 已备份和删除 $($Files.Count) 个问题文件" -ForegroundColor Green
    Write-Host "  📁 备份位置: $backupDir" -ForegroundColor Cyan
}

# 验证修复效果
function Test-ServiceAfterFix {
    param([string]$Service)

    Write-Host "`n验证修复效果: $Service" -ForegroundColor Yellow

    $result = & mvn clean compile -pl $Service -am -Dmaven.test.skip=true -Dmaven.clean.failOnError=false -q 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ 修复成功，$Service 编译通过！" -ForegroundColor Green
        return $true
    } else {
        $remainingErrors = $result | Select-String -Pattern "ERROR" | Measure-Object | Select-Object -ExpandProperty Count
        Write-Host "  ⚠️ 仍有 $remainingErrors 个编译错误" -ForegroundColor Yellow
        return $false
    }
}

# 执行主流程
try {
    if ($ServiceName) {
        $services = @($ServiceName)
    } else {
        $services = @(
            "ioedream-attendance-service",
            "ioedream-consume-service",
            "ioedream-visitor-service",
            "ioedream-video-service"
        )
    }

    $allResults = @()
    $totalDeletedFiles = 0

    foreach ($service in $services) {
        $result = Quick-AnalyzeService -Service $service
        $allResults += $result

        if ($RemoveProblematicFiles -and $result.FilesToDelete.Count -gt 0) {
            Write-Host "`n是否要删除 $service 的问题文件？(y/N)" -ForegroundColor Yellow
            $response = Read-Host
            if ($response -eq 'y' -or $response -eq 'Y') {
                Remove-ProblematicFiles -Files $result.FilesToDelete -ServiceName $service
                $totalDeletedFiles += $result.FilesToDelete.Count
            }
        }
    }

    # 显示汇总结果
    Write-Host "`n====================================" -ForegroundColor Cyan
    Write-Host "快速修复完成汇总" -ForegroundColor Cyan
    Write-Host "====================================" -ForegroundColor Cyan

    $successCount = 0
    $errorCount = 0

    foreach ($result in $allResults) {
        if ($result.Status -eq "Success") {
            $successCount++
            Write-Host "✅ $($result.Service): 编译正常" -ForegroundColor Green
        } elseif ($result.Status -eq "Error") {
            $errorCount++
            Write-Host "❌ $($result.Service): $($result.ProblematicFiles.Count) 个问题文件, $($result.MissingClasses.Count) 个缺失类" -ForegroundColor Red
        } else {
            Write-Host "⚠️ $($result.Service): 分析失败" -ForegroundColor Yellow
        }
    }

    Write-Host "`n统计结果:" -ForegroundColor Cyan
    Write-Host "- 正常服务: $successCount" -ForegroundColor Green
    Write-Host "- 问题服务: $errorCount" -ForegroundColor Red
    Write-Host "- 已删除文件: $totalDeletedFiles" -ForegroundColor Cyan

    if ($errorCount -gt 0) {
        Write-Host "`n💡 建议:" -ForegroundColor Yellow
        Write-Host "1. 专注于核心业务功能，暂时删除未完成的扩展功能"
        Write-Host "2. 使用 -RemoveProblematicFiles 参数自动删除问题文件"
        Write-Host "3. 分阶段重构，确保每个阶段都能编译通过"
    } else {
        Write-Host "`n🎉 所有指定服务都已修复！" -ForegroundColor Green
    }

} catch {
    Write-Host "`n❌ 快速修复失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}