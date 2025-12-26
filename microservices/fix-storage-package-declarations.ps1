# 修复storage模块package声明

Write-Host "=== 修复storage模块package声明 ===" -ForegroundColor Cyan

Get-ChildItem -Path "D:\IOE-DREAM\microservices\microservices-common-storage" -Recurse -Filter "*.java" | ForEach-Object {
    try {
        $content = Get-Content $_.FullName -Raw -Encoding UTF8

        # 修复截断的package声明
        $content = $content -replace '^ckage ', 'package '
        $content = $content -replace '^iimport ', 'import '

        # 写回文件
        $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText($_.FullName, $content, $utf8WithoutBom)
        Write-Host "修复: $($_.Name)" -ForegroundColor Green
    } catch {
        Write-Warning "处理失败: $($_.FullName) - $($_.Exception.Message)"
    }
}

Write-Host "=== 修复完成 ===" -ForegroundColor Green