# Entity迁移到common-entity模块脚本
# 用途：将各业务服务的Entity统一迁移到microservices-common-entity模块

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Entity迁移到common-entity模块" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseDir = "D:\IOE-DREAM"
$commonEntityDir = "$baseDir\microservices\microservices-common-entity\src\main\java\net\lab1024\sa\common\entity"

# 需要迁移的模块
$modules = @(
    @{Name="access"; SourcePath="ioedream-access-service\src\main\java\net\lab1024\sa\access\entity"},
    @{Name="attendance"; SourcePath="ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\entity"},
    @{Name="visitor"; SourcePath="ioedream-visitor-service\src\main\java\net\lab1024\sa\visitor\entity"},
    @{Name="biometric"; SourcePath="ioedream-biometric-service\src\main\java\net\lab1024\sa\biometric\entity"}
)

$totalMigrated = 0
$totalErrors = 0

foreach ($module in $modules) {
    $moduleName = $module.Name
    $sourceDir = "$baseDir\microservices\" + $module.SourcePath
    $targetDir = "$commonEntityDir\$moduleName"

    Write-Host "【$moduleName】开始迁移..." -ForegroundColor Yellow

    # 检查源目录是否存在
    if (-not (Test-Path $sourceDir)) {
        Write-Host "  ⚠️  源目录不存在: $sourceDir" -ForegroundColor Red
        continue
    }

    # 创建目标目录
    if (-not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
        Write-Host "  ✅ 创建目标目录: $targetDir" -ForegroundColor Green
    }

    # 获取所有Entity文件
    $entityFiles = Get-ChildItem -Path $sourceDir -Filter "*Entity.java"

    Write-Host "  📦 发现 $($entityFiles.Count) 个Entity文件" -ForegroundColor Cyan

    $moduleMigrated = 0
    $moduleErrors = 0

    foreach ($entityFile in $entityFiles) {
        $fileName = $entityFile.Name
        $targetFile = Join-Path $targetDir $fileName

        try {
            # 读取Entity文件内容
            $content = Get-Content $entityFile.FullName -Raw -Encoding UTF8

            # 更新包声明
            $oldPackage = "package net.lab1024.sa.$moduleName.entity;"
            $newPackage = "package net.lab1024.sa.common.entity.$moduleName;"

            if ($content -match [regex]::Escape($oldPackage)) {
                $content = $content -replace [regex]::Escape($oldPackage), $newPackage

                # 写入到目标文件
                $content | Out-File -FilePath $targetFile -Encoding UTF8 -NoNewline

                Write-Host "    ✅ $fileName" -ForegroundColor Green
                $moduleMigrated++
                $totalMigrated++
            } else {
                Write-Host "    ⚠️  $fileName (包声明不匹配)" -ForegroundColor Yellow
                $moduleErrors++
                $totalErrors++
            }
        } catch {
            Write-Host "    ❌ $fileName (错误: $($_.Exception.Message))" -ForegroundColor Red
            $moduleErrors++
            $totalErrors++
        }
    }

    Write-Host "  📊 $moduleName 模块: $moduleMigrated 个成功, $moduleErrors 个失败" -ForegroundColor Cyan
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "迁移完成统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ 成功迁移: $totalMigrated 个Entity" -ForegroundColor Green
Write-Host "❌ 迁移失败: $totalErrors 个Entity" -ForegroundColor Red
Write-Host ""

if ($totalErrors -eq 0) {
    Write-Host "🎉 所有Entity迁移成功！" -ForegroundColor Green
    exit 0
} else {
    Write-Host "⚠️  部分Entity迁移失败，请检查错误信息" -ForegroundColor Yellow
    exit 1
}
