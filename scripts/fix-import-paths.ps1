# IOE-DREAM 导入路径修复脚本
#
# 功能：批量修复导入路径错误
# 使用方法：.\scripts\fix-import-paths.ps1

param(
    [switch]$DryRun = $false,  # 干运行模式，只显示需要修复的文件，不实际修改
    [string]$TargetModule = ""  # 指定要修复的模块，为空则修复所有模块
)

$ErrorActionPreference = "Stop"

# 导入路径映射表（错误路径 -> 正确路径）
$importPathMappings = @{
    # ResponseDTO 路径修复
    "import net\.lab1024\.sa\.common\.core\.domain\.ResponseDTO"            = "import net.lab1024.sa.common.dto.ResponseDTO"
    "import net\.lab1024\.sa\.common\.domain\.ResponseDTO"                  = "import net.lab1024.sa.common.dto.ResponseDTO"

    # BusinessException 路径修复
    "import net\.lab1024\.sa\.common\.core\.exception\.BusinessException"   = "import net.lab1024.sa.common.exception.BusinessException"
    "import net\.lab1024\.sa\.common\.domain\.exception\.BusinessException" = "import net.lab1024.sa.common.exception.BusinessException"

    # Entity 路径修复
    "import net\.lab1024\.sa\.common\.entity\.AreaEntity"                   = "import net.lab1024.sa.common.organization.entity.AreaEntity"
    "import net\.lab1024\.sa\.common\.entity\.UserEntity"                   = "import net.lab1024.sa.common.organization.entity.UserEntity"
    "import net\.lab1024\.sa\.common\.entity\.DeviceEntity"                 = "import net.lab1024.sa.common.organization.entity.DeviceEntity"

    # 服务接口路径修复（如果存在错误）
    "import net\.lab1024\.sa\.common\.organization\.service\.AreaService"   = "import net.lab1024.sa.common.organization.service.AreaUnifiedService"
    "import net\.lab1024\.sa\.common\.organization\.service\.DeviceService" = "import net.lab1024.sa.common.organization.manager.AreaDeviceManager"
}

# 统计信息
$stats = @{
    TotalFiles   = 0
    FixedFiles   = 0
    FixedImports = 0
    Errors       = @()
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 导入路径修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($DryRun) {
    Write-Host "[干运行模式] 只显示需要修复的文件，不实际修改" -ForegroundColor Yellow
    Write-Host ""
}

# 确定要扫描的目录
$scanDirs = @()
if ($TargetModule -ne "") {
    $modulePath = "microservices\$TargetModule"
    if (Test-Path $modulePath) {
        $scanDirs = @($modulePath)
    }
    else {
        Write-Host "错误: 模块路径不存在: $modulePath" -ForegroundColor Red
        exit 1
    }
}
else {
    $scanDirs = @(
        "microservices\ioedream-common-service",
        "microservices\ioedream-access-service",
        "microservices\ioedream-attendance-service",
        "microservices\ioedream-consume-service",
        "microservices\ioedream-visitor-service",
        "microservices\ioedream-video-service",
        "microservices\ioedream-oa-service",
        "microservices\ioedream-device-comm-service",
        "microservices\ioedream-biometric-service",
        "microservices\ioedream-database-service"
    )
}

# 扫描并修复Java文件
foreach ($dir in $scanDirs) {
    if (-not (Test-Path $dir)) {
        Write-Host "跳过不存在的目录: $dir" -ForegroundColor Yellow
        continue
    }

    Write-Host "扫描目录: $dir" -ForegroundColor Green

    $javaFiles = Get-ChildItem -Path $dir -Filter "*.java" -Recurse | Where-Object { $_.FullName -notmatch "\\target\\" }

    foreach ($file in $javaFiles) {
        $stats.TotalFiles++
        $content = Get-Content $file.FullName -Raw -Encoding UTF-8
        $originalContent = $content
        $fileFixed = $false
        $importsFixed = 0

        # 应用所有导入路径映射
        foreach ($mapping in $importPathMappings.GetEnumerator()) {
            $pattern = $mapping.Key
            $replacement = $mapping.Value

            if ($content -match $pattern) {
                $content = $content -replace $pattern, $replacement
                $importsFixed++
                $fileFixed = $true
            }
        }

        if ($fileFixed) {
            $stats.FixedFiles++
            $stats.FixedImports += $importsFixed

            if ($DryRun) {
                Write-Host "  [需要修复] $($file.FullName) - $importsFixed 个导入路径" -ForegroundColor Yellow
            }
            else {
                try {
                    # 备份原文件
                    $backupPath = $file.FullName + ".bak"
                    Copy-Item $file.FullName $backupPath -Force

                    # 写入修复后的内容
                    [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)

                    Write-Host "  [已修复] $($file.FullName) - $importsFixed 个导入路径" -ForegroundColor Green
                }
                catch {
                    $stats.Errors += "修复文件失败: $($file.FullName) - $($_.Exception.Message)"
                    Write-Host "  [错误] $($file.FullName) - $($_.Exception.Message)" -ForegroundColor Red
                }
            }
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "扫描文件数: $($stats.TotalFiles)" -ForegroundColor White
Write-Host "修复文件数: $($stats.FixedFiles)" -ForegroundColor $(if ($DryRun) { "Yellow" } else { "Green" })
Write-Host "修复导入数: $($stats.FixedImports)" -ForegroundColor $(if ($DryRun) { "Yellow" } else { "Green" })

if ($stats.Errors.Count -gt 0) {
    Write-Host ""
    Write-Host "错误列表:" -ForegroundColor Red
    foreach ($error in $stats.Errors) {
        Write-Host "  - $error" -ForegroundColor Red
    }
}

if ($DryRun) {
    Write-Host ""
    Write-Host "提示: 使用 -DryRun:`$false 参数执行实际修复" -ForegroundColor Yellow
}

Write-Host ""

