# 修复consume-service脚本
# 目的: 删除未完成的移动端和生物识别相关代码，恢复编译

param(
    [switch]$DryRun,
    [switch]$Backup
)

$ErrorActionPreference = "Stop"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "修复consume-service" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

$problematicFiles = @(
    "src/main/java/net/lab1024/sa/consume/controller/EnhancedMobileConsumeController.java",
    "src/main/java/net/lab1024/sa/consume/controller/OfflineDataSyncController.java",
    "src/main/java/net/lab1024/sa/consume/openapi/controller/ConsumeOpenApiController.java",
    "src/main/java/net/lab1024/sa/consume/service/engine/ConsumePaymentEngine.java",
    "src/main/java/net/lab1024/sa/consume/service/engine/RiskAssessmentEngine.java"
)

$edgeDirectories = @(
    "src/main/java/net/lab1024/sa/consume/edge",
    "src/main/java/net/lab1024/sa/consume/mobile"
)

Write-Host "问题文件数量: $($problematicFiles.Count)" -ForegroundColor Yellow
Write-Host "问题目录数量: $($edgeDirectories.Count)" -ForegroundColor Yellow

if ($Backup) {
    $backupDir = "scripts/backup-consume-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    Write-Host "创建备份目录: $backupDir" -ForegroundColor White
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
}

# 删除问题文件
Write-Host "`n删除问题文件..." -ForegroundColor White
foreach ($file in $problematicFiles) {
    $fullPath = "microservices/ioedream-consume-service/$file"
    if (Test-Path $fullPath) {
        if ($DryRun) {
            Write-Host "  [DRY RUN] 将删除: $fullPath" -ForegroundColor Yellow
        } else {
            if ($Backup) {
                $relativePath = $file -replace '[\\/]', '-'
                $backupPath = Join-Path $backupDir $relativePath
                New-Item -ItemType Directory -Path (Split-Path $backupPath) -Force | Out-Null
                Copy-Item $fullPath $backupPath -Force
                Write-Host "  ✅ 备份: $fullPath → $backupPath" -ForegroundColor Green
            }
            Remove-Item $fullPath -Force -Recurse
            Write-Host "  ✅ 删除: $fullPath" -ForegroundColor Red
        }
    } else {
        Write-Host "  ⚠️ 文件不存在: $fullPath" -ForegroundColor Yellow
    }
}

# 删除问题目录
Write-Host "`n删除问题目录..." -ForegroundColor White
foreach ($dir in $edgeDirectories) {
    $fullPath = "microservices/ioedream-consume-service/$dir"
    if (Test-Path $fullPath) {
        if ($DryRun) {
            Write-Host "  [DRY RUN] 将删除: $fullPath" -ForegroundColor Yellow
        } else {
            if ($Backup) {
                $relativePath = $dir -replace '[\\/]', '-'
                $backupPath = Join-Path $backupDir $relativePath
                Copy-Item $fullPath $backupPath -Recurse -Force
                Write-Host "  ✅ 备份: $fullPath → $backupPath" -ForegroundColor Green
            }
            Remove-Item $fullPath -Force -Recurse
            Write-Host "  ✅ 删除: $fullPath" -ForegroundColor Red
        }
    } else {
        Write-Host "  ⚠️ 目录不存在: $fullPath" -ForegroundColor Yellow
    }
}

# 清理未引用的导入
Write-Host "`n清理未引用的导入..." -ForegroundColor White
$javaFiles = Get-ChildItem -Path "microservices/ioedream-consume-service/src/main/java" -Recurse -Filter "*.java"

foreach ($javaFile in $javaFiles) {
    $content = Get-Content $javaFile -Raw -Encoding UTF8
    $lines = $content -split "`n"

    $modified = $false
    $newLines = @()

    foreach ($line in $lines) {
        # 跳过包含问题包的导入行
        if ($line -match "import.*\.(edge|mobile|video)\.") {
            Write-Host "  清理导入: $($line.Trim())" -ForegroundColor Gray
            $modified = $true
        } else {
            $newLines += $line
        }
    }

    if ($modified) {
        $newContent = $newLines -join "`n"
        if ($DryRun) {
            Write-Host "  [DRY RUN] 将修改: $javaFile" -ForegroundColor Yellow
        } else {
            $newContent | Out-File -FilePath $javaFile -Encoding UTF8
            Write-Host "  ✅ 修改: $javaFile" -ForegroundColor Green
        }
    }
}

# 验证修复效果
Write-Host "`n验证修复效果..." -ForegroundColor Yellow
$testResult = & mvn clean compile -pl ioedream-consume-service -am -Dmaven.test.skip=true -Dmaven.clean.failOnError=false 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n🎉 修复成功！consume-service 编译通过！" -ForegroundColor Green
} else {
    $errorCount = ($testResult | Select-String -Pattern "ERROR" | Measure-Object).Count
    Write-Host "`n⚠️ 仍有 $errorCount 个编译错误" -ForegroundColor Yellow
    Write-Host "详细错误信息:" -ForegroundColor DarkRed
    $testResult | Select-String -Pattern "ERROR" -Context 0,1 | Select-Object -First 5 | ForEach-Object {
        Write-Host "  $($_.ToString().Trim())" -ForegroundColor DarkRed
    }
}

if ($Backup -and -not $DryRun) {
    Write-Host "`n📁 备份位置: $backupDir" -ForegroundColor Cyan
}

Write-Host "`n====================================" -ForegroundColor Cyan
Write-Host "consume-service 修复完成" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

if ($DryRun) {
    Write-Host "运行模式: DRY RUN（未实际修改文件）" -ForegroundColor Yellow
    Write-Host "要执行实际修复，请去掉 -DryRun 参数" -ForegroundColor White
} else {
    Write-Host "运行模式: 执行修复" -ForegroundColor Green
    if ($Backup) {
        Write-Host "已创建备份: ✅" -ForegroundColor Green
    }
}

exit $LASTEXITCODE