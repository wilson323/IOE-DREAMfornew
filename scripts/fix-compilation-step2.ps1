# 修复编译问题 - 步骤2: 修复Lombok日志注解
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复Lombok @Slf4j注解" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$projectRoot = "D:\IOE-DREAM"
Set-Location $projectRoot

# 修复SmartPageUtil
$pageUtilPath = "microservices\microservices-common\src\main\java\net\lab1024\sa\common\util\SmartPageUtil.java"

Write-Host "`n正在修复SmartPageUtil..." -ForegroundColor Yellow

if (Test-Path $pageUtilPath) {
    $lines = Get-Content $pageUtilPath
    $newLines = @()
    $importAdded = $false
    $annotationAdded = $false

    foreach ($line in $lines) {
        # 在package声明后添加导入
        if ($line -match "^package " -and -not $importAdded) {
            $newLines += $line
            $newLines += ""
            $newLines += "import lombok.extern.slf4j.Slf4j;"
            $importAdded = $true
            continue
        }

        # 在类声明前添加注解
        if ($line -match "^public class SmartPageUtil" -and -not $annotationAdded) {
            $newLines += "@Slf4j"
            $annotationAdded = $true
        }

        $newLines += $line
    }

    # 保存文件
    $newLines | Out-File -FilePath $pageUtilPath -Encoding UTF8 -Force
    Write-Host "✓ SmartPageUtil已修复" -ForegroundColor Green
}

Write-Host "`n✓ 步骤2完成" -ForegroundColor Green

