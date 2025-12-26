# 修复storage模块BOM

Write-Host "=== 修复storage模块BOM ===" -ForegroundColor Cyan

Get-ChildItem -Path "D:\IOE-DREAM\microservices\microservices-common-storage" -Recurse -Filter "*.java" | ForEach-Object {
    try {
        $content = Get-Content $_.FullName -Raw -Encoding UTF8
        if ($content.StartsWith("﻿")) {
            $content = $content.Substring(1)
            $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($_.FullName, $content, $utf8WithoutBom)
            Write-Host "修复: $($_.Name)" -ForegroundColor Green
        }
    } catch {
        Write-Warning "处理失败: $($_.FullName) - $($_.Exception.Message)"
    }
}

Write-Host "=== 修复完成 ===" -ForegroundColor Green