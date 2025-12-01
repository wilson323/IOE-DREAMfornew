# IOE-DREAM 系统服务编码修复脚本
# 修复system-service中的乱码问题

$ErrorActionPreference = "Stop"
$Utf8NoBomEncoding = New-Object System.Text.UTF8Encoding $false

# 定义需要修复的文件和乱码映射
$filesToFix = @(
    @{
        Path = "microservices\ioedream-system-service\src\main\java\net\lab1024\sa\system\role\service\RoleService.java"
        Fixes = @{
            "获取所有角?" = "获取所有角色"
            "为角色分配权?" = "为角色分配权限"
            "获取角色的权限列?" = "获取角色的权限列表"
            "为角色分配用?" = "为角色分配用户"
            "获取角色的用户列?" = "获取角色的用户列表"
            "为空则刷新所?" = "为空则刷新所有"
            "修改角色状?" = "修改角色状态"
            "状?" = "状态"
            "是否被用户使?" = "是否被用户使用"
        }
    },
    @{
        Path = "microservices\ioedream-system-service\src\main\java\net\lab1024\sa\system\service\UnifiedDeviceService.java"
        Fixes = @{
            "更新设备状?" = "更新设备状态"
            "设备状?" = "设备状态"
            "远程开?" = "远程开门"
            "获取实时视频?" = "获取实时视频流"
            "获取设备状态统?" = "获取设备状态统计"
        }
    },
    @{
        Path = "microservices\ioedream-system-service\src\main\java\net\lab1024\sa\system\service\impl\UnifiedDeviceServiceImpl.java"
        Fixes = @{
            "检查设备编号是否重?" = "检查设备编号是否重复"
            "设备编号已存?" = "设备编号已存在"
            "设置创建者信?" = "设置创建者信息"
            "设备不存?" = "设备不存在"
            "通知设备管理器更新设备信?" = "通知设备管理器更新设备信息"
            "软删除设?" = "软删除设备"
            "按设备类型统?" = "按设备类型统计"
            "成功处?" = "成功处理"
            "个设?" = "个设备"
        }
    }
)

Write-Host "开始修复system-service中的编码问题..." -ForegroundColor Green

$totalFiles = 0
$fixedFiles = 0
$errorFiles = 0

foreach ($fileInfo in $filesToFix) {
    $filePath = Join-Path $PSScriptRoot ".." $fileInfo.Path
    $filePath = [System.IO.Path]::GetFullPath($filePath)

    if (-not (Test-Path $filePath)) {
        Write-Host "  [WARNING] 文件不存在: $filePath" -ForegroundColor Yellow
        $errorFiles++
        continue
    }

    $totalFiles++
    Write-Host "处理文件: $($fileInfo.Path)" -ForegroundColor Cyan

    try {
        # 读取文件内容（尝试UTF-8）
        $content = Get-Content $filePath -Raw -Encoding UTF8 -ErrorAction SilentlyContinue

        if ($null -eq $content) {
            # 如果UTF-8读取失败，尝试默认编码
            $bytes = [System.IO.File]::ReadAllBytes($filePath)
            $content = [System.Text.Encoding]::GetEncoding("GBK").GetString($bytes)
        }

        $originalContent = $content
        $hasChanges = $false

        # 应用乱码修复
        foreach ($key in $fileInfo.Fixes.Keys) {
            if ($content -match [regex]::Escape($key)) {
                $content = $content -replace [regex]::Escape($key), $fileInfo.Fixes[$key]
                $hasChanges = $true
                Write-Host "    [FIX] 修复: $key -> $($fileInfo.Fixes[$key])" -ForegroundColor Yellow
            }
        }

        # 如果有更改，保存文件
        if ($hasChanges) {
            # 移除BOM标记
            if ($content.StartsWith([char]0xFEFF)) {
                $content = $content.Substring(1)
            }

            # 保存为UTF-8无BOM
            [System.IO.File]::WriteAllText($filePath, $content, $Utf8NoBomEncoding)
            Write-Host "  [SUCCESS] 文件已修复并保存: $($fileInfo.Path)" -ForegroundColor Green
            $fixedFiles++
        } else {
            Write-Host "  [SKIP] 文件无需修复: $($fileInfo.Path)" -ForegroundColor Gray
        }

    } catch {
        Write-Host "  [ERROR] 处理文件失败: $($fileInfo.Path)" -ForegroundColor Red
        Write-Host "    错误信息: $($_.Exception.Message)" -ForegroundColor Red
        $errorFiles++
    }
}

Write-Host "`n修复完成！" -ForegroundColor Green
Write-Host "  总文件数: $totalFiles" -ForegroundColor White
Write-Host "  修复文件数: $fixedFiles" -ForegroundColor Green
Write-Host "  错误文件数: $errorFiles" -ForegroundColor Red

