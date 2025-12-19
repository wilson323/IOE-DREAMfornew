# 修复consume-service剩余问题脚本
# 目的: 修复log变量缺失和包导入问题

param(
    [switch]$DryRun,
    [switch]$Backup
)

$ErrorActionPreference = "Stop"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "修复consume-service剩余问题" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

$serviceRoot = "D:/IOE-DREAM/microservices/ioedream-consume-service"
$fixCount = 0
$logFixCount = 0

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path "$serviceRoot/src/main/java" -Recurse -Filter "*.java"

Write-Host "`n处理文件: $($javaFiles.Count) 个" -ForegroundColor White

foreach ($javaFile in $javaFiles) {
    try {
        $content = Get-Content $javaFile -Raw -Encoding UTF8
        $originalContent = $content
        $modified = $false
        $logAdded = $false

        # 1. 修复包导入问题
        $content = $content -replace "import\s+net\.lab1024\.sa\.common\.consume\.entity", "import net.lab1024.sa.consume.entity"
        $content = $content -replace "import\s+net\.lab1024\.sa\.common\.consume\.dao", "import net.lab1024.sa.consume.dao"
        $content = $content -replace "import\s+net\.lab1024\.sa\.common\.consume\.manager", "import net.lab1024.sa.consume.manager"
        $content = $content -replace "import\s+net\.lab1024\.sa\.common\.consume\.service", "import net.lab1024.sa.consume.service"

        # 2. 检查是否需要添加@Slf4j注解
        $hasLogUsage = $content -match "\blog\s*\."
        $hasSlf4j = $content -match "@Slf4j"
        $isControllerOrService = $javaFile.FullName -match ".*(Controller|Service|Manager|Component)\.java$"

        # 3. 如果使用log但没有@Slf4j，且是业务类，则添加@Slf4j
        if ($hasLogUsage -and -not $hasSlf4j -and $isControllerOrService) {
            # 检查是否有lombok.extern.slf4j导入
            if ($content -notmatch "import\s+lombok\.extern\.slf4j\.Slf4j") {
                # 找到package语句后的位置
                $packageEndIndex = $content.IndexOf(";", $content.IndexOf("package"))
                if ($packageEndIndex -gt 0) {
                    $insertPosition = $packageEndIndex + 1
                    $importStatement = "`n`nimport lombok.extern.slf4j.Slf4j;"
                    $content = $content.Insert($insertPosition, $importStatement)
                    $logAdded = $true
                }
            }

            # 找到类声明前的位置添加@Slf4j注解
            $classMatch = [regex]::Match($content, "(?s)(.*?)(public\s+(?:@[\w\s.]+\s+)?class\s+\w+)")
            if ($classMatch.Success -and $content -notmatch "@Slf4j") {
                $beforeClass = $classMatch.Groups[1].Value
                $slf4jAnnotation = "@Slf4j`n"

                # 如果类前没有其他注解，直接添加@Slf4j
                if ($beforeClass -notmatch "@\w+") {
                    $content = $content.Replace($beforeClass, $beforeClass + $slf4jAnnotation)
                    $logAdded = $true
                } else {
                    # 如果有其他注解，在最后一个注解后添加@Slf4j
                    $annotationMatch = [regex]::Match($beforeClass, "(.*?)(@\w+(?:\([^)]*\))?\s*)\s*$")
                    if ($annotationMatch.Success) {
                        $content = $content.Replace($beforeClass, $beforeClass + $slf4jAnnotation)
                        $logAdded = $true
                    }
                }
            }
        }

        # 4. 删除有问题的导入
        $problematicImports = @(
            "import net.lab1024.sa.consume.consume.dao",
            "import net.lab1024.sa.consume.consume.domain.form",
            "import net.lab1024.sa.consume.consume.entity"
        )

        foreach ($import in $problematicImports) {
            $content = $content -replace "(?m)\s*$import\s+$import[^;]+;", ""
        }

        # 5. 删除对已删除类的引用
        $content = $content -replace "DefaultFixedAmountCalculator", ""
        $content = $content -replace "DefaultVariableAmountCalculator", ""

        # 检查是否有修改
        if ($content -ne $originalContent) {
            $modified = $true
            $fixCount++
        }

        if ($logAdded) {
            $logFixCount++
        }

        if ($modified) {
            if ($DryRun) {
                Write-Host "  [DRY RUN] 将修改: $($javaFile.Name)" -ForegroundColor Yellow
                if ($logAdded) {
                    Write-Host "    - 添加@Slf4j注解" -ForegroundColor Gray
                }
            } else {
                if ($Backup) {
                    $backupDir = "scripts/backup-consume-fixes-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
                    if (!(Test-Path $backupDir)) {
                        New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
                    }
                    $relativePath = $javaFile.FullName.Replace($serviceRoot, "").TrimStart('\').Replace('\', '-')
                    $backupPath = Join-Path $backupDir $relativePath
                    New-Item -ItemType Directory -Path (Split-Path $backupPath) -Force | Out-Null
                    Copy-Item $javaFile.FullName $backupPath -Force
                }

                $content | Out-File -FilePath $javaFile.FullName -Encoding UTF8
                Write-Host "  ✅ 修改: $($javaFile.Name)" -ForegroundColor Green
                if ($logAdded) {
                    Write-Host "    - 添加@Slf4j注解" -ForegroundColor Gray
                }
            }
        }
    } catch {
        Write-Host "  ⚠️ 处理文件时出错: $($javaFile.FullName) - $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

Write-Host "`n修复统计:" -ForegroundColor Cyan
Write-Host "- 修改文件数: $fixCount" -ForegroundColor White
Write-Host "- 添加@Slf4j注解数: $logFixCount" -ForegroundColor White

# 验证修复效果
Write-Host "`n验证修复效果..." -ForegroundColor Yellow
try {
    Push-Location $serviceRoot
    $testResult = mvn clean compile -Dmaven.test.skip=true -Dmaven.clean.failOnError=false 2>&1
    Pop-Location

    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n🎉 修复成功！consume-service 编译通过！" -ForegroundColor Green
    } else {
        $errorCount = ($testResult | Select-String -Pattern "ERROR" | Measure-Object).Count
        Write-Host "`n⚠️ 仍有 $errorCount 个编译错误" -ForegroundColor Yellow

        # 显示前10个错误
        Write-Host "前10个错误:" -ForegroundColor DarkRed
        $testResult | Select-String -Pattern "ERROR" | Select-Object -First 10 | ForEach-Object {
            $errorLine = $_.ToString().Trim()
            if ($errorLine -match "找不到符号.*类\s+(\w+)") {
                Write-Host "  缺失类: $($matches[1])" -ForegroundColor DarkRed
            } elseif ($errorLine -match "找不到符号.*变量\s+(\w+)") {
                Write-Host "  缺失变量: $($matches[1])" -ForegroundColor DarkRed
            } else {
                Write-Host "  $errorLine" -ForegroundColor DarkRed
            }
        }

        # 提供下一步建议
        Write-Host "`n💡 下一步建议:" -ForegroundColor Cyan
        Write-Host "1. 检查缺失的VO/DTO类并创建" -ForegroundColor White
        Write-Host "2. 验证Entity类的字段映射" -ForegroundColor White
        Write-Host "3. 检查Service/Manager类的依赖注入" -ForegroundColor White
    }
} catch {
    Write-Host "`n❌ 验证过程出错: $($_.Exception.Message)" -ForegroundColor Red
}

if ($Backup -and -not $DryRun) {
    Write-Host "`n📁 备份位置: scripts/backup-consume-fixes-*" -ForegroundColor Cyan
}

Write-Host "`n====================================" -ForegroundColor Cyan
Write-Host "consume-service 剩余问题修复完成" -ForegroundColor Cyan
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