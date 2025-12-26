# 简化的BOM修复工具

Write-Host "=== 简化BOM修复工具 ===" -ForegroundColor Cyan

# 获取当前目录的所有微服务目录
$serviceDirs = Get-ChildItem -Directory | Where-Object { $_.Name -match "^ioedream-" }

Write-Host "找到 $($serviceDirs.Count) 个微服务" -ForegroundColor Yellow

$totalFilesFixed = 0

foreach ($serviceDir in $serviceDirs) {
    $serviceName = $serviceDir.Name
    Write-Host "`n处理服务: $serviceName" -ForegroundColor Green

    # 获取所有Java文件
    $javaFiles = Get-ChildItem -Path $serviceDir.FullName -Recurse -Filter "*.java"
    $filesFixed = 0

    foreach ($javaFile in $javaFiles) {
        try {
            # 读取文件
            $content = Get-Content -Path $javaFile.FullName -Raw -Encoding UTF8

            # 检查是否有BOM
            if ($content.StartsWith("﻿")) {
                # 移除BOM
                $content = $content.Substring(1)
            }

            # 修复package声明
            if ($content.StartsWith("ackage")) {
                $content = "p" + $content
            }

            # 修复import声明
            $content = $content -replace '(?m)^iimport', 'import'

            # 写回文件（UTF8无BOM）
            $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($javaFile.FullName, $content, $utf8WithoutBom)

            $filesFixed++
        }
        catch {
            Write-Warning "处理文件失败: $($javaFile.FullName) - $($_.Exception.Message)"
        }
    }

    Write-Host "  ✓ 修复了 $filesFixed 个文件" -ForegroundColor Cyan
    $totalFilesFixed += $filesFixed
}

Write-Host "`n=== 修复完成 ===" -ForegroundColor Green
Write-Host "总计修复文件数: $totalFilesFixed" -ForegroundColor Yellow